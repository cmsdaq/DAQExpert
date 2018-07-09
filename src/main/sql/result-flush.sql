-- this script is to flush the data in a given timestamp. It's useful to prepare database for reruns.
DECLARE
    clean_from          TIMESTAMP := TIMESTAMP '2018-06-01 00:00:00'; -- localtime
    clean_to            TIMESTAMP := TIMESTAMP '2018-06-14 02:00:00'; -- localtime
    free_id             NUMBER;
    removed             NUMBER := 0;
    altered             NUMBER := 0;
    splited             NUMBER := 0;
    to_remove           NUMBER := 0;
    to_split            NUMBER := 0;
    l_message           VARCHAR2(100) := 'This script removes data from Conditions table!';
    o_id                condition.id%TYPE;
    o_title             condition.title%TYPE;
    o_description       condition.description%TYPE;
    o_group             condition.group_name%TYPE;
    o_logic             condition.logic_module%TYPE;
    o_priority          condition.priority%TYPE;
    o_start             condition.start_date%TYPE;
    o_end               condition.end_date%TYPE;
    o_duration          condition.duration%TYPE;
    new_duration        condition.duration%TYPE;
    TYPE id_list IS
        VARRAY ( 100 ) OF condition_context.id%TYPE;
    context_to_delete   id_list := id_list ();
    clean_duration      condition.duration%TYPE;


    CURSOR overlapping IS

        SELECT
            id, title, description, group_name,logic_module, priority, start_date, end_date, duration
        FROM
            condition
        WHERE
            start_date < clean_to
            AND end_date > clean_from
        ORDER BY
            start_date
        FOR UPDATE;

BEGIN

    clean_duration :=
        extract ( DAY FROM ( clean_to - clean_from ) ) * 24 * 60 * 60 * 1000
        + extract ( HOUR FROM ( clean_to - clean_from ) ) * 60 * 60 * 1000
        + extract ( MINUTE FROM ( clean_to - clean_from ) ) * 60 * 1000
        + extract ( SECOND FROM ( clean_to - clean_from ) ) * 1000;

    dbms_output.put_line('Removing data for timestamp of duration: ' || clean_duration);


    IF
        clean_duration > 0
    THEN


  -- 1) First step is to check the procedure can be executed on the given timespan.
  -- Number of removed conditions must be greater than number or splited.
  -- That's because ids released after removing will be used to splitted entries


  -- 1.a count entries to remove
        OPEN overlapping;
        LOOP
            FETCH overlapping INTO
                o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;

            EXIT WHEN overlapping%notfound;
            IF
                o_start > clean_from
                AND o_end < clean_to
            THEN
                to_remove := to_remove + 1;
            END IF;

        END LOOP;
        CLOSE overlapping;


  -- 1.b count entries to split
        OPEN overlapping;
        LOOP
            FETCH overlapping INTO
                o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;

            EXIT WHEN overlapping%notfound;
            IF
                o_start < clean_from
                AND o_end > clean_to
            THEN
                to_split := to_split + 1;
            END IF;

        END LOOP;
        CLOSE overlapping;


        IF
            to_remove >= to_split
        THEN


  -- 2) Second step is to execute the procedure.
  -- At this point there is enough id to be released to use for entries to be split


  -- 2a) delete data from point table
            DELETE FROM point
            WHERE
                clean_from < x
                AND x < clean_to;



            DELETE FROM removed_conditions;



  -- 2b) remove entries that do not need to be splitted - those that fall into requested time span without overlapping
            OPEN overlapping;
            dbms_output.put_line('Entres to remove:');
            LOOP
                FETCH overlapping INTO
                    o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;
                EXIT WHEN overlapping%notfound;

                IF
                    o_start > clean_from AND o_end < clean_to
                THEN
        --dbms_output.put_line( o_start || ' ' || o_group || ' ' || o_title);
                    INSERT INTO removed_conditions ( condition_id ) VALUES ( o_id );

                    DELETE FROM action
                    WHERE
                        condition_id = o_id;

                    FOR i IN (
                        SELECT
                            context_id
                        FROM
                            condition_context_map
                        WHERE
                            condition_id = o_id
                    ) LOOP
                        dbms_output.put_line('Deleting context id:' || i.context_id);
                        DELETE FROM condition_context_map
                        WHERE
                            context_id = i.context_id;

                        DELETE FROM condition_context_statistic
                        WHERE
                            id = i.context_id;

                        DELETE FROM condition_context_object_value
                        WHERE
                            context_object_id = i.context_id;

                        DELETE FROM condition_context_object
                        WHERE
                            id = i.context_id;

                        DELETE FROM condition_context
                        WHERE
                            id = i.context_id;

                    END LOOP;

        --DELETE FROM condition_context WHERE condition_id = o_id ;

                    DELETE FROM condition
                    WHERE
                        CURRENT OF overlapping;
                    removed := removed + 1;

                END IF;
            END LOOP;
            CLOSE overlapping;



  -- 2c) update entries that overlap with end of timespan requested - the start_date and duration will be updated
            OPEN overlapping;
            dbms_output.put_line('Entres to shorten beginning:');
            LOOP
                FETCH overlapping INTO
                    o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;

                EXIT WHEN overlapping%notfound;
                IF
                    o_start > clean_from
                    AND o_end > clean_to
                THEN
                    new_duration := extract ( DAY FROM ( o_end - clean_to ) ) * 24 * 60 * 60 * 1000
                    + extract ( HOUR FROM ( o_end - clean_to ) ) * 60 * 60 * 1000
                    + extract ( MINUTE FROM ( o_end - clean_to ) ) * 60 * 1000
                    + extract ( SECOND FROM ( o_end - clean_to ) ) * 1000;


                    dbms_output.put_line('Duration updated from: ' || o_duration || ' to:' || new_duration);

                    dbms_output.put_line(o_start || ' ' || o_group || ' ' || o_title);

                    UPDATE condition
                    SET
                        start_date = clean_to,
                        duration = new_duration
                    WHERE
                        CURRENT OF overlapping;

                    altered := altered + 1;
                END IF;
            END LOOP;
            CLOSE overlapping;


  -- 2d) update entries that overlap with start of timespan requested - the end_date and duration will be updated
            OPEN overlapping;
            dbms_output.put_line('Entres to shorten end:');
            LOOP
                FETCH overlapping INTO
                    o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;

                EXIT WHEN overlapping%notfound;
                IF
                    o_start < clean_from AND o_end < clean_to
                THEN
                    new_duration := extract ( DAY FROM ( clean_from - o_start ) ) * 24 * 60 * 60 * 1000
                    + extract ( HOUR FROM ( clean_from - o_start ) ) * 60 * 60 * 1000
                    + extract ( MINUTE FROM ( clean_from - o_start ) ) * 60 * 1000
                    + extract ( SECOND FROM ( clean_from - o_start ) ) * 1000;


                    dbms_output.put_line('Duration updated from: ' || o_duration || ' to:' || new_duration);
                    dbms_output.put_line(o_start || ' ' || o_group || ' ' || o_title);

                    UPDATE condition
                    SET
                        end_date = clean_from,
                        duration = new_duration
                    WHERE
                        CURRENT OF overlapping;

                    altered := altered + 1;
                END IF;
            END LOOP;
            CLOSE overlapping;


  -- 2e) update entries that overlap with start and end of timespan requested - the items will be splitted into two
            OPEN overlapping;
            dbms_output.put_line('Entres to split:');
            LOOP
                FETCH overlapping INTO
                    o_id, o_title, o_description, o_group, o_logic, o_priority, o_start, o_end, o_duration;

                EXIT WHEN overlapping%notfound;

                IF
                    o_start < clean_from AND o_end > clean_to
                THEN
                    dbms_output.put_line(o_start || ' ' || o_group || ' ' || o_title);

                    SELECT
                        MIN(condition_id)
                    INTO
                        free_id
                    FROM
                        removed_conditions;

                    DELETE FROM removed_conditions
                    WHERE
                        condition_id = free_id;

                    dbms_output.put_line('Splitting resulted with new condition with id: ' || free_id);

                    -- calculate new duration for LEFT splitted entry
                    new_duration := extract ( DAY FROM ( clean_from - o_start ) ) * 24 * 60 * 60 * 1000
                    + extract ( HOUR FROM ( clean_from - o_start ) ) * 60 * 60 * 1000
                    + extract ( MINUTE FROM ( clean_from - o_start ) ) * 60 * 1000
                    + extract ( SECOND FROM ( clean_from - o_start ) ) * 1000;

                    dbms_output.put_line('LEFT splitted entry: org duration: '|| o_duration|| ' after modification: ' || new_duration);

                    -- update end_date and duration for the left part of splitted entry
                    UPDATE condition
                    SET
                        end_date = clean_from,
                        duration = new_duration
                    WHERE
                        CURRENT OF overlapping;

                    -- calculate new duration for RIGHT splitted entry
                    new_duration := extract ( DAY FROM ( o_end - clean_to ) ) * 24 * 60 * 60 * 1000
                    + extract ( HOUR FROM ( o_end - clean_to ) ) * 60 * 60 * 1000
                    + extract ( MINUTE FROM ( o_end - clean_to ) ) * 60 * 1000
                    + extract ( SECOND FROM ( o_end - clean_to ) ) * 1000;

                    dbms_output.put_line('RIGHT splitted entry: org duration: ' || o_duration || ' after modification: ' || new_duration);


        -- insert the RIGHT splitted entry reusing ID taken from removed entry
                    INSERT INTO condition (
                        id, title, description, group_name, logic_module, priority, start_date, end_date, duration, mature
                    ) VALUES (
                        free_id, o_title, o_description, o_group, o_logic, o_priority, clean_to, o_end, new_duration, 1);

                    splited := splited + 1;
                END IF;
            END LOOP;
            CLOSE overlapping;



            dbms_output.put_line('Summary: removed='
                                   || removed
                                   || ', altered='
                                   || altered
                                   || ', splited='
                                   || splited);

            COMMIT;
        ELSE
            dbms_output.put_line('Could not perform the procedure, not enough entries will be removed ('
                                   || to_remove
                                   || ') to handle splitting of '
                                   || to_split
                                   || ' entries. Increase timespan');
        END IF;

    ELSE
        dbms_output.put_line('Could not perform the procedure, wrong parameters, clean from should be before clean to, duration should be greater than 0 ('
|| clean_duration);
    END IF;

END;
-- this script is to generate histograms of recovery times (reaction times or intervention times?)
DECLARE
    stat_from                       TIMESTAMP := TIMESTAMP '2018-01-01 14:00:00'; -- localtime
    stat_to                         TIMESTAMP := TIMESTAMP '2018-09-01 23:00:00'; -- localtime
    outside_of_stable_beams         BOOLEAN := FALSE;




  -- LM codes
    dataflow_stuck_code             CONSTANT PLS_INTEGER := 14;
    no_rate_code                    CONSTANT PLS_INTEGER := 0;
    level_zero_state_code           CONSTANT PLS_INTEGER := 28;
    stable_beams_code               CONSTANT PLS_INTEGER := 13;


  -- lists to store data for histogram
    TYPE int_list IS
        VARRAY ( 1000 ) OF condition.duration%TYPE;
    recovery_duration               int_list := int_list ();
    recovery_duration_i             PLS_INTEGER := 0;
    recovery_automated_duration     int_list := int_list ();
    recovery_automated_duration_i   PLS_INTEGER := 0;
    recovery_manual_duration        int_list := int_list ();
    recovery_manual_duration_i      PLS_INTEGER := 0;

  -- tmp variables
    exists_rcms_recovery            PLS_INTEGER := 0;

    auto_rec_available_entries      PLS_INTEGER := 0;
    automated_recovery_available    BOOLEAN := FALSE;


    recovery_used_entries           PLS_INTEGER := 0;
    recovery_used                   BOOLEAN := FALSE;


    ttchr_next_entries              PLS_INTEGER := 0;
    ttchr_next                      BOOLEAN := FALSE;

    same_predecesor_entries         PLS_INTEGER := 0;
    same_predecesor                 BOOLEAN := FALSE;


    exist_unidentified_entries      PLS_INTEGER := 0;
    exist_unidentified              BOOLEAN := FALSE;

    exist_rec_steps_entries         PLS_INTEGER := 0;
    exist_rec_steps                 BOOLEAN := FALSE;


    stable_beams_entries            PLS_INTEGER := 0;
    stable_beams                    BOOLEAN := FALSE;


    resync_or_hard_reset_entries    PLS_INTEGER := 0;
    resync_or_hard_reset            BOOLEAN := FALSE;


    no_rate_before_entries          PLS_INTEGER := 0;

    -- this is probably subsequent step
    probably_subsequent             BOOLEAN := FALSE;


    type message_list is varray (1000) of Condition.description%type;
    ignored_entries message_list := message_list();
    ignored_entries_i PLS_INTEGER := 0;

BEGIN
    dbms_output.put_line('Producing result for ' || TO_CHAR(stat_from, 'yyyy-mm-dd"T"hh24:mi:ss') || '-' || TO_CHAR(stat_to, 'yyyy-mm-dd"T"hh24:mi:ss'));

    dbms_output.put_line('link; start; stable beams; identified; automated available; automated used; duration');

    FOR i IN (
        SELECT
            id, title, description, group_name, logic_module, priority, start_date, end_date, duration
        FROM condition
        WHERE
            logic_module = dataflow_stuck_code
            AND mature = 1
            AND start_date > stat_from
            AND end_date < stat_to
        ORDER BY start_date
    ) LOOP


        SELECT
            COUNT(*)
        INTO exists_rcms_recovery
        FROM condition
        WHERE
            start_date >= ( i.end_date - 4 / 86400 )
            AND start_date <= ( i.end_date + 4 / 86400 )
            AND logic_module = level_zero_state_code
            AND title LIKE 'FixingSoftError';

        --- check if stable beams
        SELECT COUNT(*)
        INTO stable_beams_entries
        FROM condition
        WHERE
            start_date <= ( i.start_date )
            AND end_date >= ( i.end_date )
            AND logic_module = stable_beams_code;

        stable_beams := FALSE;
        IF stable_beams_entries > 0
        THEN stable_beams := TRUE;END IF;



        -- this is not RCMS automated recovery (Fixing soft error, dcs pause resume)
        IF exists_rcms_recovery = 0 and (outside_of_stable_beams or stable_beams)
        THEN

            --- check if automated recovery available
            SELECT COUNT(*)
            INTO auto_rec_available_entries
            FROM recovery_record
            WHERE
                start_date >= ( i.start_date )
                AND start_date <= ( i.end_date )
                AND name LIKE 'Recovery of%';

            automated_recovery_available := FALSE;
            IF auto_rec_available_entries > 0
            THEN automated_recovery_available := TRUE;END IF;


            --- check if specific type of recovery in first step
            SELECT COUNT(*)
            INTO resync_or_hard_reset_entries
            FROM condition tc inner join action ta
            on tc.id = ta.condition_id
            WHERE
                tc.start_date >= ( i.start_date )
                AND tc.start_date <= ( i.end_date )

                and (
                    UPPER(ta.action) like '%HARDRESET%'
                    or
                    UPPER(ta.action) like '%TTCRESYNC%'
                );

            resync_or_hard_reset := FALSE;
            IF resync_or_hard_reset_entries > 0
            THEN resync_or_hard_reset := TRUE;END IF;


            -- check if recovery was used
            SELECT COUNT(*)
            INTO recovery_used_entries
            FROM recovery_record
            WHERE
                start_date >= ( i.start_date )
                AND start_date <= ( i.end_date )
                AND name LIKE 'Executing%';

            recovery_used := FALSE;
            IF recovery_used_entries > 0
            THEN recovery_used := TRUE;END IF;



            -- check if next was TTCHReset
            SELECT COUNT(*)
            INTO ttchr_next_entries
            FROM condition
            WHERE
                start_date >= ( i.end_date - 4 / 86400 )
                AND start_date <= ( i.end_date + 20 / 86400 )
                and logic_module=28
                and (title like 'TTCHardResetting%' or title like 'TTCResync%' or title like 'Paused%' or title like 'Resuming%');

            ttchr_next := FALSE;
            IF ttchr_next_entries > 0
            THEN ttchr_next := TRUE;END IF;

            -- check if problem was unidentified
            SELECT COUNT(*)
            INTO exist_unidentified_entries
            FROM condition
            WHERE
                start_date >= i.start_date
                and end_date <= i.end_date
                and title like 'Unidentified problem'
                and mature = 1;
            exist_unidentified := FALSE;
            IF exist_unidentified_entries > 0
            THEN exist_unidentified := TRUE;END IF;


            SELECT count(*) INTO same_predecesor_entries
            FROM Condition
            WHERE
                end_date >= (i.start_date - 1/1440)
                AND end_date <= i.start_date
                AND logic_module=i.logic_module
                AND title = i.title;

            --- check if there is smooth running 5 min before
            SELECT count(*) INTO no_rate_before_entries
            FROM Condition
            WHERE
                end_date >= (i.start_date - 5/1440)
                AND start_date <= ( i.start_date -1/86400)
                AND logic_module=no_rate_code;

            probably_subsequent := FALSE;
            IF i.duration < 2000 or same_predecesor_entries > 0 then
                probably_subsequent := TRUE;
                ignored_entries_i := ignored_entries_i + 1;
                ignored_entries.extend;
                ignored_entries(ignored_entries_i) :=
                'http://daq-expert.cms/DAQExpert/?start=' || TO_CHAR(i.start_date, 'yyyy-mm-dd"T"hh24:mi:ss') || '&'||'end=' || TO_CHAR(i.end_date, 'yyyy-mm-dd"T"hh24:mi:ss') || ' ; '
                || TO_CHAR(i.start_date, 'yyyy-mm-dd"T"hh24:mi:ss') || ' ; '
                || sys.diutil.bool_to_int(automated_recovery_available) || ' ; '
                || sys.diutil.bool_to_int(recovery_used) || ' ; '
                || i.duration || ' ; '
                || sys.diutil.bool_to_int(probably_subsequent) || ' ; '
                || sys.diutil.bool_to_int(ttchr_next) || ' ; ';

            END IF;

            -- limit calls to following block (same condition as a main except of what's calculated here)
            IF not ttchr_next AND not probably_subsequent and not exist_unidentified THEN

                 -- check if we offer recovery steps for shifter
                SELECT COUNT(*)
                INTO exist_rec_steps_entries
                FROM condition c
                WHERE
                    start_date >= i.start_date
                    and end_date <= i.end_date
                    and (select count(*) from action where condition_id = c.id) > 0;

                exist_rec_steps := FALSE;
                IF exist_rec_steps_entries > 0
                THEN exist_rec_steps := TRUE;END IF;

            END IF;


            IF --not ttchr_next AND
            not probably_subsequent and
            no_rate_before_entries = 0 THEN

                dbms_output.put_line(
                    'http://daq-expert.cms/DAQExpert/?start=' || TO_CHAR(i.start_date, 'yyyy-mm-dd"T"hh24:mi:ss') || '&'||'end=' || TO_CHAR(i.end_date, 'yyyy-mm-dd"T"hh24:mi:ss') || ' ; '
                    || TO_CHAR(i.start_date, 'yyyy-mm-dd"T"hh24:mi:ss') || ' ; '
                    || sys.diutil.bool_to_int(stable_beams) || ' ; '
                    || sys.diutil.bool_to_int(not exist_unidentified) || ' ; '
                    || sys.diutil.bool_to_int(automated_recovery_available) || ' ; '
                    || sys.diutil.bool_to_int(recovery_used) || ' ; '
                    || i.duration
                    || ' ; ' || sys.diutil.bool_to_int(resync_or_hard_reset)
                    --|| ' ; '|| sys.diutil.bool_to_int(probably_subsequent) || ' ; '
                    --|| sys.diutil.bool_to_int(ttchr_next) || ' ; '

                    );

                IF recovery_used THEN

                recovery_automated_duration_i := recovery_automated_duration_i + 1;
                recovery_automated_duration.extend;
                recovery_automated_duration(recovery_automated_duration_i) := i.duration;

                ELSE
                recovery_manual_duration_i := recovery_manual_duration_i + 1;
                recovery_manual_duration.extend;
                recovery_manual_duration(recovery_manual_duration_i) := i.duration;
                END IF;


            END IF;





        END IF;
        --END LOOP;

    END LOOP;
  --END LOOP;


    dbms_output.put_line('Recoveries');
    dbms_output.put_line('manual recoveries   : ' || recovery_manual_duration_i);
    dbms_output.put_line('automatic recoveries: ' || recovery_automated_duration_i);
    dbms_output.put_line('Manual report');


END;
/
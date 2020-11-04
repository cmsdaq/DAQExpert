BEGIN
    FOR r IN (
        SELECT owner, table_name
        FROM all_tables
        WHERE owner = 'CMS_DAQ_EXPERT'
    )
    LOOP
        EXECUTE IMMEDIATE
            'GRANT SELECT ON '||r.owner||'.'||r.table_name||' to ' || 'CMS_DAQ_EXPERT_R';
    END LOOP;
END;

BEGIN
    FOR r IN (
        SELECT owner, table_name
        FROM all_tables
        WHERE owner = 'CMS_DAQ_EXPERT'
    )
    LOOP
        EXECUTE IMMEDIATE
            'GRANT select, insert, update, delete ON '||r.owner||'.'||r.table_name||' to ' || 'CMS_DAQ_EXPERT_W';
    END LOOP;
END;

BEGIN
    FOR r IN (
        SELECT owner, table_name
        FROM all_tables
        WHERE owner = 'CMS_DAQ_EXPERT'
    )
    LOOP
        EXECUTE IMMEDIATE
            'create synonym CMS_DAQ_EXPERT_W.'||r.table_name||'  for '||r.owner||'.'||r.table_name ;
    END LOOP;
END;

BEGIN
    FOR r IN (
        SELECT owner, table_name
        FROM all_tables
        WHERE owner = 'CMS_DAQ_EXPERT'
    )
    LOOP
        EXECUTE IMMEDIATE
            'create synonym CMS_DAQ_EXPERT_R.'||r.table_name||'  for '||r.owner||'.'||r.table_name ;
    END LOOP;
END;

SELECT * FROM USER_TAB_PRIVS;

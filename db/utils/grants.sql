--liquibase formatted sql
--changeset artemov_i:grants dbms:postgresql runAlways:true runOnChange:true splitStatements:false stripComments:false
--гранты на использование

GRANT USAGE ON SCHEMA ${user.table} TO ${user.connect};

GRANT USAGE ON SCHEMA ${ext} TO ${user.connect};

GRANT USAGE ON SCHEMA ${ext} TO ${user.update};

GRANT USAGE ON SCHEMA ${user.table} TO ${user.update};


--гранты на выполнение
CREATE SCHEMA pkg_patcher AUTHORIZATION ${user.update};
CREATE FUNCTION pkg_patcher.p_change_role_connect_user(pv_connect_user VARCHAR, pv_table_schema VARCHAR) RETURNS void
    LANGUAGE plpgsql
    SET search_path TO '${user.table}', 'pkg_patcher', 'public'
    AS $$
declare
  rec record;
begin
  for rec in (select 'GRANT USAGE ON SCHEMA ' || nspname || ' TO ' || pv_connect_user || ';' as alter_cmd 
    from pg_catalog.pg_namespace 
    where lower(nspname) like 'pkg_json_%') loop
    EXECUTE rec.alter_cmd;
  end loop;
  
  EXECUTE format('GRANT USAGE ON SCHEMA %s TO %s', pv_table_schema, pv_connect_user);

  for rec in (select
    'GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE ' || schemaname || '.' || tablename || ' TO ' || pv_connect_user || ';' as alter_cmd 
    from
        pg_catalog.pg_tables
    where
    lower(schemaname) = lower(pv_table_schema)) loop
    EXECUTE rec.alter_cmd;
  end loop;

  for rec in (select
    'GRANT EXECUTE ON FUNCTION ' || nsp.nspname || '.' || p.proname || '(' || pg_get_function_identity_arguments(p.oid)|| ') TO ' || pv_connect_user || ';' as alter_cmd 
    from
        pg_proc p
    join pg_namespace nsp on
        p.pronamespace = nsp.oid
    where
        lower(nsp.nspname) like 'pkg_json_%') loop
    EXECUTE rec.alter_cmd;
  end loop;
end;
$$;

CREATE FUNCTION pkg_patcher.p_change_role_update_user(pv_update_user VARCHAR, pv_table_schema VARCHAR) RETURNS void
    LANGUAGE plpgsql
    SET search_path TO '${user.table}', 'pkg_patcher', 'public'
    AS $$
declare
  rec record;
begin
  for rec in (select 'ALTER SCHEMA ' || nspname || ' OWNER TO ' || pv_update_user || ';' as alter_cmd 
    from pg_catalog.pg_namespace 
    where lower(nspname) like 'pkg_%') loop
    EXECUTE rec.alter_cmd;
  end loop;

  EXECUTE format('GRANT USAGE ON SCHEMA %s TO %s', pv_table_schema, pv_update_user);

  for rec in (select
    'GRANT INSERT, SELECT, UPDATE, DELETE ON TABLE ' || schemaname || '.' || tablename || ' TO ' || pv_update_user || ';' as alter_cmd 
    from
        pg_catalog.pg_tables
    where
    lower(schemaname) = lower(pv_table_schema)) loop
    EXECUTE rec.alter_cmd;
  end loop;

  for rec in (select
    'ALTER FUNCTION ' || nsp.nspname || '.' || p.proname || '(' || pg_get_function_identity_arguments(p.oid)|| ') OWNER TO ' || pv_update_user || ';'  as alter_cmd
    from
        pg_proc p
    join pg_namespace nsp on
        p.pronamespace = nsp.oid
    where
        lower(nsp.nspname) like 'pkg_%') loop
    EXECUTE rec.alter_cmd;
  end loop;
end;
$$;


SELECT pkg_patcher.p_change_role_connect_user('${user.connect}', '${user.table}');
SELECT pkg_patcher.p_change_role_update_user('${user.update}', '${user.table}');

DROP SCHEMA IF EXISTS pkg_patcher cascade;
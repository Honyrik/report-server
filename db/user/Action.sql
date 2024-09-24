--liquibase formatted sql
--changeset patcher-core:Action dbms:postgresql runOnChange:true splitStatements:false stripComments:false
INSERT INTO s_at.t_action (ck_id, cv_name, cv_description, ck_user, ct_change) VALUES(20000, 'Доступ на просмотр "Отчетная система"', 'Доступ на просмотр  "Отчетная система"', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-24T12:19:49.425+0300') on conflict (ck_id) do update set ck_id = excluded.ck_id, cv_name = excluded.cv_name, cv_description = excluded.cv_description, ck_user = excluded.ck_user, ct_change = excluded.ct_change;
INSERT INTO s_at.t_action (ck_id, cv_name, cv_description, ck_user, ct_change) VALUES(20001, 'Доступ на модификацию  "Отчетная система"', 'Доступ на модификацию  "Отчетная система"', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-24T12:20:00.349+0300') on conflict (ck_id) do update set ck_id = excluded.ck_id, cv_name = excluded.cv_name, cv_description = excluded.cv_description, ck_user = excluded.ck_user, ct_change = excluded.ct_change;
INSERT INTO s_at.t_role_action (ck_id, ck_action, ck_role, ck_user, ct_change)
    select t.ck_id, t.ck_action, t.ck_role, t.ck_user, t.ct_change::timestamp from (
        select ('23bd3a02-d794-49e4-af8c-f87313bc8e75')::uuid as ck_id, (20001)::bigint as ck_action, ('cf7e8049-27cc-4c57-b9ea-cf8131a88a41')::uuid as ck_role, '4fd05ca9-3a9e-4d66-82df-886dfa082113' as ck_user, '2024-09-04T13:46:09.304+0300' as ct_change
        union all
        select ('699d3094-6bc9-4fc0-9fe9-9d0d43169589')::uuid as ck_id, (20000)::bigint as ck_action, ('cf7e8049-27cc-4c57-b9ea-cf8131a88a41')::uuid as ck_role, '4fd05ca9-3a9e-4d66-82df-886dfa082113' as ck_user, '2024-09-04T13:46:09.304+0300' as ct_change
 ) as t 
 join s_at.t_role r
 on t.ck_role = r.ck_id
on conflict on constraint cin_u_role_action_1 do update set ck_id = excluded.ck_id, ck_action = excluded.ck_action, ck_role = excluded.ck_role, ck_user = excluded.ck_user, ct_change = excluded.ct_change;

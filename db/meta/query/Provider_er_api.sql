--liquibase formatted sql
--changeset patcher-core:Provider_er_api dbms:postgresql runOnChange:true splitStatements:false stripComments:false
INSERT INTO s_mt.t_provider (ck_id, cv_name, ck_user, ct_change) VALUES('er_api', 'Отчетная система', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-04T13:27:12.878+0300') on conflict (ck_id) do update set ck_id = excluded.ck_id, cv_name = excluded.cv_name, ck_user = excluded.ck_user, ct_change = excluded.ct_change;

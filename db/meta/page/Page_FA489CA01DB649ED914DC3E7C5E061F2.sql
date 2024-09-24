--liquibase formatted sql
--changeset patcher-core:Page_FA489CA01DB649ED914DC3E7C5E061F2 dbms:postgresql runOnChange:true splitStatements:false stripComments:false
delete from s_mt.t_page_attr where ck_page='FA489CA01DB649ED914DC3E7C5E061F2';
INSERT INTO s_mt.t_page (ck_id, ck_parent, cr_type, cv_name, cn_order, cl_static, cv_url, ck_icon, ck_user, ct_change, cl_menu, ck_view, cv_redirect_url, cl_multi) VALUES('FA489CA01DB649ED914DC3E7C5E061F2', '4', 1, '497545b9e46f4e99aabc483a67e33db9', 40, 0, null, null, '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2019-12-19T03:00:00.000+0300', 1, 'system', null, 0) on conflict (ck_id) do update set ck_parent = excluded.ck_parent, cr_type = excluded.cr_type, cv_name = excluded.cv_name, cn_order = excluded.cn_order, cl_static = excluded.cl_static, cv_url = excluded.cv_url, ck_icon = excluded.ck_icon, ck_user = excluded.ck_user, ct_change = excluded.ct_change, cl_menu = excluded.cl_menu, ck_view = excluded.ck_view, cl_multi = excluded.cl_multi, cv_redirect_url = excluded.cv_redirect_url;
INSERT INTO s_mt.t_localization (ck_id, ck_d_lang, cr_namespace, cv_value, ck_user, ct_change)
select t.ck_id, t.ck_d_lang, t.cr_namespace, t.cv_value, t.ck_user, t.ct_change::timestamp from (
    select '497545b9e46f4e99aabc483a67e33db9' as ck_id, 'ru_RU' as ck_d_lang, 'meta' as cr_namespace, 'Патчинг' as cv_value, '4fd05ca9-3a9e-4d66-82df-886dfa082113' as ck_user, '2019-12-19T00:00:00.000+0300' as ct_change
) as t 
 join s_mt.t_d_lang dl
 on t.ck_d_lang = dl.ck_id
on conflict on constraint cin_u_localization_1 do update set ck_id = excluded.ck_id, ck_d_lang = excluded.ck_d_lang, cr_namespace = excluded.cr_namespace, cv_value = excluded.cv_value, ck_user = excluded.ck_user, ct_change = excluded.ct_change;
--liquibase formatted sql
--changeset patcher-core:Page_15B76089494D4F589B6B707EF0BD8B29 dbms:postgresql runOnChange:true splitStatements:false stripComments:false
delete from s_mt.t_page_attr where ck_page='15B76089494D4F589B6B707EF0BD8B29';
INSERT INTO s_mt.t_page (ck_id, ck_parent, cr_type, cv_name, cn_order, cl_static, cv_url, ck_icon, ck_user, ct_change, cl_menu, ck_view, cv_redirect_url, cl_multi) VALUES('15B76089494D4F589B6B707EF0BD8B29', '4', 1, '9f69260990174480be6a88f26af1c3b8', 60, 0, null, 'ED68E55CD4824B1C9469C7B6DD9ED133', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23T18:56:31.516+0300', 1, 'system', null, 0) on conflict (ck_id) do update set ck_parent = excluded.ck_parent, cr_type = excluded.cr_type, cv_name = excluded.cv_name, cn_order = excluded.cn_order, cl_static = excluded.cl_static, cv_url = excluded.cv_url, ck_icon = excluded.ck_icon, ck_user = excluded.ck_user, ct_change = excluded.ct_change, cl_menu = excluded.cl_menu, ck_view = excluded.ck_view, cl_multi = excluded.cl_multi, cv_redirect_url = excluded.cv_redirect_url;
INSERT INTO s_mt.t_localization (ck_id, ck_d_lang, cr_namespace, cv_value, ck_user, ct_change)
select t.ck_id, t.ck_d_lang, t.cr_namespace, t.cv_value, t.ck_user, t.ct_change::timestamp from (
    select '9f69260990174480be6a88f26af1c3b8' as ck_id, 'ru_RU' as ck_d_lang, 'meta' as cr_namespace, 'Отчетная система' as cv_value, '4fd05ca9-3a9e-4d66-82df-886dfa082113' as ck_user, '2020-10-29T05:03:05.988+0300' as ct_change
) as t 
 join s_mt.t_d_lang dl
 on t.ck_d_lang = dl.ck_id
on conflict on constraint cin_u_localization_1 do update set ck_id = excluded.ck_id, ck_d_lang = excluded.ck_d_lang, cr_namespace = excluded.cr_namespace, cv_value = excluded.cv_value, ck_user = excluded.ck_user, ct_change = excluded.ct_change;
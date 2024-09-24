--liquibase formatted sql
--changeset patcher-core:ERJGetDPluginSource dbms:postgresql runOnChange:true splitStatements:false stripComments:false
INSERT INTO s_mt.t_query (ck_id, ck_provider, ck_user, ct_change, cr_type, cr_access, cn_action, cv_description, cc_query)
 VALUES('ERJGetDPluginSource', 'er_api', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-21T14:38:43.064+0300', 'dml', 'po_session', null, 'Список плагинов источников данных',
 'v = {
    method: ''GET'',
    url: `${jt_provider_params.defaultGateUrl}/admin/dictionary/plugin/source?session=${jt_in_param.sess_session}`,
    resultParse: "jt_result.success?jt_result.data:{jt_message:jt_result.message}",
}'
) on conflict (ck_id) do update set cc_query = excluded.cc_query, ck_provider = excluded.ck_provider, ck_user = excluded.ck_user, ct_change = excluded.ct_change, cr_type = excluded.cr_type, cr_access = excluded.cr_access, cn_action = excluded.cn_action, cv_description = excluded.cv_description;

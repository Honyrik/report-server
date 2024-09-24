--liquibase formatted sql
--changeset patcher-core:ERJUploadAsset dbms:postgresql runOnChange:true splitStatements:false stripComments:false
INSERT INTO s_mt.t_query (ck_id, ck_provider, ck_user, ct_change, cr_type, cr_access, cn_action, cv_description, cc_query)
 VALUES('ERJUploadAsset', 'er_api', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-04T20:39:42.231+0300', 'dml', 'po_session', null, 'Список ресурсов',
 'v = {
    method: { I: "PUT", U: "PATCH", D: "DELETE" }[jt_in_param.json.service.cv_action],
    url: `${jt_provider_params.defaultGateUrl}/admin/Asset/upload?session=${jt_in_param.sess_session}`,
    formData: {
        json: Object.assign(jt_in_param.json.data.cct_data || jt_in_param.json.data, {
            cctParameter: lodash.isString(jt_in_param.json.data.cct_data.cctParameter)
                ? JSON.parse(jt_in_param.json.data.cct_data.cctParameter)
                : jt_in_param.json.data.cct_data.cctParameter
        }),
        upload: jt_in_param.upload_file,
    },
    resultParse: "jt_result.success?jt_result:{jt_message:jt_result.message}",
}'
) on conflict (ck_id) do update set cc_query = excluded.cc_query, ck_provider = excluded.ck_provider, ck_user = excluded.ck_user, ct_change = excluded.ct_change, cr_type = excluded.cr_type, cr_access = excluded.cr_access, cn_action = excluded.cn_action, cv_description = excluded.cv_description;

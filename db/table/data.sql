--liquibase formatted sql
--changeset artemov_i:init_report_data_schema dbms:postgresql splitStatements:false stripComments:false
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('INTERVAL_ONLINE', '15 day', 'Интервал хранения по умолчанию', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('INTERVAL_OFFLINE', '365 day', 'Интервал хранения по умолчанию', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_TYPE', 'DB', 'Место хранения
DB - локальной базе
DIR - в локальной папке
PLUGIN - плагин
AWS - s3 хранилище
MINIO - s3 хранилище
RIAKCS - s3 хранилище
', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_DIR', '{"path":"/tmp"}', 'Настройки', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_PLUGIN', '{"plugin":"ru.tehnobear.essence.share.store.DirStorage"}', 'Настройки', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_AWS', '{
"endpoint":"http://s3.amazonaws.com",
"bucket": "report",
"dir": "report",
"accessKey": "...",
"secretKey": "...",
"signerType": "AWS4SignerType"
}', 'Настройки', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_MINIO', '{
"bucket": "report",
"dir": "report",
"accessKey": "...",
"secretKey": "...",
"proxyHost": "127.0.0.1",
"proxyPort": 8080
}', 'Настройки', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);
INSERT INTO ${user.table}.t_d_global_setting
(ck_id, cv_value, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('STORAGE_RIAKCS', '{
"bucket": "report",
"dir": "report",
"accessKey": "...",
"secretKey": "...",
"proxyHost": "127.0.0.1",
"proxyPort": 8080
}', 'Настройки', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-23 21:33:42.800', '2024-09-23 21:33:42.800', 0);

INSERT INTO ${user.table}.t_authorization
(ck_id, cv_name, cv_plugin, cct_parameter, ck_user, ct_change, ct_create, cl_deleted)
VALUES('673bc666-cd58-4706-a7ad-21e639cf32c7'::uuid, 'Anonymous', 'ru.tehnobear.essence.receiver.authorization.NoAuthorization', '{}', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 12:40:34.118', '2024-09-05 12:40:34.118', 0);
INSERT INTO ${user.table}.t_authorization
(ck_id, cv_name, cv_plugin, cct_parameter, ck_user, ct_change, ct_create, cl_deleted)
VALUES('683b27d2-0ae3-45a1-ac1b-aaace8e378f6'::uuid, 'Current', 'ru.tehnobear.essence.receiver.authorization.LocalAuthorization', '{}', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 11:13:24.327', '2024-09-05 11:13:24.327', 0);

INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('JRXML', 'XML JasperReports', '2024-09-02 17:45:57.264', '2024-09-02 17:45:57.264', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'application/xml', '.jrxml');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('QUERY', 'SQL Query', '2024-09-02 17:46:17.065', '2024-09-02 17:46:17.065', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'plain/text', '.sql');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('ARRAY', 'Json Array', '2024-09-02 17:47:12.561', '2024-09-02 17:47:12.561', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'application/json', '.json');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('OBJECT', 'Json Object', '2024-09-02 17:47:25.069', '2024-09-02 17:47:25.069', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'application/json', '.json');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('JR_REPORT_VIRTUALIZER', 'Jasper Virtualizer', '2024-09-08 19:44:32.170', '2024-09-08 19:44:32.170', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'plain/text', '.txt');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('BYNARY', 'BINARY', '2024-09-02 17:47:25.069', '2024-09-02 17:47:25.069', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'BINARY', 0, 'application/octet-stream', '.data');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('JPEG', 'JPEG', '2024-09-02 17:47:25.069', '2024-09-02 17:47:25.069', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'BINARY', 0, 'image/jpeg', '.jpg');
INSERT INTO ${user.table}.t_d_asset_type
(ck_id, cv_description, ct_create, ct_change, ck_user, cr_type, cl_deleted, cv_content_type, cv_extension)
VALUES('PLUGIN', 'Plugin', '2024-09-02 17:47:25.069', '2024-09-02 17:47:25.069', '4fd05ca9-3a9e-4d66-82df-886dfa082113', 'TEXT', 0, 'application/octet-stream', '.data');

INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('TEXT', 'Text', '.txt', 'plain/text', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRTEXTExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('HTML', 'Html', '.html', 'text/html', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRHTMLExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('PDF', 'Pdf', '.pdf', 'application/pdf', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRPDFExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('DOCX', 'Docx', '.docx', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRDOCXExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('XLSX', 'Xlsx', '.xlsx', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRXLSXExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('ODT', 'Odt', '.odt', 'application/vnd.oasis.opendocument.text', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRODTExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('ODS', 'Ods', '.ods', 'application/vnd.oasis.opendocument.spreadsheet', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRODSExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('XML', 'Xml', '.xml', 'application/xml', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRXMLExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('JSON', 'Json', '.json', 'application/json', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRJSONExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('CSV', 'Csv', '.csv', 'text/csv', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{"isWriteBOM":true,"fieldDelimiter":"\r\n"}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRCSVExporter');
INSERT INTO ${user.table}.t_d_format
(ck_id, cv_name, cv_extension, cv_content_type, ck_user, ct_change, ct_create, cl_deleted, cct_parameter, cr_type, cv_plugin)
VALUES('PPTX', 'Pptx', '.pptx', 'application/vnd.openxmlformats-officedocument.presentationml.presentation', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-05 15:31:54.916', '2024-09-05 15:31:54.916', 0, '{}', 'JRXML', 'ru.tehnobear.essence.report.jasper.format.JRPPTXExporter');

INSERT INTO ${user.table}.t_d_queue
(ck_id, cv_runner_url, ck_parent, ck_user, ct_change, ct_create, cl_deleted)
VALUES('default', NULL, NULL, '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 15:19:52.995', '2024-09-02 15:19:52.995', 0);

INSERT INTO ${user.table}.t_d_source_type
(ck_id, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('JDBC', 'JDBC источник данных', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 17:52:22.339', '2024-09-02 17:52:22.339', 0);
INSERT INTO ${user.table}.t_d_source_type
(ck_id, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('EMPTY', 'Пустой источник данных', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 17:52:52.866', '2024-09-02 17:52:52.866', 0);
INSERT INTO ${user.table}.t_d_source_type
(ck_id, cv_description, ck_user, ct_change, ct_create, cl_deleted)
VALUES('PLUGIN', 'Используется плагин', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-04 15:41:26.123', '2024-09-04 15:41:26.123', 0);

INSERT INTO ${user.table}.t_d_status
(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)
VALUES('NEW', 'Добавлен в очередь', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 21:14:31.908', '2024-09-02 18:14:31.908', 0);
INSERT INTO ${user.table}.t_d_status
(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)
VALUES('SUCCESS', 'Отчет готов', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 21:16:04.134', '2024-09-02 18:16:04.134', 0);
INSERT INTO ${user.table}.t_d_status
(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)
VALUES('FAULT', 'Ошибка формирования', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 21:16:25.153', '2024-09-02 18:16:25.153', 0);
INSERT INTO ${user.table}.t_d_status
(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)
VALUES('PROCESSING', 'Отчет на формировании', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 21:16:48.147', '2024-09-02 18:16:48.147', 0);
INSERT INTO ${user.table}.t_d_status
(ck_id, cv_name, ck_user, ct_change, ct_create, cl_deleted)
VALUES('DELETE', 'Отчет удален с хранения', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-02 21:17:20.041', '2024-09-02 18:17:20.041', 0);

INSERT INTO ${user.table}.t_source
(ck_id, cct_parameter, cv_plugin, ck_d_source, ck_user, ct_change, cl_enable, ct_create, cl_deleted, cv_script)
VALUES('empty_single', '{"count": 1}', NULL, 'EMPTY', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-04 15:51:53.865', 1, '2024-09-04 15:51:53.865', 0, NULL);
INSERT INTO ${user.table}.t_source
(ck_id, cct_parameter, cv_plugin, ck_d_source, ck_user, ct_change, cl_enable, ct_create, cl_deleted, cv_script)
VALUES('uspo', '{"schema": "${app.db.schema}", "jdbcUrl": "${app.db.jdbcUrl}", "password": "${app.db.password}", "username": "${app.db.username}", "driverClassName": "${app.db.driverClassName}"}', NULL, 'JDBC', '4fd05ca9-3a9e-4d66-82df-886dfa082113', '2024-09-18 14:59:15.715', 1, '2024-09-18 14:59:09.497', 0, NULL);


--liquibase formatted sql
--changeset artemov_i:init_report_schema dbms:postgresql splitStatements:false stripComments:false
CREATE SCHEMA ${user.table};
-- ${user.table}.t_authorization определение

-- Drop table

-- DROP TABLE ${user.table}.t_authorization;

CREATE TABLE ${user.table}.t_authorization (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	cv_name varchar(255) NOT NULL, -- Наименование
	cv_plugin varchar(2000) NOT NULL, -- Плагин
	cct_parameter text NOT NULL, -- Настройки
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время изменения
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_authorization_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_p_authorization PRIMARY KEY (ck_id)
);
CREATE UNIQUE INDEX cin_i_authorization ON ${user.table}.t_authorization USING btree (ck_id);
COMMENT ON TABLE ${user.table}.t_authorization IS 'Список систем авторизации';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_authorization.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_authorization.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_authorization.cv_plugin IS 'Плагин';
COMMENT ON COLUMN ${user.table}.t_authorization.cct_parameter IS 'Настройки';
COMMENT ON COLUMN ${user.table}.t_authorization.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_authorization.ct_change IS 'Время изменения';
COMMENT ON COLUMN ${user.table}.t_authorization.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_authorization.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_create_patch определение

-- Drop table

-- DROP TABLE ${user.table}.t_create_patch;

CREATE TABLE ${user.table}.t_create_patch (
	ck_id uuid NOT NULL, -- Идентификатор
	cv_file_name varchar(200) NOT NULL, -- Наименование файла
	ck_user varchar(100) NOT NULL, -- Аудит идентификатор пользователя
	ct_change timestamp NOT NULL, -- Аудит время модификации
	cct_parameter text NOT NULL, -- Параметры запуска
	ct_create timestamp NOT NULL, -- Дата сборки
	cn_size int8 NULL, -- Размер сборки
	CONSTRAINT cin_p_create_patch PRIMARY KEY (ck_id)
);

-- Column comments

COMMENT ON COLUMN ${user.table}.t_create_patch.ck_id IS 'Идентификатор';
COMMENT ON COLUMN ${user.table}.t_create_patch.cv_file_name IS 'Наименование файла';
COMMENT ON COLUMN ${user.table}.t_create_patch.ck_user IS 'Аудит идентификатор пользователя';
COMMENT ON COLUMN ${user.table}.t_create_patch.ct_change IS 'Аудит время модификации';
COMMENT ON COLUMN ${user.table}.t_create_patch.cct_parameter IS 'Параметры запуска';
COMMENT ON COLUMN ${user.table}.t_create_patch.ct_create IS 'Дата сборки';
COMMENT ON COLUMN ${user.table}.t_create_patch.cn_size IS 'Размер сборки';


-- ${user.table}.t_d_asset_type определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_asset_type;

CREATE TABLE ${user.table}.t_d_asset_type (
	ck_id varchar(255) NOT NULL, -- Идентификатор
	cv_description varchar(2000) NULL, -- Описание
	ct_create timestamp DEFAULT now() NOT NULL, -- Дата создания
	ct_change timestamp DEFAULT now() NOT NULL, -- Дата изменения
	ck_user varchar NOT NULL, -- Пользователь изменивший
	cr_type varchar(10) DEFAULT 'TEXT'::character varying NOT NULL, -- Тип данных
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	cv_content_type varchar(2000) DEFAULT 'plain/text'::character varying NOT NULL, -- Mime type
	cv_extension varchar(20) DEFAULT '.txt'::character varying NOT NULL, -- Расширение файла
	CONSTRAINT cin_c_d_asset_type_1 CHECK (((cv_extension)::text ~~ '.%'::text)),
	CONSTRAINT cin_p_asset_type PRIMARY KEY (ck_id)
);
COMMENT ON TABLE ${user.table}.t_d_asset_type IS 'Тип вложения';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_asset_type.ck_id IS 'Идентификатор';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.cv_description IS 'Описание';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.ct_create IS 'Дата создания';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.ct_change IS 'Дата изменения';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.ck_user IS 'Пользователь изменивший';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.cr_type IS 'Тип данных';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.cv_content_type IS 'Mime type';
COMMENT ON COLUMN ${user.table}.t_d_asset_type.cv_extension IS 'Расширение файла';


-- ${user.table}.t_d_format определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_format;

CREATE TABLE ${user.table}.t_d_format (
	ck_id varchar(255) NOT NULL, -- Индетификатор
	cv_name varchar(300) NOT NULL, -- Наименование
	cv_extension varchar(20) NOT NULL, -- Расширение файла
	cv_content_type varchar(2000) DEFAULT 'plain/text'::character varying NOT NULL, -- Mime type
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки
	cr_type varchar(10) DEFAULT 'JRXML'::character varying NOT NULL, -- Формат вывода
	cv_plugin varchar(1000) NULL, -- Плагин вывода
	CONSTRAINT cin_c_d_format_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_c_d_format_2 CHECK (((cv_extension)::text ~~ '.%'::text)),
	CONSTRAINT cin_c_d_format_3 CHECK (((ck_id)::text = upper((ck_id)::text))),
	CONSTRAINT cin_c_d_format_4 CHECK (((cr_type)::text = ANY ((ARRAY['JRXML'::character varying, 'PLUGIN'::character varying])::text[]))),
	CONSTRAINT cin_c_d_format_5 CHECK ((((cr_type)::text = 'JRXML'::text) OR (((cr_type)::text = 'PLUGIN'::text) AND (NULLIF(btrim((cv_plugin)::text), ''::text) IS NOT NULL)))),
	CONSTRAINT cin_p_d_format PRIMARY KEY (ck_id)
);
CREATE UNIQUE INDEX cin_i_d_format ON ${user.table}.t_d_format USING btree (lower((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_d_format IS 'Форматы печати';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_format.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_format.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_d_format.cv_extension IS 'Расширение файла';
COMMENT ON COLUMN ${user.table}.t_d_format.cv_content_type IS 'Mime type';
COMMENT ON COLUMN ${user.table}.t_d_format.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_d_format.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_d_format.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_d_format.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_d_format.cct_parameter IS 'Настройки';
COMMENT ON COLUMN ${user.table}.t_d_format.cr_type IS 'Формат вывода';
COMMENT ON COLUMN ${user.table}.t_d_format.cv_plugin IS 'Плагин вывода';


-- ${user.table}.t_d_global_setting определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_global_setting;

CREATE TABLE ${user.table}.t_d_global_setting (
	ck_id varchar(255) NOT NULL, -- Индетификатор
	cv_value text NULL, -- Значение
	cv_description text NULL, -- Описание
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Дата создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_d_global_setting_1 CHECK (((ck_id)::text = upper((ck_id)::text))),
	CONSTRAINT cin_p_d_global_setting PRIMARY KEY (ck_id)
);
CREATE UNIQUE INDEX cin_i_d_global_setting_1 ON ${user.table}.t_d_global_setting USING btree (ck_id);
CREATE UNIQUE INDEX cin_i_d_global_setting_2 ON ${user.table}.t_d_global_setting USING btree (upper((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_d_global_setting IS 'Основные настройки';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_global_setting.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.cv_value IS 'Значение';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.cv_description IS 'Описание';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.ct_create IS 'Дата создания';
COMMENT ON COLUMN ${user.table}.t_d_global_setting.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_d_source_type определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_source_type;

CREATE TABLE ${user.table}.t_d_source_type (
	ck_id varchar(30) NOT NULL, -- Индетификатор
	cv_description varchar(300) NOT NULL, -- Описаниние
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_source_type_1 CHECK (((ck_id)::text = upper((ck_id)::text))),
	CONSTRAINT cin_p_d_source_type PRIMARY KEY (ck_id)
);
CREATE UNIQUE INDEX cin_i_d_source_type ON ${user.table}.t_d_source_type USING btree (lower((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_d_source_type IS 'Список типов источников данных';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_source_type.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_source_type.cv_description IS 'Описаниние';
COMMENT ON COLUMN ${user.table}.t_d_source_type.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_d_source_type.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_d_source_type.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_d_source_type.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_d_status определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_status;

CREATE TABLE ${user.table}.t_d_status (
	ck_id varchar(30) NOT NULL, -- Индетификатор
	cv_name varchar(300) NOT NULL, -- Наименование
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamptz DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_d_status_1 CHECK (((ck_id)::text = upper((ck_id)::text))),
	CONSTRAINT cin_p_d_status PRIMARY KEY (ck_id)
);
CREATE UNIQUE INDEX cin_i_d_status ON ${user.table}.t_d_status USING btree (lower((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_d_status IS 'Список статусов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_status.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_status.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_d_status.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_d_status.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_d_status.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_d_status.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_log определение

-- Drop table

-- DROP TABLE ${user.table}.t_log;

CREATE TABLE ${user.table}.t_log (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- ИД записи лога
	cv_session varchar(100) NULL, -- ИД сессии
	cc_json text NULL, -- JSON
	cv_table varchar(4000) NULL, -- Имя таблицы
	cv_id varchar(4000) NULL, -- ИД записи в таблице
	cv_action varchar(30) NULL, -- ИД действия
	cv_error text NULL, -- Код ошибки
	ck_user varchar(150) NOT NULL, -- ИД пользователя
	ct_change timestamptz DEFAULT now() NOT NULL, -- Дата последнего изменения
	CONSTRAINT cin_p_log PRIMARY KEY (ck_id)
);
COMMENT ON TABLE ${user.table}.t_log IS 'Аудит';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_log.ck_id IS 'ИД записи лога';
COMMENT ON COLUMN ${user.table}.t_log.cv_session IS 'ИД сессии';
COMMENT ON COLUMN ${user.table}.t_log.cc_json IS 'JSON';
COMMENT ON COLUMN ${user.table}.t_log.cv_table IS 'Имя таблицы';
COMMENT ON COLUMN ${user.table}.t_log.cv_id IS 'ИД записи в таблице';
COMMENT ON COLUMN ${user.table}.t_log.cv_action IS 'ИД действия';
COMMENT ON COLUMN ${user.table}.t_log.cv_error IS 'Код ошибки';
COMMENT ON COLUMN ${user.table}.t_log.ck_user IS 'ИД пользователя';
COMMENT ON COLUMN ${user.table}.t_log.ct_change IS 'Дата последнего изменения';


-- ${user.table}.t_notification определение

-- Drop table

-- DROP TABLE ${user.table}.t_notification;

CREATE TABLE ${user.table}.t_notification (
	ck_id varchar(32) NOT NULL, -- ИД оповещения
	cd_st timestamp DEFAULT now() NOT NULL, -- Дата начала
	cd_en timestamp NULL, -- Дата окончания
	ck_user varchar(100) NOT NULL, -- ИД пользователя
	cl_sent int2 DEFAULT 0 NOT NULL, -- Признак отправки
	cct_message text DEFAULT '{}'::text NOT NULL, -- Сообщение
	cl_read int2 DEFAULT 0 NOT NULL, -- Признак прочтения
	CONSTRAINT cin_p_notification PRIMARY KEY (ck_id)
);
COMMENT ON TABLE ${user.table}.t_notification IS 'Т_Оповещение';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_notification.ck_id IS 'ИД оповещения';
COMMENT ON COLUMN ${user.table}.t_notification.cd_st IS 'Дата начала';
COMMENT ON COLUMN ${user.table}.t_notification.cd_en IS 'Дата окончания';
COMMENT ON COLUMN ${user.table}.t_notification.ck_user IS 'ИД пользователя';
COMMENT ON COLUMN ${user.table}.t_notification.cl_sent IS 'Признак отправки';
COMMENT ON COLUMN ${user.table}.t_notification.cct_message IS 'Сообщение';
COMMENT ON COLUMN ${user.table}.t_notification.cl_read IS 'Признак прочтения';


-- ${user.table}.t_server_flag определение

-- Drop table

-- DROP TABLE ${user.table}.t_server_flag;

CREATE TABLE ${user.table}.t_server_flag (
	ck_id varchar(255) NOT NULL, -- Идентификатор
	ct_change timestamp DEFAULT now() NOT NULL, -- Время изменения
	CONSTRAINT cin_p_server_flag PRIMARY KEY (ck_id)
);
COMMENT ON TABLE ${user.table}.t_server_flag IS 'Статус сервера';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_server_flag.ck_id IS 'Идентификатор';
COMMENT ON COLUMN ${user.table}.t_server_flag.ct_change IS 'Время изменения';


-- ${user.table}.t_shedlock определение

-- Drop table

-- DROP TABLE ${user.table}.t_shedlock;

CREATE TABLE ${user.table}.t_shedlock (
	cv_name varchar(255) NOT NULL, -- Наименование блокировки
	ct_lock_until timestamp NOT NULL, -- Время блокировки
	ct_locked_at timestamp NOT NULL, -- Время блокировки
	cv_locked_by varchar(255) NOT NULL, -- Сервер юлокировки
	CONSTRAINT t_shedlock_pkey PRIMARY KEY (cv_name)
);
COMMENT ON TABLE ${user.table}.t_shedlock IS 'Синхронизация крона';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_shedlock.cv_name IS 'Наименование блокировки';
COMMENT ON COLUMN ${user.table}.t_shedlock.ct_lock_until IS 'Время блокировки';
COMMENT ON COLUMN ${user.table}.t_shedlock.ct_locked_at IS 'Время блокировки';
COMMENT ON COLUMN ${user.table}.t_shedlock.cv_locked_by IS 'Сервер юлокировки';


-- ${user.table}.t_asset определение

-- Drop table

-- DROP TABLE ${user.table}.t_asset;

CREATE TABLE ${user.table}.t_asset (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	cv_name text NOT NULL, -- Наименование
	cb_asset bytea NULL, -- Данные
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время  создания
	ck_d_type varchar(255) NOT NULL, -- Тип вложения
	cv_asset text NULL, -- Данные
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	cv_plugin varchar(2000) NULL, -- Ссылка на плагин
	CONSTRAINT cin_c_asset_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_c_asset_2 CHECK ((((cb_asset IS NULL) AND (cv_asset IS NOT NULL)) OR ((cb_asset IS NOT NULL) AND (cv_asset IS NULL)))),
	CONSTRAINT cin_p_asset PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_asset_1 FOREIGN KEY (ck_d_type) REFERENCES ${user.table}.t_d_asset_type(ck_id)
);
CREATE UNIQUE INDEX cin_i_asset_1 ON ${user.table}.t_asset USING btree (ck_id);
COMMENT ON TABLE ${user.table}.t_asset IS 'Список ресурсов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_asset.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_asset.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_asset.cb_asset IS 'Данные';
COMMENT ON COLUMN ${user.table}.t_asset.cct_parameter IS 'Настройки';
COMMENT ON COLUMN ${user.table}.t_asset.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_asset.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_asset.ct_create IS 'Время  создания';
COMMENT ON COLUMN ${user.table}.t_asset.ck_d_type IS 'Тип вложения';
COMMENT ON COLUMN ${user.table}.t_asset.cv_asset IS 'Данные';
COMMENT ON COLUMN ${user.table}.t_asset.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_asset.cv_plugin IS 'Ссылка на плагин';


-- ${user.table}.t_d_queue определение

-- Drop table

-- DROP TABLE ${user.table}.t_d_queue;

CREATE TABLE ${user.table}.t_d_queue (
	ck_id varchar(30) NOT NULL, -- Индетификатор
	cv_runner_url varchar(2000) NULL, -- Ссылка на контекст запуска. Например http://localhost:8020/runner
	ck_parent varchar(30) NULL, -- Ссылка на родительский индетификатор
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_d_queue_1 CHECK (((ck_id)::text = lower((ck_id)::text))),
	CONSTRAINT cin_c_d_queue_2 CHECK (((ck_id)::text <> (ck_parent)::text)),
	CONSTRAINT cin_p_d_queue PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_d_queue_1 FOREIGN KEY (ck_parent) REFERENCES ${user.table}.t_d_queue(ck_id)
);
CREATE UNIQUE INDEX cin_i_d_queue ON ${user.table}.t_d_queue USING btree (lower((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_d_queue IS 'Список очередей';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_d_queue.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_queue.cv_runner_url IS 'Ссылка на контекст запуска. Например http://localhost:8020/runner';
COMMENT ON COLUMN ${user.table}.t_d_queue.ck_parent IS 'Ссылка на родительский индетификатор';
COMMENT ON COLUMN ${user.table}.t_d_queue.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_d_queue.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_d_queue.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_d_queue.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_report определение

-- Drop table

-- DROP TABLE ${user.table}.t_report;

CREATE TABLE ${user.table}.t_report (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	cv_name varchar(300) NOT NULL, -- Наименование
	ck_d_queue varchar(30) NULL, -- Индетификатор типа очереди
	ck_authorization uuid NOT NULL, -- Индетификатор авторизации
	cv_duration_expire_storage_online varchar NULL, -- Время хранения готового отчета онлайн
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки отчета
	cn_priority int4 DEFAULT 100 NOT NULL, -- Приоритет
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	cv_duration_expire_storage_offline varchar NULL, -- Время хранения готового отчета отложеной печати
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_c_report_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_p_report PRIMARY KEY (ck_id),
	CONSTRAINT cin_u_report UNIQUE (cv_name),
	CONSTRAINT cin_r_report_1 FOREIGN KEY (ck_d_queue) REFERENCES ${user.table}.t_d_queue(ck_id),
	CONSTRAINT cin_r_report_2 FOREIGN KEY (ck_authorization) REFERENCES ${user.table}.t_authorization(ck_id)
);
CREATE UNIQUE INDEX cin_i_report_1 ON ${user.table}.t_report USING btree (ck_id);
CREATE UNIQUE INDEX cin_i_report_2 ON ${user.table}.t_report USING btree (upper((cv_name)::text));
COMMENT ON TABLE ${user.table}.t_report IS 'Список отчетов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_report.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_report.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_report.ck_d_queue IS 'Индетификатор типа очереди';
COMMENT ON COLUMN ${user.table}.t_report.ck_authorization IS 'Индетификатор авторизации';
COMMENT ON COLUMN ${user.table}.t_report.cv_duration_expire_storage_online IS 'Время хранения готового отчета онлайн';
COMMENT ON COLUMN ${user.table}.t_report.cct_parameter IS 'Настройки отчета';
COMMENT ON COLUMN ${user.table}.t_report.cn_priority IS 'Приоритет';
COMMENT ON COLUMN ${user.table}.t_report.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_report.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_report.cv_duration_expire_storage_offline IS 'Время хранения готового отчета отложеной печати';
COMMENT ON COLUMN ${user.table}.t_report.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_report.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_source определение

-- Drop table

-- DROP TABLE ${user.table}.t_source;

CREATE TABLE ${user.table}.t_source (
	ck_id varchar(255) NOT NULL, -- Индетификатор
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки
	cv_plugin varchar(2000) NULL, -- Наименование плагина
	ck_d_source varchar(30) NOT NULL, -- Индетификатор типа источника данных
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	cl_enable int2 DEFAULT 0::smallint NOT NULL, -- Признак активности источника
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	cv_script text NULL, -- Скрипт иницилизации
	CONSTRAINT cin_c_source_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_c_source_2 CHECK ((((ck_d_source)::text <> 'plugin'::text) OR (((ck_d_source)::text = 'plugin'::text) AND (cv_plugin IS NOT NULL)))),
	CONSTRAINT cin_p_source PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_source_1 FOREIGN KEY (ck_d_source) REFERENCES ${user.table}.t_d_source_type(ck_id)
);
CREATE INDEX cin_i_source_1 ON ${user.table}.t_source USING btree (ck_d_source);
CREATE UNIQUE INDEX cin_i_source_2 ON ${user.table}.t_source USING btree (ck_id);
CREATE UNIQUE INDEX cin_i_source_3 ON ${user.table}.t_source USING btree (upper((ck_id)::text));
COMMENT ON TABLE ${user.table}.t_source IS 'Список источников данных';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_source.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_source.cct_parameter IS 'Настройки';
COMMENT ON COLUMN ${user.table}.t_source.cv_plugin IS 'Наименование плагина';
COMMENT ON COLUMN ${user.table}.t_source.ck_d_source IS 'Индетификатор типа источника данных';
COMMENT ON COLUMN ${user.table}.t_source.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_source.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_source.cl_enable IS 'Признак активности источника';
COMMENT ON COLUMN ${user.table}.t_source.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_source.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_source.cv_script IS 'Скрипт иницилизации';


-- ${user.table}.t_report_format определение

-- Drop table

-- DROP TABLE ${user.table}.t_report_format;

CREATE TABLE ${user.table}.t_report_format (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	ck_report uuid NOT NULL, -- Индетификатор отчета
	ck_d_format varchar(255) NOT NULL, -- Индетификатор формата
	ck_asset uuid NOT NULL, -- Индетификатор ресурса
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	ck_source varchar(255) NOT NULL, -- Источник данных
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_p_report_format PRIMARY KEY (ck_id),
	CONSTRAINT cin_u_report_format UNIQUE (ck_report, ck_d_format),
	CONSTRAINT cin_r_report_format_1 FOREIGN KEY (ck_report) REFERENCES ${user.table}.t_report(ck_id),
	CONSTRAINT cin_r_report_format_2 FOREIGN KEY (ck_d_format) REFERENCES ${user.table}.t_d_format(ck_id),
	CONSTRAINT cin_r_report_format_3 FOREIGN KEY (ck_asset) REFERENCES ${user.table}.t_asset(ck_id),
	CONSTRAINT cin_r_report_format_4 FOREIGN KEY (ck_source) REFERENCES ${user.table}.t_source(ck_id)
);
CREATE UNIQUE INDEX cin_i_report_format_1 ON ${user.table}.t_report_format USING btree (ck_report, ck_d_format);
CREATE INDEX cin_i_report_format_2 ON ${user.table}.t_report_format USING btree (ck_report);
CREATE INDEX cin_i_report_format_3 ON ${user.table}.t_report_format USING btree (ck_d_format);
CREATE UNIQUE INDEX cin_i_report_format_4 ON ${user.table}.t_report_format USING btree (ck_id);
COMMENT ON TABLE ${user.table}.t_report_format IS 'Список формата отчета';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_report_format.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_report_format.ck_report IS 'Индетификатор отчета';
COMMENT ON COLUMN ${user.table}.t_report_format.ck_d_format IS 'Индетификатор формата';
COMMENT ON COLUMN ${user.table}.t_report_format.ck_asset IS 'Индетификатор ресурса';
COMMENT ON COLUMN ${user.table}.t_report_format.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_report_format.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_report_format.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_report_format.ck_source IS 'Источник данных';
COMMENT ON COLUMN ${user.table}.t_report_format.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_scheduler определение

-- Drop table

-- DROP TABLE ${user.table}.t_scheduler;

CREATE TABLE ${user.table}.t_scheduler (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки
	cn_priority int4 DEFAULT 100 NOT NULL, -- Приоритет запуска
	cv_unix_cron varchar(100) NOT NULL, -- Настройка времени запуска в формате unix. * * * * *
	ct_next_run_cron timestamp NULL, -- Время следующего запуска
	ck_report uuid NOT NULL, -- Индетификатор отчета
	cv_report_name varchar(255) NULL, -- Наименование файла результата
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_start_run_cron timestamp NOT NULL, -- Время начала работы планировщика
	cl_enable int2 DEFAULT 0 NOT NULL, -- Признак активности планировщика
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	ck_report_format uuid NOT NULL, -- Формат отчета
	CONSTRAINT cin_c_scheduler_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_p_scheduler PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_scheduler_1 FOREIGN KEY (ck_report_format) REFERENCES ${user.table}.t_report_format(ck_id),
	CONSTRAINT cin_r_scheduler_3 FOREIGN KEY (ck_report) REFERENCES ${user.table}.t_report(ck_id)
);
CREATE INDEX cin_i_scheduler_3 ON ${user.table}.t_scheduler USING btree (ck_report);
COMMENT ON TABLE ${user.table}.t_scheduler IS 'Список плановых печатей отчета';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_scheduler.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_scheduler.cct_parameter IS 'Настройки';
COMMENT ON COLUMN ${user.table}.t_scheduler.cn_priority IS 'Приоритет запуска';
COMMENT ON COLUMN ${user.table}.t_scheduler.cv_unix_cron IS 'Настройка времени запуска в формате unix. * * * * *';
COMMENT ON COLUMN ${user.table}.t_scheduler.ct_next_run_cron IS 'Время следующего запуска';
COMMENT ON COLUMN ${user.table}.t_scheduler.ck_report IS 'Индетификатор отчета';
COMMENT ON COLUMN ${user.table}.t_scheduler.cv_report_name IS 'Наименование файла результата';
COMMENT ON COLUMN ${user.table}.t_scheduler.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_scheduler.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_scheduler.ct_start_run_cron IS 'Время начала работы планировщика';
COMMENT ON COLUMN ${user.table}.t_scheduler.cl_enable IS 'Признак активности планировщика';
COMMENT ON COLUMN ${user.table}.t_scheduler.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_scheduler.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_scheduler.ck_report_format IS 'Формат отчета';


-- ${user.table}.t_queue определение

-- Drop table

-- DROP TABLE ${user.table}.t_queue;

CREATE TABLE ${user.table}.t_queue (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	ck_d_status varchar(30) NOT NULL, -- Индетификатор статуса
	cct_parameter text DEFAULT '{}'::text NOT NULL, -- Настройки отчета
	ck_report_format uuid NOT NULL, -- Индетификатор формата отчета
	ck_d_queue varchar(30) DEFAULT 'default'::character varying NOT NULL, -- Индетификатор очереди
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	ct_st timestamp NULL, -- Время начала формирования
	ct_en timestamp NULL, -- Время окончания формирования
	ck_report uuid NOT NULL, -- Индетификатор отчета
	ck_scheduler uuid NULL, -- Индетификатор планировщика если по плану
	ct_cleaning timestamp NULL, -- Дата удаления отчета
	cv_report_name varchar(255) NULL, -- Наименования файла отчета
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	cn_priority int4 DEFAULT 100 NOT NULL, -- Приоритет
	ck_server varchar(255) NULL, -- Идентификатор сервера
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	cl_online int2 DEFAULT 0 NOT NULL, -- Признак запуска онлайн
	CONSTRAINT cin_c_queue_1 CHECK ((jsonb_typeof((cct_parameter)::jsonb) = 'object'::text)),
	CONSTRAINT cin_p_queue PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_queue_1 FOREIGN KEY (ck_report_format) REFERENCES ${user.table}.t_report_format(ck_id),
	CONSTRAINT cin_r_queue_2 FOREIGN KEY (ck_d_queue) REFERENCES ${user.table}.t_d_queue(ck_id),
	CONSTRAINT cin_r_queue_3 FOREIGN KEY (ck_d_status) REFERENCES ${user.table}.t_d_status(ck_id),
	CONSTRAINT cin_r_queue_4 FOREIGN KEY (ck_report) REFERENCES ${user.table}.t_report(ck_id),
	CONSTRAINT cin_r_queue_5 FOREIGN KEY (ck_scheduler) REFERENCES ${user.table}.t_scheduler(ck_id),
	CONSTRAINT cin_r_queue_6 FOREIGN KEY (ck_server) REFERENCES ${user.table}.t_server_flag(ck_id)
);
CREATE INDEX cin_i_queue_1 ON ${user.table}.t_queue USING btree (ck_d_status);
CREATE INDEX cin_i_queue_2 ON ${user.table}.t_queue USING btree (ck_report_format);
CREATE INDEX cin_i_queue_3 ON ${user.table}.t_queue USING btree (ck_report);
CREATE INDEX cin_i_queue_4 ON ${user.table}.t_queue USING btree (ck_scheduler);
CREATE UNIQUE INDEX cin_i_queue_5 ON ${user.table}.t_queue USING btree (ck_id);
COMMENT ON TABLE ${user.table}.t_queue IS 'Очередь отчетов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_queue.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_queue.ck_d_status IS 'Индетификатор статуса';
COMMENT ON COLUMN ${user.table}.t_queue.cct_parameter IS 'Настройки отчета';
COMMENT ON COLUMN ${user.table}.t_queue.ck_report_format IS 'Индетификатор формата отчета';
COMMENT ON COLUMN ${user.table}.t_queue.ck_d_queue IS 'Индетификатор очереди';
COMMENT ON COLUMN ${user.table}.t_queue.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_queue.ct_st IS 'Время начала формирования';
COMMENT ON COLUMN ${user.table}.t_queue.ct_en IS 'Время окончания формирования';
COMMENT ON COLUMN ${user.table}.t_queue.ck_report IS 'Индетификатор отчета';
COMMENT ON COLUMN ${user.table}.t_queue.ck_scheduler IS 'Индетификатор планировщика если по плану';
COMMENT ON COLUMN ${user.table}.t_queue.ct_cleaning IS 'Дата удаления отчета';
COMMENT ON COLUMN ${user.table}.t_queue.cv_report_name IS 'Наименования файла отчета';
COMMENT ON COLUMN ${user.table}.t_queue.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_queue.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_queue.cn_priority IS 'Приоритет';
COMMENT ON COLUMN ${user.table}.t_queue.ck_server IS 'Идентификатор сервера';
COMMENT ON COLUMN ${user.table}.t_queue.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_queue.cl_online IS 'Признак запуска онлайн';


-- ${user.table}.t_queue_log определение

-- Drop table

-- DROP TABLE ${user.table}.t_queue_log;

CREATE TABLE ${user.table}.t_queue_log (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	ck_queue uuid NOT NULL, -- Индетификатор очереди
	cv_error text NULL, -- Наименование ошибки
	cv_error_stacktrace text NULL, -- Полное описание ошибки
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_p_queue_log PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_queue_log_1 FOREIGN KEY (ck_queue) REFERENCES ${user.table}.t_queue(ck_id)
);
CREATE UNIQUE INDEX cin_i_queue_log_3 ON ${user.table}.t_queue_log USING btree (ck_id);
COMMENT ON TABLE ${user.table}.t_queue_log IS 'Список ошибок';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_queue_log.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_queue_log.ck_queue IS 'Индетификатор очереди';
COMMENT ON COLUMN ${user.table}.t_queue_log.cv_error IS 'Наименование ошибки';
COMMENT ON COLUMN ${user.table}.t_queue_log.cv_error_stacktrace IS 'Полное описание ошибки';
COMMENT ON COLUMN ${user.table}.t_queue_log.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_queue_log.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_queue_log.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_queue_log.cl_deleted IS 'Признак удаления';


-- ${user.table}.t_queue_storage определение

-- Drop table

-- DROP TABLE ${user.table}.t_queue_storage;

CREATE TABLE ${user.table}.t_queue_storage (
	ck_id uuid NOT NULL, -- Идентификатор
	cv_content_type varchar(2000) NOT NULL, -- Mime type
	cb_result bytea NOT NULL, -- File
	cv_name varchar(255) NOT NULL, -- Имя файла
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	ct_change timestamp DEFAULT now() NOT NULL, -- Время изменения
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	ck_queue uuid NOT NULL, -- Идентификатор очереди
	CONSTRAINT cin_p_queue_storage PRIMARY KEY (ck_id),
	CONSTRAINT cin_r_queue_storage_1 FOREIGN KEY (ck_queue) REFERENCES ${user.table}.t_queue(ck_id)
);
COMMENT ON TABLE ${user.table}.t_queue_storage IS 'Список файлов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_queue_storage.ck_id IS 'Идентификатор';
COMMENT ON COLUMN ${user.table}.t_queue_storage.cv_content_type IS 'Mime type';
COMMENT ON COLUMN ${user.table}.t_queue_storage.cb_result IS 'File';
COMMENT ON COLUMN ${user.table}.t_queue_storage.cv_name IS 'Имя файла';
COMMENT ON COLUMN ${user.table}.t_queue_storage.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_queue_storage.ct_change IS 'Время изменения';
COMMENT ON COLUMN ${user.table}.t_queue_storage.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_queue_storage.cl_deleted IS 'Признак удаления';
COMMENT ON COLUMN ${user.table}.t_queue_storage.ck_queue IS 'Идентификатор очереди';


-- ${user.table}.t_report_asset определение

-- Drop table

-- DROP TABLE ${user.table}.t_report_asset;

CREATE TABLE ${user.table}.t_report_asset (
	ck_id uuid DEFAULT uuid_generate_v4() NOT NULL, -- Индетификатор
	cv_name varchar(255) NOT NULL, -- Наименование
	ck_asset uuid NOT NULL, -- Индетифкатор ресурса
	ck_report uuid NOT NULL, -- Индетификатор отчета
	ck_user varchar(100) NOT NULL, -- Индетификатор пользователя изменившего/создавшего запись
	ct_change timestamp DEFAULT now() NOT NULL, -- Время модификации
	ct_create timestamp DEFAULT now() NOT NULL, -- Время создания
	ck_report_format uuid NULL, -- Формат отчета
	cl_deleted int2 DEFAULT 0 NOT NULL, -- Признак удаления
	CONSTRAINT cin_p_report_asset PRIMARY KEY (ck_id),
	CONSTRAINT cin_u_report_asset UNIQUE (ck_report, cv_name),
	CONSTRAINT cin_r_report_asset_1 FOREIGN KEY (ck_report) REFERENCES ${user.table}.t_report(ck_id),
	CONSTRAINT cin_r_report_asset_2 FOREIGN KEY (ck_asset) REFERENCES ${user.table}.t_asset(ck_id),
	CONSTRAINT cin_r_report_asset_3 FOREIGN KEY (ck_report_format) REFERENCES ${user.table}.t_report_format(ck_id)
);
CREATE UNIQUE INDEX cin_i_report_template_1 ON ${user.table}.t_report_asset USING btree (ck_id);
CREATE UNIQUE INDEX cin_i_report_template_2 ON ${user.table}.t_report_asset USING btree (ck_report, lower((cv_name)::text));
CREATE INDEX cin_i_report_template_3 ON ${user.table}.t_report_asset USING btree (ck_asset);
CREATE INDEX cin_i_report_template_4 ON ${user.table}.t_report_asset USING btree (ck_report);
COMMENT ON TABLE ${user.table}.t_report_asset IS 'Список дополнительных ресурсов';

-- Column comments

COMMENT ON COLUMN ${user.table}.t_report_asset.ck_id IS 'Индетификатор';
COMMENT ON COLUMN ${user.table}.t_report_asset.cv_name IS 'Наименование';
COMMENT ON COLUMN ${user.table}.t_report_asset.ck_asset IS 'Индетифкатор ресурса';
COMMENT ON COLUMN ${user.table}.t_report_asset.ck_report IS 'Индетификатор отчета';
COMMENT ON COLUMN ${user.table}.t_report_asset.ck_user IS 'Индетификатор пользователя изменившего/создавшего запись';
COMMENT ON COLUMN ${user.table}.t_report_asset.ct_change IS 'Время модификации';
COMMENT ON COLUMN ${user.table}.t_report_asset.ct_create IS 'Время создания';
COMMENT ON COLUMN ${user.table}.t_report_asset.ck_report_format IS 'Формат отчета';
COMMENT ON COLUMN ${user.table}.t_report_asset.cl_deleted IS 'Признак удаления';

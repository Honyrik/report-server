## TypeOrm Schema
## Установка бд

Правим liquibase.meta.properties прописываем адрес нашей бд core и пользователя с правами создания и редактирования

Правим liquibase.schema.properties прописываем адрес нашей бд essence_report_java и пользователя с правами создания и редактирования

Правим liquibase.auth.properties прописываем адрес нашей бд core_auth и пользователя с правами создания и редактирования

Запускаем 
```bash
./update_all
```
Проверяем main.log

Пример успешного ответа
```
Starting Liquibase at Пт, 10 янв 2020 09:00:24 MSK (version 3.6.3 built at 2019-01-29 11:34:48)
Liquibase: Update has been successful.
Starting Liquibase at Пт, 10 янв 2020 09:00:27 MSK (version 3.6.3 built at 2019-01-29 11:34:48)
Liquibase: Update has been successful.
Starting Liquibase at Пт, 10 янв 2020 09:00:27 MSK (version 3.6.3 built at 2019-01-29 11:34:48)
Liquibase: Update has been successful.
```
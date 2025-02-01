-- noinspection SqlResolveForFile

DROP SCHEMA IF EXISTS test CASCADE;
CREATE SCHEMA test;
-- h2 and hibernate do not support 2d arrays; this schema is purely to make hibernate quiet down during tests
CREATE TABLE test.historic_data(data_table TINYINT ARRAY NOT NULL, date DATE NOT NULL PRIMARY KEY);
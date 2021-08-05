--Clear the data before inserting
DELETE FROM NETWORK_DATA WHERE DATASPACE in ( 'DATASPACE-01', 'DATASPACE-02');
COMMIT;

-- Test pagination data
-- Test created Before filter
-- Test observed After Filter
INSERT INTO NETWORK_DATA (OBSERVED_TIMESTAMP, DATASPACE, ANCHOR, SCHEMA_SET, PAYLOAD, CREATED_TIMESTAMP)
VALUES
('2021-07-22 00:00:01.000', 'DATASPACE-01', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "status" : "up" }'::jsonb, '2021-07-22 23:00:01.000'),
('2021-07-22 01:00:01.000', 'DATASPACE-01', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "status" : "down" }'::jsonb, '2021-07-22 23:00:01.000'),
('2021-07-23 00:00:01.000', 'DATASPACE-01', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "status" : "up" }'::jsonb, '2021-07-23 23:00:01.000');

-- Test sorting on multiple fields
INSERT INTO NETWORK_DATA (OBSERVED_TIMESTAMP, DATASPACE, ANCHOR, SCHEMA_SET, PAYLOAD, CREATED_TIMESTAMP)
VALUES
('2021-07-24 00:00:01.000', 'DATASPACE-01', 'ANCHOR-02', 'SCHEMA-SET-01', '{ "status" : "up" }'::jsonb, '2021-07-24 23:00:01.000');


-- Test simple payload filter on multiple field
INSERT INTO NETWORK_DATA (OBSERVED_TIMESTAMP, DATASPACE, ANCHOR, SCHEMA_SET, PAYLOAD, CREATED_TIMESTAMP)
VALUES
('2021-07-24 00:00:01.000', 'DATASPACE-02', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "interfaces": [ { "id" : "01", "status" : "up" } ]}'::jsonb, '2021-07-24 01:00:01.000'),
('2021-07-24 01:00:01.000', 'DATASPACE-02', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "interfaces": [ { "id" : "01", "status" : "down" } ]}'::jsonb, '2021-07-24 02:00:01.000'),
('2021-07-24 02:00:01.000', 'DATASPACE-02', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "interfaces": [ { "id" : "02", "status" : "up" } ]}'::jsonb, '2021-07-24 03:00:01.000'),
('2021-07-24 03:00:01.000', 'DATASPACE-02', 'ANCHOR-01', 'SCHEMA-SET-01', '{ "interfaces": [ { "id" : "03", "status" : "up" } ]}'::jsonb, '2021-07-24 04:00:01.000');

COMMIT;





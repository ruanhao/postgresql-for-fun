DROP TABLE IF EXISTS networks;
create table networks (
    id serial PRIMARY KEY,
	name VARCHAR(50) NOT NULL,
	tenant_id VARCHAR(10)
);
insert into networks (name, tenant_id) values ('Site-Cisco', 'cisco');
insert into networks (name, tenant_id) values ('Site-Microsoft', 'microsoft');
insert into networks (name, tenant_id) values ('Site-Apple', 'apple');
-- changeset e_cha:1726476397331-1
CREATE SCHEMA IF NOT EXISTS t1_demo;

-- changeset e_cha:1726476397331-2
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- changeset e_cha:1726476397331-3
CREATE TABLE clients
(
    id UUID NOT NULL PRIMARY KEY DEFAULT uuid_generate_v4(),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    middle_name VARCHAR(255),
    client_status VARCHAR(20),
    client_id UUID NOT NULL UNIQUE
);

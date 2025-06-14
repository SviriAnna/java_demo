
-- changeset svirianna:202505220128-1
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL UNIQUE,
    client_id UUID NOT NULL,
    account_type VARCHAR(255),
    account_status VARCHAR(20),
    balance DECIMAL(15,2),
    frozen_amount DECIMAL(15,2),
    CONSTRAINT fk_account_client FOREIGN KEY (client_id)
    REFERENCES clients(id)
);

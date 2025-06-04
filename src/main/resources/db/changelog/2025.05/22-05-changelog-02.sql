
-- changeset svirianna:202505220128-1
CREATE TABLE accounts (
    account_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id UUID NOT NULL,
    account_type VARCHAR(255),
    balance DECIMAL(15,2),
    CONSTRAINT fk_account_client FOREIGN KEY (client_id)
    REFERENCES clients(id)
);

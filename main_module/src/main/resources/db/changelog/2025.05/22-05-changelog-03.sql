
-- changeset svirianna:202505220129-1
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    transaction_id UUID NOT NULL UNIQUE,
    account_id UUID NOT NULL,
    amount DECIMAL(15,2),
    transaction_time TIMESTAMP,
    transaction_status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id)
    REFERENCES accounts(id)
);

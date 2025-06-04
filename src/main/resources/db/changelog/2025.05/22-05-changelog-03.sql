
-- changeset svirianna:202505220129-1
CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    account_id UUID NOT NULL,
    amount DECIMAL(15,2),
    transaction_time TIMESTAMP,
    CONSTRAINT fk_transaction_account FOREIGN KEY (account_id)
    REFERENCES accounts(account_id)
);

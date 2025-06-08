
-- changeset svirianna:202505220229-1
CREATE TABLE time_limit_exceed_log (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    method_signature VARCHAR(255),
    execution_time BIGINT,
    wanted_time BIGINT
);

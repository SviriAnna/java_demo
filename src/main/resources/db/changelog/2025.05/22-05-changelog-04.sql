
-- changeset svirianna:202505220228-1
CREATE TABLE data_source_error_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    message VARCHAR(1024),
    stacktrace TEXT,
    method_signature VARCHAR(512)
);

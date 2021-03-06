CREATE TABLE master_key_status (
  is_unique_key     BIT           NOT NULL,
  status            VARCHAR(16)   NOT NULL,
  set_by_node_id    VARCHAR(64)   NOT NULL,
  kid               CHAR(64)      NOT NULL,
  expires           BIGINT        NOT NULL,
  CONSTRAINT kid_pk PRIMARY KEY (kid),
  CONSTRAINT unique_key UNIQUE (is_unique_key)
);
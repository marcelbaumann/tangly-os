CREATE SCHEMA crm;

CREATE TABLE crm.naturalEntities (
  oid       BIGINT PRIMARY KEY,
  id        VARCHAR(64),
  name      VARCHAR(64),
  firstname VARCHAR(64),
  lastname  varchar(64),
  fromDate  DATE,
  toDate    DATE,
  text      CLOB,
  tags      CLOB
);

CREATE TABLE crm.legalEntities (
  oid      BIGINT PRIMARY KEY,
  id       VARCHAR(64),
  name     VARCHAR(64),
  fromDate DATE,
  toDate   DATE,
  text     CLOB,
  tags     CLOB
);

CREATE TABLE crm.employees (
  oid             BIGINT PRIMARY KEY,
  id              VARCHAR(64),
  name            VARCHAR(64),
  title           VARCHAR(64),
  fromDate        DATE,
  toDate          DATE,
  text            CLOB,
  tags            CLOB,
  personOid       BIGINT NOT NULL,
  organizationOid BIGINT NOT NULL,
  FOREIGN KEY (personOid) REFERENCES crm.naturalEntities (oid),
  FOREIGN KEY (organizationOid) REFERENCES crm.legalEntities (oid)
);

CREATE TABLE prd.contracts (
  oid              BIGINT PRIMARY KEY,
  id               VARCHAR(64),
  name             VARCHAR(64),
  amountWithoutVat DECIMAL(12, 2),
  fromDate         DATE,
  toDate           DATE,
  text             CLOB,
  sellerOid        BIGINT NOT NULL,
  selleeOid        BIGINT NOT NULL,
  tags             CLOB,
  FOREIGN KEY (sellerOid) REFERENCES crm.legalEntities (oid),
  FOREIGN KEY (selleeOid) REFERENCES crm.legalEntities (oid)
);

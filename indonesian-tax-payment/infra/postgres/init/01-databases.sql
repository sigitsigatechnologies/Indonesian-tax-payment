CREATE DATABASE payment_db;
CREATE DATABASE billing_db;
CREATE DATABASE core_banking_db;
CREATE DATABASE tax_gateway_db;
CREATE DATABASE reconciliation_db;
CREATE DATABASE notification_db;

GRANT ALL PRIVILEGES ON DATABASE payment_db TO taxpay;
GRANT ALL PRIVILEGES ON DATABASE billing_db TO taxpay;
GRANT ALL PRIVILEGES ON DATABASE core_banking_db TO taxpay;
GRANT ALL PRIVILEGES ON DATABASE tax_gateway_db TO taxpay;
GRANT ALL PRIVILEGES ON DATABASE reconciliation_db TO taxpay;
GRANT ALL PRIVILEGES ON DATABASE notification_db TO taxpay;

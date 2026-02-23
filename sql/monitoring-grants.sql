-- Monitoring users for mysqld_exporter and Grafana datasource
CREATE USER IF NOT EXISTS 'mysql_exporter'@'%' IDENTIFIED BY 'mysql_exporter_password';
GRANT PROCESS, REPLICATION CLIENT, SELECT ON *.* TO 'mysql_exporter'@'%';

CREATE USER IF NOT EXISTS 'grafana_reader'@'%' IDENTIFIED BY 'grafana_reader_password';
GRANT SELECT ON mysql.* TO 'grafana_reader'@'%';
GRANT SELECT ON yeezydb.* TO 'grafana_reader'@'%';

FLUSH PRIVILEGES;

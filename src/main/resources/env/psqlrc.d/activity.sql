SELECT
    datname, pid, usename, application_name, client_addr, client_hostname, client_port, query, state
FROM
    pg_stat_activity
WHERE
    pid <> pg_backend_pid() AND state IS NOT NULL ORDER BY query;

SELECT
    pid, usename, datname, query, client_addr, wait_event_type, wait_event
FROM
    pg_stat_activity
WHERE
    pid <> pg_backend_pid() AND wait_event is not null
ORDER BY
      wait_event_type
;

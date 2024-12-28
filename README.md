# securo

For running the app in docker:

docker-compose build
docker-compose up -d
docker-compose down 

For backup and restore:

docker exec -it postgres-db psql -U postgres
\c postgres
SELECT pid, usename, datname, client_addr, state FROM pg_stat_activity WHERE datname = 'conitrack';
SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'conitrack';

docker exec -it postgres-db psql -U postgres -c "DROP DATABASE conitrack;"
docker exec -it postgres-db psql -U postgres -c "CREATE DATABASE conitrack;"

docker cp backups/backup_2024-12-28_18-00-01.sql postgres-db:/backup_2024-12-28_18-00-01.sql
docker exec -it postgres-db psql -U postgres -d conitrack -f /backup_2024-12-28_18-00-01.sql
#!/bin/bash

# Define variables
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)

docker exec conitrack_docker pg_dumpall -U postgres > ../backups/backup_$TIMESTAMP.sql
echo "Backup completado: backups/backup_$TIMESTAMP.sql"

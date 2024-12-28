#!/bin/bash

# Set timezone to GMT-3
export TZ=Etc/GMT+3

# Define variables
BACKUP_DIR="/backups"
TIMESTAMP=$(date +%Y-%m-%d_%H-%M-%S)
BACKUP_FILE="$BACKUP_DIR/backup_$TIMESTAMP.sql"

# Ensure backup directory exists
mkdir -p "$BACKUP_DIR"

# Perform the backup (example command using pg_dumpall)
pg_dumpall -U postgres > "$BACKUP_FILE"

echo "Backup completed: $BACKUP_FILE"

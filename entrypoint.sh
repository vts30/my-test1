#!/bin/bash

# Helper function to log messages in JSON format
log_message() {
    echo '{"node":"entrypoint","level":"INFO","message":"'"$1"'","timestamp":"'"$(date +%s%3N)"'"}'
}

# Log start of container
log_message "Starting Bsp-backup-monitor-Container..."

# Function to validate JAVA_XMX value
validate_java_xmx() {
    if [[ "$1" =~ ^[0-9]{1,4}[MG]$ ]]; then
        echo "$1"
    else
        log_message "Fehler: Ungültiger Wert für JAVA_XMX: $1"
        exit 1
    fi
}

# Set JAVA_XMX environment variable or use a default value
JAVA_XMX=${JAVA_XMX:-4G}
log_message "Validating JAVA_XMX value..."

# Validate JAVA_XMX variable
validated_xmx=$(validate_java_xmx "$JAVA_XMX")

echo "============================================="
echo "🚀 Starting BSP Forum Backup Monitor"
echo "🔖 Version: ${APP_VERSION}"
echo "============================================="
exec java -Xmx"$validated_xmx" -Duser.timezone=Europe/Berlin -jar ./bsp-forum-backup-monitor.jar

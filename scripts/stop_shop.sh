#!/bin/bash
# =============================================================================
# Stop Shop Management Service - Server A
# =============================================================================

APP_NAME="shop-management"
APP_DIR="/opt/microservices/${APP_NAME}"
APP_PORT=8080

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

log "Stopping ${APP_NAME}..."

PID_FILE="${APP_DIR}/${APP_NAME}.pid"

if [ -f "${PID_FILE}" ]; then
    PID=$(cat ${PID_FILE})
    if ps -p ${PID} > /dev/null 2>&1; then
        log "Stopping process with PID: ${PID}"
        kill ${PID}
        sleep 5
        
        if ps -p ${PID} > /dev/null 2>&1; then
            log "Force killing process..."
            kill -9 ${PID}
        fi
        log "Process stopped"
    else
        log "Process not running"
    fi
    rm -f ${PID_FILE}
else
    log "PID file not found"
    
    # Try to find and kill by port
    EXISTING_PID=$(lsof -t -i:${APP_PORT} 2>/dev/null || true)
    if [ -n "${EXISTING_PID}" ]; then
        log "Found process on port ${APP_PORT}, killing PID: ${EXISTING_PID}"
        kill ${EXISTING_PID}
        sleep 3
    fi
fi

log "${APP_NAME} stopped"

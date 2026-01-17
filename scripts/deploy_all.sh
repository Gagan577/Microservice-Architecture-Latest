#!/bin/bash
# =============================================================================
# Full Deployment Orchestrator
# =============================================================================
# This script orchestrates deployment across both servers
# Run this from your local machine or a CI/CD pipeline
# =============================================================================

set -e

# =============================================================================
# Configuration - UPDATE THESE VALUES
# =============================================================================
SHOP_SERVER_IP="${SHOP_SERVER_IP:-your-shop-server-ip}"
STOCK_SERVER_IP="${STOCK_SERVER_IP:-your-stock-server-ip}"
STOCK_SERVER_PRIVATE_IP="${STOCK_SERVER_PRIVATE_IP:-your-stock-server-private-ip}"

# SSH Configuration
SSH_USER="${SSH_USER:-ec2-user}"
SSH_KEY="${SSH_KEY:-~/.ssh/your-key.pem}"

# Common Configuration
GIT_REPO_URL="${GIT_REPO_URL:-https://github.com/your-org/microservices-architecture.git}"
GIT_BRANCH="${GIT_BRANCH:-main}"

# Database Configuration (from Terraform outputs)
RDS_ENDPOINT="${RDS_ENDPOINT:-your-rds-endpoint}"
DB_USERNAME="${DB_USERNAME:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-your-db-password}"

# =============================================================================
# Functions
# =============================================================================
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

validate_config() {
    log "Validating configuration..."
    
    if [[ "${SHOP_SERVER_IP}" == "your-shop-server-ip" ]]; then
        log "ERROR: SHOP_SERVER_IP not configured"
        exit 1
    fi
    
    if [[ "${STOCK_SERVER_IP}" == "your-stock-server-ip" ]]; then
        log "ERROR: STOCK_SERVER_IP not configured"
        exit 1
    fi
    
    if [[ "${RDS_ENDPOINT}" == "your-rds-endpoint" ]]; then
        log "ERROR: RDS_ENDPOINT not configured"
        exit 1
    fi
    
    if [ ! -f "${SSH_KEY/#\~/$HOME}" ]; then
        log "ERROR: SSH key not found at ${SSH_KEY}"
        exit 1
    fi
    
    log "Configuration validated"
}

copy_script() {
    local server=$1
    local script=$2
    local ssh_key="${SSH_KEY/#\~/$HOME}"
    
    log "Copying ${script} to ${server}..."
    scp -i "${ssh_key}" -o StrictHostKeyChecking=no \
        "scripts/${script}" \
        "${SSH_USER}@${server}:/tmp/${script}"
    
    ssh -i "${ssh_key}" -o StrictHostKeyChecking=no \
        "${SSH_USER}@${server}" "chmod +x /tmp/${script}"
}

deploy_stock_server() {
    log "=========================================="
    log "Deploying Product Stock Service (Server B)"
    log "=========================================="
    
    local ssh_key="${SSH_KEY/#\~/$HOME}"
    
    copy_script "${STOCK_SERVER_IP}" "deploy_stock.sh"
    
    log "Running deployment on Stock Server..."
    ssh -i "${ssh_key}" -o StrictHostKeyChecking=no \
        "${SSH_USER}@${STOCK_SERVER_IP}" \
        "GIT_REPO_URL='${GIT_REPO_URL}' \
         GIT_BRANCH='${GIT_BRANCH}' \
         RDS_ENDPOINT='${RDS_ENDPOINT}' \
         DB_USERNAME='${DB_USERNAME}' \
         DB_PASSWORD='${DB_PASSWORD}' \
         /tmp/deploy_stock.sh"
    
    log "Stock Server deployment completed"
}

deploy_shop_server() {
    log "=========================================="
    log "Deploying Shop Management Service (Server A)"
    log "=========================================="
    
    local ssh_key="${SSH_KEY/#\~/$HOME}"
    
    copy_script "${SHOP_SERVER_IP}" "deploy_shop.sh"
    
    # IMPORTANT: Pass the Stock Server's PRIVATE IP to Shop Server
    # This is how Server A knows the private IP of Server B
    log "Running deployment on Shop Server..."
    log "Note: STOCK_SERVICE_HOST is set to ${STOCK_SERVER_PRIVATE_IP}"
    
    ssh -i "${ssh_key}" -o StrictHostKeyChecking=no \
        "${SSH_USER}@${SHOP_SERVER_IP}" \
        "GIT_REPO_URL='${GIT_REPO_URL}' \
         GIT_BRANCH='${GIT_BRANCH}' \
         RDS_ENDPOINT='${RDS_ENDPOINT}' \
         DB_USERNAME='${DB_USERNAME}' \
         DB_PASSWORD='${DB_PASSWORD}' \
         STOCK_SERVICE_HOST='${STOCK_SERVER_PRIVATE_IP}' \
         STOCK_SERVICE_PORT='8081' \
         /tmp/deploy_shop.sh"
    
    log "Shop Server deployment completed"
}

# =============================================================================
# Main Execution
# =============================================================================
main() {
    log "=========================================="
    log "Microservices Full Deployment"
    log "=========================================="
    
    validate_config
    
    # Deploy Stock Server first (Server B)
    # Server A depends on Server B being available
    deploy_stock_server
    
    # Wait for Stock Server to be fully ready
    log "Waiting 30 seconds for Stock Server to stabilize..."
    sleep 30
    
    # Deploy Shop Server (Server A)
    deploy_shop_server
    
    log "=========================================="
    log "DEPLOYMENT COMPLETE"
    log "=========================================="
    log ""
    log "Shop Management API (Server A - Public):"
    log "  URL: http://${SHOP_SERVER_IP}:8080/api/shop"
    log ""
    log "Product Stock API (Server B - Internal):"
    log "  REST:    http://${STOCK_SERVER_PRIVATE_IP}:8081/api/stock"
    log "  SOAP:    http://${STOCK_SERVER_PRIVATE_IP}:8081/api/stock/soap/stock?wsdl"
    log "  GraphQL: http://${STOCK_SERVER_PRIVATE_IP}:8081/api/stock/graphql"
    log ""
    log "=========================================="
}

# Allow running specific deployment
case "${1}" in
    shop)
        validate_config
        deploy_shop_server
        ;;
    stock)
        validate_config
        deploy_stock_server
        ;;
    *)
        main
        ;;
esac

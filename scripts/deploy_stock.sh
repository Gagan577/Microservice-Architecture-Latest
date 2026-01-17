#!/bin/bash
# =============================================================================
# Deploy Product Stock Service - Server B (Port 8081)
# =============================================================================
# This script deploys the product-stock application on Server B
# It clones the repository, builds the application, and starts it
# =============================================================================

set -e

# =============================================================================
# Configuration Variables
# =============================================================================
APP_NAME="product-stock"
APP_DIR="/opt/microservices/${APP_NAME}"
LOG_DIR="/var/log/stock-app"
GIT_REPO="${GIT_REPO_URL:-https://github.com/your-org/microservices-architecture.git}"
GIT_BRANCH="${GIT_BRANCH:-main}"

# Database Configuration (passed as environment variables)
RDS_ENDPOINT="${RDS_ENDPOINT:-localhost}"
DB_USERNAME="${DB_USERNAME:-postgres}"
DB_PASSWORD="${DB_PASSWORD:-postgres}"

# Application Configuration
APP_PORT=8081
JAVA_OPTS="${JAVA_OPTS:--Xms512m -Xmx1024m -XX:+UseG1GC}"

# =============================================================================
# Functions
# =============================================================================
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1"
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        log "ERROR: Java is not installed"
        exit 1
    fi
    java_version=$(java -version 2>&1 | head -n 1)
    log "Java version: $java_version"
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        log "ERROR: Maven is not installed"
        exit 1
    fi
    mvn_version=$(mvn -version 2>&1 | head -n 1)
    log "Maven version: $mvn_version"
    
    # Check Git
    if ! command -v git &> /dev/null; then
        log "ERROR: Git is not installed"
        exit 1
    fi
    git_version=$(git --version)
    log "Git version: $git_version"
}

setup_directories() {
    log "Setting up directories..."
    
    # Create application directory
    sudo mkdir -p ${APP_DIR}
    sudo chown $(whoami):$(whoami) ${APP_DIR}
    
    # Create log directory
    sudo mkdir -p ${LOG_DIR}
    sudo chmod 755 ${LOG_DIR}
    sudo chown $(whoami):$(whoami) ${LOG_DIR}
}

clone_repository() {
    log "Cloning repository from ${GIT_REPO}..."
    
    cd ${APP_DIR}
    
    if [ -d ".git" ]; then
        log "Repository exists, pulling latest changes..."
        git fetch origin
        git checkout ${GIT_BRANCH}
        git pull origin ${GIT_BRANCH}
    else
        log "Cloning fresh repository..."
        git clone -b ${GIT_BRANCH} ${GIT_REPO} .
    fi
}

build_application() {
    log "Building ${APP_NAME}..."
    
    cd ${APP_DIR}/${APP_NAME}
    
    # Clean and build
    mvn clean package -DskipTests -q
    
    if [ ! -f "target/${APP_NAME}-1.0.0.jar" ]; then
        log "ERROR: Build failed - JAR file not found"
        exit 1
    fi
    
    log "Build completed successfully"
}

stop_existing_process() {
    log "Stopping existing ${APP_NAME} process if running..."
    
    PID_FILE="${APP_DIR}/${APP_NAME}.pid"
    
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat ${PID_FILE})
        if ps -p ${PID} > /dev/null 2>&1; then
            log "Stopping process with PID: ${PID}"
            kill ${PID} || true
            sleep 5
            
            # Force kill if still running
            if ps -p ${PID} > /dev/null 2>&1; then
                log "Force killing process..."
                kill -9 ${PID} || true
            fi
        fi
        rm -f ${PID_FILE}
    fi
    
    # Also check for any process on port 8081
    EXISTING_PID=$(lsof -t -i:${APP_PORT} 2>/dev/null || true)
    if [ -n "${EXISTING_PID}" ]; then
        log "Killing existing process on port ${APP_PORT}"
        kill ${EXISTING_PID} || true
        sleep 3
    fi
}

start_application() {
    log "Starting ${APP_NAME} on port ${APP_PORT}..."
    
    cd ${APP_DIR}/${APP_NAME}
    
    # Export environment variables
    export RDS_ENDPOINT
    export DB_USERNAME
    export DB_PASSWORD
    export LOG_PATH=${LOG_DIR}
    
    # Start the application
    nohup java ${JAVA_OPTS} \
        -Dserver.port=${APP_PORT} \
        -Dspring.profiles.active=prod \
        -DRDS_ENDPOINT=${RDS_ENDPOINT} \
        -DDB_USERNAME=${DB_USERNAME} \
        -DDB_PASSWORD=${DB_PASSWORD} \
        -DLOG_PATH=${LOG_DIR} \
        -jar target/${APP_NAME}-1.0.0.jar \
        > ${LOG_DIR}/startup.log 2>&1 &
    
    # Save PID
    echo $! > ${APP_DIR}/${APP_NAME}.pid
    PID=$(cat ${APP_DIR}/${APP_NAME}.pid)
    
    log "Application started with PID: ${PID}"
}

health_check() {
    log "Performing health check..."
    
    MAX_RETRIES=30
    RETRY_COUNT=0
    
    while [ ${RETRY_COUNT} -lt ${MAX_RETRIES} ]; do
        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${APP_PORT}/api/stock/health 2>/dev/null || echo "000")
        
        if [ "${HTTP_CODE}" = "200" ]; then
            log "Health check passed! Application is running."
            return 0
        fi
        
        RETRY_COUNT=$((RETRY_COUNT + 1))
        log "Health check attempt ${RETRY_COUNT}/${MAX_RETRIES} - Status: ${HTTP_CODE}"
        sleep 5
    done
    
    log "ERROR: Health check failed after ${MAX_RETRIES} attempts"
    return 1
}

# =============================================================================
# Main Execution
# =============================================================================
main() {
    log "=========================================="
    log "Deploying ${APP_NAME}"
    log "=========================================="
    
    check_prerequisites
    setup_directories
    clone_repository
    build_application
    stop_existing_process
    start_application
    
    sleep 10
    
    if health_check; then
        log "=========================================="
        log "Deployment completed successfully!"
        log "REST API: http://localhost:${APP_PORT}/api/stock"
        log "SOAP:     http://localhost:${APP_PORT}/api/stock/soap/stock?wsdl"
        log "GraphQL:  http://localhost:${APP_PORT}/api/stock/graphql"
        log "GraphiQL: http://localhost:${APP_PORT}/api/stock/graphiql"
        log "Logs:     ${LOG_DIR}"
        log "=========================================="
    else
        log "ERROR: Deployment failed!"
        log "Check logs at: ${LOG_DIR}/startup.log"
        exit 1
    fi
}

# Run main function
main "$@"

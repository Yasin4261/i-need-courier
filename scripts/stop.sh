#!/bin/bash

# I Need Courier - Application Stop Script

set -e

echo "🛑 Stopping I Need Courier Application..."

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Kill Java process
echo "Killing Spring Boot application..."
pkill -f "pako-0.0.1-SNAPSHOT.jar" 2>/dev/null || true

# Kill processes on ports
echo "Freeing ports 8080 and 8082..."
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8082 | xargs kill -9 2>/dev/null || true

sleep 2

# Stop Docker containers (optional)
read -p "Stop Docker containers? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Stopping Docker containers..."
    docker compose down
    echo -e "${GREEN}✅ Docker containers stopped${NC}"
fi

echo -e "${GREEN}✅ Application stopped successfully${NC}"
#!/bin/bash

# I Need Courier - Application Startup Script
# Clean Layered Architecture

set -e

echo "🚀 I Need Courier - Starting Application..."
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed. Please install Docker first.${NC}"
    exit 1
fi

# Check if Docker Compose is installed
if ! command -v docker compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed. Please install Docker Compose first.${NC}"
    exit 1
fi

# Step 1: Start PostgreSQL
echo -e "${YELLOW}📦 Step 1: Starting PostgreSQL database...${NC}"
docker compose up -d postgres
sleep 5

# Check if PostgreSQL is running
if docker compose ps | grep -q "courier-postgres.*running"; then
    echo -e "${GREEN}✅ PostgreSQL is running${NC}"
else
    echo -e "${RED}❌ Failed to start PostgreSQL${NC}"
    exit 1
fi

# Step 2: Build the application
echo ""
echo -e "${YELLOW}🔨 Step 2: Building application...${NC}"
./mvnw clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build successful${NC}"
else
    echo -e "${RED}❌ Build failed${NC}"
    exit 1
fi

# Step 3: Kill any existing process on port 8080
echo ""
echo -e "${YELLOW}🧹 Step 3: Cleaning up old processes...${NC}"
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
lsof -ti:8082 | xargs kill -9 2>/dev/null || true
pkill -f "pako-0.0.1-SNAPSHOT.jar" 2>/dev/null || true
sleep 2
echo -e "${GREEN}✅ Cleanup complete${NC}"

# Step 4: Start the application
echo ""
echo -e "${YELLOW}🚀 Step 4: Starting Spring Boot application...${NC}"
nohup java -jar target/pako-0.0.1-SNAPSHOT.jar > logs/app.log 2>&1 &
APP_PID=$!

echo "Application starting with PID: $APP_PID"
echo "Logs: logs/app.log"
echo ""
echo "Waiting for application to start..."

# Wait for application to start (max 60 seconds)
MAX_WAIT=60
COUNTER=0
while [ $COUNTER -lt $MAX_WAIT ]; do
    if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Application started successfully!${NC}"
        break
    fi
    sleep 2
    COUNTER=$((COUNTER + 2))
    echo -n "."
done

echo ""

if [ $COUNTER -ge $MAX_WAIT ]; then
    echo -e "${RED}❌ Application failed to start within $MAX_WAIT seconds${NC}"
    echo "Check logs: tail -f logs/app.log"
    exit 1
fi

# Display information
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🎉 I Need Courier Application is Running!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "📍 Application URL: http://localhost:8080"
echo "📊 Health Check:    http://localhost:8080/actuator/health"
echo "📚 API Docs:        http://localhost:8080/swagger-ui/index.html"
echo ""
echo "🔐 Test Endpoints:"
echo ""
echo "Register:"
echo "curl -X POST http://localhost:8080/api/v1/auth/register \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"phone\":\"+905551234567\",\"password\":\"password123\"}'"
echo ""
echo "Login (Test User):"
echo "curl -X POST http://localhost:8080/api/v1/auth/login \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"email\":\"courier1@test.com\",\"password\":\"password123\"}'"
echo ""
echo "📝 Logs: tail -f logs/app.log"
echo "🛑 Stop: ./stop.sh or kill $APP_PID"
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"


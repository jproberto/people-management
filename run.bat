@echo off
REM filepath: p:\workspace\Java\people-management\run.bat

title People Management - Application Runner

echo ================================================================
echo             PEOPLE MANAGEMENT - APPLICATION RUNNER
echo ================================================================
echo.

REM Verificar se estamos no diretório correto
if not exist mvnw.cmd (
    echo [ERROR] Maven wrapper not found!
    echo Please make sure you are in the project root directory.
    echo.
    pause
    exit /b 1
)

if not exist docker-compose.yml (
    echo [ERROR] docker-compose.yml not found!
    echo Please make sure you are in the project root directory.
    echo.
    pause
    exit /b 1
)

:MENU
cls
echo ================================================================
echo             PEOPLE MANAGEMENT - APPLICATION RUNNER
echo ================================================================
echo.
echo Choose your option:
echo.
echo [1]  RUN LOCALLY (Infrastructure in Docker + Spring Boot local)
echo [2]  RUN WITH DOCKER (Everything in containers)
echo [3]  RUN TESTS ONLY
echo [4]  BUILD ONLY (Clean + Package)
echo [5]  CHECK INFRASTRUCTURE STATUS
echo [6]  STOP ALL SERVICES
echo [7]  CLEANUP (Stop + Remove containers and volumes)
echo [8]  EXIT
echo.

set /p choice="Enter your choice (1-8): "

if "%choice%"=="1" goto RUN_LOCAL
if "%choice%"=="2" goto RUN_DOCKER
if "%choice%"=="3" goto RUN_TESTS
if "%choice%"=="4" goto BUILD_ONLY
if "%choice%"=="5" goto CHECK_STATUS
if "%choice%"=="6" goto STOP_SERVICES
if "%choice%"=="7" goto CLEANUP
if "%choice%"=="8" goto EXIT

echo Invalid choice. Please try again.
timeout /t 2 /nobreak >nul
goto MENU

:RUN_LOCAL
echo.
echo ================================================================
echo                       RUNNING LOCALLY
echo ================================================================
echo.
echo [STEP 1/5] Stopping any existing services...
docker-compose down >nul 2>&1

echo [STEP 2/5] Starting infrastructure (PostgreSQL + Kafka)...
docker-compose up -d postgres zookeeper kafka

echo [STEP 3/5] Waiting for services to be ready...
call :WAIT_FOR_INFRASTRUCTURE

echo [STEP 4/5] Verifying application startup readiness...
call :PRE_STARTUP_CHECK

echo [STEP 5/5] Starting Spring Boot application...
echo.
echo Application starting with profile: dev
echo Database: localhost:5432/people_management
echo Kafka: localhost:9092
echo.

REM Mostrar status final antes de iniciar
call :SHOW_STARTUP_STATUS

echo.
echo ================================================================
echo                      STARTING APPLICATION
echo ================================================================
echo.
echo The application is starting now...
echo.
pause
echo Press Ctrl+C to stop the application and return to menu
echo.
echo ----------------------------------------------------------------

mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev

REM Quando a aplicação para, mostrar status final
echo.
echo ================================================================
echo                     APPLICATION STOPPED
echo ================================================================
echo.
echo Application has been stopped.
echo Infrastructure (PostgreSQL + Kafka) is still running.
echo.
echo You can:
echo  - Run option [1] again to restart the application
echo  - Run option [6] to stop all services
echo  - Run option [5] to check infrastructure status
echo.
pause
goto MENU

:RUN_DOCKER
echo.
echo ================================================================
echo                     RUNNING WITH DOCKER
echo ================================================================
echo.
echo [STEP 1/3] Stopping any existing services...
docker-compose down >nul 2>&1

echo [STEP 2/3] Building and starting all services...
docker-compose up --build -d

echo [STEP 3/3] Waiting for application to be ready...
call :WAIT_FOR_APPLICATION

call :SHOW_FINAL_STATUS "DOCKER"
echo.
echo Press any key to view logs (Ctrl+C to stop)...
pause >nul
docker-compose logs -f people-management-app

goto MENU

:RUN_TESTS
echo.
echo ================================================================
echo                        RUNNING TESTS
echo ================================================================
echo.
echo [STEP 1/2] Running all tests...
mvnw.cmd test

if %errorlevel% equ 0 (
    echo.
    echo [OK] ALL TESTS PASSED!
    echo.
    echo Test results available in: target/surefire-reports/
) else (
    echo.
    echo [FAIL] SOME TESTS FAILED!
    echo.
    echo Check test results in: target/surefire-reports/
)

call :SHOW_TEST_SUMMARY
goto MENU

:BUILD_ONLY
echo.
echo ================================================================
echo                        BUILDING PROJECT
echo ================================================================
echo.
echo [STEP 1/2] Cleaning previous build...
mvnw.cmd clean

echo [STEP 2/2] Packaging application...
mvnw.cmd package -DskipTests

if %errorlevel% equ 0 (
    echo.
    echo [OK] BUILD SUCCESSFUL!
    echo.
    echo JAR file created: target/people-management-1.0.0.jar
    echo.
    echo You can run it with:
    echo java -jar target/people-management-1.0.0.jar
) else (
    echo.
    echo [FAIL] BUILD FAILED!
    echo.
    echo Check the output above for error details.
)

echo.
pause
goto MENU

:CHECK_STATUS
echo.
echo ================================================================
echo                     INFRASTRUCTURE STATUS
echo ================================================================
echo.

call :CHECK_INFRASTRUCTURE_DETAILED
echo.
pause
goto MENU

:STOP_SERVICES
echo.
echo ================================================================
echo                      STOPPING SERVICES
echo ================================================================
echo.
echo Stopping all Docker services...
docker-compose stop

echo.
echo [OK] All services stopped successfully!
echo.
echo Services status:
docker-compose ps

echo.
pause
goto MENU

:CLEANUP
echo.
echo ================================================================
echo                         CLEANUP
echo ================================================================
echo.
echo [WARNING] This will remove all containers and volumes (DATA LOSS)!
echo.
set /p confirm="Are you sure you want to continue? (y/n): "

if /i not "%confirm%"=="y" (
    echo Cleanup cancelled.
    timeout /t 2 /nobreak >nul
    goto MENU
)

echo.
echo Stopping and removing all services...
docker-compose down -v

echo Removing unused Docker images...
docker image prune -f >nul 2>&1

echo.
echo [OK] Cleanup completed successfully!
echo.
pause
goto MENU

:EXIT
echo.
echo Thanks for using People Management Application Runner!
echo.
exit /b 0

REM ================================================================
REM                         FUNCTIONS
REM ================================================================

:WAIT_FOR_INFRASTRUCTURE
echo Checking PostgreSQL...
set retry_count=0
:WAIT_POSTGRES
docker-compose exec -T postgres pg_isready -U people_admin -d people_management >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] PostgreSQL: READY
    goto CHECK_KAFKA
)
set /a retry_count+=1
if %retry_count% gtr 30 (
    echo [FAIL] PostgreSQL: TIMEOUT after 30 attempts
    goto CHECK_KAFKA
)
echo    Waiting for PostgreSQL... (attempt %retry_count%/30)
timeout /t 2 /nobreak >nul
goto WAIT_POSTGRES

:CHECK_KAFKA
echo Checking Kafka...
set retry_count=0
:WAIT_KAFKA
docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Kafka: READY
    goto INFRA_READY
)
set /a retry_count+=1
if %retry_count% gtr 20 (
    echo [FAIL] Kafka: TIMEOUT after 20 attempts
    goto INFRA_READY
)
echo    Waiting for Kafka... (attempt %retry_count%/20)
timeout /t 3 /nobreak >nul
goto WAIT_KAFKA

:INFRA_READY
echo.
echo [OK] Infrastructure is ready!
echo.
goto :eof

:PRE_STARTUP_CHECK
echo Performing final checks before application startup...
echo.

REM Verificar se as portas necessárias estão livres para a aplicação
netstat -an | findstr 8080 | findstr LISTENING >nul 2>&1
if %errorlevel% equ 0 (
    echo [WARNING] Port 8080 is already in use!
    echo          Please stop any other application using port 8080
    echo          or the Spring Boot application may fail to start.
    echo.
)

REM Verificar conectividade com banco
docker-compose exec -T postgres psql -U people_admin -d people_management -c "SELECT 1;" >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Database connection: VERIFIED
) else (
    echo [WARNING] Database connection: COULD NOT VERIFY
)

REM Verificar se Kafka está aceitando conexões
docker-compose exec -T kafka kafka-broker-api-versions --bootstrap-server localhost:9092 >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Kafka connection: VERIFIED
) else (
    echo [WARNING] Kafka connection: COULD NOT VERIFY
)

goto :eof

:SHOW_STARTUP_STATUS
echo.
echo ================================================================
echo                    ** READY TO START **
echo ================================================================
echo.
echo  INFRASTRUCTURE STATUS:
docker-compose exec -T postgres pg_isready -U people_admin -d people_management >nul 2>&1
if %errorlevel% equ 0 (
    echo    PostgreSQL: [OK] READY
) else (
    echo    PostgreSQL: [FAIL] NOT READY
)

docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
if %errorlevel% equ 0 (
    echo    Kafka: [OK] READY
) else (
    echo    Kafka: [FAIL] NOT READY
)

echo.
echo  APPLICATION ENDPOINTS (after startup):
echo    Main: http://localhost:8080
echo    Health: http://localhost:8080/actuator/health
echo    Swagger: http://localhost:8080/swagger-ui.html
echo.
echo  DATABASE INFO:
echo    Host: localhost:5432
echo    Database: people_management
echo    User: people_admin
echo    Password: people_password_2024
echo.
echo  KAFKA INFO:
echo    Bootstrap Servers: localhost:9092
echo.
echo ================================================================
goto :eof

:WAIT_FOR_APPLICATION
echo Waiting for application to start...
set retry_count=0
:WAIT_APP
timeout /t 5 /nobreak >nul
curl -s http://localhost:8080/actuator/health >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Application: READY
    goto :eof
)
set /a retry_count+=1
if %retry_count% gtr 24 (
    echo [FAIL] Application: TIMEOUT after 2 minutes
    goto :eof
)
echo    Waiting for application... (attempt %retry_count%/24)
goto WAIT_APP

:CHECK_INFRASTRUCTURE_DETAILED
echo === DOCKER CONTAINERS ===
docker ps --filter "name=people-management" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

echo.
echo === POSTGRESQL CHECK ===
docker-compose exec -T postgres pg_isready -U people_admin -d people_management >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] PostgreSQL: READY
) else (
    echo [FAIL] PostgreSQL: NOT READY
)

echo.
echo === KAFKA CHECK ===
docker-compose exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list >nul 2>&1
if %errorlevel% equ 0 (
    echo [OK] Kafka: READY
) else (
    echo [FAIL] Kafka: NOT READY
)
goto :eof

:SHOW_FINAL_STATUS
echo.
echo ================================================================
echo                          ** SUCCESS **
echo ================================================================
echo.
if "%~1"=="LOCAL" (
    echo [OK] Application running locally with Docker infrastructure
    echo.
    echo  MAIN ENDPOINTS:
    echo    Application: http://localhost:8080
    echo    Health Check: http://localhost:8080/actuator/health
    echo    Swagger UI: http://localhost:8080/swagger-ui.html
    echo.
    echo  INFRASTRUCTURE:
    echo    PostgreSQL: localhost:5432 (people_admin/people_password_2024)
    echo    Kafka: localhost:9092
    echo    Database: people_management
) else if "%~1"=="DOCKER" (
    echo [OK] All services running in Docker containers
    echo.
    echo  MAIN ENDPOINTS:
    echo    Application: http://localhost:8080
    echo    Health Check: http://localhost:8080/actuator/health
    echo    Swagger UI: http://localhost:8080/swagger-ui.html
    echo    Kafdrop (Kafka UI): http://localhost:9000
    echo.
    echo  INFRASTRUCTURE:
    echo    PostgreSQL: localhost:5432 (people_admin/people_password_2024)
    echo    Kafka: localhost:9092
    echo    Database: people_management
)
echo.
echo ================================================================
goto :eof

:SHOW_TEST_SUMMARY
echo.
echo ================================================================
echo                       ** TEST SUMMARY **
echo ================================================================
echo.
echo Test reports available at:
echo    target/surefire-reports/
echo    target/surefire-reports/TEST-*.xml
echo.
echo To run specific test categories:
echo   mvnw.cmd test -Dtest="**/*Test"           (Unit tests)
echo   mvnw.cmd test -Dtest="**/*IntegrationTest" (Integration tests)
echo.
echo ================================================================
goto :eof
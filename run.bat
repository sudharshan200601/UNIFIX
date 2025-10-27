@echo off
echo Compiling UniFix Application...
echo =============================

REM Create bin directory if it doesn't exist
if not exist bin mkdir bin

REM Compile the Java files
javac -d bin -cp "lib\*" ^
    src\com\unifix\database\DBConnection.java ^
    src\com\unifix\utils\Location.java ^
    src\com\unifix\utils\UIUtilities.java ^
    src\com\unifix\auth\LoginPage.java ^
    src\com\unifix\auth\SignupPage.java ^
    src\com\unifix\complaints\ComplaintForm.java ^
    src\com\unifix\complaints\ComplaintTable.java ^
    src\com\unifix\complaints\ComplaintDetailsView.java ^
    src\com\unifix\solutions\SolutionPage.java ^
    src\com\unifix\dashboard\StudentDashboard.java ^
    src\com\unifix\dashboard\WardenDashboard.java ^
    src\com\unifix\dashboard\TechnicianDashboard.java ^
    src\com\unifix\dashboard\AdminDashboard.java ^
    src\com\unifix\main\FirstTimeSetup.java ^
    src\com\unifix\main\UpdateDatabase.java ^
    src\com\unifix\main\UniFix.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed! Please check for errors.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Updating database schema...
echo =============================
echo.

REM Update database schema
java -cp "bin;lib/*;resources" com.unifix.main.UpdateDatabase

echo.
echo Starting UniFix Application...
echo =============================
echo.

REM Run the application
java -cp "bin;lib/*;resources" com.unifix.main.UniFix

pause
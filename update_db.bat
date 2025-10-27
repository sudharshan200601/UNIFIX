@echo off
echo Updating UniFix Database...
echo =============================

mysql -u root < sql/update_schema.sql

if %errorlevel% neq 0 (
    echo.
    echo Database update failed! Please check the error message above.
    pause
    exit /b 1
)

echo.
echo Database updated successfully!
pause
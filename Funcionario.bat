@echo off
cd /d %~dp0

echo ================================
echo   AstroLine - Modo Admin
echo ================================

start "" javaw -jar target/AstroLine-1.0-SNAPSHOT.jar Funcionario

exit
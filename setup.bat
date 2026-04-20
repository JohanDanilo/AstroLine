@echo off
setlocal

set "BASE=%~dp0"
set "JAR=%BASE%AstroLine-1.0-SNAPSHOT.jar"
set "ICO=%BASE%icono.ico"

REM ---------- Verificar que el JAR existe ----------
if not exist "%JAR%" (
    echo ERROR: No se encontro el JAR en:
    echo   %JAR%
    echo Asegurate de que la carpeta target este junto a este bat.
    pause
    exit /b 1
)

REM ---------- Resolver ruta absoluta de javaw.exe ----------
if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\javaw.exe" (
        set "JAVA=%JAVA_HOME%\bin\javaw.exe"
        goto :found_java
    )
)

for /f "delims=" %%i in ('where javaw 2^>nul') do (
    set "JAVA=%%i"
    goto :found_java
)

echo ERROR: No se encontro javaw.exe
echo Instala Java o define la variable JAVA_HOME.
pause
exit /b 1

:found_java
echo Java encontrado en: %JAVA%

REM ---------- Extraer icono del JAR (solo si no existe ya) ----------
if not exist "%ICO%" (
    echo Extrayendo icono del JAR...
    powershell -NoProfile -Command ^
        "Add-Type -AssemblyName System.IO.Compression.FileSystem;" ^
        "$z = [IO.Compression.ZipFile]::OpenRead($env:JAR);" ^
        "$e = $z.Entries | Where-Object { $_.FullName -like '*icono.ico' } | Select-Object -First 1;" ^
        "if ($e) { [IO.Compression.ZipFileExtensions]::ExtractToFile($e, $env:ICO, $true); Write-Host 'Icono extraido.' }" ^
        "else    { Write-Host 'ADVERTENCIA: icono.ico no encontrado en el JAR.' }" ^
        "$z.Dispose()"
)

REM ---------- Crear accesos directos ----------
for %%R in (kiosko funcionario admin proyeccion) do (
    powershell -NoProfile -Command ^
        "$mode = '%%R';" ^
        "$sh  = New-Object -ComObject WScript.Shell;" ^
        "$lnk = $sh.CreateShortcut($env:BASE + 'AstroLine-' + $mode + '.lnk');" ^
        "$lnk.TargetPath       = $env:JAVA;" ^
        "$lnk.Arguments = '-jar ' + '\"' + $env:JAR + '\" ' + $mode;" ^
        "$lnk.WorkingDirectory = $env:BASE;" ^
        "$lnk.IconLocation     = $env:ICO;" ^
        "$lnk.Save()"
    echo Creado: AstroLine-%%R.lnk
)

echo.
echo Listo.
pause
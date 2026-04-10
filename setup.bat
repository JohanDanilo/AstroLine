@echo off
set BASE=%~dp0
set JAR=%BASE%target\AstroLine-1.0-SNAPSHOT.jar
set ICO=%BASE%src\main\resources\cr\ac\una\astroline\resource\icono.ico

if defined JAVA_HOME (
    set JAVA=%JAVA_HOME%\bin\javaw.exe
) else (
    set JAVA=javaw
)

for %%R in (kiosko funcionario admin proyeccion) do (
    powershell -Command "$s=$env:COMSPEC;$sh=New-Object -ComObject WScript.Shell;$lnk=$sh.CreateShortcut('%BASE%AstroLine-%%R.lnk');$lnk.TargetPath='%JAVA%';$lnk.Arguments='-jar \"%JAR%\" %%R';$lnk.WorkingDirectory='%BASE%';$lnk.IconLocation='%ICO%';$lnk.Save()"
    echo Creado: AstroLine-%%R.lnk
)
pause
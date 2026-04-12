@echo off
setlocal

REM Ejecuta la aplicacion Java usando clases compiladas en out y los JARs en lib
if not exist out (
    echo ERROR: No se encontro la carpeta out. Ejecuta build.bat primero.
    exit /b 1
)
if not exist lib (
    echo ERROR: No se encontro la carpeta lib. Coloca los JARs de Oracle dentro.
    exit /b 1
)

java -cp "lib/*;out" ganaderia.Main
if errorlevel 1 (
    echo.
    echo EJECUCION FALLIDA.
    exit /b 1
)
endlocal

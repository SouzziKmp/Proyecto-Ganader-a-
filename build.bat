@echo off
setlocal

REM Compila el proyecto Java usando los JARs en lib y genera clases en out
if not exist lib ( 
    echo ERROR: No se encontro la carpeta lib. Crea la carpeta lib y coloca los JARs de Oracle dentro.
    exit /b 1
)

if not exist out mkdir out

javac --release 21 -cp "lib/*" -d out src\ganaderia\db\ConexionADB.java src\ganaderia\modelo\Animal.java src\ganaderia\dao\*.java src\ganaderia\Main.java
if errorlevel 1 (
    echo.
    echo COMPILACION FALLIDA.
    exit /b 1
)
echo.
echo COMPILACION EXITOSA.
endlocal

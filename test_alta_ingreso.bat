@echo off
echo Compilando tests...
call gradlew.bat compileTestJava
if %ERRORLEVEL% NEQ 0 (
    echo Error en compilacion
    exit /b 1
)

echo Ejecutando tests...
call gradlew.bat test --tests AltaIngresoProduccionServiceTest
if %ERRORLEVEL% NEQ 0 (
    echo Errores en tests
    exit /b 1
)

echo Generando reporte JaCoCo...
call gradlew.bat jacocoTestReport

echo COMPLETADO!

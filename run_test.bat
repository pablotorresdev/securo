@echo off
call gradlew.bat clean test --tests AltaIngresoProduccionServiceTest jacocoTestReport
echo Tests completed
pause

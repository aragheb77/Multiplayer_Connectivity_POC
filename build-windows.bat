@echo off
setlocal
set GRADLE_VERSION=8.13
set GRADLE_DIR=%~dp0.gradle-dist\gradle-%GRADLE_VERSION%
set GRADLE_ZIP=%~dp0.gradle-dist\gradle-%GRADLE_VERSION%-bin.zip
if not exist "%GRADLE_DIR%\bin\gradle.bat" (
  if not exist "%~dp0.gradle-dist" mkdir "%~dp0.gradle-dist"
  echo Downloading Gradle %GRADLE_VERSION%...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%GRADLE_ZIP%'"
  if errorlevel 1 goto :error
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Expand-Archive -Path '%GRADLE_ZIP%' -DestinationPath '%~dp0.gradle-dist' -Force"
  if errorlevel 1 goto :error
  del "%GRADLE_ZIP%"
)
if not exist "%~dp0local.properties" (
  echo ERROR: local.properties was not found. Open this project in Android Studio once so it can configure the Android SDK path,
  echo or create local.properties with: sdk.dir=C\:\\Users\\YOUR_USER\\AppData\\Local\\Android\\Sdk
  goto :error
)
call "%GRADLE_DIR%\bin\gradle.bat" --no-daemon :sample-app:assembleDebug :sample-app-two:assembleDebug
if errorlevel 1 goto :error
echo.
echo BUILD SUCCESSFUL. APK files:
echo %~dp0sample-app\build\outputs\apk\debug\sample-app-debug.apk
echo %~dp0sample-app-two\build\outputs\apk\debug\sample-app-two-debug.apk
exit /b 0
:error
echo Build did not complete. Review the error above and README.md.
exit /b 1

@echo off
setlocal

:: ===================================================================
:: REMS: Compilation & Running Script (Windows CMD)
:: ===================================================================

:: Add JDK bin to path if installed locally
if exist "E:\JAVA development kit\bin" (
    set "PATH=E:\JAVA development kit\bin;%PATH%"
)

:: 1. SET YOUR PATHS HERE
:: -------------------------------------------------------------------
:: Updated with local project-level paths:
set "FX_LIB=lib\javafx-sdk-21.0.2\lib"
set "SQLITE_DIR=lib"
:: -------------------------------------------------------------------
cd /d "%~dp0.."

echo [REMS] Initializing build...

:: Create bin directory if it doesn't exist
if not exist bin mkdir bin

echo [REMS] Collecting source files...
:: This finds all .java files in src/ and its subfolders (quoted and using / for cross-platform/javac compatibility)
powershell -Command "Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { '\""' + $_.FullName.Replace('\', '/') + '\""' } | Out-File -FilePath sources.txt -Encoding ascii"

echo [REMS] Compiling...
javac --module-path "%FX_LIB%" ^
      --add-modules javafx.controls,javafx.fxml ^
      -cp "%SQLITE_DIR%\*" ^
      -d bin ^
      @sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] COMPILATION FAILED. Check errors above.
    del sources.txt
    pause
    exit /b %ERRORLEVEL%
)

echo [REMS] Compilation Successful.
del sources.txt

echo [REMS] Launching Application...
echo -------------------------------------------------------------------
java --module-path "%FX_LIB%" ^
     --add-modules javafx.controls,javafx.fxml ^
     -cp "bin;%SQLITE_DIR%\*" ^
     Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [!] Application exited with error code %ERRORLEVEL%
    pause
)

endlocal

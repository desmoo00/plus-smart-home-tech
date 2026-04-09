@echo off
setlocal

REM Hub Router is compiled for Java 21, so we select a compatible JVM here.

if exist "C:\Program Files\Java\jdk-21\bin\java.exe" (
    set "SELECTED_JAVA_HOME=C:\Program Files\Java\jdk-21"
) else if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\java.exe" (
        set "SELECTED_JAVA_HOME=%JAVA_HOME%"
    )
) else if exist "C:\Program Files\Java\latest\bin\java.exe" (
    set "SELECTED_JAVA_HOME=C:\Program Files\Java\latest"
)

if not defined SELECTED_JAVA_HOME (
    echo [ERROR] JDK 21 not found. Set JAVA_HOME to a JDK 21 installation.
    exit /b 1
)

endlocal & set "JAVA_HOME=%SELECTED_JAVA_HOME%" & set "JAVA_EXE=%SELECTED_JAVA_HOME%\bin\java.exe"
exit /b 0

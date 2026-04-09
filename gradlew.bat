@rem Gradle startup script for Windows
@if "%DEBUG%" == "" @echo off
@rem Set local scope for the variables
setlocal
set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%
set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"
java %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
endlocal

@echo off
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.8.101-hotspot
set CLASSPATH=
"%JAVA_HOME%\bin\java.exe" -Dfile.encoding=UTF-8 -Dorg.gradle.appname=gradlew -classpath "%~dp0gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*

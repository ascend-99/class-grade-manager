@echo off
chcp 65001 >nul
title 知衡 - 班级学生成绩管理系统
cd /d "%~dp0"

echo.
echo ============================================================
echo    知衡 - 班级学生成绩管理系统
echo ============================================================
echo.

where mvn >nul 2>nul
if errorlevel 1 (
    echo [ERROR] Maven not found.
    echo Please install Maven and add it to PATH, or run Launcher.java in IDEA.
    echo.
    pause
    exit /b 1
)

set "PORT_ARG="
if not "%~1"=="" set "PORT_ARG=-Dserver.port=%~1"

echo Starting with Maven, first run may download dependencies...
echo.
call mvn -Dfile.encoding=UTF-8 %PORT_ARG% compile exec:java

echo.
echo Server stopped. Press any key to close this window.
pause >nul

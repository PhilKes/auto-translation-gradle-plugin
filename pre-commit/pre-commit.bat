@echo off
echo Running Gradle task: androidAutoTranslate

gradlew androidAutoTranslate

if %errorlevel% neq 0 (
    echo Gradle task 'androidAutoTranslate' failed. Please fix the issues.
    exit /b 1
)

echo Gradle task 'androidAutoTranslate' completed successfully.

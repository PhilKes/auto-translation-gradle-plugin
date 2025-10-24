@echo off
echo Running Gradle task: autoTranslate

gradlew autoTranslate

if %errorlevel% neq 0 (
    echo Gradle task 'autoTranslate' failed. Please fix the issues.
    exit /b 1
)

echo Gradle task 'autoTranslate' completed successfully.

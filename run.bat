@echo off
:: 根据不同环境修改 Chrome 以及 java.exe 程序所在地址。
start "C:\Program Files\Google\Chrome\Application\chrome.exe" "http://localhost:8090"
"C:\Program Files\Java\jdk-11.0.10\bin\java.exe" -jar -Dspring.profiles.active=xio "xio.jar"
pause

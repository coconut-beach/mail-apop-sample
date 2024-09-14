:: exec-maven-plugin による実行方法サンプル

:: (1) 接続先APOPサーバを指定してください
@set APOP_HOST=pop.example.com

:: (2) APOPサーバのユーザIDを指定してください
@set APOP_USER=test-user@example.com

mvn exec:java -Dexec.mainClass=apop.APOPSample ^
   -Dexec.args="%APOP_HOST%  %APOP_USER%"

rem maven を使用しない実行方法サンプル.
rem 事前にcopy_dependencies.bat を実行しjar をlib/ にコピーしておいてください.

:: (1) 接続先APOPサーバを指定してください
@set APOP_HOST=pop.example.com

:: (2) APOPサーバのユーザIDを指定してください
@set APOP_USER=test-user@example.com

@cd /d %~dp0
@set CP=lib

java -cp %CP%\jakarta.mail-2.0.3.jar;%CP%\jakarta.mail-api-2.1.3.jar;%CP%\jakarta.activation-api-2.1.3.jar;%CP%\angus-activation-2.0.2.jar;target\mail-apop-sample-1.0.0.jar apop.APOPSample  %APOP_HOST% %APOP_USER%

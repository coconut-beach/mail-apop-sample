rem maven ���g�p���Ȃ����s���@�T���v��.
rem ���O��copy_dependencies.bat �����s��jar ��lib/ �ɃR�s�[���Ă����Ă�������.

:: (1) �ڑ���APOP�T�[�o���w�肵�Ă�������
@set APOP_HOST=pop.example.com

:: (2) APOP�T�[�o�̃��[�UID���w�肵�Ă�������
@set APOP_USER=test-user@example.com

@cd /d %~dp0
@set CP=lib

java -cp %CP%\jakarta.mail-2.0.3.jar;%CP%\jakarta.mail-api-2.1.3.jar;%CP%\jakarta.activation-api-2.1.3.jar;%CP%\angus-activation-2.0.2.jar;target\mail-apop-sample-1.0.0.jar apop.APOPSample  %APOP_HOST% %APOP_USER%

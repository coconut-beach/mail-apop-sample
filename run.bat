:: exec-maven-plugin �ɂ����s���@�T���v��

:: (1) �ڑ���APOP�T�[�o���w�肵�Ă�������
@set APOP_HOST=pop.example.com

:: (2) APOP�T�[�o�̃��[�UID���w�肵�Ă�������
@set APOP_USER=test-user@example.com

mvn exec:java -Dexec.mainClass=apop.APOPSample ^
   -Dexec.args="%APOP_HOST%  %APOP_USER%"

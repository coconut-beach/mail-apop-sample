<p align="right">2024/09/14</p>

# APOP サンプルプログラム(Java +JakartaMail)
## 概要
APOP認証でPOP3 サーバへアクセスするJava のサンプルプログラムです。      
[src/main/java/apop/APOPSample.java](src/main/java/apop/APOPSample.java)

## ビルド方法
`mvn clean install -DskipTests`

~~JUnit テストを実行する場合は環境変数LOG_DIR にログファイル出力場所を
設定しておいてください。~~ →標準出力へのみ出力します   
テストではMockito2 でfinal クラスやstatic メソッドをモック化しています。

## 実行方法1
次のようにmaven-exec-plugin で実行できます。   
`mvn exec:java -Dexec.mainClass=apop.APOPSample -Dexec.args="pop3ホスト ユーザID"`


例:`mvn exec:java -Dexec.mainClass=apop.APOPSample -Dexec.args="pop.example.com foo@example.com"`

パスワードはプログラムが起動してから、キーボードから入力します。   
Windows の場合は[run.bat](run.bat) の変数を修正して利用してもらえます。

## 実行方法2
準備として、`mvn dependency:copy-dependencies`
を実行し、target/dependency に現れたファイルのうち次のものをlib/ へcopy しておきます。   
(Windows の場合、この準備は[copy_dependencies.bat](copy_dependencies.bat)
で行えます)

1. jakarta.mail-2.0.3.jar
2. jakarta.mail-api-2.1.3.jar
3. jakarta.activation-api-2.1.3.jar
4. angus-activation-2.0.2.jar

そのうえでWindows の場合は次のように実行することもできます。
```
set CP=lib
java -cp %CP%\jakarta.mail-2.0.3.jar;%CP%\jakarta.mail-api-2.1.3.jar;%CP%\jakarta.activation-api-2.1.3.jar;%CP%\angus-activation-2.0.2.jar;target\mail-apop-sample-1.0.0.jar apop.APOPSample pop3ホスト ユーザID
```
パスワードはプログラムが起動してから、キーボードから入力します。   
Windows の場合は[run2.bat](run2.bat) の変数を修正して利用してもらえます。

## 筆者の動作確認環境
 - Windows11 (23H2)
 - JDK-21.0.4
 - maven 3.9.9
 - (以下はpom.xml 内で設定しています)
   - jakarta.mail-api 2.1.3
   - jakarta.mail 2.0.3 (org.rclipse.angus)
   - JUnit 5.11
   - Mockito 5.13.0
   - logback 1.5.8

## 参考URL
1. Jakarta Mail 2.1 https://jakarta.ee/specifications/mail/2.1/   
2. Angus mail のpop3 プロバイダJavaDoc https://eclipse-ee4j.github.io/angus-mail/docs/api/org.eclipse.angus.mail/org/eclipse/angus/mail/pop3/package-summary.html


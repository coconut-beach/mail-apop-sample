:: lib/ に依存jar をcopy します
:: run2.bat で実行する場合の準備です

mvn dependency:copy-dependencies -DoutputDirectory=lib

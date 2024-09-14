package apop;

import java.util.Properties;
import java.util.Enumeration;
import java.io.IOException;
import java.io.Console;

import jakarta.mail.Store;
import jakarta.mail.Folder;
import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Header;
import jakarta.mail.Address;
import jakarta.mail.Multipart;
import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;

/**
 * APOP アクセス プログラムサンプル.<br>
 *<p>
 * POP3 サーバにAPOP認証で接続し、受信メールがあればヘッダや本文を
 * 取得して標準出力へダンプします.
 * アクセスしたメールは削除しません。
 *</p>
 *<p>
 * Java Mail の最新版、Jakarta Mail 2.1 の仕様を次のURLで参照できます。
 * <a href="https://jakarta.ee/specifications/mail/2.1/jakarta-mail-spec-2.1.pdf">
 * https://jakarta.ee/specifications/mail/2.1/jakarta-mail-spec-2.1.pdf</a>
 *</p>
 *<p>
 * 本プロジェクトをビルドしてできる check-apop-1.x.x.jar の他に
 * 実行するには次の依存jar がクラスパス上に必要です。
 *</p>
 *<ol>
 *   <li>jakarta.mail-2.0.3.jar
 *   <li>jakarta.mail-api-2.1.3.jar
 *   <li>jakarta.activation-api-2.1.3.jar
 *   <li>angus-activation-2.0.2.jar
 *</ol>
 */
public class APOPSample {
    /** 受信したメールの保管庫(pop3 サーバ) */
    protected Store pop3Store = null;

    /** pop3 サーバの中にある自分のメールボックス */
    protected Folder myMailBox = null;

    /** 接続先ホスト名 */
    protected String apopHost = null;

    /** ユーザID */
    protected String apopUser = null;

    /** パスワード */
    protected String apopPassword = null;

    /** パスワードをキーボード入力する時のプロンプト文字列 */
    public static final String PASSWD_PROMPT = "password: ";
    
    /** コンソールを使用できない場合の代替パスワード(上書き可能) */
    public static String defaultPassword = "default_password";

    /** System から取得したConsole */
    protected static Console con = System.console();// UTのため外に出しました

    /** APOP認証を利用するかどうか */
    protected boolean useAPOP = true;
    
    /**
     * コンストラクタ.
     * @param apopHost APOP 接続先ホスト名
     * @param apopUser APOP 接続ユーザID
     * @param apopPassword APOP 接続パスワード
     */
    public APOPSample(String apopHost, String apopUser, String apopPassword){
        // TODO: 引数チェックは省略しています
        this.apopHost = apopHost;
        this.apopUser = apopUser;
        this.apopPassword = apopPassword;
    }

    /**
     * APOP認証でpop3 サーバに接続します.
     * <p>
     * 本メソッドは副作用としてインスタンス変数pop3Store とmyMailBox
     * に値をセットします。
     * </p>
     * @throws MessagingException 通信エラーやpop3 プロトコル上の異常発生時
     */
    public void connectAPOP()
        throws MessagingException{

        Properties prop = new Properties();
        //prop.put("mail.host", apopHost);    // グローバル
        prop.put("mail.pop3.host", apopHost); // 特定プロトコル
        if (useAPOP) {
            // APOP 認証を使用する場合はこのプロパティをtrue にセットします
            // https://eclipse-ee4j.github.io/angus-mail/docs/api/org.eclipse.angus.mail/org/eclipse/angus/mail/pop3/package-summary.html
            prop.put("mail.pop3.apop.enable", true);
        }
        
        // Session はメールプロトコルプロバイダに対するアクセスAPIオブジェクト
        Session session = Session.getDefaultInstance(prop);
        
        // メールプロトコルプロバイダからpop3 プロトコルのサーバ(保管庫)を取得
        pop3Store = session.getStore("pop3");
        pop3Store.connect(apopUser, apopPassword); // JavaMail 1.4で追加
        
        // 「INBOX」はプライマリフォルダを指す予約された特別な名前です.
        // 詳しくはjavax.mail.Folder のJavaDoc を参照してください.
        // APIからシンボル(static final なString)として提供して欲しいところ
        // ですね.
        myMailBox = pop3Store.getFolder("INBOX");
        myMailBox.open(Folder.READ_ONLY);
    }

    /**
     * pop3 サーバとの通信を終了します.
     */
    public void closeAPOP() {
        if (myMailBox != null) {
            try {
                myMailBox.close(false);
            } catch(MessagingException ex) {
                ex.printStackTrace();
            }
            myMailBox = null;
        }
        if (pop3Store != null) {
            try {
                pop3Store.close();
            } catch(MessagingException ex) {
                ex.printStackTrace();
            }
            pop3Store = null;
        }
    }

    /**
     * 接続済みのpop3 サーバと通信し、メールヘッダやボディにアクセスします.
     *
     * @return 0:成功, 1: 異常(connectAPOP() が完了していない)
     * @throws MessagingException APOPサーバとのプロトコル上の異常発生時
     */
    protected int access() throws MessagingException{
        if (myMailBox == null) {
            System.err.println("access() connectAPOP ができていません.");
            return 1;
        }
        
        int msgCount = myMailBox.getMessageCount();
        System.out.println("Total Messages: "+msgCount);

        // メールボックスにある複数のメールをチェックするためのループ
        for(int count = 0; count < msgCount; count++) {
            System.out.println("-------<"+(count+1)+" of "+msgCount+">------");
            Message msg = myMailBox.getMessage(count+1);

            // (1)APIによるヘッダチェックの例
            Address[] froms = msg.getFrom();
            for(Address fromAddress : froms) {
                System.out.println("  From: "+fromAddress.toString());
            }
            System.out.println("  Subject: ["+msg.getSubject()+"]");
            System.out.println("  Rcv. Date: ["+msg.getReceivedDate()+"]");
            System.out.println("  Sent Date: ["+msg.getSentDate()+"]");
            System.out.println("  Size: "+msg.getSize());
            System.out.println("");

            // (2)全ヘッダをダンプする例
            Enumeration<Header> enume = msg.getAllHeaders();
            while(enume.hasMoreElements()) {
                Header header = enume.nextElement();
                System.out.println(header.getName()+": "+header.getValue());
            }

            // (3)テキスト系のBody をダンプする例
            System.out.println("=== Message Body ===");
            try {
                Object obj = msg.getContent();
                if (obj instanceof Multipart) {
                    Multipart multi = (Multipart)obj;
                    int numMulti = multi.getCount();

                    // メールがマルチパート形式の場合に各パートを確認するループ
                    for (int ii=0; ii<numMulti; ii++) {
                        BodyPart body = multi.getBodyPart(ii);
                        String type = body.getContentType();
                        System.out.println("------");
                        System.out.println(
                            "  Multi part No "+ii+" type: "+ type);
                        System.out.println("");
                        if (type.startsWith("text/plain")
                            || type.startsWith("text/html")) {
                            System.out.println(body.getContent());
                        }
                    }
                } else {
                    System.out.println(msg.getContent());
                }
            } catch(IOException ex) {
                ex.printStackTrace();
            }
            System.out.println("");
            System.out.println("");
        }
        return 0;
    }

    /**
     * キーボードからパスワード文字列を受け取ります.
     *<p>
     * System.console() でコンソールオブジェクトを取得するようにしていますが、
     * Maven によるJUnit テスト実行の場合などコンソールを取得できずnull と
     * なる場合があります。
     * その時はdefaultPassword にセットされた値をreturn しています。
     *</p>
     * @param prompt 入力プロンプトとして表示する文字列.
     * @return 取得したパスワード文字列.
     */
    public static String getPass(String prompt) {
        if (con == null) {
            System.err.println(
                "Console を取得できないのでデフォルトパスワードを使用します。");
            return defaultPassword;
        }
        char[] pass = con.readPassword(prompt);
        String str = new String(pass);
        return str;
    }

    /**
     * 実行用main().
     *<p>
     *パスワードはキーボードから入力するようにしています.
     *</p>
     * @param args コマンドライン引数
     *<ul>
     *   <li>第1引数 APOPホスト名 (例: pop.example.com)
     *   <li>第2引数 APOPユーザ名 (例; foo@example.com)
     *</ul>
     */
    public static void main(String[] args) {
        APOPSample app = null;

        if (args.length < 2) {
            System.err.println(
                "引数にAPOP ホストとAPOP ユーザ名を指定してください。");
            return;
        }
        try {
            String pass = getPass(PASSWD_PROMPT);
            app = new APOPSample(args[0], args[1], pass);
            app.connectAPOP();
            app.access();
        } catch(MessagingException ex) {
            ex.printStackTrace();
        } finally {
            app.closeAPOP();
        }
    }
}

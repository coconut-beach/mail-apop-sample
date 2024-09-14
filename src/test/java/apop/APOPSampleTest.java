package apop;

import java.io.Console;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;

import jakarta.mail.Store;
import jakarta.mail.Folder;
import jakarta.mail.Session;
import jakarta.mail.Message;
import jakarta.mail.Header;
import jakarta.mail.Address;
import jakarta.mail.Multipart;
import jakarta.mail.BodyPart;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mockito;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * APOPSample.java  に対するJUnit5 テストクラス    (Mockito2).
 */
@ExtendWith(MockitoExtension.class)
public class APOPSampleTest {
    protected static final Logger LOGGER =
        LoggerFactory.getLogger(APOPSampleTest.class);
    
    protected APOPSample target = null;
    protected static int numExecuted = 0;

    protected void dispMethodLogHeader(TestInfo testInfo) {
        LOGGER.info(
            "\n***********************************************\n" +
            "{} No {}. start {}" +
            "\n***********************************************",
            this.getClass().getSimpleName(), (++numExecuted),
            testInfo.getDisplayName());
    }

    @BeforeEach
    public void beforeEachTestMethod(TestInfo testInfo) {
        dispMethodLogHeader(testInfo);
        target =
            new APOPSample("pop.example.com", "test-user", "test-password");
    }
    
    /**
     * getPass() のcon がnull でない通常ケースのテスト.
     */
    @Test
    public void testGetPass1() {
        Console mock = Mockito.mock(Console.class);

        // Console#readPassword() は可変引数をとる
        Mockito.when(
            mock.readPassword(
                //ArgumentMatchers.any(String.class),ArgumentMatchers
                //.<Object>anyVararg()))
                // anyVararg() は非推奨となった. any()を使うべし 
                //ArgumentMatchers.any(String.class),ArgumentMatchers.any()))
                //↓JDK21にしてMockito version をあげると、こうしないと通らない
                ArgumentMatchers.any(String.class))).thenReturn("xxx".toCharArray());

        APOPSample.con = mock;
        
        String sret = APOPSample.getPass("TEST Prompt");
        Assertions.assertEquals("xxx", sret, "パスワード取得結果が異常です");
    }
    
    /**
     * getPass() のcon がnull の場合のテスト.
     */
    @Test
    public void testGetPass2() {
        APOPSample.con = null;
        String sret = APOPSample.getPass("TEST Prompt");
        Assertions.assertEquals(APOPSample.defaultPassword, sret,
                                "デフォルトパスワードを取得できませんでした");
    }
    
    @Test
    public void testCloseAPOP1() {
        target.pop3Store = null;
        target.myMailBox = null;
        target.closeAPOP();
    }
    
    @Test
    public void testCloseAPOP2() {
        Store pop3Store = Mockito.mock(Store.class);
        Folder myMailBox = Mockito.mock(Folder.class);
        target.pop3Store = pop3Store;
        target.myMailBox = myMailBox;

        target.closeAPOP();

        Assertions.assertNull(target.pop3Store,
                              "pop3Store のクリンナップが行われていません。");
        Assertions.assertNull(target.myMailBox,
                              "myMailBox のクリンナップが行われていません。");
    }
    
    @Test
    public void testCloseAPOP3() throws Exception{
        Store pop3Store = Mockito.mock(Store.class);
        target.pop3Store = pop3Store;
        Mockito.doThrow(new MessagingException(
                            "Store#close() 例外スローテスト"))
            .when(target.pop3Store).close();

        Folder myMailBox = Mockito.mock(Folder.class);
        target.myMailBox = myMailBox;
        Mockito.doThrow(new MessagingException(
                            "Foler#close() 例外スローテスト"))
            .when(target.myMailBox).close(ArgumentMatchers.anyBoolean());

        target.closeAPOP();

        Assertions.assertNull(target.pop3Store,
                              "pop3Store のクリンナップが行われていません。");
        Assertions.assertNull(target.myMailBox,
                              "myMailBox のクリンナップが行われていません。");
    }

    public void connectAPOPHelper() throws Exception{
        // Session のstatic メソッドの振る舞いを上書きする
        try(MockedStatic<Session> mockedStatic = Mockito.mockStatic(Session.class)){
            Session session = Mockito.mock(Session.class);
            mockedStatic.when(()->
                Session.getDefaultInstance(
                    ArgumentMatchers.any())).thenReturn(session);
        
            Folder myMailBox = Mockito.mock(Folder.class);

            Store pop3Store = Mockito.mock(Store.class);
            Mockito.when(
                pop3Store.getFolder("INBOX")).thenReturn(myMailBox);
            Mockito.when(
                session.getStore("pop3")).thenReturn(pop3Store);
        
            target.connectAPOP();

            Mockito.verify(session).getStore("pop3");
            Mockito.verify(pop3Store).connect(
                ArgumentMatchers.any(), ArgumentMatchers.any());
            Mockito.verify(pop3Store).getFolder("INBOX");
            Mockito.verify(myMailBox).open(Folder.READ_ONLY);
        }
    }
    
    /**
     * connectAPOP() をuseAPOP:true で実行するケース.
     */
    @Test
    public void testConnectAPOP1() throws Exception{
        connectAPOPHelper();
    }

    /**
     * connectAPOP() をuseAPOP:false で実行するケース.
     */
    @Test
    public void testConnectAPOP2() throws Exception{
        target.useAPOP = false;
        connectAPOPHelper();
    }

    /**
     * access() メッセージ0件で空振りするケース.
     */
    @Test
    public void testAccess1() throws Exception{
        Folder mock = Mockito.mock(Folder.class);
        Mockito.when(mock.getMessageCount()).thenReturn(0);
        target.myMailBox = mock;

        int ret = target.access();
        Assertions.assertEquals(0, ret, "access() 実行結果が異常です");
    }

    /**
     * access() 下記のようなメッセージ2件のケース.
     *
     * msg1 マルチパート 0) text/plain, 1) text/html, 2) application/xml
     * msg2 シングルパート
     */

    /*
     * Message をモックで作成するバージョン
    @Test
    public void testAccess2() throws Exception{
        Message msg = Mockito.mock(Message.class);
        Mockito.when(
            msg.getSubject()).thenReturn("ダミーSubject");
        Mockito.when(
            msg.getReceivedDate()).thenReturn(new Date());
        Mockito.when(
            msg.getSentDate()).thenReturn(new Date());
        Mockito.when(
            msg.getSize()).thenReturn(200);
        Address address = new InternetAddress("test@example.com");
        Address[] addresses = new Address[1];
        addresses[0] = address;
        Mockito.when(
            msg.getFrom()).thenReturn(addresses);

        Header header = new Header("H1", "V1");
        List<Header> headerArray = new ArrayList<Header>();
        headerArray.add(header);
        Mockito.when(
            msg.getAllHeaders()).thenReturn(
                Collections.enumeration(headerArray));

        Multipart multi = Mockito.mock(Multipart.class);
        Mockito.when(
            multi.getCount()).thenReturn(3);

        BodyPart body0 = Mockito.mock(BodyPart.class);
        Mockito.when(
            body0.getContentType()).thenReturn("text/plain ");
        Mockito.when(
            body0.getContent()).thenReturn("ダミー text/plain body 本体");
        multi.addBodyPart(body0);
        Mockito.when(
            multi.getBodyPart(0)).thenReturn(body0);

        BodyPart body1 = Mockito.mock(BodyPart.class);
        Mockito.when(
            body1.getContentType()).thenReturn("text/html ");
        Mockito.when(
            body1.getContent()).thenReturn("ダミー text/html body 本体");
        multi.addBodyPart(body1);
        Mockito.when(
            multi.getBodyPart(1)).thenReturn(body1);

        Mockito.when(
            msg.getContent()).thenReturn(multi);

        BodyPart body2 = Mockito.mock(BodyPart.class);
        Mockito.when(
            body2.getContentType()).thenReturn("application/xml part ");
        //Mockito.when(
        //    body2.getContent()).thenReturn("ダミー unknown/dumy body 本体");
        multi.addBodyPart(body2);
        Mockito.when(
            multi.getBodyPart(2)).thenReturn(body2);

        Mockito.when(
            msg.getContent()).thenReturn(multi);

        
        Message msg2 = Mockito.mock(Message.class);
        Mockito.when(
            msg2.getFrom()).thenReturn(addresses);
        Mockito.when(
            msg2.getContent()).thenReturn("シングルパートテキストメッセージ");
        Mockito.when(
            msg2.getAllHeaders()).thenReturn(
                Collections.enumeration(headerArray));
        
        Folder myMailBox = Mockito.mock(Folder.class);
        Mockito.when(
            myMailBox.getMessageCount()).thenReturn(2);
        Mockito.when(
            myMailBox.getMessage(1)).thenReturn(msg);
        Mockito.when(
            myMailBox.getMessage(2)).thenReturn(msg2);
        
        target.myMailBox = myMailBox;

        int ret = target.access();
        Assertions.assertEquals(0, ret, "access() 実行結果が異常です");
    }
    */
    
    /*
     * Message は本物オブジェクトで準備するバージョン.
     */
    @Test
    public void testAccess2() throws Exception{
        Session session = Session.getDefaultInstance(new Properties());

        //---- Message 1/2 ----
        MimeMessage msg = new MimeMessage(session);
        Address fromAddress = new InternetAddress("test@example.com");
        msg.setFrom(fromAddress);
        msg.setSubject("dumy Subject");
        msg.setSentDate(new Date());
        msg.setHeader("Header1", "value1");
        
        Multipart multi = new MimeMultipart("alternative");

        MimeBodyPart body0 = new MimeBodyPart();
        body0.setText("ダミー text/plain body 本体", "UTF-8");
        multi.addBodyPart(body0);

        MimeBodyPart body1 = new MimeBodyPart();
        body1.setContent("<html><body></body></html>",
                         "text/html; charset=UTF-8");
        multi.addBodyPart(body1);
        
        MimeBodyPart body2 = new MimeBodyPart();
        body2.setContent("<aaa>ダミー body 本体</aaa>", "application/xml");
        multi.addBodyPart(body2);

        msg.setContent(multi);

        // [重要]これがないと全部text/plain になってしまう
        // 参照→https://stackoverflow.com/questions/5028670/how-to-set-mimebodypart-contenttype-to-text-html
        msg.saveChanges();
        
        //---- Message 2/2 ----
        Message msg2 = new MimeMessage(session);
        msg2.setFrom(fromAddress);
        msg2.setSubject("dumy Subject");
        msg2.setContent("シングルパートテキストメッセージ", "text/plain; charset=UTF-8");
        msg2.setHeader("Header1", "value1");
        
        
        Folder mock = Mockito.mock(Folder.class);
        Mockito.when(mock.getMessageCount()).thenReturn(2);
        Mockito.when(mock.getMessage(1)).thenReturn(msg);
        Mockito.when(mock.getMessage(2)).thenReturn(msg2);
        
        target.myMailBox = mock;

        int ret = target.access();
        Assertions.assertEquals(0, ret, "access() 実行結果が異常です");
    }

    /**
     * access() 内部でIOException が発生するケースを網羅する.
     */
    @Test
    public void testAccess3() throws Exception{
        Message msg = Mockito.mock(Message.class);
        Mockito.when(
            msg.getSubject()).thenReturn("ダミーSubject");
        Mockito.when(
            msg.getReceivedDate()).thenReturn(new Date());
        Mockito.when(
            msg.getSentDate()).thenReturn(new Date());
        Mockito.when(
            msg.getSize()).thenReturn(200);
        Address address = new InternetAddress("test@example.com");
        Address[] addresses = new Address[1];
        addresses[0] = address;
        Mockito.when(
            msg.getFrom()).thenReturn(addresses);

        Header header = new Header("H1", "V1");
        List<Header> headerArray = new ArrayList<Header>();
        headerArray.add(header);
        Mockito.when(
            msg.getAllHeaders()).thenReturn(
                Collections.enumeration(headerArray));
        Mockito.when(msg.getContent()).thenThrow(new IOException(
                                  "getContent() のテスト用ダミー例外です."));
        Folder myMailBox = Mockito.mock(Folder.class);
        Mockito.when(
            myMailBox.getMessageCount()).thenReturn(1);
        Mockito.when(
            myMailBox.getMessage(1)).thenReturn(msg);
        
        target.myMailBox = myMailBox;
        target.access();
    }

    /**
     * access() がmyMailBox==null で実行された場合を網羅する.
     */
    @Test
    public void testAccess4() throws Exception{
        int ret = target.access();
        Assertions.assertEquals(1, ret, "access() 実行結果が異常です");
    }


    /**
     * main() の正常系実行をテストする。
     * <ol>
     *  <li> getPass() はConnect() がとれずデフォルトパスワードが返されるのでOK
     *  <li> connectAPOP() を通すためにmock を準備する
     *  <li> access() はmyMailBox.getMessageCount() に0 を返させ空振りさせる
     *  <li> closeAPOP() はモックのmyMailBox とpop3Store のclose() が何もしない
     * </ol>
     */
    @Test
    public void testMain1(){
        // Session のstatic メソッドの振る舞いを上書きする
        try(MockedStatic<Session> mockedStatic =
            Mockito.mockStatic(Session.class)){

            Session session = Mockito.mock(Session.class);
            mockedStatic.when(()->
                Session.getDefaultInstance(ArgumentMatchers.any()))
                .thenReturn(session);
        
            Folder myMailBox = Mockito.mock(Folder.class);
            
            // access() ではメッセージ0通として空振りさせる
            Mockito.when(
                myMailBox.getMessageCount()).thenReturn(0);

            Store pop3Store = Mockito.mock(Store.class);
            Mockito.when(
                pop3Store.getFolder("INBOX")).thenReturn(myMailBox);
            Mockito.when(
                session.getStore("pop3")).thenReturn(pop3Store);
        
            String[] args = new String[]{"pop.example.com", "test-user"};
            APOPSample.main(args);
        
            Mockito.verify(session).getStore("pop3");
            Mockito.verify(pop3Store).connect(
                "test-user", APOPSample.defaultPassword);
            Mockito.verify(pop3Store).getFolder("INBOX");
            Mockito.verify(myMailBox).open(Folder.READ_ONLY);
        } catch(MessagingException ex){
            LOGGER.error("testMain1() で例外が発生.", ex);
            Assertions.fail("testMain1() で例外が発生.");
        }
    }
    
    /**
     * main() の異常系実行をテストする。
     *
     * connectAPOP() 中のsession.getStore() で例外を発生させ、main() 中の
     * catch 節に入ることを網羅する.
     */
    @Test
    public void testMain2(){
        /*-----
        // クラスに「@ExtendWith(MockitoExtension.class)」を付与しておくと
        // このような未使用のモックがあるとMocito2 がエラーにしてくれる.
        Folder f = Mockito.mock(Folder.class);
        try{
            Mockito.when(f.getMessageCount()).thenReturn(0);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        -----*/
        boolean executedMain = false;
        // Session のstatic メソッドの振る舞いを上書きする
        try(MockedStatic<Session> mockedStatic =
            Mockito.mockStatic(Session.class)){

            Session session = Mockito.mock(Session.class);

            try{
                // session.getStore() で例外を発生させる.
                // 例外はmain() の中でcatch して異常処理(ログ出力)している
                Mockito.when(session.getStore("pop3"))
                    .thenThrow(new NoSuchProviderException(
                                                  "異常テスト用の例外です."));
            } catch(NoSuchProviderException ex) {
                LOGGER.error("例外が発生.", ex);
                Assertions.fail(
                    "testMain2() モックに例外スロー設定中に異常発生.");
            }
            mockedStatic.when(()->
                Session.getDefaultInstance(ArgumentMatchers.any()))
                .thenReturn(session);

        
            String[] args = new String[]{"pop.example.com", "test-user"};
            APOPSample.main(args);
        
            executedMain = true; // ここまできたことを確認したい
        }
        Assertions.assertTrue(executedMain);
        Assertions.assertNull(target.pop3Store);
        Assertions.assertNull(target.myMailBox);
    }

    /**
     * 引数不足のケースを網羅.
     */
    @Test
    public void testMain3(){
        String[] args = new String[0];
        APOPSample.main(args);
    }
}

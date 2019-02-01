/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package email;

import cassini.PrincipalJFrame;
import gerador.sas.SASActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;
import javax.swing.JOptionPane;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;

/**
 *
 * @author danieloliveira
 */
public class EmailInterface extends TimerTask {

    private String user;
    private String password;
    private String smtpHost;
    private int smtpPort;
    private String imapHost;
    private int imapPort;
    private String servidor;
    private String exchangeHost;
    private ExchangeVersion exchangeVersion;
    private String pastaValidados;
    private String pastaRecebidos;
    private Object[] contatos;
    private int apagar;
    private SASActionListener sasActionListener;
    
    private final String coletaXSD = "\\\\Servicesdata\\prpe$\\Dados\\Projeto Piloto\\xml\\coleta.xsd";
    //private final String coletaXSD = "http://sistemasds.anatel.gov.br/dici/coleta.xsd";

    public EmailInterface(String password) {
        Properties props = new Properties();
        try {
            File propsFile = new File("./properties/.properties");
            if (!propsFile.isFile()) {
                propsFile.getParentFile().mkdir();
                propsFile.createNewFile();
            }
            FileInputStream file = new FileInputStream("./properties/.properties");
            props.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        this.smtpHost = props.getProperty("cassini.smtp.host");
        this.smtpPort = Integer.parseInt(props.getProperty("cassini.smtp.port"));
        this.imapHost = props.getProperty("cassini.imap.host");
        this.imapPort = Integer.parseInt(props.getProperty("cassini.imap.port"));
        this.user = props.getProperty("cassini.usuario.email");
        this.password = password;
        this.contatos = props.getProperty("cassini.contacts").split(";");
        this.servidor = props.getProperty("cassini.servidor");
        this.exchangeHost = props.getProperty("cassini.exchange.host");
        String apagarEmails = props.getProperty("cassini.emails.apagar");
        switch (apagarEmails) {
            case "Todos do Cassini":
                apagar = EmailHandler.APAGAR_EMAILS_CASSINI;
                break;
            case "Todos":
                apagar = EmailHandler.APAGAR_TODOS_EMAILS;
                break;
            default:
                apagar = EmailHandler.NAO_APAGAR_EMAILS;
        }
        String versao = props.getProperty("cassini.exchange.versao");
        switch (versao) {
            case "2007 SP1":
                exchangeVersion = ExchangeVersion.Exchange2007_SP1;
                break;
            case "2010":
                exchangeVersion = ExchangeVersion.Exchange2010;
                break;
            case "2010 SP1":
                exchangeVersion = ExchangeVersion.Exchange2010_SP1;
                break;
            case "2010 SP2":
                exchangeVersion = ExchangeVersion.Exchange2010_SP2;
                break;
            default:
                exchangeVersion = null;
                break;
        }
        this.pastaRecebidos = props.getProperty("cassini.recebidos.folder");
        this.pastaValidados = props.getProperty("cassini.validados.folder");
    }

    public void addSASActionListener(SASActionListener sasActionListener){
        this.sasActionListener = sasActionListener;
    }
    
    @Override
    public void run() {
        switch (servidor) {
            case "MS Exchange":
                runExchange();
                break;
            default:
                runSMTP_IMAP();
                break;
        }
    }

    private synchronized void runExchange() {
        RobotExchange robot = new RobotExchange(exchangeVersion, exchangeHost, "[Cassini]");
        if (robot.conectar(user, password)) {
            List<EmailMessage> emails = robot.getEmails(RobotExchange.INBOX, Integer.MAX_VALUE, 0, false);
            robot.desconectar();
            PrincipalJFrame.addLog("Lendo " + emails.size() + " e-mail(s).");
            for (EmailMessage email : emails) {
                EmailHandler emailHandler = new EmailHandler(email,"[Cassini]",password);
                emailHandler.addSASActionListener(sasActionListener);
                emailHandler.start();
            }
        } else {
            JOptionPane.showMessageDialog(null, "Não foi possível conectar ao servidor de MS Exchange.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private synchronized void runSMTP_IMAP() {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        props.setProperty("mail.smtp.ssl.trust", "*");
        props.setProperty("mail.imap.ssl.trust", "*");
        try {
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(imapHost, imapPort, user, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] messages = inbox.search(unseenFlagTerm);
            System.err.println("Lendo " + messages.length + " e-mail(s).");
            for (int i = 0; i < messages.length; i++) {
                new EmailHandler(messages[i].getSentDate(), messages[i].getSubject(), InternetAddress.toString(messages[i].getFrom()), password).start();
            }
            inbox.close(false);
            store.close();
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package email;

import cassini.PrincipalJFrame;
import gerador.Erro;
import gerador.GeradorScriptSAS;
import gerador.SegurancaException;
import gerador.sas.SASActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FlagTerm;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.BasePropertySet;
import microsoft.exchange.webservices.data.core.enumeration.property.BodyType;
import microsoft.exchange.webservices.data.core.exception.service.local.ServiceLocalException;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.property.complex.AttachmentCollection;
import microsoft.exchange.webservices.data.property.complex.FileAttachment;
import microsoft.exchange.webservices.data.search.ItemView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import sas.SASInterface;

/**
 *
 * @author danieloliveira
 */
public class EmailHandler extends Thread {

    private String imapHost;
    private int imapPort;
    private String smtpHost;
    private int smtpPort;
    private String sasServer;
    private int sasPort;
    private String user;
    private String password;
    private Date sentDate;
    private String subject;
    private String from;
    private ExchangeVersion exchangeVersion;
    private String exchangeServer = null;
    private String servico;
    private EmailMessage email;
    private String pastaRecebidos;
    private String pastaValidados;
    private String pastaLeiautes;
    private SASActionListener sasActionListener;

    public static final int NAO_APAGAR_EMAILS = 0;
    public static final int APAGAR_TODOS_EMAILS = 1;
    public static final int APAGAR_EMAILS_CASSINI = 2;
    private int apagar;

    private final String coletaXSD = "\\\\Servicesdata\\prpe$\\Dados\\Projeto Piloto\\xml\\coleta.xsd";
    //private final String coletaXSD = "http://sistemasds.anatel.gov.br/dici/coleta.xsd";

    //<editor-fold defaultstate="collapsed" desc="Constutor SMTP">
    public EmailHandler(Date sentDate, String subject, String from, String password) {
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
        this.pastaLeiautes = props.getProperty("cassini.leiautes.folder");

        this.password = password;
        this.sentDate = sentDate;
        this.subject = subject;
        this.from = from;

        this.pastaRecebidos = props.getProperty("cassini.recebidos.folder");
        System.err.println(pastaRecebidos);
        this.pastaValidados = props.getProperty("cassini.validados.folder");
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
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Construtor Exchange">
    public EmailHandler(EmailMessage email, String servico, String password) {
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

        this.email = email;
        this.exchangeServer = props.getProperty("cassini.exchange.host");
        this.user = props.getProperty("cassini.usuario.email");
        this.pastaLeiautes = props.getProperty("cassini.leiautes.folder");
        this.password = password;
        this.sasServer = props.getProperty("cassini.sas.host");
        try {
            this.sasPort = Integer.parseInt(props.getProperty("cassini.sas.port"));
        } catch (Exception ex) {
        }
        this.servico = servico;

        String versao = props.getProperty("cassini.exchange.versao");
        switch (versao) {
            case "2007 SP1":
                this.exchangeVersion = ExchangeVersion.Exchange2007_SP1;
                break;
            case "2010":
                this.exchangeVersion = ExchangeVersion.Exchange2010;
                break;
            case "2010 SP1":
                this.exchangeVersion = ExchangeVersion.Exchange2010_SP1;
                break;
            case "2010 SP2":
                this.exchangeVersion = ExchangeVersion.Exchange2010_SP2;
                break;
            default:
                this.exchangeVersion = null;
                break;
        }
        this.pastaRecebidos = props.getProperty("cassini.recebidos.folder");
        this.pastaValidados = props.getProperty("cassini.validados.folder");
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
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="runSMTP_IMAP">
    private void runSMTP_IMAP() {
        boolean erro = false;
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            //props.put("mail.smtp.starttls.enable", "false");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", "" + smtpPort);
            props.setProperty("mail.store.protocol", "imaps");
            props.setProperty("mail.smtp.ssl.trust", "*");
            props.setProperty("mail.imap.ssl.trust", "*");
            /*props.setProperty("proxySet", "true");
             props.setProperty("socksProxyHost", "");
             props.setProperty("socksProxyPort", "");*/
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect(imapHost, imapPort, user, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Flags seen = new Flags(Flags.Flag.SEEN);
            FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
            Message[] messages = inbox.search(unseenFlagTerm);

            Message message = null;
            for (int i = 0; i < messages.length; i++) {
                if (messages[i].getSentDate().equals(sentDate) && messages[i].getSubject().equals(subject) && InternetAddress.toString(messages[i].getFrom()).equals(from)) {
                    message = messages[i];
                    i = messages.length;
                }
            }

            if (message != null) {
                String from = InternetAddress.toString(message.getFrom());
                String to = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
                Multipart multiPart = (Multipart) message.getContent();
                BodyPart bp = multiPart.getBodyPart(0);
                String subject = message.getSubject().substring(message.getSubject().indexOf("]") + 1) + ":";
                String solicitacao = null;
                String parametros = null;
                try {
                    solicitacao = subject.split(":")[0].toUpperCase();
                    parametros = subject.split(":")[1].toUpperCase();
                } catch (Exception ex) {

                }
                //Message replyMessage = new MimeMessage(session);
                Calendar calendar = Calendar.getInstance();
                PrincipalJFrame.addLog("Lendo email: " + subject + " de " + from);
                String response = null;
                for (int j = 0; j < multiPart.getCount(); j++) {
                    MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(j);
                    String anexo = null;
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        anexo = pastaRecebidos + Calendar.getInstance().getTimeInMillis() + "_" + part.getFileName();

                        new File(pastaRecebidos).mkdir();
                        new File(anexo).createNewFile();

                        FileOutputStream output = new FileOutputStream(anexo);

                        InputStream input = part.getInputStream();

                        byte[] buffer = new byte[4096];

                        int byteRead;

                        while ((byteRead = input.read(buffer)) != -1) {
                            output.write(buffer, 0, byteRead);
                        }
                        output.close();
                        j = multiPart.getCount();
                    }
                    response = getResponse(solicitacao, parametros);
                }
                if (response != null) {
                    Message replyMessage = (MimeMessage) message.reply(false);
                    replyMessage.setFrom(new InternetAddress(to));
                    replyMessage.setReplyTo(message.getReplyTo());
                    Transport transport = session.getTransport("smtps");
                    try {
                        transport.connect(smtpHost, smtpPort, user, password);
                        transport.sendMessage(replyMessage, replyMessage.getAllRecipients());
                    } finally {
                        transport.close();
                    }
                }
                message.setFlag(Flags.Flag.DELETED, true);
                inbox.close(false);
                //deleteEmails(session,store.getFolder("").list());
                store.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="runExchange">
    private void runExchange() throws Exception {
        RobotExchange robot = new RobotExchange(exchangeVersion, exchangeServer, servico);
        robot.conectar(user, password);
        email = (EmailMessage) robot.getEmail(email.getId());
        email.load();
        //String subject = message.getSubject().substring(message.getSubject().indexOf("]")) + ":";
        String subject = email.getSubject().substring(email.getSubject().indexOf("]") + 1) + ":";
        String from = email.getSender().getAddress();
        String solicitacao = null;
        String parametros = null;
        try {
            solicitacao = subject.split(":")[0].toUpperCase();
            parametros = subject.split(":")[1].toUpperCase();
        } catch (Exception ex) {

        }
        PrincipalJFrame.addLog("Lendo email: " + subject + " de " + from);

        robot.responderEmail(email.getId(), getResponse(solicitacao, parametros), apagar != APAGAR_EMAILS_CASSINI && apagar != APAGAR_TODOS_EMAILS);
        if (apagar == APAGAR_EMAILS_CASSINI) {
            robot.apagarEmail(email.getId());
        } else if (apagar == APAGAR_TODOS_EMAILS) {
            //robot.apagarTodosEmails();
        }
        robot.desconectar();

    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="run">
    @Override
    public void run() {
        if (exchangeServer != null) {
            try {
                runExchange();
            } catch (Exception ex) {
                Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            runSMTP_IMAP();
        }
    }
//</editor-fold>

    private void deleteSMTP_IMAPEmails(Session session, Folder[] folders) throws MessagingException {
        for (Folder folder : folders) {
            try {
                folder.open(Folder.READ_WRITE);
                folder.setFlags(folder.getMessages(), new Flags(Flags.Flag.DELETED), true);
                folder.close(false);
            } catch (Exception ex) {
                deleteSMTP_IMAPEmails(session, session.getStore().getFolder(folder.getFullName()).list());
            }
        }
    }

    private String getResponse(String solicitacao, String parametros) {
        String response = null;
        if (solicitacao != null) {
            switch (solicitacao) {
                case "ENVIAR ARQUIVO":
                    response = receiveFile(from, parametros);
                    break;
                case "ADICIONAR USUÁRIO":
                    response = addUser(from, parametros);
                    break;
                case "ME ADICIONAR":
                    response = addSelfUser(from, parametros);
                    break;
                case "LISTAR USUÁRIOS":
                    response = getUsers(from, parametros);
                    break;
                case "LISTAR LEIAUTES":
                    response = getLeiautes(from, parametros);
                    break;
                case "DETALHAR LEIAUTE":
                    response = getLeiaute(from, parametros);
                    break;
                case "LISTAR DOMÍNIOS":
                    response = getDominios(from, parametros);
                    break;
                case "DETALHAR DOMÍNIO":
                    response = getDominio(from, parametros);
                    break;
                case "CONTATO":
                    response = contato(from, parametros);
                    break;
                case "LISTAR ERROS":
                    response = listarErros(from, parametros);
                    break;
                default:
                    response = usage();
                    break;
            }
        }
        return response;
    }

    //<editor-fold defaultstate="collapsed" desc="Instruções">
    /**
     * Retorna uso correto do aplicado caso tenha recebido mensagem errada.
     */
    private String usage() {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append(bomDia());

        mensagem.append("Esta é uma mensagem automática. Você enviou um email pedindo ajuda para o Cassini, ou sua mensagem não foi reconhecida. O Cassini lê o que consta no campo Assunto do e-mail e executa uma tarefa de acordo. Segue abaixo os valores aceitos de Assunto e a respectiva funcionalidade. Se for encaminhado no Assunto qualquer outro valor, o Cassini irá desconsiderar a mensagem e apagá-la.<br><br>");

        mensagem.append(instrucaoEnviarArquivo());
        mensagem.append(instrucaoListarUsuarios());
        mensagem.append(instrucaoAdicionarUsuario());
        mensagem.append(instrucaoMeAdicionar());
        mensagem.append(instrucaoListarLeiautes());
        mensagem.append(instrucaoDetalharLeiaute());
        mensagem.append(instrucaoListarDominios());
        mensagem.append(instrucaoDetalharDominio());
        mensagem.append(instrucaoContato());
        mensagem.append(instrucaoAjuda());

        mensagem.append(atenciosamente());

        return mensagem.toString();

    }

    private String instrucaoEnviarArquivo() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Enviar Arquivo:coleta|domínio;nome_leiaute</b>: para enviar um arquivo CSV com dados. O texto \"coleta|domínio\" deve ser substituído por \"coleta\" caso esteve enviando um arquivo de uma coleta, ou por \"domínio\" caso esteve enviando novos dados para um domínio. O texto \"nome_leiaute\" deve ser substituído pelo nome do leiaute referente ao arquivo, e anexe o arquivo no e-mail.<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br>");
        mensagem.append("&emsp;&emsp;o	Os arquivos suportados são CSV e ZIP.<br>");
        mensagem.append("&emsp;&emsp;o	Caso seu arquivo seja grande demais para ser enviado por email, você pode colocar no corpo do e-mail o link para acessar o arquivo. O link deverá ser escrito entre << e >>. O link deverá indicar o protocolo de acesse a ser utilizado (http, ftp, file etc). Você poderá apagar o link assim que receber nossa resposta automática (pode demorar alguns minutos). Sugerimos que o link seja acessível apenas por solicitações vinda de anatel.gov.br para sua segurança.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoListarUsuarios() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Listar Usuários:ID1;ID2;ID3</b>: para listar quais usuários tem permissão de enviar arquivos de determinada empresa ou provedora de dados. Cada ID deve ser substituído pela identificação da empresa ou provedora de dados, podendo ser um CNPJ ou CPF sem pontos ou travessões (Por exemplo: 11111111000101).<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br>");
        mensagem.append("&emsp;&emsp;o	Limite de 3 IDs por solicitação.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoAdicionarUsuario() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Adicionar Usuário:leiaute;novo_usuario;parametro;valor</b>: para fornecer permissões de envio de arquivos a um usuário novo. O texto \"leiaute\" deve ser substituido pelo nome do leiaute, o texto “novo_usuario” deve ser substituído pelo email do novo usuário, “parametro” deve ser substituído pelo nome do parâmetro da coleta (Ex.: cnpj, empresa etc) e \"valor\" deve ser substituído pelo valor do parâmetro indicado (ex. 01123123000112).<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoMeAdicionar() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Me adicionar:leiaute;seu_email;parametro;valor</b>: solicitar permissão para enviar arquivos de determinada empresa ou provedora de dados. O texto \"leiaute\" deve ser substituido pelo nome do leiaute, o texto “novo_usuario” deve ser substituído pelo email do novo usuário, “parametro” deve ser substituído pelo nome do parâmetro da coleta (Ex.: cnpj, empresa etc) e \"valor\" deve ser substituído pelo valor do parâmetro indicado (ex. 01123123000112).<br>");
        mensagem.append("&emsp;&emsp;o	Um funcionário da Anatel irá receber sua solicitação e providenciará a liberação de acesso.<br><br>");
        mensagem.append("&emsp;&emsp;o	No corpo do email, escreva detalhes de sua solicitação, como seu nome, telefone de contato e demais informações que ajudem a sua solicitação a ser aceita.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoListarLeiautes() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Listar Leiautes:regex</b>: para receber uma lista de leiautes de arquivos de dados suportados. O texto “regez” deve ser substituído por uma expressão regular a ser pesquisada.<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br>");
        mensagem.append("&emsp;&emsp;o	Por exemplo, caso queira receber todos os leiautes, escreva “Listar Leiautes:.*”. Caso queira receber todos os leiautes referentes a SCM, escreva “Listar Leiautes:.*SCM.*”.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoDetalharLeiaute() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Detalhar Leiaute:leiaute</b>: para obter detalhes sobre um leiaute específico. O texto “leiaute” deve ser substituído pelo nome do leiaute desejado.<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoListarDominios() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Listar Domínios:filtro</b>: para receber uma lista de domínios de dados utilizados pelo Cassini. O texto “filtro” deve ser substituído por um texto a ser pesquisado.<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br>");
        mensagem.append("&emsp;&emsp;o	Por exemplo, caso queira receber todos os domínios, escreve “Listar Domínios:.*”.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoDetalharDominio() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Detalhar Domínio:dominio</b>: para obter detalhes sobre um domínio específico utilizado pelo Cassini. O texto \"dominio\" deve ser substituído pelo nome do domínio desejado.<br>");
        mensagem.append("&emsp;&emsp;o	O corpo do e-mail pode ficar vazio.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoContato() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Contato:leiaute|dominio</b>: para enviar um e-mail diretamente para um de nossos funcionários responsável pelo leiaute ou domínio desejado. O texto \"leiaute|dominio\" deve ser substituído pelo nome do leiaute ou domínio desejado. Um de nossos funcionários irá receber o e-mail e entrar em contato com você.<br>");
        mensagem.append("&emsp;&emsp;o	Escreva sua mensagem no corpo do e-mail.<br><br>");
        mensagem.append("&emsp;&emsp;o	Caso queira entrar em contato com o responsável por essa instância do Cassini, deixe o texto \"leiaute|dominio\" em branco.<br><br>");

        return mensagem.toString();
    }

    private String instrucaoAjuda() {
        StringBuilder mensagem = new StringBuilder();

        mensagem.append("&emsp;•	<b>[Cassini]Ajuda</b>: para obter esta mensagem atualizada, pois novas funcionalidades serão adicionadas no futuro.<br><br>");

        return mensagem.toString();
    }
//</editor-fold>

    /**
     * Recebe o arquivo enviado
     */
    private String receiveFile(String from, String parametros) {
        StringBuilder mensagem = new StringBuilder();
        String anexo = null;
        try {

            if (email.getHasAttachments()) {
                AttachmentCollection attachmentsCol = email.getAttachments();
                FileAttachment attachment = (FileAttachment) attachmentsCol.getPropertyAtIndex(0);
                anexo = pastaRecebidos + Calendar.getInstance().getTimeInMillis() + "_" + attachment.getName();
                attachment.load(anexo);
            } else {
                PropertySet itemPropertySet = new PropertySet(BasePropertySet.FirstClassProperties);
                itemPropertySet.setRequestedBodyType(BodyType.Text);
                ItemView itemview = new ItemView(1000);
                itemview.setPropertySet(itemPropertySet);
                email.load(itemPropertySet);
                String body = new String(email.getBody().toString().getBytes("UTF-8"));
                int linkInicio = body.indexOf("<<");
                int linkFim = -1;
                if (linkInicio != -1) {
                    linkFim = body.indexOf(">>");
                }
                if (linkInicio != -1 && linkFim != -1) {
                    String link = body.substring(linkInicio + 2, linkFim + 1);
                    linkInicio = -1;
                    linkFim = -1;
                    linkInicio = link.indexOf("<");
                    linkFim = link.indexOf(">");
                    String texto = "";
                    if (linkInicio != -1) {
                        texto = link.substring(0, linkInicio);
                        link = link.substring(linkInicio + 1, linkFim);
                    } else if (linkFim != -1) {
                        texto = link.substring(0, linkFim);
                        link = texto;
                    }
                    texto = texto.replaceAll("\\\\", "/").replaceAll("%20", " ");
                    link = link.replaceAll("\\\\", "/").replaceAll("%20", " ");
                    if (link.startsWith("//")) {
                        link = "file://" + link;
                    }
                    if (link.startsWith("file://///")) {
                        link = "file://" + link.substring(8, link.length());
                    }
                    if (texto.startsWith("//")) {
                        texto = "file://" + texto;
                    }
                    if (texto.startsWith("file://///")) {
                        texto = "file://" + texto.substring(8, texto.length());
                    }
                    InputStream inputStream = null;
                    String[] arquivoNome = null;
                    try {
                        inputStream = new URL(link).openStream();
                        arquivoNome = link.split("/");
                    } catch (Exception ex) {
                    }
                    if (inputStream == null) {
                        try {
                            inputStream = new URL(texto).openStream();
                            arquivoNome = texto.split("/");
                            link = texto;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        ReadableByteChannel rbc = Channels.newChannel(inputStream);
                        anexo = pastaRecebidos + Calendar.getInstance().getTimeInMillis() + "_" + arquivoNome[arquivoNome.length - 1];
                        FileOutputStream fos = new FileOutputStream(anexo);
                        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                        fos.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new Exception("Erro ao ler link: " + link + "\n" + ex.getMessage());
                    }
                }
            }
            if (anexo != null) {
                if (parametros != null) {
                    if (parametros.split(";").length != 2 && parametros.split(";").length != 3) {
                        new File(anexo).delete();
                        mensagem.append(bomDia());
                        mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto não identificamos os parâmetros necessários para receber o arquivo. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                        mensagem.append(instrucaoEnviarArquivo());
                        mensagem.append(atenciosamente());
                    } else {
                        try {
                            boolean coleta = !parametros.split(";")[0].toUpperCase().equals("DOMÍNIO");
                            String leiaute = parametros.split(";")[1];
                            boolean ignorar = parametros.split(";").length == 3 ? parametros.split(";")[2].toUpperCase().equals("IGNORAR") : false;
                            String fileType = Files.probeContentType(new File(anexo).toPath());
                            if (fileType.equals("application/vnd.ms-excel")) {
                                mensagem.append(validarArquivo(leiaute, anexo, coleta, ignorar));
                            } else if (fileType.equals("application/x-zip-compressed")) {
                                List<String> arquivos = descompactarArquivo(anexo);
                                //pegar apenas o primeiro arquivo. Se houver mais, ignora.
                                if (arquivos.size() == 1) {
                                    fileType = Files.probeContentType(new File(arquivos.get(0)).toPath());
                                    if (fileType.equals("application/vnd.ms-excel")) {
                                        mensagem.append(validarArquivo(leiaute, new File(arquivos.get(0)).getAbsolutePath(), coleta, ignorar));
                                    } else {
                                        new File(arquivos.get(0)).delete();
                                        mensagem.append(bomDia());
                                        mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto o Cassini não é compatível com o tipo de arquivo enviado. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                                        mensagem.append(instrucaoEnviarArquivo());
                                        mensagem.append(atenciosamente());
                                    }
                                } else {
                                    for (String arq : arquivos) {
                                        new File(arq).delete();
                                    }
                                    mensagem.append(bomDia());
                                    mensagem.append("Você tentou enviar um arquivo para o Cassini, o arquivo .zip que recebemos possuía mais de 1 arquivo dentro. Envie apenas 1 arquivo CSV em seu arquivo ZIP por e-mail. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                                    mensagem.append(instrucaoEnviarArquivo());
                                    mensagem.append(atenciosamente());
                                }
                            } else {
                                new File(anexo).delete();
                                mensagem.append(bomDia());
                                mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto o Cassini não é compatível com o tipo de arquivo enviado. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                                mensagem.append(instrucaoEnviarArquivo());
                                mensagem.append(atenciosamente());
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                } else {
                    new File(anexo).delete();
                    mensagem.append(bomDia());
                    mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto não identificamos os parâmetros necessários para receber o arquivo. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                    mensagem.append(instrucaoEnviarArquivo());
                    mensagem.append(atenciosamente());
                }
            } else {
                mensagem.append(bomDia());
                mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto não encontramos nenhum anexo ou link na sua mensagem. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
                mensagem.append(instrucaoEnviarArquivo());
                mensagem.append(atenciosamente());
            }
        } catch (ServiceLocalException ex) {
            Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
            if (anexo != null) {
                new File(anexo).delete();
            }
            mensagem = new StringBuilder();
            mensagem.append(bomDia());
            mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto ocorreu um erro ao tentar ler o anexo do e-mail. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
            mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
            mensagem.append(instrucaoEnviarArquivo());
            mensagem.append(atenciosamente());
        } catch (MalformedURLException ex) {
            Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
            if (anexo != null) {
                new File(anexo).delete();
            }
            mensagem = new StringBuilder();
            mensagem.append(bomDia());
            mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto o link informado não foi reconhecido. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
            mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
            mensagem.append(instrucaoEnviarArquivo());
            mensagem.append(atenciosamente());
        } catch (IOException ex) {
            Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
            if (anexo != null) {
                new File(anexo).delete();
            }
            mensagem = new StringBuilder();
            mensagem.append(bomDia());
            mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto houve um erro ao tentar salvar o arquivo enviado. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
            mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
            mensagem.append(instrucaoEnviarArquivo());
            mensagem.append(atenciosamente());
        } catch (Exception ex) {
            Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
            if (anexo != null) {
                new File(anexo).delete();
            }
            mensagem = new StringBuilder();
            mensagem.append(bomDia());
            mensagem.append("Você tentou enviar um arquivo para o Cassini, entretanto houve um erro ao processar sua solicitação. Segue abaixo instruções de como utilizar a funcionalidade de envio de arquivos do Cassini.<br><br>");
            mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
            mensagem.append(instrucaoEnviarArquivo());
            mensagem.append(atenciosamente());
        }
        return mensagem.toString();
    }

    private String validarArquivo(String leiaute, String arquivo, boolean coleta, boolean ignorar) {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("<head>");
        mensagem.append("<style>");
        mensagem.append("table {");
        mensagem.append("    font-family: arial, sans-serif;");
        mensagem.append("    border-collapse: collapse;");
        mensagem.append("    width: 100%;");
        mensagem.append("}");
        mensagem.append("td, th {");
        mensagem.append("    border: 1px solid #dddddd;");
        mensagem.append("    text-align: left;");
        mensagem.append("    padding: 8px;");
        mensagem.append("}");
        mensagem.append("tr:nth-child(even) {");
        mensagem.append("    background-color: #dddddd;");
        mensagem.append("}");
        mensagem.append("</style>");
        mensagem.append("</head>");
        mensagem.append("<body>");
        mensagem.append(bomDia());

        String leiauteFile = getLeiaute(leiaute, coleta);

        if (leiauteFile != null) {
            List<File> arquivosCSV = new ArrayList();
            File arquivoCSV = new File(arquivo);
            arquivosCSV.add(arquivoCSV);
            GeradorScriptSAS geradorScriptSAS = new GeradorScriptSAS(sasServer, sasPort, user.split("@")[0], password);
            geradorScriptSAS.addSASActionListener(sasActionListener);
            try {
                List<Erro> erros = geradorScriptSAS.gerarScriptSAS(email.getSender().getAddress(), arquivosCSV, new File(leiauteFile));
                if (erros.isEmpty()) {
                    SASInterface sasInterface = new SASInterface(user.split("@")[0], password, sasServer, sasPort);
                    sasInterface.addSASActionListener(sasActionListener);
                    List<Element> coletasTag = getLeiautes(coleta);
                    Element coletaTag = null;
                    for (Element tag : coletasTag) {
                        Element scriptTag = (Element) tag.getElementsByTagName("script").item(0);
                        if (scriptTag.getAttribute("leiaute").toUpperCase().equals(leiaute)) {
                            coletaTag = tag;
                        }
                    }
                    if (coleta) {
                        try {
                            if (sasInterface.atualizarDados(coletaTag, arquivo, email.getFrom().getAddress(), email.getDateTimeSent())) {
                                mensagem.append("Seu arquivo foi recebido com sucesso. Obrigado.<br><br>");
                                System.gc();
                                try {
                                    Files.move(arquivoCSV.toPath(), new File(pastaValidados + arquivoCSV.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
                                } catch (Exception ex) {

                                }
                            } else {
                                mensagem.append("Ocorreu um erro ao tentar cadastrar o arquivo em nosso banco de dados. Favor tentar novamente mais tarde.<br><br>");

                                System.gc();

                                arquivoCSV.delete();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            mensagem.append("Ocorreu um erro.<br><br>");
                            mensagem.append("Erro:" + ex.getMessage() + "<br><br>");
                        }
                    } else {
                        try {
                            if (sasInterface.insertDados(coletaTag, arquivo, email.getFrom().getAddress(), email.getDateTimeSent())) {
                                mensagem.append("Seu arquivo foi recebido com sucesso. Obrigado.<br><br>");
                                System.gc();
                                try {
                                    Files.move(arquivoCSV.toPath(), new File(pastaValidados + arquivoCSV.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
                                } catch (Exception ex) {

                                }
                            } else {
                                mensagem.append("Ocorreu um erro ao tentar cadastrar o arquivo em nosso banco de dados. Favor tentar novamente mais tarde.<br><br>");
                                System.gc();

                                arquivoCSV.delete();
                            }
                        } catch (Exception ex) {
                            mensagem.append("Ocorreu um erro.<br><br>");
                            mensagem.append("Erro:" + ex.getMessage() + "<br><br>");
                            arquivoCSV.delete();
                        }
                    }
                } else {

                    StringBuilder tabelaErrosNaoAceitaveis = new StringBuilder();
                    StringBuilder tabelaErrosAceitaveis = new StringBuilder();
                    tabelaErrosAceitaveis.append("<table>");
                    tabelaErrosAceitaveis.append("<tr>");
                    tabelaErrosAceitaveis.append("<th>Método</th>");
                    tabelaErrosAceitaveis.append("<th>Parâmetros</th>");
                    tabelaErrosAceitaveis.append("<th>Mensagem</th>");
                    tabelaErrosAceitaveis.append("<th>Tipo de Erro</th>");
                    tabelaErrosAceitaveis.append("</tr>");
                    tabelaErrosNaoAceitaveis.append("<table>");
                    tabelaErrosNaoAceitaveis.append("<tr>");
                    tabelaErrosNaoAceitaveis.append("<th>Método</th>");
                    tabelaErrosNaoAceitaveis.append("<th>Parâmetros</th>");
                    tabelaErrosNaoAceitaveis.append("<th>Mensagem</th>");
                    tabelaErrosNaoAceitaveis.append("<th>Tipo de Erro</th>");
                    tabelaErrosNaoAceitaveis.append("</tr>");
                    boolean errosNaoAceitaveis = false;
                    boolean errosAceitaveis = false;
                    int contadorErrosAceitaveis = 0;
                    int contadorErrosNaoAceitaveis = 0;
                    boolean verificaAcesso = false;
                    for (Erro erro : erros) {
                        if (erro.getMetodo().equals("verificaAcesso")) {
                            verificaAcesso = true;
                        }
                        if (erro.isAceitavel()) {
                            contadorErrosAceitaveis++;
                            errosAceitaveis = true;
                            if (contadorErrosAceitaveis <= 500) {
                                tabelaErrosAceitaveis.append("<tr>");
                                tabelaErrosAceitaveis.append("<th>").append(erro.getMetodo()).append("</th>");
                                tabelaErrosAceitaveis.append("<th>").append(erro.getParametros()).append("</th>");
                                tabelaErrosAceitaveis.append("<th>").append(erro.getMensagem()).append("</th>");
                                tabelaErrosAceitaveis.append("<th>").append("Aceitável").append("</th>");
                                tabelaErrosAceitaveis.append("</tr>");
                            }
                        } else {
                            contadorErrosNaoAceitaveis++;
                            errosNaoAceitaveis = true;
                            if (contadorErrosNaoAceitaveis <= 500) {
                                tabelaErrosNaoAceitaveis.append("<tr>");
                                tabelaErrosNaoAceitaveis.append("<th>").append(erro.getMetodo()).append("</th>");
                                tabelaErrosNaoAceitaveis.append("<th>").append(erro.getParametros()).append("</th>");
                                tabelaErrosNaoAceitaveis.append("<th>").append(erro.getMensagem()).append("</th>");
                                tabelaErrosNaoAceitaveis.append("<th>").append("Não aceitável").append("</th>");
                                tabelaErrosNaoAceitaveis.append("</tr>");
                            }
                        }
                    }
                    tabelaErrosAceitaveis.append("</table><br><br>");
                    tabelaErrosNaoAceitaveis.append("</table><br><br>");

                    if (!ignorar) {
                        if (errosAceitaveis) {

                            mensagem.append("A lista abaixo mostra erros que são considerados aceitáveis. Isso significa que você pode solicitar ao Cassini que os ignore. Favor revisar a lista abaixo e corrigir seu arquivo no que for necessário. Após a revisão nos reenvie o arquivo novo. ");
                            mensagem.append("Caso discorde de todos os itens da lista abaixo, reenvie o arquivo com o assunto <a href=\"mailto:").append(user).append("?subject=[Cassini]Enviar Arquivo:").append(coleta ? "coleta;" : "domínio;").append(leiaute).append(";ignorar\">").append("<b>[Cassini]Enviar Arquivo:").append((coleta ? "coleta" : "domínio")).append(";").append(leiaute).append(";ignorar</b></a><br>");
                            mensagem.append("<b>Atenção:</b> solicite que o Cassini ignore os erros abaixo apenas se acreditar que seus dados estão corretos.<br>");
                            if (contadorErrosAceitaveis > 500) {
                                mensagem.append("<b>Atenção:</b> são mostrados no máximo 500 erros. Os demais erros foram omitidos para manter o tamanho do email pequeno.<br><br>");
                            } else {
                                mensagem.append("<br>");
                            }
                            mensagem.append(tabelaErrosAceitaveis);
                        }
                    }
                    if (errosNaoAceitaveis) {
                        mensagem.append("A lista abaixo apresentar erros que são considerados não aceitáveis. Isso significa que o Cassini não irá ignorá-los. Favor revisar a lista a baixo e corrigir seu arquivo. Após a revisão nos reenvie seu arquivo corrigido.<br>");
                        if (contadorErrosNaoAceitaveis > 500) {
                            mensagem.append("<b>Atenção:</b> são mostrados no máximo 500 erros. Os demais erros foram omitidos para manter o tamanho do email pequeno.<br><br>");
                        } else {
                            mensagem.append("<br>");
                        }
                        mensagem.append(tabelaErrosNaoAceitaveis);

                        if (verificaAcesso) {
                            mensagem.append("Para obter permissão para enviar dados para os parâmetros indicados, solicite que um usuário com acesso envio um email da seguinte forma:<br>");
                            mensagem.append(instrucaoAdicionarUsuario());
                            mensagem.append("Caso nenhum outro usuário tenha acesso, favor enviar um email da seguinte forma:<br>");
                            mensagem.append(instrucaoMeAdicionar());

                        }

                    }
                    if ((ignorar || !errosAceitaveis) && !errosNaoAceitaveis) {
                        SASInterface sasInterface = new SASInterface(user.split("@")[0], password, sasServer, sasPort);
                        sasInterface.addSASActionListener(sasActionListener);
                        List<Element> coletasTag = getLeiautes(coleta);
                        Element coletaTag = null;
                        for (Element tag : coletasTag) {
                            Element scriptTag = (Element) tag.getElementsByTagName("script").item(0);
                            if (scriptTag.getAttribute("leiaute").toUpperCase().equals(leiaute)) {
                                coletaTag = tag;
                            }
                        }
                        if (coleta) {
                            try {
                                if (sasInterface.atualizarDados(coletaTag, arquivo, email.getFrom().getAddress(), email.getDateTimeSent())) {
                                    mensagem.append("Seu arquivo foi recebido com sucesso. Obrigado.<br><br>");
                                    System.gc();
                                    try {
                                        Files.move(arquivoCSV.toPath(), new File(pastaValidados + arquivoCSV.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
                                    } catch (Exception ex) {

                                    }
                                    if (ignorar) {
                                        if (errosAceitaveis) {
                                            mensagem.append("A lista abaixo mostra erros que são considerados aceitáveis e que foram ignorados conforme sua solicitação.<br>");
                                            if (contadorErrosAceitaveis > 500) {
                                                mensagem.append("<b>Atenção:</b> são mostrados no máximo 500 erros. Os demais erros foram omitidos para manter o tamanho do email pequeno.<br><br>");
                                            } else {
                                                mensagem.append("<br>");
                                            }
                                            mensagem.append(tabelaErrosAceitaveis);
                                        }
                                    }
                                } else {
                                    mensagem.append("Ocorreu um erro ao tentar cadastrar o arquivo em nosso banco de dados. Favor tentar novamente mais tarde.<br><br>");
                                    System.gc();

                                    arquivoCSV.delete();
                                }
                            } catch (Exception ex) {
                                mensagem.append("Ocorreu um erro.<br><br>");
                                mensagem.append("Erro:" + ex.getMessage() + "<br><br>");
                                arquivoCSV.delete();
                            }
                        } else {
                            try {
                                if (sasInterface.insertDados(coletaTag, arquivo, email.getFrom().getAddress(), email.getDateTimeSent())) {
                                    mensagem.append("Seu arquivo foi recebido com sucesso. Obrigado.<br><br>");
                                    System.gc();
                                    try {
                                        Files.move(arquivoCSV.toPath(), new File(pastaValidados + arquivoCSV.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
                                    } catch (Exception ex) {

                                    }
                                    if (ignorar) {
                                        if (errosAceitaveis) {
                                            mensagem.append("A lista abaixo mostra erros que são considerados aceitáveis e que foram ignorados conforme sua solicitação.<br>");
                                            if (contadorErrosAceitaveis > 500) {
                                                mensagem.append("<b>Atenção:</b> são mostrados no máximo 500 erros. Os demais erros foram omitidos para manter o tamanho do email pequeno.<br><br>");
                                            } else {
                                                mensagem.append("<br>");
                                            }
                                            mensagem.append(tabelaErrosAceitaveis);
                                        }
                                    }
                                } else {
                                    mensagem.append("Ocorreu um erro ao tentar cadastrar o arquivo em nosso banco de dados. Favor tentar novamente mais tarde.<br><br>");
                                    System.gc();

                                    arquivoCSV.delete();
                                }
                            } catch (Exception ex) {
                                mensagem.append("Ocorreu um erro.<br><br>");
                                mensagem.append("Erro:" + ex.getMessage() + "<br><br>");
                                arquivoCSV.delete();
                            }
                        }
                    }

                }
            } catch (SegurancaException ex) {
                Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
                mensagem.append("Você tentou enviar um arquivo de dados para o Cassini, entretanto ocorreu algum erro no processamento do arquivo. Favor entrar em contato conosco informando o problema.<br><br>");
                mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
                arquivoCSV.delete();
            } catch (Exception ex) {
                Logger.getLogger(EmailHandler.class.getName()).log(Level.SEVERE, null, ex);
                mensagem.append("Você tentou enviar um arquivo de dados para o Cassini, entretanto ocorreu algum erro no processamento do arquivo. Favor entrar em contato conosco informando o problema.<br><br>");
                mensagem.append("Erro: " + ex.getMessage() + "<br><br>");
                arquivoCSV.delete();
            }
        } else {
            mensagem.append("Você tentou enviar um arquivo de dados para o Cassini, entretanto, não foi possível reconhecer o leiaute informado. Favor verifique o campo assunto do seu e-mail e tente novamente. Segue abaixo instruções de como enviar um arquivo para o Cassini.<br><br>");
            mensagem.append(instrucaoEnviarArquivo());
            new File(arquivo).delete();
        }
        mensagem.append(atenciosamente());
        mensagem.append("</body>");
        return mensagem.toString();
    }

    private List<String> descompactarArquivo(String arquivoZIP) {

        List<String> arquivoCSV = new ArrayList();

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(pastaRecebidos);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis
                    = new ZipInputStream(new FileInputStream(arquivoZIP));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(pastaRecebidos + File.separator + Calendar.getInstance().getTimeInMillis() + "_" + fileName);

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
                arquivoCSV.add(newFile.getAbsolutePath());
            }

            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        new File(arquivoZIP).delete();
        return arquivoCSV;
    }

    /**
     * Adiciona usuário com acesso.
     */
    private String addUser(String from, String parametros) {
        return "Você tentou adicionar um usuário";
    }

    /**
     * Lista usuário com acesso
     */
    private String getUsers(String from, String parametros) {
        return "Você tentou listar os usuários";
    }

    /**
     *
     * @return
     */
    private String addSelfUser(String from, String parametros) {
        return "Você tentou ganhar permissão";
    }

    private String getLeiautes(String from, String parametros) {
        return gets(from, parametros, true);
    }

    private String getLeiaute(String from, String parametros) {
        return get(from, parametros, true);
    }

    private String getDominios(String from, String parametros) {
        return gets(from, parametros, false);
    }

    private String getDominio(String from, String parametros) {
        return get(from, parametros, false);
    }

    //<editor-fold defaultstate="collapsed" desc="get e gets">
    private String gets(String from, String parametros, boolean coleta) {
        StringBuilder mensagem = new StringBuilder();

        File pastaXML = new File(pastaLeiautes + (coleta ? "" : "\\dominios\\"));
        File[] leiautes = pastaXML.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        mensagem.append("<head>");
        mensagem.append("<style>");
        mensagem.append("table {");
        mensagem.append("    font-family: arial, sans-serif;");
        mensagem.append("    border-collapse: collapse;");
        mensagem.append("    width: 100%;");
        mensagem.append("}");
        mensagem.append("");
        mensagem.append("td, th {");
        mensagem.append("    border: 1px solid #dddddd;");
        mensagem.append("    text-align: left;");
        mensagem.append("    padding: 8px;");
        mensagem.append("}");
        mensagem.append("");
        mensagem.append("tr:nth-child(even) {");
        mensagem.append("    background-color: #dddddd;");
        mensagem.append("}");
        mensagem.append("</style>");
        mensagem.append("</head>");

        mensagem.append("<body>");
        mensagem.append(bomDia());
        mensagem.append("Segue abaixo o resultado da pesquisa de ").append(coleta ? "leiautes" : "domínios").append(" disponíveis no Cassini:<br><br>");

        try {
            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);

            mensagem.append("<table>");
            mensagem.append("<tr>");
            mensagem.append("<th>Leiaute</th>");
            mensagem.append("<th>Descrição</th>");
            mensagem.append("</tr>");
            for (File leiaute : leiautes) {
                try {
                    Source xmlFile = new StreamSource(leiaute);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiaute);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    Element descricao = (Element) coletaTag.getElementsByTagName("descricao").item(0);
                    String nomeLeiaute = scriptTag.getAttribute("leiaute");
                    if (nomeLeiaute.matches(parametros) || descricao.getTextContent().matches(parametros)) {
                        mensagem.append("<tr>");
                        mensagem.append("<th><a href=\"mailto:").append(user).append("?subject=[Cassini]Detalhar ").append(coleta ? "Leiaute:" : "Domínio:").append(scriptTag.getAttribute("leiaute")).append("\">").append(scriptTag.getAttribute("leiaute")).append("</a>").append("</th>");
                        mensagem.append("<th>").append(descricao.getTextContent()).append("</th>");
                        mensagem.append("</tr>");
                    }
                } catch (Exception ex) {

                }
            }
            mensagem.append("</table><br><br>");

            mensagem.append("Para obter detalhes sobre um ").append(coleta ? "leiautes" : "domínios").append(", siga as intruções abaixo sobre o valor a ser colocado no Assunto do e-mail:<br>");

            if (coleta) {
                mensagem.append(instrucaoDetalharLeiaute());
            } else {
                mensagem.append(instrucaoDetalharDominio());
            }
            mensagem.append(atenciosamente());
            mensagem.append("</body>");
        } catch (Exception ex) {
        }

        return mensagem.toString();
    }

    private String get(String from, String parametros, boolean coleta) {
        StringBuilder mensagem = new StringBuilder();

        File pastaXML = new File(pastaLeiautes + (coleta ? "" : "\\dominios\\"));
        File[] leiautes = pastaXML.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        mensagem.append("<head>");
        mensagem.append("<style>");
        mensagem.append("table {");
        mensagem.append("    font-family: arial, sans-serif;");
        mensagem.append("    border-collapse: collapse;");
        mensagem.append("    width: 100%;");
        mensagem.append("}");
        mensagem.append("td, th {");
        mensagem.append("    border: 1px solid #dddddd;");
        mensagem.append("    text-align: left;");
        mensagem.append("    padding: 8px;");
        mensagem.append("}");
        mensagem.append("tr:nth-child(even) {");
        mensagem.append("    background-color: #dddddd;");
        mensagem.append("}");
        mensagem.append("</style>");
        mensagem.append("</head>");

        mensagem.append("<body>");
        mensagem.append(bomDia());

        try {
            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);

            Element coletaTag = null;
            boolean leiauteEncontrado = false;
            for (int i = 0; i < leiautes.length; i++) {
                File leiaute = leiautes[i];
                try {
                    Source xmlFile = new StreamSource(leiaute);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiaute);
                    coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    String nomeLeiaute = scriptTag.getAttribute("leiaute").toUpperCase();
                    if (nomeLeiaute.equals(parametros)) {
                        leiauteEncontrado = true;
                        i = leiautes.length;
                    }
                } catch (Exception ex) {

                }
            }

            if (leiauteEncontrado) {

                Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                mensagem.append("Segue abaixo os detalhes sobre o ").append(coleta ? "leiaute" : "domínio").append(" solicitado.<br><br>");
                String nomeLeiaute = scriptTag.getAttribute("leiaute");
                mensagem.append("<b>Nome do ").append(coleta ? "leiaute" : "domínio").append(":</b> ").append(nomeLeiaute).append("<br>");
                Element descricao = (Element) coletaTag.getElementsByTagName("descricao").item(0);
                mensagem.append("<b>Descrição:</b> ").append(descricao.getTextContent()).append("<br>");
                Element url = (Element) coletaTag.getElementsByTagName("url").item(0);
                mensagem.append("<b>Mais informações:</b> ").append(url.getTextContent()).append("<br>");
                if (coleta) {
                    mensagem.append("<b>Periodicidade:</b> ").append(((Element) coletaTag.getElementsByTagName("tipoPeriodo").item(0)).getTextContent()).append("<br>");
                }
                mensagem.append("<a href=\"mailto:").append(user).append("?subject=[Cassini]Enviar Arquivo:").append(coleta ? "coleta;" : "domínio;").append(nomeLeiaute).append("\">Enviar Arquivo</a> (lembre-se de anexar o arquivo no email).<br><br>");

                mensagem.append("<b>Colunas</b><br><br>");
                mensagem.append("<table>");
                mensagem.append("<tr>");
                mensagem.append("<th>Nome</th>");
                mensagem.append("<th>Tipo</th>");
                mensagem.append("<th>Tamanho</th>");
                mensagem.append("<th>Descrição</th>");
                mensagem.append("<th>Domínio</th>");
                mensagem.append("</tr>");

                NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");
                for (int i = 0; i < colunasTag.getLength(); i++) {
                    Element colunaTag = (Element) colunasTag.item(i);
                    mensagem.append("<tr>");
                    mensagem.append("<th>").append(colunaTag.getAttribute("nome")).append("</th>");
                    mensagem.append("<th>").append(colunaTag.getAttribute("tipo")).append("</th>");
                    mensagem.append("<th>").append(colunaTag.getAttribute("tamanho")).append("</th>");
                    mensagem.append("<th>").append(((Element) colunaTag.getElementsByTagName("descricao").item(0)).getTextContent()).append("</th>");
                    Element dominioTag = (Element) colunaTag.getElementsByTagName("dominio").item(0);
                    if (dominioTag != null) {
                        mensagem.append("<th>Domínio: ").append(dominioTag.getAttribute("leiaute")).append("<br>Coluna: ").append(dominioTag.getAttribute("coluna"))
                                .append("<br><a href=\"mailto:").append(user).append("?subject=[Cassini]Detalhar Domínio:").append(dominioTag.getAttribute("leiaute")).append("\">Detalhar Domínio</a>").append("</th>");
                    } else {
                        mensagem.append("<th>Nenhum</th>");
                    }
                    mensagem.append("</tr>");
                }
                mensagem.append("</table><br><br>");

                if (coleta) {
                    mensagem.append("O arquivo CSV deve ter o seguinte formato. Obs.: a ordem das colunas deve ser respeitada.<br><br>");
                    mensagem.append("<table>");
                    mensagem.append("<tr>");
                    for (int i = 0; i < colunasTag.getLength(); i++) {
                        Element colunaTag = (Element) colunasTag.item(i);
                        mensagem.append("<th>").append(colunaTag.getAttribute("nome")).append("</th>");
                    }
                    mensagem.append("</tr>");
                    for (int j = 0; j < 3; j++) {
                        mensagem.append("<tr>");
                        for (int i = 0; i < colunasTag.getLength(); i++) {
                            Element colunaTag = (Element) colunasTag.item(i);
                            String tipo = colunaTag.getAttribute("tipo");
                            String classe = colunaTag.getAttribute("classe");
                            switch (classe) {
                                case "CNPJ_CPF":
                                    mensagem.append("<th>11111111000101</th>");
                                    break;
                                case "MUNICIPIO":
                                    mensagem.append("<th>5300108</th>");
                                    break;
                                case "CEP":
                                    mensagem.append("<th>70000000</th>");
                                    break;
                                case "LOCALIDADE":
                                    mensagem.append("<th>21380</th>");
                                    break;
                                case "ANO":
                                    mensagem.append("<th>").append(Calendar.getInstance().get(Calendar.YEAR)).append("</th>");
                                    break;
                                case "MES":
                                    mensagem.append("<th>").append(Calendar.getInstance().get(Calendar.MONTH)).append("</th>");
                                    break;
                                case "DIA":
                                    mensagem.append("<th>").append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).append("</th>");
                                    break;
                                case "PERIODO":
                                    mensagem.append("<th>").append(Calendar.getInstance().get(Calendar.YEAR)).append("/").append(Calendar.getInstance().get(Calendar.MONTH)).append("/").append(Calendar.getInstance().get(Calendar.DAY_OF_MONTH)).append("</th>");
                                    break;
                                case "CN":
                                    mensagem.append("<th>61</th>");
                                    break;
                                case "OUTROS":
                                    switch (tipo) {
                                        case "NUMERO":
                                            mensagem.append("<th>123</th>");
                                            break;
                                        case "NUMERO_PONTO":
                                            mensagem.append("<th>123.4</th>");
                                            break;
                                        case "NUMERO_VIRGULA":
                                            mensagem.append("<th>123,4</th>");
                                            break;
                                        case "CHAR":
                                            mensagem.append("<th>texto</th>");
                                            break;
                                        case "TIME":
                                            mensagem.append("<th>AAAA/MM/DD</th>");
                                            break;
                                    }
                                    break;
                            }
                        }
                        mensagem.append("</tr>");
                    }
                    mensagem.append("</table><br><br>");
                } else {
                    mensagem.append("Os valores desse domínio são:<br><br>");
                    mensagem.append("<table>");
                    mensagem.append("<tr>");
                    for (int i = 0; i < colunasTag.getLength(); i++) {
                        Element colunaTag = (Element) colunasTag.item(i);
                        mensagem.append("<th>").append(colunaTag.getAttribute("nome")).append("</th>");
                    }
                    mensagem.append("</tr>");
                    SASInterface sasInterface = new SASInterface(user.split("@")[0], password, sasServer, sasPort);
                    sasInterface.addSASActionListener(sasActionListener);
                    List<List<String>> linhas = sasInterface.getDados(coletaTag);
                    for (List<String> linha : linhas) {
                        mensagem.append("<tr>");
                        for (String dado : linha) {
                            mensagem.append("<th>").append(dado).append("</th>");
                        }
                        mensagem.append("</tr>");
                    }
                    mensagem.append("</table><br><br>");
                }

                if (coleta) {
                    mensagem.append("Caso alguma coluna deste leiaute tenha um domínio, você pode detalhar o domínio seguindo as instruções abaixo.<br><br>");
                    mensagem.append(instrucaoDetalharDominio());
                }

                mensagem.append("Caso ainda tenha alguma dúvida sobre esse ").append(coleta ? "leiaute" : "domínio").append(", favor entrar em contato.<br><br>");
                mensagem.append("<table>");
                mensagem.append("<tr>");
                mensagem.append("<th>Nome</th>");
                mensagem.append("<th>E-mail</th>");
                mensagem.append("<th>Telefone</th>");
                mensagem.append("</tr>");

                NodeList contatosTag = ((Element) coletaTag.getElementsByTagName("contatos").item(0)).getElementsByTagName("contato");
                for (int i = 0; i < contatosTag.getLength(); i++) {
                    Element contatoTag = (Element) contatosTag.item(i);
                    mensagem.append("<tr>");
                    mensagem.append("<th>").append(contatoTag.getAttribute("nome")).append("</th>");
                    mensagem.append("<th>").append(contatoTag.getAttribute("email")).append("</th>");
                    mensagem.append("<th>").append(contatoTag.getAttribute("telefone")).append("</th>");
                    mensagem.append("</tr>");
                }
                mensagem.append("</table><br><br>");

            } else {
                mensagem.append("Nenhum ").append(coleta ? "leiaute" : "domínio").append(" encontrado para o parâmetro informado. Segue abaixo instruções para obter os ").append(coleta ? "leiautes" : "domínios").append(" disponíveis e para obter detalhes de um ").append(coleta ? "leiaute" : "domínio").append(" disponível.<br><br>");
                if (coleta) {
                    mensagem.append(instrucaoListarLeiautes());
                    mensagem.append(instrucaoDetalharLeiaute());
                } else {
                    mensagem.append(instrucaoListarDominios());
                    mensagem.append(instrucaoDetalharDominio());
                }
            }

            mensagem.append(atenciosamente());
            mensagem.append("</body>");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return mensagem.toString();
    }
//</editor-fold>

    private String contato(String from, String parametros) {
        return "Você tentou entrar em contato com nossos funcionários";
    }

    private String atenciosamente() {
        StringBuilder mensagem = new StringBuilder();
        mensagem.append("Atenciosamente,<br>");
        mensagem.append("Equipe PRPE");
        return mensagem.toString();
    }

    private String bomDia() {
        StringBuilder mensagem = new StringBuilder();
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hora >= 0 && hora < 12) {
            mensagem.append("Bom dia,<br><br>");
        } else if (hora >= 12 && hora < 18) {
            mensagem.append("Boa tarde,<br><br>");
        } else if (hora >= 18 && hora < 24) {
            mensagem.append("Boa noite,<br><br>");
        }
        mensagem.append("Por favor não resposta a esse email.<br><br>");

        return mensagem.toString();
    }

    public List<Element> getLeiautes(boolean coleta) {
        List<Element> leiautes = new ArrayList();

        File pastaXML = new File(pastaLeiautes + (coleta ? "" : "dominios\\"));
        File[] leiauteFiles = pastaXML.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        try {
            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            for (File leiauteFile : leiauteFiles) {
                try {
                    Source xmlFile = new StreamSource(leiauteFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiauteFile);
                    Element coletaTag = doc.getDocumentElement();
                    leiautes.add(coletaTag);
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
        }
        return leiautes;
    }

    public String getLeiaute(String leiaute, boolean coleta) {
        File pastaXML = new File(pastaLeiautes + (coleta ? "" : "dominios\\"));
        File[] leiauteFiles = pastaXML.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        try {
            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            for (int i = 0; i < leiauteFiles.length; i++) {
                try {
                    File leiauteFile = leiauteFiles[i];
                    Source xmlFile = new StreamSource(leiauteFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiauteFile);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    String nomeLeiaute = scriptTag.getAttribute("leiaute");
                    if (nomeLeiaute.toUpperCase().equals(leiaute)) {
                        return leiauteFile.getAbsolutePath();
                    }
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
        }

        return null;
    }

    public void addSASActionListener(SASActionListener sasActionListener) {
        this.sasActionListener = sasActionListener;
    }

    private String listarErros(String from, String parametros) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}

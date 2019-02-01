/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.PropertySet;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import microsoft.exchange.webservices.data.core.enumeration.property.WellKnownFolderName;
import microsoft.exchange.webservices.data.core.enumeration.search.LogicalOperator;
import microsoft.exchange.webservices.data.core.enumeration.service.DeleteMode;
import microsoft.exchange.webservices.data.core.service.folder.Folder;
import microsoft.exchange.webservices.data.core.service.item.EmailMessage;
import microsoft.exchange.webservices.data.core.service.item.Item;
import microsoft.exchange.webservices.data.core.service.response.ResponseMessage;
import microsoft.exchange.webservices.data.core.service.schema.EmailMessageSchema;
import microsoft.exchange.webservices.data.core.service.schema.ItemSchema;
import microsoft.exchange.webservices.data.credential.ExchangeCredentials;
import microsoft.exchange.webservices.data.credential.WebCredentials;
import microsoft.exchange.webservices.data.property.complex.ItemId;
import microsoft.exchange.webservices.data.property.complex.MessageBody;
import microsoft.exchange.webservices.data.search.FindItemsResults;
import microsoft.exchange.webservices.data.search.ItemView;
import microsoft.exchange.webservices.data.search.filter.SearchFilter;

/**
 *
 * @author Daniel
 */
public class RobotExchange {

    public static int INBOX = 0;
    public static int SENTBOX = 1;

    private ExchangeVersion version;
    private String exchangeServer;
    private String servico;
    private ExchangeService exchangeService;
    private Folder inbox;
    private Folder sentbox;

    /**
     * Cria um robo
     *
     * @param version Versão do MS Exchange. Em 2016, a Anatel tinha a versão
     * ExchangeVersion.Exchange2007_SP1
     * @param exchangeServer Endereço do servidor de MS Exchange. Em 2016 era
     * https://correioweb.anatel.gov.br/ews/Exchange.asmx
     * @param servico Nome do serviço criado. Isso será usado pelo robo para
     * filtrar apenas os emails que tenha o valor do servico no assunto.
     */
    public RobotExchange(ExchangeVersion version, String exchangeServer, String servico) {
        this.version = version;
        this.exchangeServer = exchangeServer;
        this.servico = servico;
    }

    /**
     * Tenta conectar ao servidor de MS Exchange.
     *
     * @param email email do usuário do servidor do MS Exchange
     * @param password senha do usuário do servidor do MS Exchange
     * @return returna true se a conexão for bem sucedida.
     */
    public boolean conectar(String email, String password) {
        boolean ok = false;
        try {

            exchangeService = new ExchangeService(version);
            ExchangeCredentials credentials = new WebCredentials(email, password);
            exchangeService.setCredentials(credentials);
            exchangeService.setUrl(new URI(exchangeServer));
            exchangeService.validate();
            inbox = Folder.bind(exchangeService, WellKnownFolderName.Inbox);
            sentbox = Folder.bind(exchangeService, WellKnownFolderName.SentItems);
            email = null;
            password = null;
            ok = true;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ok;
    }

    /**
     * Obtem os emails da caixa de entrada que tenha no assunto o valor do
     * servico indicado no construtor
     *
     * @param folder RobotExchange.INBOX para a caixa de entrada ou
     * RobotExchange.SENTBOX para a caixa de enviados
     * @param pageSize Quantidade de emails a serem retornados
     * @param offset Email apartir do qual os emails serão retornados
     * @param read false para obter apenas emails não lidos. true para apenas
     * emails lidos.
     * @return returna lista de emails encontrados.
     */
    public List<EmailMessage> getEmails(int folder, int pageSize, int offset, boolean read) {
        List<EmailMessage> emails = new ArrayList();
        try {
            if (exchangeService == null || inbox == null || sentbox == null) {
                throw new Exception("Você deve chamar conectar(email,password) primeiro e verificar se ele retorna true. Caso contrário houve algum erro na conexão.");
            }
            ItemView view = new ItemView(pageSize, offset);
            FindItemsResults<Item> findResults = exchangeService.findItems(folder == INBOX ? inbox.getId() : sentbox.getId(), new SearchFilter.SearchFilterCollection(LogicalOperator.And, new SearchFilter.ContainsSubstring(ItemSchema.Subject, servico), new SearchFilter.IsEqualTo(EmailMessageSchema.IsRead, read)), view);
            //MOOOOOOST IMPORTANT: load messages' properties before
            exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            for (Item item : findResults.getItems()) {
                item.load();
                emails.add((EmailMessage) item);
            }
        } catch (Exception ex) {
        }
        return emails;
    }

    /**
     * Obtem os emails da caixa de entrada que tenha no assunto o valor do
     * servico indicado no construtor
     *
     * @param folder RobotExchange.INBOX para a caixa de entrada ou
     * RobotExchange.SENTBOX para a caixa de enviados
     * @param pageSize Quantidade de emails a serem retornados
     * @param offset Email apartir do qual os emails serão retornados
     * @return returna lista de emails encontrados.
     */
    public List<Item> getEmails(int folder, int pageSize, int offset) {
        List<Item> items = new ArrayList();
        try {
            if (exchangeService == null || inbox == null || sentbox == null) {
                throw new Exception("Você deve chamar conectar(email,password) primeiro e verificar se ele retorna true. Caso contrário houve algum erro na conexão.");
            }
            ItemView view = new ItemView(pageSize, offset);
            FindItemsResults<Item> findResults = exchangeService.findItems(folder == INBOX ? inbox.getId() : sentbox.getId(), new SearchFilter.ContainsSubstring(ItemSchema.Subject, servico), view);
            //MOOOOOOST IMPORTANT: load messages' properties before
            exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            items = findResults.getItems();
        } catch (Exception ex) {
        }
        return items;
    }

    /**
     * Obtem um email com base no Id informado
     *
     * @param itemId
     * @return
     */
    public Item getEmail(ItemId itemId) {
        EmailMessage email = null;
        try {
            email = EmailMessage.bind(exchangeService, itemId);
            email.setIsRead(true);
            email.createReply(false);
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
        return email;
    }

    /**
     * Responde a um email
     *
     * @param emailId Id do email a ser respondido. Obtenha o Id usando o método
     * getEmails
     * @param messageBody Corpo da mensagem a ser enviada
     * @param saveCopy indica se uma cópia da resposta deve ser salva na pasta
     * de mensagens enviadas.
     */
    public void responderEmail(ItemId emailId, String messageBody, boolean saveCopy) {
        try {
            EmailMessage message = EmailMessage.bind(exchangeService, emailId);
            ResponseMessage response = message.createReply(false);
            if (messageBody.getBytes().length > 8000000) {
                EmailMessage reply = response.save(WellKnownFolderName.Drafts);
                reply.setBody(MessageBody.getMessageBodyFromText("A mensagem de resposta era muito grande e não pode ser enviada."));
                //reply.getAttachments().addFileAttachment("Mensagem.zip", compress(messageBody));
                if (saveCopy) {
                    reply.sendAndSaveCopy();
                } else {
                    reply.send();
                }
            } else {
                response.setBody(MessageBody.getMessageBodyFromText(messageBody));
                if (saveCopy) {
                    response.sendAndSaveCopy();
                } else {
                    response.send();
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private byte[] compress(String messageBody) {
        try {
            File mensagem = new File("./mensagem.txt");
            Writer writer = new OutputStreamWriter(new FileOutputStream(mensagem), "Cp1252");
            writer.write(messageBody);
            writer.flush();
            writer.close();

            byte[] buffer = new byte[1024];

            FileOutputStream fos = new FileOutputStream(mensagem);
            ZipOutputStream zos = new ZipOutputStream(fos);
            ZipEntry ze = new ZipEntry("./mensagem.txt");
            zos.putNextEntry(ze);
            new File("./mensagem.zip").createNewFile();
            FileInputStream in = new FileInputStream("./mensagem.zip");

            int len;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            in.close();
            zos.closeEntry();

            //remember close it
            zos.close();

            //mensagem.delete();
            byte[] bytes = Files.readAllBytes(Paths.get("./mensagem.zip"));
            //new File("./mensagem.zip").delete();
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Envia um email
     *
     * @param destinatarios List de destinatários do email
     * @param subject Assunto do Email
     * @param messageBody Corpo do Email
     * @param anexo Endereço de uma anexo
     * @param saveCopy indica se uma cópia da resposta deve ser salva na pasta
     * de mensagens enviadas.
     */
    public void enviarEmail(List<String> destinatarios, String subject, String messageBody, String anexo, boolean saveCopy) {
        ItemId itemId = null;
        try {
            EmailMessage msg = new EmailMessage(exchangeService);
            msg.setSubject(servico + subject);
            //msg.setMimeContent(new MimeContent("utf8", messageBody.getBytes()));
            msg.setBody(MessageBody.getMessageBodyFromText(messageBody));
            for (String destinatario : destinatarios) {
                msg.getToRecipients().add(destinatario);
            }

            if (new File(anexo).isFile()) {
                msg.getAttachments().addFileAttachment(anexo);
            } else {
                throw new Exception("Anexo não encontrado");
            }

            if (saveCopy) {
                msg.sendAndSaveCopy();
            } else {
                msg.send();
            }
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Envia um email
     *
     * @param destinatarios List de destinatários do email
     * @param subject Assunto do Email
     * @param messageBody Corpo do Email
     * @param saveCopy indica se uma cópia da resposta deve ser salva na pasta
     * de mensagens enviadas.
     */
    public void enviarEmail(List<String> destinatarios, String subject, String messageBody, boolean saveCopy) {
        try {
            EmailMessage msg = new EmailMessage(exchangeService);
            msg.setSubject(servico + subject);
            //msg.setMimeContent(new MimeContent("utf8", messageBody.getBytes()));
            msg.setBody(MessageBody.getMessageBodyFromText(messageBody));
            for (String destinatario : destinatarios) {
                System.err.println("|" + destinatario + "|");
                msg.getToRecipients().add(destinatario);
            }

            if (saveCopy) {
                msg.sendAndSaveCopy();
            } else {
                msg.send();
            }
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Apaga um email
     *
     * @param emailId Id do email a ser apagado. Obtenha o Id usando o método
     * getEmails.
     */
    public void apagarEmail(ItemId emailId) {
        try {
            EmailMessage message = EmailMessage.bind(exchangeService, emailId);
            message.delete(DeleteMode.HardDelete);
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Apaga todos os emails de uma pasta
     *
     * @param folder RobotExchange.INBOX para a caixa de entrada ou
     * RobotExchange.SENTBOX para a caixa de enviados
     */
    public void apagarTodosEmails(int folder) {
        try {
            ItemView view = new ItemView(10, 0);
            FindItemsResults<Item> findResults = exchangeService.findItems(folder == INBOX ? inbox.getId() : sentbox.getId(), new SearchFilter.ContainsSubstring(ItemSchema.Subject, servico), view);
            //MOOOOOOST IMPORTANT: load messages' properties before
            exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            while (findResults.getTotalCount() > 0) {
                for (Item item : findResults.getItems()) {
                    item.delete(DeleteMode.HardDelete);
                }
                findResults = exchangeService.findItems(folder == INBOX ? inbox.getId() : sentbox.getId(), new SearchFilter.ContainsSubstring(ItemSchema.Subject, servico), view);
                //MOOOOOOST IMPORTANT: load messages' properties before
                exchangeService.loadPropertiesForItems(findResults, PropertySet.FirstClassProperties);
            }
        } catch (Exception ex) {
            Logger.getLogger(RobotExchange.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Apaga todos os emails tanto da caixa de entrada quanto da caixa de emails
     * enviados.
     */
    public void apagarTodosEmails() {
        apagarTodosEmails(INBOX);
        apagarTodosEmails(SENTBOX);
    }

    /**
     * Desconecta do servidor de MS Exchange
     */
    public void desconectar() {
        exchangeService.close();
    }
    
}

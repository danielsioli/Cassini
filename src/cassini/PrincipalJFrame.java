/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cassini;

import com.sas.iom.SAS.ILanguageServicePackage.LineType;
import email.EmailInterface;
import email.RobotExchange;
import gerador.Banco;
import gerador.Coleta;
import gerador.Coluna;
import gerador.SegurancaException;
import gerador.sas.ConexaoSAS;
import gerador.sas.SASAction;
import gerador.sas.SASActionListener;
import gerador.sas.SASException;
import java.awt.AWTException;
import java.awt.Color;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.swing.AbstractListModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import microsoft.exchange.webservices.data.core.enumeration.misc.ExchangeVersion;
import org.jsslutils.sslcontext.PKIXSSLContextFactory;
import org.jsslutils.sslcontext.SSLContextFactory;
import org.jsslutils.sslcontext.X509TrustManagerWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author danieloliveira
 */
public class PrincipalJFrame extends javax.swing.JFrame implements SASActionListener {

    private static int conexoesSAS = 0;
    private Timer timer = null;
    private Properties props = null;
    private final String coletaXSD = "\\\\Servicesdata\\prpe$\\Dados\\Projeto Piloto\\xml\\coleta.xsd";
    //private final String coletaXSD = "http://sistemasds.anatel.gov.br/dici/coleta.xsd";

    /**
     * Creates new form PrincipalJFrame
     */
    public PrincipalJFrame() {

        initComponents();
        loadProperties();
        System.setProperty("javax.net.ssl.keyStore", "./security/jssecacerts");
        System.setProperty("javax.net.ssl.trustStore", "./security/jssecacerts");
        //System.setProperty("javax.net.ssl.keyStore", "*");
        //System.setProperty("javax.net.ssl.trustStore", "*");

        setSystemTray();

    }

    private void security() throws SSLContextFactory.SSLContextFactoryException {
        PKIXSSLContextFactory sslContextFactory = new PKIXSSLContextFactory();
        sslContextFactory.setTrustManagerWrapper(new X509TrustManagerWrapper() {
            @Override
            public X509TrustManager wrapTrustManager(final X509TrustManager origManager) {
                return new X509TrustManager() {
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return origManager.getAcceptedIssuers();
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain,
                            String authType)
                            throws CertificateException {
                        try {
                            // This will call the default trust manager
                            // which will throw an exception if it doesn't know the certificate
                            origManager.checkServerTrusted(chain, authType);
                        } catch (CertificateException e) {
                            // If it throws an exception, check what this exception is
                            // the server certificate is in chain[0], you could
                            // implement a callback to the user to accept/refuse
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] chain,
                            String authType)
                            throws CertificateException {
                        origManager.checkClientTrusted(chain, authType);
                    }
                };
            }
        });
        SSLContext sslContext = sslContextFactory.buildSSLContext();
        SSLContext.setDefault(sslContext);
    }

    TrayIcon trayIcon;
    SystemTray systemTray;

    /**
     *
     */
    private void setSystemTray() {
        if (SystemTray.isSupported()) {
            trayIcon = new TrayIcon(loadImage("/cassini/saturn.jpg"), "Cassini");
            systemTray = SystemTray.getSystemTray();

            PopupMenu popupMenu = new PopupMenu();

            MenuItem configurarMenuItem = new MenuItem("Configurar");
            MenuItem iniciarMenuItem = new MenuItem("Iniciar Cassini");
            MenuItem pararMenuItem = new MenuItem("Parar Cassini");
            MenuItem gerenciarColetasMenuItem = new MenuItem("Gerenciar Coletas");
            MenuItem gerenciarPermissoesMenuItem = new MenuItem("Gerenciar Permissões");
            MenuItem fecharMenuItem = new MenuItem("Fechar");

            configurarMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showCassini();
                }
            });

            iniciarMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    iniciarJButtonActionPerformed(null);
                }
            });

            pararMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pararJButtonActionPerformed(null);
                }
            });

            gerenciarColetasMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });

            gerenciarPermissoesMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });

            fecharMenuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    close();
                }
            });

            popupMenu.add(configurarMenuItem);
            popupMenu.add(iniciarMenuItem);
            popupMenu.add(pararMenuItem);
            popupMenu.add(gerenciarColetasMenuItem);
            popupMenu.add(gerenciarPermissoesMenuItem);
            popupMenu.add(fecharMenuItem);

            trayIcon.setPopupMenu(popupMenu);

            trayIcon.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showCassini();
                }
            });

            addWindowStateListener(new WindowStateListener() {
                public void windowStateChanged(WindowEvent e) {
                    if (e.getNewState() == ICONIFIED) {
                        try {
                            systemTray.add(trayIcon);
                            setVisible(false);
                        } catch (AWTException ex) {
                        }
                    }
                    if (e.getNewState() == 7) {
                        try {
                            systemTray.add(trayIcon);
                            setVisible(false);
                        } catch (AWTException ex) {
                        }
                    }
                    if (e.getNewState() == MAXIMIZED_BOTH) {
                        systemTray.remove(trayIcon);
                        setVisible(true);
                    }
                    if (e.getNewState() == NORMAL) {
                        systemTray.remove(trayIcon);
                        setVisible(true);
                    }
                }
            });

        }
    }

    protected void showCassini() {
        this.pack();
        this.setVisible(true);
        this.setState(JFrame.NORMAL);
        this.requestFocus();
    }

    public BufferedImage loadImage(String fileName) {

        BufferedImage buff = null;
        try {
            buff = ImageIO.read(getClass().getResourceAsStream(fileName));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return buff;

    }

    private void loadProperties() {
        try {
            props = new Properties();
            File propsFile = new File("./properties/.properties");
            if (!propsFile.isFile()) {
                propsFile.getParentFile().mkdir();
                propsFile.createNewFile();
            }
            FileInputStream file = new FileInputStream("./properties/.properties");
            props.load(file);
            servidorSMTPJTextField.setText(props.getProperty("cassini.smtp.host") == null ? servidorSMTPJTextField.getText() : props.getProperty("cassini.smtp.host"));
            portaSMTPJFormattedTextField.setText(props.getProperty("cassini.smtp.port") == null ? portaSMTPJFormattedTextField.getText() : props.getProperty("cassini.smtp.port"));
            servidorIMAPJTextField.setText(props.getProperty("cassini.imap.host") == null ? servidorIMAPJTextField.getText() : props.getProperty("cassini.imap.host"));
            portaIMAPJFormattedTextField.setText(props.getProperty("cassini.imap.port") == null ? portaIMAPJFormattedTextField.getText() : props.getProperty("cassini.imap.port"));

            servidorJComboBox.setSelectedItem(props.getProperty("cassini.servidor") == null ? servidorJComboBox.getSelectedItem() : props.getProperty("cassini.servidor"));
            msExchangeVersaoJComboBox.setSelectedItem(props.getProperty("cassini.exchange.versao") == null ? msExchangeVersaoJComboBox.getSelectedItem() : props.getProperty("cassini.exchange.versao"));
            servidorMSExchangeJTextField.setText(props.getProperty("cassini.exchange.host") == null ? servidorMSExchangeJTextField.getText() : props.getProperty("cassini.exchange.host"));

            apagarEmailsJComboBox.setSelectedItem(props.getProperty("cassini.emails.apagar") == null ? apagarEmailsJComboBox.getSelectedItem() : props.getProperty("cassini.emails.apagar"));
            pastaRecebidosJTextField.setText(props.getProperty("cassini.recebidos.folder") == null ? pastaRecebidosJTextField.getText() : props.getProperty("cassini.recebidos.folder"));
            pastaValidadosJTextField.setText(props.getProperty("cassini.validados.folder") == null ? pastaValidadosJTextField.getText() : props.getProperty("cassini.validados.folder"));
            sasPortJFormattedTextField.setText(props.getProperty("cassini.sas.port") == null ? sasPortJFormattedTextField.getText() : props.getProperty("cassini.sas.port"));
            sasServerJTextField.setText(props.getProperty("cassini.sas.host") == null ? sasServerJTextField.getText() : props.getProperty("cassini.sas.host"));

            if (props.getProperty("cassini.leiautes.folder") != null) {
                pastaXMLJTextField.setText(props.getProperty("cassini.leiautes.folder"));
                tipoLeiauteJComboBox.setEnabled(true);
            }

            usuarioJTextField.setText(props.getProperty("cassini.usuario.email") == null ? "" : props.getProperty("cassini.usuario.email"));
            senhaJPasswordField.setText(props.getProperty("cassini.usuario.pass") == null ? "" : props.getProperty("cassini.usuario.pass"));
            salvarSenhaJRadioButton.setSelected(props.getProperty("cassini.usuario.pass.salvar").equals("true"));
            atualizacaoJFormattedTextField.setText(props.getProperty("cassini.timeout") == null ? atualizacaoJFormattedTextField.getText() : props.getProperty("cassini.timeout"));
            if (props.getProperty("cassini.auto").equals("true")) {
                autoIniciarJRadioButton.setEnabled(true);
                autoIniciarJRadioButton.setSelected(true);
                iniciarJButtonActionPerformed(null);
            } else {
                autoIniciarJRadioButton.setSelected(false);
            }
            if (props.getProperty("cassini.contacts") != null) {
                String[] contatos = props.getProperty("cassini.contacts").split(";");
                DefaultListModel model = (DefaultListModel) contatosJList.getModel();
                for (int i = 0; i < contatos.length; i++) {
                    model.addElement(contatos[i]);
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        incluirColetaJFrame = new javax.swing.JFrame();
        incluirColetaJScrollPane = new javax.swing.JScrollPane();
        incluirColetaJPanel = new javax.swing.JPanel();
        nomeLeiauteJLabel = new javax.swing.JLabel();
        leiauteJTextField = new javax.swing.JTextField();
        leiauteOkJLabel = new javax.swing.JLabel();
        acaoJLabel = new javax.swing.JLabel();
        acaoJComboBox = new javax.swing.JComboBox();
        tipoScriptJLabel = new javax.swing.JLabel();
        tipoScriptJComboBox = new javax.swing.JComboBox();
        enderecoScriptJLabel = new javax.swing.JLabel();
        enderecoScriptJTextField = new javax.swing.JTextField();
        periodicidadeJLabel = new javax.swing.JLabel();
        periodicidadeJComboBox = new javax.swing.JComboBox();
        jSeparator1 = new javax.swing.JSeparator();
        colunasJLabel = new javax.swing.JLabel();
        nomeColunaJLabel = new javax.swing.JLabel();
        nomeColunaJTextField = new javax.swing.JTextField();
        tipoColunaJLabel = new javax.swing.JLabel();
        tipoColunaJComboBox = new javax.swing.JComboBox();
        tamanhoColunaJLabel = new javax.swing.JLabel();
        tamanhoColunaJSlider = new javax.swing.JSlider();
        classeJLabel = new javax.swing.JLabel();
        classeColunaJComboBox = new javax.swing.JComboBox();
        colunasJScrollPane = new javax.swing.JScrollPane();
        colunasJTable = new javax.swing.JTable();
        incluirColunaJButton = new javax.swing.JButton();
        limparColunaJButton = new javax.swing.JButton();
        excluirColunaJButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        validacoesJLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        validacaoesDisponiveisJList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        validacoesRealizadasJList = new javax.swing.JList();
        addValidacaoJButton = new javax.swing.JButton();
        removeValidacaoJButton = new javax.swing.JButton();
        aliasColunaJLabel = new javax.swing.JLabel();
        aliasColunaJTextField = new javax.swing.JTextField();
        descricaoColunaJLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        descricaoColunaJTextArea = new javax.swing.JTextArea();
        descricaoLeiauteJLabel = new javax.swing.JLabel();
        jScrollPane4 = new javax.swing.JScrollPane();
        descricaoJTextArea = new javax.swing.JTextArea();
        incluirColetaJButton = new javax.swing.JButton();
        validacoesDisponiveisJLabel = new javax.swing.JLabel();
        validacoesRealizadasJLabel = new javax.swing.JLabel();
        tipoBancoJLabel = new javax.swing.JLabel();
        tipoBancoJComboBox = new javax.swing.JComboBox();
        enderecoBancoJLabel = new javax.swing.JLabel();
        enderecoBancoJTextField = new javax.swing.JTextField();
        enderecoBancoJButton = new javax.swing.JButton();
        tabelaJLabel = new javax.swing.JLabel();
        tabelaJTextField = new javax.swing.JTextField();
        tabelaOkJLabel = new javax.swing.JLabel();
        leiauteJLabel = new javax.swing.JLabel();
        leiauteJComboBox = new javax.swing.JComboBox();
        tipoLeiauteJLabel = new javax.swing.JLabel();
        tipoLeiauteJComboBox = new javax.swing.JComboBox();
        dominioJLabel = new javax.swing.JLabel();
        dominioJComboBox = new javax.swing.JComboBox();
        colunaJLabel = new javax.swing.JLabel();
        colunaDominioJComboBox = new javax.swing.JComboBox();
        regexJLabel = new javax.swing.JLabel();
        regexJTextField = new javax.swing.JTextField();
        editarRegexJRadioButton = new javax.swing.JRadioButton();
        jSeparator4 = new javax.swing.JSeparator();
        contatoJLabel = new javax.swing.JLabel();
        nomeContatoJLabel = new javax.swing.JLabel();
        emailContatoJLabel = new javax.swing.JLabel();
        telefoneJLabel = new javax.swing.JLabel();
        telefoneContatoJTextField = new javax.swing.JTextField();
        emailContatoJTextField = new javax.swing.JTextField();
        nomeContatoJTextField = new javax.swing.JTextField();
        addContatoJButton = new javax.swing.JButton();
        jScrollPane12 = new javax.swing.JScrollPane();
        contatosJTable = new javax.swing.JTable();
        removeContatoJButton = new javax.swing.JButton();
        queryJLabel = new javax.swing.JLabel();
        jScrollPane9 = new javax.swing.JScrollPane();
        queryJTextArea = new javax.swing.JTextArea();
        testarQueryJToggleButton = new javax.swing.JToggleButton();
        atualizadaoJLabel = new javax.swing.JLabel();
        atualizacaoJComboBox = new javax.swing.JComboBox();
        urlJLabel = new javax.swing.JLabel();
        urlJTextField = new javax.swing.JTextField();
        prazoJLabel = new javax.swing.JLabel();
        contadorDiasJSlider = new javax.swing.JSlider();
        contadorDiasJLabel = new javax.swing.JLabel();
        tipoPrazoJComboBox = new javax.swing.JComboBox();
        valoresDuplicadosJFrame = new javax.swing.JFrame();
        valoresDuplicadosJPanel = new javax.swing.JPanel();
        colunaDadoDuplicadosJLabel = new javax.swing.JLabel();
        colunaDadoDuplicadoJComboBox = new javax.swing.JComboBox();
        colunasDisponiveisDuplicadoJLabel = new javax.swing.JLabel();
        jScrollPane8 = new javax.swing.JScrollPane();
        colunasDisponiveisDuplicadoJList = new javax.swing.JList();
        addColunaDuplicadoJButton = new javax.swing.JButton();
        removeColunaDuplicadoJButton = new javax.swing.JButton();
        colunasConsolidacaoDuplicadoJScrollPane = new javax.swing.JScrollPane();
        colunasConsolidacaoDuplicadoJList = new javax.swing.JList();
        cancelarColunasDuplicadoJButton = new javax.swing.JButton();
        okColunasDuplicadoJButton = new javax.swing.JButton();
        crescimentoJFrame = new javax.swing.JFrame();
        crescimentoJPanel = new javax.swing.JPanel();
        colunaDadoJLabel = new javax.swing.JLabel();
        colunaDadoJComboBox = new javax.swing.JComboBox();
        colunasDisponiveisConsolidacaoJLabel = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        colunasDisponiveisConsolidacaoJList = new javax.swing.JList();
        addConsolidacaoJButton = new javax.swing.JButton();
        removeConsolidacaoJButton = new javax.swing.JButton();
        cancelCrescimentoJButton = new javax.swing.JButton();
        okCrescimentoJButton = new javax.swing.JButton();
        colunasConsolidacaoJLabel = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        limitesJLabel = new javax.swing.JLabel();
        inicioJLabel = new javax.swing.JLabel();
        inicioJFormattedTextField = new javax.swing.JFormattedTextField();
        fimJLabel = new javax.swing.JLabel();
        fimJFormattedTextField = new javax.swing.JFormattedTextField();
        crescimentoMaximoJLabel = new javax.swing.JLabel();
        crescimentoMaximoJFormattedTextField = new javax.swing.JFormattedTextField();
        crescimentoMinimoJLabel = new javax.swing.JLabel();
        crescimentoMinimoJFormattedTextField = new javax.swing.JFormattedTextField();
        limparLimiteJButton = new javax.swing.JButton();
        addLimiteJButton = new javax.swing.JButton();
        jScrollPane7 = new javax.swing.JScrollPane();
        limitesJTable = new javax.swing.JTable();
        excluirLimiteJButton = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        colunasConsolidacaoJList = new javax.swing.JList();
        densidadeJFrame = new javax.swing.JFrame();
        densidadeJPanel = new javax.swing.JPanel();
        denominadorJLabel = new javax.swing.JLabel();
        denominadorJComboBox = new javax.swing.JComboBox();
        multiplicadorJLabel = new javax.swing.JLabel();
        multiplicadorJSlider = new javax.swing.JSlider();
        valorMultiplicadorJLabel = new javax.swing.JLabel();
        cancelDensidadeJButton = new javax.swing.JButton();
        okDensidadeJButton = new javax.swing.JButton();
        chaveUnicaJFrame = new javax.swing.JFrame();
        chaveUnicaJPanel = new javax.swing.JPanel();
        colunasDisponiveisChaveUnicaJLabel = new javax.swing.JLabel();
        colunasDisponiveisChaveUnicaJScrollPane = new javax.swing.JScrollPane();
        colunasDisponiveisChaveUnicaJList = new javax.swing.JList();
        addChaveUnicaJButton = new javax.swing.JButton();
        removeChaveUnicaJButton = new javax.swing.JButton();
        colunasConsolidacaoChaveUnicaJScrollPane = new javax.swing.JScrollPane();
        colunasConsolidacaoChaveUnicaJList = new javax.swing.JList();
        cancelarColunasChaveUnicaJButton = new javax.swing.JButton();
        okColunasChaveJButton = new javax.swing.JButton();
        colunasChaveUnicaLabel = new javax.swing.JLabel();
        permissoesJFrame = new javax.swing.JFrame();
        permissoesJPanel = new javax.swing.JPanel();
        pequisaPermissaoJLabel = new javax.swing.JLabel();
        pesquisaPermissoesJComboBox = new javax.swing.JComboBox();
        tipoLeiautePermissoesJLabel = new javax.swing.JLabel();
        tipoLeiautePermissoesJComboBox = new javax.swing.JComboBox();
        leiautePermissoesJLabel = new javax.swing.JLabel();
        leiautePermissoesJComboBox = new javax.swing.JComboBox();
        parametroPermissoesJLabel = new javax.swing.JLabel();
        parametroPermissoesJComboBox = new javax.swing.JComboBox();
        valorPermissoesJLabel = new javax.swing.JLabel();
        valorPermissoesJTextField = new javax.swing.JTextField();
        usuarioPermissoesJLabel = new javax.swing.JLabel();
        usuarioPermissoesJTextField = new javax.swing.JTextField();
        limparPermissoesJButton = new javax.swing.JButton();
        okPermissoesJButton = new javax.swing.JButton();
        listaPermissoesJLabel = new javax.swing.JLabel();
        jScrollPane14 = new javax.swing.JScrollPane();
        permisoesJTable = new javax.swing.JTable();
        excluirPermissoesJButton = new javax.swing.JButton();
        incluirPermissoesJButton = new javax.swing.JButton();
        incluirPermissoesJFrame = new javax.swing.JFrame();
        incluirPermissoesJPanel = new javax.swing.JPanel();
        usuarioIncluirPermissoesJLabel = new javax.swing.JLabel();
        jScrollPane11 = new javax.swing.JScrollPane();
        usuarioPermissoesJList = new javax.swing.JList();
        incluirUsuarioPermissoesJButton = new javax.swing.JButton();
        excluirUsuarioJButton = new javax.swing.JButton();
        leiauteInlcuirPermissoesJLabel = new javax.swing.JLabel();
        tipoLeiauteIncluirPermissoesJLabel = new javax.swing.JLabel();
        tipoLeiauteIncluirPermissoesJComboBox = new javax.swing.JComboBox();
        leiauteIncluirPermissoesJComboBox = new javax.swing.JComboBox();
        parametrosIncluirPermissoesJLabel = new javax.swing.JLabel();
        parametrosIncluirPermissoesJComboBox = new javax.swing.JComboBox();
        valorIncluirPermissoesJLabel = new javax.swing.JLabel();
        valorIncluirPermissoesJTextField = new javax.swing.JTextField();
        incluirValorPermissoesJButton = new javax.swing.JButton();
        excluirValorPermissoesJButton = new javax.swing.JButton();
        cancelarIncluirPermissoesJButton = new javax.swing.JButton();
        csvIncluirPermissoesJButton = new javax.swing.JButton();
        okIncluirPermissoesJButton = new javax.swing.JButton();
        jScrollPane15 = new javax.swing.JScrollPane();
        parametrosPermissoesJTable = new javax.swing.JTable();
        incluirPermissoesJTextField = new javax.swing.JTextField();
        cronogramaJFrame = new javax.swing.JFrame();
        cronogramaJPanel = new javax.swing.JPanel();
        jScrollPane16 = new javax.swing.JScrollPane();
        cronogramaJTable = new javax.swing.JTable();
        cobrarJButton = new javax.swing.JButton();
        atualizarJButton = new javax.swing.JButton();
        cronogramaJProgressBar = new javax.swing.JProgressBar();
        compararJFrame = new javax.swing.JFrame();
        testarCompararJButton = new javax.swing.JButton();
        okCompararJButton = new javax.swing.JButton();
        limparCompararJButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        comparaJTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane17 = new javax.swing.JScrollPane();
        comparaJTextArea = new javax.swing.JTextArea();
        jScrollPane18 = new javax.swing.JScrollPane();
        logComparaJTextArea = new javax.swing.JTextArea();
        jScrollPane19 = new javax.swing.JScrollPane();
        resultadoComparaJTable = new javax.swing.JTable();
        configurarJScrollPane = new javax.swing.JScrollPane();
        configurarJPanel = new javax.swing.JPanel();
        usuarioJLabel = new javax.swing.JLabel();
        usuarioJTextField = new javax.swing.JTextField();
        senhaJLabel = new javax.swing.JLabel();
        senhaJPasswordField = new javax.swing.JPasswordField();
        contatosJLabel = new javax.swing.JLabel();
        contatoNovoJTextField = new javax.swing.JTextField();
        adicionarContatoJButton = new javax.swing.JButton();
        jScrollPane10 = new javax.swing.JScrollPane();
        contatosJList = new javax.swing.JList();
        excluirContatoJButton = new javax.swing.JButton();
        atualizacaoJLabel = new javax.swing.JLabel();
        atualizacaoJFormattedTextField = new javax.swing.JFormattedTextField();
        segundosJLabel = new javax.swing.JLabel();
        iniciarJButton = new javax.swing.JButton();
        pararJButton = new javax.swing.JButton();
        servidorSMTPJLabel = new javax.swing.JLabel();
        servidorSMTPJTextField = new javax.swing.JTextField();
        portaSMTPJLabel = new javax.swing.JLabel();
        portaSMTPJFormattedTextField = new javax.swing.JFormattedTextField();
        servidorIMAPJLabel = new javax.swing.JLabel();
        servidorIMAPJTextField = new javax.swing.JTextField();
        portaIMAPJLabel = new javax.swing.JLabel();
        portaIMAPJFormattedTextField = new javax.swing.JFormattedTextField();
        statusJLabel = new javax.swing.JLabel();
        salvarSenhaJRadioButton = new javax.swing.JRadioButton();
        autoIniciarJRadioButton = new javax.swing.JRadioButton();
        exchangeVersionJLabel = new javax.swing.JLabel();
        msExchangeVersaoJComboBox = new javax.swing.JComboBox<String>();
        servidorMSExchangeJLabel = new javax.swing.JLabel();
        servidorMSExchangeJTextField = new javax.swing.JTextField();
        tipoServidorEmailJLabel = new javax.swing.JLabel();
        servidorJComboBox = new javax.swing.JComboBox<String>();
        pastaRecebidosJLabel = new javax.swing.JLabel();
        pastaRecebidosJTextField = new javax.swing.JTextField();
        pastaRecebidosJButton = new javax.swing.JButton();
        pastaValidadosJLabel = new javax.swing.JLabel();
        pastaValidadosJTextField = new javax.swing.JTextField();
        pastaValidadosJButton = new javax.swing.JButton();
        apagarEmailsJLabel = new javax.swing.JLabel();
        apagarEmailsJComboBox = new javax.swing.JComboBox<String>();
        sasServerJLabel = new javax.swing.JLabel();
        sasServerJTextField = new javax.swing.JTextField();
        sasPortJLabel = new javax.swing.JLabel();
        sasPortJFormattedTextField = new javax.swing.JFormattedTextField();
        logJLabel = new javax.swing.JLabel();
        jScrollPane13 = new javax.swing.JScrollPane();
        logJTextArea = new javax.swing.JTextArea();
        javax.swing.text.DefaultCaret caret = (javax.swing.text.DefaultCaret)logJTextArea.getCaret();
        caret.setUpdatePolicy(javax.swing.text.DefaultCaret.ALWAYS_UPDATE);
        pastaXMLJLabel = new javax.swing.JLabel();
        pastaXMLJTextField = new javax.swing.JTextField();
        pastaXMLJButton = new javax.swing.JButton();
        conexoesSASJLabel = new javax.swing.JLabel();
        jMenuBar = new javax.swing.JMenuBar();
        coletasJMenu = new javax.swing.JMenu();
        gerenciarColetaJMenuItem = new javax.swing.JMenuItem();
        permissoesJMenuItem = new javax.swing.JMenuItem();
        cronogramaJMenuItem = new javax.swing.JMenuItem();
        enviarArquivoJMenuItem = new javax.swing.JMenuItem();

        incluirColetaJFrame.setTitle("Incluir Coleta");
        incluirColetaJFrame.setAlwaysOnTop(true);
        incluirColetaJFrame.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        incluirColetaJFrame.setLocationByPlatform(true);
        incluirColetaJFrame.setResizable(false);
        incluirColetaJFrame.setType(java.awt.Window.Type.POPUP);

        nomeLeiauteJLabel.setText("Nome Leiaute");

        leiauteJTextField.setEnabled(false);
        leiauteJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                leiauteJTextFieldKeyReleased(evt);
            }
        });

        leiauteOkJLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        acaoJLabel.setText("Ação:");

        acaoJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rodar Script", "Salvar Script" }));
        acaoJComboBox.setEnabled(false);
        acaoJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                acaoJComboBoxActionPerformed(evt);
            }
        });

        tipoScriptJLabel.setText("Tipo Script");

        tipoScriptJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SAS", "SQL Server", "MySQL" }));
        tipoScriptJComboBox.setEnabled(false);
        tipoScriptJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoScriptJComboBoxActionPerformed(evt);
            }
        });

        enderecoScriptJLabel.setText("Endereço Script");

        enderecoScriptJTextField.setEnabled(false);

        periodicidadeJLabel.setText("Periodicidade");

        periodicidadeJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Mensal", "Trimestral", "Semestral", "Anual", "Eventual" }));
        periodicidadeJComboBox.setEnabled(false);

        colunasJLabel.setText("Colunas");

        nomeColunaJLabel.setText("Nome");

        nomeColunaJTextField.setEnabled(false);
        nomeColunaJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                nomeColunaJTextFieldKeyReleased(evt);
            }
        });

        tipoColunaJLabel.setText("Tipo");

        tipoColunaJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Texto", "Tempo (AAAA/MM/DD)", "Número separado por ponto", "Número separado por vírgula", "Inteiro" }));
        tipoColunaJComboBox.setEnabled(false);
        tipoColunaJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoColunaJComboBoxActionPerformed(evt);
            }
        });

        tamanhoColunaJLabel.setText("Tamanho: 14");

        tamanhoColunaJSlider.setMaximum(200);
        tamanhoColunaJSlider.setMinimum(1);
        tamanhoColunaJSlider.setValue(14);
        tamanhoColunaJSlider.setEnabled(false);
        tamanhoColunaJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tamanhoColunaJSliderStateChanged(evt);
            }
        });

        classeJLabel.setText("Classe");

        classeColunaJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "CNPJ/CPF", "Código IBGE Município", "CEP", "Localidade SGMU", "Código Nacional", "Ano", "Mês", "Dia", "Período", "Outros" }));
        classeColunaJComboBox.setEnabled(false);
        classeColunaJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                classeColunaJComboBoxActionPerformed(evt);
            }
        });

        colunasJScrollPane.setEnabled(false);

        colunasJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "Alias", "Descrição", "Tipo", "Tamanho", "Classe", "Domínio", "Expressão Regular", "Atualização"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        colunasJScrollPane.setViewportView(colunasJTable);

        incluirColunaJButton.setText("Incluir Coluna");
        incluirColunaJButton.setEnabled(false);
        incluirColunaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incluirColunaJButtonActionPerformed(evt);
            }
        });

        limparColunaJButton.setText("Limpar");
        limparColunaJButton.setEnabled(false);
        limparColunaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limparColunaJButtonActionPerformed(evt);
            }
        });

        excluirColunaJButton.setText("Excluir Coluna Selecionada");
        excluirColunaJButton.setEnabled(false);
        excluirColunaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excluirColunaJButtonActionPerformed(evt);
            }
        });

        validacoesJLabel.setText("Validações");

        validacaoesDisponiveisJList.setModel(new MinhaListModel());
        validacaoesDisponiveisJList.setEnabled(false);
        jScrollPane1.setViewportView(validacaoesDisponiveisJList);

        validacoesRealizadasJList.setModel(new DefaultListModel());
        validacoesRealizadasJList.setEnabled(false);
        validacoesRealizadasJList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                validacoesRealizadasJListMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(validacoesRealizadasJList);

        addValidacaoJButton.setText(">>");
        addValidacaoJButton.setEnabled(false);
        addValidacaoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addValidacaoJButtonActionPerformed(evt);
            }
        });

        removeValidacaoJButton.setText("<<");
        removeValidacaoJButton.setEnabled(false);
        removeValidacaoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeValidacaoJButtonActionPerformed(evt);
            }
        });

        aliasColunaJLabel.setText("Alias");

        aliasColunaJTextField.setEnabled(false);

        descricaoColunaJLabel.setText("Descrição");

        descricaoColunaJTextArea.setColumns(20);
        descricaoColunaJTextArea.setRows(5);
        descricaoColunaJTextArea.setEnabled(false);
        jScrollPane3.setViewportView(descricaoColunaJTextArea);

        descricaoLeiauteJLabel.setText("Descrição");

        descricaoJTextArea.setColumns(20);
        descricaoJTextArea.setRows(5);
        descricaoJTextArea.setEnabled(false);
        jScrollPane4.setViewportView(descricaoJTextArea);

        incluirColetaJButton.setText("Incluir Coleta");
        incluirColetaJButton.setEnabled(false);
        incluirColetaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incluirColetaJButtonActionPerformed(evt);
            }
        });

        validacoesDisponiveisJLabel.setText("Disponíveis");

        validacoesRealizadasJLabel.setText("A Realizar");

        tipoBancoJLabel.setText("Tipo Banco");

        tipoBancoJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Arquivo SAS", "Biblioteca SAS", "Tabela MS SQL Server", "Tabela MySQL" }));
        tipoBancoJComboBox.setEnabled(false);
        tipoBancoJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoBancoJComboBoxActionPerformed(evt);
            }
        });

        enderecoBancoJLabel.setText("Endereço Banco");

        enderecoBancoJTextField.setEnabled(false);

        enderecoBancoJButton.setText("...");
        enderecoBancoJButton.setEnabled(false);
        enderecoBancoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enderecoBancoJButtonActionPerformed(evt);
            }
        });

        tabelaJLabel.setText("Tabela");

        tabelaJTextField.setEnabled(false);
        tabelaJTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tabelaJTextFieldKeyReleased(evt);
            }
        });

        tabelaOkJLabel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        leiauteJLabel.setText("Leiaute");

        leiauteJComboBox.setModel(new DefaultComboBoxModel(new Object[]{}));
        leiauteJComboBox.setEnabled(false);
        leiauteJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leiauteJComboBoxActionPerformed(evt);
            }
        });

        tipoLeiauteJLabel.setText("Tipo Leiaute");

        tipoLeiauteJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "Coleta", "Domínio" }));
        tipoLeiauteJComboBox.setEnabled(false);
        tipoLeiauteJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoLeiauteJComboBoxActionPerformed(evt);
            }
        });

        dominioJLabel.setText("Domínio");
        dominioJLabel.setToolTipText("");

        dominioJComboBox.setModel(new DefaultComboBoxModel());
        dominioJComboBox.setEnabled(false);
        dominioJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dominioJComboBoxActionPerformed(evt);
            }
        });

        colunaJLabel.setText("Coluna Domínio");

        colunaDominioJComboBox.setModel(new DefaultComboBoxModel());
        colunaDominioJComboBox.setEnabled(false);
        colunaDominioJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colunaDominioJComboBoxActionPerformed(evt);
            }
        });

        regexJLabel.setText("Expressão Regular");

        regexJTextField.setText("/^[0-9]{14}$|^[0-9]{11}$/");
        regexJTextField.setEnabled(false);

        editarRegexJRadioButton.setText("Editar");
        editarRegexJRadioButton.setEnabled(false);
        editarRegexJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editarRegexJRadioButtonActionPerformed(evt);
            }
        });

        contatoJLabel.setText("Contatos");

        nomeContatoJLabel.setText("Nome");

        emailContatoJLabel.setText("E-mail");

        telefoneJLabel.setText("Telefone");

        telefoneContatoJTextField.setEnabled(false);

        emailContatoJTextField.setEnabled(false);

        nomeContatoJTextField.setEnabled(false);

        addContatoJButton.setText("Adicionar");
        addContatoJButton.setEnabled(false);
        addContatoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addContatoJButtonActionPerformed(evt);
            }
        });

        contatosJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Nome", "E-mail", "Telefone"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        contatosJTable.setEnabled(false);
        jScrollPane12.setViewportView(contatosJTable);

        removeContatoJButton.setText("Remover Contato");
        removeContatoJButton.setEnabled(false);
        removeContatoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeContatoJButtonActionPerformed(evt);
            }
        });

        queryJLabel.setText("Query");

        queryJTextArea.setColumns(20);
        queryJTextArea.setRows(5);
        queryJTextArea.setToolTipText("Escreva uma query PROC SQL do SAS.");
        queryJTextArea.setEnabled(false);
        jScrollPane9.setViewportView(queryJTextArea);

        testarQueryJToggleButton.setText("Testar");
        testarQueryJToggleButton.setEnabled(false);
        testarQueryJToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testarQueryJToggleButtonActionPerformed(evt);
            }
        });

        atualizadaoJLabel.setText("Chave de Atualização");

        atualizacaoJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sim", "Não" }));
        atualizacaoJComboBox.setEnabled(false);

        urlJLabel.setText("URL");

        urlJTextField.setText("https://sistemasnet/wiki/doku.php?id=artigos:coleta_");
        urlJTextField.setEnabled(false);

        prazoJLabel.setText("Prazo");

        contadorDiasJSlider.setMaximum(365);
        contadorDiasJSlider.setMinimum(15);
        contadorDiasJSlider.setValue(30);
        contadorDiasJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                contadorDiasJSliderStateChanged(evt);
            }
        });

        contadorDiasJLabel.setText("30 dias");

        tipoPrazoJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Úteis", "Corridos" }));

        javax.swing.GroupLayout incluirColetaJPanelLayout = new javax.swing.GroupLayout(incluirColetaJPanel);
        incluirColetaJPanel.setLayout(incluirColetaJPanelLayout);
        incluirColetaJPanelLayout.setHorizontalGroup(
            incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addComponent(urlJLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jSeparator4)
                    .addComponent(jSeparator1)
                    .addComponent(jSeparator2)
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 554, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(addValidacaoJButton)
                                    .addComponent(removeValidacaoJButton)))
                            .addComponent(validacoesDisponiveisJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addComponent(validacoesRealizadasJLabel)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 296, Short.MAX_VALUE)
                                .addContainerGap())))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(leiauteJLabel)
                                    .addComponent(tipoLeiauteJLabel))
                                .addGap(23, 23, 23)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(leiauteJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(tipoLeiauteJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addComponent(emailContatoJLabel)
                                            .addComponent(nomeContatoJLabel))
                                        .addGap(18, 18, 18)
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(nomeContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(emailContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(telefoneJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(telefoneContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(addContatoJButton)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(removeContatoJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(colunasJScrollPane, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                                .addComponent(descricaoColunaJLabel)
                                .addGap(10, 10, 10)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(incluirColunaJButton)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(limparColunaJButton))
                                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(dominioJLabel)
                                    .addComponent(nomeColunaJLabel)
                                    .addComponent(aliasColunaJLabel))
                                .addGap(18, 18, 18)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(nomeColunaJTextField)
                                            .addComponent(aliasColunaJTextField))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(classeJLabel)
                                            .addComponent(tipoColunaJLabel))
                                        .addGap(46, 46, 46))
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(dominioJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(colunaJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(colunaDominioJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(classeColunaJComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(tipoColunaJComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, 211, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(regexJLabel)
                                            .addComponent(tamanhoColunaJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(24, 24, 24)
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                                .addComponent(regexJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 212, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(editarRegexJRadioButton))
                                            .addComponent(tamanhoColunaJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(atualizadaoJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(atualizacaoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(enderecoScriptJLabel)
                                    .addComponent(periodicidadeJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(descricaoLeiauteJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(periodicidadeJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(prazoJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(contadorDiasJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(contadorDiasJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tipoPrazoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(enderecoScriptJTextField, javax.swing.GroupLayout.Alignment.TRAILING)))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tipoBancoJLabel)
                                    .addComponent(enderecoBancoJLabel)
                                    .addComponent(queryJLabel)
                                    .addComponent(tabelaJLabel))
                                .addGap(4, 4, 4)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(enderecoBancoJTextField)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(enderecoBancoJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jScrollPane9)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(tabelaJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 672, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(tabelaOkJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(tipoBancoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(nomeLeiauteJLabel)
                                        .addGap(16, 16, 16)
                                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(urlJTextField)
                                            .addComponent(leiauteJTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 673, Short.MAX_VALUE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(leiauteOkJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(contatoJLabel)
                                    .addComponent(colunasJLabel)
                                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                                        .addComponent(acaoJLabel)
                                        .addGap(53, 53, 53)
                                        .addComponent(acaoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(24, 24, 24)
                                        .addComponent(tipoScriptJLabel)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(tipoScriptJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(validacoesJLabel))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(testarQueryJToggleButton, javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(incluirColetaJButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(excluirColunaJButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                        .addContainerGap())))
        );
        incluirColetaJPanelLayout.setVerticalGroup(
            incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tipoLeiauteJLabel)
                    .addComponent(tipoLeiauteJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leiauteJLabel)
                    .addComponent(leiauteJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(nomeLeiauteJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(leiauteJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(leiauteOkJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlJLabel)
                    .addComponent(urlJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tipoBancoJLabel)
                    .addComponent(tipoBancoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enderecoBancoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enderecoBancoJButton)
                    .addComponent(enderecoBancoJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabelaOkJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tabelaJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tabelaJLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(queryJLabel)
                    .addComponent(jScrollPane9, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(testarQueryJToggleButton)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addGap(4, 4, 4)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tipoScriptJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(tipoScriptJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(acaoJComboBox)
                            .addComponent(acaoJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(descricaoLeiauteJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(enderecoScriptJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(enderecoScriptJLabel))
                        .addGap(5, 5, 5)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(periodicidadeJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(periodicidadeJLabel)
                            .addComponent(prazoJLabel)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(contadorDiasJSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(contadorDiasJLabel)
                                .addComponent(tipoPrazoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colunasJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dominioJLabel)
                    .addComponent(dominioJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(colunaJLabel)
                    .addComponent(colunaDominioJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(atualizadaoJLabel)
                    .addComponent(atualizacaoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nomeColunaJLabel)
                    .addComponent(nomeColunaJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(classeJLabel)
                    .addComponent(classeColunaJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(regexJLabel)
                    .addComponent(regexJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(editarRegexJRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(aliasColunaJLabel)
                        .addComponent(aliasColunaJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(tipoColunaJLabel)
                        .addComponent(tamanhoColunaJLabel)
                        .addComponent(tipoColunaJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(tamanhoColunaJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(descricaoColunaJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limparColunaJButton)
                    .addComponent(incluirColunaJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(colunasJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(excluirColunaJButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(validacoesJLabel)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addComponent(addValidacaoJButton)
                        .addGap(34, 34, 34)
                        .addComponent(removeValidacaoJButton))
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(validacoesDisponiveisJLabel)
                            .addComponent(validacoesRealizadasJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator4, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(contatoJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane12, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(incluirColetaJPanelLayout.createSequentialGroup()
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nomeContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(nomeContatoJLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(emailContatoJLabel)
                            .addComponent(emailContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirColetaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(telefoneJLabel)
                            .addComponent(telefoneContatoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addContatoJButton)))
                    .addComponent(removeContatoJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(incluirColetaJButton)
                .addContainerGap())
        );

        acaoJLabel.getAccessibleContext().setAccessibleName("acaoJLabel");

        incluirColetaJScrollPane.setViewportView(incluirColetaJPanel);

        javax.swing.GroupLayout incluirColetaJFrameLayout = new javax.swing.GroupLayout(incluirColetaJFrame.getContentPane());
        incluirColetaJFrame.getContentPane().setLayout(incluirColetaJFrameLayout);
        incluirColetaJFrameLayout.setHorizontalGroup(
            incluirColetaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(incluirColetaJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 958, Short.MAX_VALUE)
        );
        incluirColetaJFrameLayout.setVerticalGroup(
            incluirColetaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(incluirColetaJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 773, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        incluirColetaJFrame.getAccessibleContext().setAccessibleParent(this);

        valoresDuplicadosJFrame.setAlwaysOnTop(true);
        valoresDuplicadosJFrame.setLocationByPlatform(true);
        valoresDuplicadosJFrame.setResizable(false);

        colunaDadoDuplicadosJLabel.setText("Coluna Dado");

        colunaDadoDuplicadoJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colunaDadoDuplicadoJComboBoxActionPerformed(evt);
            }
        });

        colunasDisponiveisDuplicadoJLabel.setText("Colunas Disponíveis");

        colunasDisponiveisDuplicadoJList.setEnabled(false);
        jScrollPane8.setViewportView(colunasDisponiveisDuplicadoJList);

        addColunaDuplicadoJButton.setText(">>");
        addColunaDuplicadoJButton.setEnabled(false);
        addColunaDuplicadoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addColunaDuplicadoJButtonActionPerformed(evt);
            }
        });

        removeColunaDuplicadoJButton.setText("<<");
        removeColunaDuplicadoJButton.setEnabled(false);
        removeColunaDuplicadoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeColunaDuplicadoJButtonActionPerformed(evt);
            }
        });

        colunasConsolidacaoDuplicadoJList.setModel(new DefaultListModel());
        colunasConsolidacaoDuplicadoJList.setEnabled(false);
        colunasConsolidacaoDuplicadoJScrollPane.setViewportView(colunasConsolidacaoDuplicadoJList);

        cancelarColunasDuplicadoJButton.setText("Apagar");
        cancelarColunasDuplicadoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarColunasDuplicadoJButtonActionPerformed(evt);
            }
        });

        okColunasDuplicadoJButton.setText("Ok");
        okColunasDuplicadoJButton.setEnabled(false);
        okColunasDuplicadoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okColunasDuplicadoJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout valoresDuplicadosJPanelLayout = new javax.swing.GroupLayout(valoresDuplicadosJPanel);
        valoresDuplicadosJPanel.setLayout(valoresDuplicadosJPanelLayout);
        valoresDuplicadosJPanelLayout.setHorizontalGroup(
            valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 540, Short.MAX_VALUE)
            .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                            .addComponent(colunasDisponiveisDuplicadoJLabel)
                            .addGap(0, 0, Short.MAX_VALUE))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, valoresDuplicadosJPanelLayout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(okColunasDuplicadoJButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelarColunasDuplicadoJButton))
                        .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                            .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                                    .addComponent(colunaDadoDuplicadosJLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(colunaDadoDuplicadoJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, valoresDuplicadosJPanelLayout.createSequentialGroup()
                                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addColunaDuplicadoJButton)
                                        .addComponent(removeColunaDuplicadoJButton))))
                            .addGap(18, 18, 18)
                            .addComponent(colunasConsolidacaoDuplicadoJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        valoresDuplicadosJPanelLayout.setVerticalGroup(
            valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 232, Short.MAX_VALUE)
            .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colunaDadoDuplicadosJLabel)
                        .addComponent(colunaDadoDuplicadoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(colunasDisponiveisDuplicadoJLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(valoresDuplicadosJPanelLayout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(addColunaDuplicadoJButton)
                            .addGap(26, 26, 26)
                            .addComponent(removeColunaDuplicadoJButton))
                        .addComponent(colunasConsolidacaoDuplicadoJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(valoresDuplicadosJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelarColunasDuplicadoJButton)
                        .addComponent(okColunasDuplicadoJButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout valoresDuplicadosJFrameLayout = new javax.swing.GroupLayout(valoresDuplicadosJFrame.getContentPane());
        valoresDuplicadosJFrame.getContentPane().setLayout(valoresDuplicadosJFrameLayout);
        valoresDuplicadosJFrameLayout.setHorizontalGroup(
            valoresDuplicadosJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, valoresDuplicadosJFrameLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(valoresDuplicadosJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        valoresDuplicadosJFrameLayout.setVerticalGroup(
            valoresDuplicadosJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(valoresDuplicadosJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        valoresDuplicadosJFrame.getAccessibleContext().setAccessibleParent(incluirColetaJFrame);

        crescimentoJFrame.setAlwaysOnTop(true);
        crescimentoJFrame.setLocationByPlatform(true);
        crescimentoJFrame.setResizable(false);

        colunaDadoJLabel.setText("Coluna Dado");

        colunaDadoJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colunaDadoJComboBoxActionPerformed(evt);
            }
        });

        colunasDisponiveisConsolidacaoJLabel.setText("Colunas Disponíveis");

        colunasDisponiveisConsolidacaoJList.setEnabled(false);
        jScrollPane5.setViewportView(colunasDisponiveisConsolidacaoJList);

        addConsolidacaoJButton.setText(">>");
        addConsolidacaoJButton.setEnabled(false);
        addConsolidacaoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addConsolidacaoJButtonActionPerformed(evt);
            }
        });

        removeConsolidacaoJButton.setText("<<");
        removeConsolidacaoJButton.setEnabled(false);
        removeConsolidacaoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeConsolidacaoJButtonActionPerformed(evt);
            }
        });

        cancelCrescimentoJButton.setText("Apagar");
        cancelCrescimentoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelCrescimentoJButtonActionPerformed(evt);
            }
        });

        okCrescimentoJButton.setText("Ok");
        okCrescimentoJButton.setEnabled(false);
        okCrescimentoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okCrescimentoJButtonActionPerformed(evt);
            }
        });

        colunasConsolidacaoJLabel.setText("Colunas de Consolidação");

        limitesJLabel.setText("Limites");

        inicioJLabel.setText("Início");

        inicioJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        inicioJFormattedTextField.setEnabled(false);
        inicioJFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                inicioJFormattedTextFieldFocusLost(evt);
            }
        });
        inicioJFormattedTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                inicioJFormattedTextFieldKeyReleased(evt);
            }
        });

        fimJLabel.setText("Fim");

        fimJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        fimJFormattedTextField.setEnabled(false);
        fimJFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                fimJFormattedTextFieldFocusLost(evt);
            }
        });
        fimJFormattedTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fimJFormattedTextFieldKeyReleased(evt);
            }
        });

        crescimentoMaximoJLabel.setText("Crescimento Máximo");

        crescimentoMaximoJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        crescimentoMaximoJFormattedTextField.setEnabled(false);
        crescimentoMaximoJFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                crescimentoMaximoJFormattedTextFieldFocusLost(evt);
            }
        });
        crescimentoMaximoJFormattedTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                crescimentoMaximoJFormattedTextFieldKeyReleased(evt);
            }
        });

        crescimentoMinimoJLabel.setText("Crescimento Mínimo");

        crescimentoMinimoJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        crescimentoMinimoJFormattedTextField.setEnabled(false);
        crescimentoMinimoJFormattedTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                crescimentoMinimoJFormattedTextFieldFocusLost(evt);
            }
        });
        crescimentoMinimoJFormattedTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                crescimentoMinimoJFormattedTextFieldKeyReleased(evt);
            }
        });

        limparLimiteJButton.setText("Limpar");
        limparLimiteJButton.setEnabled(false);
        limparLimiteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limparLimiteJButtonActionPerformed(evt);
            }
        });

        addLimiteJButton.setText("Incluir");
        addLimiteJButton.setEnabled(false);
        addLimiteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLimiteJButtonActionPerformed(evt);
            }
        });

        limitesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Inicío", "Fim", "Crescimento Máximo", "Crescimento Mínimo"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        limitesJTable.setEnabled(false);
        jScrollPane7.setViewportView(limitesJTable);

        excluirLimiteJButton.setText("Excluir Último Limite");
        excluirLimiteJButton.setEnabled(false);
        excluirLimiteJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excluirLimiteJButtonActionPerformed(evt);
            }
        });

        colunasConsolidacaoJList.setModel(new DefaultListModel());
        colunasConsolidacaoJList.setEnabled(false);
        jScrollPane6.setViewportView(colunasConsolidacaoJList);

        javax.swing.GroupLayout crescimentoJPanelLayout = new javax.swing.GroupLayout(crescimentoJPanel);
        crescimentoJPanel.setLayout(crescimentoJPanelLayout);
        crescimentoJPanelLayout.setHorizontalGroup(
            crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 683, Short.MAX_VALUE)
            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jSeparator3)
                        .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                            .addGap(20, 20, 20)
                            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(crescimentoMaximoJLabel)
                                        .addComponent(crescimentoMinimoJLabel))
                                    .addGap(18, 18, 18)
                                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(crescimentoMaximoJFormattedTextField)
                                        .addComponent(crescimentoMinimoJFormattedTextField)))
                                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(inicioJLabel)
                                        .addComponent(fimJLabel))
                                    .addGap(91, 91, 91)
                                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(inicioJFormattedTextField)
                                        .addComponent(fimJFormattedTextField)))))
                        .addComponent(jScrollPane7, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crescimentoJPanelLayout.createSequentialGroup()
                            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(colunasDisponiveisConsolidacaoJLabel)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                                    .addGap(10, 10, 10)
                                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(removeConsolidacaoJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(addConsolidacaoJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crescimentoJPanelLayout.createSequentialGroup()
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(colunasConsolidacaoJLabel)
                                    .addGap(166, 166, 166))))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crescimentoJPanelLayout.createSequentialGroup()
                            .addComponent(addLimiteJButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(limparLimiteJButton))
                        .addComponent(excluirLimiteJButton, javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, crescimentoJPanelLayout.createSequentialGroup()
                            .addComponent(okCrescimentoJButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelCrescimentoJButton))
                        .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(limitesJLabel)
                                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                                    .addComponent(colunaDadoJLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(colunaDadoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGap(0, 0, Short.MAX_VALUE)))
                    .addContainerGap()))
        );
        crescimentoJPanelLayout.setVerticalGroup(
            crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 595, Short.MAX_VALUE)
            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colunaDadoJLabel)
                        .addComponent(colunaDadoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colunasDisponiveisConsolidacaoJLabel)
                        .addComponent(colunasConsolidacaoJLabel))
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                            .addGap(28, 28, 28)
                            .addComponent(addConsolidacaoJButton)
                            .addGap(31, 31, 31)
                            .addComponent(removeConsolidacaoJButton))
                        .addGroup(crescimentoJPanelLayout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(limitesJLabel)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(inicioJLabel)
                        .addComponent(inicioJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(fimJLabel)
                        .addComponent(fimJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(crescimentoMaximoJLabel)
                        .addComponent(crescimentoMaximoJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(crescimentoMinimoJLabel)
                        .addComponent(crescimentoMinimoJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(limparLimiteJButton)
                        .addComponent(addLimiteJButton))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(excluirLimiteJButton)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(crescimentoJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelCrescimentoJButton)
                        .addComponent(okCrescimentoJButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout crescimentoJFrameLayout = new javax.swing.GroupLayout(crescimentoJFrame.getContentPane());
        crescimentoJFrame.getContentPane().setLayout(crescimentoJFrameLayout);
        crescimentoJFrameLayout.setHorizontalGroup(
            crescimentoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 683, Short.MAX_VALUE)
            .addGroup(crescimentoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crescimentoJFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(crescimentoJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        crescimentoJFrameLayout.setVerticalGroup(
            crescimentoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 595, Short.MAX_VALUE)
            .addGroup(crescimentoJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(crescimentoJFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(crescimentoJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        crescimentoJFrame.getAccessibleContext().setAccessibleParent(incluirColetaJFrame);

        densidadeJFrame.setAlwaysOnTop(true);
        densidadeJFrame.setLocationByPlatform(true);
        densidadeJFrame.setResizable(false);

        denominadorJLabel.setText("Denominador");

        denominadorJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Domicílio", "População" }));

        multiplicadorJLabel.setText("Multiplicador");

        multiplicadorJSlider.setMajorTickSpacing(1);
        multiplicadorJSlider.setMaximum(200);
        multiplicadorJSlider.setValue(100);
        multiplicadorJSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                multiplicadorJSliderStateChanged(evt);
            }
        });

        valorMultiplicadorJLabel.setText(multiplicadorJSlider.getValue() + "%");

        cancelDensidadeJButton.setText("Apagar");
        cancelDensidadeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelDensidadeJButtonActionPerformed(evt);
            }
        });

        okDensidadeJButton.setText("Ok");
        okDensidadeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okDensidadeJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout densidadeJPanelLayout = new javax.swing.GroupLayout(densidadeJPanel);
        densidadeJPanel.setLayout(densidadeJPanelLayout);
        densidadeJPanelLayout.setHorizontalGroup(
            densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 366, Short.MAX_VALUE)
            .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(densidadeJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(densidadeJPanelLayout.createSequentialGroup()
                            .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(denominadorJLabel)
                                .addComponent(multiplicadorJLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(densidadeJPanelLayout.createSequentialGroup()
                                    .addComponent(multiplicadorJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(valorMultiplicadorJLabel))
                                .addComponent(denominadorJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, densidadeJPanelLayout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(okDensidadeJButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelDensidadeJButton)))
                    .addContainerGap()))
        );
        densidadeJPanelLayout.setVerticalGroup(
            densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 107, Short.MAX_VALUE)
            .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(densidadeJPanelLayout.createSequentialGroup()
                    .addGap(4, 4, 4)
                    .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(denominadorJLabel)
                        .addComponent(denominadorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGap(15, 15, 15)
                    .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(valorMultiplicadorJLabel)
                        .addComponent(multiplicadorJSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(multiplicadorJLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(densidadeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelDensidadeJButton)
                        .addComponent(okDensidadeJButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout densidadeJFrameLayout = new javax.swing.GroupLayout(densidadeJFrame.getContentPane());
        densidadeJFrame.getContentPane().setLayout(densidadeJFrameLayout);
        densidadeJFrameLayout.setHorizontalGroup(
            densidadeJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(densidadeJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        densidadeJFrameLayout.setVerticalGroup(
            densidadeJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(densidadeJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        densidadeJFrame.getAccessibleContext().setAccessibleParent(incluirColetaJFrame);

        chaveUnicaJFrame.setAlwaysOnTop(true);
        chaveUnicaJFrame.setLocationByPlatform(true);
        chaveUnicaJFrame.setResizable(false);

        colunasDisponiveisChaveUnicaJLabel.setText("Colunas Disponíveis");

        colunasDisponiveisChaveUnicaJScrollPane.setViewportView(colunasDisponiveisChaveUnicaJList);

        addChaveUnicaJButton.setText(">>");
        addChaveUnicaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addChaveUnicaJButtonActionPerformed(evt);
            }
        });

        removeChaveUnicaJButton.setText("<<");
        removeChaveUnicaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeChaveUnicaJButtonActionPerformed(evt);
            }
        });

        colunasConsolidacaoChaveUnicaJList.setModel(new DefaultListModel());
        colunasConsolidacaoChaveUnicaJScrollPane.setViewportView(colunasConsolidacaoChaveUnicaJList);

        cancelarColunasChaveUnicaJButton.setText("Apagar");
        cancelarColunasChaveUnicaJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarColunasChaveUnicaJButtonActionPerformed(evt);
            }
        });

        okColunasChaveJButton.setText("Ok");
        okColunasChaveJButton.setEnabled(false);
        okColunasChaveJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okColunasChaveJButtonActionPerformed(evt);
            }
        });

        colunasChaveUnicaLabel.setText("Colunas de Chave Única");

        javax.swing.GroupLayout chaveUnicaJPanelLayout = new javax.swing.GroupLayout(chaveUnicaJPanel);
        chaveUnicaJPanel.setLayout(chaveUnicaJPanelLayout);
        chaveUnicaJPanelLayout.setHorizontalGroup(
            chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(chaveUnicaJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, chaveUnicaJPanelLayout.createSequentialGroup()
                            .addGap(0, 0, Short.MAX_VALUE)
                            .addComponent(okColunasChaveJButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cancelarColunasChaveUnicaJButton))
                        .addGroup(chaveUnicaJPanelLayout.createSequentialGroup()
                            .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(chaveUnicaJPanelLayout.createSequentialGroup()
                                    .addComponent(colunasDisponiveisChaveUnicaJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(addChaveUnicaJButton)
                                        .addComponent(removeChaveUnicaJButton)))
                                .addComponent(colunasDisponiveisChaveUnicaJLabel))
                            .addGap(18, 18, 18)
                            .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(colunasChaveUnicaLabel)
                                .addComponent(colunasConsolidacaoChaveUnicaJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))))
                    .addContainerGap()))
        );
        chaveUnicaJPanelLayout.setVerticalGroup(
            chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 206, Short.MAX_VALUE)
            .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(chaveUnicaJPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(colunasDisponiveisChaveUnicaJLabel)
                        .addComponent(colunasChaveUnicaLabel))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(colunasDisponiveisChaveUnicaJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(chaveUnicaJPanelLayout.createSequentialGroup()
                            .addGap(24, 24, 24)
                            .addComponent(addChaveUnicaJButton)
                            .addGap(26, 26, 26)
                            .addComponent(removeChaveUnicaJButton))
                        .addComponent(colunasConsolidacaoChaveUnicaJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(chaveUnicaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(cancelarColunasChaveUnicaJButton)
                        .addComponent(okColunasChaveJButton))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout chaveUnicaJFrameLayout = new javax.swing.GroupLayout(chaveUnicaJFrame.getContentPane());
        chaveUnicaJFrame.getContentPane().setLayout(chaveUnicaJFrameLayout);
        chaveUnicaJFrameLayout.setHorizontalGroup(
            chaveUnicaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chaveUnicaJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        chaveUnicaJFrameLayout.setVerticalGroup(
            chaveUnicaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(chaveUnicaJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        chaveUnicaJFrame.getAccessibleContext().setAccessibleParent(incluirColetaJFrame);

        pequisaPermissaoJLabel.setText("Pesquisar por");

        pesquisaPermissoesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "Leiaute", "Usuário" }));

        tipoLeiautePermissoesJLabel.setText("Tipo Leiaute");

        tipoLeiautePermissoesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "Coleta", "Domínio" }));
        tipoLeiautePermissoesJComboBox.setEnabled(false);

        leiautePermissoesJLabel.setText("Leiaute");

        leiautePermissoesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione..." }));
        leiautePermissoesJComboBox.setEnabled(false);

        parametroPermissoesJLabel.setText("Parâmetro");

        parametroPermissoesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione..." }));
        parametroPermissoesJComboBox.setEnabled(false);

        valorPermissoesJLabel.setText("Valor");

        valorPermissoesJTextField.setEnabled(false);

        usuarioPermissoesJLabel.setText("Usuário");

        usuarioPermissoesJTextField.setEnabled(false);

        limparPermissoesJButton.setText("Limpar");

        okPermissoesJButton.setText("Ok");

        listaPermissoesJLabel.setText("Permissões");

        permisoesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane14.setViewportView(permisoesJTable);

        excluirPermissoesJButton.setText("Excluir Selecionados");

        incluirPermissoesJButton.setText("Incluir");
        incluirPermissoesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incluirPermissoesJButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout permissoesJPanelLayout = new javax.swing.GroupLayout(permissoesJPanel);
        permissoesJPanel.setLayout(permissoesJPanelLayout);
        permissoesJPanelLayout.setHorizontalGroup(
            permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(permissoesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(permissoesJPanelLayout.createSequentialGroup()
                        .addComponent(incluirPermissoesJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(excluirPermissoesJButton))
                    .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(permissoesJPanelLayout.createSequentialGroup()
                            .addComponent(pequisaPermissaoJLabel)
                            .addGap(18, 18, 18)
                            .addComponent(pesquisaPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 206, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(permissoesJPanelLayout.createSequentialGroup()
                                .addComponent(okPermissoesJButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(limparPermissoesJButton))
                            .addGroup(permissoesJPanelLayout.createSequentialGroup()
                                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(tipoLeiautePermissoesJLabel)
                                    .addComponent(leiautePermissoesJLabel)
                                    .addComponent(parametroPermissoesJLabel)
                                    .addComponent(valorPermissoesJLabel)
                                    .addComponent(usuarioPermissoesJLabel))
                                .addGap(25, 25, 25)
                                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(tipoLeiautePermissoesJComboBox, 0, 205, Short.MAX_VALUE)
                                    .addComponent(leiautePermissoesJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(parametroPermissoesJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(valorPermissoesJTextField)
                                    .addComponent(usuarioPermissoesJTextField))))
                        .addComponent(listaPermissoesJLabel)
                        .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 599, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        permissoesJPanelLayout.setVerticalGroup(
            permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(permissoesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pequisaPermissaoJLabel)
                    .addComponent(pesquisaPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tipoLeiautePermissoesJLabel)
                    .addComponent(tipoLeiautePermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(leiautePermissoesJLabel)
                    .addComponent(leiautePermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(parametroPermissoesJLabel)
                    .addComponent(parametroPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(valorPermissoesJLabel)
                    .addComponent(valorPermissoesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usuarioPermissoesJLabel)
                    .addComponent(usuarioPermissoesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(limparPermissoesJButton)
                    .addComponent(okPermissoesJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(listaPermissoesJLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane14, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(permissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(excluirPermissoesJButton)
                    .addComponent(incluirPermissoesJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout permissoesJFrameLayout = new javax.swing.GroupLayout(permissoesJFrame.getContentPane());
        permissoesJFrame.getContentPane().setLayout(permissoesJFrameLayout);
        permissoesJFrameLayout.setHorizontalGroup(
            permissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 639, Short.MAX_VALUE)
            .addGroup(permissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(permissoesJFrameLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(permissoesJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        permissoesJFrameLayout.setVerticalGroup(
            permissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 475, Short.MAX_VALUE)
            .addGroup(permissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(permissoesJFrameLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(permissoesJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        usuarioIncluirPermissoesJLabel.setText("Usuário");

        usuarioPermissoesJList.setModel(new DefaultListModel());
        jScrollPane11.setViewportView(usuarioPermissoesJList);

        incluirUsuarioPermissoesJButton.setText("+");
        incluirUsuarioPermissoesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                incluirUsuarioPermissoesJButtonActionPerformed(evt);
            }
        });

        excluirUsuarioJButton.setText("-");
        excluirUsuarioJButton.setEnabled(false);
        excluirUsuarioJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excluirUsuarioJButtonActionPerformed(evt);
            }
        });

        leiauteInlcuirPermissoesJLabel.setText("Leiaute");

        tipoLeiauteIncluirPermissoesJLabel.setText("Tipo Leiaute");

        tipoLeiauteIncluirPermissoesJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "Coleta", "Domínio" }));
        tipoLeiauteIncluirPermissoesJComboBox.setEnabled(false);
        tipoLeiauteIncluirPermissoesJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tipoLeiauteIncluirPermissoesJComboBoxActionPerformed(evt);
            }
        });

        leiauteIncluirPermissoesJComboBox.setEnabled(false);

        parametrosIncluirPermissoesJLabel.setText("Parâmetro");

        parametrosIncluirPermissoesJComboBox.setEnabled(false);

        valorIncluirPermissoesJLabel.setText("Valor");

        valorIncluirPermissoesJTextField.setEnabled(false);

        incluirValorPermissoesJButton.setText("+");
        incluirValorPermissoesJButton.setEnabled(false);

        excluirValorPermissoesJButton.setText("-");
        excluirValorPermissoesJButton.setEnabled(false);

        cancelarIncluirPermissoesJButton.setText("Cancelar");
        cancelarIncluirPermissoesJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelarIncluirPermissoesJButtonActionPerformed(evt);
            }
        });

        csvIncluirPermissoesJButton.setText("CSV");

        okIncluirPermissoesJButton.setText("Ok");
        okIncluirPermissoesJButton.setEnabled(false);

        parametrosPermissoesJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Parâmetro", "Valor"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        parametrosPermissoesJTable.setEnabled(false);
        jScrollPane15.setViewportView(parametrosPermissoesJTable);

        javax.swing.GroupLayout incluirPermissoesJPanelLayout = new javax.swing.GroupLayout(incluirPermissoesJPanel);
        incluirPermissoesJPanel.setLayout(incluirPermissoesJPanelLayout);
        incluirPermissoesJPanelLayout.setHorizontalGroup(
            incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(incluirPermissoesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(usuarioIncluirPermissoesJLabel)
                    .addComponent(tipoLeiauteIncluirPermissoesJLabel)
                    .addComponent(leiauteInlcuirPermissoesJLabel)
                    .addComponent(parametrosIncluirPermissoesJLabel)
                    .addComponent(valorIncluirPermissoesJLabel))
                .addGap(18, 18, 18)
                .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(incluirPermissoesJPanelLayout.createSequentialGroup()
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(valorIncluirPermissoesJTextField)
                            .addComponent(parametrosIncluirPermissoesJComboBox, 0, 262, Short.MAX_VALUE)
                            .addComponent(leiauteIncluirPermissoesJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(incluirPermissoesJTextField)
                            .addComponent(jScrollPane11)
                            .addComponent(tipoLeiauteIncluirPermissoesJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(incluirValorPermissoesJButton)
                            .addComponent(excluirValorPermissoesJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(excluirUsuarioJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(incluirUsuarioPermissoesJButton)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, incluirPermissoesJPanelLayout.createSequentialGroup()
                        .addComponent(okIncluirPermissoesJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(csvIncluirPermissoesJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelarIncluirPermissoesJButton)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        incluirPermissoesJPanelLayout.setVerticalGroup(
            incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(incluirPermissoesJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(incluirPermissoesJPanelLayout.createSequentialGroup()
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(usuarioIncluirPermissoesJLabel)
                            .addComponent(incluirUsuarioPermissoesJButton)
                            .addComponent(incluirPermissoesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(excluirUsuarioJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(incluirPermissoesJPanelLayout.createSequentialGroup()
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tipoLeiauteIncluirPermissoesJLabel)
                            .addComponent(tipoLeiauteIncluirPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(leiauteInlcuirPermissoesJLabel)
                            .addComponent(leiauteIncluirPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(parametrosIncluirPermissoesJLabel)
                            .addComponent(parametrosIncluirPermissoesJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(valorIncluirPermissoesJLabel)
                            .addComponent(valorIncluirPermissoesJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(incluirValorPermissoesJButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane15, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(excluirValorPermissoesJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(incluirPermissoesJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelarIncluirPermissoesJButton)
                    .addComponent(csvIncluirPermissoesJButton)
                    .addComponent(okIncluirPermissoesJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout incluirPermissoesJFrameLayout = new javax.swing.GroupLayout(incluirPermissoesJFrame.getContentPane());
        incluirPermissoesJFrame.getContentPane().setLayout(incluirPermissoesJFrameLayout);
        incluirPermissoesJFrameLayout.setHorizontalGroup(
            incluirPermissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 413, Short.MAX_VALUE)
            .addGroup(incluirPermissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(incluirPermissoesJFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(incluirPermissoesJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );
        incluirPermissoesJFrameLayout.setVerticalGroup(
            incluirPermissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 464, Short.MAX_VALUE)
            .addGroup(incluirPermissoesJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(incluirPermissoesJFrameLayout.createSequentialGroup()
                    .addGap(0, 0, Short.MAX_VALUE)
                    .addComponent(incluirPermissoesJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 0, Short.MAX_VALUE)))
        );

        cronogramaJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Leiaute", "Periodicidade", "Prazo", "Última Coleta", "Próxima Coleta", "Atrasado", "Usuários"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane16.setViewportView(cronogramaJTable);

        cobrarJButton.setText("Cobrar Atrasados");
        cobrarJButton.setEnabled(false);
        cobrarJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cobrarJButtonActionPerformed(evt);
            }
        });

        atualizarJButton.setText("Atualizar");
        atualizarJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                atualizarJButtonActionPerformed(evt);
            }
        });

        cronogramaJProgressBar.setStringPainted(true);

        javax.swing.GroupLayout cronogramaJPanelLayout = new javax.swing.GroupLayout(cronogramaJPanel);
        cronogramaJPanel.setLayout(cronogramaJPanelLayout);
        cronogramaJPanelLayout.setHorizontalGroup(
            cronogramaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cronogramaJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(cronogramaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cronogramaJPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(cobrarJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(atualizarJButton))
                    .addComponent(jScrollPane16, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addComponent(cronogramaJProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        cronogramaJPanelLayout.setVerticalGroup(
            cronogramaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cronogramaJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cronogramaJProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane16, javax.swing.GroupLayout.PREFERRED_SIZE, 362, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(cronogramaJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(atualizarJButton)
                    .addComponent(cobrarJButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout cronogramaJFrameLayout = new javax.swing.GroupLayout(cronogramaJFrame.getContentPane());
        cronogramaJFrame.getContentPane().setLayout(cronogramaJFrameLayout);
        cronogramaJFrameLayout.setHorizontalGroup(
            cronogramaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cronogramaJFrameLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(cronogramaJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        cronogramaJFrameLayout.setVerticalGroup(
            cronogramaJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(cronogramaJFrameLayout.createSequentialGroup()
                .addGap(0, 15, Short.MAX_VALUE)
                .addComponent(cronogramaJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 14, Short.MAX_VALUE))
        );

        compararJFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        compararJFrame.setAlwaysOnTop(true);

        testarCompararJButton.setText("Testar");
        testarCompararJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testarCompararJButtonActionPerformed(evt);
            }
        });

        okCompararJButton.setText("Ok");
        okCompararJButton.setEnabled(false);
        okCompararJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okCompararJButtonActionPerformed(evt);
            }
        });

        limparCompararJButton.setText("Limpar");
        limparCompararJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                limparCompararJButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Utilize linguagem SAS. O código deve retornar uma tabela SAS chamada resultado com todas as colunas do arquivo csv e mais uma coluna chamada mensagem.");

        comparaJTextArea.setColumns(20);
        comparaJTextArea.setRows(5);
        comparaJTextArea.setText("PROC SQL;\n\tCREATE TABLE resultado AS\n\tSELECT\n\t\tt1.*,\n\t\t'mensagem de teste' as mensagem\n\tFROM DADOS t1;\nQUIT;");
        jScrollPane17.setViewportView(comparaJTextArea);

        comparaJTabbedPane.addTab("Código SAS", jScrollPane17);

        logComparaJTextArea.setEditable(false);
        logComparaJTextArea.setColumns(20);
        logComparaJTextArea.setRows(5);
        jScrollPane18.setViewportView(logComparaJTextArea);

        comparaJTabbedPane.addTab("Log", jScrollPane18);

        resultadoComparaJTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jScrollPane19.setViewportView(resultadoComparaJTable);

        comparaJTabbedPane.addTab("Resultado", jScrollPane19);

        javax.swing.GroupLayout compararJFrameLayout = new javax.swing.GroupLayout(compararJFrame.getContentPane());
        compararJFrame.getContentPane().setLayout(compararJFrameLayout);
        compararJFrameLayout.setHorizontalGroup(
            compararJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compararJFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(compararJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(comparaJTabbedPane)
                    .addGroup(compararJFrameLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 781, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 44, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, compararJFrameLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okCompararJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(limparCompararJButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(testarCompararJButton)))
                .addContainerGap())
        );
        compararJFrameLayout.setVerticalGroup(
            compararJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(compararJFrameLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(comparaJTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(compararJFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(testarCompararJButton)
                    .addComponent(okCompararJButton)
                    .addComponent(limparCompararJButton))
                .addContainerGap())
        );

        comparaJTabbedPane.getAccessibleContext().setAccessibleName("Código SAS");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cassini");
        setExtendedState(JFrame.ICONIFIED);
        setIconImage(loadImage("/cassini/saturn.jpg")
        );
        setLocationByPlatform(true);
        setResizable(false);
        setState(JFrame.ICONIFIED);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        usuarioJLabel.setText("Usuário");

        senhaJLabel.setText("Senha");

        contatosJLabel.setText("Contatos");

        adicionarContatoJButton.setText("Adicionar Contato");
        adicionarContatoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adicionarContatoJButtonActionPerformed(evt);
            }
        });

        contatosJList.setModel(new DefaultListModel());
        jScrollPane10.setViewportView(contatosJList);

        excluirContatoJButton.setText("Excluir Contato Selecionado");
        excluirContatoJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                excluirContatoJButtonActionPerformed(evt);
            }
        });

        atualizacaoJLabel.setText("Verificar novos emails a cada");

        atualizacaoJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        atualizacaoJFormattedTextField.setText("30");

        segundosJLabel.setText("segundos");

        iniciarJButton.setText("Iniciar");
        iniciarJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iniciarJButtonActionPerformed(evt);
            }
        });

        pararJButton.setText("Parar");
        pararJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pararJButtonActionPerformed(evt);
            }
        });

        servidorSMTPJLabel.setText("Servidor SMTP");

        servidorSMTPJTextField.setText("smtp.gmail.com");
        servidorSMTPJTextField.setEnabled(false);

        portaSMTPJLabel.setText("Porta SMTP");

        portaSMTPJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        portaSMTPJFormattedTextField.setText("465");
        portaSMTPJFormattedTextField.setEnabled(false);

        servidorIMAPJLabel.setText("Servidor IMAP");

        servidorIMAPJTextField.setText("imap.gmail.com");
        servidorIMAPJTextField.setEnabled(false);

        portaIMAPJLabel.setText("Porta IMAP");

        portaIMAPJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        portaIMAPJFormattedTextField.setText("993");
        portaIMAPJFormattedTextField.setEnabled(false);

        statusJLabel.setForeground(java.awt.Color.red);
        statusJLabel.setText("Cassini está parado");

        salvarSenhaJRadioButton.setText("Salvar Senha");
        salvarSenhaJRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                salvarSenhaJRadioButtonActionPerformed(evt);
            }
        });

        autoIniciarJRadioButton.setText("Auto Iniciar");
        autoIniciarJRadioButton.setEnabled(false);

        exchangeVersionJLabel.setText("Versão do MS Exchange");

        msExchangeVersaoJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "2007 SP1", "2010", "2010 SP1", "2010 SP2" }));
        msExchangeVersaoJComboBox.setSelectedIndex(1);

        servidorMSExchangeJLabel.setText("Servidor Exchange");

        servidorMSExchangeJTextField.setText("https://correioweb.anatel.gov.br/ews/Exchange.asmx");

        tipoServidorEmailJLabel.setText("Servidor de Email");

        servidorJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Selecione...", "MS Exchange", "SMTP/IMAP" }));
        servidorJComboBox.setSelectedIndex(1);
        servidorJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                servidorJComboBoxActionPerformed(evt);
            }
        });

        pastaRecebidosJLabel.setText("Pasta Recebidos");

        pastaRecebidosJButton.setText("...");
        pastaRecebidosJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pastaRecebidosJButtonActionPerformed(evt);
            }
        });

        pastaValidadosJLabel.setText("Pasta Validados");

        pastaValidadosJButton.setText("...");
        pastaValidadosJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pastaValidadosJButtonActionPerformed(evt);
            }
        });

        apagarEmailsJLabel.setText("Apagar Emails");

        apagarEmailsJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Nenhum", "Todos", "Todos do Cassini" }));
        apagarEmailsJComboBox.setSelectedIndex(2);

        sasServerJLabel.setText("Servidor SAS");
        sasServerJLabel.setToolTipText("");

        sasServerJTextField.setText("wisaspdin01.anatel.gov.br");

        sasPortJLabel.setText("Porta SAS");

        sasPortJFormattedTextField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        sasPortJFormattedTextField.setText("8591");

        logJLabel.setText("Log");

        logJTextArea.setEditable(false);
        logJTextArea.setColumns(20);
        logJTextArea.setRows(5);
        logJTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPane13.setViewportView(logJTextArea);

        pastaXMLJLabel.setText("Pasta XML");

        pastaXMLJTextField.setEnabled(false);

        pastaXMLJButton.setText("...");
        pastaXMLJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pastaXMLJButtonActionPerformed(evt);
            }
        });

        conexoesSASJLabel.setText("Conexões SAS: 0");

        javax.swing.GroupLayout configurarJPanelLayout = new javax.swing.GroupLayout(configurarJPanel);
        configurarJPanel.setLayout(configurarJPanelLayout);
        configurarJPanelLayout.setHorizontalGroup(
            configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurarJPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 646, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(apagarEmailsJLabel)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurarJPanelLayout.createSequentialGroup()
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                    .addComponent(exchangeVersionJLabel)
                                                    .addComponent(servidorSMTPJLabel)
                                                    .addComponent(portaSMTPJLabel)
                                                    .addComponent(servidorIMAPJLabel)
                                                    .addComponent(portaIMAPJLabel)
                                                    .addComponent(servidorMSExchangeJLabel))
                                                .addComponent(usuarioJLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                            .addComponent(senhaJLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                        .addComponent(contatosJLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                    .addComponent(pastaRecebidosJLabel, javax.swing.GroupLayout.Alignment.LEADING))
                                .addComponent(pastaValidadosJLabel, javax.swing.GroupLayout.Alignment.LEADING))
                            .addGap(18, 18, 18)
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(senhaJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(salvarSenhaJRadioButton))
                                .addComponent(usuarioJTextField)
                                .addComponent(sasPortJFormattedTextField)
                                .addComponent(servidorMSExchangeJTextField)
                                .addComponent(portaIMAPJFormattedTextField)
                                .addComponent(servidorIMAPJTextField)
                                .addComponent(portaSMTPJFormattedTextField)
                                .addComponent(servidorSMTPJTextField)
                                .addComponent(apagarEmailsJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(msExchangeVersaoJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sasServerJTextField)
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(contatoNovoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(adicionarContatoJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(excluirContatoJButton))
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(pastaRecebidosJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pastaRecebidosJButton))
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(pastaValidadosJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pastaValidadosJButton))))
                        .addComponent(sasServerJLabel)
                        .addComponent(sasPortJLabel)
                        .addComponent(logJLabel)
                        .addGroup(configurarJPanelLayout.createSequentialGroup()
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(tipoServidorEmailJLabel)
                                .addComponent(pastaXMLJLabel))
                            .addGap(51, 51, 51)
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(pastaXMLJTextField)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pastaXMLJButton))
                                .addComponent(servidorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 513, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurarJPanelLayout.createSequentialGroup()
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(atualizacaoJLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(atualizacaoJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(segundosJLabel)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(autoIniciarJRadioButton)
                                    .addGap(0, 0, Short.MAX_VALUE))
                                .addComponent(statusJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(conexoesSASJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(configurarJPanelLayout.createSequentialGroup()
                                    .addComponent(iniciarJButton)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(pararJButton))))))
                .addContainerGap())
        );
        configurarJPanelLayout.setVerticalGroup(
            configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurarJPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pastaXMLJLabel, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(pastaXMLJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(pastaXMLJButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tipoServidorEmailJLabel)
                    .addComponent(servidorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(exchangeVersionJLabel)
                    .addComponent(msExchangeVersaoJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(servidorSMTPJLabel)
                    .addComponent(servidorSMTPJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portaSMTPJLabel)
                    .addComponent(portaSMTPJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(servidorIMAPJLabel)
                    .addComponent(servidorIMAPJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portaIMAPJLabel)
                    .addComponent(portaIMAPJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(servidorMSExchangeJLabel)
                    .addComponent(servidorMSExchangeJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(apagarEmailsJLabel)
                    .addComponent(apagarEmailsJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sasServerJLabel)
                    .addComponent(sasServerJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(sasPortJLabel)
                    .addComponent(sasPortJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(usuarioJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usuarioJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(configurarJPanelLayout.createSequentialGroup()
                        .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(senhaJLabel)
                            .addComponent(senhaJPasswordField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(salvarSenhaJRadioButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(contatosJLabel)
                            .addComponent(contatoNovoJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(adicionarContatoJButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane10, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(excluirContatoJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pastaRecebidosJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pastaRecebidosJLabel)
                    .addComponent(pastaRecebidosJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(pastaValidadosJTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(pastaValidadosJLabel)
                    .addComponent(pastaValidadosJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(atualizacaoJLabel)
                    .addComponent(atualizacaoJFormattedTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(segundosJLabel)
                    .addComponent(autoIniciarJRadioButton)
                    .addComponent(iniciarJButton)
                    .addComponent(pararJButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(configurarJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusJLabel)
                    .addComponent(conexoesSASJLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(logJLabel)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane13, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        configurarJScrollPane.setViewportView(configurarJPanel);

        coletasJMenu.setText("Coletas");

        gerenciarColetaJMenuItem.setText("Gerenciar");
        gerenciarColetaJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gerenciarColetaJMenuItemActionPerformed(evt);
            }
        });
        coletasJMenu.add(gerenciarColetaJMenuItem);

        permissoesJMenuItem.setText("Permissões");
        permissoesJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                permissoesJMenuItemActionPerformed(evt);
            }
        });
        coletasJMenu.add(permissoesJMenuItem);

        cronogramaJMenuItem.setText("Cronograma");
        cronogramaJMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cronogramaJMenuItemActionPerformed(evt);
            }
        });
        coletasJMenu.add(cronogramaJMenuItem);

        enviarArquivoJMenuItem.setText("Enviar Arquivo");
        coletasJMenu.add(enviarArquivoJMenuItem);

        jMenuBar.add(coletasJMenu);

        setJMenuBar(jMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configurarJScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(configurarJScrollPane)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void pastaXMLJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pastaXMLJButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setDialogTitle("Pasta XML");
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            pastaXMLJTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath() + "\\");

            props.put("cassini.leiautes.folder", pastaXMLJTextField.getText());
            try {
                FileOutputStream fos = new FileOutputStream("./properties/.properties");
                props.store(fos, "Propriedades do Cassini");
                fos.flush();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            tipoLeiauteJComboBox.setEnabled(true);

        }
    }//GEN-LAST:event_pastaXMLJButtonActionPerformed

    private void leiauteJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_leiauteJComboBoxActionPerformed
        if (leiauteJComboBox.getSelectedItem().toString().equals("Novo Leiaute")) {
            leiauteJTextField.setEnabled(true);
            leiauteJTextField.setText("");
        } else if (!leiauteJComboBox.getSelectedItem().toString().equals("Selecione...")) {
            leiauteJTextField.setEnabled(true);
            leiauteJTextField.setText(leiauteJComboBox.getSelectedItem().toString());
            //TODO carregar xml
        } else {
            leiauteJTextField.setEnabled(false);
            leiauteJTextField.setText("");
        }

    }//GEN-LAST:event_leiauteJComboBoxActionPerformed

    private void leiauteJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_leiauteJTextFieldKeyReleased
        String folder = leiauteJTextField.getText() + (tipoLeiauteJComboBox.getSelectedItem().equals("Domínio") ? "\\dominios\\" : "");
        if (leiauteJTextField.getText().isEmpty()) {//sem nome de leiaute

            tipoBancoJComboBox.setEnabled(false);
            tipoBancoJComboBox.setSelectedIndex(0);
            enderecoBancoJTextField.setText("");
            enderecoBancoJTextField.setEnabled(false);
            enderecoBancoJButton.setEnabled(false);
            tabelaJTextField.setText("");
            tabelaJTextField.setEnabled(false);
            tabelaOkJLabel.setText("");

            acaoJComboBox.setEnabled(false);
            acaoJComboBox.setSelectedIndex(0);
            tipoScriptJComboBox.setEnabled(false);
            descricaoJTextArea.setEnabled(false);
            //descricaoJTextArea.setText("");
            periodicidadeJComboBox.setEnabled(false);
            leiauteOkJLabel.setText("");

            nomeColunaJTextField.setEnabled(false);
            //nomeColunaJTextField.setText("");
            aliasColunaJTextField.setEnabled(false);
            //aliasColunaJTextField.setText("");
            classeColunaJComboBox.setEnabled(false);
            //classeColunaJComboBox.setSelectedIndex(0);
            limparColunaJButton.setEnabled(false);
            incluirColunaJButton.setEnabled(false);
            colunasJTable.setEnabled(false);
            descricaoColunaJTextArea.setEnabled(false);
            //descricaoColunaJTextArea.setText("");

            tipoColunaJComboBox.setEnabled(false);
            tamanhoColunaJSlider.setEnabled(false);

            excluirColunaJButton.setEnabled(false);
            validacaoesDisponiveisJList.setEnabled(false);
            validacoesRealizadasJList.setEnabled(false);
            addValidacaoJButton.setEnabled(false);
            removeValidacaoJButton.setEnabled(false);
            incluirColetaJButton.setEnabled(false);
            urlJTextField.setEnabled(false);
            urlJTextField.setText("https://sistemasnet/wiki/doku.php?id=artigos:" + (tipoLeiauteJComboBox.getSelectedItem().equals("Domínio") ? "dominio_" : "coleta_"));

            //coletaJInternalFrame.hide();
        } else {//tem nome de leiaute

            DefaultComboBoxModel model = (DefaultComboBoxModel) leiauteJComboBox.getModel();

            boolean existe = false;
            if (!leiauteJTextField.getText().equals(model.getSelectedItem().toString())) {
                for (int i = 0; i < model.getSize(); i++) {
                    if (leiauteJTextField.getText().equals(model.getElementAt(i))) {
                        existe = true;
                        i = model.getSize();
                    }
                }
            }

            if (new File(folder + (tipoLeiauteJComboBox.getSelectedItem().equals("Coleta") ? "coleta_" : "dominio_") + leiauteJTextField.getText() + ".xml").isFile() || existe) {//leiaute já existe
                leiauteOkJLabel.setForeground(Color.red);
                leiauteOkJLabel.setText("Leiaute já existe");
                acaoJComboBox.setEnabled(false);
                acaoJComboBox.setSelectedIndex(0);
                tipoScriptJComboBox.setEnabled(false);
                descricaoJTextArea.setEnabled(false);
                descricaoJTextArea.setText("");
                periodicidadeJComboBox.setEnabled(false);

                tipoBancoJComboBox.setEnabled(false);
                tipoBancoJComboBox.setSelectedIndex(0);
                enderecoBancoJTextField.setText("");
                enderecoBancoJTextField.setEnabled(false);
                enderecoBancoJButton.setEnabled(false);
                tabelaJTextField.setText("");
                tabelaJTextField.setEnabled(false);
                tabelaOkJLabel.setText("");

                nomeColunaJTextField.setEnabled(false);
                //nomeColunaJTextField.setText("");
                aliasColunaJTextField.setEnabled(false);
                //aliasColunaJTextField.setText("");
                classeColunaJComboBox.setEnabled(false);
                //classeColunaJComboBox.setSelectedIndex(0);
                limparColunaJButton.setEnabled(false);
                incluirColunaJButton.setEnabled(false);
                colunasJTable.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);
                //descricaoColunaJTextArea.setText("");

                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);

                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
                //coletaJInternalFrame.hide();
                urlJTextField.setEnabled(false);
                urlJTextField.setText("https://sistemasnet/wiki/doku.php?id=artigos:" + (tipoLeiauteJComboBox.getSelectedItem().equals("Domínio") ? "dominio_" : "coleta_"));

            } else if (leiauteJTextField.getText().matches("^[a-zA-Z_][a-zA-Z0-9_]{0,31}$")) {//leiaute não existe

                tipoBancoJComboBox.setEnabled(true);
                tipoBancoJComboBox.setSelectedIndex(0);
                enderecoBancoJTextField.setText("");
                enderecoBancoJTextField.setEnabled(true);
                enderecoBancoJButton.setEnabled(true);
                tabelaJTextField.setText("");
                tabelaJTextField.setEnabled(false);
                tabelaOkJLabel.setText("");
                leiauteOkJLabel.setForeground(Color.black);
                leiauteOkJLabel.setText("Ok");

                urlJTextField.setEnabled(true);
                urlJTextField.setText("https://sistemasnet/wiki/doku.php?id=artigos:" + (tipoLeiauteJComboBox.getSelectedItem().equals("Domínio") ? "dominio_" : "coleta_") + leiauteJTextField.getText());

                /*acaoJComboBox.setEnabled(true);
                 tipoScriptJComboBox.setEnabled(true);
                 descricaoJTextArea.setEnabled(true);
                 periodicidadeJComboBox.setEnabled(true);

                

                 nomeColunaJTextField.setEnabled(true);
                 aliasColunaJTextField.setEnabled(true);
                 classeColunaJComboBox.setEnabled(true);
                 limparColunaJButton.setEnabled(true);
                 colunasJTable.setEnabled(true);
                 descricaoColunaJTextArea.setEnabled(true);

                 if (!nomeColunaJTextField.getText().isEmpty()) {
                 incluirColunaJButton.setEnabled(true);
                 } else {
                 incluirColunaJButton.setEnabled(false);
                 }

                 if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
                 tipoColunaJComboBox.setEnabled(true);
                 tamanhoColunaJSlider.setEnabled(true);
                 } else {
                 tipoColunaJComboBox.setEnabled(false);
                 tamanhoColunaJSlider.setEnabled(false);
                 }

                 if (colunasJTable.getModel().getRowCount() > 0) {
                 excluirColunaJButton.setEnabled(true);
                 validacaoesDisponiveisJList.setEnabled(true);
                 validacoesRealizadasJList.setEnabled(true);
                 addValidacaoJButton.setEnabled(true);
                 removeValidacaoJButton.setEnabled(true);
                 incluirColetaJButton.setEnabled(true);
                 } else {
                 excluirColunaJButton.setEnabled(false);
                 validacaoesDisponiveisJList.setEnabled(false);
                 validacoesRealizadasJList.setEnabled(false);
                 addValidacaoJButton.setEnabled(false);
                 removeValidacaoJButton.setEnabled(false);
                 incluirColetaJButton.setEnabled(false);
                 }*/
            } else {//nome do leiuate inválido
                leiauteOkJLabel.setForeground(Color.red);
                leiauteOkJLabel.setText("Nome do leiaute inválido");
                acaoJComboBox.setEnabled(false);
                acaoJComboBox.setSelectedIndex(0);
                tipoScriptJComboBox.setEnabled(false);
                descricaoJTextArea.setEnabled(false);
                descricaoJTextArea.setText("");
                periodicidadeJComboBox.setEnabled(false);

                tipoBancoJComboBox.setEnabled(false);
                tipoBancoJComboBox.setSelectedIndex(0);
                enderecoBancoJTextField.setText("");
                enderecoBancoJTextField.setEnabled(false);
                enderecoBancoJButton.setEnabled(false);
                tabelaJTextField.setText("");
                tabelaJTextField.setEnabled(false);
                tabelaOkJLabel.setText("");

                nomeColunaJTextField.setEnabled(false);
                //nomeColunaJTextField.setText("");
                aliasColunaJTextField.setEnabled(false);
                //aliasColunaJTextField.setText("");
                classeColunaJComboBox.setEnabled(false);
                //classeColunaJComboBox.setSelectedIndex(0);
                limparColunaJButton.setEnabled(false);
                incluirColunaJButton.setEnabled(false);
                colunasJTable.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);
                //descricaoColunaJTextArea.setText("");

                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);

                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
                //coletaJInternalFrame.hide();
                urlJTextField.setEnabled(false);
                urlJTextField.setText("https://sistemasnet/wiki/doku.php?id=artigos:" + (tipoLeiauteJComboBox.getSelectedItem().equals("Domínio") ? "dominio_" : "coleta_"));
            }
        }
    }//GEN-LAST:event_leiauteJTextFieldKeyReleased

    private void removeValidacaoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeValidacaoJButtonActionPerformed
        int[] validacoesRealizadasSelecionadas = validacoesRealizadasJList.getSelectedIndices();
        DefaultListModel modelValidacoesRealizadas = (DefaultListModel) validacoesRealizadasJList.getModel();
        for (int i = 0; i < validacoesRealizadasSelecionadas.length; i++) {
            switch (modelValidacoesRealizadas.getElementAt(validacoesRealizadasSelecionadas[i] - i).toString()) {
                case "Crescimento":
                    //TODO apagar crescimento
                    break;
                case "Densidade Geográfica":
                    //TODO apagar densidade
                    break;
                case "Média":
                    //TODO apagar média
                    break;
                case "Chave Única":
                    //TODO apagar chave única
                    break;
                case "Compara Dados":
                    //TODO apagar chave única
                    break;
                default:
                    break;
            }
            modelValidacoesRealizadas.removeElementAt(validacoesRealizadasSelecionadas[i] - i);
        }
    }//GEN-LAST:event_removeValidacaoJButtonActionPerformed

    private void addValidacaoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addValidacaoJButtonActionPerformed
        int[] validacoesDisponiveisSelecionadas = validacaoesDisponiveisJList.getSelectedIndices();
        MinhaListModel modelValidacoesDisponiveis = (MinhaListModel) validacaoesDisponiveisJList.getModel();
        DefaultListModel modelValidacoesRealizadas = (DefaultListModel) validacoesRealizadasJList.getModel();
        for (int i = 0; i < validacoesDisponiveisSelecionadas.length; i++) {
            boolean duplicado = false;
            for (int j = 0; j < modelValidacoesRealizadas.getSize(); j++) {
                if (modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString().equals(modelValidacoesRealizadas.getElementAt(j))) {
                    duplicado = true;
                }
            }
            if (!duplicado) {
                String validacao = modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString();
                DefaultComboBoxModel model = getColunasDado();
                DefaultTableModel modelColunas = (DefaultTableModel) colunasJTable.getModel();
                switch (validacao) {
                    case "Chave Única":
                        modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        colunasDisponiveisChaveUnicaJList.setModel(getColunasConsolidacao(null));
                        chaveUnicaJFrame.pack();
                        chaveUnicaJFrame.setVisible(true);
                        break;
                    case "Crescimento":
                        if (model.getSize() > 1) {
                            modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                            Object colunaDadoSelecionada = colunaDadoJComboBox.getSelectedItem();
                            colunaDadoJComboBox.setModel(model);
                            colunasDisponiveisConsolidacaoJList.setModel(getColunasConsolidacao(colunaDadoJComboBox));
                            if (colunaDadoSelecionada != null) {
                                try {
                                    ((DefaultComboBoxModel) colunasDisponiveisConsolidacaoJList.getModel()).setSelectedItem(colunaDadoSelecionada.toString());
                                } catch (Exception ex) {

                                }
                            }
                            crescimentoJFrame.pack();
                            crescimentoJFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Crescimento", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "Densidade Geográfica":
                        if (model.getSize() > 1) {
                            boolean existMunicipio = false;
                            for (int j = 0; j < modelColunas.getRowCount(); j++) {
                                if (modelColunas.getValueAt(j, 5).equals("Código IBGE Município")) {
                                    existMunicipio = true;
                                }
                            }
                            if (existMunicipio) {
                                modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                                densidadeJFrame.pack();
                                densidadeJFrame.setVisible(true);
                            } else {
                                JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com classe Código IBGE Município é necessária para a validação de Densidade Geográfica", "Erro", JOptionPane.ERROR_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Densidade Geográfica", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "Compara Dados":
                        modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        compararJFrame.pack();
                        compararJFrame.setVisible(true);
                        break;
                    case "Validar Código de Município":
                        boolean existMunicipio = false;
                        for (int j = 0; j < modelColunas.getRowCount(); j++) {
                            if (modelColunas.getValueAt(j, 5).equals("Código IBGE Município")) {
                                existMunicipio = true;
                            }
                        }
                        if (existMunicipio) {
                            modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com classe Código IBGE Município é necessária para a validação de Código Município", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "Média":
                        JOptionPane.showMessageDialog(incluirColetaJFrame, "Ainda não foi implementada a validação de média", "Erro", JOptionPane.ERROR_MESSAGE);
                        break;
                    case "Validar CNPJ":
                    case "Validar CPF":
                    case "Verificar Cadastro":
                        boolean existCNPJCPF = false;
                        for (int j = 0; j < modelColunas.getRowCount(); j++) {
                            if (modelColunas.getValueAt(j, 5).equals("CNPJ/CPF")) {
                                existCNPJCPF = true;
                            }
                        }
                        if (existCNPJCPF) {
                            modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com classe CNPJ/CPF é necessária para a validação de CNPF, CPF ou Cadastro", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "Validar Localidade SGMU":
                        boolean existLocalidadeSGMU = false;
                        for (int j = 0; j < modelColunas.getRowCount(); j++) {
                            if (modelColunas.getValueAt(j, 5).equals("Localidade SGMU")) {
                                existLocalidadeSGMU = true;
                            }
                        }
                        if (existLocalidadeSGMU) {
                            modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com classe Localidade SGMU é necessária para a validação de Localidade SGMU", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    case "Verificar Registros Duplicados":
                        if (model.getSize() > 1) {
                            modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                            Object colunaDadoDuplicadosSelecionada = colunaDadoDuplicadoJComboBox.getSelectedItem();
                            colunaDadoDuplicadoJComboBox.setModel(model);
                            colunasDisponiveisDuplicadoJList.setModel(getColunasConsolidacao(colunaDadoDuplicadoJComboBox));
                            if (colunaDadoDuplicadosSelecionada != null) {
                                try {
                                    ((DefaultComboBoxModel) colunasDisponiveisDuplicadoJList.getModel()).setSelectedItem(colunaDadoDuplicadosSelecionada.toString());
                                } catch (Exception ex) {

                                }
                            }
                            valoresDuplicadosJFrame.pack();
                            valoresDuplicadosJFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Valores Duplicados", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                        break;
                    default:
                        modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        break;
                }
            }
        }
    }//GEN-LAST:event_addValidacaoJButtonActionPerformed

    private void validacoesRealizadasJListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_validacoesRealizadasJListMouseClicked
        if (evt.getClickCount() == 2) {
            DefaultListModel modelValidacoesRealizadas = (DefaultListModel) validacoesRealizadasJList.getModel();
            String validacao = modelValidacoesRealizadas.getElementAt(validacoesRealizadasJList.getSelectedIndex()).toString();
            DefaultComboBoxModel model = getColunasDado();
            switch (validacao) {
                case "Chave Única":
                    chaveUnicaJFrame.pack();
                    chaveUnicaJFrame.setVisible(true);
                    break;
                case "Compara Dados":
                    compararJFrame.pack();
                    compararJFrame.setVisible(true);
                    break;
                case "Crescimento":
                    if (model.getSize() > 1) {
                        //modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        String colunaDadoSelecionada = colunaDadoJComboBox.getSelectedItem().toString();
                        colunaDadoJComboBox.setModel(model);
                        colunasDisponiveisConsolidacaoJList.setModel(getColunasConsolidacao(colunaDadoJComboBox));
                        if (colunaDadoSelecionada != null) {
                            try {
                                ((DefaultComboBoxModel) colunasDisponiveisConsolidacaoJList.getModel()).setSelectedItem(colunaDadoSelecionada);
                            } catch (Exception ex) {

                            }
                        }
                        crescimentoJFrame.pack();
                        crescimentoJFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Crescimento", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case "Densidade Geográfica":
                    DefaultTableModel modelColunas = (DefaultTableModel) colunasJTable.getModel();
                    if (model.getSize() > 1) {
                        boolean existMunicipio = false;
                        for (int j = 0; j < modelColunas.getRowCount(); j++) {
                            if (modelColunas.getValueAt(j, 5).equals("Código IBGE Município")) {
                                existMunicipio = true;
                            }
                        }
                        if (existMunicipio) {
                            densidadeJFrame.pack();
                            densidadeJFrame.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com classe Código IBGE Município é necessária para a validação de Densidade Geográfica", "Erro", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Densidade Geográfica", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                case "Média":
                    //TODO configurar média
                    break;
                case "Verificar Registros Duplicados":
                    if (model.getSize() > 1) {
                        //modelValidacoesRealizadas.addElement(modelValidacoesDisponiveis.getElementAt(validacoesDisponiveisSelecionadas[i]).toString());
                        String colunaDadoDuplicadoSelecionada = colunaDadoDuplicadoJComboBox.getSelectedItem().toString();
                        colunaDadoDuplicadoJComboBox.setModel(model);
                        colunasDisponiveisDuplicadoJList.setModel(getColunasConsolidacao(colunaDadoDuplicadoJComboBox));
                        if (colunaDadoDuplicadoSelecionada != null) {
                            try {
                                ((DefaultComboBoxModel) colunasDisponiveisDuplicadoJList.getModel()).setSelectedItem(colunaDadoDuplicadoSelecionada);
                            } catch (Exception ex) {

                            }
                        }
                        valoresDuplicadosJFrame.pack();
                        valoresDuplicadosJFrame.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(incluirColetaJFrame, "Uma coluna com tipo número é necessária para a validação de Valores Duplicados", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                    break;
                default:
                    break;
            }
        }
    }//GEN-LAST:event_validacoesRealizadasJListMouseClicked

    private void excluirColunaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excluirColunaJButtonActionPerformed
        int[] rows = colunasJTable.getSelectedRows();
        DefaultTableModel modelColunas = (DefaultTableModel) colunasJTable.getModel();
        DefaultListModel modelValidacoesDisponiveis = (DefaultListModel) validacoesRealizadasJList.getModel();
        for (int i = 0; i < rows.length; i++) {
            switch (modelColunas.getValueAt(rows[i] - i, 5).toString()) {
                case "CNPJ/CPF":
                    for (int j = 0; j < modelValidacoesDisponiveis.getSize(); j++) {
                        String validacao = modelValidacoesDisponiveis.getElementAt(j).toString();
                        if (validacao.equals("Validar CNPJ") || validacao.equals("Validar CPF") || validacao.equals("Verificar Cadastro")) {
                            modelValidacoesDisponiveis.removeElementAt(j);
                            j -= 1;
                        }
                    }
                    break;
                case "Código IBGE Município":
                    for (int j = 0; j < modelValidacoesDisponiveis.getSize(); j++) {
                        String validacao = modelValidacoesDisponiveis.getElementAt(j).toString();
                        if (validacao.equals("Validar Código de Município")) {
                            modelValidacoesDisponiveis.removeElementAt(j);
                            j = modelValidacoesDisponiveis.getSize();
                        }
                    }
                    break;
                case "CEP":
                    for (int j = 0; j < modelValidacoesDisponiveis.getSize(); j++) {
                        String validacao = modelValidacoesDisponiveis.getElementAt(j).toString();
                        if (validacao.equals("Validar CEP")) {
                            modelValidacoesDisponiveis.removeElementAt(j);
                            j = modelValidacoesDisponiveis.getSize();
                        }
                    }
                    break;
                case "Localidade SGMU":
                    for (int j = 0; j < modelValidacoesDisponiveis.getSize(); j++) {
                        String validacao = modelValidacoesDisponiveis.getElementAt(j).toString();
                        if (validacao.equals("Validar Localidade SGMU")) {
                            modelValidacoesDisponiveis.removeElementAt(j);
                            j = modelValidacoesDisponiveis.getSize();
                        }
                    }
                    break;
                case "Código Nacional":
                    //TODO
                    break;
                default:
                    break;
            }
            modelColunas.removeRow(rows[i] - i);
        }
        if (modelColunas.getRowCount() == 0) {
            excluirColunaJButton.setEnabled(false);
            validacaoesDisponiveisJList.setEnabled(false);
            validacoesRealizadasJList.setEnabled(false);
            addValidacaoJButton.setEnabled(false);
            removeValidacaoJButton.setEnabled(false);
            incluirColetaJButton.setEnabled(false);
            nomeContatoJTextField.setEnabled(false);
            emailContatoJTextField.setEnabled(false);
            telefoneContatoJTextField.setEnabled(false);
            contatosJTable.setEnabled(false);
            addContatoJButton.setEnabled(false);
            modelValidacoesDisponiveis.removeAllElements();
        } else {
            excluirColunaJButton.setEnabled(true);
            validacaoesDisponiveisJList.setEnabled(true);
            validacoesRealizadasJList.setEnabled(true);
            addValidacaoJButton.setEnabled(true);
            removeValidacaoJButton.setEnabled(true);
            nomeContatoJTextField.setEnabled(true);
            emailContatoJTextField.setEnabled(true);
            telefoneContatoJTextField.setEnabled(true);
            contatosJTable.setEnabled(true);
            addContatoJButton.setEnabled(true);
        }
    }//GEN-LAST:event_excluirColunaJButtonActionPerformed

    private void limparColunaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limparColunaJButtonActionPerformed
        nomeColunaJTextField.setText("");
        tipoColunaJComboBox.setSelectedItem("Texto");
        tamanhoColunaJSlider.setValue(14);
        aliasColunaJTextField.setText("");
        classeColunaJComboBox.setSelectedItem("CNPJ/CPF");
        descricaoColunaJTextArea.setText("");
        atualizacaoJComboBox.setSelectedItem("Sim");
        atualizacaoJComboBox.setEnabled(true);
    }//GEN-LAST:event_limparColunaJButtonActionPerformed

    private void incluirColunaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incluirColunaJButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) colunasJTable.getModel();

        if (nomeColunaJTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher campo Nome", "Erro", JOptionPane.ERROR_MESSAGE);
        } else if (!nomeColunaJTextField.getText().matches("^[a-zA-Z_]\\w{0,31}$")) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "O campo Nome não pode iniciar com números, não deve ter acento, nem espaços e deve ter no máximo 32 caracteres", "Erro", JOptionPane.ERROR_MESSAGE);
        } else if (aliasColunaJTextField.getText().equals("")) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher campo Alias", "Erro", JOptionPane.ERROR_MESSAGE);
        } else if (descricaoColunaJTextArea.getText().equals("")) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher campo Descrição", "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            boolean erro = false;
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(nomeColunaJTextField.getText())) {
                    erro = true;
                    JOptionPane.showMessageDialog(incluirColetaJFrame, "Não pode haver duas colunas com o mesmo nome", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
            if (!erro) {
                model.addRow(new Object[]{nomeColunaJTextField.getText(), aliasColunaJTextField.getText(), descricaoColunaJTextArea.getText(), tipoColunaJComboBox.getSelectedItem().toString(), tamanhoColunaJSlider.getValue(), classeColunaJComboBox.getSelectedItem().toString(), dominioJComboBox.getSelectedItem(), regexJTextField.getText(), atualizacaoJComboBox.getSelectedItem()});
                nomeColunaJTextField.setText("");
                tipoColunaJComboBox.setSelectedItem("Texto");
                tamanhoColunaJSlider.setValue(14);
                aliasColunaJTextField.setText("");
                classeColunaJComboBox.setSelectedItem("CNPJ/CPF");
                descricaoColunaJTextArea.setText("");
                dominioJComboBox.setSelectedItem("Não");
                colunaDominioJComboBox.setModel(new DefaultComboBoxModel());
                colunaDominioJComboBox.setEnabled(false);
                atualizacaoJComboBox.setSelectedItem("Sim");
                atualizacaoJComboBox.setEnabled(true);
            }
        }
        if (model.getRowCount() > 0) {
            excluirColunaJButton.setEnabled(true);
            validacaoesDisponiveisJList.setEnabled(true);
            validacoesRealizadasJList.setEnabled(true);
            addValidacaoJButton.setEnabled(true);
            removeValidacaoJButton.setEnabled(true);
            nomeContatoJTextField.setEnabled(true);
            emailContatoJTextField.setEnabled(true);
            telefoneContatoJTextField.setEnabled(true);
            contatosJTable.setEnabled(true);
            addContatoJButton.setEnabled(true);
        } else {
            excluirColunaJButton.setEnabled(false);
            validacaoesDisponiveisJList.setEnabled(false);
            validacoesRealizadasJList.setEnabled(false);
            addValidacaoJButton.setEnabled(false);
            removeValidacaoJButton.setEnabled(false);
            incluirColetaJButton.setEnabled(false);
            nomeContatoJTextField.setEnabled(false);
            emailContatoJTextField.setEnabled(false);
            telefoneContatoJTextField.setEnabled(false);
            contatosJTable.setEnabled(false);
            addContatoJButton.setEnabled(false);
        }
    }//GEN-LAST:event_incluirColunaJButtonActionPerformed

    private void classeColunaJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_classeColunaJComboBoxActionPerformed
        String classe = classeColunaJComboBox.getSelectedItem().toString();
        tipoColunaJComboBox.setEnabled(false);
        tamanhoColunaJSlider.setEnabled(false);
        switch (classe) {
            case "CNPJ/CPF":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Texto");
                regexJTextField.setText("/^[0-9]{14}$|^[0-9]{11}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(14);
                break;
            case "Grupo Econômico":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Inteiro");
                regexJTextField.setText("/(^[0-9]{1,4}$)|^\\-[1]$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(14);
                break;
            case "Código IBGE Município":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Não");
                tipoColunaJComboBox.setSelectedItem("Texto");
                regexJTextField.setText("/(^[0-9]{7}$)|^\\-[1]$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(7);
                break;
            case "CEP":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Não");
                tipoColunaJComboBox.setSelectedItem("Texto");
                regexJTextField.setText("/^[0-9]{8}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(8);
                break;
            case "Localidade SGMU":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Não");
                tipoColunaJComboBox.setSelectedItem("Texto");
                regexJTextField.setText("/^\\-[1]$|^[0-9]{1,8}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(6);
                break;
            case "Código Nacional":
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Não");
                tipoColunaJComboBox.setSelectedItem("Texto");
                regexJTextField.setText("/^[0-9]{2}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(2);
                break;
            case "Ano":
                atualizacaoJComboBox.setEnabled(false);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Inteiro");
                regexJTextField.setText("/^[0-9]{4}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(4);
                break;
            case "Mês":
                atualizacaoJComboBox.setEnabled(false);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Inteiro");
                regexJTextField.setText("/^[0-9]{1,2}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(2);
                break;
            case "Dia":
                atualizacaoJComboBox.setEnabled(false);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Inteiro");
                regexJTextField.setText("/^[0-9]{1,2}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(2);
                break;
            case "Período":
                atualizacaoJComboBox.setEnabled(false);
                atualizacaoJComboBox.setSelectedItem("Sim");
                tipoColunaJComboBox.setSelectedItem("Tempo (AAAA/MM/DD)");
                regexJTextField.setText("/^[0-9]{4}[\\/][0-9]{1,2}[\\/][0-9]{1,2}$/");
                editarRegexJRadioButton.setEnabled(false);
                tamanhoColunaJSlider.setValue(10);
                break;
            default:
                atualizacaoJComboBox.setEnabled(true);
                atualizacaoJComboBox.setSelectedItem("Não");
                tipoColunaJComboBox.setEnabled(true);
                tipoColunaJComboBox.setSelectedItem("Inteiro");
            //regexJTextField.setText(".*");
            //editarRegexJRadioButton.setEnabled(true);
            //tamanhoColunaJSlider.setEnabled(true);
        }
    }//GEN-LAST:event_classeColunaJComboBoxActionPerformed

    private void tamanhoColunaJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tamanhoColunaJSliderStateChanged
        tamanhoColunaJLabel.setText("Tamanho: " + tamanhoColunaJSlider.getValue());
        if (tipoColunaJComboBox.getSelectedItem().toString().equals("Inteiro") && classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
            regexJTextField.setText("/^[0-9]{1," + tamanhoColunaJSlider.getValue() + "}$/");
        } else if (tipoColunaJComboBox.getSelectedItem().toString().equals("Texto") && classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
            regexJTextField.setText("/^.{1," + tamanhoColunaJSlider.getValue() + "}$/");
        }
    }//GEN-LAST:event_tamanhoColunaJSliderStateChanged

    private void nomeColunaJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nomeColunaJTextFieldKeyReleased
        if (nomeColunaJTextField.getText().isEmpty()) {
            incluirColunaJButton.setEnabled(false);
        } else {
            incluirColunaJButton.setEnabled(true);
        }
    }//GEN-LAST:event_nomeColunaJTextFieldKeyReleased

    private void acaoJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_acaoJComboBoxActionPerformed
        String rodar = acaoJComboBox.getSelectedItem().toString();
        if (rodar.equals("Salvar Script")) {
            JFileChooser jFileChooser = new JFileChooser();
            jFileChooser.setDialogTitle("Endereço do Script");
            jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = jFileChooser.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                enderecoScriptJTextField.setText(jFileChooser.getCurrentDirectory().getAbsolutePath() + "\\script_" + leiauteJTextField.getText() + ".sas");
            } else {
                acaoJComboBox.setSelectedIndex(0);
            }
        } else {
            enderecoScriptJTextField.setText("");
        }
    }//GEN-LAST:event_acaoJComboBoxActionPerformed

    private void colunaDadoJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colunaDadoJComboBoxActionPerformed
        if (!colunaDadoJComboBox.getSelectedItem().toString().equals("Selecione...")) {
            colunasDisponiveisConsolidacaoJList.setModel(getColunasConsolidacao(colunaDadoJComboBox));
            colunasDisponiveisConsolidacaoJList.setEnabled(true);
            addConsolidacaoJButton.setEnabled(true);
            removeConsolidacaoJButton.setEnabled(true);
            colunasConsolidacaoJList.setEnabled(true);
            ((DefaultListModel) colunasConsolidacaoJList.getModel()).removeAllElements();
        } else {
            colunasDisponiveisConsolidacaoJList.removeAll();
            colunasDisponiveisConsolidacaoJList.setEnabled(false);
            addConsolidacaoJButton.setEnabled(false);
            removeConsolidacaoJButton.setEnabled(false);
            colunasConsolidacaoJList.setEnabled(false);
        }
    }//GEN-LAST:event_colunaDadoJComboBoxActionPerformed

    private void addConsolidacaoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addConsolidacaoJButtonActionPerformed
        int[] colunasDisponiveisSelecionadas = colunasDisponiveisConsolidacaoJList.getSelectedIndices();
        DefaultListModel modelColunasDisponiveis = (DefaultListModel) colunasDisponiveisConsolidacaoJList.getModel();
        DefaultListModel modelColunasConsolidacao = (DefaultListModel) colunasConsolidacaoJList.getModel();
        for (int i = 0; i < colunasDisponiveisSelecionadas.length; i++) {
            boolean duplicado = false;
            for (int j = 0; j < modelColunasConsolidacao.getSize(); j++) {
                if (modelColunasDisponiveis.getElementAt(colunasDisponiveisSelecionadas[i]).toString().equals(modelColunasConsolidacao.getElementAt(j))) {
                    duplicado = true;
                }
            }

            if (!duplicado) {
                String coluna = modelColunasDisponiveis.getElementAt(colunasDisponiveisSelecionadas[i]).toString();
                modelColunasConsolidacao.addElement(coluna);
            }
        }
        if (modelColunasConsolidacao.getSize() > 0) {
            inicioJFormattedTextField.setEnabled(true);
            fimJFormattedTextField.setEnabled(true);
            crescimentoMaximoJFormattedTextField.setEnabled(true);
            crescimentoMinimoJFormattedTextField.setEnabled(true);
        } else {
            inicioJFormattedTextField.setEnabled(false);
            fimJFormattedTextField.setEnabled(false);
            crescimentoMaximoJFormattedTextField.setEnabled(false);
            crescimentoMinimoJFormattedTextField.setEnabled(false);
        }
    }//GEN-LAST:event_addConsolidacaoJButtonActionPerformed

    private void removeConsolidacaoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeConsolidacaoJButtonActionPerformed
        int[] colunasConsolidacaoSelecionadas = colunasConsolidacaoJList.getSelectedIndices();
        DefaultListModel modelColunasConsolidacao = (DefaultListModel) colunasConsolidacaoJList.getModel();
        for (int i = 0; i < colunasConsolidacaoSelecionadas.length; i++) {
            modelColunasConsolidacao.removeElementAt(colunasConsolidacaoSelecionadas[i] - i);
        }
        if (modelColunasConsolidacao.getSize() == 0) {
            okCrescimentoJButton.setEnabled(false);
        } else {
            okCrescimentoJButton.setEnabled(true);
        }
    }//GEN-LAST:event_removeConsolidacaoJButtonActionPerformed

    private void cancelCrescimentoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelCrescimentoJButtonActionPerformed
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeElement("Crescimento");
        ((DefaultListModel) colunasConsolidacaoJList.getModel()).removeAllElements();
        DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }
        inicioJFormattedTextField.setText("");
        fimJFormattedTextField.setText("");
        crescimentoMaximoJFormattedTextField.setText("");
        crescimentoMinimoJFormattedTextField.setText("");
        colunaDadoJComboBox.setSelectedIndex(0);
        crescimentoJFrame.setVisible(false);
    }//GEN-LAST:event_cancelCrescimentoJButtonActionPerformed

    private void okCrescimentoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okCrescimentoJButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
        Double limiteFinal = Double.parseDouble(model.getValueAt(model.getRowCount() - 1, 1).toString().replaceAll(",", "."));
        if (limiteFinal < Double.POSITIVE_INFINITY) {
            int returnedValue = JOptionPane.showConfirmDialog(crescimentoJFrame, "Valores maiores que " + limiteFinal + " não serão analisados. Confirma?", "Aviso", JOptionPane.OK_CANCEL_OPTION);
            if (returnedValue == JOptionPane.OK_OPTION) {
                crescimentoJFrame.setVisible(false);
            }
        } else {
            crescimentoJFrame.setVisible(false);
        }
    }//GEN-LAST:event_okCrescimentoJButtonActionPerformed

    private void multiplicadorJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_multiplicadorJSliderStateChanged
        valorMultiplicadorJLabel.setText(multiplicadorJSlider.getValue() + "%");
    }//GEN-LAST:event_multiplicadorJSliderStateChanged

    private void cancelDensidadeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelDensidadeJButtonActionPerformed
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeElement("Densidade Geográfica");
        densidadeJFrame.setVisible(false);
    }//GEN-LAST:event_cancelDensidadeJButtonActionPerformed

    private void okDensidadeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okDensidadeJButtonActionPerformed
        densidadeJFrame.setVisible(false);
    }//GEN-LAST:event_okDensidadeJButtonActionPerformed

    private void inicioJFormattedTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_inicioJFormattedTextFieldKeyReleased
        if ((!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("")) && !crescimentoMaximoJFormattedTextField.getText().equals("") && !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            addLimiteJButton.setEnabled(true);
        } else {
            addLimiteJButton.setEnabled(false);
        }

        if (!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("") || !crescimentoMaximoJFormattedTextField.getText().equals("") || !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            limparLimiteJButton.setEnabled(true);
        } else {
            limparLimiteJButton.setEnabled(false);
        }
    }//GEN-LAST:event_inicioJFormattedTextFieldKeyReleased

    private void fimJFormattedTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fimJFormattedTextFieldKeyReleased
        if ((!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("")) && !crescimentoMaximoJFormattedTextField.getText().equals("") && !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            addLimiteJButton.setEnabled(true);
        } else {
            addLimiteJButton.setEnabled(false);
        }

        if (!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("") || !crescimentoMaximoJFormattedTextField.getText().equals("") || !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            limparLimiteJButton.setEnabled(true);
        } else {
            limparLimiteJButton.setEnabled(false);
        }
    }//GEN-LAST:event_fimJFormattedTextFieldKeyReleased

    private void crescimentoMaximoJFormattedTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_crescimentoMaximoJFormattedTextFieldKeyReleased
        if ((!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("")) && !crescimentoMaximoJFormattedTextField.getText().equals("") && !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            addLimiteJButton.setEnabled(true);
        } else {
            addLimiteJButton.setEnabled(false);
        }

        if (!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("") || !crescimentoMaximoJFormattedTextField.getText().equals("") || !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            limparLimiteJButton.setEnabled(true);
        } else {
            limparLimiteJButton.setEnabled(false);
        }
    }//GEN-LAST:event_crescimentoMaximoJFormattedTextFieldKeyReleased

    private void crescimentoMinimoJFormattedTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_crescimentoMinimoJFormattedTextFieldKeyReleased
        if ((!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("")) && !crescimentoMaximoJFormattedTextField.getText().equals("") && !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            addLimiteJButton.setEnabled(true);
        } else {
            addLimiteJButton.setEnabled(false);
        }

        if (!inicioJFormattedTextField.getText().equals("") || !fimJFormattedTextField.getText().equals("") || !crescimentoMaximoJFormattedTextField.getText().equals("") || !crescimentoMinimoJFormattedTextField.getText().equals("")) {
            limparLimiteJButton.setEnabled(true);
        } else {
            limparLimiteJButton.setEnabled(false);
        }
    }//GEN-LAST:event_crescimentoMinimoJFormattedTextFieldKeyReleased

    private void limparLimiteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limparLimiteJButtonActionPerformed
        inicioJFormattedTextField.setText("");
        fimJFormattedTextField.setText("");
        crescimentoMaximoJFormattedTextField.setText("");
        crescimentoMinimoJFormattedTextField.setText("");
        addLimiteJButton.setEnabled(false);
    }//GEN-LAST:event_limparLimiteJButtonActionPerformed

    private void fimJFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_fimJFormattedTextFieldFocusLost
        try {
            double inicio = inicioJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(inicioJFormattedTextField.getText().replaceAll(",", "."));
            double fim = fimJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(fimJFormattedTextField.getText().replaceAll(",", "."));
            if (fim < inicio) {
                JOptionPane.showMessageDialog(crescimentoJFrame, "Fim deve ser maior que inicio", "Erro", JOptionPane.ERROR_MESSAGE);
                fimJFormattedTextField.setText("");
                fimJFormattedTextField.requestFocus();
            }
        } catch (NumberFormatException ex) {
            fimJFormattedTextField.setText("");
            fimJFormattedTextField.requestFocus();
        }
    }//GEN-LAST:event_fimJFormattedTextFieldFocusLost

    private void inicioJFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_inicioJFormattedTextFieldFocusLost
        try {
            double inicio = inicioJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(inicioJFormattedTextField.getText().replaceAll(",", "."));
            double fim = fimJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(fimJFormattedTextField.getText().replaceAll(",", "."));
            if (fim < inicio) {
                JOptionPane.showMessageDialog(crescimentoJFrame, "Fim deve ser maior que inicio", "Erro", JOptionPane.ERROR_MESSAGE);
                inicioJFormattedTextField.setText("");
                inicioJFormattedTextField.requestFocus();
            }
        } catch (NumberFormatException ex) {
            inicioJFormattedTextField.setText("");
            inicioJFormattedTextField.requestFocus();
        }
    }//GEN-LAST:event_inicioJFormattedTextFieldFocusLost

    private void crescimentoMaximoJFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_crescimentoMaximoJFormattedTextFieldFocusLost
        try {
            double maximo = crescimentoMaximoJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(crescimentoMaximoJFormattedTextField.getText().replaceAll(",", "."));
            double minimo = crescimentoMinimoJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(crescimentoMinimoJFormattedTextField.getText().replaceAll(",", "."));
            if (maximo < minimo) {
                JOptionPane.showMessageDialog(crescimentoJFrame, "Crescimento Máximo deve ser maior que Crescimento Mínimo", "Erro", JOptionPane.ERROR_MESSAGE);
                crescimentoMaximoJFormattedTextField.setText("");
                crescimentoMaximoJFormattedTextField.requestFocus();
            }
        } catch (NumberFormatException ex) {
            crescimentoMaximoJFormattedTextField.setText("");
            crescimentoMaximoJFormattedTextField.requestFocus();
        }
    }//GEN-LAST:event_crescimentoMaximoJFormattedTextFieldFocusLost

    private void crescimentoMinimoJFormattedTextFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_crescimentoMinimoJFormattedTextFieldFocusLost
        try {
            double maximo = crescimentoMaximoJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(crescimentoMaximoJFormattedTextField.getText().replaceAll(",", "."));
            double minimo = crescimentoMinimoJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(crescimentoMinimoJFormattedTextField.getText().replaceAll(",", "."));
            if (maximo < minimo) {
                JOptionPane.showMessageDialog(crescimentoJFrame, "Crescimento Máximo deve ser maior que Crescimento Mínimo", "Erro", JOptionPane.ERROR_MESSAGE);
                crescimentoMinimoJFormattedTextField.setText("");
                crescimentoMinimoJFormattedTextField.requestFocus();
            }
        } catch (NumberFormatException ex) {
            crescimentoMinimoJFormattedTextField.setText("");
            crescimentoMinimoJFormattedTextField.requestFocus();
        }
    }//GEN-LAST:event_crescimentoMinimoJFormattedTextFieldFocusLost

    private void addLimiteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLimiteJButtonActionPerformed
        try {
            double maximo = crescimentoMaximoJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(crescimentoMaximoJFormattedTextField.getText().replaceAll(",", "."));
            double minimo = crescimentoMinimoJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(crescimentoMinimoJFormattedTextField.getText().replaceAll(",", "."));
            if (maximo >= minimo) {
                DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
                int rows = model.getRowCount();
                if (rows > 0) {
                    Double fimUltimoLimite = Double.parseDouble(model.getValueAt(rows - 1, 1).toString());
                    Double inicioNovoLimite = Double.parseDouble(inicioJFormattedTextField.getText().replaceAll(",", "."));
                    if (!Objects.equals(inicioNovoLimite, fimUltimoLimite)) {
                        JOptionPane.showMessageDialog(crescimentoJFrame, "O inicio do próximo limite deve ser igual ao fim do limite anterior", "Erro", JOptionPane.ERROR_MESSAGE);
                    } else {
                        model.addRow(new Object[]{inicioJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(inicioJFormattedTextField.getText().replaceAll(",", ".")), fimJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(fimJFormattedTextField.getText().replaceAll(",", ".")), Double.parseDouble(crescimentoMaximoJFormattedTextField.getText().replaceAll(",", ".")), Double.parseDouble(crescimentoMinimoJFormattedTextField.getText().replaceAll(",", "."))});
                        excluirLimiteJButton.setEnabled(true);
                    }
                } else {
                    model.addRow(new Object[]{inicioJFormattedTextField.getText().equals("") ? Double.NEGATIVE_INFINITY : Double.parseDouble(inicioJFormattedTextField.getText().replaceAll(",", ".")), fimJFormattedTextField.getText().equals("") ? Double.POSITIVE_INFINITY : Double.parseDouble(fimJFormattedTextField.getText().replaceAll(",", ".")), Double.parseDouble(crescimentoMaximoJFormattedTextField.getText().replaceAll(",", ".")), Double.parseDouble(crescimentoMinimoJFormattedTextField.getText().replaceAll(",", "."))});
                    excluirLimiteJButton.setEnabled(true);
                }
                if (fimJFormattedTextField.getText().equals("")) {
                    addLimiteJButton.setEnabled(false);
                }
                inicioJFormattedTextField.setText(fimJFormattedTextField.getText());
                fimJFormattedTextField.setText("");
                okCrescimentoJButton.setEnabled(true);
            }
        } catch (NumberFormatException ex) {
            crescimentoMinimoJFormattedTextField.setText("");
            crescimentoMinimoJFormattedTextField.requestFocus();
        }
    }//GEN-LAST:event_addLimiteJButtonActionPerformed

    private void excluirLimiteJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excluirLimiteJButtonActionPerformed
        DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
        if (model.getRowCount() > 0) {
            if (!model.getValueAt(model.getRowCount() - 1, 1).equals("Infinity")) {
                addLimiteJButton.setEnabled(true);
            }
            model.removeRow(model.getRowCount() - 1);
            if (model.getRowCount() == 0) {
                okCrescimentoJButton.setEnabled(false);
            }
        } else {
            okCrescimentoJButton.setEnabled(false);
        }
    }//GEN-LAST:event_excluirLimiteJButtonActionPerformed

    private void incluirColetaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incluirColetaJButtonActionPerformed
        StringBuilder coletaXML = new StringBuilder();
        coletaXML.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(System.getProperty("line.separator"));
        coletaXML.append("<coleta>").append(System.getProperty("line.separator"));
        coletaXML.append("	<script leiaute=\"").append(leiauteJTextField.getText()).append("\" rodar=\"").append(acaoJComboBox.getSelectedItem().toString().equals("Rodar Script") ? "SIM" : "NAO").append("\" tipo=\"").append(tipoScriptJComboBox.getSelectedItem().toString().toUpperCase()).append("\" endereco=\"").append(enderecoScriptJTextField.getText()).append("\"/>").append(System.getProperty("line.separator"));
        coletaXML.append("	<tipoPeriodo>").append(periodicidadeJComboBox.getSelectedItem().toString().toUpperCase()).append("</tipoPeriodo>").append(System.getProperty("line.separator"));
        coletaXML.append("	<prazo>").append(contadorDiasJSlider.getValue()).append("</prazo>").append(System.getProperty("line.separator"));
        coletaXML.append("	<tipoPrazo>").append(tipoPrazoJComboBox.getSelectedItem().toString().equals("Úteis") ? "UTEIS" : "CORRIDOS").append("</tipoPrazo>").append(System.getProperty("line.separator"));
        coletaXML.append("	<descricao>").append(descricaoJTextArea.getText()).append("</descricao>").append(System.getProperty("line.separator"));
        coletaXML.append("	<url>").append(urlJTextField.getText()).append("</url>").append(System.getProperty("line.separator"));
        coletaXML.append("	<colunas>").append(System.getProperty("line.separator"));

        DefaultTableModel colunasModel = (DefaultTableModel) colunasJTable.getModel();

        for (int i = 0; i < colunasModel.getRowCount(); i++) {
            coletaXML.append("		<coluna nome=\"").append(colunasModel.getValueAt(i, 0).toString()).append("\" alias=\"").append(colunasModel.getValueAt(i, 1).toString()).append("\" tipo=\"");
            String tipo = colunasModel.getValueAt(i, 3).toString();
            switch (tipo) {
                case "Texto":
                    coletaXML.append("CHAR\" tamanho=\"");
                    break;
                case "Tempo (AAAA/MM/DD)":
                    coletaXML.append("TIME\" tamanho=\"");
                    break;
                case "Número separado por ponto":
                    coletaXML.append("NUMERO_PONTO\" tamanho=\"");
                    break;
                case "Número separado por vírgula":
                    coletaXML.append("NUMERO_VIRGULA\" tamanho=\"");
                    break;
                case "Inteiro":
                    coletaXML.append("NUMERO\" tamanho=\"");
                    break;
            }
            coletaXML.append(colunasModel.getValueAt(i, 4).toString()).append("\" classe=\"");
            String classe = colunasModel.getValueAt(i, 5).toString();
            switch (classe) {
                case "CNPJ/CPF":
                    coletaXML.append("CNPJ_CPF\"");
                    break;
                case "Código IBGE Município":
                    coletaXML.append("MUNICIPIO\"");
                    break;
                case "CEP":
                    coletaXML.append("CEP\"");
                    break;
                case "Localidade SGMU":
                    coletaXML.append("LOCALIDADE\"");
                    break;
                case "Código Nacional":
                    coletaXML.append("CN\"");
                    break;
                case "Ano":
                    coletaXML.append("ANO\"");
                    break;
                case "Mês":
                    coletaXML.append("MES\"");
                    break;
                case "Dia":
                    coletaXML.append("DIA\"");
                    break;
                case "Período":
                    coletaXML.append("PERIODO\"");
                    break;
                case "Outros":
                    coletaXML.append("OUTROS\"");
                    break;
            }
            coletaXML.append(" regex=\"").append(colunasModel.getValueAt(i, 7).toString()).append("\" atualizacao=\"");
            String atualizacao = colunasModel.getValueAt(i, 8).toString();
            switch (atualizacao) {
                case "Sim":
                    coletaXML.append("SIM\"");
                    break;
                default:
                    coletaXML.append("NAO\"");
                    break;
            }
            coletaXML.append(">").append(System.getProperty("line.separator"));
            coletaXML.append("			<descricao>").append(colunasModel.getValueAt(i, 2)).append("</descricao>").append(System.getProperty("line.separator"));
            if (!colunasModel.getValueAt(i, 6).equals("Não")) {
                coletaXML.append("			<dominio leiaute=\"").append(colunasModel.getValueAt(i, 6)).append("\" coluna=\"").append(colunasModel.getValueAt(i, 0)).append("\"/>").append(System.getProperty("line.separator"));
            }

            coletaXML.append("		</coluna>").append(descricaoColunaJTextArea.getText()).append(System.getProperty("line.separator"));
        }
        coletaXML.append("	</colunas>").append(System.getProperty("line.separator"));

        coletaXML.append("	<contatos>").append(System.getProperty("line.separator"));
        DefaultTableModel modelContatos = (DefaultTableModel) contatosJTable.getModel();
        for (int i = 0; i < modelContatos.getRowCount(); i++) {
            coletaXML.append("		<contato nome=\"").append(modelContatos.getValueAt(i, 0)).append("\" email=\"").append(modelContatos.getValueAt(i, 1)).append("\" telefone=\"").append(modelContatos.getValueAt(i, 2)).append("\"/>").append(System.getProperty("line.separator"));
        }
        coletaXML.append("	</contatos>").append(System.getProperty("line.separator"));

        coletaXML.append("	<banco tipo=\"");
        String tipoBanco = tipoBancoJComboBox.getSelectedItem().toString();
        switch (tipoBanco) {
            case "Arquivo SAS":
                coletaXML.append("ARQUIVO_SAS");
                break;
            case "Biblioteca SAS":
                coletaXML.append("BIBLIOTECA_SAS");
                break;
            case "Tabela MS SQL Server":
                coletaXML.append("BANCO_DE_DADOS");
                break;
            default:
                //TODO implementar outros tipos
                coletaXML.append("ARQUIVO_SAS");
                break;
        }
        coletaXML.append("\" endereco=\"").append(enderecoBancoJTextField.getText()).append("\" tabela=\"").append(tabelaJTextField.getText()).append("\"");
        if (tipoBanco.equals("Biblioteca SAS")) {
            coletaXML.append(">").append(System.getProperty("line.separator"));
            String query = queryJTextArea.getText();
            query = query.replaceAll("&", "&#38;");
            query = query.replaceAll("'", "&#39;");
            query = query.replaceAll("\"", "&#34;");
            query = query.replaceAll("<", "&lt;");
            query = query.replaceAll(">", "&gt;");
            coletaXML.append("		<query>").append(query).append("</query>").append(System.getProperty("line.separator"));
            coletaXML.append("	</banco>").append(System.getProperty("line.separator"));
        } else {
            coletaXML.append("/>").append(System.getProperty("line.separator"));
        }

        if (!((DefaultListModel) validacoesRealizadasJList.getModel()).isEmpty()) {

            coletaXML.append("	<validacoes>").append(System.getProperty("line.separator"));

            DefaultListModel modelValidacoes = (DefaultListModel) validacoesRealizadasJList.getModel();
            for (int i = 0; i < modelValidacoes.getSize(); i++) {
                coletaXML.append("		<validacao nome=\"");
                String validacao = modelValidacoes.get(i).toString();
                switch (validacao) {
                    case "Chave Única":
                        coletaXML.append("validaChaveUnica\">").append(System.getProperty("line.separator"));
                        coletaXML.append("			<colunas>").append(System.getProperty("line.separator"));
                        DefaultListModel modelColunasChaveUnicaConsolidacao = (DefaultListModel) colunasConsolidacaoChaveUnicaJList.getModel();
                        for (int j = 0; j < modelColunasChaveUnicaConsolidacao.getSize(); j++) {
                            coletaXML.append("				<coluna nome=\"").append(modelColunasChaveUnicaConsolidacao.get(j)).append("\" uso=\"CONSOLIDACAO\" />").append(System.getProperty("line.separator"));
                        }
                        coletaXML.append("			</colunas>").append(System.getProperty("line.separator"));
                        coletaXML.append("		</validacao>").append(System.getProperty("line.separator"));
                        break;
                    case "Crescimento":
                        coletaXML.append("crescimento\">").append(System.getProperty("line.separator"));
                        coletaXML.append("			<colunas>").append(System.getProperty("line.separator"));
                        DefaultListModel modelColunasConsolidacao = (DefaultListModel) colunasConsolidacaoJList.getModel();
                        for (int j = 0; j < modelColunasConsolidacao.getSize(); j++) {
                            coletaXML.append("				<coluna nome=\"").append(modelColunasConsolidacao.get(j)).append("\" uso=\"CONSOLIDACAO\" />").append(System.getProperty("line.separator"));
                        }
                        coletaXML.append("				<coluna nome=\"").append(colunaDadoJComboBox.getSelectedItem().toString()).append("\" uso=\"DADO\" />").append(System.getProperty("line.separator"));
                        coletaXML.append("			</colunas>").append(System.getProperty("line.separator"));

                        coletaXML.append("			<limites>").append(System.getProperty("line.separator"));

                        DefaultTableModel modelLimites = ((DefaultTableModel) limitesJTable.getModel());

                        for (int j = 0; j < modelLimites.getRowCount(); j++) {
                            coletaXML.append("				<limite inicio=\"").append(modelLimites.getValueAt(j, 0).toString().equals("-Infinity") ? "" : modelLimites.getValueAt(j, 0))
                                    .append("\" fim=\"").append(modelLimites.getValueAt(j, 1).toString().equals("Infinity") ? "" : modelLimites.getValueAt(j, 1)).append("\" valor_maximo=\"").append(modelLimites.getValueAt(j, 2)).append("\" valor_minimo=\"").append(modelLimites.getValueAt(j, 3)).append("\"/>").append(System.getProperty("line.separator"));
                        }

                        coletaXML.append("			</limites>").append(System.getProperty("line.separator"));

                        coletaXML.append("		</validacao>").append(System.getProperty("line.separator"));
                        break;
                    case "Densidade Geográfica":
                        coletaXML.append("densidadeGeografica\">").append(System.getProperty("line.separator"));
                        coletaXML.append("			<denominador>").append(denominadorJComboBox.getSelectedItem().toString().equals("Domicílio") ? "DOMICILIO" : "POPULACAO").append("</denominador>").append(System.getProperty("line.separator"));
                        String multiplicador = "" + new DecimalFormat("###.##").format(multiplicadorJSlider.getValue() / 100d);
                        coletaXML.append("			<multiplicador>").append(multiplicador.replaceAll(",", ".")).append("</multiplicador>").append(System.getProperty("line.separator"));
                        coletaXML.append("		</validacao>").append(System.getProperty("line.separator"));
                        break;
                    case "Média":
                        //TODO criar média
                        break;
                    case "Compara Dados":
                        coletaXML.append("comparaDados\">").append(System.getProperty("line.separator"));
                        String query = comparaJTextArea.getText();
                        query = query.replaceAll("&", "&#38;");
                        query = query.replaceAll("'", "&#39;");
                        query = query.replaceAll("\"", "&#34;");
                        query = query.replaceAll("<", "&lt;");
                        query = query.replaceAll(">", "&gt;");
                        coletaXML.append("                  <query>").append(query).append("</query>").append(System.getProperty("line.separator"));
                        coletaXML.append("		</validacao>").append(System.getProperty("line.separator"));
                        break;
                    case "Verificar Registros Duplicados":
                        coletaXML.append("verificaDuplicados\">").append(System.getProperty("line.separator"));
                        coletaXML.append("			<colunas>").append(System.getProperty("line.separator"));
                        DefaultListModel modelColunasDuplicadoConsolidacao = (DefaultListModel) colunasConsolidacaoDuplicadoJList.getModel();
                        for (int j = 0; j < modelColunasDuplicadoConsolidacao.getSize(); j++) {
                            coletaXML.append("				<coluna nome=\"").append(modelColunasDuplicadoConsolidacao.get(j)).append("\" uso=\"CONSOLIDACAO\" />").append(System.getProperty("line.separator"));
                        }
                        coletaXML.append("				<coluna nome=\"").append(colunaDadoDuplicadoJComboBox.getSelectedItem().toString()).append("\" uso=\"DADO\" />").append(System.getProperty("line.separator"));
                        coletaXML.append("			</colunas>").append(System.getProperty("line.separator"));
                        coletaXML.append("		</validacao>").append(System.getProperty("line.separator"));
                        break;
                    case "Validar CEP":
                        coletaXML.append("validaCEP\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Validar CNPJ":
                        coletaXML.append("validaCNPJ\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Validar Código de Município":
                        coletaXML.append("validaCodigoMunicipio\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Validar CPF":
                        coletaXML.append("validaCPF\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Validar Localidade SGMU":
                        coletaXML.append("validaLocalidadeSGMU\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Verificar Cadastro":
                        coletaXML.append("verificaCadastro\"/>").append(System.getProperty("line.separator"));
                        break;
                    case "Verificar Valores em Branco":
                        coletaXML.append("verificaValoresEmBranco\"/>").append(System.getProperty("line.separator"));
                        break;
                }
            }

            coletaXML.append("	</validacoes>").append(System.getProperty("line.separator"));

        }

        coletaXML.append("</coleta>").append(System.getProperty("line.separator"));

        File coletaXMLFile = null;

        try {

            coletaXMLFile = new File(pastaXMLJTextField.getText() + (tipoLeiauteJComboBox.getSelectedItem().equals("Coleta") ? "coleta_" : "\\dominios\\dominio_") + leiauteJTextField.getText() + ".xml");
            Writer writer = new OutputStreamWriter(new FileOutputStream(coletaXMLFile), /*"Cp1252"*/ "UTF8");
            writer.write(coletaXML.toString());
            writer.flush();
            writer.close();

            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            Source xmlFile = new StreamSource(coletaXMLFile);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Erro ao tentar salvar XML", "Erro", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        boolean bancoCriado = !tipoBancoJComboBox.getSelectedItem().toString().equals("Biblioteca SAS") ? createBanco() : true;

        if (coletaXMLFile != null && coletaXMLFile.isFile()) {

            File pastaXML = new File(pastaXMLJTextField.getText() + (tipoColunaJComboBox.getSelectedItem().equals("Coleta") ? "" : "\\dominios\\"));
            File[] leiautes = pastaXML.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }
            });

            DefaultComboBoxModel modelLeiautes = new DefaultComboBoxModel();

            modelLeiautes.addElement("Selecione...");

            for (int i = 0; i < leiautes.length; i++) {
                try {
                    //URL schemaFile = new URL(coletaXSD);
                    File schemaFile = new File(coletaXSD);
                    Source xmlFile = new StreamSource(leiautes[i]);
                    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = schemaFactory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiautes[i]);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    modelLeiautes.addElement(scriptTag.getAttribute("leiaute"));

                } catch (Exception ex) {
                }
            }

            modelLeiautes.addElement("Novo Leiaute");
            leiauteJComboBox.setEnabled(true);
            leiauteJComboBox.setModel(modelLeiautes);

            leiauteJTextField.setText("");
            leiauteJTextField.setEnabled(false);
            leiauteJTextFieldKeyReleased(null);
            leiauteOkJLabel.setText("");

            descricaoJTextArea.setText("");
            while (((DefaultTableModel) colunasJTable.getModel()).getRowCount() != 0) {
                ((DefaultTableModel) colunasJTable.getModel()).removeRow(0);
            }

            ((DefaultListModel) validacoesRealizadasJList.getModel()).removeAllElements();

            ((DefaultListModel) colunasConsolidacaoJList.getModel()).removeAllElements();
            while (((DefaultTableModel) limitesJTable.getModel()).getRowCount() != 0) {
                ((DefaultTableModel) limitesJTable.getModel()).removeRow(0);
            }
            inicioJFormattedTextField.setText("");
            fimJFormattedTextField.setText("");
            crescimentoMaximoJFormattedTextField.setText("");
            crescimentoMinimoJFormattedTextField.setText("");

            ((DefaultListModel) colunasConsolidacaoDuplicadoJList.getModel()).removeAllElements();

            JOptionPane.showMessageDialog(incluirColetaJFrame, "Arquivo XML criado na pasta indicada", "Aviso", JOptionPane.INFORMATION_MESSAGE);

            if (!tipoBancoJComboBox.getSelectedItem().toString().equals("Biblioteca SAS")) {
                if (bancoCriado) {
                    JOptionPane.showMessageDialog(incluirColetaJFrame, "Banco criado na pasta indicada", "Aviso", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(incluirColetaJFrame, "Erro ao tentar criar banco", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Erro ao tentar salvar XML", "Erro", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_incluirColetaJButtonActionPerformed

    private void tipoBancoJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoBancoJComboBoxActionPerformed
        DefaultComboBoxModel model = ((DefaultComboBoxModel) tipoBancoJComboBox.getModel());
        switch (model.getSelectedItem().toString()) {
            case "Arquivo SAS":
                enderecoBancoJButton.setEnabled(true);
                queryJTextArea.setEnabled(false);
                testarQueryJToggleButton.setEnabled(false);
                tabelaJTextField.setEnabled(false);
                break;
            case "Biblioteca SAS":
                if (tipoLeiauteJComboBox.getSelectedItem().toString().equals("Domínio")) {
                    enderecoBancoJButton.setEnabled(false);
                    enderecoBancoJTextField.setEnabled(true);
                    tabelaJTextField.setText(leiauteJTextField.getText());
                    tabelaJTextField.setEnabled(true);
                    queryJTextArea.setEnabled(true);
                    testarQueryJToggleButton.setEnabled(true);
                } else {
                    JOptionPane.showMessageDialog(incluirColetaJFrame, "Opção ainda não implementada", "Erro", JOptionPane.ERROR_MESSAGE);
                    model.setSelectedItem("Arquivo SAS");
                }
                break;
            default:
                JOptionPane.showMessageDialog(incluirColetaJFrame, "Opção ainda não implementada", "Erro", JOptionPane.ERROR_MESSAGE);
                model.setSelectedItem("Arquivo SAS");
                break;
        }
    }//GEN-LAST:event_tipoBancoJComboBoxActionPerformed

    private void enderecoBancoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enderecoBancoJButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser(new File(pastaXMLJTextField.getText()));
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setDialogTitle("Pasta Banco");
        int returnVal = jFileChooser.showOpenDialog(incluirColetaJFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            enderecoBancoJTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath() + "\\");
            tabelaJTextField.setText(leiauteJTextField.getText());

            if (new File(enderecoBancoJTextField.getText() + tabelaJTextField.getText() + ".sas7bdat").isFile()) {//banco já existe
                tabelaOkJLabel.setForeground(Color.red);
                tabelaOkJLabel.setText("Banco já existe");
                acaoJComboBox.setEnabled(false);
                acaoJComboBox.setSelectedIndex(0);
                tipoScriptJComboBox.setEnabled(false);
                descricaoJTextArea.setEnabled(false);
                descricaoJTextArea.setText("");
                periodicidadeJComboBox.setEnabled(false);

                nomeColunaJTextField.setEnabled(false);
                //nomeColunaJTextField.setText("");
                aliasColunaJTextField.setEnabled(false);
                //aliasColunaJTextField.setText("");
                classeColunaJComboBox.setEnabled(false);
                //classeColunaJComboBox.setSelectedIndex(0);
                limparColunaJButton.setEnabled(false);
                incluirColunaJButton.setEnabled(false);
                colunasJTable.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);
                //descricaoColunaJTextArea.setText("");

                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);

                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
                dominioJComboBox.setEnabled(false);
                atualizacaoJComboBox.setEnabled(false);
                //coletaJInternalFrame.hide();
            } else {//banco não existe
                tabelaOkJLabel.setForeground(Color.black);
                tabelaOkJLabel.setText("Ok");
                acaoJComboBox.setEnabled(true);
                tipoScriptJComboBox.setEnabled(true);
                descricaoJTextArea.setEnabled(true);
                periodicidadeJComboBox.setEnabled(true);

                nomeColunaJTextField.setEnabled(true);
                aliasColunaJTextField.setEnabled(true);
                classeColunaJComboBox.setEnabled(true);
                limparColunaJButton.setEnabled(true);
                colunasJTable.setEnabled(true);
                descricaoColunaJTextArea.setEnabled(true);

                if (!nomeColunaJTextField.getText().isEmpty()) {
                    incluirColunaJButton.setEnabled(true);
                } else {
                    incluirColunaJButton.setEnabled(false);
                }

                if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
                    tipoColunaJComboBox.setEnabled(true);
                    tamanhoColunaJSlider.setEnabled(true);
                } else {
                    tipoColunaJComboBox.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(false);
                }

                if (colunasJTable.getModel().getRowCount() > 0) {
                    excluirColunaJButton.setEnabled(true);
                    validacaoesDisponiveisJList.setEnabled(true);
                    validacoesRealizadasJList.setEnabled(true);
                    addValidacaoJButton.setEnabled(true);
                    removeValidacaoJButton.setEnabled(true);
                    incluirColetaJButton.setEnabled(true);
                } else {
                    excluirColunaJButton.setEnabled(false);
                    validacaoesDisponiveisJList.setEnabled(false);
                    validacoesRealizadasJList.setEnabled(false);
                    addValidacaoJButton.setEnabled(false);
                    removeValidacaoJButton.setEnabled(false);
                    incluirColetaJButton.setEnabled(false);
                }
                if (tipoLeiauteJComboBox.getSelectedItem().toString().equals("Coleta")) {
                    dominioJComboBox.setEnabled(true);
                } else {
                    dominioJComboBox.setEnabled(false);
                }
            }

            tabelaJTextField.setEnabled(true);
        }
    }//GEN-LAST:event_enderecoBancoJButtonActionPerformed

    private void tabelaJTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tabelaJTextFieldKeyReleased
        if (tabelaJTextField.getText().isEmpty()) {//sem nome de tabela

            acaoJComboBox.setEnabled(false);
            acaoJComboBox.setSelectedIndex(0);
            tipoScriptJComboBox.setEnabled(false);
            descricaoJTextArea.setEnabled(false);
            //descricaoJTextArea.setText("");
            periodicidadeJComboBox.setEnabled(false);
            leiauteOkJLabel.setText("");

            nomeColunaJTextField.setEnabled(false);
            //nomeColunaJTextField.setText("");
            aliasColunaJTextField.setEnabled(false);
            //aliasColunaJTextField.setText("");
            classeColunaJComboBox.setEnabled(false);
            //classeColunaJComboBox.setSelectedIndex(0);
            limparColunaJButton.setEnabled(false);
            incluirColunaJButton.setEnabled(false);
            colunasJTable.setEnabled(false);
            descricaoColunaJTextArea.setEnabled(false);
            //descricaoColunaJTextArea.setText("");

            tipoColunaJComboBox.setEnabled(false);
            tamanhoColunaJSlider.setEnabled(false);

            excluirColunaJButton.setEnabled(false);
            validacaoesDisponiveisJList.setEnabled(false);
            validacoesRealizadasJList.setEnabled(false);
            addValidacaoJButton.setEnabled(false);
            removeValidacaoJButton.setEnabled(false);
            incluirColetaJButton.setEnabled(false);

            //coletaJInternalFrame.hide();
        } else if (!tipoBancoJComboBox.getSelectedItem().toString().equals("Biblioteca SAS")) {//tem nome de tabela
            if (new File(enderecoBancoJTextField.getText() + tabelaJTextField.getText() + ".sas7bdat").isFile()) {//banco já existe
                tabelaOkJLabel.setForeground(Color.red);
                tabelaOkJLabel.setText("Banco já existe");
                acaoJComboBox.setEnabled(false);
                acaoJComboBox.setSelectedIndex(0);
                tipoScriptJComboBox.setEnabled(false);
                descricaoJTextArea.setEnabled(false);
                descricaoJTextArea.setText("");
                periodicidadeJComboBox.setEnabled(false);

                nomeColunaJTextField.setEnabled(false);
                //nomeColunaJTextField.setText("");
                aliasColunaJTextField.setEnabled(false);
                //aliasColunaJTextField.setText("");
                classeColunaJComboBox.setEnabled(false);
                //classeColunaJComboBox.setSelectedIndex(0);
                limparColunaJButton.setEnabled(false);
                incluirColunaJButton.setEnabled(false);
                colunasJTable.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);
                //descricaoColunaJTextArea.setText("");

                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);

                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
                //coletaJInternalFrame.hide();

            } else if (tabelaJTextField.getText().matches("^[a-zA-Z_][a-zA-Z0-9_]{0,31}$")) {//Banco não existe
                tabelaOkJLabel.setForeground(Color.black);
                tabelaOkJLabel.setText("Ok");
                acaoJComboBox.setEnabled(true);
                tipoScriptJComboBox.setEnabled(true);
                descricaoJTextArea.setEnabled(true);
                periodicidadeJComboBox.setEnabled(true);

                nomeColunaJTextField.setEnabled(true);
                aliasColunaJTextField.setEnabled(true);
                classeColunaJComboBox.setEnabled(true);
                limparColunaJButton.setEnabled(true);
                colunasJTable.setEnabled(true);
                descricaoColunaJTextArea.setEnabled(true);

                if (!nomeColunaJTextField.getText().isEmpty()) {
                    incluirColunaJButton.setEnabled(true);
                } else {
                    incluirColunaJButton.setEnabled(false);
                }

                if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
                    tipoColunaJComboBox.setEnabled(true);
                    tamanhoColunaJSlider.setEnabled(true);
                } else {
                    tipoColunaJComboBox.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(false);
                }

                if (colunasJTable.getModel().getRowCount() > 0) {
                    excluirColunaJButton.setEnabled(true);
                    validacaoesDisponiveisJList.setEnabled(true);
                    validacoesRealizadasJList.setEnabled(true);
                    addValidacaoJButton.setEnabled(true);
                    removeValidacaoJButton.setEnabled(true);
                    incluirColetaJButton.setEnabled(true);
                } else {
                    excluirColunaJButton.setEnabled(false);
                    validacaoesDisponiveisJList.setEnabled(false);
                    validacoesRealizadasJList.setEnabled(false);
                    addValidacaoJButton.setEnabled(false);
                    removeValidacaoJButton.setEnabled(false);
                    incluirColetaJButton.setEnabled(false);
                }
            } else {//nome inválido
                tabelaOkJLabel.setForeground(Color.red);
                tabelaOkJLabel.setText("Nome de tabela inválido");
                acaoJComboBox.setEnabled(false);
                acaoJComboBox.setSelectedIndex(0);
                tipoScriptJComboBox.setEnabled(false);
                descricaoJTextArea.setEnabled(false);
                descricaoJTextArea.setText("");
                periodicidadeJComboBox.setEnabled(false);

                nomeColunaJTextField.setEnabled(false);
                //nomeColunaJTextField.setText("");
                aliasColunaJTextField.setEnabled(false);
                //aliasColunaJTextField.setText("");
                classeColunaJComboBox.setEnabled(false);
                //classeColunaJComboBox.setSelectedIndex(0);
                limparColunaJButton.setEnabled(false);
                incluirColunaJButton.setEnabled(false);
                colunasJTable.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);
                //descricaoColunaJTextArea.setText("");

                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);

                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
                //coletaJInternalFrame.hide();
            }
            //coletaJInternalFrame.show();
        }
    }//GEN-LAST:event_tabelaJTextFieldKeyReleased

    private void colunaDadoDuplicadoJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colunaDadoDuplicadoJComboBoxActionPerformed
        if (!colunaDadoDuplicadoJComboBox.getSelectedItem().toString().equals("Selecione...")) {
            colunasDisponiveisDuplicadoJList.setModel(getColunasConsolidacao(colunaDadoDuplicadoJComboBox));
            colunasDisponiveisDuplicadoJList.setEnabled(true);
            addColunaDuplicadoJButton.setEnabled(true);
            removeColunaDuplicadoJButton.setEnabled(true);
            colunasConsolidacaoDuplicadoJList.setEnabled(true);
            ((DefaultListModel) colunasConsolidacaoDuplicadoJList.getModel()).removeAllElements();
        } else {
            colunasDisponiveisDuplicadoJList.removeAll();
            colunasDisponiveisDuplicadoJList.setEnabled(false);
            addColunaDuplicadoJButton.setEnabled(false);
            removeColunaDuplicadoJButton.setEnabled(false);
            colunasConsolidacaoDuplicadoJList.setEnabled(false);
        }
    }//GEN-LAST:event_colunaDadoDuplicadoJComboBoxActionPerformed

    private void addColunaDuplicadoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addColunaDuplicadoJButtonActionPerformed
        int[] colunasDisponiveisDuplicadosSelecionadas = colunasDisponiveisDuplicadoJList.getSelectedIndices();
        DefaultListModel modelColunasDuplicadosDisponiveis = (DefaultListModel) colunasDisponiveisDuplicadoJList.getModel();
        DefaultListModel modelColunasDuplicadosConsolidacao = (DefaultListModel) colunasConsolidacaoDuplicadoJList.getModel();
        for (int i = 0; i < colunasDisponiveisDuplicadosSelecionadas.length; i++) {
            boolean duplicado = false;
            for (int j = 0; j < modelColunasDuplicadosConsolidacao.getSize(); j++) {
                if (modelColunasDuplicadosDisponiveis.getElementAt(colunasDisponiveisDuplicadosSelecionadas[i]).toString().equals(modelColunasDuplicadosConsolidacao.getElementAt(j))) {
                    duplicado = true;
                }
            }

            if (!duplicado) {
                String coluna = modelColunasDuplicadosDisponiveis.getElementAt(colunasDisponiveisDuplicadosSelecionadas[i]).toString();
                modelColunasDuplicadosConsolidacao.addElement(coluna);
            }
        }

        if (!modelColunasDuplicadosConsolidacao.isEmpty()) {
            okColunasDuplicadoJButton.setEnabled(true);
        } else {
            okColunasDuplicadoJButton.setEnabled(false);
        }

    }//GEN-LAST:event_addColunaDuplicadoJButtonActionPerformed

    private void removeColunaDuplicadoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeColunaDuplicadoJButtonActionPerformed
        int[] colunasConsolidacaoDuplicadosSelecionadas = colunasConsolidacaoDuplicadoJList.getSelectedIndices();
        DefaultListModel modelColunasDuplicadosConsolidacao = (DefaultListModel) colunasConsolidacaoDuplicadoJList.getModel();
        for (int i = 0; i < colunasConsolidacaoDuplicadosSelecionadas.length; i++) {
            modelColunasDuplicadosConsolidacao.removeElementAt(colunasConsolidacaoDuplicadosSelecionadas[i] - i);
        }
        if (modelColunasDuplicadosConsolidacao.getSize() == 0) {
            okColunasDuplicadoJButton.setEnabled(false);
        } else {
            okColunasDuplicadoJButton.setEnabled(true);
        }
    }//GEN-LAST:event_removeColunaDuplicadoJButtonActionPerformed

    private void cancelarColunasDuplicadoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarColunasDuplicadoJButtonActionPerformed
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeElement("Verificar Registros Duplicados");
        ((DefaultListModel) colunasConsolidacaoJList.getModel()).removeAllElements();
        DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }
        colunaDadoDuplicadoJComboBox.setSelectedIndex(0);
        valoresDuplicadosJFrame.setVisible(false);
    }//GEN-LAST:event_cancelarColunasDuplicadoJButtonActionPerformed

    private void okColunasDuplicadoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okColunasDuplicadoJButtonActionPerformed
        valoresDuplicadosJFrame.setVisible(false);
    }//GEN-LAST:event_okColunasDuplicadoJButtonActionPerformed

    private void tipoScriptJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoScriptJComboBoxActionPerformed
        String tipoScript = tipoScriptJComboBox.getSelectedItem().toString();
        switch (tipoScript) {
            case "SAS":
                break;
            default:
                JOptionPane.showMessageDialog(incluirColetaJFrame, "Apenas script SAS foi implementado", "Erro", JOptionPane.ERROR_MESSAGE);
                tipoScriptJComboBox.setSelectedItem("SAS");
                break;
        }
    }//GEN-LAST:event_tipoScriptJComboBoxActionPerformed

    private void adicionarContatoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adicionarContatoJButtonActionPerformed
        try {
            InternetAddress internetAddress = new InternetAddress(contatoNovoJTextField.getText());
            internetAddress.validate();

            DefaultListModel model = (DefaultListModel) contatosJList.getModel();
            model.addElement(contatoNovoJTextField.getText());
            contatoNovoJTextField.setText("");

        } catch (AddressException ex) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Email informado não é válido", "Erro", JOptionPane.ERROR_MESSAGE);
        }

    }//GEN-LAST:event_adicionarContatoJButtonActionPerformed

    private void excluirContatoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excluirContatoJButtonActionPerformed
        DefaultListModel model = (DefaultListModel) contatosJList.getModel();
        model.remove(contatosJList.getSelectedIndex());
    }//GEN-LAST:event_excluirContatoJButtonActionPerformed

    private void iniciarJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iniciarJButtonActionPerformed

        DefaultListModel model = (DefaultListModel) contatosJList.getModel();

        StringBuilder contatosProps = new StringBuilder();
        for (int i = 0; i < model.getSize(); i++) {
            contatosProps.append(model.getElementAt(i).toString()).append(";");
        }

        salvarProps();

        EmailInterface emailInterface = new EmailInterface(new String(senhaJPasswordField.getPassword()));
        emailInterface.addSASActionListener(this);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        timer = new Timer();

        timer.scheduleAtFixedRate(emailInterface, 0, Integer.parseInt(atualizacaoJFormattedTextField.getText()) * 1000);

        statusJLabel.setText("Cassini está rodando...");
        statusJLabel.setForeground(Color.black);


    }//GEN-LAST:event_iniciarJButtonActionPerformed

    private void salvarProps() {
        DefaultListModel model = (DefaultListModel) contatosJList.getModel();

        StringBuilder contatosProps = new StringBuilder();
        for (int i = 0; i < model.getSize(); i++) {
            contatosProps.append(model.getElementAt(i).toString()).append(";");
        }

        props.put("cassini.smtp.host", servidorSMTPJTextField.getText());
        props.put("cassini.smtp.port", portaSMTPJFormattedTextField.getText());
        props.put("cassini.imap.host", servidorIMAPJTextField.getText());
        props.put("cassini.imap.port", portaIMAPJFormattedTextField.getText());
        props.put("cassini.usuario.email", usuarioJTextField.getText());
        if (salvarSenhaJRadioButton.isSelected()) {
            props.put("cassini.usuario.pass", new String(senhaJPasswordField.getPassword()));
            props.put("cassini.usuario.pass.salvar", "true");
        } else {
            props.put("cassini.usuario.pass", "");
            props.put("cassini.usuario.pass.salvar", "false");
        }
        props.put("cassini.contacts", contatosProps.toString().substring(0, contatosProps.length() - 1));
        props.put("cassini.timeout", atualizacaoJFormattedTextField.getText());
        props.put("cassini.auto", autoIniciarJRadioButton.isSelected() ? "true" : "false");

        props.put("cassini.servidor", servidorJComboBox.getSelectedItem());
        props.put("cassini.exchange.host", servidorMSExchangeJTextField.getText());
        props.put("cassini.exchange.versao", msExchangeVersaoJComboBox.getSelectedItem());
        props.put("cassini.recebidos.folder", pastaRecebidosJTextField.getText());
        props.put("cassini.validados.folder", pastaValidadosJTextField.getText());
        props.put("cassini.emails.apagar", apagarEmailsJComboBox.getSelectedItem());
        props.put("cassini.sas.host", sasServerJTextField.getText());
        props.put("cassini.sas.port", sasPortJFormattedTextField.getText());

        try {
            FileOutputStream fos = new FileOutputStream("./properties/.properties");
            props.store(fos, "Propriedades do Cassini");
            fos.flush();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void pararJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pararJButtonActionPerformed
        if (timer != null) {
            timer.cancel();
        }
        statusJLabel.setText("Cassini está parado");
        statusJLabel.setForeground(Color.red);

        salvarProps();
    }//GEN-LAST:event_pararJButtonActionPerformed

    private void salvarSenhaJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_salvarSenhaJRadioButtonActionPerformed
        if (salvarSenhaJRadioButton.isSelected()) {
            JOptionPane.showMessageDialog(this, "Sua senha será salva em claro", "Aviso", JOptionPane.WARNING_MESSAGE);
            autoIniciarJRadioButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Se a senha não for salva o Cassini não poderá rodar automaticamente.", "Aviso", JOptionPane.WARNING_MESSAGE);
            autoIniciarJRadioButton.setEnabled(false);
            autoIniciarJRadioButton.setSelected(false);
        }
    }//GEN-LAST:event_salvarSenhaJRadioButtonActionPerformed

    private void tipoLeiauteJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoLeiauteJComboBoxActionPerformed
        String tipoLeiaute = tipoLeiauteJComboBox.getSelectedItem().toString();
        File pastaXML = null;
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        switch (tipoLeiaute) {
            case "Selecione...":
                break;
            case "Coleta":
                dominioJComboBox.setModel(getDominios());
                pastaXML = new File(pastaXMLJTextField.getText());
                break;
            case "Domínio":

                /*model = new DefaultComboBoxModel();
                 model.addElement("Não");
                 dominioJComboBox.setModel(model);*/
                dominioJComboBox.setModel(getDominios());
                pastaXML = new File(pastaXMLJTextField.getText() + "\\dominios\\");
                pastaXML.mkdir();
                break;
        }

        if (pastaXML != null) {
            File[] leiautes = pastaXML.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }
            });

            DefaultComboBoxModel modelLeiautes = new DefaultComboBoxModel();

            modelLeiautes.addElement("Selecione...");

            for (int i = 0; i < leiautes.length; i++) {
                try {
                    //URL schemaFile = new URL(coletaXSD);
                    File schemaFile = new File(coletaXSD);
                    Source xmlFile = new StreamSource(leiautes[i]);
                    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = schemaFactory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiautes[i]);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    modelLeiautes.addElement(scriptTag.getAttribute("leiaute"));

                } catch (Exception ex) {
                }
            }

            modelLeiautes.addElement("Novo Leiaute");
            leiauteJComboBox.setEnabled(true);
            leiauteJComboBox.setModel(modelLeiautes);
        } else {
            leiauteJComboBox.setEnabled(false);
            leiauteJComboBox.setModel(new DefaultComboBoxModel());
        }

    }//GEN-LAST:event_tipoLeiauteJComboBoxActionPerformed

    private void dominioJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dominioJComboBoxActionPerformed
        if (!dominioJComboBox.getSelectedItem().equals("Não")) {
            File dominioXML = new File(pastaXMLJTextField.getText() + "\\dominios\\dominio_" + dominioJComboBox.getSelectedItem().toString() + ".xml");
            try {
                //URL schemaFile = new URL(coletaXSD);
                File schemaFile = new File(coletaXSD);
                Source xmlFile = new StreamSource(dominioXML);
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(schemaFile);
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(false);
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                Document doc = docBuilder.parse(dominioXML);
                Element coletaTag = doc.getDocumentElement();
                NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");
                DefaultComboBoxModel model = new DefaultComboBoxModel();
                for (int i = 0; i < colunasTag.getLength(); i++) {
                    Element colunaTag = (Element) colunasTag.item(i);
                    model.addElement(colunaTag.getAttribute("nome"));
                }
                nomeColunaJTextField.setEnabled(false);
                aliasColunaJTextField.setEnabled(false);
                classeColunaJComboBox.setEnabled(false);
                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);
                descricaoColunaJTextArea.setEnabled(false);

                colunaDominioJComboBox.setModel(model);
                colunaDominioJComboBox.setEnabled(true);
                colunaDominioJComboBoxActionPerformed(null);
                incluirColunaJButton.setEnabled(true);
            } catch (Exception ex) {

            }
        } else {
            nomeColunaJTextField.setEnabled(true);
            aliasColunaJTextField.setEnabled(true);
            classeColunaJComboBox.setEnabled(true);
            descricaoColunaJTextArea.setEnabled(true);
            colunaDominioJComboBox.setEnabled(false);
            colunaDominioJComboBox.setModel(new DefaultComboBoxModel());
            classeColunaJComboBoxActionPerformed(null);
        }
    }//GEN-LAST:event_dominioJComboBoxActionPerformed

    private void colunaDominioJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colunaDominioJComboBoxActionPerformed
        File dominioXML = new File(pastaXMLJTextField.getText() + "\\dominios\\dominio_" + dominioJComboBox.getSelectedItem().toString() + ".xml");
        try {
            //URL schemaFile = new URL(coletaXSD);
            File schemaFile = new File(coletaXSD);
            Source xmlFile = new StreamSource(dominioXML);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlFile);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(false);
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            Document doc = docBuilder.parse(dominioXML);
            Element coletaTag = doc.getDocumentElement();
            NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                if (colunaTag.getAttribute("nome").equals(colunaDominioJComboBox.getSelectedItem().toString())) {
                    nomeColunaJTextField.setText(colunaTag.getAttribute("nome"));
                    aliasColunaJTextField.setText(colunaTag.getAttribute("alias"));
                    String classe = colunaTag.getAttribute("classe");
                    switch (classe) {
                        case "CNPJ_CPF":
                            classeColunaJComboBox.setSelectedItem("CNPJ/CPF");
                            break;
                        case "MUNICIPIO":
                            classeColunaJComboBox.setSelectedItem("Código IBGE Município");
                            break;
                        case "CEP":
                            classeColunaJComboBox.setSelectedItem("CEP");
                            break;
                        case "LOCALIDADE":
                            classeColunaJComboBox.setSelectedItem("Localidade SGMU");
                            break;
                        case "CN":
                            classeColunaJComboBox.setSelectedItem("Código Nacional");
                            break;
                        case "ANO":
                            classeColunaJComboBox.setSelectedItem("Ano");
                            break;
                        case "MES":
                            classeColunaJComboBox.setSelectedItem("Mês");
                            break;
                        case "DIA":
                            classeColunaJComboBox.setSelectedItem("Dia");
                            break;
                        case "PERIODO":
                            classeColunaJComboBox.setSelectedItem("Período");
                            break;
                        case "OUTROS":
                            classeColunaJComboBox.setSelectedItem("Outros");
                            break;
                    }
                    String tipo = colunaTag.getAttribute("tipo");
                    switch (tipo) {
                        case "CHAR":
                            tipoColunaJComboBox.setSelectedItem("Texto");
                            break;
                        case "TIME":
                            tipoColunaJComboBox.setSelectedItem("Tempo (AAAA/MM/DD)");
                            break;
                        case "NUMERO_PONTO":
                            tipoColunaJComboBox.setSelectedItem("Número separado por ponto");
                            break;
                        case "NUMERO_VIRGULA":
                            tipoColunaJComboBox.setSelectedItem("Número separado por vírgula");
                            break;
                        case "NUMERO":
                            tipoColunaJComboBox.setSelectedItem("Inteiro");
                            break;
                    }
                    String atualizacao = colunaTag.getAttribute("atualizacao");
                    switch (atualizacao) {
                        case "SIM":
                            atualizacaoJComboBox.setSelectedItem("Sim");
                            break;
                        default:
                            atualizacaoJComboBox.setSelectedItem("Não");
                            break;
                    }
                    atualizacaoJComboBox.setEnabled(true);
                    nomeColunaJTextField.setEnabled(false);
                    aliasColunaJTextField.setEnabled(false);
                    classeColunaJComboBox.setEnabled(false);
                    tipoColunaJComboBox.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(false);
                    descricaoColunaJTextArea.setEnabled(false);
                    regexJTextField.setEnabled(false);
                    editarRegexJRadioButton.setEnabled(false);
                    editarRegexJRadioButton.setSelected(false);
                    tamanhoColunaJSlider.setValue(Integer.parseInt(colunaTag.getAttribute("tamanho")));
                    descricaoColunaJTextArea.setText(colunaTag.getAttribute("alias"));
                    regexJTextField.setText(colunaTag.getAttribute("regex"));
                    i = colunasTag.getLength();
                }
            }
        } catch (Exception ex) {

        }
    }//GEN-LAST:event_colunaDominioJComboBoxActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        pararJButtonActionPerformed(null);
    }//GEN-LAST:event_formWindowClosed

    private void servidorJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_servidorJComboBoxActionPerformed
        String servidor = servidorJComboBox.getSelectedItem().toString();
        switch (servidor) {
            case "Selecione...":
                msExchangeVersaoJComboBox.setEnabled(false);
                msExchangeVersaoJComboBox.setSelectedIndex(0);
                servidorSMTPJTextField.setEnabled(false);
                portaSMTPJFormattedTextField.setEnabled(false);
                servidorIMAPJTextField.setEnabled(false);
                portaIMAPJFormattedTextField.setEnabled(false);
                servidorMSExchangeJTextField.setEnabled(false);
                break;
            case "MS Exchange":
                msExchangeVersaoJComboBox.setEnabled(true);
                msExchangeVersaoJComboBox.setSelectedIndex(0);
                servidorSMTPJTextField.setEnabled(false);
                portaSMTPJFormattedTextField.setEnabled(false);
                servidorIMAPJTextField.setEnabled(false);
                portaIMAPJFormattedTextField.setEnabled(false);
                servidorMSExchangeJTextField.setEnabled(true);
                break;
            case "SMTP/IMAP":
                JOptionPane.showMessageDialog(this, "Essa opção ainda não está disponível", "Erro", JOptionPane.ERROR_MESSAGE);
                /*msExchangeVersaoJComboBox.setEnabled(false);
                 msExchangeVersaoJComboBox.setSelectedIndex(0);
                 servidorSMTPJTextField.setEnabled(true);
                 portaSMTPJFormattedTextField.setEnabled(true);
                 servidorIMAPJTextField.setEnabled(true);
                 portaIMAPJFormattedTextField.setEnabled(true);
                 servidorMSExchangeJTextField.setEnabled(false);*/
                servidorJComboBox.setSelectedItem("MS Exchange");
                break;
        }
    }//GEN-LAST:event_servidorJComboBoxActionPerformed

    private void pastaRecebidosJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pastaRecebidosJButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setDialogTitle("Pasta Recebidos");
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            pastaRecebidosJTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath() + "\\");

            props.put("cassini.recebidos.folder", pastaXMLJTextField.getText());
            try {
                FileOutputStream fos = new FileOutputStream("./properties/.properties");
                props.store(fos, "Propriedades do Cassini");
                fos.flush();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_pastaRecebidosJButtonActionPerformed

    private void pastaValidadosJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pastaValidadosJButtonActionPerformed
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser.setDialogTitle("Pasta Recebidos");
        int returnVal = jFileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            pastaValidadosJTextField.setText(jFileChooser.getSelectedFile().getAbsolutePath() + "\\");

            props.put("cassini.validados.folder", pastaXMLJTextField.getText());
            try {
                FileOutputStream fos = new FileOutputStream("./properties/.properties");
                props.store(fos, "Propriedades do Cassini");
                fos.flush();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(PrincipalJFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_pastaValidadosJButtonActionPerformed

    private void editarRegexJRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editarRegexJRadioButtonActionPerformed
        if (editarRegexJRadioButton.isSelected()) {
            regexJTextField.setEnabled(true);
        } else {
            regexJTextField.setEnabled(false);
        }
    }//GEN-LAST:event_editarRegexJRadioButtonActionPerformed

    private void tipoColunaJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoColunaJComboBoxActionPerformed
        String tipo = tipoColunaJComboBox.getSelectedItem().toString();
        /*
         Texto
         Tempo (AAAA/MM/DD)
         Número separado por ponto
         Número separado por vírgula
         Inteiro
         */
        if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
            switch (tipo) {
                case "Texto":
                    regexJTextField.setText("/^.{1," + tamanhoColunaJSlider.getValue() + "}$/");
                    editarRegexJRadioButton.setEnabled(true);
                    tamanhoColunaJSlider.setEnabled(true);
                    break;
                case "Tempo (AAAA/MM/DD)":
                    regexJTextField.setText("/^[0-9]{4}[\\/][0-9]{1,2}[\\/][0-9]{1,2}$/");
                    regexJTextField.setEnabled(false);
                    editarRegexJRadioButton.setEnabled(false);
                    tamanhoColunaJSlider.setValue(10);
                    tamanhoColunaJSlider.setEnabled(false);
                    break;
                case "Número separado por ponto":
                    regexJTextField.setText("/^[0-9]+[.]?[0-9]*$/");
                    regexJTextField.setEnabled(false);
                    editarRegexJRadioButton.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(true);
                    break;
                case "Número separado por vírgula":
                    regexJTextField.setText("/^[0-9]+[,]?[0-9]*$/");
                    regexJTextField.setEnabled(false);
                    editarRegexJRadioButton.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(true);
                    break;
                case "Inteiro":
                    if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
                        regexJTextField.setText("/^[0-9]{1," + tamanhoColunaJSlider.getValue() + "}$/");
                    } else {
                        regexJTextField.setText("/^[0-9]+$/");
                    }
                    regexJTextField.setEnabled(false);
                    editarRegexJRadioButton.setEnabled(false);
                    tamanhoColunaJSlider.setEnabled(true);
                    break;
            }
        }
    }//GEN-LAST:event_tipoColunaJComboBoxActionPerformed

    private void addContatoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addContatoJButtonActionPerformed
        if (nomeContatoJTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher Nome do contato.", "Erro", JOptionPane.ERROR_MESSAGE);
        } else if (telefoneContatoJTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher Telefone do contato.", "Erro", JOptionPane.ERROR_MESSAGE);
        } else if (emailContatoJTextField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Preencher E-mail do contato.", "Erro", JOptionPane.ERROR_MESSAGE);
        } else {
            try {
                InternetAddress internetAddress = new InternetAddress(emailContatoJTextField.getText());
                internetAddress.validate();

                DefaultTableModel model = (DefaultTableModel) contatosJTable.getModel();
                model.addRow(new Object[]{nomeContatoJTextField.getText(), emailContatoJTextField.getText(), telefoneContatoJTextField.getText()});
                nomeContatoJTextField.setText("");
                telefoneContatoJTextField.setText("");
                emailContatoJTextField.setText("");
                incluirColetaJButton.setEnabled(true);
                removeContatoJButton.setEnabled(true);
            } catch (AddressException ex) {
                JOptionPane.showMessageDialog(incluirColetaJFrame, "E-mail do contato inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_addContatoJButtonActionPerformed

    private void removeContatoJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeContatoJButtonActionPerformed
        int[] rows = contatosJTable.getSelectedRows();
        DefaultTableModel modelContatos = (DefaultTableModel) contatosJTable.getModel();
        for (int i = 0; i < rows.length; i++) {
            modelContatos.removeRow(rows[i] - i);
        }
        if (modelContatos.getRowCount() == 0) {
            incluirColetaJButton.setEnabled(false);
            removeContatoJButton.setEnabled(false);
        }
    }//GEN-LAST:event_removeContatoJButtonActionPerformed

    private void addChaveUnicaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addChaveUnicaJButtonActionPerformed
        int[] colunasDisponiveisChaveUnicaSelecionadas = colunasDisponiveisChaveUnicaJList.getSelectedIndices();
        DefaultListModel modelColunasChaveUnicaDisponiveis = (DefaultListModel) colunasDisponiveisChaveUnicaJList.getModel();
        DefaultListModel modelColunasChaveUnicaConsolidacao = (DefaultListModel) colunasConsolidacaoChaveUnicaJList.getModel();
        for (int i = 0; i < colunasDisponiveisChaveUnicaSelecionadas.length; i++) {
            boolean duplicado = false;
            for (int j = 0; j < modelColunasChaveUnicaConsolidacao.getSize(); j++) {
                if (modelColunasChaveUnicaDisponiveis.getElementAt(colunasDisponiveisChaveUnicaSelecionadas[i]).toString().equals(modelColunasChaveUnicaConsolidacao.getElementAt(j))) {
                    duplicado = true;
                }
            }

            if (!duplicado) {
                String coluna = modelColunasChaveUnicaDisponiveis.getElementAt(colunasDisponiveisChaveUnicaSelecionadas[i]).toString();
                modelColunasChaveUnicaConsolidacao.addElement(coluna);
            }
        }

        if (!modelColunasChaveUnicaConsolidacao.isEmpty()) {
            okColunasChaveJButton.setEnabled(true);
        } else {
            okColunasChaveJButton.setEnabled(false);
        }
    }//GEN-LAST:event_addChaveUnicaJButtonActionPerformed

    private void removeChaveUnicaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeChaveUnicaJButtonActionPerformed
        int[] colunasConsolidacaoChaveUnicaSelecionadas = colunasConsolidacaoChaveUnicaJList.getSelectedIndices();
        DefaultListModel modelColunasChaveUnicaConsolidacao = (DefaultListModel) colunasConsolidacaoChaveUnicaJList.getModel();
        for (int i = 0; i < colunasConsolidacaoChaveUnicaSelecionadas.length; i++) {
            modelColunasChaveUnicaConsolidacao.removeElementAt(colunasConsolidacaoChaveUnicaSelecionadas[i] - i);
        }
        if (modelColunasChaveUnicaConsolidacao.getSize() == 0) {
            okColunasChaveJButton.setEnabled(false);
        } else {
            okColunasChaveJButton.setEnabled(true);
        }
    }//GEN-LAST:event_removeChaveUnicaJButtonActionPerformed

    private void cancelarColunasChaveUnicaJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarColunasChaveUnicaJButtonActionPerformed
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeElement("Chave Única");
        ((DefaultListModel) colunasConsolidacaoJList.getModel()).removeAllElements();
        DefaultTableModel model = (DefaultTableModel) limitesJTable.getModel();
        while (model.getRowCount() != 0) {
            model.removeRow(0);
        }
        chaveUnicaJFrame.setVisible(false);
    }//GEN-LAST:event_cancelarColunasChaveUnicaJButtonActionPerformed

    private void okColunasChaveJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okColunasChaveJButtonActionPerformed
        chaveUnicaJFrame.setVisible(false);
    }//GEN-LAST:event_okColunasChaveJButtonActionPerformed

    private void testarQueryJToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testarQueryJToggleButtonActionPerformed
        String HOST = props.getProperty("cassini.sas.host");
        int PORT = Integer.parseInt(props.getProperty("cassini.sas.port"));
        String USERNAME = props.getProperty("cassini.usuario.email").split("@")[0];
        String PASSWORD = props.getProperty("cassini.usuario.pass");

        String query = queryJTextArea.getText();

        StringBuilder erros = new StringBuilder();

        if (!query.isEmpty()) {
            if (query.toLowerCase().contains("run;")) {
                erros.append("Não incluir comando RUN na query.").append("\n");
            }
            if (query.toLowerCase().contains("proc sql;")) {
                erros.append("Não incluir comando PROC SQL na query.").append("\n");
            }
            if (query.toLowerCase().contains("quit;")) {
                erros.append("Não incluir QUIT; na query.").append("\n");
            }
            if (!query.toLowerCase().startsWith("select")) {
                erros.append("Somente SELECT é aceito.").append("\n");
            }
            if (query.endsWith(";")) {
                erros.append("Não terminar query com ; no final.").append("\n");
            }
        }
        if (erros.toString().isEmpty()) {
            try {
                ConexaoSAS sasc = new ConexaoSAS();
                sasc.addActionListener(this);
                sasc.conectar(HOST, PORT, USERNAME, PASSWORD);
                if (query.length() == 0) {
                    query = "SELECT * FROM " + enderecoBancoJTextField.getText() + "." + tabelaJTextField.getText() + " WHERE monotonic(1) = 1;";
                }
                Connection conn = sasc.executarProcessFlow("libname " + enderecoBancoJTextField.getText() + " meta LIBRARY=" + enderecoBancoJTextField.getText() + ";PROC SQL;CREATE TABLE WORK." + tabelaJTextField.getText() + " AS " + query + ";QUIT;");

                LineType[] lineTypes = sasc.getLogLineTypes();
                String[] log = sasc.getLog();
                for (int i = 0; i < lineTypes.length; i++) {
                    LineType lineType = lineTypes[i];
                    if (lineType.value() == LineType._LineTypeError || log[i].startsWith("ERROR:")) {
                        erros.append(log[i]).append("\n");
                    }
                }

                conn.close();
                this.actionPerformed(new SASAction(SASAction.DESCONECTAR));

            } catch (Exception ex) {
                ex.printStackTrace();
                erros.append(ex.getMessage());
            }
        }
        if (!erros.toString().isEmpty()) {
            JOptionPane.showMessageDialog(incluirColetaJFrame, erros.toString(), "Erro", JOptionPane.ERROR_MESSAGE);
            acaoJComboBox.setEnabled(false);
            acaoJComboBox.setSelectedIndex(0);
            tipoScriptJComboBox.setEnabled(false);
            descricaoJTextArea.setEnabled(false);
            descricaoJTextArea.setText("");
            periodicidadeJComboBox.setEnabled(false);
            atualizacaoJComboBox.setEnabled(false);

            nomeColunaJTextField.setEnabled(false);
            //nomeColunaJTextField.setText("");
            aliasColunaJTextField.setEnabled(false);
            //aliasColunaJTextField.setText("");
            classeColunaJComboBox.setEnabled(false);
            //classeColunaJComboBox.setSelectedIndex(0);
            limparColunaJButton.setEnabled(false);
            incluirColunaJButton.setEnabled(false);
            colunasJTable.setEnabled(false);
            descricaoColunaJTextArea.setEnabled(false);
            //descricaoColunaJTextArea.setText("");

            tipoColunaJComboBox.setEnabled(false);
            tamanhoColunaJSlider.setEnabled(false);

            excluirColunaJButton.setEnabled(false);
            validacaoesDisponiveisJList.setEnabled(false);
            validacoesRealizadasJList.setEnabled(false);
            addValidacaoJButton.setEnabled(false);
            removeValidacaoJButton.setEnabled(false);
            incluirColetaJButton.setEnabled(false);
            dominioJComboBox.setEnabled(false);
            //coletaJInternalFrame.hide();
        } else {
            JOptionPane.showMessageDialog(incluirColetaJFrame, "Query Ok", "Ok", JOptionPane.INFORMATION_MESSAGE);
            tabelaOkJLabel.setForeground(Color.black);
            tabelaOkJLabel.setText("Ok");
            acaoJComboBox.setEnabled(true);
            tipoScriptJComboBox.setEnabled(true);
            descricaoJTextArea.setEnabled(true);
            periodicidadeJComboBox.setEnabled(true);

            nomeColunaJTextField.setEnabled(true);
            aliasColunaJTextField.setEnabled(true);
            classeColunaJComboBox.setEnabled(true);
            limparColunaJButton.setEnabled(true);
            colunasJTable.setEnabled(true);
            descricaoColunaJTextArea.setEnabled(true);

            if (!nomeColunaJTextField.getText().isEmpty()) {
                incluirColunaJButton.setEnabled(true);
            } else {
                incluirColunaJButton.setEnabled(false);
            }

            if (classeColunaJComboBox.getSelectedItem().toString().equals("Outros")) {
                tipoColunaJComboBox.setEnabled(true);
                tamanhoColunaJSlider.setEnabled(true);
            } else {
                tipoColunaJComboBox.setEnabled(false);
                tamanhoColunaJSlider.setEnabled(false);
            }

            if (colunasJTable.getModel().getRowCount() > 0) {
                excluirColunaJButton.setEnabled(true);
                validacaoesDisponiveisJList.setEnabled(true);
                validacoesRealizadasJList.setEnabled(true);
                addValidacaoJButton.setEnabled(true);
                removeValidacaoJButton.setEnabled(true);
                incluirColetaJButton.setEnabled(true);
            } else {
                excluirColunaJButton.setEnabled(false);
                validacaoesDisponiveisJList.setEnabled(false);
                validacoesRealizadasJList.setEnabled(false);
                addValidacaoJButton.setEnabled(false);
                removeValidacaoJButton.setEnabled(false);
                incluirColetaJButton.setEnabled(false);
            }
            if (tipoLeiauteJComboBox.getSelectedItem().equals("Coleta")) {
                dominioJComboBox.setEnabled(true);
            } else {
                dominioJComboBox.setEnabled(false);
            }
        }
    }//GEN-LAST:event_testarQueryJToggleButtonActionPerformed

    private void gerenciarColetaJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gerenciarColetaJMenuItemActionPerformed
        if (!pastaXMLJTextField.getText().isEmpty() && new File(pastaXMLJTextField.getText()).isDirectory()) {
            incluirColetaJFrame.pack();
            incluirColetaJFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Configure primeiro a pasta XML do Cassini", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_gerenciarColetaJMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        close();
    }//GEN-LAST:event_formWindowClosing

    private void incluirUsuarioPermissoesJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incluirUsuarioPermissoesJButtonActionPerformed
        try {
            InternetAddress internetAddress = new InternetAddress(incluirPermissoesJTextField.getText());
            internetAddress.validate();
            internetAddress = null;
            DefaultListModel model = (DefaultListModel) usuarioPermissoesJList.getModel();
            model.addElement(incluirPermissoesJTextField.getText());
            incluirPermissoesJTextField.setText("");
            model = null;
            excluirUsuarioJButton.setEnabled(true);
            tipoLeiauteIncluirPermissoesJComboBox.setEnabled(true);
        } catch (AddressException ex) {
            JOptionPane.showMessageDialog(incluirPermissoesJPanel, "Email informado não é válido", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_incluirUsuarioPermissoesJButtonActionPerformed

    private void permissoesJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_permissoesJMenuItemActionPerformed
        if (!pastaXMLJTextField.getText().isEmpty() && new File(pastaXMLJTextField.getText()).isDirectory()) {
            permissoesJFrame.pack();
            permissoesJFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Configure primeiro a pasta XML do Cassini", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_permissoesJMenuItemActionPerformed

    private void incluirPermissoesJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_incluirPermissoesJButtonActionPerformed
        incluirPermissoesJFrame.pack();
        incluirPermissoesJFrame.setVisible(true);
    }//GEN-LAST:event_incluirPermissoesJButtonActionPerformed

    private void excluirUsuarioJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_excluirUsuarioJButtonActionPerformed
        try {
            DefaultListModel model = (DefaultListModel) usuarioPermissoesJList.getModel();

            model.remove(usuarioPermissoesJList.getSelectedIndex());
            if (model.isEmpty()) {
                excluirUsuarioJButton.setEnabled(false);
                tipoLeiauteIncluirPermissoesJComboBox.setSelectedIndex(0);
                tipoLeiauteIncluirPermissoesJComboBox.setEnabled(false);
                leiauteIncluirPermissoesJComboBox.setSelectedIndex(0);
                leiauteIncluirPermissoesJComboBox.setEnabled(false);
                parametrosIncluirPermissoesJComboBox.setSelectedIndex(0);
                parametrosIncluirPermissoesJComboBox.setEnabled(false);
                valorIncluirPermissoesJTextField.setText("");
                valorIncluirPermissoesJTextField.setEnabled(false);
                incluirValorPermissoesJButton.setEnabled(false);
                parametrosPermissoesJTable.setEnabled(false);
                parametrosPermissoesJTable.setModel(new javax.swing.table.DefaultTableModel(
                        new Object[][]{},
                        new String[]{
                            "Parâmetro", "Valor"
                        }
                ) {
                    Class[] types = new Class[]{
                        java.lang.String.class, java.lang.Object.class
                    };
                    boolean[] canEdit = new boolean[]{
                        false, false
                    };

                    public Class getColumnClass(int columnIndex) {
                        return types[columnIndex];
                    }

                    public boolean isCellEditable(int rowIndex, int columnIndex) {
                        return canEdit[columnIndex];
                    }
                });
                parametrosPermissoesJTable.setEnabled(false);
                excluirValorPermissoesJButton.setEnabled(false);
                okIncluirPermissoesJButton.setEnabled(false);
            }
        } catch (Exception ex) {

        }
    }//GEN-LAST:event_excluirUsuarioJButtonActionPerformed

    private void cancelarIncluirPermissoesJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelarIncluirPermissoesJButtonActionPerformed
        incluirPermissoesJTextField.setText("");
        usuarioPermissoesJList.setModel(new DefaultListModel());
        excluirUsuarioJButton.setEnabled(false);
        tipoLeiauteIncluirPermissoesJComboBox.setSelectedIndex(0);
        tipoLeiauteIncluirPermissoesJComboBox.setEnabled(false);
        if (leiauteIncluirPermissoesJComboBox.getItemCount() > 0) {
            leiauteIncluirPermissoesJComboBox.setSelectedIndex(0);
        }
        leiauteIncluirPermissoesJComboBox.setEnabled(false);
        if (parametrosIncluirPermissoesJComboBox.getItemCount() > 0) {
            parametrosIncluirPermissoesJComboBox.setSelectedIndex(0);
        }
        parametrosIncluirPermissoesJComboBox.setEnabled(false);
        valorIncluirPermissoesJTextField.setText("");
        valorIncluirPermissoesJTextField.setEnabled(false);
        incluirValorPermissoesJButton.setEnabled(false);
        parametrosPermissoesJTable.setEnabled(false);
        parametrosPermissoesJTable.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{},
                new String[]{
                    "Parâmetro", "Valor"
                }
        ) {
            Class[] types = new Class[]{
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean[]{
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        parametrosPermissoesJTable.setEnabled(false);
        excluirValorPermissoesJButton.setEnabled(false);
        okIncluirPermissoesJButton.setEnabled(false);
        incluirPermissoesJFrame.setVisible(false);
    }//GEN-LAST:event_cancelarIncluirPermissoesJButtonActionPerformed

    private void tipoLeiauteIncluirPermissoesJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tipoLeiauteIncluirPermissoesJComboBoxActionPerformed
        String tipoLeiaute = tipoLeiauteIncluirPermissoesJComboBox.getSelectedItem().toString();
        File pastaXML = null;
        DefaultComboBoxModel model = new DefaultComboBoxModel();

        switch (tipoLeiaute) {
            case "Selecione...":
                break;
            case "Coleta":
                //dominioJComboBox.setModel(getDominios());
                pastaXML = new File(pastaXMLJTextField.getText());
                break;
            case "Domínio":

                /*model = new DefaultComboBoxModel();
                 model.addElement("Não");
                 dominioJComboBox.setModel(model);*/
                //dominioJComboBox.setModel(getDominios());
                pastaXML = new File(pastaXMLJTextField.getText() + "\\dominios\\");
                pastaXML.mkdir();
                break;
        }

        if (pastaXML != null) {
            File[] leiautes = pastaXML.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".xml")) {
                        return true;
                    }
                    return false;
                }
            });

            DefaultComboBoxModel modelLeiautes = new DefaultComboBoxModel();

            modelLeiautes.addElement("Selecione...");

            for (int i = 0; i < leiautes.length; i++) {
                try {
                    //URL schemaFile = new URL(coletaXSD);
                    File schemaFile = new File(coletaXSD);
                    Source xmlFile = new StreamSource(leiautes[i]);
                    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = schemaFactory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiautes[i]);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    modelLeiautes.addElement(scriptTag.getAttribute("leiaute"));

                } catch (Exception ex) {
                }
            }

            leiauteIncluirPermissoesJComboBox.setEnabled(true);
            leiauteIncluirPermissoesJComboBox.setModel(modelLeiautes);
        } else {
            leiauteIncluirPermissoesJComboBox.setEnabled(false);
            leiauteIncluirPermissoesJComboBox.setModel(new DefaultComboBoxModel());
        }
    }//GEN-LAST:event_tipoLeiauteIncluirPermissoesJComboBoxActionPerformed

    private void contadorDiasJSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_contadorDiasJSliderStateChanged
        contadorDiasJLabel.setText(contadorDiasJSlider.getValue() + " dias");
    }//GEN-LAST:event_contadorDiasJSliderStateChanged

    private void cronogramaJMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cronogramaJMenuItemActionPerformed
        new Cronograma(this).start();
        cronogramaJFrame.pack();
        cronogramaJFrame.setVisible(true);
    }//GEN-LAST:event_cronogramaJMenuItemActionPerformed

    private void atualizarJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_atualizarJButtonActionPerformed
        new Cronograma(this).start();
    }//GEN-LAST:event_atualizarJButtonActionPerformed

    private void cobrarJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cobrarJButtonActionPerformed
        new CobraAtradados().start();
    }//GEN-LAST:event_cobrarJButtonActionPerformed

    private void limparCompararJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_limparCompararJButtonActionPerformed
        comparaJTextArea.setText("PROC SQL;\n"
                + "	CREATE TABLE resultado AS\n"
                + "	SELECT\n"
                + "		t1.*,\n"
                + "		'mensagem de teste' as mensagem\n"
                + "	FROM DADOS t1;\n"
                + "QUIT;");
        logComparaJTextArea.setText("");
        resultadoComparaJTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{}));
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeElement("Compara Dados");
        compararJFrame.setVisible(false);
    }//GEN-LAST:event_limparCompararJButtonActionPerformed

    private void okCompararJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okCompararJButtonActionPerformed
        compararJFrame.setVisible(false);
    }//GEN-LAST:event_okCompararJButtonActionPerformed

    private void testarCompararJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_testarCompararJButtonActionPerformed
        logComparaJTextArea.setText("");
        resultadoComparaJTable.setModel(new DefaultTableModel(new Object[][]{}, new String[]{}));
        try {
            final String HOST = props.getProperty("cassini.sas.host");
            final int PORT = Integer.parseInt(props.getProperty("cassini.sas.port"));
            final String USERNAME = props.getProperty("cassini.usuario.email").split("@")[0];
            final String PASSWORD = props.getProperty("cassini.usuario.pass");

            StringBuilder createBancoScript = new StringBuilder();
            //createBancoScript.append("LIBNAME DADOS BASE \"").append(enderecoBancoJTextField.getText()).append("\";").append(System.getProperty("line.separator"));
            createBancoScript.append("DATA WORK.DADOS;").append(System.getProperty("line.separator"));
            createBancoScript.append("	INPUT").append(System.getProperty("line.separator"));
            DefaultTableModel model = (DefaultTableModel) colunasJTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" : ");
                switch (model.getValueAt(i, 3).toString()) {
                    case "Texto":
                        createBancoScript.append("$CHAR").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Tempo (AAAA/MM/DD)":
                        createBancoScript.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por ponto":
                        createBancoScript.append("?? BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por vírgula":
                        createBancoScript.append("?? COMMAX").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Inteiro":
                        createBancoScript.append("?? BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                }
            }
            createBancoScript.append("		arquivo : $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente : $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio : IS8601DT26.").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	FORMAT").append(System.getProperty("line.separator"));
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" ");
                switch (model.getValueAt(i, 3).toString()) {
                    case "Texto":
                        createBancoScript.append("$CHAR").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Tempo (AAAA/MM/DD)":
                        createBancoScript.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por ponto":
                        createBancoScript.append("BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por vírgula":
                        createBancoScript.append("COMMAX").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Inteiro":
                        createBancoScript.append("BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                }
            }
            createBancoScript.append("		arquivo $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio IS8601DT26.").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	LABEL").append(System.getProperty("line.separator"));
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" =\"").append(model.getValueAt(i, 1)).append("\"").append(System.getProperty("line.separator"));
            }
            createBancoScript.append("		arquivo =\"Nome do Arquivo Recebido\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente =\"Email do remetente do arquivo\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio =\"Data de envio do arquivo\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	DATALINES;").append(System.getProperty("line.separator"));
            Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < model.getRowCount(); i++) {
                switch (model.getValueAt(i, 3).toString()) {
                    case "Texto":
                        createBancoScript.append("ABCD ");
                        break;
                    case "Tempo (AAAA/MM/DD)":
                        createBancoScript.append(calendar.get(Calendar.YEAR)).append("/").append(calendar.get(Calendar.MONTH) < 10 ? "0" + calendar.get(Calendar.MONTH) : calendar.get(Calendar.MONTH)).append("/").append(calendar.get(Calendar.DAY_OF_YEAR) < 10 ? "0" + calendar.get(Calendar.DAY_OF_YEAR) : calendar.get(Calendar.DAY_OF_YEAR)).append(" ");
                        break;
                    case "Número separado por ponto":
                        createBancoScript.append("123.4 ");
                        break;
                    case "Número separado por vírgula":
                        createBancoScript.append("123,4 ");
                        break;
                    case "Inteiro":
                        if (model.getValueAt(i, 5).toString().equals("Ano")) {
                            createBancoScript.append(calendar.get(Calendar.YEAR)).append(" ");
                        } else if (model.getValueAt(i, 5).toString().equals("Mês")) {
                            createBancoScript.append(calendar.get(Calendar.MONTH)).append(" ");
                        } else if (model.getValueAt(i, 5).toString().equals("Dia")) {
                            createBancoScript.append("1 ");
                        } else {
                            createBancoScript.append("123 ");
                        }
                        break;
                }
            }
            createBancoScript.append("123_arquivo.csv danieloliveira@anatel.gov.br 01\\01\\1900:0:0:0").append(System.getProperty("line.separator"));
            createBancoScript.append(";").append(System.getProperty("line.separator"));

            createBancoScript.append(comparaJTextArea.getText()).append(System.getProperty("line.separator"));
            createBancoScript.append(System.getProperty("line.separator"));

            createBancoScript.append("PROC SQL;").append(System.getProperty("line.separator"));
            createBancoScript.append("CREATE TABLE mensagem_validacao AS ").append(System.getProperty("line.separator"));
            createBancoScript.append("SELECT distinct \"teste\" as metodo,").append(System.getProperty("line.separator"));
            createBancoScript.append("'123' AS arquivo, ").append(System.getProperty("line.separator"));
            createBancoScript.append("/* parametro */").append(System.getProperty("line.separator"));
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("(cat(catx(';',");
            for (int i = 0; i < model.getRowCount(); i++) {
                switch (model.getValueAt(i, 3).toString()) {
                    case "Tempo (AAAA/MM/DD)":
                        stringBuilder.append("cat('").append(model.getValueAt(i, 0)).append("=', put(t1.").append(model.getValueAt(i, 0)).append(",ptgdfmy7.)),");
                        break;
                    default:
                        stringBuilder.append("cat('").append(model.getValueAt(i, 0)).append("=',").append("t1.").append(model.getValueAt(i, 0)).append("),");
                        break;
                }
            }
            createBancoScript.append(stringBuilder.toString().substring(0, stringBuilder.length() - 1)).append("))) AS parametros,").append(System.getProperty("line.separator"));
            createBancoScript.append("/* mensagem */").append(System.getProperty("line.separator"));
            createBancoScript.append("t1.mensagem,").append(System.getProperty("line.separator"));
            createBancoScript.append("'true' AS aceitavel").append(System.getProperty("line.separator"));
            createBancoScript.append("FROM resultado t1;").append(System.getProperty("line.separator"));
            createBancoScript.append("QUIT;").append(System.getProperty("line.separator"));
            createBancoScript.append(System.getProperty("line.separator"));

            createBancoScript.append("PROC SQL; DROP TABLE resultado;").append(System.getProperty("line.separator"));
            createBancoScript.append("QUIT;").append(System.getProperty("line.separator"));
            createBancoScript.append("PROC SQL; DROP TABLE dados;").append(System.getProperty("line.separator"));
            createBancoScript.append("QUIT;").append(System.getProperty("line.separator"));

            ConexaoSAS sasc = new ConexaoSAS();
            sasc.addActionListener(this);
            sasc.conectar(HOST, PORT, USERNAME, PASSWORD);
            Connection conn = sasc.executarProcessFlow(createBancoScript.toString());
            this.actionPerformed(new SASAction(SASAction.DESCONECTAR));

            LineType[] lineTypes = sasc.getLogLineTypes();
            String[] log = sasc.getLog();
            StringBuilder logString = new StringBuilder();
            boolean ok = true;
            for (int i = 0; i < lineTypes.length; i++) {
                logString.append(log[i]).append(System.getProperty("line.separator"));
                LineType lineType = lineTypes[i];
                if (lineType.value() == LineType._LineTypeError || log[i].startsWith("ERROR:")) {
                    ok = false;
                }
            }
            okCompararJButton.setEnabled(ok);
            comparaJTabbedPane.setSelectedIndex(1);

            logComparaJTextArea.setText(logString.toString());

            String[] colunas = new String[]{"metodo", "arquivo", "parametros", "mensagem", "aceitavel"};
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM mensagem_validacao");
            String[][] linhas = new String[1][5];
            while (rs.next()) {
                linhas[0][0] = rs.getString("metodo");
                linhas[0][1] = rs.getString("arquivo");
                linhas[0][2] = rs.getString("parametros");
                linhas[0][3] = rs.getString("mensagem");
                linhas[0][4] = rs.getString("aceitavel");
            }

            resultadoComparaJTable.setModel(new DefaultTableModel(linhas, colunas));
            comparaJTabbedPane.setSelectedIndex(2);
            conn.close();

            //return new File(enderecoBancoJTextField.getText() + "\\" + tabelaJTextField.getText() + ".sas7bdat").isFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            //return false;
        }
    }//GEN-LAST:event_testarCompararJButtonActionPerformed

    private boolean isPermissoes() {
        File pastaXML = new File(pastaXMLJTextField.getText());
        File[] permissoes = pastaXML.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith("_permissoes.xml")) {
                    return true;
                }
                return false;
            }
        });

        boolean permissaoOk = true;

        if (permissoes != null && permissoes.length == 1) {
            try {
                //URL schemaFile = new URL(coletaXSD);
                File schemaFile = new File(coletaXSD);
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(schemaFile);
                Source xmlFile = new StreamSource(permissoes[0]);
                Validator validator = schema.newValidator();
                validator.validate(xmlFile);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(false);
                DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                Document doc = docBuilder.parse(permissoes[0]);
                Element coletaTag = doc.getDocumentElement();
                Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                String nomeLeiaute = scriptTag.getAttribute("leiaute");
                if (nomeLeiaute.toUpperCase().equals("PERMISSOES")) {
                    NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");
                    if (colunasTag.getLength() == 4) {
                        Element usuarioColunaTag = (Element) colunasTag.item(0);
                        Element leiauteColunaTag = (Element) colunasTag.item(1);
                        Element parametroColunaTag = (Element) colunasTag.item(2);
                        Element valorColunaTag = (Element) colunasTag.item(3);
                        if (usuarioColunaTag.getAttribute("nome") == "usuario"
                                || leiauteColunaTag.getAttribute("nome") == "leiaute"
                                || parametroColunaTag.getAttribute("nome") == "parametro"
                                || valorColunaTag.getAttribute("nome") == "valor") {
                            permissaoOk = false;
                        }
                    } else {
                        permissaoOk = false;
                    }

                } else {
                    permissaoOk = false;
                }
            } catch (Exception ex) {
                permissaoOk = false;
            }
            return permissaoOk;
        }
        return false;
    }

    class CobraAtradados extends Thread {

        private SASActionListener sasActionListener;

        class Leiaute {

            private String ultimaColeta;
            private String proximaColeta;
            private String prazo;
            private String nome;
            private String periodicidade;

            public Leiaute(String nome, String periodicidade, String prazo, String ultimaColeta, String proximaColeta) {
                this.ultimaColeta = ultimaColeta;
                this.proximaColeta = proximaColeta;
                this.prazo = prazo;
                this.nome = nome;
                this.periodicidade = periodicidade;
            }

            public String getUltimaColeta() {
                return ultimaColeta;
            }

            public String getProximaColeta() {
                return proximaColeta;
            }

            public String getPrazo() {
                return prazo;
            }

            public String getNome() {
                return nome;
            }

            public String getPeriodicidade() {
                return periodicidade;
            }

        }

        class Usuario {

            private List<Leiaute> leiautes;
            private String email;

            public Usuario(String email) {
                this.email = email;
                leiautes = new ArrayList();
            }

            public void addLeiaute(Leiaute leiaute) {
                leiautes.add(leiaute);
            }

            public String getEmail() {
                return email;
            }

            public List<Leiaute> getLeiautes() {
                return leiautes;
            }

        }

        public void CobraAtrasados(SASActionListener sasActionListener) {
            this.sasActionListener = sasActionListener;
        }

        @Override
        public void run() {
            DefaultTableModel model = (DefaultTableModel) cronogramaJTable.getModel();
            int rows = model.getRowCount();

            List<Usuario> usuarios = new ArrayList();

            for (int i = 0; i < rows; i++) {
                if (Boolean.valueOf(model.getValueAt(i, 5).toString())) {
                    Leiaute leiaute = new Leiaute(model.getValueAt(i, 0).toString(), model.getValueAt(i, 1).toString(), model.getValueAt(i, 2).toString(), model.getValueAt(i, 3).toString(), model.getValueAt(i, 4).toString());
                    String[] emails = model.getValueAt(i, 6).toString().split(";");
                    if (usuarios.isEmpty()) {
                        for (String email : emails) {
                            Usuario usuario = new Usuario(email.replaceAll("^\\s+|\\s+$", ""));
                            usuario.addLeiaute(leiaute);
                            usuarios.add(usuario);
                        }
                    } else {
                        for (String email : emails) {
                            boolean newUsuario = true;
                            for (Usuario usuario : usuarios) {
                                if (email.replaceAll("^\\s+|\\s+$", "").equals(usuario.getEmail())) {
                                    newUsuario = false;
                                    usuario.addLeiaute(leiaute);
                                }
                            }
                            if (newUsuario) {
                                Usuario novoUsuario = new Usuario(email.replaceAll("^\\s+|\\s+$", ""));
                                novoUsuario.addLeiaute(leiaute);
                                usuarios.add(novoUsuario);
                            }
                        }
                    }

                }
            }

            String versao = props.getProperty("cassini.exchange.versao");
            ExchangeVersion exchangeVersion = ExchangeVersion.Exchange2007_SP1;
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

            RobotExchange robot = new RobotExchange(exchangeVersion, props.getProperty("cassini.exchange.host"), "[Cassini]");
            robot.conectar(props.getProperty("cassini.usuario.email"), new String(senhaJPasswordField.getPassword()));

            String subject = "Lista de Coletas Atrasadas";

            for (Usuario usuario : usuarios) {
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
                mensagem.append("Segue abaixo lista de coletas cadastradas no Cassini como sendo de sua responsabilidade e que estão atrasadas:<br><br>");

                mensagem.append("<table>");
                mensagem.append("<tr>");
                mensagem.append("<th>Coleta</th>");
                mensagem.append("<th>Periodicidade</th>");
                mensagem.append("<th>Última Coleta</th>");
                mensagem.append("<th>Próxima Coleta</th>");
                mensagem.append("<th>Consultar Leiaute</th>");
                mensagem.append("<th>Enviar Dados</th>");
                mensagem.append("</tr>");
                for (Leiaute leiaute : usuario.getLeiautes()) {
                    mensagem.append("<tr>");
                    mensagem.append("<th>").append(leiaute.getNome()).append("</th>");
                    mensagem.append("<th>").append(leiaute.getPeriodicidade()).append("</th>");
                    mensagem.append("<th>").append(leiaute.getUltimaColeta()).append("</th>");
                    mensagem.append("<th>").append(leiaute.getProximaColeta()).append("</th>");
                    mensagem.append("<th><a href=\"mailto:").append(props.getProperty("cassini.usuario.email")).append("?subject=[Cassini]Detalhar Leiaute:").append(leiaute.getNome()).append("\">Consultar</a></th>");
                    mensagem.append("<th><a href=\"mailto:").append(props.getProperty("cassini.usuario.email")).append("?subject=[Cassini]Enviar Arquivo:coleta;").append(leiaute.getNome()).append("\">Enviar</a></th>");
                    mensagem.append("</tr>");
                }
                mensagem.append("</table><br><br>");

                mensagem.append("Estamos aguardando a coleta do período indicado no campo \"Próxima Coleta\".<br>");
                mensagem.append("Para consultar o leiaute do arquivo, basta clicar no link \"Consultar\" correspondente. Uma caixa de email abrirá. Clique em enviar e aguarde a resposta do Cassini.<br>");
                mensagem.append("Para enviar o arquivo, basta clicar no link \"Enviar\" correspondente. Uma caixa de email abrirá. Anexe o arquivo ao email e envie.<br>");
                mensagem.append(atenciosamente());
                mensagem.append("</body>");

                List<String> destinatarios = new ArrayList();
                destinatarios.add(usuario.getEmail());

                robot.enviarEmail(destinatarios, subject, mensagem.toString(), true);

            }

            robot.desconectar();

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
    }

    private void close() {
        int response = JOptionPane.YES_OPTION;
        if (conexoesSAS > 0) {
            response = JOptionPane.showConfirmDialog(null, "Ainda há " + conexoesSAS + " conexões SAS rodando. Se o Cassini for fechado agora,\nas conexões permanecerão abertas, mas nenhuma resposta será encaminhada as usuários do Cassini.\nDeseja fechar mesmo assim?", "Aviso", JOptionPane.YES_NO_OPTION);
        }
        if (response == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }

    private void statusInicial() {
        tipoLeiauteJComboBox.setSelectedIndex(0);
        leiauteJComboBox.setSelectedIndex(0);
        leiauteJComboBox.setEnabled(false);
        leiauteJTextField.setText("");
        leiauteJTextField.setEnabled(false);
        leiauteOkJLabel.setText("");
        urlJTextField.setText("");
        urlJTextField.setEnabled(false);
        tipoBancoJComboBox.setSelectedIndex(0);
        tipoBancoJComboBox.setEnabled(false);
        enderecoBancoJTextField.setText("");
        enderecoBancoJTextField.setEnabled(false);
        enderecoBancoJButton.setEnabled(false);
        tabelaJTextField.setText("");
        tabelaJTextField.setEnabled(false);
        tabelaOkJLabel.setText("");
        queryJTextArea.setText("");
        queryJTextArea.setEnabled(false);
        acaoJComboBox.setSelectedIndex(0);
        acaoJComboBox.setEnabled(false);
        tipoScriptJComboBox.setSelectedIndex(0);
        tipoScriptJComboBox.setEnabled(false);
        descricaoJTextArea.setText("");
        descricaoJTextArea.setEnabled(false);
        enderecoScriptJTextField.setText("");
        enderecoScriptJTextField.setEnabled(false);
        periodicidadeJComboBox.setSelectedIndex(0);
        periodicidadeJComboBox.setEnabled(false);
        dominioJComboBox.setSelectedIndex(0);
        dominioJComboBox.setEnabled(false);
        nomeColunaJTextField.setText("");
        nomeColunaJTextField.setEnabled(false);
        aliasColunaJTextField.setText("");
        aliasColunaJTextField.setEnabled(false);
        colunaDominioJComboBox.setModel(null);
        colunaDominioJComboBox.setEnabled(false);
        classeColunaJComboBox.setSelectedIndex(0);
        classeColunaJComboBox.setEnabled(false);
        tipoColunaJComboBox.setSelectedIndex(0);
        tipoColunaJComboBox.setEnabled(false);
        atualizacaoJComboBox.setSelectedIndex(0);
        atualizacaoJComboBox.setEnabled(false);
        regexJTextField.setText("/^[0-9]{14}$|^[0-9]{11}$/");
        regexJTextField.setEnabled(false);
        tamanhoColunaJSlider.setValue(14);
        tamanhoColunaJSlider.setEnabled(false);
        descricaoColunaJTextArea.setText("");
        descricaoColunaJTextArea.setEnabled(false);
        ((DefaultTableModel) colunasJTable.getModel()).setRowCount(0);
        colunasJTable.setEnabled(false);
        validacaoesDisponiveisJList.setEnabled(false);
        ((DefaultListModel) validacoesRealizadasJList.getModel()).removeAllElements();
        validacoesRealizadasJList.setEnabled(false);
        nomeContatoJTextField.setText("");
        nomeContatoJTextField.setEnabled(false);
        emailContatoJTextField.setText("");
        emailContatoJTextField.setEnabled(false);
        telefoneContatoJTextField.setText("");
        telefoneContatoJTextField.setEnabled(false);
        ((DefaultTableModel) contatosJTable.getModel()).setRowCount(0);
        contatosJTable.setEnabled(false);
        removeContatoJButton.setEnabled(false);
        incluirColetaJButton.setEnabled(false);
    }

    private void statusTipoLeiauteSelecionado() {

    }

    private void statusLeiauteSelecionado() {

    }

    private void statusNomeLeiauteOk() {

    }

    private void statusBancoSelecionado() {

    }

    private void statusDescricaoPreenchida() {

    }

    private void statusSemColunas() {

    }

    private void statusComColunas() {

    }

    private void statusSemContatos() {

    }

    private void statusComContatos() {

    }

    private void statusPronto() {

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            /*for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
             if ("Nimbus".equals(info.getName())) {
             javax.swing.UIManager.setLookAndFeel(info.getClassName());
             break;
             }
             }*/
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PrincipalJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrincipalJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrincipalJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrincipalJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PrincipalJFrame().setVisible(true);
            }
        });
    }

    private DefaultComboBoxModel getColunasDado() {
        DefaultTableModel colunasModel = (DefaultTableModel) colunasJTable.getModel();
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement("Selecione...");
        for (int i = 0; i < colunasModel.getRowCount(); i++) {
            String tipo = colunasModel.getValueAt(i, 3).toString();
            if (tipo.equals("Número separado por ponto") || tipo.equals("Número separado por vírgula") || tipo.equals("Inteiro")) {
                model.addElement(colunasModel.getValueAt(i, 0));
            }
        }
        return model;
    }

    private DefaultListModel getColunasConsolidacao(JComboBox jComboBox) {
        DefaultTableModel colunasModel = (DefaultTableModel) colunasJTable.getModel();
        DefaultListModel model = new DefaultListModel();
        for (int i = 0; i < colunasModel.getRowCount(); i++) {
            if (jComboBox == null || !colunasModel.getValueAt(i, 0).toString().equals(jComboBox.getSelectedItem().toString())) {
                model.addElement(colunasModel.getValueAt(i, 0));
            }
        }
        return model;
    }

    public static void addLog(String mensagem) {
        Calendar calendar = Calendar.getInstance();
        if (logJTextArea.getText().getBytes().length > 5000000) {
            logJTextArea.setText("[Cassini " + (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) < 10 ? "0" : "") + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + calendar.get(Calendar.HOUR_OF_DAY) + ":" + (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE) + ":" + (calendar.get(Calendar.SECOND) < 10 ? "0" : "") + calendar.get(Calendar.SECOND) + "] " + mensagem + "\n");
        } else {
            logJTextArea.setText(logJTextArea.getText() + "[Cassini " + (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" : "") + calendar.get(Calendar.DAY_OF_MONTH) + "/" + (calendar.get(Calendar.MONTH) < 10 ? "0" : "") + calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.YEAR) + " " + (calendar.get(Calendar.HOUR_OF_DAY) < 10 ? "0" : "") + calendar.get(Calendar.HOUR_OF_DAY) + ":" + (calendar.get(Calendar.MINUTE) < 10 ? "0" : "") + calendar.get(Calendar.MINUTE) + ":" + (calendar.get(Calendar.SECOND) < 10 ? "0" : "") + calendar.get(Calendar.SECOND) + "] " + mensagem + "\n");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox acaoJComboBox;
    private javax.swing.JLabel acaoJLabel;
    private javax.swing.JButton addChaveUnicaJButton;
    private javax.swing.JButton addColunaDuplicadoJButton;
    private javax.swing.JButton addConsolidacaoJButton;
    private javax.swing.JButton addContatoJButton;
    private javax.swing.JButton addLimiteJButton;
    private javax.swing.JButton addValidacaoJButton;
    private javax.swing.JButton adicionarContatoJButton;
    private javax.swing.JLabel aliasColunaJLabel;
    private javax.swing.JTextField aliasColunaJTextField;
    private javax.swing.JComboBox<String> apagarEmailsJComboBox;
    private javax.swing.JLabel apagarEmailsJLabel;
    private javax.swing.JComboBox atualizacaoJComboBox;
    private javax.swing.JFormattedTextField atualizacaoJFormattedTextField;
    private javax.swing.JLabel atualizacaoJLabel;
    private javax.swing.JLabel atualizadaoJLabel;
    private javax.swing.JButton atualizarJButton;
    private javax.swing.JRadioButton autoIniciarJRadioButton;
    private javax.swing.JButton cancelCrescimentoJButton;
    private javax.swing.JButton cancelDensidadeJButton;
    private javax.swing.JButton cancelarColunasChaveUnicaJButton;
    private javax.swing.JButton cancelarColunasDuplicadoJButton;
    private javax.swing.JButton cancelarIncluirPermissoesJButton;
    private javax.swing.JFrame chaveUnicaJFrame;
    private javax.swing.JPanel chaveUnicaJPanel;
    private javax.swing.JComboBox classeColunaJComboBox;
    private javax.swing.JLabel classeJLabel;
    private javax.swing.JButton cobrarJButton;
    private javax.swing.JMenu coletasJMenu;
    private javax.swing.JComboBox colunaDadoDuplicadoJComboBox;
    private javax.swing.JLabel colunaDadoDuplicadosJLabel;
    private javax.swing.JComboBox colunaDadoJComboBox;
    private javax.swing.JLabel colunaDadoJLabel;
    private javax.swing.JComboBox colunaDominioJComboBox;
    private javax.swing.JLabel colunaJLabel;
    private javax.swing.JLabel colunasChaveUnicaLabel;
    private javax.swing.JList colunasConsolidacaoChaveUnicaJList;
    private javax.swing.JScrollPane colunasConsolidacaoChaveUnicaJScrollPane;
    private javax.swing.JList colunasConsolidacaoDuplicadoJList;
    private javax.swing.JScrollPane colunasConsolidacaoDuplicadoJScrollPane;
    private javax.swing.JLabel colunasConsolidacaoJLabel;
    private javax.swing.JList colunasConsolidacaoJList;
    private javax.swing.JLabel colunasDisponiveisChaveUnicaJLabel;
    private javax.swing.JList colunasDisponiveisChaveUnicaJList;
    private javax.swing.JScrollPane colunasDisponiveisChaveUnicaJScrollPane;
    private javax.swing.JLabel colunasDisponiveisConsolidacaoJLabel;
    private javax.swing.JList colunasDisponiveisConsolidacaoJList;
    private javax.swing.JLabel colunasDisponiveisDuplicadoJLabel;
    private javax.swing.JList colunasDisponiveisDuplicadoJList;
    private javax.swing.JLabel colunasJLabel;
    private javax.swing.JScrollPane colunasJScrollPane;
    private javax.swing.JTable colunasJTable;
    private javax.swing.JTabbedPane comparaJTabbedPane;
    private javax.swing.JTextArea comparaJTextArea;
    private javax.swing.JFrame compararJFrame;
    private javax.swing.JLabel conexoesSASJLabel;
    private javax.swing.JPanel configurarJPanel;
    private javax.swing.JScrollPane configurarJScrollPane;
    private javax.swing.JLabel contadorDiasJLabel;
    private javax.swing.JSlider contadorDiasJSlider;
    private javax.swing.JLabel contatoJLabel;
    private javax.swing.JTextField contatoNovoJTextField;
    private javax.swing.JLabel contatosJLabel;
    private javax.swing.JList contatosJList;
    private javax.swing.JTable contatosJTable;
    private javax.swing.JFrame crescimentoJFrame;
    private javax.swing.JPanel crescimentoJPanel;
    private javax.swing.JFormattedTextField crescimentoMaximoJFormattedTextField;
    private javax.swing.JLabel crescimentoMaximoJLabel;
    private javax.swing.JFormattedTextField crescimentoMinimoJFormattedTextField;
    private javax.swing.JLabel crescimentoMinimoJLabel;
    private javax.swing.JFrame cronogramaJFrame;
    private javax.swing.JMenuItem cronogramaJMenuItem;
    private javax.swing.JPanel cronogramaJPanel;
    private javax.swing.JProgressBar cronogramaJProgressBar;
    private javax.swing.JTable cronogramaJTable;
    private javax.swing.JButton csvIncluirPermissoesJButton;
    private javax.swing.JComboBox denominadorJComboBox;
    private javax.swing.JLabel denominadorJLabel;
    private javax.swing.JFrame densidadeJFrame;
    private javax.swing.JPanel densidadeJPanel;
    private javax.swing.JLabel descricaoColunaJLabel;
    private javax.swing.JTextArea descricaoColunaJTextArea;
    private javax.swing.JTextArea descricaoJTextArea;
    private javax.swing.JLabel descricaoLeiauteJLabel;
    private javax.swing.JComboBox dominioJComboBox;
    private javax.swing.JLabel dominioJLabel;
    private javax.swing.JRadioButton editarRegexJRadioButton;
    private javax.swing.JLabel emailContatoJLabel;
    private javax.swing.JTextField emailContatoJTextField;
    private javax.swing.JButton enderecoBancoJButton;
    private javax.swing.JLabel enderecoBancoJLabel;
    private javax.swing.JTextField enderecoBancoJTextField;
    private javax.swing.JLabel enderecoScriptJLabel;
    private javax.swing.JTextField enderecoScriptJTextField;
    private javax.swing.JMenuItem enviarArquivoJMenuItem;
    private javax.swing.JLabel exchangeVersionJLabel;
    private javax.swing.JButton excluirColunaJButton;
    private javax.swing.JButton excluirContatoJButton;
    private javax.swing.JButton excluirLimiteJButton;
    private javax.swing.JButton excluirPermissoesJButton;
    private javax.swing.JButton excluirUsuarioJButton;
    private javax.swing.JButton excluirValorPermissoesJButton;
    private javax.swing.JFormattedTextField fimJFormattedTextField;
    private javax.swing.JLabel fimJLabel;
    private javax.swing.JMenuItem gerenciarColetaJMenuItem;
    private javax.swing.JButton incluirColetaJButton;
    private javax.swing.JFrame incluirColetaJFrame;
    private javax.swing.JPanel incluirColetaJPanel;
    private javax.swing.JScrollPane incluirColetaJScrollPane;
    private javax.swing.JButton incluirColunaJButton;
    private javax.swing.JButton incluirPermissoesJButton;
    private javax.swing.JFrame incluirPermissoesJFrame;
    private javax.swing.JPanel incluirPermissoesJPanel;
    private javax.swing.JTextField incluirPermissoesJTextField;
    private javax.swing.JButton incluirUsuarioPermissoesJButton;
    private javax.swing.JButton incluirValorPermissoesJButton;
    private javax.swing.JButton iniciarJButton;
    private javax.swing.JFormattedTextField inicioJFormattedTextField;
    private javax.swing.JLabel inicioJLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane10;
    private javax.swing.JScrollPane jScrollPane11;
    private javax.swing.JScrollPane jScrollPane12;
    private javax.swing.JScrollPane jScrollPane13;
    private javax.swing.JScrollPane jScrollPane14;
    private javax.swing.JScrollPane jScrollPane15;
    private javax.swing.JScrollPane jScrollPane16;
    private javax.swing.JScrollPane jScrollPane17;
    private javax.swing.JScrollPane jScrollPane18;
    private javax.swing.JScrollPane jScrollPane19;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JScrollPane jScrollPane9;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JComboBox leiauteIncluirPermissoesJComboBox;
    private javax.swing.JLabel leiauteInlcuirPermissoesJLabel;
    private javax.swing.JComboBox leiauteJComboBox;
    private javax.swing.JLabel leiauteJLabel;
    private javax.swing.JTextField leiauteJTextField;
    private javax.swing.JLabel leiauteOkJLabel;
    private javax.swing.JComboBox leiautePermissoesJComboBox;
    private javax.swing.JLabel leiautePermissoesJLabel;
    private javax.swing.JLabel limitesJLabel;
    private javax.swing.JTable limitesJTable;
    private javax.swing.JButton limparColunaJButton;
    private javax.swing.JButton limparCompararJButton;
    private javax.swing.JButton limparLimiteJButton;
    private javax.swing.JButton limparPermissoesJButton;
    private javax.swing.JLabel listaPermissoesJLabel;
    private javax.swing.JTextArea logComparaJTextArea;
    private javax.swing.JLabel logJLabel;
    private static javax.swing.JTextArea logJTextArea;
    private javax.swing.JComboBox<String> msExchangeVersaoJComboBox;
    private javax.swing.JLabel multiplicadorJLabel;
    private javax.swing.JSlider multiplicadorJSlider;
    private javax.swing.JLabel nomeColunaJLabel;
    private javax.swing.JTextField nomeColunaJTextField;
    private javax.swing.JLabel nomeContatoJLabel;
    private javax.swing.JTextField nomeContatoJTextField;
    private javax.swing.JLabel nomeLeiauteJLabel;
    private javax.swing.JButton okColunasChaveJButton;
    private javax.swing.JButton okColunasDuplicadoJButton;
    private javax.swing.JButton okCompararJButton;
    private javax.swing.JButton okCrescimentoJButton;
    private javax.swing.JButton okDensidadeJButton;
    private javax.swing.JButton okIncluirPermissoesJButton;
    private javax.swing.JButton okPermissoesJButton;
    private javax.swing.JComboBox parametroPermissoesJComboBox;
    private javax.swing.JLabel parametroPermissoesJLabel;
    private javax.swing.JComboBox parametrosIncluirPermissoesJComboBox;
    private javax.swing.JLabel parametrosIncluirPermissoesJLabel;
    private javax.swing.JTable parametrosPermissoesJTable;
    private javax.swing.JButton pararJButton;
    private javax.swing.JButton pastaRecebidosJButton;
    private javax.swing.JLabel pastaRecebidosJLabel;
    private javax.swing.JTextField pastaRecebidosJTextField;
    private javax.swing.JButton pastaValidadosJButton;
    private javax.swing.JLabel pastaValidadosJLabel;
    private javax.swing.JTextField pastaValidadosJTextField;
    private javax.swing.JButton pastaXMLJButton;
    private javax.swing.JLabel pastaXMLJLabel;
    private javax.swing.JTextField pastaXMLJTextField;
    private javax.swing.JLabel pequisaPermissaoJLabel;
    private javax.swing.JComboBox periodicidadeJComboBox;
    private javax.swing.JLabel periodicidadeJLabel;
    private javax.swing.JTable permisoesJTable;
    private javax.swing.JFrame permissoesJFrame;
    private javax.swing.JMenuItem permissoesJMenuItem;
    private javax.swing.JPanel permissoesJPanel;
    private javax.swing.JComboBox pesquisaPermissoesJComboBox;
    private javax.swing.JFormattedTextField portaIMAPJFormattedTextField;
    private javax.swing.JLabel portaIMAPJLabel;
    private javax.swing.JFormattedTextField portaSMTPJFormattedTextField;
    private javax.swing.JLabel portaSMTPJLabel;
    private javax.swing.JLabel prazoJLabel;
    private javax.swing.JLabel queryJLabel;
    private javax.swing.JTextArea queryJTextArea;
    private javax.swing.JLabel regexJLabel;
    private javax.swing.JTextField regexJTextField;
    private javax.swing.JButton removeChaveUnicaJButton;
    private javax.swing.JButton removeColunaDuplicadoJButton;
    private javax.swing.JButton removeConsolidacaoJButton;
    private javax.swing.JButton removeContatoJButton;
    private javax.swing.JButton removeValidacaoJButton;
    private javax.swing.JTable resultadoComparaJTable;
    private javax.swing.JRadioButton salvarSenhaJRadioButton;
    private javax.swing.JFormattedTextField sasPortJFormattedTextField;
    private javax.swing.JLabel sasPortJLabel;
    private javax.swing.JLabel sasServerJLabel;
    private javax.swing.JTextField sasServerJTextField;
    private javax.swing.JLabel segundosJLabel;
    private javax.swing.JLabel senhaJLabel;
    private javax.swing.JPasswordField senhaJPasswordField;
    private javax.swing.JLabel servidorIMAPJLabel;
    private javax.swing.JTextField servidorIMAPJTextField;
    private javax.swing.JComboBox<String> servidorJComboBox;
    private javax.swing.JLabel servidorMSExchangeJLabel;
    private javax.swing.JTextField servidorMSExchangeJTextField;
    private javax.swing.JLabel servidorSMTPJLabel;
    private javax.swing.JTextField servidorSMTPJTextField;
    private javax.swing.JLabel statusJLabel;
    private javax.swing.JLabel tabelaJLabel;
    private javax.swing.JTextField tabelaJTextField;
    private javax.swing.JLabel tabelaOkJLabel;
    private javax.swing.JLabel tamanhoColunaJLabel;
    private javax.swing.JSlider tamanhoColunaJSlider;
    private javax.swing.JTextField telefoneContatoJTextField;
    private javax.swing.JLabel telefoneJLabel;
    private javax.swing.JButton testarCompararJButton;
    private javax.swing.JToggleButton testarQueryJToggleButton;
    private javax.swing.JComboBox tipoBancoJComboBox;
    private javax.swing.JLabel tipoBancoJLabel;
    private javax.swing.JComboBox tipoColunaJComboBox;
    private javax.swing.JLabel tipoColunaJLabel;
    private javax.swing.JComboBox tipoLeiauteIncluirPermissoesJComboBox;
    private javax.swing.JLabel tipoLeiauteIncluirPermissoesJLabel;
    private javax.swing.JComboBox tipoLeiauteJComboBox;
    private javax.swing.JLabel tipoLeiauteJLabel;
    private javax.swing.JComboBox tipoLeiautePermissoesJComboBox;
    private javax.swing.JLabel tipoLeiautePermissoesJLabel;
    private javax.swing.JComboBox tipoPrazoJComboBox;
    private javax.swing.JComboBox tipoScriptJComboBox;
    private javax.swing.JLabel tipoScriptJLabel;
    private javax.swing.JLabel tipoServidorEmailJLabel;
    private javax.swing.JLabel urlJLabel;
    private javax.swing.JTextField urlJTextField;
    private javax.swing.JLabel usuarioIncluirPermissoesJLabel;
    private javax.swing.JLabel usuarioJLabel;
    private javax.swing.JTextField usuarioJTextField;
    private javax.swing.JLabel usuarioPermissoesJLabel;
    private javax.swing.JList usuarioPermissoesJList;
    private javax.swing.JTextField usuarioPermissoesJTextField;
    private javax.swing.JList validacaoesDisponiveisJList;
    private javax.swing.JLabel validacoesDisponiveisJLabel;
    private javax.swing.JLabel validacoesJLabel;
    private javax.swing.JLabel validacoesRealizadasJLabel;
    private javax.swing.JList validacoesRealizadasJList;
    private javax.swing.JLabel valorIncluirPermissoesJLabel;
    private javax.swing.JTextField valorIncluirPermissoesJTextField;
    private javax.swing.JLabel valorMultiplicadorJLabel;
    private javax.swing.JLabel valorPermissoesJLabel;
    private javax.swing.JTextField valorPermissoesJTextField;
    private javax.swing.JFrame valoresDuplicadosJFrame;
    private javax.swing.JPanel valoresDuplicadosJPanel;
    // End of variables declaration//GEN-END:variables

    private boolean createBanco() {
        try {
            final String HOST = props.getProperty("cassini.sas.host");
            final int PORT = Integer.parseInt(props.getProperty("cassini.sas.port"));
            final String USERNAME = props.getProperty("cassini.usuario.email").split("@")[0];
            final String PASSWORD = props.getProperty("cassini.usuario.pass");

            StringBuilder createBancoScript = new StringBuilder();
            createBancoScript.append("LIBNAME DADOS BASE \"").append(enderecoBancoJTextField.getText()).append("\";").append(System.getProperty("line.separator"));
            createBancoScript.append("DATA DADOS.").append(tabelaJTextField.getText()).append(";").append(System.getProperty("line.separator"));
            createBancoScript.append("	INPUT").append(System.getProperty("line.separator"));
            DefaultTableModel model = (DefaultTableModel) colunasJTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" : ");
                switch (model.getValueAt(i, 3).toString()) {
                    case "Texto":
                        createBancoScript.append("$CHAR").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Tempo (AAAA/MM/DD)":
                        createBancoScript.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por ponto":
                        createBancoScript.append("?? BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por vírgula":
                        createBancoScript.append("?? COMMAX").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Inteiro":
                        createBancoScript.append("?? BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                }
            }
            createBancoScript.append("		arquivo : $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente : $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio : IS8601DT26.").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	FORMAT").append(System.getProperty("line.separator"));
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" ");
                switch (model.getValueAt(i, 3).toString()) {
                    case "Texto":
                        createBancoScript.append("$CHAR").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Tempo (AAAA/MM/DD)":
                        createBancoScript.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por ponto":
                        createBancoScript.append("BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Número separado por vírgula":
                        createBancoScript.append("COMMAX").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "Inteiro":
                        createBancoScript.append("BEST").append(model.getValueAt(i, 4)).append(".").append(System.getProperty("line.separator"));
                        break;
                }
            }
            createBancoScript.append("		arquivo $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente $CHAR100.").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio IS8601DT26.").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	LABEL").append(System.getProperty("line.separator"));
            for (int i = 0; i < model.getRowCount(); i++) {
                createBancoScript.append("		").append(model.getValueAt(i, 0)).append(" =\"").append(model.getValueAt(i, 1)).append("\"").append(System.getProperty("line.separator"));
            }
            createBancoScript.append("		arquivo =\"Nome do Arquivo Recebido\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		remetente =\"Email do remetente do arquivo\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		data_envio =\"Data de envio do arquivo\"").append(System.getProperty("line.separator"));
            createBancoScript.append("		;").append(System.getProperty("line.separator"));

            createBancoScript.append("	DATALINES;").append(System.getProperty("line.separator"));
            createBancoScript.append("	;").append(System.getProperty("line.separator"));
            createBancoScript.append("RUN;").append(System.getProperty("line.separator"));

            ConexaoSAS sasc = new ConexaoSAS();
            sasc.addActionListener(this);
            sasc.conectar(HOST, PORT, USERNAME, PASSWORD);
            Connection conn = sasc.executarProcessFlow(createBancoScript.toString());
            this.actionPerformed(new SASAction(SASAction.DESCONECTAR));
            conn.close();

            return new File(enderecoBancoJTextField.getText() + "\\" + tabelaJTextField.getText() + ".sas7bdat").isFile();

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private DefaultComboBoxModel getDominios() {

        File pastaDominio = new File(pastaXMLJTextField.getText() + "\\dominios\\");

        File[] leiautes = pastaDominio.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().endsWith(".xml")) {
                    return true;
                }
                return false;
            }
        });

        DefaultComboBoxModel modelLeiautes = new DefaultComboBoxModel();

        modelLeiautes.addElement("Não");

        if (leiautes != null) {
            for (int i = 0; i < leiautes.length; i++) {
                try {
                    //URL schemaFile = new URL(coletaXSD);
                    File schemaFile = new File(coletaXSD);
                    Source xmlFile = new StreamSource(leiautes[i]);
                    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                    Schema schema = schemaFactory.newSchema(schemaFile);
                    Validator validator = schema.newValidator();
                    validator.validate(xmlFile);

                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setNamespaceAware(false);
                    DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                    Document doc = docBuilder.parse(leiautes[i]);
                    Element coletaTag = doc.getDocumentElement();
                    Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                    modelLeiautes.addElement(scriptTag.getAttribute("leiaute"));

                } catch (Exception ex) {
                }
            }
        }

        return modelLeiautes;
    }

    @Override
    public void actionPerformed(SASAction e) {
        if (e.getAction() == SASAction.CONECTAR) {
            conexoesSAS++;
        } else if (e.getAction() == SASAction.DESCONECTAR && conexoesSAS > 0) {
            conexoesSAS--;
        } else if (e.getAction() == SASAction.RODAR_SCRIPT) {
            System.out.println("---------------------------------------------");
            StackTraceElement[] ste = e.getStackTraceElement();
            for (StackTraceElement element : ste) {
                System.out.println(element.getMethodName());
            }
            System.out.println(e.getScript());
            System.out.println("---------------------------------------------");
        }
        conexoesSASJLabel.setText("Conexões SAS: " + conexoesSAS);
    }

    class MinhaListModel extends AbstractListModel {

        private String[] strings = {"Chave Única", "Crescimento", "Compara Dados", "Densidade Geográfica", "Média", "Validar CEP", "Validar CNPJ", "Validar Código de Município", "Validar CPF", "Validar Localidade SGMU", "Verificar Cadastro", "Verificar Registros Duplicados", "Verificar Valores em Branco"};
        private List<String> elements = new ArrayList<>(Arrays.asList(strings));

        @Override
        public int getSize() {
            return elements.size();
        }

        @Override
        public Object getElementAt(int index) {
            return elements.get(index);
        }

        public void removeElementAt(int index) {
            elements.remove(index);
        }

        public void addElement(String element) {
            elements.add(element);
        }

        public List<String> getElements() {
            return elements;
        }

        public void removeAllElements() {
            elements.removeAll(elements);
        }
    }

    class Cronograma extends Thread {

        private SASActionListener sasActionListener;

        public Cronograma(SASActionListener sasActionListner) {
            this.sasActionListener = sasActionListner;
        }

        @Override
        public void run() {
            cronogramaJFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            cobrarJButton.setEnabled(false);
            DefaultTableModel model = (DefaultTableModel) cronogramaJTable.getModel();
            while (model.getRowCount() > 0) {
                model.removeRow(0);
            }

            File pastaXML = new File(pastaXMLJTextField.getText());
            if (pastaXML.isDirectory()) {
                File[] leiautes = pastaXML.listFiles(new FileFilter() {

                    @Override
                    public boolean accept(File pathname) {
                        if (pathname.getName().endsWith(".xml")) {
                            return true;
                        }
                        return false;
                    }
                });

                cronogramaJProgressBar.setMaximum(leiautes.length - 1);
                cronogramaJProgressBar.setValue(0);

                boolean permissoesOk = isPermissoes();

                for (int i = 0; i < leiautes.length; i++) {
                    try {
                        //URL schemaFile = new URL(coletaXSD);
                        File schemaFile = new File(coletaXSD);
                        Source xmlFile = new StreamSource(leiautes[i]);
                        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                        Schema schema = schemaFactory.newSchema(schemaFile);
                        Validator validator = schema.newValidator();
                        validator.validate(xmlFile);

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        dbf.setNamespaceAware(false);
                        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
                        Document doc = docBuilder.parse(leiautes[i]);
                        Element coletaTag = doc.getDocumentElement();

                        Element scriptTag = (Element) coletaTag.getElementsByTagName("script").item(0);
                        Element tipoPeriodoTag = (Element) coletaTag.getElementsByTagName("tipoPeriodo").item(0);
                        Element prazoTag = (Element) coletaTag.getElementsByTagName("prazo").item(0);
                        Element tipoPrazoTag = (Element) coletaTag.getElementsByTagName("tipoPrazo").item(0);

                        String leiaute = scriptTag.getAttribute("leiaute");
                        String tipoPeriodo = tipoPeriodoTag.getTextContent();
                        String prazo = prazoTag.getTextContent();
                        String tipoPrazo = tipoPrazoTag.getTextContent().equals("UTEIS") ? "úteis" : "corridos";

                        //le colunas
                        NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");
                        List<Coluna> colunas = new ArrayList<>();

                        Coluna colunaAno = null;
                        Coluna colunaMes = null;
                        Coluna colunaDia = null;
                        Coluna colunaPeriodo = null;

                        for (int j = 0; j < colunasTag.getLength(); j++) {
                            Element colunaTag = (Element) colunasTag.item(j);
                            int tipo = Coluna.TIPO_CHAR;
                            int classe = Coluna.CLASSE_OUTROS;
                            switch (colunaTag.getAttribute("tipo")) {
                                case "NUMERO":
                                    tipo = Coluna.TIPO_NUMERO;
                                    break;
                                case "NUMERO_PONTO":
                                    tipo = Coluna.TIPO_NUMERO_PONTO;
                                    break;
                                case "NUMERO_VIRGULA":
                                    tipo = Coluna.TIPO_NUMERO_VIRGULA;
                                    break;
                                case "CHAR":
                                    tipo = Coluna.TIPO_CHAR;
                                    break;
                                case "TIME":
                                    tipo = Coluna.TIPO_TIME;
                                    break;
                                default:
                                    throw new Exception("Tipo de coluna não reconhecido");

                            }

                            Element dominioTag = (Element) colunaTag.getElementsByTagName("dominio").item(0);

                            switch (colunaTag.getAttribute("classe")) {
                                case "CNPJ_CPF":
                                    classe = Coluna.CLASSE_CNPJ_CPF;
                                    break;
                                case "MUNICIPIO":
                                    classe = Coluna.CLASSE_MUNICIPIO;
                                    break;
                                case "CEP":
                                    classe = Coluna.CLASSE_CEP;
                                    break;
                                case "LOCALIDADE":
                                    classe = Coluna.CLASSE_LOCALIDADE;
                                    break;
                                case "ANO":
                                    classe = Coluna.CLASSE_ANO;
                                    colunaAno = new Coluna(colunaTag.getAttribute("nome"), tipo, Integer.parseInt(colunaTag.getAttribute("tamanho")), Coluna.USO_IGNORAR, classe, (dominioTag != null ? dominioTag.getAttribute("leiaute") : null), (dominioTag != null ? dominioTag.getAttribute("coluna") : null), colunaTag.getAttribute("regex"), colunaTag.getAttribute("atualizacao").equals("SIM"));
                                    break;
                                case "MES":
                                    classe = Coluna.CLASSE_MES;
                                    colunaMes = new Coluna(colunaTag.getAttribute("nome"), tipo, Integer.parseInt(colunaTag.getAttribute("tamanho")), Coluna.USO_IGNORAR, classe, (dominioTag != null ? dominioTag.getAttribute("leiaute") : null), (dominioTag != null ? dominioTag.getAttribute("coluna") : null), colunaTag.getAttribute("regex"), colunaTag.getAttribute("atualizacao").equals("SIM"));
                                    break;
                                case "DIA":
                                    classe = Coluna.CLASSE_DIA;
                                    colunaDia = new Coluna(colunaTag.getAttribute("nome"), tipo, Integer.parseInt(colunaTag.getAttribute("tamanho")), Coluna.USO_IGNORAR, classe, (dominioTag != null ? dominioTag.getAttribute("leiaute") : null), (dominioTag != null ? dominioTag.getAttribute("coluna") : null), colunaTag.getAttribute("regex"), colunaTag.getAttribute("atualizacao").equals("SIM"));
                                    break;
                                case "PERIODO":
                                    classe = Coluna.CLASSE_PERIODO;
                                    colunaPeriodo = new Coluna(colunaTag.getAttribute("nome"), tipo, Integer.parseInt(colunaTag.getAttribute("tamanho")), Coluna.USO_IGNORAR, classe, (dominioTag != null ? dominioTag.getAttribute("leiaute") : null), (dominioTag != null ? dominioTag.getAttribute("coluna") : null), colunaTag.getAttribute("regex"), colunaTag.getAttribute("atualizacao").equals("SIM"));
                                    break;
                                case "OUTROS":
                                    classe = Coluna.CLASSE_OUTROS;
                                    break;
                                case "CN":
                                    classe = Coluna.CLASSE_CN;
                                    break;
                                default:
                                    throw new Exception("Classe de coluna não reconhecido");
                            }
                        }

                        //le banco
                        Element bancoTag = (Element) coletaTag.getElementsByTagName("banco").item(0);
                        int tipo = Banco.TIPO_ARQUIVO_SAS;
                        switch (bancoTag.getAttribute("tipo")) {
                            case "ARQUIVO_SAS":
                                tipo = Banco.TIPO_ARQUIVO_SAS;
                                break;
                            case "BIBLIOTECA_SAS":
                                tipo = Banco.TIPO_BIBLIOTECA_SAS;
                                break;
                            case "BANCO_DE_DADOS":
                                tipo = Banco.TIPO_BANCO_DE_DADOS;
                                break;
                            default:
                                throw new Exception("Tipo de banco não reconhecido");
                        }
                        Banco banco = new Banco(tipo, bancoTag.getAttribute("endereco"), bancoTag.getAttribute("tabela"));

                        StringBuilder scriptSAS = new StringBuilder();

                        if (banco.getTipo() == Banco.TIPO_ARQUIVO_SAS) {
                            scriptSAS.append("LIBNAME historic base \"").append(banco.getEndereco()).append("\";\n\n");
                        } else if (banco.getTipo() == Banco.TIPO_BIBLIOTECA_SAS) {
                            scriptSAS.append("LIBNAME historic META LIBRARY=").append(banco.getEndereco()).append(";\n\n");
                        }

                        scriptSAS.append("PROC SQL;\n");
                        scriptSAS.append("CREATE TABLE WORK.periodos AS\n");
                        scriptSAS.append("SELECT DISTINCT\n");

                        if (colunaPeriodo != null) {
                            scriptSAS.append("t1.").append(colunaPeriodo.getNome()).append(" FORMAT=ptgdfmy7. as hitmonlee,\n");
                        } else if (colunaDia != null && colunaAno != null && colunaMes != null) {
                            scriptSAS.append("mdy(t1.").append(colunaMes.getNome()).append(",t1.").append(colunaDia.getNome()).append(",t1.").append(colunaAno.getNome()).append(") FORMAT=ptgdfmy7. AS hitmonlee\n");
                        } else if (colunaAno != null && colunaMes != null) {
                            scriptSAS.append("mdy(t1.").append(colunaMes.getNome()).append(",1,t1.").append(colunaAno.getNome()).append(") FORMAT=ptgdfmy7. AS hitmonlee\n");
                        } else if (colunaAno != null) {
                            scriptSAS.append("mdy(12,1,t1.").append(colunaAno.getNome()).append(") FORMAT=ptgdfmy7. AS hitmonlee\n");
                        }
                        scriptSAS.append("FROM HISTORIC.").append(banco.getTabela()).append(" t1;\n");
                        scriptSAS.append("QUIT;\n\n");

                        scriptSAS.append("PROC SQL;\n");
                        scriptSAS.append("CREATE TABLE WORK.periodos AS\n");
                        scriptSAS.append("SELECT\n");
                        scriptSAS.append("MAX(t1.hitmonlee) FORMAT=PTGDFMY7. AS hitmonlee\n");
                        scriptSAS.append("FROM WORK.periodos t1;\n");
                        scriptSAS.append("QUIT;\n\n");

                        if (permissoesOk) {
                            scriptSAS.append("PROC SQL;\n");
                            scriptSAS.append("CREATE TABLE WORK.usuarios AS\n");
                            scriptSAS.append("SELECT DISTINCT\n");
                            scriptSAS.append("t1.usuario\n");
                            scriptSAS.append("FROM HISTORIC.PERMISSOES t1\n");
                            scriptSAS.append("WHERE t1.leiaute = '").append(leiaute).append("';\n");
                            scriptSAS.append("QUIT;\n");
                        }

                        String HOST = props.getProperty("cassini.sas.host");
                        int PORT = Integer.parseInt(props.getProperty("cassini.sas.port"));
                        String USERNAME = props.getProperty("cassini.usuario.email").split("@")[0];
                        String PASSWORD = props.getProperty("cassini.usuario.pass");

                        ConexaoSAS sasc = new ConexaoSAS();
                        sasc.addActionListener(sasActionListener);
                        sasc.conectar(HOST, PORT, USERNAME, PASSWORD);
                        Connection conn = sasc.executarProcessFlow(scriptSAS.toString());
                        sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
                        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM periodos");
                        long ultimaColetaInMillis = 0;
                        while (rs.next()) {
                            Date date = rs.getDate("hitmonlee");
                            if (date != null) {
                                ultimaColetaInMillis = date.getTime();
                            }
                        }

                        rs = conn.createStatement().executeQuery("SELECT * FROM usuarios");
                        StringBuilder usuarios = new StringBuilder();
                        while (rs.next()) {
                            usuarios.append(rs.getString("usuario")).append(";");
                        }
                        conn.close();

                        Calendar ultimaColeta = Calendar.getInstance();
                        ultimaColeta.setTimeInMillis(ultimaColetaInMillis);

                        Calendar proximaColeta = Calendar.getInstance();
                        proximaColeta.setTimeInMillis(ultimaColetaInMillis);
                        int month = 0;

                        switch (tipoPeriodo) {
                            case "MENSAL":
                                proximaColeta.add(Calendar.MONTH, 1);
                                break;
                            case "TRIMESTRAL":
                                month = (ultimaColeta.get(Calendar.MONTH) + 1) % 12;
                                proximaColeta.add(Calendar.MONTH, 1);
                                while ((month + 1) % 3 != 0) {
                                    month++;
                                    proximaColeta.add(Calendar.MONTH, 1);
                                }
                                break;
                            case "SEMESTRAL":
                                month = (ultimaColeta.get(Calendar.MONTH) + 1) % 12;
                                proximaColeta.add(Calendar.MONTH, 1);
                                while ((month + 1) % 6 != 0) {
                                    month++;
                                    proximaColeta.add(Calendar.MONTH, 1);
                                }
                                break;
                            case "ANUAL":
                                proximaColeta.add(Calendar.YEAR, 1);
                                break;
                            case "EVENTUAL":
                                proximaColeta.add(Calendar.YEAR, 1);
                                break;
                            default:
                                throw new Exception("valor da tag tipoPeriodo inválido");
                        }

                        Calendar prazoFinal = Calendar.getInstance();
                        prazoFinal.setTimeInMillis(proximaColeta.getTimeInMillis());
                        prazoFinal.add(Calendar.MONTH, 1);
                        int prazoInt = Integer.parseInt(prazo);
                        for (int z = 0; z < prazoInt; z++) {
                            int dayOfWeek = prazoFinal.get(Calendar.DAY_OF_WEEK);
                            if (dayOfWeek == Calendar.FRIDAY) {
                                prazoFinal.add(Calendar.DAY_OF_MONTH, 3);
                            } else {
                                prazoFinal.add(Calendar.DAY_OF_MONTH, 1);
                            }
                        }
                        Calendar hoje = Calendar.getInstance();
                        boolean atrasado = !tipoPeriodo.equals("EVENTUAL") ? hoje.compareTo(prazoFinal) > 0 : false;

                        model.addRow(new Object[]{leiaute, tipoPeriodo, prazo + " dias " + tipoPrazo, ultimaColeta.get(Calendar.YEAR) == 1969 ? "Nunca Enviado" : ultimaColeta.get(Calendar.MONTH) + 1 + "/" + ultimaColeta.get(Calendar.YEAR), ultimaColeta.get(Calendar.YEAR) == 1969 ? "Nunca Enviado" : proximaColeta.get(Calendar.MONTH) + 1 + "/" + proximaColeta.get(Calendar.YEAR), atrasado, usuarios.toString()});

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    cronogramaJProgressBar.setValue(i);
                }

            }
            cronogramaJFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            cobrarJButton.setEnabled(true);
        }

    }

}

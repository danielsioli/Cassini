/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sas;

import com.sas.iom.SAS.ILanguageServicePackage.LineType;
import static gerador.GeradorScriptSAS.isEncodingOk;
import gerador.sas.ConexaoSAS;
import gerador.sas.SASAction;
import gerador.sas.SASActionListener;
import gerador.sas.SASException;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author danieloliveira
 */
public class SASInterface {

    private String user;
    private String password;
    private String sasServer;
    private int sasPort;
    private SASActionListener sasActionListener;

    /**
     *
     * @param user
     * @param password
     * @param sasServer
     * @param sasPort
     */
    public SASInterface(String user, String password, String sasServer, int sasPort) {
        this.user = user;
        this.password = password;
        this.sasServer = sasServer;
        this.sasPort = sasPort;
    }

    public void addSASActionListener(SASActionListener sasActionListener) {
        this.sasActionListener = sasActionListener;
    }

    /**
     *
     * @param coletaTag
     * @param file
     * @param from
     * @param dataSent
     * @return
     */
    private String iniciarScript(Element coletaTag, String file, String from, Date dataSent) throws Exception {
        StringBuilder script = new StringBuilder();
        Element bancoTag = (Element) coletaTag.getElementsByTagName("banco").item(0);
        NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");

        File in = new File(file);

        ByteBuffer byteBuffer = ByteBuffer.wrap(Files.readAllBytes(Paths.get(file)));
        String encoding = "UTF8";
        if (isEncodingOk(byteBuffer, "UTF8")) {
            encoding = "UTF8";
        } else if (isEncodingOk(byteBuffer, "Cp1252")) {
            encoding = "ANSI";
        } else {
            throw new Exception("O arquivo de dados deve ter codificação UTF-8 ou ANSI para ser lido pelo Cassini.");
        }

        if (bancoTag.getAttribute("tipo").equals("ARQUIVO_SAS")) {
            script.append("LIBNAME historic BASE \"").append(bancoTag.getAttribute("endereco")).append("\";").append(System.getProperty("line.separator"));
            script.append("data arquivo;").append(System.getProperty("line.separator"));
            script.append("length").append(System.getProperty("line.separator"));
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                script.append("").append(colunaTag.getAttribute("nome"));
                script.append(" ");
                switch (colunaTag.getAttribute("tipo")) {
                    case "CHAR":
                        script.append("$");
                        script.append(colunaTag.getAttribute("tamanho")).append(System.getProperty("line.separator"));
                        break;
                    default:
                        if (Integer.parseInt(colunaTag.getAttribute("tamanho")) < 3 || Integer.parseInt(colunaTag.getAttribute("tamanho")) > 8) {
                            script.append("8").append(System.getProperty("line.separator"));
                        } else {
                            script.append(colunaTag.getAttribute("tamanho")).append(System.getProperty("line.separator"));
                        }
                        break;
                }
            }
            /*script.append("arquivo $100").append(System.getProperty("line.separator"));
             script.append("remetente $100").append(System.getProperty("line.separator"));
             script.append("data_envio $19").append(System.getProperty("line.separator"));*/

            script.append(System.getProperty("line.separator"));
            script.append(";").append(System.getProperty("line.separator"));
            script.append(System.getProperty("line.separator"));

            script.append("format").append(System.getProperty("line.separator"));
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                script.append(colunaTag.getAttribute("nome"));
                script.append(" ");
                switch (colunaTag.getAttribute("tipo")) {
                    case "CHAR":
                        script.append("$CHAR").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "TIME":
                        script.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                    default:
                        script.append("BEST").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                }
            }
            /*script.append("arquivo $CHAR100.").append(System.getProperty("line.separator"));
             script.append("remetente $CHAR100.").append(System.getProperty("line.separator"));
             script.append("data_envio ANYDTDTM19.").append(System.getProperty("line.separator"));*/

            script.append(";").append(System.getProperty("line.separator"));
            script.append(System.getProperty("line.separator"));
            script.append("infile '").append(file).append("'").append(System.getProperty("line.separator"));
            script.append("lrecl=1048576").append(System.getProperty("line.separator"));
            script.append("ENCODING=\"").append(encoding).append("\"").append(System.getProperty("line.separator"));
            script.append("firstobs=2").append(System.getProperty("line.separator"));
            script.append("DELIMITER=\';\'").append(System.getProperty("line.separator"));
            script.append("MISSOVER").append(System.getProperty("line.separator"));
            script.append("DSD;").append(System.getProperty("line.separator"));
            script.append("input").append(System.getProperty("line.separator"));
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                script.append("").append(colunaTag.getAttribute("nome"));
                script.append(" : ?? ");
                switch (colunaTag.getAttribute("tipo")) {
                    case "CHAR":
                        script.append("$CHAR").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "NUMERO":
                        script.append("BEST").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "NUMERO_PONTO":
                        script.append("COMMA").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "NUMERO_VIRGULA":
                        script.append("COMMAX").append(colunaTag.getAttribute("tamanho")).append(".").append(System.getProperty("line.separator"));
                        break;
                    case "TIME":
                        script.append("PTGDFMY7.").append(System.getProperty("line.separator"));
                        break;
                }
            }
            /*script.append("arquivo : ?? $CHAR100.").append(System.getProperty("line.separator"));
             script.append("remetente : ?? $CHAR100.").append(System.getProperty("line.separator"));
             script.append("data_envio : ?? ANYDTDTM19.").append(System.getProperty("line.separator"));*/
            script.append(";").append(System.getProperty("line.separator"));
            script.append("run;").append(System.getProperty("line.separator"));

            script.append("PROC SQL;").append(System.getProperty("line.separator"));
            script.append("CREATE TABLE arquivo AS").append(System.getProperty("line.separator"));
            script.append("SELECT *,").append(System.getProperty("line.separator"));
            script.append("'").append(in.getName()).append("'").append(" format=$CHAR100. as arquivo,").append(System.getProperty("line.separator"));
            script.append("'").append(from).append("'").append(" format=$CHAR100. as remetente,").append(System.getProperty("line.separator"));
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dataSent);
            String data_envio = "'" + calendar.get(Calendar.DAY_OF_MONTH);
            switch (calendar.get(Calendar.MONTH)) {
                case Calendar.JANUARY:
                    data_envio = data_envio + "Jan" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.FEBRUARY:
                    data_envio = data_envio + "Feb" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.MARCH:
                    data_envio = data_envio + "Mar" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.APRIL:
                    data_envio = data_envio + "Apr" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.MAY:
                    data_envio = data_envio + "May" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.JUNE:
                    data_envio = data_envio + "Jun" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.JULY:
                    data_envio = data_envio + "Jul" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.AUGUST:
                    data_envio = data_envio + "Aug" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.SEPTEMBER:
                    data_envio = data_envio + "Sep" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.OCTOBER:
                    data_envio = data_envio + "Oct" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.NOVEMBER:
                    data_envio = data_envio + "Nov" + calendar.get(Calendar.YEAR) + "'d";
                    break;
                case Calendar.DECEMBER:
                    data_envio = data_envio + "Dec" + calendar.get(Calendar.YEAR) + "'d";
                    break;
            }
            script.append("DHMS(").append(data_envio).append(",").append(calendar.get(Calendar.HOUR_OF_DAY)).append(",").append(calendar.get(Calendar.MINUTE)).append(",").append(calendar.get(Calendar.SECOND)).append(") format=DATETIME18. as data_envio").append(System.getProperty("line.separator"));
            script.append("FROM arquivo;").append(System.getProperty("line.separator"));
            script.append("quit;").append(System.getProperty("line.separator"));
            script.append(System.getProperty("line.separator"));

            script.append("data arquivo (reuse=yes compress=yes);").append(System.getProperty("line.separator"));
            script.append("set arquivo;").append(System.getProperty("line.separator"));
            script.append("run;").append(System.getProperty("line.separator"));
            script.append(System.getProperty("line.separator"));

        } else {
            //TODO implementar demais bacos
        }
        return script.toString();
    }

    /**
     *
     * @param coletaTag
     * @param file
     * @param from
     * @param dataSent
     * @return
     * @throws SASException
     * @throws SQLException
     */
    public boolean insertDados(Element coletaTag, String file, String from, Date dataSent) throws Exception {
        StringBuilder script = new StringBuilder();
        Element bancoTag = (Element) coletaTag.getElementsByTagName("banco").item(0);
        NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");

        script.append(iniciarScript(coletaTag, file, from, dataSent));

        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_antigo as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table historic.").append(bancoTag.getAttribute("tabela")).append(" as").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        script.append("from arquivo").append(System.getProperty("line.separator"));
        script.append("union all").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(";").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_arquivo as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from arquivo t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_total as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        /*script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table dados_atualizados as").append(System.getProperty("line.separator"));
         script.append("select *").append(System.getProperty("line.separator"));
         script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1").append(System.getProperty("line.separator"));
         script.append("left join arquivo t2 on ").append(System.getProperty("line.separator"));
         StringBuilder stringBuilder = new StringBuilder();
         for (int i = 0; i < colunasTag.getLength(); i++) {
         Element colunaTag = (Element) colunasTag.item(i);
         stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
         }
         stringBuilder.append("t1.arquivo = t2.arquivo and t1.remetente = t2.remetente and t1.data_envio = t2.data_envio and ").append(System.getProperty("line.separator"));
         script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));
        
         script.append("where ").append(System.getProperty("line.separator"));
         stringBuilder = new StringBuilder();
         for (int i = 0; i < colunasTag.getLength(); i++) {
         Element colunaTag = (Element) colunasTag.item(i);
         stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
         }
         stringBuilder.append("t1.arquivo = t2.arquivo and t1.remetente = t2.remetente and t1.data_envio = t2.data_envio and ").append(System.getProperty("line.separator"));
         script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));
         script.append(";").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));
        
         script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table linhas_atualizado as").append(System.getProperty("line.separator"));
         script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
         script.append("from dados_atualizados t1;").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));
        
         script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table linhas_arquivo as").append(System.getProperty("line.separator"));
         script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
         script.append("from arquivo t1;").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));*/
        script.append("PROC SQL; DROP TABLE arquivo;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        script.append("data historic.").append(bancoTag.getAttribute("tabela")).append(" (reuse=yes compress=yes);").append(System.getProperty("line.separator"));
        script.append("set historic.").append(bancoTag.getAttribute("tabela")).append(";").append(System.getProperty("line.separator"));
        script.append("run;").append(System.getProperty("line.separator"));

        ConexaoSAS sasc = new ConexaoSAS();
        sasc.addActionListener(sasActionListener);
        sasc.conectar(sasServer, sasPort, user, password);
        Connection conn = sasc.executarProcessFlow(script.toString());

        LineType[] lineTypes = sasc.getLogLineTypes();
        String[] log = sasc.getLog();
        for (int i = 0; i < lineTypes.length; i++) {
            LineType lineType = lineTypes[i];
            if (lineType.value() == LineType._LineTypeError || log[i].startsWith("ERROR:")) {
                conn.close();
                sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
                throw new SASException(log[i]);
            }
        }

        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM linhas_arquivo");
        int linhasArquivo = 0;
        while (rs.next()) {
            linhasArquivo = rs.getInt("linhas");
        }
        rs = conn.createStatement().executeQuery("SELECT * FROM linhas_antigo");
        int linhasAntigo = 0;
        while (rs.next()) {
            linhasAntigo = rs.getInt("linhas");
        }
        rs = conn.createStatement().executeQuery("SELECT * FROM linhas_total");
        int linhasTotal = 0;
        while (rs.next()) {
            linhasTotal = rs.getInt("linhas");
        }
        conn.close();
        conn = sasc.executarProcessFlow("PROC SQL; DROP TABLE linhas_atualizado;QUIT;PROC SQL; DROP TABLE linhas_arquivo;QUIT;");
        conn.close();
        sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
        return linhasTotal == linhasAntigo + linhasArquivo;
    }

    /**
     *
     * @param coletaTag
     * @param file
     * @param from
     * @return true se o arquivo SAS foi devidamente atualizado
     * @throws SASException
     * @throws SQLException
     */
    public boolean atualizarDados(Element coletaTag, String file, String from, Date dataSent) throws Exception {
        StringBuilder script = new StringBuilder();
        Element bancoTag = (Element) coletaTag.getElementsByTagName("banco").item(0);
        NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");

        script.append(iniciarScript(coletaTag, file, from, dataSent));
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table provedora_periodo as").append(System.getProperty("line.separator"));
        script.append("select").append(System.getProperty("line.separator"));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < colunasTag.getLength(); i++) {
            Element colunaTag = (Element) colunasTag.item(i);
            switch (colunaTag.getAttribute("atualizacao")) {
                case "SIM":
                    stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(",").append(System.getProperty("line.separator"));
                    break;
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                switch (colunaTag.getAttribute("classe")) {
                    case "CNPJ_CPF":
                    case "ANO":
                    case "MES":
                    case "DIA":
                    case "PERIODO":
                        stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(",").append(System.getProperty("line.separator"));
                        break;
                }
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            script.append("*").append(System.getProperty("line.separator"));
        } else {
            script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 3)).append(System.getProperty("line.separator"));
        }
        script.append("from arquivo t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table dados_antigos as").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1").append(System.getProperty("line.separator"));
        script.append("left join arquivo t2 on ").append(System.getProperty("line.separator"));
        stringBuilder = new StringBuilder();
        for (int i = 0; i < colunasTag.getLength(); i++) {
            Element colunaTag = (Element) colunasTag.item(i);
            switch (colunaTag.getAttribute("atualizacao")) {
                case "SIM":
                    stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
                    break;
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                switch (colunaTag.getAttribute("classe")) {
                    case "CNPJ_CPF":
                    case "ANO":
                    case "MES":
                    case "DIA":
                    case "PERIODO":
                        stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
                        break;
                }
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
            }
        }

        script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));

        script.append("where ").append(System.getProperty("line.separator"));
        stringBuilder = new StringBuilder();
        for (int i = 0; i < colunasTag.getLength(); i++) {
            Element colunaTag = (Element) colunasTag.item(i);
            switch (colunaTag.getAttribute("atualizacao")) {
                case "SIM":
                    stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" not = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
                    break;
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                switch (colunaTag.getAttribute("classe")) {
                    case "CNPJ_CPF":
                    case "ANO":
                    case "MES":
                    case "DIA":
                    case "PERIODO":
                        stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" not = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
                        break;
                }
            }
        }
        if (stringBuilder.toString().isEmpty()) {
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" not = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
            }
        }
        script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));
        script.append(";").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table historic.").append(bancoTag.getAttribute("tabela")).append(" as").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        script.append("from dados_antigos").append(System.getProperty("line.separator"));
        script.append("union all").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        script.append("from arquivo;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_antigo as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from dados_antigos t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_arquivo as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from arquivo t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table linhas_total as").append(System.getProperty("line.separator"));
        script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
        script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));

        /*script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table dados_atualizados as").append(System.getProperty("line.separator"));
         script.append("select *").append(System.getProperty("line.separator"));
         script.append("from historic.").append(bancoTag.getAttribute("tabela")).append(" t1").append(System.getProperty("line.separator"));
         script.append("left join arquivo t2 on ").append(System.getProperty("line.separator"));
         stringBuilder = new StringBuilder();
         for (int i = 0; i < colunasTag.getLength(); i++) {
         Element colunaTag = (Element) colunasTag.item(i);
         switch (colunaTag.getAttribute("classe")) {
         case "CNPJ_CPF":
         case "ANO":
         case "MES":
         case "DIA":
         case "PERIODO":
         stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
         break;
         }
         }
         stringBuilder.append("t1.arquivo = t2.arquivo and t1.remetente = t2.remetente and t1.data_envio = t2.data_envio and ").append(System.getProperty("line.separator"));
         script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));
        
         script.append("where ").append(System.getProperty("line.separator"));
         stringBuilder = new StringBuilder();
         for (int i = 0; i < colunasTag.getLength(); i++) {
         Element colunaTag = (Element) colunasTag.item(i);
         switch (colunaTag.getAttribute("classe")) {
         case "CNPJ_CPF":
         case "ANO":
         case "MES":
         case "DIA":
         case "PERIODO":
         stringBuilder.append("t1.").append(colunaTag.getAttribute("nome")).append(" = t2.").append(colunaTag.getAttribute("nome")).append(" and ").append(System.getProperty("line.separator"));
         break;
         }
         }
         stringBuilder.append("t1.arquivo = t2.arquivo and t1.remetente = t2.remetente and t1.data_envio = t2.data_envio and ").append(System.getProperty("line.separator"));
         script.append(stringBuilder.toString().subSequence(0, stringBuilder.toString().length() - 7)).append(System.getProperty("line.separator"));
         script.append(";").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));
        
         script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table linhas_atualizado as").append(System.getProperty("line.separator"));
         script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
         script.append("from dados_atualizados t1;").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));
        
         script.append("proc sql;").append(System.getProperty("line.separator"));
         script.append("create table linhas_arquivo as").append(System.getProperty("line.separator"));
         script.append("select count(1) as linhas").append(System.getProperty("line.separator"));
         script.append("from arquivo t1;").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));*/
        script.append("PROC SQL; DROP TABLE provedora_periodo;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        script.append("PROC SQL; DROP TABLE dados_antigos;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        script.append("PROC SQL; DROP TABLE arquivo;").append(System.getProperty("line.separator"));
        script.append("quit;").append(System.getProperty("line.separator"));
        /*script.append("PROC SQL; DROP TABLE dados_atualizados;").append(System.getProperty("line.separator"));
         script.append("quit;").append(System.getProperty("line.separator"));*/

        script.append("data historic.").append(bancoTag.getAttribute("tabela")).append(" (reuse=yes compress=yes);").append(System.getProperty("line.separator"));
        script.append("set historic.").append(bancoTag.getAttribute("tabela")).append(";").append(System.getProperty("line.separator"));
        script.append("run;").append(System.getProperty("line.separator"));

        ConexaoSAS sasc = new ConexaoSAS();
        sasc.addActionListener(sasActionListener);
        sasc.conectar(sasServer, sasPort, user, password);
        Connection conn = sasc.executarProcessFlow(script.toString());

        LineType[] lineTypes = sasc.getLogLineTypes();
        String[] log = sasc.getLog();
        for (int i = 0; i < lineTypes.length; i++) {
            LineType lineType = lineTypes[i];
            if (lineType.value() == LineType._LineTypeError || log[i].startsWith("ERROR:")) {
                conn.close();
                sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
                throw new SASException(log[i]);
            }
        }

        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM linhas_arquivo");
        int linhasArquivo = 0;
        while (rs.next()) {
            linhasArquivo = rs.getInt("linhas");
        }
        rs = conn.createStatement().executeQuery("SELECT * FROM linhas_antigo");
        int linhasAntigo = 0;
        while (rs.next()) {
            linhasAntigo = rs.getInt("linhas");
        }
        rs = conn.createStatement().executeQuery("SELECT * FROM linhas_total");
        int linhasTotal = 0;
        while (rs.next()) {
            linhasTotal = rs.getInt("linhas");
        }
        conn.close();
        conn = sasc.executarProcessFlow("PROC SQL; DROP TABLE linhas_atualizado;QUIT;PROC SQL; DROP TABLE linhas_arquivo;QUIT;");
        conn.close();
        sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
        //return linhasArquivo != 0 && linhasAtualizado != 0 && linhasArquivo == linhasAtualizado;
        return linhasTotal == linhasAntigo + linhasArquivo;
    }

    /**
     *
     * @param coletaTag
     * @return
     * @throws SASException
     * @throws SQLException
     * @throws SASException
     */
    public List<List<String>> getDados(Element coletaTag) throws SASException, SQLException, SASException {
        List<List<String>> linhas = new ArrayList();
        StringBuilder script = new StringBuilder();
        Element bancoTag = (Element) coletaTag.getElementsByTagName("banco").item(0);
        NodeList colunasTag = ((Element) coletaTag.getElementsByTagName("colunas").item(0)).getElementsByTagName("coluna");

        if (bancoTag.getAttribute("tipo").equals("ARQUIVO_SAS")) {
            script.append("LIBNAME dados BASE \"").append(bancoTag.getAttribute("endereco")).append("\";").append(System.getProperty("line.separator"));
        } else if (bancoTag.getAttribute("tipo").equals("BIBLIOTECA_SAS")) {
            script.append("LIBNAME dados meta LIBRARY=").append(bancoTag.getAttribute("endereco")).append(";").append(System.getProperty("line.separator"));
            script.append("LIBNAME ").append(bancoTag.getAttribute("endereco")).append(" meta LIBRARY=").append(bancoTag.getAttribute("endereco")).append(";").append(System.getProperty("line.separator"));
        } else {
            //TODO implementar outros tipos de bibliotecas.
        }
        script.append("proc sql;").append(System.getProperty("line.separator"));
        script.append("create table dados as").append(System.getProperty("line.separator"));
        script.append("select *").append(System.getProperty("line.separator"));
        Element queryTag = (Element) bancoTag.getElementsByTagName("query").item(0);
        if (queryTag != null && !queryTag.getTextContent().isEmpty()) {
            String query = queryTag.getTextContent();
            /*query = query.replaceAll("&#39;","'");
             query = query.replaceAll("&#34;","\"");
             query = query.replaceAll("&lt;","<");
             query = query.replaceAll("&gt;",">");
             query = query.replaceAll("&#38;","&");*/
            script.append("from (").append(query).append(");").append(System.getProperty("line.separator"));
        } else {
            script.append("from dados.").append(bancoTag.getAttribute("tabela")).append(";").append(System.getProperty("line.separator"));
        }
        script.append("quit;").append(System.getProperty("line.separator"));

        ConexaoSAS sasc = new ConexaoSAS();
        sasc.addActionListener(sasActionListener);
        sasc.conectar(sasServer, sasPort, user, password);
        Connection conn = sasc.executarProcessFlow(script.toString());
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM DADOS");
        while (rs.next()) {
            List<String> linha = new ArrayList();
            for (int i = 0; i < colunasTag.getLength(); i++) {
                Element colunaTag = (Element) colunasTag.item(i);
                switch (colunaTag.getAttribute("tipo")) {
                    case "NUMERO":
                        linha.add(rs.getInt(colunaTag.getAttribute("nome")) + "");
                        break;
                    case "NUMERO_PONTO":
                    case "NUMERO_VIRGULA":
                        linha.add(rs.getDouble(colunaTag.getAttribute("nome")) + "");
                        break;
                    case "CHAR":
                    case "TIME":
                        linha.add(rs.getString(colunaTag.getAttribute("nome")).trim());
                        break;
                }
            }
            System.err.println();
            linhas.add(linha);
        }
        conn.close();
        conn = sasc.executarProcessFlow("PROC SQL; DROP TABLE work.dados;QUIT;");
        conn.close();
        sasActionListener.actionPerformed(new SASAction(SASAction.DESCONECTAR));
        return linhas;
    }

    /**
     *
     * @param coletaTag
     * @return
     */
    public boolean createBanco(Element coletaTag) {
        boolean bancoCriado = false;

        return bancoCriado;
    }

}

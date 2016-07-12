/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.cliente;

import br.uff.redes.segmento.JanelaEnvio;
import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.ArraySpliter;
import br.uff.redes.tools.FileConverter;
import br.uff.redes.tools.ObjectConverter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;

public class ClienteEmuladoTCP {

    public static final Integer SOLICITANDO_CONEXAO = 0;
    public static final Integer CONEXAO_ACEITA = 5;
    public static final Integer CONECTADO = 3;
    public static final Integer SOLICITANDO_FECHAMENTO = 1;
    public static final Integer CONFIRMANDO_FECHAMENTO = 6;
    public static final Integer ENVIANDO_ARQUIVO = 2;
    public static final Integer DESCONECTADO = 4;

    private volatile Integer numeroSequenciaInicialCliente;
    private volatile Integer numeroSequenciaInicialServidor;

    private volatile Integer estado;
    private volatile JanelaEnvio janela;

    private final String ipDest;
    private final Integer portaDest;
    private final String ipOrig;
    private final Integer portaOrig;

    private final DatagramSocket clienteSocket;
    private volatile List<SegmentoTCP> pacotes;

    public ClienteEmuladoTCP(String ipDest, int portaDest, int portaOrig) throws SocketException, UnknownHostException {
        this.ipDest = ipDest;
        this.ipOrig = InetAddress.getLocalHost().getHostName();
        this.portaDest = portaDest;
        this.portaOrig = portaOrig;
        this.clienteSocket = new DatagramSocket(portaOrig);
        estado = DESCONECTADO;
        new Thread(new Controlador()).start();
    }

    public class Controlador implements Runnable {

        @Override
        public void run() {
            boolean continua = true;
            Scanner teclado = new Scanner(System.in);
            while (continua) {

                System.out.println("digite S para desconectar, O para abrir conexao, A para enviar arquivo E para ver o estado");
                String opcao = teclado.nextLine().toUpperCase();
                if (opcao.equals("S")) {
                    try {
                        solicitarFechamentoConexao();
                    } catch (IOException ex) {
                        Logger.getLogger(ClienteEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (estado == CONECTADO && opcao.equals("A")) {
                    File file = getArquivo();
                    try {
                        pacotes = getSegmentos(file);
                        janela = new JanelaEnvio(pacotes, 5);
                        estado = ENVIANDO_ARQUIVO;
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(ClienteEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else if (estado == DESCONECTADO && opcao.endsWith("O")) {
                    new Thread(new EnviadorDePacotes()).start();
                    new Thread(new RecebidorDeACKRunnable()).start();
                } else if (opcao.equals("E")) {
                    System.out.println("estado: " + estado);
                }
            }
        }

        private List<SegmentoTCP> getSegmentos(File file) throws FileNotFoundException {

            byte[] todos = FileConverter.convertFileToArray(file);
            System.out.println("arquivo possui " + todos.length + " bytes");
            int tamanho = 1000;
            List<byte[]> partes = ArraySpliter.split(todos, tamanho);
            System.out.println("arquivo foi divido em " + partes.size() + " partes");
            int seq = numeroSequenciaInicialCliente + 2;

            List<SegmentoTCP> segmentos = new ArrayList<>();
            //primeiro segmento vai apenas com informações do arquivo
            SegmentoTCP novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
            novo.setSeq(seq);
            byte[] informacoes = ObjectConverter.convertObjectToByteArray(file.getName() + "#" + todos.length + "#" + tamanho);
            novo.setPacote(informacoes);
            segmentos.add(novo);
            seq = seq + informacoes.length;
            for (byte[] parte : partes) {
                novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
                novo.setSeq(seq);
                novo.setPacote(parte);
                segmentos.add(novo);
                seq = seq + parte.length;
            }
           System.out.print("seq"+0+":"+segmentos.get(0).getSeq()+" ");
           System.out.print("seq"+(segmentos.size()-1)+":"+segmentos.get(segmentos.size()-1).getSeq()+" ");
            System.out.println("");
            return segmentos;
        }

        public File getArquivo() {
            File arquivo = null;
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Escolha o arquivo...");
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setApproveButtonText("OK");
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setMultiSelectionEnabled(false);
            int resultado = fc.showOpenDialog(fc);
            if (resultado == JFileChooser.CANCEL_OPTION) {
                System.exit(1);
            }
            arquivo = fc.getSelectedFile();
            System.out.println("NOME: " + arquivo.getName());
            return arquivo;
        }

        public void solicitarFechamentoConexao() throws UnknownHostException, IOException {
            estado = CONECTADO;
            clienteSocket.send(pacotePedidoFechamentoConexao());
        }

        public DatagramPacket pacotePedidoFechamentoConexao() throws UnknownHostException {
            byte[] sendData = new byte[2048];
            InetAddress IPAddress = InetAddress.getByName(ipDest);
            SegmentoTCP novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
            novo.setFIN((byte) 1);
            novo.setSeq(numeroSequenciaInicialCliente);
            sendData = ObjectConverter.convertObjectToByteArray(novo);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDest);
            return sendPacket;
        }
    }

    public class EnviadorDePacotes implements Runnable {

        @Override
        public void run() {
            boolean continua = true;
            while (continua) {
                try {
                    if (estado == DESCONECTADO) {
                        solicitarAberturaConexao();
                        System.out.println("solicitou abertura de conexão");
                    } else if (estado == CONEXAO_ACEITA) {
                        confirmarAberturaConexao();
                        System.out.println("confirmou abertura de conexão");
                    } else if (estado == ENVIANDO_ARQUIVO) {
                        int seq = janela.proximoEnvio();
                        if(seq != -1)
                        for (SegmentoTCP tcp : pacotes) {
                            if (tcp.getSeq() == seq) {
                                System.out.println("enviando o segmento de numero " + seq);
                                enviarPacote(tcp);
                            }
                        }
                    } else if (estado == CONFIRMANDO_FECHAMENTO) {
                        confirmarFechamentoConexao();
                        System.out.println("confirmou fechamento de conexão");
                    }
                } catch (Exception ex) {
                    Logger.getLogger(ClienteEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // metodos de mudança de estado
        public void solicitarAberturaConexao() throws UnknownHostException, IOException {
            estado = SOLICITANDO_CONEXAO;
            numeroSequenciaInicialCliente = new Random().nextInt(9999);
            clienteSocket.send(pacotePedidoConexao());
        }

        public void confirmarAberturaConexao() throws UnknownHostException, IOException {
            estado = CONECTADO;
            clienteSocket.send(pacoteConfirmaConexao());
        }

        public void confirmarFechamentoConexao() throws UnknownHostException, IOException {
            estado = DESCONECTADO;
            clienteSocket.send(pacoteConfirmaFechamentoConexao());
        }

        public void enviarPacote(SegmentoTCP tcp) throws UnknownHostException, IOException {
            clienteSocket.send(pacoteEnvioArquivo(tcp));
        }

        //metodos para gerar os pacotes UDP
        public DatagramPacket pacotePedidoConexao() throws UnknownHostException {
            byte[] sendData = new byte[2048];
            //pegar endereço do servidor
            InetAddress IPAddress = InetAddress.getByName(ipDest);
            //envia pedido de conexão
            SegmentoTCP novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
            novo.setSYN((byte) 1);
            novo.setSeq(numeroSequenciaInicialCliente);
            sendData = ObjectConverter.convertObjectToByteArray(novo);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDest);
            return sendPacket;
        }

        public DatagramPacket pacoteConfirmaConexao() throws UnknownHostException {
            byte[] sendData = new byte[2048];
            //pegar endereço do servidor
            InetAddress IPAddress = InetAddress.getByName(ipDest);
            //envia pedido de conexão
            SegmentoTCP novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
            novo.setACKbit((byte) 1);
            novo.setACKnum(numeroSequenciaInicialServidor + 1);
            sendData = ObjectConverter.convertObjectToByteArray(novo);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDest);
            return sendPacket;
        }

        public DatagramPacket pacoteEnvioArquivo(SegmentoTCP tcp) throws UnknownHostException {
            byte[] sendData = new byte[2048];
            sendData = ObjectConverter.convertObjectToByteArray(tcp);
            InetAddress IPAddress = InetAddress.getByName(ipDest);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDest);
            return sendPacket;
        }

        public DatagramPacket pacoteConfirmaFechamentoConexao() throws UnknownHostException {
            byte[] sendData = new byte[2048];
            InetAddress IPAddress = InetAddress.getByName(ipDest);
            SegmentoTCP novo = new SegmentoTCP(ipOrig, portaOrig, ipDest, portaDest);
            novo.setACKbit((byte) 1);
            novo.setACKnum(numeroSequenciaInicialServidor + 1);
            sendData = ObjectConverter.convertObjectToByteArray(novo);
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDest);
            return sendPacket;
        }
    }

    public class RecebidorDeACKRunnable implements Runnable {

        @Override
        public void run() {
            boolean continua = true;

            while (continua) {
                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    clienteSocket.receive(receivePacket);
                    SegmentoTCP seg = (SegmentoTCP) ObjectConverter.convertByteArrayToObject(receivePacket.getData());
                    if (confirmaAbrirConexao(seg) && estado == SOLICITANDO_CONEXAO) {
                        System.out.println("servidor confirmou o pedido de conexão do conexao do cliente");
                        estado = CONEXAO_ACEITA;
                        numeroSequenciaInicialServidor = seg.getSeq();
                    } else if (servidorSolicitouFecharConexao(seg)) {
                        System.out.println("servidor solocitou fechamento de conexao");
                        estado = CONFIRMANDO_FECHAMENTO;// envia ack de resposta 
                    } else if (estado == SOLICITANDO_FECHAMENTO && servidorConfirmouFecharConexao(seg)) {
                        System.out.println("servidor confirmou pedido de fechamento do cliente");
                        estado = DESCONECTADO;
                    } else if (estado == ENVIANDO_ARQUIVO) {
                        System.out.println("chegou um ACK de numero " + seg.getACKnum());
                        janela.processa(seg.getACKnum());
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ClienteEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // metodos que checam qual é a confirmação
        public boolean confirmaAbrirConexao(SegmentoTCP seg) {
            return (seg.getSYN() == 1 && seg.getACKbit() == 1 && seg.getACKnum() == (numeroSequenciaInicialCliente + 1));
        }

        public boolean servidorSolicitouFecharConexao(SegmentoTCP seg) {
            return seg.getFIN() == 1;
        }

        public boolean servidorConfirmouFecharConexao(SegmentoTCP seg) {
            return (seg.getACKnum() == numeroSequenciaInicialCliente + 1) && seg.getACKbit() == 1;
        }
    }

    public static void main(String args[]) throws Exception {
        ClienteEmuladoTCP cliente = new ClienteEmuladoTCP("localhost", 456, 123);
    }
}

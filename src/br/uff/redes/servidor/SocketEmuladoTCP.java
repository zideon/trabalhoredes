/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.segmento.JanelaRecebimento;
import br.uff.redes.segmento.SegmentoComparator;
import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.ArrayJoin;
import br.uff.redes.tools.ObjectConverter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fabio
 */
public class SocketEmuladoTCP {

    public static final int CONFIRMANDO_CONEXAO = 5;// estado que espera o ack da conexao do cliente
    public static final int CONFIRMANDO_FECHAMENTO = 6;
    public static final int SOLICITANDO_FECHAMENTO = 1;// estado que servidor solicitou fechamento e espera confirmação
    public static final int CONECTADO = 2;// estado que esta conetado esperando dados do arquivo a receber 
    public static final int ENVIANDO_ACKS = 3;//estado que ja sabe qual arquivo ele deve receber
    public static final int DESCONECTADO = 4;

    private String ipOrigem;
    private int portaOrigem;
    private String ipDestino;
    private int portaDestino;

    private int numeroSequenciaInicialCliente;
    private int numeroSequenciaInicialServidor;

    private int estado;
    private JanelaRecebimento janelaRecebimento;
    private List<SegmentoTCP> pacotes; // recebidos na ordem correta
    private List<SegmentoTCP> buffer;
    private int tamanhoBuffer;
    private String nomeArquivo;
    private int tamanhoArquivo;
    private int tamanhoPacote;
    private int qtdPacotes;

    public SocketEmuladoTCP(String ipOrigem, int portaOrigem, String ipDestino, int portaDestino) {
        this.ipOrigem = ipOrigem;
        this.portaOrigem = portaOrigem;
        this.ipDestino = ipDestino;
        this.portaDestino = portaDestino;
//        this.numeroSequenciaInicialServidor = new Random().nextInt(9999);
        this.numeroSequenciaInicialServidor =0;
        this.pacotes = new ArrayList<>();
        this.buffer = new ArrayList<>();
        estado = DESCONECTADO;
    }

    public String getIpOrigem() {
        return ipOrigem;
    }

    public void setIpOrigem(String ipOrigem) {
        this.ipOrigem = ipOrigem;
    }

    public int getPortaOrigem() {
        return portaOrigem;
    }

    public void setPortaOrigem(int portaOrigem) {
        this.portaOrigem = portaOrigem;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public void setIpDestino(String ipDestino) {
        this.ipDestino = ipDestino;
    }

    public int getPortaDestino() {
        return portaDestino;
    }

    public void setPortaDestino(int portaDestino) {
        this.portaDestino = portaDestino;
    }

    public DatagramPacket processa(SegmentoTCP tcp) throws UnknownHostException {
        if (pedidoFechamentoConexao(tcp)) {
            System.out.println("cliente pediu para fechar conexão");
            return enviarConfirmacaoFecharConexao(tcp);
        } else if (estado == CONECTADO && contemInformacoesDoArquivo(tcp)) {
            System.out.println("recebeu informações do arquivo");
            estado = ENVIANDO_ACKS;
            if(janelaRecebimento.processa(tcp.getSeq())){
                 return enviarACK();
            }
        } else if (estado == DESCONECTADO && pedidoAberturaConexao(tcp)) {
            System.out.println("pediu abertura de conexão");
            numeroSequenciaInicialCliente = tcp.getSeq();
            return enviaConfirmacaoAberturaConexao(tcp);
        } else if (estado == CONFIRMANDO_CONEXAO && confirmacaoConexao(tcp)) {
            System.out.println("recebeu confirmação do 3 hand shake");
            estado = CONECTADO;
        } else if (estado == SOLICITANDO_FECHAMENTO) {
            System.out.println("servidor pediu para fechar a conexão");
            return enviaPedidoFechamentoConexao(tcp);
        } else if (estado == CONFIRMANDO_FECHAMENTO && pedidoConfirmacaoFechamento(tcp)) {
            System.out.println("servidor recebeu a confirmação de seu pedido de fechamento de conexão");
            estado = DESCONECTADO;
        } else if (estado == ENVIANDO_ACKS) {
            if (!janelaRecebimento.isTerminou()) {
                if(janelaRecebimento.processa(tcp.getSeq())){
                    pacotes.add(tcp);
                    for (SegmentoTCP pacote : buffer) {
                        if(janelaRecebimento.processa(pacote.getSeq())){
                            buffer.remove(pacote);
                            System.out.println("usou pacote do buffer para elementos fora de ordem");
                        }
                    }
                }else{// aqui que vou fazer espaço do buffer digamos que seja 4
                   if(!buffer.contains(tcp) && buffer.size()<tamanhoBuffer){
                      adicionarBufferOrdenado(tcp); 
                   }else if(!buffer.contains(tcp) && buffer.size()==tamanhoBuffer){
                       if(buffer.size()>=1 && tcp.compareTo(buffer.get(buffer.size()-1))==-1){
                           buffer.remove((buffer.size()-1));
                           adicionarBufferOrdenado(tcp);
                       }
                   }
                }
                return enviarACK();
            } else {
                System.out.println("TERMINOU TRANSFERENCIA DO ARQUIVO");
                List<byte[]> bytes = new ArrayList<>();
                
                for (SegmentoTCP pacote : pacotes) {
                    bytes.add(pacote.getPacote());
                }
                System.out.println("FORAM BAIXADAS "+ bytes.size()+ " PARTES");
                byte[] arquivoCompleto = ArrayJoin.combine(bytes);
                System.out.println("arquivo completo tem "+arquivoCompleto.length+" bytes");
                try {
                    FileOutputStream arq = new FileOutputStream(new File(nomeArquivo));
                    arq.write(arquivoCompleto);
                    arq.close();
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(SocketEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SocketEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                }
                estado = CONECTADO;
                return enviarACKArquivoCompleto();
            }
        }else{
            System.out.println("não é uma solicitação valida");
        }
        return null;
    }

    private boolean pedidoAberturaConexao(SegmentoTCP tcp) {
        return tcp.getSYN() == 1;
    }

    private DatagramPacket enviaConfirmacaoAberturaConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = CONFIRMANDO_CONEXAO;
        byte[] sendData = new byte[20480];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setSYN((byte) 1);
        novo.setSeq(numeroSequenciaInicialServidor);
        novo.setACKbit((byte) 1);
        novo.setACKnum(numeroSequenciaInicialCliente + 1);
        sendData = ObjectConverter.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean pedidoFechamentoConexao(SegmentoTCP tcp) {
        return tcp.getFIN() == 1;
    }

    private DatagramPacket enviarConfirmacaoFecharConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = DESCONECTADO;
        byte[] sendData = new byte[20480];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setACKbit((byte) 1);
        novo.setACKnum(tcp.getSeq() + 1);
        sendData = ObjectConverter.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean confirmacaoConexao(SegmentoTCP tcp) {
        return tcp.getACKbit() == 1 && tcp.getACKnum() == (numeroSequenciaInicialServidor + 1);
    }

    private boolean contemInformacoesDoArquivo(SegmentoTCP tcp) {
        try {
            String informacoes = (String) ObjectConverter.convertByteArrayToObject(tcp.getPacote());
            String[] split = informacoes.split("#");
            nomeArquivo = split[0];
            tamanhoArquivo = Integer.parseInt(split[1]);
            qtdPacotes = Integer.parseInt(split[2]);
            tamanhoPacote = Integer.parseInt(split[3]);
            System.out.println("nome do arquivo :"+nomeArquivo);
            System.out.println("tamanho do arquivo:"+tamanhoArquivo);
            System.out.println("tamanho dos pacotes:"+tamanhoPacote);
            System.out.println("quantidade de pacotes "+ qtdPacotes);
            janelaRecebimento = new JanelaRecebimento(numeroSequenciaInicialCliente + 2 ,qtdPacotes,tcp.getPacote().length, tamanhoPacote);
            return true;

        }catch (Exception ex) {
            System.out.println("pacote recebido não contem informações , possivel reenvio de pacotede"
                    + " outro arquivo ja enviado");
            return false;
        }

    }

    private DatagramPacket enviaPedidoFechamentoConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = CONFIRMANDO_FECHAMENTO;
        byte[] sendData = new byte[20480];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setFIN((byte) 1);
        novo.setSeq(numeroSequenciaInicialServidor);
        sendData = ObjectConverter.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean pedidoConfirmacaoFechamento(SegmentoTCP tcp) {
        return tcp.getACKbit() == 1 && tcp.getACKnum() == (numeroSequenciaInicialServidor + 1);
    }

    private void adicionarBufferOrdenado(SegmentoTCP tcp) {
        buffer.add(tcp);
        buffer.sort(new SegmentoComparator());
    }

    private DatagramPacket enviarACK() throws UnknownHostException {
        byte[] sendData = new byte[20480];
        
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setSeq(numeroSequenciaInicialServidor+ janelaRecebimento.indice());
        novo.setACKbit((byte) 1);
        novo.setACKnum(janelaRecebimento.getUltimoACK());
        System.out.println("enviando ACK "+janelaRecebimento.getUltimoACK());
        sendData = ObjectConverter.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    public int getNumeroSequenciaInicialCliente() {
        return numeroSequenciaInicialCliente;
    }

    public void setNumeroSequenciaInicialCliente(int numeroSequenciaInicialCliente) {
        this.numeroSequenciaInicialCliente = numeroSequenciaInicialCliente;
    }

    public int getNumeroSequenciaInicialServidor() {
        return numeroSequenciaInicialServidor;
    }

    public void setNumeroSequenciaInicialServidor(int numeroSequenciaInicialServidor) {
        this.numeroSequenciaInicialServidor = numeroSequenciaInicialServidor;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public JanelaRecebimento getJanelaRecebimento() {
        return janelaRecebimento;
    }

    public void setJanelaRecebimento(JanelaRecebimento janelaRecebimento) {
        this.janelaRecebimento = janelaRecebimento;
    }

    public List<SegmentoTCP> getPacotes() {
        return pacotes;
    }

    public void setPacotes(List<SegmentoTCP> pacotes) {
        this.pacotes = pacotes;
    }

    public List<SegmentoTCP> getBuffer() {
        return buffer;
    }

    public void setBuffer(List<SegmentoTCP> buffer) {
        this.buffer = buffer;
    }

    public int getTamanhoBuffer() {
        return tamanhoBuffer;
    }

    public void setTamanhoBuffer(int tamanhoBuffer) {
        this.tamanhoBuffer = tamanhoBuffer;
    }

    public String getNomeArquivo() {
        return nomeArquivo;
    }

    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    public int getTamanhoArquivo() {
        return tamanhoArquivo;
    }

    public void setTamanhoArquivo(int tamanhoArquivo) {
        this.tamanhoArquivo = tamanhoArquivo;
    }

    private DatagramPacket enviarACKArquivoCompleto() throws UnknownHostException {
        byte[] sendData = new byte[20480];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setSeq(numeroSequenciaInicialServidor);
        novo.setACKbit((byte) 1);
        novo.setACKnum(-100);
        sendData = ObjectConverter.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }


}

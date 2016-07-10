/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.segmento.JanelaRecebimento;
import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.Conversor;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

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
    private int corte;

    public SocketEmuladoTCP(String ipOrigem, int portaOrigem, String ipDestino, int portaDestino) {
        this.ipOrigem = ipOrigem;
        this.portaOrigem = portaOrigem;
        this.ipDestino = ipDestino;
        this.portaDestino = portaDestino;
        this.numeroSequenciaInicialServidor = new Random().nextInt(9999);
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
            return enviarConfirmacaoFecharConexao(tcp);
        } else if (estado == CONECTADO && contemInformacoesDoArquivo(tcp)) {
            estado = ENVIANDO_ACKS;
        } else if (estado == DESCONECTADO && pedidoAberturaConexao(tcp)) {
            numeroSequenciaInicialCliente = tcp.getSeq();
            return enviaConfirmacaoAberturaConexao(tcp);
        } else if (estado == CONFIRMANDO_CONEXAO && confirmacaoConexao(tcp)) {
            estado = CONECTADO;
        } else if (estado == SOLICITANDO_FECHAMENTO) {
            return enviaPedidoFechamentoConexao(tcp);
        } else if (estado == CONFIRMANDO_FECHAMENTO && pedidoConfirmacaoFechamento(tcp)) {
            estado = DESCONECTADO;
        } else if (estado == ENVIANDO_ACKS) {
            if (!janelaRecebimento.isTerminou()) {
                if(janelaRecebimento.processa(tcp.getSeq())){
                    pacotes.add(tcp);
                    for (SegmentoTCP pacote : buffer) {
                        
                    }
                }else{// aqui que vou fazer espaço do buffer digamos que seja 4
                   if(!buffer.contains(tcp) && buffer.size()<tamanhoBuffer){
                       buffer.add(tcp);
                   }
                }
                if (!pacotes.contains(tcp)) {
                    for (SegmentoTCP pacote : buffer) {
                        
                    }
                }
            } else {
                estado = CONECTADO;
            }
        }
        return null;
    }

    private boolean pedidoAberturaConexao(SegmentoTCP tcp) {
        return tcp.getSYN() == 1;
    }

    private DatagramPacket enviaConfirmacaoAberturaConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = CONFIRMANDO_CONEXAO;
        byte[] sendData = new byte[1024];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setSYN((byte) 1);
        novo.setSeq(numeroSequenciaInicialServidor);
        novo.setACKbit((byte) 1);
        novo.setACKnum(numeroSequenciaInicialCliente + 1);
        sendData = Conversor.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean pedidoFechamentoConexao(SegmentoTCP tcp) {
        return tcp.getFIN() == 1;
    }

    private DatagramPacket enviarConfirmacaoFecharConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = DESCONECTADO;
        byte[] sendData = new byte[1024];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setACKbit((byte) 1);
        novo.setACKnum(tcp.getSeq() + 1);
        sendData = Conversor.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean confirmacaoConexao(SegmentoTCP tcp) {
        return tcp.getACKbit() == 1 && tcp.getACKnum() == (numeroSequenciaInicialServidor + 1);
    }

    private boolean contemInformacoesDoArquivo(SegmentoTCP tcp) {
        try {
            String informacoes = (String) Conversor.convertByteArrayToObject(tcp.getPacote());
            String[] split = informacoes.split("#");
            nomeArquivo = split[0];
            tamanhoArquivo = Integer.parseInt(split[1]);
            corte = Integer.parseInt(split[2]);
            janelaRecebimento = new JanelaRecebimento(numeroSequenciaInicialCliente + 2, tamanhoArquivo, corte);
            return true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }

    }

    private DatagramPacket enviaPedidoFechamentoConexao(SegmentoTCP tcp) throws UnknownHostException {
        estado = CONFIRMANDO_FECHAMENTO;
        byte[] sendData = new byte[1024];
        InetAddress IPAddress = InetAddress.getByName(ipDestino);
        SegmentoTCP novo = new SegmentoTCP(ipOrigem, portaOrigem, ipDestino, portaDestino);
        novo.setFIN((byte) 1);
        novo.setSeq(numeroSequenciaInicialServidor);
        sendData = Conversor.convertObjectToByteArray(novo);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, portaDestino);
        return sendPacket;
    }

    private boolean pedidoConfirmacaoFechamento(SegmentoTCP tcp) {
        return tcp.getACKbit() == 1 && tcp.getACKnum() == (numeroSequenciaInicialServidor + 1);
    }
}

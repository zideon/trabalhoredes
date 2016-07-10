/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.segmento.Janela;
import br.uff.redes.segmento.SegmentoTCP;
import java.net.DatagramPacket;
import java.util.List;
import java.util.Random;

/**
 *
 * @author fabio
 */
public class SocketEmuladoTCP {
    
    public static final int CONFIRMANDO_CONEXAO = 5;// estado que espera o ack da conexao do cliente
    public static final int SOLICITANDO_FECHAMENTO = 1;// estado que servidor solicitou fechamento e espera confirmação
    public static final int CONECTADO = 2;// estado que esta conetado esperando dados do arquivo a receber 
    public static final int ENVIANDO_ACKS = 3;//estado que ja sabe qual arquivo ele deve receber
    public static final int DESCONECTADO = 4;
    
    private String ipOrigem;
    private int portaOrigem;
    private String ipDestino;
    private int portaDestino;

    private int seq;
    private int estado;
  
    
    private List<SegmentoTCP> pacotes;
    private String nomeArquivo;
    private int tamanhoArquivo;
    
    public SocketEmuladoTCP(String ipOrigem, int portaOrigem, String ipDestino, int portaDestino) {
        this.ipOrigem = ipOrigem;
        this.portaOrigem = portaOrigem;
        this.ipDestino = ipDestino;
        this.portaDestino = portaDestino;
        this.seq = new Random().nextInt(9999);
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

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }
    public DatagramPacket processa(SegmentoTCP tcp){
        if(pedidoFechamentoConexao(tcp)){
            return enviarConfirmacaoFecharConexao(tcp);
        }else if(estado ==CONECTADO && contemInformacoesDoArquivo()){
            estado = ENVIANDO_ACKS;
        }else if(estado ==DESCONECTADO && pedidoAberturaConexao(tcp)){
            return enviaConfirmacaoAberturaConexao(tcp);
        }else  if(estado ==CONFIRMANDO_CONEXAO && confirmacaoConexao(tcp)){
            estado = CONECTADO;
        }else if(estado == SOLICITANDO_FECHAMENTO){
            return enviaPedidoFechamentoConexao(tcp);
        }else if(estado == ENVIANDO_ACKS){
            if(foraDeOrdemOuJaPossui(tcp)){
                return enviarUltimoACKValido();
            }else {
                return enviarACK(tcp);//modifica o ultimo ACK valido e atualiza a janela
            }
        }
        return null;
    }

    private boolean pedidoAberturaConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DatagramPacket enviaConfirmacaoAberturaConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean pedidoFechamentoConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DatagramPacket enviarConfirmacaoFecharConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean confirmacaoConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean contemInformacoesDoArquivo() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DatagramPacket enviaPedidoFechamentoConexao(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean foraDeOrdemOuJaPossui(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DatagramPacket enviarUltimoACKValido() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private DatagramPacket enviarACK(SegmentoTCP tcp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}

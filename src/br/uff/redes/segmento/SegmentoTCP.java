/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.segmento;

import java.io.Serializable;

/**
 *
 * @author fabio
 */
public class SegmentoTCP implements Serializable {
    private int checksum;
    private byte FIN;
    private byte SYN;
    private byte RST;
    private byte ACKbit;
    private int receiveWindow;
    private int ACKnum;
    private int seq;
    private int length;
    private String ipOrigem;
    private int portaOrigem;
    private String ipDestino;
    private int portaDestino;
    private  byte[] pacote;

    public SegmentoTCP(String ipOrigem, int portaOrigem, String ipDestino, int portaDestino) {
        this.ipOrigem = ipOrigem;
        this.portaOrigem = portaOrigem;
        this.ipDestino = ipDestino;
        this.portaDestino = portaDestino;
    }
     public SegmentoTCP() {}
    public int getChecksum() {
        return checksum;
    }

    public void setChecksum(int checksum) {
        this.checksum = checksum;
    }

    public byte getFIN() {
        return FIN;
    }

    public void setFIN(byte FIN) {
        this.FIN = FIN;
    }

    public byte getSYN() {
        return SYN;
    }

    public void setSYN(byte SYN) {
        this.SYN = SYN;
    }

    public byte getRST() {
        return RST;
    }

    public void setRST(byte RST) {
        this.RST = RST;
    }

    public int getReceiveWindow() {
        return receiveWindow;
    }

    public void setReceiveWindow(int receiveWindow) {
        this.receiveWindow = receiveWindow;
    }

    public byte[] getPacote() {
        return pacote;
    }

    public void setPacote(byte[] pacote) {
        this.pacote = pacote;
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

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int nSeq) {
        this.seq = nSeq;
    }

    public byte getACKbit() {
        return ACKbit;
    }

    public void setACKbit(byte ACKbit) {
        this.ACKbit = ACKbit;
    }

    public int getACKnum() {
        return ACKnum;
    }

    public void setACKnum(int ACKnum) {
        this.ACKnum = ACKnum;
    }
    
}

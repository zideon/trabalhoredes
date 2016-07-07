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
    private int receiveWindow;
    private int ACK;
    private int length;
    private String ipOrigem;
    private String portaOrigem;
    private String ipDestino;
    private String portaDestino;
    private  byte[] pacote;

    public SegmentoTCP(String ipOrigem, String portaOrigem, String ipDestino, String portaDestino) {
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

    public int getACK() {
        return ACK;
    }

    public void setACK(int ACK) {
        this.ACK = ACK;
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

    public String getPortaOrigem() {
        return portaOrigem;
    }

    public void setPortaOrigem(String portaOrigem) {
        this.portaOrigem = portaOrigem;
    }

    public String getIpDestino() {
        return ipDestino;
    }

    public void setIpDestino(String ipDestino) {
        this.ipDestino = ipDestino;
    }

    public String getPortaDestino() {
        return portaDestino;
    }

    public void setPortaDestino(String portaDestino) {
        this.portaDestino = portaDestino;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
    
}

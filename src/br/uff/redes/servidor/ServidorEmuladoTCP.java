/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.Conversor;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServidorEmuladoTCP {

    private List<SocketEmuladoTCP> clientes;
    private DatagramSocket serverSocket;

    public class Ouvinte implements Runnable {

        @Override
        public void run() {
            clientes = new ArrayList<>();
            byte[] receiveData = new byte[1024];
            boolean continua = true;
            while (continua) {
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    serverSocket.receive(receivePacket);
                    SegmentoTCP novo = (SegmentoTCP) Conversor.convertByteArrayToObject(receivePacket.getData());
                    SocketEmuladoTCP cliente = getCliente(novo);
                    if(cliente == null){
                        throw new Exception("mensagem destinada a lugar nenhum");
                    }
                    serverSocket.send(cliente.processa(novo));
                }  catch (Exception ex) {
                    Logger.getLogger(ServidorEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        public SocketEmuladoTCP getCliente(SegmentoTCP novo) {
            for (SocketEmuladoTCP cliente : clientes) {
                if (novo.getIpDestino().equals(cliente.getIpOrigem())
                        && novo.getPortaDestino() == cliente.getPortaOrigem()
                        && novo.getIpOrigem() == cliente.getIpDestino()
                        && novo.getPortaOrigem() == cliente.getPortaDestino()) {
                    return cliente;
                }
            }
            if(novo.getSYN()==1){
            SocketEmuladoTCP cliente = new SocketEmuladoTCP(novo.getIpDestino(), novo.getPortaDestino(), novo.getIpOrigem(), novo.getPortaOrigem());
                return cliente;
            }
            return null;
        }
    }

    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            SegmentoTCP novo = (SegmentoTCP) Conversor.convertByteArrayToObject(receivePacket.getData());
            String sentence = (String) Conversor.convertByteArrayToObject(novo.getPacote());
            System.out.println(novo.getIpOrigem());
            System.out.println(novo.getPortaOrigem());
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();
            sendData = capitalizedSentence.getBytes();
            DatagramPacket sendPacket
                    = new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}

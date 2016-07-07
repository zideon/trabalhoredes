/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.ConvertObjectToByteArray;
import java.io.*;
import java.net.*;

class ServidorEmuladoTCP {

    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            SegmentoTCP novo = (SegmentoTCP) ConvertObjectToByteArray.convertByteArrayToObject(receivePacket.getData());
            String sentence = (String) ConvertObjectToByteArray.convertByteArrayToObject(novo.getPacote());
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.cliente;

import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.ConvertObjectToByteArray;
import java.io.*;
import java.net.*;

class ClienteEmuladoTCP
{
    int nSeq;
    
   public static void main(String args[]) throws Exception
   {
       SegmentoTCP novo = new SegmentoTCP("localhost","123","localhost","123");
       novo.setPacote(ConvertObjectToByteArray.convertObjectToByteArray("ola tudo bem"));
       byte[] sendData = ConvertObjectToByteArray.convertObjectToByteArray(novo);
//      BufferedReader inFromUser =
//         new BufferedReader(new InputStreamReader(System.in));
      DatagramSocket clientSocket = new DatagramSocket();
      InetAddress IPAddress = InetAddress.getByName("localhost");
//      byte[] sendData = new byte[1024];
      byte[] receiveData = new byte[1024];
//      String sentence = inFromUser.readLine();
//      sendData = sentence.getBytes();
      DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
      clientSocket.send(sendPacket);
      DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
      clientSocket.receive(receivePacket);
      String modifiedSentence = new String(receivePacket.getData());
      System.out.println("FROM SERVER:" + modifiedSentence);
      clientSocket.close();
   }
}

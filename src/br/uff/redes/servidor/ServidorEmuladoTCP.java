/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.servidor;

import br.uff.redes.cliente.ClienteEmuladoTCP;
import br.uff.redes.segmento.SegmentoTCP;
import br.uff.redes.tools.ObjectConverter;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

class ServidorEmuladoTCP {

    private volatile List<SocketEmuladoTCP> clientes;
    private volatile DatagramSocket serverSocket;

    public ServidorEmuladoTCP(int port) throws SocketException {
        clientes = new ArrayList<>();
        serverSocket = new DatagramSocket(port);
        new Thread(new Ouvinte()).start();
    }

    public class Ouvinte implements Runnable {

        @Override
        public void run() {

            boolean continua = true;
            while (continua) {
                byte[] receiveData = new byte[2048];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                try {
                    System.out.println("ouvindo....");
                    serverSocket.receive(receivePacket);
                    SegmentoTCP novo = (SegmentoTCP) ObjectConverter.convertByteArrayToObject(receivePacket.getData());
                    System.out.println("chegou segmento TCP de numero de sequencia " + novo.getSeq());
                    SocketEmuladoTCP cliente = getCliente(novo);
                    if (cliente == null) {
                        throw new Exception("mensagem destinada a lugar nenhum");
                    }
                    DatagramPacket data = cliente.processa(novo);
                    if (data == null) {
                        System.out.println("datagram nulo");
                    } else {
                        serverSocket.send(data);
                        System.out.println("enviou data");
                    }

                } catch (Exception ex) {
                    Logger.getLogger(ServidorEmuladoTCP.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }

        public SocketEmuladoTCP getCliente(SegmentoTCP novo) {
            for (SocketEmuladoTCP cliente : clientes) {
                if (novo.getIpDestino().equals(cliente.getIpOrigem())
                        && novo.getPortaDestino() == cliente.getPortaOrigem()
                        && novo.getIpOrigem().equals(cliente.getIpDestino())
                        && novo.getPortaOrigem() == cliente.getPortaDestino()) {
                    System.out.println("cliente ja existia");
                    return cliente;
                }
            }
            if (novo.getSYN() == 1) {
                SocketEmuladoTCP cliente = new SocketEmuladoTCP(novo.getIpDestino(), novo.getPortaDestino(), novo.getIpOrigem(), novo.getPortaOrigem());
                System.out.println("cliente foi criado");
                clientes.add(cliente);
                return cliente;
            }
            return null;
        }
    }

    public static void main(String args[]) throws Exception {
        ServidorEmuladoTCP servidor = new ServidorEmuladoTCP(456);
    }
}

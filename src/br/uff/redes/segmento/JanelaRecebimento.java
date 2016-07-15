/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.segmento;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fabio
 */
public class JanelaRecebimento {

    public static final int RECEBIDO = 0;
    public static final int NAORECEBIDO = 1;
    List<Integer> numerosSeq;
    List<Integer> estados;
    int ultimoACK;
    boolean terminou;

    public JanelaRecebimento(int seqInicial, int qtdPacotes, int tamanhoInformacao, int tamanhoPacote) {

        numerosSeq = new ArrayList<>();
        estados = new ArrayList<>();
        numerosSeq.add(seqInicial);
        estados.add(NAORECEBIDO);
        seqInicial = seqInicial + tamanhoInformacao;
        for (int i = 0; i < qtdPacotes - 1; i++) {
            numerosSeq.add(seqInicial);
            estados.add(NAORECEBIDO);
            //defini estado do proximo

            seqInicial += tamanhoPacote;

        }

        System.out.print("seq " + 0 + ":" + numerosSeq.get(0) + " ");
        System.out.print("seq " + (numerosSeq.size() - 1) + ":" + numerosSeq.get(numerosSeq.size() - 1));
        System.out.println(
                "");
        ultimoACK = -1;
        terminou = false;
    }

// true para pacote na ordem correta
    public boolean processa(int seq) {
        int indice = numerosSeq.indexOf(seq);
        if (indice != -1) {
            if (estados.get(indice) == NAORECEBIDO) {

                if (indice == estados.size() - 1) {
                    if (estados.get(indice - 1) == RECEBIDO) {
                         System.out.println("terminou");
                            terminou = true;
                            return true;
                    }
                } else if (indice == 0) {
                    estados.set(indice, RECEBIDO);
                    if (indice + 1 < numerosSeq.size()) {
                        ultimoACK = numerosSeq.get(indice + 1);

                    } else {
                        terminou = true;
                    }
                    return true;
                } else {
                    boolean ordemCorreta = true;

                    if (estados.get(indice - 1) == NAORECEBIDO) {
                        ordemCorreta = false;
                    }

                    if (ordemCorreta) {
                        estados.set(indice, RECEBIDO);
                        if (indice + 1 < numerosSeq.size()) {
                            ultimoACK = numerosSeq.get(indice + 1);
                        } 
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getUltimoACK() {
        return ultimoACK;
    }

    public void setUltimoACK(int ultimoACK) {
        this.ultimoACK = ultimoACK;
    }

    public boolean isTerminou() {
        return terminou;
    }

    public void setTerminou(boolean terminou) {
        this.terminou = terminou;
    }

    public int indice() {
        return numerosSeq.indexOf(ultimoACK);
    }
}

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

    public JanelaRecebimento(int seqInicial, int tamanhoInformacao, int tamanho, int corte) {
        int n = tamanho / corte;
        int resto = tamanho % corte;
        numerosSeq = new ArrayList<>();
        estados = new ArrayList<>();
        numerosSeq.add(seqInicial);
        estados.add(NAORECEBIDO);
        seqInicial = seqInicial + tamanhoInformacao;
        for (int i = 0; i < n; i++) {
            if (i != (n - 1)) {
                numerosSeq.add(seqInicial + i * corte);
            } else {
                numerosSeq.add(seqInicial + (i - 1) * corte + resto);
            }
            estados.add(NAORECEBIDO);
        }

        System.out.print("seq " + 0 + ":" + numerosSeq.get(0) + " ");
        System.out.print("seq " + (numerosSeq.size() - 1) + ":" + numerosSeq.get(numerosSeq.size() - 1) + " ");
        System.out.println("");
        ultimoACK = -1;
        terminou = false;
    }

    // true para pacote na ordem correta
    public boolean processa(int seq) {
        int indice = numerosSeq.indexOf(seq);
        if (estados.get(indice) == NAORECEBIDO) {

            if (indice == 0) {
                estados.set(indice, RECEBIDO);
                if (indice + 1 < numerosSeq.size()) {
                    ultimoACK = numerosSeq.get(indice + 1);

                } else {
                    terminou = true;
                }
                return true;
            } else {
                boolean ordemCorreta = true;
                for (int i = indice - 1; i > 0; i--) {
                    if (estados.get(i) == NAORECEBIDO) {
                        ordemCorreta = false;
                    }
                }
                if (ordemCorreta) {
                    estados.set(indice, RECEBIDO);
                    if (indice + 1 < numerosSeq.size()) {
                        ultimoACK = numerosSeq.get(indice + 1);

                    } else {
                        terminou = true;
                    }
                    return true;
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

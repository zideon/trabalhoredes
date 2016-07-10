/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.segmento;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author fabio
 */
public class JanelaEnvio {

    public static final int DISPONIVEL = 0;
    public static final int INDISPONIVEL = 1;
    public static final int ACKED = 1;
    public static final int ESPERANDOACK = 2;
    //numero de sequencia e estado
    List<Integer> numerosSeq;
    List<Integer> estados;
    int tamanho;
    int primeiroSeq;
    int ultimoSeq;
    int qtdRepeticoes;
    Timer timer;

    boolean tempoCorrendo;
    int temporizado;
    public JanelaEnvio(List<SegmentoTCP> segmentos) {
        numerosSeq = new ArrayList<>();
        estados = new ArrayList<>();
        primeiroSeq = 0;
        ultimoSeq = tamanho;
        for (int i = 0; i < segmentos.size(); i++) {
            numerosSeq.add(segmentos.get(i).getSeq());
            if (i < tamanho) {
                estados.add(DISPONIVEL);
            } else {
                estados.add(INDISPONIVEL);
            }
        }
    }

    public void processa(Integer ACK) {
        int n = numerosSeq.indexOf(ACK);
        // 3 repetições de ACK reenvia o 
        if (estados.get(n) == ESPERANDOACK) {
            qtdRepeticoes++;
            if (qtdRepeticoes == 3) {
                estados.set(n, DISPONIVEL);
                qtdRepeticoes = 0;
            }
            //andando com a janela após receber os ACKS acumulativos
        } else {
            int qtdAvancos = 0;
            for (int i = n - 1; i >= primeiroSeq; i--) {
                if (estados.get(i)==ESPERANDOACK){
                    estados.set(i, ACKED);
                    qtdAvancos++;
                    if(tempoCorrendo && i == temporizado){
                        tempoCorrendo=false;
                        timer.cancel();
                        temporizado=0;
                    }
                }
            }
            primeiroSeq = primeiroSeq + qtdAvancos;
            for (int i = ultimoSeq+1; i <=ultimoSeq+qtdAvancos ; i++) {
                if (estados.get(i)==INDISPONIVEL){
                    estados.set(i, DISPONIVEL);
                }
            }
            ultimoSeq = ultimoSeq + qtdAvancos;
        }
    }

    public int proximoEnvio() {
        for (int i = primeiroSeq; i <= ultimoSeq; i++) {
            if (estados.get(i) == DISPONIVEL) {
                if (!tempoCorrendo) {
                    timer = new Timer();
                    timer.schedule(new Temporizador(), 60 * 1000);// 60 segundos
                    tempoCorrendo =true;
                    temporizado=i; 
                }
                estados.set(i, ESPERANDOACK);
                return numerosSeq.get(i);
            }
        }
        return -1;
    }

    class Temporizador extends TimerTask {

        @Override
        public void run() {
            System.out.println("estouro do temporizador");
            // reenvia todos que estão encaminhados
            for (int i = primeiroSeq; i <= ultimoSeq; i++) {
                estados.set(i, DISPONIVEL);
            }
            timer.cancel();
        }
    }
}

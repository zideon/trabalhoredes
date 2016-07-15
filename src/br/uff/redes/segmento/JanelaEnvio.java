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
    public static final Integer TEMPO = 10;
    //numero de sequencia e estado
    List<Integer> numerosSeq;
    List<Integer> estados;
    int tamanho;
    int limiteJanela;
    int limiteInferior;
    int qtdRepeticoes;
    int ACKrepetido;
    Timer timer;

    boolean tempoCorrendo;

    int temporizado;

    public JanelaEnvio(List<SegmentoTCP> segmentos) {
        this.numerosSeq = new ArrayList<>();
        this.estados = new ArrayList<>();
        this.limiteInferior = 0;
        this.tamanho = 1;
        this.ACKrepetido = -1;

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
        if (n == numerosSeq.size() - 1) {
            System.out.println("enviar o final");
        }
        // 3 repetições de ACK reenvia o 
        if (n != -1) {
            if (estados.get(n) == ESPERANDOACK) {
                if (ACKrepetido != ACK) {
                    ACKrepetido = numerosSeq.get(n);
                } else {
                    qtdRepeticoes++;
                    if (qtdRepeticoes == 3) {
                        System.out.println("segmento perdido 3 acks repetidos");
                        for (int i = limiteInferior; i <= getLimite(); i++) {
                            estados.set(i, DISPONIVEL);
                        }
//                        tamanho=1;
                        qtdRepeticoes = 0;
                    }
                }
                //andando com a janela após receber os ACKS acumulativos
            } else {
                int qtdAvancos = 0;
                for (int i = n - 1; i >= limiteInferior; i--) {
                    if (estados.get(i) == ESPERANDOACK) {
                        System.out.println("ACKED " + numerosSeq.get(i));
                        estados.set(i, ACKED);
                        qtdAvancos++;
                        if (tempoCorrendo && i == temporizado) {
                            tempoCorrendo = false;
                            timer.cancel();
                            temporizado = 0;
                        }
                    }
                }
                limiteInferior = limiteInferior + qtdAvancos;
                tamanho += qtdAvancos;
                //andar com a janela

                for (int i = limiteInferior; i <= getLimite(); i++) {
                    if (estados.get(i) == INDISPONIVEL) {
                        estados.set(i, DISPONIVEL);
                    }
                }
                System.out.println("tamanho " + tamanho);
//                System.out.println("limite inferior " + limiteInferior);
            }
        }
    }

    public int proximoEnvio() {
     
        for (int i = limiteInferior; i <= getLimite(); i++) {
            if (estados.get(i) == DISPONIVEL) {
                if (!tempoCorrendo) {
                    timer = new Timer();
                    timer.schedule(new Temporizador(), TEMPO * 1000);// 60 segundos
                    tempoCorrendo = true;
                    temporizado = i;
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
          
            for (int i = limiteInferior; i <= getLimite(); i++) {
                estados.set(i, DISPONIVEL);
            }
            tamanho=1;
            timer.cancel();
        }
    }

    public void slowStart() {

    }

    public int getLimite() {
        int limite = limiteInferior + tamanho;
        if (limite >= numerosSeq.size()) {
            limite = numerosSeq.size() - 1;
        }
        return limite;
    }
}

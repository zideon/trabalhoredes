/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.segmento;

import java.util.Comparator;

/**
 *
 * @author fabio
 */
public class SegmentoComparator implements Comparator<SegmentoTCP>{

    @Override
    public int compare(SegmentoTCP o1, SegmentoTCP o2) {
        return o1.compareTo(o2);
    }
    
}

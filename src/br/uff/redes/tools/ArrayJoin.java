/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.tools;

import java.util.List;

/**
 *
 * @author fabio
 */
public class ArrayJoin {

    public static byte[] combine(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];

        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }
    public static byte[] combine(List<byte[]> list){
        byte[] result = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            result = combine(result,list.get(i));
        }
        return result;
    }
}

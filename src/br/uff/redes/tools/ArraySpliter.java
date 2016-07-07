/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author fabio
 */
public class ArraySpliter {

    public static List<byte[]> split(byte[] input, int segmentSize) {
        int x = 3;  // chunk size
        int len = input.length;
        List<byte[]> output = new ArrayList<>();
        for (int i = 0; i < len - x + 1; i += x) {
           output.add(Arrays.copyOfRange(input, i, i + x));
        }

        if (len % x != 0) {
            output.add(Arrays.copyOfRange(input, len - len % x, len));
        }
        return output;
    }
}

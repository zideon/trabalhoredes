/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.uff.redes.tools;

import java.io.File;
import java.io.FileInputStream;

/**
 *
 * @author fabio
 */
public class FileConverter {
    public static byte[] convertFileToArray(File file){
        FileInputStream fileInputStream=null;  
        byte[] bFile = new byte[(int) file.length()];
        
        try {
            //convert file into array of bytes
	    fileInputStream = new FileInputStream(file);
	    fileInputStream.read(bFile);
	    fileInputStream.close();	
	    System.out.println("Done");
        }catch(Exception e){
        	e.printStackTrace();
        }
        return bFile;
    }
}

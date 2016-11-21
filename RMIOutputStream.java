/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmsa3;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

/**
 *
 * @author Anthony
 */
public class RMIOutputStream extends OutputStream implements 
        Serializable { 
    
    private RMIOutputStreamInterf out;
    
    public RMIOutputStream(RMIOutputStreamImpl out) {
        this.out = out;
    }
    
    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte[] b, int off, int len) throws 
            IOException {
        out.write(b, off, len);
    }
    
    public void close() throws IOException {
        out.close();
    }   
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmsa3;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Anthony
 */
public interface RMIOutputStreamInterf extends Remote {
    
    public void write(int b) throws IOException, RemoteException;
    public void write(byte[] b, int off, int len) throws 
    IOException, RemoteException;
    public void close() throws IOException, RemoteException;
}

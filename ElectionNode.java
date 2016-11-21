/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmsa3leader;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author Anthony
 */
public interface ElectionNode extends Remote{
    
    public String startElection(String senderName) throws RemoteException, DeadNodeException;
    public void newLeader(String newLeaderName) throws RemoteException;
    public String recvMsg(String senderName, String msg) throws RemoteException;  
}

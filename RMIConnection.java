package dmsa3;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIConnection extends Remote 
{
	public void getNotification() throws RemoteException;
        public OutputStream getOutputStream(File f) throws RemoteException;
        public InputStream getInputStream(File f) throws RemoteException;
}

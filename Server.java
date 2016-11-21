package dmsa3;

import java.awt.*;

import javax.swing.*;

import java.io.*;
import java.net.*;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.awt.event.*;
import java.rmi.Remote;
import static java.rmi.server.UnicastRemoteObject.unexportObject;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends JFrame implements RMIConnection {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JTextField userTextField;
    private JTextArea chatWindow;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private ServerSocket server;
    private Socket connection;
    private String message;
    
    Map<String,Queue<String>>  map; //Player, Queue<Message>;
    
    
    
    public boolean join(String user)
    {
        if(!map.containsKey(user))
        {
            map.put(user,new LinkedList<String>());
            // loop through eah of the other   queues and post message that new user joine
            return true;
        }
        else return false;
    }
    
    public void sendAll(String message)
    {
        Collection<Queue<String>> queues = map.values();
        for(Queue<String> queue:queues)
            queue.offer(message);
    }
    
    public String checkMail(String user)
    {
        return map.get(user).poll();
    }
    public Server(String message) {
        super("Chat System");
        this.message = message;
        userTextField = new JTextField();
        chatWindow = new JTextArea();
        userTextField.setEditable(false);
        
        map = new LinkedHashMap<>();
        
        userTextField.addActionListener(new ChatListener());
        add(userTextField, BorderLayout.NORTH);
        add(new JScrollPane(chatWindow));
        setSize(300, 150);
        setVisible(true);
    }

    public void startRunning() {
        try {
            server = new ServerSocket(6789, 100);
            while (true) {
                ClientThread client;
                try {
                  // client = new ClientThread(client);
                  // Thread thread = new Thread(client);
                  // thread.start();
                    waitForConnection();
                    setupStreams();
                    whileChatting();
                } catch (EOFException eofException) {
                    showMessage("\n Server ended the connection");
                } finally {
                    closeConnection();
                }
            }

        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private void waitForConnection() throws IOException {
        showMessage("Waiting for someone to connect \n");
        connection = server.accept();
        showMessage("Connection established! Connected with " + connection.getInetAddress().getHostName());
    }

    private void setupStreams() throws IOException {
        oos = new ObjectOutputStream(connection.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(connection.getInputStream());
        showMessage("\nStreams are now setup! \n");
    }

    private void whileChatting() throws IOException {
        String message = "You are now connected!";
        sendMessage(message);
        ableToType(true);
        do {
            try {
                message = (String) ois.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException cnfe) {
                showMessage("\n Unknown message sent");
            }

        } while (!message.equals("CLIENT - END"));
    }

    private void closeConnection() {
        showMessage("\nClosing the connection! \n");
        ableToType(false);
        try {
            if (oos != null) {
                oos.close();
            }
            if (ois != null) {
                ois.close();
            }
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

    private void sendMessage(String messageToSend) {
        try {
            oos.writeObject("SERVER - " + messageToSend);
            oos.flush();
            showMessage("\nSERVER - " + messageToSend);
        } catch (IOException e) {
            e.getStackTrace();
            chatWindow.append("\n Message cannot be sent");
        }
    }

    private void showMessage(final String messageToShow) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chatWindow.append(messageToShow);
            }
        });
    }

    private void ableToType(final boolean value) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userTextField.setEditable(value);
            }
        });
    }

    @Override
    public OutputStream getOutputStream(File f) throws RemoteException {
        try {
            //return new RMIOutputStream(new RMIOutputStreamImpl(new FileOutputStream(f)));
            RMIOutputStream ros = new RMIOutputStreamImpl(new FileOutputStream(f));
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch(IOException e)
        {
            e.getStackTrace();
        }
    }

    @Override
    public InputStream getInputStream(File f) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class ChatListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sendMessage(e.getActionCommand());
            userTextField.setText("");
        }

    }

    public void getNotification() throws RemoteException {

        showMessage(message);
        System.out.println(message);
    }

    /**
     *
     * @param f
     * @return
     * @throws IOException
     */
    /*
    public OutputStream getOutputStream(File f) throws IOException {
        return new RMIOutputStream(new RMIOutputStreamImpl(new FileOutputStream(f)));
    }

    public InputStream getInputStream(File f) throws IOException {
        return new RMIInputStreams(new RMIInputStreamImpl(new FileInputStream(f)));
    }*/

    public static class ServerImpl extends Server {

        Registry rmiRegistry;

        public ServerImpl(String message) {
            super(message);
        }

        /**
         * public ServerImpl() throws RemoteException { super();
        }
         * @throws java.lang.Exception
         */
        public void start() throws Exception {
            rmiRegistry = LocateRegistry.createRegistry(1099);
            rmiRegistry.bind("server", this);
            //System.out.println("Server started");
        }

        public void stop() throws Exception {
            rmiRegistry.unbind("server");
            unexportObject(this, true);
            unexportObject(rmiRegistry, true);
            //System.out.println("Server stopped");
        }

        public static void main(String[] args) {
            Server server = new Server("Welcome to our chat system!");
            server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                RMIConnection stub = (RMIConnection) UnicastRemoteObject.exportObject((Remote) server, 0);
                Registry registry = LocateRegistry.createRegistry(1099);
                registry.rebind("greeting", stub);

                String[] bindings = Naming.list("localhost");
                for (String name : bindings) {
                    System.out.println(name);
                }
            } catch (RemoteException e) {
                e.getStackTrace();
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            server.startRunning();
        }

    }
}

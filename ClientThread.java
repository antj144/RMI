/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dmsa3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Anthony
 */
class ClientThread implements Runnable {
  private Socket client;
  private clientThread t[];
  //private JTextArea textArea;

//Constructor
  ClientThread(Socket client) {
    this.client = client;
    
    //this.textArea = textArea;
  }

  public void run(){
    String line;
    BufferedReader in = null;
    PrintWriter out = null;
    try{
      in = new BufferedReader(new 
        InputStreamReader(client.getInputStream()));
      out = new 
        PrintWriter(client.getOutputStream(), true);
    } catch (IOException e) {
      System.out.println("in or out failed");
      System.exit(-1);
    }

    while(true){
      try{
        line = in.readLine();
//Send data back to client
        out.println(line);
//Append data to text area
        //textArea.append(line);
       }catch (IOException e) {
        System.out.println("Read failed");
        System.exit(-1);
       }
    }
  }
}

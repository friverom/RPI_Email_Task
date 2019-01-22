/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rpi_email_task;

import Communication.EmailMessage;
import Communication.RD3mail;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author Federico
 */
public class RPI_Email_Task {
    
    private boolean runFlag = true;
    RPI_Email emailTask = null;
    
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        
        new RPI_Email_Task().serverRun();
    }
    
    private void serverRun() throws IOException, ClassNotFoundException{
    
        ServerSocket serversocket = new ServerSocket(30006);
        emailTask = new RPI_Email();
        emailTask.start();
        
        while (runFlag) {
            
            String request = "";
            Object reply = null;
            Socket socket = serversocket.accept();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            request = (String) ois.readObject(); //Get Command
            oos.writeObject(this.processCommand(request)); //Process command
            oos.close();
            ois.close();
            socket.close();
            

        }
    }
    
    private Object processCommand(String command){
    
        Object reply = null;
        switch(command){
            case"kill thread":
                runFlag = false;
                emailTask.killTask();
                reply = "Task Killed";
                break;
                
            case "get timer":
                reply = emailTask.getTimer();
                break;
                
            default:
                reply = "Error";
                
            }
               
        return reply;
    }
    
}

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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import static java.lang.System.in;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.NoSuchProviderException;
import rpio_client.Net_RPI_IO;

/**
 *
 * @author Federico
 */
public class RPI_Email {
    
     private static final int INTRUSION = 30001;
    private static final int ENERGY = 30002;
    private static final int LIGHTS = 30003;
    private static final int AC = 30004;
    private static final int EMAIL = 30006;
    
    private boolean runFlag = true;
    private long timer = 0;
    private RD3mail rd3email = new RD3mail("test.adr3@adr3group.com", "$test.2018*");            
    private EmailMessage message = new EmailMessage();
    private ArrayList<EmailMessage> emailList = new ArrayList<EmailMessage>();
    private boolean messageFlag=false;
    
    public String start() {
        Thread emailTask = new Thread(new email(),"Email Task");
        emailTask.start();
        return "Started";
    }
    
    public long getTimer(){
        return timer;
    }
    
    public String killTask(){
        runFlag=false;
        return "Task killed";
    }
    
    public class email implements Runnable{
        
        Net_RPI_IO rpio = new Net_RPI_IO("localhost",30000);
        int connect_status=0;
        
        @Override
        public void run() {
            
            while(runFlag){
                timer=System.currentTimeMillis();
                rpio.setLedON(1, 1);
                try {
                    connect_status = rd3email.checkEmail(emailList);
                } catch (NoSuchProviderException ex) {
                    Logger.getLogger(RPI_Email.class.getName()).log(Level.SEVERE, null, ex);
                }
                //   System.out.println("Email count "+emailList.size());
            String subject;
            String request="";
            if (connect_status == 0) {
                for (int i = 0; i < emailList.size(); i++) {
                    message = (EmailMessage) emailList.get(i);
                    /*    System.out.println("Email "+i);
                System.out.println("From: "+message.getFrom());
                System.out.println("To: "+message.getTo()[0]);*/
                    subject = message.getSubject();
                    try {
                        request = getRequest(subject);
                    } catch (IOException ex) {
                        Logger.getLogger(RPI_Email.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    message.setReply();
                    if(request.equalsIgnoreCase("invalid command")){
                        rd3email.sendEmail(message, request);
                    }else {
                    String[] parts = request.split("/");
                    if (parts[1].equalsIgnoreCase("home")) {
                        String text=message.getActualDate()+"\n";
                        text=text+subject+" File attached";
                        String[] file = request.split("\n");
                        rd3email.sendEmailAttach(message, text, file[0]);
                    } else {
                        rd3email.sendEmail(message, request);
                    }
                }
                    emailList.clear();

                }
                
            }
            try {
                    rpio.setLedOFF(1, 1);
                    Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                    Logger.getLogger(RPI_Email.class.getName()).log(Level.SEVERE, null, ex);
                }
            
        }
        
    }
}
    
    public String getRequest(String subject) throws IOException {
        String request = message.getActualDate() + "\n";
        request = request + "Monitor3 Version 2.0\n\n";
        String[] parts = subject.split(",");
        
        subject = parts[0].toLowerCase();
        
        switch(subject){
            case "get status":
                request=request+sendCommand("localhost",INTRUSION,"get status");
                request=request+sendCommand("localhost",LIGHTS,"get status");
                request=request+sendCommand("localhost",ENERGY,"get status");
                request=request+sendCommand("localhost",AC,"get status");
                break;
                
            case "ack ac alarm":
                request=request+sendCommand("localhost",AC,"ack alarm");
                break;
                
            case "reset ac alarm":
                request=request+sendCommand("localhost",AC,"reset alarm");
                break;
                
            case "set alarm temp":
                request=request+sendCommand("localhost",AC,"set alarm temp,"+parts[1]);
                break;
                
            case "get alarm temp":
                request=request+"Alarm Temperature "+sendCommand("localhost",AC,"get alarm temp");
                break;
                
            case "set sim":
                request=request+sendCommand("localhost",AC,"set sim");
                break;
            case "reset sim":
                request=request+sendCommand("localhost",AC,"reset sim");
                break;
                
            case "set sim temp":
                request=request+sendCommand("localhost",AC,"set sim temp,"+parts[1]);
                break;
            
            case "set schedule":
                request=request+sendCommand("localhost",AC,"set schedule,"+parts[1]);
                break;
                
            case "get schedule":
                request=request+"AC switch over every "+sendCommand("localhost",AC,"get schedule")+" minutes";
                break;
                
            case "get temperature":
            case "get temp":
                request=request+"Radar Room Temp: "+sendCommand("localhost",AC,"get temperature");
                break;
            case "get temp log":
                request=sendCommand("localhost",AC,"get temp log,"+parts[1]);
                break;
            
            case "get light timer":
                request=request+"Interior Lights timer set to "+sendCommand("localhost",LIGHTS,"get timer")+" minutes";
                break;
            
            case "set light timer":
                request=request+sendCommand("localhost",LIGHTS,"set timer,"+parts[1]);
                break;
                
            case "platform lights on":
                request=request+sendCommand("localhost",LIGHTS,"platform on");
                break;
                
            case "platform lights off":
                request=request+sendCommand("localhost",LIGHTS,"platform off");
                break;
                
            case "obstruction lights on":
                request=request+sendCommand("localhost",LIGHTS,"obstruction on");
                break;
                
            case "obstruction lights off":
                request=request+sendCommand("localhost",LIGHTS,"obstruction off");
                break;
                
            case "set latitude":
                request=request+sendCommand("localhost",LIGHTS,"set latitude,"+parts[1]);
                break;
                
            case "get latitude":
                request=request+"Latitud: "+sendCommand("localhost",LIGHTS,"get latitude");
                break;
                
            case "set longitude":
                request=request+sendCommand("localhost",LIGHTS,"set longitude,"+parts[1]);
                break;
            
            case "get longitude":
                request=request+"Longitude: "+sendCommand("localhost",LIGHTS,"get longitude");
                break;
                
            case "enable intrusion alarm":
                request=request+sendCommand("localhost",INTRUSION,"enable alarm");
                break;
                
            case "disable intrusion alarm":
                request=request+sendCommand("localhost",INTRUSION,"disable alarm");
                break;
                
            case "set alarm timer":
                request=request+sendCommand("localhost",INTRUSION,"set alarm timer,"+parts[1]);
                break;
                
            case "get alarm timer":
                request=request+"Alarm Reset timer after "+sendCommand("localhost",INTRUSION,"get alarm timer")+" minutes";
                break;
                
            case "reset intrusion alarm":
                request=request+sendCommand("localhost",INTRUSION,"reset alarm");
                break;
            
            case "get energy log":
                request=sendCommand("localhost",ENERGY,"get log");
                break;
                
            case "reset energy logger":
                request=request+sendCommand("localhost",ENERGY,"reset monitor");
                break;
                
            default:
                request="Invalid Command";
            
        }
        return request;
    } 
    
    public String sendCommand(String address, int port, String command) throws IOException{
        Socket socket = new Socket(address, port);
        InputStreamReader in = new InputStreamReader(socket.getInputStream());
        BufferedReader bfin = new BufferedReader(in);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        out.println(command);
        String thisline="";
        String reply ="";
        
        while((thisline=bfin.readLine())!=null){
            reply = reply+String.format("%s%n", thisline);
            //reply=reply+thisline+"\n";
        }
        bfin.close();
        out.close();
        in.close();
        socket.close();
        
        return reply;
    }
}

    



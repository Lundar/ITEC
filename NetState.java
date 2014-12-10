/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs241;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/**
 *
 * @author Zeus
 */
public class NetState {
    static String protocol;
    static String host;
    static int port;
    static int sendPort;
    String username;
    static int keepAlive;
    static boolean imaptls;
    static boolean smtptls;
    static boolean poptls;
    ArrayList<Message> messages;
    
    Session session;
    Store store;
    Folder inbox;
    
    static{
    protocol="imap";
    host="mymail.clarkson.edu";
    port=143;
    sendPort=587;
    keepAlive = 300000;
    imaptls=true;
    poptls=true;
    smtptls=true;
    }  
    
    /**
     * Create a new Netstate with a given username.
     * @param uname username to use
     */
    public NetState(String uname){
    
    session=null;
    store=null;
    inbox=null;
    username=uname;
    messages=new ArrayList();
    
    }
    
    /**
     * Get a new blank message to be filled and sent
     * @return an empty message
     */
    public MimeMessage getBlank(){
        return new MimeMessage(session);
    }
    
    /**
     * Sends an EMail. 
     * @param m the Message to be sent
     * @param pass password for reauthentication
     * @return always returns false
     * @throws Exception handle possible network exceptions
     */
    public boolean sendMail(Message m, String pass) throws Exception{
    
        Transport trans = session.getTransport();
        trans.connect(host,sendPort, username, pass);
        //m.setFrom(new InternetAddress(username));
        trans.sendMessage(m, m.getAllRecipients());
    
    return false;
    }
    
    /**
     * Reloads the inbox.
     */
    public void reload(){
        messages.clear();
        try{
        Message[] mm=inbox.getMessages();
            for(Message n: mm)
                messages.add(n);
        }catch(Exception e){}
    }
    
    /**
     * Starts a connection and populates the inbox.
     * This should be called almost imeadiatly after creation. 
     * @param password password for authentication
     * @return true if connection was successful 
     */
    public boolean startSession(String password){
    Properties props = new Properties();
        props.setProperty("mail.store.protocol", protocol);
        //props.put("mail.debug", "true");
        if(imaptls)
            props.put("mail.imap.starttls.enable","true");
        if(smtptls)
            props.put("mail.smtp.starttls.enable","true");
        if(poptls)
            props.put("mail.pop3.starttls.enable","true");

        try {
            session = Session.getInstance(props, null);
            store = session.getStore();
            store.connect(host,port, username, password);
            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            
            Message[] mm=inbox.getMessages();
            for(Message n: mm)
                messages.add(n);
            
            if(inbox instanceof IMAPFolder)
                new HeartBeet();
            
            return true;
        } catch (Exception mex) {
            mex.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Thread to maintain connection
     */
    private class HeartBeet extends Thread{
    
        public HeartBeet(){
        start();
        }
        
    @Override
    public void run(){
        IMAPFolder fold;
        fold=(IMAPFolder)inbox;
    while(true){
        try{
            Thread.sleep(keepAlive);
        
            // Perform a NOOP just to keep alive the connection
            fold.doCommand((IMAPProtocol p) -> {
              p.simpleCommand("NOOP", null);
              return null;
            });
        }catch (InterruptedException e){}
        catch (MessagingException e){}
    }   
    
    }
    
    }
    
}

package cs241;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Tongxin
 * @version 2.1
 */
public class FileManager {    
    final private String rootFolder = "Email_Client";
    final private String emails ="\\Emails\\";
    private String content;
    
    /**
     * default constructor
     */
    public FileManager(){}
    
    /**
     * load email list from drafts
     * @param username current client,the sender
     * @return an array of emailList for drafts
     */
    public Email[] loadDrafts(String username){
        Email[] aEmailList = loadEmailList(username,"Drafts");
        return aEmailList;
    }

    /**
     * load email list from inbox
     * @param username current client,the sender
     * @return an array of emailList for inbox
     */
    public Email[] loadInbox(String username){
        Email[] aEmailList = loadEmailList(username,"Inbox");
        return aEmailList;
    }
    
    /**
     * load email list from sentItems
     * @param username current client,the sender
     * @return an array of emailList for sentItems
     */
    public Email[] loadSentItems(String username){
        Email[] aEmailList = loadEmailList(username,"SentItems");
        return aEmailList;
    }
    /**
     * save an Email object to draft
     * @param e an Email object
     */
    public void saveDraft(Email e){
        String info = "";
        String emailFolderName = e.subject + "_" + e.sender + "_" + e.receiver + "_" + e.cc + "_";
        File pathDraftTop = new File(rootFolder + emails + e.sender + "\\Drafts");
        if(!pathDraftTop.exists()){boolean p = pathDraftTop.mkdirs();}
        File pathDraft = new File(pathDraftTop + "\\" + emailFolderName);
        boolean createDraft = pathDraft.mkdirs();
        File fDraft = new File(pathDraft + "\\email.html");
        writeFile(e,fDraft,info);

// Try to deal with repeated mail by renaming the file.
// However, this may conflicts the way a client reply an email (will create a new mail instead)
//        while(pathDraft.exists()){
//            emailFolderName = emailFolderName + 1;
//            pathDraft = new File(pathDraftTop + "\\" + emailFolderName);
//        }
//        boolean createDraft = pathDraft.mkdirs();
//        File fDraft = new File(pathDraft + "\\email.html");
//        writeFile(e,fDraft,info);
    }
    
    /**
     * save an Email object to receiver's inbox,sender's sentitems and delete draft
     * @param e an Email object
     */
    public void saveSentEmail(Email e){
        String emailFolderName = e.subject + "_" + e.sender + "_" + e.receiver + "_" + e.cc + "_";
        String info = "<p>Subject: "+ e.subject + "</p>" + 
                      "<p>From: " + e.sender + "</p>" +
                      "<p>To: " + e.receiver + "</p>" +
                      "<p>cc: " + e.cc + "</p>" +
                      "<p>Date: " + e.date + "</p>";
        
        File pathInboxTop = new File(rootFolder + emails + e.receiver + "\\Inbox");
        if(!pathInboxTop.exists()){boolean p = pathInboxTop.mkdirs();}
        File pathReceivedEmail = new File(pathInboxTop + "\\" + emailFolderName); 
        boolean createReceivedEmail = pathReceivedEmail.mkdirs();
        File fReceivedEmail = new File(pathReceivedEmail + "\\email.html");
        writeFile(e,fReceivedEmail,info); 
        
        File pathSentItemsTop = new File(rootFolder + emails + e.sender + "\\SentItems");
        if(!pathSentItemsTop.exists()){boolean p = pathSentItemsTop.mkdirs();}
        File pathSentEmail = new File(pathSentItemsTop + "\\" + emailFolderName);
        boolean createSentEmail = pathSentEmail.mkdirs();
        File fSentEmail = new File(pathSentEmail + "\\email.html");
        writeFile(e,fSentEmail,info);           
        
        File draftFolder = new File(rootFolder + emails + e.sender + "\\Drafts\\" + emailFolderName);
        if(draftFolder.exists()){
            deleteFolder(draftFolder);
        }
    }    
    
    /**
     * delete an Email object
     * @param e an Email object
     */
    public void deleteEmail(Email e){
        //e.location should be http,then convert it to local
        //eg: String flocation = toLocal(e.location);
        //String folderURL = new File(flocation).getParent();
        String folderURL = new File(e.location).getParent();
        File folder = new File(folderURL.substring(6));
        deleteFolder(folder);
    }

    /**
     * save user's attachments(images,zips etc.) to attachments folder and returns
     * a file url in a string form
     * @param username current client
     * @param source   source file from that client
     * @return a string of url
     * @throws MalformedURLException () if the url is not in a correct form
     */
    public String uploadAttachments(String username, File source) throws MalformedURLException{
        String name = source.getName();
        File pathAttachmentTop = new File(rootFolder + emails + username + "\\attachment\\");
        if(!pathAttachmentTop.exists()){boolean p = pathAttachmentTop.mkdirs();}
        String attachmentFolder = "temp";
        File pathAttachment = new File(pathAttachmentTop + "\\" + attachmentFolder);
        int i = 1;
        while(pathAttachment.exists()){
            attachmentFolder = "temp" + i;
            pathAttachment = new File(pathAttachmentTop + "\\" + attachmentFolder);
            i++;
        }
        boolean createAttachment = pathAttachment.mkdirs();
        File fAttachment = new File(pathAttachment + "\\" + name);
        try {
            copyFile(source,fAttachment);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        //should convert fURL to http
        String fURL = new File(fAttachment.getPath()).toURI().toURL().toString();
        return fURL;
    }

    //private-------------------------------------------------------------------
    
    /**
     * read the html code from an email file
     * @param f the file where an email been saved
     * @return the html code of an email content
     */
    private String readEmailContent(File f){
        try {
            BufferedReader BR =new BufferedReader(new FileReader(f));
            //it is ok to read just a single line because there is only one line in the email html
            content = BR.readLine();
            BR.close();            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return content;
    }
    
    /**
     * the process of saving an Email
     * @param e an email that will be saved
     * @param f the file for saving a email
     * @param info information for an email subject,date etc.
     */
    private void writeFile(Email e, File f, String info)
    {
        try {
            FileWriter FW = new FileWriter(f);
            FW.write(info);
            FW.write(e.content);
            FW.close();
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * the process of loading an EmailList for current user
     * @param username current client
     * @param directory where the list of emails been saved
     * @return an array of emails
     */
    private Email[] loadEmailList(String username, String directory){
        String Top = rootFolder + emails + username + "\\" + directory;
        Email[] emailList;
        File pathTop = new File(Top);
        String[] paths = pathTop.list();
        //null pointer ex
        if(paths != null){
            emailList = new Email[paths.length];
            for (int i=0;i<paths.length;i++){
                emailList[i] = new Email();
                StringTokenizer st = new StringTokenizer(paths[i],"_ ");
                emailList[i].subject = st.nextToken();
                emailList[i].sender = st.nextToken();
                emailList[i].receiver = st.nextToken();
                //(no such element ex) because cc can be empty
                if(st.hasMoreTokens()){
                    emailList[i].cc = st.nextToken();
                } else {
                    emailList[i].cc = "";
                }
    //            emailList[i].location = Top + "\\" + paths[i] + "\\email.html"; //can't read
                try {
                    File f = new File(Top + "\\" + paths[i] + "\\email.html");
                    String flocation = new File(f.getPath()).toURI().toURL().toString();
                    //need to convert file url to http
                    emailList[i].location = flocation;
                    emailList[i].content = readEmailContent(f);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            emailList = new Email[0];
        }        
        return emailList;
    }
    
    /**
     * delete a folder for an email
     * @param folder the targeted folder
     */
    private static void deleteFolder(File folder){ //Credit: http://stackoverflow.com/questions/779519/delete-files-recursively-in-java
        //I don't know how to import the FileUtils class from apache's commons io, so choose this recursion method
        if(folder.exists()){
            for(File f: folder.listFiles()){
                if(f.isDirectory()) deleteFolder(f);
                else f.delete();
            }
            folder.delete();
        }
    }
    
    /**
     * copy a source file to destination
     * @param source source file
     * @param dest   destination file
     * @throws IOException if the source file did not transfered properly or the channels did not closed properly
     */
    private static void copyFile(File source,File dest) throws IOException{ //Credit: http://examples.javacodegeeks.com/core-java/io/file/4-ways-to-copy-file-in-java/
        //simple and fast by using filechannel
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel,0,inputChannel.size());
            inputChannel.close();
            outputChannel.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
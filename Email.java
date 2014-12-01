package cs241;

public class Email {
    
    //location = http url of the email file,currently use file url
    //content = html code
    public String subject,content,sender,receiver,cc,date,location;
    
    public Email(){
        this.subject = "";
        this.sender = "";
        this.receiver = "";
        this.content = "";
        this.cc = "";
        this.date = "";
        this.location = "";
    }
       
}

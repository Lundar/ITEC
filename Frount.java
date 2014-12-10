package cs241;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.swing.JOptionPane;
/**
 *
 * @author Austin Lund
 */
public class Frount extends Application {

    NetState networker;
    VBox emailList;
    VBox draftList;
    VBox sentList;
    VBox localList;
    int loaded;
    WebView eDisplay;
    FileManager fileManager;
    Email current;
    
    /**
     * Initalize lists 
     */
    public Frount(){
        fileManager= new FileManager();
        networker=null;   
        loaded =0;
        current=null;
    }
    
    /**
     * populates the list of email buttons from the server
     */
    public void populate(){
        for(int x=1;x<21;x++){
            try{
                if(x+loaded < networker.messages.size()){
                Message m = networker.messages.get(networker.messages.size()-x-loaded);
                Button b= new Button(m.getSubject()+"\n"+m.getFrom()[0]);
                b.setOnAction(new mailButtonListener(m));
                emailList.getChildren().add(b);
                }
            }catch (MessagingException e) {e.printStackTrace();}
        }
        loaded += 20;
        
    }
    
    /**
     * clear and repopulate the local folders from the file system
     */
    public void loadLocal(){
        draftList.getChildren().clear();
        for(Email e:fileManager.loadDrafts("local")){
            Button b= new Button(e.subject+"\n"+e.receiver);
                b.setOnAction(new localButtonListener(e));
                draftList.getChildren().add(b);
        }
            
        sentList.getChildren().clear();
        for(Email e:fileManager.loadSentItems("local")){
            Button b= new Button(e.subject+"\n"+e.receiver);
                b.setOnAction(new localButtonListener(e));
                sentList.getChildren().add(b);
        }
        
        localList.getChildren().clear();
        for(Email e:fileManager.loadInbox("local")){
            Button b= new Button(e.subject+"\n"+e.sender);
                b.setOnAction(new localButtonListener(e));
                localList.getChildren().add(b);
        }
    
    
    }
    
    /**
     * Setup all UI elements on the stage.
     *
     * @param primaryStage Stage: The primary JavaFX app stage object.
     *
     */
    @Override
    public void start(Stage primaryStage) {
 
        
        primaryStage.setTitle("EMAIL!");
        BorderPane pain = new BorderPane();

        emailList = new VBox();
        draftList = new VBox();
        sentList = new VBox();
        localList = new VBox();

        eDisplay = new WebView();
        MenuBar menubar = new MenuBar();
        TabPane folders= new TabPane();
        ScrollPane scrollPain = new ScrollPane(emailList);
        scrollPain.setPrefWidth(200);
        Tab in,local,sent,draft;
        in = new Tab("Inbox");
        sent = new Tab("Sent");
        draft = new Tab("Drafts");
        local = new Tab("Local");
        in.setContent(scrollPain);
        sent.setContent(new ScrollPane(sentList));
        draft.setContent(new ScrollPane(draftList));
        local.setContent(new ScrollPane(localList));
        folders.getTabs().add(in);
        folders.getTabs().add(sent);
        folders.getTabs().add(draft);
        //folders.getTabs().add(local);
        folders.setSide(Side.LEFT);
        folders.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        loadLocal();
        
        pain.setTop(menubar);
        pain.setLeft(folders);
        pain.setCenter(eDisplay);
        
        
        Menu file = new Menu("File");
        MenuItem exit= new MenuItem("Exit");
        MenuItem send= new MenuItem("Compose");
        MenuItem load= new MenuItem("Load ...");
        MenuItem log= new MenuItem("Login");
        MenuItem reload= new MenuItem("Refresh");
        MenuItem sett= new MenuItem("Settings");
        menubar.getMenus().add(file);
        file.getItems().add(log);
        file.getItems().add(send);
        file.getItems().add(reload);
        file.getItems().add(sett);
        file.getItems().add(load);
        file.getItems().add(exit);
        
        
        ChangeListener scrollList = (ChangeListener<Number>) (ObservableValue<? extends Number> ov, Number old_val, Number new_val) -> {
            if(new_val.floatValue()>.9) populate();//System.out.println(new_val);
        };
        scrollPain.vvalueProperty().addListener(scrollList);
        
        send.setOnAction(new sendMail());
        log.setOnAction(new loginListener());
        sett.setOnAction(new settingListener());
        
        EventHandler exitHandle = (EventHandler<Event>) (Event a) -> {
            System.exit(0);
        };
        
        exit.setOnAction(exitHandle);
        primaryStage.setOnCloseRequest(exitHandle);
        
        EventHandler refreshHandle = (EventHandler<Event>) (Event a) -> {
            emailList.getChildren().clear();
            loaded=0;
            if(networker!=null){
                networker.reload();
                populate();
            }
            loadLocal();
        };
        reload.setOnAction(refreshHandle);
        
        EventHandler loadHandle = (EventHandler<Event>) (Event a) -> {
            if(current==null){
                JOptionPane.showMessageDialog(null, "No message selected");
                return;
            }
            
            sendMail ss= new sendMail(current);
            ss.handle(a);                
        };
        load.setOnAction(loadHandle);
        
        primaryStage.setScene(new Scene(pain));
        primaryStage.sizeToScene();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Event Handler for logging in action.
     */
    private class loginListener implements EventHandler{

        TextField uname;
        PasswordField pass;
        Button submit;
        
        
        @Override
        public void handle(Event event) {
            Stage stage = new Stage();
            stage.setTitle("Login");
            VBox things= new VBox();
            uname=new TextField();
            pass=new PasswordField();
            submit=new Button("Login!");
            things.getChildren().add(new Text("Username:"));
            things.getChildren().add(uname);
            things.getChildren().add(new Text("Password:"));
            things.getChildren().add(pass);
            things.getChildren().add(submit);
            
            EventHandler login = (EventHandler<ActionEvent>) (ActionEvent a) -> {
                stage.close();
                networker = new NetState(uname.getText());
                Boolean b = networker.startSession(pass.getText());
                if(b){ JOptionPane.showMessageDialog(null, "Login Successful!"); populate();
                } else { 
                    networker = null;
                    JOptionPane.showMessageDialog(null, "Login Failed!");
                } 
            };
            submit.setOnAction(login);
            pass.setOnAction(login);
            
            stage.setScene(new Scene(things));
            stage.sizeToScene();
            stage.show();
        }
    
    }
    
    /**
     * Event handler for each email "Button"
     * handles loading and displaying emails from the server
     */
    private class mailButtonListener implements EventHandler{

        Message msg;
        
        public mailButtonListener(Message m){
            msg=m;
        }
        
        @Override
        public void handle(Event event) {
            try{
                if(msg.getContent() instanceof Multipart){
                    Multipart mp = (Multipart) msg.getContent();
                    for(int x=0;x<mp.getCount();x++){
                    BodyPart bp = mp.getBodyPart(x);
                    //System.out.println(bp.getContent().toString());
                    if(!(bp.getDisposition() != null && bp.getDisposition().equalsIgnoreCase(Part.ATTACHMENT)))
                    eDisplay.getEngine().loadContent(bp.getContent().toString(),"text/html");//setText(bp.getContent().toString());
                    }
                }else{
                    //System.out.println(msg.getContent().toString());
                    eDisplay.getEngine().loadContent(msg.getContent().toString(),"text/html");
                }
            } catch(Exception e){e.printStackTrace();}
        }
    }
    
    /**
     * Event handler for locally stored emails
     */
        private class localButtonListener implements EventHandler{

        Email msg;
        
        public localButtonListener(Email m){
            msg=m;
        }
        
        @Override
        public void handle(Event event) {
            try{
                    eDisplay.getEngine().loadContent(msg.content,"text/html");
                    current=msg;
            } catch(Exception e){e.printStackTrace();}
        }
    }
    
        /**
         * Event Handler for compose Email button
         * creates a compose email window
         */
    private class sendMail implements EventHandler{

        TextField toAddr;
        TextField fromAddr;
        TextField subject;
        HTMLEditor  compose;
        PasswordField pass;
        Button send;
        Button save;
        Email pre;
        public sendMail(){pre=null;}
        public sendMail(Email e){pre=e;}
        
        @Override
        public void handle(Event event) {
            
            Stage stage = new Stage();
            stage.setTitle("Compose");
            VBox things= new VBox();
            toAddr=new TextField();
            fromAddr=new TextField();
            subject=new TextField();
            compose=new HTMLEditor();
            pass = new PasswordField();
            send=new Button("Send");
            save=new Button("Save Draft");
            things.getChildren().add(new Text("To:"));
            things.getChildren().add(toAddr);
            things.getChildren().add(new Text("From:"));
            things.getChildren().add(fromAddr);
            things.getChildren().add(new Text("Subject:"));
            things.getChildren().add(subject);
            things.getChildren().add(compose);
            things.getChildren().add(new Text("Password:"));
            things.getChildren().add(pass);
            HBox buttons= new HBox();
            buttons.getChildren().add(send);
            buttons.getChildren().add(save);
            things.getChildren().add(buttons);
            
            if(pre!=null){
            toAddr.setText(pre.receiver);
            subject.setText(pre.subject);
            compose.setHtmlText(pre.content);
            }
            
            EventHandler saveHandler = (EventHandler<ActionEvent>) (ActionEvent a) -> {
                Email e = new Email();
                e.sender="local";//fromAddr.getText();
                e.receiver=toAddr.getText();
                e.subject=subject.getText();
                e.content=compose.getHtmlText();
                fileManager.saveDraft(e);
                JOptionPane.showMessageDialog(null, "Draft saved");
            };
            
            EventHandler login = (EventHandler<ActionEvent>) (ActionEvent a) -> {
                if(networker == null){
                    JOptionPane.showMessageDialog(null, "Not logged in!");
                    return;
                }
                stage.close();
                try{
                Message out = networker.getBlank();
                
                out.setFrom(new InternetAddress(fromAddr.getText()));
                out.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr.getText()));
                out.setSubject(subject.getText());
                out.setText(compose.getHtmlText());
                
                networker.sendMail(out,pass.getText());
                JOptionPane.showMessageDialog(null, "Mail Sent!");
                Email e = new Email();
                e.sender="local";
                e.receiver=toAddr.getText();
                e.subject=subject.getText();
                e.content=compose.getHtmlText();
                fileManager.saveSentEmail(e);
                }catch (Exception e){
                    JOptionPane.showMessageDialog(null, "Send Mail failed");
                }
            };
            
            send.setOnAction(login);
            save.setOnAction(saveHandler);
            
            stage.setScene(new Scene(things));
            stage.sizeToScene();
            stage.show();
        }
    
    }
    
    /**
     * settings Event handler
     * creates a new window for settings operations
     */
    private class settingListener implements EventHandler{

        @Override
        public void handle(Event event) {
            Stage stage = new Stage();
            stage.setTitle("Settings");
            VBox things= new VBox();
            TextField protocol=new TextField();
            TextField host=new TextField();
            TextField mport=new TextField();
            TextField sport=new TextField();
            TextField tick=new TextField();
            CheckBox imaptls = new CheckBox("IMAP starttls");
            CheckBox poptls = new CheckBox("POP3 starttls");
            CheckBox smtptls = new CheckBox("SMTP starttls");
            Button save = new Button("Save");
            
            //populate with netstate
            protocol.setText(NetState.protocol);
            host.setText(NetState.host);
            mport.setText(NetState.port+"");
            sport.setText(NetState.sendPort+"");
            tick.setText(NetState.keepAlive+"");
            imaptls.setSelected(NetState.imaptls);
            poptls.setSelected(NetState.poptls);
            smtptls.setSelected(NetState.smtptls);

            
            things.getChildren().add(new Text("Protocol:"));
            things.getChildren().add(protocol);
            things.getChildren().add(new Text("Host:"));
            things.getChildren().add(host);
            things.getChildren().add(new Text("Mail Port:"));
            things.getChildren().add(mport);
            things.getChildren().add(new Text("Send Port:"));
            things.getChildren().add(sport);
            things.getChildren().add(new Text("Heartbeat:"));
            things.getChildren().add(tick);
            things.getChildren().add(imaptls);
            things.getChildren().add(poptls);
            things.getChildren().add(smtptls);
            things.getChildren().add(save);
            
            EventHandler login = (EventHandler<ActionEvent>) (ActionEvent a) -> {
                stage.close();
                NetState.protocol=protocol.getText();
                NetState.host=host.getText();
                NetState.port=Integer.parseInt(mport.getText());
                NetState.sendPort=Integer.parseInt(sport.getText());
                NetState.keepAlive=Integer.parseInt(tick.getText());
                NetState.imaptls=imaptls.selectedProperty().get();
                NetState.poptls=poptls.selectedProperty().get();
                NetState.smtptls=smtptls.selectedProperty().get();
            };
            save.setOnAction(login);
            
            stage.setScene(new Scene(things));
            stage.sizeToScene();
            stage.show();
        }
    
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs241;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
 * @author Zeus
 */
public class Frount extends Application {

    NetState networker;
    VBox emailList;
    int loaded;
    /*TextArea*/ WebView eDisplay;
    
    public Frount(){
    networker=null;   
    loaded =0;
    }
    
    public void populate(){
        /*for(Message m:networker.messages){
            try{
            emailList.getChildren().add(new Button(m.getSubject()+"\n"+m.getFrom()[0]));
            }catch (MessagingException e) {e.printStackTrace();}
        }*/
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
        //ScrollPane scrollList = ;
        eDisplay = new WebView();//TextArea("hi");
        MenuBar menubar = new MenuBar();
        ScrollPane scrollPain = new ScrollPane(emailList);
        
        scrollPain.setPrefWidth(200);
        
        pain.setTop(menubar);
        pain.setLeft(scrollPain);
        pain.setCenter(eDisplay);
        
        //for(int x=0;x<100;x++)
        //    emailList.getChildren().add(new Button("Email\nJoe"+x));
        
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
        
        EventHandler exitHandle = (EventHandler<Event>) (Event a) -> {
            System.exit(0);
        };
        
        exit.setOnAction(exitHandle);
        primaryStage.setOnCloseRequest(exitHandle);
        
        EventHandler refreshHandle = (EventHandler<Event>) (Event a) -> {
            emailList.getChildren().clear();
            loaded=0;
            populate();
        };
        reload.setOnAction(refreshHandle);
        
        primaryStage.setScene(new Scene(pain));
        primaryStage.sizeToScene();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

    
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
    
    private class sendMail implements EventHandler{

        TextField toAddr;
        TextField fromAddr;
        TextField subject;
        TextArea  compose;
        PasswordField pass;
        Button send;
        
        
        @Override
        public void handle(Event event) {
            if(networker == null){
                JOptionPane.showMessageDialog(null, "Not logged in!");
                return;
            }
            
            Stage stage = new Stage();
            stage.setTitle("Login");
            VBox things= new VBox();
            toAddr=new TextField();
            fromAddr=new TextField();
            subject=new TextField();
            compose=new TextArea();
            pass = new PasswordField();
            send=new Button("Send");
            things.getChildren().add(new Text("To:"));
            things.getChildren().add(toAddr);
            things.getChildren().add(new Text("From:"));
            things.getChildren().add(fromAddr);
            things.getChildren().add(new Text("Subject:"));
            things.getChildren().add(subject);
            things.getChildren().add(compose);
            things.getChildren().add(new Text("Password:"));
            things.getChildren().add(pass);
            things.getChildren().add(send);
            
            EventHandler login = (EventHandler<ActionEvent>) (ActionEvent a) -> {
                stage.close();
                try{
                Message out = networker.getBlank();
                
                out.setFrom(new InternetAddress(fromAddr.getText()));
                out.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddr.getText()));
                out.setSubject(subject.getText());
                out.setText(compose.getText());
                
                networker.sendMail(out,pass.getText());
                    JOptionPane.showMessageDialog(null, "Mail Sent!");
                }catch (Exception e){
                    JOptionPane.showMessageDialog(null, "Send Mail failed");
                }
            };
            
            send.setOnAction(login);
            
            stage.setScene(new Scene(things));
            stage.sizeToScene();
            stage.show();
        }
    
    }
    
    
}

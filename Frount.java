/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package cs241;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Zeus
 */
public class Frount extends Application {


    
    
    
    public void addEmail(Email m){}
    
    
    
    
    
    
    
    
    
    
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

        
        VBox emailList = new VBox();
        //ScrollPane scrollList = ;
        TextArea eDisplay = new TextArea("hi");
        MenuBar menubar = new MenuBar();
        
        pain.setTop(menubar);
        pain.setLeft(new ScrollPane(emailList));
        pain.setCenter(eDisplay);
        
        for(int x=0;x<10;x++)
            emailList.getChildren().add(new Button("Email\nJoe"));
        
        Menu file = new Menu("File");
        MenuItem exit= new MenuItem("Exit");
        MenuItem load= new MenuItem("Load ...");
        menubar.getMenus().add(file);
        file.getItems().add(load);
        file.getItems().add(exit);
        
        primaryStage.setScene(new Scene(pain));
        primaryStage.sizeToScene();
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

import java.sql.SQLException;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import java.sql.Timestamp;

/**
 *
 * @author Mesa
 */
/*
Tasks:  Log-in form in FX, localize login and error control messages into 2+ languages
            --Hash/Encrypt user password?
        Add/Update/Delete customer records -- modifies 4 tables
            --Create helper class/interface?  AddCust() DeleteCust() UpdateCust() methods
            --Customer objects?  Can be instantiated and acted upon, stored in a map
        Add/Update/Delete appointments
            --Helper class as above?
        View calendar by month or week
            --Create GUI interface, load all appointments
            --Query by user with threadpool, load into map and sort?
        Localize/DST appointment times
        Exception controls/lambdas: see design doc
        Alert if appointments within 15 minutes of login
        Generate three reports: Appt types by month, schedule per consultant, one additional
            --Format for reports? .txt .docx .xlsx
            --Appointment types: Physical, VoIP, Phone
        Log for user logins w/ timestamps

*/
public class C195FinalProject extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        
        Scene scene = GetLogin();
        GetCalendar();
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    public Scene GetLogin(){
        Button btnLogin = new Button();        
        Label lblName = new Label("Name:");
        Label lblPass = new Label("Password:");
        TextField txtName = new TextField();
        PasswordField txtPass = new PasswordField();
        btnLogin.setText("Login");
        btnLogin.setOnAction(event -> {
            try{if(txtPass.getText().equals(SQLHelper.GetPass(txtName.getText()).toString())){
                   System.out.println("");
               }
            }            
            catch(SQLException|NullPointerException e){e.getMessage();}
            }
        );
        GridPane login = new GridPane();
        //login.setGridLinesVisible(true);
        login.setHgap(3);
        login.setVgap(5);
        login.setPadding(new Insets(25,25,25,25));
        GridPane.setConstraints(btnLogin,1,3);
        GridPane.setConstraints(lblName,0,0);
        GridPane.setConstraints(lblPass,0,1);
        GridPane.setConstraints(txtName,1,0);
        GridPane.setConstraints(txtPass,1,1);
        login.getChildren().addAll(btnLogin,lblName,lblPass,txtName,txtPass);
        Scene loginScene = new Scene(login,300,250);
        return loginScene;
    }
    
    public Scene GetCalendar(){
        BorderPane calPane = new BorderPane();
        
        //top panel creation
        GridPane paneTop = new GridPane();
        Button btnWeek = new Button();
        Button btnMonth = new Button();
        btnWeek.setText("Week View");
        btnMonth.setText("Month View");
        GridPane.setConstraints(btnWeek,0,0);
        GridPane.setConstraints(btnMonth,1,0);
        paneTop.getChildren().addAll(btnWeek,btnMonth);
        //end top panel creation
        //left side creation
        GridPane leftSide = new GridPane();
        leftSide.setGridLinesVisible(true);
        leftSide.setHgap(20);
        leftSide.setVgap(50);
        leftSide.setPadding(new Insets(25,25,25,25));
        //end left side creation        
        //right side creation
        ScrollPane rightSide = new ScrollPane();
        rightSide.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        //end right side creation        
        //center panel creation
        Button[] btnMonthArray = new Button[31];
        Button[] btnWeekArray = new Button[7];
        
        //end center panel creation
        
        Button insert = new Button();
        Button update = new Button();
        calPane.setLeft(leftSide);
        calPane.setRight(rightSide);
        calPane.setTop(paneTop);
        Scene calScene = new Scene(calPane);
        return calScene;
    }
    
    public Scene GetAppointments(){
        BorderPane apptPane = new BorderPane();
        ComboBox apptCBox = new ComboBox();
        
        
        Scene apptScene = new Scene(apptPane);
        return apptScene;
    }
}

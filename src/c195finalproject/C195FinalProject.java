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
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;

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
    
    private Stage mainStage;
    @Override
    public void start(Stage primaryStage) {
        
        mainStage = primaryStage;
        //Scene scene = GetLogin();
        Scene scene = GetLogin();
        primaryStage.setTitle("C195 Inc. Login");
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
        Label loginError = new Label("An error occured with your login.\nTry again or call the help desk at ext#555.");
        loginError.setVisible(false);        
        TextField txtName = new TextField();
        PasswordField txtPass = new PasswordField();
        btnLogin.setText("Login");
        btnLogin.setOnAction(event -> {
            try{if(txtPass.getText().equals(SQLHelper.GetPass(txtName.getText()).toString())){
                   Scene loadCal = GetCalendar(txtName.getText());
                   mainStage.setScene(loadCal);
                   mainStage.show();
               }
            else{
                loginError.setVisible(true);
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
        GridPane.setConstraints(loginError,0,5,2,4);
        login.getChildren().addAll(btnLogin,lblName,lblPass,txtName,txtPass,loginError);
        Scene loginScene = new Scene(login,300,250);
        return loginScene;
    }
    
    public Scene GetCalendar(String curUser){
        BorderPane calPane = new BorderPane();
        Label timer = new Label();
        ScheduledService<Void> startTimer = new ScheduledService<Void>(){
            @Override
            protected Task<Void> createTask(){
            return new Task<Void>()
                {        
                @Override
                protected Void call()
                {
                    Platform.runLater(() ->{
                        LocalTime time = LocalTime.now(ZoneId.systemDefault());
                        time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT));
                        timer.setText(time.toString());});            
                        return null;
                }
                };            
            };
        };
        //ScheduledExecutorService startTimer = Executors.newSingleThreadScheduledExecutor();
        // <editor-fold defaultstate="collapsed" desc="top panel creation">
        GridPane paneTop = new GridPane();
        paneTop.setGridLinesVisible(true);
        paneTop.setHgap(5);
        paneTop.setPadding(new Insets(5,0,10,5));
        Button btnWeek = new Button();
        Button btnMonth = new Button();
        Button btnExit = new Button();
        btnExit.setText("Exit");
        btnExit.setOnAction(event -> {if(!startTimer.isRunning())startTimer.cancel();mainStage.close();});
        btnWeek.setText("Week View");
        btnMonth.setText("Month View");
        GridPane.setConstraints(btnWeek,0,0);
        GridPane.setConstraints(btnMonth,1,0);
        GridPane.setConstraints(btnExit,2,0);
        paneTop.getChildren().addAll(btnWeek,btnMonth,btnExit);
        // </editor-fold> end top panel creation
        
        // <editor-fold defaultstate="collapsed" desc="left side creation">
        GridPane leftSide = new GridPane();
        leftSide.setGridLinesVisible(true);
        leftSide.setHgap(20);
        leftSide.setVgap(50);
        leftSide.setPadding(new Insets(25,25,25,25));
        // </editor-fold> end left side creation        
        
        // <editor-fold defaultstate="collaped" desc="right side creation">
        ScrollPane rightSide = new ScrollPane();
        rightSide.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        
        // </editor-fold>end right side creation    
        
        //center panel creation
        GridPane paneCenter = new GridPane();
        Label lblCenter = new Label();
        GridPane.setConstraints(lblCenter,0,8,2,2);
        paneCenter.getChildren().add(lblCenter);
        paneCenter.setGridLinesVisible(true);
        Button[] btnMonthArray = new Button[31];
        Button[] btnWeekArray = new Button[7];
        for(int i = 0; i < btnMonthArray.length;i++)
        {
            Button btn = new Button();
            btn.setText("January " + (i + 1));
            btn.setMaxWidth(Double.MAX_VALUE);
            GridPane.setConstraints(btn, i % 7, i / 7);
            btn.setOnAction(event ->{lblCenter.setText(btn.getText());});
            paneCenter.getChildren().add(btn);
        }       
        //end center panel creation
        
        //bottom panel creation
              
        //GetTime(timer);
        HBox paneBottom = new HBox();
        paneBottom.setPadding(new Insets(0,10,0,0));
        paneBottom.setAlignment(Pos.BASELINE_RIGHT);
        paneBottom.getChildren().add(timer);
        try{
            startTimer.setPeriod(Duration.seconds(.001));
            startTimer.start();
        }
        catch(Exception e){System.out.println(e.getMessage());System.out.println("Failed to run");}
        //end bottom panel creation
        
        Button insert = new Button();
        Button update = new Button();
        calPane.setLeft(leftSide);
        calPane.setRight(rightSide);
        calPane.setTop(paneTop);
        calPane.setCenter(paneCenter);
        calPane.setBottom(paneBottom);
        Scene calScene = new Scene(calPane,800,600);
        return calScene;
    }
    
    public Scene GetAppointments(){
        BorderPane apptPane = new BorderPane();
        ComboBox apptCBox = new ComboBox();
        
        
        Scene apptScene = new Scene(apptPane);
        return apptScene;
    }
    
}

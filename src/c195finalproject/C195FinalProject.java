/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

import java.io.IOException;
import java.sql.SQLException;
import javafx.application.Application;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import javafx.util.Duration;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;


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
    private Stage altStage;    
    private static final Locale MYLOCALE = Locale.getDefault();
    private static final ResourceBundle RB = ResourceBundle.getBundle("c195finalproject/C195properties",MYLOCALE);
    
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        //Scene scene = GetLogin();
        Scene scene = GetCalendar("test");
        primaryStage.setTitle("C195 Inc. Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{Logging.Init();}
        catch(IOException e){System.out.println("Unable to log this session.");}        
        launch(args);
    }
    
    public Scene GetLogin(){
        Button btnLogin = new Button();        
        Label lblName = new Label(RB.getString("username") + ":");
        Label lblPass = new Label(RB.getString("userpass") + ":");
        Label loginError = new Label(RB.getString("loginError") + System.lineSeparator() + RB.getString("helpDesk"));
        loginError.setVisible(false);
        loginError.setWrapText(true);
        TextField txtName = new TextField();
        PasswordField txtPass = new PasswordField();
        btnLogin.setText("Login");
        btnLogin.setOnAction(event -> {
            try{if(txtPass.getText().equals(SQLHelper.GetPass(txtName.getText()).toString()) && (txtName.getText().length() > 0)){
                   Logging.StampLog(txtName.getText());
                   Scene loadCal = GetCalendar(txtName.getText());
                   mainStage.setScene(loadCal);
                   mainStage.show();
               }
                else{
                    loginError.setVisible(true);
                }
            }            
            catch(SQLException|NullPointerException e){System.out.println(e.getMessage());}
            }
        );
        GridPane login = new GridPane();
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
        
        // <editor-fold defaultstate="collapsed" desc="variable declarations and tasks">
        BorderPane calPane = new BorderPane();        
        GridPane paneTop = new GridPane();
        VBox leftSide = new VBox();
        ScrollPane rightSide = new ScrollPane();
        GridPane paneCenterWeek = new GridPane();
        GridPane paneCenterMonth = new GridPane();
        HBox paneBottom = new HBox();
        
        Month currMonth = Month.from(LocalDate.now());
        Boolean leapYear = LocalDate.now().getYear()%4 == 0;
        
        Button btnWeek = new Button();
        Button btnMonth = new Button();
        Button btnExit = new Button();
        Button btnInsert = new Button();
        Button btnUpdate = new Button();
        Button btnDelete = new Button();
        Button[] btnMonthArray = new Button[currMonth.length(leapYear)];
        Button[] btnWeekArray = new Button[7];
        
        Label lblTimer = new Label();
        Label lblCenterMonth = new Label();
        Label lblCenterWeek = new Label();
        
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
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
                        lblTimer.setText(time.format(timeFormat));
                    });            
                    return null;
                }
                };            
            };
        };
        // </editor-fold>
        
        // <editor-fold defaultstate="collapsed" desc="top panel creation">
        paneTop.setGridLinesVisible(true);
        paneTop.setHgap(5);
        paneTop.setPadding(new Insets(5,0,10,5));
        btnExit.setText("Exit");
        btnExit.setOnAction(event -> {mainStage.setScene(GetLogin());mainStage.show();});
        btnWeek.setText("Week View");
        btnMonth.setText("Month View");
        GridPane.setConstraints(btnWeek,0,0);
        GridPane.setConstraints(btnMonth,1,0);
        GridPane.setConstraints(btnExit,2,0);
        paneTop.getChildren().addAll(btnWeek,btnMonth,btnExit);
        // </editor-fold> 
        //end top panel creation
        
        // <editor-fold defaultstate="collapsed" desc="left side creation">
        btnInsert.setText("Insert Appointment"); btnInsert.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setText("Update Appointment"); btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setText("Delete Appointment"); btnDelete.setMaxWidth(Double.MAX_VALUE);
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(15);
        leftSide.setPadding(new Insets(0,0,0,0));
        leftSide.getChildren().addAll(btnInsert,btnUpdate,btnDelete);
        // </editor-fold> 
        //end left side creation        
        
        // <editor-fold defaultstate="collaped" desc="right side creation">
        rightSide.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        rightSide.setPrefWidth(80);
        try{
           /* TreeMap<Integer,Appointment> apptMap = SQLHelper.GetAppointments(curUser);
            TextFlow rightText = new TextFlow();
            Collection<Appointment> values = apptMap.values();
            values.forEach(value -> {rightText.getChildren().add(new TextField(value.toString()));});*/
            TreeMap<Integer,Customer> apptMap = SQLHelper.GetCustomers();
            TextFlow rightText = new TextFlow();
            Collection<Customer> values = apptMap.values();
            values.forEach(value -> {rightText.getChildren().add(new TextField(value.toString()));});
            rightSide.setContent(rightText);
        }
        catch(SQLException|NullPointerException e){
            System.out.println("Error retrieving appointments||Null value returned");
        }
        // </editor-fold>
        //end right side creation    
        
        // <editor-fold defaultstate="collapsed" desc="center panel creation">
        GridPane.setConstraints(lblCenterMonth,0,8,2,2);
        paneCenterMonth.getChildren().add(lblCenterMonth);
        paneCenterMonth.setGridLinesVisible(true);
        GridPane.setConstraints(lblCenterWeek,0,2,2,2);
        paneCenterWeek.getChildren().add(lblCenterWeek);
        paneCenterWeek.setGridLinesVisible(true);        
        for(int i = 0; i < btnMonthArray.length;i++)
        {
            Button btn = new Button();
            btn.setText(currMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()) + (i + 1));
            btn.setMaxWidth(Double.MAX_VALUE);
            GridPane.setConstraints(btn, i % 7, i / 7);
            btn.setOnAction(event ->{lblCenterMonth.setText(btn.getText());});
            paneCenterMonth.getChildren().add(btn);
        }
        for(int i = 0; i < btnWeekArray.length; i++)
        {
            Button btn = new Button();
            btn.setText(LocalDate.now(ZoneId.systemDefault()).plusDays(i).toString());
            btn.setMaxWidth(Double.MAX_VALUE);
            GridPane.setConstraints(btn, i, 0);
            btn.setOnAction(event -> {lblCenterWeek.setText(btn.getText());});
            paneCenterWeek.getChildren().add(btn);
        }
        btnMonth.setOnAction(event -> {calPane.setCenter(paneCenterMonth);});
        btnWeek.setOnAction(event -> {calPane.setCenter(paneCenterWeek);});
        // </editor-fold> 
        //end center panel creation
        
        // <editor-fold defaultstate="collapsed" desc="bottom panel creation">
              
        //GetTime(timer);
        paneBottom.setPadding(new Insets(0,10,0,0));
        paneBottom.setAlignment(Pos.BASELINE_RIGHT);
        paneBottom.getChildren().add(lblTimer);
        try{
            startTimer.setPeriod(Duration.seconds(.01));
            startTimer.start();
        }
        catch(Exception e){System.out.println(e.getMessage());System.out.println("Failed to run");}
        // </editor-fold>
        //end bottom panel creation
        
        
        calPane.setLeft(leftSide);
        calPane.setRight(rightSide);
        calPane.setTop(paneTop);
        calPane.setCenter(paneCenterWeek);
        calPane.setBottom(paneBottom);
        Scene calScene = new Scene(calPane,800,450);
        return calScene;
    }
    
    public Scene GetAppointments(String curUser) throws SQLException{
        BorderPane apptPane = new BorderPane();
        ComboBox apptCBox = new ComboBox();
        try{
            TreeMap<Integer,Object> apptMap = SQLHelper.GetAppointments(curUser);
        }
        catch(SQLException e){
            
        }
        Scene apptScene = new Scene(apptPane);
        return apptScene;
    }
    
    public Scene GetCustomers(String curUser){
        BorderPane custPane = new BorderPane();
        Scene custScene = new Scene(custPane,800,600);
        return custScene;
    }
    
}

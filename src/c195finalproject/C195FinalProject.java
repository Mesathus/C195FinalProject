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
import java.time.LocalDateTime;
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
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;
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
        Scene scene = GetAppointments("test");
        primaryStage.setTitle(RB.getString("loginTitle"));
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
        btnLogin.setText(RB.getString("login"));
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
        Button btnEditCust = new Button();
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
        btnExit.setOnAction(event -> {Platform.exit();});
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
        btnEditCust.setText(RB.getString("btnEditCust")); btnEditCust.setMaxWidth(Double.MAX_VALUE);
        btnEditCust.setOnAction(event -> 
                                {altStage = new Stage();
                                altStage.setTitle("Customer Edits");
                                Scene scene = GetCustomers(curUser);
                                altStage.setScene(scene);
                                altStage.show();
                                });
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(15);
        leftSide.setPadding(new Insets(0,0,0,0));
        leftSide.getChildren().addAll(btnInsert,btnUpdate,btnDelete,btnEditCust);
        // </editor-fold> 
        //end left side creation        
        
        // <editor-fold defaultstate="collaped" desc="right side creation">
        rightSide.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        rightSide.setPrefWidth(120);
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
        catch(Exception e){System.out.println(e.getMessage());System.out.println("Failed to start clock.");}
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
    
    public Scene GetAppointments(String curUser){
        
        BorderPane apptPane = new BorderPane();
        ComboBox apptCBox = new ComboBox();
        TextFlow apptFlow = new TextFlow();
        ScrollPane apptList = new ScrollPane();
        Alert altApptDBError = new Alert(AlertType.ERROR); altApptDBError.setContentText("An error occured when retrieving the appointment list.");
        Alert altApptListEmpty = new Alert(AlertType.ERROR); altApptListEmpty.setContentText("The appointment list is empty.");
        Alert altApptSoon = new Alert(AlertType.INFORMATION); altApptSoon.setContentText("You have an appointment in the next 15 minutes.");
        
        apptPane.setPrefSize(800, 400);
        
        TreeMap<Integer,Object> apptMap;
        try{
                LocalDateTime startTime = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1, 0, 0);
                LocalDateTime endTime = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getMonth().length(LocalDate.now().getYear()%4 == 0), 23, 59);
                if(startTime.plusDays(7).isAfter(endTime)) endTime = endTime.plusDays(7);
                apptMap = SQLHelper.GetAppointments(curUser, startTime, endTime);
                apptMap.values().forEach(value -> {
                    TextField nextAppt = new TextField(value.toString());
                    nextAppt.setEditable(false);
                    apptFlow.getChildren().add(nextAppt);
                    //apptCBox.getItems().add(value.toString());
                });                
        }
        catch(SQLException e){altApptDBError.show();}
        catch(NullPointerException e){altApptListEmpty.show(); System.out.println("Query returned a null map.");}
        
        
        apptList.setContent(apptFlow);
        apptPane.setRight(apptList);
        Scene apptScene = new Scene(apptPane);
        return apptScene;
    }
    
    public Scene GetCustomers(String curUser){
        // <editor-fold defaultstate="collapsed" desc="creating form components">
        GridPane custPane = new GridPane();
        Scene custScene = new Scene(custPane,900,300);
        ComboBox cityBox = new ComboBox();        
        ComboBox countryBox = new ComboBox();
        ComboBox activeBox = new ComboBox(); activeBox.getItems().addAll(RB.getString("active"),RB.getString("inactive"));
        Button btnCreateCust = new Button(RB.getString("btnCreateCust")); btnCreateCust.setMaxWidth(Double.MAX_VALUE);
        Button btnUpdateCust = new Button(RB.getString("btnUpdateCust")); btnUpdateCust.setMaxWidth(Double.MAX_VALUE);
        Button btnDeleteCust = new Button(RB.getString("btnDeleteCust")); btnDeleteCust.setMaxWidth(Double.MAX_VALUE);
        Label lblFName = new Label(RB.getString("lblfirstName"));
        Label lblLName = new Label(RB.getString("lbllastName"));
        Label lblAddr1 = new Label(RB.getString("lbladdrOne"));
        Label lblAddr2 = new Label(RB.getString("lbladdrTwo"));
        Label lblPostCode = new Label(RB.getString("lblpostCode"));
        Label lblActive = new Label(RB.getString("lblactive"));
        Label lblPhone = new Label(RB.getString("lblphone"));
        Label lblCity = new Label(RB.getString("lblcity"));
        Label lblCountry = new Label(RB.getString("lblcountry"));
        Label lblReq = new Label(RB.getString("lblrequired"));
        TextField txtFName = new TextField();
        TextField txtLName = new TextField();
        TextField txtAddr1 = new TextField();
        TextField txtAddr2 = new TextField();
        TextField txtPostCode = new TextField();
        TextField txtPhone = new TextField();
        TextField txtID = new TextField();
        Alert altEmptyField = new Alert(AlertType.INFORMATION); altEmptyField.setContentText("A value must be entered in all required fields.");
        Alert altDBError = new Alert(AlertType.ERROR); altDBError.setContentText("An error occured when processing your database request.");
        ScrollPane custList = new ScrollPane();
        TextFlow custFlow = new TextFlow();
        // </editor-fold>
        
        //other variable and objects
        TreeMap<Integer,TreeMap> CiCo;
        TreeMap<Integer,String> cityMap, countryMap;
        TreeMap<Integer,Customer> custMap = new TreeMap();
        
        //load customer data
        try{custMap = SQLHelper.GetCustomers();}
        catch(SQLException e){System.out.println("Unable to load customer list.");}
        if(custMap.size() > 0){
            custMap.values().forEach(value -> {
                TextField nextCust = new TextField(value.toString());
                nextCust.setEditable(false);
                nextCust.setOnMouseReleased(event -> {
                    String[] nameArr = value.getName().split(" ");
                    txtFName.setText(nameArr[0]);
                    txtLName.setText(nameArr[1]);
                    txtAddr1.setText(value.getAddr()[0]);
                    txtAddr2.setText(value.getAddr()[1]);
                    txtPostCode.setText(value.getZip());
                    txtPhone.setText(value.getPhone());
                    if(value.getActive()) activeBox.setValue(RB.getString("active"));
                    else activeBox.setValue(RB.getString("inactive"));
                    cityBox.setValue(value.getCity());
                    countryBox.setValue(value.getCountry());
                    txtID.setText(value.getID().toString());
                });
                custFlow.getChildren().add(nextCust);
            });
        }
        
        // <editor-fold defaultstate="collapsed" desc="setting grid positions">
        GridPane.setConstraints(cityBox,4,3);
        GridPane.setConstraints(countryBox,4,4);
        GridPane.setConstraints(activeBox,4,0);
        GridPane.setConstraints(btnCreateCust,0,6);
        GridPane.setConstraints(btnUpdateCust,1,6);
        GridPane.setConstraints(btnDeleteCust,2,6);
        GridPane.setConstraints(lblFName,0,0);
        GridPane.setConstraints(lblLName,0,1);
        GridPane.setConstraints(lblAddr1,0,2);
        GridPane.setConstraints(lblAddr2,0,3);
        GridPane.setConstraints(lblPostCode,0,4);
        GridPane.setConstraints(lblActive,3,0);
        GridPane.setConstraints(lblPhone,3,2);
        GridPane.setConstraints(lblCity,3,3);
        GridPane.setConstraints(lblCountry,3,4);
        GridPane.setConstraints(lblReq,0,5,4,1);
        GridPane.setConstraints(txtFName,1,0);
        GridPane.setConstraints(txtLName,1,1);
        GridPane.setConstraints(txtAddr1,1,2);
        GridPane.setConstraints(txtAddr2,1,3);
        GridPane.setConstraints(txtPostCode,1,4);
        GridPane.setConstraints(txtPhone,4,2);
        GridPane.setConstraints(custList,5,0,1,8);
        // </editor-fold>
        
        custFlow.setPrefWidth(80);
        custPane.setPadding(new Insets(20,5,5,10));
        custList.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        custList.setPrefWidth(120);
        custList.setContent(custFlow);
        txtID.setVisible(false);
        
        //<editor-fold defaultstate="collapsed" desc="Button event handlers">
        btnCreateCust.setOnAction(event -> {
            //String custName, String address, Boolean active,String postCode, String phone, String city, String country
                try{
                    StringBuilder filler = new StringBuilder(); filler.insert(0,"\u0020"); filler.insert(0,txtAddr2.getText());
                    Boolean bool = activeBox.getValue().equals(RB.getString("active"));
                    Customer cust = new Customer(txtFName.getText() + " " + txtLName.getText(),
                                                txtAddr1.getText() + "," + filler, bool,
                                                txtPostCode.getText(),txtPhone.getText(),
                                                cityBox.getValue().toString(),countryBox.getValue().toString());
                    if(SQLHelper.Insert(cust, curUser)){mainStage.setScene(GetCustomers(curUser)); mainStage.show();}
                }
                catch(NullPointerException|ArrayIndexOutOfBoundsException e){altEmptyField.show();}
                catch(SQLException e){altDBError.show();}
            });
        btnUpdateCust.setOnAction(event -> {
                try{
                    SQLHelper.PurgeAddr();
                    StringBuilder filler = new StringBuilder(); filler.insert(0,"\u0020"); filler.insert(0,txtAddr2.getText());
                    Boolean bool = activeBox.getValue().equals(RB.getString("active"));
                    Customer cust = new Customer(Integer.parseInt(txtID.getText()),txtFName.getText() + " " + txtLName.getText(),
                                                txtAddr1.getText() + "," + filler, bool,
                                                txtPostCode.getText(),txtPhone.getText(),
                                                cityBox.getValue().toString(),countryBox.getValue().toString());
                    if(SQLHelper.Update(cust, curUser)){mainStage.setScene(GetCustomers(curUser)); mainStage.show();}
                }
                catch(NullPointerException|ArrayIndexOutOfBoundsException e){altEmptyField.show();}
                catch(SQLException e){altDBError.show();}
            });
        btnDeleteCust.setOnAction(event -> {
                try{
                    StringBuilder filler = new StringBuilder(); filler.insert(0,"\u0020"); filler.insert(0,txtAddr2.getText());
                    Boolean bool = activeBox.getValue().equals(RB.getString("active"));
                    Customer cust = new Customer(Integer.parseInt(txtID.getText()),txtFName.getText() + " " + txtLName.getText(),
                                                txtAddr1.getText() + "," + filler, bool,
                                                txtPostCode.getText(),txtPhone.getText(),
                                                cityBox.getValue().toString(),countryBox.getValue().toString());
                    if(SQLHelper.Delete(cust)){mainStage.setScene(GetCustomers(curUser)); mainStage.show();}
                }
                catch(NullPointerException|ArrayIndexOutOfBoundsException e){altEmptyField.show();}
                catch(SQLException e){altDBError.show();}
            });
        //</editor-fold>
        
        try{
            CiCo = SQLHelper.GetCiCo();
            cityMap = CiCo.get(0);
            countryMap = CiCo.get(1);
            cityMap.values().forEach(value -> {cityBox.getItems().add(value);});
            countryMap.values().forEach(value -> {countryBox.getItems().add(value);});
        }
        catch(SQLException e){
            System.out.println("An error occured retrieving the city/country lists.");
        }
        
        custPane.getChildren().addAll(cityBox,countryBox,activeBox,btnCreateCust,btnUpdateCust,btnDeleteCust,
                                      lblFName,lblLName,lblAddr1,lblAddr2,lblPostCode,lblActive,lblPhone,lblCity,lblCountry,lblReq,
                                      txtFName,txtLName,txtAddr1,txtAddr2,txtPostCode,txtPhone,txtID,
                                      custList);
        return custScene;
    }
    
}


package c195finalproject;

import java.io.IOException;
import java.sql.SQLException;
import java.time.DateTimeException;
import java.time.DayOfWeek;
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
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.TextStyle;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
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
    
    //create stages and loading resource bundle
    private Stage mainStage;
    private Stage altStage;    
    private static final Locale MYLOCALE = Locale.getDefault();
    private static final ResourceBundle RB = ResourceBundle.getBundle("c195finalproject/C195properties",MYLOCALE);
    private TreeMap<Integer,Appointment> apptMap;
    private TreeMap<Integer,Customer> custMap;
    
    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        Scene scene = GetLogin();
        //Scene scene = GetCalendar("test"); //to skip the login step to expedite testing
        //try{SQLHelper.PurgeAddr();}catch(SQLException e){} //repurposed to add cities/countries/users if DB is purged
        primaryStage.setTitle(RB.getString("loginTitle"));
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{Logging.Init();}  //create the directory/file for the daily log if it doesn't exist
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
            try{if(txtPass.getText().equals(SQLHelper.GetPass(txtName.getText()).toString()) && (txtName.getText().length() > 0)){ //SQL function to retrieve password, and ensure something was entered
                   Logging.StampLog(txtName.getText(),"successful");     //Logging class creates an entry in the daily log
                   Scene loadCal = GetCalendar(txtName.getText());
                   mainStage.setScene(loadCal);
                   mainStage.show();
               }
                else{
                    Logging.StampLog(txtName.getText(),"failed");
                    loginError.setVisible(true);
                }
            }            
            catch(SQLException|NullPointerException e){System.out.println(e.getMessage());}
            }
        );
        
        //GUI formatting
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
        TextFlow rightText = new TextFlow();
        
        Month currMonth = Month.from(LocalDate.now());
        Boolean leapYear = LocalDate.now().getYear()%4 == 0;
        
        Button btnWeek = new Button();
        Button btnMonth = new Button();
        Button btnExit = new Button();
        Button btnEditAppt = new Button();
        Button btnEditCust = new Button();
        Button btnReports = new Button();
        Button[] btnMonthArray = new Button[currMonth.length(leapYear)];
        Button[] btnWeekArray = new Button[7];        
        
        Label lblTimer = new Label();
        Label lblCenterMonth = new Label();
        Label lblCenterWeek = new Label();
        Label lblApptDescW = new Label();
        Label lblApptDescM = new Label();
        
        Alert altApptDBError = new Alert(AlertType.ERROR); altApptDBError.setContentText("An error occured when retrieving the appointment list.");
        Alert altApptListEmpty = new Alert(AlertType.ERROR); altApptListEmpty.setContentText("The appointment list is empty.");
        Alert altApptSoon = new Alert(AlertType.INFORMATION); altApptSoon.setContentText("You have an appointment in the next 15 minutes.");
        
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        //create task to run a clock in a separate thread
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm:ss a");
        ScheduledService<Void> startTimer = new ScheduledService<Void>(){            
            @Override
            protected Task<Void> createTask(){
            return new Task<Void>()
                {        
                @Override
                protected Void call()
                {
                    //lambda to prepare the timer to run in the GUI
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
        btnExit.setOnAction(event -> {Platform.exit();});  //lambda assigning the event handler to close the program
        btnExit.setCancelButton(true);
        btnWeek.setText("Week View");
        btnMonth.setText("Month View");
        GridPane.setConstraints(btnWeek,0,0);
        GridPane.setConstraints(btnMonth,1,0);
        GridPane.setConstraints(btnExit,2,0);
        paneTop.getChildren().addAll(btnWeek,btnMonth,btnExit);
        
        // </editor-fold> 
        //end top panel creation
        
        // <editor-fold defaultstate="collapsed" desc="left side creation">
        btnEditAppt.setText(RB.getString("btnEditAppt")); btnEditAppt.setMaxWidth(Double.MAX_VALUE);
        btnEditCust.setText(RB.getString("btnEditCust")); btnEditCust.setMaxWidth(Double.MAX_VALUE);
        btnReports.setText(RB.getString("btnReports")); btnReports.setMaxWidth(Double.MAX_VALUE);
        btnEditCust.setOnAction(event -> //lambda assigning event to the customer edit form button
                                {altStage = new Stage();
                                altStage.setTitle("Customer Edits");
                                Scene scene = GetCustomers(curUser);
                                altStage.setScene(scene);
                                altStage.show();
                                });
        btnEditAppt.setOnAction(event -> //lambda assigning event to the appointment edit form button
                                {altStage = new Stage();
                                altStage.setTitle("Appointment Edits");
                                Scene scene = EditAppointments(curUser);
                                altStage.setScene(scene);
                                altStage.show();
                                });
        btnReports.setOnAction(event -> 
                                {altStage = new Stage();
                                altStage.setTitle("View Reports");
                                Scene scene = GetReports(curUser);
                                altStage.setScene(scene);
                                altStage.show();
                                });
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(15);
        leftSide.setPadding(new Insets(0,0,0,0));
        leftSide.getChildren().addAll(btnEditAppt,btnEditCust,btnReports);
        // </editor-fold> 
        //end left side creation        
        
        // <editor-fold defaultstate="collaped" desc="right side creation">
        rightSide.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        rightSide.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
        rightSide.setPrefWidth(200);
        rightText.setPrefWidth(180);
        rightSide.setContent(rightText);
        try{
            //create date variables to retrieve calendar data from the database
            LocalDateTime startTime = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getMonth().length(LocalDate.now().getYear()%4 == 0), 23, 59).plusDays(7); //add 7 days to ensure we get a weeks worth of calendar
            apptMap = SQLHelper.GetAppointments(curUser, startTime, endTime);            
            
            Collection<Appointment> values = apptMap.values(); //convert map values to a collection so we can use it as a stream
            Long count = values.stream()
                    .filter(x -> x.getStart()
                        .toLocalDate()
                        .format(formatDate)
                        .matches(ChronoLocalDateTime.from(LocalDateTime.now()).toLocalDate().format(formatDate)))  //filter to current day
                    .filter(x -> x.getStart()
                            .toLocalTime()
                            .isBefore(LocalTime.now().plusMinutes(15)))  
                    .filter(x -> x.getStart()
                            .toLocalTime()
                            .isAfter(LocalTime.now()))  //two filters to find appointments that begin 15 minutes from loading the form
                    .count();
            if(count > 0) altApptSoon.showAndWait();  //notify user if an appointment is scheduled within 15 minutes of loading the form            
        }
        catch(SQLException|NullPointerException e){
            System.out.println("Error retrieving appointments||Null value returned");
        }
        // </editor-fold>
        //end right side creation    
        
        // <editor-fold defaultstate="collapsed" desc="center panel creation">
        GridPane.setConstraints(lblApptDescW,2,10,5,5);
        GridPane.setConstraints(lblApptDescM,2,10,5,5);
        GridPane.setConstraints(lblCenterMonth,0,8,2,2);
        paneCenterMonth.getChildren().addAll(lblCenterMonth,lblApptDescM);
        paneCenterMonth.setGridLinesVisible(false);
        GridPane.setConstraints(lblCenterWeek,0,2,2,2);
        paneCenterWeek.getChildren().addAll(lblCenterWeek,lblApptDescW);
        paneCenterWeek.setGridLinesVisible(false);        
        
        for(int i = 0; i < btnMonthArray.length;i++)  //populate the monthly calendar button array
        {
            Button btn = new Button();
            final Long days = (long)i;  //variable converting loop counter to a final to use with .plusDays
            Long count = apptMap.values()  //filters a stream from appointments by a single day to put a count on each calendar button
                    .stream()
                    .filter(x -> x.getStart()
                            .toLocalDate()
                            .format(formatDate)
                            .matches(ChronoLocalDateTime.from(LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1, 0, 0).plusDays(days)).toLocalDate().format(formatDate))
                    )
                    .count();
            btn.setText(currMonth.getDisplayName(TextStyle.FULL, Locale.getDefault()) + (i + 1) + System.lineSeparator() + count.toString() + " appointments");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER);
            GridPane.setConstraints(btn, i % 7, i / 7);
            Collection<Appointment> values = apptMap.values();
            btn.setOnAction((ActionEvent event) -> {  //button event loads a title for each appointment that day into the right side text flow
                rightText.getChildren().clear();
                lblApptDescW.setText("");
                lblApptDescM.setText("");
                values.stream().filter(x -> x.getStart()
                            .toLocalDate()
                            .format(formatDate)
                            .matches(ChronoLocalDateTime.from(LocalDateTime
                                    .of(LocalDate.now().getYear(),LocalDate.now().getMonth(), 1, 0, 0).plusDays(days)).toLocalDate().format(formatDate))
                ).forEach((Appointment value) -> {
                    TextField apptText = new TextField(value.toString());
                    apptText.setEditable(false);
                    apptText.setPrefWidth(180);
                    apptText.setOnMouseReleased((javafx.scene.input.MouseEvent event1) -> {  //button text boxes load more details into the text area in the center panel
                        lblApptDescW.setText(value.getTitle() + System.lineSeparator() + value.getName() + System.lineSeparator() + value.getDesc() + System.lineSeparator() + 
                                             "Appointment begins at: " + value.getStart().toLocalTime() + System.lineSeparator() + "Appointment ends at: "+ value.getEnd().toLocalTime());
                        lblApptDescM.setText(value.getTitle() + System.lineSeparator() + value.getName() + System.lineSeparator() + value.getDesc() + System.lineSeparator() + 
                                             "Appointment begins at: " + value.getStart().toLocalTime() + System.lineSeparator() + "Appointment ends at: "+ value.getEnd().toLocalTime());
                    });
                    rightText.getChildren().add(apptText);
                });              
            });
            paneCenterMonth.getChildren().add(btn);
        }
        for(int i = 0; i < btnWeekArray.length; i++)  //populate the weekly calendar button array
        {
            Button btn = new Button();
            final Long days = (long)i;
            Long count = apptMap.values()
                    .stream()
                    .filter(x -> x.getStart()
                            .toLocalDate()
                            .format(formatDate)
                            .matches(ChronoLocalDateTime.from(LocalDateTime.now().plusDays(days))
                                    .toLocalDate()
                                    .format(formatDate)))
                    .count();
            btn.setText(LocalDate.now(ZoneId.systemDefault()).plusDays(i).toString() + System.lineSeparator() + count.toString() + " appointments");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(Pos.CENTER);
            GridPane.setConstraints(btn, i, 0);
            Collection<Appointment> values = apptMap.values();
            btn.setOnAction((ActionEvent event) -> {
                rightText.getChildren().clear();
                lblApptDescW.setText("");
                lblApptDescM.setText("");
                values.stream().filter(x -> x.getStart()
                            .toLocalDate()
                            .format(formatDate)
                            .matches(ChronoLocalDateTime.from(LocalDateTime.now().plusDays(days)).toLocalDate().format(formatDate))
                ).forEach((Appointment value) -> {
                    TextField apptText = new TextField(value.toString());
                    apptText.setEditable(false);
                    apptText.setPrefWidth(180);
                    apptText.setOnMouseReleased((javafx.scene.input.MouseEvent event1) -> {
                        lblApptDescW.setText(value.getTitle() + System.lineSeparator() + value.getName() + System.lineSeparator() + value.getDesc()+ System.lineSeparator() + 
                                             "Appointment begins at: " + value.getStart().toLocalTime() + System.lineSeparator() + "Appointment ends at: "+ value.getEnd().toLocalTime());
                        lblApptDescM.setText(value.getTitle() + System.lineSeparator() + value.getName() + System.lineSeparator() + value.getDesc()+ System.lineSeparator() + 
                                             "Appointment begins at: " + value.getStart().toLocalTime() + System.lineSeparator() + "Appointment ends at: "+ value.getEnd().toLocalTime());
                    });
                    rightText.getChildren().add(apptText);
                });             
            });
            paneCenterWeek.getChildren().add(btn);
        }
        btnMonth.setOnAction(event -> {
            calPane.setCenter(paneCenterMonth);
            rightText.getChildren().clear();
            lblApptDescM.setText("");
            lblApptDescW.setText("");
        });
        btnWeek.setOnAction(event -> {
            calPane.setCenter(paneCenterWeek);
            rightText.getChildren().clear();
            lblApptDescW.setText("");
            lblApptDescM.setText("");
        });
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
        Scene calScene = new Scene(calPane,1050,450);
        return calScene;
    }
    
    public Scene EditAppointments(String curUser){
        // <editor-fold defaultstate="collapsed" desc="creating form components">
        BorderPane apptPane = new BorderPane();
        ComboBox cboxName = new ComboBox();
        ComboBox cboxType = new ComboBox();
        HBox bottomSide = new HBox();
        GridPane centerSide = new GridPane();
        DatePicker dp = new DatePicker();
        
        Button btnInsert = new Button();
        Button btnUpdate = new Button();
        Button btnDelete = new Button();
        
        Label lblApptID = new Label();
        Label lblUserID = new Label();
        Label lblCustName = new Label(RB.getString("lblFullName"));
        Label lblTitle = new Label(RB.getString("lblApptTitle"));
        Label lblLoc = new Label(RB.getString("lblApptLoc"));
        Label lblURL = new Label(RB.getString("lblApptURL"));
        Label lblContact = new Label(RB.getString("lblApptContact"));
        Label lblType = new Label(RB.getString("lblApptType"));
        Label lblDesc = new Label(RB.getString("lblApptDesc"));
        Label lblDate = new Label(RB.getString("lblApptDate"));
        Label lblStartTime = new Label(RB.getString("lblApptStart"));
        Label lblEndTime = new Label(RB.getString("lblApptEnd"));
        
        TextArea txtDesc = new TextArea();
        TextField txtTitle = new TextField();
        TextField txtLoc = new TextField();
        TextField txtURL = new TextField();
        TextField txtContact = new TextField();
        TextField txtStartTime = new TextField();
        TextField txtEndTime = new TextField();
        
        TextFlow apptFlow = new TextFlow();
        ScrollPane rightSide = new ScrollPane();
        Alert altApptDBError = new Alert(AlertType.ERROR); altApptDBError.setContentText("An error occured with the appointment database.");
        Alert altApptListEmpty = new Alert(AlertType.ERROR); altApptListEmpty.setContentText("The appointment list is empty.");
        Alert altApptSoon = new Alert(AlertType.INFORMATION); altApptSoon.setContentText("You have an appointment in the next 15 minutes.");
        Alert altNullInsert = new Alert(AlertType.ERROR); altNullInsert.setContentText("Please enter a value for all fields.");
        Alert altSelectAppt = new Alert(AlertType.ERROR); altSelectAppt.setContentText("Select an appointment first to make changes.");
        Alert altInvalidEntry = new Alert(AlertType.ERROR); altInvalidEntry.setContentText("Please enter valid values for all fields.");
        Alert altOutsideHours = new Alert(AlertType.WARNING); altOutsideHours.setContentText("Please enter a time during office business hours.");
        Alert altConflict = new Alert(AlertType.WARNING); altConflict.setContentText("This appointment time conflicts with another appointment.");
        Alert altTimeOrder = new Alert(AlertType.WARNING); altTimeOrder.setContentText("Your appointment cannot end before it begins.");
        
        ObservableList<Customer> listCust = FXCollections.observableArrayList();
        DateTimeFormatter formatTime = DateTimeFormatter.ofPattern("HH:mm");
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Grid Positions">
        GridPane.setConstraints(lblApptID,0,0);
        GridPane.setConstraints(lblCustName,0,1);
        GridPane.setConstraints(lblTitle,0,2);
        GridPane.setConstraints(lblLoc,0,3);
        GridPane.setConstraints(lblURL,0,4);
        GridPane.setConstraints(lblContact,0,5);
        GridPane.setConstraints(lblType,0,6);
        GridPane.setConstraints(lblDesc,0,8);
        GridPane.setConstraints(lblDate,2,1);
        GridPane.setConstraints(lblStartTime,2,2);
        GridPane.setConstraints(lblEndTime,2,3);
        GridPane.setConstraints(cboxName,1,1);
        GridPane.setConstraints(txtDesc,1,8,4,3);
        GridPane.setConstraints(txtTitle,1,2);
        GridPane.setConstraints(txtLoc,1,3);
        GridPane.setConstraints(txtURL,1,4);
        GridPane.setConstraints(txtContact,1,5);
        GridPane.setConstraints(cboxType,1,6);
        GridPane.setConstraints(txtStartTime,3,2);
        GridPane.setConstraints(txtEndTime,3,3);
        GridPane.setConstraints(dp,3,1);
        //</editor-fold>
        
        centerSide.setPadding(new Insets(20,5,5,10));
        centerSide.getChildren().addAll(lblApptID,lblCustName,lblTitle,lblLoc,lblURL,lblContact,lblType,lblDesc,lblDate,lblStartTime,lblEndTime,lblUserID,
                                        cboxName,cboxType,
                                        txtDesc,txtTitle,txtLoc,txtURL,txtContact,txtStartTime,txtEndTime,
                                        dp);
        txtStartTime.setPromptText(RB.getString("txtTime"));
        txtEndTime.setPromptText(RB.getString("txtTime"));
        lblApptID.setVisible(false); //hide the appointment ID, but retain for use with update/delete functions
        cboxName.setItems(listCust.sorted());  //populate the customer list
        cboxType.setItems(FXCollections.observableArrayList("Phone","In-person","Video conference","Teleconference"));
        
        bottomSide.getChildren().addAll(btnInsert,btnUpdate,btnDelete);
        bottomSide.setAlignment(Pos.CENTER_LEFT);
        bottomSide.setSpacing(15);
        bottomSide.setPadding(new Insets(20,20,20,20));        
        btnInsert.setText(RB.getString("btnCreateAppt")); btnInsert.setMaxWidth(Double.MAX_VALUE);
        btnUpdate.setText(RB.getString("btnUpdateAppt")); btnUpdate.setMaxWidth(Double.MAX_VALUE);
        btnDelete.setText(RB.getString("btnDeleteAppt")); btnDelete.setMaxWidth(Double.MAX_VALUE);
        
        apptFlow.setPrefWidth(180);
        rightSide.setPrefWidth(200);
        
        apptPane.setPrefSize(800, 400);
        
        try{
                LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1, 0, 0);
                //monthEnd adds 7 days to what it fetches so a week can be populated even at the end of the month
                //for example if the calendar is launched on April 30th, we can still view 7 days out, putting us into May
                LocalDateTime monthEnd = LocalDateTime.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), LocalDate.now().getMonth().length(LocalDate.now().getYear()%4 == 0), 23, 59).plusDays(7);
                apptMap = SQLHelper.GetAppointments(curUser, monthStart, monthEnd);
                custMap = SQLHelper.GetCustomers();
                custMap.values().forEach(value -> {
                    listCust.add(value);
                });
                apptMap.values().forEach(value -> {  //load the text flow with clickable appointments to fill fields for easy updates
                    TextField nextAppt = new TextField(value.toString());
                    nextAppt.setEditable(false);
                    nextAppt.setPrefWidth(180);
                    nextAppt.setOnMouseReleased(event ->{
                        lblApptID.setText(value.getApptID().toString());
                        lblUserID.setText(value.getUserID().toString());
                        cboxName.getSelectionModel().select(value.getLName() + ", " + value.getFName());
                        txtTitle.setText(value.getTitle());
                        txtLoc.setText(value.getLoc());
                        txtURL.setText(value.getURL());
                        txtContact.setText(value.getContact());
                        cboxType.getSelectionModel().select(value.getType());
                        dp.setValue(value.getStart().toLocalDate());
                        txtStartTime.setText(value.getStart().toLocalTime().format(formatTime));
                        txtEndTime.setText(value.getEnd().toLocalTime().format(formatTime));
                        txtDesc.setText(value.getDesc());
                    });
                    apptFlow.getChildren().add(nextAppt);
                });
        }
        catch(SQLException e){altApptDBError.show();}
        catch(NullPointerException e){altApptListEmpty.show(); System.out.println("Query returned no appointments.");}
        rightSide.setContent(apptFlow);
        
        //<editor-fold defaultstate="collapsed" desc="button event handlers">
        btnInsert.setOnAction(event -> {
            try{
                String[] start = txtStartTime.getText().split(":");
                String[] end = txtEndTime.getText().split(":");
                String[] nameArr = cboxName.getValue().toString().split(",");
                ZonedDateTime startTime = ZonedDateTime.of(LocalDateTime.of(dp.getValue().getYear(),dp.getValue().getMonth(),dp.getValue().getDayOfMonth(),
                        Integer.parseInt(start[0]),Integer.parseInt(start[1])),ZoneId.systemDefault());
                ZonedDateTime endTime = ZonedDateTime.of(LocalDateTime.of(dp.getValue().getYear(),dp.getValue().getMonth(),dp.getValue().getDayOfMonth(),
                        Integer.parseInt(end[0]),Integer.parseInt(end[1])),ZoneId.systemDefault());
                if(startTime.isBefore(endTime)){
                    TreeMap<Integer,Appointment> tempMap = SQLHelper.GetAppointments();
                    Optional<Customer> cust = custMap.values().stream().filter(value -> value.getName().equals(nameArr[1].trim() + " " + nameArr[0].trim())).findFirst();
                    ZoneId custZone = getZone(cust.get().getCity());
                    //boolean to check if entered time is within office business hours
                    Boolean blnOfficeHours = startTime.withZoneSameInstant(custZone).toLocalTime().isAfter(ZonedDateTime.of(LocalDate.now(),LocalTime.of(8, 59),custZone).toLocalTime())
                            && endTime.withZoneSameInstant(custZone).toLocalTime().isBefore(ZonedDateTime.of(LocalDate.now(),LocalTime.of(17, 1),custZone).toLocalTime())
                            && !startTime.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                            && !startTime.getDayOfWeek().equals(DayOfWeek.SUNDAY);
                    //check if an appointment is scheduled suring the same time
                    Long conflict = tempMap.values().stream().filter(value -> startTime.equals(value.getStart().atZone(ZoneId.systemDefault())) || 
                                                                          (startTime.isAfter(value.getStart().atZone(ZoneId.systemDefault()))) && startTime.isBefore(value.getEnd().atZone(ZoneId.systemDefault())) ||
                                                                           endTime.equals(value.getEnd().atZone(ZoneId.systemDefault())) ||
                                                                          (endTime.isBefore(value.getEnd().atZone(ZoneId.systemDefault())) && endTime.isAfter(value.getStart().atZone(ZoneId.systemDefault()))))
                                                             .count();
                    if(blnOfficeHours)
                    {
                        if(conflict == 0){
                            Appointment nextAppt = new Appointment(nameArr[1].trim() + " " + nameArr[0].trim(),txtTitle.getText(),txtDesc.getText(),
                                                                   txtLoc.getText(),txtContact.getText(),cboxType.getValue().toString(),txtURL.getText(),
                                                                   startTime,endTime);
                            if(SQLHelper.Insert(nextAppt, curUser)){mainStage.setScene(GetCalendar(curUser));mainStage.show();altStage.setScene(EditAppointments(curUser));altStage.show();}
                        }
                        else{altConflict.showAndWait();}
                    }
                    else{altOutsideHours.showAndWait();}
                }
                else{altTimeOrder.showAndWait();}
            }
            catch(SQLException e){System.out.println(e.getMessage());}
            catch(NullPointerException e){System.out.println(e.getMessage());}//altNullInsert.show();}
            catch(IllegalArgumentException|ArrayIndexOutOfBoundsException|DateTimeException e){altInvalidEntry.show(); e.printStackTrace();}
            finally{}
        });
        btnDelete.setOnAction(event -> {
            try{
                if(SQLHelper.Delete(Integer.parseInt(lblApptID.getText()))){mainStage.setScene(GetCalendar(curUser));mainStage.show();altStage.setScene(EditAppointments(curUser));altStage.show();}
            }
            catch(SQLException e){System.out.println(e.getMessage());}
            catch(NullPointerException e){altSelectAppt.show();}
            finally{}
        });
        btnUpdate.setOnAction(event -> {
            try{                
                String[] start = txtStartTime.getText().split(":");
                String[] end = txtEndTime.getText().split(":");
                String[] nameArr = cboxName.getValue().toString().split(",");
                ZonedDateTime startTime = ZonedDateTime.of(LocalDateTime.of(dp.getValue().getYear(),dp.getValue().getMonth(),dp.getValue().getDayOfMonth(),
                        Integer.parseInt(start[0]),Integer.parseInt(start[1])),ZoneId.systemDefault());
                ZonedDateTime endTime = ZonedDateTime.of(LocalDateTime.of(dp.getValue().getYear(),dp.getValue().getMonth(),dp.getValue().getDayOfMonth(),
                        Integer.parseInt(end[0]),Integer.parseInt(end[1])),ZoneId.systemDefault());
                if(startTime.isBefore(endTime)){
                    TreeMap<Integer,Appointment> tempMap = SQLHelper.GetAppointments();
                    Optional<Customer> cust = custMap.values().stream().filter(value -> value.getName().equals(nameArr[1].trim() + " " + nameArr[0].trim())).findFirst();
                    ZoneId custZone = getZone(cust.get().getCity());

                    Boolean blnOfficeHours = startTime.withZoneSameInstant(custZone).toLocalTime().isAfter(ZonedDateTime.of(LocalDate.now(),LocalTime.of(8, 59),custZone).toLocalTime())
                            && endTime.withZoneSameInstant(custZone).toLocalTime().isBefore(ZonedDateTime.of(LocalDate.now(),LocalTime.of(17, 1),custZone).toLocalTime())
                            && !startTime.getDayOfWeek().equals(DayOfWeek.SATURDAY)
                            && !startTime.getDayOfWeek().equals(DayOfWeek.SUNDAY);                

                    Long conflict = tempMap.values().stream().filter(value -> startTime.equals(value.getStart().atZone(ZoneId.systemDefault())) || 
                                                                    (startTime.isAfter(value.getStart().atZone(ZoneId.systemDefault()))) && startTime.isBefore(value.getEnd().atZone(ZoneId.systemDefault())) ||
                                                                     endTime.equals(value.getEnd().atZone(ZoneId.systemDefault())) ||
                                                                    (endTime.isBefore(value.getEnd().atZone(ZoneId.systemDefault())) && endTime.isAfter(value.getStart().atZone(ZoneId.systemDefault()))))
                                                             .count();
                    if(blnOfficeHours)
                    {
                        if(conflict == 0){
                            Appointment nextAppt = new Appointment(Integer.parseInt(lblApptID.getText()),Integer.parseInt(lblUserID.getText()),nameArr[1].trim() + " " + nameArr[0].trim(),txtTitle.getText(),txtDesc.getText(),
                                                                   txtLoc.getText(),txtContact.getText(),cboxType.getValue().toString(),txtURL.getText(),
                                                                   startTime,endTime);
                            if(SQLHelper.Update(nextAppt, curUser)){mainStage.setScene(GetCalendar(curUser));mainStage.show();altStage.setScene(EditAppointments(curUser));altStage.show();}
                        }
                        else{altConflict.showAndWait();}
                    }
                    else{altOutsideHours.showAndWait();}
                }
                else{altTimeOrder.showAndWait();}
            }
            catch(SQLException e){System.out.println(e.getMessage());}
            catch(NullPointerException e){e.printStackTrace();}//altSelectAppt.show();}
            catch(IllegalArgumentException|ArrayIndexOutOfBoundsException|DateTimeException e){altInvalidEntry.show();}
            finally{}
        });
        //</editor-fold>        
        
        apptPane.setRight(rightSide);
        apptPane.setBottom(bottomSide);
        apptPane.setCenter(centerSide);
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
        TextField txtAddrID = new TextField();
        Alert altEmptyField = new Alert(AlertType.INFORMATION); altEmptyField.setContentText("A value must be entered in all required fields.");
        Alert altDBError = new Alert(AlertType.ERROR); altDBError.setContentText("An error occured when processing your database request.");
        ScrollPane custList = new ScrollPane();
        TextFlow custFlow = new TextFlow();
        
        // </editor-fold>
        
        //other variable and objects
        TreeMap<Integer,TreeMap> CiCo;
        TreeMap<Integer,String[]> cityMap, countryMap;
        
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
                    txtAddrID.setText(value.getAddrID().toString());
                });
                custFlow.getChildren().add(nextCust);
            });
        }
        
        // <editor-fold defaultstate="collapsed" desc="setting grid positions">
        GridPane.setConstraints(countryBox,4,3);
        GridPane.setConstraints(cityBox,4,4);
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
        GridPane.setConstraints(lblCountry,3,3);
        GridPane.setConstraints(lblCity,3,4);
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
        txtAddrID.setVisible(false);
        
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
                    if(SQLHelper.Insert(cust, curUser)){altStage.setScene(GetCustomers(curUser)); altStage.show();}
                }
                catch(NullPointerException|ArrayIndexOutOfBoundsException e){altEmptyField.show();}
                catch(SQLException e){altDBError.show();}
            });
        btnUpdateCust.setOnAction(event -> {
                try{
                    SQLHelper.PurgeAddr();
                    StringBuilder filler = new StringBuilder(); filler.insert(0,"\u0020"); filler.insert(0,txtAddr2.getText());
                    Boolean bool = activeBox.getValue().equals(RB.getString("active"));
                    //int custID, String custName, Integer addressID, Boolean active,String add1, String add2, String postCode, String phone, String city, String country
                    Customer cust = new Customer(Integer.parseInt(txtID.getText()),txtFName.getText() + " " + txtLName.getText(),
                                                Integer.parseInt(txtAddrID.getText()),txtAddr1.getText(), filler.toString(), bool,
                                                txtPostCode.getText(),txtPhone.getText(),
                                                cityBox.getValue().toString(),countryBox.getValue().toString());
                    if(SQLHelper.Update(cust, curUser)){altStage.setScene(GetCustomers(curUser)); altStage.show();}
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
                    if(SQLHelper.Delete(cust)){altStage.setScene(GetCustomers(curUser)); altStage.show();}
                }
                catch(NullPointerException|ArrayIndexOutOfBoundsException e){altEmptyField.show();}
                catch(SQLException e){altDBError.show();}
            });
        //</editor-fold>
        
        try{
            cityMap = SQLHelper.GetCiCo();
            cityMap.values().forEach(value -> {
                if(!countryBox.getItems().contains(value[2])) countryBox.getItems().add(value[2]);
                });
            countryBox.getSelectionModel().selectedItemProperty().addListener((ObservableValue o, Object obj1, Object obj2) -> {
                cityBox.getItems().clear();
                cityMap.values().stream()
                        .filter(value -> value[2].equals(countryBox.getValue()))
                        .forEach(value -> cityBox.getItems().add(value[0]));
            });
        }
        catch(SQLException e){
            System.out.println("An error occured retrieving the city/country lists.");
            e.printStackTrace();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        custPane.getChildren().addAll(cityBox,countryBox,activeBox,btnCreateCust,btnUpdateCust,btnDeleteCust,
                                      lblFName,lblLName,lblAddr1,lblAddr2,lblPostCode,lblActive,lblPhone,lblCity,lblCountry,lblReq,
                                      txtFName,txtLName,txtAddr1,txtAddr2,txtPostCode,txtPhone,txtID,txtAddrID,
                                      custList);
        return custScene;
    }
    
    public Scene GetReports(String user){
        //<editor-fold defaultstate="collapsed" desc="component creation">
        BorderPane reportPane = new BorderPane();
        Scene reportScene = new Scene(reportPane,900,500);
        VBox leftSide = new VBox();
        HBox topSide = new HBox();
        GridPane centerSide = new GridPane();
        ComboBox cboxMulti = new ComboBox();
        ComboBox cboxMonth = new ComboBox();
        ComboBox cboxYear = new ComboBox();
        TextArea centerText = new TextArea();
        //# appt types by month
        //schedule for each ocnsultant
        //one more
        Button btnTypes = new Button();
        Button btnSchedule = new Button();
        Button btnLocation = new Button();
        Button btnGenerate = new Button();
        Alert altEmptyField = new Alert(AlertType.ERROR);  altEmptyField.setContentText("Make sure to select a value for all fields.");
        
        ObservableList listApptTypes = FXCollections.observableArrayList();//"Phone","In-person","Video conference","Teleconference");
        ObservableList<String> users = FXCollections.observableArrayList();
        ObservableList<Integer> years = FXCollections.observableArrayList();
        ObservableList<String> locations = FXCollections.observableArrayList();
        
        cboxMulti.setVisible(false);
        
        try{
            apptMap = SQLHelper.GetAppointments();
            custMap = SQLHelper.GetCustomers();            
            apptMap.values().forEach(value -> {users.add(value.getUser());});
            apptMap.values().forEach(value -> {years.add(value.getStart().getYear());});
            apptMap.values().forEach(value -> {listApptTypes.add(value.getType());});
            custMap.values().forEach(value -> {locations.add(value.getCity());});
        }
        catch(SQLException e){
            System.out.println(e.getMessage());
        }
        //</editor-fold>
        GridPane.setConstraints(cboxMulti, 0, 0);
        GridPane.setConstraints(centerText, 0, 1);
        GridPane.setConstraints(cboxMonth, 1, 0);
        GridPane.setConstraints(cboxYear, 2, 0);
        GridPane.setConstraints(btnGenerate, 4, 0);
        
        //<editor-fold defaultstate="collapsed" desc="left side">
        leftSide.getChildren().addAll(btnTypes,btnSchedule,btnLocation,btnGenerate);
        leftSide.setAlignment(Pos.CENTER_LEFT);
        leftSide.setSpacing(15);
        leftSide.setPadding(new Insets(0,0,0,0));
        btnTypes.setAlignment(Pos.CENTER);
        btnSchedule.setAlignment(Pos.CENTER);
        btnLocation.setAlignment(Pos.CENTER);
        btnTypes.setMaxWidth(Double.MAX_VALUE);
        btnSchedule.setMaxWidth(Double.MAX_VALUE);
        btnLocation.setMaxWidth(Double.MAX_VALUE);
        btnGenerate.setMaxWidth(Double.MAX_VALUE);
        btnTypes.setText("Appointments by Type");
        btnSchedule.setText("Appointments by Consultant");
        btnLocation.setText("Customers by Location");
        btnGenerate.setText("Generate Report");
        
        btnTypes.setOnAction(event -> {
            try{
                centerText.clear();
                cboxMulti.getItems().clear();
                cboxMonth.getItems().clear();
                cboxYear.getItems().clear();
                cboxMulti.setVisible(false); 
                cboxMonth.setVisible(true);
                cboxYear.setVisible(true);
                cboxMulti.getItems().addAll(listApptTypes.stream().distinct().toArray());
                cboxMonth.setItems(FXCollections.observableArrayList(Month.values()));
                cboxYear.getItems().addAll(years.stream().distinct().toArray());
                btnGenerate.setOnAction((ActionEvent event1) -> {
                    try{
                        centerText.clear();
                        Month m = (Month)cboxMonth.getValue();
                        final LocalDateTime monthStart = LocalDateTime.of(Integer.parseInt(cboxYear.getValue().toString()), m, 1, 0, 0);
                        final LocalDateTime monthEnd = LocalDateTime.of(Integer.parseInt(cboxYear.getValue().toString()), m, m.length(LocalDate.now().getYear()%4 == 0), 23, 59);
                        cboxMulti.getItems().stream().forEach(value -> {
                            Long count = apptMap.values().stream()
                                    .filter(appt -> appt.getStart().isAfter(monthStart))
                                    .filter(appt -> appt.getStart().isBefore(monthEnd))
                                    .filter(appt -> appt.getType().equals(value)).count();
                            String type = value.toString();
                            centerText.appendText(type + ": " + count.toString() + System.lineSeparator());
                        });
                    }
                    catch(NullPointerException e){altEmptyField.show();}
                });
            }
            catch(NullPointerException e){altEmptyField.show();}
        });
        btnSchedule.setOnAction(event -> {
            try{
                centerText.clear();
                cboxMulti.getItems().clear();
                cboxMonth.getItems().clear();
                cboxYear.getItems().clear();
                cboxMulti.setVisible(true);                
                cboxMonth.setVisible(true);
                cboxYear.setVisible(true);
                cboxMulti.getItems().addAll(users.stream().distinct().toArray());
                cboxMonth.setItems(FXCollections.observableArrayList(Month.values()));
                cboxYear.getItems().addAll(years.stream().distinct().toArray());
                btnGenerate.setOnAction((ActionEvent event1) -> {
                    try{
                        centerText.clear();
                        Month m = (Month)cboxMonth.getValue();
                        final LocalDateTime monthStart = LocalDateTime.of(Integer.parseInt(cboxYear.getValue().toString()), m, 1, 0, 0);
                        final LocalDateTime monthEnd = LocalDateTime.of(Integer.parseInt(cboxYear.getValue().toString()), m, m.length(LocalDate.now().getYear()%4 == 0), 23, 59);
                        apptMap.values().stream()
                                .filter(value -> value.getUser().equals(cboxMulti.getValue()))
                                .filter(value -> value.getStart().isAfter(monthStart))
                                .filter(value -> value.getStart().isBefore(monthEnd))
                                .forEach(value -> centerText.appendText(value.toString() + System.lineSeparator()));
                    }
                    catch(NullPointerException e){altEmptyField.show();}
                });
            }
            catch(NullPointerException e){altEmptyField.show();}
        });
        btnLocation.setOnAction(event -> {
            try{
                centerText.clear();
                cboxMulti.getItems().clear();
                cboxMonth.getItems().clear();
                cboxYear.getItems().clear();
                cboxMulti.setVisible(false);
                cboxMonth.setVisible(false);
                cboxYear.setVisible(false);
                cboxMulti.getItems().addAll(locations.stream().distinct().toArray());
                cboxMonth.setItems(FXCollections.observableArrayList(Month.values()));
                cboxYear.getItems().addAll(years.stream().distinct().toArray());
                btnGenerate.setOnAction((ActionEvent event1) -> {
                    try{
                        centerText.clear();
                        cboxMulti.getItems().stream().forEach(value -> {
                            Long count = custMap.values().stream()
                                    .filter(cust -> cust.getCity().equals(value)).count();
                            String location = value.toString();
                            centerText.appendText(location + ": " + count.toString() + " customers" + System.lineSeparator());
                        });
                    }
                    catch(NullPointerException e){altEmptyField.show();}
                });
            }
            catch(NullPointerException e){altEmptyField.show();}
        });
        
        //</editor-fold>
        
        centerSide.getChildren().addAll(centerText);
        centerText.setPrefWidth(550);
        centerText.setPrefHeight(650);
        
        topSide.getChildren().addAll(cboxMulti, cboxMonth, cboxYear, btnGenerate);
        topSide.setAlignment(Pos.CENTER);
        cboxMulti.setPrefWidth(170);
        cboxMonth.setPrefWidth(90);
        cboxYear.setPrefWidth(90);
        cboxMulti.setMaxWidth(Double.MAX_VALUE);
        cboxMonth.setMaxWidth(Double.MAX_VALUE);
        cboxYear.setMaxWidth(Double.MAX_VALUE);
        
        reportPane.setLeft(leftSide);
        reportPane.setCenter(centerSide);
        reportPane.setTop(topSide);        
        
        return reportScene;
    }
    
    public static ZoneId getZone(String city){ //method can be customized with additional time zones as cities are added to DB
        switch (city){
            case "Phoenix" : return ZoneId.of("America/Phoenix");
            case "New York" : return ZoneId.of("America/New_York");
            case "London" : return ZoneId.of("UTC");
            default : return ZoneId.systemDefault();
        }        
    }
}

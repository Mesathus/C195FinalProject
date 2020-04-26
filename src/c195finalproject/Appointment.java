
package c195finalproject;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
/**
 *
 * @author Mesa
 */
public class Appointment implements Comparable<Appointment>{
    
    private Integer apptId = null;
    private Integer custId = null;
    private Integer userId = null;
    private String custFirstName;
    private String custLastName;
    private String userName;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private ZonedDateTime startTime;
    private ZonedDateTime endTime;
    
    public Appointment(Integer apptID, Integer userID, String custName, String title, String desc, String location, String contact, String type, String url, ZonedDateTime start, ZonedDateTime end){
        try{
            this.apptId = apptID;
            this.userId = userID;
            String[] appt = custName.split(" ");
            this.custFirstName = appt[0];
            this.custLastName = appt[1];
            this.title = title;
            this.description = desc;
            this.location = location;
            this.contact = contact;
            this.type = type;
            this.url = url;
            this.startTime = start.withZoneSameInstant(ZoneId.of("UTC"));
            this.endTime = end.withZoneSameInstant(ZoneId.of("UTC"));
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
        }
    }
    
    public Appointment(Integer apptID, Integer userID, String userName, String custName, String title, String desc, String location, String contact, String type, String url, ZonedDateTime start, ZonedDateTime end){
        try{
            this.apptId = apptID;
            this.userId = userID;
            String[] appt = custName.split(" ");
            this.custFirstName = appt[0];
            this.custLastName = appt[1];
            this.userName = userName;
            this.title = title;
            this.description = desc;
            this.location = location;
            this.contact = contact;
            this.type = type;
            this.url = url;
            this.startTime = start.withZoneSameInstant(ZoneId.of("UTC"));
            this.endTime = end.withZoneSameInstant(ZoneId.of("UTC"));
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
        }
    }
    
    public Appointment(String custName, String title, String desc, String location, String contact, String type, String url, ZonedDateTime start, ZonedDateTime end){
        try{            
            String[] appt = custName.split(" ");
            this.custFirstName = appt[0];
            this.custLastName = appt[1];
            this.title = title;
            this.description = desc;
            this.location = location;
            this.contact = contact;
            this.type = type;
            this.url = url;
            this.startTime = start.withZoneSameInstant(ZoneId.of("UTC"));
            this.endTime = end.withZoneSameInstant(ZoneId.of("UTC"));
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
        }
    }
    
    
    @Override
    public int compareTo(Appointment appt){
        return this.startTime.compareTo(appt.startTime);
    }
    @Override
    public String toString(){
        return title + ": " + custLastName + ", " + custFirstName;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Getters">
    public Integer getApptID(){return this.apptId;}
    public Integer getCustID(){return this.custId;}
    public Integer getUserID(){return this.userId;}
    public String getName(){return custFirstName + " " + custLastName;}
    public String getFName(){return custFirstName;}
    public String getLName(){return custLastName;}
    public String getUser(){return this.userName;}
    public String getTitle(){return title;}
    public String getDesc(){return description;}
    public String getLoc(){return location;}
    public String getContact(){return contact;}
    public String getType(){return type;}
    public String getURL(){return url;}
    public LocalDateTime getStart(){return startTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();}
    public LocalDateTime getEnd(){return endTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();}
    // </editor-fold>
}

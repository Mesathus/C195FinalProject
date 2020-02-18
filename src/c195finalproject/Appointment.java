/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

import java.time.LocalDateTime;
/**
 *
 * @author Mesa
 */
public class Appointment implements Comparable<Appointment>{
    
    private Integer apptId = null;
    private String custFirstName;
    private String custLastName;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    public Appointment(Integer apptID, String custName, String title, String desc, String location, String contact, String url, LocalDateTime start, LocalDateTime end){
        try{
            this.apptId = apptID;
            String[] appt = custName.split(" ");
            this.custFirstName = appt[0];
            this.custLastName = appt[1];
            this.title = title;
            this.description = desc;
            this.location = location;
            this.contact = contact;
            this.url = url;
            this.startTime = start;
            this.endTime = end;
        }
        catch(NullPointerException e){
            System.out.println(e.getMessage());
        }
    }
    
    public Appointment(String custName, String title, String desc, String location, String contact, String url, LocalDateTime start, LocalDateTime end){
        try{            
            String[] appt = custName.split(" ");
            this.custFirstName = appt[0];
            this.custLastName = appt[1];
            this.title = title;
            this.description = desc;
            this.location = location;
            this.contact = contact;
            this.url = url;
            this.startTime = start;
            this.endTime = end;
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
        return title + System.lineSeparator() + custLastName + " ," + custFirstName;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Getters">
    public Integer getID(){return this.apptId;}
    public String getName(){return custFirstName + " " + custLastName;}
    public String getFName(){return custFirstName;}
    public String getLName(){return custLastName;}
    public String getTitle(){return title;}
    public String getDesc(){return description;}
    public String getLoc(){return location;}
    public String getContact(){return contact;}
    public String getType(){return type;}
    public String getURL(){return url;}
    public LocalDateTime getStart(){return startTime;}
    public LocalDateTime getEnd(){return endTime;}
    // </editor-fold>
}

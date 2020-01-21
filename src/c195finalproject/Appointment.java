/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

import java.time.LocalDateTime;
import java.time.LocalTime;
/**
 *
 * @author Mesa
 */
public class Appointment implements Comparable<Appointment>{
    
    private Integer apptId;
    private String custFirstName;
    private String custLastName;
    private String title;
    private String description;
    private String location;
    private String contact;
    private String type;
    private String url;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalDateTime apptTime;
    
    public Appointment(){
        
    }
    
    @Override
    public int compareTo(Appointment appt){
        return this.startTime.compareTo(appt.startTime);
    }
    @Override
    public String toString(){
        return title;
    }
    
    
    
}

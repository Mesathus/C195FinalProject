/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package c195finalproject;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 *
 * @author Mesathus
 */
public class Logging {
    private static LocalDate currDate = LocalDate.now(ZoneId.of("UTC"));
    private static LocalDateTime currDateTime;
    private final static Path BASEDIR = Paths.get(System.getProperty("user.dir"));
    private static Path logDir;
    private final static String OSTYPE = System.getProperty("os.name");
    
    public static void Init() throws IOException{
        try{
            logDir = Paths.get(BASEDIR.toString(),"Logs");
            if(!Files.exists(logDir)) Files.createDirectory(logDir);
            logDir = Paths.get(BASEDIR.toString(),"Logs",currDate.toString() + ".txt");
            if(!Files.exists(logDir)) Files.createFile(logDir);            
        }
        catch(IOException e){     //print a system message if the log file is unavailable
            System.out.println("The user log file was unable to be created");
            System.out.println(e.getMessage());
        }
    }
    
    public static boolean StampLog(String user) throws IOException{
        logDir = Paths.get(BASEDIR.toString(),"Logs",currDate.toString() + ".txt");
        try(BufferedWriter writer = Files.newBufferedWriter(logDir)){
                currDateTime = LocalDateTime.now(ZoneId.of("UTC"));                
                writer.append(user + " " + currDateTime.toString() + System.lineSeparator());
                writer.newLine();
                return true;            
        }
        catch(IOException e){
            System.out.println(e.getMessage());
            return false;
        }
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adc.util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import static org.apache.log4j.Logger.getLogger;
import org.graylog2.GelfMessage;
import org.graylog2.GelfSender;

/**
 *
 * @author gcarlson
 */
public class Graywrap extends Logger{
    private static Logger logger;
    private String applicationName;
    private String serviceName;
    private String facilityName;
    private Level graylogThreshold;
    private String graylogHost;
    private Integer graylogPort;

    protected Graywrap(Class aClass, Properties props) {
        super (aClass.getName());
        this.logger = getLogger(aClass.getName());
        this.applicationName = props.getProperty("graywrap.applicationName","gelf-application");
        this.serviceName = props.getProperty("graywrap.serviceName","gelf-service");
        this.facilityName = props.getProperty("graywrap.facilityName","gelf-facility");
        this.graylogThreshold = Level.toLevel(props.getProperty("graywrap.threshold","INFO"));
        this.graylogHost = props.getProperty("graywrap.host");
        this.graylogPort = Integer.valueOf(props.getProperty("graywrap.port"));
    }
    
    public static Graywrap getLogger(Class aClass, Properties props){
        return new Graywrap(aClass, props);
    }
    
    public static Graywrap getLogger(Class aClass, String resource){
        Properties props = new java.util.Properties();
        try {
            props.load(Graywrap.class.getClassLoader().getResourceAsStream(resource));
        } catch (IOException ex) {
            //Where do you log an error if you are inside the logger constructor?
        }
        return new Graywrap(aClass, props);
    }
    
    private void sendGelfMessage(String shortMessage, String longMessage, String facility, Throwable throwable, Level level){
        String sysLevel = String.valueOf(level.getSyslogEquivalent());
        if(shortMessage==null || shortMessage.isEmpty()){
            if(throwable!=null){
                shortMessage=throwable.getMessage();
            } else{
                shortMessage="";
            }
        }
        if(shortMessage.length()>4096){
            shortMessage=shortMessage.substring(0, 4093)+"...";
        }
        
        if(longMessage==null || longMessage.isEmpty()){
            if(throwable!=null){
                longMessage=ExceptionLogger.formatStacktrace(throwable);
            } else{
                longMessage="";
            }
        }
        
        ArrayList<String> longMessages = new ArrayList();
        while(longMessage.length()>0){
            if(longMessage.length()>4000){
                longMessages.add(longMessage.substring(0, 4000)+"...");
                longMessage=longMessage.substring(4000);
            } else{
                longMessages.add(longMessage);
                longMessage="";
            }
        }
        
        Integer errorCode = new Random(new Date().getTime()+longMessages.size()).nextInt();
        if(errorCode<0){
            errorCode=errorCode*-1;
        }
        for(int i=0; i<longMessages.size(); i++){
            String aShortMessage;
            String aLongMessage=longMessages.get(i);
            if(longMessages.size()>1){
                aShortMessage=shortMessage+" Error Code "+errorCode+" Part "+(i+1)+" of "+longMessages.size();
            }else{
                aShortMessage=shortMessage;
            }
            StackTraceElement stackTraceElement = new Exception().getStackTrace()[2];
            GelfMessage gelfMessage = new GelfMessage(aShortMessage, aLongMessage, new Date(), sysLevel);
            gelfMessage.setFacility(facility);
            gelfMessage.setFile(stackTraceElement.getFileName());
            gelfMessage.setLine(String.valueOf(stackTraceElement.getLineNumber()));
            gelfMessage.addField("application", applicationName)
                       .addField("Service", serviceName);
            try {
                gelfMessage.setHost(InetAddress.getLocalHost().getHostName());
            } catch (UnknownHostException ex) {
                gelfMessage.setHost("unknown");
                java.util.logging.Logger.getLogger(Graywrap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
            }
            if(level.isGreaterOrEqual(graylogThreshold)){
                try {
                    GelfSender gelfSender = new GelfSender(graylogHost, graylogPort);
                    if(gelfMessage.isValid()){
                        gelfSender.sendMessage(gelfMessage);                    
                    }
                } catch (UnknownHostException ex) {
                    java.util.logging.Logger.getLogger(Graywrap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                } catch (SocketException ex) {
                    java.util.logging.Logger.getLogger(Graywrap.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                }
            }
        }
    }
      
    @Override
    public void debug(Object message){
        logger.debug(message);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.DEBUG);
    }
    
    public void debug(Object shortMessage, Object longMessage){
        logger.debug(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facilityName, null, Level.DEBUG);
    }
      
    public void debug(Object message, String facility){
        logger.debug(message);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.DEBUG);
    }
      
    public void debug(Object shortMessage, Object longMessage, String facility){
        logger.debug(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facility, null, Level.DEBUG);
    }
    
    @Override
    public void debug(Object message, Throwable throwable){
        logger.debug(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facilityName, throwable, Level.DEBUG);
    }
    
    public void debug(Object message, String facility, Throwable throwable){
        logger.debug(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facility, throwable, Level.DEBUG);
    }
      
    @Override
    public void error(Object message){
        logger.error(message);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.ERROR);
    }
    
    public void error(Object shortMessage, Object longMessage){
        logger.error(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facilityName, null, Level.ERROR);
    }
      
    public void error(Object message, String facility){
        logger.error(message);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.ERROR);
    }
      
    public void error(Object shortMessage, Object longMessage, String facility){
        logger.error(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facility, null, Level.ERROR);
    }
    
    @Override
    public void error(Object message, Throwable throwable){
        logger.error(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facilityName, throwable, Level.ERROR);
    }
    
    public void error(Object message, String facility, Throwable throwable){
        logger.error(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facility, throwable, Level.ERROR);
    }
      
    @Override
    public void fatal(Object message){
        logger.fatal(message);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.FATAL);
    }
    
    public void fatal(Object shortMessage, Object longMessage){
        logger.fatal(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facilityName, null, Level.FATAL);
    }
      
    public void fatal(Object message, String facility){
        logger.fatal(message);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.FATAL);
    }
      
    public void fatal(Object shortMessage, Object longMessage, String facility){
        logger.fatal(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facility, null, Level.FATAL);
    }
    
    @Override
    public void fatal(Object message, Throwable throwable){
        logger.fatal(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.FATAL);
    }
    
    public void fatal(Object message, String facility, Throwable throwable){
        logger.fatal(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.FATAL);
    }
      
    @Override
    public void info(Object message){
        logger.info(message);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.INFO);
    }
    
    public void info(Object shortMessage, Object longMessage){
        logger.info(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facilityName, null, Level.INFO);
    }
      
    public void info(Object message, String facility){
        logger.info(message);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.INFO);
    }
      
    public void info(Object shortMessage, Object longMessage, String facility){
        logger.info(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facility, null, Level.INFO);
    }
    
    @Override
    public void info(Object message, Throwable throwable){
        logger.info(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.INFO);
    }
    
    public void info(Object message, String facility, Throwable throwable){
        logger.info(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.INFO);
    }
      
    @Override
    public void warn(Object message){
        logger.warn(message);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.WARN);
    }
    
    public void warn(Object shortMessage, Object longMessage){
        logger.warn(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facilityName, null, Level.WARN);
    }
      
    public void warn(Object message, String facility){
        logger.warn(message);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.WARN);
    }
      
    public void warn(Object shortMessage, Object longMessage, String facility){
        logger.warn(shortMessage);
        sendGelfMessage(shortMessage.toString(), longMessage.toString(), facility, null, Level.WARN);
    }
    
    @Override
    public void warn(Object message, Throwable throwable){
        logger.warn(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facilityName, null, Level.WARN);
    }
    
    public void warn(Object message, String facility, Throwable throwable){
        logger.warn(message,throwable);
        sendGelfMessage(message.toString(), message.toString(), facility, null, Level.WARN);
    }
}

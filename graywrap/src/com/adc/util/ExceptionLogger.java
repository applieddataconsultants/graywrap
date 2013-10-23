/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adc.util;

/**
 *
 * @author gcarlson
 */
public class ExceptionLogger {    
    public static String formatStacktrace(Throwable exception){
        StringBuilder stacktrace= new StringBuilder();
        while(exception!=null){
            StringBuilder lines=new StringBuilder();
            for(StackTraceElement ste : exception.getStackTrace()){
                lines.append(ste.getClassName())
                     .append("(")
                        .append(ste.getFileName()).append(":").append(ste.getLineNumber())
                     .append(")")
                     .append(System.getProperty("line.separator"));
            }
            stacktrace.append(System.getProperty("line.separator"));
            stacktrace.append(exception.getClass().toString());
            if(exception.getMessage()!=null){
                stacktrace.append(System.getProperty("line.separator"));
                stacktrace.append(exception.getMessage());
            }
            stacktrace.append(System.getProperty("line.separator"));
            stacktrace.append(lines.toString().replaceFirst(",", ""));
            stacktrace.append(System.getProperty("line.separator"));
            exception=exception.getCause();
        }
        return stacktrace.toString();
    }
}
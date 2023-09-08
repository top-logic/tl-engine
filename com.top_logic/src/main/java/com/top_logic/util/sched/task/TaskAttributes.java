/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task;

/**
 * This interface contains all attributes which are  defined in "kbmeta.xml"
 * to represent information about tasks.
 *
 * @author    <a href="mailto:asc@top-logic.com">Alice Scheerer</a>
 */
public interface TaskAttributes {
   
   // Static attributes 

    /** The attribute "name". 
    */
   public static final String TASK_NAME     = "name";
   
   /** The attribute "daytype". 
    */
   public static final String DAYTYPE       = "daytype";
   
   /** The attribute "daymask". 
    */
   public static final String DAYMASK       = "daymask";
   
   /** The attribute "hour". 
    */
   public static final String HOUR          = "hour";   
   
   /** The attribute "minute". 
    */
   public static final String MINUTE        = "minute";    
   
   /** The attribute "stopHour". 
    */
   public static final String STOP_HOUR     = "stopHour"; 
   
   /** The attribute "stopMinute". 
    */
   public static final String STOP_MINUTE   = "stopMinute";    
   
   /** The attribute "interval". 
    */
   public static final String INTERVAL      = "interval";   
   
   /** The attribute "when". 
    */
   public static final String WHEN          = "when"; 
   
   /** The FIELD NAME  "nextShed". 
    */
   public static final String NEXT_SHED     = "nextShed";
   
   /** The FIELD NAME  "started". 
    */
   public static final String STARTED       = "started";  
   
   /** The FIELD NAME  "periodically". 
    */
   public static final String PERIODICALLY  = "periodically";     
   
}

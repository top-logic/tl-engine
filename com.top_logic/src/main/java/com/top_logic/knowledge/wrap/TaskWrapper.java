/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskAttributes;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;

/**
 * A Task that is initialized by a KnowledgeObject.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public abstract class TaskWrapper extends AbstractWrapper implements Task {

    /** internal task */
	private final Task task;
    
    /** 
     * Internal Map for the datatypes of the fields in the knowledge object.
     */
    private Map<String, Class<?>> dataTypesMap;
    
    /**
     * Internal cache for fielnames, getters and setters of TaskImpl
     */
    private Map<String, Method> taskImplFieldsMap;

    /**
     * Construct an instance wrapped around the specified
     * {@link com.top_logic.knowledge.objects.KnowledgeObject}.
     *
     * This CTor is only for the WrapperFactory! <b>DO NEVER USE THIS
     * CONSTRUCTOR!</b> Always use the getInstance() method of the wrappers.
     *  
     * @param    ko        The KnowledgeObject, must never be <code>null</code>.
     */
    public TaskWrapper(KnowledgeObject ko) {
        super(ko);
		{
			String aName = (String) ko.getAttributeValue(TaskAttributes.TASK_NAME);
			int aDaytype = ((Integer) ko.getAttributeValue(TaskAttributes.DAYTYPE)).intValue();
			int aDaymask = ((Integer) ko.getAttributeValue(TaskAttributes.DAYMASK)).intValue();
			int anHour = ((Integer) ko.getAttributeValue(TaskAttributes.HOUR)).intValue();
			int aMinute = ((Integer) ko.getAttributeValue(TaskAttributes.MINUTE)).intValue();
            long   anInterval  = 0;
            int    aStopHour   = 0;
            int    aStopMinute = 0;
            Date   aDate       = null;

            if (0 != (aDaytype & LegacySchedulesCommon.PERIODICALLY)) {
				anInterval = ((Long) ko.getAttributeValue(TaskAttributes.INTERVAL)).longValue();
				aStopHour = ((Integer) ko.getAttributeValue(TaskAttributes.STOP_HOUR)).intValue();
				aStopMinute = ((Integer) ko.getAttributeValue(TaskAttributes.STOP_MINUTE)).intValue();
                this.task = this.createInnerTask(aName, aDaytype, aDaymask, anHour, aMinute, anInterval, aStopHour, aStopMinute);
            } 
            else if (aDaytype == LegacySchedulesCommon.DATE) {
				aDate = (Date) ko.getAttributeValue(TaskAttributes.WHEN);
                this.task = this.createInnerTask(aName, anHour, aMinute, aDate); 
            } 
            else {
                this.task = this.createInnerTask(aName, aDaytype, aDaymask, anHour, aMinute);
            }
        }
    }

    protected TaskImpl createInnerTask(String aName, int aDaytype, int aDaymask, int anHour, int aMinute) {
        return new TaskImpl(aName, aDaytype, aDaymask, anHour, aMinute);
    }

    protected TaskImpl createInnerTask(String aName, int anHour, int aMinute, Date aDate) {
        return new TaskImpl(aName, aDate, anHour, aMinute);
    }

    protected TaskImpl createInnerTask(String aName, int aDaytype, int aDaymask, int anHour, int aMinute, long anInterval, int aStopHour, int aStopMinute) {
        return new TaskImpl(aName, aDaytype, aDaymask, anHour, aMinute, anInterval, aStopHour, aStopMinute);
    }

    /**
	 * Create a new Task including the needed KO.
	 * 
	 * This will not add the Task to the Scheduler, but on the next it will be loaded so better add
	 * it later. Will not commit() the KB.
	 * 
	 * @param aKB
	 *        KnowledgeBase where the new task will be created.
	 */
    protected static KnowledgeObject createKO(
        KnowledgeBase aKB, String aKOType, String name,
        int daytype      , int daymask,
			int hour, int minute) {
    	return internalCreateKO(aKB, aKOType, name, daytype, daymask, hour, minute, 0, 0, 0, null);
    }

    /**
	 * Construct a new Task to be run at a specific date and the indicated time.
	 * 
	 * This will not add the Task to the Scheduler, but on the next it will be loaded so better add
	 * it later. Will not commit() the KB.
	 * 
	 * @param aKB
	 *        KnowledgeBase where the new task will be created.
	 */
	protected static KnowledgeObject createKO(KnowledgeBase aKB, String aKOType, String name, Date when, int hour,
			int minute) {
    	return internalCreateKO(aKB, aKOType, name, LegacySchedulesCommon.DATE, 0, hour, minute, 0, 0, 0, when);
    }

    /**
	 * Construct a new Task to be run periodically.
	 * 
	 * This will not add the Task to the Scheduler, but on the next it will be loaded so better add
	 * it later. Will not commit() the KB.
	 * 
	 * @param aKB
	 *        KnowledgeBase where the new task will be created.
	 */
    protected static KnowledgeObject createKO(
        KnowledgeBase aKB   , String aKOType, String name,
        int daytype         , int daymask,
        int startHour       , int startMinute,
        long interval       ,
			int stopHour, int stopMinute) {
    	
    	return internalCreateKO(aKB, aKOType, name, daytype, daymask, startHour, startMinute, interval, stopHour, stopMinute, null);
    }
    
    /**
     * Create a new knwolegdeobject of type task with all attributes initially filled.
     * 
     * @param aKOType  	   the knowledge object type
     * @param name         the name of the task
     * @param daytype	   @see {@link TaskAttributes#DAYTYPE}
     * @param daymask	   @see {@link TaskAttributes#DAYMASK}
     * @param startHour    @see {@link TaskAttributes#HOUR}
     * @param startMinute  @see {@link TaskAttributes#MINUTE}
     * @param interval     @see {@link TaskAttributes#INTERVAL}
     * @param stopHour     @see {@link TaskAttributes#STOP_HOUR}
     * @param stopMinute   @see {@link TaskAttributes#STOP_MINUTE}
     * @param when         @see {@link TaskAttributes#WHEN}
     */
    private static KnowledgeObject internalCreateKO(KnowledgeBase aKB, String aKOType, String name, int daytype, int daymask, 
    	int startHour       , int startMinute,
        long interval       ,
        int stopHour        , int stopMinute,
			Date when) {
    	
		{
			NameValueBuffer initialValues = new NameValueBuffer();
			initialValues.setValue(TaskAttributes.TASK_NAME, name);
			initialValues.setValue(TaskAttributes.DAYTYPE, Integer.valueOf(daytype));
			initialValues.setValue(TaskAttributes.DAYMASK, Integer.valueOf(daymask));
			initialValues.setValue(TaskAttributes.HOUR, Integer.valueOf(startHour));
			initialValues.setValue(TaskAttributes.MINUTE, Integer.valueOf(startMinute));
			initialValues.setValue(TaskAttributes.INTERVAL, Long.valueOf(interval));
			initialValues.setValue(TaskAttributes.STOP_HOUR, Integer.valueOf(stopHour));
			initialValues.setValue(TaskAttributes.STOP_MINUTE, Integer.valueOf(stopMinute));
			initialValues.setValue(TaskAttributes.WHEN, when);
            
			return aKB.createKnowledgeObject(aKOType, initialValues);
        }
    	
    }

    /**
     * Overwritten to handle differences in data types.
     * These problems will be resolved in further versions. 
     * 
     * @see com.top_logic.knowledge.wrap.Wrapper#setValue(java.lang.String, java.lang.Object)
     */
    @Override
	public void setValue(String aName, Object aValue) {
           // Really stupid solution to convert
           // the incoming strings to the right DataTypes. 
           // TODO ASC: Should be replaced by a generic solution
           // The cause for this solution is, that the JAVA-Datatypes for KnowledgeAttributes
           // do not seem to be readable in any way (getAttribute.getMetaObject().getAttributes("att_name")
           // and all other possibilities lead to empty, Exception throwing or nonexisting methods.)
          Object theRealValue;
          if (aValue instanceof Date) {
              theRealValue = aValue;
         }  else {
              theRealValue = convertToRightDataType(aName, aValue);
              
          } 
            setTaskImplValue(aName, theRealValue);
            super.setValue(aName, theRealValue);
    }
    
       
    /**
     * Indicates the type of daily Schedule needed. 
     * @return the Daytype
     */
	public int getDaytype() {
		return tGetDataInt(TaskAttributes.DAYTYPE);
    }
    
    /**
     * Bitmask indicating Day in Week/Month for WEEKLY/MONTHLY 
     * 
     * Bit 0 is Sunday, Bit 6 is Saturday (for WEEKLY)
     * @return an int between 0 and 6.
     */
	public int getDaymask() {
		return tGetDataInt(TaskAttributes.DAYMASK);
    }
    
    /** 
     * Optional Date at which a Task should be scheduled (TaskAttributes.WHEN)
     * @return a Date or <b>null</b>
     */
	public Date getDate() {
		return tGetDataDate(TaskAttributes.WHEN);
    }
    
    /** 
     * At what hour (0..23) shall the task run (or start).
     * @return an int between 0 and 23.
     */
	public int getHour() {
		return tGetDataInt(TaskAttributes.HOUR);
    }
    
    /** 
     * At what minute (0..59) shall the task run (or start).
     * @return an int between 0 and 59.
     */
	public int getMinute() {
		return tGetDataInt(TaskAttributes.MINUTE);
    }
    
    /** 
     * Interval that Task should run PERIODICALLY (in milliseconds). 
     * @return the intervalin milliseconds
     */
	public long getInterval() {
		return tGetDataLongValue(TaskAttributes.INTERVAL);
    }
    
    /** 
     * At what hour (1..24) shall the task stop, 0 indicates no stop at all 
     * @return an int between 0 and 24. (No Integer because of problems with constraint 
     *          implementations like RangedFormConstraint)
     */
	public int getStopHour() {
		return tGetDataInt(TaskAttributes.STOP_HOUR);
    }
    
    /**
     * At what minute (1..60) shall the task run (or start) 
     * @return an int between 1 and 60.
     */
	public int getStopMinute() {
		return tGetDataInt(TaskAttributes.STOP_MINUTE);
    }
    
    /**
     * Signalizes that the Task should run periodically.
     * 
     * @return <b>true</b> if the <code>periodically</code> bitflag
     *          is set.
     */
	public boolean isPeriodically() {
		int theDaytype = tGetDataInt(TaskAttributes.DAYTYPE);
        
        return (theDaytype & LegacySchedulesCommon.PERIODICALLY ) != 0;
        
    }    

	@Override
	public SchedulingAlgorithm getSchedulingAlgorithm() {
		return task.getSchedulingAlgorithm();
	}

    @Override
	public long calcNextShed(long notBefore) {
        return task.calcNextShed(notBefore);
    }

	@Override
	public boolean markAsRun(long start) {
		return task.markAsRun(start);
	}

	@Override
	public long getLastSchedule() {
		return task.getLastSchedule();
	}

	@Override
	public void forceRun(long start) {
		task.forceRun(start);
	}

	@Override
	public boolean isForcedRun() {
		return task.isForcedRun();
	}

    /** delegate to embedded task */
    @Override
	public String getName() {
        return task.getName();
    }

    /** delegate to embedded task */
    @Override
	public long getNextShed() {
        return task.getNextShed();
    }

    /** delegate to embedded task */
    @Override
	public Maybe<Calendar> getNextSchedule() {
		return task.getNextSchedule();
	}

	/** delegate to embedded task */
	@Override
	public long getStarted() {
        return task.getStarted();
    }

    /** delegate to embedded task */
    @Override
	public void run() {
        task.run();
    }

    /** delegate to embedded task */
    @Override
	public boolean signalStop() {
        return task.signalStop();
    }

	/** delegate to embedded task */
	@Override
	public boolean shouldStop() {
		return task.shouldStop();
	}

    /** delegate to embedded task */
    @Override
	public void signalShutdown() {
        task.signalShutdown();
    }

    /** delegate to embedded task */
    @Override
	public void started(long when) {
        task.started(when);
    }

    @Override
	protected String toStringValues() {
		return super.toStringValues() + ", task: " + task.toString();
    }

    /**
     * This method converts the the value <code>aValue</code> from <code>String</code>
     * to the data type mapped in the internal map of data types for the attributes in the
     * knowledge object.
     * 
     * Currently the following data types are supported:
     * <ul>
     * <li>Integer</li>
     * <li>Long</li>
     * <li>Date</li>
     * <li>String</li>
     * </ul>
     * 
     * @param aFieldName    the name of the field (normally contained in 
     *                      {@link com.top_logic.util.sched.task.TaskAttributes}).
     * @param aValue        the value to convert.
     * @return              the converted value as Integer, Long, Date or String 
     *                      (if it is a String or an error occured.)
     */
    private Object convertToRightDataType(String aFieldName, Object aValue) {
        if(dataTypesMap == null) {
            fillDataTypesMap();
        }
        
        if(dataTypesMap.get(aFieldName) != null) {
            if(dataTypesMap.get(aFieldName).equals(Integer.class)) {
                if (aValue instanceof String) {
                    return Integer.decode((String)aValue);
                } else {
					return Integer.valueOf(((Number) aValue).intValue());
                }
            } else if(dataTypesMap.get(aFieldName).equals(Long.class)) {
                if (aValue instanceof String) {
                    return Long.decode((String)aValue);
                } else {
					return Long.valueOf(((Number) aValue).longValue());
                }
            } else if (dataTypesMap.get(aFieldName).equals(Date.class)) {      
				Formatter formatter = HTMLFormatter.getInstance(TimeZones.defaultUserTimeZone(), Locale.GERMANY);
                DateFormat theFormat = formatter.getDateFormat();
                
                try {
                    if (aValue instanceof String) {
                        return theFormat.parse((String)aValue);
                    } 
                } catch (ParseException e) {
                    Logger.error("Exception parsing date",e, this);
                }
            }
        }
        return aValue;
    }
    
    /**
     * This method initializes the Map containing the DataTypes 
     * of the known fields in the DataObject of the wrapped task.
     *
     */
    private void fillDataTypesMap() {
        dataTypesMap = new HashMap<>(11);
        dataTypesMap.put(TaskAttributes.DAYMASK,Integer.class);
        dataTypesMap.put(TaskAttributes.WHEN,Date.class);
        dataTypesMap.put(TaskAttributes.DAYTYPE,Integer.class);
        dataTypesMap.put(TaskAttributes.HOUR,Integer.class);
        dataTypesMap.put(TaskAttributes.INTERVAL,Long.class);
        dataTypesMap.put(TaskAttributes.MINUTE,Integer.class);
        dataTypesMap.put(TaskAttributes.TASK_NAME,String.class);
        dataTypesMap.put(TaskAttributes.NEXT_SHED,Long.class);
        dataTypesMap.put(TaskAttributes.STARTED,Long.class);
        dataTypesMap.put(TaskAttributes.STOP_HOUR,Integer.class);
        dataTypesMap.put(TaskAttributes.STOP_MINUTE,Integer.class);
    }
    
    private void fillTaskImplFields() {
        PropertyDescriptor[] theProperties=null;
        try {
            theProperties = Introspector.getBeanInfo(TaskImpl.class)
                                .getPropertyDescriptors();
            taskImplFieldsMap = new HashMap<>(theProperties.length);                    
        } catch (IntrospectionException e) {
            Logger.error("Problem getting BeanInfos of TaskImpl.",e,this);
            return;
        }

        for(int i=0;i<theProperties.length;i++) {
            Method theWriteMethod = theProperties[i].getWriteMethod();
            taskImplFieldsMap.put(theProperties[i].getName(), theWriteMethod);
        }
    }
    
    private void setTaskImplValue(String aFieldName, Object aValue) {
        if(taskImplFieldsMap == null) {
            fillTaskImplFields();
        }
        Method theWriteMethod = taskImplFieldsMap.get(aFieldName);
        try {
			// Uh, not having a setter for a property is surely not an error
			if (theWriteMethod != null) {
				theWriteMethod.invoke(task, new Object[] { aValue });
			}
        } catch (Exception ex) {
			// IllegalArgumentException e) {
                 // IllegalAccessException
                 // InvocationTargetException
                 // ClassCastException
             Logger.error("The value for " + aFieldName + " could not be set.",ex ,this);
        }

    }

	@Override
	public boolean isNodeLocal() {
		return task.isNodeLocal();
	}

	@Override
	public boolean needsMaintenanceMode() {
		return task.needsMaintenanceMode();
	}

	@Override
	public long getMaintenanceModeDelay() {
		return task.getMaintenanceModeDelay();
	}

	@Override
	public boolean isMaintenanceModeSafe() {
		return task.isMaintenanceModeSafe();
	}

	@Override
	public boolean isRunOnStartup() {
		return task.isRunOnStartup();
	}

	@Override
	public boolean isPersistent() {
		return task.isPersistent();
	}

	@Override
	public TaskLog getLog() {
		return task.getLog();
	}

	@Override
	public void attachTo(Scheduler scheduler) {
		task.attachTo(scheduler);
	}

	@Override
	public void detachFromScheduler() {
		task.detachFromScheduler();
	}

	@Override
	public void logGuiWarning(String message) {
		task.logGuiWarning(message);
	}

	@Override
	public boolean isBlockingAllowed() {
		return task.isBlockingAllowed();
	}

	@Override
	public boolean isBlockedByDefault() {
		return task.isBlockedByDefault();
	}
}

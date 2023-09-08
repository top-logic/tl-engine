/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.time.CalendarUtil;

/**
 * Represent a date interval from the start date (00:00:00,000)
 * to the end date (23:59:59,999).
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class DayIntervall {
	private Date startDate;
	private Date endDate;
	
	/**
	 * CTor with start and end date 
	 * 
	 * @param start	the start date. May be <code>null</code>.
	 * @param end	the end date. may be <code>null</code>.
	 */
	public DayIntervall(Date start, Date end){
		Calendar cal = CalendarUtil.createCalendar();
		if(start !=null){
			cal.setTime(start);
			cal.set(Calendar.HOUR_OF_DAY,0);
			cal.set(Calendar.MINUTE,0);
			cal.set(Calendar.SECOND,0);	
			cal.set(Calendar.MILLISECOND,0);
			startDate = cal.getTime();									
		}
		if(end !=null){
			cal.setTime(end);
			cal.set(Calendar.HOUR_OF_DAY,23);
			cal.set(Calendar.MINUTE,59);
			cal.set(Calendar.SECOND,59);	
			cal.set(Calendar.MILLISECOND,999);
			endDate = cal.getTime();									
		}		
	}
	
	/**
	 * The duration as string.
	 */
	public String getDurationString() {
		return (this.startDate + " - " + this.endDate);
	}

	/**
	 * Check if the given interval intersects the current one.
	 * 
	 * @param anIntervall	the interval. Must not be <code>null</code>.
	 * @return true if the given interval intersect the current one.
	 */
	public boolean intersects(DayIntervall anIntervall){
		boolean res=true;
		if(startDate !=null && anIntervall.endDate !=null){
			res=startDate.before(anIntervall.endDate);	
		}
		if(endDate !=null && anIntervall.startDate !=null){
			res=res && endDate.after(anIntervall.startDate)	;
		}
		return res;	
	}
	
	/**
	 * Check if the given date is contained in the current interval.
	 * 
	 * @param aDate	the date. Must not be <code>null</code>.
	 * @return if the given date is contained in the current interval.
	 */
	public boolean contains(Date aDate){
		if (startDate == null && endDate == null) {
			return aDate == null;
		}
		if (aDate == null) {
			return false;
		}
		boolean res=true;
		if(startDate !=null ){
			res=startDate.before(aDate) || startDate.equals(aDate);	
		}
		if(endDate !=null ){
			res=res && endDate.after(aDate) || endDate.equals(aDate);
		}
		return res;				
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return "DayIntervall["+getDurationString()+"]";	
	}
	
}
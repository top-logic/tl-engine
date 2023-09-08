/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.Serializable;
import java.util.Date;

import com.top_logic.reporting.remote.ReporterState;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReporterStateImpl implements ReporterState, Serializable {

	private static final long serialVersionUID = -245508921546685486L;
    
	private final int numOpen;
	private final int numDone;
	private final int numError;
	private Date date;

	
	public ReporterStateImpl(int aNumOpen, int aNumDone, int aNumError, Date aDate) {
		numOpen = aNumOpen;
		numDone = aNumDone;
		numError = aNumError;
		date = aDate;
	}	
	
	
	public ReporterStateImpl(int aNumOpen, int aNumDone, int aNumError) {
		numOpen = aNumOpen;
		numDone = aNumDone;
		numError = aNumError;
		date = new Date();
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public int getNumberOfDoneReports() {
		return numDone;
	}

	@Override
	public int getNumberOfErrorReports() {
		return numError;
	}

	@Override
	public int getNumberOfOpenReports() {
		return numOpen;
	}

}

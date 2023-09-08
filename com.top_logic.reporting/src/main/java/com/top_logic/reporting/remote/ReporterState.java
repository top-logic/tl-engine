/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote;

import java.util.Date;

/**
 * Interface  for the state of a Reporter 
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public interface ReporterState {

	/**
	 * number of reports which are open
	 */
	public int getNumberOfOpenReports();
	
	/**
	 * number of reports which are done
	 */
	public int getNumberOfDoneReports();

	/**
	 * number of reprorts which had an error
	 */
	public int getNumberOfErrorReports();
	
	/**
	 * moment in time represented by this ReporterState
	 */
	public Date getDate();
	
	
	
	
}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.remote.common;

import java.io.Serializable;

import com.top_logic.reporting.remote.ReportStatus;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class ReportStatusImpl implements ReportStatus, Serializable {
	
	private static final long serialVersionUID = -4844108324205739715L;
    
	private int       expectedDuration = -1;
	private Exception exception        = null;
	
	public ReportStatusImpl(int aExpectedDuration,Exception anException) {
		expectedDuration = aExpectedDuration;
		exception        = anException;
	}
	
	public ReportStatusImpl(int aExpectedDuration) {
		expectedDuration=aExpectedDuration;
	}

	public ReportStatusImpl(Exception anException) {
		exception=anException;
	}

	@Override
	public int getExpectedDuration() {
		return expectedDuration;
	}

	@Override
	public boolean isError() {
		return exception!=null || expectedDuration<0;
	}

	@Override
	public boolean isDone() {
		return expectedDuration==0;
	}

    @Override
	public Exception getException() {
        return exception;
    }

}

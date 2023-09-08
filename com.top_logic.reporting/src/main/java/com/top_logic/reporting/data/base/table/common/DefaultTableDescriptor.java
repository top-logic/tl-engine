/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.data.base.table.common;

import java.util.List;

import com.top_logic.reporting.data.base.description.Description;
import com.top_logic.reporting.data.base.table.AbstractTableDescriptor;

/**
 * @author    <a href="mailto:tri@top-logic.com">tri</a>
 */
public class DefaultTableDescriptor extends AbstractTableDescriptor {

	private static final String DESCRIPTOR_ID = "DefaultTableDescriptor";
	private String displayName;
	
	public DefaultTableDescriptor(List aHeaderRow, List aHeaderColumn, Class aType, int valueSize, String displayName){
		super(aHeaderRow,aHeaderColumn, aType,valueSize);
		this.displayName = displayName;		
	}

	public DefaultTableDescriptor(List aHeaderRow, List aHeaderColumn, Description aDescription){
		super(aHeaderRow,aHeaderColumn, aDescription.getType(),aDescription.getNumberOfValueEntries());
		this.displayName = aDescription.getDisplayName();		
	}

    /**
     * @see com.top_logic.reporting.data.base.description.Description#getEntries()
     */
    @Override
	public String[] getEntries() {
        throw new UnsupportedOperationException("getEntries() has to be implemented");
    }

	/**
	 * @see com.top_logic.reporting.data.base.description.Description#getName()
	 */
	@Override
	public String getName() {
		return DESCRIPTOR_ID;
	}

	/**
	 * @see com.top_logic.reporting.data.base.description.Description#getDisplayName()
	 */
	@Override
	public String getDisplayName() {
		return this.displayName;
	}


}

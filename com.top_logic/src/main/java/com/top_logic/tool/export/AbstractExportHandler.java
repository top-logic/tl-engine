/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * The AbstractExportHandler provides basic security features.
 * 
 * @author    <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class AbstractExportHandler implements ExportHandler {

	
	private final String exportID;
	
	public AbstractExportHandler(String anExportID) {
		this.exportID = anExportID;
	}

	@Override
	public final String getExportHandlerID() {
		return exportID;
	}
	
	/**
	 * Use {@link SimpleBoundCommandGroup#EXPORT} as default
	 * 
	 * @see com.top_logic.tool.export.ExportHandler#getExportCommandGroup()
	 */
    @Override
	public BoundCommandGroup getExportCommandGroup() {
        return SimpleBoundCommandGroup.EXPORT;
    }

    /**
	 * Use {@link SimpleBoundCommandGroup#EXPORT} as default
     * 
     * @see com.top_logic.tool.export.ExportHandler#getReadCommandGroup()
     */
    @Override
	public BoundCommandGroup getReadCommandGroup() {
        return SimpleBoundCommandGroup.EXPORT;
    }
    
    /**
	 * Return <code>false</code> as default.
     * 
     * @see com.top_logic.tool.export.ExportHandler#isPersonalized()
     */
    @Override
	public boolean isPersonalized() {
    	return false;
    }
}


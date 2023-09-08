/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.export.ExportQueueManager.ExportExecutor;
import com.top_logic.util.TLContext;

/**
 * An {@link ExportHandler} creates an export for a business model.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface ExportHandler {

	public static final Class[] CONSTRUCTOR_SIGNATURE = { String.class };
	
	public static final String TECHNOLOGY_WORD = "WORD";
	public static final String TECHNOLOGY_EXCEL = "EXCEL";
	public static final String TECHNOLOGY_POWERPOINT = "POWERPOINT";

	public static final String DURATION_SHORT = "SHORT";
	public static final String DURATION_LONG = "LONG";
	
	/**
	 * Perform the export for a model. The Handler must fill
	 * {@link ExportResult} with appropriate values.
	 */
	void exportObject(Object aModel, ExportResult aResult);

	/**
	 * Return the technology this {@link ExportHandler} makes use of. The
	 * technology is significant for selection a {@link ExportExecutor} of the
	 * {@link ExportQueueManager}.
	 */
	String getExportTechnology();

	/**
	 * Return the estimated duration of a typical {@link Export} this handler
	 * will perform. The duration is another criteria for selection of a
	 * {@link ExportExecutor} of the {@link ExportQueueManager}.
	 */
	String getExportDuration();

	/**
	 * Return the {@link BoundCommandGroup} that allows a user to download/read
	 * the export result. This {@link BoundCommandGroup} will be added
	 * automatically to {@link BoundComponent#getCommandGroups()}.
	 */
	BoundCommandGroup getReadCommandGroup();

	/**
	 * Return the {@link BoundCommandGroup} that allows a user to start or
	 * enqueue this export. This {@link BoundCommandGroup} will be added
	 * automatically to {@link BoundComponent#getCommandGroups()}.
	 */
	BoundCommandGroup getExportCommandGroup();

	/**
	 * <p>
	 * A personalized export will be created in a {@link TLContext} with the current person set.
	 * Later the export result will be accessible the that person only.  
	 * </p>
	 * <p>
	 * Not personalized exports will be created in a root context and can be accessed by all users.
	 * </p>
	 */
	boolean isPersonalized();
	
	/**
	 * Check weather this {@link ExportHandler} can export the given object.
	 */
	boolean canExport(Object aModel);
	
	/**
	 * Return the ID of this {@link ExportHandler}. The ID is usually set by {@link ExportHandlerRegistry}.
	 */
	String getExportHandlerID();
}

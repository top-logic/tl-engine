/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.tool.export.Export.State;

/**
 * The ExportRegistry provides access to {@link Export}s.
 * 
 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
 */
public interface ExportRegistry {

    /**
     * Return an {@link Export} for a handler id and a model. The method must not return the
     * same object for the same handler and model.
     */
    Export getExport(String aHandlerID, Object aModel);

	/**
	 * <p>
	 * Retrieve an {@link Export} for further processing. The returned {@link Export} will be set to
	 * {@link State#RUNNING}.
	 * </p>
	 * <p>
	 * The method will search for the {@link Export} that is currently in {@link State#QUEUED} and
	 * matches a given technology and possible a given duration.
	 * </p>
	 * <p>
	 * If there are more than one {@link Export}s matching the criteria, then the {@link Export}
	 * which was queued first will be returned (FIFO).
	 * </p>
	 * 
	 * @throws ExportFailure
	 *         If access to the {@link ExportRegistry} fails.
	 */
    Export getNextRunningExport(String aTechnology, String aDuration) throws ExportFailure;

	/** 
	 * Starts this {@link ExportRegistry}.
	 * 
	 * @throws ExportFailure
	 *         If starting fails.
	 */
	void startup() throws ExportFailure;

	/** 
	 * Stops and cleans this {@link ExportRegistry}.
	 */
	void shutdown();
}

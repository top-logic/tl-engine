/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import com.top_logic.layout.table.TableModel;


/**
 * Common interface for observers of a {@link TableModel}.
 * 
 * <p>
 * This interface is modeled in the style of
 * {@link javax.swing.event.TableModelListener}. However, the Swing version
 * could not be reused, beacuse it has an indirect dependency (via
 * {@link javax.swing.event.TableModelEvent}) to the Swing interface
 * {@link javax.swing.table.TableModel}, which could not be reused.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TableModelListener {
	
	/**
	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
	 */
	public void handleTableModelEvent(TableModelEvent event);
	
}

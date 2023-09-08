/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.event.ListDataEvent;

/**
 * Assertion on a {@link ListDataEvent}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class Check {

	public abstract void checkEvent(ListDataEvent evt);
	
}

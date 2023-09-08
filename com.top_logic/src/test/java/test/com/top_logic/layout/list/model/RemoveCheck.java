/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.event.ListDataEvent;

import junit.framework.Assert;

/**
 * {@link Check} that tests for list element removal.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveCheck extends Check {

	private final int first;
	private final int last;

	public RemoveCheck(int first, int last) {
		this.first = first;
		this.last = last;
	}

	@Override
	public void checkEvent(ListDataEvent evt) {
		Assert.assertEquals("Is add event.", ListDataEvent.INTERVAL_REMOVED, evt.getType());
		Assert.assertEquals("Range start match.", first, evt.getIndex0());
		Assert.assertEquals("Range stop match.", last, evt.getIndex1());
	}

}

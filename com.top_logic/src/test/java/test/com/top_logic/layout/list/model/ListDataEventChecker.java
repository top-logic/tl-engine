/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import junit.framework.Assert;

/**
 * {@link ListDataListener} that applies {@link Check}s to reported {@link ListDataEvent}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListDataEventChecker implements ListDataListener {

	private Check[] checks;
	private int currentIndex;

	public ListDataEventChecker() {
		this.checks = new Check[] {};
	}
	
	public ListDataEventChecker(Check check) {
		this.checks = new Check[] {check};
	}

	public ListDataEventChecker(Check check1, Check check2) {
		this.checks = new Check[] {check1, check2};
	}

	public ListDataEventChecker(Check check1, Check check2, Check check3) {
		this.checks = new Check[] {check1, check2, check3};
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		check(e);
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		check(e);
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		check(e);
	}

	private void check(ListDataEvent e) {
		Assert.assertTrue("Expected event count.", currentIndex < checks.length);
		checks[currentIndex++].checkEvent(e);
	}

	public boolean expectsMoreEvents() {
		return currentIndex < checks.length;
	}

}

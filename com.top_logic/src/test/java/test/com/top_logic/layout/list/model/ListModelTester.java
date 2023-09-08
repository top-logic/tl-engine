/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.list.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import junit.framework.Assert;

import com.top_logic.layout.basic.AbstractAttachable;

/**
 * Attaches as {@link ListDataListener} to a {@link ListModel} and interprets
 * all sent events.
 * 
 * <p>
 * An instance of this class tries to reconstruct all elements of the base model
 * by listening to the events sent by the base model. This instance can be
 * checked for equality with its base model after each modification done to its
 * base.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListModelTester extends AbstractAttachable implements ListDataListener {

	private ListModel base;
	private List observedElements;
	private int maxEventCount;
	private int eventCount = 0;
	
	public ListModelTester(ListModel base, int maxEventCount) {
		if (maxEventCount < 0)
			throw new IndexOutOfBoundsException("maxEventCount must be geq 0!");
		this.base = base;
		this.maxEventCount = maxEventCount;
	}

	public ListModelTester(ListModel base) {
		this.base = base;
		this.maxEventCount = -1;
	}

	@Override
	protected void internalAttach() {
		base.addListDataListener(this);
		observedElements = new ArrayList(base.getSize());
		for (int cnt = base.getSize(), n = 0; n < cnt; n++) {
			observedElements.add(base.getElementAt(n));
		}
	}

	@Override
	protected void internalDetach() {
		base.removeListDataListener(this);
		observedElements = null;
		eventCount = 0;
	}
	
	@Override
	public void contentsChanged(ListDataEvent evt) {
		for (int stop = evt.getIndex1(), n = evt.getIndex0(); n <= stop; n++) {
			observedElements.set(n, base.getElementAt(n));
		}
		eventCount++;
		if (eventCount == maxEventCount) {
			detach();
		}
	}
	
	@Override
	public void intervalAdded(ListDataEvent evt) {
		for (int stop = evt.getIndex1(), n = evt.getIndex0(); n <= stop; n++) {
			observedElements.add(n, base.getElementAt(n));
		}
		eventCount++;
		if (eventCount == maxEventCount) {
			detach();
		}
	}

	@Override
	public void intervalRemoved(ListDataEvent evt) {
		for (int stop = evt.getIndex0(), n = evt.getIndex1(); n >= stop; n--) {
			observedElements.remove(n);
		}
		eventCount++;
		if (eventCount == maxEventCount) {
			detach();
		}
	}

	public Object getElementAt(int index) {
		return observedElements.get(index);
	}

	public int getSize() {
		return observedElements.size();
	}

	/**
	 * Checks the buffered elements against the base model.
	 * 
	 * <p>
	 * If both collection of elements are not equal, an {@link AssertionError}
	 * is thrown.
	 * </p>
	 */
	public void check() {
		if (isAttached()) {
			Assert.assertEquals(observedElements.size(), base.getSize());
			for (int cnt = base.getSize(), n = 0; n < cnt; n++) {
				Assert.assertEquals(observedElements.get(n), base.getElementAt(n));
			}
		}
	}

	/**
	 * This method sets the maxEventCount.
	 *
	 * @param    maxEventCount    The maxEventCount to set.
	 */
	public void setMaxEventCount(int maxEventCount) {
		this.maxEventCount = maxEventCount;
	}
}

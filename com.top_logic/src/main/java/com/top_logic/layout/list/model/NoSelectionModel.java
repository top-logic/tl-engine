/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list.model;

import javax.swing.event.ListSelectionListener;

/**
 * {@link RestrictedListSelectionModel} that prevents all selection.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoSelectionModel implements RestrictedListSelectionModel {

	public static final NoSelectionModel INSTANCE = new NoSelectionModel();
	
	private NoSelectionModel() {
		// Singleton constructor.
	}
	
	@Override
	public void addListSelectionListener(ListSelectionListener x) {
		// Ignored.
	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearSelection() {
		// Ignored.
	}

	@Override
	public int getAnchorSelectionIndex() {
		return -1;
	}

	@Override
	public int getLeadSelectionIndex() {
		return -1;
	}

	@Override
	public int getMaxSelectionIndex() {
		return -1;
	}

	@Override
	public int getMinSelectionIndex() {
		return -1;
	}

	@Override
	public int getSelectionMode() {
		return SINGLE_SELECTION;
	}

	@Override
	public boolean getValueIsAdjusting() {
		return false;
	}

	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSelectedIndex(int index) {
		return false;
	}

	@Override
	public boolean isSelectionEmpty() {
		return true;
	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeListSelectionListener(ListSelectionListener x) {
		// Ignored.
	}

	@Override
	public void removeSelectionInterval(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAnchorSelectionIndex(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLeadSelectionIndex(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSelectionMode(int selectionMode) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setValueIsAdjusting(boolean valueIsAdjusting) {
		// Ignored
	}

	@Override
	public boolean isSelectableIndex(int index) {
		return false;
	}
}

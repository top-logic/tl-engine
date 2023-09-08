/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Selection model that cannot select any object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoSelectionModel implements SelectionModel {

	public static final NoSelectionModel INSTANCE = new NoSelectionModel();
	
	/**
	 * Creates a new {@link NoSelectionModel}.
	 */
	protected NoSelectionModel() {
		// nothing to set here.
	}
	
	@Override
	public boolean isSelectable(Object obj) {
		return false;
	}
	
	@Override
	public boolean isMultiSelectionSupported() {
		return false;
	}

	@Override
	public void clear() {
		// Immutable.
	}

	@Override
	public Set<?> getSelection() {
		return Collections.emptySet();
	}

	@Override
	public boolean isSelected(Object obj) {
		return false;
	}

	@Override
	public void setSelected(Object obj, boolean select) {
		// Immutable.
	}

	@Override
	public void addToSelection(Collection<?> objects) {
		// Immutable.
	}

	@Override
	public void removeFromSelection(Collection<?> objects) {
		// Immutable.
	}

	@Override
	public void setSelection(Set<?> newSelection) {
		// Immutable.
	}

	@Override
	public boolean addSelectionListener(SelectionListener listener) {
		// Immutable, no updates, no listener notification required.
		return true;
	}
	
	@Override
	public boolean removeSelectionListener(SelectionListener listener) {
		// Immutable, no updates, no listener notification required.
		return true;
	}

	@Override
	public ModelName getModelName() {
		return ModelResolver.buildModelName(this);
	}

	@Override
	public SelectionModelOwner getOwner() {
		// Singletons have no owner. Therefore, a custom ModelNamingScheme is registered
		// (NoSelectionModelNaming), and so this method isn't needed anyway.
		return SelectionModelOwner.NO_OWNER;
	}

}

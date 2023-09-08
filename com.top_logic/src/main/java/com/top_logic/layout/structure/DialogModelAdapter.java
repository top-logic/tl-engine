/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.awt.Dimension;

import com.top_logic.layout.basic.Command;

/**
 * Adapter for a {@link DialogModel}.
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DialogModelAdapter extends WindowModelAdapter implements DialogModel {

	/**
	 * Dispatches to {@link #getDialogModelImplementation()}.
	 * 
	 * @see com.top_logic.layout.structure.WindowModelAdapter#getWindowModelImplementation()
	 */
	@Override
	protected WindowModel getWindowModelImplementation() {
		return getDialogModelImplementation();
	}

	/**
	 * Returns the actual {@link DialogModel} implementation to dispatch to.
	 * 
	 * @return not <code>null</code>
	 */
	protected abstract DialogModel getDialogModelImplementation();

	@Override
	public boolean isSet(Property<?> property) {
		return getDialogModelImplementation().isSet(property);
	}

	@Override
	public <T> T get(Property<T> property) {
		return getDialogModelImplementation().get(property);
	}

	@Override
	public <T> T set(Property<T> property, T value) {
		return getDialogModelImplementation().set(property, value);
	}

	@Override
	public <T> T reset(Property<T> property) {
		return getDialogModelImplementation().reset(property);
	}

	@Override
	public boolean hasCloseButton() {
		return getDialogModelImplementation().hasCloseButton();
	}

	@Override
	public boolean isResizable() {
		return getDialogModelImplementation().isResizable();
	}

	@Override
	public boolean isClosed() {
		return getDialogModelImplementation().isClosed();
	}

	@Override
	public Command getCloseAction() {
		return getDialogModelImplementation().getCloseAction();
	}

	@Override
	public String getHelpID() {
		return getDialogModelImplementation().getHelpID();
	}

	@Override
	public void saveCustomizedSize(Dimension size) {
		getDialogModelImplementation().saveCustomizedSize(size);
	}

	@Override
	public boolean hasCustomizedSize() {
		return getDialogModelImplementation().hasCustomizedSize();
	}

	@Override
	public Dimension getCustomizedSize() {
		return getDialogModelImplementation().getCustomizedSize();
	}
}

/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.basic.util.Utils.*;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * A {@link ConfigurationListener} that reacts on a new element in a multiple property and delegates
 * the created element to a {@link CreateHandler}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
final class AddElementListener implements ConfigurationListener {

	private static final TypedAnnotatable.Property<Boolean> DISABLED =
		TypedAnnotatable.property(Boolean.class, "listener_disabled", Boolean.FALSE);

	private final CreateHandler _factory;

	/**
	 * Creates a {@link AddElementListener}.
	 * 
	 * @param factory
	 *        When an element is added, this factory is called with the new elementS.
	 */
	AddElementListener(CreateHandler factory) {
		_factory = requireNonNull(factory);
	}

	/**
	 * Disables {@link AddElementListener} for the current
	 * {@link ThreadContextManager#getInteraction() interaction}.
	 * 
	 * @param b
	 *        Whether the listener must be disabled.
	 * @return The old old "disabled" value. This value should be set when the listener is
	 *         re-activated.
	 */
	public static boolean setDisabled(boolean b) {
		return ThreadContextManager.getInteraction().set(DISABLED, Boolean.valueOf(b)).booleanValue();
	}

	@Override
	public void onChange(ConfigurationChange change) {
		if (change.getKind() != ConfigurationChange.Kind.ADD) {
			return;
		}
		if (ThreadContextManager.getInteraction().get(DISABLED).booleanValue()) {
			return;
		}

		Object newEntry = change.getNewValue();
		ConfigurationItem newConfig = change.getProperty().getConfigurationAccess().getConfig(newEntry);
		_factory.addElement(newEntry, newConfig);
	}

}

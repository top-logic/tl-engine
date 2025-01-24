/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Listener that is invoked, if an objects attribute was set.
 * 
 * @see AbstractStorageBase.Config#getChangeListeners()
 */
public interface AttributeChangeListener {

	/**
	 * Callback that is invoked, when the given object's attribute gets a new value.
	 */
	void notifyChange(TLObject obj, TLStructuredTypePart attr, Object newValue);

	/**
	 * Creates a (combined) {@link AttributeChangeListener} from the given configurations.
	 */
	static AttributeChangeListener instantiate(InstantiationContext context,
			List<PolymorphicConfiguration<? extends AttributeChangeListener>> changeListeners) {
		switch (changeListeners.size()) {
			case 0:
				return none();
			case 1:
				return context.getInstance(changeListeners.get(0));
			default:
				return combine(TypedConfiguration.getInstanceList(context, changeListeners));
		}
	}

	private static AttributeChangeListener none() {
		return (obj, attr, newValue) -> {
			// Ignore.
		};
	}

	private static AttributeChangeListener combine(List<AttributeChangeListener> listeners) {
		return (obj, attr, newValue) -> {
			for (AttributeChangeListener listener : listeners) {
				listener.notifyChange(obj, attr, newValue);
			}
		};
	}

}

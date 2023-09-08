/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import com.top_logic.basic.listener.PropertyObservableBase;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.table.ConfigKey;

/**
 * {@link Collapsible} that stores its state directly to the {@link PersonalConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonalizedExpansionModel extends PropertyObservableBase implements Collapsible {

	private ConfigKey _personalizationKey;

	private boolean _initiallyCollaped;

	/**
	 * Creates a {@link PersonalizedExpansionModel}.
	 * 
	 * @param personalizationKey
	 *        The personalization key to store the state to the {@link PersonalConfiguration}.
	 */
	public PersonalizedExpansionModel(ConfigKey personalizationKey) {
		this(false, personalizationKey);
	}

	/**
	 * Creates a {@link PersonalizedExpansionModel} with an initial collapsed state.
	 * 
	 * @param initiallyCollaped
	 *        Whether the {@link PersonalizedExpansionModel} is initially collapsed or opened.
	 * @param personalizationKey
	 *        The personalization key to store the state to the {@link PersonalConfiguration}.
	 */
	public PersonalizedExpansionModel(boolean initiallyCollaped, ConfigKey personalizationKey) {
		_initiallyCollaped = initiallyCollaped;
		_personalizationKey = personalizationKey;
	}

	@Override
	public boolean isCollapsed() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		Boolean storedValue = pc.getBoolean(_personalizationKey.get());
		boolean collapsed;
		if (storedValue != null) {
			collapsed = storedValue.booleanValue();
		} else {
			collapsed = _initiallyCollaped;
		}
		return collapsed;
	}

	@Override
	public void setCollapsed(boolean newValue) {
		boolean oldValue = isCollapsed();
		if (newValue == oldValue) {
			return;
		}

		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		pc.setBooleanValue(_personalizationKey.get(), newValue);

		notifyListeners(COLLAPSED_PROPERTY, this, oldValue, newValue);
	}

}

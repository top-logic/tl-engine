/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;

/**
 * {@link DefaultExpandable} saving its {@link #getExpansionState()} to the
 * {@link PersonalConfiguration}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PersonalizingExpandable extends DefaultExpandable {

	/**
	 * Value in the personal configuration, that states that the initial expansion state is
	 * {@link Expandable.ExpansionState#NORMALIZED}, and the user have changed to
	 * {@link Expandable.ExpansionState#MINIMIZED}.
	 * 
	 * @see PersonalizingExpandable#NORMALIZED
	 */
	private static final String MINIMIZED = "minimized";

	/**
	 * Value in the personal configuration, that states that the initial expansion state is
	 * {@link Expandable.ExpansionState#MINIMIZED}, and the user have changed to
	 * {@link Expandable.ExpansionState#NORMALIZED}.
	 * 
	 * @see PersonalizingExpandable#MINIMIZED
	 */
	private static final String NORMALIZED = "normalized";

	private final String _personalizationKey;

	private final boolean _initiallyMinimized;

	/**
	 * Creates a {@link PersonalizingExpandable}.
	 * 
	 * @param personalizationKey
	 *        The key under which to store the expansion state (only the minimized state is stored).
	 */
	public PersonalizingExpandable(String personalizationKey) {
		this(personalizationKey, false);
	}

	/**
	 * Creates a {@link PersonalizingExpandable}.
	 * 
	 * @param personalizationKey
	 *        The key under which to store the expansion state (only the minimized state is stored).
	 */
	public PersonalizingExpandable(String personalizationKey, boolean initiallyMinimized) {
		_personalizationKey = personalizationKey;
		_initiallyMinimized = initiallyMinimized;

		setExpansionState(loadCollapsed(personalizationKey, _initiallyMinimized) ? ExpansionState.MINIMIZED
			: ExpansionState.NORMALIZED);
	}

	/**
	 * The key under which the expansion state is stored in the personal configuration.
	 */
	public String getPersonalizationKey() {
		return _personalizationKey;
	}

	@Override
	public void setExpansionState(ExpansionState state) {
		super.setExpansionState(state);

		saveExpansionState(_personalizationKey, state, _initiallyMinimized);
	}

	/**
	 * Helper to store the <code>collapsed</code> part of the given expansion state value under the
	 * given personalization key.
	 * 
	 * @param isCollapsedDefault
	 *        Whether {@link Expandable.ExpansionState#MINIMIZED} is the default
	 *        {@link Expandable.ExpansionState}.
	 */
	public static void saveExpansionState(String personalizationKey, ExpansionState state, boolean isCollapsedDefault) {
		if (state != ExpansionState.MAXIMIZED) {
			saveCollapsed(personalizationKey, state == ExpansionState.MINIMIZED, isCollapsedDefault);
		}
	}

	/**
	 * Helper to store the <code>collapsed</code> flag under the given personalization key.
	 * 
	 * @param isCollapsedDefault
	 *        Whether {@link Expandable.ExpansionState#MINIMIZED} is the default
	 *        {@link Expandable.ExpansionState}.
	 */
	public static void saveCollapsed(String personalizationKey, boolean collapsed, boolean isCollapsedDefault) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration == null) {
			return;
		}
		String value;
		if (collapsed == isCollapsedDefault) {
			// Delete personal entry when value is default
			value = null;
		} else {
			value = isCollapsedDefault ? NORMALIZED : MINIMIZED;
		}
		personalConfiguration.setValue(personalizationKey, value);
	}

	/**
	 * Helper to load the <code>collapsed</code> flag stored under the given personalization key.
	 * 
	 * @param isCollapsedDefault
	 *        Whether {@link Expandable.ExpansionState#MINIMIZED} is the default
	 *        {@link Expandable.ExpansionState}.
	 */
	public static boolean loadCollapsed(String personalizationKey, boolean isCollapsedDefault) {
		PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();
		if (personalConfiguration == null) {
			return isCollapsedDefault;
		}
		Object personalValue = personalConfiguration.getValue(personalizationKey);
		if (personalValue == null) {
			return isCollapsedDefault;
		} else {
			return !isCollapsedDefault;
		}
	}

}

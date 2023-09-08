/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

import com.top_logic.basic.StringServices;

/**
 * Container for the value of a JSP tag attribute of an enumeration type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EnumAttribute extends TagAttribute {

	private final String[] choices;
	private int selectedChoice;
	private int defaultChoice;

	public EnumAttribute(String[] choices) {
		this(choices, null);
	}

	public EnumAttribute(String[] choices, String defaultChoiceValue) {
		assert choices != null;
		this.choices = choices;
		
		if (defaultChoiceValue != null) {
			defaultChoice = getChoiceIndex(defaultChoiceValue);
		} else {
			defaultChoice = -1;
		}
	}
	
	@Override
	protected String internalGetValue() {
		return choices[isSet() ? selectedChoice : defaultChoice];
	}

	@Override
	protected boolean internalHasDefault() {
		return defaultChoice >= 0;
	}

	@Override
	protected void internalSetValue(String stringValue) {
		selectedChoice = getChoiceIndex(stringValue);
	}

	private int getChoiceIndex(String stringValue) {
		for (int cnt = choices.length, n = 0; n < cnt; n++) {
			if (choices[n].equals(stringValue)) {
				return n;
			}
		}
		throw new IllegalArgumentException(
			"Choice '" + stringValue + "' not supported. " + 
			"Expected one of: " + StringServices.toString(choices));
	}

}

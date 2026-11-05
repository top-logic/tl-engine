/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

import jakarta.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.tag.AbstractTag;

/**
 * Value container for a {@link Tag} attribute.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class TagAttribute {
	private boolean hasValue;

	/** Whether a value was assigned to this attribute. */
	public boolean isSet() {
		return hasValue;
	}

	/** The value of this attribute as string. */
	public final String getAsString() {
		assert isSet() || internalHasDefault() : "attribute must be assigned";
		return internalGetValue();
	}
	
	/** Assigns the value of this attribute from its string representation. */
	public final void set(String stringValue) {
		assert ! isSet() : "attribute may be assigned only once";
		internalSetValue(stringValue);
		set();
	}

	/** Marks this attribute as assigned. */
	protected void set() {
		this.hasValue = true;
	}
	
    /**
     * You need to reset the tag attributes, during the {@link AbstractTag#teardown()}.
     */
	public void reset() {
		this.hasValue = false;
	}

	/** The value of this attribute as string. */
	protected abstract String internalGetValue();

	/** Stores the value of this attribute parsed from the given string. */
	protected abstract void internalSetValue(String stringValue);
	
	/** Whether this attribute has a default value. */
	protected abstract boolean internalHasDefault();
	
}

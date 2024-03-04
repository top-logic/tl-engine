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

	public boolean isSet() {
		return hasValue;
	}

	public final String getAsString() {
		assert isSet() || internalHasDefault() : "attribute must be assigned";
		return internalGetValue();
	}
	
	public final void set(String stringValue) {
		assert ! isSet() : "attribute may be assigned only once";
		internalSetValue(stringValue);
		set();
	}

	protected void set() {
		this.hasValue = true;
	}
	
    /**
     * You need to reset the tag attributes, during the {@link AbstractTag#teardown()}.
     */
	public void reset() {
		this.hasValue = false;
	}

	protected abstract String internalGetValue();

	protected abstract void internalSetValue(String stringValue);
	
	protected abstract boolean internalHasDefault();
	
}

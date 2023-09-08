/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;


/**
 * Boolean-valued {@link TagAttribute}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BooleanAttribute extends TagAttribute {
	boolean hasDefault;
	boolean defaultValue;
	boolean value;

	public BooleanAttribute() {
		this.hasDefault = false;
	}

	public BooleanAttribute(boolean defaultValue) {
		this.hasDefault = true;
		this.defaultValue = defaultValue;
	}

    @Override
	public String toString() {
        String theValues = this.isSet() ? "value: " + this.value + ", default: " + this.defaultValue : "";

        return (this.getClass().getName() + " [" + theValues + ']');
    }

	public boolean get() {
		assert isSet() | hasDefault;
		return isSet() ? value : defaultValue;
	}
	
	public void setAsBoolean(boolean value) {
		assert ! isSet() : "attribute may be assigned only once";
		this.value = value;
		set();
	}

	public void set(BooleanAttribute other) {
		if (other.isSet()) {
			setAsBoolean(other.get());
		}
	}

	@Override
	protected String internalGetValue() {
		return isSet() ? Boolean.toString(value) : Boolean.toString(defaultValue);
	}

	@Override
	protected void internalSetValue(String stringValue) {
		if (Boolean.TRUE.toString().equals(stringValue)) {
			this.value = true;
		} 
		else if (Boolean.FALSE.toString().equals(stringValue)) {
			this.value = false;
		}
		else 
			throw new IllegalArgumentException(
				"Boolean 'true' or 'false' expected, '" + stringValue + "' found.");
	}

	@Override
	protected boolean internalHasDefault() {
		return hasDefault;
	}

}

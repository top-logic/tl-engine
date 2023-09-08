/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

/**
 * Int-valued {@link TagAttribute}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class IntAttribute extends TagAttribute {
	private int value;
	
	private boolean hasDefaultValue;
	private int defaultValue;

	public IntAttribute() {
		this.hasDefaultValue = false;
	}

	public IntAttribute(int defaultValue){
		this.defaultValue = defaultValue;
		this.hasDefaultValue = true;
	}

    @Override
	public String toString() {
        String theValues = this.isSet() ? "value: " + this.value : "";

        return (this.getClass().getName() + " [" + theValues + ']');
    }

    public int get() {
		assert isSet();
		return value;
	}

	public void setAsInt(int value) {
		this.value = value;
		set();
	}

	public void set(IntAttribute other) {
		if (other.isSet()) {
			setAsInt(other.get());
		}
	}

	@Override
	protected String internalGetValue() {
		return Integer.toString(isSet() ? value : defaultValue);
	}

	@Override
	protected void internalSetValue(String stringValue) {
		this.value = Integer.parseInt(stringValue);
	}

	@Override
	protected boolean internalHasDefault() {
		return hasDefaultValue;
	}

}

/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag.util;

/**
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class StringAttribute extends TagAttribute {

	private String defaultValue;
	private String value;

	public StringAttribute() {
	}
	
	public StringAttribute(String value) {
		assert value != null : "default value not null";
		this.defaultValue = value;
	}

    @Override
	public String toString() {
        String theValues = this.isSet() ? "value: '" + this.value + "', default: '" + this.defaultValue + '\'': "";

        return (this.getClass().getName() + " [" + theValues + ']');
    }

	public void set(StringAttribute other) {
		if (other.isSet()) {
			this.set(other.getAsString());
		}
	}

	/** Convenience shortcut for {@link #getAsString()}}. */
	public String get() {
		return getAsString();
	}

	@Override
	protected String internalGetValue() {
		if (value != null) 
			return value;
		else
			return defaultValue;
	}

	@Override
	protected void internalSetValue(String stringValue) {
		this.value = stringValue;
	}

	@Override
	protected boolean internalHasDefault() {
		return defaultValue != null;
	}
	
	@Override
	public void reset() {
		super.reset();
		this.value = null;
	}
}

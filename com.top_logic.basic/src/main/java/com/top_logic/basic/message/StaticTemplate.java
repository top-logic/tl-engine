/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.message;

import java.lang.reflect.Field;

import com.top_logic.basic.message.AbstractMessages.PackageProtected;

/**
 * Base class for static message templates implementing {@link Template} classes.
 * 
 * @param <T>
 *        The concrete template type of this {@link Template}.
 * 
 * @see AbstractMessages
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
/*package protected*/ 
abstract class StaticTemplate<T extends Template> implements Template {

	private boolean initialized;
	private final Field constant;
	private T defaultTemplate;

	/*package protected*/ StaticTemplate(PackageProtected internal, Field constant) {
		assert internal != null : "Templates must be instantiated only by declaring a constant in a Messages class.";
		this.constant = constant;
	}

	@Override
	public String getNameSpace() {
		return constant.getDeclaringClass().getPackage().getName();
	}
	
	@Override
	public String getLocalName() {
		return constant.getName();
	}
	
	public final T getDefault() {
		if (initialized) {
			return defaultTemplate;
		} else {
			return null;
		}
	}
	
	public final void setDefaultTemplate(T value) {
		if (value == null) {
			throw new IllegalArgumentException("Default must be non-null.");
		}
		if (initialized) {
			throw new IllegalStateException("Template already initialized.");
		}
		if (this.defaultTemplate != null) {
			throw new IllegalStateException("Template default already initialized.");
		}
		this.defaultTemplate = value;
	}
	
	/*package protected*/ boolean finishInitialization() {
		if (this.initialized) {
			return false;
		}
		
		this.initialized = true;
		return true;
	}
	
}

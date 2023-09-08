/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.template;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.WithPropertiesDelegate;
import com.top_logic.layout.basic.WithPropertiesDelegateFactory;

/**
 * Base class for {@link WithProperties} implementations.
 * 
 * <p>
 * By using this class as base class, implementations can simply annotate getter and writer methods
 * with a {@link TemplateVariable} annotation to automatically export the method as property
 * accessor with appropriate documentation derived from the method's <i>JavaDoc</i>.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class WithPropertiesBase implements WithProperties {

	private final WithPropertiesDelegate _propertyDelegate;

	/**
	 * Creates a {@link WithPropertiesBase} and initializes the delegate implementation for property
	 * lookup.
	 */
	protected WithPropertiesBase() {
		_propertyDelegate = WithPropertiesDelegateFactory.lookup(getClass());
	}

	/**
	 * Creates a {@link WithPropertiesBase}.
	 *
	 * @param delegate
	 *        The type-specific delegate for reflectively lookuing up properties. See
	 *        {@link WithPropertiesDelegateFactory#lookup(Class)}.
	 */
	protected WithPropertiesBase(WithPropertiesDelegate delegate) {
		_propertyDelegate = delegate;
	}

	@Override
	public Object getPropertyValue(String propertyName) throws NoSuchPropertyException {
		return _propertyDelegate.getPropertyValue(this, propertyName);
	}

	@Override
	public Optional<Collection<String>> getAvailableProperties() {
		return Optional.of(_propertyDelegate.getAvailableProperties(this));
	}

	@Override
	public void renderProperty(DisplayContext context, TagWriter out, String propertyName) throws IOException {
		_propertyDelegate.renderProperty(context, out, this, propertyName);
	}

}

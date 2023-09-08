/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Base class for {@link ResourceProvider} implementations that dispatch to another
 * {@link ResourceProvider} for non-<code>null</code> objects.
 * 
 * @see #getProviderImpl(Object)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractDispatchingResourceProvider implements ResourceProvider {

	@Override
	public String getLabel(Object anObject) {
		return (anObject == null) ? null : getProviderImpl(anObject).getLabel(anObject);
	}

	@Override
	public String getTooltip(Object anObject) {
		return (anObject == null) ? null : getProviderImpl(anObject).getTooltip(anObject);
	}

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return (anObject == null) ? null : getProviderImpl(anObject).getImage(anObject, aFlavor);
	}

	@Override
	public String getType(Object anObject) {
		return (anObject == null) ? null : getProviderImpl(anObject).getType(anObject);
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		return (anObject == null) ? null : getProviderImpl(anObject).getLink(context, anObject);
	}

	@Override
	public String getCssClass(Object anObject) {
		return (anObject == null) ? null : getProviderImpl(anObject).getCssClass(anObject);
	}

	/**
	 * Retrieve the {@link ResourceProvider} suitable for the given object.
	 * 
	 * @param object
	 *        The object, to look up the {@link ResourceProvider} for, never <code>null</code>.
	 * @return The {@link ResourceProvider} to use for the given object.
	 */
	protected abstract ResourceProvider getProviderImpl(Object object);

}


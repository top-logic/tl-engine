/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Base class for {@link ResourceProvider}s that delegate to another provider with a mapped input
 * object.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMappingResourceProviderBase implements ResourceProvider {

	private final ResourceProvider _impl;

	/**
	 * Creates a {@link AbstractMappingResourceProviderBase}.
	 *
	 * @param impl
	 *        The {@link ResourceProvider} to delegate {@link #mapValue(Object) mapped} objects to.
	 */
	public AbstractMappingResourceProviderBase(ResourceProvider impl) {
		_impl = impl;
	}

	/**
	 * Gets the inner resource provider.
	 */
	public final ResourceProvider getImpl() {
		return _impl;
	}

	/**
	 * Maps the given object to another object. This method is used to transform the object for
	 * which resources are desired.
	 * 
	 * @param anObject
	 *        the object the resources are asked for.
	 * @return the object which will transmitted to the {@link #_impl implementation}.
	 */
	protected abstract Object mapValue(Object anObject);

	@Override
	public ThemeImage getImage(Object anObject, Flavor aFlavor) {
		return _impl.getImage(mapValue(anObject), aFlavor);
	}

	@Override
	public String getLink(DisplayContext context, Object anObject) {
		return _impl.getLink(context, mapValue(anObject));
	}

	@Override
	public String getTooltip(Object anObject) {
		return _impl.getTooltip(mapValue(anObject));
	}

	@Override
	public String getType(Object anObject) {
		return _impl.getType(mapValue(anObject));
	}

	@Override
	public String getLabel(Object object) {
		return _impl.getLabel(mapValue(object));
	}

	@Override
	public String getCssClass(Object object) {
		return _impl.getCssClass(mapValue(object));
	}

}

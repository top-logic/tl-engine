/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Proxy for {@link ListModelBuilder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListModelBuilderProxy implements ListModelBuilder {
	
	private final ListModelBuilder _impl;
	
	/**
	 * Creates a {@link ListModelBuilderProxy}.
	 *
	 * @param impl The delegate.
	 */
	public ListModelBuilderProxy(ListModelBuilder impl) {
		_impl = impl;
	}

	/**
	 * The wrapped {@link ListModelBuilder}
	 */
	public final ListModelBuilder unwrap() {
		return _impl;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return _impl.getModel(businessModel, aComponent);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return _impl.supportsModel(aModel, aComponent);
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return _impl.supportsListElement(contextComponent, listElement);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return _impl.retrieveModelFromListElement(contextComponent, listElement);
	}

}

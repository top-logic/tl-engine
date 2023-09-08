/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Read-only {@link ComponentChannel} that computes its model value using a {@link ModelProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DerivedComponentChannel extends AbstractComponentChannel {

	private ModelProvider _provider;

	/**
	 * Creates a {@link DerivedComponentChannel}.
	 *
	 * @param component
	 *        See {@link #getComponent()}.
	 * @param provider
	 *        The algorithm computing the model value.
	 */
	public DerivedComponentChannel(LayoutComponent component, ModelProvider provider) {
		super(component, "provider(" + provider.getClass().getName() + ")");
		_provider = provider;
	}

	@Override
	public Object get() {
		return _provider.getBusinessModel(getComponent());
	}

	@Override
	protected void storeValue(Object newValue, Object oldValue) {
		// Ignore, cannot be updated.
	}

	@Override
	public Collection<ComponentChannel> sources() {
		return Collections.emptyList();
	}

	@Override
	public void link(ComponentChannel source) {
		throw new UnsupportedOperationException("A derived channel cannot be linked to some source.");
	}

	@Override
	public void unlink(ComponentChannel source) {
		throw new UnsupportedOperationException("A derived channel cannot be unlinked from a source.");
	}

}

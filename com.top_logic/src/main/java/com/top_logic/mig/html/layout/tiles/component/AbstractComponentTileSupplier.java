/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.Objects;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Base implementation of {@link ComponentTileSupplier}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractComponentTileSupplier implements ComponentTileSupplier {

	private final LayoutComponent _tileRoot;

	/**
	 * Creates a new {@link AbstractComponentTileSupplier}.
	 */
	public AbstractComponentTileSupplier(LayoutComponent tileRoot) {
		_tileRoot = Objects.requireNonNull(tileRoot);
	}

	@Override
	public LayoutComponent getRootComponent() {
		return _tileRoot;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _tileRoot.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractComponentTileSupplier other = (AbstractComponentTileSupplier) obj;
		if (!_tileRoot.equals(other._tileRoot))
			return false;
		return true;
	}

}


/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Abstract implementation of {@link ComponentTile}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractComponentTile implements ComponentTile {

	private final Object _businessObject;

	private final LayoutComponent _tileComponent;
	
	/**
	 * Creates a new {@link AbstractComponentTile}.
	 */
	public AbstractComponentTile(LayoutComponent tileComponent, Object businessObject) {
		_tileComponent = tileComponent;
		_businessObject = businessObject;
	}

	@Override
	public LayoutComponent getTileComponent() {
		return _tileComponent;
	}

	@Override
	public Object getBusinessObject() {
		return _businessObject;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime;
		result = prime * result + ((_tileComponent == null) ? 0 : _tileComponent.hashCode());
		result = prime * result + ((_businessObject == null) ? 0 : _businessObject.hashCode());
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
		AbstractComponentTile other = (AbstractComponentTile) obj;
		if (_tileComponent == null) {
			if (other._tileComponent != null)
				return false;
		} else if (!_tileComponent.equals(other._tileComponent))
			return false;
		if (_businessObject == null) {
			if (other._businessObject != null)
				return false;
		} else if (!_businessObject.equals(other._businessObject))
			return false;
		return true;
	}

}


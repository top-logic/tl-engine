/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;

import com.top_logic.layout.basic.ControlCommand;

/**
 * A {@link ContainerControl}, which arranges its children either horizontally or vertically.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public abstract class FlowLayoutControl<I extends FlowLayoutControl<?>> extends ContainerControl<I>
		implements OrientationAware {

	private Orientation _orientation;

	/**
	 * @param orientation
	 *        How to layout children.
	 * @param commandsByName
	 *        {@link ControlCommand}s associated with this flow layout
	 */
	protected FlowLayoutControl(Orientation orientation, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		_orientation = orientation;
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * @see #getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		this._orientation = orientation;
	}
}

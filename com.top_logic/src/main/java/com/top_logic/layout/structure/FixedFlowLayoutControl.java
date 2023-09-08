/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Collections;

import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.layoutRenderer.FixedFlowLayoutRenderer;

/**
 * A {@link FlowLayoutControl}, which cannot change the size of its children.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FixedFlowLayoutControl extends FlowLayoutControl<FixedFlowLayoutControl> {

	/**
	 * Creates a new {@link FixedFlowLayoutControl}.
	 * 
	 * @param orientation
	 *        How to layout children.
	 */
	public FixedFlowLayoutControl(Orientation orientation) {
		super(orientation, Collections.<String, ControlCommand> emptyMap());
	}

	@Override
	protected ControlRenderer<? super FixedFlowLayoutControl> createDefaultRenderer() {
		return FixedFlowLayoutRenderer.INSTANCE;
	}

	@Override
	public FixedFlowLayoutControl self() {
		return this;
	}
}

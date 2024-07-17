/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;

import com.top_logic.layout.Control;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.ControlCommand;

/**
 * {@link Control} that consists of multiple {@link View}s rendered as a block
 * of HTML consisting of a single top-level HTML element.
 * 
 * <p>
 * A {@link BlockControl} is responsible for the visibility and rendering of all of
 * its {@link Control} children.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlockControl extends AbstractCompositeControl<BlockControl> {

	/**
	 * The visibility of this composite control. 
	 */
	private boolean visible = true;
	
	public BlockControl() {
		super(Collections.<String, ControlCommand>emptyMap());
	}
	
	@Override
	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean newVisible) {
		boolean changed = newVisible != this.visible;
		if (changed) {
			this.visible = newVisible;
			requestRepaint();
		}
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public BlockControl self() {
		return this;
	}

	@Override
	protected String getTypeCssClass() {
		return "tl-block-control";
	}

}

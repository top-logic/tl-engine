/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.control;

import java.io.IOException;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.form.boxes.model.Box;
import com.top_logic.layout.form.boxes.model.BoxListener;
import com.top_logic.layout.form.boxes.model.BoxRenderer;

/**
 * {@link Control} rendering a {@link Box} layout.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BoxControl extends AbstractControlBase implements BoxListener {

	private Box _model;

	/**
	 * Creates a {@link BoxControl}.
	 */
	public BoxControl() {
		super();
	}

	/**
	 * The {@link Box} model to render.
	 */
	@Override
	public Box getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public void setModel(Box model) {
		// Call before setting new model to remove as listener from the old model.
		requestRepaint();

		_model = model;
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();

		_model.addListener(Box.LAYOUT_CHANGE, this);
	}

	@Override
	protected void detachInvalidated() {
		_model.removeListener(Box.LAYOUT_CHANGE, this);

		super.detachInvalidated();
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	public Bubble layoutChanged(Box changed) {
		requestRepaint();
		return Bubble.BUBBLE;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		BoxRenderer.INSTANCE.writeBox(context, out, _model, getID());
	}

}

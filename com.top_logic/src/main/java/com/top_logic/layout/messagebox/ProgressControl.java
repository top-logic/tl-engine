/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.PropertyUpdate;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractVisibleControl;
import com.top_logic.layout.basic.ConstantDisplayValue;

/**
 * Displays a progress bar.
 */
public class ProgressControl extends AbstractVisibleControl {

	private int _max = 100;

	private int _value;

	@Override
	protected String getTypeCssClass() {
		return "cProgress";
	}

	/**
	 * The current progress value in the range from 0 to {@link #getMax()}.
	 */
	public int getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(int value) {
		_value = value;

		if (isAttached() && !isRepaintRequested()) {
			addUpdate(new PropertyUpdate(getID(), VALUE_ATTR, new ConstantDisplayValue(Integer.toString(_value))));
		}
	}

	/**
	 * The {@link #getValue()} that means completion (normally 100).
	 */
	public int getMax() {
		return _max;
	}

	/**
	 * @see #getMax()
	 */
	public void setMax(int max) {
		_max = max;

		if (isAttached() && !isRepaintRequested()) {
			addUpdate(new PropertyUpdate(getID(), MAX_ATTR, new ConstantDisplayValue(Integer.toString(_max))));
		}
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);

		out.writeAttribute(MAX_ATTR, _max);
		out.writeAttribute(VALUE_ATTR, _value);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(PROGRESS);
		writeControlAttributes(context, out);
		out.endBeginTag();
		
		out.writeInt(100 * _value / _max);
		out.append("%");

		out.endTag(PROGRESS);
	}

	@Override
	public Object getModel() {
		return null;
	}

}

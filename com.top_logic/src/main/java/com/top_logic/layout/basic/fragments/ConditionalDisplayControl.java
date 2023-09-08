/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.fragments;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.fragments.Fragments.Attribute;
import com.top_logic.layout.form.model.VisibilityModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link Control} whose visibility is controled by a {@link VisibilityModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ConditionalDisplayControl extends AbstractControlBase implements VisibilityListener {

	private final VisibilityModel _visibility;

	private final Tag _display;

	/**
	 * Creates a {@link ConditionalDisplayControl}.
	 * 
	 * @param visibility
	 *        The {@link VisibilityModel} to use.
	 * @param display
	 *        The contents to display.
	 * 
	 * @see Fragments#conditional(VisibilityModel, Tag)
	 */
	public ConditionalDisplayControl(VisibilityModel visibility, Tag display) {
		_visibility = visibility;
		_display = display;
	}

	@Override
	public VisibilityModel getModel() {
		return _visibility;
	}

	@Override
	public boolean isVisible() {
		return _visibility.isVisible();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_visibility.addListener(VisibilityModel.VISIBLE_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		_visibility.removeListener(VisibilityModel.VISIBLE_PROPERTY, this);

		super.internalDetach();
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		requestRepaint();
		return Bubble.BUBBLE;
	}

	@Override
	protected boolean hasUpdates() {
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// Ignore.
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);
		
		if (_visibility.isVisible()) {
			for (Attribute attribute : _display.getAttributes()) {
				String attributeName = attribute.getName();
				if (attributeName.equals(HTMLConstants.ID_ATTR)) {
					continue;
				}
				if (attributeName.equals(HTMLConstants.CLASS_ATTR)) {
					continue;
				}

				out.writeAttribute(attributeName, attribute.getValue());
			}
		} else {
			out.writeAttribute(HTMLConstants.STYLE_ATTR, "display:none;");
		}
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);

		if (_visibility.isVisible()) {
			for (Attribute attribute : _display.getAttributes()) {
				String attributeName = attribute.getName();
				if (attributeName.equals(HTMLConstants.CLASS_ATTR)) {
					out.append(attribute.getValue());
					break;
				}
			}
		}
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		String tagName = _display.getTagName();

		out.beginBeginTag(tagName);
		writeControlAttributes(context, out);
		if (_display.isEmptyTag()) {
			out.endEmptyTag();
		} else {
			out.endBeginTag();

			if (_visibility.isVisible()) {
				for (HTMLFragment part : _display.getContent()) {
					part.write(context, out);
				}
			}

			out.endTag(tagName);
		}
	}

}

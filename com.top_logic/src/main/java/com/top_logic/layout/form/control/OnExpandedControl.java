/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link AbstractConstantControl} rendering a given content based on the
 * {@link Collapsible#isCollapsed() collapsed state} of the given model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class OnExpandedControl extends AbstractConstantControl implements CollapsedListener {

	private final Collapsible _model;

	private final HTMLFragment _content;

	private String _controlTag = DIV;

	private String _additionalClasses;

	private boolean _inverted = false;

	/**
	 * Creates a new {@link OnExpandedControl}.
	 * 
	 * @param content
	 *        The content to write when the given model is expanded.
	 */
	public OnExpandedControl(Collapsible model, HTMLFragment content) {
		_model = model;
		_content = content;
	}

	@Override
	public Collapsible getModel() {
		return _model;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(controlTag());
		writeControlAttributes(context, out);
		out.endBeginTag();
		if (isInverted() ^ !getModel().isCollapsed()) {
			_content.write(context, out);
		}
		out.endTag(controlTag());
	}

	@Override
	protected String getTypeCssClass() {
		return "cOnExpandedControl";
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, additionalCSS());
	}

	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		if (getModel().equals(collapsible)) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getModel().addListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void internalDetach() {
		getModel().removeListener(Collapsible.COLLAPSED_PROPERTY, this);
		super.internalDetach();
	}

	@Override
	public OnExpandedControl self() {
		return this;
	}

	/**
	 * HTML tag used by this control.
	 */
	public String controlTag() {
		return _controlTag;
	}

	/**
	 * Setter for {@link #controlTag()}.
	 *
	 * @param controlTag
	 *        New value for {@link #controlTag()}.
	 * @return This control.
	 */
	public OnExpandedControl setControlTag(String controlTag) {
		_controlTag = controlTag;
		return this;
	}

	/**
	 * If <code>true</code> the content is written when {@link #getModel() model} is collapsed and
	 * it is not written, when it is expanded.
	 */
	public boolean isInverted() {
		return _inverted;
	}

	/**
	 * Setter for {@link #isInverted()}.
	 * 
	 * @param inverted
	 *        New value for {@link #isInverted()}.
	 * 
	 * @return This control.
	 */
	public OnExpandedControl setInverted(boolean inverted) {
		_inverted = inverted;
		return this;
	}

	/**
	 * Additional CSS classes to set.
	 */
	public String additionalCSS() {
		return _additionalClasses;
	}

	/**
	 * Setter for {@link #additionalCSS()}
	 * 
	 * @param additionalClasses
	 *        New value for {@link #additionalCSS()}.
	 * 
	 * @return This control.
	 */
	public OnExpandedControl setAdditionalCSS(String additionalClasses) {
		_additionalClasses = StringServices.nonEmpty(additionalClasses);
		return this;
	}

}


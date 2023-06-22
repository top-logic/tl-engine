/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.decorator.DecorateInfo;
import com.top_logic.layout.form.decorator.DecorateService;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * Base class for {@link Control} implementations for {@link FormField}s that
 * automatically responds on {@link FormMember#VISIBLE_PROPERTY} and
 * {@link FormMember#IMMUTABLE_PROPERTY} property changes by repainting the
 * view.
 * 
 * @see AbstractFormFieldControlBase for a base class that does not impose
 *      constraints on the rendering in editable and immutable mode.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormFieldControl extends AbstractFormFieldControlBase {

	private boolean hasTabIndex;
	private int tabIndex;

	protected AbstractFormFieldControl(FormField model, Map commandsByName) {
		super(model, commandsByName);
	}
	
	protected AbstractFormFieldControl(FormField model) {
		super(model);
	}

    /**
     * This function is final to avoid problems when (Ajax-)updating the control.
     * 
     * Writing anything <em>outside</em> the control will accumulate with
     * every update.
     */
    @Override
	protected final void doInternalWrite(DisplayContext context, TagWriter out) throws IOException {
		if (getFieldModel().isVisible()) {
			writeVisible(context, out);
		} else {
			writeInvisible(context, out);
		}
	}

    protected void writeVisible(DisplayContext context, TagWriter out) throws IOException {
		FormField theModel = getFieldModel();
		if (theModel.isBlocked()) {
			writeBlocked(context, out);
		} else if (theModel.isImmutable()) {
			DecorateInfo decorateInfo = DecorateService.start(context, out, theModel, MetaLabelProvider.INSTANCE);

			writeImmutable(context, out);

			DecorateService.end(context, out, theModel, decorateInfo);
		} else {
			writeEditable(context, out);
		}
	}

	/**
	 * Renders this control's {@link #getFieldModel() field} in edit mode.
	 */
	protected abstract void writeEditable(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Renders this control's {@link #getFieldModel() field} in immutable mode.
	 */
	protected abstract void writeImmutable(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Renders a replacement value for this control's
	 * {@link #getFieldModel() field}, if it's blocked.
	 */
	protected void writeBlocked(DisplayContext context, TagWriter out) throws IOException {
		writeBlocked(context, out, this, I18NConstants.BLOCKED_VALUE_TEXT);
	}
	
	/**
	 * Writes a replacement for a blocked {@link FormField}s value.
	 * 
	 * @param context 
	 *     The current context.
	 * @param out
	 *     The writer to write to.
	 * @param control 
	 *     The control that is rendered.
	 * @param substitutionKey
	 *     The I18N key that is used to lookup the substitution text.
	 */
	public static void writeBlocked(DisplayContext context, TagWriter out, AbstractFormMemberControl control,
			ResKey substitutionKey) throws IOException {
		out.beginBeginTag(SPAN);
		control.accessibleWriteControlAttributes(context, out);
		out.endBeginTag();
		final String string = context.getResources().getString(substitutionKey);
		out.writeText(string);
		out.endTag(SPAN);
	}
	
	/**
	 * Render this control's {@link #getFieldModel() field}, if it's invisible.
	 */
	protected void writeInvisible(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		out.writeAttribute(ID_ATTR, getID());
		out.endBeginTag();
		out.endTag(SPAN);
	}

	/**
	 * Optional tab index value.
	 */
	@TemplateVariable("tabindex")
	public final Integer tabIndex() {
		if (hasTabIndex()) {
			return Integer.valueOf(getTabIndex());
		} else {
			return null;
		}
	}

	/**
	 * @see #getTabIndex()
	 */
	public boolean hasTabIndex() {
		return hasTabIndex;
	}
	
	/**
	 * The tab index attribute of this control's input element.
	 * 
	 * <p>
	 * Note: This method may only be called, if this control
	 * {@link #hasTabIndex() has a tab index set}.
	 * </p>
	 */
	public int getTabIndex() {
		assert hasTabIndex;
		return tabIndex;
	}

	/**
	 * @see #getTabIndex()
	 */
	public void setTabIndex(int tabIndex) {
		this.hasTabIndex = true;
		this.tabIndex = tabIndex;
	}

	/**
	 * @see #getTabIndex()
	 */
	public void removeTabIndex() {
		this.hasTabIndex = false;
	}

}

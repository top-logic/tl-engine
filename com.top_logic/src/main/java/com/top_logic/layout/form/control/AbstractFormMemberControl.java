/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.CSSClassListener;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMember.IDUsage;
import com.top_logic.layout.form.RemovedListener;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * Base class for {@link com.top_logic.layout.Control} implementations that display a single
 * {@link FormMember} in the GUI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFormMemberControl extends AbstractControl implements FormMemberControl, CSSClassListener,
		RemovedListener, VisibilityListener, ContextMenuOwner {
	
    /** The part of the FormContext this Control is based on */
	private final FormMember model;
	
	/** @see #getCustomStyle() */
	private String style;

	private String _inputId;

	/**
	 * Basic set of commands for all form member controls.
	 * 
	 * <p>
	 * This set of commands must be added to all command sets of all sub-classes.
	 * </p>
	 */
	protected static final Map<String, ControlCommand> COMMANDS = createCommandMap(ContextMenuOpener.INSTANCE);

	/**
	 * Creates a {@link AbstractFormMemberControl}.
	 *
	 * @param model
	 *        See {@link #getModel()}.
	 * @param commandsByName
	 *        See {@link AbstractControl#AbstractControl(Map)}.
	 */
	public AbstractFormMemberControl(FormMember model, Map<String, ControlCommand> commandsByName) {
		super(commandsByName);
		assert model != null : "Model must not be null.";
		this.model = model;
	}

	@Override
	public Menu createContextMenu(String contextInfo) {
		return getContextMenuProvider().getContextMenu(getModel());
	}

	/**
	 * Whether a context menu can be opened on this control.
	 */
	@TemplateVariable("hasContextMenu")
	public boolean hasContextMenu() {
		return getContextMenuProvider().hasContextMenu(getModel());
	}

	/**
	 * The {@link ContextMenuProvider} of {@link #getModel()}.
	 */
	private ContextMenuProvider getContextMenuProvider() {
		return getModel().getContextMenu();
	}

	/**
	 * Sets the custom part of this control's style.
	 * 
	 * @see #getCustomStyle()
	 */
	public void setStyle(String style) {
		this.style = style;
		
		requestRepaint();
	}

	/**
	 * The application-defined part of the style of the top-level client-side DOM element of this
	 * control.
	 * 
	 * <p>
	 * Note: This value must not written directly to the output. Instead the method
	 * {@link #writeStyle(TagWriter)} must be used.
	 * </p>
	 * 
	 * @see #writeStyle(TagWriter)
	 * @see #setStyle(String)
	 */
	public final String getCustomStyle() {
		return style;
	}

	/**
	 * Write the control's <code>style</code> attribute.
	 * 
	 * @see #writeStyleContent(TagWriter)
	 * @deprecated Use {@link #writeStyleContent(TagWriter)} with template-based rendering.
	 */
	@Deprecated
	public final void writeStyle(TagWriter out) throws IOException {
		out.beginAttribute(STYLE_ATTR);
		writeStyleContent(out);
		out.endAttribute();
	}

	/**
	 * Hook for adding additional computed styles when writing the application defined
	 * {@link #getCustomStyle()}.
	 */
	@TemplateVariable("style")
	public void writeStyleContent(TagWriter out) throws IOException {
		out.append(style);
	}

	@Override
	public final FormMember getModel() {
		return model;
	}

	@Override
	public boolean isVisible() {
		return model.isVisible();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		_inputId = buildInputId();
	}

	/**
	 * Creates the ID for the input element.
	 */
	protected String buildInputId() {
		return getModel().uiIdentifier(getFrameScope(), IDUsage.INPUT);
	}

	/**
	 * Implemented to attach this control as listener for property events from
	 * its {@link FormMember}.
	 * 
	 * @see AbstractControl#attachRevalidated()
	 */
	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		
		registerListener(model);
	}

	protected void registerListener(FormMember member) {
		member.addListener(FormMember.VISIBLE_PROPERTY, this);
		member.addListener(FormMember.CLASS_PROPERTY, this);
		member.addListener(FormMember.REMOVED_FROM_PARENT, this);
	}
	
	/**
	 * @see #attachRevalidated()
	 * @see AbstractControl#detachInvalidated()
	 */
	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
		
		deregisterListener(model);
	}

	protected void deregisterListener(FormMember member) {
		member.removeListener(FormMember.REMOVED_FROM_PARENT, this);
		member.removeListener(FormMember.CLASS_PROPERTY, this);
		member.removeListener(FormMember.VISIBLE_PROPERTY, this);
	}
	

	protected final boolean skipEvent(Object sender) {
		return sender != getModel();
	}

	@Override
	public Bubble handleCSSClassChange(Object sender, String oldValue, String newValue) {
		if (!skipEvent(sender)) {
			updateCss();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleRemovedFromParent(FormMember sender, FormContainer formerParent) {
		if (!skipEvent(sender)) {
			detach();
		}
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		return repaintOnEvent(sender);
	}

	protected Bubble repaintOnEvent(Object sender) {
		if (!skipEvent(sender)) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	/**
	 * Allow calling {@link #writeControlAttributes(DisplayContext, TagWriter)} from the outside in
	 * sub-classes.
	 */
	protected final void accessibleWriteControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		writeControlAttributes(context, out);
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);

		if (hasContextMenu()) {
			out.writeAttribute(TL_CONTEXT_MENU_ATTR, getID());
		}
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		appendMemberControlClasses(out, getModel());
	}
	
	public static void appendMemberControlClasses(Appendable out, final FormMember model)
			throws IOException {
		String customCss = model.getCssClasses();
		HTMLUtil.appendCSSClass(out, customCss);
		if (! model.isVisible()) {
			HTMLUtil.appendCSSClass(out, FormConstants.INVISIBLE_CSS_CLASS);
		}
	}
	
	/**
	 * The ID of this control's input element.
	 */
	@TemplateVariable("inputId")
	public final String getInputId() {
		return _inputId;
	}

	/**
	 * Writes a JavaScript literal containing the {@link #getInputId()}.
	 */
	public final void writeInputIdJsString(TagWriter out) throws IOException {
		out.beginJsString();
		appendInputId(out);
		out.endJsString();
	}

	/**
	 * Utility to write the {@link #getInputId()} as {@link HTMLConstants#ID_ATTR} attribute.
	 * 
	 * @deprecated Use {@link #getInputId()} with template-based rendering.
	 */
	@Deprecated
	protected final void writeInputIdAttr(TagWriter out) throws IOException {
		out.beginAttribute(ID_ATTR);
		appendInputId(out);
		out.endAttribute();
	}

	/**
	 * Writes {@link #getInputId()} (allocation-free) to the given writer.
	 * 
	 * @deprecated Use {@link #getInputId()} with template-based rendering.
	 */
	@Deprecated
	public final void appendInputId(Appendable writer) throws IOException {
		writer.append(getInputId());
	}
	
	/**
	 * Utility for writing the {@link #getModel()}'s GUI identifier as
	 * {@link HTMLConstants#NAME_ATTR} attribute.
	 *
	 * @deprecated Use {@link #getInputId()} with template-based rendering.
	 */
	@Deprecated
	protected final void writeQualifiedNameAttribute(TagWriter out) throws IOException {
		out.beginAttribute(NAME_ATTR);
		appendInputId(out);
		out.endAttribute();
	}

}

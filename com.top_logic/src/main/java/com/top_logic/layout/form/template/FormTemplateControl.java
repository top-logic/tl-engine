/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.w3c.dom.Document;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.View;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.mig.html.layout.VisibilityListener;

/**
 * {@link Control} displaying a {@link FormContainer} using a template.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 * 
 * @deprecated Use {@link TemplateControl} with new-style templates.
 */
@Deprecated
public class FormTemplateControl extends AbstractConstantControlBase implements VisibilityListener,
		MemberChangedListener, CollapsedListener {

	/** The {@link FormMember} to display */
	private final FormMember model;

	private FormTemplate template;

	/**
	 * Creates a {@link FormTemplateControl}.
	 * 
	 * @param model
	 *        The model to display.
	 * @param template
	 *        The template to expand.
	 * @param commands
	 *        The {@link ControlCommand}s for this {@link AbstractControlBase}.
	 */
	public FormTemplateControl(FormMember model, FormTemplate template, Map<String, ControlCommand> commands) {
		super(commands);
		this.model = model;
		this.template = template;
	}

	/**
	 * Creates a {@link FormTemplateControl}.
	 * 
	 * @param model
	 *        The model to display.
	 * @param template
	 *        The template to expand.
	 */
	public FormTemplateControl(FormMember model, FormTemplate template) {
		this(model, template, Collections.<String, ControlCommand> emptyMap());
	}

	/**
	 * Returns the {@link FormMember} this control displays
	 */
	@Override
	public final FormMember getModel() {
		return model;
	}
	
	/**
	 * The {@link FormTemplate} that should be used for rendering.
	 */
	public FormTemplate getTemplate() {
		return template;
	}

	/**
	 * This method sets a new {@link Document} which shall be used to render the model of this
	 * {@link Control}.
	 */
	public void setTemplate(FormTemplate newTemplate) {
		this.template = newTemplate;
		
		requestRepaint();
	}
	
	/**
	 * This method sets a new {@link Document} which shall be used to render the model of this
	 * {@link Control}.
	 */
	public void setTemplate(Document doc) {
		FormTemplate oldTemplate = this.template;

		setTemplate(
			new FormTemplate(
				oldTemplate.getResources(),
				oldTemplate.getControlProvider(), 
				oldTemplate.hasAutomaticErrorDisplay(), 
				doc));
	}

	/**
	 * This method is a service method which parses the given template to a {@link Document} and
	 * sets it to this control.
	 * 
	 * @see DOMUtil#parse(String)
	 * @see #setTemplate(Document)
	 */
	public final void setTemplate(String template) {
		this.setTemplate(DOMUtil.parse(template));
	}

	/**
	 * The rendering is dispatched to its renderer.
	 * 
	 * @see AbstractControlBase#internalWrite(DisplayContext, TagWriter)
	 * @see FormTemplate#getRenderer()
	 */
	@Override
	protected final void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		templateRenderer().write(context, out, this);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		templateRenderer().appendControlCSSClasses(out, this);
	}

	private PatternRenderer templateRenderer() {
		return this.template.getRenderer();
	}

	/**
	 * This view is visible iff its {@link #getModel() model} is visible
	 * 
	 * @see View#isVisible()
	 */
	@Override
	public final boolean isVisible() {
		return getModel().isVisible();
	}

	@Override
	public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
		return repaintOnEvent(sender);
	}

	private Bubble repaintOnEvent(Object sender) {
		if (sender != getModel()) {
			// Ignore bubbling events.
			return Bubble.BUBBLE;
		}
		requestRepaint();
		return Bubble.BUBBLE;
	}

	@Override
	public Bubble memberAdded(FormContainer parent, FormMember member) {
		return repaintOnEvent(parent);
	}

	@Override
	public Bubble memberRemoved(FormContainer parent, FormMember member) {
		return repaintOnEvent(parent);
	}

	@Override
	public Bubble handleCollapsed(Collapsible collapsible, Boolean oldValue, Boolean newValue) {
		return repaintOnEvent(collapsible);
	}

	@Override
	protected void detachInvalidated() {
		super.detachInvalidated();
		model.removeListener(FormMember.VISIBLE_PROPERTY, this);
		model.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		model.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
		model.removeListener(Collapsible.COLLAPSED_PROPERTY, this);
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		model.addListener(Collapsible.COLLAPSED_PROPERTY, this);
		model.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, this);
		model.addListener(FormContainer.MEMBER_ADDED_PROPERTY, this);
		model.addListener(FormMember.VISIBLE_PROPERTY, this);
	}

}

/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.listener.GenericPropertyListener;
import com.top_logic.basic.listener.PropertyObservable;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.css.CssUtil;

/**
 * {@link Control} managing the view of a form field group.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FormGroupControl extends AbstractCompositeControl<FormGroupControl> implements GenericPropertyListener {

	private static final ControlCommand[] COMMANDS = { new ToggleCollapsed() };
	private static final Map<String, ControlCommand> COMMAND_MAP = createCommandMap(COMMANDS);

	private final FormGroup model;

	private boolean collapsible;

	private String _customCssClass;

	/**
	 * Creates a {@link FormGroupControl}.
	 * 
	 * @param model
	 *        See {@link #getFormGroup()}.
	 */
	public FormGroupControl(FormGroup model) {
		super(COMMAND_MAP);
		this.model = model;
	}

	@Override
	public FormGroup getModel() {
		return model;
	}

	@Override
	public boolean isVisible() {
		return model.isVisible();
	}

	/**
	 * The {@link FormGroup} that is used as model.
	 */
	public FormGroup getFormGroup() {
		return model;
	}

	/**
	 * All CSS classes to add to the control element.
	 */
	public String getCssClasses() {
		String modelClasses = getFormGroup().getCssClasses();
		return CssUtil.joinCssClasses(modelClasses, _customCssClass);
	}

	/**
	 * Adds additional CSS classes to the model-defined ones.
	 * 
	 * @see #getCssClasses()
	 * @see FormGroup#getCssClasses()
	 */
	public void setViewCssClass(String customCssClass) {
		_customCssClass = customCssClass;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		model.addListener(PropertyObservable.GLOBAL_LISTENER_TYPE, this);
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();
		model.removeListener(PropertyObservable.GLOBAL_LISTENER_TYPE, this);
	}

	@Override
	public Bubble handlePropertyChanged(EventType<?, ?, ?> type, Object sender, Object oldValue, Object newValue) {
		if (sender != model) {
			return Bubble.BUBBLE;
		}
		if (type != FormMember.IS_CHANGED_PROPERTY) {
			requestRepaint();
		}
		return Bubble.BUBBLE;
	}

	/**
	 * This method determines whether this {@link FormGroupControl} offers a possibility to toggle
	 * the collapse state of the {@link FormGroup}.
	 */
	public boolean isCollapsible() {
		return collapsible;
	}
	
	/**
	 * @see #isCollapsible()
	 */
	public void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
		requestRepaint();
	}
	/**
	 * This method returns the JS construct which can be used in JS event handlers to toggle the
	 * collapse state of the group.
	 * 
	 * @throws IllegalStateException
	 *             if this group control currently not offers the possibility to collapse its group.
	 */
	public void writeOnClickToggle(TagWriter out) throws IOException {
		if (!isCollapsible()) {
			throw new IllegalStateException("This FormGroupControl currently not offers a possibility to collapse its group.");
		}
		out.beginAttribute(ONCLICK_ATTR);
		out.append("return services.form.FormGroupControl.handleToggle(");
		writeIdJsString(out);
		out.append(");");
		out.endAttribute();
	}

	/**
	 * Command that is invoked, if the user clicks on the collapse/expand button.
	 */
	private static class ToggleCollapsed extends ControlCommand {

		private static final ResKey COLLAPSED_COMMAND_NOT_POSSIBLE = I18NConstants.ERROR_CANNOT_COLLAPSE_GROUP;

		public ToggleCollapsed() {
			super("toggleCollapsed");
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			FormGroupControl groupControl = (FormGroupControl) control;
			FormContainer container = groupControl.getFormGroup();
			if (!groupControl.isCollapsible()) {
				HandlerResult result = new HandlerResult();
				FormGroup group = groupControl.getFormGroup();
				result.addErrorMessage(COLLAPSED_COMMAND_NOT_POSSIBLE, group.getLabel());
				return result;
			}
			container.setCollapsed(!container.isCollapsed());
			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.TOGGLE_FORM_GROUP_COLLAPSE_STATE;
		}
	}

	@Override
	public FormGroupControl self() {
		return this;
	}
}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ConstantControl;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * A control of the form editor which delegates changes of the {@link FormDefinition} to other parts
 * of the form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public abstract class FormEditorAbstractControl extends ConstantControl<FormDefinition> {

	private TLStructuredType _type;

	private ResPrefix _resPrefix;

	private boolean _isInEditMode = false;

	private AttributeFormContext _formContext;

	private FormContext _parent;

	/**
	 * Create a new {@link FormEditorAbstractControl}.
	 */
	protected FormEditorAbstractControl(FormDefinition model, Map<String, ControlCommand> commandsByName,
			TLStructuredType type, FormContext parent, ResPrefix resPrefix, boolean isInEditMode) {
		super(model, commandsByName);
		_type = type;
		_parent = parent;
		_resPrefix = resPrefix;
		_isInEditMode = isInEditMode;
	}

	/**
	 * Returns the {@link TLStructuredType} of the form.
	 */
	protected TLStructuredType getType() {
		return _type;
	}

	/**
	 * Returns the {@link ResPrefix}.
	 */
	protected ResPrefix getResPrefix() {
		return _resPrefix;
	}

	/**
	 * Whether the form editor is in edit mode.
	 */
	protected boolean isInEditMode() {
		return _isInEditMode;
	}

	/**
	 * The form context.
	 */
	protected AttributeFormContext getFormContext() {
		return _formContext;
	}

	/**
	 * Set the outer form context.
	 */
	protected void setFormContext(AttributeFormContext formContext) {
		_formContext = formContext;
	}

	/**
	 * The outer form context.
	 */
	protected FormContext getParent() {
		return _parent;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		setFormContext(new AttributeFormContext(getResPrefix()));

		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeContent(context, out);
		out.endTag(DIV);

		applyJS(out);
	}

	abstract void writeContent(DisplayContext context, TagWriter out) throws IOException;

	/**
	 * Runs JavaScript after rendering while in edit mode.
	 */
	protected void applyJS(TagWriter out) throws IOException {
		if (isInEditMode()) {
			// only load js in edit mode to avoid unexpected behavior
			HTMLUtil.beginScriptAfterRendering(out);
			out.append("FormEditor.init('");
			out.append(getID());
			out.append("', '");
			out.append(Resources.getInstance().getString(I18NConstants.FORM_EDITOR__PUT_ELEMENT_BACK));
			out.append("')");
			HTMLUtil.endScriptAfterRendering(out);
		}
	}

	/**
	 * Moves an element in the {@link FormDefinition}.
	 * 
	 * @param commandContext
	 *        The {@link DisplayContext}.
	 * @param control
	 *        The control which should handle the movement.
	 * @param arguments
	 *        The parameters. See the implementations of this method.
	 */
	protected abstract void moveMember(DisplayContext commandContext, Control control, Map<String, Object> arguments);
	
	/**
	 * {@link ControlCommand} to move a member.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	protected static class MoveMember extends ControlCommand {

		private static final String COMMAND_NAME = "moveMember";

		/**
		 * Single instance of the {@link MoveMember}.
		 */
		public static final ControlCommand INSTANCE = new MoveMember();

		/**
		 * Creates a {@link MoveMember}.
		 */
		private MoveMember() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			FormEditorAbstractControl formEditorAbstractControl = (FormEditorAbstractControl) control;
			formEditorAbstractControl.moveMember(commandContext, control, arguments);

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_EDITOR__MOVE_ELEMENT;
		}
	}
}

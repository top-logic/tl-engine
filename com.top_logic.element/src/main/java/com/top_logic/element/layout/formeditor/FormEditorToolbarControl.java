/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormEditorMapping;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a toolbar for the form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorToolbarControl extends FormEditorAbstractControl {

	FormEditorToolboxControl _toolbox;

	private FormMember _templateField;

	FormEditorAttributesControl _attributes;

	private FormEditorMapping _formEditorMapping;

	private static final String CSS = "cFormEditorToolbar";

	private List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> _buttonElements;

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(MoveMember.INSTANCE, UpdateToolbox.INSTANCE);

	/**
	 * Create a new {@link FormEditorAttributesControl} by the given parameters and initializes a
	 * mapping between the representations on the GUI and the model.
	 */
	public FormEditorToolbarControl(FormDefinition model,
			TLStructuredType type, ResPrefix resPrefix, boolean isInEditMode, FormEditorMapping formEditorMapping,
			FormMember templateField,
			List<Class<? extends FormElement<? extends FormElementTemplateProvider>>> list) {
		super(model, COMMANDS, type, null, resPrefix, isInEditMode);
		_formEditorMapping = formEditorMapping;
		_templateField = templateField;
		_buttonElements = list;
	}
	
	@Override
	protected String getTypeCssClass() {
		return CSS;
	}

	@Override
	void writeContent(DisplayContext context, TagWriter out) throws IOException {
		writeTemplateList(context, out);
		writeToolbox(context, out);
		writeAttributes(context, out);
	}

	private void writeTemplateList(DisplayContext context, TagWriter out) throws IOException {
		if (isInEditMode()) {
			if (_templateField != null) {
				Control templateSelector = DefaultFormFieldControlProvider.INSTANCE.createControl(_templateField);
				templateSelector.write(context, out);
			}
		}
	}

	private void writeAttributes(DisplayContext context, TagWriter out) throws IOException {
		if (getAttributes() == null) {
			setAttributes(
				new FormEditorAttributesControl(getModel(), getType(), getResPrefix(), isInEditMode(),
					_formEditorMapping));
		}

		getAttributes().write(context, out);
	}

	/**
	 * Returns the {@link FormEditorAttributesControl} of this control.
	 */
	protected FormEditorAttributesControl getAttributes() {
		return _attributes;
	}

	private void setAttributes(FormEditorAttributesControl attributes) {
		_attributes = attributes;
	}

	private void writeToolbox(DisplayContext context, TagWriter out) throws IOException {
		if (isInEditMode()) {
			if (getToolbox() == null) {
				setToolbox(new FormEditorToolboxControl(getModel(), getType(), getResPrefix(), isInEditMode(),
					_formEditorMapping, _buttonElements));
			}

			getToolbox().write(context, out);
		}
	}

	/**
	 * Returns the {@link FormEditorToolboxControl} of this control.
	 */
	protected FormEditorToolboxControl getToolbox() {
		return _toolbox;
	}

	private void setToolbox(FormEditorToolboxControl toolbox) {
		_toolbox = toolbox;
	}

	/**
	 * An element is moved to the list of attributes so it has to be removed of the
	 * {@link TLFormDefinition}.
	 * 
	 * @param elementID
	 *        The ID of the moved element.
	 * @param type
	 *        Type of the attribute.
	 */
	protected void removeMember(String elementID, String type) {
		ContainerDefinition<?> container = getModel();
		FormElement<?> remove = _formEditorMapping.getMapping(elementID);

		boolean removed = ContentDefinitionUtil.removeDefininition(container, remove);
		if (!removed) {
			// element was not in editor -> is removed from attribute list and put back there so the
			// GUI has to be updated
			requestRepaint();
		}
		_formEditorMapping.removeMapping(elementID);
	}

	/**
	 * {@link ControlCommand} to update the toolbox.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	protected static class UpdateToolbox extends ControlCommand {

		private static final String COMMAND_NAME = "updateToolbox";

		/**
		 * Single instance of the {@link UpdateToolbox}.
		 */
		public static final ControlCommand INSTANCE = new UpdateToolbox();

		/**
		 * Creates a {@link UpdateToolbox}.
		 */
		private UpdateToolbox() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext,
				Control control, Map<String, Object> arguments) {
			((FormEditorToolbarControl) control).getToolbox().requestRepaint();

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_EDITOR__UPDATE_TOOLBOX;
		}
	}

	@Override
	protected void moveMember(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		String elementID = (String) arguments.get("elementID");
		String type = (String) arguments.get("type");

		((FormEditorToolbarControl) control).removeMember(elementID, type);
	}
}

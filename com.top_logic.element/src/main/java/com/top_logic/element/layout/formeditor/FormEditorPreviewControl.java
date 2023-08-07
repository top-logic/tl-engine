/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.structure.DialogClosedListener;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.definition.FormElement;
import com.top_logic.model.form.implementation.FormEditorMapping;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A control of the form editor which contains controls to preview the created form.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorPreviewControl extends FormEditorAbstractControl {

	private FormEditorEditorControl _preview;
	
	private static final String TARGET_CSS = "rf_dropTarget";
	
	private static final String CSS = "cFormEditorPreview";

	private static final Map<String, ControlCommand> COMMANDS =
		createCommandMap(MoveMember.INSTANCE, EditElement.INSTANCE);

	private FormEditorMapping _formEditorMapping;

	/**
	 * Create a new {@link FormEditorPreviewControl}.
	 */
	protected FormEditorPreviewControl(FormDefinition model, TLStructuredType type, ResPrefix resPrefix,
			boolean isInEditMode, FormEditorMapping formEditorMapping) {
		super(model, COMMANDS, type, null, resPrefix, isInEditMode);
		_formEditorMapping = formEditorMapping;
	}

	/**
	 * The current {@link FormEditorMapping}.
	 */
	public FormEditorMapping getFormEditorMapping() {
		return _formEditorMapping;
	}

	private FormEditorEditorControl getPreview() {
		return _preview;
	}

	private void setPreview(FormEditorEditorControl preview) {
		_preview = preview;
	}

	@Override
	void writeContent(DisplayContext context, TagWriter out) throws IOException {
		writePreview(context, out);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		out.append(TARGET_CSS);
	}

	@Override
	protected String getTypeCssClass() {
		return CSS;
	}
	
	void writePreview(DisplayContext context, TagWriter out) throws IOException {
		if (getPreview() == null) {
			setPreview(
				new FormEditorEditorControl(getModel(), getType(), getResPrefix(), isInEditMode(), this));
		}

		getPreview().write(context, out);
	}

	@Override
	protected void moveMember(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		FormEditorPreviewControl dynamicFormDisplayControl = (FormEditorPreviewControl) control;
		String elementID = (String) arguments.get("elementID");
		String siblingID = (String) arguments.get("siblingID");
		String parentID = (String) arguments.get("parentID");
		String attribute = (String) arguments.get("elementName");
		boolean inEditor = (boolean) arguments.get("inEditor");

		dynamicFormDisplayControl.moveMember(commandContext, elementID, siblingID, parentID, attribute, inEditor);

	}

	private Function<? super FormElement<?>, HandlerResult> createOkHandleEditGroup(
			FormElement<?> content) {
		return item -> {
			ConfigCopier.copyContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, item, content);
			repaintPreview();

			return HandlerResult.DEFAULT_RESULT;
		};
	}

	private Function<? super FormElement<?>, HandlerResult> createOkHandle(FormElement<?> sibling,
			ContainerDefinition<?> parent) {
		return item -> {
			addElement(parent, item, sibling);

			return HandlerResult.DEFAULT_RESULT;
		};
	}

	private DialogClosedListener createDialogClosedListener() {
		return new DialogClosedListener() {
			@Override
			public void handleDialogClosed(Object sender, Boolean oldValue, Boolean newValue) {
				repaintPreview();
			}
		};
	}

	void repaintPreview() {
		getPreview().requestRepaint();
	}

	private void addElement(ContainerDefinition<?> parent, FormElement<?> element, FormElement<?> sibling) {
		if (parent == null) {
			parent = getModel();
		}
		List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content = parent.getContent();
		int id = content.size();
		if (sibling != null) {
			id = content.indexOf(sibling);
		}
		content.add(id, element);
	}

	void editElement(DisplayContext context, String id) {
		FormElement<? extends FormElementTemplateProvider> def = _formEditorMapping.getMapping(id);
		if (def != null) {
			if (ContentDefinitionUtil.hasAttributes(def.descriptor())) {
				if (def instanceof FieldDefinition) {
					FieldDefinition fieldDefinition = (FieldDefinition) def;
					TLStructuredTypePart part = _preview.getType().getPart(fieldDefinition.getAttribute());
					fieldDefinition.setFullQualifiedName(TLModelUtil.qualifiedName(part));
					fieldDefinition.setTypeSpec(TLModelUtil.qualifiedName(part.getType()));
				}

				FormEditorElementFactory.openDialog(context, def, createOkHandleEditGroup(def),
					createDialogClosedListener(), _preview.getType());
			}
		}
	}

	/**
	 * Moves an element in the {@link TLFormDefinition} under the given parent and before the given
	 * sibling.
	 * <ul>
	 * <li>If there is no element and the element is dropped in the editor it will be created
	 * first.</li>
	 * <li>If there is no parent it will take the root.</li>
	 * <li>If there is no sibling it will append the element in the end of the parent-list.</li>
	 * </ul>
	 * 
	 * @param elementID
	 *        The GUI-ID to identify the {@link FormElement}.
	 * @param siblingID
	 *        The GUI-ID to identify the {@link FormElement}.
	 * @param parentID
	 *        The GUI-ID to identify the {@link ContainerDefinition}.
	 * @param name
	 *        <ul>
	 *        <li>For type element: The name of the {@link TLClassPart} of the element.</li>
	 *        <li>For type group: The String for the {@link ResKey}.</li>
	 *        </ul>
	 * @param inEditor
	 *        Whether the element is dropped in the editor.
	 */
	public HandlerResult moveMember(DisplayContext commandContext, String elementID, String siblingID, String parentID,
			String name, boolean inEditor) {
		ContainerDefinition<?> form = getModel();
		FormEditorMapping formEditorMapping = _formEditorMapping;
		FormElement<?> element = formEditorMapping.getMapping(elementID);
		FormElement<?> sibling = formEditorMapping.getMapping(siblingID);
		ContainerDefinition<?> parent = formEditorMapping.getMapping(parentID) != null
			? (ContainerDefinition<?>) formEditorMapping.getMapping(parentID)
			: form;

		Function<? super FormElement<?>, HandlerResult> okHandle = createOkHandle(sibling, parent);
		DialogClosedListener dialogCloseListener = createDialogClosedListener();

		if (ContentDefinitionUtil.contains(form, element)) {
			// move element inside form
			ContentDefinitionUtil.removeDefininition(form, element);
			addElement(parent, element, sibling);
		} else {
			// add new element to form
			if (inEditor) {
				// create default
				ConfigurationDescriptor descriptor =
					TypedConfiguration.getConfigurationDescriptor(element.getConfigurationInterface());
				boolean openDialog = TypedConfigUtil.createInstance(element).openDialog();
				if (ContentDefinitionUtil.hasMandatoryAttributes(descriptor) || openDialog) {
					// open a dialog
					return FormEditorElementFactory.openDialog(commandContext, element, okHandle, dialogCloseListener,
						_preview.getType());
				} else {
					// no visible, mandatory attributes -> no dialog
					addElement(parent, element, sibling);
					formEditorMapping.putMapping(elementID, element);
				}
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * {@link ControlCommand} to edit an element.
	 * 
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	protected static class EditElement extends ControlCommand {

		private static final String COMMAND_NAME = "editElement";

		/**
		 * Single instance of the {@link EditElement}.
		 */
		public static final ControlCommand INSTANCE = new EditElement();

		/**
		 * Creates a {@link EditElement}.
		 */
		private EditElement() {
			super(COMMAND_NAME);
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			FormEditorPreviewControl formEditorPreviewControl = (FormEditorPreviewControl) control;
			String elementID = (String) arguments.get("elementID");
			formEditorPreviewControl.editElement(commandContext, elementID);

			return HandlerResult.DEFAULT_RESULT;
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.FORM_EDITOR__EDIT_ELEMENT;
		}
	}
}

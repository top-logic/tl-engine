/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.FormEditorEditorControl.RepaintTrigger;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.definition.TLFormDefinition;
import com.top_logic.element.layout.formeditor.definition.TabbarDefinition;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.DefaultExpansionModel;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.reactive_tag.DefaultGroupSettings;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.control.I18NConstants;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.ContainerDefinition;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormEditorMapping;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelUtil;

/**
 * Lists all elements of the given {@link TLFormDefinition} which are not used in the editor and
 * which are not hidden.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorAttributesControl extends FormEditorDisplayControl {

	private FormEditorMapping _formEditorMapping;

	/**
	 * Create a new {@link FormEditorAttributesControl} by the given parameters and initializes a
	 * mapping between the representations on the GUI and the model.
	 */
	public FormEditorAttributesControl(FormDefinition model, TLStructuredType type, ResPrefix resPrefix,
			boolean isInEditMode, FormEditorMapping formEditorMapping) {
		super(type, model, resPrefix, isInEditMode);
		_formEditorMapping = formEditorMapping;

		PropertyDescriptor contentProperty = ContentDefinitionUtil.getContentProperty(model);
		new RepaintTrigger(contentProperty, true, kind -> kind == Kind.REMOVE, this).attachTo(model);
		new RepaintTrigger(contentProperty, true, kind -> kind == Kind.ADD, this).attachTo(model);
	}

	@Override
	void writeContent(DisplayContext context, TagWriter out, AttributeFormContext formContext) throws IOException {
		createTemplate(formContext);

		Control ctrl = createControl(formContext);
		GroupCellControl groupAttributes =
			new GroupCellControl(ctrl, new DefaultExpansionModel(false), new DefaultGroupSettings());
		groupAttributes.setTitle(Fragments.message(I18NConstants.FORM_EDITOR__ATTRIBUTES));
		groupAttributes.write(context, out);
	}

	private List<String> filterAttributes(List<String> fieldNames) {
		if (getModel() != null) {
			FormDefinition form = getModel();
			List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content = form.getContent();
			getFieldNames(content, fieldNames);
		}

		return fieldNames;
	}

	private void getFieldNames(List<PolymorphicConfiguration<? extends FormElementTemplateProvider>> content, List<String> fieldNames) {
		for (PolymorphicConfiguration<? extends FormElementTemplateProvider> c : content) {
			if (c instanceof ContainerDefinition) {
				ContainerDefinition<?> container = (ContainerDefinition<?>) c;
				getFieldNames(container.getContent(), fieldNames);
			} else if (c instanceof FieldDefinition) {
				FieldDefinition field = (FieldDefinition) c;
				fieldNames.add(field.getAttribute());
			} else if (c instanceof TabbarDefinition) {
				for (ContainerDefinition<?> tab : ((TabbarDefinition) c).getTabs()) {
					getFieldNames(tab.getContent(), fieldNames);
				}
			}
		}
	}

	private void createTemplate(AttributeFormContext context) {
		TLStructuredType type = getType();
		if (type != null) {
			List<? extends TLStructuredTypePart> attributes = list(type.getAllParts());

			FormEditorUtil.template(context,
				div(toTemplateForm(type, attributes, context)));
		}
	}

	/**
	 * Create a template for for every attribute in a given list for the given
	 * {@link AttributeFormContext}.
	 * 
	 * @param type
	 *        The type the form is built for. Note: This cannot be derived from the single
	 *        attributes, because those may be declared on any generalization thereof.
	 * @param content
	 *        List of attributes to create {@link FieldDefinition}s for.
	 * @param context
	 *        The context for the created {@link FormMember}s.
	 * 
	 * @return The created template.
	 */
	protected HTMLTemplateFragment toTemplateForm(TLStructuredType type, List<? extends TLStructuredTypePart> content,
			AttributeFormContext context) {
		List<HTMLTemplateFragment> formFieldTemplates = new ArrayList<>();
		List<String> fieldNames = filterAttributes(new ArrayList<>());
		List<? extends TLStructuredTypePart> attributes = list(content);
		Comparator<TLStructuredTypePart> sortByLabel =
			Comparator.comparing(MetaResourceProvider.INSTANCE::getLabel, String.CASE_INSENSITIVE_ORDER);
		Collections.sort(attributes, sortByLabel);

		for (TLStructuredTypePart attribute : attributes) {
			if (!fieldNames.contains(attribute.getName())) {
				createFormElement(context, formFieldTemplates, type, attribute);
			}
		}

		return verticalBox(formFieldTemplates);
	}

	// Creates {@link FieldDefinition}s for an attribute and its mapping.
	private void createFormElement(AttributeFormContext context, List<HTMLTemplateFragment> formFieldTemplates,
			TLStructuredType type, TLStructuredTypePart attribute) {
		// create ConfigItem and mapping
		FieldDefinition item = TypedConfiguration.newConfigItem(FieldDefinition.class);
		item.setAttribute(attribute.getName());
		item.setTypeSpec(TLModelUtil.qualifiedName(attribute.getType()));
		item.setFullQualifiedName(TLModelUtil.qualifiedName(attribute));

		// create formElement
		FormElementTemplateProvider fieldProvider = TypedConfigUtil.createInstance(item);

		FormEditorContext formContext = new FormEditorContext.Builder()
			.formMode(FormMode.DESIGN)
			.formType(type)
			.formContext(context)
			.contentGroup(context)
			.frameScope(getFrameScope())
			.formEditorMapping(_formEditorMapping)
			.editMode(_isInEditMode)
			.build();

		formFieldTemplates.add(fieldProvider.createDesignTemplate(formContext));
	}
}

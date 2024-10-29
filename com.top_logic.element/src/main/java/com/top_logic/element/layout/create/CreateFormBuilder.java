/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.CustomComparator;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.builder.ConfiguredDynamicFormBuilder;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.values.edit.FormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;

/**
 * {@link FormBuilder} that allows to generically create new objects and link them to their context.
 * 
 * <p>
 * As {@link com.top_logic.layout.form.component.AbstractCreateComponent.Config#getCreateHandler()},
 * use a subclass of {@link GenericCreateHandler}, e.g. {@link GenericStructureCreateHandler} for
 * creating objects in a {@link StructuredElement structure context}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreateFormBuilder extends ConfiguredDynamicFormBuilder {

	/**
	 * Name of the form group containing attribute fields.
	 */
	public static final String ATTRIBUTES_GROUP = "attributes";

	/**
	 * Name of the field whose contents is shown in the dialogs header area.
	 */
	@CalledFromJSP
	public static final String TITLE_FIELD = "title";

	/**
	 * The name of the field for displaying the type of the new element being created.
	 * 
	 * <p>
	 * The value of this field is the {@link TLClass} selected for creation.
	 * </p>
	 * 
	 * @see #ELEMENT_TYPE
	 */
	@CalledFromJSP
	public static final String TYPE_IMAGE_FIELD = "typeIcon";

	/** Name of the field for selecting the type to instantiate. */
	public static final String ELEMENT_TYPE = "createType";

	/**
	 * Configuration options for {@link CreateFormBuilder}.
	 */
	public interface Config extends ConfiguredDynamicFormBuilder.Config, UIOptions {
		// Pure sum interface.
	}

	/**
	 * Options that can be configured in-app for a {@link CreateFormBuilder}.
	 */
	@Abstract
	public interface UIOptions extends ConfigurationItem {
		/**
		 * @see #getHeader()
		 */
		String HEADER = "header";

		/**
		 * @see #getTypeOptions()
		 */
		String TYPE_OPTIONS = "typeOptions";

		/**
		 * The title displayed in the page header.
		 * 
		 * <p>
		 * The resource created type can be embedded in title by using the placeholder
		 * <code>{0}.</code>
		 * </p>
		 */
		@Name(HEADER)
		ResKey1 getHeader();

		/**
		 * Algorithm to compute possible types that can be instantiated in the context defined by
		 * this component's model.
		 */
		@Mandatory
		@Name(TYPE_OPTIONS)
		PolymorphicConfiguration<CreateTypeOptions> getTypeOptions();
	}

	private final CreateTypeOptions _typeOptions;

	private ResKey1 _header;

	/**
	 * Creates a {@link CreateFormBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CreateFormBuilder(InstantiationContext context, Config config) {
		super(context, config);

		_typeOptions = context.getInstance(config.getTypeOptions());
		_header = nonNull(config.getHeader(), I18NConstants.TITLE_CREATE_OBJECT__TYPE);
    }

	private static ResKey1 nonNull(ResKey1 title, ResKey1 defaultTitle) {
		return title == null ? defaultTitle : title;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent component) {
		AttributeFormContext formContext = new AttributeFormContext(component.getResPrefix());
		FormComponent.initFormContext(component, (FormHandler) component, formContext);

		FormField typeField = createTypeField(businessModel);
		typeField.addValueListener((FormField field, Object oldValue, Object newValue) -> onTypeChange(field, oldValue,
			newValue, businessModel));
		formContext.addMember(typeField);

		final HiddenField titleField = FormFactory.newHiddenField(TITLE_FIELD);
		titleField.setControlProvider(ValueDisplay.INSTANCE);
		formContext.addMember(titleField);

		final HiddenField imageField = FormFactory.newHiddenField(TYPE_IMAGE_FIELD);
		imageField.setVisible(false);
		imageField.setInheritDeactivation(false);
		formContext.addMember(imageField);

		FormGroup attributesGroup = new FormGroup(ATTRIBUTES_GROUP, ResPrefix.NONE);
		formContext.addMember(attributesGroup);

		// Initialize.
		onTypeChange(typeField, null, typeField.getValue(), businessModel);

		return formContext;
	}

	/**
	 * Creates the type selector field.
	 */
	protected FormField createTypeField(Object createContext) {
		List<TLClass> types = _typeOptions.getPossibleTypes(createContext);
		SelectField result = FormFactory.newSelectField(ELEMENT_TYPE, types, false, types.size() == 1);
		result.setOptionComparator(CustomComparator.newCustomComparator(types));
		result.setMandatory(true);
		result.setAsSingleSelection(_typeOptions.getDefaultType(createContext, types));
		return result;
	}

	/**
	 * Hook being called, if the {@value #ELEMENT_TYPE} field is changed.
	 *
	 * @param typeField
	 *        The changed field.
	 * @param oldValue
	 *        The field's old value.
	 * @param newValue
	 *        The field's new value.
	 */
	protected void onTypeChange(FormField typeField, Object oldValue, Object newValue, Object businessModel) {
		TLClass type = (TLClass) CollectionUtil.getSingleValueFrom(newValue);
		if (type == null) {
			return;
		}

		AttributeFormContext formContext = (AttributeFormContext) typeField.getFormContext();
		final FormField imageType = formContext.getField(TYPE_IMAGE_FIELD);
		imageType.setValue(type);

		final FormField titleField = formContext.getField(TITLE_FIELD);
		titleField.setValue(_header.fill(type));

		TLFormObject oldCreation = formContext.getAttributeUpdateContainer().getOverlay(null, null);
		if (oldCreation != null) {
			formContext.getAttributeUpdateContainer().removeOverlay(oldCreation);
		}

		TLObject container = businessModel instanceof TLObject ? (TLObject) businessModel : null;
		TLFormObject newCreation = formContext.createObject(type, null, container);

		// Note: The attribute group is statically rendered by the JSP. An inner group must
		// be created containing the object's attributes and the display template, because the whole
		// group must be replaced, if the type is changed.
		FormContainer editorGroup = formContext.createFormContainerForOverlay(newCreation);
		FormGroup attributeGroup = (FormGroup) formContext.getMember(ATTRIBUTES_GROUP);
		attributeGroup.addMember(editorGroup);
		TypedForm typedForm = TypedForm.lookup(getConfiguredForms(), type);
		setDisplayedTypedForm(typedForm);
		FormEditorContext context = new FormEditorContext.Builder()
			.formMode(FormMode.CREATE)
			.formType(typedForm.getFormType())
			.concreteType(typedForm.getDisplayedType())
			.formContext(formContext)
			.contentGroup(editorGroup)
			.concreteType(type)
			.build();
		FormEditorUtil.createAttributes(context, typedForm.getFormDefinition());

		moveValues(oldCreation, newCreation);

		initValues(newCreation, businessModel);
	}

	/**
	 * Initializes dynamic defaults in the given form object.
	 * 
	 * <p>
	 * This method is invoked initially when displaying the form and in reaction to a change of the
	 * type to create.
	 * </p>
	 * @param obj
	 *        The form object currently displayed.
	 * @param businessModel
	 *        The context model of the creation (the model of the create dialog).
	 */
	protected void initValues(TLFormObject obj, Object businessModel) {
		// Hook for subclasses.
	}

	/**
	 * Moves already entered values from the old object to the new object, when the user changes the
	 * type to create after he already started filling out the form.
	 */
	private void moveValues(TLFormObject oldCreation, TLFormObject newCreation) {
		if (oldCreation != null) {
			for (TLStructuredTypePart part : oldCreation.tType().getAllParts()) {
				if (part.isDerived()) {
					continue;
				}

				if (!DisplayAnnotations.isEditableInCreate(part)) {
					continue;
				}

				if (newCreation.tType().getPart(part.getName()) != part) {
					// Only copy values, if parts are the same. If they are different overrides,
					// the types may not be compatible.
					continue;
				}

				if (newCreation.getUpdate(part) == null) {
					// Part is not displayed in the current form.
					continue;
				}

				Object existingValue = oldCreation.tValue(part);
				if (existingValue == null
					|| (existingValue instanceof Collection<?> && ((Collection<?>) existingValue).isEmpty())) {
					// Do not copy empty values, those are most likely not entered explicitly.
					continue;
				}

				newCreation.tUpdate(part, existingValue);
			}
		}
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return _typeOptions.supportsContext(model);
	}
}

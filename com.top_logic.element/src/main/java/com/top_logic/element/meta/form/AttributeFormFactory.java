/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Configuration;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.element.layout.table.renderer.AttributedFieldProvider;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelI18N;

/**
 * Create a FormMember that allows editing values of a given AttributeUpdate.
 *
 *          form fields.
 *
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public abstract class AttributeFormFactory {

	public static final ResPrefix ATTRIBUTE_I18N_PREFIX = I18NConstants.ATTRIBUTED;

	public static final String SEARCH_FROM_FIELDNAME = "from";

	public static final String SEARCH_TO_FIELDNAME = "to";

    /** The key for finding the class to be used as factory. */
    private static final String IMPLEMENTATION_CLASS_CONFIGURATION_KEY = "implementation";

	/**
	 * Property key for attaching the corresponding {@link AttributeUpdate} to a {@link FormField}
	 * using the {@link FormMember#set(Property, Object)} interface.
	 * 
	 * @see #setAttributeUpdate(FormMember, AttributeUpdate)
	 * @see #getAttributeUpdate(FormMember)
	 */
	private static final Property<AttributeUpdate> ATTRIBUTE_UPDATE_KEY =
		TypedAnnotatable.property(AttributeUpdate.class, "ATTRIBUTE_UPDATE_KEY");

    /** Singleton pattern. */
    private static AttributeFormFactory singleton;

	/**
	 * Get the single instance
	 *
	 * @return the single instance
	 */
	public static synchronized AttributeFormFactory getInstance() {
		if (singleton == null) {
			try {
				Configuration configuration =
					Configuration.getConfiguration(AttributeFormFactory.class);

				String className =
					configuration.getValue(IMPLEMENTATION_CLASS_CONFIGURATION_KEY);

				if (!StringServices.isEmpty(className)) {
					Class implementationClass = Class.forName(className);
					singleton = (AttributeFormFactory) implementationClass.newInstance();
				} else {
					Logger.info(
							"No AttributeFormFactory configured, using default",
							AttributeFormFactory.class);
					singleton = new DefaultAttributeFormFactory();
				}
			} catch (Exception ex) {
				Logger.error("Error instantiating AttributeFormFactory", ex,
						      AttributeFormFactory.class);
			}
		}

		return singleton;
	}

	public FormMember toFormMember(AttributeUpdate update, AttributeUpdateContainer aContainer) {
		// Get FormConstraint(s)
		String fieldName = update.createFieldName();

		return toFormMember(update, aContainer, fieldName);
	}

	public FormMember toFormMember(AttributeUpdate update, AttributeUpdateContainer aContainer, String fieldName) {
		// Get FormConstraint(s)
		TLStructuredTypePart metaAttribute = update.getAttribute();

		FormMember result;
		if (update.isSearchUpdate()) {
			if (AttributeOperations.allowsSearchRange(metaAttribute)) {
				FormField from =
					(FormField) toFormField(update, aContainer, AttributeFormFactory.SEARCH_FROM_FIELDNAME);
				from.initializeField(update.getFromSearchUpdate());

				FormField to = (FormField) toFormField(update, aContainer, AttributeFormFactory.SEARCH_TO_FIELDNAME);
				to.initializeField(update.getToSearchUpdate());

				// Add range constraints to the fields. This ensures that the
				// entered values define a non-empty search range.
				from.addConstraint(
					new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, to));

				to.addConstraint(
					new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, from));

				FormGroup range = new FormGroup(fieldName, AttributeFormFactory.ATTRIBUTE_I18N_PREFIX);
				range.addMember(from);
				range.addMember(to);

				// Make the group representing the complex attribute have the
				// attribute's label.
				range.setLabel(TLModelI18N.getI18NName(metaAttribute));

				result = range;
			}
			else {
				FormMember field = toFormField(update, aContainer, fieldName);

				if (field instanceof FormField) {
					Object value;
					value = AttributeOperations.isCollectionValued(metaAttribute) ? update.getCollectionSearchUpdate()
						: update.getSimpleSearchUpdate();
					((FormField) field).initializeField(value);
				}

				result = field;
			}
		} else {
			// Update for create or edit.
			FormMember field = toFormField(update, aContainer, fieldName);

			result = field;
		}

		update.initField(result);

		return result;
	}

	/**
	 * Initialized the value of the given {@link FormField} with the value from the given
	 * {@link EditContext}.
	 */
	public static void initFieldValue(EditContext context, FormField field) {
		Object modelValue = context.getCorrectValues();
		Object fieldValue = toFieldValue(context, field, modelValue);
		field.initializeField(fieldValue);
	}

	/**
	 * Converts the given model value to a value suitable for editing in the given field.
	 * 
	 * @see AttributeUpdate#fieldToAttributeValue(FormField)
	 */
	public static Object toFieldValue(EditContext context, FormField field, Object modelValue) {
		if (context.isMultiple()) {
			if ((modelValue instanceof Collection) && (!(modelValue instanceof List))) {
				return new ArrayList<>((Collection<?>) modelValue);
			}
		} else {
			// A SelectField might have been created even for non-multiple attributes. And select
			// fields always use lists as values.
			if (field instanceof SelectField) {
				if (modelValue == null) {
					return Collections.emptyList();
				} else if (!(modelValue instanceof Collection)) {
					return Collections.singletonList(modelValue);
				}
			}
		}
		return modelValue;
	}

	/**
	 * Get a FormMember for the given AttributeUpdate.
	 *
	 * The {@link AttributeUpdate#getObject() attributed object} may be <code>null</code>, e.g.
	 * for a search or if data should be entered from which an {@link Wrapper} should be created. If
	 * the disabled flag is set, no input will be possible. If the default flag is set, the default
	 * value of the (primitive) attribute will be used to preset the value, if the object is
	 * <code>null</code> or its current value is <code>null</code>. Otherwise the current value will
	 * be used. If the attribute is mandatory an input or a selection will be required. The
	 * parameter type has to be one of those specified in {@link MetaAttributeGUIHelper} or
	 * <code>null</code>. In that case ATT_PREFIX will be used.
	 *
	 * @param aAttributeUpdate
	 *        The AttributeUpdate. Must not be <code>null</code>.
	 * @param aContainer
	 *        the {@link AttributeUpdateContainer} needed to get possible values
	 * @param aFieldName
	 *        The {@link FormMember#getName() name} of the created form field.
	 * @return the FormMember. <code>null</code> if the AttributeUpdate type is wrapper set or
	 *         unknown.
	 */
	protected abstract FormMember toFormField(AttributeUpdate aAttributeUpdate, AttributeUpdateContainer aContainer,
			String aFieldName);

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someObjects    The attributed objects to be displayed in the table, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @return   The new created table form field, never <code>null</code>.
     */
    public TableField createTableField(String aName, AttributeFormContext aContext, List someObjects, String[] someColumns) {
        return this.createTableField(aName, aContext, someObjects, someColumns, DefaultFormFieldControlProvider.INSTANCE);
    }

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someObjects    The attributed objects to be displayed in the table, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @param    aProvider      The control provider to be used, must not be <code>null</code>.
     * @return   The new created table form field, never <code>null</code>.
     */
    public TableField createTableField(String aName, AttributeFormContext aContext, List someObjects, String[] someColumns, ControlProvider aProvider) {
        return this.createTableField(aName, aContext, someObjects, someColumns, aProvider, null);
    }

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someObjects    The attributed objects to be displayed in the table, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @param    anAccessor     The accessor to be used for getting the values from the attributed objects, may be <code>null</code>.
     * @return   The new created table form field, never <code>null</code>.
     */
    public TableField createTableField(String aName, AttributeFormContext aContext, List someObjects, String[] someColumns, ControlProvider aProvider, Accessor anAccessor) {
        ControlProvider[] theProviders = new ControlProvider[someColumns.length];

        Arrays.fill(theProviders, aProvider);

        return this.doCreateTableField(aName, aContext, someObjects, someColumns, theProviders, anAccessor);
    }

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someObjects    The attributed objects to be displayed in the table, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @param    someProviders  The control providers, must have the same size than "someColumns", values may be <code>null</code>.
     * @param    anAccessor     The accessor to be used for getting the values from the attributed objects, may be <code>null</code>.
     * @return   The new created table form field, never <code>null</code>.
     */
    public TableField createTableField(String aName, AttributeFormContext aContext, List someObjects, String[] someColumns, ControlProvider[] someProviders, Accessor anAccessor) {
        for (int thePos = 0; thePos < someProviders.length; thePos++) {
            if (someProviders[thePos] == null) {
                someProviders[thePos] = DefaultFormFieldControlProvider.INSTANCE;
            }
        }

        return this.doCreateTableField(aName, aContext, someObjects, someColumns, someProviders, anAccessor);
    }

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aMember        The select field to be used as base for the table, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @param    someProviders  The control providers, must have the same size than "someColumns", values may be <code>null</code>.
     * @param    anAccessor     The accessor to be used for getting the values from the attributed objects, must not be <code>null</code>.
     * @return   The new created table form field, never <code>null</code>.
     */
    public TableField createTableField(String aName, SelectField aMember, AttributeFormContext aContext, String[] someColumns, ControlProvider[] someProviders, Accessor anAccessor) {
        for (int thePos = 0; thePos < someProviders.length; thePos++) {
            if (someProviders[thePos] == null) {
                someProviders[thePos] = DefaultFormFieldControlProvider.INSTANCE;
            }
        }

        return FormFactory.newTableField(aName, aMember, aContext, new AttributedFieldProvider(aContext), someColumns, someProviders, anAccessor);
    }

    /** 
     * Create an editable table field.
     * 
     * @param    aName          The name of the field to be created, must not be <code>null</code>.
     * @param    aContext       The form context the field will live in, must not be <code>null</code>.
     * @param    someObjects    The attributed objects to be displayed in the table, must not be <code>null</code>.
     * @param    someColumns    The names of the columns to be displayed, must not be <code>null</code> or empty.
     * @param    someProviders  The control providers, must have the same size than "someColumns", values must not be <code>null</code>.
     * @param    anAccessor     The accessor to be used for getting the values from the attributed objects, may be <code>null</code>.
     * @return   The new created table form field, never <code>null</code>.
     */
    protected TableField doCreateTableField(String aName, AttributeFormContext aContext, List someObjects, String[] someColumns, ControlProvider[] someProviders, Accessor anAccessor) {
        if (anAccessor == null) {
            anAccessor = WrapperAccessor.INSTANCE;
        }

        return FormFactory.newTableField(aName,
                FormFactory.newSelectField(aName, someObjects, true, someObjects, false),
                aContext, 
                new AttributedFieldProvider(aContext),
                someColumns,
                someProviders,
                anAccessor);
    }

	/**
	 * Retrieve the {@link AttributeUpdate} associated with the given field.
	 * 
	 * @param field
	 *        The {@link FormMember} for which the {@link AttributeUpdate} should be retrieved.
	 * @return The {@link AttributeUpdate} associated with the given field.
	 */
	public static AttributeUpdate getAttributeUpdate(FormMember field) {
		return field.get(ATTRIBUTE_UPDATE_KEY);
	}

	/**
	 * Returns the {@link KnowledgeObject} of the {@link Wrapper} the given field were built for.
	 */
	@FrameworkInternal
	public static KnowledgeItem getAttributedKO(FormMember field) {
		return getAttributeUpdate(field).getObject().tHandle();
	}
	/**
	 * Associate the given {@link AttributeUpdate} with the given {@link FormMember}.
	 * 
	 * @param field
	 *        The {@link FormMember} on which to set the {@link AttributeUpdate}.
	 * @param update
	 *        The {@link AttributeUpdate} to associate with the given {@link FormMember}.
	 */
	public static void setAttributeUpdate(FormMember field, AttributeUpdate update) {
		field.set(ATTRIBUTE_UPDATE_KEY, update);
	}
}

/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.layout.create.CreateFormBuilder;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.model.TLClass;
import com.top_logic.util.Resources;


/**
 * Component (usually a Dialog) to create a new StructuredElement.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 * 
 * @deprecated Use {@link AbstractCreateComponent} with {@link CreateFormBuilder}
 */
@Deprecated
public class StructuredElementCreateComponent extends AbstractCreateComponent {

	/**
	 * The image type of the selected {@link #ELEMENT_NAME}.
	 */
	@CalledFromJSP
	public static final String IMAGE_TYPE_FIELD = "imageType";

    /** Name if the new element. */
    public static final String ELEMENT_NAME = "name";

    /** Type of the new element. */
    public static final String ELEMENT_TYPE = "create_parameter_type";

	/**
	 * Configuration for the {@link StructuredElementCreateComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		@StringDefault(StructuredElementCreateHandler.COMMAND)
		@Override
		String getCreateHandler();

	}

	/**
	 * @param someAttr
	 *        The attributes to configure this component.
	 */
    public StructuredElementCreateComponent(InstantiationContext context, Config someAttr) throws ConfigurationException {
        super(context, someAttr);
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return (anObject instanceof StructuredElement);
    }

    @Override
	public FormContext createFormContext() {
        ResPrefix            thePrefix  = this.getResPrefix();
        StructuredElement theElement = (StructuredElement) this.getModel();
        FormContext       theContext = new FormContext("createSE", thePrefix);

        theContext.addMember(this.getNameField(thePrefix));
		FormField typeField = this.getTypeField(theElement, thePrefix);
		theContext.addMember(typeField);

		final HiddenField imageType = FormFactory.newHiddenField(IMAGE_TYPE_FIELD);
		imageType.setVisible(false);
		imageType.setInheritDeactivation(false);
		theContext.addMember(imageType);

		ValueListener listener = new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				TLClass subtype = (TLClass) CollectionUtil.getSingleValueFrom(field.getValue());
				if (subtype == null) {
					imageType.setValue(null);
				} else {
					imageType.setValue(subtype);
				}
			}
		};
		typeField.addValueListener(listener);
		// Initialize.
		listener.valueChanged(typeField, null, typeField.getValue());

        return (theContext);
    }

    /** 
     * Return a name field for the dialog.
     * 
     * @param    resPrefix    The resource prefix, must not be <code>null</code>.
     * @return   The requested form field, never <code>null</code>.
     */
    protected FormField getNameField(ResPrefix resPrefix) {
        StringField theField = FormFactory.newStringField(StructuredElementCreateComponent.ELEMENT_NAME);

        theField.setMandatory(true);

        theField.setDefaultValue(Resources.getInstance().getString(resPrefix.key("newName")));
        theField.addConstraint(new StringLengthConstraint(1, 50));

        return (theField);
    }

    /** 
     * Return a selection for the type of the new created element.
     * 
     * @param    anElement    The current element to create a child for, must not be <code>null</code>.
     * @param    resPrefix      The resource prefix, must not be <code>null</code>.
     * @return   The requested form field, never <code>null</code>.
     */
    protected FormField getTypeField(StructuredElement anElement, ResPrefix resPrefix) {
		List<TLClass> types = list(anElement.getChildrenTypes());
		LabelComparator comparator = LabelComparator.newCachingInstance();
		Collections.sort(types, comparator);
		SelectField theField =
			FormFactory.newSelectField(StructuredElementCreateComponent.ELEMENT_TYPE, types, false,
				types.size() == 1);
		theField.setOptionComparator(comparator);

		if (types.size() > 0) {
			theField.setAsSingleSelection(chooseInitialType(types));
        }
        theField.setMandatory(true);

        return (theField);
    }

	/**
	 * Hook for subclasses to choose the initial type.
	 * <p>
	 * This method is called only when there are multiple types and when there is no "default" type
	 * given to {@link #getTypeField(StructuredElement, ResPrefix)}.
	 * </p>
	 * 
	 * @param types
	 *        The {@link TLClass}es to choose from. Never null or empty.
	 * @return Has to be an element of the given {@link List} of {@link TLClass}, or null.
	 */
	protected TLClass chooseInitialType(List<TLClass> types) {
		return types.get(0);
	}

}

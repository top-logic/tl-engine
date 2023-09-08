/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table.renderer;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.fieldprovider.FloatFieldProvider;
import com.top_logic.element.meta.form.fieldprovider.LongFieldProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.table.model.AbstractFieldProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Provide form fields for a given form context.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedFieldProvider extends AbstractFieldProvider {

    /** The context used by this instance. */
    private final AttributeFormContext context;

    /** 
     * Creates a {@link AttributedFieldProvider}.
     * 
     * @param    aContext    The context to be used, must not be <code>null</code>.
     */
    public AttributedFieldProvider(AttributeFormContext aContext) {
        assert aContext != null : "Context must not be null";

        this.context = aContext;
    }

    /**
     * Create a form field for the given attribute name.
     * 
     * @param    aModel        The attributed to get the value from, must not be <code>null</code>.
     * @param    anAccessor    The accessor to get values from, must not be <code>null</code>.
     * @param    aName         The name of the requested meta attribute, must not be <code>null</code>.
     * @return   A new created form field, never <code>null</code>.
     * @throws   TopLogicException    If the requested meta attribute type is not supported or creation fails for another reason.
     * @see      com.top_logic.layout.table.model.FieldProvider#createField(java.lang.Object, com.top_logic.layout.Accessor, java.lang.String)
     */
    @Override
	public FormMember createField(Object aModel, Accessor anAccessor, String aName) {
        try {
            Wrapper    theModel    = (Wrapper) aModel;
			TLStructuredTypePart theMA = theModel.tType().getPart(aName);
			if (theMA == null) {
				throw new NoSuchAttributeException(
					"No attribute with name " + aName + " in type " + theModel.tType());
			}
            Object        theValue    = anAccessor.getValue(aModel, aName);
            boolean       isMandatory = theMA.isMandatory();
            int           theType     = AttributeOperations.getMetaAttributeType(theMA);

			if (AttributeOperations.getOptions(theMA) != null) {
				OptionModel<?> options = AttributeOperations
					.allOptions(this.context.getAttributeUpdateContainer().getAttributeUpdate(theMA, theModel));

                switch (theType) {
                    case LegacyTypeCodes.TYPE_WRAPPER:
						return FormFactory.newSelectField(aName, options, false, Collections.singletonList(theValue),
							isMandatory, false, null);
                    case LegacyTypeCodes.TYPE_TYPEDSET:
						return FormFactory.newSelectField(aName, options, true,
							CollectionUtil.toList((Collection) theValue), isMandatory, false, null);
                    default:
                        break;
                }
            }
            else {
                switch (theType) {
                    case LegacyTypeCodes.TYPE_BOOLEAN:
                        return FormFactory.newBooleanField(aName, theValue, isMandatory, false, null);
                    case LegacyTypeCodes.TYPE_LONG:
                        return FormFactory.newComplexField(aName, LongFieldProvider.getLongFormat(), theValue, true, isMandatory, false, null);
                    case LegacyTypeCodes.TYPE_FLOAT:
                        return FormFactory.newComplexField(aName, FloatFieldProvider.getFloatFormat(), theValue, true, isMandatory, false, null);
                    case LegacyTypeCodes.TYPE_DATE:
                        return FormFactory.newComplexField(aName, HTMLFormatter.getInstance().getDateFormat(), theValue, true, isMandatory, false, null);
                    case LegacyTypeCodes.TYPE_STRING:
                        return FormFactory.newStringField(aName, theValue, isMandatory, false, null);
                    default:
                        break;
                }
            }

            throw new TopLogicException(AttributedFieldProvider.class, "field.create.unsupported", new Object[] {aModel, aName, AttributeSettings.getInstance().getTypeAsString(theType)});
        }
        catch (Exception ex) {
            throw new TopLogicException(AttributedFieldProvider.class, "field.create", new Object[] {aModel, aName}, ex);
        }
    }
}


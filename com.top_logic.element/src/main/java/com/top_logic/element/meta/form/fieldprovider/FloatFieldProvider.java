/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.text.Format;
import java.text.NumberFormat;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.FloatPrimitiveRangeConstraint;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLCollectionSeparator;
import com.top_logic.model.annotate.TLRange;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Double}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FloatFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		Format format;
		try {
			format = DisplayAnnotations
				.getFloatFormat(editContext.getAnnotation(com.top_logic.model.annotate.ui.Format.class));
		} catch (ConfigurationException ex) {
			Logger.error("Invalid attribute definition for '" + editContext.toString() + "'.", ex,
				FloatFieldProvider.class);
			format = getFloatFormat();
		}
		boolean multiple = editContext.isMultiple();
		if (multiple) {
			format = LongFieldProvider.collectionFormat(format, editContext.isOrdered(),
				editContext.getAnnotation(TLCollectionSeparator.class));
		}

		Generator options = editContext.getOptions();
		if (options != null) {
			OptionModel<?> optionModel = options.generate(editContext);
			SelectField result = FormFactory.newSelectField(fieldName, optionModel, multiple,
				isMandatory, isDisabled, mandatoryChecker);
			result.setOptionLabelProvider(new FormatLabelProvider(format));
			return result;
		}

		ComplexField theField = FormFactory.newComplexField(fieldName, format, null, true,
			isMandatory, isDisabled, mandatoryChecker);
		if (!editContext.isDerived()) {
			TLRange range = editContext.getAnnotation(TLRange.class);
			Number maximum = AttributeOperations.getMaximum(range);
			Number minimum = AttributeOperations.getMinimum(range);
			boolean hasMinimum = minimum != null;
			boolean hasMaximum = maximum != null;
			if (hasMaximum || hasMinimum) {
				theField.addConstraint(
					new FloatPrimitiveRangeConstraint(
						hasMinimum,
						hasMinimum ? minimum.floatValue() : Float.MIN_VALUE,
						hasMaximum,
						hasMaximum ? maximum.floatValue() : Float.MAX_VALUE,
						isSearch || !editContext.isMandatory()
					)
					);
			}
		}
		return theField;
	}

	/**
	 * A suitable {@link NumberFormat} for attributes of type {@link LegacyTypeCodes#TYPE_FLOAT}.
	 */
	public static Format getFloatFormat() {
		return HTMLFormatter.getInstance().getDoubleFormat();
	}

}

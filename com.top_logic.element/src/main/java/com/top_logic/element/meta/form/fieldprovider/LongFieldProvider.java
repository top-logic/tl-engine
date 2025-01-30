/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.format.ListFormat;
import com.top_logic.basic.format.SetFormat;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.constraints.LongPrimitiveRangeConstraint;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectField.Config;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.provider.FormatLabelProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLCollectionSeparator;
import com.top_logic.model.annotate.TLRange;
import com.top_logic.util.TLContext;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Long}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LongFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember createFormField(EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		Format format;
		try {
			format = DisplayAnnotations.getLongFormat(editContext);
		} catch (ConfigurationException ex) {
			Logger.error("Invalid attribute definition for '" + editContext.toString() + "'.", ex,
				LongFieldProvider.class);
			format = getLongFormat();
		}
		boolean multiple = editContext.isMultiple();
		if (multiple) {
			format = collectionFormat(format, editContext.isOrdered(),
				editContext.getAnnotation(TLCollectionSeparator.class));
		}

		Generator options = editContext.getOptions();
		if (options != null) {
			OptionModel<?> optionModel = options.generate(editContext);
			SelectField result = FormFactory.newSelectField(fieldName, optionModel, multiple,
				isMandatory, isDisabled, mandatoryChecker);
			Comparator optionOrder = options.getOptionOrder();
			if (optionOrder != null) {
				result.setOptionComparator(optionOrder);
			}
			result.setOptionLabelProvider(new FormatLabelProvider(format));
			return result;
		}

		ComplexField theField = FormFactory.newComplexField(fieldName, format, null, true,
			isMandatory, isDisabled, mandatoryChecker);
		if (!editContext.isDerived()) {
			TLRange range = editContext.getAnnotation(TLRange.class);
			Number maximum = AttributeOperations.getMaximum(range);
			boolean hasMaximum = maximum != null;
			Number minimum = AttributeOperations.getMinimum(range);
			boolean hasMinimum = minimum != null;
			if (hasMaximum || hasMinimum) {
				theField.addConstraint(
					new LongPrimitiveRangeConstraint(
						hasMinimum,
						hasMinimum ? minimum.longValue() : Long.MIN_VALUE,
						hasMaximum,
						hasMaximum ? maximum.longValue() : Long.MAX_VALUE,
						isSearch || !editContext.isMandatory()
					));
			}
		}
		return theField;
	}

	/**
	 * Upgrades the given list item format to a list format using the specified collection
	 * separators, or the application-wide configured delimiters, if no separators are given.
	 */
	public static Format collectionFormat(Format format, boolean ordered, TLCollectionSeparator collectionSeparator) {
		Pattern parseDelimiter;
		String formatDelimiter;
		if (collectionSeparator == null) {
			Config config = SelectFieldUtils.getSelectFieldConfiguration();
			parseDelimiter = Pattern.compile(Pattern.quote(config.getMultiSelectionSeparator()));
			formatDelimiter = config.getMultiSelectionSeparatorFormat();
		} else if (collectionSeparator.isRegularExpression()) {
			parseDelimiter = Pattern.compile(collectionSeparator.getParseSeparator());
			formatDelimiter = collectionSeparator.getFormatSeparator();
		} else {
			parseDelimiter = Pattern.compile(Pattern.quote(collectionSeparator.getParseSeparator()));
			formatDelimiter = collectionSeparator.getFormatSeparator();
			if (formatDelimiter == null) {
				formatDelimiter = collectionSeparator.getParseSeparator() + " ";
			}
		}
		return ordered ? new ListFormat(format, parseDelimiter, formatDelimiter, true)
			: new SetFormat(format, parseDelimiter, formatDelimiter, true);
	}

	/**
	 * A suitable {@link NumberFormat} for attributes of type {@link LegacyTypeCodes#TYPE_LONG}.
	 */
	public static NumberFormat getLongFormat() {
		return NumberFormat.getIntegerInstance(TLContext.getLocale());
	}

}

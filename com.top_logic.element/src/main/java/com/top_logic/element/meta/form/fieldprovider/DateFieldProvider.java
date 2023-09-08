/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.text.Format;
import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.control.CalendarControl;
import com.top_logic.layout.form.control.CalendarMarker;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MarkerFactory;
import com.top_logic.model.annotate.ui.MarkerFactoryAnnotation;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link Date}.
 * 
 * @see DateTimeFieldProvider
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DateFieldProvider extends AbstractFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {

		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON : null;
		ComplexField field =
			FormFactory.newDateField(fieldName, null, true, isMandatory, isDisabled, mandatoryChecker);
		setConfiguredFormat(field, editContext);
		setMarkerImplementation(field, editContext);

		com.top_logic.layout.table.filter.DateFieldProvider.setShortcutParser(field);
		return field;
	}

	private void setConfiguredFormat(ComplexField field, EditContext editContext) {
		Format format;
		try {
			format = DisplayAnnotations
				.getConfiguredDateFormat(editContext.getAnnotation(com.top_logic.model.annotate.ui.Format.class));
		} catch (ConfigurationException ex) {
			Logger.error("Invalid attribute definition for '" + editContext + "'.", ex, DateFieldProvider.class);
			format = null;
		}
		if (format != null) {
			field.setFormatAndParser(format);
		}
	}

	/**
	 * This method handles the {@link MarkerFactoryAnnotation}.
	 * 
	 * @param field
	 *        The date field.
	 * @see MarkerFactoryAnnotation
	 */
	private void setMarkerImplementation(ComplexField field, EditContext editContext) {
		MarkerFactoryAnnotation factory = editContext.getAnnotation(MarkerFactoryAnnotation.class);
		if (factory != null) {
			MarkerFactory markerFactory = TypedConfigUtil.createInstance(factory.getImplementation());
			CalendarMarker calendarMarker = markerFactory.getCalendarMarker(editContext.getObject(), field);
			field.set(CalendarControl.MARKER_CSS, calendarMarker);
		}
	}

}

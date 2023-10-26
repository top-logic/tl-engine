/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import java.util.Collections;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElementComparator;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.constraints.GenericMandatoryConstraint;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.util.TLCollator;

/**
 * {@link FieldProvider} for {@link TLStructuredTypePart}s of type {@link FastList}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassificationFieldProvider extends AbstractSelectFieldProvider {

	@Override
	public FormMember getFormField(EditContext editContext, String fieldName) {
		ClassificationPresentation presentation = TLAnnotations.getClassificationPresentation(
			editContext.getAnnotation(ClassificationDisplay.class), editContext.isMultiple());
		switch (presentation) {
			case CHECKLIST:
				return getCheckListField(editContext, fieldName);
			case POP_UP:
			case DROP_DOWN:
				return getDropDownField(editContext, fieldName);
			default:
				throw ClassificationPresentation.noSuchEnum(presentation);
		}
	}

	private FormMember getDropDownField(final EditContext editContext, String fieldName) {
		boolean isMandatory = editContext.isMandatory();
		boolean isDisabled = editContext.isDisabled();
		boolean isSearch = editContext.isSearchUpdate();

		Constraint mandatoryChecker = isMandatory ? GenericMandatoryConstraint.SINGLETON
			: null;

		boolean isMultiple = editContext.isMultiple();
		boolean isOrdered = editContext.isOrdered();

		SelectField selectField =
			newSelectField(fieldName, Collections.EMPTY_LIST, isMultiple, isOrdered, isSearch, isMandatory,
			mandatoryChecker, isDisabled, true);
		initField(selectField, editContext);
		return selectField;
	}

	private static FormMember getCheckListField(final EditContext editContext,
			String fieldName) {
		boolean isDisabled = editContext.isDisabled();
	
		SelectField selectField = FormFactory.newSelectField(fieldName, Collections.EMPTY_LIST, true, isDisabled);
		initField(selectField, editContext);
		selectField.setOptionLabelProvider(MetaResourceProvider.INSTANCE);

		return selectField;
	}

	private static void initField(final SelectField selectField, final EditContext editContext) {
		selectField.setOptionModel(AttributeOperations.allOptions(editContext));
		selectField.setOptionComparator(new FastListElementComparator(new TLCollator()));
	}

}

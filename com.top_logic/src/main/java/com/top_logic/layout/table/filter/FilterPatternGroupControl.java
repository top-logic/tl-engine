/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.basic.util.Utils;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link AbstractControl} for managing and display filter patterns of {@link ComparableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class FilterPatternGroupControl extends AbstractControl implements ValueListener {

	private static final String FIRST_PATTERN = "firstPattern";
	private static final String SECOND_PATTERN = "secondPattern";
	private static final String PUSH_DOWN_FOREGROUND = "pushDownForeground";
	private static final String PUSH_DOWN_BACKGROUND = "pushDownBackground";
	private static final String PULL_UP_FOREGROUND = "pullUpForeground";
	private static final String PULL_UP_BACKGROUND = "pullUpBackground";

	private static final String PATTERN_BOX = "patternBox";
	private static final String PATTERN_BOX_EXPANDED = "patternBoxExpanded";

	private static final DefaultFormFieldControlProvider CONTROL_PROVIDER = new DefaultFormFieldControlProvider() {
		@Override
		public Control visitFormField(FormField member, Void arg) {
			TextInputControl textInputControl = new TextInputControl(member);
			textInputControl.setColumns(18);
			return textInputControl;
		};
	};

	private ComparableFilterViewFormFields dialogFormFields;

	private Comparator<Object> patternFieldValueComparator;
	private String patternBoxState;
	private String firstPatternState;
	private String secondPatternState;
	
	FilterPatternGroupControl(ComparableFilterViewFormFields dialogFormFields, Comparator<Object> fieldValueComparator) {
		this.dialogFormFields = dialogFormFields;
		this.patternFieldValueComparator = fieldValueComparator;
		initState();
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeField(out, context, dialogFormFields.getPrimaryFilterPatternField(), getUpperFieldId(), firstPatternState);
		writeField(out, context, dialogFormFields.getSecondaryFilterPatternField(), getLowerFieldId(), secondPatternState);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, patternBoxState);
	}

	private CharSequence getPatternBoxId() {
		return getID();
	}

	private String getUpperFieldId() {
		return getID() + dialogFormFields.getPrimaryFilterPatternField().getName();
	}

	private String getLowerFieldId() {
		return getID() + dialogFormFields.getSecondaryFilterPatternField().getName();
	}

	private void writeField(TagWriter out, DisplayContext context, FormField filterPatternField, String patternId, String patternClass)
			throws IOException {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, patternId);
		out.writeAttribute(CLASS_ATTR, patternClass);
		out.endBeginTag();
		CONTROL_PROVIDER.createControl(filterPatternField).write(
			context, out);
		CONTROL_PROVIDER.createControl(filterPatternField, FormTemplateConstants.STYLE_ERROR_VALUE).write(
			context, out);
		out.endTag(DIV);
	}

	@Override
	protected void attachRevalidated() {
		super.attachRevalidated();
		initState();
	}

	private void initState() {
		if (isRangeOperatorSelected()) {
			if (haveNotAscendingOrder()) {
				swapPatternFields();
			}
			addPatternValueListeners();
			patternBoxState = PATTERN_BOX_EXPANDED;
		} else {
			patternBoxState = PATTERN_BOX;
		}
		firstPatternState = FIRST_PATTERN;
		secondPatternState = SECOND_PATTERN;
		dialogFormFields.getOperatorField().addValueListener(this);
	}

	private boolean isRangeOperatorSelected() {
		return (Operators) dialogFormFields.getOperatorField().getSingleSelection() == Operators.BETWEEN;
	}

	@Override
	protected void detachInvalidated() {
		dialogFormFields.getOperatorField().removeValueListener(this);
		removePatternListeners();
		super.detachInvalidated();
	}

	private void updateFieldPositions(FormField changedField) {
		if (changedField == dialogFormFields.getPrimaryFilterPatternField()) {
			addUpdate(new JSSnipplet(createPushDownUpdate()));
			firstPatternState = PULL_UP_BACKGROUND;
			secondPatternState = PUSH_DOWN_FOREGROUND;
		} else {
			addUpdate(new JSSnipplet(createPullUpUpdate()));
			firstPatternState = PULL_UP_FOREGROUND;
			secondPatternState = PUSH_DOWN_BACKGROUND;
		}
		swapPatternFields();
	}

	private void swapPatternFields() {
		FormField temp = dialogFormFields.getPrimaryFilterPatternField();
		dialogFormFields.setPrimaryFilterPatternField(dialogFormFields.getSecondaryFilterPatternField());
		dialogFormFields.setSecondaryFilterPatternField(temp);
	}
	
	private void enlargePatternBox() {
		patternBoxState = PATTERN_BOX_EXPANDED;
		addPatternValueListeners();
		addUpdate(new JSSnipplet(createPatternBoxUpdate()));
	}

	private void addPatternValueListeners() {
		dialogFormFields.getPrimaryFilterPatternField().addValueListener(this);
		dialogFormFields.getSecondaryFilterPatternField().addValueListener(this);
	}

	private void shrinkPatternBox() {
		patternBoxState = PATTERN_BOX;
		removePatternListeners();
		addUpdate(new JSSnipplet(createPatternBoxUpdate()));
	}

	private void removePatternListeners() {
		dialogFormFields.getPrimaryFilterPatternField().removeValueListener(this);
		dialogFormFields.getSecondaryFilterPatternField().removeValueListener(this);
	}

	private String createPatternBoxUpdate() {
		StringBuilder builder = new StringBuilder();
		builder.append("(function() {");
		builder.append("var patternBox = document.getElementById('" + getPatternBoxId() + "');");
		if (Utils.equals(patternBoxState, PATTERN_BOX_EXPANDED)) {
			builder.append("BAL.DOM.replaceClass(patternBox, '" + PATTERN_BOX + "', '" + PATTERN_BOX_EXPANDED + "');");
		} else {
			builder.append("BAL.DOM.replaceClass(patternBox, '" + PATTERN_BOX_EXPANDED + "', '" + PATTERN_BOX + "');");
		}
		builder.append("})()");
		return builder.toString();
	}

	private String createPushDownUpdate() {
		StringBuilder builder = new StringBuilder();
		builder.append("(function() {");
		builder.append("var upperField = document.getElementById('" + getUpperFieldId() + "');");
		builder.append("var lowerField = document.getElementById('" + getLowerFieldId() + "');");
		builder.append("BAL.DOM.replaceClass(upperField, '" + firstPatternState + "', '" + PUSH_DOWN_FOREGROUND + "');");
		builder.append("BAL.DOM.replaceClass(lowerField, '" + secondPatternState + "', '" + PULL_UP_BACKGROUND + "');");
		builder.append("})()");
		return builder.toString();
	}

	private String createPullUpUpdate() {
		StringBuilder builder = new StringBuilder();
		builder.append("(function() {");
		builder.append("var upperField = document.getElementById('" + getUpperFieldId() + "');");
		builder.append("var lowerField = document.getElementById('" + getLowerFieldId() + "');");
		builder.append("BAL.DOM.replaceClass(upperField, '" + firstPatternState + "', '" + PUSH_DOWN_BACKGROUND + "');");
		builder.append("BAL.DOM.replaceClass(lowerField, '" + secondPatternState + "', '" + PULL_UP_FOREGROUND + "');");
		builder.append("})()");
		return builder.toString();
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		if (field == dialogFormFields.getOperatorField()) {
			handleOperatorUpdate(oldValue, newValue);
		} else {
			handlePatternFieldUpdates(field);
		}
	}

	private void handleOperatorUpdate(Object oldValue, Object newValue) {
		Operators oldOperator = decodeOperatorValue(oldValue);
		Operators newOperator = decodeOperatorValue(newValue);

		if (newOperator == Operators.BETWEEN) {
			setupMultiFieldDisplay();
		} else if (oldOperator == Operators.BETWEEN) {
			setupSingleFieldDisplay();
		}
	}

	private Operators decodeOperatorValue(Object encodedValue) {
		return (Operators) ((List) encodedValue).get(0);
	}

	private void setupSingleFieldDisplay() {
		FormField secondaryFilterPatternField = dialogFormFields.getSecondaryFilterPatternField();
		secondaryFilterPatternField.setVisible(false);
		secondaryFilterPatternField.setValue(null);
		shrinkPatternBox();
	}

	private void setupMultiFieldDisplay() {
		FormField secondaryFilterPatternField = dialogFormFields.getSecondaryFilterPatternField();
		secondaryFilterPatternField.setVisible(true);
		enlargePatternBox();
	}

	private void handlePatternFieldUpdates(FormField changedField) {
		if (haveNotAscendingOrder()) {
			updateFieldPositions(changedField);
		}
	}

	private boolean haveNotAscendingOrder() {
		if (havePatternFieldsValues()) {
			FormField primaryFilterPatternField = dialogFormFields.getPrimaryFilterPatternField();
			FormField secondaryFilterPatternField = dialogFormFields.getSecondaryFilterPatternField();
			return patternFieldValueComparator.compare(primaryFilterPatternField.getValue(),
				secondaryFilterPatternField.getValue()) > 0;
		} else {
			return false;
		}
	}

	private boolean havePatternFieldsValues() {
		FormField primaryFilterPatternField = dialogFormFields.getPrimaryFilterPatternField();
		FormField secondaryFilterPatternField = dialogFormFields.getSecondaryFilterPatternField();
		return primaryFilterPatternField.hasValue() && secondaryFilterPatternField.hasValue()
			&& primaryFilterPatternField.getValue() != null && secondaryFilterPatternField.getValue() != null;
	}
}

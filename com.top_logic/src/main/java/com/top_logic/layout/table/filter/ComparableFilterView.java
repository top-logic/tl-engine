/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.SelectionFilterViewBuilder.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;
import com.top_logic.layout.table.filter.ComparableFilterConfiguration.Operators;
import com.top_logic.layout.table.filter.SelectionFilterView.CheckboxMultiSelectControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.util.Utils;

/**
 * {@link FilterViewControl} of a {@link ComparableFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">sts</a>
 */
public class ComparableFilterView extends FilterViewControl<ComparableFilterConfiguration> {
	
	static final String FILTER_GROUP_NAME = ComparableFilterView.class.getSimpleName();

	static final String PATTERN_GROUP_NAME = FILTER_GROUP_NAME + "-Pattern";

	static final ResPrefix RES_PREFIX = ResPrefix.legacyClassLocal(ComparableFilterView.class);
	
	static final String SELECTABLE_VALUES_FIELD = "selectableValuesField";
	static final String OPERATOR_SELECT_FIELD = "operator";
	private static final String FILTER_BOX = "filterBox";
	private static final String OPERATOR_BOX = "operatorBox";

	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";
	private static final String DEFAULT_FILTER_CONTAINER_CLASS = "defaultFilterContainer";
	
	private static final Document DIALOG_TEMPLATE = createPattern();

	private static Document createPattern() {
		return DOMUtil.parseThreadSafe(
			"<t:group"
				+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
				+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
				+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
				+ ">"
				+ "<form action=''>"
				+ "<div class='" + FILTER_BOX + "'>"
				+ "<div class='" + OPERATOR_BOX + "'>"
				+ "<span style='margin-right: 3px'>"
				+ "<p:field name='" + OPERATOR_SELECT_FIELD + "' style='label'/>"
				+ "</span>"
				+ "<p:field name='" + OPERATOR_SELECT_FIELD + "' />"
				+ "</div>"
				+ "<p:field name='" + PATTERN_GROUP_NAME + "' />"
				+ "</div>"
				+ "</form>"
				+ "</t:group>");
	}
	
	private ComparableFilterViewFormFields filterFormFields;
	private int _separateOptionDisplayThreshold;

	/**
	 * Create a new {@link ComparableFilterView}
	 *
	 */
	public ComparableFilterView(ComparableFilterConfiguration filterConfiguration, FormGroup formGroup,
			ComparableFilterViewFormFields dialogFormFields, int separateOptionDisplayTreshold) {
		super(filterConfiguration, formGroup);
		this.filterFormFields = dialogFormFields;
		_separateOptionDisplayThreshold = separateOptionDisplayTreshold;
	}
	
	/**
	 * This method returns the document pattern for the filter dialog
	 * 
	 * @return document pattern
	 */
	private Document getDialogTemplate() {
		return DIALOG_TEMPLATE;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	protected boolean internalApplyFilterSettings() {
		ComparableFilterConfiguration filterModel = getFilterModel();
		List newSelection = filterFormFields.getSelectableValuesField().getSelection();
		boolean changed =
			!CollectionUtil.containsSame(newSelection, filterModel.getFilterPattern());
		filterModel.setFilterPattern(newSelection);
		Comparable<?> newPrimaryPattern = (Comparable<?>) filterFormFields.getPrimaryFilterPatternField().getValue();
		changed |= !Utils.equals(newPrimaryPattern, filterModel.getPrimaryFilterPattern());
		filterModel
			.setPrimaryFilterPattern(newPrimaryPattern);
		Comparable<?> newSecondaryPattern =
			(Comparable<?>) filterFormFields.getSecondaryFilterPatternField().getValue();
		changed |= !Utils.equals(newSecondaryPattern, filterModel.getSecondaryFilterPattern());
		filterModel.setSecondaryFilterPattern(newSecondaryPattern);

		Operators newOperator;
		if (filterModel.getPrimaryFilterPattern() == null
			&& filterModel.getSecondaryFilterPattern() == null) {
			newOperator = filterModel.getDefaultOperator();
		} else {
			newOperator = (Operators) filterFormFields.getOperatorField().getSingleSelection();
		}

		changed |= !Utils.equals(newOperator, filterModel.getOperator());
		filterModel.setOperator(newOperator);
		return changed;
	}

	@Override
	public void resetFilterSettings() {
		setSelectionFilterSettings(getOptions(getFilterModel()), Collections.emptyList());
		setRuleBasedFilterSettings(getFilterModel().getDefaultOperator(), null, null);
	}

	private void setSelectionFilterSettings(List<?> optionValues, List<?> selectedValues) {
		filterFormFields.getSelectableValuesField().setOptions(optionValues);
		filterFormFields.getSelectableValuesField().setAsSelection(selectedValues);
	}

	private void setRuleBasedFilterSettings(Operators standardOperator, Comparable<?> primaryPattern,
			Comparable<?> secondaryPattern) {
		filterFormFields.getOperatorField().setAsSingleSelection(standardOperator);
		filterFormFields.getPrimaryFilterPatternField().setValue(primaryPattern);
		filterFormFields.getSecondaryFilterPatternField().setValue(secondaryPattern);
	}

	@Override
	protected void internalValueChanged() {
		setSelectionFilterSettings(getOptions(getFilterModel()), getFilterModel().getFilterPattern());
		setRuleBasedFilterSettings(getFilterModel().getOperator(), getFilterModel().getPrimaryFilterPattern(),
			getFilterModel().getSecondaryFilterPattern());
		requestRepaint();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeFilterContent(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, FILTER_MENU_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, DEFAULT_FILTER_CONTAINER_CLASS);
	}

	private void writeFilterContent(DisplayContext context, TagWriter out) throws IOException {
		if (shallDisplayOptionsSeparately()) {
			if (ruleBasedFilterActive()) {
				writeRuleBasedFilter(context, out);
				if (selectionBasedFilterActive()) {
					writeSelectionBasedFilter(context, out, true);
				}
			} else {
				writeSelectionBasedFilter(context, out, false);
			}
		} else {
			writeRuleBasedFilter(context, out);
			if (selectionBasedFilterActive()) {
				writeSelectionBasedFilter(context, out, true);
			}
		}
	}

	private void writeSelectionBasedFilter(DisplayContext context, TagWriter out, boolean showSelectionOnly) throws IOException {
		CheckboxMultiSelectControl checkboxMultiSelectControl = new CheckboxMultiSelectControl(filterFormFields.getSelectableValuesField(), getFilterModel());
		checkboxMultiSelectControl.setShowSelectionOnly(showSelectionOnly);
		checkboxMultiSelectControl.write(context, out);
	}

	private void writeRuleBasedFilter(DisplayContext context, TagWriter out) throws IOException {
		DefaultFormFieldControlProvider provider = new DefaultFormFieldControlProvider() {

			@Override
			public Control visitCommandField(CommandField member, Void arg) {
				return new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public Control visitFormGroup(FormGroup member, Void arg) {
				if (member.getName().equals(PATTERN_GROUP_NAME)) {
					return new FilterPatternGroupControl(filterFormFields, getFilterModel().getFilterComparator());
				} else {
					return super.visitFormGroup(member, arg);
				}
			}

		};
		FormGroupControl dialogContentControl = new FormGroupControl(getFilterGroup(),
			provider,
			DOMUtil.getFirstElementChild(getDialogTemplate().getDocumentElement()),
			RES_PREFIX);

		dialogContentControl.write(context, out);
	}

	private boolean ruleBasedFilterActive() {
		return (getFilterModel().getPrimaryFilterPattern() != null)
			|| (getFilterModel().getSecondaryFilterPattern() != null);
	}

	private boolean selectionBasedFilterActive() {
		return !getFilterModel().getFilterPattern().isEmpty();
	}

	private boolean shallDisplayOptionsSeparately() {
		return getFilterModel().showOptionEntries()
			&& _separateOptionDisplayThreshold > getFilterModel().getOptions().size();
	}
}

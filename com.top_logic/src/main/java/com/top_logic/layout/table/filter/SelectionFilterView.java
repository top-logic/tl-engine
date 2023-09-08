/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import static com.top_logic.layout.table.filter.SelectionFilterViewBuilder.*;
import static com.top_logic.layout.table.filter.SingleEmptyValueMatchCounter.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.AbstractConstantControlBase;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.SelectOptionControl;
import com.top_logic.layout.form.control.SelectionControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.OptionsListener;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.mig.html.HTMLUtil;

/**
 * {@link FilterViewControl} of {@link SelectionFilter}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class SelectionFilterView extends FilterViewControl<SelectionFilterConfiguration> {

	static final String FILTER_GROUP = StaticFilterWrapperView.class.getSimpleName();
	static final String CELL_VALUES_FIELD = "cellValues";

	static final ResPrefix RES_PREFIX = ResPrefix.GLOBAL;

	private static final String FILTER_MENU_CSS_CLASS = "fltMenu";
	private static final String DEFAULT_FILTER_CONTAINER_CLASS = "defaultFilterContainer";

	private int separateOptionDisplayThreshold;

	/**
	 * Create a new {@link SelectionFilterView}
	 */
	public SelectionFilterView(SelectionFilterConfiguration filterModel, FormGroup formGroup, int separateOptionDisplayThreshold) {
		super(filterModel, formGroup);
		this.separateOptionDisplayThreshold = separateOptionDisplayThreshold;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected boolean internalApplyFilterSettings() {
		List newSelection = getFilterOptionsField().getSelection();
		boolean changed = !CollectionUtil.containsSame(newSelection, getFilterModel().getFilterPattern());
		getFilterModel().setFilterPattern(newSelection);
		return changed;
	}

	@Override
	public void resetFilterSettings() {
		setFilterSelection(Collections.emptyList());
	}

	@Override
	protected void internalValueChanged() {
		setFilterOptions(getOptions(getFilterModel()));
		setFilterSelection(getFilterModel().getFilterPattern());
		requestRepaint();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeOptions(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, FILTER_MENU_CSS_CLASS);
		HTMLUtil.appendCSSClass(out, DEFAULT_FILTER_CONTAINER_CLASS);
	}

	private void writeOptions(DisplayContext context, TagWriter out) throws IOException {
		if (shallDisplayOptionsSeparately()) {
			new CheckboxMultiSelectControl(getFilterOptionsField(), getFilterModel()).write(context, out);
		} else {
			SelectionControl selectionControl = new SelectionControl(getFilterOptionsField());
			selectionControl.setPopupOpener(false);
			selectionControl.write(context, out);
			ErrorControl errorControl = new ErrorControl(getFilterOptionsField(), true);
			errorControl.write(context, out);
		}
	}

	private boolean shallDisplayOptionsSeparately() {
		return getFilterModel().showOptionEntries()
			&& (separateOptionDisplayThreshold >= getFilterModel().getOptions().size() ||
				separateOptionDisplayThreshold == -1);
	}

	private SelectField getFilterOptionsField() {
		return (SelectField) getFilterGroup().getFirstMemberRecursively(CELL_VALUES_FIELD);
	}

	private void setFilterSelection(List<?> selectedWrappers) {
		getFilterOptionsField().setAsSelection(selectedWrappers);
	}

	private void setFilterOptions(List<?> options) {
		getFilterOptionsField().setOptions(options);
	}

	/**
	 * Container {@link Control}, that renders a {@link SelectField} as a series of check boxes.
	 * 
	 * @author <a href=mailto:sts}@top-logic.com>Stefan Steinert</a>
	 */
	public static class CheckboxMultiSelectControl extends AbstractConstantControlBase implements OptionsListener {

		private static final String CHECKBOX_MULTI_SELECT_CONTAINER_CLASS = "checkboxMultiSelectContainer";
		private static final String CHECKBOX_MULTI_SELECT_ENTRY_CLASS = "checkboxMultiSelectEntry";
		private static final String CHECKBOX_MULTI_SELECT_NO_ENTRIES_CLASS = "checkboxMultiSelectNoEntries";

		private SelectionFilterConfiguration _filterConfiguration;
		private boolean _showSelectionOnly;

		private SelectField _selectField;

		CheckboxMultiSelectControl(SelectField selectField, SelectionFilterConfiguration filterConfiguration) {
			_selectField = selectField;
			_filterConfiguration = filterConfiguration;
		}

		@Override
		public SelectField getModel() {
			return _selectField;
		}

		@Override
		protected void internalAttach() {
			super.internalAttach();
			getSelectField().addListener(SelectField.OPTIONS_PROPERTY, this);
		}

		@Override
		protected void internalDetach() {
			getSelectField().removeListener(SelectField.OPTIONS_PROPERTY, this);
			super.internalDetach();
		}

		/**
		 * @param showSelectionOnly
		 *        true, if only selected options shall be displayed, false otherwise
		 */
		public void setShowSelectionOnly(boolean showSelectionOnly) {
			_showSelectionOnly = showSelectionOnly;
		}

		@Override
		protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
			out.beginBeginTag(DIV);
			writeControlAttributes(context, out);
			out.endBeginTag();
			writeOptions(context, out);
			out.endTag(DIV);
		}

		@Override
		protected String getTypeCssClass() {
			return CHECKBOX_MULTI_SELECT_CONTAINER_CLASS;
		}

		private void writeOptions(DisplayContext context, TagWriter out) throws IOException {
			SelectField selectField = getSelectField();
			List<?> allOptions = getOptions(selectField);
			if (!allOptions.isEmpty()) {
				writeOptionEntries(context, out, selectField, allOptions);
			} else if (!_showSelectionOnly) {
				writeNoOptionsAvailable(context, out);
			}
		}

		private List<?> getOptions(SelectField selectField) {
			if(_showSelectionOnly) {
				return SelectFieldUtils.getSelectionListSorted(selectField);
			} else {
				return SelectFieldUtils.getOptionAndSelectionOuterJoinOrdered(selectField);
			}
		}

		private void writeOptionEntries(DisplayContext context, TagWriter out, SelectField selectField,
				List<?> allOptions) throws IOException {
			for (Object option : allOptions) {
				out.beginBeginTag(PARAGRAPH);
				out.writeAttribute(CLASS_ATTR, CHECKBOX_MULTI_SELECT_ENTRY_CLASS);
				out.endBeginTag();
				{
					writeOptionCheckbox(context, out, selectField, option);
					writeOptionLabel(out, selectField, option);
					writeOptionCount(out, option);
				}
				out.endTag(PARAGRAPH);
			}
		}

		private void writeOptionCheckbox(DisplayContext context, TagWriter out, SelectField selectField, Object option)
				throws IOException {
			new SelectOptionControl(selectField, option).write(context, out);
		}

		private void writeOptionLabel(TagWriter out, SelectField selectField, Object option) {
			out.writeText(selectField.getOptionLabel(option));
		}

		private void writeOptionCount(TagWriter out, Object option) {
			MutableInteger optionValue = _filterConfiguration.getOptions().get(option);
			int optionCount;
			if (optionValue != null) {
				optionCount = optionValue.intValue();
			} else {
				if (_filterConfiguration.isOptionMatchCountable()) {
					optionCount = 0;
				} else {
					optionCount = EMPTY_VALUE;
				}
			}
			if (optionCount >= 0) {
				out.writeText(" (" + optionCount + ")");
			}
		}

		private void writeNoOptionsAvailable(DisplayContext context, TagWriter out) {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, CHECKBOX_MULTI_SELECT_NO_ENTRIES_CLASS);
			out.endBeginTag();
			out.writeText(context.getResources().getString(I18NConstants.NO_FILTER_ENTRIES));
			out.endTag(DIV);
		}

		private SelectField getSelectField() {
			return _selectField;
		}

		@Override
		public Bubble handleOptionsChanged(SelectField sender) {
			if (getSelectField() == sender) {
				requestRepaint();
			}
			return Bubble.BUBBLE;
		}

		@Override
		public boolean isVisible() {
			return getSelectField().isVisible();
		}
	}
}

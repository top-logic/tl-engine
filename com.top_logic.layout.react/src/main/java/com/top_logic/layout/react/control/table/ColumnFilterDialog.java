/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.model.SelectFieldModel;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactAlertControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactFormBuilder;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackGap;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.FilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.BoundCodec;
import com.top_logic.table.filter.ColumnFilterEditor;
import com.top_logic.table.filter.FilterField;
import com.top_logic.util.Resources;

/**
 * The body of the filter dialog of a column, composed of standard React controls around a
 * {@link ColumnFilterEditor}.
 *
 * <p>
 * A column offering several kinds of filter gets a segmented switch of the kinds above the form of
 * the active kind; switching replaces the form, while each kind keeps what was entered in it. A
 * kind the column does not {@link ColumnFilterEditor#offered(String) offer} for its current values
 * appears in the switch only while it is the active one - because the state of the column is of
 * that kind -, marked as not available, and its form is introduced by a notice saying so.
 * </p>
 *
 * <p>
 * The fields of the active kind are laid out as a form. A field holding a value of the column (a
 * bound of a range) is entered with the input the {@link FilterFieldFactory} creates for the
 * column - a date picker, a number field -, as text otherwise. A field selecting an option is a
 * row of a checkbox, the value shown as the factory shows a cell of the column, and the number of
 * rows matching it; the checkbox is named by the label of the option.
 * </p>
 */
public class ColumnFilterDialog {

	private final ReactContext _context;

	private final String _column;

	private final ColumnFilterEditor _editor;

	private final FilterFieldFactory _factory;

	private final Resources _resources;

	private final ReactStackControl _content;

	private final ReactControl _body;

	/**
	 * Creates a {@link ColumnFilterDialog}.
	 *
	 * @param context
	 *        The context the controls are created in.
	 * @param column
	 *        The name of the filtered column.
	 * @param editor
	 *        The editor of the filter of the column.
	 * @param factory
	 *        Creates the controls depending on the type of the values of the column.
	 */
	public ColumnFilterDialog(ReactContext context, String column, ColumnFilterEditor editor,
			FilterFieldFactory factory) {
		_context = context;
		_column = column;
		_editor = editor;
		_factory = factory;
		_resources = Resources.getInstance();
		_content = column(content());
		List<String> kinds = switchableKinds();
		if (kinds.size() > 1) {
			_body = column(List.of(kindSwitch(kinds), _content));
		} else {
			_body = _content;
		}
	}

	/**
	 * The control showing the dialog body.
	 */
	public ReactControl body() {
		return _body;
	}

	/**
	 * The filter state the user entered, see {@link ColumnFilterEditor#read()}.
	 */
	public FilterState read() {
		return _editor.read();
	}

	/**
	 * The editor this dialog edits.
	 */
	public ColumnFilterEditor editor() {
		return _editor;
	}

	/**
	 * The kinds the switch offers: the offered ones, and the active one, also when it is not offered.
	 */
	private List<String> switchableKinds() {
		List<String> result = new ArrayList<>();
		for (ColumnFilter<?> filter : _editor.kinds()) {
			String kind = filter.kind();
			if (_editor.offered(kind) || kind.equals(_editor.activeKind())) {
				result.add(kind);
			}
		}
		return result;
	}

	/**
	 * The segmented choice of the kind of filter.
	 *
	 * <p>
	 * The value of the model of a {@link ReactDropdownSelectControl} is the list of the selected
	 * options, a single one in a choice that is not {@link SelectFieldModel#isMultiple() multiple}.
	 * </p>
	 */
	private ReactControl kindSwitch(List<String> kinds) {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(List.of(_editor.activeKind()), kinds, false);
		model.setNullable(false);
		model.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				if (newValue instanceof List<?> selection && selection.size() == 1) {
					String kind = String.valueOf(selection.get(0));
					if (!kind.equals(_editor.activeKind())) {
						switchTo(kind);
					}
				}
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// The choice stays editable.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// A choice among the offered kinds is always valid.
			}
		});
		LabelProvider labels = value -> kindLabel(String.valueOf(value));
		ReactDropdownSelectControl select =
			new ReactDropdownSelectControl(_context, model, labels, null, false, SelectDisplay.SEGMENTED);
		ReactFormBuilder form = new ReactFormBuilder(_context);
		form.addField(_resources.getString(I18NConstants.FILTER_KIND), select);
		return form.build();
	}

	private String kindLabel(String kind) {
		for (ColumnFilter<?> filter : _editor.kinds()) {
			if (filter.kind().equals(kind)) {
				String label = _resources.getString(filter.kindLabel());
				return _editor.offered(kind) ? label
					: _resources.getString(I18NConstants.FILTER_KIND_NOT_AVAILABLE__KIND.fill(label));
			}
		}
		return kind;
	}

	/**
	 * Makes the given kind of filter the active one, showing its form.
	 */
	void switchTo(String kind) {
		_editor.setActiveKind(kind);
		List<ReactControl> previous = new ArrayList<>(_content.displayedChildren());
		_content.setChildren(content());
		// The previous form is no longer displayed; the editors behind it keep what was entered.
		for (ReactControl control : previous) {
			control.cleanupTree();
		}
	}

	/**
	 * The controls showing the active kind of filter: a notice if it is not offered, then its
	 * fields.
	 */
	private List<ReactControl> content() {
		List<ReactControl> result = new ArrayList<>();
		if (!_editor.offered(_editor.activeKind())) {
			ReactAlertControl notice = new ReactAlertControl(_context);
			notice.setMessage(_resources.getString(I18NConstants.FILTER_KIND_NOT_AVAILABLE_NOTICE));
			result.add(notice);
		}
		ReactFormBuilder form = null;
		List<ReactControl> options = null;
		for (FilterField field : _editor.fields()) {
			if (field.option() != null) {
				if (form != null) {
					result.add(form.build());
					form = null;
				}
				if (options == null) {
					options = new ArrayList<>();
				}
				options.add(optionRow(field));
			} else {
				if (options != null) {
					result.add(column(options));
					options = null;
				}
				if (form == null) {
					form = new ReactFormBuilder(_context);
				}
				addField(form, field);
			}
		}
		if (options != null) {
			result.add(column(options));
		}
		if (form != null) {
			result.add(form.build());
		}
		return result;
	}

	private void addField(ReactFormBuilder form, FilterField field) {
		ReactControl input = fieldControl(field);
		ReactFormFieldChromeControl chrome = form.addField(label(field.label()), input);
		if (input instanceof ReactCheckboxControl) {
			// A checkbox reads as its own statement ("Yes", "case sensitive"), so the label belongs
			// next to the box - above it, the two look like unrelated lines.
			chrome.setLabelPosition(LabelPosition.AFTER);
		}
	}

	/**
	 * A row selecting an option: the checkbox, named by the label of the option, the value shown as
	 * a cell of the column shows it - or the label of an option that
	 * {@link FilterField#isColumnValue() is no value of the column} -, and the number of rows
	 * matching it.
	 */
	private ReactControl optionRow(FilterField field) {
		Option option = field.option();
		ReactFormFieldChromeControl check = new ReactFormFieldChromeControl(_context, label(field.label()),
			new ReactCheckboxControl(_context, field.model()));
		check.setLabelPosition(LabelPosition.HIDDEN);
		ReactControl display = field.isColumnValue() ? _factory.valueDisplay(_context, _column, option.value()) : null;
		if (display == null) {
			display = new ReactTextControl(_context, label(option.label()));
		}
		List<ReactControl> parts = new ArrayList<>(List.of(check, display));
		if (field.count() != null) {
			parts.add(new ReactTextControl(_context, "(" + field.count() + ")"));
		}
		return new ReactStackControl(_context, StackDirection.ROW, StackGap.COMPACT, StackAlign.CENTER, false,
			parts);
	}

	/**
	 * The input of a field: the value input of the factory for a value of the column, a dropdown for
	 * a choice, a checkbox for a flag, a text input otherwise.
	 */
	private ReactControl fieldControl(FilterField field) {
		FieldModel model = field.model();
		if (field.valueCodec() != null) {
			ReactControl input = _factory.valueInput(_context, _column, model);
			if (input != null) {
				return input;
			}
			return new ReactTextInputControl(_context, textModel(model, field.valueCodec()));
		}
		if (model instanceof SelectFieldModel selectModel) {
			LabelProvider labels = field.optionLabels() != null ? field.optionLabels() : String::valueOf;
			return new ReactSelectFormFieldControl(_context, selectModel, labels);
		}
		if (model.getValue() instanceof Boolean) {
			return new ReactCheckboxControl(_context, model);
		}
		return new ReactTextInputControl(_context, model);
	}

	/**
	 * A text field model editing the typed value of the given model as the text the given codec
	 * writes and reads; a text the codec cannot read clears the value.
	 */
	private static <V> FieldModel textModel(FieldModel typed, BoundCodec<V> codec) {
		@SuppressWarnings("unchecked")
		String text = codec.format((V) typed.getValue());
		return new AbstractFieldModel(text == null ? "" : text) {
			@Override
			public void setValue(Object newValue) {
				super.setValue(newValue);
				typed.setValue(codec.parse(newValue == null ? null : newValue.toString()));
			}
		};
	}

	private ReactStackControl column(List<? extends ReactControl> children) {
		return new ReactStackControl(_context, StackDirection.COLUMN, StackGap.DEFAULT, StackAlign.STRETCH, false,
			children);
	}

	private String label(ResKey key) {
		return key == null ? "" : _resources.getString(key);
	}

}

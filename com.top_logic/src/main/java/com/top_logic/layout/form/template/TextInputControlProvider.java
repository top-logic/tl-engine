/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.StringField;

/**
 * Configurable {@link ControlProvider} that creates {@link TextInputControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class TextInputControlProvider extends DefaultFormFieldControlProvider {

	/** Configuration options for {@link TextInputControlProvider}. */
	public interface Config extends PolymorphicConfiguration<ControlProvider> {
		/** The number of columns of the input field. */
		@Name(COLUMNS_PROPERTY)
		Integer getColumns();

		/** The CSS style of the input element. */
		@Name(INPUT_STYLE_PROPERTY)
		String getInputStyle();

		/** The maximum number of characters shown when the field is displayed in view mode. */
		@Name(MAX_LENGTH_SHOWN_PROPERTY)
		Integer getMaxLengthShown();

		/** Whether a multi-line input is displayed. */
		@Name(MULTI_LINE_PROPERTY)
		Boolean getMultiLine();

		/** The number of rows of a multi-line input. */
		@Name(ROWS_PROPERTY)
		Integer getRows();

		/** The style of the created control. */
		@Name(STYLE_PROPERTY)
		String getStyle();

		/** The tab index of the input field. */
		@Name(TAB_INDEX_PROPERTY)
		Integer getTabIndex();

		/** The type of the input field. */
		@Name(TYPE_PROPERTY)
		String getType();
	}

	private static final String COLUMNS_PROPERTY = "columns";

	private static final String INPUT_STYLE_PROPERTY = "inputStyle";

	private static final String MAX_LENGTH_SHOWN_PROPERTY = "maxLengthShown";

	private static final String MULTI_LINE_PROPERTY = "multiLine";

	private static final String ROWS_PROPERTY = "rows";

	private static final String STYLE_PROPERTY = "style";

	private static final String TAB_INDEX_PROPERTY = "tabIndex";

	private static final String TYPE_PROPERTY = "type";

	private int _columns = -1;

	private String _inputStyle;

	private int _maxLengthShown = -1;

	private boolean _multiLine = false;

	private int _rows = -1;

	private String _style;

	private int _tabIndex = -1;

	private String _type;

	/**
	 * Create a {@link TextInputControlProvider}.
	 * 
	 * @param config
	 *        Configuration of control attributes.
	 */
	public TextInputControlProvider(InstantiationContext context, Config config) {
		_columns = (config.getColumns() == null) ? _columns : config.getColumns();
		_inputStyle = (config.getInputStyle().isEmpty()) ? _inputStyle : config.getInputStyle();
		_maxLengthShown = (config.getMaxLengthShown() == null) ? _maxLengthShown : config.getMaxLengthShown();
		_multiLine = (config.getMultiLine() == null) ? _multiLine : config.getMultiLine();
		_rows = (config.getRows() == null) ? _rows : config.getRows();
		_style = (config.getStyle().isEmpty()) ? _style : config.getStyle();
		_tabIndex = (config.getTabIndex() == null) ? _tabIndex : config.getTabIndex();
		_type = (config.getType().isEmpty()) ? _type : config.getType();
	}

	public TextInputControlProvider() {
		// Nothing to do
	}

	@Override
	public Control visitStringField(StringField member, Void arg) {
		TextInputControl text = new TextInputControl(member);
		if (_columns >= 0) {
			text.setColumns(_columns);
		}
		text.setInputStyle(_inputStyle);
		text.setMaxLengthShown(_maxLengthShown);
		text.setMultiLine(_multiLine);
		if (_rows >= 0) {
			text.setRows(_rows);
		}
		text.setStyle(_style);
		if (_tabIndex >= 0) {
			text.setTabIndex(_tabIndex);
		}
		text.setType(_type);
		return text;
	}

	/** The number of columns of the created input. */
	public int getColumns() {
		return _columns;
	}

	/** Setter for {@link #getColumns()}. */
	public void setColumns(int columns) {
		_columns = columns;
	}

	/** The CSS style of the created input element. */
	public String getInputStyle() {
		return _inputStyle;
	}

	/** Setter for {@link #getInputStyle()}. */
	public void setInputStyle(String inputStyle) {
		_inputStyle = inputStyle;
	}

	/** The maximum number of characters shown in a view mode display. */
	public int getMaxLengthShown() {
		return _maxLengthShown;
	}

	/** Setter for {@link #getMaxLengthShown()}. */
	public void setMaxLengthShown(int maxLengthShown) {
		_maxLengthShown = maxLengthShown;
	}

	/** Whether a multi-line input is created. */
	public boolean isMultiLine() {
		return _multiLine;
	}

	/** Setter for {@link #isMultiLine()}. */
	public void setMultiLine(boolean multiLine) {
		_multiLine = multiLine;
	}

	/** The number of rows of a multi-line input. */
	public int getRows() {
		return _rows;
	}

	/** Setter for {@link #getRows()}. */
	public void setRows(int rows) {
		_rows = rows;
	}

	/** The style of the created control. */
	public String getStyle() {
		return _style;
	}

	/** Setter for {@link #getStyle()}. */
	public void setStyle(String style) {
		_style = style;
	}

	/** The tab index of the created input. */
	public int getTabIndex() {
		return _tabIndex;
	}

	/** Setter for {@link #getTabIndex()}. */
	public void setTabIndex(int tabIndex) {
		_tabIndex = tabIndex;
	}

	/** The type of the created input. */
	public String getType() {
		return _type;
	}

	/** Setter for {@link #getType()}. */
	public void setType(String type) {
		_type = type;
	}

}
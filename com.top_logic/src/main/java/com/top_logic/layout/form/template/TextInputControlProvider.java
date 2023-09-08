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

	public interface Config extends PolymorphicConfiguration<ControlProvider> {
		@Name(COLUMNS_PROPERTY)
		Integer getColumns();

		@Name(INPUT_STYLE_PROPERTY)
		String getInputStyle();

		@Name(MAX_LENGTH_SHOWN_PROPERTY)
		Integer getMaxLengthShown();

		@Name(MULTI_LINE_PROPERTY)
		Boolean getMultiLine();

		@Name(ROWS_PROPERTY)
		Integer getRows();

		@Name(STYLE_PROPERTY)
		String getStyle();

		@Name(TAB_INDEX_PROPERTY)
		Integer getTabIndex();

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

	public int getColumns() {
		return _columns;
	}

	public void setColumns(int columns) {
		_columns = columns;
	}

	public String getInputStyle() {
		return _inputStyle;
	}

	public void setInputStyle(String inputStyle) {
		_inputStyle = inputStyle;
	}

	public int getMaxLengthShown() {
		return _maxLengthShown;
	}

	public void setMaxLengthShown(int maxLengthShown) {
		_maxLengthShown = maxLengthShown;
	}

	public boolean isMultiLine() {
		return _multiLine;
	}

	public void setMultiLine(boolean multiLine) {
		_multiLine = multiLine;
	}

	public int getRows() {
		return _rows;
	}

	public void setRows(int rows) {
		_rows = rows;
	}

	public String getStyle() {
		return _style;
	}

	public void setStyle(String style) {
		_style = style;
	}

	public int getTabIndex() {
		return _tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		_tabIndex = tabIndex;
	}

	public String getType() {
		return _type;
	}

	public void setType(String type) {
		_type = type;
	}

}
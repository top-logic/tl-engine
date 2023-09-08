/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.component.export;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.ppt.SlideReplacement;
import com.top_logic.base.office.ppt.StyledValue;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.format.ColorConfigFormat;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.tool.export.AbstractOfficeExportHandler.OfficeExportValueHolder;
import com.top_logic.tool.export.ExportUtil;
import com.top_logic.util.Resources;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class SlideReplacerBuilder implements ConfiguredInstance<SlideReplacerBuilder.Config> {

	/**
	 * Enum to differ between even and odd rows
	 */
	public enum EvenOdd {
		/**
		 * <code>EVEN</code> rows where index mod 2 == 0
		 */
		EVEN,
		/**
		 * <code>ODD</code> rows where index mod 2 == 1
		 */
		ODD
	}

	private enum RowData {
		CONTENT, MODEL, INDEX
	}

	/**
	 * Use this for custom translations of header-values
	 */
	public interface HeaderTranslator {

		/**
		 * @param columnName
		 *        the name of the column
		 * @return the translated header of the column-key
		 */
		public String translate(String columnName);
	}

	/**
	 * Default {@link HeaderTranslator}.
	 */
	public static class DefaultHeaderTranslator implements HeaderTranslator {

		/** Default instance of this class. */
		public static final DefaultHeaderTranslator INSTANCE = new DefaultHeaderTranslator(ResPrefix.GLOBAL);

		private final ResPrefix _resPrefix;

		/**
		 * Creates a new {@link DefaultHeaderTranslator}.
		 */
		public DefaultHeaderTranslator(ResPrefix resPrefix) {
			_resPrefix = resPrefix;
		}

		@Override
		public String translate(String columnName) {
			ResKey key;
			if (INDEX_COLUMN.equals(columnName)) {
				key = I18NConstants.INDEX_COLUMN;
			} else {
				key = _resPrefix.key(columnName);
			}
			return Resources.getInstance().getString(key);
		}
	}

	/**
	 * Class to cut text and insert line breaks so that height of a row can be computed.
	 */
	public interface TextCutter {

		/**
		 * Cuts the given cell value and inserts line breaks, so that the text fits into the column
		 * with the given name
		 * 
		 * @param text
		 *        the text to cut / line break
		 * @param column
		 *        the index of the column the given text shall fit into
		 * @param maxRows
		 *        the maximum row count that fits into a slide
		 * @return the given text with inserted line breaks so that it fits into the column with the
		 *         given name
		 */
		public String cutText(String text, int column, int maxRows);

	}

	/**
	 * {@link TextCutter} which doesn't cut anything.
	 */
	public static class NoTextCutter implements TextCutter, TextCutterFactory {

		/** Default instance of this class. */
		public static final NoTextCutter INSTANCE = new NoTextCutter();

		@Override
		public String cutText(String text, int column, int maxRows) {
			return text;
		}

		@Override
		public TextCutter createTextCutter(List<String> columns) {
			return this;
		}

	}

	/**
	 * Abstract {@link TextCutter} implementation.
	 */
	public static abstract class AbstractTextCutter implements TextCutter {

		/**
		 * Gets the maximum chars for the {@link ExportUtil#cutText(String, int, int, CharSizeMap)}
		 * method for the given column.
		 */
		public abstract int getMaxChars(int column);

		/**
		 * Gets the {@link CharSizeMap} to use for cutting text.
		 */
		public abstract CharSizeMap getCharSizeMap();

		@Override
		public String cutText(String text, int column, int maxRows) {
			return ExportUtil.cutText(text, maxRows, getMaxChars(column), getCharSizeMap());
		}

	}

	/**
	 * Config-interface for {@link SlideReplacerBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<SlideReplacerBuilder> {

		/**
		 * the template-file for the slide-replacement
		 */
		public String getTemplate();

		/**
		 * See {@link #getTemplate()}
		 */
		public void setTemplate(String path);

		/**
		 * the names of the columns in the exported table
		 */
		@Format(CommaSeparatedStrings.class)
		public List<String> getColumns();

		/**
		 * See {@link #getColumns()}
		 */
		public void setColumns(List<String> columns);

		/**
		 * the max number of rows in a table per slide
		 */
		@IntDefault(17)
		public int getMaxRows();

		/**
		 * true if the table has a header-row
		 */
		@BooleanDefault(true)
		@Name("headline")
		public boolean getHasHeadline();

		/**
		 * the key to identify the table in the template
		 */
		@StringDefault("FIXEDTABLE_DATA")
		public String getTableKey();

		/**
		 * the background color of even rows
		 */
		@Format(ColorConfigFormat.class)
		public Color getColorEvenRow();

		/**
		 * the background color of odd rows
		 */
		@Format(ColorConfigFormat.class)
		public Color getColorOddRow();

		/**
		 * the text color for rows
		 */
		@Format(ColorConfigFormat.class)
		public Color getTextColor();

	}

	/**
	 * <code>INDEX_COLUMN</code>: key for column used to visualize the row-index
	 */
	public static final String INDEX_COLUMN = "_index";

	private final String[] _columns;

	@SuppressWarnings("rawtypes")
	private Accessor _accessor;

	private LabelProvider _labelProvider;

	private HeaderTranslator _translator;

	private TextCutter _textCutter;

	private final Config _config;

	private Map<String, Object> _initialContext;

	/**
	 * Config-Constructor for {@link SlideReplacerBuilder}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public SlideReplacerBuilder(InstantiationContext context, Config config) {
		_config = config;
		_columns = ArrayUtil.toStringArray(config.getColumns());
		_accessor = WrapperAccessor.INSTANCE;
		_labelProvider = MetaLabelProvider.INSTANCE;
		_translator = DefaultHeaderTranslator.INSTANCE;
		_textCutter = NoTextCutter.INSTANCE;
	}

	@Override
	public Config getConfig() {
		return _config;
	}
	
	/**
	 * @param accessor
	 *        sets the accessor that provides the cell values
	 */
	@SuppressWarnings("rawtypes")
	public void setAccessor(Accessor accessor) {
		_accessor = accessor;
	}

	/**
	 * Sets the label provider to use for exporting cell values.
	 */
	public void setLabelProvider(LabelProvider labelProvider) {
		_labelProvider = labelProvider;
	}

	/**
	 * Sets the text cutter to use.
	 */
	public void setTextCutter(TextCutter textCutter) {
		_textCutter = textCutter;
	}

	/**
	 * Sets initial values that are added to all {@link OfficeExportValueHolder}s
	 */
	public void initContext(Map<String, Object> map) {
		_initialContext = map;
	}

	/**
	 * @param objects
	 *        the objects to create a {@link SlideReplacement} for
	 * @return a new SlideReplacment for the given values using the configured template
	 */
	public SlideReplacement create(List<Object> objects) {
		List<OfficeExportValueHolder> values = createExportValueHolder(objects);
		return new SlideReplacement(getConfig().getTemplate(), values);
	}

	private List<OfficeExportValueHolder> createExportValueHolder(List<Object> objects) {
		List<OfficeExportValueHolder> holderList = new ArrayList<>();
		List<Map<RowData, Object>> list = prepareRows(objects);
		int maxRows = getConfig().getMaxRows();
		List<Object[]> pageList = new ArrayList<>(maxRows);
		double heigthCounter = 0;
		for (int i = 0, length = list.size(); i < length; i++) {
			Object[] row = (Object[]) list.get(i).get(RowData.CONTENT);
			double rowCount = computeRowCount(row);
			heigthCounter += rowCount;
			if (heigthCounter > maxRows && !pageList.isEmpty()) {
				Map<String, Object> exportValues = exportContext();
				exportValues.put(getConfig().getTableKey(), ArrayUtil.toArray(pageList));
				holderList.add(new OfficeExportValueHolder(null, null, exportValues, false));
				pageList = new ArrayList<>(maxRows);
				heigthCounter = rowCount;
			}
			if (getConfig().getHasHeadline() && pageList.isEmpty()) {
				pageList.add(createHeaderRow());
			}
			pageList.add(row);
		}
		if (!pageList.isEmpty()) {
			Map<String, Object> exportValues = exportContext();
			exportValues.put(getConfig().getTableKey(), ArrayUtil.toArray(pageList));
			holderList.add(new OfficeExportValueHolder(null, null, exportValues, false));
		}
		return holderList;
	}

	private double computeRowCount(Object[] row) {
		double rowCount = 1.0;
		for (int i = 0; i < row.length; i++) {
			int numberOfRowsInColumn = countRows(row[i]);
			// first row has full height, each further row is only 62% of first rows height
			double rowCountForColumn = 1.0 + ((numberOfRowsInColumn - 1) * 0.62);
			rowCount = Math.max(rowCount, rowCountForColumn);
		}
		return rowCount;
	}

	private int countRows(Object cellValue) {
		if (cellValue instanceof StyledValue) {
			cellValue = ((StyledValue) cellValue).getValue();
		}
		String string = StringServices.toString(cellValue);
		return StringServices.count(string, StringServices.LINE_BREAK) + 1;
	}

	private Map<String, Object> exportContext() {
		Map<String, Object> result = new HashMap<>();
		if (_initialContext != null) {
			result.putAll(_initialContext);
		}
		return result;
	}

	private List<Map<RowData, Object>> prepareRows(List<Object> objects) {
		List<Map<RowData, Object>> result = new ArrayList<>(objects.size());
		int row = 0;
		for (Object obj : objects) {
			EvenOdd evenOdd = (row % 2 == 0) ? EvenOdd.EVEN : EvenOdd.ODD;
			Map<RowData, Object> rowMap = new HashMap<>();
			Object[] values = createRowArray(row, obj, evenOdd);
			rowMap.put(RowData.INDEX, row);
			rowMap.put(RowData.MODEL, obj);
			rowMap.put(RowData.CONTENT, values);
			result.add(rowMap);
			row++;
		}

		return result;
	}

	private Object[] createHeaderRow() {
		Object[] values = new Object[_columns.length];
		for (int col = 0; col < _columns.length; col++) {
			String string = _columns[col];
			values[col] = translateHeader(string);
		}
		cleanup(values);
		return values;
	}

	private void cleanup(Object[] values) {
		ExportUtil.cleanupTableRowForPOI(values);
	}

	private String translateHeader(String string) {
		return _translator.translate(string);
	}

	/**
	 * @param translator
	 *        sets the translator for the column-header
	 */
	public void setHeaderTranslator(HeaderTranslator translator) {
		if (translator == null) {
			translator = DefaultHeaderTranslator.INSTANCE;
		}
		_translator = translator;
	}

	private Object[] createRowArray(int row, Object obj, EvenOdd evenOdd) {
		Object[] values = new Object[_columns.length];
		for (int col = 0; col < _columns.length; col++) {
			values[col] = createValue(evenOdd, row, col, _columns[col], obj);
		}
		cleanup(values);
		return values;
	}

	private Color getBackgroundColor(EvenOdd evenOdd) {
		switch (evenOdd) {
		case EVEN:
			return getConfig().getColorEvenRow();
		case ODD:
			return getConfig().getColorOddRow();
		default:
			throw new UnsupportedOperationException();
		}
	}

	@SuppressWarnings("unchecked")
	private Object createValue(EvenOdd evenOdd, int row, int col, String columnName, Object obj) {
		String value = null;
		if (INDEX_COLUMN.equals(columnName)) {
			value = String.valueOf(row + 1);
		} else {
			value = _labelProvider.getLabel(_accessor.getValue(obj, columnName));
		}
		value = _textCutter.cutText(value, col, getConfig().getMaxRows());

		StyledValue result = new StyledValue();
		result.setValue(cleanupValue(value));
		result.setBackgroundColor(getBackgroundColor(evenOdd));
		result.setTextColor(getConfig().getTextColor());
		result.setBold(Decision.FALSE);
		return result;
	}

	private Object cleanupValue(Object value) {
		return StringServices.isEmpty(value) ? ExportUtil.EXPORT_EMPTY_CELL_VALUE : value;
	}

	/**
	 * Factory method to create an initialized {@link SlideReplacerBuilder}.
	 * 
	 * @return a new SlideReplacerBuilder.
	 */
	public static SlideReplacerBuilder instance(String template, List<String> cols) {
		return instance(TypedConfiguration.newConfigItem(Config.class), template, cols);
	}

	/**
	 * Factory method to create an initialized {@link SlideReplacerBuilder}.
	 * 
	 * @return a new SlideReplacerBuilder.
	 */
	public static SlideReplacerBuilder instance(Config config, String template, List<String> cols) {
		config.setTemplate(template);
		config.setColumns(cols);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

}
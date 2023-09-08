/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.util.error.TopLogicException;

/**
 * This class is responsible for providing the configuration for a POI
 * template.
 * 
 * <p>
 * Each cell may contain a template string which has the following format
 * <pre>
 * &lt;% LABEL_NAME (key:value[;])* %&gt;
 * 
 * Example:
 * 
 * &lt;% QUICK_BROWN_FOX_JUMPED OVER THE LAZY DOG %&gt;
 * &lt;% A_MORE_COMPLEX_ONE valign:top %&gt;
 * &lt;% ONE_MORE valign:top; halign:center %&gt;
 * 
 * </pre>
 * </p>
 * 
 * @author <a href="mailto:wta@top-logic.com">wta</a>
 */
public class POIExcelTemplate {

	/**
	 * Configuration used to parse the Templates.
	 * 
	 *
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * Maximal number of rows to be contained in the {@link POIExcelTemplate} without writing a
		 * warning in the logs when parsing the Excel-sheet.
		 */
		@LongDefault(300)
		long getMaxRowsWithoutWarning();

		/**
		 * Maximal number of rows to be contained in the {@link POIExcelTemplate} without writing a
		 * warning in the logs when parsing the Excel-sheet.
		 */
		@LongDefault(100)
		long getMaxColumnsWithoutWarning();

		/**
		 * Defines if a warning should be generated for each row which has to many columns.
		 * 
		 * @return {@code true} if there should be a warning for each row which has to many columns.
		 *         {@code false} if there should be only one warning for the column which has the
		 *         most columns.
		 * 
		 */
		@BooleanDefault(false)
		boolean isShowAllColumnWarnings();

	}

	/**
     * This enumeration defines all possible options which can be specified for
     * vertical alignments.
     * 
     * @author <a href=mailto:wta@top-logic.com>wta</a>
     */
    public static enum VerticalAlign {

		/**
		 * Enumeration literal for aligning cell contents to the top.
		 */
        TOP,

		/**
		 * Enumeration literal for aligning cell contents to the center.
		 */
        CENTER,

		/**
		 * Enumeration literal for aligning cell contents to the bottom.
		 */
        BOTTOM
    }
    
    /**
     * This enumeration defines all possible options which can be specified for
     * horizontal alignments.
     * 
     * @author <a href=mailto:wta@top-logic.com>wta</a>
     */
    public static enum HorizontalAlign {

		/**
		 * Enumeration literal for aligning cell contents to the left.
		 */
        LEFT,

		/**
		 * Enumeration literal for aligning cell contents to the center.
		 */
        CENTER,

		/**
		 * Enumeration literal for aligning cell contents to the right.
		 */
        RIGHT
    }
    
    /**
     * Instances of this class represent a single template entry in a POI
     * document.
     * 
     * @author <a href=mailto:wta@top-logic.com>wta</a>
     */
    public static final class POITemplateEntry {
        
        /**
         * The constant defining the "rows" attribute.
         */
        public static final String ATTRIBUTE_ROWS = "rows";

		/**
		 * The constant defining the "shiftrows" attribute.
		 */
		public static final String ATTRIBUTE_SHIFT_ROWS = "shiftrows";
        
        /**
         * The constant defining the "cols" attribute.
         */
        public static final String ATTRIBUTE_COLS = "cols";

        /**
         * The constant defining the boolean "header" attribute which indicates
         * if the table header is visible or not.
         */
        public static final String ATTRIBUTE_HEADER = "header";

        /**
         * The constant defining the boolean "replace" attribute which indicates
         * if only the template should be replaced.
         */
        public static final String ATTRIBUTE_REPLACE = "replace";

        /**
         * The constant defining the integer attribute which indicates an
         * image's width in columns.
         */
        public static final String ATTRIBUTE_WIDTH = "width";
        
        /**
         * The constant defining the integer attribute which indicates an
         * image's height in columns.
         */
        public static final String ATTRIBUTE_HEIGHT = "height";

        /**
         * The constant defining the enumeration (of the type VerticalAlign)
         * attribute which specifies the vertical alignment.
         */
        public static final String ATTRIBUTE_V_ALIGN = "valign";
        
        /**
         * The constant defining the enumeration (of the type HorizontalAlign)
         * attribute which specifies the horizontal alignment.
         */
        public static final String ATTRIBUTE_H_ALIGN = "halign";
        
        /**
         * The constant defining the integer attribute specifying the 
         * vertical indentation in pixels.
         */
        public static final String ATTRIBUTE_V_INDENT = "vindent";
        
        /**
         * The constant defining the integer attribute specifying the 
         * horizontal indentation in pixels.
         */
        public static final String ATTRIBUTE_H_INDENT = "hindent";
        
        /**
         * The constant defining the boolean attribute indicating if the images
         * have to be automagically resized in order to fit the bounding area.
         */
        public static final String ATTRIBUTE_AUTOFIT = "autofit";

        /**
         * The constant defining the integer attribute indicating an offset.
         * The interpretation of this attribute depends on the functionality.
         */
        public static final String ATTRIBUTE_OFFSET = "offset";

        /**
         * The constant defining the boolean attribute indicating if
         * excel groups are to be created when exporting trees.
         */
		public static final String ATTRIBUTE_AUTOGROUP = "autogroup";

		/**
		 * The constant defining the boolean attribute indicating if 
		 * grouped rows or columns are to be collapsed or not.
		 */
		public static final String ATTRIBUTE_AUTOCOLLAPSE = "autocollapse";
		
		/**
		 * The constant defining the String attribute defining the template
		 * this one actually refers to.
		 */
		public static final String ATTRIBUTE_TEMPLATE = "template";

		/**
		 * The constant defining the boolean attribute indicating if the root
		 * node is to be exported or not.
		 */
		public static final String ATTRIBUTE_ROOT = "root";

		/**
		 * The constant defining the integer attribute indicating the maximum
		 * depth of the tree to be exported.
		 * 
		 * <p>
		 * Specify -1 to export the entire tree.
		 * </p>
		 */
		public static final String ATTRIBUTE_MAX_DEPTH = "maxDepth";

		/**
		 * The constant defining the infinite depth.
		 */
		public static final int INFINITE_DEPTH = -1;
        
        /**
		 * The {@link Cell}, the receiver has been parsed from.
         */
		private final Cell _cell;
        
        /**
         * The map of attribute keys to the appropriate value.
         */
		private final Map<String, String> _properties = new HashMap<>();
        
        /**
         * Creates an instance of this class for the specified cell.
         * 
		 * @param cell
		 *        the {@link Cell} to create a new template entry for
         */
		public POITemplateEntry(final Cell cell) {
			_cell = cell;
        }
        
        /**
		 * Creates an instance of this class for the specified cell using the specified attributes.
         * <p>
		 * The attribute string has to have the following format: (key:value[;])*
         * </p>
         * 
		 * @param cell
		 *        the {@link Cell} to create a template for
		 * @param attributes
		 *        the template attributes or {@code null}
         */
		public POITemplateEntry(final Cell cell, final String attributes) {
			_cell = cell;

			parse(attributes);
        }
        
        /**
		 * Adds the specified key - value pair to the receiver's collection of properties.
         * 
		 * @param key
         *            the key to add the value for
		 * @param value
         *            the value to be added for the specified property
         */
		public void addProperty(final String key, final String value) {
			_properties.put(key, value);
        }
        
        @Override
        public String toString() {
			return getClass().getSimpleName() + " " + _properties;
        }
        
        /**
		 * the {@link Cell} the receiver has been created for
         */
        public Cell getCell() {
			return _cell;
        }
        
        /**
         * Returns the boolean property value for the specified key.
         * 
		 * @param key
         *            the key to return the boolean property value for
		 * @param defaultValue
		 *        the default value to be returned when the property has not been set yet
		 * @return the property value for the specified key or {@code null}
         */
		public boolean getBoolean(final String key, final boolean defaultValue) {
			final String value = _properties.get(key);

			return value == null ? defaultValue : Boolean.parseBoolean(value);
        }
        
        /**
         * Returns the integer property value for the specified key.
         * 
		 * @param key
         *            the key to return the integer property value for
		 * @param defaultValue
		 *        the default value to be returned when the property has not been set yet
		 * @return the property value for the specified key or {@code null}
         */
		public int getInteger(final String key, final int defaultValue) {
			final String value = _properties.get(key);

			return value == null ? defaultValue : Integer.parseInt(value);
        }
        
        /**
         * Returns the string property value for the specified key.
         * 
		 * @param key
         *            the key to return the string property value for
		 * @param defaultValue
		 *        the default value to be returned when the property has not been set yet
		 * @return the property value for the specified key or {@code null}
         */
		public String getString(final String key, final String defaultValue) {
			final String value = _properties.get(key);

			return value == null ? defaultValue : value;
        }
        
        /**
         * Returns the enumeration property value for the specified key.
         * 
		 * @param key
         *            the key to return the enumeration property value for
		 * @param defaultValue
		 *        the default value to be returned when the property has not been set yet
		 * @return the property value for the specified key or {@code null}
         */
		public <T extends Enum<T>> T getEnum(final String key, final Class<T> clazz, final T defaultValue) {
			final String value = _properties.get(key);

			return value == null ? defaultValue : Enum.valueOf(clazz, value);
        }
        
        /**
         * Remove the specified property from the receiver's collection.
         * 
		 * @param key
         *            the name of the property to be removed
         */
		public void removeProperty(final String key) {
			_properties.remove(key);
        }
        
		/**
		 * Translates the receiver by shifting it by the specified number of rows and columns.
		 * 
		 * @param rows
		 *            the number of rows to shift
		 * @param columns
		 *            the number of columns to shift
		 * @return the translated copy
		 * @throws IllegalArgumentException
		 *         if the receiver cannot be translated using the specified values
		 */
		public POITemplateEntry translate(final int rows, final int columns) throws IllegalArgumentException {
			final int targetRowIndex = _cell.getRowIndex() + rows;
			final int targetColIndex = _cell.getColumnIndex() + columns;
        	
			if (targetRowIndex < 0 || targetColIndex < 0) {
				throw new IllegalArgumentException("Cannot shift to " + targetRowIndex + ", " + targetColIndex);
        	}
        	
        	// now resolve the appropriate cell.
			final Sheet sheet = _cell.getSheet();
			final Row targetRow = POIExcelUtil.createRow(sheet, targetRowIndex);
			final Cell targetCell = POIExcelUtil.createCell(targetRow, targetColIndex, _cell.getCellStyle(), false);
        	
        	// create a new template entry and copy the attributes.
			final POITemplateEntry translatedTemplate = new POITemplateEntry(targetCell);
			POIExcelUtil.copyTemplate(this, translatedTemplate);
        	
			return translatedTemplate;
        	
        }
        
		/**
		 * Resolve the receiver's attributes in case the receiver links to another template.
		 * 
		 * @param template
		 *            the template to retrieve the linked templates from
		 */
		public void resolve(final POIExcelTemplate template) {
			final String label = getString(ATTRIBUTE_TEMPLATE, null);

			if (label != null) {
				final POITemplateEntry source = template.getEntry(label);
        		
        		// now transfer the excel cells as well.
				POIExcelUtil.copyTemplate(source, this);
        	}
        }

		/**
		 * Copies the properties of the specified template. The previous properties are discarded
		 * completely.
		 * 
		 * @param source
		 *            the template to retrieve the new properties from
		 */
		void copyProperties(final POITemplateEntry source) {
        	// remove all properties before setting the new ones.
			_properties.clear();
        	
        	// now add the specified map to the receiver's properties map
			_properties.putAll(source._properties);
        }
        
        /**
         * Parses the specified string and extract all defined properties.
         * 
         * The string has to have the following format:
		 * 
         * <pre>
         * (key:value[;])*
		 * 
         * <pre>
         * 
		 * @param properties the properties to parse or
		 * {@code null}
         */
		private void parse(final String properties) {
			if (StringServices.isEmpty(properties)) {
                return;
            }
            
            // first of all, split the declarations
			final StringTokenizer tokenizer = new StringTokenizer(properties, ";");
            
            // now go through all tokens and extract the key-value pairs
			while (tokenizer.hasMoreTokens()) {
				final String property = tokenizer.nextToken();
                
                // this one has the following format:
                // <key>:<value>
                // Make sure to trim the white spaces!
				final int separatorIndex = property.indexOf(':');
				if (separatorIndex > -1 && separatorIndex + 1 < property.length()) {
					final String key = property.substring(0, separatorIndex).trim();
					final String value = property.substring(separatorIndex + 1).trim();
                    
                    // neither the key nor the value must be an empty string
                    // (or whitespace)!
					if (!StringServices.isEmpty(key) && !StringServices.isEmpty(value)) {
						_properties.put(key, value);
                    } else {
						throw new TopLogicException(POITemplateEntry.class, "Invalid template attribute entry: "
							+ property);
                    }
                    
                } else {
					throw new TopLogicException(POITemplateEntry.class, "Invalid template attribute entry: "
						+ property + " (in: " + properties + ')');
                }
            }
        }
    }
    
    /**
     * The pattern to be used for finding template definitions in a cell's
     * content containing a valid label.
     * The label can be retrieved by using the group at index 1.
     * The attributes can be retrieved by using the group at index 2;
     */
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("<%\\s*(\\w+)(\\s+.*)?%>");
    
    /**
	 * Replaces the template string found in the specified content string with the specified target
	 * value.
     * 
	 * @param content
     *            the string to replace the template definition in
	 * @param value
     *            the value to replace the template definition with
     * @return the resulting string
     */
	public static String replaceTemplate(final String content, final String value) {
		final Matcher matcher = TEMPLATE_PATTERN.matcher(content);

		if (matcher.find()) {
			final String template = matcher.group(0);

			return content.replace(template, value);
        }
        
		return content;
    }
    
    /**
     * This is the actual monster which will contain the mapping of
     * label names to their parameter maps.
     */
	private final Map<String, POITemplateEntry> _templates = new HashMap<>();

	/**
	 * Configuration for the {@link POIExcelTemplate}.
	 */
	private final Config _config;
    
    /**
	 * Creates an instance of this class for the specified sheet. The specified template is parsed
	 * immediately for all template definitions which can be later on accessed by the appropriate
	 * methods.
	 * 
	 * @param sheet
	 *        the {@link Sheet} to create a new template for
	 */
    public POIExcelTemplate(final Sheet sheet) {
		_config = ApplicationConfig.getInstance().getConfig(Config.class);
        parse(sheet);
    }
    
    /**
     * Returns the template definition for the specified label.
     * 
	 * @param label
     *            the label to retrieve the template definition for
	 * @return the template definition for the specified label or {@code null} if no definition has
	 *         been found
     */
	public POITemplateEntry getEntry(final String label) {
		return _templates.get(label);
    }

    /**
     * Parse the specified sheet and extract all template definitions.
     * 
	 * @param sheet
	 *        a {@link Sheet} to extract template definitions from
     */
	private void parse(final Sheet sheet) {
        // the algorithm is pretty straight forward:
        // 1. for each existing cell read the contents
        // 2. find the first occurrence of the "<% " String
        // 3. extract the label name
        // 4. create a new template entry and extract all attributes
        // 5. remove the template string from the cell

		final Config config = getConfig();
		final long maxRowsWithoutWarning = config.getMaxRowsWithoutWarning();
		final long maxColsWithoutWarning = config.getMaxColumnsWithoutWarning();

		// Map that contains the rows which have to many columns. The format of the map is: <number
		// of columns of the row, Row>
		final Map<Long, Row> rowsWithToManyColumns = new HashMap<>();
		
		// Counter for the number of rows that are present in the current sheet.
		long countRows = 0;
		for (final Row row : sheet) {
			countRows++;

			// Counter for the number of columns that are in the current row.
			long countCols = 0;

			for (Cell cell : row) {
				countCols++;
				final Object value = POIExcelUtil.getCellValue(cell);
                
                // ignore empty cells
				if (value != null) {
					final String stringValue = String.valueOf(value);
					final Matcher matcher = TEMPLATE_PATTERN.matcher(stringValue);
                    
                    // yup, found a proper template definition here!
                    // extract the label name and attributes
					if (matcher.find()) {
						final String template = matcher.group(0);
						final String label = matcher.group(1);
						final String attrs = matcher.group(2);
                        
                        // create a new template entry
						final POITemplateEntry entry = new POITemplateEntry(cell, attrs);
                        
                        // now add the new entry to the receiver's entry collection
						_templates.put(label, entry);
                        
                        // check if we have to replace the template.
                        // if so, we must NOT remove the template definition
						final boolean replace = entry.getBoolean(POITemplateEntry.ATTRIBUTE_REPLACE, false);
                        if(!replace) {
							cell.setCellValue(stringValue.replace(template, ""));
                        }
                    }
                }
            }

			if (countCols > maxColsWithoutWarning) {
				rowsWithToManyColumns.put(Long.valueOf(countCols), row);
			}
        }

		logWarningForTooManyColumns(sheet, config.isShowAllColumnWarnings(), maxColsWithoutWarning,
			rowsWithToManyColumns);

		logWarningForToManyRows(sheet, maxRowsWithoutWarning, countRows);
	}

	/**
	 * Log a warning if the sheet {@code aSheet} contains more rows than the expected quantity
	 * specified with the parameter {@code maxRowsWithoutWarning}.
	 * 
	 * @param sheet
	 *        the sheet for which to write a warning in the logs
	 * @param maxRowsWithoutWarning
	 *        the maximum number of rows that the sheet can contain without generating a warning in
	 *        the logs.
	 * @param countRows
	 *        the current number of rows in the sheet.
	 */
	private void logWarningForToManyRows(final Sheet sheet, final long maxRowsWithoutWarning, long countRows) {
		if (countRows > maxRowsWithoutWarning) {
			final StringBuilder warnCountRows = new StringBuilder();
			warnCountRows.append("The sheet '");
			warnCountRows.append(sheet.getSheetName());
			warnCountRows.append("' has more rows (");
			warnCountRows.append(countRows);
			warnCountRows.append(") than the maximum expected (");
			warnCountRows.append(maxRowsWithoutWarning);
			warnCountRows.append(").");

			Logger.warn(warnCountRows.toString(), this);
        }
	}

	/**
	 * Log a warning if some rows of the sheet contains more columns than the expected quantity
	 * specified with the parameter {@code maxColsWithoutWarning}.
	 * 
	 * @param sheet
	 *        the sheet for which to write a warning in the logs
	 * @param showAllColumnWarnings
	 *        {@code true} indicates that a warning has to be generated for each rows that contains
	 *        to many columns, {@code false} indicates that only one warning for the entire sheet
	 *        has to be generated.
	 * @param maxColsWithoutWarning
	 *        the maximum number of columns that a row can contain without generating a warning in
	 *        the logs
	 * @param rowsWithToManyColumns
	 *        the rows that contains to many columns.
	 */
	private void logWarningForTooManyColumns(final Sheet sheet, final boolean showAllColumnWarnings,
			final long maxColsWithoutWarning,
			final Map<Long, Row> rowsWithToManyColumns) {
		if (!CollectionUtil.isEmptyOrNull(rowsWithToManyColumns)) {
			if (!showAllColumnWarnings) {

				Long biggestCountColumns = Long.valueOf(0);
				final Set<Long> keySet = rowsWithToManyColumns.keySet();
				for (Long countColumns : keySet) {
					if (countColumns.compareTo(biggestCountColumns) > 0) {
						biggestCountColumns = countColumns;
					}
				}
				final Row rowWithBiggerCountColumns = rowsWithToManyColumns.get(biggestCountColumns);
				logWarningToManyColumns(sheet, maxColsWithoutWarning, biggestCountColumns, rowWithBiggerCountColumns);
				
			} else {
				final Set<Entry<Long, Row>> entrySet = rowsWithToManyColumns.entrySet();
				for (Entry<Long, Row> entry : entrySet) {
					final Long currKey = entry.getKey();
					final Row currRow = entry.getValue();

					logWarningToManyColumns(sheet, maxColsWithoutWarning, currKey, currRow);
				}
			}
		}
	}

	/**
	 * Generate and write a warning message in the logs for a row which contains to many columns.
	 */
	private void logWarningToManyColumns(final Sheet aSheet, final long maxColsWithoutWarning, final Long currKey,
			final Row currRow) {
		final StringBuilder warningCountColumns = new StringBuilder();
		warningCountColumns.append("The sheet '");
		warningCountColumns.append(aSheet.getSheetName());
		warningCountColumns.append("' has more columns (");
		warningCountColumns.append(currKey.toString());
		warningCountColumns.append(") than the maximum expected (");
		warningCountColumns.append(maxColsWithoutWarning);
		warningCountColumns.append(") at row '");
		warningCountColumns.append(currRow.getRowNum());
		warningCountColumns.append("'.");

		Logger.warn(warningCountColumns.toString(), this);
	}

	/**
	 * the configuration {@link com.top_logic.base.office.excel.POIExcelTemplate.Config} for
	 *         this {@link POIExcelTemplate}.
	 */
	public Config getConfig() {
		return _config;
	}
}

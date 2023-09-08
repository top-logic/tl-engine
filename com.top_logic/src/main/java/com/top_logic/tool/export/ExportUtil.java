/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.charsize.CharSizeMap;
import com.top_logic.basic.charsize.ProportionalCharSizeMap;
import com.top_logic.basic.format.configured.Formatter;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.ResourceView;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.TableViewModel;
import com.top_logic.layout.table.control.TableControl;
import com.top_logic.mig.html.HTMLFormatter;
import com.top_logic.model.TLClassifier;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * The ExportUtil contains useful static methods for exports.
 *
 * @author <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ExportUtil {

	/** Name of the beacon tree fast list. */
	public static final String BEACON_TREE_LIST_NAME = "tl.beacon.three";

	public static final String DOTS = "...";

	/** Value which is used in POI PPT tables as empty cell values. */
	public static final String EXPORT_EMPTY_CELL_VALUE = " ";

    public static final int IMAGE_HORIZONTAL_MED=0;
    public static final int IMAGE_HORIZONTAL_SMALL=1;
    public static final int IMAGE_HORIZONTAL_LARGE=2;
    public static final int IMAGE_VERTICAL=3;
    public static final int IMAGE_VERTICAL_LARGE=4;
    public static final int IMAGE_SMALL=5;

    protected ExportUtil() {
        // Use the static methods
    }


    public static ExcelValue[] getExcelHeaderAndValuesFor(String aSheetName, TableControl aTableControl, int startAtRow) {
		TableViewModel tableModel = aTableControl.getViewModel();
        ResourceView   resources  = aTableControl.getResources();
        int            columCount = tableModel.getColumnCount();
        ArrayList      values     = new ArrayList(columCount);
        for (int i = 0; i < columCount; i++) {
            values.add(new ExcelValue(aSheetName, startAtRow, i, resources.getStringResource(tableModel.getColumnName(i))));
        }
        ExcelValue[] columnNames = (ExcelValue[]) values.toArray(new ExcelValue[values.size()]);
        ExcelValue[] data        = getExcelValuesFor(aSheetName, tableModel, startAtRow + 1);

        return (ExcelValue[]) ArrayUtil.join(columnNames, data);
    }


    public static ExcelValue[] getExcelValuesFor(String aSheetName, TableModel aTableModel, int startAtRow) {
        int           rowCount   = aTableModel.getRowCount();
        int           columCount = aTableModel.getColumnCount();
        ArrayList     values     = new ArrayList(rowCount * columCount);
		Formatter formatter = HTMLFormatter.getInstance();
        Resources     resources  = Resources.getInstance();

        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columCount; j++) {
                Object obj = aTableModel.getValueAt(i, j);
                String formattedObj;
                if (obj instanceof String) {
					formattedObj = resources.decodeMessageFromKeyWithEncodedArguments((String) obj);
					if (formattedObj != null && StringServices.startsWithChar(formattedObj, '[')
						&& StringServices.endsWithChar(formattedObj, ']')) {
                        formattedObj = (String) obj;
                    }
                } else {
					formattedObj = HTMLFormatter.formatObject(formatter, obj);
                }

                values.add(new ExcelValue(aSheetName, i + startAtRow, j, formattedObj));
            }
        }

        return (ExcelValue[]) values.toArray(new ExcelValue[values.size()]);
    }

    /**
     * This method adds an excel value to the list for the given label of the
     * object.
     *
     * @param excelValues
     *            An excel value must NOT be <code>null</code>.
     * @param cellPosition
     *            A cell position (e.g. C5). Must NOT be <code>null</code>.
     * @param object
     *            An object for the excel cell. <code>Null</code> is
     *            permitted.
     */
    public static ExcelValue addExcelValue(List excelValues, String cellPosition, Object object) {
        ExcelValue excelValue = new ExcelValue(cellPosition, MetaResourceProvider.INSTANCE.getLabel(object));
        excelValues.add(excelValue);

        return excelValue;
    }


    /**
     * Counts how many rows the given string will be use if it is filled into an area which will
     * automatically line-break after the given number of chars.
     *
     * @param string the string to export
     * @param columns the amount of chars which fit in one row
     * @param charSizeMap mapping to compute sizes when using non proportional fonts
     * @return the amount of rows this string requires
     */
    public static int countRows(String string, int columns, CharSizeMap charSizeMap) {
        String cutted = StringServices.cutString(string, Integer.MAX_VALUE, columns, charSizeMap);
        return StringServices.count(cutted, StringServices.LINE_BREAK) + 1;
    }



    public static String cutLabel(Object object, int length) {
        return cut(MetaResourceProvider.INSTANCE.getLabel(object), length);
    }

    public static String cutSeparator(Object object, int length) {
        String string = MetaResourceProvider.INSTANCE.getLabel(object);
        if (StringServices.isEmpty(string)) return "";
        if (string.length() > length) {
            String cuttedString = StringServices.cutString(string, length);
            int lastIndexOf = cuttedString.lastIndexOf(",");
            if (lastIndexOf > 0) {
                cuttedString = cuttedString.substring(0, lastIndexOf);
            }
			string = cuttedString + DOTS;

        }
		string = string.replaceAll(StringServices.LINE_BREAK, "\r\n");

        return string;
    }

    public static String cut(String string, int length) {
        if (StringServices.isEmpty(string)) return "";
        String cuttedString = StringServices.cutString(string, length);
        if (string.length() > cuttedString.length()) {
			cuttedString = cuttedString.substring(0, cuttedString.length() - 3) + DOTS;
        }
        cuttedString = cuttedString.replaceAll(StringServices.LINE_BREAK, "\r\n");
        return cuttedString;
    }

	/**
	 * Cuts the given {@link String text} to the given number of rows and columns using the
	 * {@link ProportionalCharSizeMap}.
	 * 
	 * @param string
	 *        The {@link String text} that has to be cut.
	 * @param rows
	 *        The amount of rows the given text has to be cut to.
	 * @param columns
	 *        The amount of columns the given text has to be cut to.
	 * @return The cut text.
	 */
    public static String cutText(String string, int rows, int columns) {
        return cutText(string, rows, columns, ProportionalCharSizeMap.INSTANCE);
    }

	/**
	 * Cuts the given {@link String text} to the given number of rows and columns using the
	 * {@link ProportionalCharSizeMap}.
	 * 
	 * @param string
	 *        The {@link String text} that has to be cut.
	 * @param rows
	 *        The amount of rows the given text has to be cut to.
	 * @param columns
	 *        The amount of columns the given text has to be cut to.
	 * @param replaceNewlines
	 *        Whether or not to replace "\n" (line breaks) with "\r\n".
	 * @return The cut text.
	 */
    public static String cutText(String string, int rows, int columns, boolean replaceNewlines) {
    	return cutText(string, rows, columns, ProportionalCharSizeMap.INSTANCE, replaceNewlines);
    }

	/**
	 * Cuts the given {@link String text} to the given number of rows and columns using the given
	 * {@link CharSizeMap}.
	 * 
	 * @param string
	 *        The {@link String text} that has to be cut.
	 * @param rows
	 *        The amount of rows the given text has to be cut to.
	 * @param columns
	 *        The amount of columns the given text has to be cut to.
	 * @param aCharSizeMap
	 *        The {@link CharSizeMap} that is used to cut the text.
	 * @return The cut text.
	 */
    public static String cutText(String string, int rows, int columns, CharSizeMap aCharSizeMap) {
		return cutText(string, rows, columns, aCharSizeMap, false);
    }
    
	/**
	 * Cuts the given {@link String text} to the given number of rows and columns using the given
	 * {@link CharSizeMap}.
	 * 
	 * @param string
	 *        The {@link String text} that has to be cut.
	 * @param rows
	 *        The amount of rows the given text has to be cut to.
	 * @param columns
	 *        The amount of columns the given text has to be cut to.
	 * @param aCharSizeMap
	 *        The {@link CharSizeMap} that is used to cut the text.
	 * @param replaceNewlines
	 *        Whether or not to replace "\n" (line breaks) with "\r\n".
	 * @return The cut text.
	 */
    public static String cutText(String string, int rows, int columns, CharSizeMap aCharSizeMap, boolean replaceNewlines) {
        if (StringServices.isEmpty(string)) return "";
		String cutted = StringServices.cutString(string, rows + 1, columns, aCharSizeMap);
		if (hatToBeCut(rows, cutted)) {
			cutted = StringServices.cutString(string, rows, columns, aCharSizeMap);
			int index = cutted.lastIndexOf(StringServices.LINE_BREAK);
			String lastLine = cutted.substring(index + StringServices.LINE_BREAK.length());
			if (aCharSizeMap.getSize(lastLine) + aCharSizeMap.getSize(DOTS) <= columns) {
				// dots will fit behind cutted text without becoming longer than available space
				cutted += DOTS;
			} else {
				// add dots to cutted text but delete appropriate number of characters before
				double maxSize = aCharSizeMap.getSize(lastLine) - aCharSizeMap.getSize(DOTS);
				while (lastLine.length() > 0 && aCharSizeMap.getSize(lastLine) > maxSize) {
					lastLine = lastLine.substring(0, lastLine.length() - 1);
				}

				if (index >= 0) {
					// there is more than one line available after cutting -> put all lines together and add dots
					String lines = cutted.substring(0, index + StringServices.LINE_BREAK.length());
					cutted = lines + lastLine + DOTS;
				} else {
					// there is only one line available after cutting -> simply add dots
					cutted = lastLine + DOTS;
				}
            }
		}
        if (replaceNewlines) {
			cutted = cutted.replaceAll(StringServices.LINE_BREAK, "\r\n");
        }
		return cutted;
    }


	private static boolean hatToBeCut(int rows, String cutted) {
		return StringServices.count(cutted, StringServices.LINE_BREAK) >= rows;
	}

	/**
	 * Returns <code>true</code> or <code>false</code> depending on whether the given string needs
	 * to be cut to fit the given number of rows and columns using the given {@link CharSizeMap} or
	 * not.
	 * 
	 * @see #cutText(String, int, int, CharSizeMap)
	 */
	public static boolean needsToBeCut(String string, int rows, int columns, CharSizeMap aCharSizeMap) {
		if (StringServices.isEmpty(string)) {
			return false;
		}
		String cut = StringServices.cutString(string, rows + 1, columns, aCharSizeMap);
		return hatToBeCut(rows, cut);
	}

	/**
	 * Replaces empty values in the given array with the EXPORT_EMPTY_CELL_VALUE as workaround for
	 * POI PPT which doesn't handle cell styles of empty cells correctly.
	 * 
	 * @param table
	 *        the array representing a table to export
	 */
	public static void cleanupTableForPOI(Object[][] table) {
		if (table != null)
			for (int i = 0; i < table.length; i++)
				cleanupTableRowForPOI(table[i]);
	}

	/**
	 * Replaces empty values in the given array with the EXPORT_EMPTY_CELL_VALUE as workaround for
	 * POI PPT which doesn't handle cell styles of empty cells correctly.
	 * 
	 * @param row
	 *        the array representing a table row to export
	 */
	public static void cleanupTableRowForPOI(Object[] row) {
		if (row != null)
			for (int i = 0; i < row.length; i++)
				if (StringServices.isEmpty(row[i]))
					row[i] = EXPORT_EMPTY_CELL_VALUE;
	}

	/**
	 * Replaces empty values with the EXPORT_EMPTY_CELL_VALUE as workaround for POI PPT which
	 * doesn't handle cell styles of empty cells correctly.
	 * 
	 * @param value
	 *        the value to check
	 */
	public static String cleanupTableValueForPOI(String value) {
		return StringServices.isEmpty(value) ? EXPORT_EMPTY_CELL_VALUE : value;
	}

	public static void addImage(Map values, String key, BinaryData file) {
        values.put(key, file);
    }

    public static void addLabel(Map values, String key, Object object) {
        addLabel(values, key, object, MetaResourceProvider.INSTANCE);
    }

    public static void addLabel(Map values, String key, Object object, ResourceProvider resourceProvider) {
        values.put(key, resourceProvider.getLabel(object));
    }

    public static void addRawValue(Map values, String key, Object rawObject) {
        values.put(key, rawObject);
    }

    public static void addRawString(Map values, String key, String stringValue) {
        addRawValue(values, key, stringValue);
    }



	public static BinaryData getBeaconStateImage(Number stateValue, boolean horizontal) {
        if(horizontal){
            return getBeaconStateImage(stateValue, IMAGE_HORIZONTAL_MED);
        }else{
            return getBeaconStateImage(stateValue, IMAGE_VERTICAL);
        }
    }

	/**
	 * Fetch the image for the given element of the enumeration {@link #BEACON_TREE_LIST_NAME} in
	 * the given type.
	 * 
	 * @param element
	 *        The {@link TLClassifier} to get image for. May be <code>null</code>. <code>null</code>
	 *        is interpreted as undecided.
	 * @param imageType
	 *        The image type. One of {@link #IMAGE_HORIZONTAL_LARGE}, {@link #IMAGE_HORIZONTAL_MED},
	 *        {@link #IMAGE_HORIZONTAL_SMALL}, {@link #IMAGE_SMALL}, {@link #IMAGE_VERTICAL_LARGE},
	 *        or {@link #IMAGE_VERTICAL}.
	 * 
	 * @return The {@link File} containing the image.
	 */
	public static BinaryData getBeaconThreeStateImage(TLClassifier element, int imageType) {
		return ExportUtil.getBeaconStateImage(getBeaconThreeState(element), imageType);
	}

	/**
	 * Converts the given element of the enumeration {@link #BEACON_TREE_LIST_NAME} into an Integer
	 * value which is compatible with the {@link #getBeaconStateColor(Number)} method.
	 * 
	 * @param element
	 *        The {@link TLClassifier} to get image for. May be <code>null</code>. <code>null</code>
	 *        is interpreted as undecided.
	 * @return the Integer value of the given element
	 */
	public static Integer getBeaconThreeState(TLClassifier element) {
		return Integer.valueOf(element == null ? 0 : element.getIndex() + 1);
	}

	/**
	 * Converts the given Integer value which is compatible with the
	 * {@link #getBeaconStateColor(Number)} method into an element of the enumeration
	 * {@link #BEACON_TREE_LIST_NAME}. This is the reverse method of
	 * {@link #getBeaconThreeState(TLClassifier)}.
	 * 
	 * @param state
	 *        the Integer value representing the desired beacon value (0: white, 1:green, 2:yellow,
	 *        3:red).
	 * @return the {@link TLClassifier} of the enumeration {@link #BEACON_TREE_LIST_NAME}
	 *         corresponding to the given beacon value
	 */
	public static TLClassifier getBeaconThreeElement(Number state) {
		FastList list = FastList.getFastList(BEACON_TREE_LIST_NAME);
		int stateIndex = Utils.getintValue(state);
		if (stateIndex > 0) {
			stateIndex--;
			{
				for (Iterator<TLClassifier> it = list.getClassifiers().iterator(); it.hasNext();) {
					TLClassifier element = it.next();
					if (element.getIndex() == stateIndex) {
						return element;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Fetch the color for the given element of the enumeration {@link #BEACON_TREE_LIST_NAME} in
	 * the given type.
	 * 
	 * @param element
	 *        The {@link TLClassifier} to get color for. May be <code>null</code>. <code>null</code>
	 *        is interpreted as undecided.
	 * 
	 * @return The color for the {@link TLClassifier}.
	 */
	public static Color getBeaconThreeStateColor(TLClassifier element) {
		return ExportUtil.getBeaconStateColor(getBeaconThreeState(element));
	}

	/**
	 * Converts the given Integer value which represents a beacon value (0: white, 1:green,
	 * 2:yellow, 3:red) into a corresponding color.
	 * 
	 * @param stateValue
	 *        the Integer value representing the desired beacon value (0: white, 1:green, 2:yellow,
	 *        3:red).
	 * @return the {@link Color} corresponding to the given beacon value
	 */
    public static Color getBeaconStateColor(Number stateValue) {
		switch (Utils.getintValue(stateValue)) {
        case 0:
        	return Color.WHITE;
        case 1:
        	return Color.GREEN;
        case 2:
        	return Color.YELLOW;
        case 3:
        	return Color.RED;
        default:
        	return null;
        }
    }

	public static BinaryData getBeaconStateImage(Number stateValue, int imageType) {
		switch (Utils.getintValue(stateValue)) {
        case 1:
        	switch(imageType) {
        	case IMAGE_VERTICAL:       		return getImageFile("/beacon/ppt/greenVertical.png");
        	case IMAGE_VERTICAL_LARGE: 		return getImageFile("/beacon/ppt/greenVerticalLarge.png");
        	case IMAGE_HORIZONTAL_MED: 		return getImageFile("/beacon/ppt/greenHorizontal.png");
        	case IMAGE_HORIZONTAL_LARGE: 	return getImageFile("/beacon/ppt/greenHorizontalLarge.png");
        	case IMAGE_HORIZONTAL_SMALL: 	return getImageFile("/beacon/ppt/greenHorizontalSmall.png");
        	default: 				   		return getImageFile("/beacon/ppt/greenSmall.png");
        	}
        case 2:
        	switch(imageType) {
        	case IMAGE_VERTICAL:       		return getImageFile("/beacon/ppt/yellowVertical.png");
        	case IMAGE_VERTICAL_LARGE: 		return getImageFile("/beacon/ppt/yellowVerticalLarge.png");
        	case IMAGE_HORIZONTAL_MED: 		return getImageFile("/beacon/ppt/yellowHorizontal.png");
        	case IMAGE_HORIZONTAL_LARGE: 	return getImageFile("/beacon/ppt/yellowHorizontalLarge.png");
        	case IMAGE_HORIZONTAL_SMALL: 	return getImageFile("/beacon/ppt/yellowHorizontalSmall.png");
        	default: 				   		return getImageFile("/beacon/ppt/yellowSmall.png");
        	}
        case 3:
        	switch(imageType) {
        	case IMAGE_VERTICAL:       		return getImageFile("/beacon/ppt/redVertical.png");
        	case IMAGE_VERTICAL_LARGE: 		return getImageFile("/beacon/ppt/redVerticalLarge.png");
        	case IMAGE_HORIZONTAL_MED: 		return getImageFile("/beacon/ppt/redHorizontal.png");
        	case IMAGE_HORIZONTAL_LARGE: 	return getImageFile("/beacon/ppt/redHorizontalLarge.png");
        	case IMAGE_HORIZONTAL_SMALL: 	return getImageFile("/beacon/ppt/redHorizontalSmall.png");
        	default: 				   		return getImageFile("/beacon/ppt/redSmall.png");
        	}
        default:
        	switch(imageType) {
        	case IMAGE_VERTICAL:       		return getImageFile("/beacon/ppt/whiteVertical.png");
        	case IMAGE_VERTICAL_LARGE:		return getImageFile("/beacon/ppt/whiteVerticalLarge.png");
        	case IMAGE_HORIZONTAL_MED: 		return getImageFile("/beacon/ppt/whiteHorizontal.png");
        	case IMAGE_HORIZONTAL_LARGE: 	return getImageFile("/beacon/ppt/whiteHorizontalLarge.png");
        	case IMAGE_HORIZONTAL_SMALL: 	return getImageFile("/beacon/ppt/whiteHorizontalSmall.png");
        	default: 				   		return getImageFile("/beacon/ppt/whiteSmall.png");
        	}
        }
    }

	public static BinaryData getImageFile(String relativePath) {
		{
			BinaryData theFile = FileManager.getInstance().getDataOrNull(ThemeFactory.getTheme().getFileLink(relativePath));
            if (theFile == null) {
                throw new TopLogicException(ExportUtil.class, "Couldn't load the image with the path ('" + relativePath + "').");
            }
            return theFile;
        }
    }

}

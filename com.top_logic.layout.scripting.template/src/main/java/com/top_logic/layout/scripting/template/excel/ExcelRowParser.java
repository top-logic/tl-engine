/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigBuilder;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.template.excel.ExcelActionOp.ExcelAction;

/**
 * Convert an excel row into a {@link ExcelAction}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelRowParser {

	private static final InstantiationContext FAIL_IMMEDIATELY_CONTEXT =
		SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

    /** Context definition in the configuration. */
    public static final String CONTEXT_PROPERTY = "Kontext";

    /** Parameter definition in the configuration. */
    public static final String PARAM_PROPERTY = "Parameter";

	/** The column name for the variable name in which the result of the action should be stored. */
    public static final String RESULT_PROPERTY = "Ergebnis";

    /** 
     * Creates a {@link ExcelRowParser}.
     */
    public ExcelRowParser() {
    }

    /**
	 * Convert the given test row into a configuration item.
	 * 
	 * @param actionClass
	 *        The class to create the item from, must not be <code>null</code>.
	 * @param aRow
	 *        The test row to get the values from, must not be <code>null</code>.
	 * @return The requested configuration item, never <code>null</code>.
	 * @throws ConfigurationError
	 *         When parsing the configuration fails for a reason.
	 */
	public <T extends ExcelAction> T getConfig(Class<T> actionClass, ExcelRow aRow) {
        try {
			return this.parse(actionClass, aRow);
        }
        catch (ConfigurationException ex) {
			throw new ConfigurationError("Unparsable configuration at " + aRow.location(), ex);
        }
    }

	private <T extends ExcelAction> T parse(Class<T> actionClass, ExcelRow aRow) throws ConfigurationException {
		ConfigBuilder result = TypedConfiguration.createConfigBuilder(actionClass);

        parsParams(aRow, result);

		result.initLocation(aRow.location());

		T excelAction = actionClass.cast(result.createConfig(FAIL_IMMEDIATELY_CONTEXT));
		excelAction.setFileName(aRow.getFileName());
		excelAction.setSheetName(aRow.getSheetName());
		excelAction.setRowNumber(aRow.getRowNum());
		parseContext(aRow, excelAction);
		parseReturn(aRow, excelAction);
		parseBusinessModel(aRow, excelAction);
		parseBusinessAction(aRow, excelAction);
		return excelAction;
    }

	private void parsParams(ExcelRow aRow, ConfigBuilder result) throws ConfigurationException {
		try {
			parseParamsUnsafe(aRow, result);
        }
        catch (ConfigurationException ex) {
			throw new ConfigurationException("Failed to parse " + aRow + ": " + ex.getMessage(), ex);
        }
	}

	private void parseReturn(ExcelRow aRow, ExcelAction excelAction) {
		excelAction.setResultName(aRow.getReturnName());
    }

	private void parseContext(ExcelRow aRow, ExcelAction excelAction) {
		excelAction.setContext(aRow.getContextName());
    }

	private void parseParamsUnsafe(ExcelRow aRow, ConfigBuilder result) throws ConfigurationException {
        PropertyDescriptor paramProperty = result.descriptor().getProperty(PARAM_PROPERTY);

		List<Object> theValues;
		if (ExcelRow.PARAM_COLUMN > aRow.getRawValues().size()) {
			theValues = Collections.emptyList();
		}
		else {
			theValues = aRow.getRawValues().subList(ExcelRow.PARAM_COLUMN, aRow.getRawValues().size());
		}

        result.update(paramProperty, parseValue(aRow, paramProperty, theValues, ExcelRow.PARAM_COLUMN));
    }

    @SuppressWarnings("unchecked")
    private Object parseValue(ExcelRow aRow, PropertyDescriptor aProperty, List<Object> someCells, int aColumn) throws ConfigurationException {
        switch (aProperty.kind()) {
    	case ARRAY: {
				return PropertyDescriptorImpl.listAsArray(aProperty,
					parseCollectionValue(aRow, aProperty, someCells, aColumn));
    	}
        case LIST: {
            return parseCollectionValue(aRow, aProperty, someCells, aColumn);
        }
        case ITEM: {
            ConfigBuilder result = TypedConfiguration.createConfigBuilder(aProperty.getType());

			result.initLocation(aRow.location());

            for (Object theCell : someCells) {
                String[] theKeyValue = aRow.getKeyValue((String) theCell);
				String theKey = nonNull(theKeyValue[0]).trim();

                if (!ExcelRow.NO_KEY.equals(theKey)) {
					String theValue = nonNull(theKeyValue[1]).trim();

                    PropertyDescriptor contentProperty = result.descriptor().getProperty(theKey);

                    if (contentProperty == null) {
						throw new IllegalArgumentException("Cannot find property called '" + theKey + "' "
							+ aRow.location() + ".");
                    }

                    Object elementValue = parseValue(aRow, contentProperty, theValue, aColumn++);

                    switch (contentProperty.kind()) {
                	case ARRAY: {
								List<Object> listValue =
									new ArrayList<>(PropertyDescriptorImpl.arrayAsList(result.value(contentProperty)));
                		listValue.add(elementValue);
								result.update(contentProperty,
									PropertyDescriptorImpl.listAsArray(contentProperty, listValue));
                		break;
                	}
                    case LIST: {
                        List<Object> listValue = (List<Object>) result.value(contentProperty);
                        if (CollectionUtil.isEmptyOrNull(listValue)) {
                            listValue = new ArrayList<>();
                        }
                        listValue.add(elementValue);
                        result.update(contentProperty, listValue);
                        break;
                    }
                    default: {
                        result.update(contentProperty, elementValue);
                    }
                    }
                }
            }

				return result.createConfig(FAIL_IMMEDIATELY_CONTEXT);
        }
        default: {
			throw new UnsupportedOperationException("At " + aRow.location() + ".");
        }
        } 
    }

	private ArrayList<Object> parseCollectionValue(ExcelRow aRow, PropertyDescriptor aProperty, List<Object> someCells,
			int aColumn) throws ConfigurationException {
		ArrayList<Object> result = new ArrayList<>();

		for (Object theCell : someCells) {
		    String[] value = aRow.getKeyValue((String) theCell);

			result.add(parseValue(aRow, aProperty, nonNull(value[1]).trim(), aColumn++));
		}
		return result;
	}
    
    private Object parseValue(ExcelRow aRow, PropertyDescriptor aProperty, String aValue, int aColumn) throws ConfigurationException {
        switch (aProperty.kind()) {
        case COMPLEX: 
        case PLAIN: {
            return aProperty.getValueProvider().getValue(aProperty.getPropertyName(), aValue);
        }
        
        case ITEM: {
            return parseContents(aRow, aValue, aProperty.getType(), aColumn);
        }
        
			case ARRAY:
        case LIST: {
            return parseContents(aRow, aValue, aProperty.getElementType(), aColumn);
        }
        
        default: {
			throw new UnsupportedOperationException("At " + aRow.location() + ".");
        }
        } 
    }

	private Object parseContents(ExcelRow aRow, String aValue, Class<?> aType, int aColumn)
			throws ConfigurationException {
        ConfigBuilder       theResult   = TypedConfiguration.createConfigBuilder(aType);
        Map<String, String> theValueMap = ExcelRow.getValueMap(aValue);

		theResult.initLocation(aRow.location());

        for (Entry<String, String> entry : theValueMap.entrySet()) {
            PropertyDescriptor theProperty;
            String             theValue;
            String             theKey = entry.getKey();

			if (ExcelRow.NO_KEY.endsWith(theKey)) {
                theValue    = aValue;
                theProperty = getShortcutProperty(theResult.descriptor());
            } 
            else {
                theProperty = theResult.descriptor().getProperty(theKey);
                theValue    = entry.getValue();
            }

            theResult.update(theProperty, parseValue(aRow, theProperty, theValue, aColumn));
        }

		return theResult.createConfig(FAIL_IMMEDIATELY_CONTEXT);
    }

    private PropertyDescriptor getShortcutProperty(ConfigurationDescriptor aDescriptor) throws ConfigurationException {
        for (PropertyDescriptor theProperty : aDescriptor.getProperties()) {
			if (theProperty.getAnnotation(Shortcut.class) != null) {
                return theProperty;
            }
        }
        throw new ConfigurationException("No shortcut property in " + aDescriptor.getConfigurationInterface().getName());
    }

	private void parseBusinessModel(ExcelRow excelRow, ExcelAction action) {
		action.setBusinessModel(excelRow.getModelName());
	}

	private void parseBusinessAction(ExcelRow excelRow, ExcelAction action) {
		action.setBusinessAction(excelRow.getActionName());
	}
}


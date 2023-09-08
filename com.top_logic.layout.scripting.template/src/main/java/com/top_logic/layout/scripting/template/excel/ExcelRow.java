/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import static com.top_logic.basic.shared.collection.CollectionUtilShared.nonNull;
import static com.top_logic.basic.shared.string.StringServicesShared.nonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.Row;

import com.top_logic.base.office.POIUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DefaultConfigConstructorScheme.Factory;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.LocationImpl;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.template.action.BusinessOperationTemplateAction;
import com.top_logic.layout.scripting.template.action.TemplateAction.Parameter;
import com.top_logic.layout.scripting.template.excel.ExcelActionOp.ExcelAction;
import com.top_logic.layout.scripting.template.excel.ExcelActionRegistry.Config;

/**
 * Accessing class to one row in the excel sheet.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelRow {

    /** Flag for "no key provided". */
    public static final String NO_KEY = "__no_key";

	/** Column number for the variable name in which the result should be stored. */
	public static final int RETURN_COLUMN = 0;

	/** Column number for the business action name. */
	private static final int ACTION_COLUMN = 1;

	/** Column number for the business model name. */
	private static final int MODEL_COLUMN = 2;

    /** Column number for the context. */
    public static final int CONTEXT_COLUMN = 3;

	/** Column number for the parameter. */
	public static final int PARAM_COLUMN = 4;

	/** The key for the context. (Only available in script template.) */
	public static final String CONTEXT_KEY = "kontext";

	/** The key for the return value. (Only available in script template.) */
	public static final String RESULT_KEY = "ergebnis";

    /** The values contained in the test row. */
	private final List<Object> _rowValues;

	private final String _fileName;

    /** Name of the sheet this row belongs to. */
	private final String _sheetName;

    /** Row represented by this instance. */
	private final Row _row;

    /** The parsed map of parameters. */
	private Map<String, String> _params;

    /**
	 * This constructor creates an {@link ExcelRow}.
	 * 
	 * @param someRawValues
	 *        Is allowed to be <code>null</code>.
	 */
	public ExcelRow(String fileName, String sheetName, Row aRow, List<Object> someRawValues) {
		_fileName = fileName;
		_sheetName = sheetName;
		_row = aRow;
		_rowValues = nonNull(someRawValues);
    }

    @Override
    public String toString() {
		return getClass().getSimpleName() + " [Excel file: '" + getFileName() + "', Sheet: '" + getSheetName()
			+ "', Row: " + getRowNum() + ", Operation: '" + getActionName() + "' '" + getModelName() + "'.]";
    }

    /** 
     * Return the raw values of this test row.
     * 
     * @return    The requested raw values, never <code>null</code>.
     */
    public List<Object> getRawValues() {
		return _rowValues;
    }

    /** 
     * Check, if there is an action defined (a value in the {@link #ACTION_COLUMN}).
     * 
     * @return    <code>true</code> when the column contains a string.
     */
    public boolean hasAction() {
		return !(StringServices.isEmpty(getActionName()) && StringServices.isEmpty(getModelName()));
    }

    /**
	 * Return the real test handler to perform the operation.
	 * 
	 * This will be requested by the combination of the {@link #ACTION_COLUMN} and the
	 * {@link #MODEL_COLUMN}.
	 * 
	 * @return The requested {@link ApplicationAction}, never <code>null</code>.
	 * @throws ConfigurationException
	 *         When preparing the action fails.
	 */
	public ApplicationAction getAction(Config excelActionRegistry) throws ConfigurationException {
		String actionName = getActionName();
		String modelName = getModelName();
		Class<? extends ExcelActionOp<?>> theClass =
			ExcelActionRegistry.getExcelActionOpClass(excelActionRegistry, actionName, modelName);

        if (theClass == null) {
			return findActionScript(modelName, actionName, location());
		} else {
	        Factory           theFactory   = DefaultConfigConstructorScheme.getFactory(theClass);
			Class<?> untypedConfigInterface = theFactory.getConfigurationInterface();
			if (!ExcelAction.class.isAssignableFrom(untypedConfigInterface)) {
				// Should not happen:
				// The config interface for an ExcelActionOp is always an ExcelAction.
				// And the handler class for which the config interface is created,
				// is types as subtype of ExcelActionOp.
				throw new UnreachableAssertion(untypedConfigInterface + " is not a subtype of " + ExcelAction.class
					+ ". Location: " + this);
			}
			@SuppressWarnings("unchecked")
			Class<? extends ExcelAction> actionInterface = (Class<? extends ExcelAction>) untypedConfigInterface;
			ExcelAction excelAction = new ExcelRowParser().getConfig(actionInterface, this);
			excelAction.setImplementationClass(theClass);
			return excelAction;
        }
    }

	/**
	 * Find the scripted test template for the given business object and business action
	 * combination.
	 * <p>
	 * If no scripted test template is found or it cannot be accessed, an action that will log this
	 * error is returned.
	 * </p>
	 * 
	 * @param businessObject
	 *        Is allowed to be <code>null</code>.
	 * @param businessAction
	 *        Is allowed to be <code>null</code>.
	 * @param location
	 *        The {@link Location} to use for the generated configuration.
	 * 
	 * @return Never <code>null</code>.
	 */
	protected ApplicationAction findActionScript(String businessObject, String businessAction, Location location) {
		try {
			BusinessOperationTemplateAction action = newItemWithLocation(BusinessOperationTemplateAction.class, location);
			action.setBusinessObject(businessObject);
			action.setBusinessAction(businessAction);
			Map<String, String> parameters = convert(getParameters());
			for (Entry<String, String> entry : parameters.entrySet()) {
				Parameter parameter = TypedConfiguration.newConfigItem(Parameter.class);
				parameter.setName(entry.getKey());
				StringValue stringValue = TypedConfiguration.newConfigItem(StringValue.class);
				stringValue.setString(entry.getValue());
				ValueRef value = stringValue;
				parameter.setValue(value);
				action.getParameters().add(parameter);
			}
			return action;
		} catch (Exception ex) {
			throw new RuntimeException(this + " can not be executed, as loading the script template failed."
				+ " Cause: " + ex.getMessage(), ex);
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T newItemWithLocation(Class<T> type, Location location) {
		ConfigurationItem result = TypedConfiguration.getConfigurationDescriptor(type).factory().createNew(location);
		return (T) result;
	}

	private Map<String, String> convert(Map<String, String> parameters) {
		Map<String, String> templateParameters = new HashMap<>(parameters);
		templateParameters.remove(NO_KEY);
		String contextName = getContextName();
		if (!StringServices.isEmpty(contextName)) {
			templateParameters.put(CONTEXT_KEY, contextName);
		}
		String returnName = getReturnName();
		if (!StringServices.isEmpty(returnName)) {
			templateParameters.put(RESULT_KEY, returnName);
		}
		return templateParameters;
	}

    /**
	 * The name of the variable in which the result of this action should be stored.
	 * 
	 * @return Never <code>null</code>.
	 */
    public String getReturnName() {
		if (_rowValues.size() <= RETURN_COLUMN) {
			return "";
		}
		return nonNull((String) _rowValues.get(RETURN_COLUMN)).trim();
    }

	/**
	 * The name of the business model, this {@link ExcelRow} is about.
	 * 
	 * @return Never <code>null</code>.
	 */
	public String getModelName() {
		if (_rowValues.size() <= MODEL_COLUMN) {
			return "";
		}
		return nonNull((String) _rowValues.get(MODEL_COLUMN)).trim();
	}

	/**
	 * The name of the business action, this {@link ExcelRow} is about.
	 * 
	 * @return Never <code>null</code>.
	 */
	public String getActionName() {
		if (_rowValues.size() <= ACTION_COLUMN) {
			return "";
		}
		return nonNull((String) _rowValues.get(ACTION_COLUMN)).trim();
	}

    /** 
     * Return the name of the context the row operates on.
     * 
     * @return    The requested context name.
     */
    public String getContextName() {
		if (CONTEXT_COLUMN >= _rowValues.size()) {
			return null;
		}
		String[] theKeyValue = this.getKeyValue((String) _rowValues.get(CONTEXT_COLUMN));

		String prefix = nonNull(theKeyValue[0]).trim();
		if (StringServices.isEmpty(theKeyValue) || NO_KEY.equals(prefix)) {
            return null;
        }
		else if ("in".equals(prefix)) {
			return nonNull(theKeyValue[1]).trim();
        } 
        else { 
			throw new IllegalArgumentException("Context has no correct 'in:' prefix. Location: " + this);
        }
    }

    /** 
     * Return the parameters from this row.
     * 
     * @return   The parameters, never <code>null</code>.
     */
    public Map<String, String> getParameters() {
		if (_params == null) {
			int theSize = _rowValues.size();

			_params = new HashMap<>();

            for (int thePos = PARAM_COLUMN; thePos < theSize; thePos++) {
				String[] theKeyValue = this.getKeyValue((String) _rowValues.get(thePos));

                if (theKeyValue != null) {
					_params.put(nonNull(theKeyValue[0]).trim(), nonNull(theKeyValue[1]).trim());
                }
            }
        }

		return _params;
    }

    /** 
     * Return the value for the given parameter.
     * 
     * @param    aString    The parameter to get the value from, must not be <code>null</code>.
     * @return   The requested value, may be <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(String aString) {
        return (T) this.getParameters().get(aString);
    }

    /** 
     * Split the given string into a key, value pair.
     * 
     * @param     aString    The string to be split up, may be <code>null</code>.
     * @return    The key value pair or <code>null</code>.
     */
    public String[] getKeyValue(String aString) {
        return ExcelRow.getKeyValue(aString, ':');
    }

    /** 
     * Return the name of this test row in excel notation.
     * 
     * @return    The requested name, never <code>null</code>.
     */
    public String getName() {
        Row theRow = this.getRow();

        if (theRow != null) {
            int theRowNum = theRow.getRowNum() + 1;

            return this.getSheetName() + "!A" + theRowNum + ':' + POIUtil.getExcelColumnName(theRow.getPhysicalNumberOfCells()) + theRowNum;
        }
        else {
            return "???";
        }
    }

	/** The name of the file this excel row belongs to. */
	public String getFileName() {
		return _fileName;
	}

    /**
     * This method returns the sheetName.
     * 
     * @return    Returns the sheetName.
     */
    public String getSheetName() {
		return _sheetName;
    }

    /** 
     * Return the row number of this excel row.
     * 
     * @return    The requested excel row number (starting excel like with "1").
     */
    public int getRowNum() {
        return this.getRow().getRowNum() + 1;
    }

    /**
     * This method returns the rowNr.
     * 
     * @return    Returns the rowNr.
     */
    public Row getRow() {
		return _row;
    }

	/**
	 * A resource name pointing to a sheet in an Excel file.
	 */
	public Location location() {
		return location(getFileName(), getSheetName(), getRowNum());
	}

	private static Location location(String fileName, String sheetName, int rowNum) {
		return new Location() {
			@Override
			public String getResource() {
				return "file '" + fileName + "' sheet '" + sheetName + "'";
			}

			@Override
			public int getLine() {
				return rowNum;
			}

			@Override
			public int getColumn() {
				return 1;
			}

			@Override
			public String toString() {
				return LocationImpl.toString(this);
			}
		};
	}

	private static String getValueFromString(String aString, int aPos) {
        return (aString != null) && (aPos < aString.length()) ? aString.substring(aPos + 1).trim() : null;
    }

    /** 
     * Convert the given string into a (key, value) pair.
     * 
     * @param    aString       The string to be separated, must not be <code>null</code>.
     * @param    aSeparator    The separator character, must not be <code>null</code>.
     * @return   The requested key value pair, may be <code>null</code>, if separator cannot be found.
     */
    public static String[] getKeyValue(String aString, char aSeparator) {
        int theDiv = (aString != null) ? aString.indexOf(aSeparator) : -1;

        if (theDiv < 0) {
            return new String[] {NO_KEY, aString};
        }
        else {
	        @SuppressWarnings("null")
			String theKey   = aString.substring(0, theDiv).trim();
	        String theValue = ExcelRow.getValueFromString(aString, theDiv);

	        return new String[] {theKey, theValue};
        }
    }

    /** 
     * Convert the given string into a map of keys and values 
     * (separator for keys and values is '=', for the pairs its the ';').
     * 
     * @param    someValues    The string to be separated, must not be <code>null</code>.
     * @return   The requested map, never <code>null</code>.
     */
    public static Map<String, String> getValueMap(String someValues) {
        Map<String, String> theValues = new HashMap<>();

        for (String theValue : StringServices.toList(someValues, ';')) {
            String[] theKeyValue = ExcelRow.getKeyValue(theValue, '=');

            theValues.put(theKeyValue[0], theKeyValue[1]);
        }
        return theValues;
    }
}

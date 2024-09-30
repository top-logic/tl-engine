/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.admin.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.CalledFromJSP;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.knowledge.gui.layout.form.DeleteSimpleWrapperCommandHandler;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.constraints.AllowedCharactersOnlyConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.constraints.UniqueValuesDependency;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.listener.RadioButtonValueListenerDependency;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.RowObjectRemover;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.control.TableListControl;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.I18NResourceProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;
import com.top_logic.util.error.TopLogicException;

/**
 * This class is an edit component to view and edit a single ordered list. The
 * list to edit can be specified in the {@link #ATTRIBUTE_LISTNAME} in the layout XML file, 
 * then this component will be in
 * 'SingleListMode', needing no model and no master layout component. Else a master can be
 * specified, then the component will be in 'MultiListMode', showing the list, which was set
 * as model by the master component. In addition, the columns to show can be specified in
 * the {@link #ATTRIBUTE_COLUMNS} and the sizes of the text input fields for each column can
 * be specified in the {@link #ATTRIBUTE_COLUMN_SIZES}
 *
 * @author <a href=mailto:CBR@top-logic.com>CBR</a>
 */
public class EditListComponent extends EditComponent implements RowObjectCreator, RowObjectRemover {

    public interface Config extends EditComponent.Config {
		@Name(ATTRIBUTE_LISTNAME)
		String getListname();

		@Name(ATTRIBUTE_LISTTYPE)
		@StringDefault(DEFAULT_LISTTYPE)
		String getListtype();

		@Name(ATTRIBUTE_COLUMNS)
		String getColumns();

		@Name(ATTRIBUTE_COLUMN_SIZES)
		String getColumnSizes();

		@Name(ATTRIBUTE_ALLOW_REMOVING_ELEMENTS_IN_USE)
		@BooleanDefault(false)
		boolean getAllowRemovingElementsInUse();

		@Override
		@StringDefault(ListApplyCommandHandler.COMMAND)
		String getApplyCommand();

		@Override
		@StringDefault("listSwitchToAJAXEdit")
		String getEditCommand();

		@Override
		@StringDefault(DeleteListCommandHandler.COMMAND)
		String getDeleteCommand();
	}

	public static final String ATTRIBUTE_LISTNAME = "listname";
    public static final String ATTRIBUTE_LISTTYPE = "listtype";
    public static final String ATTRIBUTE_COLUMNS = "columns";
    public static final String ATTRIBUTE_COLUMN_SIZES = "columnSizes";
    public static final String ATTRIBUTE_ALLOW_REMOVING_ELEMENTS_IN_USE = "allowRemovingElementsInUse";
    public static final String DEFAULT_LISTTYPE = "Classification";
    public static final String LANGUAGE_VALUE = "_languages";
    public static final String DEFAULT_VALUE = "default";

	public static final String FIELD_CLASSIFICATION_TYPE = "type";
    public static final String FIELD_LIST_NAME = "field_list_name";
    public static final String FIELD_LIST_DESCRIPTION = "field_list_description";
    public static final String FIELD_LIST_MULTI = "field_list_multi";

	/**
	 * Name of the {@link FormField} displaying (and editing) the property {@link FastList#isOrdered()}.
	 */
	public static final String FIELD_LIST_ORDERED = "field_list_ordered";
    public static final String FIELD_TABLELIST = "field_tablelist";
    public static final String TABLE_EDIT_FIELDS_GROUP = "table_edit_fields_group";

    public static final String COLUMN_SELF = "column_self";
    public static final String COLUMN_NAME = "column_name";
    public static final String COLUMN_DESCRIPTION = "column_description";
    public static final String COLUMN_DEFAULT = "column_default";
    public static final String COLUMN_IN_USE = "column_in_use";

    public static final String COLUMN_I18N_PREFIX = "column_i18n_";
    public static final String FIELD_I18N_PREFIX = "name_i18n_";
    public static final String FIELD_PREFIX = "field_";
    public static final String CONSTRAINT_MESSAGE_KEY = "constraint_message";

    public static final String ALLOWED_CHARS = StringServices.ALPHANUMERIC + "_-.,()";
    public static final AllowedCharactersOnlyConstraint ALLOWED_CHARS_CONSTRAINT = new AllowedCharactersOnlyConstraint(ALLOWED_CHARS);

	public static final StringLengthConstraint LIST_NAME_LENGTH_CONSTRAINT = new StringLengthConstraint(0, 64);
	public static final StringLengthConstraint LIST_DESCRIPTION_LENGTH_CONSTRAINT = new StringLengthConstraint(0, 128);
	public static final StringLengthConstraint ELEMENT_NAME_LENGTH_CONSTRAINT = new StringLengthConstraint(0, 72);
	public static final StringLengthConstraint ELEMENT_DESCRIPTION_LENGTH_CONSTRAINT = new StringLengthConstraint(0, 128);

	@CalledFromJSP
	public static final String MULTIPLE_FILED_NAME = "multi";

    /**
     * The name of the list to edit.
     * <code>null</code> means 'MultiListMode' (see class comment).
     */
    protected String listname = null;

    /** The type of the list to create in SingleListMode if the list was not found. */
    protected String listtype = null;

    /** The columns to show by the table as set in the configuration XML file. */
    protected String columns = null;

    /** The text input field sizes for each column as set in the configuration XML file. */
    protected String columnSizes = null;

    /** Indicates whether removing of list elements in use should be permitted. */
    protected boolean allowRemovingElementsInUse = false;

	/** Counter to create always new IDs for the {@link RowObjectCreator}. */
    protected int idCounter = 0;

	/** Counter to create always new IDs for new groups in {@link RowObjectCreator}. */
	protected int newIdCounter = 0;

    /** Saves the FormFieldHelper. */
    protected FormFieldHelper fieldHelper = new FormFieldHelper();

    /** Saves the fields to check for identity. */
    protected Set constraints = new HashSet();

    /** Saves the BooleanFields. */
    protected Set booleanFields = new HashSet();

    /** Saves the UniqueValuesDependency. */
    protected UniqueValuesDependency dependency;

    /** Saves the RadioButtonValueListenerDependency. */
    protected RadioButtonValueListenerDependency listenerDependency;

    /** Saves the columns to use. */
    protected String[] columnsArray = null;

    /** Saves the columns to use. */
    protected int[] columnSizesArray = null;

    /**
     * Creates a {@link EditListComponent}.
     */
    public EditListComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
        super(context, aSomeAttrs);
        listname = StringServices.nonEmpty(StringServices.nonEmpty(aSomeAttrs.getListname()));
        listtype = StringServices.nonEmpty(aSomeAttrs.getListtype());
        columns = StringServices.nonEmpty(aSomeAttrs.getColumns());
        columnSizes = StringServices.nonEmpty(aSomeAttrs.getColumnSizes());
        allowRemovingElementsInUse = aSomeAttrs.getAllowRemovingElementsInUse();
	}

    @Override
	public FormContext createFormContext() {
        idCounter = 0;
		newIdCounter = 0;
		final ResPrefix theResPrefix = getResPrefix();
		final Resources resources = Resources.getInstance();
		FormContext theContext = new FormContext("editList", theResPrefix);
        fieldHelper.clear();
        constraints.clear();
        booleanFields.clear();

        if (getModel() instanceof FastList) {
            FastList theModel = (FastList)getModel();

            // Check if a previous commit failed and repair list
            if (ListApplyCommandHandler.removeChangedFlags(theModel)) {
                Logger.warn("It seems that a previous commit failed. List was repaired.", this);
            }

            // create list attributes
            StringField theField = fieldHelper.createStringField(FIELD_LIST_NAME, theModel.getName(), null, theContext, false, true);
			theField.addConstraint(LIST_NAME_LENGTH_CONSTRAINT);
            theField.addConstraint(ALLOWED_CHARS_CONSTRAINT);
            if (isSingleListMode()) theField.setImmutable(true);
			StringField descriptionField = fieldHelper.createStringField(FIELD_LIST_DESCRIPTION, theModel.getDescription(), null, theContext);
			descriptionField.addConstraint(LIST_DESCRIPTION_LENGTH_CONSTRAINT);
            fieldHelper.createBooleanField(FIELD_LIST_MULTI, theModel.isMultiSelect(), null, theContext, true);
			if (theModel.isSystem()) {
				theField.setImmutable(true);
			}
			fieldHelper.createBooleanField(FIELD_LIST_ORDERED, theModel.isOrdered(), null, theContext, false);

            addMoreToContext(theContext, theModel);

			final String[] theColumns = getColumns();
			List<TableConfigurationProvider> tableConfigurationProviders = new ArrayList<>();
			tableConfigurationProviders.add(new TableConfigurationProvider() {

				@Override
				public void adaptDefaultColumn(ColumnConfiguration defaultColumn) {
					defaultColumn.setAccessor(MapAccessor.INSTANCE);
				}

				@Override
				public void adaptConfigurationTo(TableConfiguration table) {
					table.setResPrefix(getResPrefix().append(FIELD_TABLELIST));
					table.setDefaultFilterProvider(null);
					table.setColumnCustomization(ColumnCustomization.ORDER);
					table.setPageSizeOptions(PagingModel.SHOW_ALL);
					for (String column : theColumns) {
						if (column.startsWith(COLUMN_I18N_PREFIX)) {
							String theLang = column.substring(COLUMN_I18N_PREFIX.length());
							table.declareColumn(column).setColumnLabel(
								resources.getMessage(theResPrefix.key(FIELD_I18N_PREFIX),
									Resources.getDisplayLanguage(ResourcesModule.localeFromString(theLang))));
						}
					}

				}
			});
			TableConfigurationProvider xmlConfiguration = lookupTableConfigurationBuilder(FIELD_TABLELIST);
			if (xmlConfiguration != null) {
				tableConfigurationProviders.add(xmlConfiguration);
			}

			// create element table
			TableConfiguration tableConfiguration = TableConfigurationFactory.build(tableConfigurationProviders);
			ObjectTableModel theTableModel = new ObjectTableModel(theColumns,
				tableConfiguration, createRowList(theContext), true);
            TableField theTable = FormFactory.newTableField(FIELD_TABLELIST, theTableModel, isInEditMode());
            theContext.addMember(theTable);
        }
		refreshConstraintsAndListeners();
        return theContext;
    }

    /**
     * Hook for sub classes to add more stuff to the context.
     * In this class, a field for each supported language is added.
     */
    protected void addMoreToContext(FormContext aContext, FastList aModel) {
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			String displayLanguage = Resources.getDisplayLanguage(locale);
			ResKey theLabel = getResPrefix().key(FIELD_I18N_PREFIX).asResKey1().fill(displayLanguage);
			fieldHelper.createStringField(FIELD_PREFIX + FIELD_I18N_PREFIX + locale.toString(),
				Resources.getInstance(locale).getStringOptional(FastListElementLabelProvider.labelKey(aModel)),
				theLabel,
				aContext);
        }
    }



    /**
     * Gets the columns to show in the table
     *
     * @return an array with the columns to show in the table
     */
    protected String[] getColumns() {
        if (columnsArray == null) {
            computeColumns();
        }
        return columnsArray;
    }

    /**
     * Gets the text input field sizes for the individual columns.
     *
     * @return an array with the text input field sizes for the individual columns
     */
    protected int[] getColumnSizes() {
        if (columnSizesArray == null) {
            computeColumns();
        }
        return columnSizesArray;
    }



    /**
     * Computes the columns to show and text input field sizes.
     */
    protected void computeColumns() {
        // Compute language columns
        String[] theLanguages = ResourcesModule.getInstance().getSupportedLocaleNames().clone();
        for (int i = 0; i < theLanguages.length; i++) {
            theLanguages[i] = COLUMN_I18N_PREFIX + theLanguages[i];
        }

        // Compute columns array
        String[] theConfColumns, theConfSizes;
        if (columns == null) { // default columns to show
            theConfColumns = new String[] {COLUMN_DEFAULT, COLUMN_NAME, COLUMN_DESCRIPTION, LANGUAGE_VALUE};
        }
        else {
            theConfColumns = StringServices.toArray(columns, ",");
        }
        if (columnSizes == null) { // default sizes to use
            theConfSizes = new String[] {"", "", "", "", ""};
        }
        else {
            theConfSizes = StringServices.toArray(columnSizes, ",");
        }

        // Replace each element equals LANGUAGE_VALUE with all elements from theLanguages
        for (int i = 0; i < theConfColumns.length; i++) {
            if (LANGUAGE_VALUE.equals(theConfColumns[i])) {
                theConfColumns = expandArray(theConfColumns, i, theLanguages);
                if (i < theConfSizes.length) {
                    // expand sizes array also
                    theConfSizes = expandArray(theConfSizes, i, theLanguages.length);
                }
                i += theLanguages.length - 1;
            }
        }
        columnsArray = theConfColumns;

        // Compute sizes array
        int[] theSizes = new int[theConfColumns.length];
        Arrays.fill(theSizes, -1); // size < 0 means leave default
        if (columnSizes != null) {
            for (int i = 0; i < theConfSizes.length && i < theSizes.length; i++) {
                try {
                    theSizes[i] = Integer.parseInt(theConfSizes[i]);
                }
                catch (NumberFormatException e) {
                    // ignore no number - leave default size
                }
            }
        }
        columnSizesArray = theSizes;
    }

    private String[] expandArray(String[] aArray, int aIndex, String[] aSubArray) {
        String[] theResult = new String[aArray.length - 1 + aSubArray.length];
        System.arraycopy(aArray, 0, theResult, 0, aIndex);
        System.arraycopy(aSubArray, 0, theResult, aIndex, aSubArray.length);
        if (aIndex < aArray.length - 1)
            System.arraycopy(aArray, aIndex + 1, theResult, aIndex + aSubArray.length, aArray.length - 1 - aIndex);
        return theResult;
    }

    private String[] expandArray(String[] aArray, int aIndex, int aLength) {
        String[] theResult = new String[aArray.length - 1 + aLength];
        System.arraycopy(aArray, 0, theResult, 0, aIndex);
        Arrays.fill(theResult, aIndex, aIndex + aLength, aArray[aIndex]);
        if (aIndex < aArray.length - 1)
            System.arraycopy(aArray, aIndex + 1, theResult, aIndex + aLength, aArray.length - 1 - aIndex);
        return theResult;
    }



    /**
     * Creates the model for the TableField, this is a list of maps
     * (each row in table is a map).
     *
     * @return the model for the TableField
     */
    protected List createRowList(FormContext aContext) {
        ArrayList theRowList = new ArrayList();
		{
            FastList theFastList = (FastList)getModel();
            if (theFastList == null) return theRowList;
            FastListElement theDefault = theFastList.getDefaultElement();
            Iterator it = theFastList.elements().iterator();
            while (it.hasNext()) {
				idCounter++;
				FormGroup theGroup = new FormGroup(TABLE_EDIT_FIELDS_GROUP + idCounter, getResPrefix());
				aContext.addMember(theGroup);
                HashMap theRow = new HashMap();
                FastListElement theElement = (FastListElement)it.next();
				theGroup.setStableIdSpecialCaseMarker(theElement);
                theRow.put(COLUMN_SELF, theElement);
				theRow.put(COLUMN_NAME,
					createStringField(FIELD_PREFIX + COLUMN_NAME, theElement.getName(), theGroup, true));
				theRow.put(COLUMN_DESCRIPTION,
					createDescriptionField(FIELD_PREFIX + COLUMN_DESCRIPTION, theElement.getDescription(), theGroup));
				theRow.put(COLUMN_DEFAULT,
					createRadioButton(FIELD_PREFIX + COLUMN_DEFAULT, theElement == theDefault, theGroup));
                if (needInUse()) {
					theRow.put(COLUMN_IN_USE,
						createCheckButton(FIELD_PREFIX + COLUMN_IN_USE, isInUse(theElement), theGroup));
                }
                addMoreToRow(theRow, theElement, theGroup);
                setFieldSizes(theRow);
                theRowList.add(theRow);
            }
		}
        return theRowList;
    }

    /**
     * Hook for sub classes to add more stuff to a row.
     * In this class, a field for each supported language is added.
     */
    protected void addMoreToRow(Map aRow, FastListElement aElement, FormContainer aGroup) {
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			String localeName = locale.toString();
			aRow.put(COLUMN_I18N_PREFIX + localeName,
				createStringField(FIELD_PREFIX + COLUMN_I18N_PREFIX + localeName,
					Resources.getInstance(locale)
						.getStringOptional(FastListElementLabelProvider.labelKey(aElement)),
					aGroup));
        }
    }

    @Override
	public Object createNewRow(Control aControl) {
		newIdCounter--;
		FormGroup theGroup = new FormGroup(TABLE_EDIT_FIELDS_GROUP + newIdCounter, getResPrefix());
		getFormContext().addMember(theGroup);
        HashMap theRow = new HashMap();
        theRow.put(COLUMN_SELF, null);
		theRow.put(COLUMN_NAME,
			createStringField(FIELD_PREFIX + COLUMN_NAME, getNameFieldValue(aControl), theGroup, true));
		theRow.put(COLUMN_DESCRIPTION,
			createDescriptionField(FIELD_PREFIX + COLUMN_DESCRIPTION, null, theGroup));
		theRow.put(COLUMN_DEFAULT, createRadioButton(FIELD_PREFIX + COLUMN_DEFAULT, false, theGroup));
        if (needInUse()) {
			theRow.put(COLUMN_IN_USE, createCheckButton(FIELD_PREFIX + COLUMN_IN_USE, false, theGroup));
        }
        addMoreToNewRow(theRow, theGroup);
        setFieldSizes(theRow);
        refreshConstraintsAndListeners();
        return theRow;
    }

    /**
     * Hook for sub classes to add more stuff to a new row.
     * In this class, a field for each supported language is added.
     */
    protected void addMoreToNewRow(Map aRow, FormContainer aGroup) {
        String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
		for (int i = 0; i < supportedLanguages.length; i++) {
			aRow.put(COLUMN_I18N_PREFIX + supportedLanguages[i],
				createStringField(FIELD_PREFIX + COLUMN_I18N_PREFIX + supportedLanguages[i], null, aGroup));
        }
    }

    /**
     * Sets the sizes of the input fields.
     *
     * @param aRow
     *            the row map with the fields to set the sizes for
     */
    protected void setFieldSizes(Map aRow) {
        String[] theColumns = getColumns();
        int[] theSizes = getColumnSizes();
        for (int i = 0; i < theColumns.length; i++) {
            if (theSizes[i] < 0) continue;
            Object theList = aRow.get(theColumns[i]);
            if (theList instanceof List) {
                Object theControl = CollectionUtil.getFirst((List)theList);
                if (theControl instanceof TextInputControl) {
                    ((TextInputControl)theControl).setColumns(theSizes[i]);
                }
            }
        }
    }

    /**
     * Hook for sub classes to get a default value for the mandatory new name field.
     */
    protected String getNameFieldValue(Control aControl) {
        ObjectTableModel theModel = (ObjectTableModel)((TableListControl)aControl).getApplicationModel();
        int size = theModel.getRowCount();
        String prefix = StringServices.checkOnNullAndTrim((String)getFormContext().getField(FIELD_LIST_NAME).getValue());
        if (StringServices.isEmpty(prefix)) prefix = "element";
        prefix += '.';
        HashSet theSet = new HashSet(size);
		Iterator it = theModel.getAllRows().iterator();
        while (it.hasNext()) {
            theSet.add(FormFieldHelper.getStringValue(((Map)it.next()).get(COLUMN_NAME)));
        }
        String theName = prefix + size;
        if (!theSet.contains(theName)) return theName;

        for (int i = 0; i < size; i++) {
            theName = prefix + i;
            if (!theSet.contains(theName)) return theName;
        }
        return null;
    }

    @Override
	public void removeRow(Object aRowObject, Control aControl) {
        Map theRow = (Map)aRowObject;
        FormField theField = ((AbstractFormFieldControlBase)((List)theRow.get(COLUMN_NAME)).get(0)).getFieldModel();
        constraints.remove(theField);
        theField = ((AbstractFormFieldControlBase)((List)theRow.get(COLUMN_DEFAULT)).get(0)).getFieldModel();
        booleanFields.remove(theField);
        fieldHelper.removeObject(theRow.get(COLUMN_NAME), this);
        fieldHelper.removeObject(theRow.get(COLUMN_DESCRIPTION), this);
        fieldHelper.removeObject(theRow.get(COLUMN_DEFAULT), this);
        if (needInUse()) {
        	fieldHelper.removeObject(theRow.get(COLUMN_IN_USE), this);
        }
        removeMoreFromRow(theRow);
        refreshConstraintsAndListeners();
    }

    /**
     * Hook for sub classes to remove more stuff from a row.
     * In this class, the fields for each supported language is removed.
     */
    protected void removeMoreFromRow(Map aRow) {
        String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
		for (int i = 0; i < supportedLanguages.length; i++) {
            fieldHelper.removeObject(aRow.get(COLUMN_I18N_PREFIX + supportedLanguages[i]), this);
        }
    }

	@Override
    public ResKey allowRemoveRow(int row, TableData data, Control control) {
    	if (allowRemovingElementsInUse) {
    		return null;
    	}
		Map rowObject = (Map) data.getTableModel().getRowObject(row);
		if (!FormFieldHelper.getbooleanValue(rowObject.get(COLUMN_IN_USE))) {
			return null;
		} else {
			return com.top_logic.layout.table.I18NConstants.REMOVE_ROW_DISABLED;
		}
    }

    /**
     * Checks whether the isInUse calculation is needed.
     */
    protected boolean needInUse() {
    	return ArrayUtil.contains(getColumns(), COLUMN_IN_USE) || !allowRemovingElementsInUse;
    }

    /**
     * Checks whether the given FastListElement is in use or not.
     * This method may be expensive, so don't call it if it isn't necessary.
     */
    protected boolean isInUse(FastListElement element) {
		return ((KnowledgeObject) element.tHandle()).getIncomingAssociations().hasNext();
    }

    /**
     * Refreshes the field constraints and listeners.
     * This is needed because constraint dependences are cached.
     */
    protected void refreshConstraintsAndListeners() {
        String[] theColumns = getColumns();

        // Constraints
        if (ArrayUtil.contains(theColumns, COLUMN_NAME)) {
            if (dependency != null && dependency.isAttached()) {
                dependency.detach();
            }
            dependency = new UniqueValuesDependency((FormField[])constraints.toArray(new FormField[constraints.size()]));
            dependency.attach();
            checkFields();
        }

        // ValueListeners
        if (ArrayUtil.contains(theColumns, COLUMN_DEFAULT)) {
            if (listenerDependency != null && listenerDependency.isAttached()) {
                listenerDependency.detach();
            }
            listenerDependency = new RadioButtonValueListenerDependency((BooleanField[])booleanFields.toArray(new BooleanField[booleanFields.size()]), true);
            listenerDependency.attach();
        }
    }



    /**
     * Checks all fields except the empty ones for their constraints.
     */
    private void checkFields() {
        Iterator it = constraints.iterator();
        while (it.hasNext()) {
            FormField theField = (FormField)it.next();
            if (!StringServices.isEmpty(FormFieldHelper.getStringValue(theField))) {
                if (theField.isCheckRequired(true))
					theField.checkWithAllDependencies();
            }
        }
    }



    /**
     * The FormContext is rebuild every time because the specified list could
     * be changed in another component. The components model is set in SingleListMode.
     * @see com.top_logic.mig.html.layout.LayoutComponent#becomingVisible()
     */
    @Override
	protected void becomingVisible() {
        removeFormContext();
        if (isSingleListMode()) {
            setModel(createModel());
        }
        super.becomingVisible();
    }

    /**
	 * Gets the specified list as model for this component, if the component is in SingleListMode.
	 * The specified list will be created, if it doesn't exist. Returns <code>null</code> in
	 * MultiListMode.
	 * 
	 * @return the configured list or <code>null</code> if the component is in MultiListMode.
	 */
    protected FastList createModel() {
        if (!isSingleListMode()) return null;
        FastList theModel = FastList.getFastList(listname);
        if (theModel == null) {
			{
				KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
				theModel = FastList.createEnum(kb, listname, null, listtype, false);
                Logger.info("Specified list '" + listname + "' not found. A new one was created.", this);
				kb.commit();
			}
        }
        return theModel;
    }

    /**
     * Checks, if the list is in SingleListMode or in MultiListMode.
     *
     * @return <code>true</code>, if the list is in SingleListMode, <code>false</code> otherwise
     */
    public boolean isSingleListMode() {
        return (listname != null);
    }

    /**
     * Gets the name (ID) of the edited list.
     *
     * @return the ID of the edited list or <code>null</code>, if no model is set
     */
    public String getListname() {
        if (isSingleListMode()) {
            return listname;
        }
        else {
            Object theModel = getModel();
            return (theModel instanceof FastList) ? ((FastList)theModel).getName() : null;
        }
    }

    /**
     * Gets the type of the edited list.
     *
     * @return the ID of the edited list or <code>null</code>, if no model is set
     */
    public String getListtype() {
        if (isSingleListMode()) {
            return listtype;
        }
        else {
            Object theModel = getModel();
            return (theModel instanceof FastList) ? ((FastList)theModel).getClassificationType() : null;
        }
    }

    /**
     * This Component needs no model respectively sets its model itself, if it is in SingleListMode.
     * @see com.top_logic.layout.form.component.EditComponent#supportsInternalModel(java.lang.Object)
     */
    @Override
	protected boolean supportsInternalModel(Object aObject) {
		if (isSingleListMode()) {
			return aObject == null || Utils.equals(aObject, FastList.getFastList(listname));
		}
		else {
			return aObject == null || aObject instanceof FastList;
		}
    }

    /**
     * Command handler for applying changes made in the FastList or FastList.
     * This class is intended to be used only from the EditListComponent.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
	public static class ListApplyCommandHandler extends AbstractApplyCommandHandler {

        public static final String COMMAND = "ApplyList";

        public static final String CHANGED_FLAG = "[!*changed*!]";

        /**
         * I18N changes aren't saved immediately but after the successful commit. Therefore
         * I18N store actions are saved in this list and executed in the {@link #saveI18N()}
         * method. The list is a list of Object[] containing the Parameters.
         */
        protected List saveI18NList;

        

        public ListApplyCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
            FastList theFastList = (FastList)model;

            ObjectTableModel theTableModel = (ObjectTableModel)((TableField)formContext.getMember(FIELD_TABLELIST)).getTableModel();
			Accessor theAccessor = theTableModel.getTableConfiguration().getDefaultColumn().getAccessor();
            saveI18NList = new ArrayList();

            // Apply changes of the list
            FormField theField = formContext.getField(FIELD_LIST_NAME);
            String theListName = (String)theField.getValue();
            String[] supportedLanguages = ResourcesModule.getInstance().getSupportedLocaleNames();
			if (!StringServices.equalsEmpty(theFastList.getName(), theListName)) {
				ResKey theOldName = ResKey.legacy(theFastList.getName());
                for (int j = 0; j < supportedLanguages.length; j++) {
					saveI18NList.add(new Object[] { supportedLanguages[j], theOldName, null });
                }
            }
            theFastList.setName(theListName);
            theField = formContext.getField(FIELD_LIST_DESCRIPTION);
            theFastList.setDescription((String)theField.getValue());

            // Create I18N entries for the list
			ResKey listKey = TLModelNamingConvention.enumKey(theFastList);
            String theI18N;
            for (int j = 0; j < supportedLanguages.length; j++) {
				String language = supportedLanguages[j];
				theField = formContext.getField(FIELD_PREFIX + FIELD_I18N_PREFIX + language);
                theI18N = FormFieldHelper.getStringValue(theField);
				if (!StringServices.equalsEmpty(Resources.getInstance(language).getStringOptional(listKey),
					theI18N)) {
					saveI18NList.add(new Object[] { language, listKey, theI18N });
					theFastList.tTouch();
                }
            }

            // Create added elements
            HashSet theSet = new HashSet();
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                FastListElement theElement = (FastListElement)theAccessor.getValue(theRow, COLUMN_SELF);
                if (theElement == null) {
                    String theName = FormFieldHelper.getStringValue(theRow, COLUMN_NAME, theAccessor);
                    theElement = theFastList.addElement(null, theName, null, 0);
                    theAccessor.setValue(theRow, COLUMN_SELF, theElement);
                }
                theSet.add(theElement);
            }

            // Delete removed elements
            Iterator it = new ArrayList(theFastList.elements()).iterator();
            while (it.hasNext()) {
                FastListElement theElement = (FastListElement)it.next();
                if (!theSet.contains(theElement)) {
					ResKey key = ResKey.legacy(theElement.getName());
                    for (int j = 0; j < supportedLanguages.length; j++) {
						saveI18NList.add(new Object[] { supportedLanguages[j], key, null });
                    }
                    theFastList.removeElement(theElement);
                }
            }

            // Get old database keys
            // This is required to avoid commit failing when two lines are swapped
            // and their names are swapped, too
            String[] theNameKeys = new String[theFastList.size()];
            int[] theOrderKeys = new int[theFastList.size()];
            it = theFastList.elements().iterator();
            for (int i = 0; it.hasNext(); i++) {
                FastListElement theElement = (FastListElement)it.next();
                theNameKeys[i] = theElement.getName();
                theOrderKeys[i] = theElement.getIndex();
            }

            // Apply changes of elements
            theFastList.setDefaultElement(null);
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                FastListElement theElement = (FastListElement)theAccessor.getValue(theRow, COLUMN_SELF);

                boolean changed = false;
                String theName = FormFieldHelper.getStringValue(theRow, COLUMN_NAME, theAccessor);
                String theDescription = FormFieldHelper.getStringValue(theRow, COLUMN_DESCRIPTION, theAccessor);
                boolean theDefault = FormFieldHelper.getbooleanValue(theRow, COLUMN_DEFAULT, theAccessor);

                if (!StringServices.equals(theElement.getName(), theName)) {
					ResKey theOldName = ResKey.legacy(theElement.getName());
                    for (int j = 0; j < supportedLanguages.length; j++) {
						saveI18NList.add(new Object[] { supportedLanguages[j], theOldName, null });
                    }
                    theElement.setName(theName);
                    changed = true;
                }
                if (theElement.getIndex() != i) {
                    theElement.setOrder(i);
                    changed = true;
                }
                if (!StringServices.equals(theElement.getDescription(), theDescription)) {
                    theElement.setDescription(theDescription);
                }
                if (theDefault) {
                    theFastList.setDefaultElement(theElement);
                }

                // Create I18N entries for elements
				ResKey elementKey = TLModelNamingConvention.classifierKey(theElement);
                for (int j = 0; j < supportedLanguages.length; j++) {
					String language = supportedLanguages[j];
					theI18N = FormFieldHelper.getStringValue(theRow, COLUMN_I18N_PREFIX + language, theAccessor);
					if (StringServices.isEmpty(theI18N)) {
						theI18N = theName;
					}
					if (!StringServices.equalsEmpty(Resources.getInstance(language).getStringOptional(elementKey),
						theI18N)) {
						saveI18NList.add(new Object[] { language, elementKey, theI18N });
						theElement.tTouch();
                    }
                }

				if (changed) {
					// Check database key (Name + Order) to avoid duplicated database entries
					for (int j = 0; j < theNameKeys.length; j++) {
						if (StringServices.equals(theName, theNameKeys[j]) && (i == theOrderKeys[j])) {
							theElement.setName(theName + CHANGED_FLAG);
							break;
						}
					}
				}
            }

			theField = formContext.getField(FIELD_LIST_ORDERED);
			if (theField.isChanged()) {
				theFastList.setOrdered(((BooleanField) theField).getAsBoolean());
			}

            return true;
        }

        /**
		 * After changes are committed, the FastList gets refetched to resort their elements and the
		 * Resources are reloaded.
		 * 
		 * @see AbstractApplyCommandHandler#commit
		 */
        @Override
		public void commit(Transaction tx, Object model) {
			super.commit(tx, model);

			FastList classification = (FastList) model;
			if (removeChangedFlags(classification)) {
				Logger.info("Double commit was necessary for saving the list " + classification.getName(), this);
            }
			saveI18N();
        }

        /**
         * Executes the saved I18N store actions.
         */
        protected void saveI18N() {
			try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
				saveI18N(tx);
				tx.commit();
			}
        }

		private void saveI18N(ResourceTransaction tx) {
            if (saveI18NList == null || saveI18NList.isEmpty()) return;
            Iterator it = saveI18NList.iterator();
            while (it.hasNext()) {
                Object[] theParams = (Object[])it.next();
				ResKey key = (ResKey) theParams[1];
				String language = (String) theParams[0];
				tx.saveI18N(language, key, theParams[2] == null ? null : theParams[2].toString());
            }
            saveI18NList = new ArrayList();
        }

        /**
         * Removes CHANGED_FLAGs from a FastList and commits the list, if changed flags were found.
         *
         * @param aModel
         *            the fastList to remove changed flags from
         * @return <code>true</code> if the list had CHANGED_FLAGs which were removed,
         *         <code>false</code> otherwise
         */
		public static boolean removeChangedFlags(TLEnumeration aModel) {
            boolean result = false, changed = false;
			{
				Iterator<TLClassifier> it = aModel.getClassifiers().iterator();
                while (it.hasNext()) {
					TLClassifier theElement = it.next();
                    String theName = StringServices.getEmptyString(theElement.getName());
                    if (theName.endsWith(CHANGED_FLAG)) {
                        theElement.setName(theName.substring(0, theName.length() - CHANGED_FLAG.length()));
                        changed = true;
                    }
                }
                if (changed) {
					KnowledgeBase theBase = aModel.tKnowledgeBase();
                    result = ((theBase != null) && theBase.commit());
                    if (!result) {
                        Logger.error("Failed to remove changed flags from fast list.", ListApplyCommandHandler.class);
                    }
                }
			}
            return changed && result;
        }

    }



    /**
     * Dialog component for creating new fast lists or ordered lists.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class NewListComponent extends AbstractCreateComponent {

        public interface Config extends AbstractCreateComponent.Config {
			@Name(ATTRIBUTE_LISTTYPE)
			String getListtype();

			@Override
			@FormattedDefault("null()")
			public ModelSpec getModelSpec();

			@StringDefault(NewListCommandHandler.COMMAND)
			@Override
			String getCreateHandler();

		}

        public static final String ALLOW_ORDERED_LIST_SELECTION_ATTRIBUTE = "allowOrderedListSelection";


        /** The default list type of new lists. */
        protected String createType;

        /**
         * Creates a {@link NewListComponent}.
         */
        public NewListComponent(InstantiationContext context, Config aSomeAttrs) throws ConfigurationException {
            super(context, aSomeAttrs);
            
            this.createType = StringServices.nonEmpty(StringServices.nonEmpty(aSomeAttrs.getListtype()));
        }

        @Override
		public FormContext createFormContext() {
            boolean allowTypeSelection = createType == null;
            
			List options;
			if (allowTypeSelection) {
				options = getAllListTypes();
			} else {
				options = Collections.emptyList();
			}
			SelectField theTypeField = FormFactory.newSelectField(EditListComponent.FIELD_CLASSIFICATION_TYPE, options,
				false, !allowTypeSelection);
			
            theTypeField.setMandatory(true);
            theTypeField.setAsSingleSelection(createType);
            theTypeField.setOptionLabelProvider(new I18NResourceProvider(I18NConstants.LIST_TYPES));

            StringField nameField;
			FormField[] theFields = new FormField[] {
				nameField = FormFactory.newStringField(AbstractWrapper.NAME_ATTRIBUTE, true, false,
					LIST_NAME_LENGTH_CONSTRAINT),
					FormFactory.newStringField(FastList.DESC_ATTRIBUTE, !MANDATORY, !IMMUTABLE, LIST_DESCRIPTION_LENGTH_CONSTRAINT),
				FormFactory.newBooleanField(MULTIPLE_FILED_NAME),
                    theTypeField
            };
            nameField.addConstraint(ALLOWED_CHARS_CONSTRAINT);
            return new FormContext("newListComponent", this.getResPrefix(), theFields);
        }



        public List getAllListTypes() {
			ArrayList theList = new ArrayList(FastList.getAllListTypes());
            if (createType != null && !theList.contains(createType)) {
            	theList.add(createType);
            }
            return theList;
        }

		@Override
		protected void afterModelSet(Object oldModel, Object newModel) {
			super.afterModelSet(oldModel, newModel);

			removeFormContext(); // reset FormContext before new Model
		}

		@Override
		protected boolean supportsInternalModel(Object aObject) {
			return aObject == null || aObject instanceof Wrapper;
        }

    }



    /**
     * Command handler for creation of new lists.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class NewListCommandHandler extends AbstractCreateCommandHandler {

        public static final String COMMAND = "CreateNewList";

        public NewListCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

		@Override
		protected void validateAdditional(LayoutComponent component, FormContext context, Object model) {
			super.validateAdditional(component, context, model);
			String listName = FormFieldHelper.getStringValue(context.getField(FastList.NAME_ATTRIBUTE));
			if (FastList.listExists(listName)) {
				throw new TopLogicException(I18NConstants.DUPLICATE_LIST_NAME);
			}
		}

        @Override
		public FastList createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map<String, Object> arguments) {
			String theName = FormFieldHelper.getStringValue(formContainer.getField(FastList.NAME_ATTRIBUTE));
            String theDescription = FormFieldHelper.getStringValue(formContainer.getField(FastList.DESC_ATTRIBUTE));
			boolean theMultiFlag = FormFieldHelper.getbooleanValue(formContainer.getField(MULTIPLE_FILED_NAME));
			String theListType = (String) CollectionUtil.getFirst((List) FormFieldHelper
				.getProperValue(formContainer.getField(EditListComponent.FIELD_CLASSIFICATION_TYPE)));
			return createFastList(theName, theDescription, theMultiFlag, theListType);
        }

        public FastList createFastList(String aName, String aDescr, boolean isMultiSelect, String aListType) {
			KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
			return FastList.createEnum(kb, aName, aDescr, aListType, isMultiSelect);
        }
    }

	/**
	 * Prohibit deletion in single list mode.
	 */
	public static class SingleListModeRule implements ExecutabilityRule {
		/** Singleton {@link SingleListModeRule} instance. */
		public static final SingleListModeRule INSTANCE = new SingleListModeRule();

		private SingleListModeRule() {
			// singleton instance
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			EditListComponent theComp = (EditListComponent) aComponent;
			if (theComp.isSingleListMode()) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			return ExecutableState.EXECUTABLE;
		}
	}

	/**
	 * Prohibit editing of system lists.
	 */
	public static class NonSystemListExecutabilityRule implements ExecutabilityRule {

		public static final NonSystemListExecutabilityRule INSTANCE = new NonSystemListExecutabilityRule();

		@Override
		public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
			if (model == null || ((FastList) model).isSystem()) {
				return ExecutableState.createDisabledState(component.getResPrefix().key("notAllowedForSystemLists"));
			}
			return ExecutableState.EXECUTABLE;
		}
	}

    /**
     * Command handler for deletion of lists.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class DeleteListCommandHandler extends DeleteSimpleWrapperCommandHandler {

        public static final String COMMAND = "DeleteList";

		public static interface Config extends DeleteSimpleWrapperCommandHandler.Config {

			@Override
			@ListDefault({
				NonSystemListExecutabilityRule.class,
				SingleListModeRule.class
			})
			List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		}

        public DeleteListCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

    }

    /**
     * @see #createStringField(String, String, FormContainer, boolean)
     */
    protected Collection createStringField(String aName, String aValue, FormContainer aGroup) {
        return createStringField(aName, aValue, aGroup, false);
    }

    /**
	 * @see #createStringField(String, String, FormContainer, boolean)
	 */
	protected Collection createDescriptionField(String aName, String aValue, FormContainer aGroup) {
		StringField theField = fieldHelper.createStringField(aName, aValue, null, aGroup, false, !MANDATORY);
		theField.setLabel(getResPrefix().key(COLUMN_DESCRIPTION));
		theField.addConstraint(ELEMENT_DESCRIPTION_LENGTH_CONSTRAINT);
		return fieldHelper.createControls(theField, this);
	}

	/**
	 * Creates a new StringField, the corresponding control and TextInputControl and returns the
	 * controls as a collection.
	 * 
	 * @param aName
	 *        the name (ID) of the StringField
	 * @param aValue
	 *        the value to set
	 * @param aGroup
	 *        the FormGroup to add the SelectField
	 * @param aNameFieldFlag
	 *        a flag indicating whether the StringField is a NameField (is mandatory, gets a
	 *        IdentityConstraint)
	 * @return a list with the corresponding TextInputControl and ErrorControl
	 */
    protected List createStringField(String aName, String aValue, FormContainer aGroup, boolean aNameFieldFlag) {
        StringField theField = fieldHelper.createStringField(aName, aValue, null, aGroup, false, aNameFieldFlag);
        if (aNameFieldFlag) {
			theField.setLabel(getResPrefix().key(COLUMN_NAME));
			theField.addConstraint(ELEMENT_NAME_LENGTH_CONSTRAINT);
            theField.addConstraint(ALLOWED_CHARS_CONSTRAINT);
            constraints.add(theField);
        }
        return fieldHelper.createControls(theField, this);
    }

    /**
     * Creates a new BooleanField, the corresponding CheckboxControl and ErrorControl and
     * returns the controls as a collection. The Field is added to the fields for the
     * RadioButtonDependency.
     *
     * @param aName
     *            the name (ID) of the StringField
     * @param aValue
     *            the value to set
     * @param aGroup
     *            the FormGroup to add the SelectField
     * @return a list with the corresponding CheckboxControl and ErrorControl
     */
    protected List createRadioButton(String aName, boolean aValue, FormContainer aGroup) {
        BooleanField theField = fieldHelper.createBooleanField(aName, aValue, null, aGroup);
        booleanFields.add(theField);
        return fieldHelper.createControls(theField, this);
    }

    /**
     * Creates a new immutable BooleanField, the corresponding CheckboxControl and
     * ErrorControl and returns the controls as a collection.
     *
     * @param aName
     *            the name (ID) of the StringField
     * @param aValue
     *            the value to set
     * @param aGroup
     *            the FormGroup to add the SelectField
     * @return a list with the corresponding CheckboxControl and ErrorControl
     */
    protected List createCheckButton(String aName, boolean aValue, FormContainer aGroup) {
        BooleanField theField = fieldHelper.createBooleanField(aName, aValue, null, aGroup, true);
        return fieldHelper.createControls(theField, this);
    }

}

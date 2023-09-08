/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.currency;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.MapAccessor;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.NullModelSwitchEditCommandHandler;
import com.top_logic.layout.form.constraints.AbstractStringConstraint;
import com.top_logic.layout.form.constraints.NotEmptyConstraint;
import com.top_logic.layout.form.constraints.UniqueValuesDependency;
import com.top_logic.layout.form.control.AbstractFormFieldControlBase;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.ImageButtonRenderer;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.format.UpperCaseStringFormat;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.RowObjectCreator;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnCustomization;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.PagingModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * This class displays all {@link com.top_logic.knowledge.wrap.currency.Currency}s of the
 * system and allows to add, edit and remove them.
 *
 * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
 */
public class EditCurrencyListComponent extends EditComponent implements RowObjectCreator {

    public interface Config extends EditComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(XML_ATTRIBUTE_ALLOW_RENAMING)
		@BooleanDefault(false)
		boolean getAllowRenaming();

		@Override
		@StringDefault(ApplyCurrencyCommand.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(NullModelSwitchEditCommandHandler.COMMAND_ID)
		String getEditCommand();

		/**
		 * Model is not used, therefore component must be displayed
		 */
		@BooleanDefault(true)
		@Override
		boolean getDisplayWithoutModel();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			EditComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(RemoveCurrencyCommand.COMMAND);
		}

	}

	/** Constant of the currency format pattern. */
    public static final String CURRENCY_RATE_FORMAT_PATTERN = "#,##0.0000";

    /** Attribute indicating whether renaming of currencies is allowed. */
    public static final String XML_ATTRIBUTE_ALLOW_RENAMING = "allowRenaming";

    /** The default conversion factor of new currencies. */
    public static final Double DEFAULT_FACTOR = Double.valueOf(1.0000);

	// Columns of the table in this component:
    public static final String COLUMN_SELF = "column_self";
    public static final String COLUMN_NAME = "column_name";
    public static final String COLUMN_FACTOR = "column_factor";
    public static final String COLUMN_REMOVE = "column_remove";

    public static final String[] COLUMNS_VIEW_MODE = {COLUMN_NAME, COLUMN_FACTOR};
    public static final String[] COLUMNS_EDIT_MODE = {COLUMN_NAME, COLUMN_FACTOR, COLUMN_REMOVE};

    public static final String FIELD_PREFIX = "field_";
    public static final String FIELD_TABLE = "field_table";

	private static final String LABEL_SUFFIX = FIELD_TABLE + "." + COLUMN_NAME;

	private static final String FACTOR_SUFFIX = FIELD_TABLE + "." + COLUMN_FACTOR;
    public static final String REMOVE_ID = "remove_id";



    /** Indicates whether renaming of currencies is allowed. */
    private boolean allowRenaming;

    /** The number format used to format the currencies. */
    private NumberFormat numberFormat = new DecimalFormat(CURRENCY_RATE_FORMAT_PATTERN, new DecimalFormatSymbols(TLContext.getLocale()));

    private FormFieldHelper fieldHelper = new FormFieldHelper();

    private int idCounter;

    /** Saves the rows of the currency table. */
    private List rowList = new ArrayList();

    /** Saves the fields to check for identity. */
    private Set constraintFields = new HashSet();

    /** Saves the UniqueValuesDependency. */
    private UniqueValuesDependency dependency;



    /**
     * Constructor for the layout framework.
     *
     * @param someAttrs
     *        Attributes of the xml file.
     */
    public EditCurrencyListComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
        super(context, someAttrs);
        allowRenaming = someAttrs.getAllowRenaming();
    }

    @Override
	protected boolean supportsInternalModel(Object aObject) {
        // This component needs no model
        return aObject == null;
    }

    @Override
	public Object getTokenContextBase() {
        return Currency.getSystemCurrency();
    }

    /**
     * The FormContext is rebuild every time because the specified list could
     * be changed in another component. The components model is set in SingleListMode.
     * @see com.top_logic.mig.html.layout.LayoutComponent#becomingVisible()
     */
    @Override
	protected void becomingVisible() {
        removeFormContext();
        super.becomingVisible();
    }



    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext("editCurrencies", getResPrefix());
        constraintFields.clear();
        fieldHelper.clear();
        fieldHelper.setNumberFormat(numberFormat);
        idCounter = 0;
        String[] theColumns = isInEditMode() ? COLUMNS_EDIT_MODE : COLUMNS_VIEW_MODE;

		List theCurrencies = Currency.getAllCurrencies();

        rowList = new ArrayList();
        Iterator it = theCurrencies.iterator();
        while (it.hasNext()) {
            Currency theCurrency = (Currency)it.next();
            String theName = theCurrency.getName();
            boolean isRemoveable = Currency.isRemovableCurrency(theCurrency);

            HashMap theRow = new HashMap();
            theRow.put(COLUMN_SELF, theCurrency);
            theRow.put(COLUMN_NAME, createNameField(theName, theContext, isRemoveable));
            theRow.put(COLUMN_FACTOR, createFactorField(Double.valueOf(theCurrency.getConversionFactor()), theContext));
            if (isRemoveable) {
                theRow.put(COLUMN_REMOVE, createButton(theContext));
            }
            rowList.add(theRow);
            idCounter++;
        }

		TableConfiguration config = TableConfiguration.table();
		config.setResPrefix(getResPrefix().append(FIELD_TABLE));
		config.setDefaultFilterProvider(null);
		config.setColumnCustomization(ColumnCustomization.ORDER);
		ColumnConfiguration defaultColumn = config.getDefaultColumn();
		defaultColumn.setAccessor(MapAccessor.INSTANCE);
		defaultColumn.setFilterProvider(null);
		config.setPageSizeOptions(PagingModel.SHOW_ALL);
		adaptTableConfiguration(FIELD_TABLE, config);
		// create currency table
		ObjectTableModel theTableModel = new ObjectTableModel(theColumns, config, rowList);
        TableField theTable = FormFactory.newTableField(FIELD_TABLE, theTableModel, false);
        theContext.addMember(theTable);

        return theContext;
    }



    private List createNameField(String aValue, FormContainer aGroup, boolean isRemoveable) {
        String theName = FIELD_PREFIX + COLUMN_NAME + idCounter;
		String theLabel = Resources.getInstance().getMessage(getResPrefix().key(LABEL_SUFFIX), aValue);
        ComplexField theField = fieldHelper.createComplexField(theName, aValue, theLabel, aGroup, !allowRenaming || !isRemoveable);
        theField.setFormatAndParser(UpperCaseStringFormat.INSTANCE);
        theField.addConstraint(CurrencyConstraint.INSTANCE);
        constraintFields.add(theField);
        List theControls = fieldHelper.createControls(theField, this);
        ((TextInputControl)CollectionUtil.getFirst(theControls)).setColumns(3);
        return theControls;
    }

    private List createFactorField(Double aValue, FormContainer aGroup) {
        String theName = FIELD_PREFIX + COLUMN_FACTOR + idCounter;
		String theLabel = Resources.getInstance().getMessage(getResPrefix().key(FACTOR_SUFFIX), aValue);
        ComplexField theField = fieldHelper.createComplexField(theName, aValue, theLabel, aGroup);
        theField.addConstraint(NotEmptyConstraint.INSTANCE);
        List theControls = fieldHelper.createControls(theField, this);
        ((TextInputControl)CollectionUtil.getFirst(theControls)).setColumns(16);
        return theControls;
    }

    private ButtonControl createButton(FormContainer aGroup) {
		String theLabel = Resources.getInstance().getString(getResPrefix().key("delete"));
		Map arguments = Collections.singletonMap(REMOVE_ID, String.valueOf(idCounter));
		CommandModel theCommandModel =
			CommandModelFactory.commandModel(getCommandById(RemoveCurrencyCommand.COMMAND), this, arguments);
        theCommandModel.setLabel(theLabel);
        ButtonControl theButton = new ButtonControl(theCommandModel, ImageButtonRenderer.INSTANCE);
		theButton.setImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON);
		theButton.setDisabledImage(com.top_logic.layout.form.tag.Icons.DELETE_BUTTON_DISABLED);
        return theButton;
    }



    @Override
	public Object createNewRow(Control aControl) {
        FormContext theContext = getFormContext();
        HashMap theRow = new HashMap();
        theRow.put(COLUMN_NAME, createNameField(null, theContext, true));
        theRow.put(COLUMN_FACTOR, createFactorField(DEFAULT_FACTOR, theContext));
        theRow.put(COLUMN_REMOVE, createButton(theContext));
        rowList.add(theRow);
        idCounter++;
        refreshDependency();
        return theRow;
    }


    /**
     * Refreshes the field constraints.
     * This is needed because constraint dependences are cached.
     */
    private void refreshDependency() {
        if (dependency != null && dependency.isAttached()) {
            dependency.detach();
        }
        dependency = new UniqueValuesDependency((FormField[])constraintFields.toArray(new FormField[constraintFields.size()]));
        dependency.attach();
        checkFields();
    }


    /**
     * Checks all fields except the empty ones for their constraints.
     */
    private void checkFields() {
        Iterator it = constraintFields.iterator();
        while (it.hasNext()) {
            FormField theField = (FormField)it.next();
            if (!StringServices.isEmpty(FormFieldHelper.getStringValue(theField))) {
                if (theField.isCheckRequired(true))
					theField.checkWithAllDependencies();
            }
        }
    }


    @Override
	public void writeBody(ServletContext aContext, HttpServletRequest aRequest,HttpServletResponse aResponse, TagWriter aOut) throws IOException, ServletException {
        super.writeBody(aContext, aRequest, aResponse, aOut);
        refreshDependency();
    }



    /**
     * Constraint that checks whether the given String exactly consists of 3 upper case
     * letters.
     *
     * @author <a href=mailto:Christian.Braun@top-logic.com>Christian Braun</a>
     */
    public static class CurrencyConstraint extends AbstractStringConstraint {

        public static final CurrencyConstraint INSTANCE = new CurrencyConstraint();

        @Override
		protected boolean checkString(String aValue) throws CheckException {
            NotEmptyConstraint.INSTANCE.check(aValue);
            if (aValue.matches("[A-Z]{3}")) return true;
			throw new CheckException(Resources.getInstance().getString(I18NConstants.INVALID_CURRENCY));
        }

    }



    /**
     * Command to remove a currency from the list.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class RemoveCurrencyCommand extends AJAXCommandHandler {

        public final static String COMMAND = "RemoveCurrencyCommand";

        public RemoveCurrencyCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
            EditCurrencyListComponent theComponent = ((EditCurrencyListComponent)aComponent);
            int index = FormFieldHelper.getintValue(aArguments.get(REMOVE_ID));
            ObjectTableModel theTableModel = (ObjectTableModel)((TableField)theComponent.getFormContext().getMember(FIELD_TABLE)).getTableModel();
            Map theRow = (Map)theComponent.rowList.get(index);
            theTableModel.removeRowObject(theRow);
            FormField theField = ((AbstractFormFieldControlBase)((List)theRow.get(COLUMN_NAME)).get(0)).getFieldModel();
            theComponent.constraintFields.remove(theField);
            theComponent.fieldHelper.removeObject(theRow.get(COLUMN_NAME), theComponent);
            theComponent.fieldHelper.removeObject(theRow.get(COLUMN_FACTOR), theComponent);
            theComponent.fieldHelper.removeObject(theRow.get(COLUMN_REMOVE), theComponent);
            theComponent.refreshDependency();
            return HandlerResult.DEFAULT_RESULT;
        }

    }



    /**
     * Stores the changes made with this EditCurrencyListComponent.
     * This class is intended to be used only from the EditCurrencyListComponent.
     *
     * @author <a href=mailto:CBR@top-logic.com>CBR</a>
     */
    public static class ApplyCurrencyCommand extends AbstractApplyCommandHandler {

        /** The unique ID for this command */
        public final static String COMMAND_ID = "applyCurrencyList";

        public ApplyCurrencyCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean supportsModel(Object aTheModel) {
            return true;
        }


        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {

            ObjectTableModel theTableModel = (ObjectTableModel)((TableField)formContext.getMember(FIELD_TABLE)).getTableModel();
			Accessor theAccessor = theTableModel.getTableConfiguration().getDefaultColumn().getAccessor();
            boolean changed = false;

            // Saves the currencies which gets not deleted.
            // This needs to be a list instead of a set for the case that an element gets
            // renamed and a new element with the old name gets created.
            ArrayList theRemainingList = new ArrayList();

            // Create added elements
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                Currency theCurrency = (Currency)theAccessor.getValue(theRow, COLUMN_SELF);
                if (theCurrency == null) {
                    String theName = FormFieldHelper.getStringValue(theRow, COLUMN_NAME, theAccessor);
                    Double theFactor = FormFieldHelper.getDoubleValue(theRow, COLUMN_FACTOR, theAccessor);
                    theCurrency = createCurrency(theName, theFactor);
                    theAccessor.setValue(theRow, COLUMN_SELF, theCurrency);
                    changed = true;
                }
                theRemainingList.add(theCurrency);
            }

            // Apply changes of currencies
            for (int i = 0; i < theTableModel.getRowCount(); i++) {
                Map theRow = (Map)theTableModel.getRowObject(i);
                Currency theCurrency = (Currency)theAccessor.getValue(theRow, COLUMN_SELF);
                String theName = FormFieldHelper.getStringValue(theRow, COLUMN_NAME, theAccessor);
                double theFactor = FormFieldHelper.getdoubleValue(theRow, COLUMN_FACTOR, theAccessor);

                if (!StringServices.equals(theCurrency.getName(), theName)) {
                    // Name change is not possible because the KO ID is set to the name and is used
                    // on many places in the application, which is very bad. Therefore the currency
                    // must be deleted and a new currency with the new name must be created.
                    theRemainingList.remove(theCurrency);
                    theCurrency = createCurrency(theName, theFactor);
                    theRemainingList.add(theCurrency);
                    changed = true;
                }
                if (theCurrency.getConversionFactor() != theFactor) {
                    theCurrency.setConversionFactor(theFactor);
                    changed = true;
                }
            }

            // Delete removed elements
			{
                Iterator it = Currency.getAllCurrencies().iterator();
                while (it.hasNext()) {
                    Currency theCurrency = (Currency)it.next();
                    if (!theRemainingList.contains(theCurrency)) {
                        removeCurrency(theCurrency);
                        changed = true;
                    }
                }
            }

            return changed;
        }


        /**
         * Creates a new currency. If the currency already exists, the existing will be
         * returned. That is BAD, but this is required because the currency IDs are set
         * manually and currencies are referenced with their ID instead of their name
         * everywhere in the application.
         *
         * @param aName
         *        the name of the new currency; must NOT be <code>null</code>
         * @param aFactor
         *        the conversion factor of the currency
         * @return the new created currency
         */
        private Currency createCurrency(String aName, Double aFactor) {
            Currency theCurrency = Currency.getCurrencyInstance(aName);
            if (theCurrency == null) {
                theCurrency = Currency.createCurrency(aName, 2048);
            }
            theCurrency.setConversionFactor(aFactor);
            return theCurrency;
        }

        /**
         * Creates a new currency.
         *
         * @param aName
         *        the name of the new currency; must NOT be <code>null</code>
         * @param aFactor
         *        the conversion factor of the currency
         * @return the new created currency
         */
        private Currency createCurrency(String aName, double aFactor) {
            return createCurrency(aName, Double.valueOf(aFactor));
        }

        /**
         * Deletes a currency.
         *
         * @param aCurrency
         *        the currency to delete
         */
        private void removeCurrency(Currency aCurrency) {
            aCurrency.tDelete();
        }

    }

}

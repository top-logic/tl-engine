/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.event.layout;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.event.logEntry.LogEntry;
import com.top_logic.event.logEntry.LogEntryConfiguration;
import com.top_logic.event.logEntry.LogEntryDisplayGroup;
import com.top_logic.event.logEntry.LogEntryFilter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.DateComparisonDependency;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.table.model.CachedObjectTableModel;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link LayoutComponent} displaying a filter for {@link LogEntry} elements.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class LogEntryFilterComponent  extends FormComponent {

	public interface Config extends FormComponent.Config {
		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name("allowedEventGroups")
		String getAllowedEventGroups();

		@Name(XML_ATTR_TABLE_NAME)
		@Mandatory
		String getTableName();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(UpdateLogEntriesCommandHandler.COMMAND_ID);
		}

	}

	/** Name of the table showing the log entries */
	private static final String XML_ATTR_TABLE_NAME = "tableName";

	/** The name of the start date. */
    public static final String START_DATE       = "startDate";
    /** The name of the end date. */
    public static final String END_DATE         = "endDate";
    
    private LogEntryConfiguration configuration = LogEntryConfiguration.getInstance();

    /**
     * the event groups shown by this component
     * these groups can be configured in the layout xml-file
     * if no types are configured, all types will be shown (default)
     */
    /** Set<LogEntryDisplayGroup> */
    private Set allowedDisplayGroups;

	private String _tableName;

    public LogEntryFilterComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);

        this.allowedDisplayGroups = new TreeSet();
        String theConf = atts.getAllowedEventGroups();
        if (!StringServices.isEmpty(theConf)) { // configured groups
            for (Iterator theIter = StringServices.toList(theConf, ',').iterator(); theIter.hasNext();) {
                String               theGroupName = (String) theIter.next();
                LogEntryDisplayGroup theGroup = configuration.getDisplayGroup(theGroupName);
                if (theGroup != null) {
                    this.allowedDisplayGroups.add(theGroup);
                }
                else {
                    Logger.warn(this.getName() + " misconfiguration: DisplayGroup '"+theGroupName+"' is unknown to LogEntryConfiguration", this);
                }
            }
        }
        else { // all groups
            this.allowedDisplayGroups.addAll(configuration.getDisplayGroups());
        }
		this._tableName = atts.getTableName();
    }

    @Override
	public FormContext createFormContext() {
        ComplexField theStartField  = FormFactory.newDateField(START_DATE, null, false);
        theStartField.setMandatory(true);
        ComplexField theEndField    = FormFactory.newDateField(END_DATE, null, false);
        theEndField.setMandatory(true);
        LogEntryFilter theModel = (LogEntryFilter) getModel();
        theStartField.setValue(theModel.getStartDate());
        theEndField.setValue  (theModel.getEndDate());

		DateComparisonDependency.buildStartEndWithEqualDependency(DateComparisonDependency.GRANULARITY_TYPE_DAY,
			theStartField, theEndField);

        // This code has the result that both fields get an error message, if one changes
        // and fails the comparison dependency.
        theStartField.checkWithAllDependencies();
        theEndField.checkWithAllDependencies();

		FormContext theContext = new FormContext("FormContext", getResPrefix());
        theContext.addMember(theStartField);
        theContext.addMember(theEndField);
        
        theContext.addMember(this.getLogEntryTable());

        return theContext;
    }

    /** 
     * Create the table model out of the given select field.
     * 
     * @return   The requested table model, never <code>null</code>.
     */
    protected TableModel createTableModel() {
        List<LogEntry>     theItems    = this.getItems();
		TableConfiguration theManager = this.getTableConfiguration(_tableName);

		return new CachedObjectTableModel(null, theManager, theItems);
    }

	private TableField getLogEntryTable() {
		return FormFactory.newTableField(_tableName, this.createTableModel());
	}

	@SuppressWarnings("unchecked")
	private List<LogEntry> getItems() {
		return (List<LogEntry>) LogEntryBuilder.INSTANCE.getModel(getModel(), this);
	}

    @Override
	protected void becomingVisible() {
    	super.becomingVisible();
		removeFormContext();
	}

    public Set getAllowedDisplayGroups() {
        return this.allowedDisplayGroups;
    }

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(initialModel());
		}
		return super.validateModel(context);
	}

	private Object initialModel() {
		LogEntryFilter model;
            if (!this.allowedDisplayGroups.isEmpty()) {
                Set theDisplayGroupTypeNames  = new HashSet(this.allowedDisplayGroups.size() * 3); // holds keys like "Document.created"

                // get all event types for all groups and create a new filter
                for (Iterator theIter = this.allowedDisplayGroups.iterator(); theIter.hasNext();) {
                    LogEntryDisplayGroup theGroup = (LogEntryDisplayGroup) theIter.next();
                    for (Iterator theTypes = theGroup.getEventTypes().iterator(); theTypes.hasNext(); ) {
                        String theType = (String) theTypes.next();
                        theDisplayGroupTypeNames.add(theGroup.getName() + "."+theType);
                    }
                }

                // Get the allowed display groups and use the information of
                // the personal configuration.
			Set personalEventTypes = LogEntryFilter.getConfiguredEventTypes(getName());
                theDisplayGroupTypeNames.retainAll(personalEventTypes);
                model = new LogEntryFilter(LogEntryFilter.getInitialStartDate(), LogEntryFilter.getInitialEndDate(), theDisplayGroupTypeNames);
            }
            else { // never reached ?
			model = new LogEntryFilter(getName());
            }
            Selectable selma = getSelectableMaster();
            if (selma != null) {
                model.setSelection(selma.getSelected());
            }

		return model;
    }

	protected void updateModel() {
        FormContext theCtx = getFormContext();
        FormField theField = theCtx.getField(START_DATE);
        Date startDate  = (Date)(theField.hasValue() ? theField.getValue() : null);
        theField = theCtx.getField(END_DATE);
        Date endDate    = (Date)(theField.hasValue() ? theField.getValue() : null);

        if (startDate == null || endDate == null) return;

        LogEntryFilter theModel = (LogEntryFilter) getModel();
        theModel.setDates(startDate, endDate);

		TableField theTable = (TableField) theCtx.getMember(_tableName);
        ObjectTableModel theTableModel = (ObjectTableModel) theTable.getTableModel();
        List<LogEntry>   theItems      = this.getItems();

		theTableModel.setRowObjects(theItems);
		theTableModel.updateRows(0, theItems.size());
    }

    /**
     * The UpdateStatusReportCommandHandler updates the component.
     */
	public static class UpdateLogEntriesCommandHandler extends AbstractCommandHandler {

        /** ID for this command handler. */
        public static final String COMMAND_ID = "updateLogEntries";

        public UpdateLogEntriesCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            if (aComponent instanceof LogEntryFilterComponent) {
                LogEntryFilterComponent theComponent = (LogEntryFilterComponent) aComponent;

                if (theComponent.getFormContext().checkAll()) {
                    theComponent.updateModel();
                    theComponent.invalidate();
                }
            }
            return HandlerResult.DEFAULT_RESULT;
        }

    }

}

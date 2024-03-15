/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.meta.search;

import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.expression.SaveExpressionCommand;
import com.top_logic.element.layout.meta.search.DeleteQueryCommandHandler;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.element.layout.meta.search.SearchCommandHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.layout.flexreporting.component.ChartConfigurationComponent;
import com.top_logic.reporting.layout.flexreporting.component.ReportQuerySelectorComponent;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.wrap.StoredReport;
import com.top_logic.reporting.report.xmlutilities.ReportWriter;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.tool.execution.NullModelDisabled;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * This class offers field support for publishing reports and queries. It also contains the command
 * handlers needed for the search analysis functionality.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ReportingCommandSupport implements ReportConstants{
	
	public static FormGroup getPublishingFormGroup(boolean visible, ResPrefix aResPrefix, ValueListener aListener) {
		FormGroup    theGroup          = new FormGroup(QueryUtils.FORM_GROUP, aResPrefix);
		BooleanField publishQueryField = FormFactory.newBooleanField(QueryUtils.PUBLISH_QUERY_FIELD);
		
		publishQueryField.setAsBoolean(false);
		theGroup.addMember(publishQueryField);

		List visibleGroups = Group.getAll();
		final SelectField visibleGroupsField = FormFactory.newSelectField(QueryUtils.VISIBLE_GROUPS_FIELD, visibleGroups, true, false);
		
		visibleGroupsField.setDisabled(true);
		theGroup.addMember(visibleGroupsField);

		theGroup.setVisible(visible);

		publishQueryField.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
				if (((BooleanField) aField).getAsBoolean()) {
					visibleGroupsField.setDisabled(false);
					visibleGroupsField.setMandatory(true);
				}
				else {
					visibleGroupsField.setDisabled(true);
					visibleGroupsField.setMandatory(false);
				}
			}
		});
		
		publishQueryField.addValueListener(aListener);
		
		return theGroup;
	}

    public static class WriteReportCommandHandler extends SaveExpressionCommand {

    	public static final String COMMAND_ID = "writeReport";
    	
		
		
		public WriteReportCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			ChartConfigurationComponent  theCCComp  = (ChartConfigurationComponent) component;
			ReportQuerySelectorComponent theRSQComp = theCCComp.getReportQuerySelector();

			StoredReport      theSelectedReport = theRSQComp.getStoredReport();
			ChartComponent    theChartComp      = theCCComp.getChartComponent();

			if (theSelectedReport == null || theChartComp == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			FormContext       theCtx            = theRSQComp.getFormContext();

			if (!theCtx.checkAll()) {
				HandlerResult theResult = new HandlerResult();
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, theCtx, theResult);
				return theResult;
	        }
			publish(theSelectedReport, theCtx);
			try {
    			String theString = ReportWriter.writeReportConfig(theCCComp.getReportConfiguration());
    			theSelectedReport.setValue(StoredReport.ATTRIBUTE_REPORT, theString);
    			theSelectedReport.setBOType(theRSQComp.getObjectType());
    			KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase().commit();
    			theRSQComp.getFormContext().getField(REPORT_SELECTION_FIELD).setDisabled(false);
			} 
			catch (XMLStreamException x) {
			    Logger.error("Unable to write report to xml", x, ReportQuerySelectorComponent.class);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
		
	    @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(
				InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE, OnlyOwnerRule.INSTANCE);
	    }
    }
    
	public static class OnlyOwnerRule implements ExecutabilityRule {

		/**
		 * Singleton {@link OnlyOwnerRule} instance.
		 */
		public static final OnlyOwnerRule INSTANCE = new OnlyOwnerRule();

		private OnlyOwnerRule() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (model == null) {
				return ExecutableState.NO_EXEC_NO_MODEL;
			}
			if (!(model instanceof StoredReport)) {
				return ExecutableState.NO_EXEC_NOT_SUPPORTED;
			}
			Person creator = ((StoredReport) model).getCreator();
			Person currentUser = TLContext.getContext().getCurrentPersonWrapper();

			if (!WrapperHistoryUtils.equalsUnversioned(currentUser, creator) || Person.isAdmin(currentUser)) {
				return ExecutableState.NO_EXEC_PERMISSION;
			}

			return ExecutableState.EXECUTABLE;
		}
	}

    public static class ResetStoredReportCommandHandler extends AJAXCommandHandler {
    	
    	public static final String COMMAND_ID = "resetStoredReport";
    	
		/**
		 * Configuration options for
		 * {@link ReportingCommandSupport.ResetStoredReportCommandHandler}.
		 */
		public interface Config extends AJAXCommandHandler.Config {

			@Override
			@BooleanDefault(true)
			boolean getConfirm();

		}
    	
		public ResetStoredReportCommandHandler(InstantiationContext context, Config config) {
	        super(context, config);
        }

		@Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
			ChartConfigurationComponent  theCCComp  = (ChartConfigurationComponent) component;
			ReportQuerySelectorComponent theRSQComp = theCCComp.getReportQuerySelector();
	        StoredReport                 theReport  = theRSQComp.getStoredReport();

	        theCCComp.setStoredReport(theReport);
	        theRSQComp.acceptStoredReport(theReport);
	        theRSQComp.setChanged(false);
	        
			return HandlerResult.DEFAULT_RESULT;
        }
		
	    @Override
		@Deprecated
		public ExecutabilityRule createExecutabilityRule() {
	    	return CombinedExecutabilityRule.combine(InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE);
	    }
    }
    
    public static class DeleteReportCommandHandler extends DeleteQueryCommandHandler {

    	public static final String	COMMAND_ID	= "deleteReport";

		/**
		 * Configuration for {@link DeleteReportCommandHandler}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends DeleteQueryCommandHandler.Config {

			@FormattedDefault(QueryUtils.OWNER_WRITE_NAME)
			@Override
			CommandGroupReference getGroup();

		}

		public DeleteReportCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
    	}

    	@Override
		@Deprecated
		public ResKey getDefaultI18NKey() {
			return I18NConstants.DELETE_REPORT;
    	}

    	@Override
		public HandlerResult handleCommand(DisplayContext commandContext, LayoutComponent component, Object model, Map<String, Object> arguments) {
    		ChartConfigurationComponent  theComp    = (ChartConfigurationComponent) component;
    		ReportQuerySelectorComponent theRQSComp = theComp.getReportQuerySelector();
    		StoredReport                 theReport  = theRQSComp.getStoredReport();
    		HandlerResult                theResult  = new HandlerResult();

    		if (theReport != null) {
    			try {
    				KnowledgeBase theKB = this.deleteStoredWrapper(theReport);

    				if (theKB.commit()) {
    					this.updateComponent(theRQSComp, theReport);
    					theComp.invalidateButtons();
    				}
    				else {
						theResult.addErrorMessage(I18NConstants.ERROR_DELETE_FAILED);

    					Logger.error("Failed to commit deleting stored report " + theReport, this);
    				}
    			}
    			catch (Exception ex) {
    				theResult.setException(new TopLogicException(this.getClass(), "reporting.report.delete.failed", ex));
    			}
    		}

    		return theResult;
    	}

    	protected void updateComponent(ReportQuerySelectorComponent aComp, StoredReport aReport) {
    		if (aComp.hasFormContext()) {
    			SelectField theField = (SelectField) aComp.getFormContext().getField(ReportConstants.REPORT_SELECTION_FIELD);
    			updateField(theField, aReport);
    			theField.setDisabled(false);
    		}
    	}
    	
    	@Override
		@Deprecated
    	public ExecutabilityRule createExecutabilityRule() {
			return CombinedExecutabilityRule.combine(
				InViewModeExecutable.INSTANCE, NullReportDisabled.INSTANCE, OnlyOwnerRule.INSTANCE);
    	}
    }
    
    public static class NullReportDisabled extends NullModelDisabled {

		protected static final ExecutableState EXEC_STATE_DISABLED =
			ExecutableState.createDisabledState(I18NConstants.ERROR_NO_REPORT);

    	public static final ExecutabilityRule INSTANCE = new NullReportDisabled();

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (aComponent instanceof ChartConfigurationComponent) {
				ChartConfigurationComponent  theComp    = (ChartConfigurationComponent) aComponent;
	    		ReportQuerySelectorComponent theRQSComp = theComp.getReportQuerySelector();
	    		StoredReport                 theReport  = theRQSComp.getStoredReport();
	    		
	    		if (theReport == null) {
	    			return NullReportDisabled.EXEC_STATE_DISABLED;
	    		}
	    		return ExecutableState.EXECUTABLE;
			}
			else {
				return super.isExecutable(aComponent, model, someValues);
			}
		}
    }
    
    public static class ReportingSearchCommandHandler extends SearchCommandHandler {

        public static final String COMMAND_ID = "reportingSearchAttributed";
        
    	
    	
    	
    	
    	public ReportingSearchCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
    	}
    	
    	@Override
    	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
    		HandlerResult               theResult = new HandlerResult();
    		ChartConfigurationComponent theCCComp = (ChartConfigurationComponent) aComponent;
            
            try {
                FormContext               theContext = theCCComp.getFormContext();

                if (theContext.checkAll()) {
                	theCCComp.fireModelModifiedEvent(theCCComp.getReportConfiguration(), theCCComp);
                }
                else {
					AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
                }
            }
            catch (Exception ex) {
                Logger.error("Unable to update '" + aComponent + "'", ex, this);

				theResult.addErrorText(ex.toString());
            }
    		return theResult;
    	}
    }
}

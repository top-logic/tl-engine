/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.expression.NewExpressionCommand;
import com.top_logic.element.layout.meta.search.PublishableFieldSupport;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.mig.html.layout.Expandable;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.report.model.RevisedReport;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.wrap.StoredReport;
import com.top_logic.reporting.report.xmlutilities.ReportWriter;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class PublishableNewStoredReportComponent extends AbstractCreateComponent implements Expandable, ReportConstants{

	/**
	 * Configuration for the {@link PublishableNewStoredReportComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCreateComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@StringDefault(PublishableNewStoredReportCommandHandler.COMMAND_ID)
		@Override
		String getCreateHandler();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			AbstractCreateComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	public PublishableNewStoredReportComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
	    super(context, someAttrs);
    }

	@Override
	public FormContext createFormContext() {
		
		FormField theName = newNameField();

        FormContext theContext = new FormContext("default", this.getResPrefix(), new FormField[] {theName});
		
		theContext = PublishableFieldSupport.createFormContext(this, theContext, this.getResPrefix());
	    
		return theContext;
	}

	private FormField newNameField() {
		String fieldName = PublishableNewStoredReportCommandHandler.NAME_ATTRIBUTE;
		StringLengthConstraint lengthConstraint =
			new StringLengthConstraint(1, PublishableNewStoredReportCommandHandler.NAME_LENGTH);
		FormField nameField = FormFactory.newStringField(fieldName, "", false, false, lengthConstraint);
		nameField.setMandatory(true);
		return nameField;
	}

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return (anObject instanceof RevisedReport);
    }
	
	@Override
	public boolean isExpanded() {
		return allow(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
	}
	
    @Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);
		
    	ChartConfigurationComponent  theCCComp  = (ChartConfigurationComponent) this.getDialogParent();
		ReportQuerySelectorComponent theRQSComp = theCCComp.getReportQuerySelector();
		
		
		theRQSComp.acceptStoredReport((StoredReport) newModel);
//		if (theMaster instanceof ChartComponent) {
//			// TODO #294/TBE: Looks strange to send a create event at from the model setter. 
//			theMaster.fireModelCreatedEvent(aModel, theMaster);
//			return true;
//		}
//		return false;
	}
    
	public static class PublishableNewStoredReportCommandHandler extends NewExpressionCommand {


		/** The name to be used for creating the new StoredReport. */
		public static final int		NAME_LENGTH		 = 40;

		/** The ID of this command handler. */
		public static final String	COMMAND_ID		 = "publishabelNewStoredReport";

	    public static final String  NAME_ATTRIBUTE   = "name";
	    public static final String  REPORT_ATTRIBUTE = "report";

		/**
		 * Creates a {@link PublishableNewStoredReportCommandHandler}.
		 */
		public PublishableNewStoredReportCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
		}

		/**
		 * @see com.top_logic.layout.form.component.AbstractCreateCommandHandler#createObject(com.top_logic.mig.html.layout.LayoutComponent,
		 *      java.lang.Object, FormContainer, java.util.Map)
		 */
		protected StoredReport createNewReport(FormContext aContext, LayoutComponent aComponent) throws Exception {
			ChartConfigurator theMaster = (ChartConfigurator) aComponent.getDialogParent();

//			ChartConfigurator theMaster = (ChartConfigurator)theParent.getMaster();
			ReportQuerySelectorComponent theMastersMaster = theMaster.getReportQuerySelector();
			String theMEType = theMastersMaster.getObjectType();
			
			String theName = (String) (aContext.getField(NAME_ATTRIBUTE)).getValue();
			KnowledgeBase theKB = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
			KnowledgeObject theObject = theKB.createKnowledgeObject(StoredReport.KO_TYPE);
			StoredReport theStoredReport = (StoredReport) WrapperFactory.getWrapper(theObject);
			storeOwner(theStoredReport);
			String theString = ReportWriter.writeReportConfig(theMaster.getReportConfiguration());
			theStoredReport.setValue(StoredReport.ATTRIBUTE_NAME, theName);
			theStoredReport.setValue(StoredReport.ATTRIBUTE_REPORT, theString);
			theStoredReport.setValue(StoredReport.ATTRIBUTE_BO_TYPE, theMEType);
			publish(theStoredReport, aContext);
			return theStoredReport;
		}

		@Override
		public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map<String, Object> arguments) {
		    return null;
		}
		
       @Override
	public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> aArguments) {
	            FormComponent theComp    = (FormComponent) aComponent;
	            FormContext   theContext = theComp.getFormContext();
			ResKey theError = null;
	            
	            StoredReport theReport = null;

	            if (theContext.checkAll()) {
	                try {
	                    theReport = this.createNewReport(theContext, aComponent);
	                    KnowledgeBase theKB = theReport.getKnowledgeBase();
					{
	                        if (theKB.commit()) {
//	                            theComp.acceptModel(theReport);
	                        }
	                        else {
							theError = I18NConstants.ERROR_COMMIT_FAILED;
	                        }
	                    }
	                }
	                catch (Exception ex) {
					theError = I18NConstants.ERROR_CREATE_FAILED;

	                    Logger.error("Unable to create a new query", ex, this);
	                }
	            }
	            else {
					HandlerResult theResult = new HandlerResult();
					AbstractApplyCommandHandler.fillHandlerResultWithErrors(null, theContext, theResult);
					return theResult;
	            }

	            if (theError != null) {
	            	HandlerResult theResult = new HandlerResult();
	            	theResult.addErrorMessage(theError);
	            	return theResult;
	            }
	            else {
	            	if (theReport != null) {
	            		ChartConfigurator theParent = (ChartConfigurator) aComponent.getDialogParent();
	            		theParent.acceptStoredReport(theReport);
	            	}
	    			
	                theComp.closeDialog();

	                theComp.getMainLayout().invalidate();

	                return HandlerResult.DEFAULT_RESULT;
	            }
	        }
	}
}

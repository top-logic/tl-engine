/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.element.meta.query.PublishedFlexWrapperLabelProvider;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.DeleteReportCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.ReportingSearchCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.ResetStoredReportCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.WriteReportCommandHandler;
import com.top_logic.reporting.report.util.ReportConstants;
import com.top_logic.reporting.report.wrap.StoredReport;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;

/**
 * This component is the master component for the analysis of search results. It is
 * connected to the search input component (some kind of an
 * {@link AttributedSearchComponent}) to access the search meta element.
 * 
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ReportSelectorComponent extends FormComponent implements ReportConstants{

	public static final String BOTYPE = "PaymentReport";
	
	/**
	 * Configuration for the {@link ReportSelectorComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(ReportingSearchCommandHandler.COMMAND_ID);
			registry.registerButton(ResetStoredReportCommandHandler.COMMAND_ID);
			registry.registerButton(DeleteReportCommandHandler.COMMAND_ID);
			registry.registerButton(WriteReportCommandHandler.COMMAND_ID);
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
		}

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
		}

	}

	public boolean monitorChanges;

	private Collection                  lastReport;
	private boolean                     hasChanged;
	private ChartConfigurationComponent chartComponent;
	private boolean                     showPublishingGroup;
	
	public ReportSelectorComponent(InstantiationContext context, Config atts) throws ConfigurationException {
	    super(context, atts);
	    hasChanged = false;
	    monitorChanges = true;
    }
	
	@Override
	protected void componentsResolved(InstantiationContext context) {
	    super.componentsResolved(context);
	    MainLayout theMain = getMainLayout();
	    
	    if (theMain != null) { // Not yet completely resolved
			for (LayoutComponent slave : getSlaves()) {
				if (slave instanceof ChartConfigurationComponent) {
					this.chartComponent = (ChartConfigurationComponent) slave;
				}
			}
	    }
	}


	@Override
	public FormContext createFormContext() {
		FormContext theCtx = new FormContext("reportQuerySelection", this.getResPrefix());
		Person      thePerson          = TLContext.getContext().getCurrentPersonWrapper();
        List        theReports         = StoredReport.getStoredReports(thePerson, true, BOTYPE);
		SelectField theReportSelection = FormFactory.newSelectField(REPORT_SELECTION_FIELD, theReports);
		
		theReportSelection.setOptionLabelProvider(PublishedFlexWrapperLabelProvider.INSTANCE);
		theReportSelection.setAsSelection(Collections.EMPTY_LIST);
		theCtx.addMember(theReportSelection);
		
		// Form Group for published StoredQueries
		theCtx.addMember(getPublishingFormGroup());

		theReportSelection.addValueListener(new ChangeFieldListener());

		return theCtx;
	}
	
   /**
	 * Returns the FormGroup for publishing details.
	 */
	protected FormGroup getPublishingFormGroup() {
		ValueListener theListener = new HasChangedListener();
		return ReportingCommandSupport.getPublishingFormGroup(showPublishingGroup, this.getResPrefix(), theListener);
	}
	
	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return (anObject instanceof StoredReport);
    }

	/**
     * Returns the storedReport.
     */
    public StoredReport getStoredReport() {
		return (StoredReport) getModel();
    }

	/**
     * Returns the lastReport.
     */
    public Collection getLastReport() {
    	return (lastReport);
    }

	/**
     * @param    lastReport    The lastReport to set.
     */
    public void setLastReport(Collection lastReport) {
    	this.lastReport = lastReport;
    }

	/**
     * Returns the chartComponent.
     */
    public ChartConfigurationComponent getChartConfigurationComponent() {
    	return chartComponent;
    }

	/**
     * Returns the hasChanged.
     */
    public boolean hasChanged() {
    	return (hasChanged);
    }

	/**
     * @param    hasChanged    The hasChanged to set.
     */
    public void setChanged(boolean hasChanged) {
    	this.hasChanged = hasChanged;
    }
    
    public String getObjectType() {
    	return BOTYPE;
    }
    
    /**
     * Updates the component to reflect the settings of the given {@link StoredReport}
     * 
     * @param aReport a {@link StoredReport} with some saved values
     * @return <code>true</code> if aReport was accepted, <code>false</code> otherwise.
     */
    public boolean acceptStoredReport(StoredReport aReport) {
		FormGroup   thePublishGroup   = (FormGroup) getFormContext().getMember(QueryUtils.FORM_GROUP);
		SelectField theSelectionField = (SelectField) thePublishGroup.getMember(QueryUtils.VISIBLE_GROUPS_FIELD);

		if(aReport == null) {
			thePublishGroup.setDisabled(true);
			thePublishGroup.setVisible(false);
			theSelectionField.setVisible(false);
			this.showPublishingGroup = false;
			this.invalidateButtons();
			return false;
		}
		this.monitorChanges = false;
		
		// The publishing Group is only visible if the selected query is
		// already published and the if the current user equals the
		// owner of the selected query.
		if (QueryUtils.allowWriteAndPublish(this)) {
			{
				List   groupAssociations = MapBasedPersistancySupport.getGroupAssociation(aReport);
				Person theCreator        = aReport.getCreator();
				Person theCurrentUser    = TLContext.getContext().getCurrentPersonWrapper();
				
				// root or owner can modify published queries
				if (WrapperHistoryUtils.getUnversionedIdentity(theCurrentUser).equals(WrapperHistoryUtils.getUnversionedIdentity(theCreator)) || SecurityContext.isAdmin()) {
					boolean isPublished = groupAssociations.isEmpty() ? false : true;
					
					thePublishGroup.setVisible(true);
					thePublishGroup.setDisabled(false);
					((BooleanField) thePublishGroup.getMember(QueryUtils.PUBLISH_QUERY_FIELD)).setAsBoolean(isPublished);
					theSelectionField.setAsSelection(groupAssociations);
					theSelectionField.setDisabled(!isPublished);
					theSelectionField.setVisible(true);
					this.showPublishingGroup = true;
				}
				else {
					thePublishGroup.setDisabled(true);
					thePublishGroup.setVisible(false);
					theSelectionField.setVisible(false);
					this.showPublishingGroup = false;
				}
				setChanged(false);
				
			}
		}
		else {
			thePublishGroup.setVisible(false);
			thePublishGroup.setDisabled(true);
		}
		this.monitorChanges = true;
		return true;
    }
    
    // value listener
    private class HasChangedListener implements ValueListener {
    	
    	@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			FormContext theCtx = getFormContext();
			
			if (monitorChanges && !Utils.equals(oldValue, newValue)) {
				FormField theSelectionField = theCtx.getField(REPORT_SELECTION_FIELD);
				
				if (!Utils.isEmpty(theSelectionField.getValue())) {
					theSelectionField.setDisabled(true);
				}
				setChanged(true);
			}
		}
    }
    
    private class ChangeFieldListener implements ValueListener {
    	
    	@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
    		String     theFieldName = field.getName();
    		Collection theNewVale   = (Collection) newValue;
    		
    		if(!theNewVale.isEmpty()) {
    			Object theVal = theNewVale.iterator().next();
    			
    			if(REPORT_SELECTION_FIELD.equals(theFieldName)) {
					setModel(theVal);
    			}
    		}
    		else {
				setModel(null);
    		}
    	}
    }
}

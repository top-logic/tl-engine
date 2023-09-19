/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.layout.flexreporting.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.element.layout.meta.search.SearchFieldSupport;
import com.top_logic.element.layout.meta.search.SearchFieldSupport.ChangeStoredQueryListener;
import com.top_logic.element.meta.query.FlexWrapperAdminComparator;
import com.top_logic.element.meta.query.FlexWrapperUserComparator;
import com.top_logic.element.meta.query.PublishedFlexWrapperLabelProvider;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport;
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
public class ReportQuerySelectorComponent extends FormComponent implements ReportConstants{

	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Name(REPORT_OBJECT_TYPE)
		String getReportObjects();

		@Override
		default void addAdditionalCommandGroups(Set<? super BoundCommandGroup> additionalGroups) {
			FormComponent.Config.super.addAdditionalCommandGroups(additionalGroups);
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.PUBLISH_NAME));
			additionalGroups.add(CommandGroupRegistry.resolve(QueryUtils.OWNER_WRITE_NAME));
		}

	}

	private AttributedSearchComponent searchComponent;
	private String                    objectType;
	private StoredReport              lastReport;
	private StoredQuery               storedQuery;
	private StoredQuery               lastQuery;
	private boolean                   hasChanged;
	private boolean                   showPublishingGroup;

	private TLClass searchMetaElement;
	
	public  boolean                   monitorChanges;
	
	public ReportQuerySelectorComponent(InstantiationContext context, Config atts) throws ConfigurationException {
	    super(context, atts);
	    objectType     = StringServices.nonEmpty(atts.getReportObjects());
	    hasChanged     = false;
	    monitorChanges = true;
    }
	
	@Override
	public FormContext createFormContext() {
		FormContext theCtx = new FormContext("reportQuerySelection", this.getResPrefix());
		String      aBOType;
		
		if(StringServices.isEmpty(this.objectType)) {
			aBOType = this.getSearchMetaElement().getName();
		}
		else {
			aBOType = this.objectType;
		}
	
		SelectField theQueries = SearchFieldSupport.getStoredQueryConstraint(this.searchMetaElement, null);
		
		theQueries.removeValueListener(ChangeStoredQueryListener.INSTANCE);
		theQueries.setAsSingleSelection(storedQuery);
	
		Person      thePerson          = TLContext.getContext().getCurrentPersonWrapper();
	    List        theReports         = StoredReport.getStoredReports(thePerson, true, aBOType);
		SelectField theReportSelection = FormFactory.newSelectField(REPORT_SELECTION_FIELD, theReports);
		
		theReportSelection.setOptionLabelProvider(PublishedFlexWrapperLabelProvider.INSTANCE);
		theReportSelection.setAsSelection(Collections.EMPTY_LIST);
		theCtx.addMember(theReportSelection);
		theCtx.addMember(theQueries);
		
		// Form Group for published StoredQueries
		theCtx.addMember(getPublishingFormGroup());
	
		theReportSelection.addValueListener(new ChangeFieldListener());
		theQueries.addValueListener(new ChangeFieldListener());
	
		return theCtx;
	}

	@Override
	public boolean isModelValid() {
		if(this.getSearchMetaElement() == this.searchComponent.getSearchMetaElement()) {
			return super.isModelValid();
		}
		return false;
	}

	@Override
	public boolean validateModel(DisplayContext context) {
	    this.searchMetaElement = null;
		return super.validateModel(context);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return (anObject instanceof StoredReport);
	}

	public AttributedSearchComponent getSearchComponent() {
		return (searchComponent);
	}

	/**
	 * Returns the storedReport.
	 */
	public StoredReport getStoredReport() {
		return (StoredReport) getModel();
	}

	/**
	 * Returns the storedQuery.
	 */
	public StoredQuery getStoredQuery() {
		return (storedQuery);
	}

	public void setStoredQuery(StoredQuery aQuery) {
		this.storedQuery = aQuery;
	}

	/**
	 * Returns the lastReport.
	 */
	public StoredReport getLastReport() {
		return (lastReport);
	}

	/**
	 * @param    lastReport    The lastReport to set.
	 */
	public void setLastReport(StoredReport lastReport) {
		this.lastReport = lastReport;
	}

	/**
	 * Returns the lastQuery.
	 */
	public StoredQuery getLastQuery() {
		return (lastQuery);
	}

	/**
	 * @param    lastQuery    The lastQuery to set.
	 */
	public void setLastQuery(StoredQuery lastQuery) {
		this.lastQuery = lastQuery;
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

	/**
	 * Updates the component to reflect the settings of the given {@link StoredReport}
	 * 
	 * @param aReport a {@link StoredReport} with some saved values
	 * @return <code>true</code> if aReport was accepted, <code>false</code> otherwise.
	 */
	public boolean acceptStoredReport(StoredReport aReport) {
		FormContext theCtx            = getFormContext();
		FormGroup   thePublishGroup   = (FormGroup) theCtx.getMember(QueryUtils.FORM_GROUP);
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
				if (WrapperHistoryUtils.getUnversionedIdentity(theCurrentUser)
					.equals(WrapperHistoryUtils.getUnversionedIdentity(theCreator)) || theCurrentUser.isAdmin()) {
					boolean isPublished;
					if (groupAssociations.isEmpty()) {
						isPublished = false;
					}
					else {
						isPublished = true;
					}
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

	public String getObjectType() {
		return (objectType == null ? this.searchMetaElement.getName() : objectType);
	}

	/** 
	 * If the report selection field has a value it needs to be disabled.
	 */
	public void disableReportSelector(boolean disable) {
		FormField theSelectionField = this.getFormContext().getField(REPORT_SELECTION_FIELD);
	
		if( this.monitorChanges && !Utils.isEmpty(theSelectionField.getValue())) {
			theSelectionField.setDisabled(disable);
			this.setChanged(true);
		}
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
	    super.componentsResolved(context);
	    this.searchComponent = (AttributedSearchComponent) this.getMaster().getMaster();
	}

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

	@Override
	protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
		if (models.stream().anyMatch(StoredQuery.class::isInstance)) {
			Person      thePerson = TLContext.getContext().getCurrentPersonWrapper();
            List        theSQs    = StoredQuery.getStoredQueries(this.searchMetaElement, thePerson, true);
            
            FormContext theCtx = this.getFormContext();
			if (theCtx.hasMember(SearchFieldSupport.STORED_QUERY)) {
				SelectField theField = (SelectField) theCtx.getField(SearchFieldSupport.STORED_QUERY);
				Object oldSelection = theField.getSingleSelection();
				theField.setOptions(theSQs);
				if (theSQs.contains(oldSelection)) {
					theField.setAsSingleSelection(oldSelection);
				}
			}
			super.receiveModelDeletedEvent(models, changedBy);
			return true;
		}
		return super.receiveModelDeletedEvent(models, changedBy);
	}
	
	@Override
	protected boolean receiveModelCreatedEvent(Object model, Object changedBy) {
		// a new stored report was created
		if (model instanceof StoredReport) {
			{
				Comparator theComparator;
				Person theUser = TLContext.getContext().getCurrentPersonWrapper();
				if (ThreadContext.isSuperUser() || PersonManager.isAdmin(theUser)) {
					theComparator = FlexWrapperAdminComparator.INSTANCE;
				}
				else {
					theComparator = FlexWrapperUserComparator.INSTANCE;
				}

				FormContext theCtx = this.getFormContext();
				if (theCtx.hasMember(REPORT_SELECTION_FIELD)) {
					SelectField theField = (SelectField) theCtx.getField(REPORT_SELECTION_FIELD);
					ArrayList theOptions = new ArrayList(theField.getOptions());

					if (!theOptions.contains(model)) {
						theOptions.add(model);
						Collections.sort(theOptions, theComparator);

						theField.setOptions(theOptions);
						theField.setAsSingleSelection(model);
						theField.setDisabled(false);
						setModel(model);
						this.lastReport = getStoredReport();
					}
				}
				return true;
			}
		}
		//a new stored query was created
		else if (model instanceof StoredQuery) {
			StoredQuery theQuery  = (StoredQuery) model;
			Person      thePerson = TLContext.getContext().getCurrentPersonWrapper();
            List        theSQs    = StoredQuery.getStoredQueries(this.searchMetaElement, thePerson, true);
            List        theSel    = (theQuery != null) ? Collections.singletonList(theQuery) : Collections.EMPTY_LIST;
            
            FormContext theCtx = this.getFormContext();
			if (theCtx.hasMember(SearchFieldSupport.STORED_QUERY)) {
				SelectField theField = (SelectField) theCtx.getField(SearchFieldSupport.STORED_QUERY);
				Object oldSelection = theField.getSingleSelection();
				theField.setOptions(theSQs);
				if (theSQs.contains(oldSelection)) {
					theField.setAsSingleSelection(oldSelection);
				}
			}
			return true;
		}
		return super.receiveModelCreatedEvent(model, changedBy);
	}
	
   /**
	 * Returns the FormGroup for publishing details.
	 */
	protected FormGroup getPublishingFormGroup() {
		ValueListener theListener = new HasChangedListener();
		return ReportingCommandSupport.getPublishingFormGroup(showPublishingGroup, this.getResPrefix(), theListener);
	}
	
	private TLClass getSearchMetaElement() {
	    if (this.searchMetaElement == null) {
	        if (this.searchComponent != null) {
	            this.searchMetaElement = this.searchComponent.getSearchMetaElement();
	        }
	    }
	    return this.searchMetaElement;
	}

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
    
    // value listener
    
    private class ChangeFieldListener implements ValueListener {
    	
    	@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
    		if (monitorChanges) {
    			String     theFieldName = field.getName();
    			Collection<Object> theNewVale   = (Collection<Object>) newValue;

    			if(SearchFieldSupport.STORED_QUERY.equals(theFieldName)) {
    				if(!theNewVale.isEmpty()) {
    					Object theVal = CollectionUtil.getFirst(theNewVale);
    					storedQuery   = (StoredQuery) theVal;
    				}
    				else {
    					storedQuery = null;
    				}
    				return;
    			}
    			else if(REPORT_SELECTION_FIELD.equals(theFieldName)) {
    				if(!theNewVale.isEmpty()) {
    					Object theVal = CollectionUtil.getFirst(theNewVale);
						setModel(theVal);
    				}
    				else {
						setModel(null);
    					showPublishingGroup = false;
    				}
					StoredReport storedReport = getStoredReport();
					acceptStoredReport(storedReport);
    			}
    		}
    	}
    }
    
    public void setQueryAndReport(StoredQuery query, StoredReport report) {
    	FormContext context = this.getFormContext();
    	SelectField queryField = (SelectField) context.getField(SearchFieldSupport.STORED_QUERY);
    	if (query != null) {
    		queryField.setAsSingleSelection(query);
    	} else {
    		queryField.setAsSelection(Collections.emptyList());
    	}
    	SelectField reportField = (SelectField) context.getField(REPORT_SELECTION_FIELD);
    	//reportField.setAsSelection(null);
    	reportField.setAsSingleSelection(report);
    }
}

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
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.Utils;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSetAware;
import com.top_logic.element.layout.meta.search.SearchCommandHandler;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.progress.DefaultProgressInfo;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.reporting.layout.meta.search.ChartComponent;
import com.top_logic.reporting.layout.meta.search.ReportingAttributedSearchResultSet;
import com.top_logic.reporting.layout.meta.search.ReportingChartComponent;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.DeleteReportCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.ReportingSearchCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.ResetStoredReportCommandHandler;
import com.top_logic.reporting.layout.meta.search.ReportingCommandSupport.WriteReportCommandHandler;
import com.top_logic.reporting.report.control.producer.ChartContext;
import com.top_logic.reporting.report.model.ReportConfiguration;
import com.top_logic.reporting.report.model.objectproducer.AttributedSearchResultObjectProducer.AttributedSearchResultProducerConfiguration;
import com.top_logic.reporting.report.model.partition.PartitionFunctionConfiguration;
import com.top_logic.reporting.report.view.component.DefaultProducerFilterComponent;
import com.top_logic.reporting.report.wrap.StoredReport;
import com.top_logic.reporting.report.xmlutilities.ReportReader;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * This component is used to offer chart manipulation on the analysis page of the
 * search.
 * 
 * @author <a href="mailto:tbe@top-logic.com">tbe</a>
 */
@Deprecated
public class ChartConfigurationComponent extends DefaultProducerFilterComponent implements ChartConfigurator, AttributedSearchResultSetAware {
	
	/**
	 * Configuration for the {@link ChartConfigurationComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends DefaultProducerFilterComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			registry.registerButton(WriteReportCommandHandler.COMMAND_ID);
			registry.registerButton(DeleteReportCommandHandler.COMMAND_ID);
			registry.registerButton(ResetStoredReportCommandHandler.COMMAND_ID);
			registry.registerButton(ReportingSearchCommandHandler.COMMAND_ID);
			DefaultProducerFilterComponent.Config.super.modifyIntrinsicCommands(registry);
		}

	}

	private ReportConfiguration reportConfig;

	private ReportQuerySelectorComponent reportQuerySelector = null;
	
	/**
	 * Component that draws the graphics
	 */
	private ChartComponent                    chartComponent;
	
	/**
	 * Component that did the search
	 */
	private AttributedSearchComponent         searchComponent;

	/**
	 * The result of the last search that was executed
	 */
	AttributedSearchResultSet resultSet;
	
	/**
	 * @deprecated, what is this?
	 */
	private AdditionalReportingOptionProvider additionalReportingOptionProvider;
	
	/**
	 * Some metaelement, most likely the searchMetaElement of the master, but somehow used for isModelValid
	 */
	private TLClass                       searchMetaElement;

	private ChartConfigurationFieldHelper     fieldHelper;
	
	public static final String   XML_CONFIG_ADDITIONAL_REPORTING_OPTIONS = "additionalReportingOptionProviderClass";

	public ChartConfigurationComponent(InstantiationContext context, Config atts) throws ConfigurationException {
	    super(context, atts);
	    this.additionalReportingOptionProvider = null;//(AdditionalReportingOptionProvider) XMLAttributeHelper.getAsInstanceOfClass(atts, XML_CONFIG_ADDITIONAL_REPORTING_OPTIONS, AdditionalReportingOptionProvider.class);
    }
	
	@Override
	protected void componentsResolved(InstantiationContext context) {
	    super.componentsResolved(context);
		Collection<? extends LayoutComponent> slaves = this.getSlaves();
	    
		for (Iterator<? extends LayoutComponent> theIterator = slaves.iterator(); theIterator.hasNext();) {
			LayoutComponent aComp = theIterator.next();
	    	if(aComp instanceof ChartComponent) {
	    		this.chartComponent = (ChartComponent)aComp;
	    	}
	    	if (aComp instanceof ReportQuerySelectorComponent) {
	    		this.reportQuerySelector = (ReportQuerySelectorComponent) aComp;
	    	}
	    }

	    this.searchComponent = (AttributedSearchComponent) this.getMaster();
		if (searchComponent != null) {
		    this.searchMetaElement = this.searchComponent.getSearchMetaElement();
		}
	}
	
	public ChartConfigurationFieldHelper getConfigurationFormFieldHelper() {
	    if (this.fieldHelper == null) {
	        this.fieldHelper = this.createConfigurationFormFieldHelper();
	    }
	    return this.fieldHelper;
	}
	
	protected ChartConfigurationFieldHelper createConfigurationFormFieldHelper() {
	    return new ChartConfigurationFieldHelper(this.searchMetaElement, this.getSearchComponent().getExcludeSetForReporting());
	}
	
	@Override
	public ReportQuerySelectorComponent getReportQuerySelector() {
	    if (this.reportQuerySelector == null) {
	        this.reportQuerySelector = (ReportQuerySelectorComponent) this.getMaster();
	    }
	    return this.reportQuerySelector;
	}
	
	@Override
	protected void afterModelSet(Object oldModel, Object newModel) {
		super.afterModelSet(oldModel, newModel);

		TLClass theOldME = this.searchMetaElement;

		this.resultSet = (AttributedSearchResultSet) newModel;
		if (resultSet != null) {
			this.searchMetaElement = this.resultSet.getMetaElement();
		} else {
			searchMetaElement = null;
		}
		if (!Utils.equals(this.searchMetaElement, theOldME)) {
			this.fieldHelper = null;
		}
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject instanceof AttributedSearchResultSet;
	}

	public boolean setStoredReport(StoredReport aReport) {
        String theContent = (String) aReport.getValue(StoredReport.ATTRIBUTE_REPORT);

        try {
            ReportConfiguration modifiedConfiguration = ReportReader.readConfig(theContent);
            if (!this.getReportConfiguration().equals(modifiedConfiguration) ) {
                this.reportConfig = modifiedConfiguration;
				this.removeFormContext();
                this.invalidate();
                // this causes the report chart component to update the chart
                fireModelModifiedEvent(this.reportConfig, this);
            }
        } catch (ConfigurationException cfex) {
            Logger.error("Unable to load stored report", cfex, ReportingChartComponent.class);
        } catch (XMLStreamException xml) {
            Logger.error("Unable to load stored report", xml, ReportingChartComponent.class);
        }
        this.disableReportSelector(false);
        return true;
    }
	
	private boolean internalReceiveModelSetEvent(Object aModel) {
		// A different report was selected in the report selection field
	    if (aModel instanceof StoredReport) {
			StoredReport theStoredReport = (StoredReport) aModel;
			String       theContent      = (String) theStoredReport.getValue(StoredReport.ATTRIBUTE_REPORT);

			try {
				ReportConfiguration theConfiguration = ReportReader.readConfig(theContent);

                if (! isSupportedConfigurationType(theConfiguration)) {
                    return false;
                }

				if (!this.getReportConfiguration().equals(theConfiguration) ) {
					this.reportConfig = theConfiguration;
					this.removeFormContext();
					this.invalidate();
				}
				
				// this causes the report chart component to update the chart
				fireModelModifiedEvent(this.reportConfig, this);
			}
			catch (ConfigurationException cfex) {
			    Logger.error("Unable to load stored report", cfex, ReportingChartComponent.class);
			}
			catch (XMLStreamException xml) {
			    Logger.error("Unable to load stored report", xml, ReportingChartComponent.class);
			}

			this.disableReportSelector(false);

			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	protected boolean receiveModelChangedEvent(Object aModel, Object aChangedBy) {
		if (aModel instanceof StoredReport) {
			return super.receiveModelChangedEvent(aModel, aChangedBy);
		}
		return false;
	}

	@Override
	protected boolean resetFormContext(Object anOldModel, Object aModel) {
	    if (aModel instanceof AttributedSearchResultSet) {
	        TLClass theOldME = null;
	        TLClass theNewME = ((AttributedSearchResultSet) aModel).getMetaElement();

	        if (anOldModel instanceof AttributedSearchResultSet) {
                theOldME = ((AttributedSearchResultSet) anOldModel).getMetaElement();
	        }

	        return !theNewME.equals(theOldME);
	    }
		return super.resetFormContext(anOldModel, aModel);
	}
	
	@Override
	public boolean isModelValid() {
		if(this.searchMetaElement == this.searchComponent.getSearchMetaElement()) {
			return super.isModelValid();
		}
		return false;
	}
	
	@Override
	public boolean validateModel(DisplayContext context) {
		this.searchMetaElement = this.searchComponent.getSearchMetaElement();
		return super.validateModel(context);
	}
	
	protected FormGroup internalAddFormGroup(FormContext aContext, ConfigurationItem anItem, ConfigurationDescriptor aConfigDesc) {
	    FormGroup theGroup = this.getConfigurationFormFieldHelper().addFormGroup(aContext, anItem, aConfigDesc);
	    
	    if (theGroup != null) {
	        this.internalProcessFormContainer(theGroup, aConfigDesc);
	    }
	    
	    return theGroup;
	}
	
	protected void internalProcessFormContainer(FormContainer aContainer, ConfigurationDescriptor aDescriptor) {
	    Iterator theMembers = aContainer.getMembers();
        while (theMembers.hasNext()) {
            FormMember theMember = (FormMember) theMembers.next();
			PropertyDescriptor thePropDesc = theMember.get(ConfigurationFormFieldHelper.PROPERTY_DESCRIPTOR);
			ConfigurationDescriptor theConfDesc = theMember.get(ConfigurationFormFieldHelper.CONFIG_DESCRIPTOR);
            if (theMember instanceof FormField) {
                this.internalPostProcessFormField((FormField) theMember, thePropDesc, theMember.getName());
            }
            else if (theMember instanceof FormContainer) {
                this.internalPostProcessFormContainer((FormContainer) theMember, theConfDesc);
                this.internalProcessFormContainer((FormContainer) theMember, theConfDesc); 
            }
        }
	}
	
	protected void internalPostProcessFormContainer(FormContainer aContainer, ConfigurationDescriptor aDescriptor) {
	    if (aDescriptor != null && PartitionFunctionConfiguration.class.isAssignableFrom(aDescriptor.getConfigurationInterface())) {
            aContainer.setVisible(false);
        }
	}
	
	protected void internalPostProcessFormField(FormField aMember, PropertyDescriptor aProperty, String aMemberName) {
        aMember.addValueListener(new HasChangedListener());
	}
	
	@Override
	protected void fill(FormContext aContext) {
	    if (this.reportConfig == null ) {
	    	List theList = this.getConfigurationClasses();
	    	for (int i=0, cnt=theList.size(); i<cnt; i++) {
	    		Class theItemClass = (Class) theList.get(i);
	    		ConfigurationDescriptor theDesc = TypedConfiguration.getConfigurationDescriptor(theItemClass);
	    		this.internalAddFormGroup(aContext, null, theDesc);
	    	}
	    }
	    else {
	    	this.internalAddFormGroup(aContext, this.reportConfig, this.reportConfig.descriptor());
	    }
	}
	
	@Override
	protected void fill(ChartContext chartContext) { 
	    AttributedSearchResultSet theResultSet = (AttributedSearchResultSet) chartContext.getModel();
		if (theResultSet != null) {
		    
		    ReportConfiguration theOptions = this.getReportConfiguration();
		    
			ReportingAttributedSearchResultSet theNewResultSet = new ReportingAttributedSearchResultSet(theResultSet, theOptions);
			fireSelection(theNewResultSet);
		}
	}
	
	/**
	 * Returns the currently used {@link ReportConfiguration}.
	 */
	@Override
	public ReportConfiguration getReportConfiguration() {
		FormContext theCtx  = getFormContext();
		Iterator<? extends FormMember>    theIter = theCtx.getMembers();
		
		while (theIter.hasNext()) {
			FormMember theMember = theIter.next();
		
			if (theMember instanceof FormContainer) {
				ConfigurationDescriptor theDesc = theMember.get(ConfigurationFormFieldHelper.CONFIG_DESCRIPTOR);
			
				if (theDesc != null && ReportConfiguration.class.isAssignableFrom(theDesc.getConfigurationInterface())) {
					this.reportConfig = (ReportConfiguration) this.getConfigurationFormFieldHelper().createConfigurationItem((FormContainer) theMember, null);
				}
				AttributedSearchResultProducerConfiguration theObjectProdConf = TypedConfiguration.newConfigItem(AttributedSearchResultProducerConfiguration.class);
				theObjectProdConf.setLayoutComponent(this);
				theObjectProdConf.setImplementationClass(theObjectProdConf.getImplementationClass());
				this.reportConfig.setBusinessObjectProducer(theObjectProdConf);
				this.reportConfig.setSearchMetaElement(this.searchMetaElement);
			}
		}
		this.reportConfig.setShowLegend(true);

		return this.reportConfig;
	}
	
	@Override
	public boolean acceptStoredReport(StoredReport aReport) {
		try {
			return acceptReportConfiguration(this.getConfigurationFromStoredReport(aReport));
		}
		catch (ConfigurationException e) {
			Logger.error("Could not create ReportConfiguration from given StoredReport " + aReport.getName() + ".", e, ChartConfigurationComponent.class);
			return false;
		}
	}

	/**
     * Whether the config is type of a declared configuration class or not
     */
    private boolean isSupportedConfigurationType(ReportConfiguration config) {
        return this.getConfigurationClasses().contains(config.descriptor().getConfigurationInterface());
    }
    
	/** 
	 * Checks whether the given configuration valid and accepted.
	 */
	private boolean acceptReportConfiguration(ReportConfiguration aConfig) {
		      
        if (! isSupportedConfigurationType(aConfig)) {
            return false;
        }
        
        if (this.reportConfig.equals(aConfig)) {
			return true;
		}
		else {
			this.reportConfig = aConfig;
		}
		// TODO tbe: und nun?
		return true;
	}

	/**
	 * Transforms a given {@link StoredReport} into the equivalent {@link ReportConfiguration}.
	 * 
	 * @param aReport a {@link StoredReport} to be transformed
	 * 
	 * @return        a {@link ReportConfiguration} representing the given report
	 * 
	 * @throws ConfigurationException if either the report was not configured properly or if the
	 *             report was not well-formated
	 */
	protected ReportConfiguration getConfigurationFromStoredReport(StoredReport aReport) throws ConfigurationException {
		String theContent = (String) aReport.getValue(StoredReport.ATTRIBUTE_REPORT);
		try {
			ReportConfiguration theConfig = ReportReader.readConfig(theContent);
			return theConfig;
		}
		catch (XMLStreamException e) {
			throw new ConfigurationException("Unable to load stored report due to XML errors:", e);
		}
	}

	/**
     * Returns the additionalReportingOptionProvider.
     */
    public AdditionalReportingOptionProvider getAdditionalReportingOptionProvider() {
    	return (this.additionalReportingOptionProvider);
    }

	/**
     * Returns the searchComponent.
     */
    @Override
	public AttributedSearchComponent getSearchComponent() {
    	return (this.searchComponent);
    }

	/**
     * Returns the chartComponent.
     */
    @Override
	public ChartComponent getChartComponent() {
    	return (this.chartComponent);
    }

    public List getConfigurationClasses() {
        List theList = new ArrayList();
        theList.add(ReportConfiguration.class);
        return theList;
    }
    
	protected void disableReportSelector(boolean disable) {
		getReportQuerySelector().disableReportSelector(disable);
	}
/*
	protected final ReportConfiguration getReportConfig() {
		return reportConfig;
	}
*/
	protected final void setReportConfig(ReportConfiguration reportConfig) {
		this.reportConfig = reportConfig;
	}

    public static interface AdditionalReportingOptionProvider {
        public List getAdditionalReportingOptions();
        public TLStructuredTypePart getMetaAttributeForReportingOption(String aReportingOption);
        public boolean hasOption(String aReportingOption);
    }

	public class HasChangedListener implements ValueListener {
	    
	    @Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
	        if (!Utils.equals(aOldValue, aNewValue)) {
                disableReportSelector(true);
            }
	    }
	}

	@Override
	public AttributedSearchResultSet getSearchResult() {
		StoredQuery currentQuery = reportQuerySelector.getStoredQuery();
		StoredQuery lastQuery    = reportQuerySelector.getLastQuery();
		
		if (!Utils.equals(currentQuery, lastQuery)) {
			if (currentQuery != null) {
				this.searchComponent.setModel(currentQuery);
			}
			else {
				// TODO tbe: setFormContext is not the right way, but for some reason the
				// fillFormContext-way does not reset the search filters if called this way.
				this.searchComponent.removeFormContext();
				this.searchComponent.resetStoredQuery();
//				this.searchComponent.getSearchFilterSupport().fillFormContext((AttributeFormContext) this.searchComponent.getFormContext(), currentQuery);
			}
			this.resultSet = null;
			this.reportQuerySelector.setLastQuery(currentQuery);
		}
		
		if (this.resultSet == null) {
			AttributedSearchComponent r = this.searchComponent;
			SearchCommandHandler theSearchHandler = (SearchCommandHandler) CommandHandlerFactory.getInstance().getHandler(SearchCommandHandler.COMMAND_ID);
			this.resultSet = (AttributedSearchResultSet) theSearchHandler.search(this.searchComponent, this.searchComponent.getFormContext(), Collections.EMPTY_MAP, new DefaultProgressInfo());
		}
		return this.resultSet;
	}
}

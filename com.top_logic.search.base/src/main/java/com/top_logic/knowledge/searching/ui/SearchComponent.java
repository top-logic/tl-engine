/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.ui;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.base.search.Query;
import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.searching.Precondition;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchEngine;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.provider.PercentageLabelProvider;
import com.top_logic.layout.provider.StringOptionLabelProvider;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link FormComponent} handling search requests in 
 * the search GUI.
 * 
 * @author     <a href="mailto:sko@top-logic.com">sko</a>
 */
public class SearchComponent extends FormComponent {

    /** The field with the keyword /term value */
    public static final String FORM_ITEM_KEYWORDS           = "keywords";
    
    /** The select box with the search modes */
    public static final String FORM_ITEM_SEARCHMODE         = "searchmode";

    /** The select box with the rating values */
    public static final String FORM_ITEM_RATING             = "rating";

    /** The select box with the attribute names */
    public static final String FORM_ITEM_ATTRIBUTE_NAME     = "attribute_name";
    
    /** The select box with the attribute filters */
    public static final String FORM_ITEM_ATTRIBUTE_FILTER   = "attribute_filter";
    
    /** The field with the attribute value */
    public static final String FORM_ITEM_ATTRIBUTE_VALUE    = "attribute_value";

    /** The value prefix for the checkboxes representing the search engines */
    public static final String ENGINE_PREFIX                = "engine_";
    
    /** The search mode for searching for all entered search terms */
    public static final String AND                          = "AND";
    
    /** The search mode for searching at least one entered search term */
    public static final String OR                           = "OR";
    
	/**
	 * Configuration for the {@link SearchComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerButton(SearchCommandHandler.COMMAND_ID);
		}

	}

	/**
	 * Create a new SearchComponent.
	 */
    public SearchComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }


    /** 
     * Creates the {@link FormContext}.
     * 
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {

        FormContext formContext = new FormContext("searchFormContext", getResPrefix());
        
        StringField keyField = FormFactory.newStringField(FORM_ITEM_KEYWORDS, true, false, new StringLengthConstraint(1, 50));
        formContext.addMember(keyField);
        
        List searchModes = new ArrayList();
        searchModes.add(AND);
        searchModes.add(OR);
        SelectField searchModeField = FormFactory.newSelectField(FORM_ITEM_SEARCHMODE, searchModes, false, true, false, new SelectionSizeConstraint(1,1));
        searchModeField.setOptionLabelProvider(new StringOptionLabelProvider(Resources.getInstance(),getResPrefix()));
        searchModeField.setAsSingleSelection(OR);
        formContext.addMember(searchModeField);
        
        List searchRatings = new ArrayList();
		searchRatings.add(Float.valueOf(0.0f));
		searchRatings.add(Float.valueOf(0.1f));
		searchRatings.add(Float.valueOf(0.2f));
		searchRatings.add(Float.valueOf(0.3f));
		searchRatings.add(Float.valueOf(0.4f));
		searchRatings.add(Float.valueOf(0.5f));
		searchRatings.add(Float.valueOf(0.6f));
		searchRatings.add(Float.valueOf(0.7f));
		searchRatings.add(Float.valueOf(0.8f));
		searchRatings.add(Float.valueOf(0.9f));
		searchRatings.add(Float.valueOf(1.0f));
        SelectField searchRatingField = FormFactory.newSelectField(FORM_ITEM_RATING, searchRatings, false, true, false, new SelectionSizeConstraint(1,1));
        searchRatingField.setOptionLabelProvider(new PercentageLabelProvider(">= ", "=  ", ""));
		searchRatingField.setAsSingleSelection(Float.valueOf(0));
        formContext.addMember(searchRatingField);
        
        return formContext;
    }

    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(new SearchModel());
		}
		return super.validateModel(context);
    }

    /** 
     * Only supports {@link SearchModel}
     */
    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof SearchModel;
    }

    /**
     * Start the search.
     */
    public void startSearch() {
        this.feedModel();

        SearchModel searchModel = (SearchModel) this.getModel();
        SearchResultSet theSet = null;
        Query theQuery  = this.createQuery();
        if (theQuery != null) {    // there has to be a query to start the search
            QueryConfig theConfig = new QueryConfig();
        
            // create Precondition
            Precondition theCond = this.createPrecondition();
            theConfig.setPrecondition(theCond);

            // set search result filter
//            theConfig.setFilter(this.getFilter());
            
            // set the search query
            theConfig.setQuery(theQuery);
        
            // --> start search
            theSet = searchModel.getSearchService().search(theConfig);
			theSet.waitForClosed(200);
            
			fireSelection(theSet);
        }
    }
    
    /** 
     * Feed the model with the user's search options 
     */
    private void feedModel() {
        FormContext formContext = this.getFormContext();
        SearchModel searchModel = (SearchModel) this.getModel();
        
        // feed with keywords
        String keywords = formContext.getField(FORM_ITEM_KEYWORDS).getValue().toString();
        searchModel.setKeywords(keywords);
        
        // feed with selected search engines
        List theChosenOnes = ((SearchModel)this.getModel()).getAllSearchEngines();
        searchModel.setSelectedEngines(theChosenOnes);
        
        // feed with search mode
        SelectField modeConstraint = (SelectField) formContext.getField(FORM_ITEM_SEARCHMODE);
        String modeValue = (String) modeConstraint.getSingleSelection();
        searchModel.setMode(modeValue);
        
        // feed with selected rating
        SelectField ratingConstraint = (SelectField) formContext.getField(FORM_ITEM_RATING);
        SearchAttribute rating = searchModel.getSearchAttribute(SearchAttribute.RANKING.getKey());
        rating.setValue(ratingConstraint.getSingleSelection());
        
        // o.k. so far, the attribute filters are already fed to the model
    }
    
    /**
     * Create a precondition for the search. The precondition contains
     * the list of search engines to be used and the search attributes.
     * 
     * @return  {@link Precondition}
     */
    private Precondition createPrecondition() {
        Precondition theCond = new Precondition();
//        theCond.setKnowledgeBaseName("Default");
        SearchModel searchModel = (SearchModel) this.getModel();
        Iterator engines = searchModel.getSelectedEngines().iterator();
        Iterator attributes = searchModel.getSearchAttributes().values().iterator();

        // Set used search engines
        while (engines.hasNext()) {
            SearchEngine engine = (SearchEngine) engines.next();
            theCond.addSearchEngine(engine);
        }
        // Set supported search attributes with their user's selected values
        while (attributes.hasNext()) {
            SearchAttribute attribute = (SearchAttribute) attributes.next();
            theCond.addSearchAttributes(attribute);
        }

        return (theCond);
    }
    
    /**
     * Create a <i>TopLogic</i> query for the keywords stored in the search model.
     * 
     * @return  the <i>TopLogic</i> {@link Query}
     */
    private Query createQuery() {
        SearchModel searchModel = (SearchModel) this.getModel();
        String theWords = searchModel.getKeywords();
        
        if (StringServices.isEmpty(theWords)) {
            // we don't need to create a query for search because
            // the  user hasn't provided any search expressions
            return null;
        }
        
        return (Query.getFullTextQuery(theWords, searchModel.getMode().equals(AND), true));
    }
    
//    inner class
    public static class SearchCommandHandler extends AJAXCommandHandler{

        public static final String COMMAND_ID = "searchQuery";
        
        public SearchCommandHandler(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model, Map<String, Object> someArguments) {
            if(component instanceof SearchComponent) {
                SearchComponent searchComponent = (SearchComponent)component;
                FormContext formContext = (searchComponent).getFormContext();
                if(formContext.checkAll()) {
                    searchComponent.startSearch();
                }
            }
            return new HandlerResult();
        }
        
    }
}

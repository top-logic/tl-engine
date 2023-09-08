/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * Extend the ExtendedSearchModelBuilder to dispatch to sub-modelBuilders via type.
 * 
 * @author    tdi
 */
public class DispatchingSearchModelBuilder
		implements ExtendedSearchModelBuilder, ConfiguredInstance<DispatchingSearchModelBuilder.Config> {

    /** XML-Attribute that will result in a call to {@link #setModelBuilders(List)} */
    private static final String XML_LIST_MODEL_BUILDERS = "modelBuilders";
    
	public static interface Config extends PolymorphicConfiguration<DispatchingSearchModelBuilder> {

		@Name(XML_LIST_MODEL_BUILDERS)
		@InstanceFormat
		List<ExtendedSearchModelBuilder> getModelBuilders();

	}

    /**
     * The model builder map contains for all configured meta elements a
     * corresponding model builder. 
     * Key:   String (meta element type) 
     * Value: MultiMetaElementSearchModelBuilder (model builder)
     */
    private final Map<String, ExtendedSearchModelBuilder> modelBuilder;

	private Config _config;
    
    /**
     * Creates a new DispatchingSearchModelBuilder wit a pre build map.
     * 
     * @param aBuilderMap
     *            The builder map contains the model builder for the configured
     *            meta elements. See {@link #modelBuilder} comment.
     */
    protected DispatchingSearchModelBuilder(Map<String, ExtendedSearchModelBuilder> aBuilderMap) {
        this.modelBuilder = aBuilderMap;
    }
    
    /**
     * Create a DispatchingSearchModelBuilder with an empty Map.
     */
    @CalledByReflection
    public DispatchingSearchModelBuilder() {
		this(new HashMap<>());
    }
    
    /**
     * Create a DispatchingSearchModelBuilder from XML.
     */
	public DispatchingSearchModelBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		this();
		_config = config;
		setModelBuilders(config.getModelBuilders());
    }

	@Override
	public Config getConfig() {
		return _config;
	}

    /**
     * This method returns the model builder for the given meta element type.
     * 
     * @param aMetaElementType
     *            The meta element type must NOT be <code>null</code>.
     */
    public ExtendedSearchModelBuilder getSearchModelBuilder(String aMetaElementType) {
        return this.modelBuilder.get(aMetaElementType);
    }
    
    /**
     * This method returns the model builder for the given meta element.
     * 
     * @param aMetaElement
     *            The meta element must NOT be <code>null</code>.
     */
    public ExtendedSearchModelBuilder getSearchModelBuilder(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement.getName());
    }
    
    /** 
     * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
        if (aComponent instanceof AttributedSearchComponent) {
            AttributedSearchComponent searchComponent = (AttributedSearchComponent) aComponent;
            TLClass metaElement = searchComponent.getSearchMetaElement();

			return getSearchModelBuilder(metaElement).getModel(businessModel, aComponent);
        }
        
        throw new TopLogicException(this.getClass(), "The component ('" + aComponent.getName() + "') isn't a AttributedSearchComponent.");
    }

    /** 
     * Dispatch via {@link #getSearchModelBuilder(String)}
     * 
     * @see com.top_logic.element.layout.meta.search.SearchModelBuilder#getMetaElement(java.lang.String)
     */
    @Override
	public TLClass getMetaElement(String aME) throws IllegalArgumentException {
        ExtendedSearchModelBuilder esmb = getSearchModelBuilder(aME);
        if (esmb == null) {
            throw new NullPointerException("No ExtendedSearchModelBuilder for '" + aME + "' configured");
        }
        return esmb.getMetaElement(aME);
    }

    /** 
     * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
        if (aComponent instanceof AttributedSearchComponent) {
            AttributedSearchComponent searchComponent = (AttributedSearchComponent) aComponent;
            TLClass metaElement = searchComponent.getSearchMetaElement();

            return getSearchModelBuilder(metaElement).supportsModel(aModel, aComponent);
        }
        
        throw new TopLogicException(this.getClass(), "The component ('" + aComponent.getName() + "') isn't a AttributedSearchComponent.");
    }

    /** 
     * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getExcludedAttributesForSearch(com.top_logic.model.TLClass)
     */
    @Override
	public Set<String> getExcludedAttributesForSearch(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement).getExcludedAttributesForSearch(aMetaElement);
    }

    @Override
	public Set<String> getExcludedAttributesForColumns(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement).getExcludedAttributesForColumns(aMetaElement);
    }
    
    @Override
	public Set<String> getExcludedAttributesForReporting(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement).getExcludedAttributesForReporting(aMetaElement);
    }
    
    /** 
     * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getResultColumnsFor(com.top_logic.model.TLClass)
     */
    @Override
	public List<String> getResultColumnsFor(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement).getResultColumnsFor(aMetaElement);
    }

    /** 
     * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getJspFor(com.top_logic.model.TLClass)
     */
    @Override
	public String getJspFor(TLClass aMetaElement) {
        return getSearchModelBuilder(aMetaElement).getJspFor(aMetaElement);
    }
    
    /**
     * Set the model builders configured in layout xml as defined by {@link #XML_LIST_MODEL_BUILDERS}.
     */
	private boolean setModelBuilders(List<ExtendedSearchModelBuilder> someBuilders) {
		for (ExtendedSearchModelBuilder theBuilder : someBuilders) {
            TLClass                theMeta    = theBuilder.getMetaElement(null);
            this.modelBuilder.put(theMeta.getName(), theBuilder);
        }
        return true;
    }
    
}

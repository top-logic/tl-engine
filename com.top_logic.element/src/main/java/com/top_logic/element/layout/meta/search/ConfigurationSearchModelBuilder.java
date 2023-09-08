/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Mappings;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * The {@link ConfigurationSearchModelBuilder} get the neccessary information of the
 * layout configuration. The attributes searchObjectsProducer, excludedAttributes,
 * excludedAttributesForReporting, page and resultColumns can be following encoded.
 * 
 * metaElementName1{value1},metaElementName2{value2}
 * E.g. configuration
 *  ...
 *  page='projElement.Project{/jsp/project/search/SearchInput.jsp};riskItem.goal{/jsp/search/SearchInputGoal.jsp}'
 *  excludedAttributes='projElement.Project{testContainer,testContainerDate};riskItem.goal{responsible}'
 *  ...
 *  
 * For all configured meta elements (e.g. projElement.Project and riskItem.goal) must the
 * attribues: searchObjectsProducer, excludedAttributes, excludedAttributesForReporting, 
 * page and resultColumns have a configuration (see above). It isn't allow to leave 
 * a configuration for an declared meta element. In this case is the configuration 
 * (e.g. projElement.Project{}) empty but NOT missing.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ConfigurationSearchModelBuilder extends AbstractConfiguredInstance<ConfigurationSearchModelBuilder.Config>
		implements ExtendedSearchModelBuilder {

	public interface Config extends PolymorphicConfiguration<ExtendedSearchModelBuilder> {
		@Name("searchObjectsProducer")
		@Mandatory
		String getSearchObjectsProducer();

		@Name("excludedAttributesForSearch")
		@Mandatory
		String getExcludedAttributesForSearch();

		@Name("excludedAttributesForColumns")
		@Mandatory
		String getExcludedAttributesForColumns();

		@Name("excludedAttributesForReporting")
		@Mandatory
		String getExcludedAttributesForReporting();

		@Name("typedResultColumns")
		@Mandatory
		String getTypedResultColumns();

		@Name("page")
		@Mandatory
		String getPage();
	}

	private Map searchObjectsProducer;
	private Map excludedAttributesForSearch;
	private Map excludedAttributesForColumns;
	private Map excludedAttributesForReporting;
	private Map page;
	private Map resultColumns;

	/** 
	 * The framework uses this constructor to create new
	 * instances of {@link ConfigurationSearchModelBuilder}s.
	 */
	public ConfigurationSearchModelBuilder(InstantiationContext context, Config someAttrs)
			throws ConfigurationException {
		super(context, someAttrs);
		this.searchObjectsProducer =
			instantiate(new InstanceMapping(), someAttrs.getSearchObjectsProducer());
		this.excludedAttributesForSearch =
			instantiate(new SetMapping(), someAttrs.getExcludedAttributesForSearch());
		this.excludedAttributesForColumns =
			instantiate(new SetMapping(), someAttrs.getExcludedAttributesForColumns());
		this.excludedAttributesForReporting =
			instantiate(new SetMapping(), someAttrs.getExcludedAttributesForReporting());
		this.resultColumns =
			instantiate(new ListMapping(), someAttrs.getTypedResultColumns());
		this.page = instantiate(Mappings.identity(), someAttrs.getPage());
	}

	/** 
	 * @see com.top_logic.mig.html.ModelBuilder#getModel(Object, com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		if (aComponent instanceof AttributedSearchComponent) {
			AttributedSearchComponent searchComponent = (AttributedSearchComponent) aComponent;
			TLClass metaElement = searchComponent.getSearchMetaElement();

			return ((ModelBuilder) this.searchObjectsProducer.get(metaElement)).getModel(businessModel, aComponent);
		}
		
		throw new TopLogicException(this.getClass(), "The component ('" + aComponent.getName() + "') isn't a AttributedSearchComponent.");
	}

	/** 
	 * @see com.top_logic.mig.html.ModelBuilder#supportsModel(java.lang.Object, com.top_logic.mig.html.layout.LayoutComponent)
	 */
	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof TLClass;
	}
	
	/** 
	 * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getExcludedAttributesForSearch(com.top_logic.model.TLClass)
	 */
	@Override
	public Set getExcludedAttributesForSearch(TLClass aMetaElement) {
		Set excludedAttributes = (Set) this.excludedAttributesForSearch.get(aMetaElement);
		excludedAttributes.add(WebFolderFactory.STANDARD_FOLDER);
		excludedAttributes.add("folder");
		excludedAttributes.add("documents");
		
		return excludedAttributes;
	}

	@Override
	public Set getExcludedAttributesForColumns(TLClass aMetaElement) {
		Set excludedAttributes = (Set) this.excludedAttributesForColumns.get(aMetaElement);
		excludedAttributes.add(WebFolderFactory.STANDARD_FOLDER);
		excludedAttributes.add("folder");
		excludedAttributes.add("documents");
		
		return excludedAttributes;
	}
	
	/** 
	 * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getExcludedAttributesForReporting(com.top_logic.model.TLClass)
	 */
	@Override
	public Set getExcludedAttributesForReporting(TLClass aMetaElement) {
		Set excludedAttributesForReporting = (Set) this.excludedAttributesForReporting.get(aMetaElement);
		excludedAttributesForReporting.addAll(getExcludedAttributesForSearch(aMetaElement));
		
		return excludedAttributesForReporting;
	}

	/** 
	 * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getJspFor(com.top_logic.model.TLClass)
	 */
	@Override
	public String getJspFor(TLClass aMetaElement) {
		return (String) this.page.get(aMetaElement);
	}

	/** 
	 * @see com.top_logic.element.layout.meta.search.ExtendedSearchModelBuilder#getResultColumnsFor(com.top_logic.model.TLClass)
	 */
	@Override
	public List getResultColumnsFor(TLClass aMetaElement) {
		return (List) this.resultColumns.get(aMetaElement);
	}

	@Override
	public TLClass getMetaElement(String typeName) throws IllegalArgumentException {
		for (Iterator iterator = MetaElementFactory.getInstance().getAllMetaElements().iterator(); iterator.hasNext();) {
			TLClass metaElement = (TLClass) iterator.next();
			if (typeName.equals(metaElement.getName())) {
				return metaElement;
			}
		}
		
		throw new TopLogicException(this.getClass(), "Couldn't find type '" + typeName + "'.");
	}

	private Map instantiate(Mapping mapping, String attributeValue) {
		String[] splitValues = StringServices.split(attributeValue, ';');
		Map map = new HashMap();
		
		for (int i = 0; i < splitValues.length; i++) {
			String value = splitValues[i];
			int firstIndex = value.indexOf('{');
			int secondIndex = value.indexOf('}');
			
			String metaElementName = value.substring(0, firstIndex);
			String metaElementValue = value.substring(firstIndex + 1, secondIndex);
			map.put(getMetaElement(metaElementName), mapping.map(metaElementValue));
		}
		
		return map;
	}

	private class ListMapping implements Mapping<String, List<String>> {

		public ListMapping() {
			// default constructor
		}

		@Override
		public List<String> map(String input) {
			if (input.isEmpty()) {
				return Collections.emptyList();
			}
			return Arrays.asList(StringServices.split(input, ','));
		}
		
	}

	private class SetMapping implements Mapping<String, Set<String>> {

		public SetMapping() {
			// default constructor
		}

		@Override
		public Set<String> map(String input) {
			HashSet<String> result = new HashSet<>();
			if (!input.isEmpty()) {
				Collections.addAll(result, StringServices.split(input, ','));
			}
			return result;
		}
		
	}
	
	private class InstanceMapping implements Mapping<String, Object> {

		public InstanceMapping() {
			// default constructor
		}

		@Override
		public Object map(String input) {
			try {
				return Class.forName(input).newInstance();
			} catch (Exception e) {
				throw new TopLogicException(getClass(), "Couldn't instanciate an object of the class ('" + input
					+ "').");
			} 
		}
		
	}
	
}

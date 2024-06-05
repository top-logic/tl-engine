/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.util.Utils.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Default configurable model builder for {@link AttributedSearchComponent}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DefaultConfigurableSearchModelBuilder<C extends DefaultConfigurableSearchModelBuilder.Config>
		extends AbstractExtendedSearchModelBuilder
		implements ConfiguredInstance<DefaultConfigurableSearchModelBuilder.Config> {

	/**
	 * Configuration just holds the different {@link ElementConfig}s.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends PolymorphicConfiguration<DefaultConfigurableSearchModelBuilder<?>> {

		/**
		 * Elements supported by this search model builder (name is {@link TLClass}
		 * ({@link TLClass}) name).
		 */
		@Mandatory
		@Key(ElementConfig.NAME_ATTRIBUTE)
		Map<String, ElementConfig> getElements();
	}

	/**
	 * Search configuration for one kind of business object.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface ElementConfig extends NamedConfiguration {

		/** Type of knowledge object to be found. */
		String getKOType();

		/** Input JSP to define the search. */
		@StringDefault("/jsp/element/search/SearchInput.jsp")
		String getPage();

		/**
		 * Default column selection when initially displaying the search input and result page.
		 * 
		 * @see AttributedSearchComponent.Config#getResultColumns() Constraints for the usage of
		 *      this value.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getResultColumns();

		/** Attribute names to be excluded from column selection. */
		@Format(CommaSeparatedStrings.class)
		List<String> getExcludeColumns();

		/** Attribute names to be excluded from reporting. */
		@Format(CommaSeparatedStrings.class)
		List<String> getExcludeReporting();

		/** Attribute names to be excluded from search. */
		@Format(CommaSeparatedStrings.class)
		List<String> getExcludeSearch();

		/**
		 * Attribute names to be excluded from everything: {@link #getExcludeColumns()},
		 * {@link #getExcludeReporting()} and {@link #getExcludeSearch()}.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getExclude();

	}

	private final C _config;

	private TLClass _searchedType;

	/**
	 * Called by the {@link TypedConfiguration} for creating a
	 * {@link DefaultConfigurableSearchModelBuilder}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public DefaultConfigurableSearchModelBuilder(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent searchComponent) {
		if (!(searchComponent instanceof AttributedSearchComponent)) {
			errorUnsupportedSearchComponentType(searchComponent);
		}
		_searchedType = ((AttributedSearchComponent) searchComponent).getSearchMetaElement();
		return getWrappersByType(Wrapper.class);
	}

	private void errorUnsupportedSearchComponentType(LayoutComponent searchComponent) {
		String message = "The component ('" + searchComponent.getName() + "') isn't an AttributedSearchComponent: "
			+ debug(searchComponent);
		throw new TopLogicException(ResKey.text(message));
	}

	@Override
	public TLClass getMetaElement(String metaElementType) {
		return MetaElementUtil.getMetaElement(metaElementType);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model instanceof TLClass;
	}

	@Override
	public String getJspFor(TLClass aME) {
		if (aME == null) {
			return null;
		}
		ElementConfig theConfig = getElementConfig(aME);
		return theConfig.getPage();
	}

	@Override
	public List<String> getResultColumnsFor(TLClass aME) {
		ElementConfig theConfig = getElementConfig(aME);
		return theConfig.getResultColumns();
	}

	@Override
	public Set<String> getExcludedAttributesForColumns(TLClass aME) {
		Set<String> theSet = super.getExcludedAttributesForColumns(aME);
		return addAllTo(theSet, getElementConfig(aME).getExcludeColumns());
	}

	@Override
	public Set<String> getExcludedAttributesForReporting(TLClass aME) {
		Set<String> theSet = super.getExcludedAttributesForReporting(aME);
		return addAllTo(theSet, getElementConfig(aME).getExcludeReporting());
	}

	@Override
	public Set<String> getExcludedAttributesForSearch(TLClass aME) {
		Set<String> theSet = super.getExcludedAttributesForSearch(aME);
		return addAllTo(theSet, getElementConfig(aME).getExcludeSearch());
	}

	@Override
	public Set<String> getExcludedAttributes(TLClass aME) {
		Set<String> theSet = super.getExcludedAttributes(aME);
		return addAllTo(theSet, getElementConfig(aME).getExclude());
	}

	/**
	 * The wrappers to search on.
	 */
	protected <W extends Wrapper> List<W> getWrappersByType(Class<W> wrapperType) {
		List<W> wrappersByKO = getWrappersByKOType(wrapperType);
		List<W> result = list();
		/* "subtypes" is an optimization for faster checking which of the wrappers are of the
		 * searched type. */
		Collection<TLClass> subtypes = getAllSubtypes(_searchedType);
		for (W wrapper : wrappersByKO) {
			if (subtypes.contains(wrapper.tType())) {
				result.add(wrapper);
			}
		}
		return result;
	}

	private Set<TLClass> getAllSubtypes(TLClass searchedType) {
		return TLModelUtil.getReflexiveTransitiveSpecializations(searchedType);
	}

	/**
	 * Return all wrappers of the configured KO-type known by the {@link KnowledgeBaseFactory}.
	 * 
	 * @return The requested list of wrappers.
	 */
	private <W extends Wrapper> List<W> getWrappersByKOType(Class<W> wrapperType) {
		String koType = getConfiguredKOType(_searchedType);
		Collection<KnowledgeObject> knowledgeObjects = getKnowledgeBase().getAllKnowledgeObjects(koType);
		return WrapperFactory.getWrappersForKOs(wrapperType, knowledgeObjects);
	}

	/** The {@link KnowledgeBase} to use. */
	protected final KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/** The KO-type configured for the given {@link TLClass}. */
	protected String getConfiguredKOType(TLClass searchedType) {
		return getElementConfig(searchedType).getKOType();
	}

	/**
	 * Return the configuration for the given {@link TLClass}.
	 * 
	 * @param searchedType
	 *        The {@link TLClass} whose instances are searched.
	 * @return The requested configuration.
	 */
	protected ElementConfig getElementConfig(TLClass searchedType) {
		return getConfig().getElements().get(searchedType.getName());
	}

	/**
	 * The configuration of this instance.
	 */
	@Override
	public C getConfig() {
		return _config;
	}

}

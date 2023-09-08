/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.filter.ModelFilter;
import com.top_logic.model.filter.ModelFilterConfig;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.supplier.PredefinedSearchParameterCalculator;
import com.top_logic.model.search.expr.supplier.SupplierSearchExpressionBuilder;
import com.top_logic.model.search.ui.model.AttributeFilter;
import com.top_logic.model.search.ui.model.combinator.SearchExpressionCombinator;
import com.top_logic.util.model.ModelService;

/**
 * The module of the model based search.
 * <p>
 * Holds the config options for the search. This has two advantages. First, without this module,
 * they would be distributed all over the subpackages, hidden for example in option providers.
 * Second, this allows multiple search configurations. For example one for the normal users, one for
 * power users and one for developers.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@ServiceDependencies({
	SearchBuilder.Module.class
})
public class ModelBasedSearch extends ConfiguredManagedClass<ModelBasedSearch.Config> {

	/**
	 * The configuration of a model based search.
	 * <p>
	 * There can be different searches (for example: normal user, power user, developer) in one
	 * application. To configure these searches, the actual search configuration is extracted to
	 * this interface and the {@link ModelBasedSearch} module can have various of these configs.
	 * </p>
	 */
	public interface SearchConfig extends NamedConfiguration, ModelFilterConfig {

		/** Property name of {@link #getCombinators()}. */
		String COMBINATORS = "combinators";

		/** Property name of {@link #getMaxAttributesShown()}. */
		String MAX_ATTRIBUTES_SHOWN = "max-attributes-shown";

		/** Property name of {@link #getIncludeSubtypeUsages()}. */
		String INCLUDE_SUBTYPE_USAGES = "include-subtype-usages";

		/** Property name of {@link #getEnableOptimizations()}. */
		String DISABLE_OPTIMIZATIONS = "disable-optimizations";

		/** Property name of {@link #getAdditionalPredefinedParameters()}. */
		String ADDITIONAL_PREDEFINED_PARAMETERS = "additional-predefined-parameters";

		/** Property name of {@link #getDisabledPredefinedParameters()}. */
		String DISABLED_PREDEFINED_PARAMETERS = "disabled-predefined-parameters";

		/**
		 * The {@link SearchExpressionCombinator}s that should be available in the search.
		 */
		@InstanceFormat
		@Name(COMBINATORS)
		List<SearchExpressionCombinator> getCombinators();

		/**
		 * If there are no main properties defined for a type, this is the maximum number of
		 * attributes for {@link AttributeFilter#getAttribute()} that are offered before the user
		 * selects the {@link AttributeFilter}.
		 * <p>
		 * The other attributes are offered once the user selects the {@link AttributeFilter}.
		 * </p>
		 */
		@IntDefault(5)
		@Name(MAX_ATTRIBUTES_SHOWN)
		int getMaxAttributesShown();

		/**
		 * Whether usages of subtypes should be included, when usages of a type are displayed.
		 */
		@BooleanDefault(true)
		@Name(INCLUDE_SUBTYPE_USAGES)
		boolean getIncludeSubtypeUsages();

		/**
		 * Enable optimizations.
		 * <p>
		 * Disabling the optimizations can be useful for example for debugging.
		 * </p>
		 */
		@BooleanDefault(true)
		@Name(DISABLE_OPTIMIZATIONS)
		boolean getEnableOptimizations();

		/**
		 * {@link SupplierSearchExpressionBuilder} in addition to those registered in
		 * {@link com.top_logic.model.search.expr.config.SearchBuilder.Config#getMethods()}.
		 */
		@Name(ADDITIONAL_PREDEFINED_PARAMETERS)
		List<SupplierSearchExpressionBuilder.Config> getAdditionalPredefinedParameters();

		/**
		 * {@link SupplierSearchExpressionBuilder} to disable from those registered in
		 * {@link com.top_logic.model.search.expr.config.SearchBuilder.Config#getMethods()}.
		 */
		@Name(DISABLED_PREDEFINED_PARAMETERS)
		List<SupplierSearchExpressionBuilder.Config> getDisabledPredefinedParameters();

	}

	/**
	 * The {@link TypedConfiguration} of the {@link ModelBasedSearch}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<ModelBasedSearch> {

		/**
		 * There can be different searches (for example: normal user, power user, developer) in one
		 * application.
		 */
		@EntryTag("search")
		@Key(SearchConfig.NAME_ATTRIBUTE)
		Map<String, SearchConfig> getSearches();

	}

	private final Map<String, List<SupplierSearchExpressionBuilder>> _predefinedParameters;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ModelBasedSearch}.
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
	public ModelBasedSearch(InstantiationContext context, Config config) {
		super(context, config);
		_predefinedParameters = createParameterProviders(context, config);
	}

	private Map<String, List<SupplierSearchExpressionBuilder>> createParameterProviders(
			InstantiationContext context, Config config) {
		Map<String, List<SupplierSearchExpressionBuilder>> predefinedParameters = map();
		for (SearchConfig searchConfig : config.getSearches().values()) {
			predefinedParameters.put(searchConfig.getName(), createPredefinedParameters(context, searchConfig));
		}
		return unmodifiableMap(predefinedParameters);
	}

	private List<SupplierSearchExpressionBuilder> createPredefinedParameters(
			InstantiationContext context, SearchConfig searchConfig) {
		return unmodifiableList(getInstanceList(context, getPredefinedParameters(searchConfig)));
	}

	private List<SupplierSearchExpressionBuilder.Config> getPredefinedParameters(SearchConfig searchConfig) {
		return PredefinedSearchParameterCalculator.INSTANCE.apply(searchConfig);
	}

	/**
	 * Returns the {@link SearchConfig} for the given name.
	 * <p>
	 * The returned {@link ConfigurationItem}s must not be changed.
	 * </p>
	 * 
	 * @param searchName
	 *        Is not allowed to be null.
	 */
	public final SearchConfig getSearchConfig(String searchName) {
		return getConfig().getSearches().get(searchName);
	}

	/**
	 * Filters which {@link TLModelPart}s that should be displayed.
	 * 
	 * @param modelParts
	 *        Is allowed to be null. Is not allowed to contain null.
	 * @param searchName
	 *        Is not allowed to be null.
	 * @return A modifiable and resizable {@link List} containing the model parts that should be
	 *         displayed.
	 */
	public <T extends TLModelPart> List<T> filterModel(Collection<? extends T> modelParts, String searchName) {
		return applySearchModelFilter(ModelService.getInstance().filterModel(modelParts), searchName);
	}

	private <T extends TLModelPart> List<T> applySearchModelFilter(List<T> modelParts, String searchName) {
		ModelFilter<?> modelFilter = getSearchConfig(searchName).getModelFilter();

		if (modelFilter != null) {
			return modelFilter.filterModel(modelParts);
		}

		return modelParts;
	}

	/**
	 * The {@link SupplierSearchExpressionBuilder}s for the search with the given name.
	 * 
	 * @param searchName
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public List<SupplierSearchExpressionBuilder> getPredefinedParameters(String searchName) {
		if (!_predefinedParameters.containsKey(searchName)) {
			throw new IllegalArgumentException("No such search configuration: '" + searchName + "'");
		}
		return _predefinedParameters.get(searchName);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("searches", getConfig().getSearches())
			.build();
	}

	/**
	 * The singleton {@link ModelBasedSearch} instance.
	 */
	public static ModelBasedSearch getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * {@link TypedRuntimeModule} of the {@link ModelBasedSearch}.
	 */
	public static final class Module extends TypedRuntimeModule<ModelBasedSearch> {

		/**
		 * Singleton module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<ModelBasedSearch> getImplementation() {
			return ModelBasedSearch.class;
		}

	}

}

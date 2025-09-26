/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import static com.top_logic.basic.config.TypedConfiguration.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.list;
import static com.top_logic.basic.util.Utils.*;
import static java.util.Collections.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * A {@link GlobalModelNamingScheme} that identifies a node in a trees of {@link TLObject}s by its
 * label.
 * <p>
 * It uses the {@link MetaLabelProvider} per default, but that can be overridden in the
 * configuration per {@link TLClass}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLObjectTreeNaming
		extends AbstractModelNamingScheme<TLObject, TLObjectTreeNaming.TLObjectTreeName, Object> {

	/** {@link ModelName} for the {@link TLObjectTreeNaming}. */
	public interface TLObjectTreeName extends ModelName {

		/**
		 * The root {@link TLObject} from which the {@link #getPath()} starts. If not given, the
		 * root object is derived from the value context of the name.
		 */
		ModelName getRoot();

		/** @see #getRoot() */
		void setRoot(ModelName root);

		/**
		 * The {@link TLObjectTreeStep} path to the identified {@link TLObject}.
		 * <p>
		 * The path does not contain the root, as that is referenced separately in
		 * {@link #getRoot()}.
		 * </p>
		 */
		List<TLObjectTreeStep> getPath();

		/** @see #getPath() */
		void setPath(List<TLObjectTreeStep> path);

	}

	/** One step in the {@link TLObjectTreeName}. */
	public interface TLObjectTreeStep extends ConfigurationItem {

		/** The name of the attribute in which the inner {@link TLObject} is stored. */
		String getAttribute();

		/** @see #getAttribute() */
		void setAttribute(String attribute);

		/** The label of the {@link TLObject} stored in the {@link #getAttribute()}. */
		String getNode();

		/** @see #getNode() */
		void setNode(String node);

		/** Creates a {@link TLObjectTreeStep}. */
		static TLObjectTreeStep create(String attribute, String node) {
			TLObjectTreeStep step = newConfigItem(TLObjectTreeStep.class);
			step.setAttribute(attribute);
			step.setNode(node);
			return step;
		}

	}

	/**
	 * The {@link ConfigurationItem} for the {@link TLObjectTreeNaming}.
	 * 
	 * @see #getTypeConfigs()
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * A {@link Map} of types and which {@link LabelProvider} should be used for each of them.
		 * 
		 * @see TLObjectTreeNamingTypeConfig
		 */
		@Key(TLObjectTreeNamingTypeConfig.TYPE)
		@Name("types")
		Map<String, TLObjectTreeNamingTypeConfig> getTypeConfigs();

	}

	/**
	 * Defines that a non-standard {@link LabelProvider} has to be used for instances of a given
	 * {@link TLClass}.
	 * <p>
	 * This includes instances of subtypes: They use the configured {@link LabelProvider}, too.
	 * </p>
	 */
	public interface TLObjectTreeNamingTypeConfig extends ConfigurationItem {

		/** Property name of {@link #getType()}. */
		String TYPE = "type";

		/** The qualified name of the {@link TLClass}. */
		@Name(TYPE)
		@Mandatory
		String getType();

		/** The {@link LabelProvider} that should be used for instances of this {@link TLClass}. */
		@Name("label-provider")
		@NonNullable
		@InstanceFormat
		LabelProvider getLabelProvider();

	}

	private static final LabelProvider DEFAULT_LABEL_PROVIDER = MetaLabelProvider.INSTANCE;

	private Map<TLClass, LabelProvider> _labelProviders = null;

	/** Creates a {@link TLObjectTreeNaming}. */
	public TLObjectTreeNaming() {
		super(TLObject.class, TLObjectTreeName.class, Object.class);
	}

	@Override
	protected boolean isCompatibleModel(Object valueContext, TLObject model) {
		if (model.tType().getModelKind() != ModelKind.CLASS) {
			return false;
		}
		return model.tContainer() != null;
	}

	@Override
	protected void initName(Object valueContext, TLObjectTreeName name, TLObject model) {
		List<TLObjectTreeStep> path = list();

		TLObject node = model;
		TLObject parent = node.tContainer();
		while (true) {
			ModelName contextName = buildLocalName(valueContext, node);
			if (contextName != null) {
				name.setRoot(contextName);
				break;
			}
			if (parent == null) {
				// Hit the top of the hierarchy.
				TLObject root = node;
				name.setRoot(ModelResolver.buildModelName(valueContext, root));
				break;
			}

			path.add(createStep(node));
			node = parent;
			parent = parent.tContainer();
		}
		reverse(path);

		name.setPath(path);
	}

	/**
	 * Tries to create a context-local name for the given object.
	 * 
	 * @return a context-local name for the given object, or <code>null</code>, if this is not
	 *         possible.
	 */
	private ModelName buildLocalName(Object valueContext, TLObject obj) {
		if (valueContext instanceof LayoutComponent component) {
			// Note: Other channels as the model channel cannot be used to identify a context
			// object, since an operation on component A might e.g. chance the value the component
			// A's selection. Since the action is recorded after the operation, it may try to
			// reference an object based on state that is not available during replay. Typically,
			// the model channel of component A does not change, when performing an operation on
			// that component.
			Collection<?> model = CollectionUtil.asCollection(component.getModel());
			if (model.isEmpty()) {
				return null;
			} else if (model.size() == 1) {
				if (model.iterator().next() == obj) {
					ComponentModel.Config<?> channelRef =
						TypedConfiguration.newConfigItem(ComponentModel.Config.class);
					return channelRef;
				}
			} else {
				TLStructuredType type = obj.tType();
				List<TLObject> withType = model.stream()
					.filter(x -> x instanceof TLObject)
					.map(x -> ((TLObject) x))
					.filter(x -> x.tType() == type).toList();
				if (withType.isEmpty()) {
					return null;
				} else if (withType.size() == 1) {
					if (withType.iterator().next() == obj) {
						ComponentModel.Config<?> channelRef =
							TypedConfiguration.newConfigItem(ComponentModel.Config.class);
						channelRef.setType(TLModelPartRef.ref(type));
						return channelRef;
					}
				} else {
					String id = getLabel(obj);
					List<TLObject> withId = withType.stream().filter(x -> id.equals(getLabel(x))).toList();
					if (withId.isEmpty()) {
						return null;
					} else if (withId.size() == 1) {
						if (withId.iterator().next() == obj) {
							ComponentModel.Config<?> channelRef =
								TypedConfiguration.newConfigItem(ComponentModel.Config.class);
							channelRef.setType(TLModelPartRef.ref(type));
							channelRef.setId(id);
							return channelRef;
						}
					}
				}
			}
		}
		return null;
	}

	private TLObjectTreeStep createStep(TLObject node) {
		String attribute = node.tContainerReference().getName();
		String label = getLabel(node);
		return TLObjectTreeStep.create(attribute, label);
	}

	@Override
	public TLObject locateModel(ActionContext context, Object valueContext, TLObjectTreeName name) {
		TLObject root;
		ModelName rootName = name.getRoot();
		if (rootName instanceof LocalName<?> localName) {
			ContextResolver resolver = TypedConfigUtil.createInstance(localName);
			root = (TLObject) resolver.resolve(this::getLabel, valueContext);
		} else {
			root = (TLObject) context.resolve(rootName, valueContext);
		}

		TLObject node = root;
		for (TLObjectTreeStep step : name.getPath()) {
			node = findChild(node, step);
		}
		return node;
	}

	private TLObject findChild(TLObject node, TLObjectTreeStep step) {
		try {
			return findChildInternal(node, step);
		} catch (RuntimeException exception) {
			throw new RuntimeException("Failed to find the next node in the path. Parent: " + debug(node)
				+ ". Attribute: '" + step.getAttribute() + "'. Child label: '" + step.getNode()
				+ "'. Cause: " + exception.getMessage(), exception);
		}
	}

	private TLObject findChildInternal(TLObject parent, TLObjectTreeStep step) {
		Object innerValue = parent.tValueByName(step.getAttribute());
		String searchedLabel = step.getNode();
		if (innerValue instanceof TLObject) {
			TLObject child = (TLObject) innerValue;
			assertHasSearchedLabel(searchedLabel, child);
			return child;
		}
		if (innerValue instanceof Collection) {
			return searchChild(searchedLabel, (Collection<?>) innerValue);
		}
		throw new RuntimeException("Expected a TLObject or a Collection of them. Actual value: " + debug(innerValue));
	}

	private void assertHasSearchedLabel(String searchedLabel, TLObject child) {
		String actualLabel = getLabel(child);
		if (!Objects.equals(actualLabel, searchedLabel)) {
			throw new RuntimeException("The single contained TLObject does not have the expected label."
				+ " Expected: '" + searchedLabel + "'. Actual: '" + actualLabel + "'");
		}
	}

	private TLObject searchChild(String searchedLabel, Collection<?> children) {
		SearchResult<TLObject> searchResult = new SearchResult<>();
		for (Object child : children) {
			searchResult.addCandidate(child);
			if (isSearchedChild(searchedLabel, child)) {
				searchResult.add((TLObject) child);
			}
		}
		return searchResult.getSingleResult("Failed to find the child with the label '" + searchedLabel + "'.");
	}

	private boolean isSearchedChild(String searchedLabel, Object child) {
		if (!(child instanceof TLObject)) {
			return false;
		}
		TLObject tlChild = (TLObject) child;
		if (tlChild.tType().getModelKind() != ModelKind.CLASS) {
			return false;
		}
		String actualLabel = getLabel(tlChild);
		if (StringServices.isEmpty(actualLabel)) {
			return false;
		}
		return Objects.equals(actualLabel, searchedLabel);
	}

	private String getLabel(TLObject model) {
		TLClass type = (TLClass) model.tType();
		String label = getLabelProvider(type).getLabel(model);
		if (StringServices.isEmpty(label)) {
			throw new RuntimeException("The label for the object must not be null or empty. Object: " + debug(model));
		}
		return label;
	}

	private LabelProvider getLabelProvider(TLClass type) {
		LabelProvider labelProvider = searchLabelProvider(type);
		if (labelProvider == null) {
			/* Apply the default _after_ searching and initializing the LabelProvider map.
			 * Otherwise, the default LabelProvider would be stored as the configured LabelProvider
			 * for types without a configured LabelProvider. For subtypes of such types, this would
			 * cause the search to return the stored default LabelProvider and not keep searching in
			 * other super types. */
			return DEFAULT_LABEL_PROVIDER;
		}
		return labelProvider;
	}

	private LabelProvider searchLabelProvider(TLClass type) {
		Map<TLClass, LabelProvider> labelProviders = getLabelProviders();
		LabelProvider labelProvider = labelProviders.get(type);
		if (labelProvider != null) {
			return labelProvider;
		}

		LabelProvider superTypeLabelProvider = searchLabelProviderInSuperTypes(type);
		Map<TLClass, LabelProvider> labelProviders2 = getLabelProviders();
		labelProviders2.put(type, superTypeLabelProvider);
		return superTypeLabelProvider;
	}

	private LabelProvider searchLabelProviderInSuperTypes(TLClass type) {
		for (TLClass superType : type.getGeneralizations()) {
			LabelProvider superTypeLabelProvider = searchLabelProvider(superType);
			if (superTypeLabelProvider != null) {
				return superTypeLabelProvider;
			}
		}
		return null;
	}

	private Map<TLClass, LabelProvider> getLabelProviders() {
		if (_labelProviders == null) {
			_labelProviders = createLabelProvidersMap();
		}
		return _labelProviders;
	}

	private Map<TLClass, LabelProvider> createLabelProvidersMap() {
		Map<TLClass, LabelProvider> result = map();
		for (TLObjectTreeNamingTypeConfig typeConfig : getConfig().getTypeConfigs().values()) {
			TLClass tlClass = resolveTLClass(typeConfig.getType());
			if (tlClass == null) {
				continue;
			}
			result.put(tlClass, typeConfig.getLabelProvider());
		}
		return result;
	}

	private Config getConfig() {
		return ApplicationConfig.getInstance().getConfig(TLObjectTreeNaming.Config.class);
	}

	private TLClass resolveTLClass(String tlClassName) {
		try {
			return (TLClass) TLModelUtil.resolveQualifiedName(tlClassName);
		} catch (TopLogicException exception) {
			/* Avoid breaking the initialization. As this happens lazy, it would be repeated on
			 * every access attempt. And that would flood the log. */
			logError("Failed to resolve the TLClass '" + tlClassName + "'. Cause: " + exception.getMessage(), exception);
			return null;
		}
	}

	private void logError(String message, Throwable exception) {
		Logger.error(message, exception, TLObjectTreeNaming.class);
	}

	/**
	 * {@link ModelName} that can be instantiated to a {@link ContextResolver}.
	 */
	public interface LocalName<I extends ContextResolver> extends ModelName, PolymorphicConfiguration<I> {
		// Marker interface.
	}

	/**
	 * Algorithm for resolving the value context of a {@link ModelNamingScheme} to a root object to
	 * start locating an object.
	 */
	public interface ContextResolver {
		/**
		 * Resolves the root object from the naming scheme's value context.
		 */
		Object resolve(Function<TLObject, String> idProvider, Object valueContext);
	}

	/**
	 * {@link ContextResolver} for the context component's model.
	 */
	public static class ComponentModel extends AbstractConfiguredInstance<ComponentModel.Config<?>>
			implements ContextResolver {

		/**
		 * Configuration options for {@link TLObjectTreeNaming.ComponentModel}.
		 */
		public interface Config<I extends TLObjectTreeNaming.ComponentModel> extends LocalName<I> {
			/**
			 * Type of the referenced model. If set, used to identify object uniquely.
			 */
			TLModelPartRef getType();

			/**
			 * @see #getType()
			 */
			void setType(TLModelPartRef value);

			/**
			 * Unique name of the referenced object. If set, used to identify the object uniquely.
			 */
			@Nullable
			String getId();

			/**
			 * @see #getId()
			 */
			void setId(String id);

		}

		/**
		 * Creates a {@link TLObjectTreeNaming.ComponentModel} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public ComponentModel(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Object resolve(Function<TLObject, String> idProvider, Object valueContext) {
			Stream<TLObject> candidates =
				CollectionUtil.asCollection(((LayoutComponent) valueContext).getModel())
					.stream()
					.filter(x -> x instanceof TLObject)
					.map(x -> ((TLObject) x));
			TLModelPartRef typeRef = getConfig().getType();
			if (typeRef != null) {
				TLType type = typeRef.resolveType();
				candidates = candidates.filter(x -> x.tType() == type);
			}
			String id = getConfig().getId();
			if (id != null) {
				candidates = candidates.filter(x -> id.equals(idProvider.apply(x)));
			}
			List<TLObject> matches = candidates.toList();
			if (matches.isEmpty()) {
				throw ApplicationAssertions.fail(getConfig(), "No match found.");
			}
			if (matches.size() > 1) {
				throw ApplicationAssertions.fail(getConfig(), "No unique match found, candidates: " + matches);
			}
			return matches.get(0);
		}
	}

}

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

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.NonNullable;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
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
public class TLObjectTreeNaming extends AbstractModelNamingScheme<TLObject, TLObjectTreeNaming.TLObjectTreeName> {

	/** {@link ModelName} for the {@link TLObjectTreeNaming}. */
	public interface TLObjectTreeName extends ModelName {

		/** The root {@link TLObject} from which the {@link #getPath()} starts. */
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
		super(TLObject.class, TLObjectTreeName.class);
	}

	@Override
	protected boolean isCompatibleModel(TLObject model) {
		if (model.tType().getModelKind() != ModelKind.CLASS) {
			return false;
		}
		return model.tContainer() != null;
	}

	@Override
	protected void initName(TLObjectTreeName name, TLObject model) {
		Pair<TLObject, List<TLObjectTreeStep>> rootAndPath = getRootAndCreatePath(model);
		List<TLObjectTreeStep> path = rootAndPath.getSecond();
		name.setPath(path);
		name.setRoot(ModelResolver.buildModelName(rootAndPath.getFirst()));
	}

	private Pair<TLObject, List<TLObjectTreeStep>> getRootAndCreatePath(TLObject node) {
		List<TLObjectTreeStep> path = list();
		TLObject child = node;
		TLObject parent = child.tContainer();
		while (parent != null) {
			path.add(createStep(child));
			child = parent;
			parent = parent.tContainer();
		}
		reverse(path);
		TLObject root = child;
		return new Pair<>(root, path);
	}

	private TLObjectTreeStep createStep(TLObject child) {
		String attribute = child.tContainerReference().getName();
		String label = getLabel(child);
		return TLObjectTreeStep.create(attribute, label);
	}

	@Override
	public TLObject locateModel(ActionContext context, TLObjectTreeName name) {
		TLObject root = (TLObject) context.resolve(name.getRoot());
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
		LabelProvider labelProvider = getLabelProviders().get(type);
		if (labelProvider != null) {
			return labelProvider;
		}
		LabelProvider superTypeLabelProvider = searchLabelProviderInSuperTypes(type);
		getLabelProviders().put(type, superTypeLabelProvider);
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

}

/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.tree.component.AbstractTreeModelBuilder;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * Build a tree structure out of wrappers which are no structured elements.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperStructureModelBuilder<C extends WrapperStructureModelBuilder.Config>
		extends AbstractTreeModelBuilder<Wrapper> implements ConfiguredInstance<C> {

	/**
	 * Configuration for structural information in the provided model.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends PolymorphicConfiguration<WrapperStructureModelBuilder<?>> {

		/** Mapping for children of a currently selected node. */
		@Key(ChildConfig.NAME_ATTRIBUTE)
		Map<String, ChildConfig> getChildAttributes();

		/** Return the model builder to be used for getting the root objects of the tree table. */
		@Mandatory
		@ImplementationClassDefault(WrapperListModelBuilder.class)
		PolymorphicConfiguration<? extends ListModelBuilder> getRootObjects();

		/**
		 * Setter for {@link #getRootObjects()}.
		 */
		void setRootObjects(PolymorphicConfiguration<? extends ListModelBuilder> builder);
	}

	/**
	 * Configuration for a child node in the tree.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface ChildConfig extends NamedConfigMandatory {
		// Empty here...
	}

	private static final Property<StructPreloader> PRELOADER =
		TypedAnnotatable.property(StructPreloader.class, "preloader");

	private final C _config;

	private final ListModelBuilder _builder;

	/**
	 * Map that maps a {@link TLStructuredType} to the configured {@link TLStructuredTypePart} that
	 * belong to the type.
	 */
	private final Map<TLStructuredType, List<TLStructuredTypePart>> _partsPerType = new HashMap<>();

	/**
	 * Map that maps a {@link TLStructuredType} to the configured {@link TLStructuredTypePart} that
	 * have this type as target type.
	 */
	private final Map<TLStructuredType, List<TLStructuredTypePart>> _partsPerTargetType = new HashMap<>();

	private final Wrapper _root;

	/**
	 * Creates a new {@link WrapperStructureModelBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WrapperStructureModelBuilder}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public WrapperStructureModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		_config = config;
		_builder = context.getInstance(config.getRootObjects());
		_root = (Wrapper) new WrapperStructureModelProvider().getBusinessModel(null);
		indexParts(context);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == _root || aModel == null;
	}

	@Override
	public boolean supportsNode(LayoutComponent contextComponent, Object aNode) {
		if (!(aNode instanceof Wrapper)) {
			return false;
		} else if (aNode == _root) {
			return true;
		} else if (_builder.supportsListElement(contextComponent, aNode)) {
			return true;
		} else {
			return hasRelevantParts((Wrapper) aNode, _partsPerTargetType);
		}
	}

	@Override
	public Iterator<? extends Wrapper> getChildIterator(Wrapper aWrapper) {
		return getChildren(aWrapper).iterator();
	}

	@Override
	public Collection<? extends Wrapper> getParents(LayoutComponent contextComponent, Wrapper node) {
		Set<Wrapper> result = Collections.emptySet();

		for (TLStructuredTypePart part : getParentParts(node)) {
			@SuppressWarnings("unchecked")
			Set<Wrapper> theParents = (Set<Wrapper>) AttributeOperations.getReferers(node, (TLStructuredTypePart) part);

			result = CollectionUtil.union2(result, theParents);
		}

		if (result.isEmpty()) {
			return Collections.singleton(_root);
		} else {
			return result;
		}
	}

	@Override
	public Wrapper retrieveModelFromNode(LayoutComponent contextComponent, Wrapper node) {
		return supportsNode(contextComponent, node) ? _root : null;
	}

	/**
	 * Gets the wrappers to be displayed as children of the given wrapper.
	 *
	 * @param node
	 *        The node of which the children are requested.
	 * @return The child wrappers of the given node.
	 */
	protected Collection<? extends Wrapper> getChildren(Wrapper node) {
		if (node == _root) {
			StructPreloader preloader = getPreloader();
			List<? extends Wrapper> result = preloader.getValues();

			if (result == null) {
				result = preloader.setValues(getModelForRootObject(node));
			}

			return result;
		} else {
			Collection<TLStructuredTypePart> configuredParts = getTLClassParts(node);
			if (configuredParts.isEmpty()) {
				return Collections.emptyList();
			}
			StructPreloader preloader = getPreloader();
			try (PreloadContext context = new PreloadContext()) {
				Collection<Wrapper> wrappers = new HashSet<>();

				for (TLStructuredTypePart part : configuredParts) {
					preloader.preload(context, part);
					addValues(wrappers, node.tValue(part));
				}

				return wrappers;
			}
		}
	}

	/**
	 * Returns the configuration of this instance.
	 */
	@Override
	public C getConfig() {
		return _config;
	}

	/**
	 * Return the model for the root object.
	 * 
	 * @param aWrapper
	 *        The object working as root in the structure.
	 * @return The requested list of wrappers to be displayed in first level of the tree table.
	 */
	@SuppressWarnings("unchecked")
	protected List<? extends Wrapper> getModelForRootObject(Wrapper aWrapper) {
		return (List<? extends Wrapper>) _builder.getModel(null, null);
	}

	/**
	 * The parts which may have the given wrapper as value (the type of the part matches the part of
	 * the wrapper).
	 */
	private Collection<TLStructuredTypePart> getParentParts(TLObject wrapper) {
		return getRelevantParts(wrapper, _partsPerTargetType);
	}

	private Collection<TLStructuredTypePart> getTLClassParts(TLObject wrapper) {
		return getRelevantParts(wrapper, _partsPerType);
	}

	private void addValues(Collection<Wrapper> result, Object value) {
		if (value instanceof Collection<?>) {
			result.addAll(CollectionUtil.dynamicCastView(Wrapper.class, (Collection<?>) value));
		} else if (value instanceof Wrapper) {
			result.add((Wrapper) value);
		}
	}

	private void indexParts(Log log) {
		for (ChildConfig config : _config.getChildAttributes().values()) {
			TLStructuredTypePart part = TLModelUtil.findPart(config.getName());
			TLType targetType = part.getType();
			if (!(targetType instanceof TLStructuredType)) {
				log.error("Configuration '" + config.getName() + "' is not a reference but " + targetType.toString());
				continue;
			}
			MapUtil.addObject(_partsPerType, part.getOwner(), part);
			MapUtil.addObject(_partsPerTargetType, (TLStructuredType) targetType, part);
		}
	}

	/**
	 * Returns the parts from the given {@link Map} that are mapped by the type (or a super type) of
	 * the given wrapper.
	 * 
	 * @return Must not be modified.
	 */
	private Collection<TLStructuredTypePart> getRelevantParts(TLObject wrapper,
			Map<TLStructuredType, List<TLStructuredTypePart>> parts) {
		List<TLStructuredTypePart> result = null;
		boolean needsCopy = false;
		for (TLClass theType : TLModelUtil.getReflexiveTransitiveGeneralizations((TLClass) wrapper.tType())) {
			List<TLStructuredTypePart> partsForType = parts.get(theType);
			if (partsForType == null) {
				// No parts configured for type
				continue;
			}
			if (result == null) {
				result = partsForType;
				needsCopy = true;
			} else {
				if (needsCopy) {
					// Copy original attributes
					ArrayList<TLStructuredTypePart> newAttributes =
						new ArrayList<>(result.size() + partsForType.size());
					newAttributes.addAll(result);
					result = newAttributes;
					needsCopy = false;
				}
				result.addAll(partsForType);
			}
		}

		return CollectionUtil.nonNull(result);
	}

	/**
	 * Whether there is a {@link TLStructuredTypePart} in the map for the type (or a super type) of
	 * the given wrapper.
	 */
	private boolean hasRelevantParts(TLObject wrapper, Map<TLStructuredType, List<TLStructuredTypePart>> parts) {
		for (TLClass theType : TLModelUtil.getReflexiveTransitiveGeneralizations((TLClass) wrapper.tType())) {
			List<TLStructuredTypePart> partsForType = parts.get(theType);
			if (partsForType!= null) {
				return true;
			}
		}
		
		return false;
	}

	private StructPreloader getPreloader() {
		return getPreloader(DefaultDisplayContext.getDisplayContext());
	}

	private StructPreloader getPreloader(DisplayContext context) {
		StructPreloader preloader = context.get(PRELOADER);

		if (preloader == null) {
			preloader = new StructPreloader();
			context.set(PRELOADER, preloader);
		}

		return preloader;
	}

	private static class StructPreloader {

		private List<TLStructuredTypePart> _resolved;

		private List<? extends Wrapper> _values;

		StructPreloader() {
			_resolved = new ArrayList<>();
		}

		@Override
		public String toString() {
			return new NameBuilder(this)
				.add("resolved", _resolved)
				.add("values", (_values != null) ? _values.size() : null)
				.build();
		}

		/**
		 * Perform a preload when the given attribute value hasn't been loaded before.
		 * 
		 * @param context
		 *        The preload context to be used.
		 * @param part
		 *        The requested attribute.
		 */
		public void preload(PreloadContext context, TLStructuredTypePart part) {
			if ((_values != null) && !_resolved.contains(part)) {

				MetaElementUtil.preloadAttributes(context, getValues(), (TLStructuredTypePart) part);

				_resolved.add(part);
			}
		}

		/**
		 * The values handled by this instance.
		 */
		public List<? extends Wrapper> getValues() {
			return _values;
		}

		/**
		 * Set the given values to be used by this instance.
		 * 
		 * @param someValues
		 *        The values to be used.
		 * @return The given values.
		 */
		public List<? extends Wrapper> setValues(List<? extends Wrapper> someValues) {
			_values = someValues;
			_resolved.clear();

			return _values;
		}

	}

	/**
	 * Model provider for a generic root object.
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public static class WrapperStructureModelProvider implements ModelProvider {

		@Override
		public Object getBusinessModel(LayoutComponent businessComponent) {
			return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
		}
	}
}

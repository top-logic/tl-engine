/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.StructureView;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.structured.wrap.SubtreeLoaderBase;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLType;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ReferenceListModelBuilder} that retrieves all values accessible through a configured
 * {@link Config#getReference() reference} from all elements in a substructure.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructureReferenceListModelBuilder<C extends StructureReferenceListModelBuilder.Config<?>>
		extends ReferenceListModelBuilder<C> {

	/**
	 * Configuration options for {@link StructureReferenceListModelBuilder}.
	 */
	public interface Config<I extends StructureReferenceListModelBuilder<?>>
			extends ReferenceListModelBuilder.Config<I> {

		/**
		 * @see #getStructure()
		 */
		String STRUCTURE = "structure";

		/**
		 * @see #getAdditionalTypes()
		 */
		String ADDITIONAL_TYPES = "additionalTypes";

		/**
		 * Definition of a model structure that serves as source for accessing
		 * {@link #getReference()}.
		 */
		@Name(STRUCTURE)
		PolymorphicConfiguration<StructureView<TLObject>> getStructure();

		/**
		 * (Optional) additional types (in addition to the type defining the {@link #getReference()
		 * configured reference}) for which values should be retrieved descending the
		 * {@link #getStructure() structure}.
		 * 
		 * <p>
		 * If no additional types are configured, values are only displayed, if the component's
		 * model is an object defining {@link #getReference()}.
		 * </p>
		 */
		@Name(ADDITIONAL_TYPES)
		@Format(CommaSeparatedStrings.class)
		List<String> getAdditionalTypes();

	}

	private final StructureView<TLObject> _structure;

	private List<TLType> _additionalTypes;

	/**
	 * Creates a {@link StructureReferenceListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StructureReferenceListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
		_structure = context.getInstance(config.getStructure());

		_additionalTypes = new ArrayList<>();
		for (String additionalTypeName : config.getAdditionalTypes()) {
			_additionalTypes.add(TLModelUtil.findType(additionalTypeName));
		}
	}

	@Override
	protected boolean hasSupportedType(TLObject obj) {
		return super.hasSupportedType(obj) || hasAdditionalType(obj);
	}

	private boolean hasAdditionalType(TLObject obj) {
		for (TLType type : _additionalTypes) {
			if (TLModelUtil.isCompatibleInstance(type, obj)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected boolean supportsOwner(LayoutComponent component, TLObject owner) {
		TLObject anchestorOrSelf = owner;
		while (true) {
			if (super.supportsOwner(component, anchestorOrSelf)) {
				return true;
			}

			anchestorOrSelf = parent(anchestorOrSelf);
			if (anchestorOrSelf == null) {
				return false;
			}
		}
	}

	@Override
	protected boolean shouldKeepModel(LayoutComponent aComponent, TLObject owner) {
		boolean result = super.shouldKeepModel(aComponent, owner);
		if (result) {
			return true;
		}

		TLObject parent = parent(owner);
		if (parent == null) {
			return false;
		}

		return shouldKeepModel(aComponent, parent);
	}

	@Override
	protected void addValues(List<TLObject> result, TLObject model) {
		PreloadContext preloadContext = new PreloadContext();
		try (SubtreeLoaderBase<TLObject> loader = new SubtreeLoaderBase<>(_structure, preloadContext)) {
			preloadValues(preloadContext, loader, model);
			addValuesRecursive(result, model);
		}
	}

	/**
	 * Return the wrappers assigned to the given element in the named attribute.
	 * 
	 * @param result
	 *        List to append the wrappers to.
	 * @param parent
	 *        The element to get the wrappers from.
	 */
	protected void addValuesRecursive(Collection<TLObject> result, TLObject parent) {
		addDirectValues(result, parent);
		for (Iterator<? extends TLObject> it = childrenIt(parent); it.hasNext();) {
			addValuesRecursive(result, it.next());
		}
	}

	@Override
	protected void addDirectValues(Collection<TLObject> result, TLObject parent) {
		if (definesReference(parent)) {
			super.addDirectValues(result, parent);
		}
	}

	/**
	 * Preload the tree structure an attribute when this model builder works on a recursive
	 * structure.
	 * 
	 * <p>
	 * The given context and loader will be closed by calling method.
	 * </p>
	 * 
	 * @param preloadContext
	 *        The preload context we live in.
	 * @param loader
	 *        The loader for structured elements.
	 * @param element
	 *        The element to start the preload at.
	 */
	protected void preloadValues(PreloadContext preloadContext, SubtreeLoaderBase<TLObject> loader, TLObject element) {
		loader.loadTree(element);

		List<? extends TLObject> loadedNodes = loader.getLoadedNodes();
		if (loadedNodes.size() > 1) {
			TLStructuredType currentType = element.tType();
			TLStructuredType attributeType = getReference().getOwner();
			if (TLModelUtil.isGeneralization(attributeType, currentType)) {
				MetaElementUtil.preloadAttributes(preloadContext, loadedNodes, getReference());
			}
		}
	}

	private TLObject parent(TLObject potentialModel) {
		return _structure.getParent(potentialModel);
	}

	private Iterator<? extends TLObject> childrenIt(TLObject parent) {
		return _structure.getChildIterator(parent);
	}

}

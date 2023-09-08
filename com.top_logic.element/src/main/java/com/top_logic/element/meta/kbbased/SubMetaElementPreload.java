/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.v5.AssociationCachePreload;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * {@link AssociationNavigationPreload} that loads the sub meta elements of a given collection of
 * meta elements including the attributes.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SubMetaElementPreload extends AssociationNavigationPreload {

	private static final Property<Integer> REMAINING_DEPTH_PROPERTY = TypedAnnotatable.property(Integer.class, "depth");

	/** Describes how many levels of the {@link TLClass} hierarchy must be loaded. */
	private final int _depth;

	/** {@link PreloadOperation} operation loading the attributes of the loaded {@link TLClass}s. */
	private final AssociationCachePreload _attributePreload;

	/**
	 * Creates a {@link SubMetaElementPreload}.
	 * 
	 * @param depth
	 *        Depth of the subtree to load. Must be &gt;=1.
	 */
	public SubMetaElementPreload(int depth) {
		super(KBBasedMetaElement.SUB_MES_ATTR);
		if (depth < 1) {
			throw new IllegalArgumentException("Must not preload subtree with empty depth.");
		}
		_depth = depth;
		_attributePreload = new AssociationCachePreload(PersistentClass.ATTRIBUTES_ATTR);
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		SuperMetaElementPreload.INSTANCE.prepare(context, baseObjects);
		InteractionContext interaction = ThreadContextManager.getInteraction();
		interaction.set(REMAINING_DEPTH_PROPERTY, Integer.valueOf(_depth - 1));
		try {
			internalPrepare(context, baseObjects);
		} finally {
			interaction.reset(REMAINING_DEPTH_PROPERTY);
		}
		loadAttributes(context, baseObjects);
	}

	private void loadAttributes(PreloadContext context, Collection<?> baseObjects) {
		Set<Object> allTypes = new HashSet<>(baseObjects);
		addSubTypesOfTypes(allTypes, baseObjects);

		_attributePreload.prepare(context, allTypes);
	}

	private void addSubTypesOfTypes(Set<Object> allTypes, Collection<?> foundTypes) {
		for (Object me : foundTypes) {
			addSubTypes(allTypes, (TLClass) me, _depth);
		}
	}

	private void addSubTypes(Set<Object> allTypes, TLClass type, int remainingDepth) {
		if (remainingDepth == 0) {
			return;
		}
		for (TLClass subType : type.getSpecializations()) {
			boolean newlyAdded = allTypes.add(subType);
			if (!newlyAdded) {
				continue;
			}
			addSubTypes(allTypes, subType, remainingDepth - 1);
		}
	}

	@Override
	protected void processDestinations(PreloadContext context, List<Wrapper> destinations) {
		super.processDestinations(context, destinations);
		if (!destinations.isEmpty()) {
			InteractionContext interaction = ThreadContextManager.getInteraction();
			Integer remainingDepth = interaction.get(REMAINING_DEPTH_PROPERTY);
			if (remainingDepth.intValue() > 0) {
				interaction.set(REMAINING_DEPTH_PROPERTY, Integer.valueOf(remainingDepth.intValue() - 1));
				/* Must not call super to ensure that all attributes of the MetaElements are loaded
				 * bulk. */
				internalPrepare(context, destinations);
			}
		}
	}

	private void internalPrepare(PreloadContext context, Collection<?> baseObjects) {
		super.prepare(context, baseObjects);
	}

}


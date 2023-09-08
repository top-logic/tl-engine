/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.element.meta.PersistentClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.v5.AssociationCachePreload;
import com.top_logic.model.v5.AssociationNavigationPreload;

/**
 * {@link AssociationNavigationPreload} that loads the super meta elements of a given collection of
 * {@link TLClass}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class SuperMetaElementPreload extends AssociationNavigationPreload {

	/** Singleton {@link SuperMetaElementPreload} instance. */
	public static final SuperMetaElementPreload INSTANCE = new SuperMetaElementPreload();

	private final PreloadOperation _attributePreload;

	private SuperMetaElementPreload() {
		super(KBBasedMetaElement.SUPER_ME_ATTR);
		_attributePreload = new AssociationCachePreload(PersistentClass.ATTRIBUTES_ATTR);
	}

	@Override
	protected void processDestinations(PreloadContext context, List<Wrapper> destinations) {
		super.processDestinations(context, destinations);

		// load super MetaElements
		if (!destinations.isEmpty()) {
			/* Do not call prepare(...) to avoid loading of attributes these are loaded bulk after
			 * loading complete meta element hierarchy. */
			internalPrepare(context, destinations);
		}
	}

	private void internalPrepare(PreloadContext context, Collection<?> metaElements) {
		super.prepare(context, metaElements);
	}

	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		internalPrepare(context, baseObjects);
		loadAttributes(context, baseObjects);
	}

	private void loadAttributes(PreloadContext context, Collection<?> baseObjects) {
		Collection<TLClass> baseClasses = CollectionUtil.dynamicCastView(TLClass.class, baseObjects);
		LinkedHashSet<TLClass> allTypes = TLModelUtil.getReflexiveTransitiveGeneralizations(baseClasses);
		_attributePreload.prepare(context, allTypes);
	}

}

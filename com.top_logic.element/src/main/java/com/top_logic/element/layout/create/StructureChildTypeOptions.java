/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.basic.LabelSorter;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;

/**
 * {@link CreateTypeOptions} in the context of a {@link StructuredElement}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StructureChildTypeOptions implements CreateTypeOptions {

	/**
	 * Singleton {@link StructureChildTypeOptions} instance.
	 */
	public static final StructureChildTypeOptions INSTANCE = new StructureChildTypeOptions();

	private StructureChildTypeOptions() {
		// Singleton constructor.
	}

	@Override
	public List<TLClass> getPossibleTypes(Object contextModel) {
		List<TLClass> types = list(childrenTypes(contextModel));
		LabelSorter.sortByLabelInline(types, MetaLabelProvider.INSTANCE);
		return types;
	}

	@Override
	public boolean supportsContext(Object contextModel) {
		return !childrenTypes(contextModel).isEmpty();
	}

	private Set<TLClass> childrenTypes(Object contextModel) {
		if (!(contextModel instanceof StructuredElement)) {
			return Collections.emptySet();
		}
		StructuredElement parent = (StructuredElement) contextModel;
		Set<TLClass> childrenTypes = parent.getChildrenTypes();
		return childrenTypes;
	}

}

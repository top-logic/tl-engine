/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import static com.top_logic.basic.CollectionUtil.map;
import static com.top_logic.basic.col.FilterUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Named;
import com.top_logic.basic.col.Filter;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.util.TLModelUtil;

/**
 * Provide a default search model with exclude attributes which have been marked as hidden in the
 * meta element.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public abstract class AbstractExtendedSearchModelBuilder implements ExtendedSearchModelBuilder {

	@Override
	public Set<String> getExcludedAttributesForColumns(TLClass searchedType) {
		return getExcludedAttributes(searchedType);
	}

	@Override
	public Set<String> getExcludedAttributesForReporting(TLClass searchedType) {
		return getExcludedAttributes(searchedType);
	}

	@Override
	public Set<String> getExcludedAttributesForSearch(TLClass searchedType) {
		return getExcludedAttributes(searchedType);
	}

	/**
	 * The names of the attributes that should be excluded from everything:
	 * {@link #getExcludedAttributesForColumns(TLClass)},
	 * {@link #getExcludedAttributesForReporting(TLClass)} and
	 * {@link #getExcludedAttributesForColumns(TLClass)}.
	 * <p>
	 * Excludes the {@link #getHiddenAttributes(TLClass) attributes marked as hidden}.
	 * </p>
	 * 
	 * @param searchedType
	 *        Is not allowed to be null.
	 * @return Never null. The {@link Set} is modifiable and resizable.
	 */
	protected Set<String> getExcludedAttributes(TLClass searchedType) {
		return getHiddenAttributes(searchedType);
	}

	/**
	 * Return all meta attribute names of the given meta element which are marked as hidden.
	 * 
	 * @param metaElement
	 *        The meta element to get the attributes from.
	 * @return Names of the hidden attributes. The {@link Set} is modifiable and resizable.
	 */
	protected Set<String> getHiddenAttributes(TLClass metaElement) {
		if (metaElement == null) {
			return new HashSet<>();
		}
		List<TLStructuredTypePart> attributes = TLModelUtil.getMetaAttributes(metaElement);
		Set<TLStructuredTypePart> filteredMetaAttributes = filterSet(HiddenMetaAttributesFilter.INSTANCE, attributes);
		return mapToNames(filteredMetaAttributes);
	}

	private Set<String> mapToNames(Set<TLStructuredTypePart> filteredMetaAttributes) {
		Set<String> filteredAttributeNames = set();
		map(filteredMetaAttributes.iterator(), filteredAttributeNames, Named.NameMapping.INSTANCE);
		return filteredAttributeNames;
	}

	private static class HiddenMetaAttributesFilter implements Filter<TLStructuredTypePart> {

		/** The instance of the {@link HiddenMetaAttributesFilter}. */
		public static final HiddenMetaAttributesFilter INSTANCE = new HiddenMetaAttributesFilter();

		@Override
		public boolean accept(TLStructuredTypePart attribute) {
			return DisplayAnnotations.isHidden(attribute);
		}

	}

}

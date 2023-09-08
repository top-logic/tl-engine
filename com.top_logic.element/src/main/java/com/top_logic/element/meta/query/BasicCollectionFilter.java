/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.query;

import java.util.Map;

import com.top_logic.knowledge.wrap.mapBasedPersistancy.BasicMapBasedPersistancyAware;
import com.top_logic.util.Utils;

/**
 * Implements the comparable interface based on a sort order.
 * 
 * @author <a href="mailto:fma@top-logic.com">Frank Mausz</a>
 */
public abstract class BasicCollectionFilter<C> extends BasicMapBasedPersistancyAware implements CollectionFilter<C> {

	public static final Integer SORT_ORDER_LEVEL_LOW = Integer.valueOf(10);

	public static final Integer SORT_ORDER_LEVEL_MEDIUM = Integer.valueOf(100);

	public static final Integer SORT_ORDER_LEVEL_HIGH = Integer.valueOf(200);

	/** Key for value map to store negate flag. */
	protected static final String KEY_NEGATE = "neg";

	/** Key for value map to store relevant flag. */
	protected static final String KEY_RELEVANT = "isRelevant";

	/** If <code>true</code> invert the filter result for each collection element. */
	private boolean negate;

	/** The relevant flag, i.e. if the filter is applied in a search or not. */
	private boolean relevant;

	/**
	 * Creates a {@link BasicCollectionFilter} (no negate, is relevant).
	 */
	public BasicCollectionFilter() {
		this(false, true);
	}

	/**
	 * Creates a {@link BasicCollectionFilter}.
	 * 
	 * @param aValueMap
	 *        the Map with all values. Must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         if the map is <code>null</code> or some of its values do not match the filter's
	 *         constraints.
	 */
	public BasicCollectionFilter(Map<String, Object> aValueMap) throws IllegalArgumentException {
		this.setUpFromValueMap(aValueMap);
	}

	/**
	 * Creates a {@link BasicCollectionFilter}.
	 * 
	 * @param doNegate
	 *        the negation flag
	 * @param isRelevant
	 *        the relevant flag
	 */
	public BasicCollectionFilter(boolean doNegate, boolean isRelevant) {
		super();

		this.setNegate(doNegate);
		this.setIsRelevant(isRelevant);
	}

	/**
	 * Get the sort order of this filter.
	 * 
	 * <p>
	 * Used to sort the filters for performance reasons.
	 * </p>
	 * 
	 * @return The sort order of this filter.
	 */
	public abstract Integer getSortOrder();

	@Override
	public int compareTo(Object anObject) {
		if (anObject instanceof CollectionFilter) {
			return this.getSortOrder().compareTo(((BasicCollectionFilter<?>) anObject).getSortOrder());
		} else {
			return 1;
		}
	}

	@Override
	public Map<String, Object> getValueMap() {
		Map<String, Object> theMap = super.getValueMap();

		theMap.put(KEY_NEGATE, Boolean.valueOf(this.getNegate()));
		theMap.put(KEY_RELEVANT, Boolean.valueOf(this.isRelevant()));

		return theMap;
	}

	@Override
	public void setUpFromValueMap(@SuppressWarnings("rawtypes") Map aValueMap) throws IllegalArgumentException {
		if (aValueMap == null) {
			throw new IllegalArgumentException("Value Map must not be null!");
		}

		// Negation
		this.setNegate(((Boolean) aValueMap.get(KEY_NEGATE)).booleanValue());

		// Relevant flag
		Object theValue = aValueMap.get(KEY_RELEVANT);

		if (theValue != null) {
			this.setIsRelevant(Utils.getBooleanValue(theValue));
		} else {
			this.setIsRelevant(true);
		}
	}

	/**
	 * Get the flag for negate a filter result.
	 * 
	 * <p>
	 * For some reasons it might be interesting to find object which are not matching the given
	 * pattern.
	 * </p>
	 * 
	 * @return The negation flag.
	 */
	public boolean getNegate() {
		return this.negate;
	}

	/**
	 * @see #getNegate()
	 */
	public void setNegate(boolean doNegate) {
		this.negate = doNegate;
	}

	/**
	 * Get the relevant flag which denotes if the filter should be applied in a search.
	 * 
	 * @return The relevant flag.
	 */
	public boolean isRelevant() {
		return this.relevant;
	}

	/**
	 * @see #isRelevant()
	 */
	public void setIsRelevant(boolean aBoolean) {
		this.relevant = aBoolean;
	}
}

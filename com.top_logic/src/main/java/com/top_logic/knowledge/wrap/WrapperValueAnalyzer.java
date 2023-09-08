/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.json.JSON.DefaultValueAnalyzer;
import com.top_logic.basic.json.JSON.ValueAnalyzer;
import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * {@link DefaultValueAnalyzer} that serialises a {@link TLObject} as {@link Map}.The created map
 * can be used to restore later using {@link WrapperValueFactory}.
 * 
 * @see WrapperValueFactory
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WrapperValueAnalyzer extends DefaultValueAnalyzer {

	/** Default instance of {@link WrapperValueAnalyzer}. */
	public static final ValueAnalyzer WRAPPER_INSTANCE = new WrapperValueAnalyzer();

	/** Marker key in wrapper map that identifies the map as wrapper map. */
	private static final String CLASS = "class";

	/** Key in wrapper map holding the KO type of the wrapper. */
	private static final String TYPE = "type";

	/** Key in wrapper map holding the internal identifier of the wrapper. */
	private static final String NAME = "name";

	/** Key in wrapper map holding the revision number of the wrapper. */
	private static final String REVISION = "revision";

	/** Key in wrapper map holding the branch number of the wrapper. */
	private static final String BRANCH = "branch";

	@Override
	public int getType(Object value) {
		if (value instanceof TLObject) {
			return MAP_TYPE;
		}
		return super.getType(value);
	}

	@Override
	public Iterator<? extends Map.Entry<?, ?>> getMapIterator(Object map) {
		if (map instanceof TLObject) {
			Map<String, Object> wrapperMap = wrapperAsMap((TLObject) map);
			return wrapperMap.entrySet().iterator();
		}
		return super.getMapIterator(map);
	}

	/**
	 * Creates a {@link Map} representing the given wrapper.
	 * 
	 * @param wrapper
	 *        The {@link TLObject} to to get map representation for.
	 * 
	 * @see #mapToWrapper(Map)
	 */
	public static Map<String, Object> wrapperAsMap(TLObject wrapper) {
		Map<String, Object> result = MapUtil.newMap(5);
		// Marker to mark map as "wrapper map"
		result.put(CLASS, markerValue());
		result.put(TYPE, wrapper.tTable().getName());
		result.put(NAME, IdentifierUtil.toExternalForm(KBUtils.getWrappedObjectName(wrapper)));
		long branchId = WrapperHistoryUtils.getBranch(wrapper).getBranchId();
		if (branchId != TLContext.TRUNK_ID) {
			result.put(BRANCH, Long.valueOf(branchId));
		}
		Revision revision = WrapperHistoryUtils.getRevision(wrapper);
		if (!revision.isCurrent()) {
			result.put(REVISION, revision.getCommitNumber());
		}
		return result;
	}

	private static String markerValue() {
		return TLObject.class.getSimpleName();
	}

	/**
	 * Whether the given map can be transformed to a {@link TLObject}.
	 * 
	 * @param map
	 *        The map in question.
	 * 
	 * @return Whether {@link #mapToWrapper(Map)} can be called.
	 */
	public static boolean isWrapperMap(Map<?, ?> map) {
		return StringServices.equals(markerValue(), map.get(CLASS));
	}

	/**
	 * Transforms the given map to a {@link TLObject}.
	 * 
	 * @param map
	 *        The map representation of a wrapper.
	 * 
	 * @see #wrapperAsMap(TLObject)
	 */
	public static TLObject mapToWrapper(Map<?, ?> map) {
		if (!isWrapperMap(map)) {
			throw new IllegalArgumentException("Given map '" + map + "' is not a map that can be transformed by "
				+ WrapperValueAnalyzer.class);
		}
		String type = String.valueOf(map.get(TYPE));
		TLID name = IdentifierUtil.fromExternalForm(String.valueOf(map.get(NAME)));

		Number branchID = (Number) map.get(BRANCH);
		Branch branch;
		if (branchID != null) {
			branch = HistoryUtils.getBranch(branchID.longValue());
		} else {
			branch = HistoryUtils.getTrunk();
		}

		Number revNumber = (Number) map.get(REVISION);
		Revision revision;
		if (revNumber != null) {
			revision = HistoryUtils.getRevision(revNumber.longValue());
		} else {
			revision = Revision.CURRENT;
		}
		return WrapperFactory.getWrapper(branch, revision, name, type);
	}

}

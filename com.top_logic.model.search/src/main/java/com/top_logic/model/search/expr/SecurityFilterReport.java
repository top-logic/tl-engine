/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Record of the business objects that the security filter removed from a value.
 *
 * <p>
 * The result of a {@link QueryExecutor} execution is filtered for the read rights of the current
 * user, see {@link SearchExpression#filterSecurity(Object, SecurityFilterReport)}. The filter drops
 * what the user must not read without saying so, therefore a caller that wants to show how much was
 * removed attaches a {@link SecurityFilterReport} to the {@link EvalContext} of the execution, see
 * {@link EvalContext#setSecurityReport(SecurityFilterReport)}.
 * </p>
 *
 * <p>
 * Collecting the removals is opt-in: without a report, the filter records nothing. A report is bound
 * to the thread that runs the execution and is not synchronized.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityFilterReport {

	private final Map<TLStructuredType, Integer> _droppedByType = new LinkedHashMap<>();

	private int _droppedCount;

	/**
	 * Records that the given object was removed from the filtered value.
	 *
	 * @param object
	 *        The object that the current user must not read.
	 */
	public void dropped(TLObject object) {
		_droppedCount++;
		_droppedByType.merge(object.tType(), Integer.valueOf(1), (a, b) -> Integer.valueOf(a.intValue() + b.intValue()));
	}

	/**
	 * The number of objects that were {@link #dropped(TLObject) removed}.
	 */
	public int droppedCount() {
		return _droppedCount;
	}

	/**
	 * The number of {@link #dropped(TLObject) removed} objects per type.
	 *
	 * <p>
	 * The types are ordered by the first removal of an object of that type. The map is a view of
	 * this report and reflects later removals.
	 * </p>
	 *
	 * @return An unmodifiable map keyed by the {@link TLObject#tType() type} of the removed objects.
	 */
	public Map<TLStructuredType, Integer> droppedByType() {
		return Collections.unmodifiableMap(_droppedByType);
	}

	/**
	 * Whether nothing was {@link #dropped(TLObject) removed}.
	 */
	public boolean isEmpty() {
		return _droppedCount == 0;
	}

	/**
	 * Forgets all recorded removals, so that this report can be used for a further execution.
	 */
	public void reset() {
		_droppedCount = 0;
		_droppedByType.clear();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[dropped=" + _droppedCount + ", byType=" + _droppedByType + "]";
	}

}

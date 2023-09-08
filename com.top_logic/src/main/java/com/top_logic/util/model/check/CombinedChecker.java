/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model.check;

import java.util.List;

import com.top_logic.basic.col.Sink;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.TLObject;

/**
 * {@link InstanceCheck} delegating to multiple other {@link InstanceCheck}s.
 * 
 * @see #combine(List)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class CombinedChecker implements InstanceCheck {

	private final List<InstanceCheck> _checks;

	/**
	 * Creates a {@link CombinedChecker}.
	 *
	 * @param checks
	 *        All {@link InstanceCheck}s to perform.
	 * 
	 * @see #combine(List)
	 */
	private CombinedChecker(List<InstanceCheck> checks) {
		_checks = checks;
	}

	@Override
	public void check(Sink<ResKey> problems, TLObject object) {
		for (InstanceCheck check : _checks) {
			check.check(problems, object);
		}
	}

	/**
	 * Creates an {@link InstanceCheck} validating all given {@link InstanceCheck}s on a given
	 * object.
	 */
	public static InstanceCheck combine(List<InstanceCheck> checks) {
		switch (checks.size()) {
			case 0:
				return NoChecks.INSTANCE;
			case 1:
				return checks.get(0);
			default:
				return new CombinedChecker(checks);
		}
	}
}
/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint;

import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;

/**
 * Default {@link ConstraintSpec} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultConstraintSpec implements ConstraintSpec {

	private final ConstraintAlgorithm _algorithm;

	private final Ref[] _related;

	private final boolean _asWarning;

	/**
	 * Creates a {@link DefaultConstraintSpec}.
	 * 
	 * @param algorithm
	 *        See {@link #getAlgorithm()}.
	 * @param related
	 *        See {@link #getRelated()}.
	 * @param asWarning
	 *        See {@link #asWarning()}.
	 */
	public DefaultConstraintSpec(ConstraintAlgorithm algorithm, Ref[] related, boolean asWarning) {
		_algorithm = algorithm;
		_related = related;
		_asWarning = asWarning;
	}

	@Override
	public ConstraintAlgorithm getAlgorithm() {
		return _algorithm;
	}

	@Override
	public Ref[] getRelated() {
		return _related;
	}

	@Override
	public boolean asWarning() {
		return _asWarning;
	}

}

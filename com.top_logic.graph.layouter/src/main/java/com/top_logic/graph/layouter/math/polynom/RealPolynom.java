/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.math.polynom;

import java.util.List;

/**
 * Representation of a mathmatical polynom using coefficients.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class RealPolynom {
	private List<Double> _coefficients;

	/**
	 * Creates a {@link RealPolynom} for the given coefficients.
	 */
	public RealPolynom(List<Double> coefficients) {
		setCoefficients(coefficients);
	}

	/**
	 * Degree of this polynom.
	 */
	public int degree() {
		return getCoefficients().size();
	}

	/**
	 * Coefficients of this polynom.
	 */
	public List<Double> getCoefficients() {
		return _coefficients;
	}

	/**
	 * @see #getCoefficients()
	 */
	public void setCoefficients(List<Double> coefficients) {
		_coefficients = coefficients;
	}

	/**
	 * @see #degree()
	 */
	public int getDegree() {
		return _coefficients.size() - 1;
	}

}

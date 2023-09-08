/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.math.polynom;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.Logger;
import com.top_logic.util.Resources;

/**
 * Solver for quadratic equations.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class QuadraticEquationSolver implements Consumer<RealPolynom> {

	private static final int QUADRATIC_DEGREE = 2;

	private boolean _hasSolutions;

	private Set<Double> _solutions = new LinkedHashSet<>();

	@Override
	public void accept(RealPolynom polynom) {
		if (polynom.getDegree() > QUADRATIC_DEGREE) {
			Logger.error(Resources.getInstance().getString(I18NConstants.POLYNOM_IS_NOT_QUADRATIC), this);
		}

		List<Double> coefficients = polynom.getCoefficients();

		double a = coefficients.get(0);
		double b = coefficients.get(1);
		double c = coefficients.get(2);

		double descriminant = getDescriminant(a, b, c);

		setSolutions(a, b, descriminant);
	}

	private void setSolutions(double a, double b, double descriminant) {
		if (descriminant < 0) {
			_hasSolutions = false;
		} else {
			_hasSolutions = true;

			_solutions.add((-b + Math.sqrt(descriminant)) / (2 * a));
			_solutions.add((-b - Math.sqrt(descriminant)) / (2 * a));
		}
	}

	private double getDescriminant(double a, double b, double c) {
		return Math.pow(b, 2) - 4 * a * c;
	}

	/**
	 * True if this equation has solutions, otherwise false.
	 */
	public boolean hasSolutions() {
		return _hasSolutions;
	}

	/**
	 * Solutions of this quadratic equation.
	 */
	public Set<Double> getSolutions() {
		return _solutions;
	}

}

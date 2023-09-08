/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.util;

import java.util.Collection;
import java.util.LinkedList;

import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1DContainer;

/**
 * Prioritize {@link Line1DContainer} to prevent vertical overlappings.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultLineContainerPrioritizer implements LineContainerPrioritizerAlgorithm {

	private LayoutDirection _direction;

	/**
	 * Creates a {@link DefaultLineContainerPrioritizer}.
	 */
	public DefaultLineContainerPrioritizer(LayoutDirection direction) {
		_direction = direction;
	}

	@Override
	public void prioritize(Collection<Line1DContainer> containers) {
		LinkedList<Line1DContainer> linkedList = new LinkedList<>(containers);

		for (int i = 0; i < linkedList.size(); i++) {
			for (int j = i + 1; j < linkedList.size(); j++) {
				Line1DContainer container1 = linkedList.get(i);
				Line1DContainer container2 = linkedList.get(j);

				if (existVerticalOverlapping(container1, container2)) {
					setPriority(container1, container2);
				}
			}
		}
	}

	private void setPriority(Line1DContainer container1, Line1DContainer container2) {
		int priority1 = container1.getPriority();
		int priority2 = container2.getPriority();

		if (priority2 <= priority1) {
			container2.setPriority(priority1 + 1);
		}
	}

	private boolean existVerticalOverlapping(Line1DContainer container1, Line1DContainer container2) {
		for (Line1D line1 : container1.getLines()) {
			for (Line1D line2 : container2.getLines()) {
				if (existVerticalOverlapping(line1, line2)) {
					return true;
				}
			}
		}

		return false;
	}

	private boolean existVerticalOverlapping(Line1D line1, Line1D line2) {
		if (_direction == LayoutDirection.VERTICAL_FROM_SOURCE) {
			if (line1.getEnd() == line2.getStart()) {
				return true;
			}
		} else {
			if (line1.getStart() == line2.getEnd()) {
				return true;
			}
		}

		return false;
	}
}

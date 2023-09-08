/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines.util;

import java.util.Comparator;

import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D;
import com.top_logic.graph.layouter.algorithm.rendering.lines.Line1D.Orientation;

/**
 * Comparator for one dimensional lines.
 * 
 * <p>
 * It compares lines by their orientation and end. Left orientated lines are smaller than right
 * orientated. Two left orientated lines are compared as usual, but two right orientated lines are
 * compared in the other "inverse" way. In explicit:
 * </p>
 * 
 * <xmp> a < b :<=> </xmp>
 * 
 * <ol>
 * <li>Both left orientated: a.end < b.end</li>
 * <li>Both right orientated: a.end > b.end</li>
 * <li>a is left (right) and b is right (left) orientated: a < (>) b</li>
 * </ol>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class LineEndTopBottomComparator implements Comparator<Line1D> {

	@Override
	public int compare(Line1D line1, Line1D line2) {
		Orientation orientation1 = line1.getOrientation();
		Orientation orientation2 = line2.getOrientation();

		if (orientation1 == Orientation.LEFT) {
			if (orientation2 == Orientation.RIGHT) {
				return -1;
			} else {
				return Double.compare(line1.getEnd(), line2.getEnd());
			}
		} else {
			if (orientation2 == Orientation.LEFT) {
				return 1;
			} else {
				return -1 * Double.compare(line1.getEnd(), line2.getEnd());
			}
		}
	}

}
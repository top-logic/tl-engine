/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines;

import java.util.Collection;
import java.util.LinkedHashSet;

/**
 * Container for a collection of one dimensional lines.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Line1DContainer {

	private static final int DEFAULT_PRIORITY = 1;

	private Collection<Line1D> _lines;

	private int _priority;

	/**
	 * Creates a container for {@link Line1D}.
	 */
	public Line1DContainer(Collection<Line1D> lines) {
		_lines = new LinkedHashSet<>(lines);

		_priority = DEFAULT_PRIORITY;
	}

	/**
	 * All containing lines.
	 */
	public Collection<Line1D> getLines() {
		return new LinkedHashSet<>(_lines);
	}

	/**
	 * {@link #getLines()}
	 */
	public void setLines(Collection<Line1D> lines) {
		_lines = new LinkedHashSet<>(lines);
	}

	/**
	 * Check intersection with an other one dimensional line container.
	 */
	public IntersectionStatus checkIntersection(Line1DContainer container) {
		IntersectionStatus currentStatus = IntersectionStatus.NONE;

		for (Line1D line : _lines) {
			IntersectionStatus insertStatus = line.checkIntersection(container.getLines());

			if (insertStatus.equals(IntersectionStatus.PARTIAL)) {
				return IntersectionStatus.PARTIAL;
			} else if (insertStatus.equals(IntersectionStatus.FULL)) {
				currentStatus = IntersectionStatus.FULL;
			}
		}

		return currentStatus;
	}

	/**
	 * Priority.
	 */
	public int getPriority() {
		return _priority;
	}

	/**
	 * @see #getPriority()
	 */
	public void setPriority(int priority) {
		_priority = priority;
	}
}

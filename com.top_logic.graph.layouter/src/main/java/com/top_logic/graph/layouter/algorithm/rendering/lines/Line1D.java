/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.rendering.lines;

import java.util.Collection;

/**
 * General structure of an one dimensional line.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class Line1D {

	private double _start;

	private double _end;

	private Orientation _orientation;

	private Object _businessObject;

	/**
	 * Orientation of this line.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public enum Orientation {
		/**
		 * From right to left. Start is greater than the end.
		 */
		LEFT,

		/**
		 * From left to right. End is greater than the start.
		 */
		RIGHT
	}

	/**
	 * Creates an one dimensional line for the given coordinates.
	 */
	public Line1D(double start, double end) {
		_start = start;
		_end = end;

		_orientation = start < end ? Orientation.RIGHT : Orientation.LEFT;
	}

	/**
	 * @see #Line1D(double, double)
	 */
	public Line1D(double start, double end, Object businessObject) {
		this(start, end);
		
		_businessObject = businessObject;
	}

	/**
	 * End point of this line.
	 */
	public double getEnd() {
		return _end;
	}

	/**
	 * @see #getEnd()
	 */
	public void setEnd(double end) {
		_end = end;
	}

	/**
	 * Start point of this line.
	 */
	public double getStart() {
		return _start;
	}

	/**
	 * @see #getStart()
	 */
	public void setStart(double start) {
		_start = start;
	}

	/**
	 * @see Orientation
	 */
	public Orientation getOrientation() {
		return _orientation;
	}

	/**
	 * @see #getOrientation()
	 */
	public void setOrientation(Orientation orientation) {
		_orientation = orientation;
	}

	/**
	 * Business object of this line.
	 */
	public Object getBusinessObject() {
		return _businessObject;
	}

	/**
	 * @see #getBusinessObject()
	 */
	public void setBusinessObject(Object businessObject) {
		_businessObject = businessObject;
	}

	/**
	 * @see #checkIntersection(Line1D)
	 */
	public IntersectionStatus checkIntersection(Collection<Line1D> lines) {
		IntersectionStatus currentStatus = IntersectionStatus.NONE;

		for (Line1D line : lines) {
			IntersectionStatus insertStatus = checkIntersection(line);

			if (insertStatus.equals(IntersectionStatus.PARTIAL)) {
				return IntersectionStatus.PARTIAL;
			} else if (insertStatus.equals(IntersectionStatus.FULL)) {
				currentStatus = IntersectionStatus.FULL;
			}
		}

		return currentStatus;
	}

	/**
	 * Check intersection with an other one dimensional line.
	 */
	public IntersectionStatus checkIntersection(Line1D line) {
		double start1 = Math.min(_start, _end);
		double end1 = Math.max(_start, _end);
		double start2 = Math.min(line.getStart(), line.getEnd());
		double end2 = Math.max(line.getStart(), line.getEnd());

		return checkIntersection(start1, end1, start2, end2);
	}

	private IntersectionStatus checkIntersection(double start1, double end1, double start2, double end2) {
		if (start1 > end2 || start2 > end1) {
			return IntersectionStatus.NONE;
		} else if((start1 < end2 && start1 > start2) && (end1 < end2 && end1 > start2)) {
			return IntersectionStatus.FULL;
		} else {
			return IntersectionStatus.PARTIAL;
		}
	}

}

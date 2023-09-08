/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.func.Function1;

/**
 * Something that has an intrinsic direction, that is either {@link Orientation#HORIZONTAL}, or
 * {@link Orientation#VERTICAL}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public interface OrientationAware {

    /**
	 * The layout orientation of children of this {@link FlowLayoutControl}.
	 */
    public Orientation getOrientation();

	/**
	 * The how child controls of a container are arranged.
	 */
	public static enum Orientation {

		/**
		 * In a horizontal layout, children are placed from left to right.
		 */
		HORIZONTAL() {
			@Override
			public boolean isHorizontal() {
				return true;
			}
		},

		/**
		 * A vertical layout, children are placed from top to bottom.
		 */
		VERTICAL() {
			@Override
			public boolean isHorizontal() {
				return false;
			}
		};

		/**
		 * Convert the boolean encoding for orientations to an {@link Orientation} constant.
		 */
		public static Orientation horizontal(boolean horizontal) {
			return horizontal ? HORIZONTAL : VERTICAL;
		}

		/**
		 * Whether this orientation is horizontal.
		 */
		public abstract boolean isHorizontal();

		/**
		 * Function computing {@link Orientation#isHorizontal()} for the given argument.
		 */
		public static class IsHorizontal extends Function1<Boolean, Orientation> {
			@Override
			public Boolean apply(Orientation arg) {
				return arg == null ? true : arg.isHorizontal();
			}
		}

	}
}


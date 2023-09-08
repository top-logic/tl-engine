/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.equal;

import java.util.Arrays;

import com.top_logic.basic.UnreachableAssertion;

/**
 * {@link EqualitySpecification} deeply compares arrays.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ArrayEqualitySpecification extends EqualitySpecification<Object> {

	/** Singleton {@link ArrayEqualitySpecification} instance. */
	public static final ArrayEqualitySpecification INSTANCE = new ArrayEqualitySpecification();

	private ArrayEqualitySpecification() {
		// singleton instance
	}

	@Override
	protected boolean equalsInternal(Object left, Object right) {
		Class<?> leftClass = left.getClass();
		Class<?> rightClass = right.getClass();
		if (!leftClass.isArray() || !rightClass.isArray()) {
			return left.equals(right);
		}

		if (left instanceof Object[] && right instanceof Object[])
			return Arrays.deepEquals((Object[]) left, (Object[]) right);
		else if (left instanceof byte[] && right instanceof byte[])
			return Arrays.equals((byte[]) left, (byte[]) right);
		else if (left instanceof short[] && right instanceof short[])
			return Arrays.equals((short[]) left, (short[]) right);
		else if (left instanceof int[] && right instanceof int[])
			return Arrays.equals((int[]) left, (int[]) right);
		else if (left instanceof long[] && right instanceof long[])
			return Arrays.equals((long[]) left, (long[]) right);
		else if (left instanceof char[] && right instanceof char[])
			return Arrays.equals((char[]) left, (char[]) right);
		else if (left instanceof float[] && right instanceof float[])
			return Arrays.equals((float[]) left, (float[]) right);
		else if (left instanceof double[] && right instanceof double[])
			return Arrays.equals((double[]) left, (double[]) right);
		else if (left instanceof boolean[] && right instanceof boolean[])
			return Arrays.equals((boolean[]) left, (boolean[]) right);
		else
			return left.equals(right);

	}

	@Override
	protected int hashCodeInternal(Object object) {
		Class<?> oClass = object.getClass();
		if (!oClass.isArray()) {
			return object.hashCode();
		}
		if (object instanceof Object[])
			return Arrays.deepHashCode((Object[]) object);
		else if (object instanceof byte[])
			return Arrays.hashCode((byte[]) object);
		else if (object instanceof short[])
			return Arrays.hashCode((short[]) object);
		else if (object instanceof int[])
			return Arrays.hashCode((int[]) object);
		else if (object instanceof long[])
			return Arrays.hashCode((long[]) object);
		else if (object instanceof char[])
			return Arrays.hashCode((char[]) object);
		else if (object instanceof float[])
			return Arrays.hashCode((float[]) object);
		else if (object instanceof double[])
			return Arrays.hashCode((double[]) object);
		else if (object instanceof boolean[])
			return Arrays.hashCode((boolean[]) object);
		else
			throw new UnreachableAssertion("All arrays covered.");
	}

}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.build.doclet;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import test.com.top_logic.build.doclet.B.C;

/**
 * A silly target for JavaDoc.
 * 
 * <p>
 * Does nothing usefull, especially in {@link #getFoobar(Map)}.
 * </p>
 * 
 * @see "http://www.top-logic.com/?target=camelCaseWord"
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@B(value = "A text.", other = { @C("More,..."), @C("and"), @C("more text.") })
public class A<X extends Number & AutoCloseable, Y extends List<? super X>> extends ArrayList<Y>
		implements Comparable<X> {

	/**
	 * Method that does nothing.
	 *
	 * @param map
	 *        Some arbitrary map.
	 * @return Always <code>null</code>.
	 */
	public static <T extends Number & Serializable> List<T> getFoobar(Map<? super String, List<? extends T>> map) {
		return null;
	}

	/**
	 * Function re-declaring a type variable with the same name as the enclosing type.
	 * 
	 * @param <X>
	 *        Name clash.
	 * @param in
	 *        Some input.
	 * @return Some output.
	 * 
	 * @see #twoArgMethod(String, int) A method with two arguments. Do it call on your own risk.
	 */
	@SuppressWarnings("hiding")
	public <X extends Comparable<X>> X map(X in) {
		return null;
	}

	/**
	 * Reference target for "see".
	 */
	@SuppressWarnings("unused")
	public final int twoArgMethod(String x, int y) {
		return 0;
	}

	/**
	 * A two-dimensional array type, can be used e.g. for representing a matrix (`A.get_matrix()` is
	 * a factory) - <span>oh...</span> before I forget, read the following paragraph before using
	 * this method.
	 * 
	 * <p>
	 * Use with case, since the method always returns <code>null</code>. The method only exists for
	 * testing that a dot in the description sentence can be "escaped" with a following comment.
	 * </p>
	 */
	public static int[][] getMatrix() {
		return null;
	}

	/**
	 * Have fun calling this method in a way `have.fun()` to experience the bracing effect of an
	 * `IOException`.
	 * 
	 * Only this sentence should to the the description detail part of the method.
	 *
	 * @throws IOException
	 *         Always, without exception.
	 */
	public static void fun() throws IOException {
		throw new IOException();
	}

	@Override
	public int compareTo(X o) {
		return 0;
	}

}

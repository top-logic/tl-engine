/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.util.Utils;

/**
 * Algorithm computing the longest common subsequence of two input sequences.
 * 
 * @see "https://en.wikipedia.org/wiki/Longest_common_subsequence_problem"
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LongestCommonSubsequence {

	/**
	 * Computes the longest common subsequence of both input sequences.
	 * 
	 * <p>
	 * Note: The time and space complexity of this algorithm in the general case is
	 * <code>O(left.size() * right.size())</code>. Therefore, this should only be used for
	 * relatively short sequences. However, if the difference between the two input sequences
	 * consists of at most one consecutive insertion or deletion, the time and space complexity is
	 * limited by <code>O(max(left.size(), right.size())</code>.
	 * </p>
	 *
	 * @param <T>
	 *        The entry type of the sequences.
	 * @param left
	 *        The left input sequence.
	 * @param right
	 *        The right input sequence.
	 * @return The longest sequence with common elements of both input sequences in the same order.
	 *         Note: The result must not be modified. Note: There is no guarantee that the result is
	 *         stable, since either the left or right input may be returned.
	 */
	public static <T> List<? extends T> compute(List<? extends T> left, List<? extends T> right) {
		return compute(Utils::equals, left, right);
	}

	/**
	 * Computes the longest common subsequence of both input sequences with custom equality
	 * definition.
	 * 
	 * @see #compute(List, List)
	 */
	public static <T> List<? extends T> compute(BiPredicate<? super T, ? super T> equality, List<? extends T> left,
			List<? extends T> right) {
		// Detect common prefix.
		int prefix = 0;
		int leftSize = left.size();
		int rightSize = right.size();
		int commonSize = Math.min(leftSize, rightSize);

		while (prefix < commonSize) {
			if (!equality.test(left.get(prefix), right.get(prefix))) {
				break;
			}
			prefix++;
		}

		commonSize -= prefix;

		if (commonSize == 0) {
			// One is a subsequence of the other.
			if (leftSize <= rightSize) {
				return left;
			} else {
				return right;
			}
		}

		// Detect common suffix.
		int suffix = 0;
		int leftEnd = leftSize - 1;
		int rightEnd = rightSize - 1;
		while (suffix < commonSize) {
			if (!equality.test(left.get(leftEnd - suffix), right.get(rightEnd - suffix))) {
				break;
			}
			suffix++;
		}
		
		if (prefix == 0 && suffix == 0) {
			// No common prefix or suffix, full blown computation.
			return doCcompute(equality, left, right);
		} else {
			// Restrict search to sequences with common prefix and suffix stripped.
			ArrayList<T> result = new ArrayList<>();
			result.addAll(left.subList(0, prefix));
			result.addAll(
				doCcompute(equality,
					left.subList(prefix, leftSize - suffix),
					right.subList(prefix, rightSize - suffix)));
			result.addAll(left.subList(leftSize - suffix, leftSize));
			return result;
		}
	}

	private static <T> List<? extends T> doCcompute(BiPredicate<? super T, ? super T> equality, List<? extends T> left,
			List<? extends T> right) {
		int leftSize = left.size();
		int rightSize = right.size();

		if (leftSize == 0) {
			// Pure insertion.
			return left;
		}
		if (rightSize == 0) {
			// Pure deletion.
			return right;
		}

		int[][] lenAndDir = new int[rightSize + 1][leftSize + 1];

		{
			// Initialize guard for empty right prefix.
			int rightLen = 0;
			for (int leftLen = 0; leftLen < leftSize; leftLen++) {
				lenAndDir[rightLen][leftLen] = 0;
			}
		}

		for (int rightLen = 1; rightLen <= rightSize; rightLen++) {
			// Initialize guard for empty left prefix.
			lenAndDir[rightLen][0] = 0;

			for (int leftLen = 1; leftLen <= leftSize; leftLen++) {
				int lcp;
				if (equality.test(left.get(leftLen - 1), right.get(rightLen - 1))) {
					// Both prefixes end with the same value. The maximum common prefix length is
					// one plus the maximum common prefix length of the both prefixes shortened by
					// that value.
					lcp = both(lenAndDir[rightLen - 1][leftLen - 1] + 1);
				} else {
					// Decide which prefix to shorten. Since the last value does not match, it can
					// be removed either from the left or the right prefix.

					// The length when choosing the value from the left prefix (shortening the right
					// one).
					int lcpLeft = len(lenAndDir[rightLen - 1][leftLen]);
					int lcpRight = len(lenAndDir[rightLen][leftLen - 1]);

					if (lcpLeft >= lcpRight) {
						// Keep the value from the right prefix.
						lcp = left(lcpLeft);
					} else {
						// Keep the value from the left prefix.
						lcp = right(lcpRight);
					}
				}

				lenAndDir[rightLen][leftLen] = lcp;
			}
		}
		
		// Reconstruct what value to keep.
		List<T> result = new ArrayList<>();

		int leftLen = leftSize;
		int rightLen = rightSize;
		while (leftLen > 0 && rightLen > 0) {
			int direction = dir(lenAndDir[rightLen][leftLen]);
			switch (direction) {
				case BOTH: {
					// The value is equal and part of the longest common subsequence.
					result.add(left.get(leftLen - 1));
					leftLen--;
					rightLen--;
					break;
				}
				case LEFT: {
					// The value from the left sequence is taken, the right one was shortened.
					rightLen--;
					break;
				}
				case RIGHT: {
					// The value from the right sequence is taken, the left one was shortened.
					leftLen--;
					break;
				}
				default:
					throw new UnreachableAssertion("No such direction: " + direction);
			}
		}

		// The result was produced in reverse order.
		Collections.reverse(result);

		return result;
	}

	private static final int DIR_MASK = 0b11;
	private static final int DIR_BITS = 2;
	private static final int LEFT = 0b10;
	private static final int RIGHT = 0b01;
	private static final int BOTH = 0b11;

	private static int len(int encodedLen) {
		return encodedLen >>> DIR_BITS;
	}

	private static int dir(int encoded) {
		return encoded & DIR_MASK;
	}

	private static int both(int len) {
		return len << DIR_BITS | BOTH;
	}

	private static int right(int len) {
		return len << DIR_BITS | RIGHT;
	}

	private static int left(int len) {
		return len << DIR_BITS | LEFT;
	}

}

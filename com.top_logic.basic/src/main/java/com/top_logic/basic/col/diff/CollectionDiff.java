/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col.diff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiPredicate;
import java.util.function.Function;

import com.top_logic.basic.col.diff.op.Create;
import com.top_logic.basic.col.diff.op.Delete;
import com.top_logic.basic.col.diff.op.DiffOp;
import com.top_logic.basic.col.diff.op.Move;
import com.top_logic.basic.col.diff.op.Update;
import com.top_logic.basic.util.Utils;

/**
 * Utilities for computing diffs for sets and lists.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CollectionDiff {

	/**
	 * Compares the two sets of items.
	 *
	 * @param <K>
	 *        The key type.
	 * @param <T>
	 *        The item type.
	 * @param idFun
	 *        Function extracting an identifier from an item.
	 * @param left
	 *        Original items.
	 * @param right
	 *        Target set of items.
	 * @return {@link SetDiff} analyzing the differences.
	 */
	public static <K, T> SetDiff<T> diffSet(Function<T, K> idFun, Collection<? extends T> left,
			Collection<? extends T> right) {
		Map<K, T> leftIndex = index(idFun, left);
		Map<K, T> rightIndex = index(idFun, right);

		List<T> created = new ArrayList<>();
		List<Update<T>> updated = new ArrayList<>();
		List<T> deleted = new ArrayList<>();
		for (Entry<K, T> rightEntry : rightIndex.entrySet()) {
			T leftItem = leftIndex.remove(rightEntry.getKey());
			if (leftItem != null) {
				// Common part.
				updated.add(new Update<>(leftItem, rightEntry.getValue()));
			} else {
				// New part.
				created.add(rightEntry.getValue());
			}
		}
		for (Entry<K, T> leftName : leftIndex.entrySet()) {
			// Deleted part.
			deleted.add(leftName.getValue());
		}

		return new SetDiff<>(created, updated, deleted);
	}

	/**
	 * Computes {@link DiffOp}s that transform the left list into the right one.
	 *
	 * @param <K>
	 *        The key type of list items.
	 * @param <T>
	 *        The list item type.
	 * @param idFun
	 *        Function for creating identifiers for list items.
	 * @param left
	 *        The left list.
	 * @param right
	 *        The right list.
	 * @return List of operations transforming the left list into the right one.
	 */
	public static <K, T> List<DiffOp<T>> diffList(Function<T, K> idFun, List<? extends T> left,
			List<? extends T> right) {
		BiPredicate<T, T> eq = equality(idFun);
		List<? extends T> common = LongestCommonSubsequence.compute(eq, left, right);

		Map<K, T> leftIndex = index(idFun, left);

		List<DiffOp<T>> result = new ArrayList<>();
		int commonPos = 0;
		for (int rightPos = 0, cnt = right.size(); rightPos < cnt; rightPos++) {
			T rightItem = right.get(rightPos);
			if (commonPos < common.size()) {
				T commonItem = common.get(commonPos);
				if (eq.test(rightItem, commonItem)) {
					// Take (no change).
					result.add(new Update<>(commonItem, rightItem));
					commonPos++;
					leftIndex.remove(idFun.apply(commonItem));
				} else {
					// Insert in between.
					addInsert(result, idFun, leftIndex, commonItem, rightItem);
				}
			} else {
				// Append.
				addInsert(result, idFun, leftIndex, null, rightItem);
			}
		}

		int idx = 0;
		for (T removed : leftIndex.values()) {
			result.add(idx++, new Delete<>(removed));
		}

		return result;
	}

	private static <T, K> BiPredicate<T, T> equality(Function<T, K> idFun) {
		return (a, b) -> Utils.equals(idFun.apply(a), idFun.apply(b));
	}

	private static <T, K> void addInsert(List<DiffOp<T>> result, Function<T, K> idFun, Map<K, T> leftIndex,
			T common, T rightItem) {
		T leftItem = leftIndex.remove(idFun.apply(rightItem));
		if (leftItem == null) {
			// Create.
			result.add(new Create<>(common, rightItem));
		} else {
			// Move.
			result.add(new Move<>(common, leftItem, rightItem));
		}
	}

	private static <K, T> Map<K, T> index(Function<T, K> idFun, Collection<? extends T> parts) {
		// Preserve order.
		Map<K, T> result = new LinkedHashMap<>();
		for (T part : parts) {
			T clash = result.put(idFun.apply(part), part);
			assert clash == null : "Name clash: " + part + " vs. " + clash;
		}
		return result;
	}

}

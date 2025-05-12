/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog.model;

import java.util.List;

import com.top_logic.basic.col.diff.op.Create;
import com.top_logic.basic.col.diff.op.Delete;
import com.top_logic.basic.col.diff.op.DiffOp;
import com.top_logic.basic.col.diff.op.Move;

/**
 * Algorithm to apply a list patch to a list.
 */
public class ApplyListDiff<T> implements DiffOp.Visitor<T, Void, List<T>> {

	/**
	 * Singleton {@link ApplyListDiff} instance.
	 */
	@SuppressWarnings("rawtypes")
	private static final ApplyListDiff INSTANCE = new ApplyListDiff<>() {
		@Override
		public Void visit(Create<? extends Object> op, List<Object> merged) {
			if (!merged.contains(op.getItem())) {
				super.visit(op, merged);
			}
			return null;
		}
	};

	/**
	 * Singleton {@link ApplyListDiff} instance.
	 */
	@SuppressWarnings("rawtypes")
	private static final ApplyListDiff INSTANCE_BAG = new ApplyListDiff();

	private ApplyListDiff() {
		// Singleton constructor.
	}

	/**
	 * The {@link ApplyListDiff} implementation for unique ordered sets.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ApplyListDiff<T> getInstance() {
		return INSTANCE;
	}

	/**
	 * The {@link ApplyListDiff} implementation.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ApplyListDiff<T> getInstanceBag() {
		return INSTANCE_BAG;
	}

	@Override
	public Void visit(Create<? extends T> op, List<T> merged) {
		int index = merged.indexOf(op.getBefore());
		if (index < 0) {
			merged.add(op.getItem());
		} else {
			merged.add(index, op.getItem());
		}
		return null;
	}

	@Override
	public Void visit(com.top_logic.basic.col.diff.op.Update<? extends T> op,
			List<T> merged) {
		// Ignore, no internal update.
		return null;
	}

	@Override
	public Void visit(Delete<? extends T> op, List<T> merged) {
		merged.remove(op.getDelted());
		return null;
	}

	@Override
	public Void visit(Move<? extends T> op, List<T> merged) {
		int moved = merged.indexOf(op.getMovedLeft());
		if (moved >= 0) {
			int index = op.getBefore() == null ? merged.size() : merged.indexOf(op.getBefore());
			if (index >= 0) {
				merged.remove(moved);
				if (index > moved) {
					index--;
				}
				merged.add(index, op.getMovedRight());
			}
		}
		return null;
	}
}
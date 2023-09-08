/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col.compare;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.TestCase;

import com.top_logic.basic.col.diff.CollectionDiff;
import com.top_logic.basic.col.diff.op.Create;
import com.top_logic.basic.col.diff.op.Delete;
import com.top_logic.basic.col.diff.op.DiffOp;
import com.top_logic.basic.col.diff.op.Move;
import com.top_logic.basic.col.diff.op.Update;
import com.top_logic.basic.col.diff.op.DiffOp.Visitor;

/**
 * Test case for {@link CollectionDiff}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDiffUtil extends TestCase {

	private static final Visitor<Object, Void, List<Object>> APPLY_STRICT = new Visitor<>() {

		@Override
		public Void visit(Create<?> op, List<Object> arg) {
			arg.add(index(arg, op.getBefore()), op.getItem());
			return null;
		}

		@Override
		public Void visit(Update<?> op, List<Object> arg) {
			arg.set(arg.indexOf(op.getLeft()), op.getRight());
			return null;
		}

		@Override
		public Void visit(Delete<?> op, List<Object> arg) {
			arg.remove(index(arg, op.getDelted()));
			return null;
		}

		@Override
		public Void visit(Move<?> op, List<Object> arg) {
			arg.remove(index(arg, op.getMovedLeft()));
			arg.add(index(arg, op.getBefore()), op.getMovedRight());
			return null;
		}

		private int index(List<Object> arg, Object before) {
			int index = before == null ? arg.size() : arg.indexOf(before);
			return index;
		}
	};

	private static final Visitor<Object, Void, List<Object>> APPLY_LAZY = new Visitor<>() {

		@Override
		public Void visit(Create<? extends Object> op, List<Object> arg) {
			arg.add(op.getItem());
			return null;
		}

		@Override
		public Void visit(Update<? extends Object> op, List<Object> arg) {
			arg.add(op.getRight());
			return null;
		}

		@Override
		public Void visit(Delete<? extends Object> op, List<Object> arg) {
			// Skip
			return null;
		}

		@Override
		public Void visit(Move<? extends Object> op, List<Object> arg) {
			arg.add(op.getMovedRight());
			return null;
		}

	};

	public void testSame() {
		doTest(list(1, 2, 3), list(1, 2, 3));
	}

	public void testMove() {
		doTest(list(3, 2, 1), list(1, 2, 3));
	}

	public void testMixed() {
		doTest(list(0, 3, 1, 4), list(1, 2, 3));
	}

	public void testInsert() {
		doTest(list(3), list(1, 2, 3));
	}

	public void testRemove() {
		doTest(list(1, 2, 3), list(2));
	}

	/**
	 * Wraps identifier values into new Objects to test strictness of compares.
	 */
	private static List<Wrapped> list(Object... values) {
		return Arrays.asList(values).stream().map(Wrapped::new).collect(Collectors.toList());
	}

	private static class Wrapped {
		private Object _val;

		/**
		 * Creates a {@link TestDiffUtil.Wrapped}.
		 *
		 */
		public Wrapped(Object val) {
			_val = val;
		}

		public Object getVal() {
			return _val;
		}
	}

	private static void doTest(List<Wrapped> left, List<Wrapped> right) {
		List<DiffOp<Wrapped>> ops = CollectionDiff.diffList(Wrapped::getVal, left, right);
		check(right, ops, APPLY_STRICT, new ArrayList<>(left));
		check(right, ops, APPLY_LAZY, new ArrayList<>());
	}

	private static void check(List<Wrapped> right, List<DiffOp<Wrapped>> ops,
			Visitor<Object, Void, List<Object>> checker, List<Object> result) {
		for (DiffOp<?> op : ops) {
			op.visit(checker, result);
		}
		assertEquals(right, result);
	}

}

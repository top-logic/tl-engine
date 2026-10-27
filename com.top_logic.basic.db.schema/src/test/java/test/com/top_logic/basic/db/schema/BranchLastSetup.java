/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema;

import junit.framework.Test;

import test.com.top_logic.basic.RearrangableTestSetup;

import com.top_logic.basic.col.ArrayStack;
import com.top_logic.basic.col.Stack;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;

/**
 * {@link RearrangableTestSetup} that decides about the value of
 * {@link SchemaConfiguration#getBranchLast()} in tests.
 * 
 * <p>
 * Without a decision, the default of the SQL dialect of the test database applies.
 * </p>
 * 
 * @see SchemaTestSetup#updateBranchLast(SchemaConfiguration, Decision)
 * @see MultipleBranchSetup
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BranchLastSetup extends RearrangableTestSetup {

	private static final Stack<BranchLastSetup> DECISION_STACK = new ArrayStack<>(2);

	private static final MultipleSetupCounter SETUP_CNT = newMultipleCounter();

	private final Decision _branchLast;

	/**
	 * Creates a {@link BranchLastSetup}.
	 * 
	 * @param innerTest
	 *        The actual test.
	 * @param branchLast
	 *        See {@link #branchLast()}.
	 */
	public BranchLastSetup(Test innerTest, Decision branchLast) {
		super(innerTest, SETUP_CNT.getCounterFor(branchLast));
		_branchLast = branchLast;
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _branchLast);
	}

	@Override
	protected void doSetUp() throws Exception {
		DECISION_STACK.push(this);
	}

	@Override
	protected void doTearDown() throws Exception {
		BranchLastSetup head = DECISION_STACK.pop();
		if (head != this) {
			throw new IllegalStateException(
				"Invalid nesting of test setups: Expected: " + this + ", actual: " + head);
		}
	}

	/**
	 * Decision for {@link SchemaConfiguration#getBranchLast()}.
	 * 
	 * <ul>
	 * <li>If {@link Decision#DEFAULT}: Use the configured value, or the default of the SQL dialect,
	 * if no value is configured.</li>
	 * <li>If {@link Decision#TRUE}: Set value to <code>true</code>.</li>
	 * <li>If {@link Decision#FALSE}: Set value to <code>false</code>.</li>
	 * </ul>
	 */
	public static Decision branchLast() {
		if (DECISION_STACK.isEmpty()) {
			return Decision.DEFAULT;
		}
		return DECISION_STACK.peek()._branchLast;
	}

}

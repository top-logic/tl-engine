/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * {@link RearrangableTestSetup} that decide about the value of
 * {@link SchemaConfiguration#hasMultipleBranches()} in tests.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultipleBranchSetup extends RearrangableTestSetup {

	private static final Stack<MultipleBranchSetup> DECISION_STACK = new ArrayStack<>(2);

	private static final MultipleSetupCounter SETUP_CNT = newMultipleCounter();

	private final Decision _multipleBranches;

	/**
	 * Creates a new {@link MultipleBranchSetup}.
	 * 
	 * @param innerTest
	 *        The actual test.
	 * @param multipleBranches
	 *        See {@link #multipleBranches()}.
	 */
	public MultipleBranchSetup(Test innerTest, Decision multipleBranches) {
		super(innerTest, SETUP_CNT.getCounterFor(multipleBranches));
		_multipleBranches = multipleBranches;
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _multipleBranches);
	}

	@Override
	protected void doSetUp() throws Exception {
		DECISION_STACK.push(this);
	}

	@Override
	protected void doTearDown() throws Exception {
		MultipleBranchSetup head = DECISION_STACK.pop();
		if (head != this) {
			throw new IllegalStateException("Invalid nesting of test setups: Expected: " + this +", actual: " + head);
		}
	}
	
	/**
	 * Decision for {@link SchemaConfiguration#hasMultipleBranches()}.
	 * 
	 * <p>
	 * Modify the value of {@link SchemaConfiguration#hasMultipleBranches()} according to return
	 * value:
	 * <ul>
	 * <li>If {@link Decision#DEFAULT}: Use the configured value.</li>
	 * <li>If {@link Decision#TRUE}: Set value to <code>true</code>.</li>
	 * <li>If {@link Decision#FALSE}: Set value to <code>false</code>.</li>
	 * </ul>
	 * <p>
	 */
	public static Decision multipleBranches() {
		if (DECISION_STACK.isEmpty()) {
			return Decision.DEFAULT;
		}
		return DECISION_STACK.peek()._multipleBranches;
	}

}


/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.model.TLModel;
import com.top_logic.model.TransientObject;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the arguments the functions of an {@link ObjectListScope} are applied to: the values of the
 * list's inputs, in declaration order, and the element behind them.
 *
 * <p>
 * An element is attached to what the inputs hold, so an input without a value - one that was never
 * set, or one holding an object that was deleted meanwhile - is no place to attach it to, and the
 * attempt is refused instead of running the function on nothing.
 * </p>
 */
public class TestObjectListScope extends AbstractDBKnowledgeBaseTest {

	private DefaultViewChannel _first;

	private DefaultViewChannel _second;

	private RecordingFunction _function;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_first = new DefaultViewChannel("first");
		_first.set("A");
		_second = new DefaultViewChannel("second");
		_second.set("B");
		_function = new RecordingFunction("done");
	}

	/**
	 * Tests that the link function is applied to the values of all inputs, in declaration order,
	 * with the element behind them.
	 */
	public void testLinkAppliesInputValuesInOrder() {
		Object result = scope(List.of(_first, _second)).linkElement("element");

		assertEquals("The result of the function is handed back.", "done", result);
		assertEquals("The inputs lead the arguments, the element follows them.",
			List.of(List.of("A", "B", "element")), _function.calls());
	}

	/**
	 * Tests that the remove function is applied to the same arguments as the link function.
	 */
	public void testRemoveAppliesInputValuesInOrder() {
		scope(List.of(_first, _second)).removeElement("element");

		assertEquals("The inputs lead the arguments, the element follows them.",
			List.of(List.of("A", "B", "element")), _function.calls());
	}

	/**
	 * Tests that a list without inputs applies its functions to the element alone.
	 */
	public void testLinkWithoutInputs() {
		scope(List.of()).linkElement("element");

		assertEquals("The element is the only argument.", List.of(List.of("element")), _function.calls());
	}

	/**
	 * Tests that an element is not attached while an input holds no value.
	 */
	public void testLinkWithoutInputValue() {
		_second.set(null);

		try {
			scope(List.of(_first, _second)).linkElement("element");
			fail("Expected the attempt to be refused.");
		} catch (TopLogicException expected) {
			assertEquals("The function must not run without a place to attach to.", List.of(),
				_function.calls());
		}
	}

	/**
	 * Tests that an input holding a deleted object is no place to attach an element to.
	 */
	public void testLinkWithDeletedInput() {
		DeletableObject deleted = new DeletableObject();
		_second.set(deleted);
		deleted.setValid(false);

		try {
			scope(List.of(_first, _second)).linkElement("element");
			fail("Expected the attempt to be refused.");
		} catch (TopLogicException expected) {
			assertEquals("The function must not run on a deleted object.", List.of(), _function.calls());
		}
	}

	/**
	 * Tests that a list refuses what it has no function for.
	 */
	public void testMissingFunctions() {
		ObjectListScope scope = new ObjectListScope(List.of(_first), null, null);

		try {
			scope.linkElement("element");
			fail("Expected the attempt to be refused.");
		} catch (TopLogicException expected) {
			// Expected: the list has no link function.
		}

		try {
			scope.removeElement("element");
			fail("Expected the attempt to be refused.");
		} catch (TopLogicException expected) {
			// Expected: the list has no remove function.
		}
	}

	/**
	 * A scope over the given inputs, whose link and remove functions both record their arguments.
	 */
	private ObjectListScope scope(List<ViewChannel> inputs) {
		return new ObjectListScope(inputs, _function, _function);
	}

	/**
	 * A compiled function recording the arguments it is applied to.
	 */
	private static class RecordingFunction extends QueryExecutor {

		private final List<List<Object>> _calls = new ArrayList<>();

		private final Object _result;

		/**
		 * Creates a {@link RecordingFunction}.
		 *
		 * @param result
		 *        The value the function delivers.
		 */
		public RecordingFunction(Object result) {
			_result = result;
		}

		/**
		 * The arguments of each application so far, in application order.
		 */
		public List<List<Object>> calls() {
			return _calls;
		}

		@Override
		protected Object internalExecuteWith(EvalContext definitions, Args args) {
			List<Object> arguments = new ArrayList<>();
			for (Args current = args; current.hasValue(); current = current.next()) {
				arguments.add(current.value());
			}
			_calls.add(arguments);
			return _result;
		}

		@Override
		public SearchExpression getSearch() {
			throw new UnsupportedOperationException();
		}

		@Override
		protected KnowledgeBase getKnowledgeBase() {
			return null;
		}

		@Override
		protected TLModel getTLModel() {
			return null;
		}

		@Override
		protected void internalDisableSecurity() {
			// Nothing to switch off, the function accesses no data.
		}
	}

	/**
	 * Object an input of the list holds; its validity is what these tests switch.
	 */
	private static class DeletableObject extends TransientObject {

		private boolean _valid = true;

		@Override
		public boolean tValid() {
			return _valid;
		}

		/**
		 * Sets whether this object still exists.
		 */
		public void setValid(boolean valid) {
			_valid = valid;
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestObjectListScope.class);
	}

}

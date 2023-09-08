/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.LocalTestSetup;
import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeItem.State;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link KnowledgeItem#getState()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestKnowledgeItemState extends AbstractDBKnowledgeBaseTest {

	public void testState() {
		Transaction tx1 = kb().beginTransaction();

		BObjWithState b1 = (BObjWithState) BObj.newBObj("b1");
		assertEquals(State.NEW, b1.getStateInConstructor());
		assertEquals(State.NEW, b1.tHandle().getState());

		tx1.commit();

		assertEquals(State.PERSISTENT, b1.tHandle().getState());

		Transaction tx2 = kb().beginTransaction();

		b1.tDelete();
		assertEquals(State.PERSISTENT, b1.tHandle().getState());

		tx2.commit();
	}

	public void testLoadingState() throws DataObjectException {
		Transaction tx1 = kb().beginTransaction();
		BObj b1Old = BObj.newBObj("b1");
		tx1.commit();

		flushCache();

		KnowledgeItem b1Handle = (KnowledgeItem) kb().getObjectByAttribute(B_NAME, A1_NAME, "b1");
		assertNotNull(b1Handle);
		BObjWithState b1New = (BObjWithState) b1Handle.getWrapper();

		assertTrue("Cache flush did not work.", b1New != b1Old);

		assertEquals(State.PERSISTENT, b1New.getStateInConstructor());
	}

	private void flushCache() {
		Map<?, ?> cache = (Map<?, ?>) ReflectionUtils.getValue(kb(), "cache");
		cache.clear();
	}

	class ScenarioWithState extends KnowledgeBaseTestScenarioImpl {
		@Override
		protected MOClass bType() {
			MOClass result = (MOClass) super.bType().copy();
			setApplicationType(result, BObjWithState.class);
			return result;
		}
	}

	public static class BObjWithState extends BObj {

		private final State _stateInConstructor;

		public BObjWithState(KnowledgeObject ko) {
			super(ko);

			_stateInConstructor = ko.getState();
		}

		/**
		 * The {@link #tHandle()} state at the time, the constructor was called.
		 */
		public State getStateInConstructor() {
			return _stateInConstructor;
		}

	}

	@Override
	protected LocalTestSetup createSetup(Test self) {
		return new DBKnowledgeBaseTestSetup(self, new ScenarioWithState());
	}

	public static Test suite() {
		return suite(TestKnowledgeItemState.class);
	}

}

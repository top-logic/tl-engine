/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.table;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.basic.Protocol;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateChain;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.table.ReactResourceCellControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.mig.html.layout.AssociationEndRelevance;
import com.top_logic.mig.html.layout.GlobalModelEventForwarder;
import com.top_logic.mig.html.layout.MapBasedAssociationEndRelevance;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that a {@link ReactResourceCellControl} follows the object it displays: editing that object
 * elsewhere updates the label the cell shows.
 *
 * <p>
 * The changes are committed to the {@link com.top_logic.knowledge.service.KnowledgeBase} of the
 * test scenario and delivered by a {@link GlobalModelEventForwarder}, which is also the
 * {@link ModelScope} of the cell's {@link ReactContext}: what the cell shows here is therefore what
 * a session shows.
 * </p>
 */
public class TestResourceCellObservation extends AbstractDBKnowledgeBaseTest {

	/** The labels of the test objects, so that nothing else can produce them. */
	private static final ResourceProvider NAMES = new AbstractResourceProvider() {
		@Override
		public String getLabel(Object object) {
			return ((BObj) object).getA1();
		}
	};

	/** The scope delivering the changes committed to the knowledge base. */
	private GlobalModelEventForwarder _scope;

	/** The context the cells under test are displayed in. */
	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		UpdateChain updates = kb().getUpdateChain();
		Protocol log = new AssertProtocol(getName());
		AssociationEndRelevance relevance = MapBasedAssociationEndRelevance.newAssociationEndRelevance(log,
			Collections.emptyMap(), kb().getMORepository());
		log.checkErrors();
		_scope = new GlobalModelEventForwarder(kb(), updates, relevance);
		_context = new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue())) {
			@Override
			public ModelScope getModelScope() {
				return _scope;
			}
		};
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_scope = null;

		super.tearDown();
	}

	/**
	 * Tests that renaming the displayed object updates the label of the cell.
	 */
	public void testRenamingTheDisplayedObjectUpdatesTheLabel() throws Exception {
		BObj b1 = create("b1");
		ReactResourceCellControl cell = displayedCell(b1);
		assertEquals("b1", label(cell));

		change(b1, "b1 renamed");

		assertEquals("The cell shows the label the object now has.", "b1 renamed", label(cell));
	}

	/**
	 * Tests that a cell that was never displayed is not refreshed.
	 */
	public void testACellNeverDisplayedIsNotRefreshed() throws Exception {
		BObj b1 = create("b1");
		ReactResourceCellControl cell = cell(b1);

		change(b1, "b1 renamed");

		assertEquals("A cell nobody sees observes nothing.", "b1", label(cell));
	}

	/**
	 * Tests that a cell that left the display is not refreshed any more, and is refreshed again
	 * once it is displayed again.
	 */
	public void testADetachedCellIsNotRefreshed() throws Exception {
		BObj b1 = create("b1");
		ReactResourceCellControl cell = displayedCell(b1);
		cell.detach();

		change(b1, "b1 renamed");
		assertEquals("A cell that left the display observes nothing.", "b1", label(cell));

		cell.attach();
		change(b1, "b1 renamed again");
		assertEquals("A cell displayed again observes its value once more.", "b1 renamed again", label(cell));
	}

	/**
	 * Tests that a new displayed value points the observation at the object now shown.
	 */
	public void testANewValueRepointsTheObservation() throws Exception {
		BObj b1 = create("b1");
		BObj b2 = create("b2");
		ReactResourceCellControl cell = displayedCell(b1);

		cell.update(b2);
		assertEquals("b2", label(cell));

		change(b1, "b1 renamed");
		assertEquals("The object no longer shown is not observed any more.", "b2", label(cell));

		change(b2, "b2 renamed");
		assertEquals("The object now shown is observed.", "b2 renamed", label(cell));
	}

	/**
	 * A cell displaying the given object, as the user sees it.
	 */
	private ReactResourceCellControl displayedCell(BObj value) {
		ReactResourceCellControl cell = cell(value);
		cell.attach();
		return cell;
	}

	private ReactResourceCellControl cell(BObj value) {
		return new ReactResourceCellControl(_context, value, NAMES, false, true, false);
	}

	/**
	 * The label the given cell shows, as the client reads it from the cell's state.
	 */
	private static String label(ReactResourceCellControl cell) {
		String state = cell.stateAsJSON();
		String marker = "\"label\":\"";
		int start = state.indexOf(marker);
		assertTrue("The cell state names the label.", start >= 0);
		start += marker.length();
		return state.substring(start, state.indexOf('"', start));
	}

	/**
	 * Creates a committed {@link BObj} with the given name and delivers the pending events, so that
	 * the creation itself is not among the changes a later observation reports.
	 */
	private BObj create(String name) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
		return result;
	}

	/**
	 * Commits a new name for the given object and delivers the resulting change.
	 */
	private void change(BObj object, String name) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		deliverChanges();
	}

	/**
	 * Delivers the changes committed since the last call to the listeners of {@link #_scope}.
	 */
	private void deliverChanges() {
		_scope.synthesizeModelEvents();
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestResourceCellObservation.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.table.control.TableUpdateAccumulator;
import com.top_logic.layout.table.control.TableUpdateAccumulator.UpdateRequest;

/**
 * Test case for {@link TableUpdateAccumulator}
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TestTableUpdateAccumulator extends TestCase {

	TableUpdateAccumulator updateAccumulator;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		updateAccumulator = new TableUpdateAccumulator();
		updateAccumulator.attach();
	}

	public void testSingleUpdate() {
		addUpdate(0, 10, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(10, updates.get(0).getLastAffectedRow());
	}

	public void testOneRowUpdate() {
		addUpdate(0, 0, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(0, updates.get(0).getLastAffectedRow());
	}

	// First update covers fully second one
	public void testFirstCoveringAllUpdates() {
		addUpdate(0, 10, false);
		addUpdate(3, 7, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(10, updates.get(0).getLastAffectedRow());
	}

	// First update covers fully second one
	public void testSecondCoveringAllUpdates() {
		addUpdate(3, 7, false);
		addUpdate(0, 10, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(10, updates.get(0).getLastAffectedRow());
	}

	public void testOverlappingUpdates() {
		addUpdate(0, 10, false);
		addUpdate(4, 12, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(12, updates.get(0).getLastAffectedRow());
	}

	public void testSeamConcatenatedUpdates() {
		addUpdate(0, 10, false);
		addUpdate(11, 12, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(12, updates.get(0).getLastAffectedRow());
	}

	public void testSplittedUpdates() {
		createSplittedUpdates();

		List<UpdateRequest> updates = getUpdates(2);

		// First update
		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(10, updates.get(0).getLastAffectedRow());

		// Second update
		assertEquals(15, updates.get(1).getFirstAffectedRow());
		assertEquals(20, updates.get(1).getLastAffectedRow());
	}

	public void testJoiningUpdate() {
		createJoinedUpdate();

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(20, updates.get(0).getLastAffectedRow());
	}

	// First row must not be greater than second row
	public void testEnsureCorrectRowRangeDefinition() {
		addUpdate(4, 3, true);
	}

	// Check, whether updates can be added/combined after getUpdates() (check internal cache), or
	// not
	public void testSingleCycleIncrementalUpdateCreation() {
		// Perform single update test
		addUpdate(0, 10, false);

		// Retrieve updates
		getUpdates(1);

		// Add second update after getUpdates() call (done within upper test method)
		addUpdate(6, 14, false);

		List<UpdateRequest> updates = getUpdates(1);

		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(14, updates.get(0).getLastAffectedRow());
	}

	// Check, if updates made within different update cycles do not interfere
	public void testMultiCycleIncrementalUpdateCreation() {

		// Create joined updates
		createJoinedUpdate();

		// Clear cache
		updateAccumulator.clearUpdateQueue();

		// Create splitted updates
		createSplittedUpdates();

		List<UpdateRequest> updates = getUpdates(2);

		// First update
		assertEquals(0, updates.get(0).getFirstAffectedRow());
		assertEquals(10, updates.get(0).getLastAffectedRow());

		// Second update
		assertEquals(15, updates.get(1).getFirstAffectedRow());
		assertEquals(20, updates.get(1).getLastAffectedRow());
	}

	// Check, hasUpdates() answers true before call of getUpdates()
	public void testUpdateCheckBeforeRetrieval() {
		assertFalse(updateAccumulator.hasUpdates());

		createSplittedUpdates();

		assertTrue(updateAccumulator.hasUpdates());
	}

	// Check, hasUpdates() answers true after call of getUpdates()
	public void testUpdateCheckAfterRetrieval() {
		assertFalse(updateAccumulator.hasUpdates());

		createSplittedUpdates();
		getUpdates(2);

		assertTrue(updateAccumulator.hasUpdates());
	}

	// Check, hasUpdates() answers false before call of getUpdates() and after clearUpdateQueue()
	public void testUpdateCheckBeforeRetrievalAfterClearance() {
		assertFalse(updateAccumulator.hasUpdates());

		createSplittedUpdates();
		updateAccumulator.clearUpdateQueue();

		assertFalse(updateAccumulator.hasUpdates());
	}

	// Check, hasUpdates() answers false after call of getUpdates() and after clearUpdateQueue()
	public void testUpdateCheckAfterRetrievalAfterClearance() {
		assertFalse(updateAccumulator.hasUpdates());

		createSplittedUpdates();
		getUpdates(2);
		updateAccumulator.clearUpdateQueue();

		assertFalse(updateAccumulator.hasUpdates());
	}

	private void createSplittedUpdates() {
		addUpdate(0, 10, false);
		addUpdate(15, 20, false);
	}

	private void createJoinedUpdate() {
		// Create splitted updates
		createSplittedUpdates();

		// add joining update
		addUpdate(9, 14, false);
	}

	private List<UpdateRequest> getUpdates(int expectedUpdatesCount) {
		List<UpdateRequest> updates = updateAccumulator.getUpdates();

		assertEquals(expectedUpdatesCount, updates.size());

		return updates;
	}

	private void addUpdate(int firstRow, int lastRow, boolean expectAssertionError) {

		try {
			updateAccumulator.addUpdate(firstRow, lastRow);

			// First row was expected to be greater than second row, but was not recognized.
			if (expectAssertionError) {
				fail("First row was greater than last row, but was not recognized!");
			}
		} catch (AssertionError ex) {
			if (!expectAssertionError) {
				fail("First row was greater than last row, but was not expected!");
			}
		}
	}

	public static Test suite() {
		return new TestSuite(TestTableUpdateAccumulator.class);
	}
}

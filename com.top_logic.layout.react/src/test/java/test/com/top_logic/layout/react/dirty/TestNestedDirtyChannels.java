/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.dirty;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.layout.react.dirty.StateHandler;

/**
 * Tests that what is unsaved in a scope is unsaved in the scope enclosing it.
 *
 * <p>
 * The scopes of the application nest: a tab lies within the sidebar item displaying it, and the
 * form the user typed into sits in both. Leaving either of them is leaving that input behind, so
 * both channels must know about it.
 * </p>
 */
public class TestNestedDirtyChannels extends TestCase {

	/** The channel of the enclosing scope - the sidebar item, say. */
	private DirtyChannel _item;

	/** The channel of the scope within it - the tab the item displays. */
	private DirtyChannel _tab;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_item = new DirtyChannel();
		_tab = new DirtyChannel(_item);
	}

	/**
	 * Tests that input left unsaved in the tab is unsaved in the item displaying that tab.
	 */
	public void testUnsavedInTheTabIsUnsavedInTheItem() {
		StateHandler changes = new UnsavedChanges();
		_tab.updateState(changes, true);

		assertEquals("The tab reports the form the user typed into.",
			List.of(changes), _tab.getDirtyHandlers());
		assertTrue("Leaving the item leaves that input behind just as leaving the tab does.",
			_item.hasDirtyHandlers());
		assertEquals(List.of(changes), _item.getDirtyHandlers());
	}

	/**
	 * Tests that resolving the changes - saving or discarding them - settles both scopes.
	 */
	public void testResolvedChangesClearBothScopes() {
		StateHandler changes = new UnsavedChanges();
		_tab.updateState(changes, true);

		changes.executeDiscard();
		_tab.updateState(changes, false);

		assertFalse("Nothing is left unsaved in the tab.", _tab.hasDirtyHandlers());
		assertFalse("Nothing is left unsaved in the item either.", _item.hasDirtyHandlers());
	}

	/**
	 * Tests that a form disposed with the tab it sat in is no longer held against the item.
	 */
	public void testADisposedFormIsDroppedFromBothScopes() {
		StateHandler changes = new UnsavedChanges();
		_tab.updateState(changes, true);

		_tab.removeHandler(changes);

		assertFalse("The disposed form is gone from the tab.", _tab.hasDirtyHandlers());
		assertFalse("The disposed form is gone from the item.", _item.hasDirtyHandlers());
	}

	/**
	 * Tests that what the item holds directly is none of the tab's business: only the enclosing
	 * scope hears about a nested one, not the other way round.
	 */
	public void testTheTabDoesNotSeeWhatTheItemHolds() {
		StateHandler changes = new UnsavedChanges();
		_item.updateState(changes, true);

		assertTrue(_item.hasDirtyHandlers());
		assertFalse("A form outside the tab is not held against leaving the tab.",
			_tab.hasDirtyHandlers());
	}

	/**
	 * Tests that a channel of a scope nothing encloses tracks what is reported to it, and nothing
	 * else.
	 */
	public void testAScopeWithoutAnEnclosingOne() {
		DirtyChannel standalone = new DirtyChannel();

		StateHandler changes = new UnsavedChanges();
		standalone.updateState(changes, true);
		assertEquals(List.of(changes), standalone.getDirtyHandlers());

		standalone.updateState(changes, false);
		assertFalse(standalone.hasDirtyHandlers());
	}

	/**
	 * Tests that a scope nested two levels deep reaches the outermost one: a tab of a tab bar the
	 * user drilled into still belongs to the sidebar item displaying all of it.
	 */
	public void testNestingReachesTheOutermostScope() {
		DirtyChannel innerTab = new DirtyChannel(_tab);

		StateHandler changes = new UnsavedChanges();
		innerTab.updateState(changes, true);

		assertTrue(innerTab.hasDirtyHandlers());
		assertTrue("The enclosing tab holds what its inner tab holds.", _tab.hasDirtyHandlers());
		assertTrue("The item holds what the tabs it displays hold.", _item.hasDirtyHandlers());

		innerTab.updateState(changes, false);

		assertFalse(_tab.hasDirtyHandlers());
		assertFalse(_item.hasDirtyHandlers());
	}

}

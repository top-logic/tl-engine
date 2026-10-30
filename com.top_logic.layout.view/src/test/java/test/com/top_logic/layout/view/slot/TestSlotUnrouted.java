/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.TestCase;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.DeliveryScope;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotHandle;
import com.top_logic.layout.view.slot.SlotPath;
import com.top_logic.layout.view.slot.SlotPlaceholder;
import com.top_logic.layout.view.slot.SlotRegistry;

/**
 * Tests that the {@link SlotRegistry} reports a {@link SlotContribution} that reaches no
 * {@link SlotPlaceholder} once the interaction displaying it has completed.
 */
public class TestSlotUnrouted extends TestCase {

	private static final String SLOT = "actions";

	private static final String UNKNOWN_SLOT = "nowhere";

	private SlotRegistry _registry;

	private CollectingLogListener _warnings;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SlotRegistry();
		_warnings = new CollectingLogListener(Set.of(Level.WARN), true);
	}

	@Override
	protected void tearDown() throws Exception {
		_warnings.deactivate();
		super.tearDown();
	}

	/** A contribution to a slot no placeholder offers is marked and logged. */
	public void testUnknownSlotIsReported() {
		Contribution contribution = new Contribution(UNKNOWN_SLOT, false);

		DeliveryScope.enter();
		try {
			_registry.registerContribution(contribution);
			assertFalse("Reported when the interaction completes.", contribution.isUnrouted());
		} finally {
			DeliveryScope.exit();
		}

		assertTrue(contribution.isUnrouted());
		assertEquals(List.of(contribution.getSource()), reported());
	}

	/** A placeholder attached after its contribution in the same interaction catches it. */
	public void testSlotAttachedLaterInTheInteraction() {
		Contribution contribution = new Contribution(SLOT, false);

		DeliveryScope.enter();
		try {
			_registry.registerContribution(contribution);
			_registry.registerPlaceholder(new Placeholder(SLOT));
		} finally {
			DeliveryScope.exit();
		}

		assertFalse(contribution.isUnrouted());
		assertEquals(List.of(), reported());
	}

	/** An optional contribution that reaches no slot is not reported. */
	public void testOptionalIsNotReported() {
		Contribution contribution = new Contribution(UNKNOWN_SLOT, true);

		DeliveryScope.enter();
		try {
			_registry.registerContribution(contribution);
		} finally {
			DeliveryScope.exit();
		}

		assertFalse(contribution.isUnrouted());
		assertEquals(List.of(), reported());
	}

	/**
	 * A reported contribution is reported only once while it reaches no slot, loses its mark when a
	 * slot catches it, and is reported anew when it loses the slot again.
	 */
	public void testReportedContributionGetsASlot() {
		Contribution contribution = new Contribution(SLOT, false);

		DeliveryScope.enter();
		try {
			_registry.registerContribution(contribution);
		} finally {
			DeliveryScope.exit();
		}
		assertTrue(contribution.isUnrouted());

		DeliveryScope.enter();
		try {
			_registry.registerContribution(new Contribution(SLOT, true)).close();
		} finally {
			DeliveryScope.exit();
		}
		assertEquals("Reported once while it stays without slot.", 1, reported().size());

		SlotHandle placeholder;
		DeliveryScope.enter();
		try {
			placeholder = _registry.registerPlaceholder(new Placeholder(SLOT));
		} finally {
			DeliveryScope.exit();
		}
		assertFalse("A routed contribution loses its mark.", contribution.isUnrouted());

		DeliveryScope.enter();
		try {
			placeholder.close();
		} finally {
			DeliveryScope.exit();
		}
		assertTrue(contribution.isUnrouted());
		assertEquals("Reported anew after it lost its slot.", 2, reported().size());
	}

	/** A contribution that leaves the display within the interaction is not reported. */
	public void testUnregisteredBeforeTheEndIsNotReported() {
		Contribution contribution = new Contribution(UNKNOWN_SLOT, false);

		DeliveryScope.enter();
		try {
			_registry.registerContribution(contribution).close();
		} finally {
			DeliveryScope.exit();
		}

		assertFalse(contribution.isUnrouted());
		assertEquals(List.of(), reported());
	}

	/**
	 * A slot that leaves the display outside an interaction - a session that ends disposes its trees
	 * so - orphans its contribution without a report.
	 */
	public void testNoReportOutsideAnInteraction() {
		Contribution contribution = new Contribution(SLOT, false);
		SlotHandle placeholder = _registry.registerPlaceholder(new Placeholder(SLOT));
		SlotHandle registration = _registry.registerContribution(contribution);

		placeholder.close();
		registration.close();

		assertFalse(contribution.isUnrouted());
		assertEquals(List.of(), reported());
	}

	/**
	 * The {@link SlotContribution#getSource() sources} named in the warnings logged so far.
	 */
	private List<String> reported() {
		List<LogEntry> entries = _warnings.getLogEntries();
		return entries.stream()
			.map(LogEntry::getMessage)
			.filter(message -> message.contains(Contribution.SOURCE_PREFIX))
			.map(message -> message.substring(message.indexOf(Contribution.SOURCE_PREFIX),
				message.indexOf(' ', message.indexOf(Contribution.SOURCE_PREFIX))))
			.collect(Collectors.toList());
	}

	private static final class Contribution implements SlotContribution {

		static final String SOURCE_PREFIX = "source#";

		private static int _nextId;

		private final String _slotName;

		private final boolean _optional;

		private final String _source = SOURCE_PREFIX + _nextId++;

		private boolean _unrouted;

		Contribution(String slotName, boolean optional) {
			_slotName = slotName;
			_optional = optional;
		}

		@Override
		public String getSlotName() {
			return _slotName;
		}

		@Override
		public SlotPath getSlotPath() {
			return SlotPath.ROOT;
		}

		@Override
		public List<ReactControl> getControls() {
			return List.of();
		}

		@Override
		public boolean isOptional() {
			return _optional;
		}

		@Override
		public String getSource() {
			return _source;
		}

		@Override
		public boolean isUnrouted() {
			return _unrouted;
		}

		@Override
		public void setUnrouted(boolean unrouted) {
			_unrouted = unrouted;
		}
	}

	private static final class Placeholder implements SlotPlaceholder {

		private final String _slotName;

		Placeholder(String slotName) {
			_slotName = slotName;
		}

		@Override
		public String getSlotName() {
			return _slotName;
		}

		@Override
		public SlotPath getSlotPath() {
			return SlotPath.ROOT;
		}

		@Override
		public void setRoutedContributions(List<SlotContribution> contributions) {
			// Displays nothing.
		}
	}

}

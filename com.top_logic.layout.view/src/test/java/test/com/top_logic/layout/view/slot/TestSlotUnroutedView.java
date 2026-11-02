/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.slot;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.layout.view.slot.SlotContentElement;
import com.top_logic.layout.view.slot.SlotContribution;
import com.top_logic.layout.view.slot.SlotRegistry;
import com.top_logic.layout.view.slot.control.SlotContentControl;

/**
 * A {@code <slot-content>} of a loaded view that reaches no slot carries the diagnostic
 * {@link SlotContentControl#UNROUTED} and is logged, unless it is
 * {@link SlotContentElement.Config#isOptional() optional}.
 *
 * <p>
 * The fixture declares a routed contribution before its slot, so the page build routes it only
 * when the slot attaches after it - which the report waits for.
 * </p>
 */
public class TestSlotUnroutedView extends AbstractSlotViewTest {

	private static final String UNROUTED_VIEW = "slot-unrouted.view.xml";

	/** The slot name of {@link #UNROUTED_VIEW} that no slot offers. */
	private static final String UNKNOWN_SLOT = "nowhere";

	@Override
	protected String[] fixtures() {
		return new String[] { UNROUTED_VIEW };
	}

	/**
	 * Only the contribution to the unknown slot that is not optional is marked and logged, naming
	 * the slot and the view declaring it.
	 */
	public void testUnroutedContributionIsReported() throws IOException, ConfigurationException {
		CollectingLogListener warnings = new CollectingLogListener(Set.of(Level.WARN), true);
		try {
			createView(UNROUTED_VIEW);
			loadPage("");

			List<SlotContribution> contributions = contributions();
			assertEquals("Contributions displayed.", 3, contributions.size());
			for (SlotContribution contribution : contributions) {
				SlotContentControl control = (SlotContentControl) contribution;
				boolean expected = UNKNOWN_SLOT.equals(contribution.getSlotName()) && !contribution.isOptional();
				assertEquals("Diagnostic of the contribution to '" + contribution.getSlotName() + "' (optional: "
					+ contribution.isOptional() + ").",
					expected, control.diagnostics().containsKey(SlotContentControl.UNROUTED));
			}

			List<String> reports = warnings.getLogEntries().stream()
				.filter(entry -> entry.getCaller() == SlotRegistry.class)
				.map(LogEntry::getMessage)
				.collect(Collectors.toList());
			assertEquals("Reported once: " + reports, 1, reports.size());
			String report = reports.get(0);
			assertTrue(report, report.contains("'" + UNKNOWN_SLOT + "'"));
			assertTrue(report, report.contains(UNROUTED_VIEW));
		} finally {
			warnings.deactivate();
		}
	}

	/**
	 * Test suite with the services of {@link AbstractSlotViewTest#createSuite(Class)}.
	 */
	public static Test suite() {
		return createSuite(TestSlotUnroutedView.class);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.environment;

import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.agent.environment.UserMemories.Entry;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.MemoryKind;

/**
 * That what the agent kept about a user reaches the environment block, and that it is a section
 * which disappears whole when there is nothing to say.
 */
public class TestUserMemories extends TestCase {

	/** The label the test renders per kind - the production one is a translated resource. */
	private static final java.util.function.Function<MemoryKind, String> LABEL =
		kind -> switch (kind) {
			case PREFERENCE -> "Preference";
			case FACT -> "Fact";
			case FEEDBACK -> "Feedback";
		};

	private static final String HEADING = "What you know about this user:";

	/** One line per memory, each naming its kind. */
	public void testSection() {
		String section = UserMemories.section(List.of(
			new Entry(MemoryKind.PREFERENCE, "Keep answers to three sentences."),
			new Entry(MemoryKind.FACT, "Works in accounting.")), HEADING, LABEL);

		assertEquals(HEADING
			+ "\n- [Preference] Keep answers to three sentences."
			+ "\n- [Fact] Works in accounting.", section);
	}

	/** Nothing to say is no heading and no gap, the way an empty preferences section is. */
	public void testNothingIsNoSection() {
		assertEquals("", UserMemories.section(List.of(), HEADING, LABEL));
		assertEquals("", UserMemories.section(null, HEADING, LABEL));
		assertEquals("", UserMemories.section(
			List.of(new Entry(MemoryKind.FACT, "   ")), HEADING, LABEL));
	}

	/**
	 * A memory occupies exactly one line.
	 *
	 * <p>
	 * The block is read line by line, so a second line of one memory would read as a memory of its
	 * own.
	 * </p>
	 */
	public void testOneMemoryIsOneLine() {
		String section = UserMemories.section(
			List.of(new Entry(MemoryKind.FACT, "Works in accounting.\nSits in Bonn.")),
			HEADING, LABEL);

		assertEquals(HEADING + "\n- [Fact] Works in accounting. Sits in Bonn.", section);
	}

	/** A row that lost its kind is still rendered; only the kind marker is left out. */
	public void testMemoryWithoutAKind() {
		assertEquals(HEADING + "\n- Likes coffee.",
			UserMemories.section(List.of(new Entry(null, "Likes coffee.")), HEADING, LABEL));
	}

	/** The section lands in the block, below the preferences, and drops out when it is empty. */
	public void testInTheBlock() {
		String block = RunEnvironment.of(EnvironmentTemplate.DEFAULT, Map.of(
			PromptPlaceholders.USER, "root",
			PromptPlaceholders.LANGUAGE, "English (en)",
			PromptPlaceholders.DATE, "2026-09-19 (Europe/Berlin)",
			PromptPlaceholders.AGENT, "Albert",
			PromptPlaceholders.MODEL, "test-model",
			PromptPlaceholders.APPLICATION, "Demo",
			PromptPlaceholders.CONTEXT, "",
			PromptPlaceholders.PREFERENCES, "",
			PromptPlaceholders.MEMORIES,
			UserMemories.section(List.of(new Entry(MemoryKind.PREFERENCE, "Short answers.")),
				HEADING, LABEL))).block();

		assertTrue(block, block.contains(HEADING + "\n- [Preference] Short answers."));
		assertTrue("The section keeps its place inside the block.", block.endsWith("</environment>"));
	}

	/** With nothing kept, the block reads exactly as it did before this feature. */
	public void testAnEmptySectionLeavesNoTrace() {
		Map<String, String> values = Map.of(
			PromptPlaceholders.USER, "root",
			PromptPlaceholders.LANGUAGE, "English (en)",
			PromptPlaceholders.DATE, "2026-09-19 (Europe/Berlin)",
			PromptPlaceholders.AGENT, "Albert",
			PromptPlaceholders.MODEL, "test-model",
			PromptPlaceholders.APPLICATION, "Demo",
			PromptPlaceholders.CONTEXT, "",
			PromptPlaceholders.PREFERENCES, "",
			PromptPlaceholders.MEMORIES, "");

		String block = RunEnvironment.of(EnvironmentTemplate.DEFAULT, values).block();
		assertFalse(block, block.contains("{{"));
		assertFalse("No orphaned blank line where the section would be.", block.contains("\n\n"));
	}

	/** A switched-off installation reads nothing, whatever is stored. */
	public void testSwitchedOffReadsNothing() {
		UserMemoryConfig off = TypedConfiguration.newConfigItem(UserMemoryConfig.class);
		off.update(off.descriptor().getProperty(UserMemoryConfig.ENABLED), Boolean.FALSE);

		try (UserMemorySettings.Overridden overridden = UserMemorySettings.override(off)) {
			assertFalse(UserMemorySettings.enabled());
			assertEquals(List.of(), UserMemories.load(null));
			assertEquals(List.of(), UserMemories.forRun(null));
		}
		assertTrue("The override is undone.", UserMemorySettings.enabled());
	}

	/**
	 * The conversation's snapshot wins over the store, which is what keeps the block identical
	 * across the turns of one conversation.
	 */
	public void testTheBoundSnapshotWins() {
		List<Entry> snapshot = List.of(new Entry(MemoryKind.FACT, "From the snapshot."));
		try (UserMemoryBinding.Binding bound = UserMemoryBinding.bind(snapshot)) {
			assertEquals(snapshot, UserMemories.forRun(null));
		}
		assertEquals("Outside the binding the store answers again.", List.of(),
			UserMemories.forRun(null));
	}

	/**
	 * "Read, and there were none" is not "not read yet".
	 *
	 * <p>
	 * A conversation that was opened with no memories must bind an empty list, not nothing, or
	 * every run of it would read the store afresh and a memory written mid-conversation would
	 * appear in the next turn's block.
	 * </p>
	 */
	public void testAnEmptySnapshotIsStillASnapshot() {
		try (UserMemoryBinding.Binding bound = UserMemoryBinding.bind(List.of())) {
			assertEquals(List.of(), UserMemoryBinding.snapshot());
			assertNotNull(UserMemoryBinding.snapshot());
		}
		assertNull(UserMemoryBinding.snapshot());
	}
}

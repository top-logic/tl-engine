/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.List;

import junit.framework.Test;

import com.top_logic.ai.demo.secdemo.SecDemoSetupService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;

/**
 * Classifying a TL-Script tool in {@code <tool-groups>}, end to end.
 *
 * <p>
 * A TL-Script tool has no {@code @ToolMethod} annotation to carry a group, so it arrives ungrouped
 * and - deny by default - is denied to every role. There is deliberately no way for such a tool to
 * declare its own group: a tool's group states what the tool <em>does</em> while the check asks what
 * the <em>user</em> holds, so whoever picks the group picks which capability it costs to call the
 * tool, and {@code createScriptTool} is reachable with {@code agent.authoring} rather than with
 * {@code data.write}. Classification therefore stays with the administrator, in
 * {@code <tool-groups>}, one named tool at a time.
 * </p>
 *
 * <p>
 * That mechanism is the migration path every application with script tools has to walk, which is why
 * it is tested against a real boot rather than only against a hand-built policy
 * ({@code TestRoleToolAccess.testConfiguredGroupClassifiesAnUngroupedScriptTool} does the latter).
 * The fixture ships three one-tool factories:
 * </p>
 * <ul>
 * <li>{@code secdemo-reporting.countNotes}, classified into {@code data.read},</li>
 * <li>{@code secdemo-editing.addNote}, classified into {@code data.write},</li>
 * <li>{@code secdemo-unclassified.countNotesUnclassified}, named by no {@code <tool-groups>} entry.</li>
 * </ul>
 *
 * <p>
 * The first and the last have the same body, the same mode, the same parameters and the same
 * description apart from one sentence. The single difference between them is one name in one
 * {@code <tool-groups>} entry, so the pair isolates the mechanism: whatever makes one reachable and
 * the other not can only be the classification.
 * </p>
 */
public class SecDemoScriptToolClassificationTest extends SecDemoPersonaTest {

	/**
	 * Precondition: the three script tools are really registered, and really carry no declared group.
	 *
	 * <p>
	 * If {@code Tool.group()} ever started returning something for a script tool, every case below
	 * would still pass while testing something else entirely - the declaration rather than the
	 * classification.
	 * </p>
	 */
	public void testScriptToolsDeclareNoGroupOfTheirOwn() {
		for (String name : List.of(CLASSIFIED_READ_SCRIPT_TOOL, CLASSIFIED_WRITE_SCRIPT_TOOL,
			UNCLASSIFIED_SCRIPT_TOOL)) {
			Tool tool = tool(name);
			assertTrue("A TL-Script tool must declare no group; '" + name + "' declares '"
				+ tool.group() + "'.", tool.group() == null || tool.group().isBlank());
		}
	}

	/**
	 * The unclassified tool is denied to every persona, and the message says why in the administrator's
	 * terms.
	 *
	 * <p>
	 * "belongs to no tool group" is a different statement from "requires the data.read capability":
	 * the first is addressed by writing a {@code <tool-groups>} entry, the second by granting a role.
	 * An operator who cannot tell them apart cannot fix either.
	 * </p>
	 */
	public void testUnclassifiedScriptToolDeniedToEveryPersona() {
		for (String login : SecDemoSetupService.personaLogins()) {
			Tool tool = tool(UNCLASSIFIED_SCRIPT_TOOL);
			TopLogicSecurityContext security = securityFor(login);

			assertFalse(login + " must not reach an unclassified script tool.",
				security.hasToolPermission(tool));

			String reason = security.toolDenialReason(tool);
			assertNotNull(reason);
			assertTrue("The denial must name the missing classification, not a missing capability: "
				+ reason, reason.contains("no tool group"));
			assertTrue("...and say who can fix it: " + reason, reason.contains("administrator"));
		}
	}

	/**
	 * The other half: naming the tool makes the otherwise identical tool reachable to whoever holds the
	 * group it is named in.
	 *
	 * <p>
	 * This pair is the whole test. One name in one {@code <tool-groups>} entry is the difference between
	 * a script tool nobody can call and a script tool the {@code data.read} holders can.
	 * </p>
	 */
	public void testClassificationMakesTheSameToolReachable() {
		assertAllowed(READER, CLASSIFIED_READ_SCRIPT_TOOL);
		assertDenied(READER, UNCLASSIFIED_SCRIPT_TOOL, "no tool group");
	}

	/**
	 * The classification is a group assignment, not a grant: a persona without the group is still
	 * denied.
	 *
	 * <p>
	 * {@code secviewer1} holds {@code model.read} and not {@code data.read}, so the entry that
	 * classifies the tool does nothing for it. Were classification a grant in disguise, classifying a
	 * tool would hand it to everybody.
	 * </p>
	 */
	public void testClassifyingIsNotGranting() {
		assertDenied(VIEWER, CLASSIFIED_READ_SCRIPT_TOOL, ToolGroups.DATA_READ);
		assertDenied(BARE, CLASSIFIED_READ_SCRIPT_TOOL, ToolGroups.DATA_READ);
	}

	/**
	 * A second factory mapped to a second group behaves independently of the first.
	 *
	 * <p>
	 * {@code secdemo-editing.addNote} is the only <em>annotated-tool-like</em> member of
	 * {@code data.write} in this fixture; the framework's own consumer of that capability is the commit
	 * path of {@code executeTLScript}, which is an argument rather than a tool of its own. That makes
	 * this a clean statement about the mechanism: the capability reaches exactly the one tool an
	 * administrator put in it.
	 * </p>
	 */
	public void testEachFactoryLandsInItsOwnGroup() {
		assertAllowed(WRITER, CLASSIFIED_WRITE_SCRIPT_TOOL);
		assertAllowed(WRITER, CLASSIFIED_READ_SCRIPT_TOOL);

		// secreader1 holds data.read and not data.write, so the two groups do not bleed into each other.
		// secEditor is deliberately not the contrast here: it holds data.write, because committing
		// through executeTLScript costs that capability and the matrix promises it can create a note.
		assertDenied(READER, CLASSIFIED_WRITE_SCRIPT_TOOL, ToolGroups.DATA_WRITE);
		assertDenied(VIEWER, CLASSIFIED_WRITE_SCRIPT_TOOL, ToolGroups.DATA_WRITE);
	}

	/**
	 * A classification reaches every name a tool has, which is why one entry is enough.
	 *
	 * <p>
	 * A tool has three: the bare name the model calls, the {@code {factory}.{tool}} name an
	 * administrator writes, and the provider-safe {@code factory__tool} form the registry keys it by.
	 * The {@code <tool-groups>} entry is written in the second form, and the check that matters happens
	 * on the {@link Tool} object the agent loop holds - so if matching only looked at one of the three,
	 * this would fail while the unit test still passed.
	 * </p>
	 */
	public void testTheClassificationMatchesTheToolTheAgentLoopHolds() {
		Tool tool = tool(CLASSIFIED_READ_SCRIPT_TOOL);

		assertEquals("The bare name the model calls is not the configured name.",
			"countNotes", tool.name());
		assertTrue("The classification must apply to the Tool object, not only to its configured name.",
			securityFor(READER).hasToolPermission(tool));
		assertEquals(ToolGroups.DATA_READ, securityFor(READER).toolCapability(tool));
	}

	/** An administrator reaches an unclassified tool too - the bypass does not care about groups. */
	public void testAdministratorReachesTheUnclassifiedTool() {
		Person administrator = anyAdministrator();
		assertNotNull("The application must have at least one administrator account.", administrator);

		assertTrue(TopLogicSecurityContext.forRun(administrator, false)
			.hasToolPermission(tool(UNCLASSIFIED_SCRIPT_TOOL)));
	}

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoScriptToolClassificationTest.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.text;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.AgentTuning;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.model.Agent;
import com.top_logic.tl.ai.core.model.TlAiCoreFactory;
import com.top_logic.tl.ai.core.screenshots.model.Screenshot;
import com.top_logic.tl.ai.core.screenshots.model.TlAiScreenshotsFactory;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link TextEditTool} against real objects in a real knowledge base.
 *
 * <p>
 * The applier's own behaviour is pinned by {@code TestTextEdits} and is not repeated here. What this
 * class is about is everything the applier does not know: which attributes may be edited at all, what
 * the run's principal is allowed to do with them, and - the assertion most of these tests end on -
 * that a refused edit leaves the <em>stored</em> value exactly as it was. A tool that reported a
 * refusal and wrote anyway would pass every test that only read its answer.
 * </p>
 */
public class TestTextEditTool extends BasicTestCase {

	/** The attribute the happy paths work on: a {@code tl.core:Text} property. */
	private static final String PROMPT = Agent.SYSTEM_PROMPT_ATTR;

	private static final String BODY = "# Title\n\nFirst paragraph.\n\nSecond paragraph.\n";

	private TextEditTool _tool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_tool = new TextEditTool();
	}

	// -- reading --

	/** The value comes back as stored, every line behind its number. */
	public void testReadIsVerbatimWithLineNumbers() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = read(agent, PROMPT, null, null);

		assertContains("  1| # Title", answer);
		assertContains("  3| First paragraph.", answer);
		assertContains("  5| Second paragraph.", answer);
		assertContains("The count is stated before the content.", "all 6 lines", answer);
	}

	/**
	 * The value is business data - text some other end user typed - so it arrives inside the
	 * untrusted-content envelope, like every other disclosure of stored data.
	 */
	public void testReadMarksTheValueAsBusinessData() throws Exception {
		Agent agent = agentWith(BODY);

		assertContains("<untrusted_model_data", read(agent, PROMPT, null, null));
	}

	/** A line range reads a part of a long value without loading all of it. */
	public void testReadOfALineRange() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = read(agent, PROMPT, 3, 3);

		assertContains("  3| First paragraph.", answer);
		assertFalse("Only the asked-for line: " + answer, answer.contains("Second paragraph."));
		assertContains("lines 3 to 3 of 6", answer);
	}

	/** An unset value has no passage to anchor on, and the answer says what to do instead. */
	public void testReadOfAnEmptyValue() throws Exception {
		Agent agent = agentWith(null);

		String answer = read(agent, PROMPT, null, null);

		assertContains("is empty", answer);
		assertContains("empty 'oldText'", answer);
	}

	// -- editing --

	/** The anchored passage changes and nothing else does, character for character. */
	public void testEditChangesOnlyTheAnchoredPassage() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(agent, PROMPT, "First paragraph.", "First paragraph, revised.", null);

		assertContains("Replaced 1 occurrence.", answer);
		assertEquals("# Title\n\nFirst paragraph, revised.\n\nSecond paragraph.\n", stored(agent));
	}

	/** The answer shows the region as it now reads, so the edit needs no second read to verify. */
	public void testEditShowsTheChangedRegion() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(agent, PROMPT, "Second paragraph.", "Second paragraph, revised.", null);

		assertContains("  5| Second paragraph, revised.", answer);
		assertContains("The region is stored data and is marked as such.",
			"<untrusted_model_data", answer);
	}

	/** An empty replacement deletes the passage. */
	public void testEditCanDelete() throws Exception {
		Agent agent = agentWith(BODY);

		edit(agent, PROMPT, "\nSecond paragraph.\n", "", null);

		assertEquals("# Title\n\nFirst paragraph.\n", stored(agent));
	}

	/** Adding text is anchoring on a passage that stays and putting it back with the addition. */
	public void testEditCanAppendBehindAnAnchor() throws Exception {
		Agent agent = agentWith(BODY);

		edit(agent, PROMPT, "Second paragraph.", "Second paragraph.\n\nThird paragraph.", null);

		assertEquals("# Title\n\nFirst paragraph.\n\nSecond paragraph.\n\nThird paragraph.\n",
			stored(agent));
	}

	/** An empty anchor against an unset value writes the first content. */
	public void testEditWritesTheFirstContentOfAnEmptyValue() throws Exception {
		Agent agent = agentWith(null);

		edit(agent, PROMPT, "", "# New\n", null);

		assertEquals("# New\n", stored(agent));
	}

	/** replaceAll is how a caller says it does not need to tell the occurrences apart. */
	public void testReplaceAllTakesEveryOccurrence() throws Exception {
		Agent agent = agentWith("a\nb\na\n");

		String answer = edit(agent, PROMPT, "a", "c", Boolean.TRUE);

		assertContains("Replaced 2 occurrences.", answer);
		assertEquals("c\nb\nc\n", stored(agent));
	}

	/** An edit that would change nothing is reported and not written. */
	public void testAnEditThatChangesNothingIsSaidSo() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(agent, PROMPT, "First paragraph.", "First paragraph.", null);

		assertContains("No change", answer);
		assertEquals(BODY, stored(agent));
	}

	// -- refused edits leave the value alone --

	/** An ambiguous anchor changes nothing at all, and the refusal names the lines. */
	public void testAnAmbiguousAnchorWritesNothing() throws Exception {
		Agent agent = agentWith("a\nb\na\n");

		String answer = edit(agent, PROMPT, "a", "c", null);

		assertError(answer, "occurs 2 times");
		assertContains("lines 1, 3", answer);
		assertEquals("a\nb\na\n", stored(agent));
	}

	/** An anchor that is nowhere changes nothing, and the refusal says where it stopped agreeing. */
	public void testAMissingAnchorWritesNothing() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(agent, PROMPT, "First paragraph!!", "x", null);

		assertError(answer, "does not occur");
		assertEquals(BODY, stored(agent));
	}

	/** An empty anchor against a value that is not empty addresses nothing, so it is refused. */
	public void testAnEmptyAnchorOnANonEmptyValueIsRefused() throws Exception {
		Agent agent = agentWith(BODY);

		assertError(edit(agent, PROMPT, "", "x", null), "anchor is empty");
		assertEquals(BODY, stored(agent));
	}

	// -- which attributes may be edited --

	/** A multi-valued property is a list, not a text, and the refusal says which. */
	public void testAMultiValuedAttributeIsRefused() throws Exception {
		Agent agent = agentWith(BODY);

		assertError(edit(agent, Agent.TOOLS_ATTR, "a", "b", null), "holds a list of values");
	}

	/** A reference names other objects, and the refusal says so rather than "not a text". */
	public void testAReferenceIsRefused() throws Exception {
		Screenshot shot = screenshot();

		assertError(edit(shot, Screenshot.OWNER_ATTR, "a", "b", null), "reference to other objects");
	}

	/** A date is not editable as text. */
	public void testADateAttributeIsRefused() throws Exception {
		Screenshot shot = screenshot();

		assertError(edit(shot, Screenshot.CAPTURED_AT_ATTR, "a", "b", null), "not a text");
	}

	/** Neither is a binary, whose Java value is not a string however it renders. */
	public void testABinaryAttributeIsRefused() throws Exception {
		Screenshot shot = screenshot();

		assertError(edit(shot, Screenshot.IMAGE_ATTR, "a", "b", null), "not a text");
	}

	/** An attribute the type does not have is a typo, and the refusal says where to look. */
	public void testAnUnknownAttributeIsRefused() throws Exception {
		Agent agent = agentWith(BODY);

		assertError(edit(agent, "noSuchAttribute", "a", "b", null), "has no attribute named");
	}

	/** Reading refuses the same attributes as editing: the pair is only useful if both agree. */
	public void testReadingRefusesTheSameAttributes() throws Exception {
		Agent agent = agentWith(BODY);

		assertError(read(agent, Agent.TOOLS_ATTR, null, null), "holds a list of values");
		assertError(read(agent, "noSuchAttribute", null, null), "has no attribute named");
	}

	/** A plain String property is editable too, not only a Text one. */
	public void testAStringAttributeIsEditable() throws Exception {
		Agent agent = agentWith(BODY);

		edit(agent, Agent.DESCRIPTION_ATTR, "old", "new", null);

		assertEquals("a new description", agent.getDescription());
	}

	// -- security --

	/** An object the principal may not write is refused, and the value stays as it was. */
	public void testAnObjectTheAccountMayNotWriteIsRefused() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(context(new DenyWrite(agent, null)), agent, PROMPT,
			"First paragraph.", "x", null);

		assertError(answer, "Permission denied");
		assertContains("The type is named, the withheld value is not.", "tl.ai.core:Agent", answer);
		assertEquals(BODY, stored(agent));
	}

	/**
	 * Model access is attribute-granular, so holding the object is not holding every property of it.
	 * A principal who may write the agent but not its prompt is refused the prompt.
	 */
	public void testAnAttributeTheAccountMayNotWriteIsRefused() throws Exception {
		Agent agent = agentWith(BODY);
		TLStructuredTypePart prompt = agent.tType().getPart(PROMPT);

		String answer = edit(context(new DenyWrite(agent, prompt)), agent, PROMPT,
			"First paragraph.", "x", null);

		assertError(answer, "Permission denied");
		assertEquals(BODY, stored(agent));
	}

	/**
	 * An object the principal may not read is refused like one that does not exist - the refusal must
	 * not disclose that it is there.
	 */
	public void testAnObjectTheAccountMayNotReadIsNotFound() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = edit(context(new DenyRead(agent)), agent, PROMPT, "First paragraph.", "x", null);

		assertError(answer, "not found");
		assertEquals(BODY, stored(agent));
	}

	/** Reading is refused the same way, and by the same check. */
	public void testReadingAnObjectTheAccountMayNotReadIsNotFound() throws Exception {
		Agent agent = agentWith(BODY);

		String answer = _tool.readText(idOf(agent), PROMPT, null, null,
			context(new DenyRead(agent)));

		assertError(answer, "not found");
	}

	/** An attribute the principal may not read is refused without disclosing its content. */
	public void testAnAttributeTheAccountMayNotReadIsRefused() throws Exception {
		Agent agent = agentWith(BODY);
		TLStructuredTypePart prompt = agent.tType().getPart(PROMPT);

		String answer = _tool.readText(idOf(agent), PROMPT, null, null,
			context(new DenyReadAttribute(agent, prompt)));

		assertError(answer, "Permission denied");
		assertFalse("The withheld value must not be quoted: " + answer,
			answer.contains("First paragraph."));
	}

	/** An omitted argument is not an empty one: a model that forgot 'newText' must not delete. */
	public void testAnOmittedArgumentIsRefused() throws Exception {
		Agent agent = agentWith(BODY);

		assertError(edit(agent, PROMPT, "First paragraph.", null, null), "'newText' is missing");
		assertError(edit(agent, PROMPT, null, "x", null), "'oldText' is missing");
		assertEquals(BODY, stored(agent));
	}

	/**
	 * What {@code readText} renders stays under the agent loop's own result limit.
	 *
	 * <p>
	 * A result the framework truncates ends in the middle of a line, and a model that anchors on what
	 * it was shown then quotes half a sentence as if it were whole. This tool cuts on a line boundary
	 * and says where it stopped, which only helps while its own bound is the smaller one.
	 * </p>
	 */
	public void testTheReadBoundStaysUnderTheFrameworksOwn() {
		assertTrue("The read bound must leave room for the framing and the envelope.",
			TextEditTool.MAX_READ_CHARS < AgentTuning.MAX_TOOL_RESULT_CHARS);
	}

	// -- what the model is offered --

	/**
	 * Both tools are discovered, under the names the shipped configuration offers to the agents.
	 *
	 * <p>
	 * The names are a wire contract: they stand in {@code tl-ai-core.conf.config.xml} as
	 * {@code text-edit.readText} and {@code text-edit.editText}, and renaming a method here would
	 * leave two agents referring to tools that no longer exist.
	 * </p>
	 */
	public void testBothToolsAreDiscovered() {
		assertNotNull(tool("readText"));
		assertNotNull(tool("editText"));
		assertEquals(2, AnnotatedTools.from(_tool).size());
	}

	/**
	 * The declared modes, which decide the approval and the transaction the framework gives the call.
	 *
	 * <p>
	 * {@link ToolMode} defaults to {@code STRUCTURAL} when a tool forgets to declare one, which would
	 * make the read ask for approval and classify the edit as a model change. Both are wrong in a way
	 * nothing else reports.
	 * </p>
	 */
	public void testTheDeclaredModes() {
		assertEquals(ToolMode.READ, tool("readText").mode());
		assertEquals(ToolMode.DATA_WRITE, tool("editText").mode());
		assertTrue("A write enrols in the turn's transaction.",
			tool("editText").mode().writesApplicationState());
	}

	/** The groups an administrator grants, which are the ones the other data tools are in. */
	public void testTheDeclaredGroups() {
		assertEquals(ToolGroups.DATA_READ, tool("readText").group());
		assertEquals(ToolGroups.DATA_WRITE, tool("editText").group());
	}

	/**
	 * The parameter names the model fills in.
	 *
	 * <p>
	 * They come from the compiled parameter names, so they are as much a wire contract as the tool
	 * names, and a rename would be invisible until a model's call stopped binding.
	 * </p>
	 */
	public void testTheParameterNames() {
		String edit = tool("editText").schema().inputSchemaJson();
		for (String parameter : List.of("objectId", "attribute", "oldText", "newText", "replaceAll")) {
			assertContains(parameter, edit);
		}
		String read = tool("readText").schema().inputSchemaJson();
		for (String parameter : List.of("objectId", "attribute", "fromLine", "toLine")) {
			assertContains(parameter, read);
		}
	}

	/** The optional parameters are optional, so a call that leaves them out is well-formed. */
	public void testTheOptionalParametersAreNotRequired() {
		assertFalse("replaceAll is a refinement, not a decision the model has to take.",
			tool("editText").schema().inputSchemaJson()
				.replaceAll("\\s", "").contains("\"required\":[\"objectId\",\"attribute\",\"oldText\","
					+ "\"newText\",\"replaceAll\"]"));
	}

	private Tool tool(String name) {
		for (Tool candidate : AnnotatedTools.from(_tool)) {
			if (name.equals(candidate.name())) {
				return candidate;
			}
		}
		fail("No tool named '" + name + "'.");
		return null;
	}

	// -- fixtures --

	private Agent agentWith(String prompt) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Agent agent = TlAiCoreFactory.getInstance().createAgent();
			agent.tUpdate(TlAiCoreFactory.getNameAgentAttr(), "test-agent-" + System.nanoTime());
			agent.setDescription("a old description");
			agent.setSystemPrompt(prompt);
			tx.commit();
			return agent;
		}
	}

	private Screenshot screenshot() {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			Screenshot shot = TlAiScreenshotsFactory.getInstance().createScreenshot();
			shot.setName("shot-" + System.nanoTime());
			shot.setImage(BinaryDataFactory.createBinaryData(new byte[] { 1 }, "image/png", "s.png"));
			tx.commit();
			return shot;
		}
	}

	/**
	 * Runs an edit inside a transaction of its own and commits it, which is what the agent loop does
	 * around a {@code DATA_WRITE} tool call.
	 */
	private String edit(TLObject target, String attribute, String oldText, String newText,
			Boolean replaceAll) {
		return edit(ToolContext.EMPTY, target, attribute, oldText, newText, replaceAll);
	}

	private String edit(ToolContext context, TLObject target, String attribute, String oldText,
			String newText, Boolean replaceAll) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			String answer =
				_tool.editText(idOf(target), attribute, oldText, newText, replaceAll, context);
			tx.commit();
			return answer;
		}
	}

	private String read(TLObject target, String attribute, Integer fromLine, Integer toLine) {
		return _tool.readText(idOf(target), attribute, fromLine, toLine, ToolContext.EMPTY);
	}

	private static String idOf(TLObject target) {
		return ReadFilter.idOf(target);
	}

	private static String stored(Agent agent) {
		return agent.getSystemPrompt();
	}

	private static ToolContext context(ModelSecurity security) {
		return new ToolContext(new SecurityContext() {

			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return security;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}
		}, ApprovalCallback.AUTO_APPROVE, TransactionScope.NOOP);
	}

	/** Everything except the named write. */
	private record DenyWrite(TLObject object, TLStructuredTypePart attribute)
			implements DelegatingSecurity {

		@Override
		public boolean canWrite(TLObject obj) {
			return attribute != null || !obj.equals(object);
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart part) {
			return !obj.equals(object) || !part.equals(attribute);
		}
	}

	/** Everything except reading the named object at all. */
	private record DenyRead(TLObject object) implements DelegatingSecurity {

		@Override
		public boolean canRead(TLObject obj) {
			return !obj.equals(object);
		}
	}

	/** Everything except reading the named attribute of the named object. */
	private record DenyReadAttribute(TLObject object, TLStructuredTypePart attribute)
			implements DelegatingSecurity {

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart part) {
			return !obj.equals(object) || !part.equals(attribute);
		}
	}

	/**
	 * Grants everything, so that a test subclass has to override only the one question it is about.
	 *
	 * <p>
	 * Not the engine's rights: what is under test here is that the tool <em>asks</em> and honours the
	 * answer. Whether the engine answers correctly for a given role model is
	 * {@code ModelAccessRightsSecurity}'s own business and is tested there.
	 * </p>
	 */
	private interface DelegatingSecurity extends ModelSecurity {

		@Override
		default boolean canRead(TLObject obj) {
			return true;
		}

		@Override
		default boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		default boolean canWrite(TLObject obj) {
			return true;
		}

		@Override
		default boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		default boolean canDelete(TLObject obj) {
			return true;
		}

		@Override
		default boolean canCreate(com.top_logic.model.TLClass type, TLObject context) {
			return true;
		}

		@Override
		default boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute, com.top_logic.model.TLClass type) {
			return true;
		}

		@Override
		default boolean canSetInitial(TLObject draft, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		default boolean canPerform(TLObject obj,
				com.top_logic.tool.boundsec.BoundCommandGroup operation) {
			return true;
		}

		@Override
		default <T extends TLObject> List<T> filterReadable(java.util.Collection<T> objects) {
			return List.copyOf(objects);
		}

		@Override
		default Set<com.top_logic.model.TLClass> accessibleTypes(
				com.top_logic.tool.boundsec.BoundCommandGroup operation) {
			return Set.of();
		}
	}

	private static void assertError(String answer, String expected) {
		assertTrue("Expected a refusal, got: " + answer, answer.startsWith("error: "));
		assertContains(expected, answer);
	}

	/** The knowledge base plus {@link ModelService}: these tests work on real model objects. */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestTextEditTool.class),
				ModelService.Module.INSTANCE));
	}
}

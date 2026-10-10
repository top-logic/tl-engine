/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore.SessionKind;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.context.DevelopmentWorkspace;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Albert's {@code TL_init} skill, run end to end: the shipped {@code TL_SingleAgent} loads the skill,
 * reads the existing context, reads a derived attribute's calculation rule and saves the application
 * context with the user's approval into the application's source configuration - and once the LLM
 * service is restarted, the next run of Albert is given it (Gitea #667).
 *
 * <p>
 * In tl-ai-eval because this module boots the agent stack the way a chat request meets it - the
 * shipped agent definitions, the tool registry, the tool-access gate, a knowledge base - with a model
 * whose answers a test writes. What the model answers is scripted; everything the answers set in
 * motion is the real thing. The application's source is a folder of the test's own: the test runs in
 * the test environment, where nothing may be written into a real source, so it says where the source
 * is with {@link DevelopmentWorkspace#setWebappForTests(Path)}. The restart is the one the service
 * editor does - reload the application configuration, restart the service - with the written file
 * read last, where a file of the application's {@code autoconf} folder is read.
 * </p>
 *
 * <p>
 * Whether a real model follows the skill's workflow is not something a scripted model can say; that
 * is the live check of the pull request.
 * </p>
 */
public class TestInitSkill extends BasicTestCase {

	private static final String ALBERT = "TL_SingleAgent";

	private static final String DERIVED = "initfixture:Item#total";

	/** The application's own configuration of the LLM service, which the test application has. */
	private static final String SERVICE_FILE = "com.top_logic.tl.ai.core.provider.LLMService.config.xml";

	/** What the save writes when the application has no configuration file of the service. */
	private static final String NEW_FILE = "ai-application-context.config.xml";

	/** A fragment of the calculation rule in {@code initfixture.model.xml}. */
	private static final String RULE = "$item.get(`initfixture:Item#price`) * $item.get(`initfixture:Item#quantity`)";

	private static final String DOCUMENT = "# Application context: Init fixture\n\n"
		+ "## Purpose\nPrices items.\n\n"
		+ "## Calculated values\n- `initfixture:Item#total`: what all units of an item cost together.";

	private static final String REVISED = DOCUMENT + "\n\n## Rules\n- An item is never sold below its price.";

	/** The tools that run in the order the script calls them. */
	private static final List<String> CALLS = List.of("loadSkill", "readApplicationContext", "loadSkill",
		"getCurrentAnnotations", "saveApplicationContext");

	private Path _project;

	private Path _autoconf;

	private final List<BinaryContent> _layers = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
		_project = Files.createTempDirectory("init-source");
		Path webapp = _project.resolve("src/main/webapp");
		_autoconf = Files.createDirectories(webapp.resolve("WEB-INF/autoconf"));
		DevelopmentWorkspace.setWebappForTests(webapp);
	}

	@Override
	protected void tearDown() throws Exception {
		DevelopmentWorkspace.setWebappForTests(null);
		if (!_layers.isEmpty()) {
			XMLProperties.Module.INSTANCE.config().getTypedConfigs().removeAll(_layers);
			_layers.clear();
			restartLlmService();
		}
		try (Stream<Path> files = Files.walk(_project)) {
			for (Path file : files.sorted(Comparator.reverseOrder()).toList()) {
				Files.delete(file);
			}
		}
		super.tearDown();
	}

	/**
	 * The whole workflow: every step runs, the rule is read, the save is the one call that needs an
	 * approval, it writes the application's configuration file and nothing of the application's data,
	 * and after the restart the next run is given the context.
	 */
	public void testInitWritesTheContextIntoTheSourceConfiguration() throws Exception {
		Path file = copyServiceFile();
		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(new TextContent("I will write the application context."),
				call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("readApplicationContext", "{}"));
			case 2 -> List.of(call("loadSkill", "{\"skillId\": \"TL_modelExploration\"}"));
			case 3 -> List.of(call("getCurrentAnnotations",
				"{\"targetRef\": \"" + DERIVED + "\", \"includeInherited\": true}"));
			case 4 -> List.of(call("saveApplicationContext", "{\"text\": " + json(DOCUMENT) + "}"));
			default -> List.of(new TextContent("Saved."));
		});
		List<ApprovalRequest> approvals = new ArrayList<>();
		long before = lastRevision();

		Run run = run("Write the application context.", request -> {
			approvals.add(request);
			return new ApprovalResult.Approved();
		});

		assertEquals(StopReason.COMPLETE, run.stopReason());
		List<ToolResultMessage> results = run.results();
		assertEquals(CALLS, results.stream().map(ToolResultMessage::toolName).toList());
		for (ToolResultMessage result : results) {
			assertTrue(result.toolName() + " failed: " + result.state(),
				result.state() instanceof ToolState.Completed);
		}
		assertTrue("Before anything is saved, the read says so: " + text(results.get(1)),
			text(results.get(1)).contains("No application context has been written yet."));
		assertTrue("The derived attribute's rule reaches the model: " + text(results.get(3)),
			text(results.get(3)).contains(RULE));

		assertEquals("Exactly one call needs a person's approval: the save.",
			List.of("saveApplicationContext"), approvals.stream().map(ApprovalRequest::toolName).toList());
		assertEquals("The person approves the text that is saved.",
			DOCUMENT, approvals.get(0).arguments().get("text"));
		String saved = text(results.get(4));
		assertTrue("The developer is told where it went, to restart and to commit: " + saved,
			saved.contains("src/main/webapp/WEB-INF/autoconf/" + SERVICE_FILE) && saved.contains("restarted")
				&& saved.contains("commit"));
		String written = read(file);
		assertEquals(1, occurrences(written, "name=\"application-context\""));
		assertTrue(written.contains(DOCUMENT));
		assertEquals("The run changed nothing of the application's data.", List.of(),
			applicationChangesSince(before));

		assertFalse("Saved is not active: the running service still has its old configuration.",
			nextPrompt().contains(DOCUMENT));
		activate(file);
		assertTrue("After the restart, the next run of Albert is given the context.",
			nextPrompt().contains(DOCUMENT));
	}

	/**
	 * A second run reads the context it wrote and replaces it: still one entry, with the new text.
	 */
	public void testASecondRunRevisesTheContext() throws Exception {
		Path file = copyServiceFile();
		saveWithScript(DOCUMENT);
		activate(file);

		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("readApplicationContext", "{}"));
			case 2 -> List.of(call("saveApplicationContext", "{\"text\": " + json(REVISED) + "}"));
			default -> List.of(new TextContent("Saved."));
		});
		Run run = run("Revise the application context.", ApprovalCallback.AUTO_APPROVE);

		assertTrue("The re-run reads what is there: " + text(run.results().get(1)),
			text(run.results().get(1)).contains(DOCUMENT));
		String written = read(file);
		assertEquals(1, occurrences(written, "name=\"application-context\""));
		assertTrue(written.contains(REVISED));
		restartLlmService();
		assertTrue(nextPrompt().contains(REVISED));
	}

	/**
	 * An application that configures the LLM service in no file of its own gets a file that adds the
	 * context to the service's configuration - and the rest of that configuration stays, which is
	 * why the next run of Albert still reaches its model.
	 */
	public void testWithoutAServiceFileTheContextIsAddedToTheConfiguration() throws Exception {
		saveWithScript(DOCUMENT);

		Path created = _autoconf.resolve(NEW_FILE);
		assertTrue(Files.exists(created));
		assertFalse(read(created).contains("config:override=\""));
		activate(created);

		assertEquals("eval-scripted", LLMService.getInstance().getConfig().getAgentExecution().getDefaultModel());
		assertTrue(nextPrompt().contains(DOCUMENT));
	}

	/**
	 * Outside the development environment there is no source to write to: the save is not offered,
	 * a call to it is answered with the reason, and nobody is asked to approve anything.
	 */
	public void testOutsideTheDevelopmentEnvironmentTheSaveIsNotOffered() throws Exception {
		Path file = copyServiceFile();
		String before = read(file);
		DevelopmentWorkspace.setWebappForTests(null);
		List<List<String>> offered = new ArrayList<>();
		ScriptedProvider.install((request, turn) -> {
			offered.add(request.tools().stream().map(ToolSchema::name).toList());
			return switch (turn) {
				case 0 -> List.of(call("loadSkill", "{\"skillId\": \"TL_init\"}"));
				case 1 -> List.of(call("saveApplicationContext", "{\"text\": " + json(DOCUMENT) + "}"));
				default -> List.of(new TextContent("Here is the document."));
			};
		});
		List<ApprovalRequest> approvals = new ArrayList<>();

		Run run = run("Write the application context.", request -> {
			approvals.add(request);
			return new ApprovalResult.Approved();
		});

		assertTrue(offered.get(1).contains("readApplicationContext"));
		assertFalse(offered.get(1).contains("saveApplicationContext"));
		String answer = text(run.results().get(1));
		assertTrue(answer, answer.contains("only offered in the development environment"));
		assertEquals(List.of(), approvals);
		assertEquals(before, read(file));
	}

	/**
	 * A person who declines the save keeps the configuration as it was.
	 */
	public void testDeclinedSaveWritesNothing() throws Exception {
		Path file = copyServiceFile();
		String before = read(file);
		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("saveApplicationContext", "{\"text\": " + json(DOCUMENT) + "}"));
			default -> List.of(new TextContent("What shall I change?"));
		});

		run("Write the application context.", request -> new ApprovalResult.Rejected("Not yet."));

		assertEquals(before, read(file));
	}

	private void saveWithScript(String document) {
		ScriptedProvider.install((request, turn) -> switch (turn) {
			case 0 -> List.of(call("loadSkill", "{\"skillId\": \"TL_init\"}"));
			case 1 -> List.of(call("saveApplicationContext", "{\"text\": " + json(document) + "}"));
			default -> List.of(new TextContent("Saved."));
		});
		Run run = run("Write the application context.", ApprovalCallback.AUTO_APPROVE);
		assertTrue(run.results().get(1).state() instanceof ToolState.Completed);
	}

	/**
	 * The application's file of the service, in the test's source folder.
	 */
	private Path copyServiceFile() throws IOException {
		Path file = _autoconf.resolve(SERVICE_FILE);
		try (InputStream in = FileManager.getInstance().getStream("/WEB-INF/autoconf/" + SERVICE_FILE)) {
			Files.copy(in, file);
		}
		return file;
	}

	/**
	 * Reads the given file last, as the application's {@code autoconf} files are read, and restarts
	 * the LLM service as the service editor does.
	 */
	private void activate(Path file) throws Exception {
		BinaryData layer = BinaryDataFactory.createBinaryDataWithName(file.toFile(), file.getFileName().toString());
		XMLProperties.Module.INSTANCE.config().getTypedConfigs().add(layer);
		_layers.add(layer);
		restartLlmService();
	}

	private static void restartLlmService() throws Exception {
		assertTrue("The configuration reloads.", ApplicationConfig.getInstance().reload());
		ModuleUtil.INSTANCE.restart(LLMService.Module.INSTANCE, null);
	}

	/**
	 * The system prompt of the next run of Albert.
	 */
	private static String nextPrompt() {
		List<String> prompts = new ArrayList<>();
		ScriptedProvider.install((request, turn) -> {
			prompts.add(request.systemPrompt());
			return List.of(new TextContent("Hello."));
		});
		run("Hello.", ApprovalCallback.AUTO_APPROVE);
		assertEquals(1, prompts.size());
		return prompts.get(0);
	}

	private record Run(StopReason stopReason, Conversation conversation) {

		List<ToolResultMessage> results() {
			List<ToolResultMessage> result = new ArrayList<>();
			for (Message message : conversation.messages()) {
				if (message instanceof ToolResultMessage toolResult) {
					result.add(toolResult);
				}
			}
			return result;
		}
	}

	private static Run run(String prompt, ApprovalCallback approval) {
		Person root = PersonManager.getManager().getRoot();
		CancellationToken cancellation = new CancellationToken();
		ToolContext context = new ToolContext(
			RunSecurity.contextFor(new RunPrincipal(root, RunEntryPoint.CHAT, false)),
			approval, new TransactionScope(cancellation), cancellation);
		Conversation conversation = new Conversation();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent(prompt))));
			Turn turn = env.logListener().log().startTurn();
			turn.setUserParts(List.of(new Part.TextPart(UUID.randomUUID().toString(), Instant.now(), prompt)));
			AgentRunner.RecordedRun run = AgentRunner.run(ALBERT, conversation, null, context, event -> {
				// Only the outcome matters here.
			}, null, env.logListener().log(),
				AgentExecutionStore.getInstance().createSession(SessionKind.SCRIPT, root, "TestInitSkill"), null);
			return new Run(run.result().stopReason(), conversation);
		} finally {
			context.transaction().close();
			HistoryUtils.updateSessionRevision();
		}
	}

	/**
	 * Everything committed since the given revision that is not the record of the run itself - which
	 * the execution store keeps in the tables of {@code tl.ai.execution}, on its own thread. A
	 * reference row counts as part of the object it belongs to.
	 */
	private static List<String> applicationChangesSince(long before) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Set<String> runTables = runRecordTables();
		List<ItemEvent> events = new ArrayList<>();
		ChangeSetReader reader = kb.getChangeSetReader(ReaderConfigBuilder.createConfig(
			HistoryUtils.getRevision(before + 1), Revision.CURRENT));
		try {
			for (ChangeSet changes = reader.read(); changes != null; changes = reader.read()) {
				events.addAll(changes.getCreations());
				events.addAll(changes.getUpdates());
				events.addAll(changes.getDeletions());
			}
		} finally {
			reader.close();
		}

		Set<Object> allowed = new HashSet<>();
		for (ItemEvent event : events) {
			if (runTables.contains(event.getObjectType().getName())) {
				allowed.add(event.getObjectName());
			}
		}
		List<String> result = new ArrayList<>();
		for (ItemEvent event : events) {
			Object owner = event.getObjectName();
			if (event instanceof ItemChange change && change.getValues().get("source") instanceof ObjectKey source) {
				owner = source.getObjectName();
			}
			if (!allowed.contains(owner)) {
				result.add(event.getObjectType().getName() + ":" + event.getObjectName());
			}
		}
		return result;
	}

	private static Set<String> runRecordTables() {
		Set<String> result = new HashSet<>();
		for (TLClass type : ModelService.getApplicationModel().getModule("tl.ai.execution").getClasses()) {
			String table = TLAnnotations.getTable(type);
			if (!TLAnnotations.GENERIC_TABLE_NAME.equals(table)) {
				result.add(table);
			}
		}
		return result;
	}

	private static long lastRevision() {
		HistoryUtils.updateSessionRevision();
		return HistoryUtils.getLastRevision().getCommitNumber();
	}

	private static String read(Path file) throws IOException {
		return Files.readString(file, StandardCharsets.UTF_8);
	}

	private static String text(ToolResultMessage result) {
		if (result.state() instanceof ToolState.Completed completed) {
			return completed.result();
		}
		if (result.state() instanceof ToolState.Failed failed) {
			return failed.error();
		}
		return "";
	}

	private static int occurrences(String text, String part) {
		int count = 0;
		for (int index = text.indexOf(part); index >= 0; index = text.indexOf(part, index + 1)) {
			count++;
		}
		return count;
	}

	private static ToolCall call(String tool, String arguments) {
		return new ToolCall("call-" + UUID.randomUUID(), tool, arguments);
	}

	private static String json(String text) {
		return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
	}

	/**
	 * The suite, on the services a chat request passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestInitSkill.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMService.Module.INSTANCE));
	}
}

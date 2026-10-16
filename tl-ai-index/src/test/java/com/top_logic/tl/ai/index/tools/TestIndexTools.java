/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.index.tools;

import static com.top_logic.tl.ai.index.config.IndexTestModel.*;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.index.AiIndex;
import com.top_logic.tl.ai.index.lucene.IndexFixture;
import com.top_logic.tl.ai.index.query.SearchFixture;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.util.model.ModelService;

/**
 * A model calls {@code search} and {@code similar} in a real agent run, and reads hits with their ids,
 * their passages and the page they are on - marked as business data.
 */
public class TestIndexTools extends BasicTestCase {

	private static final String CONFIG = """
		<index readable-scopes="all(`test.aiindex:Project`)">
			<chunking page-marker="^\\[p\\.(\\d+)\\]$"/>
			<types>
		""" + STATEMENT_CONFIG + MATERIAL_CONFIG + """
			</types>
		</index>
		""";

	/** The tool answers of the last run, by call id. */
	private final Map<String, String> _answers = new HashMap<>();

	/** A search and a similarity search: hits with id, passage, page; the start object left out. */
	public void testModelCallsBothTools() throws Exception {
		Person user = TestPerson.createPerson("tools-user");
		TLObject[] made = committed(() -> {
			TLObject project = create(PROJECT, Map.of("name", "Tools"));
			TLObject spec = create(MATERIAL, Map.of("name", "Pump specification", "project", project,
				"extractedText", "[p.1]\nIntroduction.\n[p.2]\nThe base plate is made of anodised aluminium."));
			TLObject twin = create(MATERIAL, Map.of("name", "Pump datasheet", "project", project,
				"extractedText", "[p.1]\nA base plate of anodised aluminium carries the pump."));
			return new TLObject[] { project, spec, twin };
		});
		Path dir = IndexFixture.tempDir("tl-ai-index-tools");
		AiIndex index = SearchFixture.start(CONFIG, dir);
		try {
			ModelSecurity rights = new SearchFixture.Rights().read(List.of(made));
			SearchFixture.asUser(user, () -> {
				run(index, rights,
					new ToolCall("c-search", "search", "{\"query\": \"anodised base plate\", \"types\": [\"test.aiindex:Material\"]}"),
					new ToolCall("c-similar", "similar", "{\"id\": \"" + ReadFilter.idOf(made[1]) + "\"}"));
				return null;
			});
		} finally {
			index.stop();
			IndexFixture.deleteRecursively(dir);
		}

		String search = _answers.get("c-search");
		assertTrue(search, search.contains("[id:" + ReadFilter.idOf(made[1]) + "]"));
		assertTrue(search, search.contains("page 2"));
		assertTrue(search, search.contains("The base plate is made of anodised aluminium."));
		assertTrue("The answer is marked as business data: " + search, search.contains("<untrusted_"));

		String similar = _answers.get("c-similar");
		assertTrue(similar, similar.contains("[id:" + ReadFilter.idOf(made[2]) + "]"));
		assertFalse("The start object is no hit: " + similar,
			similar.contains("1. Pump specification [id:"));
	}

	private void run(AiIndex index, ModelSecurity rights, ToolCall... calls) {
		ToolCatalog catalog = new ToolCatalog();
		for (Tool tool : AnnotatedTools.from(new IndexTools(() -> index))) {
			catalog.register(tool);
		}
		catalog.freeze();
		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Wo steht etwas zur Grundplatte?"))));
		AgentLoop loop = new AgentLoop(new AgentParams("test-model", "You search for the user.", 6,
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS), new ScriptedModel(calls), catalog);
		loop.run(conversation, new ToolContext(security(rights), ApprovalCallback.AUTO_APPROVE), event -> {
			// The tool results in the conversation are what matters.
		});
		for (Message message : conversation.messages()) {
			if (message instanceof ToolResultMessage result) {
				if (result.state() instanceof ToolState.Completed completed) {
					_answers.put(result.toolCallId(), completed.result());
				} else {
					fail("The call " + result.toolCallId() + " failed: " + result.state());
				}
			}
		}
		assertEquals(calls.length, _answers.size());
	}

	private static SecurityContext security(ModelSecurity rights) {
		return new SecurityContext() {
			@Override
			public boolean hasToolPermission(Tool tool) {
				return true;
			}

			@Override
			public ExecutionMode executionMode() {
				return ExecutionMode.DEVELOPMENT;
			}

			@Override
			public ModelSecurity modelSecurity() {
				return rights;
			}
		};
	}

	/** A model that makes one call per turn and then answers with text. */
	private static final class ScriptedModel implements LLMProvider {

		private final ToolCall[] _calls;

		private int _turn;

		ScriptedModel(ToolCall... calls) {
			_calls = calls;
		}

		@Override
		public void stream(ProviderRequest request, StreamHandler handler) {
			int turn = _turn++;
			List<ContentPart> content = new ArrayList<>();
			handler.onEvent(new StreamEvent.StreamStart());
			if (turn < _calls.length) {
				ToolCall call = _calls[turn];
				content.add(call);
				handler.onEvent(new StreamEvent.ToolCallStart(0, call.id(), call.name()));
				handler.onEvent(new StreamEvent.ToolCallDelta(0, call.arguments()));
				handler.onEvent(new StreamEvent.ToolCallEnd(0));
			} else {
				content.add(new TextContent("Done."));
				handler.onEvent(new StreamEvent.TextStart(0));
				handler.onEvent(new StreamEvent.TextDelta(0, "Done."));
				handler.onEvent(new StreamEvent.TextEnd(0));
			}
			handler.onEvent(new StreamEvent.MessageComplete("scripted-" + turn, request.model(),
				turn < _calls.length ? "tool_use" : "end_turn", TokenUsage.EMPTY, content));
		}
	}

	/** The knowledge base with the test model and accounts. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestIndexTools.class), ModelService.Module.INSTANCE));
	}

}

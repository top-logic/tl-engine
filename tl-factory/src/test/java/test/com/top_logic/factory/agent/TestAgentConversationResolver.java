/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.AgentConversationResolver;
import com.top_logic.factory.agent.StoredConversation;
import com.top_logic.factory.agent.WorkspaceAgent;
import com.top_logic.factory.agent.WorkspaceAgentService;
import com.top_logic.factory.agent.claude.ClaudeCodeHarness;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.ProjectWorkspaces.NewWorkspace;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link AgentConversationResolver}: the chats about an agent conversation are answered by
 * the agent of its workspace, all others by the agent of the control application; the
 * conversations go with their workspace.
 */
public class TestAgentConversationResolver extends BasicTestCase {

	private static final String WORKSPACE_ID = "agent-resolver-test-1";

	private static final String ARTIFACT_ID = "agent-resolver-test";

	private TLObject _project;

	private TLObject _workspace;

	private TLObject _conversation;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "Agent resolver test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, ARTIFACT_ID);
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
		NewWorkspace created = ProjectWorkspaces.commitNewWorkspace(_project, WORKSPACE_ID);
		_workspace = created.workspace();
		_conversation = commit(() -> FactoryModel.createConversation(_workspace, "Resolve me."));
	}

	@Override
	protected void tearDown() throws Exception {
		commit(() -> {
			if (_workspace.tValid()) {
				_workspace.tDelete();
			}
			_project.tDelete();
			return null;
		});
		super.tearDown();
	}

	/**
	 * A chat about a conversation is answered by the agent of its workspace, always the same for
	 * the same conversation.
	 */
	public void testClaimsConversation() {
		AgentConversationResolver resolver = new AgentConversationResolver(null, null);
		ConversationBackend backend = resolver.backendFor(session(_conversation));
		assertTrue("Backend: " + backend, backend instanceof WorkspaceAgent);
		WorkspaceAgent agent = (WorkspaceAgent) backend;
		assertEquals(ClaudeCodeHarness.AGENT_NAME, agent.agentName());
		assertEquals(WORKSPACE_ID, agent.workspaceId());
		assertSame(agent, resolver.backendFor(session(_conversation)));

		StoredConversation target = (StoredConversation) agent.getConversation();
		assertEquals(ApplicationWorkspaces.appDir(ARTIFACT_ID), target.workingDir());
		assertEquals(_project, target.displayName());
		assertNull(target.sessionId());
		target.storeSessionId("session-1");
		assertEquals("session-1", FactoryModel.getSessionId(_conversation));
		assertNotNull(_conversation.tValueByName(FactoryModel.CONVERSATION_CREATED_AT));
	}

	/**
	 * A chat about anything else, or about nothing, is not claimed.
	 */
	public void testIgnoresOtherSubjects() {
		AgentConversationResolver resolver = new AgentConversationResolver(null, null);
		assertNull(resolver.backendFor(session(_workspace)));
		assertNull(resolver.backendFor(session(_project)));
		assertNull(resolver.backendFor(session(null)));
	}

	/**
	 * The conversations of a workspace are deleted with it and claimed no more.
	 */
	public void testDeletedWithWorkspace() {
		assertEquals(FactoryModel.getConversationWorkspace(_conversation), _workspace);
		WorkspaceAgentService.getInstance().stopAll(WORKSPACE_ID);
		commit(() -> {
			_workspace.tDelete();
			return null;
		});
		assertFalse(_conversation.tValid());
		assertNull(new AgentConversationResolver(null, null).backendFor(session(_conversation)));
	}

	/**
	 * A chat session about the given subject, as far as the resolver reads it.
	 */
	private static AgentSession session(TLObject subject) {
		return (AgentSession) Proxy.newProxyInstance(TestAgentConversationResolver.class.getClassLoader(),
			new Class<?>[] { AgentSession.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getSubject":
						return subject;
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					case "toString":
						return "Session about " + subject;
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	private static <T> T commit(Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("test"))) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * The suite.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentConversationResolver.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				WorkspaceService.Module.INSTANCE,
				LLMProxyService.Module.INSTANCE,
				WorkspaceAgentService.Module.INSTANCE));
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.ai.chat.backend.ConversationBackend;
import com.top_logic.ai.chat.backend.ConversationBackendService;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.ChatSessionRegistry;
import com.top_logic.ai.chat.layout.ChatStart;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.agent.AgentTasks;
import com.top_logic.factory.agent.I18NConstants;
import com.top_logic.factory.agent.WorkspaceAgent;
import com.top_logic.factory.agent.WorkspaceAgentService;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.WorkspaceStatus;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link AgentTasks}, the server side of "Give a task": whether the agent can work in a
 * workspace, the conversation a task creates, and the chat started about it, whose first message is
 * answered by the coding agent of the workspace rather than by the agent of the control
 * application.
 */
public class TestAgentTasks extends BasicTestCase {

	private static final String WORKSPACE_ID = "agent-tasks-test-1";

	private static final long TIMEOUT_MILLIS = 60_000;

	private TLObject _project;

	private TLObject _workspace;

	private Person _owner;

	private Person _previousPerson;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_owner = PersonManager.getManager().getRoot();
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		_previousPerson = subSession.getPerson();
		subSession.setPerson(_owner);
		_project = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "Agent tasks test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, "agent-tasks-test");
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
		_workspace = ProjectWorkspaces.commitNewWorkspace(_project, WORKSPACE_ID).workspace();
	}

	@Override
	protected void tearDown() throws Exception {
		WorkspaceAgentService.getInstance().stopAll(WORKSPACE_ID);
		commit(() -> {
			_project.tDelete();
			return null;
		});
		TLContextManager.getSubSession().setPerson(_previousPerson);
		super.tearDown();
	}

	/**
	 * The agent works only in a running workspace on the current image; the reasons name the
	 * project.
	 */
	public void testUnavailable() {
		assertEquals(I18NConstants.ERROR_NO_WORKSPACE, AgentTasks.unavailable(null));
		assertEquals(I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE.fill("Agent tasks test"),
			AgentTasks.unavailable(_workspace));

		setStatus(WorkspaceStatus.RUNNING);
		assertEquals(I18NConstants.ERROR_AGENT_TOOLS_OUTDATED__WORKSPACE.fill("Agent tasks test"),
			AgentTasks.unavailable(_workspace));

		setCurrentImage();
		assertNull(AgentTasks.unavailable(_workspace));
	}

	/** A task creates a conversation of the workspace, but only where the agent can work. */
	public void testCreateConversation() {
		try {
			AgentTasks.createConversation(_workspace, "Do it.");
			fail("A conversation in a stopped workspace was created.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE.fill("Agent tasks test"),
				ex.getErrorKey());
		}
		makeAvailable();
		try {
			AgentTasks.createConversation(_workspace, "  ");
			fail("An empty task was accepted.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_EMPTY_TASK, ex.getErrorKey());
		}

		TLObject conversation = AgentTasks.createConversation(_workspace, " Lege AGENT.md an. ");
		assertTrue(FactoryModel.isConversation(conversation));
		assertEquals(_workspace, FactoryModel.getConversationWorkspace(conversation));
		assertEquals("Lege AGENT.md an.", conversation.tValueByName(FactoryModel.CONVERSATION_TASK));
		assertNotNull(conversation.tValueByName(FactoryModel.CONVERSATION_CREATED_AT));
		assertEquals(List.of(conversation), _workspace.tValueByName(FactoryModel.WORKSPACE_CONVERSATIONS));
	}

	/**
	 * The chat started about the conversation is about it from its start, and its first message is
	 * answered by the coding agent of the workspace. The workspace has no container, so the agent
	 * ends the run in an error.
	 */
	public void testFirstMessageIsAnsweredByTheWorkspaceAgent() throws Exception {
		makeAvailable();
		TLObject conversation = AgentTasks.createConversation(_workspace, "Lege AGENT.md an.");
		TLSubSessionContext window = TLContextManager.getSubSession();

		AgentSession session = ChatStart.start(window, "workspace-agent", _owner, conversation,
			(String) conversation.tValueByName(FactoryModel.CONVERSATION_TASK));

		assertEquals(conversation, session.getSubject());
		ConversationBackend backend = ConversationBackendService.backendFor(session);
		assertTrue("Backend: " + backend, backend instanceof WorkspaceAgent);
		assertEquals(WORKSPACE_ID, ((WorkspaceAgent) backend).workspaceId());

		TaskProgressTracker tracker = ChatSessionRegistry.tracker(window, "workspace-agent");
		assertNotNull("The chat of the window is not registered.", tracker);
		ChatProcessor.AIProcessingState run = tracker.getProcessingStateForSession(
			session.tHandle().getObjectName().toExternalForm());
		assertNotNull("No run was started.", run);
		long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
		while (ChatProcessor.isProcessing(tracker)) {
			assertTrue("The run did not end.", System.currentTimeMillis() < deadline);
			TimeUnit.MILLISECONDS.sleep(20);
		}
		// The workspace has no container: the agent of the workspace refuses to start its process,
		// which the agent of the control application would never do.
		Throwable error = run.getError();
		assertNotNull("The run did not reach the agent of the workspace.", error);
		assertTrue("Not ended by the agent of the workspace: " + error, ranIn(error, WorkspaceAgent.class));
	}

	private static boolean ranIn(Throwable error, Class<?> type) {
		for (StackTraceElement frame : error.getStackTrace()) {
			if (frame.getClassName().equals(type.getName())) {
				return true;
			}
		}
		return false;
	}

	private void makeAvailable() {
		setStatus(WorkspaceStatus.RUNNING);
		setCurrentImage();
	}

	private void setStatus(WorkspaceStatus status) {
		commit(() -> {
			FactoryModel.setStatus(_workspace, status);
			return null;
		});
	}

	private void setCurrentImage() {
		String image = WorkspaceService.getInstance().getApplications().currentImage();
		commit(() -> {
			_workspace.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, image);
			return null;
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
	 * The suite, on the services of the control application and those a chat run passes through.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestAgentTasks.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				WorkspaceService.Module.INSTANCE,
				LLMProxyService.Module.INSTANCE,
				WorkspaceAgentService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE,
				ConversationBackendService.Module.INSTANCE));
	}

}

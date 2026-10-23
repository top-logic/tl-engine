/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.ProjectWorkspaces.NewWorkspace;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.access.SecretTokens;
import com.top_logic.util.model.ModelService;

/**
 * Test of the token a new workspace gets for the LLM proxy, against a knowledge base: the stored
 * hash authenticates the token until the workspace is removed.
 */
public class TestWorkspaceLLMToken extends BasicTestCase {

	private static final String PROXY_URL = "http://llm-proxy.test/llm-proxy";

	/**
	 * A stored workspace authenticates its token, no other one, and nothing once it is removed.
	 */
	public void testTokenAuthenticatesUntilWorkspaceRemoved() {
		TLObject project = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "LLM token test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, "llm-token-test");
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
		try {
			NewWorkspace created = ProjectWorkspaces.commitNewWorkspace(project, "llm-token-test-1");
			TLObject workspace = created.workspace();
			assertSame(workspace, FactoryModel.getWorkspace(project));

			String plain = created.spec().getEnv().get(LLMProxyService.ENV_AUTH_TOKEN);
			assertNotNull("The container gets a token.", plain);
			assertEquals(PROXY_URL, created.spec().getEnv().get(LLMProxyService.ENV_BASE_URL));
			assertEquals("Only the hash is stored.", SecretTokens.hash(plain),
				FactoryModel.getString(workspace, FactoryModel.WORKSPACE_LLM_TOKEN_HASH));

			LLMProxyService proxy = LLMProxyService.getInstance();
			assertTrue(proxy.authenticate(plain));
			assertFalse("Another well-formed token.",
				proxy.authenticate(SecretTokens.newToken(LLMProxyService.TOKEN_PREFIX)));

			commit(() -> {
				workspace.tDelete();
				return null;
			});
			assertFalse("The token of a removed workspace.", proxy.authenticate(plain));
		} finally {
			commit(() -> {
				project.tDelete();
				return null;
			});
		}
	}

	private static <T> T commit(Supplier<T> change) {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(ResKey.text("Test"))) {
			T result = change.get();
			tx.commit();
			return result;
		}
	}

	/**
	 * The tests of this class, with a knowledge base holding the model of the control application
	 * and the LLM proxy switched on by the test configuration.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestWorkspaceLLMToken.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				// The default engine version of a new project; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE,
				LLMProxyService.Module.INSTANCE));
	}

}

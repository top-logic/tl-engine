/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.time.Duration;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.tenant.TestTenants;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.ci.CIRoute;
import com.top_logic.factory.git.GitRoute;
import com.top_logic.factory.llm.LLMRoute;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.ProjectWorkspaces.NewWorkspace;
import com.top_logic.factory.proxy.ProxyAdmission;
import com.top_logic.factory.proxy.TokenGrants;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.access.SecretTokens;
import com.top_logic.util.model.ModelService;

/**
 * Test of the tokens of the workspace proxy against a knowledge base: a token issued for a stored
 * workspace is valid until it is revoked, its lifetime ends or its workspace is removed.
 */
public class TestWorkspaceLLMToken extends BasicTestCase {

	/**
	 * The address the requests of the test come from.
	 */
	private static final String SOURCE = "172.30.0.5";

	/**
	 * The URL of the LLM route: the proxy URL of the test configuration and the path of the route.
	 */
	private static final String PROXY_URL = "http://workspace-proxy.test" + LLMRoute.DEFAULT_PATH;

	private static final String WORKSPACE_ID = "llm-token-test-1";

	private TLObject _project;

	private TLObject _workspace;

	private WorkspaceProxyService _proxy;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_project = commit(() -> {
			TLObject result = TestTenants.newProject();
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "LLM token test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, "llm-token-test");
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
		NewWorkspace created = ProjectWorkspaces.commitNewWorkspace(_project, WORKSPACE_ID);
		_workspace = created.workspace();
		assertEquals(PROXY_URL, created.spec().getEnv().get(LLMRoute.ENV_BASE_URL));
		assertNull("The container gets no token.", created.spec().getEnv().get(LLMRoute.ENV_AUTH_TOKEN));
		_proxy = WorkspaceProxyService.getInstance();
	}

	@Override
	protected void tearDown() throws Exception {
		_proxy.revokeAll(WORKSPACE_ID);
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
	 * The proxy of the application configuration has the LLM route, the git route and the CI route,
	 * also with the proxy settings of the test configuration.
	 */
	public void testLLMRouteConfigured() {
		assertEquals(3, _proxy.routes().size());
		assertEquals(LLMRoute.DEFAULT_PATH, _proxy.getRoute(LLMRoute.class).getPath());
		assertEquals(GitRoute.DEFAULT_PATH, _proxy.getRoute(GitRoute.class).getPath());
		assertEquals(CIRoute.DEFAULT_PATH, _proxy.getRoute(CIRoute.class).getPath());
	}

	/**
	 * An issued token is valid, another well-formed one is not, a revoked one no longer.
	 */
	public void testIssueAndRevoke() {
		String token = _proxy.issue(WORKSPACE_ID, Duration.ofMinutes(5));
		assertTrue(admitted(token));
		assertFalse("Another well-formed token.",
			admitted(SecretTokens.newToken(TokenGrants.TOKEN_PREFIX)));

		String other = _proxy.issue(WORKSPACE_ID, Duration.ofMinutes(5));
		_proxy.revoke(token);
		assertFalse("Revoked.", admitted(token));
		assertTrue("Other runs keep their tokens.", admitted(other));
	}

	/**
	 * A token is not valid after its lifetime.
	 */
	public void testExpired() throws InterruptedException {
		String token = _proxy.issue(WORKSPACE_ID, Duration.ofMillis(100));
		assertTrue(admitted(token));
		Thread.sleep(300);
		assertFalse("Expired.", admitted(token));
	}

	/**
	 * A token is not valid once its workspace is removed, and a token for an unknown workspace
	 * never.
	 */
	public void testWorkspaceRemoved() {
		String token = _proxy.issue(WORKSPACE_ID, Duration.ofMinutes(5));
		String unknown = _proxy.issue("no-such-workspace", Duration.ofMinutes(5));
		assertFalse("Unknown workspace.", admitted(unknown));

		commit(() -> {
			_workspace.tDelete();
			return null;
		});
		assertFalse("Workspace removed.", admitted(token));
	}

	/**
	 * Revoking the tokens of a workspace, as its removal does.
	 */
	public void testRevokeAll() {
		String first = _proxy.issue(WORKSPACE_ID, Duration.ofMinutes(5));
		String second = _proxy.issue(WORKSPACE_ID, Duration.ofMinutes(5));
		_proxy.revokeAll(WORKSPACE_ID);
		assertFalse(admitted(first));
		assertFalse(admitted(second));
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
	 * and the workspace proxy switched on by the test configuration.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestWorkspaceLLMToken.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				// The default engine version of a new project; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE,
				WorkspaceProxyService.Module.INSTANCE));
	}

	/**
	 * Whether the proxy admits a request with the given token.
	 */
	private boolean admitted(String token) {
		return _proxy.admit(token, SOURCE).status() == ProxyAdmission.Status.ADMITTED;
	}

}

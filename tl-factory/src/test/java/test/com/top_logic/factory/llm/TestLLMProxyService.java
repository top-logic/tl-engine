/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.llm;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.factory.llm.LLMProxyService;
import com.top_logic.factory.llm.LLMProxyService.WorkspaceToken;
import com.top_logic.factory.workspace.ExecCommand;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.ExecProcess;
import com.top_logic.factory.workspace.ImageContext;
import com.top_logic.factory.workspace.LogFollower;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceProvider;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationSpec;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.tl.ai.core.access.SecretTokens;

/**
 * Test of the tokens {@link LLMProxyService} issues for new workspaces and of the environment the
 * workspace container gets for the proxy.
 */
public class TestLLMProxyService extends TestCase {

	private static final String PROXY_URL = "http://host.docker.internal:8080/tl-factory-app/llm-proxy";

	/**
	 * A token has the format of a workspace token, its hash is the one stored, and token and URL of
	 * the proxy are in the environment of the created workspace container - nothing else of it.
	 */
	public void testTokenInContainerEnvironment() throws Exception {
		LLMProxyService service = service(PROXY_URL);
		assertTrue(service.isEnabled());

		WorkspaceToken token = service.issueToken();
		assertTrue(token.plain(), SecretTokens.hasFormat(LLMProxyService.TOKEN_PREFIX, token.plain()));
		assertEquals(SecretTokens.hash(token.plain()), token.hash());
		assertFalse("The plain token is not printed.", token.toString().contains(token.plain()));
		assertFalse("Every workspace gets a token of its own.", token.plain().equals(service.issueToken().plain()));

		RecordingProvider provider = new RecordingProvider();
		ApplicationWorkspaces applications =
			new ApplicationWorkspaces(TypedConfiguration.newConfigItem(ApplicationWorkspaces.Config.class), provider);
		applications.create(new ApplicationSpec("my-app-1234", "com.example", "my-app")
			.setInitialPassword("initial")
			.putEnv(service.containerEnv(token)), ExecOutput.NONE);

		WorkspaceSpec created = provider.created("my-app-1234");
		Map<String, String> env = created.getEnv();
		assertEquals(PROXY_URL, env.get(LLMProxyService.ENV_BASE_URL));
		assertEquals(token.plain(), env.get(LLMProxyService.ENV_AUTH_TOKEN));
		assertEquals("initial", env.get(ApplicationWorkspaces.ENV_INITIAL_PASSWORD));
		assertFalse("The stored hash is not passed to the container.", env.containsValue(token.hash()));

		WorkspaceSpec resolver = provider.created(
			ApplicationWorkspaces.RESOLVER_WORKSPACE_PREFIX + ApplicationWorkspaces.releaseVolume(
				applications.getConfig().getEngineVersion()));
		assertNull("Only the workspace of the application gets a token.",
			resolver.getEnv().get(LLMProxyService.ENV_AUTH_TOKEN));
	}

	/**
	 * Without proxy URL, the proxy is switched off and issues no tokens.
	 */
	public void testDisabledWithoutUrl() {
		LLMProxyService service = service("");
		assertFalse(service.isEnabled());
		assertNull(service.issueToken());
	}

	/**
	 * A token that is not a workspace token is refused without a lookup.
	 */
	public void testForeignTokenRefused() {
		LLMProxyService service = service(PROXY_URL);
		assertFalse(service.authenticate(null));
		assertFalse(service.authenticate(""));
		assertFalse(service.authenticate(SecretTokens.newToken("tlpat_")));
	}

	private static LLMProxyService service(String proxyUrl) {
		LLMProxyService.Config<?> config = TypedConfiguration.newConfigItem(LLMProxyService.Config.class);
		TypedConfigUtil.setProperty(config, LLMProxyService.Config.PROXY_URL, proxyUrl);
		return new LLMProxyService(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

	/**
	 * {@link WorkspaceProvider} recording the created workspaces and running every command
	 * successfully.
	 */
	private static final class RecordingProvider implements WorkspaceProvider {

		private final List<WorkspaceSpec> _created = new ArrayList<>();

		WorkspaceSpec created(String workspaceId) {
			for (WorkspaceSpec spec : _created) {
				if (spec.getWorkspaceId().equals(workspaceId)) {
					return spec;
				}
			}
			throw new AssertionError("Not created: " + workspaceId);
		}

		@Override
		public boolean isAvailable() {
			return true;
		}

		@Override
		public void ensureImage(String image) {
			// Always there.
		}

		@Override
		public String buildImage(String repository, ImageContext context, ExecOutput output) {
			return repository + ":test";
		}

		@Override
		public void ensureSharedVolume(String name) {
			// Always there.
		}

		@Override
		public List<String> listSharedVolumes() {
			return Collections.emptyList();
		}

		@Override
		public void removeSharedVolume(String name) {
			// Nothing to remove.
		}

		@Override
		public WorkspaceInfo create(WorkspaceSpec spec) {
			_created.add(spec);
			return info(spec.getWorkspaceId(), WorkspaceState.CREATED);
		}

		@Override
		public WorkspaceInfo start(String workspaceId) {
			return info(workspaceId, WorkspaceState.RUNNING);
		}

		@Override
		public WorkspaceInfo stop(String workspaceId) {
			return info(workspaceId, WorkspaceState.STOPPED);
		}

		@Override
		public void remove(String workspaceId) {
			// Nothing to remove.
		}

		@Override
		public WorkspaceInfo status(String workspaceId) {
			return WorkspaceInfo.missing(workspaceId, Collections.emptyList());
		}

		@Override
		public List<WorkspaceInfo> list() {
			return Collections.emptyList();
		}

		@Override
		public List<String> cleanup(Collection<String> keep) {
			return Collections.emptyList();
		}

		@Override
		public int exec(String workspaceId, ExecCommand command, ExecOutput output) {
			return 0;
		}

		@Override
		public ExecProcess startExec(String workspaceId, ExecCommand command, ExecOutput output) {
			throw new UnsupportedOperationException();
		}

		@Override
		public LogFollower followLog(String workspaceId, int tailLines, Instant since, ExecOutput output) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void close() {
			// Nothing to release.
		}

		private static WorkspaceInfo info(String id, WorkspaceState state) {
			return new WorkspaceInfo(id, state, "container-" + id, "image", WorkspaceInfo.NO_PORT, null,
				Collections.emptyList());
		}
	}

	/**
	 * The tests of this class, with the type index the configuration needs.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestLLMProxyService.class, TypeIndex.Module.INSTANCE);
	}

}

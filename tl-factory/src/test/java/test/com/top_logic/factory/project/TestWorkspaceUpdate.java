/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.factory.workspace.RecordingWorkspaceProvider;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.factory.llm.LLMRoute;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.factory.project.ProjectWorkspaces;
import com.top_logic.factory.project.ProjectWorkspaces.NewWorkspace;
import com.top_logic.factory.project.ProjectWorkspaces.WorkspaceUpdate;
import com.top_logic.factory.proxy.WorkspaceProxyService;
import com.top_logic.factory.workspace.ExecOutput;
import com.top_logic.factory.workspace.VolumeMount;
import com.top_logic.factory.workspace.WorkspaceInfo;
import com.top_logic.factory.workspace.WorkspaceService;
import com.top_logic.factory.workspace.WorkspaceSpec;
import com.top_logic.factory.workspace.WorkspaceState;
import com.top_logic.factory.workspace.app.ApplicationWorkspaces;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * Test of the bookkeeping of updating a workspace to the current workspace image, against a
 * knowledge base and a {@link RecordingWorkspaceProvider}: the stored image, the outdated flag, the
 * kept initial password and the URL of the LLM proxy.
 */
public class TestWorkspaceUpdate extends BasicTestCase {

	private static final String WORKSPACE_ID = "update-test-1";

	private static final String OLD_IMAGE = "tl-factory-workspace:0000000000000000";

	/**
	 * A created workspace runs the current image; after the image changed, the update creates the
	 * container anew on the same volumes with the initial password and the URL of the LLM proxy.
	 */
	public void testUpdate() throws Exception {
		TLObject project = commit(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
			result.tUpdateByName(FactoryModel.PROJECT_NAME, "Update test");
			result.tUpdateByName(FactoryModel.PROJECT_GROUP_ID, "com.example");
			result.tUpdateByName(FactoryModel.PROJECT_ARTIFACT_ID, "update-test");
			result.tUpdateByName(FactoryModel.PROJECT_ENGINE_VERSION, "8.0.0");
			return result;
		});
		try {
			RecordingWorkspaceProvider provider = new RecordingWorkspaceProvider();
			ApplicationWorkspaces applications = new ApplicationWorkspaces(
				WorkspaceService.getInstance().getApplications().getConfig(), provider);
			NewWorkspace created = ProjectWorkspaces.commitNewWorkspace(project, WORKSPACE_ID);
			TLObject workspace = created.workspace();
			assertFalse("A workspace without container is not outdated.", ProjectWorkspaces.isImageOutdated(workspace));

			WorkspaceInfo createdInfo = applications.create(created.spec(), ExecOutput.NONE);
			commit(() -> workspace.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, createdInfo.getImage()));
			assertEquals(applications.currentImage(), createdInfo.getImage());
			assertFalse("A created workspace runs the current image.", ProjectWorkspaces.isImageOutdated(workspace));

			commit(() -> workspace.tUpdateByName(FactoryModel.WORKSPACE_IMAGE, OLD_IMAGE));
			assertTrue(ProjectWorkspaces.isImageOutdated(workspace));

			WorkspaceUpdate update = ProjectWorkspaces.prepareUpdate(workspace);
			assertEquals(created.spec().getInitialPassword(), update.spec().getInitialPassword());
			assertFalse("The password is not printed.", update.toString().contains(update.spec().getInitialPassword()));

			WorkspaceInfo updated = applications.update(update.spec(), ExecOutput.NONE);
			assertEquals(WorkspaceState.CREATED, updated.getState());
			assertEquals(applications.currentImage(), updated.getImage());
			assertEquals(createdInfo.getVolumes(), updated.getVolumes());

			List<WorkspaceSpec> containers = provider.allCreated(WORKSPACE_ID);
			assertEquals("One container created anew.", 2, containers.size());
			WorkspaceSpec first = containers.get(0);
			WorkspaceSpec second = containers.get(1);
			assertEquals(mountPaths(first), mountPaths(second));
			assertTrue("The agent's conversations survive the update.",
				mountPaths(second).contains(ApplicationWorkspaces.AGENT_SESSIONS_PATH));
			Map<String, String> env = second.getEnv();
			assertEquals(created.spec().getInitialPassword(), env.get(ApplicationWorkspaces.ENV_INITIAL_PASSWORD));
			assertNotNull(env.get(LLMRoute.ENV_BASE_URL));
			assertEquals(first.getEnv().get(LLMRoute.ENV_BASE_URL), env.get(LLMRoute.ENV_BASE_URL));
			assertFalse("No container gets a token.", env.containsKey(LLMRoute.ENV_AUTH_TOKEN)
				|| first.getEnv().containsKey(LLMRoute.ENV_AUTH_TOKEN));
			assertEquals(first.getEnv().get(ApplicationWorkspaces.ENV_APP_DIR), env.get(ApplicationWorkspaces.ENV_APP_DIR));

			commit(() -> ProjectWorkspaces.recordUpdate(workspace, updated));
			assertEquals(updated.getImage(), FactoryModel.getString(workspace, FactoryModel.WORKSPACE_IMAGE));
			assertFalse("An updated workspace runs the current image.", ProjectWorkspaces.isImageOutdated(workspace));
		} finally {
			commit(() -> {
				TLObject workspace = FactoryModel.getWorkspace(project);
				if (workspace != null) {
					workspace.tDelete();
				}
				project.tDelete();
			});
		}
	}

	private static List<String> mountPaths(WorkspaceSpec spec) {
		return spec.getVolumes().stream().map(VolumeMount::getMountPath).toList();
	}

	private static void commit(Runnable change) {
		commit(() -> {
			change.run();
			return null;
		});
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
				new TestSuite(TestWorkspaceUpdate.class),
				ModelService.Module.INSTANCE,
				SecureRandomService.Module.INSTANCE,
				// The configuration of the workspace image; starting it contacts no container platform.
				WorkspaceService.Module.INSTANCE,
				WorkspaceProxyService.Module.INSTANCE));
	}

}

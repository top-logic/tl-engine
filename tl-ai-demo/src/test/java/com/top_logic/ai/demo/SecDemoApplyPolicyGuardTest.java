/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Predicate;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.tl.ai.core.security.layout.MatrixDimension;
import com.top_logic.tl.ai.core.security.layout.PolicyChecks;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tl.ai.core.security.layout.PolicyEditSession;
import com.top_logic.tl.ai.core.security.layout.PolicyMatrix;
import com.top_logic.tl.ai.core.security.layout.PolicyStore;
import com.top_logic.tl.ai.core.security.layout.ToolAccessPolicyComponent;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * The apply of the policy editor consults the refusals before it writes anything.
 *
 * <p>
 * {@link SecDemoPolicyEditorTest} proves that the editor's refusals are the refusals of the next
 * application start. That says what an apply <em>should</em> ask; it does not prove that it asks.
 * This does: a draft the next start would refuse is applied, and the file the policy is stored in
 * stays as it was. Move the write above the check, or ignore a non-empty refusal list, and this is
 * the test that fails - every other one stays green while the editor saves a policy the application
 * cannot boot from.
 * </p>
 *
 * @see SecDemoPolicyEditorTest
 */
public class SecDemoApplyPolicyGuardTest extends SecDemoPersonaTest {

	/** A role name the fixture does not have, so that a draft naming it has to be refused. */
	private static final String UNKNOWN_ROLE = "supportAgent";

	/** The fixture's role that every run section names. */
	private static final String READER = "ticketReader";

	/** A model no provider of the fixture lists. */
	private static final String UNKNOWN_MODEL = "gpt-4-turbo";

	/** The file an apply writes a policy to. */
	private File _target;

	/** What {@link #_target} held before this test ran, or <code>null</code> if it did not exist. */
	private String _before;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_target = PolicyStore.configurationFile();
		_before = contentOf(_target);
	}

	@Override
	protected void tearDown() throws Exception {
		/* Only a broken guard writes anything here, and what it writes is a policy naming a role that
		 * does not exist - left behind, it would stop every following start, which is the damage this
		 * test is about. The failure is reported either way; it just does not have to be paid for
		 * twice. */
		restore();
		super.tearDown();
	}

	/**
	 * A draft naming a role the application does not have never reaches the disk.
	 */
	public void testApplyPolicyDoesNotWriteAnUnknownRole() throws Exception {
		PolicyDraft refused = savedDraft();
		refused.setCapabilityState(UNKNOWN_ROLE, ToolGroups.DATA_READ, CellState.GRANTED);

		assertRefusedAndNotWritten(refused);
	}

	/**
	 * A draft naming a model the application does not have never reaches the disk, and the editor says
	 * why in the words the next start would use.
	 */
	public void testApplyPolicyDoesNotWriteAnUnknownName() throws Exception {
		PolicyDraft refused = savedDraft();
		new PolicyMatrix(refused, MatrixDimension.MODELS).setCell(READER, UNKNOWN_MODEL, CellState.GRANTED);

		assertRefusedAndNotWritten(refused);
	}

	private void assertRefusedAndNotWritten(PolicyDraft refused) throws Exception {
		List<ResKey> refusals = PolicyChecks.refusals(refused.config(), ROLE_EXISTS);
		assertFalse("The draft this test applies has to be one the next start refuses, otherwise the"
			+ " apply below would be allowed to write and this test would say nothing.",
			refusals.isEmpty());

		ToolAccessPolicyComponent editor = editorOn(refused);
		editor.applyPolicy();

		assertEquals("A refused policy must not be written to " + _target.getAbsolutePath()
			+ "; the apply saved before it checked. Refused for: " + refusals,
			_before, contentOf(_target));
		assertEquals("The editor shows exactly the sentences of the boot check.", keys(refusals),
			keys(editor.session().refusals()));
		assertSame("The refused draft is still the one being edited.", refused, editor.session().draft());
	}

	private static PolicyDraft savedDraft() {
		PolicyDraft draft = PolicyDraft.ofSavedPolicy();
		assertNotNull("The saved tool access configuration has to be readable.", draft);
		return draft;
	}

	private static List<String> keys(List<ResKey> messages) {
		return messages.stream().map(ResKey::getKey).toList();
	}

	/**
	 * The policy editor, opened on the given draft.
	 *
	 * <p>
	 * The component is built from a bare configuration rather than from a layout, because a refused
	 * apply repaints the view and touches nothing that needs a surrounding component tree. Without a
	 * main layout the component keeps its edit session itself, which is where the draft goes.
	 * </p>
	 */
	private static ToolAccessPolicyComponent editorOn(PolicyDraft draft) throws Exception {
		ToolAccessPolicyComponent.Config config =
			TypedConfiguration.newConfigItem(ToolAccessPolicyComponent.Config.class);
		config.update(config.descriptor().getProperty(ToolAccessPolicyComponent.Config.DIMENSION),
			MatrixDimension.MODELS);
		ToolAccessPolicyComponent editor =
			new ToolAccessPolicyComponent(new DefaultInstantiationContext(new AssertProtocol()), config);
		PolicyEditSession.of(editor).reset(draft);
		return editor;
	}

	/** Puts the policy file back into the state {@link #setUp()} found it in. */
	private void restore() throws IOException {
		if (_target == null) {
			// The fixture did not come up; there is nothing this test could have written.
			return;
		}
		String now = contentOf(_target);
		if (_before == null) {
			if (now != null) {
				Files.delete(_target.toPath());
			}
		} else if (!_before.equals(now)) {
			Files.write(_target.toPath(), _before.getBytes(StandardCharsets.UTF_8));
		}
	}

	/** What the given file contains, or <code>null</code> if it does not exist. */
	private static String contentOf(File file) throws IOException {
		if (!file.exists()) {
			return null;
		}
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

	/** Whether a role of the given name exists, the oracle the boot check uses. */
	private static final Predicate<String> ROLE_EXISTS =
		name -> BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), name) != null;

	/**
	 * Creates the test suite.
	 */
	public static Test suite() {
		return suite(SecDemoApplyPolicyGuardTest.class);
	}

}

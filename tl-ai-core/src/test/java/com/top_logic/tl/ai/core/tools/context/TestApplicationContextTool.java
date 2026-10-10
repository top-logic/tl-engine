/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.context;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.tl.ai.core.agent.ApplicationContextContributor;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * {@link ApplicationContextTool}: a save that cannot go through is refused before anybody is asked to
 * approve it, and one that can replaces the text the agents are given (Gitea #667).
 */
public class TestApplicationContextTool extends BasicTestCase {

	private static final String TEXT = "# Application context: Test\n\n## Purpose\nTracks test orders.";

	private final ApplicationContextTool _tool = new ApplicationContextTool();

	private Person _before;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_before = TLContext.getContext().getPerson();
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			ApplicationContextContributor.current().setText(null);
			tx.commit();
		}
		TLContext.getContext().setPerson(_before);
		super.tearDown();
	}

	/** A saved text is what the next read answers and what the agents are given. */
	public void testSaveReplacesTheText() {
		assertNull(_tool.refusal(TEXT, context(ModelSecurity.UNCHECKED)));

		ToolResult saved = save("  " + TEXT + "\n", ModelSecurity.UNCHECKED);

		assertFalse(saved.text(), saved.failed());
		assertEquals(TEXT, ApplicationContextContributor.current().getText());
		String read = _tool.readApplicationContext(context(ModelSecurity.UNCHECKED));
		assertTrue(read, read.endsWith(TEXT));
	}

	/** Reading before anything was written says so, and names the bound. */
	public void testReadWithoutContext() {
		String read = _tool.readApplicationContext(context(ModelSecurity.UNCHECKED));

		assertTrue(read, read.startsWith("No application context has been written yet."));
	}

	/** An account that may not change the context is refused, and nothing is written. */
	public void testAccountWithoutTheRightIsRefused() {
		ModelSecurity denyWrite = new DenyWrite();

		assertRefused(_tool.refusal(TEXT, context(denyWrite)), "Permission denied");
		assertTrue(save(TEXT, denyWrite).failed());
		assertNull(ApplicationContextContributor.current().getText());
	}

	/** Every run pays for the text, so it has a bound, asked before the approval. */
	public void testTooLongIsRefused() {
		String text = "x".repeat(ApplicationContextContributor.MAX_LENGTH + 1);

		assertRefused(_tool.refusal(text, context(ModelSecurity.UNCHECKED)),
			"at most " + ApplicationContextContributor.MAX_LENGTH);
	}

	/** A placeholder nobody fills would reach the model verbatim. */
	public void testUnknownPlaceholderIsRefused() {
		assertRefused(_tool.refusal("Ask {{customer}} first.", context(ModelSecurity.UNCHECKED)),
			"{{customer}}");
	}

	/** An empty save would wipe the context; that is not what a save is for. */
	public void testEmptyTextIsRefused() {
		assertRefused(_tool.refusal(" ", context(ModelSecurity.UNCHECKED)), "The text is empty.");
	}

	private ToolResult save(String text, ModelSecurity security) {
		try (TransactionScope turn = new TransactionScope()) {
			turn.begin();
			ToolResult result = _tool.saveApplicationContext(text, context(security, turn));
			turn.commit();
			return result;
		}
	}

	private static void assertRefused(String refusal, String expected) {
		assertNotNull("Expected a refusal.", refusal);
		assertTrue(refusal, refusal.contains(expected));
	}

	private static ToolContext context(ModelSecurity security) {
		return context(security, TransactionScope.NOOP);
	}

	private static ToolContext context(ModelSecurity security, TransactionScope transaction) {
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
		}, ApprovalCallback.AUTO_APPROVE, transaction);
	}

	/** Everything but writing. */
	private static final class DenyWrite implements ModelSecurity {

		@Override
		public boolean canRead(TLObject obj) {
			return true;
		}

		@Override
		public boolean canRead(TLObject obj, TLStructuredTypePart attribute) {
			return true;
		}

		@Override
		public boolean canWrite(TLObject obj) {
			return false;
		}

		@Override
		public boolean canWrite(TLObject obj, TLStructuredTypePart attribute) {
			return false;
		}

		@Override
		public boolean canDelete(TLObject obj) {
			return true;
		}

		@Override
		public boolean canCreate(TLClass type, TLObject context) {
			return true;
		}

		@Override
		public boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute) {
			return true;
		}

		@Override
		public boolean canPerform(TLObject obj, BoundCommandGroup operation) {
			return true;
		}

		@Override
		public Set<TLClass> accessibleTypes(BoundCommandGroup operation) {
			return ModelSecurity.UNCHECKED.accessibleTypes(operation);
		}

		@Override
		public <T extends TLObject> List<T> filterReadable(Collection<T> objects) {
			return ModelSecurity.UNCHECKED.filterReadable(objects);
		}
	}

	/**
	 * The suite, on a knowledge base with accounts and the model service running.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestApplicationContextTool.class),
				ModelService.Module.INSTANCE));
	}
}

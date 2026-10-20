/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import static test.com.top_logic.layout.view.command.TestConfiguredActionArguments.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.layout.view.command.WithTransactionAction;

/**
 * Tests the actions a {@link WithTransactionAction} runs after its commit, and the refusal of actions
 * changing the state of the view inside the transaction.
 */
public class TestAfterCommit extends AbstractModelAccessTest {

	/** An action recording the last revision of the knowledge base when it runs. */
	private static final String REVISION_PROBE = "<action class='" + RevisionProbe.class.getName() + "'/>";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
		RevisionProbe.REVISIONS.clear();
		becomeUser(_root);
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		RevisionProbe.REVISIONS.clear();
		super.tearDown();
	}

	/**
	 * Tests that the actions after the commit run once the changes are committed, with the
	 * arguments of the chain and the result of the transaction under the configured name.
	 */
	public void testAfterCommitSeesResult() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, _project, OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"{ $" + SELECTION + ".set(`" + qualified(PROJECT) + "#" + NAME
				+ "`, 'renamed'); 'created'; }\"/>"
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + " "
				+ WithTransactionAction.AfterCommit.WITH_RESULT + "='created'>"
				+ REVISION_PROBE
				+ PROBE
				+ "<write-channel name='" + OTHER + "'/>"
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction>"
				+ "</generic-command>");

		long before = lastRevision();
		command.execute(context, _project);

		assertEquals("renamed", _project.tValueByName(NAME));
		assertEquals("The actions run after the commit.", 1, RevisionProbe.REVISIONS.size());
		assertTrue("The actions run after the commit.", RevisionProbe.REVISIONS.get(0) > before);

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(SELECTION, "created"), seen.names());
		assertSame(_project, seen.get(SELECTION));
		assertEquals("created", seen.value());
		assertEquals("created", context.resolveChannel(new ChannelRef(OTHER)).get());
	}

	/**
	 * Tests that the result of the transaction is called {@value WithResultActions#RESULT} without a
	 * configured name.
	 */
	public void testDefaultResultName() throws Exception {
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"'created'\"/>"
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + ">" + PROBE
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction>"
				+ "</generic-command>");

		command.execute(context(Map.of()), "s");

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(SELECTION, WithResultActions.RESULT), seen.names());
		assertEquals("created", seen.value());
	}

	/**
	 * Tests that the actions after the commit do not run when the transaction fails, so that the
	 * channel keeps its value and the changes are rolled back.
	 */
	public void testNoAfterCommitOnRollback() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, _project, OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"{ $" + SELECTION + ".set(`" + qualified(PROJECT) + "#" + NAME
				+ "`, 'renamed'); throw('failed'); }\"/>"
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ PROBE
				+ "<write-channel name='" + OTHER + "'/>"
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction>"
				+ "</generic-command>");

		try {
			command.execute(context, _project);
			fail("The transaction fails.");
		} catch (RuntimeException ex) {
			// Expected.
		}

		assertEquals("project", _project.tValueByName(NAME));
		assertTrue("The actions after the commit do not run.", ArgumentsProbe.SEEN.isEmpty());
		assertEquals("o", context.resolveChannel(new ChannelRef(OTHER)).get());
	}

	/**
	 * Tests that an action changing the state of the view is a configuration error inside the
	 * transaction - directly, as follow-up action of an inner action, in a branch, and after the
	 * commit of a transaction nested in another one - but not after the commit.
	 */
	public void testViewStateInTransactionIsError() throws Exception {
		String write = "<write-channel name='" + OTHER + "'/>";
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>" + write + "</with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><execute-script function='1'>" + write + "</execute-script></with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><if test='true'><then><close-dialog/></then></if></with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><with-transaction>"
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + ">" + write
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction></with-transaction>"
				+ "</generic-command>");

		command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + ">" + write + "<close-dialog/>"
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction>"
				+ write
				+ "</generic-command>");
	}

	/**
	 * Tests that the editor offers the names the runtime instantiates the actions in the
	 * transaction and after the commit with, and agrees about the transaction.
	 */
	public void testEditorScope() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestAfterCommit.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation,
			"<generic-command inputs='" + SELECTION + " as ticket'>"
				+ "<with-transaction>"
				+ PROBE
				+ "<" + WithTransactionAction.Config.AFTER_COMMIT + " "
				+ WithTransactionAction.AfterCommit.WITH_RESULT + "='created'>"
				+ PROBE
				+ "</" + WithTransactionAction.Config.AFTER_COMMIT + ">"
				+ "</with-transaction>"
				+ "</generic-command>");
		instantiation.getInstance(config);
		instantiation.checkErrors();

		WithTransactionAction.Config transaction = (WithTransactionAction.Config) config.getActions().get(0);
		ActionScope body = ActionScopes.scopeAt(transaction.getActions().get(0));
		ActionScope afterCommit = ActionScopes.scopeAt(transaction.getAfterCommit().getActions().get(0));

		assertEquals(2, ArgumentsProbe.SCOPES.size());
		ActionScope runtimeBody = ArgumentsProbe.SCOPES.get(0);
		ActionScope runtimeAfterCommit = ArgumentsProbe.SCOPES.get(1);

		assertEquals(List.of("ticket"), body.names());
		assertEquals(runtimeBody.names(), body.names());
		assertTrue(body.isTransactional());
		assertTrue(runtimeBody.isTransactional());

		assertEquals(List.of("ticket", "created"), afterCommit.names());
		assertEquals("created", afterCommit.defaultName());
		assertEquals(runtimeAfterCommit.names(), afterCommit.names());
		assertEquals(runtimeAfterCommit.defaultName(), afterCommit.defaultName());
		assertFalse(afterCommit.isTransactional());
		assertFalse(runtimeAfterCommit.isTransactional());
	}

	private static long lastRevision() {
		return PersistencyLayer.getKnowledgeBase().getHistoryManager().getLastRevision();
	}

	/**
	 * Action recording the last revision of the knowledge base when it runs.
	 */
	public static class RevisionProbe extends ArgumentsViewAction {

		/**
		 * Configuration of a {@link RevisionProbe}.
		 */
		public interface Config extends PolymorphicConfiguration<RevisionProbe> {

			@Override
			@ClassDefault(RevisionProbe.class)
			Class<? extends RevisionProbe> getImplementationClass();
		}

		/** The revisions seen by all executions. */
		static final List<Long> REVISIONS = new ArrayList<>();

		/**
		 * Creates a {@link RevisionProbe}.
		 */
		@CalledByReflection
		public RevisionProbe(InstantiationContext context, Config config) {
			// No configuration.
		}

		@Override
		public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
			REVISIONS.add(lastRevision());
			continuation.resume(arguments.value());
		}
	}

	/**
	 * The test suite.
	 */
	public static Test suite() {
		return suite(TestAfterCommit.class);
	}
}

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
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ExecuteScriptAction;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.PersistTransientAction;
import com.top_logic.layout.view.command.WithResult;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.layout.view.command.WithTransactionAction;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * Tests the follow-up actions of an action with a result, configured in its {@link WithResult}
 * element, for actions committing by themselves and for a {@link WithTransactionAction}, and the
 * refusal of actions changing the state of the view inside a transaction.
 */
public class TestWithResult extends AbstractModelAccessTest {

	/** An action recording the last revision of the knowledge base when it runs. */
	private static final String REVISION_PROBE = "<action class='" + RevisionProbe.class.getName() + "'/>";

	/** Opening tag of a {@link WithResult} naming the result {@code created}. */
	private static final String WITH_CREATED =
		"<" + WithResultActions.WITH_RESULT + " " + WithResult.AS + "='created'>";

	/** Opening tag of a {@link WithResult} with the default result name. */
	private static final String WITH_RESULT = "<" + WithResultActions.WITH_RESULT + ">";

	/** Closing tag of a {@link WithResult}. */
	private static final String END_WITH_RESULT = "</" + WithResultActions.WITH_RESULT + ">";

	private static final String WRITE_OTHER = "<write-channel name='" + OTHER + "'/>";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ArgumentsProbe.reset();
		RevisionProbe.REVISIONS.clear();
	}

	@Override
	protected Person initialUser() {
		return _root;
	}

	@Override
	protected void tearDown() throws Exception {
		ArgumentsProbe.reset();
		RevisionProbe.REVISIONS.clear();
		super.tearDown();
	}

	/**
	 * Tests that the follow-up actions of an {@code <execute-script>} see its result under the
	 * configured name, as their default value.
	 */
	public void testExecuteScriptFollowUps() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, "s", OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<execute-script function=\"$" + SELECTION + " + '-created'\">"
				+ WITH_CREATED + PROBE + WRITE_OTHER + END_WITH_RESULT
				+ "</execute-script>"
				+ PROBE
				+ "</generic-command>");

		command.execute(context, "s");

		assertEquals(2, ArgumentsProbe.SEEN.size());
		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(SELECTION, "created"), seen.names());
		assertEquals("s-created", seen.value());
		assertEquals("s-created", context.resolveChannel(new ChannelRef(OTHER)).get());

		assertEquals("The result does not reach the actions following the action.", List.of(SELECTION),
			ArgumentsProbe.SEEN.get(1).names());
	}

	/**
	 * Tests that the follow-up actions of a {@code <persist-transient>} run once the object is
	 * committed, with the persistent object as result.
	 */
	public void testPersistTransientFollowUps() throws Exception {
		TLObject draft = TransientObjectFactory.INSTANCE.createObject(type(PROJECT));
		draft.tUpdateByName(NAME, "draft");
		DefaultViewContext context = context(Map.of(SELECTION, draft));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<" + PersistTransientAction.Config.TAG_NAME + ">"
				+ WITH_CREATED + REVISION_PROBE + PROBE + END_WITH_RESULT
				+ "</" + PersistTransientAction.Config.TAG_NAME + ">"
				+ "</generic-command>");

		long before = lastRevision();
		command.execute(context, draft);

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		TLObject created = (TLObject) seen.get("created");
		try {
			assertEquals(List.of(SELECTION, "created"), seen.names());
			assertSame(created, seen.value());
			assertFalse(created.tTransient());
			assertEquals("draft", created.tValueByName(NAME));
			assertEquals("The follow-up actions run after the commit.", 1, RevisionProbe.REVISIONS.size());
			assertTrue("The follow-up actions run after the commit.", RevisionProbe.REVISIONS.get(0) > before);
		} finally {
			delete(created);
		}
	}

	/**
	 * Tests that the actions after the commit of a transaction run once the changes are committed,
	 * with the arguments of the chain and the result of the transaction under the configured name.
	 */
	public void testTransactionFollowUpsAfterCommit() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, _project, OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"{ $" + SELECTION + ".set(`" + qualified(PROJECT) + "#" + NAME
				+ "`, 'renamed'); 'created'; }\"/>"
				+ WITH_CREATED
				+ REVISION_PROBE
				+ PROBE
				+ WRITE_OTHER
				+ END_WITH_RESULT
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
	 * Tests that the result is called {@value WithResultActions#RESULT} without a configured name.
	 */
	public void testDefaultResultName() throws Exception {
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"'created'\"/>"
				+ WITH_RESULT + PROBE + END_WITH_RESULT
				+ "</with-transaction>"
				+ "<execute-script function=\"'other'\">" + WITH_RESULT + PROBE + END_WITH_RESULT + "</execute-script>"
				+ "</generic-command>");

		command.execute(context(Map.of()), "s");

		ActionArguments seen = ArgumentsProbe.SEEN.get(0);
		assertEquals(List.of(SELECTION, WithResultActions.RESULT), seen.names());
		assertEquals("created", seen.value());
		ActionArguments script = ArgumentsProbe.SEEN.get(1);
		assertEquals(List.of(SELECTION, WithResultActions.RESULT), script.names());
		assertEquals("other", script.value());
	}

	/**
	 * Tests that the actions after the commit do not run when the transaction fails, so that the
	 * channel keeps its value and the changes are rolled back.
	 */
	public void testNoFollowUpsOnRollback() throws Exception {
		DefaultViewContext context = context(Map.of(SELECTION, _project, OTHER, "o"));
		GenericViewCommand command = command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ "<execute-script function=\"{ $" + SELECTION + ".set(`" + qualified(PROJECT) + "#" + NAME
				+ "`, 'renamed'); throw('failed'); }\"/>"
				+ WITH_RESULT
				+ PROBE
				+ WRITE_OTHER
				+ END_WITH_RESULT
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
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>" + WRITE_OTHER + "</with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><execute-script function='1'>" + WITH_RESULT + WRITE_OTHER + END_WITH_RESULT
				+ "</execute-script></with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><if test='true'><then><close-dialog/></then></if></with-transaction>"
				+ "</generic-command>");
		assertInstantiationError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction><with-transaction>"
				+ WITH_RESULT + WRITE_OTHER + END_WITH_RESULT
				+ "</with-transaction></with-transaction>"
				+ "</generic-command>");

		command(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<with-transaction>"
				+ WITH_RESULT + WRITE_OTHER + "<close-dialog/>" + END_WITH_RESULT
				+ "</with-transaction>"
				+ "<execute-script function='1'>" + WITH_RESULT + WRITE_OTHER + END_WITH_RESULT + "</execute-script>"
				+ WRITE_OTHER
				+ "</generic-command>");
	}

	/**
	 * Tests that an action written directly into an action with a result, outside of its
	 * {@link WithResult}, is a configuration error.
	 */
	public void testPlainFollowUpIsError() throws Exception {
		assertParseError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<execute-script function='1'>" + WRITE_OTHER + "</execute-script>"
				+ "</generic-command>");
		assertParseError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<" + PersistTransientAction.Config.TAG_NAME + ">" + WRITE_OTHER
				+ "</" + PersistTransientAction.Config.TAG_NAME + ">"
				+ "</generic-command>");
		assertParseError(
			"<generic-command inputs='" + SELECTION + "'>"
				+ "<execute-script function='1' " + WithResult.AS + "='created'/>"
				+ "</generic-command>");
	}

	/**
	 * Tests that the editor offers the names the runtime instantiates the actions in the
	 * transaction and after the commit with, and agrees about the transaction.
	 */
	public void testEditorScope() throws Exception {
		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestWithResult.class);
		GenericViewCommand.Config config = TestConfiguredActionArguments.parse(instantiation,
			"<generic-command inputs='" + SELECTION + " as ticket'>"
				+ "<with-transaction>"
				+ PROBE
				+ "<execute-script function='1'>"
				+ "<" + WithResultActions.WITH_RESULT + " " + WithResult.AS + "='inner'>" + PROBE + END_WITH_RESULT
				+ "</execute-script>"
				+ WITH_CREATED
				+ PROBE
				+ END_WITH_RESULT
				+ "</with-transaction>"
				+ "</generic-command>");
		instantiation.getInstance(config);
		instantiation.checkErrors();

		WithTransactionAction.Config transaction = (WithTransactionAction.Config) config.getActions().get(0);
		ActionScope body = ActionScopes.scopeAt(transaction.getActions().get(0));
		ExecuteScriptAction.Config innerAction = (ExecuteScriptAction.Config) transaction.getActions().get(1);
		ActionScope inner = ActionScopes.scopeAt(innerAction.getWithResult().getActions().get(0));
		ActionScope afterCommit = ActionScopes.scopeAt(transaction.getWithResult().getActions().get(0));

		assertEquals(3, ArgumentsProbe.SCOPES.size());
		ActionScope runtimeBody = ArgumentsProbe.SCOPES.get(0);
		ActionScope runtimeInner = ArgumentsProbe.SCOPES.get(1);
		ActionScope runtimeAfterCommit = ArgumentsProbe.SCOPES.get(2);

		assertEquals(List.of("ticket"), body.names());
		assertEquals(runtimeBody.names(), body.names());
		assertTrue(body.isTransactional());
		assertTrue(runtimeBody.isTransactional());

		assertEquals(List.of("ticket", "inner"), inner.names());
		assertEquals(runtimeInner.names(), inner.names());
		assertEquals(runtimeInner.defaultName(), inner.defaultName());
		assertTrue("A follow-up action inside the transaction runs before the commit.", inner.isTransactional());
		assertTrue(runtimeInner.isTransactional());

		assertEquals(List.of("ticket", "created"), afterCommit.names());
		assertEquals("created", afterCommit.defaultName());
		assertEquals(runtimeAfterCommit.names(), afterCommit.names());
		assertEquals(runtimeAfterCommit.defaultName(), afterCommit.defaultName());
		assertFalse(afterCommit.isTransactional());
		assertFalse(runtimeAfterCommit.isTransactional());
	}

	private static void assertParseError(String xml) {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestWithResult.class);
		try {
			TestConfiguredActionArguments.parse(context, xml);
		} catch (ConfigurationException ex) {
			return;
		}
		fail("Parsing is refused: " + xml);
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
		return suite(TestWithResult.class);
	}
}

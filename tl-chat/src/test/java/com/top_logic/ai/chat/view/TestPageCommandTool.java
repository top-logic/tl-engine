/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.tools.ChatCommandTool;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.tool.api.AnnotatedTools;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * What the page command tools run in a page, and what they refuse.
 *
 * <p>
 * The test drives the real path, as {@link TestPresentTool} does: a real sub-session displayed by a
 * live browser window of a logged-in session, and a real {@link ChatConversationControl} built from
 * a {@code <chat-conversation>} with {@code <agent-commands>}. What stands in for the browser's
 * dialogs is the {@link DialogManager} of the window's queue.
 * </p>
 */
public class TestPageCommandTool extends BasicTestCase {

	/** The dialog the command of the fixture opens. */
	private static final String DIALOG = "test-agent-command-dialog.view.xml";

	/** The command of the fixture's page that opens {@link #DIALOG}. */
	private static final String OPEN = "connectGit";

	/** What {@link #OPEN} does, as the page tells the model. */
	private static final String OPEN_DESCRIPTION = "Opens the form for the access token of a git connection.";

	/** A command of the fixture's page whose executability says no. */
	private static final String LOCKED = "lockedCommand";

	/** The reason {@link #LOCKED} gives. */
	private static final String LOCKED_REASON = "The connection is locked by another user.";

	/** A command of the fixture's page whose executability says no with a translated reason. */
	private static final String TRANSLATED = "translatedCommand";

	/** The English text of the reason {@link #TRANSLATED} gives. */
	private static final String TRANSLATED_REASON_EN = "The connection is being checked.";

	/** The German text of the reason {@link #TRANSLATED} gives. */
	private static final String TRANSLATED_REASON_DE = "Die Verbindung wird gerade geprüft.";

	/** A command of the fixture's page that takes an object and writes it to {@link #SHOWN}. */
	private static final String SHOW = "showObject";

	/** What object {@link #SHOW} takes, as the page tells the model. */
	private static final String SHOW_INPUT = "The object to show.";

	/** A command of the fixture's page that takes an object and whose rule refuses every object. */
	private static final String GUARDED = "guardedCommand";

	/** The reason {@link #GUARDED} gives for an object. */
	private static final String GUARDED_REASON = "This object cannot be shown here.";

	/** The channel of the fixture's page the chains of {@link #SHOW} and {@link #GUARDED} write. */
	private static final String SHOWN = "shown";

	private static final String WINDOW = "pageCommandWindow";

	private static Person user;

	private ChatCommandTool _tool;

	private LiveWindow _window;

	private ChatConversationControl _conversation;

	private Dialogs _dialogs;

	private DefaultViewChannel _shown;

	private File _webapp;

	private FileManager _fileManagerBefore;

	private Person _previousAccount;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_previousAccount = subSession().getPerson();
		subSession().setPerson(PersonManager.getManager().getRoot());
		if (user == null) {
			user = TestPerson.createPerson("commander");
		}
		_tool = new ChatCommandTool();
		_dialogs = new Dialogs();
		_shown = new DefaultViewChannel(SHOWN);

		// The dialog view, on top of the files the application has.
		_webapp = Files.createTempDirectory("tl-agent-commands").toFile();
		File views = new File(_webapp, ViewLoader.VIEW_BASE_PATH.substring(1));
		views.mkdirs();
		Files.writeString(new File(views, DIALOG).toPath(),
			"<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<view>\n\t<panel title=\"Git connection\"/>\n</view>\n",
			StandardCharsets.UTF_8);
		_fileManagerBefore = FileManager.getInstance();
		List<Path> paths = new ArrayList<>();
		paths.add(_webapp.toPath());
		paths.addAll(_fileManagerBefore.getPaths());
		FileManager.setInstance(MultiFileManager.createMultiFileManager(paths));
	}

	@Override
	protected void tearDown() throws Exception {
		if (_conversation != null) {
			_conversation.cleanupTree();
			_conversation = null;
		}
		if (_window != null) {
			_window.close();
			_window = null;
		}
		FileManager.setInstance(_fileManagerBefore);
		FileUtilities.deleteR(_webapp);
		subSession().setPerson(_previousAccount);
		super.tearDown();
	}

	/**
	 * A command the page lists opens its dialog in the window of the run, and the tools list it with
	 * its description.
	 */
	public void testACommandOpensItsDialogInTheRunsWindow() throws Exception {
		displayPage();
		Tool run = tool(ChatCommandTool.RUN_TOOL);
		assertNull("A page that lists a command is offered the tool.", run.unavailableReason(context()));

		String listed = _tool.pageCommands(context());
		assertSays("- " + OPEN + ": " + OPEN_DESCRIPTION, listed);
		assertSays("- " + LOCKED + ": ", listed);
		assertSays("- " + OPEN + ": " + OPEN_DESCRIPTION + " Takes no object.", listed);
		assertSays("- " + SHOW + ": Shows an object. Takes an object, its id passed as '"
			+ ChatCommandTool.ARG_INPUT + "': " + SHOW_INPUT, listed);

		String result = _tool.runPageCommand(OPEN, null, context());

		assertSays("The command '" + OPEN + "' ran", result);
		assertNotNull("The dialog is open in the run's window.", _dialogs._open);
	}

	/** A name the page does not list is refused, and the refusal says which names exist. */
	public void testAnUnlistedNameIsRefused() throws Exception {
		displayPage();

		String result = _tool.runPageCommand("deleteEverything", null, context());

		assertSays("offers no command 'deleteEverything'", result);
		assertSays(OPEN, result);
		assertNull("Nothing was opened.", _dialogs._open);
	}

	/**
	 * A run whose window does not display a chat listing a command is not offered the tools and is
	 * refused - and a run without a window says that it has no page.
	 */
	public void testARunWithoutTheElementIsRefused() throws Exception {
		Tool run = tool(ChatCommandTool.RUN_TOOL);
		Tool list = tool(ChatCommandTool.LIST_TOOL);
		assertSays("No page is attached", run.unavailableReason(context()));
		assertSays("No page is attached", _tool.runPageCommand(OPEN, null, context()));

		buildPage();
		assertSays("Built but not displayed.", "offers no command", run.unavailableReason(context()));
		assertSays("offers no command", list.unavailableReason(context()));
		assertSays("offers no command", _tool.runPageCommand(OPEN, null, context()));

		_conversation.attach();
		assertNull(run.unavailableReason(context()));

		_conversation.detach();
		assertSays("Left, not gone.", "offers no command", _tool.runPageCommand(OPEN, null, context()));
		assertNull("Nothing was opened.", _dialogs._open);
	}

	/** A command whose executability says no is refused with the reason its rule gives. */
	public void testANonExecutableCommandIsRefusedWithItsReason() throws Exception {
		displayPage();

		String result = _tool.runPageCommand(LOCKED, null, context());

		assertEquals(LOCKED_REASON, refusal(result, LOCKED, null));
		assertNull("Its chain did not run.", _dialogs._open);
	}

	/**
	 * The reason a refused command gives is the application's text about the user: it reaches the
	 * model in the language of the run's user, as the reason of a structured refusal.
	 */
	public void testTheReasonOfARefusalIsInTheUsersLanguage() throws Exception {
		displayPage();
		TLContext context = TLContext.getContext();
		Locale previous = context.getCurrentLocale();
		String result;
		try {
			context.setCurrentLocale(Locale.GERMAN);
			result = _tool.runPageCommand(TRANSLATED, null, context());
		} finally {
			context.setCurrentLocale(previous);
		}

		String reason = refusal(result, TRANSLATED, null);
		assertSays(TRANSLATED_REASON_DE, reason);
		assertFalse("Not the English text: " + reason, reason.contains(TRANSLATED_REASON_EN));
		assertNull("Its chain did not run.", _dialogs._open);
	}

	/**
	 * The object the agent passes is the main argument of the command's run: its chain processes
	 * it, here by writing it to a channel.
	 */
	public void testTheObjectIsTheArgumentOfTheRun() throws Exception {
		displayPage();

		String result = _tool.runPageCommand(SHOW, tid(user), context());

		assertSays("The command '" + SHOW + "' ran in the user's page for ", result);
		assertSays(tid(user), result);
		assertSame("The chain processed the object.", user, _shown.get());
	}

	/**
	 * An object the user cannot read is refused like one that does not exist, and the command does
	 * not run.
	 */
	public void testAnUnreadableObjectIsRefused() throws Exception {
		displayPage();
		ToolContext run = new ToolContext(new TestPresentTool.RunAs(new TestPresentTool.NoReadSecurity()),
			ApprovalCallback.AUTO_APPROVE);

		String result = _tool.runPageCommand(SHOW, tid(user), run);

		assertNotNull("Refused with a reason.", refusal(result, SHOW, tid(user)));
		assertNull("The chain did not run.", _shown.get());
	}

	/** An object for a command without an input description is refused; the command does not run. */
	public void testAnObjectForACommandTakingNoneIsRefused() throws Exception {
		displayPage();

		String result = _tool.runPageCommand(OPEN, tid(user), context());

		assertSays("takes no object", refusal(result, OPEN, tid(user)));
		assertNull("Nothing was opened.", _dialogs._open);
	}

	/**
	 * The executability rules decide over the object the agent passes, and a refusal answers with
	 * their reason.
	 */
	public void testARuleRefusingTheObjectAnswersWithItsReason() throws Exception {
		displayPage();

		String result = _tool.runPageCommand(GUARDED, tid(user), context());

		assertEquals(GUARDED_REASON, refusal(result, GUARDED, tid(user)));
		assertNull("Its chain did not run.", _shown.get());
	}

	// --- Fixture ---

	/** Builds the page and displays it. */
	private void displayPage() throws Exception {
		buildPage();
		_conversation.attach();
	}

	/**
	 * Builds the page in the window of the run - a {@code <chat-conversation>} listing a command
	 * opening a dialog, two that are never executable, one writing the object it takes to
	 * {@link #SHOWN} and one refusing every object - without displaying it.
	 */
	private void buildPage() throws Exception {
		_window = LiveWindow.open(user, WINDOW, subSession());
		ReactWindowRegistry registry = _window.registry();
		SSEUpdateQueue queue = registry.getOrCreateQueue(WINDOW);
		queue.setDialogManager(_dialogs);
		DefaultViewContext view = new DefaultViewContext(new DefaultReactContext("/app", WINDOW, queue, registry));
		view.registerChannel(SHOWN, _shown);
		_conversation = new ChatConversationControl(view, (ChatConversationElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent(
				"<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'>"
					+ "<agent-commands>"
					+ "<agent-command name='" + OPEN + "' description='" + OPEN_DESCRIPTION + "'>"
					+ "<open-dialog dialog-view='" + DIALOG + "'/>"
					+ "</agent-command>"
					+ "<agent-command name='" + LOCKED + "' description='Never runs.'>"
					+ "<executability><disabled-if expr='\"" + LOCKED_REASON + "\"'/></executability>"
					+ "<open-dialog dialog-view='" + DIALOG + "'/>"
					+ "</agent-command>"
					+ "<agent-command name='" + TRANSLATED + "' description='Never runs either.'>"
					+ "<executability><disabled-if expr='#(\"" + TRANSLATED_REASON_EN + "\"@en, \""
					+ TRANSLATED_REASON_DE + "\"@de)'/></executability>"
					+ "<open-dialog dialog-view='" + DIALOG + "'/>"
					+ "</agent-command>"
					+ "<agent-command name='" + SHOW + "' description='Shows an object.' input-description='"
					+ SHOW_INPUT + "'>"
					+ "<write-channel name='" + SHOWN + "'/>"
					+ "</agent-command>"
					+ "<agent-command name='" + GUARDED + "' description='Shows no object.' input-description='"
					+ SHOW_INPUT + "'>"
					+ "<executability><disabled-if expr='if($input != null, \"" + GUARDED_REASON
					+ "\")'/></executability>"
					+ "<write-channel name='" + SHOWN + "'/>"
					+ "</agent-command>"
					+ "</agent-commands>"
					+ "</chat-conversation>")));
	}

	/** A run of no particular account, which asks the chats of every account of the window. */
	private static ToolContext context() {
		return new ToolContext(SecurityContext.UNRESTRICTED, ApprovalCallback.AUTO_APPROVE);
	}

	/**
	 * Asserts that the given answer is the structured refusal of the given command and returns its
	 * reason.
	 *
	 * @param input
	 *        The id of the object the run was asked for, {@code null} for a run without one.
	 */
	private static String refusal(String answer, String command, String input) throws ParseException {
		Map<?, ?> refusal = (Map<?, ?>) JSON.fromString(answer);
		assertEquals(answer, Boolean.TRUE, refusal.get(ChatCommandTool.REFUSAL_REFUSED));
		assertEquals(answer, command, refusal.get(ChatCommandTool.REFUSAL_COMMAND));
		assertEquals(answer, input, refusal.get(ChatCommandTool.REFUSAL_INPUT));
		Object reason = refusal.get(ChatCommandTool.REFUSAL_REASON);
		assertTrue(answer, reason instanceof String && !((String) reason).isBlank());
		return (String) reason;
	}

	private static String tid(Person person) {
		return person.tId().asString();
	}

	/** The tool of the given name as the agent's catalog holds it. */
	private Tool tool(String name) {
		for (Tool tool : AnnotatedTools.from(_tool)) {
			if (tool.name().equals(name)) {
				return tool;
			}
		}
		throw new AssertionError("No tool " + name + ".");
	}

	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	private static void assertSays(String expected, String actual) {
		assertSays(null, expected, actual);
	}

	private static void assertSays(String message, String expected, String actual) {
		assertNotNull((message == null ? "" : message + " ") + "Expected '" + expected + "', got nothing.", actual);
		assertTrue((message == null ? "" : message + " ") + "Expected '" + expected + "' in: " + actual,
			actual.contains(expected));
	}

	/**
	 * Stands in for the dialogs of the window, holding the one that was opened.
	 */
	private static class Dialogs implements DialogManager {

		ReactControl _open;

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_open = child;
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open = null;
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("Nothing is stacked on the dialog of these tests.");
		}
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestPageCommandTool.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}

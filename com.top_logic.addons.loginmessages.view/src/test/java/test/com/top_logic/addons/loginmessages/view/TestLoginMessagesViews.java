/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.addons.loginmessages.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.addons.loginmessages.model.LoginMessagesWrapperFactory;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.addons.loginmessages.script.LoginMessagesDisplay;
import com.top_logic.addons.loginmessages.script.LoginMessagesFunctions;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.HtmlValues;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.CommandArguments;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.login.DialogStep;
import com.top_logic.layout.view.login.LoginAction;
import com.top_logic.layout.view.login.LoginFlow;
import com.top_logic.layout.view.login.LoginStep;
import com.top_logic.layout.view.login.LoginStepsConfig;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Executes the commands of the views of this module as they are configured: creating, deleting
 * and re-requesting the confirmation of a login message in the administration, and confirming the
 * messages in the dialog of the login step.
 *
 * <p>
 * The views are parsed from their files and their commands instantiated and executed without a
 * display; a confirmation asked for by a command proceeds without a dialog. The administration
 * commands run for the root account, as they do in the administration.
 * </p>
 */
public class TestLoginMessagesViews extends BasicTestCase {

	/** The administration view of the login messages. */
	private static final String ADMIN_VIEW = "admin/system/login-messages/login-messages.view.xml";

	/** The dialog creating a login message. */
	private static final String CREATE_VIEW = "admin/system/login-messages/create-login-message.view.xml";

	/** The dialog of the login step. */
	private static final String STEP_VIEW = "login-messages/login-messages.view.xml";

	/** Qualified name of the type of a login message. */
	private static final String TYPE = "LoginMessages:LoginMessage";

	/** Channel of the administration view holding the selected message. */
	private static final String SELECTED = "selectedMessage";

	/** Channel of the create dialog receiving the created message. */
	private static final String SELECTION = "selection";

	/** Channel of the login dialog holding the display of the due messages. */
	private static final String DISPLAY = "display";

	/**
	 * A text as the HTML editor of the React UI writes it: headings, marks, a list, a link, a table
	 * and an image, stored as reference to the image of the text.
	 */
	private static final String EDITOR_HTML = "<h2>Maintenance</h2>"
		+ "<p>The system is <strong>unavailable</strong> on <em>Saturday</em>, see "
		+ "<a target=\"_blank\" rel=\"noopener noreferrer nofollow\" href=\"https://example.com/notice\">the notice</a>.</p>"
		+ "<ul><li><p>Save your work.</p></li><li><p>Log out.</p></li></ul>"
		+ "<table><tbody><tr><th colspan=\"1\" rowspan=\"1\"><p>From</p></th><td colspan=\"1\" rowspan=\"1\"><p>8:00</p></td></tr></tbody></table>"
		+ "<p><img src=\"ref:plan.png\" alt=\"Plan\" title=\"Plan\"></p>";

	private final List<TLObject> _created = new ArrayList<>();

	private Person _account;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_account = TestPerson.createPerson("loginMessagesViews");
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			for (TLObject object : _created) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			tx.commit();
		}
		_created.clear();
		TestPerson.deletePersonAndUser(_account);
		super.tearDown();
	}

	/**
	 * Tests the command "New" of the administration and the command "Create" of its dialog: a new
	 * message is created from the transient model the form edits, and selected.
	 */
	public void testCreate() throws Exception {
		ViewContext admin = context(SELECTED);
		asRoot(() -> {
			command(ADMIN_VIEW, "New").execute(admin, null);
			return null;
		});

		TLObject model = asRoot(() -> {
			TLObject result = TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(TYPE));
			result.tUpdateByName(LoginMessage.NAME_ATTR, "created");
			result.tUpdateByName(LoginMessage.ACTIVE_ATTR, Boolean.TRUE);
			result.tUpdateByName(LoginMessage.MESSAGE_ATTR, editorText());
			return result;
		});
		ViewContext dialog = context(SELECTION);
		asRoot(() -> {
			command(CREATE_VIEW, "Create").execute(dialog, model);
			return null;
		});

		Object selected = channel(dialog, SELECTION).get();
		assertTrue("The created message is selected: " + selected, selected instanceof LoginMessage);
		LoginMessage created = (LoginMessage) selected;
		_created.add(created);
		assertTrue(created.tValid());
		assertEquals("created", created.getName());
		assertNotNull("The text is stored with its image.", created.getMessage());
		assertTrue(LoginMessagesFunctions.due(_account).contains(created));
	}

	/**
	 * Tests the command "Delete" of the administration: the selected message is deleted and the
	 * selection cleared.
	 */
	public void testDelete() throws Exception {
		LoginMessage message = message("deleted");
		ViewContext admin = context(SELECTED);
		channel(admin, SELECTED).set(message);

		asRoot(() -> {
			command(ADMIN_VIEW, "Delete").execute(admin, null);
			return null;
		});

		assertFalse(message.tValid());
		assertNull(channel(admin, SELECTED).get());
	}

	/**
	 * Tests the command "Ask again for confirmation": a message an account has confirmed is due for
	 * it again.
	 */
	public void testAskAgain() throws Exception {
		LoginMessage message = message("again");
		LoginMessagesFunctions.confirm(LoginMessagesFunctions.display(_account));
		assertFalse(LoginMessagesFunctions.due(_account).contains(message));

		// The confirmation is older than the reset, not of the same instant.
		Thread.sleep(5);

		ViewContext admin = context(SELECTED);
		channel(admin, SELECTED).set(message);
		asRoot(() -> {
			command(ADMIN_VIEW, "Ask again for confirmation").execute(admin, null);
			return null;
		});

		Date reset = message.getConfirmExpirationDate();
		assertNotNull(reset);
		assertTrue(LoginMessagesFunctions.due(_account).contains(message));
	}

	/**
	 * Tests the dialog of the login step: its command "OK" confirms the messages shown and finishes
	 * the step, which completes the login.
	 */
	public void testConfirmInLogin() throws Exception {
		LoginMessage message = message("shown");
		List<String> completed = new ArrayList<>();

		// The dialog runs in the window of the visitor, before the account has a session.
		Person anonymous = PersonManager.getManager().getAnonymous();
		assertNotNull(anonymous);
		TLContextManager.inPersonContext(anonymous, () -> {
			ReactContext react = new DefaultReactContext("", "test", null, new ReactWindowRegistry("test"));
			List<LoginStep> steps = LoginStepsConfig.configured().stream()
				.filter(DialogStep.class::isInstance)
				.toList();
			new LoginFlow(_account, steps) {
				@Override
				protected void complete(ReactContext context) {
					completed.add(getAccount().getName());
				}
			}.begin(react);
			assertEquals("The login waits for the messages to be confirmed.", List.of(), completed);

			ViewContext dialog = context(LoginAction.ACCOUNT_CHANNEL, DISPLAY);
			channel(dialog, LoginAction.ACCOUNT_CHANNEL).set(_account);
			channel(dialog, DISPLAY).set(LoginMessagesFunctions.display(_account));

			command(STEP_VIEW, "OK").execute(dialog, null);
			return null;
		});

		assertEquals(List.of(_account.getName()), completed);
		assertFalse(LoginMessagesFunctions.due(_account).contains(message));
	}

	/**
	 * Tests that a text written by the HTML editor of the React UI, with an image, is displayed by
	 * the dialog: the HTML of the display passes the check the <code>&lt;html&gt;</code> element
	 * applies, and carries the image.
	 */
	public void testEditorHtmlPassesCheck() throws Exception {
		LoginMessage message = message("editor");
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			message.setMessage(editorText());
			tx.commit();
		}

		String html = TLContextManager.inPersonContext(_account,
			() -> HtmlValues.toHtml(LoginMessagesFunctions.display(_account)));

		SafeHTML.getInstance().check(html);
		assertTrue(html, html.contains("data:image/png;base64,"));
		assertTrue(html, html.contains("unavailable"));
		assertTrue(html, html.contains(LoginMessagesDisplay.MESSAGE_CSS_CLASS));
	}

	private static I18NStructuredText editorText() {
		StructuredText text = new StructuredText(EDITOR_HTML,
			Map.of("plan.png", BinaryDataFactory.createBinaryData(new byte[] { 1, 2, 3, 4 }, "image/png", "plan.png")));
		return new I18NStructuredText(Map.of(Locale.ENGLISH, text, Locale.GERMAN, text));
	}

	private LoginMessage message(String name) {
		LoginMessage message;
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			message = LoginMessagesWrapperFactory.getInstance().createLoginMessage();
			message.setName(name);
			message.setActive(true);
			tx.commit();
		}
		_created.add(message);
		return message;
	}

	/**
	 * A context of a view with the given channels.
	 */
	private static ViewContext context(String... channels) {
		ViewContext result =
			new DefaultViewContext(new DefaultReactContext("", "test", null, new ReactWindowRegistry("test")));
		for (String name : channels) {
			result.registerChannel(name, new DefaultViewChannel(name));
		}
		return result;
	}

	private static ViewChannel channel(ViewContext context, String name) {
		return context.resolveChannel(new ChannelRef(name));
	}

	/**
	 * The command of the given view with the given English label, instantiated from the view's
	 * configuration.
	 */
	private static GenericViewCommand command(String viewRef, String label) throws Exception {
		ConfigurationItem view = ViewLoader.loadConfig(ViewLoader.fullPath(viewRef));
		List<GenericViewCommand.Config> commands = new ArrayList<>();
		collectCommands(view, commands);
		Resources english = Resources.getInstance(Locale.ENGLISH);
		for (GenericViewCommand.Config command : commands) {
			if (command.getLabel() != null && label.equals(english.getString(command.getLabel()))) {
				DefaultInstantiationContext context = new DefaultInstantiationContext(TestLoginMessagesViews.class);
				GenericViewCommand result = (GenericViewCommand) ActionScope.single(CommandArguments.DEFAULT_ARGUMENT)
					.bind(context).getInstance(command);
				context.checkErrors();
				return result;
			}
		}
		throw new AssertionError("No command '" + label + "' in '" + viewRef + "'.");
	}

	private static void collectCommands(ConfigurationItem item, List<GenericViewCommand.Config> commands) {
		if (item instanceof GenericViewCommand.Config command) {
			commands.add(command);
		}
		for (PropertyDescriptor property : item.descriptor().getProperties()) {
			if (property.isDerived() || property.hasContainerAnnotation()) {
				continue;
			}
			Object value = item.value(property);
			if (value instanceof ConfigurationItem child) {
				collectCommands(child, commands);
			} else if (value instanceof Collection<?> entries) {
				for (Object entry : entries) {
					if (entry instanceof ConfigurationItem child) {
						collectCommands(child, commands);
					}
				}
			}
		}
	}

	private static <T> T asRoot(Job<T> job) throws Exception {
		return TLContextManager.inPersonContext(PersonManager.getManager().getRoot(), job::run);
	}

	/**
	 * A computation that may fail.
	 */
	private interface Job<T> {

		/** Runs the computation. */
		T run() throws Exception;
	}

	/**
	 * The suite, with the application model, the TL-Script functions and the HTML check.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestLoginMessagesViews.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE, SafeHTML.Module.INSTANCE, ThreadContextManager.Module.INSTANCE));
	}

}

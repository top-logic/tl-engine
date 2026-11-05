/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.contact;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.contact.business.AbstractContact;
import com.top_logic.contact.business.ContactFactory;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.overlay.DialogHandle;
import com.top_logic.layout.react.control.overlay.DialogManager;
import com.top_logic.layout.react.control.overlay.DialogResult;
import com.top_logic.layout.react.control.overlay.DialogResultHandler;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.admin.CreateAccountAction;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.util.model.ModelService;

/**
 * Runs the commands of the people page - the account administration of an application with
 * contacts, {@value #ACCOUNTS} as merged with the overlay of this module - on a displayed instance
 * of the page, with the dialogs they open.
 */
@SuppressWarnings("javadoc")
public class TestPeopleView extends BasicTestCase {

	private static final String ACCOUNTS = "admin/access-control/accounts.view.xml";

	private static final String SELECTED_CONTACT = "selectedContact";

	private static final String SELECTED_ACCOUNT = "selectedAccount";

	/** The channel of a creation dialog holding the transient object it creates. */
	private static final String MODEL = "model";

	private static final String NEW_PERSON = "new-person";

	/** The channel of the "new person" dialog deciding whether a login is created. */
	private static final String ALLOW_LOGIN = "allowLogin";

	private static final String ADD_LOGIN = "add-login";

	private static final String REMOVE_LOGIN = "remove-login";

	private static final String DELETE_CONTACT = "delete-contact";

	private static final String CLICK = "click";

	/** The label of the button creating the object of a creation dialog, in either language. */
	private static final List<String> CREATE = List.of("Create", "Erstellen");

	private static final String LOGIN = "peopleViewLogin";

	private SSEUpdateQueue _sseQueue;

	private Dialogs _dialogs;

	private ViewContext _page;

	private ReactControl _view;

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_sseQueue = new SSEUpdateQueue();
		_dialogs = new Dialogs();
		_sseQueue.setDialogManager(_dialogs);
		_page = new DefaultViewContext(new DefaultReactContext("", "test", _sseQueue, new ReactWindowRegistry("test")));
		ViewElement view = ViewLoader.loadView(ViewLoader.fullPath(ACCOUNTS));
		IReactControl control = ThreadContext.withoutSecurityChecks(() -> {
			return view.createControl(_page);
		});
		_view = (ReactControl) control;
		_view.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_view.cleanupTree();
		_sseQueue.shutdown();
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction()) {
				for (TLObject object : _created) {
					if (object instanceof PersonContact contact && contact.tValid()) {
						Person account = contact.getPerson();
						if (account != null && account.tValid()) {
							account.tDelete();
						}
					}
					if (object.tValid()) {
						object.tDelete();
					}
				}
				tx.commit();
			}
		});
		_created.clear();
		super.tearDown();
	}

	/**
	 * The page keeps the account commands of the base and replaces the creation and deletion of an
	 * account by the commands managing contacts and their logins.
	 */
	public void testCommands() {
		for (String name : List.of(NEW_PERSON, ADD_LOGIN, REMOVE_LOGIN, DELETE_CONTACT, "reset-password",
			"expire-password", "require-mfa", "reset-mfa")) {
			assertNotNull("Command " + name, command(name));
		}
		assertNull(command("new-account"));
		assertNull(command("delete-account"));
	}

	/**
	 * "New person" creates a persistent person contact without login from the dialog and selects
	 * it.
	 */
	public void testNewPerson() {
		ThreadContext.withoutSecurityChecks(() -> {
			assertSuccess(perform(NEW_PERSON));
			TLObject draft = (TLObject) dialogChannel(MODEL).get();
			assertTrue(draft.tTransient());
			draft.tUpdateByName(AbstractContact.NAME_ATTRIBUTE, "Created by the people page");
			draft.tUpdateByName(CreateAccountAction.LOGIN, LOGIN);

			_dialogs.click(CREATE);

			TLObject created = (TLObject) channel(SELECTED_CONTACT).get();
			assertNotNull("The new contact is selected.", created);
			_created.add(created);
			assertTrue(created instanceof PersonContact);
			assertFalse(created.tTransient());
			assertEquals("Created by the people page", created.tValueByName(AbstractContact.NAME_ATTRIBUTE));
			assertNull("No login without allowing it.", ((PersonContact) created).getPerson());
			assertNull(Person.byName(LOGIN));
		});
	}

	/**
	 * "New person" with "Allow login" creates the contact and its login in one step; no further
	 * contact is created for the login.
	 */
	public void testNewPersonWithLogin() {
		ThreadContext.withoutSecurityChecks(() -> {
			assertSuccess(perform(NEW_PERSON));
			TLObject draft = (TLObject) dialogChannel(MODEL).get();
			draft.tUpdateByName(AbstractContact.NAME_ATTRIBUTE, "With login");
			draft.tUpdateByName(CreateAccountAction.LOGIN, LOGIN);
			dialogChannel(ALLOW_LOGIN).set(Boolean.TRUE);

			_dialogs.click(CREATE);

			PersonContact created = (PersonContact) channel(SELECTED_CONTACT).get();
			assertNotNull("The new contact is selected.", created);
			_created.add(created);
			Person account = Person.byName(LOGIN);
			assertNotNull("The login is created with the person.", account);
			assertSame(created, account.getUser());
			assertSame(account, channel(SELECTED_ACCOUNT).get());
			assertEquals("No further contact for the login.", List.of(created), contactsWithLogin(LOGIN));
		});
	}

	/**
	 * "Add login" creates an account for the selected contact, which becomes the contact of the
	 * account; no further contact is created.
	 */
	public void testAddLogin() {
		ThreadContext.withoutSecurityChecks(() -> {
			PersonContact contact = newContact("Login");
			channel(SELECTED_CONTACT).set(contact);
			assertNull(channel(SELECTED_ACCOUNT).get());

			assertSuccess(perform(ADD_LOGIN));
			TLObject newAccount = (TLObject) dialogChannel(MODEL).get();
			assertSame(contact, newAccount.tValueByName(CreateAccountAction.INITIAL_DATA));
			newAccount.tUpdateByName(CreateAccountAction.LOGIN, LOGIN);

			_dialogs.click(CREATE);

			Person account = Person.byName(LOGIN);
			assertNotNull(account);
			assertSame(contact, account.getUser());
			assertEquals(LOGIN, contact.tValueByName(ContactFactory.LOGIN_NAME));
			assertEquals("No further contact for the login.", List.of(contact), contactsWithLogin(LOGIN));
			assertSame("The contact of the new account stays selected.", contact, channel(SELECTED_CONTACT).get());

			assertFalse("A person with login has no second login.", isExecutable(ADD_LOGIN));
		});
	}

	/**
	 * Selecting an account selects its contact, and the account is the login of the selected
	 * contact.
	 */
	public void testSelectingAccountSelectsContact() {
		ThreadContext.withoutSecurityChecks(() -> {
			PersonContact contact = newContact("Selected");
			Person account = newAccount(contact);

			channel(SELECTED_ACCOUNT).set(account);

			assertSame(contact, channel(SELECTED_CONTACT).get());
			assertSame(account, channel(SELECTED_ACCOUNT).get());
		});
	}

	/**
	 * "Remove login" deletes the account of the selected contact after a confirmation; the contact
	 * remains, and can be deleted afterwards only.
	 */
	public void testRemoveLogin() {
		ThreadContext.withoutSecurityChecks(() -> {
			PersonContact contact = newContact("Remove");
			Person account = newAccount(contact);
			channel(SELECTED_CONTACT).set(contact);
			assertSame(account, channel(SELECTED_ACCOUNT).get());
			assertFalse("A contact with login is not deleted.", isExecutable(DELETE_CONTACT));

			perform(REMOVE_LOGIN);
			_dialogs.confirm();

			assertFalse(account.tValid());
			assertTrue(contact.tValid());
			assertNull(contact.getPerson());
			assertEquals(LOGIN, contact.tValueByName(ContactFactory.LOGIN_NAME));
			assertTrue("The contact without login can be deleted.", isExecutable(DELETE_CONTACT));
		});
	}

	private PersonContact newContact(String name) {
		try (Transaction tx = kb().beginTransaction()) {
			PersonContact result = ContactFactory.getInstance().createNewPersonContact(name, null);
			tx.commit();
			_created.add(result);
			return result;
		}
	}

	private Person newAccount(PersonContact contact) {
		try (Transaction tx = kb().beginTransaction()) {
			Person result = Person.create(kb(), LOGIN,
				TLSecurityDeviceManager.getInstance().getDefaultAuthenticationDevice(), contact);
			tx.commit();
			return result;
		}
	}

	private static List<?> contactsWithLogin(String login) {
		return ContactFactory.getInstance().getAllContactsWithAttribute(ContactFactory.PERSON_TYPE,
			AbstractContact.FKEY_ATTRIBUTE, login, false);
	}

	private HandlerResult perform(String command) {
		ViewCommandModel model = command(command);
		assertNotNull("Command " + command, model);
		return model.perform(_page);
	}

	private boolean isExecutable(String command) {
		ViewCommandModel model = command(command);
		return model.executability(model.resolveInput()).isExecutable();
	}

	private static void assertSuccess(HandlerResult result) {
		assertTrue(String.valueOf(result), result.isSuccess());
	}

	private ViewChannel channel(String name) {
		return _page.resolveChannel(new ChannelRef(name));
	}

	private ViewChannel dialogChannel(String name) {
		return ((ViewContext) _dialogs.top().getReactContext()).resolveChannel(new ChannelRef(name));
	}

	/**
	 * The command with the given name of a command scope within the page, {@code null} if there
	 * is none.
	 */
	private ViewCommandModel command(String name) {
		return command(_view, name);
	}

	private static ViewCommandModel command(ReactControl control, String name) {
		if (control.getReactContext() instanceof ViewContext context) {
			CommandScope scope = context.getScope(CommandScope.class);
			if (scope != null) {
				CommandModel found = scope.resolveCommand(name);
				if (found instanceof ViewCommandModel result) {
					return result;
				}
			}
		}
		for (ReactControl child : control.displayedChildren()) {
			ViewCommandModel found = command(child, name);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Stands in for the dialogs of the window, holding the ones opened.
	 */
	private static class Dialogs implements DialogManager {

		private final List<ReactControl> _open = new ArrayList<>();

		@Override
		public DialogHandle openDialog(boolean closeOnBackdrop, ReactControl child,
				DialogResultHandler<Void> handler) {
			_open.add(child);
			// Displayed like a dialog of the window.
			child.attach();
			return null;
		}

		@Override
		public void closeTopDialog(DialogResult<Void> result) {
			_open.remove(_open.size() - 1).detach();
		}

		@Override
		public void closeDialogsAbove(DialogHandle dialog) {
			throw new UnsupportedOperationException("No dialog is stacked in these tests.");
		}

		ReactControl top() {
			assertFalse("A dialog is open.", _open.isEmpty());
			return _open.get(_open.size() - 1);
		}

		/** Clicks the button with one of the given labels in the topmost dialog. */
		void click(List<String> labels) {
			ReactButtonControl button = findButton(top(), labels);
			assertNotNull("The dialog offers " + labels + ".", button);
			assertTrue(button.executeCommand(CLICK, Map.of()).isSuccess());
		}

		/** Clicks the affirmative button of the confirmation in the topmost dialog. */
		void confirm() {
			List<ReactButtonControl> buttons = new ArrayList<>();
			collectButtons(top(), buttons);
			assertFalse("The confirmation offers buttons.", buttons.isEmpty());
			// The affirmative button follows the declining one.
			assertTrue(buttons.get(buttons.size() - 1).executeCommand(CLICK, Map.of()).isSuccess());
		}

		private static ReactButtonControl findButton(ReactControl control, List<String> labels) {
			if (control instanceof ReactButtonControl button
				&& labels.stream().anyMatch(button.scriptingScalarState()::containsValue)) {
				return button;
			}
			for (ReactControl child : control.displayedChildren()) {
				ReactButtonControl found = findButton(child, labels);
				if (found != null) {
					return found;
				}
			}
			return null;
		}

		private static void collectButtons(ReactControl control, List<ReactButtonControl> buttons) {
			if (control instanceof ReactButtonControl button) {
				buttons.add(button);
			}
			for (ReactControl child : control.displayedChildren()) {
				collectButtons(child, buttons);
			}
		}
	}

	/**
	 * The suite, starting the knowledge base with the application model and the services the page
	 * and its dialogs are built with.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestPeopleView.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE,
				SchedulerService.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE,
				LabelProviderService.Module.INSTANCE, FieldControlService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE, CommandGroupRegistry.Module.INSTANCE,
				CommandCliqueService.Module.INSTANCE, TimeZones.Module.INSTANCE));
	}
}

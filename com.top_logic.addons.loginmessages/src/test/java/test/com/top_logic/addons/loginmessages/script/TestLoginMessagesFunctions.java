/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.addons.loginmessages.script;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.DefaultTestFactory;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.service.AbstractKnowledgeBaseTest;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.addons.loginmessages.model.LoginMessagesUtil;
import com.top_logic.addons.loginmessages.model.LoginMessagesWrapperFactory;
import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.addons.loginmessages.script.LoginMessagesDisplay;
import com.top_logic.addons.loginmessages.script.LoginMessagesFunctions;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests {@link LoginMessagesFunctions}, the TL-Script functions showing the login messages to an
 * account and recording their confirmation.
 */
public class TestLoginMessagesFunctions extends AbstractKnowledgeBaseTest {

	private Person _p1;

	private Person _p2;

	private final List<LoginMessage> _messages = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_p1 = TestPerson.createPerson("p1");
		_p2 = TestPerson.createPerson("p2");
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = kb().beginTransaction()) {
			for (LoginMessage message : _messages) {
				message.tDelete();
			}
			tx.commit();
		}
		TestPerson.deletePersonAndUser(_p1);
		TestPerson.deletePersonAndUser(_p2);
		super.tearDown();
	}

	/**
	 * Tests that only active messages in their time interval are due, sorted by name in descending
	 * order.
	 */
	public void testDue() {
		LoginMessage a = message("a", true, null, null);
		LoginMessage b = message("b", true, DateUtil.addDays(now(), -1), DateUtil.addDays(now(), 1));
		message("inactive", false, null, null);
		message("ended", true, null, DateUtil.addDays(now(), -1));
		message("future", true, DateUtil.addDays(now(), 1), null);

		assertEquals(List.of(b, a), own(LoginMessagesFunctions.due(_p1)));
	}

	/**
	 * Tests that confirming a display confirms its messages for its account, and no longer shows
	 * them to it, while they are still due for other accounts.
	 */
	public void testConfirm() {
		LoginMessage a = message("a", true, null, null);
		LoginMessage b = message("b", true, null, null);

		LoginMessagesDisplay display = LoginMessagesFunctions.display(_p1);
		assertEquals(List.of(b, a), own(display.getMessages()));

		LoginMessagesFunctions.confirm(display);
		assertEquals(List.of(), own(LoginMessagesFunctions.due(_p1)));
		assertEquals(List.of(b, a), own(LoginMessagesFunctions.due(_p2)));
	}

	/**
	 * Tests that a display confirms only the messages it showed: a message becoming due after the
	 * display was made stays due.
	 */
	public void testConfirmOnlyShown() {
		message("a", true, null, null);
		LoginMessagesDisplay display = LoginMessagesFunctions.display(_p1);
		LoginMessage later = message("later", true, null, null);

		LoginMessagesFunctions.confirm(display);

		assertEquals(List.of(later), own(LoginMessagesFunctions.due(_p1)));
	}

	/**
	 * Tests that a confirmation expires after the confirmation duration of its message.
	 */
	public void testConfirmExpires() {
		LoginMessage a = message("a", true, null, null);
		try (Transaction tx = kb().beginTransaction()) {
			a.setConfirmDuration(2);
			tx.commit();
		}
		LoginMessagesFunctions.confirm(LoginMessagesFunctions.display(_p1));
		assertEquals(List.of(), own(LoginMessagesFunctions.due(_p1)));

		TLContextManager.inPersonContext(_p1, () -> {
			LoginMessagesUtil.setConfirmDate(PersonalConfiguration.getPersonalConfiguration(), a,
				DateUtil.addDays(now(), -2));
			PersonalConfiguration.storePersonalConfiguration();
		});
		assertEquals(List.of(a), own(LoginMessagesFunctions.due(_p1)));
	}

	/**
	 * Tests that a confirmation made before the session of the account starts - in a login step -
	 * reaches the personal configuration of the session and survives when the session writes its
	 * personal configuration back.
	 */
	public void testConfirmSurvivesSession() {
		LoginMessage a = message("a", true, null, null);

		LoginMessagesFunctions.confirm(LoginMessagesFunctions.display(_p1));

		TLContextManager.inPersonContext(_p1, () -> {
			PersonalConfiguration session = PersonalConfiguration.getPersonalConfiguration();
			assertFalse("The session sees the confirmation.", LoginMessagesUtil.isConfirmExpired(session, a));

			// The session changes something else and writes its configuration back, as at logout.
			session.setValue("testSetting", "value");
			PersonalConfiguration.storePersonalConfiguration();
		});

		assertEquals(List.of(), own(LoginMessagesFunctions.due(_p1)));
	}

	/**
	 * Tests that a display without messages confirms nothing.
	 */
	public void testConfirmNothing() {
		LoginMessagesDisplay display = LoginMessagesFunctions.display(_p1);
		assertTrue(own(display.getMessages()).isEmpty());

		LoginMessage a = message("a", true, null, null);
		LoginMessagesFunctions.confirm(display);

		assertEquals(List.of(a), own(LoginMessagesFunctions.due(_p1)));
	}

	/**
	 * Tests the HTML of a display: the text of a message, and the name, escaped, of a message
	 * without text.
	 */
	public void testDisplayHtml() throws IOException {
		LoginMessage withText = message("b", true, null, null);
		message("a<b", true, null, null);
		try (Transaction tx = kb().beginTransaction()) {
			withText.setMessage(new I18NStructuredText(
				Map.of(Locale.ENGLISH, new StructuredText("<p>Hello</p>"), Locale.GERMAN,
					new StructuredText("<p>Hallo</p>"))));
			tx.commit();
		}

		String html = html(LoginMessagesFunctions.display(_p1));

		assertTrue(html, html.contains("<p>Hello</p>") || html.contains("<p>Hallo</p>"));
		assertTrue(html, html.contains("a&lt;b"));
		assertTrue("Sorted by name in descending order: " + html,
			html.indexOf("Hallo") >= 0 ? html.indexOf("Hallo") < html.indexOf("a&lt;b")
				: html.indexOf("Hello") < html.indexOf("a&lt;b"));
	}

	/**
	 * Tests that the HTML of a display carries the images of the messages it shows, and that the
	 * images of a message that is not due are not part of it.
	 */
	public void testDisplayImages() throws IOException {
		LoginMessage due = message("due", true, null, null);
		LoginMessage inactive = message("inactive", false, null, null);
		try (Transaction tx = kb().beginTransaction()) {
			due.setMessage(withImage("shown.png", new byte[] { 1, 2, 3 }));
			inactive.setMessage(withImage("hidden.png", new byte[] { 7, 8, 9 }));
			tx.commit();
		}

		String html = html(LoginMessagesFunctions.display(_p1));

		String shown = "data:image/png;base64," + Base64.getEncoder().encodeToString(new byte[] { 1, 2, 3 });
		String hidden = Base64.getEncoder().encodeToString(new byte[] { 7, 8, 9 });
		assertTrue(html, html.contains(shown));
		assertFalse(html, html.contains(hidden));
		assertFalse(html, html.contains("hidden.png"));
	}

	private static I18NStructuredText withImage(String name, byte[] content) {
		StructuredText text = new StructuredText("<p><img src=\"ref:" + name + "\" /></p>",
			Map.of(name, BinaryDataFactory.createBinaryData(content, "image/png", name)));
		return new I18NStructuredText(Map.of(Locale.ENGLISH, text, Locale.GERMAN, text));
	}

	private String html(LoginMessagesDisplay display) throws IOException {
		StringWriter buffer = new StringWriter();
		try (TagWriter out = new TagWriter(buffer)) {
			TLContextManager.inPersonContext(_p1, () -> {
				try {
					display.write(null, out);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
			});
		}
		return buffer.toString();
	}

	/**
	 * The messages of the given ones this test created, in their order: the knowledge base is shared
	 * with other tests, whose messages are no concern of this one.
	 */
	private List<LoginMessage> own(List<LoginMessage> messages) {
		List<LoginMessage> result = new ArrayList<>(messages);
		result.retainAll(_messages);
		return result;
	}

	private LoginMessage message(String name, boolean active, Date start, Date end) {
		LoginMessage message;
		try (Transaction tx = kb().beginTransaction()) {
			message = LoginMessagesWrapperFactory.getInstance().createLoginMessage();
			message.setName(name);
			message.setActive(active);
			message.setStartDate(start);
			message.setEndDate(end);
			tx.commit();
		}
		_messages.add(message);
		return message;
	}

	private static Date now() {
		return new Date();
	}

	/**
	 * The suite of this test, with the model and the accounts.
	 */
	public static Test suite() {
		TestFactory f = DefaultTestFactory.INSTANCE;
		f = ServiceTestSetup.createStarterFactory(ModelService.Module.INSTANCE, f);

		return PersonManagerSetup.createPersonManagerSetup(TestLoginMessagesFunctions.class, f);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import junit.framework.Test;

import jakarta.mail.Address;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Provider;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.Transport;
import jakarta.mail.URLName;

import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.basic.ConfigurationEncryption;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.credential.NamedSecretSource;
import com.top_logic.basic.credential.SecretStoreService;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.mail.proxy.MailReceiverService;

/**
 * Tests the passwords of {@link MailSenderService} and {@link MailReceiverService} given by a
 * secret source: the attribute forms, the reference to an environment variable read at each new
 * connection, and the written form of the configuration.
 *
 * <p>
 * The mail session of the services is directed to a {@link RecordingTransport} or
 * {@link RecordingStore} by selecting their protocol, which is registered by the
 * {@link RecordingTransportProvider} and the {@link RecordingStoreProvider} as services of the
 * tests. They record the password of each
 * connection instead of contacting a server.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestMailPasswordSources extends AbstractTypedConfigurationTestCase {

	private static final String ROOT_TAG = "config";

	/** The system property holding the password in the tests referencing a variable. */
	private static final String VARIABLE = "test_mail_password_sources_password";

	private static final String PASSWORD = "first-password";

	private static final String ROTATED_PASSWORD = "second-password";

	private static final String TRANSPORT_PROTOCOL = "recordingsmtp";

	private static final String STORE_PROTOCOL = "recordingimap";

	/** Passwords of the connections opened by a {@link RecordingTransport} or {@link RecordingStore}. */
	static final List<String> PASSWORDS = new CopyOnWriteArrayList<>();

	public interface Config extends ConfigurationItem {

		String SENDER = "sender";

		String RECEIVER = "receiver";

		@Name(SENDER)
		MailSenderService.Config getSender();

		@Name(RECEIVER)
		MailReceiverService.Config getReceiver();

	}

	private static final String VENDOR = "Business Operation Systems GmbH";

	/**
	 * {@link Provider} of the {@link RecordingTransport}.
	 */
	public static class RecordingTransportProvider extends Provider {

		public RecordingTransportProvider() {
			super(Provider.Type.TRANSPORT, TRANSPORT_PROTOCOL, RecordingTransport.class.getName(), VENDOR, null);
		}

	}

	/**
	 * {@link Provider} of the {@link RecordingStore}.
	 */
	public static class RecordingStoreProvider extends Provider {

		public RecordingStoreProvider() {
			super(Provider.Type.STORE, STORE_PROTOCOL, RecordingStore.class.getName(), VENDOR, null);
		}

	}

	/**
	 * {@link Transport} recording the password it is connected with.
	 */
	public static class RecordingTransport extends Transport {

		public RecordingTransport(Session session, URLName url) {
			super(session, url);
		}

		@Override
		protected boolean protocolConnect(String host, int port, String user, String password) {
			PASSWORDS.add(password);
			return true;
		}

		@Override
		public void sendMessage(Message message, Address[] addresses) {
			// Nothing to send to.
		}

	}

	/**
	 * {@link Store} recording the password it is connected with.
	 */
	public static class RecordingStore extends Store {

		public RecordingStore(Session session, URLName url) {
			super(session, url);
		}

		@Override
		protected boolean protocolConnect(String host, int port, String user, String password) {
			PASSWORDS.add(password);
			return true;
		}

		@Override
		public Folder getDefaultFolder() throws MessagingException {
			throw new MessagingException("No folders.");
		}

		@Override
		public Folder getFolder(String name) throws MessagingException {
			throw new MessagingException("No folders.");
		}

		@Override
		public Folder getFolder(URLName url) throws MessagingException {
			throw new MessagingException("No folders.");
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		PASSWORDS.clear();
	}

	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(VARIABLE);
		PASSWORDS.clear();
		super.tearDown();
	}

	/** The SMTP password attribute with prefix <code>unencrypted:</code> keeps working. */
	public void testSenderUnencryptedAttribute() throws Exception {
		assertEquals(List.of(PASSWORD), senderPasswords("password='unencrypted:" + PASSWORD + "'", ""));
	}

	/** An encrypted SMTP password attribute keeps working. */
	public void testSenderEncryptedAttribute() throws Exception {
		assertEquals(List.of(PASSWORD),
			senderPasswords("password='" + ConfigurationEncryption.encrypt(PASSWORD) + "'", ""));
	}

	/** Without password, the SMTP connection is opened with an empty password, as before. */
	public void testSenderNoPassword() throws Exception {
		assertEquals(List.of(""), senderPasswords("password=''", ""));
	}

	/** The SMTP password from a variable is read for each new connection. */
	public void testSenderEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		MailSenderService sender = getInstance(readConfig(xml(sender("", environmentPassword()), "")).getSender());
		start(sender);
		try {
			sender.reload();
			System.setProperty(VARIABLE, ROTATED_PASSWORD);
			sender.reload();
		} finally {
			stop(sender);
		}
		assertEquals(List.of(PASSWORD, ROTATED_PASSWORD), PASSWORDS);
	}

	/** The IMAP password attribute with prefix <code>unencrypted:</code> keeps working. */
	public void testReceiverUnencryptedAttribute() throws Exception {
		assertEquals(List.of(PASSWORD), receiverPasswords("password='unencrypted:" + PASSWORD + "'", ""));
	}

	/** An encrypted IMAP password attribute keeps working. */
	public void testReceiverEncryptedAttribute() throws Exception {
		assertEquals(List.of(PASSWORD),
			receiverPasswords("password='" + ConfigurationEncryption.encrypt(PASSWORD) + "'", ""));
	}

	/** The IMAP password from a variable is read for each login. */
	public void testReceiverEnvironmentRotation() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		MailReceiverService receiver =
			getInstance(readConfig(xml("", receiver("", environmentPassword()))).getReceiver());
		receiver.getServerInstance().login();
		System.setProperty(VARIABLE, ROTATED_PASSWORD);
		receiver.getServerInstance().login();

		assertEquals(List.of(PASSWORD, ROTATED_PASSWORD), PASSWORDS);
	}

	/** References to named secrets are written as elements without class holding only the name. */
	public void testWriteEnvironmentPasswords() throws Exception {
		System.setProperty(VARIABLE, PASSWORD);
		Config config = read(xml(sender("", environmentPassword()), receiver("", environmentPassword())));

		String written = toXML(config);
		assertFalse("Password written: " + written, written.contains(PASSWORD));
		assertTrue("Not written in short form: " + written,
			written.contains("<" + MailSenderService.Config.PASSWORD + " name=\"" + VARIABLE + "\""));

		Config copy = (Config) fromXML(written);
		assertTrue(copy.getSender().getPassword() instanceof NamedSecretSource.Config);
		assertTrue(copy.getReceiver().getPassword() instanceof NamedSecretSource.Config);
		assertEquals(written, toXML(copy));
	}

	/** Configured passwords are written encrypted. */
	public void testWriteLiteralPasswords() throws Exception {
		String attribute = "password='unencrypted:" + PASSWORD + "'";
		Config config = read(xml(sender(attribute, ""), receiver(attribute, "")));

		String written = toXML(config);
		assertFalse("Password written in plain text: " + written, written.contains(PASSWORD));
		assertTrue("Not written as encrypted attribute: " + written,
			written.contains(MailSenderService.Config.PASSWORD + "=\"" + ConfigurationEncryption.AUTHENTICATED_PREFIX));

		Config copy = (Config) fromXML(written);
		assertEquals(config, copy);
	}

	private List<String> senderPasswords(String attributes, String content) throws Exception {
		MailSenderService sender = getInstance(readConfig(xml(sender(attributes, content), "")).getSender());
		start(sender);
		try {
			sender.reload();
		} finally {
			stop(sender);
		}
		return List.copyOf(PASSWORDS);
	}

	private List<String> receiverPasswords(String attributes, String content) throws Exception {
		MailReceiverService receiver = getInstance(readConfig(xml("", receiver(attributes, content))).getReceiver());
		receiver.getServerInstance().login();
		return List.copyOf(PASSWORDS);
	}

	private Config readConfig(String xml) throws ConfigurationException {
		return read(xml);
	}

	private static String xml(String sender, String receiver) {
		return "<" + ROOT_TAG + ">" + sender + receiver + "</" + ROOT_TAG + ">";
	}

	private static String sender(String attributes, String content) {
		return "<" + Config.SENDER + " class='" + MailSenderService.class.getName() + "'"
			+ " activated='true' server='localhost' from-address='app@localhost' user='app' " + attributes + ">"
			+ content
			+ "<options>"
			+ "<option name='mail.transport.protocol' value='" + TRANSPORT_PROTOCOL + "'/>"
			+ "</options>"
			+ "</" + Config.SENDER + ">";
	}

	private static String receiver(String attributes, String content) {
		return "<" + Config.RECEIVER + " class='" + MailReceiverService.class.getName() + "'"
			+ " activated='true' server='localhost' user='app' " + attributes + ">"
			+ content
			+ "<options>"
			+ "<option name='mail.store.protocol' value='" + STORE_PROTOCOL + "'/>"
			+ "</options>"
			+ "</" + Config.RECEIVER + ">";
	}

	private static String environmentPassword() {
		return "<password name='" + VARIABLE + "'/>";
	}

	/**
	 * Starts the given service without registering it as module, so that it opens connections.
	 */
	private static void start(ManagedClass service) throws Exception {
		lifecycleMethod("startUp").invoke(service);
	}

	private static void stop(ManagedClass service) throws Exception {
		lifecycleMethod("shutDown").invoke(service);
	}

	private static Method lifecycleMethod(String name) throws NoSuchMethodException {
		Method result = ManagedClass.class.getDeclaredMethod(name);
		result.setAccessible(true);
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.singletonMap(ROOT_TAG, TypedConfiguration.getConfigurationDescriptor(Config.class));
	}

	public static Test suite() {
		return suite(TestMailPasswordSources.class, SecretStoreService.Module.INSTANCE);
	}

}

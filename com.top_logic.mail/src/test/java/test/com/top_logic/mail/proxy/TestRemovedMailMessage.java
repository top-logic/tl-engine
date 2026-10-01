/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.util.Properties;

import jakarta.mail.Flags;
import jakarta.mail.Flags.Flag;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.LogListeningTestCase;

import com.top_logic.mail.proxy.AbstractMailServerMessage;
import com.top_logic.mail.proxy.MailServerMessage;
import com.top_logic.mail.proxy.exchange.ExchangeMail;

/**
 * Test that a {@link MailServerMessage} whose message was removed from its folder by another client
 * is handled without logging an error.
 *
 * <p>
 * {@link LogListeningTestCase} fails each test that logs an error or a warning.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestRemovedMailMessage extends LogListeningTestCase {

	private static final int MESSAGE_NUMBER = 54;

	private static final String MESSAGE_ID = "<test-message@top-logic.com>";

	public void testExpungedMessage() {
		ExchangeMail mail = new ExchangeMail(new TestingMessage(true, true));

		assertTrue(mail.isRemoved());
		assertEquals(Integer.toString(MESSAGE_NUMBER), mail.getID());
		assertNotNull(mail.getName());
		assertFalse(mail.setFlag(Flag.DELETED, true));
	}

	public void testRemovedOnServerOnly() {
		// The message is not yet known to be expunged locally, but each server access fails.
		ExchangeMail mail = new ExchangeMail(new TestingMessage(false, true));

		assertFalse(mail.isRemoved());
		assertEquals(Integer.toString(MESSAGE_NUMBER), mail.getID());
		assertNotNull(mail.getName());
		assertFalse(mail.setFlag(Flag.DELETED, true));
	}

	public void testAvailableMessage() throws MessagingException {
		TestingMessage message = new TestingMessage(false, false);
		ExchangeMail mail = new ExchangeMail(message);

		assertFalse(mail.isRemoved());
		assertEquals(MESSAGE_ID, mail.getID());
		assertEquals("Subject", mail.getName());
		assertTrue(mail.setFlag(Flag.DELETED, true));
		assertTrue(message.isSet(Flag.DELETED));
	}

	/**
	 * {@link MimeMessage} that simulates a message in a mail folder, optionally removed from that
	 * folder by another client.
	 */
	private static class TestingMessage extends MimeMessage {

		private final boolean _expunged;

		private final boolean _removed;

		TestingMessage(boolean expunged, boolean removed) {
			super(Session.getInstance(new Properties()));
			_expunged = expunged;
			_removed = removed;
			setMessageNumber(MESSAGE_NUMBER);
			try {
				super.setHeader(AbstractMailServerMessage.MAIL_ID, MESSAGE_ID);
				super.setSubject("Subject");
			} catch (MessagingException ex) {
				throw new AssertionError(ex);
			}
		}

		@Override
		public boolean isExpunged() {
			return _expunged;
		}

		@Override
		public String[] getHeader(String name) throws MessagingException {
			checkRemoved();
			return super.getHeader(name);
		}

		@Override
		public String getSubject() throws MessagingException {
			checkRemoved();
			return super.getSubject();
		}

		@Override
		public synchronized void setFlags(Flags flag, boolean set) throws MessagingException {
			checkRemoved();
			super.setFlags(flag, set);
		}

		private void checkRemoved() throws MessageRemovedException {
			if (_removed) {
				throw new MessageRemovedException("Message " + MESSAGE_NUMBER + " was removed.");
			}
		}
	}

	public static Test suite() {
		return new TestSuite(TestRemovedMailMessage.class);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.addons.loginmessages.script;

import static com.top_logic.mig.html.HTMLConstants.*;

import java.io.IOException;
import java.util.List;

import com.top_logic.addons.loginmessages.model.intf.LoginMessage;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextUtil;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;

/**
 * The login messages due for an account, as they are shown to it at its login.
 *
 * <p>
 * Created by {@link LoginMessagesFunctions#display(Person)} only, for the messages due at that
 * moment. As an {@link HTMLFragment} it writes what the account is shown - the text of each message
 * in the current language with its images, or its name where it has no text, one below the other.
 * Passed to {@link LoginMessagesFunctions#confirm(LoginMessagesDisplay)}, it confirms exactly these
 * messages for its account: a script cannot confirm other messages, or confirm for another account, through
 * it.
 * </p>
 *
 * <p>
 * The value is no model object, so a script running on behalf of a user who may not read the login
 * messages - the anonymous user of a login that has not completed - still receives it.
 * </p>
 *
 * <p>
 * The images of a message are written into its HTML as <code>data:</code> URIs, so that showing
 * them needs no further request: the display carries the images of the messages it shows, and no
 * others.
 * </p>
 *
 * @implNote The HTML of a text with its images inlined is
 *           {@link StructuredTextUtil#getCodeWithInlinedImages(StructuredText)}.
 */
public final class LoginMessagesDisplay implements HTMLFragment {

	/** CSS class of the element holding one message. */
	public static final String MESSAGE_CSS_CLASS = "tlLoginMessage";

	private final Person _account;

	private final List<LoginMessage> _messages;

	LoginMessagesDisplay(Person account, List<LoginMessage> messages) {
		_account = account;
		_messages = List.copyOf(messages);
	}

	/**
	 * The account the messages are shown to.
	 */
	public Person getAccount() {
		return _account;
	}

	/**
	 * The messages shown, in the order they are shown.
	 */
	public List<LoginMessage> getMessages() {
		return _messages;
	}

	/**
	 * Whether there is no message to show.
	 */
	public boolean isEmpty() {
		return _messages.isEmpty();
	}

	@Override
	public void write(DisplayContext context, TagWriter out) throws IOException {
		for (LoginMessage message : _messages) {
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, MESSAGE_CSS_CLASS);
			out.endBeginTag();
			StructuredText text = localized(message.getMessage());
			if (text != null) {
				out.writeContent(StructuredTextUtil.getCodeWithInlinedImages(text));
			} else {
				out.beginTag(PARAGRAPH);
				out.writeText(message.getName());
				out.endTag(PARAGRAPH);
			}
			out.endTag(DIV);
		}
	}

	private static StructuredText localized(I18NStructuredText message) {
		if (message == null) {
			return null;
		}
		StructuredText text = message.localize();
		if (text == null || text.getSourceCode() == null || text.getSourceCode().isBlank()) {
			return null;
		}
		return text;
	}

}

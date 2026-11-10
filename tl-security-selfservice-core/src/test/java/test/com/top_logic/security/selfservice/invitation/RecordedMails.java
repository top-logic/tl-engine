/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.security.selfservice.invitation;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

/**
 * TL-Script function recording the mails the self-service would send, for the mail scripts of the
 * test configuration.
 */
@ScriptPrefix(RecordedMails.PREFIX)
public class RecordedMails extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "testSelfService";

	/**
	 * A recorded mail.
	 *
	 * @param kind
	 *        Which mail script sent the mail.
	 * @param to
	 *        The address of the mail.
	 * @param argument
	 *        The code or link of the mail, <code>null</code> if it has none.
	 */
	public record Mail(String kind, String to, String argument) {
		// Pure data.
	}

	private static final List<Mail> MAILS = new ArrayList<>();

	/**
	 * Records a mail instead of sending it.
	 *
	 * @param kind
	 *        Which mail script sends the mail.
	 * @param to
	 *        The address of the mail.
	 * @param argument
	 *        The code or link of the mail.
	 * @return <code>true</code>, the mail counts as sent.
	 */
	public static boolean mail(String kind, String to, String argument) {
		synchronized (MAILS) {
			MAILS.add(new Mail(kind, to, argument));
		}
		return true;
	}

	/**
	 * Removes and returns the mails recorded so far.
	 */
	public static List<Mail> take() {
		synchronized (MAILS) {
			List<Mail> result = new ArrayList<>(MAILS);
			MAILS.clear();
			return result;
		}
	}

}

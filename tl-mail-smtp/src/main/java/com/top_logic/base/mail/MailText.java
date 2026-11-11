/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.values.edit.annotation.Html;

/**
 * Configuration of the internationalized texts of a mail: its subject and its body.
 *
 * <p>
 * The texts may contain placeholders <code>{0}</code>, <code>{1}</code>, ... that the sender of
 * the mail fills. Which values the placeholders stand for is described by the option that uses this
 * configuration.
 * </p>
 */
@DisplayOrder({
	MailText.SUBJECT,
	MailText.BODY,
})
public interface MailText extends ConfigurationItem {

	/** Configuration name for {@link #getSubject()}. */
	String SUBJECT = "subject";

	/** Configuration name for {@link #getBody()}. */
	String BODY = "body";

	/**
	 * The subject of the mail.
	 */
	@Name(SUBJECT)
	@Mandatory
	ResKey getSubject();

	/**
	 * The content of the mail as HTML.
	 */
	@Name(BODY)
	@Mandatory
	@Html
	ResKey getBody();

}

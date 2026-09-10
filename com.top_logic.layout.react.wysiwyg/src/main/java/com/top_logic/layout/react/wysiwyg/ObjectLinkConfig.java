/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;

/**
 * Where the editor gets the object a link is inserted for.
 *
 * <p>
 * The user picks the object in a dialog of the application's own making, which publishes the
 * chosen object on a channel. Anything can be picked and in any way, as long as the dialog writes
 * the object to that channel: a table of the objects in question, a search, a tree.
 * </p>
 *
 * @see WysiwygControlProvider
 */
public interface ObjectLinkConfig extends ConfigurationItem {

	/** @see #getDialogView() */
	String DIALOG_VIEW = "dialog-view";

	/** @see #getResultChannel() */
	String RESULT_CHANNEL = "result-channel";

	/** Default value of {@link #getResultChannel()}. */
	String RESULT_CHANNEL_DEFAULT = "result";

	/**
	 * Path of the view the object is picked in, relative to the view base path.
	 */
	@Name(DIALOG_VIEW)
	@Mandatory
	String getDialogView();

	/** @see #getDialogView() */
	void setDialogView(String value);

	/**
	 * Name of the channel the dialog publishes the picked object on.
	 *
	 * <p>
	 * The editor inserts a link as soon as an object appears on that channel, so the dialog writes
	 * it when the user has decided - not on every step of the way there. A dialog that does not
	 * close itself when it publishes its result is closed once the link is in the text.
	 * </p>
	 */
	@Name(RESULT_CHANNEL)
	@StringDefault(RESULT_CHANNEL_DEFAULT)
	String getResultChannel();

	/** @see #getResultChannel() */
	void setResultChannel(String value);

}

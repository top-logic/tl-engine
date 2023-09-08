/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Command used to inform the user that the object corresponding to some bookmark could not be found
 * for some reason.
 * 
 * @since 5.7.1
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class BookmarkNotFoundNotification extends AbstractCommandHandler {

	/** arguments used to resolve the bookmark argument */
	private Object _bookmarkArguments;

	/** command to execute after confirm */
	private Command _continutation;

	/**
	 * Creates a new {@link BookmarkNotFoundNotification}.
	 */
	public BookmarkNotFoundNotification(InstantiationContext context, Config config) {
		super(context, config);
	}

	public void setBookmarkArguments(Object bookmarkArguments) {
		_bookmarkArguments = bookmarkArguments;
	}

	public void setContinutation(Command continutation) {
		_continutation = continutation;
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		CommandModel button = MessageBox.button(ButtonType.OK, _continutation);
		Resources resources = aContext.getResources();
		String confirmText;
		if (_bookmarkArguments == null) {
			confirmText = resources.getString(I18NConstants.BOOKMARK_NOT_FOUND);
		} else {
			confirmText = resources.getString(I18NConstants.BOOKMARK_NOT_FOUND__BOOKMARKARGS.fill(_bookmarkArguments));
		}
		return MessageBox.confirm(aContext, MessageType.INFO, confirmText, button);
	}

	/**
	 * Creates a {@link BookmarkNotFoundNotification}
	 * 
	 * @param commandId
	 *        The {@link CommandHandler#getID() id} of the command
	 * @param bookmarkArguments
	 *        The arguments used to resolve the object of the bookmark.
	 * @param continuation
	 *        A command to be executed when the user confirms the message. Must not be
	 *        <code>null</code>.
	 */
	public static BookmarkNotFoundNotification newInstance(String commandId, Object bookmarkArguments,
			Command continuation) {
		BookmarkNotFoundNotification result = newInstance(BookmarkNotFoundNotification.class, commandId);
		result.setBookmarkArguments(bookmarkArguments);
		result.setContinutation(continuation);
		return result;
	}

}

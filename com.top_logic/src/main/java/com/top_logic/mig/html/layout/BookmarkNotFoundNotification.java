/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
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
public final class BookmarkNotFoundNotification implements Command {

	/** arguments used to resolve the bookmark argument */
	private Object _bookmarkArguments;

	/** command to execute after confirm */
	private Command _continutation;

	/**
	 * Sets the arguments that were used to find bookmark object.
	 * 
	 * @param bookmarkArguments
	 *        Object to include into the error message. May be <code>null</code>.
	 */
	public void setBookmarkArguments(Object bookmarkArguments) {
		_bookmarkArguments = bookmarkArguments;
	}

	/**
	 * Sets the command to execute when message was confirmed. May be <code>null</code>.
	 */
	public void setContinutation(Command continutation) {
		_continutation = continutation;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		CommandModel button = MessageBox.button(ButtonType.OK, nonNull(_continutation));
		Resources resources = context.getResources();
		String confirmText;
		if (_bookmarkArguments == null) {
			confirmText = resources.getString(I18NConstants.BOOKMARK_NOT_FOUND);
		} else {
			confirmText = resources.getString(I18NConstants.BOOKMARK_NOT_FOUND__BOOKMARKARGS.fill(_bookmarkArguments));
		}
		return MessageBox.confirm(context, MessageType.INFO, confirmText, button);
	}

	private static Command nonNull(Command continutation) {
		return continutation != null ? continutation : Command.DO_NOTHING;
	}

	/**
	 * Creates a {@link BookmarkNotFoundNotification}
	 * 
	 * @param bookmarkArguments
	 *        The arguments used to resolve the object of the bookmark.
	 * @param continuation
	 *        A command to be executed when the user confirms the message.
	 */
	public static BookmarkNotFoundNotification newInstance(Object bookmarkArguments, Command continuation) {
		BookmarkNotFoundNotification result = new BookmarkNotFoundNotification();
		result.setBookmarkArguments(bookmarkArguments);
		result.setContinutation(continuation);
		return result;
	}

}

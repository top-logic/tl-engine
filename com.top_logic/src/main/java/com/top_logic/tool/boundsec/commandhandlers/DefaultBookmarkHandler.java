/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Collection;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.ObjectNotFound;

/**
 * {@link CommandHandler} that resolves a stable link (a bookmark) and displays the bookmarked
 * object.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultBookmarkHandler extends GotoHandler {

	/**
	 * Configuration of the {@link DefaultBookmarkHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends GotoHandler.Config {

		// No special properties here

	}

	/**
	 * Name under which the handler must be registered in the {@link CommandHandlerFactory} that
	 * should resolve bookmark links.
	 */
	public static final String COMMAND_RESOLVE_BOOKMARK = "resolveBookmark";

	/**
	 * Creates a {@link DefaultBookmarkHandler}.
	 */
	public DefaultBookmarkHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * The object the bookmark is leading to.
	 * 
	 * @param someArguments
	 *        Information of the object.
	 * @return Object of the bookmark.
	 * 
	 * @throws ObjectNotFound
	 *         When informations the arguments should have lead to a valid object but it can not be
	 *         resolved, i.e. the informations are appropriate for some {@link BookmarkHandler} but
	 *         are invalid.
	 */
	public Object getBookmarkObject(Map<String, Object> someArguments) throws ObjectNotFound {
		return getObject(someArguments);
	}

	@Override
	protected Object getObject(Map<String, Object> someArguments) {
		BookmarkService bms = BookmarkService.getInstance();
		Collection<BookmarkHandler> specialProviders = bms.getSpecialisedHandlers();
		for (BookmarkHandler provider : specialProviders) {
			Object targetObject = provider.getBookmarkObject(someArguments);
			if (targetObject != null) {
				return targetObject;
			}
		}
		BookmarkHandler defaultProvider = bms.getDefaultHandler();
		Object target = defaultProvider.getBookmarkObject(someArguments);
		if (target != null) {
			return target;
		}
		return null;
	}

}

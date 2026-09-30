/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.wysiwyg;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.wysiwyg.ui.TLObjectLinkUtil;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.ObjectNotFound;

/**
 * Follows the {@link TLObjectLinkUtil#TL_OBJECT object links} embedded in displayed application
 * HTML.
 *
 * <p>
 * Such a link names its target object in its {@code href}, as
 * {@link TLObjectLinkUtil#getLinkDestination(TLObject, String)} wrote it. Following the link resolves
 * that description back to the object and hands it to the {@link ObjectNavigator} of the context,
 * which leads to the place the application displays it at. Whatever stands in the way - a link to
 * an object that is gone, an object with no place of its own - is told to the user.
 * </p>
 */
public final class ObjectLinks {

	private ObjectLinks() {
		// Only static methods.
	}

	/**
	 * Displays the object the given link points at.
	 *
	 * @param context
	 *        Where the click came from; decides which of several places is the nearest one.
	 * @param href
	 *        The {@code href} of the followed link, as
	 *        {@link TLObjectLinkUtil#getLinkDestination(TLObject, String)} wrote it.
	 */
	public static void follow(ReactContext context, String href) {
		Wrapper target;
		try {
			target = href == null ? null : TLObjectLinkUtil.getObject(href);
		} catch (ObjectNotFound problem) {
			ViewMessages.error(context, I18NConstants.ERROR_LINKED_OBJECT_NOT_FOUND, problem.getErrorKey());
			return;
		}
		if (target == null) {
			ViewMessages.error(context, I18NConstants.ERROR_LINKED_OBJECT_NOT_FOUND);
			return;
		}
		show(context, target);
	}

	/**
	 * Displays the given object where the application displays objects of its kind, telling the
	 * user when there is no such place.
	 *
	 * @param context
	 *        Where the request comes from.
	 * @param object
	 *        The object to display.
	 */
	public static void show(ReactContext context, Object object) {
		ObjectNavigator navigator = context == null ? null : context.getObjectNavigator();
		if (navigator == null || !navigator.canShow(object)) {
			ViewMessages.error(context, I18NConstants.ERROR_LINKED_OBJECT_NOT_DISPLAYED);
			return;
		}
		navigator.show(context, object);
	}

}

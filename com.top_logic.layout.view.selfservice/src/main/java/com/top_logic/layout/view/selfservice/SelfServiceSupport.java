/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.selfservice;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * Helpers of the self-service actions: the values their dialogs carry on channels, and the
 * transient objects their forms edit.
 */
final class SelfServiceSupport {

	private SelfServiceSupport() {
		// Static helpers only.
	}

	/**
	 * The value of the channel with the given name in the view of the given context.
	 *
	 * @return The value, or <code>null</code> if the view has no such channel.
	 */
	static Object channel(ReactContext context, String name) {
		if (!(context instanceof ViewContext)) {
			return null;
		}
		ViewContext viewContext = (ViewContext) context;
		if (!viewContext.hasChannel(name)) {
			return null;
		}
		return viewContext.resolveChannel(new ChannelRef(name)).get();
	}

	/**
	 * A new transient object of the model type with the given qualified name.
	 */
	static TLObject newTransient(String typeName) {
		TLClass type = (TLClass) TLModelUtil.findType(typeName);
		return TransientObjectFactory.INSTANCE.createObject(type);
	}

	/**
	 * The value of the attribute with the given name of the given object as string.
	 */
	static String stringValue(TLObject object, String attribute) {
		Object value = object.tValueByName(attribute);
		return value == null ? null : value.toString();
	}

	/**
	 * Whether the given string is <code>null</code> or empty.
	 */
	static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

}

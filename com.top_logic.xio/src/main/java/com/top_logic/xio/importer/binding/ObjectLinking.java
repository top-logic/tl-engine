/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.binding;

import com.top_logic.xio.importer.handlers.Handler;

/**
 * Algorithm for linking an inner object to it's context.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectLinking {

	/**
	 * Links the given target to the given scope.
	 *
	 * @param context
	 *        See {@link Handler#importXml(ImportContext, javax.xml.stream.XMLStreamReader)}.
	 * @param scope
	 *        The context object to link the given target to.
	 * @param target
	 *        The target object to link.
	 * @param continuation
	 *        How to continue, if this algorithm decides that it cannot link the given target to its
	 *        scope.
	 */
	void linkOrElse(ImportContext context, Object scope, Object target, Runnable continuation);

}

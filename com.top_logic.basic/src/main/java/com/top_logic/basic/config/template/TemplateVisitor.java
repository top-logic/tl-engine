/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.template;


/**
 * Visitor interface for all kinds of {@link TemplateExpression}s.
 * 
 * @param <R>
 *        The visit result.
 * @param <A>
 *        The visit argument.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateVisitor<R, A, E extends Throwable>
		extends TemplateStructureVisitor<R, A, E>, ConfigExpressionVisitor<R, A, E> {

	// Pure sum interface.

}

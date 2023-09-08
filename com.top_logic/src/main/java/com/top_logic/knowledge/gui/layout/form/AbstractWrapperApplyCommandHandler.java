/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.form;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link CommandHandler} that controls a {@link KnowledgeBase#commit() transaction}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Subclass {@link AbstractApplyCommandHandler} directly.
 */
@Deprecated
public abstract class AbstractWrapperApplyCommandHandler extends AbstractApplyCommandHandler {

	/**
	 * Creates a {@link AbstractWrapperApplyCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractWrapperApplyCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

}

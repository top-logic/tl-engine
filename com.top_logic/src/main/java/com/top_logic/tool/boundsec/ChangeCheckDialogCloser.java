/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.CheckScopeProviderAdapter;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * The class {@link ChangeCheckDialogCloser} is a
 * {@link CloseModalDialogCommandHandler} which performs dirty handling. The
 * {@link FormHandler}s to check are the handlers in the dialog closed by this
 * command.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeCheckDialogCloser extends CloseModalDialogCommandHandler {
    
    /** name of handler as registered in factory */
    public static final String HANDLER_NAME = "closeWithChangeCheck";
	
	private final static CheckScopeProvider provider = new CheckScopeProviderAdapter(ChildrenCheckScopeProvider.INSTANCE,
		new Mapping<LayoutComponent, LayoutComponent>() {
			@Override
			public LayoutComponent map(LayoutComponent comp) {
				return comp.getDialogTopLayout();
			}
		});
	

	public ChangeCheckDialogCloser(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	@Override
	@Deprecated
	public CheckScopeProvider getCheckScopeProvider() {
		return provider;
	}
}


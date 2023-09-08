/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.layout.form.model.FormContext;

/**
 * @author     <a href="mailto:tbe@top-logic.com">tbe</a>
 */
public class PublishableSaveQueryCommandHandler extends SaveQueryCommandHandler {

	public static final String COMMAND_ID = "publishableSaveAttributedQuery";
	
	
	
	public PublishableSaveQueryCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}
	
	
	
	@Override
	public StoredQuery storeQuery(AttributedSearchComponent comp, FormContext context, Map someArguments) {
		StoredQuery theQuery = super.storeQuery(comp, context, someArguments);
		publish(theQuery, context);
		return theQuery;
	}
}

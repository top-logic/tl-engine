/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.test.app.rewrite;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;

/**
 * Demo for a {@link Rewriter} that fills a new attribute with a default value derived from another
 * attribute.
 */
public class CreateUuidRewriter extends Rewriter {

	/**
	 * Creates a {@link CreateUuidRewriter}.
	 */
	public CreateUuidRewriter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object processCreateObject(ObjectCreation event) {
		event.getValues().put("uuid", "UUID-" + event.getValues().get("name"));
		return super.processCreateObject(event);
	}

}

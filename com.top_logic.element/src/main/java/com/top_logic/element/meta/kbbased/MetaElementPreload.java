/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.List;

import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;
import com.top_logic.model.v5.ReferencePreload;
import com.top_logic.model.TLObject;

/**
 * {@link PreloadOperation} that resolves the meta elements and their attributes on the context
 * objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MetaElementPreload extends ReferencePreload {

	/**
	 * Singleton {@link MetaElementPreload} instance.
	 */
	public static final MetaElementPreload INSTANCE = new MetaElementPreload();

	
	private MetaElementPreload() {
		super(PersistentObject.OBJECT_TYPE, PersistentObject.TYPE_REF);
	}

	@Override
	protected void processReferences(PreloadContext context, List<TLObject> destinations) {
		super.processReferences(context, destinations);

		if (!destinations.isEmpty()) {
			SuperMetaElementPreload.INSTANCE.prepare(context, destinations);

		}
	}

}
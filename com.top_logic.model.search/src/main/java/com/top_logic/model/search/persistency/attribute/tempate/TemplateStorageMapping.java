/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.persistency.attribute.tempate;

/**
 * {@link AbstractTemplateStorageMapping} checking the HTML validity of the content.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TemplateStorageMapping extends AbstractTemplateStorageMapping {

	/**
	 * Singleton {@link TemplateStorageMapping} instance.
	 */
	public static final TemplateStorageMapping INSTANCE = new TemplateStorageMapping();

	private TemplateStorageMapping() {
		// Singleton constructor.
	}

}

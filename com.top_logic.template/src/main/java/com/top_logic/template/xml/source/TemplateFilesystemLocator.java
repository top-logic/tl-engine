/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.io.File;

/**
 * {@link TemplateLocator} creating {@link TemplateFilesystemSource}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TemplateFilesystemLocator implements TemplateLocator {

	@Override
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		return new TemplateFilesystemSource(new File(templateReference));
	}

}

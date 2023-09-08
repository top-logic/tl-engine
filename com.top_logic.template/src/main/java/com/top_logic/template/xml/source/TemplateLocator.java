/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

/**
 * Algorithm resolving a {@link TemplateSource} from a template name.
 * 
 * <p>
 * Such algorithm can be plugged into {@link TemplateSourceFactory} using a protocol name, see
 * {@link TemplateSourceFactory.Config#getProtocols()}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TemplateLocator {

	/**
	 * Resolves the given template reference.
	 * <p>
	 * Template references are used in templates when invoking other templates. <br/>
	 * They can be relative to the invoking template and are therefore resolved by those.
	 * </p>
	 */
	TemplateSource resolve(TemplateSource context, String templateReference);

}

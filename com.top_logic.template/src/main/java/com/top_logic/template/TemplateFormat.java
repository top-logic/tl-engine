/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template;

import com.top_logic.basic.StringServices;

/**
 * The format of a template: Either {@link #XML} or {@link #COMS}
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public enum TemplateFormat {

	/**
	 * These templates consist of well-formed XML.
	 * <p>
	 * They also contain a header with (among others) the parameter model definition. <br/>
	 * Used for scripted tests.
	 * </p>
	 */
	XML,

	/**
	 * These templates use the COMS syntax that does not allow the templates to be well-formed xml.
	 * <p>
	 * (The templates can produce well-formed xml, but they are not well-formed xml themselves.) <br/>
	 * They also contain no header and therefore need an external parameter model definition. <br/>
	 * It's used primarily in COMS and the other Config products. It was also used for scripted
	 * tests in various products and the framework for some time. <br/>
	 * Using this template type for scripted tests is deprecated. Use the {@link #XML} syntax
	 * instead.
	 * </p>
	 */
	COMS;

	/**
	 * The default {@link TemplateFormat}.
	 */
	public static TemplateFormat getDefault() {
		return XML;
	}

	/**
	 * Parses the {@link TemplateFormat} from the given {@link String}.
	 */
	public static TemplateFormat parse(String templateType) {
		if (StringServices.equals(templateType, "xml")) {
			return XML;
		}
		if (StringServices.equals(templateType, "coms")) {
			return COMS;
		}
		throw new IllegalArgumentException("This is not a valid template format: '" + templateType + "'");
	}

}

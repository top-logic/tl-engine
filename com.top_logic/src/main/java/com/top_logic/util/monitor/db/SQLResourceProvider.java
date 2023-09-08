/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.monitor.db;

import java.util.Arrays;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.ResourceProvider;

/**
 * {@link ResourceProvider} printing a SQL string with a formatted tooltip.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLResourceProvider extends AbstractResourceProvider {

	@Override
	public String getLabel(Object object) {
		return (String) object;
	}

	@Override
	public String getTooltip(Object anObject) {
		String sql = getLabel(anObject);

		Pattern pattern =
			Pattern.compile(
					"\\s(?=(?:from\\b))|\\s(?=(?:where\\b))|\\s(?=(?:set\\b))|\\s(?=(?:and\\b))|\\s(?=(?:or\\b))|\\s(?=(?:into\\b))|\\s(?=(?:values\\b))|\\s(?=(?:order\\b))|\\s(?=(?:limit\\b))",
				Pattern.CASE_INSENSITIVE);
		String[] fragments = pattern.split(sql);
		for (int i = 0; i < fragments.length; i++) {
			fragments[i] = TagUtil.encodeXML(fragments[i]);
		}
		return StringServices.join(Arrays.asList(fragments), "<br/>");
	}

}

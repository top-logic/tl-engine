/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.scripting.check.ValueCheck.ValueCheckConfig;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link Constraint} that checks the content type of a {@link BinaryDataSource} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ContentTypeCheck extends ValueCheck<ContentTypeCheck.ContentTypeCheckConfig> {

	/**
	 * Configuration for {@link ContentTypeCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ContentTypeCheckConfig extends ValueCheckConfig {
		/**
		 * The content type that is checked for.
		 */
		String getExpectedContentType();
	}

	/**
	 * Create a {@link ContentTypeCheck} from configuration.
	 */
	public ContentTypeCheck(InstantiationContext context, ContentTypeCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object value) {
		String actual = ((BinaryDataSource) value).getContentType();

		ApplicationAssertions.assertEquals(
			_config, "Unexpected content type.", _config.getExpectedContentType(), actual);
	}

}

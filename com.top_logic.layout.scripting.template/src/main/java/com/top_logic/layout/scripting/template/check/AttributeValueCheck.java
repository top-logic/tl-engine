/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.check;

import static com.top_logic.layout.scripting.runtime.action.ApplicationAssertions.*;

import java.text.ParseException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.scripting.check.ValueCheck;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertion;
import com.top_logic.layout.scripting.template.check.AttributeValueCheck.AttributeValueCheckConfig;
import com.top_logic.layout.scripting.template.fuzzy.FuzzyUtil;

/**
 * {@link ValueCheck} that checks the equality of the attribute of the test object against a given
 * value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributeValueCheck extends ValueCheck<AttributeValueCheckConfig> {
	
	/**
	 * Configuration of {@link AttributeValueCheck}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface AttributeValueCheckConfig extends ValueCheck.ValueCheckConfig {

		/**
		 * Name of the attribute to check
		 */
		String getAttribute();

		/**
		 * Expected value.
		 */
		String getValue();

		/**
		 * Whether it is expected that the value is <code>null</code>.
		 */
		boolean getNull();

	}

	/**
	 * Creates a {@link AttributeValueCheck} from a {@link AttributeValueCheckConfig}.
	 * <p>
	 * Is called by the typed configuration.
	 * </p>
	 */
	@CalledByReflection
	public AttributeValueCheck(InstantiationContext context, AttributeValueCheckConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object value) throws ApplicationAssertion {
		String attributeName = _config.getAttribute();
		String expected = _config.getValue();

		assertNotNull(_config, "Value is null, no chance to get attribute " + attributeName, value);
		assertInstanceOf(_config, "", Wrapper.class, value);

		Object actual = ((Wrapper) value).getValue(attributeName);
		if (actual == null) {
			if (expected.isEmpty() && _config.getNull()) {
				// expected null
				return;
			}
			throw fail(_config, "Expected '" + expected + "' but was null");
		}

		Object fuzzyValue;
		try {
			fuzzyValue = FuzzyUtil.parseFuzzy(actual.getClass(), expected);
		} catch (ParseException ex) {
			throw fail(_config, "Unable to create application value for '" + expected + "'", ex);
		}

		assertEquals(_config, "", fuzzyValue, actual);
	}

}


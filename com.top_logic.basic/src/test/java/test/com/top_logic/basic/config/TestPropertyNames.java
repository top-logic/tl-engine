/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static com.top_logic.basic.config.TypedConfiguration.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;

/**
 * Tests the "method name" to "property name" conversion.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestPropertyNames extends AbstractTypedConfigurationTestCase {

	/** A name consisting of one word. */
	public interface SingleWord extends ConfigurationItem {

		int getFoo();

	}

	/** A name consisting of multiple words. */
	public interface MultiWord extends ConfigurationItem {

		int getFooFooFoo();

	}

	/** A name consisting only of an abbreviation. */
	public interface OnlyAbbreviation extends ConfigurationItem {

		int getFOO();

	}

	/** A name containing an abbreviation in the middle. */
	public interface AbbreviationBetweenWords extends ConfigurationItem {

		int getFooFOOFoo();

	}

	public void testSingleWord() {
		testProperty("foo", SingleWord.class);
	}

	public void testMultiWord() {
		testProperty("foo-foo-foo", MultiWord.class);
	}

	public void testOnlyAbbreviation() {
		testProperty("foo", OnlyAbbreviation.class);
	}

	public void testAbbreviationBetweenWords() {
		testProperty("foo-foo-foo", AbbreviationBetweenWords.class);
	}

	private void testProperty(String exampleProperty, Class<? extends ConfigurationItem> exampleClass) {
		String message = "Expected a property of name '" + exampleProperty + "'. All properties: "
			+ getAllProperties(exampleClass);
		assertTrue(message, getProperty(exampleProperty, exampleClass) != null);
	}

	private PropertyDescriptor getProperty(String name, Class<? extends ConfigurationItem> exampleClass) {
		return getConfigurationDescriptor(exampleClass).getProperty(name);
	}

	private Collection<String> getAllProperties(Class<? extends ConfigurationItem> exampleClass) {
		ConfigurationDescriptor configDescriptor = getConfigurationDescriptor(exampleClass);
		List<String> result = new ArrayList<>();
		for (PropertyDescriptor prop : configDescriptor.getProperties()) {
			result.add(prop.getPropertyName());
		}
		return result;
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestPropertyNames.class);
	}

}

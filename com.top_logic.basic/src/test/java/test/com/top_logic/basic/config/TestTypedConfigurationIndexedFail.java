/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import java.util.Collection;

import junit.framework.TestCase;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for wrong {@link Indexed} annotations in {@link TypedConfiguration}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTypedConfigurationIndexedFail extends TestCase {

	@NoImplementationClassGeneration
	public interface WrongParameterCount extends ConfigurationItem {

		String B_BY_NAME_PROPERTY = "bs-by-name";

		@Key(TestTypedConfigurationIndexedFail.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_PROPERTY)
		B getBByName();
	}

	@NoImplementationClassGeneration
	public interface NoSuchProperty extends ConfigurationItem {

		String B_BY_NAME_PROPERTY = "bs-by-name";

		@Key(TestTypedConfigurationIndexedFail.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = "no-such-property")
		B getBByName(String name);
	}

	@NoImplementationClassGeneration
	public interface WrongReturnType extends ConfigurationItem {

		String B_BY_NAME_PROPERTY = "bs-by-name";

		@Key(TestTypedConfigurationIndexedFail.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_PROPERTY)
		C getBByName(String name);
	}

	@NoImplementationClassGeneration
	public interface WrongKeyType extends ConfigurationItem {

		String B_BY_NAME_PROPERTY = "bs-by-name";

		@Key(TestTypedConfigurationIndexedFail.B.NAME_ATTRIBUTE)
		@Name(B_BY_NAME_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_PROPERTY)
		B getBByName(Integer name);
	}

	@NoImplementationClassGeneration
	public interface MissingKeyAnnotation extends ConfigurationItem {

		String B_BY_NAME_PROPERTY = "bs-by-name";

		@Name(B_BY_NAME_PROPERTY)
		Collection<B> getBsByName();

		@Indexed(collection = B_BY_NAME_PROPERTY)
		B getBByName(String name);
	}

	public interface B extends NamedConfiguration {
		String X_PROPERTY = "x";

		@Name(X_PROPERTY)
		int getX();
	}

	public interface C extends B {
		// Subinterface of B
	}

	public void testWrongParameterCount() {
		try {
			TypedConfiguration.getConfigurationDescriptor(WrongParameterCount.class);
			fail("Must detect error.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testNoSuchProperty() {
		try {
			TypedConfiguration.getConfigurationDescriptor(NoSuchProperty.class);
			fail("Must detect error.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testWrongReturnType() {
		try {
			TypedConfiguration.getConfigurationDescriptor(WrongReturnType.class);
			fail("Must detect error.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testWrongKeyType() {
		try {
			TypedConfiguration.getConfigurationDescriptor(WrongKeyType.class);
			fail("Must detect error.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

	public void testMissingKeyAnnotation() {
		try {
			TypedConfiguration.getConfigurationDescriptor(MissingKeyAnnotation.class);
			fail("Must detect error.");
		} catch (RuntimeException ex) {
			// Expected.
		}
	}

}

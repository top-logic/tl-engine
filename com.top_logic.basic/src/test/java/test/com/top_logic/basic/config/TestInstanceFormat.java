/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.internal.gen.NoImplementationClassGeneration;

/**
 * Test case for {@link InstanceFormat}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestInstanceFormat extends AbstractTypedConfigurationTestCase {

	public interface ScenarioTypes {

		@NoImplementationClassGeneration
		public interface MissingInstanceFormatAnnotationOnClass extends ConfigurationItem {

			String INSTANCE = "instance";

			@Name(INSTANCE)
			Object getInstance();

		}

		public interface NonConfigItem {
			// Nothing needed but the type itself.
		}

		public interface MissingInstanceFormatAnnotationOnInterface extends ConfigurationItem {

			String INSTANCE = "instance";

			@Name(INSTANCE)
			NonConfigItem getInstance();

		}

	}

	public void testMissingInstanceFormatAnnotationOnClass() {
		String message = "Unhelpfull error message when the @InstanceFormat annotation is missing.";
		String errorPart = "@InstanceFormat";
		assertIllegal(message, errorPart, ScenarioTypes.MissingInstanceFormatAnnotationOnClass.class);
	}

	@SuppressWarnings("unchecked")
	public void testMissingInstanceFormatAnnotationOnInterface() throws ConfigurationException {
		try {
			Map<String, ConfigurationDescriptor> rootDescriptorMap =
				createDescriptorMap(ScenarioTypes.MissingInstanceFormatAnnotationOnInterface.class);
			String xml = XML_DECLARATION
				+ "<MissingInstanceFormatAnnotationOnInterface " + XML_CONFIG_NAMESPACE_DECLARATION + ">"
				+ "  <instance class='java.lang.Object' />"
				+ "</MissingInstanceFormatAnnotationOnInterface>";
			read(rootDescriptorMap, xml);
		} catch (Error ex) {
			String message = "Unhelpful error message when the @InstanceFormat annotation is missing.";
			assertErrorMessage(message, Pattern.compile("@InstanceFormat"), ex);
			return;
		}
		fail("A missing @InstanceFormat annotation did not cause the reader to fail.");
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

}

/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.net.URL;

import com.top_logic.basic.MultiProperties;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.URLBinaryData;

/**
 * {@link CustomConfigurationDecorator} that reads configuration from resources relative to a
 * {@link Class} file.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClassBasedPropertiesDecorator extends CustomConfigurationDecorator {

	private static abstract class ClassBasedMapping implements Mapping<Class<?>, BinaryContent> {

		public ClassBasedMapping() {
			// avoid synthetic access
		}

		@Override
		public BinaryContent map(Class<?> input) {
			URL url = input.getResource(getResourceName(input));
			if (url == null) {
				return null;
			}
			return new URLBinaryData(url);
		}

		protected abstract String getResourceName(Class<?> input);

		protected String untypedConfigName(Class<?> testClass) {
			return CustomPropertiesDecorator.customPropertiesFileName(testClass);
		}

	}

	private static final Mapping<Class<?>, BinaryContent> TYPED_CLASS_MAPPING = new ClassBasedMapping() {

		@Override
		protected String getResourceName(Class<?> input) {
			return MultiProperties.typedConfigName(untypedConfigName(input));
		}

	};

	private static final Mapping<Class<?>, BinaryContent> UNTYPED_CLASS_MAPPING = new ClassBasedMapping() {

		@Override
		protected String getResourceName(Class<?> input) {
			return untypedConfigName(input);
		}

	};

	private final boolean _withDefaultConfig;

	private final BinaryContent[] _untypedContent;

	private final BinaryContent[] _typedContent;

	/**
	 * Creates a {@link ClassBasedPropertiesDecorator} from the given test classes. It is expected
	 * that near the source of the test class there is a ".properties.xml" with the same name as the
	 * test class. That file is taken as properties file.
	 * 
	 * @param testClasses
	 *        Test classes which are used to fetch configuration resources.
	 * @param withDefaultConfig
	 *        whether the current existing configuration should be used as default
	 */
	public ClassBasedPropertiesDecorator(Class<?>[] testClasses, boolean withDefaultConfig) {
		this(content(testClasses, UNTYPED_CLASS_MAPPING), content(testClasses, TYPED_CLASS_MAPPING), withDefaultConfig);
	}

	private ClassBasedPropertiesDecorator(BinaryContent[] untypedContent, BinaryContent[] typedContent,
			boolean withDefaultConfig) {
		_untypedContent = untypedContent;
		_typedContent = typedContent;
		_withDefaultConfig = withDefaultConfig;
	}

	private static <T> BinaryContent[] content(T[] input, Mapping<T, BinaryContent> mapping) {
		BinaryContent[] result = new BinaryContent[input.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = mapping.map(input[i]);
		}
		return result;
	}

	@Override
	protected void installConfiguration() throws Exception {
		if (_withDefaultConfig) {
			MultiProperties.restartWithConfigs(_untypedContent, _typedContent);
		} else {
			XMLProperties.XMLPropertiesConfig config = new XMLProperties.XMLPropertiesConfig();
			for (int i = 0; i < _untypedContent.length; i++) {
				config.pushAdditionalContent(_untypedContent[i], _typedContent[i]);
			}
			XMLProperties.restartXMLProperties(config);
		}
	}

}

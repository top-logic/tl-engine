/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.navigation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.navigation.ViewResolver;

/**
 * The view files of a test scenario, read from the resources next to this class.
 *
 * <p>
 * Keeps one element tree per view - as the application's loader does, so that a view referenced
 * twice is the same element tree in both places.
 * </p>
 */
public class FixtureViews implements ViewResolver {

	private final Map<String, ViewElement> _views = new HashMap<>();

	@Override
	public ViewElement getView(String viewRef) throws ConfigurationException {
		ViewElement cached = _views.get(viewRef);
		if (cached != null) {
			return cached;
		}

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		DefaultInstantiationContext context = new DefaultInstantiationContext(FixtureViews.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(FixtureViews.class, viewRef));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		ViewElement view = (ViewElement) context.getInstance(config);
		context.checkErrors();

		_views.put(viewRef, view);
		return view;
	}
}

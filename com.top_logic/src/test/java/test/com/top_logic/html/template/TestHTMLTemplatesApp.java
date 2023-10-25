/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.html.template;

import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.HTMLTemplateUtils;
import com.top_logic.layout.template.MapWithProperties;
import com.top_logic.layout.template.WithProperties;

/**
 * Test case for {@link HTMLTemplateFragment}s in the application context.
 */
@SuppressWarnings("javadoc")
public class TestHTMLTemplatesApp extends AbstractLayoutTest {

	public void testError() throws IOException, ConfigurationException {
		MapWithProperties properties = new MapWithProperties();
		properties.put("a", 1);

		BasicTestCase.assertContains("/ by zero", html("<span>{a / 0}</span>", properties));
		BasicTestCase.assertContains("No such property 'noSuchProperty'",
			html("<span>{noSuchProperty}</span>", properties));
	}

	private String html(String template, WithProperties properties) throws IOException, ConfigurationException {
		return render(parse(template), properties);
	}

	private HTMLTemplateFragment parse(String template) throws ConfigurationException {
		return HTMLTemplateUtils.parse("test", template);
	}

	private String render(HTMLTemplateFragment template, WithProperties properties) throws IOException {
		TagWriter writer = new TagWriter();

		template.write(displayContext(), writer, properties);

		return writer.toString();
	}

	public static Test suite() {
		return suite(TestHTMLTemplates.class);
	}

}

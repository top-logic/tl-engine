/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.resource;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationSchemaConstants;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.resource.ClientResources;

/**
 * Tests for {@link ClientResources}: a second configuration fragment replaces or removes a named
 * resource, and a dependency on a removed resource is a configuration error.
 */
public class TestClientResources extends TestCase {

	private static final String NS = " xmlns:config='" + ConfigurationSchemaConstants.CONFIG_NS + "' ";

	/** Tokens, components depending on them, and the controls bundle. */
	private static final String BASE = "<config class='" + ClientResources.class.getName() + "'" + NS + ">"
		+ "<resources>"
		+ "<stylesheet name='tokens' resource='/style/tokens.css'/>"
		+ "<stylesheet name='components' resource='/style/components.css' requires='tokens'/>"
		+ "<module-script name='controls' resource='/script/controls.js'/>"
		+ "</resources>"
		+ "</config>";

	/** A customer module dropping the component styles and adding its own bundle after ours. */
	private static final String CUSTOMER = "<config class='" + ClientResources.class.getName() + "'" + NS + ">"
		+ "<resources>"
		+ "<stylesheet name='components' config:operation='remove'/>"
		+ "<module-script name='mbui' resource='/script/mbui.js' requires='controls'/>"
		+ "</resources>"
		+ "</config>";

	/** A customer module replacing the component styles by its own file. */
	private static final String REPLACE = "<config class='" + ClientResources.class.getName() + "'" + NS + ">"
		+ "<resources>"
		+ "<stylesheet name='components' config:operation='update' resource='/style/mbui.css'/>"
		+ "</resources>"
		+ "</config>";

	/** A customer module removing what another entry still requires. */
	private static final String DANGLING = "<config class='" + ClientResources.class.getName() + "'" + NS + ">"
		+ "<resources>"
		+ "<stylesheet name='tokens' config:operation='remove'/>"
		+ "</resources>"
		+ "</config>";

	/** The second fragment removes an entry by name and keeps the others. */
	public void testRemoveByName() throws ConfigurationException, IOException {
		String styles = styles(service(BASE, CUSTOMER));

		assertTrue(styles, styles.contains("/style/tokens.css"));
		assertFalse(styles, styles.contains("/style/components.css"));
	}

	/** The second fragment replaces the resource of a named entry. */
	public void testUpdateByName() throws ConfigurationException, IOException {
		String styles = styles(service(BASE, REPLACE));

		assertTrue(styles, styles.contains("/style/mbui.css"));
		assertFalse(styles, styles.contains("/style/components.css"));
		assertBefore(styles, "/style/tokens.css", "/style/mbui.css");
	}

	/** A module script added by the customer is emitted after the one it requires. */
	public void testCustomerScriptAfterOurs() throws ConfigurationException, IOException {
		String scripts = scripts(service(BASE, CUSTOMER));

		assertBefore(scripts, "/script/controls.js", "/script/mbui.js");
	}

	/** Removing an entry another one requires is an error, not a silent omission. */
	public void testRequiresOnRemovedEntryIsAnError() throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		service(log, BASE, DANGLING);

		assertTrue(log.getError(), log.hasErrors());
		assertTrue(log.getError(), log.getError().contains("'components' requires unknown resource 'tokens'"));
	}

	private static void assertBefore(String output, String first, String second) {
		int firstIndex = output.indexOf(first);
		int secondIndex = output.indexOf(second);
		assertTrue(output, firstIndex >= 0);
		assertTrue(output, secondIndex >= 0);
		assertTrue(output, firstIndex < secondIndex);
	}

	private static ClientResources service(String... fragments) throws ConfigurationException {
		BufferingProtocol log = new BufferingProtocol();
		ClientResources result = service(log, fragments);
		assertFalse(log.getError(), log.hasErrors());
		return result;
	}

	private static ClientResources service(BufferingProtocol log, String... fragments) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> roots = new HashMap<>();
		roots.put("config", TypedConfiguration.getConfigurationDescriptor(ClientResources.Config.class));
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		ConfigurationReader reader = new ConfigurationReader(context, roots);
		reader.setSources(Arrays.stream(fragments).map(CharacterContents::newContent).toList());
		ClientResources.Config config = (ClientResources.Config) reader.read();
		return new ClientResources(context, config);
	}

	private static String styles(ClientResources service) throws IOException {
		StringWriter buffer = new StringWriter();
		service.writeStyleRefs(new TagWriter(buffer), "");
		return buffer.toString();
	}

	private static String scripts(ClientResources service) throws IOException {
		StringWriter buffer = new StringWriter();
		service.writeScriptRefs(new TagWriter(buffer), "");
		return buffer.toString();
	}

	/**
	 * Test suite with the module setup.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestClientResources.class, TypeIndex.Module.INSTANCE));
	}

}

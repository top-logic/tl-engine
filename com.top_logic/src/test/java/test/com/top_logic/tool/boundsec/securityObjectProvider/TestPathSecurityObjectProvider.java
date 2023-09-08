/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.securityObjectProvider;

import java.util.Arrays;
import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.config.AbstractTypedConfigurationTestCase;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider.Config;
import com.top_logic.tool.boundsec.securityObjectProvider.path.Component;
import com.top_logic.tool.boundsec.securityObjectProvider.path.SecurityPath;

/**
 * {@link TestCase} for {@link PathSecurityObjectProvider}.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class TestPathSecurityObjectProvider extends BasicTestCase {

	public void testParseComponentIdsWithoutDots() throws ConfigurationException {
		ComponentName name = ComponentName.newName("mycomponent");
		String securityPath = "component(" + name.localName() + ").currentobject";
		PathSecurityObjectProvider pathSecurityObjectProvider = parse(securityPath);
		
		assertInternalPath(pathSecurityObjectProvider, newComponentConfig(name),
			SecurityPath.currentObject().getConfig());
	}

	public void testParseQualifiedComponentIdsWithoutDots() throws ConfigurationException {
		ComponentName name = ComponentName.newName("scope", "mycomponent");
		String securityPath = "component(" + name.qualifiedName() + ").currentobject";
		PathSecurityObjectProvider pathSecurityObjectProvider = parse(securityPath);

		assertInternalPath(pathSecurityObjectProvider, newComponentConfig(name),
			SecurityPath.currentObject().getConfig());
	}

	public void testAcceptComponentIdsWithDots() throws ConfigurationException {
		ComponentName name = ComponentName.newName("my.component");
		String securityPath = "component(" + name.localName() + ").currentobject";
		PathSecurityObjectProvider pathSecurityObjectProvider = parse(securityPath);
		
		assertInternalPath(pathSecurityObjectProvider, newComponentConfig(name),
			SecurityPath.currentObject().getConfig());
	}

	public void testAcceptQualifiedComponentIdsWithDotsInName() throws ConfigurationException {
		ComponentName name = ComponentName.newName("scope", "my.component");
		String securityPath = "component(" + name.qualifiedName() + ").currentobject";
		PathSecurityObjectProvider pathSecurityObjectProvider = parse(securityPath);

		assertInternalPath(pathSecurityObjectProvider, newComponentConfig(name),
			SecurityPath.currentObject().getConfig());
	}

	public void testAcceptQualifiedComponentIdsWithDotsInScope() throws ConfigurationException {
		ComponentName name = ComponentName.newName("my.scope", "mycomponent");
		String securityPath = "component(" + name.qualifiedName() + ").currentobject";
		PathSecurityObjectProvider pathSecurityObjectProvider = parse(securityPath);

		assertInternalPath(pathSecurityObjectProvider, newComponentConfig(name),
			SecurityPath.currentObject().getConfig());
	}

	private PathSecurityObjectProvider parse(String securityPath) throws ConfigurationException {
		return PathSecurityObjectProvider.newPathSecurityObjectProvider("prop", securityPath);
	}

	private static Component.Config newComponentConfig(ComponentName componentName) {
		return SecurityPath.componentConfig(componentName);
	}

	public void testParseCompactPath() throws ConfigurationException {
		ConfigurationItem provider = ConfigurationReader.readContent(new AssertProtocol(), Collections.emptyMap(),
			CharacterContents.newContent(
				"<provider "
					+ "xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" "
					+ "config:interface=\"com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider$Config\" "
					+ "path=\"component(mycomponent).currentobject\""
					+ "/>"));
		assertInstanceof(provider, PathSecurityObjectProvider.Config.class);
		PathSecurityObjectProvider.Config providerConfig = (Config) provider;
		assertEquals(2, providerConfig.getPath().size());
		AbstractTypedConfigurationTestCase.assertEquals(newComponentConfig(ComponentName.newName("mycomponent")),
			providerConfig.getPath().get(0));
		AbstractTypedConfigurationTestCase.assertEquals(SecurityPath.currentObject().getConfig(),
			providerConfig.getPath().get(1));
	}

	public void testParseExplicitPath() throws ConfigurationException {
		ConfigurationItem provider = ConfigurationReader.readContent(new AssertProtocol(), Collections.emptyMap(),
			CharacterContents.newContent(
				"<provider "
					+ "xmlns:config=\"http://www.top-logic.com/ns/config/6.0\" "
					+ "config:interface=\"com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider$Config\" "
					+ ">"
					+ " <path>"
					+ "  <component name=\"myscope#mycomponent\"/>"
					+ "  <currentobject/>"
					+ " </path>"
					+ "</provider>"

			));
		assertInstanceof(provider, PathSecurityObjectProvider.Config.class);
		PathSecurityObjectProvider.Config providerConfig = (Config) provider;
		assertEquals(2, providerConfig.getPath().size());
		AbstractTypedConfigurationTestCase.assertEquals(
			newComponentConfig(ComponentName.newName("myscope", "mycomponent")), providerConfig.getPath().get(0));
		AbstractTypedConfigurationTestCase.assertEquals(SecurityPath.currentObject().getConfig(),
			providerConfig.getPath().get(1));
	}

	private void assertInternalPath(PathSecurityObjectProvider pathSecurityObjectProvider,
			ConfigurationItem... expectedPath) {
		assertConfigEquals(Arrays.asList(expectedPath), pathSecurityObjectProvider.getConfig().getPath());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestPathSecurityObjectProvider.class);
	}

}

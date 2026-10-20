/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.configured;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.model.search.configured.PrivilegedScript;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the configuration of a {@link PrivilegedScript} (ticket #29858).
 */
@SuppressWarnings("javadoc")
public class TestPrivilegedScriptConfig extends BasicTestCase {

	public void testReadsProperties() throws ConfigurationException {
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(new BufferingProtocol(), """
			<privileged-script name="approve" security-object="$p" command-group="Write" on-denied="null">
				<parameters><parameter name="p"/></parameters>
				<implementation>$p</implementation>
			</privileged-script>
			""");
		assertEquals("Write", config.getCommandGroup().id());
		assertNotNull(config.getSecurityObject());
		assertNotNull(config.getOnDenied());
	}

	public void testSecurityObjectIsMandatory() {
		assertRejected("getSecurityObject()", """
			<privileged-script name="approve" command-group="Write">
				<implementation>null</implementation>
			</privileged-script>
			""");
	}

	public void testCommandGroupIsMandatory() {
		assertRejected("getCommandGroup()", """
			<privileged-script name="approve" security-object="null">
				<implementation>null</implementation>
			</privileged-script>
			""");
	}

	public void testUnknownCommandGroupIsRejected() throws ConfigurationException {
		Protocol log = new BufferingProtocol();
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(log, """
			<privileged-script name="approve" security-object="null" command-group="NoSuchGroup">
				<implementation>null</implementation>
			</privileged-script>
			""");
		try {
			DefaultInstantiationContext context = new DefaultInstantiationContext(log);
			context.getInstance(config);
			context.checkErrors();
		} catch (ConfigurationException | AbortExecutionException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("NoSuchGroup"));
			return;
		}
		fail("An unknown command group must be rejected.");
	}

	public void testResolvesCommandGroup() throws ConfigurationException {
		Protocol log = new BufferingProtocol();
		PrivilegedScript.Config config = (PrivilegedScript.Config) read(log, """
			<privileged-script name="approve" security-object="null" command-group="Write">
				<implementation>null</implementation>
			</privileged-script>
			""");
		PrivilegedScript script =
			(PrivilegedScript) new DefaultInstantiationContext(log).getInstance(config);
		log.checkErrors();
		assertSame(SimpleBoundCommandGroup.WRITE, script.getCommandGroup());
	}

	private void assertRejected(String property, String xml) {
		try {
			Protocol log = new BufferingProtocol();
			read(log, xml);
			log.checkErrors();
		} catch (ConfigurationException | RuntimeException ex) {
			assertTrue("The missing property is reported: " + ex.getMessage(),
				String.valueOf(ex.getMessage()).contains(property));
			return;
		}
		fail("Missing " + property + " must be rejected.");
	}

	private static Object read(Protocol log, String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors =
			Collections.singletonMap(PrivilegedScript.Config.TAG_NAME,
				TypedConfiguration.getConfigurationDescriptor(PrivilegedScript.Config.class));
		return new ConfigurationReader(new DefaultInstantiationContext(log), descriptors)
			.setSource(CharacterContents.newContent(xml))
			.read();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(ServiceTestSetup.createSetup(TestPrivilegedScriptConfig.class,
			TypeIndex.Module.INSTANCE, CommandGroupRegistry.Module.INSTANCE));
	}
}

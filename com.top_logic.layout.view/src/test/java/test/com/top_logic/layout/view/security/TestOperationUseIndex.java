/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.element.boundsec.manager.coverage.OperationUse;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.command.CommandCliqueService;
import com.top_logic.layout.view.security.OperationUseIndex;
import com.top_logic.model.search.configured.ConfiguredScript;
import com.top_logic.model.search.configured.PrivilegedScript;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.CommandGroupReference;

/**
 * Test of {@link OperationUseIndex}: the places of a view and of the configured TL-Script functions
 * checking an operation, with the type they check it on.
 */
@SuppressWarnings("javadoc")
public class TestOperationUseIndex extends AbstractModelAccessTest {

	private static final String FIXTURE = "operation-uses.view.xml";

	private static final String VIEW_REF = "security/" + FIXTURE;

	private static final String PROJECT_TYPE = MODULE + ":" + PROJECT;

	private static final String TASK_TYPE = MODULE + ":" + TASK;

	/** The type of the steps of a project, created through the link access of a list. */
	private static final String STEP_TYPE = MODULE + ":" + STEP;

	/**
	 * The places of the fixture view, each as kind, operation and checked type; the checks of the
	 * link and remove access of an object list count as uses of commands.
	 */
	public void testScanView() throws Exception {
		List<OperationUse> uses = OperationUseIndex.scanView(VIEW_REF, readFixture());

		assertEquals(Set.of(
			"VIEW Administration root",
			"COMMAND Write root",
			"VIEW Finish " + PROJECT_TYPE,
			"COMMAND Delete " + PROJECT_TYPE,
			"COMMAND Read " + TASK_TYPE,
			"COMMAND Create " + TASK_TYPE,
			"COMMAND Create " + STEP_TYPE,
			"COMMAND Delete " + TASK_TYPE,
			"FORM Finish ?",
			"COMMAND Finish ?"),
			uses.stream().map(TestOperationUseIndex::describe).collect(Collectors.toSet()));
		assertEquals("A form keeping the default edit operation is no use of its own.", 10, uses.size());
	}

	/**
	 * A place is described by the view and the line it is defined at, and by the id of an element
	 * that has one.
	 */
	public void testDescription() throws Exception {
		List<OperationUse> uses = OperationUseIndex.scanView(VIEW_REF, readFixture());
		OperationUse unit = uses.stream()
			.filter(use -> use.kind() == OperationUse.Kind.VIEW && use.target() == OperationUse.Target.TYPE)
			.findFirst().orElseThrow();
		assertTrue(unit.user(), unit.user().startsWith(VIEW_REF + ":"));
		assertTrue(unit.user(), unit.user().endsWith("#planning"));
	}

	public void testScanScripts() {
		PrivilegedScript.Config privileged = TypedConfiguration.newConfigItem(PrivilegedScript.Config.class);
		privileged.update(privileged.descriptor().getProperty(ConfiguredScript.Config.NAME_ATTRIBUTE), "approve");
		privileged.update(privileged.descriptor().getProperty(PrivilegedScript.Config.COMMAND_GROUP),
			new CommandGroupReference(FINISH));

		List<OperationUse> uses = OperationUseIndex.scanScripts(List.of(privileged));

		assertEquals(1, uses.size());
		OperationUse use = uses.get(0);
		assertEquals(OperationUse.Kind.SCRIPT, use.kind());
		assertEquals(FINISH, use.operation());
		assertEquals("The type of the security object is not declared.", OperationUse.Target.UNKNOWN,
			use.target());
		assertTrue(use.user(), use.user().contains("approve"));
	}

	private static String describe(OperationUse use) {
		String checked = switch (use.target()) {
			case TYPE -> TLModelUtil.qualifiedName(use.type());
			case ROOT -> "root";
			case UNKNOWN -> "?";
		};
		return use.kind() + " " + use.operation() + " " + checked;
	}

	private static ViewElement.Config readFixture() throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestOperationUseIndex.class);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestOperationUseIndex.class, FIXTURE));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return suiteWith(TestOperationUseIndex.class, TypeIndex.Module.INSTANCE, CommandCliqueService.Module.INSTANCE);
	}

}

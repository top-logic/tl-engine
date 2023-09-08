/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.execution;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertNoErrorLogListener;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.tool.boundsec.TestAbstractCommandHandler;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.RuleReference;

/**
 * Test case for {@link ExecutabilityRuleManager}.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@SuppressWarnings("javadoc")
public class TestExecutabilityRuleManager extends BasicTestCase {

	public void testGetInstance() {
		ExecutabilityRuleManager theManager = ExecutabilityRuleManager.getInstance();
		assertNotNull(theManager);
		assertSame(theManager, ExecutabilityRuleManager.getInstance());
	}

	public void testGetSingleRule() {
		ExecutabilityRule rule = ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_ALWAYS);
		assertNotNull(rule);
		// rules following the singleton pattern are not instantiated twice
		assertSame(rule, AlwaysExecutable.INSTANCE);
		// rules are cached as singletons
		assertSame(rule, ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_ALWAYS));
	}

	public void testReference() {
		ExecutabilityRule rule1 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerReference");
		assertNotNull(rule1);
		ExecutabilityRule rule2 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerReference3");
		assertNotNull(rule2);
		assertSame("Rule reference has been resolved.", rule2, rule1);
	}

	public void testCompact() {
		ExecutabilityRule rule1 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerCompact1");
		assertNotNull(rule1);
		ExecutabilityRule rule2 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerCompact2");
		assertNotNull(rule2);
		ExecutabilityRule rule3 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerCompact3");
		assertNotNull(rule3);
		assertSame("Rule reference has been resolved.", rule2, rule1);
		assertInstanceof(rule3, CombinedExecutabilityRule.class);
	}

	public void testEmpty() {
		ExecutabilityRule rule1 = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerEmpty");
		assertNotNull(rule1);
		assertSame("Rule reference has been resolved.", AlwaysExecutable.INSTANCE, rule1);
	}

	public void testResolve() {
		CombinedExecutabilityRule.Config nested =
			TypedConfiguration.newConfigItem(CombinedExecutabilityRule.Config.class);

		CombinedExecutabilityRule.Config combined =
			TypedConfiguration.newConfigItem(CombinedExecutabilityRule.Config.class);

		RuleReference.Config reference = TypedConfiguration.newConfigItem(RuleReference.Config.class);
		reference.setRuleId("TestExecutabilityRuleManagerReference");
		combined.getExecutability().add(reference);

		nested.getExecutability().add(combined);
		ExecutabilityRule resolved = ExecutabilityRuleManager.resolveRules(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, combined);

		ExecutabilityRule expected = ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerReference");
		assertNotNull(expected);
		assertSame("Rule reference must be resolved.", expected, resolved);

		ExecutabilityRule resolvedNested = ExecutabilityRuleManager.resolveRules(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, nested);
		assertSame("Rule reference must be resolved.", expected, resolvedNested);
	}

	public void testFailNullID() {
		try {
			ExecutabilityRuleManager.getRule(null);
			fail("Must not lookup rule with null ID.");
		} catch (NullPointerException ex) {
			// Expected.
		}
	}

	public void testErrorOnUndefinedRuleID() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener();
		logListener.activate();
		try {
			ExecutabilityRuleManager.getRule("undefined-rule");
			List<LogEntry> log2 = logListener.getAndClearLogEntries();
			assertEquals(1, log2.size());
			assertContains("Reference to undefined executability rule", log2.get(0).getMessage());
		} finally {
			logListener.deactivate();
		}
	}

	public void testConfiguredCombinedRule() {
		ExecutabilityRule rule =
			ExecutabilityRuleManager.getRule("TestExecutabilityRuleManagerCombined");
		assertNotNull(rule);

		Map<String, Object> combinedValues = new HashMap<>();
		combinedValues.put(
			TestAbstractCommandHandler.CHECK_UNCONFIGURED_VALUE, TestAbstractCommandHandler.GLOBAL_VALUE_UNCONFIGURED);
		combinedValues.put(
			TestAbstractCommandHandler.CHECK_CONFIGURED_VALUE, "combinedValue");
		assertEquals(ExecutableState.EXECUTABLE, rule.isExecutable(null, null, combinedValues));

		assertEquals(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null, null, Collections
			.<String, Object> singletonMap(TestAbstractCommandHandler.CHECK_UNCONFIGURED_VALUE,
				TestAbstractCommandHandler.GLOBAL_VALUE_UNCONFIGURED)));

		assertEquals(ExecutableState.NOT_EXEC_HIDDEN, rule.isExecutable(null, null, Collections
			.<String, Object> singletonMap(TestAbstractCommandHandler.CHECK_CONFIGURED_VALUE,
				"combinedValue")));
	}

	/**
	 * Test rule used in configuration.
	 */
	public static class TestedExecutabilityRule implements ExecutabilityRule {

		/**
		 * Singleton {@link TestedExecutabilityRule} instance.
		 */
		public static final TestedExecutabilityRule INSTANCE = new TestedExecutabilityRule();

		private TestedExecutabilityRule() {
			// Singleton constructor.
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
			return ExecutableState.EXECUTABLE;
		}
	}

	/**
	 * the suite of Tests to execute.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestExecutabilityRuleManager.class,
			ExecutabilityRuleManager.Module.INSTANCE));
	}
}

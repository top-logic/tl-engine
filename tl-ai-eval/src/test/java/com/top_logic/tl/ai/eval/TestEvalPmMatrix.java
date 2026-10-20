/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * The eval itself: the five {@code pm} scenarios, put to every configured arm, three times each.
 *
 * <p>
 * This is a measurement, not an assertion about a model. It fails when the <em>harness</em> is
 * broken - nothing ran, or every arm was unable to start - and never because a model got a scenario
 * wrong. A model getting a scenario wrong is the finding, and it belongs in the CSV and in the
 * report, not in a red build.
 * </p>
 *
 * <h2>Running it</h2>
 *
 * <pre>
 * source env.sh
 * mvn -pl tl-ai-eval test -DskipTests=false -Dtl.ai.eval=true -Dtest=TestEvalPmMatrix
 * </pre>
 *
 * <p>
 * The CSV lands in {@code target/eval/} unless {@code -Dtl.ai.eval.out=&lt;file&gt;} says otherwise,
 * and the report is printed. Without {@code -Dtl.ai.eval=true} the class reports as skipped; with
 * it but without an API key, the real arms report as "could not run" and the scripted arm still
 * exercises the whole pipeline.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestEvalPmMatrix extends BasicTestCase {

	/** System property naming the file the CSV is written to. */
	public static final String OUTPUT_PROPERTY = "tl.ai.eval.out";

	/**
	 * Runs the method outside an interaction context while the eval is off.
	 *
	 * <p>
	 * {@link BasicTestCase} wraps every method in one, and an interaction context needs a started
	 * module system - which is exactly what {@link #suite()} skips when the eval is off, so that a
	 * build not running the eval does not boot the whole agent stack to report a skip. The method
	 * then does nothing but violate its assumption, and needs no context to do it.
	 * </p>
	 */
	@Override
	public void runBare() throws Throwable {
		if (!EvalE2e.enabled()) {
			superRunBare();
			return;
		}
		super.runBare();
	}

	/** Runs the whole matrix, writes the CSV and prints the report. */
	public void testThePmMatrix() throws Exception {
		EvalE2e.assumeEnabled();
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
		ScriptedProvider.install(EvalScripts.oracle());

		EvalConfig config = TestEvalConfig.pmScenarios();
		List<RunRecord> rows = EvalMatrix.run(config, PersonManager.getManager().getRoot(), new PmFixture());

		Path csv = outputFile();
		EvalCsv.write(csv, rows);
		String report = EvalReport.render(rows);

		// Printed rather than logged: this is the artifact the run exists to produce, and a reader
		// wants it beside the command they typed.
		System.out.println();
		System.out.println("Eval rows written to " + csv.toAbsolutePath());
		System.out.println();
		System.out.println(report);
		if (EvalE2e.apiKey() == null) {
			System.out.println("No API key in the environment, so only the scripted arm ran."
				+ " Set ANTHROPIC_API_KEY (or tl_anthropic_api_key) for the real arms.");
		}

		assertFalse("The matrix produced no rows at all.", rows.isEmpty());
		assertTrue("No arm could be started, so this measured nothing. Reasons: "
			+ rows.stream().filter(r -> !r.outcome().ran()).map(RunRecord::error).distinct().toList(),
			rows.stream().anyMatch(r -> r.outcome().ran()));
	}

	private static Path outputFile() {
		String configured = System.getProperty(OUTPUT_PROPERTY);
		if (configured != null && !configured.isEmpty()) {
			return Paths.get(configured);
		}
		String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
		return Paths.get("target", "eval", "pm-" + stamp + ".csv");
	}

	public static Test suite() {
		if (!EvalE2e.enabled()) {
			// The setup below boots the whole agent stack, which takes longer than every other test
			// of this module together. Handing back the bare class lets the method reach its
			// assumption and be reported as skipped instead; see runBare() for the other half.
			return new TestSuite(TestEvalPmMatrix.class);
		}
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestEvalPmMatrix.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}

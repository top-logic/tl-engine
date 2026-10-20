/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tl.ai.core.agent.AgentApplicationService;
import com.top_logic.tl.ai.core.agent.AgentPolicyService;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.TopLogicAgentDefinitions;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.security.AiToolAccessService;
import com.top_logic.tl.ai.core.tools.web.WebToolsService;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * The research questions of {@code research-scenarios.config.xml}, put to real models on the real web
 * and graded on the answers (#755).
 *
 * <p>
 * Behind {@link ResearchE2e}: without {@code -Dtl.ai.research.e2e=true} the test reports as skipped.
 * The CSV lands in {@code target/eval/research-<timestamp>.csv} ({@code -Dtl.ai.eval.out} puts it
 * elsewhere) and the report is printed.
 * </p>
 *
 * <p>
 * An arm names the shipped agent, {@code TL_SingleAgent}, and a model; which model an agent runs on is
 * the agent runtime service's to say. So before an arm's runs this test makes the arm's model the
 * default model - for the lead and for the workers it spawns - and restarts that service, the way the
 * service editor does.
 * </p>
 */
@RunWith(AssumptionAwareRunner.class)
public class TestResearchEval extends BasicTestCase {

	private final List<BinaryData> _layers = new ArrayList<>();

	@Override
	public void runBare() throws Throwable {
		if (!ResearchE2e.enabled()) {
			superRunBare();
			return;
		}
		super.runBare();
	}

	@Override
	protected void tearDown() throws Exception {
		FixtureWeb.clear();
		if (!_layers.isEmpty()) {
			XMLProperties.Module.INSTANCE.config().getTypedConfigs().removeAll(_layers);
			_layers.clear();
			restartRuntime();
		}
		super.tearDown();
	}

	/** Every question (or the selected ones) on every arm, graded. */
	public void testTheResearchQuestions() throws Exception {
		ResearchE2e.assumeEnabled();
		TLContext.getContext().setPerson(PersonManager.getManager().getRoot());
		ResearchFixture.install();

		EvalConfig config = selected(researchScenarios());
		Judge judge = judge(config);
		List<RunRecord> rows = new ArrayList<>();
		for (String arm : List.copyOf(config.getArms().keySet())) {
			EvalConfig single = EvalConfigs.restrictedTo(config, List.of(arm));
			runOn(single.getArms().get(arm));
			rows.addAll(EvalMatrix.run(single, PersonManager.getManager().getRoot(), () -> {
				// A research question reads the web and changes no data.
			}, judge));
		}

		Path csv = outputFile();
		EvalCsv.write(csv, rows);
		System.out.println();
		System.out.println("Research eval rows written to " + csv.toAbsolutePath() + ", judged by " + judge.model());
		System.out.println();
		System.out.println(EvalReport.render(rows));
		for (RunRecord row : rows) {
			for (Verdict verdict : row.verdicts()) {
				System.out.println(row.scenarioId() + " / " + row.arm() + " #" + row.repeat() + " - "
					+ verdict.label() + ": " + (verdict.passed() ? "PASS" : "FAIL") + " - " + verdict.detail());
			}
		}

		assertFalse("The run produced no rows at all.", rows.isEmpty());
		assertTrue("No arm could be started, so this measured nothing. Reasons: "
			+ rows.stream().filter(r -> !r.outcome().ran()).map(RunRecord::error).distinct().toList(),
			rows.stream().anyMatch(r -> r.outcome().ran()));
	}

	/** The question set, as shipped. */
	static EvalConfig researchScenarios() throws Exception {
		return EvalConfigs.load(
			new ClassRelativeBinaryContent(TestResearchEval.class, "research-scenarios.config.xml"));
	}

	/** The configuration with only the arms, scenarios and repeats the system properties ask for. */
	private static EvalConfig selected(EvalConfig config) {
		String arms = System.getProperty(ResearchE2e.ARMS_PROPERTY, "").strip();
		if (!arms.isEmpty()) {
			List<String> wanted = Arrays.stream(arms.split(",")).map(String::strip).toList();
			assertTrue("Unknown arm in -D" + ResearchE2e.ARMS_PROPERTY + "=" + arms,
				config.getArms().keySet().containsAll(wanted));
			config = EvalConfigs.restrictedTo(config, wanted);
		}
		EvalConfig result = TypedConfiguration.copy(config);
		String ids = System.getProperty(ResearchE2e.SCENARIOS_PROPERTY, "").strip();
		if (!ids.isEmpty()) {
			Set<String> wanted = Arrays.stream(ids.split(",")).map(String::strip).collect(Collectors.toSet());
			result.getScenarios().removeIf(scenario -> !wanted.contains(scenario.getId()));
			assertEquals("Unknown scenario in -D" + ResearchE2e.SCENARIOS_PROPERTY + "=" + ids, wanted.size(),
				result.getScenarios().size());
		}
		String repeats = System.getProperty(ResearchE2e.REPEATS_PROPERTY, "").strip();
		if (!repeats.isEmpty()) {
			result.update(result.descriptor().getProperty(EvalConfig.REPEATS), Integer.valueOf(repeats));
		}
		EvalConfigs.check(result);
		return result;
	}

	private static Judge judge(EvalConfig config) {
		String model = System.getProperty(ResearchE2e.JUDGE_PROPERTY, "").strip();
		if (model.isEmpty()) {
			return Judge.of(config);
		}
		return new Judge(model, System.getProperty(ResearchE2e.JUDGE_PROVIDER_PROPERTY, "").strip());
	}

	/**
	 * Assigns the arm's models - its model as the default one, for the lead and every agent the arm
	 * names no model for, and the models of its agents as agent overrides - and restarts the runtime.
	 * The layer of the arm before is removed first, so that nothing of it carries over.
	 */
	private void runOn(EvalConfig.Arm arm) throws Exception {
		XMLProperties.Module.INSTANCE.config().getTypedConfigs().removeAll(_layers);
		_layers.clear();

		StringBuilder overrides = new StringBuilder();
		for (EvalConfig.AgentModel agentModel : arm.getAgentModels()) {
			overrides.append("<agent name=\"").append(agentModel.getAgent()).append("\" model=\"")
				.append(agentModel.getModel()).append('"');
			if (agentModel.getProvider() != null && !agentModel.getProvider().isBlank()) {
				overrides.append(" provider=\"").append(agentModel.getProvider()).append('"');
			}
			overrides.append("/>");
		}
		String capacity = arm.getSubagentWorkers() > 0
			? "<capacity subagent-workers=\"" + arm.getSubagentWorkers() + "\"/>"
			: "";
		Path file = Files.createTempFile("research-arm", ".config.xml");
		Files.writeString(file, """
			<?xml version="1.0" encoding="utf-8" ?>
			<application config:interface="com.top_logic.basic.config.ApplicationConfig$Config"
				xmlns:config="http://www.top-logic.com/ns/config/6.0"
			>
				<services>
					<config service-class="com.top_logic.tl.ai.core.agent.AgentRuntimeService">
						<instance class="com.top_logic.tl.ai.core.agent.AgentRuntimeService" default-model="%s">
							%s
							<agent-overrides>%s</agent-overrides>
						</instance>
					</config>
				</services>
			</application>
			""".formatted(arm.getModel(), capacity, overrides), StandardCharsets.UTF_8);
		file.toFile().deleteOnExit();
		BinaryData layer = BinaryDataFactory.createBinaryDataWithName(file.toFile(), file.getFileName().toString());
		XMLProperties.Module.INSTANCE.config().getTypedConfigs().add(layer);
		_layers.add(layer);
		restartRuntime();
		assertEquals(arm.getModel(), AgentRuntimeService.getInstance().getDefaultModel());
		for (EvalConfig.AgentModel agentModel : arm.getAgentModels()) {
			assertEquals(agentModel.getModel(), AgentRuntimeService.getInstance().resolveModel(agentModel.getAgent()));
		}
	}

	private static void restartRuntime() throws Exception {
		assertTrue("The configuration reloads.", ApplicationConfig.getInstance().reload());
		ModuleUtil.INSTANCE.restart(AgentRuntimeService.Module.INSTANCE, null);
	}

	private static Path outputFile() {
		String configured = System.getProperty(TestEvalPmMatrix.OUTPUT_PROPERTY);
		if (configured != null && !configured.isEmpty()) {
			return Paths.get(configured);
		}
		String stamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
		return Paths.get("target", "eval", "research-" + stamp + ".csv");
	}

	/**
	 * The suite, on the services a research run passes through - or the bare class when the questions
	 * are off, so that the method reaches its assumption without the agent stack being booted.
	 */
	public static Test suite() {
		if (!ResearchE2e.enabled()) {
			return new TestSuite(TestResearchEval.class);
		}
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestResearchEval.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ToolRegistryService.Module.INSTANCE,
				TopLogicAgentDefinitions.Module.INSTANCE,
				AiToolAccessService.Module.INSTANCE,
				WebToolsService.Module.INSTANCE,
				LLMProviderService.Module.INSTANCE,
				AgentRuntimeService.Module.INSTANCE,
				AgentApplicationService.Module.INSTANCE,
				AgentPolicyService.Module.INSTANCE));
	}
}

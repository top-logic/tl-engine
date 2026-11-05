/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.bpe.execution.engine;

import java.util.List;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.bpe.bpml.BPMLDefinitions;
import com.top_logic.bpe.bpml.display.ConfigurableCondition;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.display.SequenceFlowRule;
import com.top_logic.bpe.bpml.model.Collaboration;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.Lane;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.StartEvent;
import com.top_logic.bpe.execution.engine.ExecutionEngine;
import com.top_logic.bpe.execution.engine.ProcessExecutions;
import com.top_logic.bpe.execution.model.ExecutionState;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.TlBpeExecutionFactory;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.execution.script.BPEFunctions;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.persistency.attribute.expr.ExprStorageMapping;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link ProcessExecutions} and the TL-Script functions of {@link BPEFunctions}.
 *
 * <p>
 * The fixture is a workflow with a single lane: the task <code>Review</code> is followed by a
 * gateway at which the user decides between <code>approve</code> (blocked by an error) and
 * <code>rework</code> (the default flow, with a warning), each leading to a task with a single
 * path to the end.
 * </p>
 *
 * <p>
 * Tests of a process object prepared by a creation form use the type {@link #REQUEST} as the model
 * type of the workflow, with the mandatory attribute {@link #SUBJECT}; finishing with additional
 * data uses the form type {@link #ANSWER}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestProcessExecutions extends BasicTestCase {

	private static final String FIXTURE = "fixtures/task-decision.bpmn.xml";

	private static final String APPROVE_FLOW = "Flow_Approve";

	private static final String REWORK_FLOW = "Flow_Rework";

	private static final String REWORK_END_FLOW = "Flow_Rework_End";

	private static final String BLOCKED_MESSAGE = "Approval is blocked.";

	private static final String WARNING_MESSAGE = "Rework takes time.";

	private static final String MODULE = "test.bpe.prepared";

	private static final String REQUEST = "Request";

	private static final String SUBJECT = "subject";

	private static final String ANSWER_TEXT = "answerText";

	private static final String ANSWER = "Answer";

	private static final String TEXT = "text";

	private static int _personCount;

	private static TLClass _requestType;

	private static TLClass _answerType;

	private Collaboration _workflow;

	private StartEvent _start;

	private Person _actor;

	private Person _other;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_actor = createPerson("actor");
		_other = createPerson("other");

		BinaryData fixture = BinaryDataFactory.createBinaryData(TestProcessExecutions.class, FIXTURE);
		_workflow = inTx(() -> {
			Collaboration result = BPMLDefinitions.importCollaboration(fixture);

			Group actors = Group.createGroup("actors" + _personCount);
			actors.addMember(_actor);
			for (Process process : result.getProcesses()) {
				for (Lane lane : process.getLanes()) {
					lane.addActorGroup(actors);
				}
			}

			setRule((SequenceFlow) edge(result, APPROVE_FLOW), RuleType.DEFAULT, BLOCKED_MESSAGE);
			setRule((SequenceFlow) edge(result, REWORK_FLOW), RuleType.WARNING, WARNING_MESSAGE);

			BPMLDefinitions.release(result);
			return result;
		});
		_start = (StartEvent) node(_workflow, "Start");
	}

	public void testStartAndTasks() {
		assertTrue(ProcessExecutions.mayStart(_actor, _start));
		assertFalse(ProcessExecutions.mayStart(_other, _start));
		assertTrue(ProcessExecutions.startEvents(_actor).contains(_start));
		assertFalse(ProcessExecutions.startEvents(_other).contains(_start));

		assertRefused(() -> as(_other, () -> ProcessExecutions.start(_start, _other)));

		Token review = startReview();
		assertEquals("Review", review.getNode().getName());
		assertTrue(ProcessExecutions.tasks(_actor).contains(review));
		assertFalse(ProcessExecutions.tasks(_other).contains(review));
	}

	public void testOptions() {
		Token review = startReview();

		List<Edge> options = BPEFunctions.options(review);
		assertEquals(2, options.size());
		assertTrue(options.contains(edge(_workflow, APPROVE_FLOW)));
		assertTrue(options.contains(edge(_workflow, REWORK_FLOW)));
		assertSame(edge(_workflow, REWORK_FLOW), BPEFunctions.defaultOption(review));

		Edge approve = edge(_workflow, APPROVE_FLOW);
		assertEquals(List.of(BLOCKED_MESSAGE), BPEFunctions.errors(review, approve));
		assertEquals(List.of(), BPEFunctions.warnings(review, approve));

		Edge rework = edge(_workflow, REWORK_FLOW);
		assertEquals(List.of(), BPEFunctions.errors(review, rework));
		assertEquals(List.of(WARNING_MESSAGE), BPEFunctions.warnings(review, rework));
		assertEquals(ResKey.text("rework"), BPEFunctions.optionLabel(review, rework));

		assertRefused(() -> BPEFunctions.errors(review, edge(_workflow, REWORK_END_FLOW)));
	}

	public void testFinishBlocked() {
		Token review = startReview();

		assertRefused(() -> finish(_actor, review, edge(_workflow, APPROVE_FLOW)));
		assertTrue(review.getActive());
	}

	public void testFinishNoActor() {
		Token review = startReview();

		assertRefused(() -> finish(_other, review, edge(_workflow, REWORK_FLOW)));
		assertTrue(review.getActive());
	}

	public void testFinishDecisionRequired() {
		Token review = startReview();

		assertRefused(() -> finish(_actor, review, null));
		assertTrue(review.getActive());
	}

	public void testFinishChosenAndSinglePath() {
		Token review = startReview();
		ProcessExecution execution = review.getProcessExecution();

		Token rework = finish(_actor, review, edge(_workflow, REWORK_FLOW));
		assertFalse(review.getActive());
		assertNotNull(rework);
		assertEquals("Rework", rework.getNode().getName());
		assertTrue(ProcessExecutions.tasks(_actor).contains(rework));
		assertFalse(ProcessExecutions.tasks(_actor).contains(review));

		assertEquals(List.of(edge(_workflow, REWORK_END_FLOW)), BPEFunctions.options(rework));
		assertSame(edge(_workflow, REWORK_END_FLOW), BPEFunctions.defaultOption(rework));

		Token next = finish(_actor, rework, null);
		assertNull(next);
		assertEquals(ExecutionState.finishedClassifier(), execution.getExecutionState());
		assertFalse(ProcessExecutions.tasks(_actor).contains(rework));
	}

	public void testScriptFunctions() throws ConfigurationException {
		QueryExecutor startEvents = compile("bpeStartEvents()");
		QueryExecutor start = compile("e -> bpeStart($e)");
		QueryExecutor startTask = compile("x -> bpeStartTask($x)");
		QueryExecutor tasks = compile("bpeTasks()");
		QueryExecutor options = compile("t -> bpeOptions($t)");
		QueryExecutor errors = compile("t -> o -> bpeErrors($t, $o)");
		QueryExecutor finish = compile("t -> o -> bpeFinish($t, $o)");

		as(_actor, () -> {
			assertTrue(((List<?>) startEvents.execute()).contains(_start));

			ProcessExecution execution = (ProcessExecution) inTx(() -> start.execute(_start));
			Token review = (Token) startTask.execute(execution);
			assertEquals("Review", review.getNode().getName());
			assertTrue(((List<?>) tasks.execute()).contains(review));

			assertEquals(2, ((List<?>) options.execute(review)).size());
			assertEquals(List.of(BLOCKED_MESSAGE), errors.execute(review, edge(_workflow, APPROVE_FLOW)));

			Token rework = (Token) inTx(() -> finish.execute(review, edge(_workflow, REWORK_FLOW)));
			assertEquals("Rework", rework.getNode().getName());
			return null;
		});
		assertTrue(((List<?>) as(_other, () -> tasks.execute())).isEmpty());
	}

	public void testStartEventByName() throws ConfigurationException {
		String name = "Start event by name " + _personCount;
		inTx(() -> {
			_workflow.setName(name);
			return null;
		});
		QueryExecutor byName = compile("w -> bpeStartEvent($w)");
		QueryExecutor byNames = compile("w -> e -> bpeStartEvent($w, $e)");
		QueryExecutor label = compile("e -> bpeStartEventLabel($e)");

		as(_actor, () -> {
			assertSame(_start, byName.execute(name));
			assertSame(_start, byNames.execute(name, "Start"));
			assertNull(byNames.execute(name, "No such event"));
			assertNull(byName.execute("No such workflow"));
			assertEquals(name, label.execute(_start));
			return null;
		});
		assertNull("Not startable by the user.", as(_other, () -> byName.execute(name)));

		inTx(() -> {
			BPMLDefinitions.revoke(_workflow);
			return null;
		});
		assertNull("Not released.", as(_actor, () -> byName.execute(name)));
	}

	public void testStartPrepared() {
		useRequestType();

		TLObject draft = TransientModelFactory.createTransientObject(_requestType);
		draft.tUpdateByName(SUBJECT, "Holidays");

		ProcessExecution execution =
			as(_actor, () -> inTx(() -> ProcessExecutions.start(_start, draft, _actor)));
		assertFalse(execution.tTransient());
		assertSame(_requestType, execution.tType());
		assertEquals("Holidays", execution.tValueByName(SUBJECT));
		assertSame(_start.getProcess(), execution.getProcess());
		assertNotNull(execution.getExecutionNumber());

		Token review = ProcessExecutions.startTask(execution);
		assertNotNull(review);
		assertEquals("Review", review.getNode().getName());
		assertSame(execution, review.getProcessExecution());
	}

	public void testStartPreparedPersistent() throws ConfigurationException {
		useRequestType();
		QueryExecutor start = compile("e -> p -> bpeStart($e, $p)");
		QueryExecutor modelType = compile("e -> bpeModelType($e)");
		assertSame(_requestType, modelType.execute(_start));

		TLObject prepared = inTx(() -> {
			TLObject result = ModelService.getInstance().getFactory().createObject(_requestType);
			result.tUpdateByName(SUBJECT, "Training");
			return result;
		});

		ProcessExecution execution = (ProcessExecution) as(_actor, () -> inTx(() -> start.execute(_start, prepared)));
		assertSame(prepared, execution);
		assertNotNull(ProcessExecutions.startTask(execution));

		assertRefused(() -> as(_actor, () -> inTx(() -> start.execute(_start, prepared))));
	}

	public void testStartPreparedOfOtherType() {
		useRequestType();

		TLObject draft = TransientModelFactory.createTransientObject(_answerType);
		assertRefused(() -> as(_actor, () -> inTx(() -> ProcessExecutions.start(_start, draft, _actor))));
	}

	public void testFinishWithAdditional() throws ConfigurationException {
		useRequestType();
		SequenceFlow rework = (SequenceFlow) edge(_workflow, REWORK_FLOW);
		inTx(() -> {
			rework.setFormType(_answerType);
			rework.setOperation(ExprStorageMapping.INSTANCE.getBusinessObject(
				"process -> finished -> next -> answer -> $process.set(`" + MODULE + ":" + REQUEST + "#"
					+ ANSWER_TEXT + "`, $answer.get(`" + MODULE + ":" + ANSWER + "#" + TEXT + "`))"));
			return null;
		});

		TLObject draft = TransientModelFactory.createTransientObject(_requestType);
		draft.tUpdateByName(SUBJECT, "Holidays");
		Token review = as(_actor, () -> {
			ProcessExecution execution = inTx(() -> ProcessExecutions.start(_start, draft, _actor));
			return ProcessExecutions.startTask(execution);
		});
		assertSame(_answerType, BPEFunctions.formType(review, rework));

		QueryExecutor finish = compile("t -> o -> a -> bpeFinish($t, $o, $a)");
		TLObject answer = TransientModelFactory.createTransientObject(_answerType);
		answer.tUpdateByName(TEXT, "Please rework.");
		Token next = (Token) as(_actor, () -> inTx(() -> finish.execute(review, rework, answer)));
		assertEquals("Rework", next.getNode().getName());
		assertEquals("Please rework.", next.getProcessExecution().tValueByName(ANSWER_TEXT));
	}

	/**
	 * Makes {@link #REQUEST} the model type of the workflow of the test, creating the types of the
	 * test model on first use.
	 */
	private void useRequestType() {
		if (_requestType == null) {
			inTx(() -> {
				TLModel model = ModelService.getApplicationModel();
				TLType stringType = model.getModule("tl.core").getType("String");
				TLModule module = TLModelUtil.addModule(model, MODULE);

				_requestType = TLModelUtil.addClass(module, REQUEST);
				_requestType.getGeneralizations().add(TlBpeExecutionFactory.getProcessExecutionType());
				TLClassProperty subject = TLModelUtil.addProperty(_requestType, SUBJECT, stringType);
				subject.setMandatory(true);
				TLModelUtil.addProperty(_requestType, ANSWER_TEXT, stringType);

				_answerType = TLModelUtil.addClass(module, ANSWER);
				TLModelUtil.addProperty(_answerType, TEXT, stringType).setMandatory(true);
				return null;
			});
		}
		inTx(() -> {
			_start.getProcess().getParticipant().setModelType(_requestType);
			return null;
		});
	}

	private Token startReview() {
		return as(_actor, () -> {
			ProcessExecution execution = inTx(() -> ProcessExecutions.start(_start, _actor));
			Token result = ProcessExecutions.startTask(execution);
			assertNotNull(result);
			return result;
		});
	}

	private static Token finish(Person person, Token token, Edge option) {
		return as(person, () -> inTx(() -> ProcessExecutions.finish(token, option, null, person)));
	}

	private static void setRule(SequenceFlow flow, RuleType type, String message) {
		try {
			@SuppressWarnings("unchecked")
			ConfigurableCondition.Config<ConfigurableCondition> condition =
				TypedConfiguration.newConfigItem(ConfigurableCondition.Config.class);
			condition.update(condition.descriptor().getProperty(ConfigurableCondition.Config.RULETYPE), type);
			condition.update(condition.descriptor().getProperty(ConfigurableCondition.Config.CONDITION),
				ExprFormat.INSTANCE.getValue(ConfigurableCondition.Config.CONDITION, "p -> m -> false"));
			condition.update(condition.descriptor().getProperty(ConfigurableCondition.Config.MESSAGE),
				ResKey.text(message));

			SequenceFlowRule rule = TypedConfiguration.newConfigItem(SequenceFlowRule.class);
			rule.getRuleConditions().add(condition);
			flow.setRule(rule);
		} catch (ConfigurationException ex) {
			throw new AssertionError(ex);
		}
	}

	private static Node node(Collaboration collaboration, String extId) {
		for (Process process : collaboration.getProcesses()) {
			for (Node node : process.getNodes()) {
				if (extId.equals(node.getExtId())) {
					return node;
				}
			}
		}
		throw new AssertionError("No node '" + extId + "'.");
	}

	private static Edge edge(Collaboration collaboration, String extId) {
		for (Process process : collaboration.getProcesses()) {
			for (Node node : process.getNodes()) {
				for (Edge edge : node.getOutgoing()) {
					if (extId.equals(edge.getExtId())) {
						return edge;
					}
				}
			}
		}
		throw new AssertionError("No edge '" + extId + "'.");
	}

	private static Person createPerson(String prefix) {
		String name = prefix + (++_personCount);
		return inTx(() -> Person.create(PersistencyLayer.getKnowledgeBase(), name,
			PersonManager.getManager().getRoot().getAuthenticationDevice()));
	}

	/**
	 * Compiles a script with security switched off: the persons of the test have no read access
	 * to the workflow objects, which would remove them from the results.
	 */
	private static QueryExecutor compile(String script) throws ConfigurationException {
		QueryExecutor result = QueryExecutor.compile(ExprFormat.INSTANCE.getValue("script", script));
		result.disableSecurity();
		return result;
	}

	private static void assertRefused(Runnable operation) {
		try {
			operation.run();
			fail("Operation must be refused.");
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private static <T> T as(Person person, Supplier<T> operation) {
		return TLContextManager.inPersonContext(person, () -> {
			return operation.get();
		});
	}

	private static <T> T inTx(Supplier<T> operation) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			T result = operation.get();
			tx.commit();
			return result;
		}
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(
			ServiceTestSetup.createSetup(
				new TestSuite(TestProcessExecutions.class),
				TypeIndex.Module.INSTANCE,
				SearchBuilder.Module.INSTANCE,
				PersonManager.Module.INSTANCE,
				ExecutionEngine.Module.INSTANCE));
	}

}

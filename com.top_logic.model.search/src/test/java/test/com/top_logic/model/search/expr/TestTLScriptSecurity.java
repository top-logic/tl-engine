/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
import com.top_logic.basic.SessionContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.util.TLContext;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for the security of TL-Script evaluation.
 *
 * <p>
 * The model {@code TestTLScriptSecurity} defines two attribute-driven roles on {@code Project},
 * both resolved by a role rule that navigates to {@code Employee#account} (a
 * {@code tl.accounts:Person}):
 * </p>
 * <ul>
 * <li>{@code ProjectResponsible} (via {@code Project#responsible}) grants read, write and delete.</li>
 * <li>{@code ProjectReader} (via {@code Project#members}) grants read only.</li>
 * </ul>
 * <p>
 * {@code Employee} has no grant and is therefore not readable for non-administrative users
 * (deny-by-default). {@code UnsecuredData} has no grant either, but is declared without security, so
 * its objects &ndash; and those of its specialization {@code UnsecuredDataSub} &ndash; are
 * accessible for everybody.
 * </p>
 *
 * <p>
 * The configuration is stacked: {@code TestTLScriptSecurity-test.config.xml} plays the role of the
 * framework layer, {@code TestTLScriptSecurity-overlay.config.xml} the role of an application that
 * appends its own rules. The application layer adds the roles {@code ProjectAuditor} and
 * {@code ProjectViewer} with their own read grants, restricts the {@code Project} specialization
 * {@code SpecialProject} through revocations, and drops the read denial of
 * {@code Project#secret}. Both layers assign their roles through role rules navigating to a
 * {@code tl.accounts:Person}.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLScriptSecurity extends AbstractSearchExpressionTest {

	/**
	 * Filter selecting all projects whose responsible employee earns more than 1000.
	 *
	 * <p>
	 * The access to the salary happens on the <em>navigated</em> employee, which the restricted
	 * user must not read.
	 * </p>
	 */
	private static final String PROJECTS_WITH_WELL_PAID_RESPONSIBLE =
		"all(`TestTLScriptSecurity:Project`)"
			+ ".filter(p -> $p.get(`TestTLScriptSecurity:Project#responsible`)"
			+ ".get(`TestTLScriptSecurity:Employee#salary`) > 1000)";

	private static final String ALL_EMPLOYEES = "all(`TestTLScriptSecurity:Employee`)";

	/** Name of the test model module all test types belong to. */
	private static final String MODULE = "TestTLScriptSecurity";

	/** Name of the type the framework configuration layer declares its rules for. */
	private static final String PROJECT = "Project";

	/** Name of the {@link #PROJECT} specialization the application layer restricts. */
	private static final String SPECIAL_PROJECT = "SpecialProject";

	/** Name of the attribute both layers declare access rules for. */
	private static final String SECRET = "secret";

	/** Role of the framework layer granting read, write and delete on a project. */
	private static final String ROLE_RESPONSIBLE = MODULE + ".ProjectResponsible";

	/** Role of the framework layer granting read on a project. */
	private static final String ROLE_READER = MODULE + ".ProjectReader";

	/** Role of the application layer, granted read on a project and its specializations. */
	private static final String ROLE_AUDITOR = MODULE + ".ProjectAuditor";

	/** Role of the application layer, granted read on the exact type {@link #PROJECT} only. */
	private static final String ROLE_VIEWER = MODULE + ".ProjectViewer";

	/** Navigates to the responsible employee of the given project. */
	private static final String RESPONSIBLE_OF = "p -> $p.get(`TestTLScriptSecurity:Project#responsible`)";

	/** Reads the salary of the responsible employee of the given project. */
	private static final String SALARY_OF_RESPONSIBLE =
		RESPONSIBLE_OF + ".get(`TestTLScriptSecurity:Employee#salary`)";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Person _root;

	/**
	 * Non-administrative write actor: the account of the responsible employees, so it holds
	 * {@code ProjectResponsible} (read + write + delete) on both projects, but may not read
	 * employees.
	 */
	private Person _user;

	/**
	 * Non-administrative read-only user: the account of a member of {@code p1}, so it holds
	 * {@code ProjectReader} (read only) on {@code p1}.
	 */
	private Person _reader;

	/** Non-administrative user that holds no role on any project. */
	private Person _other;

	private TLObject _e1;

	private TLObject _e2;

	private TLObject _m1;

	/**
	 * Non-administrative user holding {@code ProjectResponsible} through the role rule of the
	 * application layer, on both {@link #_p1} and {@link #_sp1}.
	 */
	private Person _specResponsible;

	/** Non-administrative user holding {@code ProjectAuditor} on {@link #_p1} and {@link #_sp1}. */
	private Person _auditor;

	/** Non-administrative user holding {@code ProjectViewer} on {@link #_p1} and {@link #_sp1}. */
	private Person _viewer;

	private TLObject _p1;

	private TLObject _p2;

	/** A {@code SpecialProject}, the specialization of {@code Project} the application layer restricts. */
	private TLObject _sp1;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		_root = PersonManager.getManager().getRoot();

		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_user = TestPerson.createPerson("tlScriptSecUser");
			_reader = TestPerson.createPerson("tlScriptSecReader");
			_other = TestPerson.createPerson("tlScriptSecOther");
			_specResponsible = TestPerson.createPerson("tlScriptSecSpecResp");
			_auditor = TestPerson.createPerson("tlScriptSecAuditor");
			_viewer = TestPerson.createPerson("tlScriptSecViewer");

			// Employee#name is derived from the account's name (see the model), so it is not set here.
			// _e1/_e2 are the responsible employees; their account _user therefore holds
			// ProjectResponsible (read + write + delete) on p1/p2.
			_e1 = newObject("Employee");
			_e1.tUpdateByName("salary", Integer.valueOf(2000));
			_e1.tUpdateByName("account", _user);

			_e2 = newObject("Employee");
			_e2.tUpdateByName("salary", Integer.valueOf(500));
			_e2.tUpdateByName("account", _user);

			// _m1 is a member of p1; its account _reader therefore holds ProjectReader (read only) on
			// p1.
			_m1 = newObject("Employee");
			_m1.tUpdateByName("salary", Integer.valueOf(300));
			_m1.tUpdateByName("account", _reader);

			_p1 = newObject("Project");
			_p1.tUpdateByName("name", "p1");
			_p1.tUpdateByName("budget", Integer.valueOf(100));
			_p1.tUpdateByName("responsible", _e1);
			_p1.tUpdateByName("members", Collections.singletonList(_m1));
			// The account references feed the role rules of the application layer.
			_p1.tUpdateByName("responsibleAccount", _specResponsible);
			_p1.tUpdateByName("auditorAccount", _auditor);
			_p1.tUpdateByName("viewerAccount", _viewer);

			_p2 = newObject("Project");
			_p2.tUpdateByName("name", "p2");
			_p2.tUpdateByName("budget", Integer.valueOf(200));
			_p2.tUpdateByName("responsible", _e2);
			_p2.tUpdateByName("members", Collections.emptyList());

			// The role rules of the framework layer are not inherited, so the responsible employee
			// and the members of a special project convey no role; its roles come from the account
			// references of the application layer alone. _reader holds ProjectReader here, but the
			// application layer revokes the read right of that role on SpecialProject.
			_sp1 = newObject(SPECIAL_PROJECT);
			_sp1.tUpdateByName("name", "sp1");
			_sp1.tUpdateByName("budget", Integer.valueOf(100));
			_sp1.tUpdateByName("responsible", _e2);
			_sp1.tUpdateByName("members", Collections.emptyList());
			_sp1.tUpdateByName("responsibleAccount", _specResponsible);
			_sp1.tUpdateByName("readerAccount", _reader);
			_sp1.tUpdateByName("auditorAccount", _auditor);
			_sp1.tUpdateByName("viewerAccount", _viewer);

			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	private TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className);
		return DynamicModelService.getFactoryFor("TestTLScriptSecurity").createObject(type);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	/**
	 * The committed result evaluated for the restricted user must not reveal projects through an
	 * attribute of an employee that the user is not allowed to read.
	 *
	 * <p>
	 * The interpreted and the compiled evaluation (asserted to be equal by {@link #eval}) must both
	 * exclude {@code p1}: reading the salary of the navigated employee is denied and yields
	 * {@code null}, so the comparison fails.
	 * </p>
	 */
	public void testNavigationLeakFixedForRestrictedUser() throws Exception {
		becomeUser(_user);
		assertEquals(set(), asSet(eval(PROJECTS_WITH_WELL_PAID_RESPONSIBLE)));
	}

	/**
	 * An administrator may read everything, so the same query returns {@code p1}.
	 */
	public void testNavigationVisibleForAdmin() throws Exception {
		becomeUser(_root);
		assertEquals(set(_p1), asSet(eval(PROJECTS_WITH_WELL_PAID_RESPONSIBLE)));
	}

	/**
	 * Employees are not readable for a non-administrative user (deny-by-default), so the result
	 * filter of the {@link QueryExecutor} removes them.
	 */
	public void testEmployeesNotReadable() throws Exception {
		QueryExecutor query = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));

		becomeUser(_root);
		assertEquals(set(_e1, _e2, _m1), asSet(query.execute()));

		becomeUser(_user);
		assertEquals(set(), asSet(query.execute()));
	}

	/**
	 * A user holding the responsible role (derived from the {@code responsible} reference through a
	 * role rule) may read the granted {@code Project} type. {@code _user} is the responsible of both
	 * projects, so both are visible.
	 */
	public void testProjectReadableForResponsible() throws Exception {
		QueryExecutor query = QueryExecutor.compile(kb(), model(), search("all(`TestTLScriptSecurity:Project`)"));

		becomeUser(_user);
		assertEquals(set(_p1, _p2), asSet(query.execute()));
	}

	/**
	 * Within a system context, the security check is bypassed even for a restricted current user.
	 */
	public void testSystemContextBypass() throws Exception {
		becomeUser(_user);
		// Sanity: restricted in person context.
		assertEquals(set(), asSet(eval(PROJECTS_WITH_WELL_PAID_RESPONSIBLE)));

		TLContext context = TLContext.getContext();
		Person formerPerson = context.getCurrentPersonWrapper();
		// An explicit context id can only be set without a current person.
		context.setCurrentPerson(null);
		context.setContextId(ThreadContextManager.systemContextId(TestTLScriptSecurity.class));
		try {
			assertTrue(ThreadContext.isSystemContext());
			assertEquals(set(_p1), asSet(eval(PROJECTS_WITH_WELL_PAID_RESPONSIBLE)));
		} finally {
			context.setCurrentPerson(formerPerson);
		}
	}

	/**
	 * Disabling the security of a {@link QueryExecutor} lifts the read restriction.
	 */
	public void testDisableSecurity() throws Exception {
		becomeUser(_user);

		QueryExecutor secured =
			QueryExecutor.compile(kb(), model(), search(PROJECTS_WITH_WELL_PAID_RESPONSIBLE));
		assertEquals(set(), asSet(secured.execute()));

		QueryExecutor unsecured =
			QueryExecutor.compile(kb(), model(), search(PROJECTS_WITH_WELL_PAID_RESPONSIBLE));
		unsecured.disableSecurity();
		assertEquals(set(_p1), asSet(unsecured.execute()));
	}

	/**
	 * The result of an execution is secured by the {@link QueryExecutor}: the reference navigation
	 * itself delivers the referenced employee unfiltered, but the employee is not readable for the
	 * user, so the result of the execution is {@code null}.
	 */
	public void testResultSecuredByExecutor() throws Exception {
		QueryExecutor responsible = QueryExecutor.compile(kb(), model(), search(RESPONSIBLE_OF));

		becomeUser(_root);
		assertEquals(_e1, responsible.execute(_p1));

		becomeUser(_user);
		assertNull("The user must not read the navigated employee.", responsible.execute(_p1));
	}

	/**
	 * Disabling the security switches off the result filter as well.
	 */
	public void testDisableSecurityKeepsForbiddenResults() throws Exception {
		becomeUser(_user);

		QueryExecutor secured = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));
		assertEquals(set(), asSet(secured.execute()));

		QueryExecutor unsecured = QueryExecutor.compile(kb(), model(), search(ALL_EMPLOYEES));
		unsecured.disableSecurity();
		assertEquals(set(_e1, _e2, _m1), asSet(unsecured.execute()));
	}

	/**
	 * The result filter keeps model elements: they are meta-data a computation works with, not
	 * business objects.
	 *
	 * <p>
	 * No rights are configured for the meta-model, so a non-administrative user holds no role on a
	 * {@code TLClass}. A script delivering a type (or the type of an object) must nevertheless return
	 * it, otherwise the computation would silently lose its meta-data.
	 * </p>
	 */
	public void testModelElementsNotFiltered() throws Exception {
		becomeUser(_other);
		assertFalse("Test premise: the user holds no read right on the meta-model.",
			ModelAccessRights.getInstance().isReadAllowed(_other, (TLObject) projectType()));

		assertEquals(projectType(), execute(search("`TestTLScriptSecurity:Project`")));
		assertEquals(part("Project", "budget"), execute(search("`TestTLScriptSecurity:Project#budget`")));

		becomeUser(_user);
		assertEquals(projectType(), execute(search("p -> $p.type()"), _p1));
	}

	private TLClass projectType() {
		return type(PROJECT);
	}

	/**
	 * A nested execution (a script called from within an ongoing evaluation) delivers its result
	 * unfiltered, since it is an intermediate result of the calling script.
	 *
	 * <p>
	 * The security of the executed expression itself is unaffected, therefore the salary of the
	 * navigated employee stays inaccessible even in a nested execution.
	 * </p>
	 */
	public void testNestedResultNotFiltered() throws Exception {
		becomeUser(_user);

		QueryExecutor responsible = QueryExecutor.compile(kb(), model(), search(RESPONSIBLE_OF));
		assertEquals("A nested result must keep the objects the calling script computes with.",
			_e1, responsible.executeIntermediate(_p1));

		QueryExecutor salary = QueryExecutor.compile(kb(), model(), search(SALARY_OF_RESPONSIBLE));
		assertNull("The attribute of an unreadable object stays inaccessible in a nested execution.",
			salary.executeIntermediate(_p1));
	}

	/**
	 * Demonstrates that a filter on {@code Project#budget} is pushed into the database query.
	 *
	 * <p>
	 * {@code budget} is a real database column of the (root) project, so the whole filter compiles to
	 * a {@link com.top_logic.model.search.expr.KBQuery} and is evaluated in SQL rather than in memory.
	 * To observe the compiled path while debugging, set a breakpoint in
	 * {@link com.top_logic.model.search.expr.compile.eval.CompiledExpression#processAccess} or in
	 * {@code CompiledAttributeAccess}: the budget access produces a {@code CompiledAttributeAccess},
	 * not an {@code InterpretedExpression}.
	 * </p>
	 */
	public void testBudgetFilterCompiledToSql() throws Exception {
		becomeUser(_root);
		// budget: p1 = 100, p2 = 200; the SQL condition budget > 150 selects p2.
		assertEquals(set(_p2), asSet(eval(
			"all(`TestTLScriptSecurity:Project`).filter(p -> $p.get(`TestTLScriptSecurity:Project#budget`) > 150)")));
	}

	/**
	 * Demonstrates a mixed query where part of the filter runs in SQL and part in memory.
	 *
	 * <p>
	 * The {@code budget} condition (a root column) compiles to SQL, while the navigated
	 * {@code responsible.salary} access falls back to interpreted evaluation (it would otherwise
	 * bypass the read check of the navigated employee). The {@code and} therefore combines a compiled
	 * SQL prefilter with an in-memory condition; a breakpoint in {@code CombinedAndValue} shows the
	 * SQL part being applied first.
	 * </p>
	 */
	public void testMixedBudgetAndSalaryFilter() throws Exception {
		String mixed = "all(`TestTLScriptSecurity:Project`).filter(p -> "
			+ "$p.get(`TestTLScriptSecurity:Project#budget`) > 50 "
			+ "and $p.get(`TestTLScriptSecurity:Project#responsible`)"
			+ ".get(`TestTLScriptSecurity:Employee#salary`) > 1000)";

		// Administrator: budget > 50 matches p1 and p2 (SQL), salary > 1000 keeps only p1 (interpreted).
		becomeUser(_root);
		assertEquals(set(_p1), asSet(eval(mixed)));

		// Restricted user: the SQL budget prefilter still runs, but the navigated salary access is
		// denied (null), so no project remains.
		becomeUser(_user);
		assertEquals(set(), asSet(eval(mixed)));
	}

	private static final String UPDATE_BUDGET = "p -> $p.set(`TestTLScriptSecurity:Project#budget`, 999)";

	private static final String ADD_MEMBER = "p -> e -> $p.add(`TestTLScriptSecurity:Project#members`, $e)";

	private static final String REMOVE_MEMBER = "p -> e -> $p.remove(`TestTLScriptSecurity:Project#members`, $e)";

	private static final String DELETE = "p -> $p.delete()";

	/**
	 * {@code set(...)} (Update) succeeds for the responsible's account (ProjectResponsible grants
	 * write, see the test configuration).
	 */
	public void testUpdateAllowedForResponsible() throws Exception {
		becomeUser(_user);
		try (Transaction tx = beginTx()) {
			execute(search(UPDATE_BUDGET), _p1);
			tx.commit();
		}
		assertEquals(999, ((Number) _p1.tValueByName("budget")).intValue());
	}

	/**
	 * {@code set(...)} (Update) is denied for a user without any role on the object (WRITE check).
	 */
	public void testUpdateDeniedForNonReader() {
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(UPDATE_BUDGET), _p1);
				tx.commit();
			}
		});
	}

	/**
	 * Combined allowed-then-denied update in a single method: the reader first performs and commits
	 * an allowed write to the project, and afterwards a write by an unrelated, roleless user must
	 * still be denied.
	 *
	 * <p>
	 * This is a regression test for a {@code PersonRoleCache} staleness bug: the per-interaction role
	 * computation cache in {@code StorageAccessManager} was reused after the current person was
	 * switched (via {@code setCurrentPerson}), so it reported the previous user's roles for the new
	 * user, wrongly allowing the second write. The cache is now rebuilt when the current person
	 * changes (see {@code StorageAccessManager.getRoleComputation}).
	 * </p>
	 */
	public void testUpdatePermission() throws Exception {
		// The responsible's account may update the project (ProjectResponsible grants write).
		becomeUser(_user);
		try (Transaction tx = beginTx()) {
			execute(search(UPDATE_BUDGET), _p1);
			tx.commit();
		}
		assertEquals(999, ((Number) _p1.tValueByName("budget")).intValue());

		// The unrelated, roleless user must still be denied to write after the reader's commit.
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(UPDATE_BUDGET), _p1);
				tx.commit();
			}
		});
	}

	/**
	 * {@code add(...)} / {@code remove(...)} succeed for the responsible's account (ProjectResponsible
	 * grants write). Editing {@code members} cannot revoke the actor's own grant, which comes from
	 * {@code responsible}.
	 */
	public void testAddRemoveAllowedForResponsible() throws Exception {
		becomeUser(_user);
		try (Transaction tx = beginTx()) {
			execute(search(ADD_MEMBER), _p1, _e1);
			tx.commit();
		}
		assertTrue(members(_p1).contains(_e1));

		try (Transaction tx = beginTx()) {
			execute(search(REMOVE_MEMBER), _p1, _e1);
			tx.commit();
		}
		assertFalse(members(_p1).contains(_e1));
	}

	/**
	 * {@code add(...)} is denied for a user without any role on the object (WRITE check).
	 */
	public void testAddDeniedForNonReader() {
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(ADD_MEMBER), _p1, _e1);
				tx.commit();
			}
		});
	}

	/**
	 * {@code delete()} (DeleteObject) succeeds for the responsible's account (ProjectResponsible
	 * grants delete, see the test configuration).
	 */
	public void testDeleteAllowedForResponsible() throws Exception {
		becomeUser(_user);
		try (Transaction tx = beginTx()) {
			execute(search(DELETE), _p2);
			tx.commit();
		}
		assertFalse(_p2.tValid());
	}

	/**
	 * {@code delete()} (DeleteObject) is denied for a user without any role on the object (DELETE
	 * check).
	 */
	public void testDeleteDeniedForNonReader() {
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(DELETE), _p1);
				tx.commit();
			}
		});
		assertTrue("Denied deletion must not remove the object.", _p1.tValid());
	}

	/**
	 * A member's account holds {@code ProjectReader}, which grants read only: it may read the project
	 * it is a member of, but writing and deleting it is denied.
	 */
	public void testReadOnlyMemberCannotWrite() throws Exception {
		QueryExecutor allProjects =
			QueryExecutor.compile(kb(), model(), search("all(`TestTLScriptSecurity:Project`)"));

		becomeUser(_reader);
		// _reader is a member of p1 only, so it may read p1 but not p2.
		assertEquals(set(_p1), asSet(allProjects.execute()));

		// ProjectReader has no write grant: updating the project is denied.
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(UPDATE_BUDGET), _p1);
				tx.commit();
			}
		});

		// ProjectReader has no delete grant: deleting the project is denied.
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search(DELETE), _p1);
				tx.commit();
			}
		});
		assertTrue("Denied deletion must not remove the object.", _p1.tValid());
	}

	/**
	 * {@code referers(...)} respects the read access to the base object: if the object is not
	 * readable, no referrers are returned.
	 */
	public void testReferersRespectsReadAccess() throws Exception {
		// e1 is the responsible of p1.
		String referers = "e -> $e.referers(`TestTLScriptSecurity:Project#responsible`)";

		// The user cannot read the employee, so its referrers are not disclosed.
		becomeUser(_user);
		assertEquals(set(), asSet(execute(search(referers), _e1)));

		// An administrator sees the referrers.
		becomeUser(_root);
		assertEquals(set(_p1), asSet(execute(search(referers), _e1)));
	}

	/**
	 * Object creation ({@code new(...)}) is denied for a user without a CREATE role on the (global)
	 * security root &ndash; create condition 1 with the security root as context, because a top-level
	 * {@code new(...)} has no composition parent.
	 */
	public void testCreateObjectDeniedForRoleless() {
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search("new(`TestTLScriptSecurity:Employee`)"));
				tx.rollback();
			}
		});
	}

	/**
	 * An administrator bypasses the create check and may create instances.
	 */
	public void testCreateObjectAllowedForAdmin() throws Exception {
		becomeUser(_root);
		try (Transaction tx = beginTx()) {
			Object created = execute(search("new(`TestTLScriptSecurity:Employee`)"));
			assertTrue(created instanceof TLObject);
			assertEquals("Employee", ((TLObject) created).tType().getName());
			tx.rollback();
		}
	}

	/**
	 * {@code copy()} is a shortcut for {@code new(type)..set(attr, $orig.get(attr))..}. On the write
	 * side, allocating a copy requires the CREATE permission on the copied type (mirroring
	 * {@code new(type)}): an administrator may copy, a user without the CREATE right on the top-level
	 * type may not.
	 */
	public void testCopyRequiresCreatePermission() throws Exception {
		// Admin bypasses the create check -> faithful copy.
		becomeUser(_root);
		try (Transaction tx = beginTx()) {
			TLObject adminCopy = (TLObject) execute(search("p -> $p.copy()"), _p1);
			assertEquals(100, ((Number) adminCopy.tValueByName("budget")).intValue());
			tx.rollback();
		}

		// A user without the CREATE right on the (top-level) Project type may not copy it, mirroring
		// new(Project): allocating the copy is denied (create condition 1 against the security root).
		becomeUser(_other);
		assertPermissionDenied(() -> {
			try (Transaction tx = beginTx()) {
				execute(search("p -> $p.copy()"), _p1);
				tx.rollback();
			}
		});
	}

	/**
	 * {@code canRead(...)} reports the read access without failing or filtering: the responsible's
	 * account may read both projects.
	 */
	public void testCanReadForResponsible() throws Exception {
		becomeUser(_user);
		assertTrue((Boolean) execute(search("p -> canRead($p)"), _p1));
		assertTrue((Boolean) execute(search("p -> canRead($p)"), _p2));
	}

	/**
	 * {@code canRead(...)} for a member (ProjectReader on its project only): {@code p1} readable,
	 * {@code p2} not.
	 */
	public void testCanReadForMember() throws Exception {
		becomeUser(_reader);
		assertTrue((Boolean) execute(search("p -> canRead($p)"), _p1));
		assertFalse((Boolean) execute(search("p -> canRead($p)"), _p2));
	}

	/**
	 * {@code canRead(...)} is {@code false} for a roleless user (deny-by-default), instead of
	 * throwing.
	 */
	public void testCanReadDeniedForRoleless() throws Exception {
		becomeUser(_other);
		assertFalse((Boolean) execute(search("p -> canRead($p)"), _p1));
	}

	/**
	 * {@code canWrite(...)} distinguishes the responsible (write granted) from a read-only member.
	 */
	public void testCanWrite() throws Exception {
		becomeUser(_user);
		assertTrue((Boolean) execute(search("p -> canWrite($p)"), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(search("p -> canWrite($p)"), _p1));
	}

	/**
	 * {@code canDelete(...)} distinguishes the responsible (delete granted) from a read-only member.
	 */
	public void testCanDelete() throws Exception {
		becomeUser(_user);
		assertTrue((Boolean) execute(search("p -> canDelete($p)"), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(search("p -> canDelete($p)"), _p1));
	}

	/**
	 * {@code canWriteAttribute(...)} lets a script check write access to an attribute up-front, so it
	 * can branch instead of running into the {@code set(...)} permission exception.
	 */
	public void testCanWriteAttribute() throws Exception {
		String script = "p -> canWriteAttribute($p, `TestTLScriptSecurity:Project#budget`)";

		becomeUser(_user);
		assertTrue((Boolean) execute(search(script), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(search(script), _p1));
	}

	/**
	 * {@code canReadAttribute(...)} follows the type-level read access: the member may read
	 * attributes of {@code p1} (its project) but not of {@code p2}.
	 */
	public void testCanReadAttribute() throws Exception {
		String script = "p -> canReadAttribute($p, `TestTLScriptSecurity:Project#name`)";

		becomeUser(_reader);
		assertTrue((Boolean) execute(search(script), _p1));
		assertFalse((Boolean) execute(search(script), _p2));
	}

	/**
	 * {@code canCreate(...)} checks the write access to the composition context (adding to
	 * {@code members}): allowed for the responsible, denied for the read-only member.
	 */
	public void testCanCreate() throws Exception {
		String script = "p -> canCreate($p, `TestTLScriptSecurity:Project#members`)";

		becomeUser(_user);
		assertTrue((Boolean) execute(search(script), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(search(script), _p1));
	}

	/**
	 * {@code canExecute(...)} checks an operation referenced by its command group id (here the
	 * built-in {@code Delete}): allowed for the responsible, denied for the read-only member.
	 */
	public void testCanExecute() throws Exception {
		String script = "p -> canExecute($p, \"" + SimpleBoundCommandGroup.DELETE_NAME + "\")";

		becomeUser(_user);
		assertTrue((Boolean) execute(search(script), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(search(script), _p1));
	}

	/**
	 * {@code canExecute(...)} with an unknown operation name fails with a {@link TopLogicException}.
	 */
	public void testCanExecuteUnknownOperation() {
		becomeUser(_user);
		try {
			execute(search("p -> canExecute($p, \"NoSuchOperation\")"), _p1);
			fail("Expected a failure for an unknown operation.");
		} catch (Throwable ex) {
			if (!hasCause(ex, TopLogicException.class)) {
				throw new AssertionError("Expected a TopLogicException for an unknown operation, but got: " + ex, ex);
			}
		}
	}

	/**
	 * The functions declare named parameters (via their {@code ArgumentDescriptor}), so they can be
	 * called with named arguments.
	 */
	public void testNamedArguments() throws Exception {
		becomeUser(_user);
		assertTrue((Boolean) execute(search("p -> canRead(object: $p)"), _p1));
		assertTrue((Boolean) execute(
			search("p -> canWriteAttribute(object: $p, attribute: `TestTLScriptSecurity:Project#budget`)"), _p1));
		assertTrue((Boolean) execute(
			search("p -> canExecute(object: $p, operation: \"" + SimpleBoundCommandGroup.DELETE_NAME + "\")"), _p1));

		becomeUser(_reader);
		assertFalse((Boolean) execute(
			search("p -> canWriteAttribute(object: $p, attribute: `TestTLScriptSecurity:Project#budget`)"), _p1));
	}

	/**
	 * Instance-level checks are skipped for not-yet-committed objects (their roles are only computed
	 * at commit), while committed objects are checked normally.
	 */
	public void testUncommittedObjectExemptFromInstanceChecks() {
		becomeUser(_other);
		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		// A committed object is checked: the roleless user may neither write nor delete it.
		assertFalse(accessRights.isAllowed(_other, _p1, SimpleBoundCommandGroup.WRITE));
		assertFalse(accessRights.isAllowed(_other, _p1, SimpleBoundCommandGroup.DELETE));

		try (Transaction tx = beginTx()) {
			// A freshly created, not-yet-committed object is exempt (no computed roles yet).
			TLObject fresh = newObject("Project");
			assertTrue(accessRights.isAllowed(_other, fresh, SimpleBoundCommandGroup.WRITE));
			assertTrue(accessRights.isAllowed(_other, fresh, SimpleBoundCommandGroup.DELETE));
			assertTrue(accessRights.isReadAllowed(_other, fresh));
			tx.rollback();
		}
	}

	/**
	 * A computed attribute has definer's-rights semantics: its computation does not apply the
	 * current user's security, but reading the computed attribute itself is still gated by its own
	 * read grant.
	 *
	 * <p>
	 * {@code Project#runningCosts} collects the responsible and all members into a set and sums
	 * their salaries. Employees are not readable for non-administrative users, yet the responsible
	 * (who may read the project) still obtains the total through the derived attribute &ndash; the
	 * computation ignores the user's inability to read {@code Employee}. A user who may not read the
	 * project at all gets the empty value (the read of the derived attribute is denied at the type
	 * level).
	 * </p>
	 */
	public void testDerivedAttributeUsesDefinerRights() throws Exception {
		String script = "p -> $p.get(`TestTLScriptSecurity:Project#runningCosts`)";

		// responsible e1 (2000) + member m1 (300) = 2300, although the user must not read Employee.
		becomeUser(_user);
		assertEquals(2300, ((Number) execute(search(script), _p1)).intValue());

		becomeUser(_other);
		assertNull(execute(search(script), _p1));
	}

	/**
	 * A transient object is never persisted and is exempt from instance-level checks, exactly like a
	 * not-yet-committed persistent object: a roleless user may set/read/delete its attributes.
	 */
	public void testTransientObjectExemptFromInstanceChecks() throws Exception {
		becomeUser(_other);
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		TLClass projectType = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:Project");
		com.top_logic.model.TLStructuredTypePart budget = projectType.getPart("budget");
		TLObject transientProject =
			com.top_logic.model.impl.TransientObjectFactory.INSTANCE.createObject(projectType, null);

		assertTrue(accessRights.isAllowed(_other, transientProject, budget, SimpleBoundCommandGroup.WRITE));
		assertTrue(accessRights.isAllowed(_other, transientProject, SimpleBoundCommandGroup.DELETE));
		assertTrue(accessRights.isReadAllowed(_other, transientProject, budget));
	}

	/**
	 * An attribute-level grant that is present but lists no roles denies the operation for every
	 * role, overriding the (passed) class-level grant. Only a bypassing super-user is unaffected.
	 *
	 * <p>
	 * {@code Project#secret} has an empty {@code Write} grant (see the test config). The responsible
	 * ({@code _user}) holds the class-level read and write rights on {@code Project}, yet may not
	 * write {@code secret}. Attributes without such a deny ({@code budget}, {@code name}) stay
	 * governed by the class level.
	 * </p>
	 */
	public void testAttributeDeniedForAllRoles() throws Exception {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		TLStructuredTypePart secret = part(PROJECT, SECRET);
		TLStructuredTypePart budget = part(PROJECT, "budget");
		TLStructuredTypePart name = part(PROJECT, "name");

		becomeUser(_user);
		// Denied for the responsible, although the class grants write.
		assertFalse("An empty grant denies the operation for every role.",
			accessRights.isAllowed(_user, _p1, secret, SimpleBoundCommandGroup.WRITE));
		// Attributes without a deny stay governed by the class level (responsible: read + write).
		assertTrue("An attribute without a grant follows the class decision.",
			accessRights.isAllowed(_user, _p1, budget, SimpleBoundCommandGroup.WRITE));
		assertTrue("An attribute without a grant follows the class decision.",
			accessRights.isReadAllowed(_user, _p1, name));

		// A bypassing super-user (system context) is unaffected by the attribute deny.
		TLContext context = TLContext.getContext();
		Person formerPerson = context.getCurrentPersonWrapper();
		context.setCurrentPerson(null);
		context.setContextId(ThreadContextManager.systemContextId(TestTLScriptSecurity.class));
		try {
			assertTrue(ThreadContext.isSystemContext());
			assertTrue("A system context bypasses the attribute deny.",
				accessRights.isAllowed(null, _p1, secret, SimpleBoundCommandGroup.WRITE));
		} finally {
			context.setCurrentPerson(formerPerson);
		}

		// A bypassing super-user is unaffected by the attribute deny.
		becomeUser(_root);
		assertTrue("A super-user bypasses the attribute deny.",
			accessRights.isAllowed(_root, _p1, secret, SimpleBoundCommandGroup.WRITE));
	}

	/**
	 * The application configuration layer appends a read grant for {@link #ROLE_AUDITOR} to the
	 * rules the framework layer declares for {@code Project}, so a user holding that role may read
	 * a project although the framework layer does not know the role at all.
	 */
	public void testAddedRoleMayRead() throws Exception {
		assertTrue("The role added by the application layer must be allowed to read a project.",
			allowedRoleNames(PROJECT, SimpleBoundCommandGroup.READ).contains(ROLE_AUDITOR));

		becomeUser(_auditor);
		assertTrue("The auditor holds the added role on the project and must therefore read it.",
			ModelAccessRights.getInstance().isReadAllowed(_auditor, _p1));
	}

	/**
	 * The scenario of the ticket: adding a role in the application configuration layer must not
	 * drop the roles the framework layer granted. All roles of both layers keep the read right on
	 * {@code Project}, and both users of the framework layer still read their project.
	 */
	public void testAddedRoleKeepsFrameworkRoles() throws Exception {
		assertEquals("The application layer must add to the read roles of the framework layer, not replace them.",
			set(ROLE_RESPONSIBLE, ROLE_READER, ROLE_AUDITOR, ROLE_VIEWER),
			allowedRoleNames(PROJECT, SimpleBoundCommandGroup.READ));

		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		becomeUser(_user);
		assertTrue("The responsible of the framework layer must still read the project.",
			accessRights.isReadAllowed(_user, _p1));

		becomeUser(_reader);
		assertTrue("The reader of the framework layer must still read the project.",
			accessRights.isReadAllowed(_reader, _p1));
	}

	/**
	 * The application layer grants the added role the read operation only, so it obtains neither
	 * the write nor the delete right the framework layer reserves for {@link #ROLE_RESPONSIBLE}.
	 */
	public void testAddedRoleHasNoWrite() throws Exception {
		assertEquals("The write right must stay with the role of the framework layer.",
			set(ROLE_RESPONSIBLE), allowedRoleNames(PROJECT, SimpleBoundCommandGroup.WRITE));
		assertEquals("The delete right must stay with the role of the framework layer.",
			set(ROLE_RESPONSIBLE), allowedRoleNames(PROJECT, SimpleBoundCommandGroup.DELETE));

		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		becomeUser(_auditor);
		assertFalse("The auditor must not write the project it may read.",
			accessRights.isAllowed(_auditor, _p1, SimpleBoundCommandGroup.WRITE));
		assertFalse("The auditor must not delete the project it may read.",
			accessRights.isAllowed(_auditor, _p1, SimpleBoundCommandGroup.DELETE));
	}

	/**
	 * A revocation naming a single role removes exactly that role: {@code SpecialProject} receives
	 * the read right of both framework roles from its generalization {@code Project}, and the
	 * application layer revokes it for {@link #ROLE_READER} alone. The reader therefore reads a
	 * project but not a special project, while the responsible reads both.
	 */
	public void testRevokeOfOneOfTwoRoles() throws Exception {
		assertEquals("Exactly the revoked role must be missing on the specialization.",
			set(ROLE_RESPONSIBLE, ROLE_AUDITOR), allowedRoleNames(SPECIAL_PROJECT, SimpleBoundCommandGroup.READ));
		assertTrue("A revocation on the specialization must not affect the general type.",
			allowedRoleNames(PROJECT, SimpleBoundCommandGroup.READ).contains(ROLE_READER));

		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		becomeUser(_reader);
		assertTrue("Test premise: the reader holds its role on the special project, too.",
			heldRoleNames(_reader, _sp1).contains(ROLE_READER));
		assertTrue("The reader holds its role on the project and may read it.",
			accessRights.isReadAllowed(_reader, _p1));
		assertFalse("The reader holds its role on the special project, but the read right was revoked.",
			accessRights.isReadAllowed(_reader, _sp1));

		becomeUser(_specResponsible);
		assertTrue("The responsible keeps the read right on the special project.",
			accessRights.isReadAllowed(_specResponsible, _sp1));
	}

	/**
	 * A revocation without roles drops the whole operation entry of an attribute: the framework
	 * layer denies reading and writing {@code Project#secret} for every role, the application layer
	 * revokes the read entry. Reading the attribute is then decided by the class alone, while
	 * writing stays denied for everybody.
	 */
	public void testRevokeWithoutRolesOnAttribute() throws Exception {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		TLStructuredTypePart secret = part(PROJECT, SECRET);

		becomeUser(_user);
		assertTrue("Without an attribute entry, the class decision applies: the responsible may read.",
			accessRights.isReadAllowed(_user, _p1, secret));
		assertFalse("The write entry of the framework layer is untouched and denies for every role.",
			accessRights.isAllowed(_user, _p1, secret, SimpleBoundCommandGroup.WRITE));

		becomeUser(_reader);
		assertTrue("The reader may read the project and therefore its attribute.",
			accessRights.isReadAllowed(_reader, _p1, secret));

		becomeUser(_other);
		assertFalse("A user without a role on the project may not read the attribute either.",
			accessRights.isReadAllowed(_other, _p1, secret));
	}

	/**
	 * A grant following a revocation allows the operation again: the application layer revokes the
	 * delete right of {@link #ROLE_RESPONSIBLE} on {@code SpecialProject} and grants it again later
	 * in the same rule sequence, so the responsible may still delete a special project.
	 */
	public void testGrantAfterRevoke() throws Exception {
		assertEquals("The grant following the revocation must restore the role.",
			set(ROLE_RESPONSIBLE), allowedRoleNames(SPECIAL_PROJECT, SimpleBoundCommandGroup.DELETE));

		becomeUser(_specResponsible);
		assertTrue("A delete right revoked and granted again must be effective.",
			ModelAccessRights.getInstance().isAllowed(_specResponsible, _sp1, SimpleBoundCommandGroup.DELETE));
	}

	/**
	 * A revocation that no grant follows takes the operation away: the application layer revokes the
	 * write right of {@link #ROLE_RESPONSIBLE} on {@code SpecialProject} only, so the responsible
	 * writes a project but not a special project.
	 */
	public void testRevokeWithoutLaterGrant() throws Exception {
		assertEquals("No role may write a special project.",
			set(), allowedRoleNames(SPECIAL_PROJECT, SimpleBoundCommandGroup.WRITE));
		assertEquals("The general type keeps its write role.",
			set(ROLE_RESPONSIBLE), allowedRoleNames(PROJECT, SimpleBoundCommandGroup.WRITE));

		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		becomeUser(_specResponsible);
		assertTrue("Test premise: the responsible holds its role on the special project, too.",
			heldRoleNames(_specResponsible, _sp1).contains(ROLE_RESPONSIBLE));
		assertTrue("The responsible may write a project.",
			accessRights.isAllowed(_specResponsible, _p1, SimpleBoundCommandGroup.WRITE));
		assertFalse("The responsible must not write a special project.",
			accessRights.isAllowed(_specResponsible, _sp1, SimpleBoundCommandGroup.WRITE));
	}

	/**
	 * The inherit flag of a rule decides whether it reaches a specialization: the read grant of the
	 * application layer for {@link #ROLE_AUDITOR} is inherited and therefore holds for
	 * {@code SpecialProject}, while the read grant for {@link #ROLE_VIEWER} is not inherited and
	 * applies to {@code Project} alone. Both users hold their role on both objects, so only the
	 * flag makes the difference.
	 */
	public void testInheritedAndNonInheritedGrant() throws Exception {
		Set<String> specialReadRoles = allowedRoleNames(SPECIAL_PROJECT, SimpleBoundCommandGroup.READ);
		assertTrue("An inherited grant must reach the specialization.",
			specialReadRoles.contains(ROLE_AUDITOR));
		assertFalse("A grant that is not inherited must not reach the specialization.",
			specialReadRoles.contains(ROLE_VIEWER));

		ModelAccessRights accessRights = ModelAccessRights.getInstance();

		becomeUser(_auditor);
		assertTrue("The auditor may read a project.", accessRights.isReadAllowed(_auditor, _p1));
		assertTrue("The inherited grant lets the auditor read a special project, too.",
			accessRights.isReadAllowed(_auditor, _sp1));

		becomeUser(_viewer);
		assertTrue("Test premise: the viewer holds its role on the special project, too.",
			heldRoleNames(_viewer, _sp1).contains(ROLE_VIEWER));
		assertTrue("The viewer may read a project.", accessRights.isReadAllowed(_viewer, _p1));
		assertFalse("The grant of the viewer must not reach the specialization.",
			accessRights.isReadAllowed(_viewer, _sp1));
	}

	/**
	 * The names of the roles the {@link SecurityConfigurationService} allows to perform the given
	 * operation on the given type of the test model.
	 */
	private Set<String> allowedRoleNames(String typeName, BoundCommandGroup operation) {
		Set<String> result = new HashSet<>();
		for (BoundedRole role : ModelAccessRights.getInstance().getAllowedRoles(type(typeName), operation)) {
			result.add(role.getName());
		}
		return result;
	}

	private TLClass type(String typeName) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + typeName);
	}

	/**
	 * The names of the roles the given person holds on the given object, as computed by the role
	 * rules.
	 */
	private Set<String> heldRoleNames(Person person, TLObject object) {
		Set<String> result = new HashSet<>();
		for (BoundRole role : AccessManager.getInstance().getRoles(person, (BoundObject) object)) {
			result.add(role.getName());
		}
		return result;
	}

	/**
	 * An object of a type without security is accessible for everybody, including its attribute
	 * values and the creation of such an object.
	 *
	 * <p>
	 * {@code UnsecuredData} is declared without security and has no grant at all (see the test
	 * config). Therefore a user without any role may read, write and delete the object as well as
	 * {@code UnsecuredData#data}, and may create objects of that type. The same user has no access
	 * to an access controlled type without a matching grant.
	 * </p>
	 */
	public void testTypeWithoutSecurityAccessibleForEverybody() throws Exception {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		TLClass unsecuredType = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:UnsecuredData");
		TLStructuredTypePart data = part("UnsecuredData", "data");
		TLStructuredTypePart budget = part("Project", "budget");

		TLObject unsecured;
		try (Transaction tx = beginTx()) {
			unsecured = newObject("UnsecuredData");
			unsecured.tUpdateByName("data", "some unsecured value");
			tx.commit();
		}

		assertTrue(accessRights.isWithoutSecurity(unsecuredType));

		becomeUser(_other);
		// The attribute values.
		assertTrue(accessRights.isReadAllowed(_other, unsecured, data));
		assertTrue(accessRights.isAllowed(_other, unsecured, data, SimpleBoundCommandGroup.WRITE));
		// The object itself.
		assertTrue(accessRights.isReadAllowed(_other, unsecured));
		assertTrue(accessRights.isAllowed(_other, unsecured, SimpleBoundCommandGroup.WRITE));
		assertTrue(accessRights.isAllowed(_other, unsecured, SimpleBoundCommandGroup.DELETE));
		// The creation of such an object. Note: The cast selects the type-based overload, since a
		// TLClass is a TLObject, too.
		assertTrue(accessRights.isAllowedCreate(_other, unsecuredType, (TLObject) null));
		// The type is reported as accessible, although no rights are configured for it.
		assertTrue(accessRights.getAccessibleTypes(_other, SimpleBoundCommandGroup.READ)
			.contains(unsecuredType));

		// An access controlled type without a matching grant stays inaccessible.
		assertFalse(accessRights.isReadAllowed(_other, _p1, budget));
		assertFalse(accessRights.isAllowed(_other, _p1, SimpleBoundCommandGroup.WRITE));
	}

	/**
	 * A specialization of a type without security is without security, too.
	 *
	 * <p>
	 * {@code UnsecuredDataSub} specializes {@code UnsecuredData}, which is declared without
	 * security, and has no own configuration entry (see the test config), yet its objects are
	 * accessible for a user without any role.
	 * </p>
	 */
	public void testSpecializationOfTypeWithoutSecurityIsWithoutSecurity() throws Exception {
		ModelAccessRights accessRights = ModelAccessRights.getInstance();
		TLStructuredTypePart data = part("UnsecuredData", "data");

		TLObject sub;
		try (Transaction tx = beginTx()) {
			sub = newObject("UnsecuredDataSub");
			tx.commit();
		}

		assertTrue(accessRights
			.isWithoutSecurity((TLClass) TLModelUtil.findType("TestTLScriptSecurity:UnsecuredDataSub")));

		becomeUser(_other);
		assertTrue(accessRights.isReadAllowed(_other, sub, data));
		assertTrue(accessRights.isAllowed(_other, sub, data, SimpleBoundCommandGroup.WRITE));
		assertTrue(accessRights.isAllowed(_other, sub, SimpleBoundCommandGroup.DELETE));
	}

	private TLStructuredTypePart part(String className, String partName) {
		return type(className).getPart(partName);
	}

	@SuppressWarnings("unchecked")
	private Collection<? extends TLObject> members(TLObject project) {
		return (Collection<? extends TLObject>) project.tValueByName("members");
	}

	/**
	 * Asserts that the given action fails with a permission-denial {@link TopLogicException} (which
	 * the executor wraps in a {@link RuntimeException}).
	 */
	private void assertPermissionDenied(ThrowingAction action) {
		try {
			action.run();
		} catch (Throwable ex) {
			if (hasCause(ex, TopLogicException.class)) {
				return;
			}
			throw new AssertionError("Expected a permission-denial TopLogicException, but got: " + ex, ex);
		}
		fail("Expected a permission denial, but the operation succeeded.");
	}

	private static boolean hasCause(Throwable ex, Class<? extends Throwable> type) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (type.isInstance(current)) {
				return true;
			}
		}
		return false;
	}

	@FunctionalInterface
	private interface ThrowingAction {
		void run() throws Exception;
	}

	public static Test suite() {
		return suite(TestTLScriptSecurity.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}

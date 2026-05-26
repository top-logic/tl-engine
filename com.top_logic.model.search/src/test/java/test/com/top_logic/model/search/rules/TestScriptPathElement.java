/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.model.search.rules;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.model.testScriptPathElement.Assembly;
import test.com.top_logic.model.search.model.testScriptPathElement.Assignment;
import test.com.top_logic.model.search.model.testScriptPathElement.Plant;
import test.com.top_logic.model.search.model.testScriptPathElement.Plants;
import test.com.top_logic.model.search.model.testScriptPathElement.TestScriptPathElementFactory;
import test.com.top_logic.model.search.model.testScriptPathElement.ZArea;
import test.com.top_logic.model.search.model.testScriptPathElement.ZAreaInstance;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.boundsec.manager.rule.BaseObjects;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.rules.PathByExpression;
import com.top_logic.model.search.rules.PathByExpression.Config;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Tests for {@link PathByExpression}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestScriptPathElement extends BasicTestCase {

	private KnowledgeBase _kb;

	private Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {

		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	private Plants _root1;

	private Plants _root2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_root1 = factory().getRoot1Singleton();
		_root2 = factory().getRoot2Singleton();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction deleteTX = beginTX()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			deleteTX.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	private Transaction beginTX() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	private static TestScriptPathElementFactory factory() {
		return TestScriptPathElementFactory.getInstance();
	}

	/**
	 * Tests inheritance of the the configured rules.
	 */
	public void testRuleInheritance() throws RuntimeException {
		Person person1;
		Person person2;
		BoundedRole readerRole = BoundedRole.getRoleByName("TestScriptPathElement.Reader");

		Plant plant1;
		Plant plant2;
		Assignment assign1;
		Assignment assign2;
		try (Transaction tx = beginTX()) {
			plant1 = createPlant("plant1");
			plant2 = createPlant("plant2");
			ZArea z1 = createZArea("z1");

			assign1 = createAssignment("assignment1", plant1, z1);
			assign2 = createAssignment("assignment2", plant2, z1);

			person1 = TestPerson.createPerson("p1");
			person2 = TestPerson.createPerson("p2");

			assign1.addReader(person1);
			assign1.addReader(person2);

			assign2.addReader(person2);

			tx.commit();
		}

		assertTrue(hasRole(person1, readerRole, assign1));
		assertFalse(hasRole(person1, readerRole, assign2));
		assertTrue(hasRole(person2, readerRole, assign1));
		assertTrue(hasRole(person2, readerRole, assign2));

		Assembly assembly1;
		Assembly assembly2;
		try (Transaction tx = beginTX()) {
			assembly1 = createAssembly("assembly1", createZAreaInstance(assign1.getArea()), plant1);
			assembly2 = createAssembly("assembly2", createZAreaInstance(assign2.getArea()), plant2);

			tx.commit();
		}

		assertTrue(hasRole(person1, readerRole, assembly1));
		assertFalse(hasRole(person1, readerRole, assembly2));
		assertTrue(hasRole(person2, readerRole, assign1));
		assertTrue(hasRole(person2, readerRole, assign2));

		try (Transaction tx = beginTX()) {
			// Change name such that condition in rule changes
			plant1.setName(plant2.getName());
			tx.commit();
		}

		assertTrue(hasRole(person1, readerRole, assembly1));
		assertTrue(hasRole(person1, readerRole, assembly2));
		assertTrue(hasRole(person2, readerRole, assign1));
		assertTrue(hasRole(person2, readerRole, assign2));
	}

	/**
	 * Tests {@link PathByExpression} where the expression is a simple navigation step.
	 */
	public void testSimpleChain() {
		PathByExpression path = newPathByExpression("assembly -> $assembly"
			+ ".get(`TestScriptPathElement:Assembly#area`)"
			+ ".get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".referers(`TestScriptPathElement:Assignment#area`)");
		Plant plant1;
		Plant plant2;
		Assignment assign1;
		Assignment assign2;
		Assembly assembly1;
		Assembly assembly2;
		Assembly assembly3;
		try (Transaction tx = beginTX()) {
			plant1 = createPlant("plant1");
			plant2 = createPlant("plant2");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");

			assign1 = createAssignment("assignment1", plant1, z1);
			assign2 = createAssignment("assignment2", plant2, z2);

			assembly1 = createAssembly("assembly1", createZAreaInstance(assign1.getArea()), plant1);
			assembly2 = createAssembly("assembly2", createZAreaInstance(assign2.getArea()), plant2);
			assembly3 = createAssembly("assembly3", createZAreaInstance(assign1.getArea()), plant2);
			
			assertPathBase(set(assembly1, assembly3),
				path, assign1, TestScriptPathElementFactory.getAreaAssignmentAttr());
			assertPathBase(set(assembly2),
				path, assign2, TestScriptPathElementFactory.getAreaAssignmentAttr());
			assertPathBase(set(assembly1),
				path, assembly1.getArea(), TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources(TLObject)}: inverse navigation through a simple
	 * chain without filters.
	 */
	public void testGetSources() {
		PathByExpression path = newPathByExpression("assembly -> $assembly"
			+ ".get(`TestScriptPathElement:Assembly#area`)"
			+ ".get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".referers(`TestScriptPathElement:Assignment#area`)");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");
			// Each assembly needs its own ZAreaInstance: the configured role rule uses
			// .singleElement() on Assembly#area referers and would fail otherwise.
			ZAreaInstance zai1a = createZAreaInstance(z1);
			ZAreaInstance zai1b = createZAreaInstance(z1);
			ZAreaInstance zai2 = createZAreaInstance(z2);
			Assignment assign1 = createAssignment("assign1", plant1, z1);
			Assignment assign2 = createAssignment("assign2", plant1, z2);
			Assembly assembly1 = createAssembly("assembly1", zai1a, plant1);
			Assembly assembly2 = createAssembly("assembly2", zai2, plant1);
			Assembly assembly3 = createAssembly("assembly3", zai1b, plant1);

			assertSources(set(assembly1, assembly3), path, assign1);
			assertSources(set(assembly2), path, assign2);

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getPathBase} for a filter predicate that accesses an attribute
	 * of the filter element directly ({@code AccessStep} in the predicate chain) and compares it to
	 * an outer expression ({@code OuterSideStep} in the predicate chain).
	 */
	public void testFilterWithPredicateChain() {
		PathByExpression path = newPathByExpression("assembly -> $assembly"
			+ ".get(`TestScriptPathElement:Assembly#area`)"
			+ ".get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".referers(`TestScriptPathElement:Assignment#area`)"
			+ ".filter(assign -> $assign.get(`TestScriptPathElement:Assignment#plant`)"
			+ " == $assembly.get(`TestScriptPathElement:Assembly#plant`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			Plant plant2 = createPlant("plant2");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			ZAreaInstance zai2 = createZAreaInstance(z2);
			Assignment assign1 = createAssignment("assign1", plant1, z1);
			Assignment assign2 = createAssignment("assign2", plant2, z2);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assembly assembly2 = createAssembly("assembly2", zai2, plant2);

			// ReferersStep: when Assignment#area changes -> assemblies with area.template = z1
			assertPathBase(set(assembly1), path, assign1,
				TestScriptPathElementFactory.getAreaAssignmentAttr());
			// AccessStep: when ZAreaInstance#template changes -> assembly1
			assertPathBase(set(assembly1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());
			// AccessStep in predicate chain: when Assignment#plant changes on assign1 -> assembly1
			assertPathBase(set(assembly1), path, assign1,
				TestScriptPathElementFactory.getPlantAssignmentAttr());
			// LetBindingStep at chain position 0: when Assembly#plant changes on assembly1
			// -> assembly1 is the outer lambda parameter, returned directly as base object
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} and {@link PathByExpression#getPathBase} for a
	 * union whose one branch is rooted in a singleton constant ({@code ROOT1}).
	 *
	 * <ul>
	 * <li>When a destination object is in the singleton's collection, every base assembly could
	 * contribute it via the constant branch, so {@link PathByExpression#getSources} must return
	 * {@link BaseObjects#all()}.</li>
	 * <li>When the destination is not in the singleton's collection, only assemblies whose plant
	 * equals the destination contribute via the parameter branch.</li>
	 * <li>{@link PathByExpression#getPathBase} for {@code Plants#plants} on {@code ROOT1} must
	 * return {@link BaseObjects#all()}: ROOT1 is the anchor for that step in the constant chain.
	 * The same change on a different {@code Plants} object must yield an empty set.</li>
	 * </ul>
	 */
	public void testUnionWithSingleton() {
		PathByExpression path = newPathByExpression("assembly -> "
			+ "$assembly.get(`TestScriptPathElement:Assembly#plant`)"
			+ ".union(`TestScriptPathElement#ROOT1`.get(`TestScriptPathElement:Plants#plants`))");
		try (Transaction tx = beginTX()) {
			Plant plantInRoot1 = addPlant(_root1, "plantInRoot1");
			Plant plantNotInRoot1 = createPlant("plantNotInRoot1");
			Assembly assemblyA = createAssembly("assemblyA", createZAreaInstance(createZArea("z1")), plantNotInRoot1);
			Assembly assemblyB = createAssembly("assemblyB", createZAreaInstance(createZArea("z2")), plantInRoot1);

			// plantInRoot1 is reachable from any assembly via the constant branch -> all()
			assertTrue(path.getSources(plantInRoot1).isAll());
			// plantNotInRoot1 is only reachable from assemblyA via the parameter branch
			assertSources(set(assemblyA), path, plantNotInRoot1);

			// Plants#plants on ROOT1 is the anchor of the constant chain -> all()
			assertPathBaseIsAll(path, _root1, TestScriptPathElementFactory.getPlantsPlantsAttr());
			// Plants#plants on ROOT2 is not the anchor of ROOT1's chain -> empty (irrelevant)
			assertEquals(set(), getPathBase(path, _root2, TestScriptPathElementFactory.getPlantsPlantsAttr()));
			// Assembly#plant on assemblyA: parameter-branch AccessStep -> assemblyA itself
			assertPathBase(set(assemblyA), path, assemblyA,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} and {@link PathByExpression#getPathBase} for a
	 * union whose two extra branches are each rooted in a different singleton ({@code ROOT1} and
	 * {@code ROOT2}).
	 *
	 * <p>
	 * Both singletons are tracked independently: a change to {@code Plants#plants} on either one
	 * triggers a full recompute ({@link BaseObjects#all()}), while the same change on an unrelated
	 * {@code Plants} object is correctly identified as irrelevant (empty set).
	 * </p>
	 */
	public void testUnionTwoSingletons() {
		PathByExpression path = newPathByExpression("assembly -> "
			+ "$assembly.get(`TestScriptPathElement:Assembly#plant`)"
			+ ".union(`TestScriptPathElement#ROOT1`.get(`TestScriptPathElement:Plants#plants`))"
			+ ".union(`TestScriptPathElement#ROOT2`.get(`TestScriptPathElement:Plants#plants`))");
		try (Transaction tx = beginTX()) {
			Plant plantInRoot1 = addPlant(_root1, "plantInRoot1");
			Plant plantInRoot2 = addPlant(_root2, "plantInRoot2");
			Plant plantInNeither = createPlant("plantInNeither");
			Assembly assemblyA = createAssembly("assemblyA", createZAreaInstance(createZArea("z1")), plantInNeither);

			// Both constant branches -> all() for plants in either singleton
			assertTrue(path.getSources(plantInRoot1).isAll());
			assertTrue(path.getSources(plantInRoot2).isAll());
			// plantInNeither only reachable via parameter branch -> {assemblyA}
			assertSources(set(assemblyA), path, plantInNeither);

			// ROOT1 is the anchor for the ROOT1 constant branch -> all()
			assertPathBaseIsAll(path, _root1, TestScriptPathElementFactory.getPlantsPlantsAttr());
			// ROOT2 is the anchor for the ROOT2 constant branch -> all()
			assertPathBaseIsAll(path, _root2, TestScriptPathElementFactory.getPlantsPlantsAttr());
			// Assembly#plant on assemblyA: only affects the parameter branch -> {assemblyA}
			assertPathBase(set(assemblyA), path, assemblyA,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getPathBase} for a let-binding expression. When an attribute
	 * referenced in the binding expression changes, {@code getPathBase} must still compute the
	 * correct base objects via the {@code LetBindingStep}.
	 */
	public void testLetBinding() {
		PathByExpression path = newPathByExpression("a -> {"
			+ " zai = $a.get(`TestScriptPathElement:Assembly#area`);"
			+ " $zai.get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".referers(`TestScriptPathElement:Assignment#area`)"
			+ "}");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assignment assign1 = createAssignment("assign1", plant1, z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);

			// LetBindingStep: Assembly#area is in the binding expression -> tracked separately
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getAreaAssemblyAttr());
			// ZAreaInstance#template: in the body, reached via the substituted variable
			assertPathBase(set(assembly1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());
			// Assignment#area: at the end of the chain
			assertPathBase(set(assembly1), path, assign1,
				TestScriptPathElementFactory.getAreaAssignmentAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} and {@link PathByExpression#getPathBase} for a
	 * {@code map()} expression. The {@code ForeachStep} must support both full inverse navigation
	 * ({@code getSources}) and pivot computation ({@code getPathBase}).
	 */
	public void testForeach() {
		PathByExpression path = newPathByExpression("area -> $area"
			+ ".referers(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".map(zai -> $zai.referers(`TestScriptPathElement:Assembly#area`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			ZAreaInstance zai2 = createZAreaInstance(z2);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assembly assembly2 = createAssembly("assembly2", zai2, plant1);

			// getSources: navigate backwards assembly -> zai -> area
			assertSources(set(z1), path, assembly1);
			assertSources(set(z2), path, assembly2);

			// ReferersStep in ForeachStep inner chain: Assembly#area changes on assembly1 -> z1
			assertPathBase(set(z1), path, assembly1,
				TestScriptPathElementFactory.getAreaAssemblyAttr());
			// ReferersStep in outer chain: ZAreaInstance#template changes on zai1 -> z1
			assertPathBase(set(z1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} and {@link PathByExpression#getPathBase} for a
	 * {@code union} expression. Both branches always contribute to inverse navigation.
	 */
	public void testUnion() {
		PathByExpression path = newPathByExpression("assembly -> $assembly"
			+ ".get(`TestScriptPathElement:Assembly#area`)"
			+ ".get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ ".referers(`TestScriptPathElement:Assignment#area`)"
			+ ".union($assembly"
			+ ".get(`TestScriptPathElement:Assembly#plant`)"
			+ ".referers(`TestScriptPathElement:Assignment#plant`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			Plant plant2 = createPlant("plant2");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assignment assign1 = createAssignment("assign1", plant1, z1);  // both branches
			Assignment assign2 = createAssignment("assign2", plant2, z1);  // area branch only
			Assignment assign3 = createAssignment("assign3", plant1, z2);  // plant branch only

			assertSources(set(assembly1), path, assign1);
			assertSources(set(assembly1), path, assign2);
			assertSources(set(assembly1), path, assign3);

			assertPathBase(set(assembly1), path, assign1,
				TestScriptPathElementFactory.getAreaAssignmentAttr());
			assertPathBase(set(assembly1), path, assign1,
				TestScriptPathElementFactory.getPlantAssignmentAttr());
			assertPathBase(set(assembly1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getAreaAssemblyAttr());
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} and {@link PathByExpression#getPathBase} for an
	 * {@code if/else} expression whose condition reads a model attribute.
	 *
	 * <p>
	 * {@link PathByExpression#getSources} is an over-approximation: results are collected from both
	 * branches regardless of which branch the condition selects at runtime. When a condition
	 * attribute changes, the condition expression is navigated backwards to identify precisely which
	 * base objects may have switched branches.
	 * </p>
	 */
	public void testIfElse() {
		// condition uses Assembly#area -> condParts contains that attribute.
		PathByExpression path = newPathByExpression("assembly ->"
			+ " $assembly.get(`TestScriptPathElement:Assembly#area`) != null"
			+ " ? $assembly.get(`TestScriptPathElement:Assembly#area`)"
			+ "   .get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ "   .referers(`TestScriptPathElement:Assignment#area`)"
			+ " : $assembly.get(`TestScriptPathElement:Assembly#plant`)"
			+ "   .referers(`TestScriptPathElement:Assignment#plant`)");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			Plant plant2 = createPlant("plant2");
			ZArea z1 = createZArea("z1");
			ZArea z2 = createZArea("z2");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assignment assignIf = createAssignment("assignIf", plant2, z1);     // matches if-branch
			Assignment assignElse = createAssignment("assignElse", plant1, z2); // matches else-branch

			// Both lead back to assembly1; assignElse is an over-approximation.
			assertSources(set(assembly1), path, assignIf);
			assertSources(set(assembly1), path, assignElse);

			// Condition part: backward navigation through the condition chain identifies assembly1
			// as the only affected base object (Assembly#area is read directly from the base).
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getAreaAssemblyAttr());

			// Parts in the if-branch.
			assertPathBase(set(assembly1), path, assignIf,
				TestScriptPathElementFactory.getAreaAssignmentAttr());
			assertPathBase(set(assembly1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());

			// Parts in the else-branch: over-approximation since the condition is not evaluated
			// at runtime (it may reference variables not available during backward navigation).
			assertPathBase(set(assembly1), path, assignElse,
				TestScriptPathElementFactory.getPlantAssignmentAttr());
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests an {@code if/else} expression whose else-branch is {@code all(...).filter(...)} with a
	 * filter predicate that references the outer lambda parameter.
	 *
	 * <p>
	 * The else-branch is represented by an {@code AllStep} and is fully analyzable:
	 * </p>
	 * <ul>
	 * <li>{@link PathByExpression#getSources} still returns {@link BaseObjects#all()} because
	 * {@code AllStep.applyInverse} returns {@code null} for destinations compatible with the
	 * enumerated type ({@code Assignment}).</li>
	 * <li>For {@code Assembly#plant} (the outer-variable attribute captured in
	 * {@code outerLetBindings}), {@link PathByExpression#getPathBase} returns the precise base
	 * object rather than {@link BaseObjects#all()}.</li>
	 * <li>For {@code Assignment#plant} (an attribute of the enumerated instances inside the
	 * filter), backward navigation yields a non-empty set of affected instances but not the
	 * corresponding base objects, so {@link BaseObjects#all()} is returned conservatively.</li>
	 * </ul>
	 */
	public void testIfElseWithAllStepElseBranch() {
		PathByExpression path = newPathByExpression("assembly ->"
			+ " $assembly.get(`TestScriptPathElement:Assembly#area`) != null"
			+ " ? $assembly.get(`TestScriptPathElement:Assembly#area`)"
			+ "   .get(`TestScriptPathElement:ZAreaInstance#template`)"
			+ "   .referers(`TestScriptPathElement:Assignment#area`)"
			+ " : all(`TestScriptPathElement:Assignment`)"
			+ "   .filter(x -> $x.get(`TestScriptPathElement:Assignment#plant`)"
			+ "               == $assembly.get(`TestScriptPathElement:Assembly#plant`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assignment assignIf = createAssignment("assignIf", plant1, z1);

			// getSources falls back because AllStep.applyInverse returns null for Assignment
			// destinations (compatible with the enumerated type).
			assertTrue(path.getSources(assignIf).isAll());

			// Condition part: backward navigation through condChain gives the exact base object.
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getAreaAssemblyAttr());

			// Parts only in the if-branch: precise backward navigation.
			assertPathBase(set(assembly1), path, assignIf,
				TestScriptPathElementFactory.getAreaAssignmentAttr());
			assertPathBase(set(assembly1), path, zai1,
				TestScriptPathElementFactory.getTemplateZAreaInstanceAttr());

			// Assembly#plant is the outer-variable attribute (captured in outerLetBindings):
			// AllStep computes the precise pivot -> {assembly1}.
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			// Assignment#plant is an attribute of enumerated instances inside the filter (steps).
			// AllStep knows that assignIf is an affected instance but cannot determine which
			// base assemblies correspond -> conservative BaseObjects.all().
			assertPathBaseIsAll(path, assignIf, TestScriptPathElementFactory.getPlantAssignmentAttr());

			tx.commit();
		}
	}

	/**
	 * Tests a script-step expression without a lambda argument. Such an expression ignores the base
	 * object and always returns the same set. {@link PathByExpression#getSources} and
	 * {@link PathByExpression#getPathBase} must always return {@link BaseObjects#all()} because
	 * there is no base-object dependency to invert.
	 */
	public void testNoArgument() {
		// Expression has no lambda: returns all assignments whose plant is non-null,
		// ignoring the base object entirely.
		PathByExpression path = newPathByExpression(
			"all(`TestScriptPathElement:Assignment`)"
				+ ".filter(a -> $a.get(`TestScriptPathElement:Assignment#plant`) != null)");
		Plant plant1;
		ZArea z1;
		ZAreaInstance zai1;
		Assembly assembly1;
		Assignment assign1;
		Assignment assign2;
		try (Transaction tx = beginTX()) {
			plant1 = createPlant("plant1");
			z1 = createZArea("z1");
			zai1 = createZAreaInstance(z1);
			assembly1 = createAssembly("assembly1", zai1, plant1);
			assign1 = createAssignment("assign1", plant1, z1);
			assign2 = createAssignment("assign2", plant1, z1);
			tx.commit();
		}
		// all() only sees committed objects, so we query after the commit above.
		try (Transaction tx = beginTX()) {
			// getValues ignores the base -- both assignments match the filter.
			Collection<? extends TLObject> values = path.getValues(assembly1);
			assertTrue("getValues must include assign1", values.contains(assign1));
			assertTrue("getValues must include assign2", values.contains(assign2));
			// Result is the same regardless of the base argument.
			assertEquals(toSet(values), toSet(path.getValues(assign1)));

			// getSources has no lambda to invert -- must fall back to BaseObjects.all().
			assertTrue(path.getSources(assign1).isAll());

			// getPathBase has no base dependency -- must fall back to BaseObjects.all().
			assertPathBaseIsAll(path, assign1, TestScriptPathElementFactory.getAreaAssignmentAttr());

			tx.rollback();
		}
	}

	/**
	 * Tests that {@link PathByExpression#getPathBase} handles attribute overrides correctly.
	 *
	 * <p>
	 * When an expression uses {@code Named:name} (the base definition) and the access manager
	 * calls {@code getPathBase} with {@code Plants:name} (an override of {@code Named:name}), the
	 * {@code usesPart} check must still match via
	 * {@link TLStructuredTypePart#getDefinition()} comparison.
	 * </p>
	 *
	 * <p>
	 * In the test model, {@code Plants} overrides {@code Named:name} with {@code Plants:name}
	 * ({@code override="true"}). Without the fix, {@code Named:name.equals(Plants:name)} is
	 * {@code false}, so no step would match and {@code getPathBase} would fall back to
	 * {@link BaseObjects#all()}. With the fix, both resolve to the same definition and the
	 * correct pivot is returned.
	 * </p>
	 */
	public void testPartOverride() {
		// Expression reads Named:name from the input -- creates AccessStep(Named:name) in chain.
		PathByExpression path = newPathByExpression(
			"x -> $x.get(`TestScriptPathElement:Named#name`)");
		try (Transaction tx = beginTX()) {
			Plants plantsObj = factory().createPlants();

			// Plants:name is an override of Named:name (override="true" in TestScriptPathElement.model.xml).
			// getPathBase is called with Plants:name (the concrete override), but the expression
			// chain stores Named:name (the base definition). matchesPart must return true so that
			// plantsObj is identified as the base object rather than falling back to BaseObjects.all().
			assertPathBase(set(plantsObj), path, plantsObj,
				TestScriptPathElementFactory.getNamePlantsAttr());

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getSources} for an expression rooted in
	 * {@code all(module:Type)}.
	 *
	 * <p>
	 * An object of the enumerated type can in principle be produced by the expression for any base
	 * object, so {@link PathByExpression#getSources} must return {@link BaseObjects#all()}. An
	 * object of a different type can never appear in the result, so the method must return an empty
	 * set.
	 * </p>
	 */
	public void testAllStepGetSources() {
		// Produces all Assignments whose plant equals the base Assembly's plant.
		PathByExpression path = newPathByExpression(
			"base -> all(`TestScriptPathElement:Assignment`)"
				+ ".filter(a -> $a.get(`TestScriptPathElement:Assignment#plant`)"
				+ " == $base.get(`TestScriptPathElement:Assembly#plant`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			Assignment assign1 = createAssignment("assign1", plant1, z1);

			// assign1 is an Assignment (the enumerated type): any base assembly could produce it
			// via the filter, so getSources must return BaseObjects.all().
			assertTrue(path.getSources(assign1).isAll());

			// plant1 is a Plant, not an Assignment; it can never appear in the result of
			// all(Assignment).filter(...), so getSources must return an empty set.
			assertSources(set(), path, plant1);

			tx.commit();
		}
	}

	/**
	 * Tests {@link PathByExpression#getPathBase} for an expression rooted in
	 * {@code all(module:Type)}.
	 *
	 * <p>
	 * Two cases are distinguished:
	 * </p>
	 * <ul>
	 * <li><b>Base-object side</b> ({@code outerLetBindings}): when an attribute of the outer
	 * lambda parameter changes, backward navigation through the captured outer-variable chain
	 * yields the precise base object.</li>
	 * <li><b>Enumerated-instance side</b> ({@code steps}): when an attribute of an enumerated
	 * instance changes, the corresponding base objects cannot be determined without evaluating the
	 * full expression, so {@link BaseObjects#all()} is returned conservatively.</li>
	 * </ul>
	 */
	public void testAllStepGetPathBase() {
		// Produces all Assignments whose plant equals the base Assembly's plant.
		PathByExpression path = newPathByExpression(
			"base -> all(`TestScriptPathElement:Assignment`)"
				+ ".filter(a -> $a.get(`TestScriptPathElement:Assignment#plant`)"
				+ " == $base.get(`TestScriptPathElement:Assembly#plant`))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);
			Assignment assign1 = createAssignment("assign1", plant1, z1);

			// Base-object side: Assembly#plant is referenced via the outer variable (base).
			// outerLetBindings captures AccessStep(Assembly#plant) -> pivot = {assembly1}.
			assertPathBase(set(assembly1), path, assembly1,
				TestScriptPathElementFactory.getPlantAssemblyAttr());

			// Enumerated-instance side: Assignment#plant is inside the all()-steps.
			// assign1 is an affected enumerated Assignment, but the corresponding base
			// assemblies cannot be determined -> BaseObjects.all().
			assertPathBaseIsAll(path, assign1, TestScriptPathElementFactory.getPlantAssignmentAttr());

			tx.commit();
		}
	}

	/**
	 * Tests the empty-set optimisation in {@link PathByExpression#getPathBase} for the
	 * enumerated-instance side of an {@code all(module:Type)} step.
	 *
	 * <p>
	 * The filter predicate uses a {@code referers} step: when the changed attribute value is
	 * {@code null} (i.e. a reference is being removed), backward navigation through the
	 * {@code ReferersStep} yields an empty set of enumerated instances. Because no enumerated
	 * instance is affected, no base object needs re-evaluation and an empty set is returned
	 * instead of {@link BaseObjects#all()}.
	 * </p>
	 *
	 * <p>
	 * When the new attribute value is non-{@code null} (a reference is being set), backward
	 * navigation yields a non-empty set, and {@link BaseObjects#all()} is returned conservatively.
	 * </p>
	 */
	public void testAllStepGetPathBaseEnumeratedSideEmpty() {
		// Produces all ZAreaInstances that are pointed to by the base Assembly via Assembly#area.
		// The filter reads: "zai whose referers via Assembly#area include the base object".
		PathByExpression path = newPathByExpression(
			"base -> all(`TestScriptPathElement:ZAreaInstance`)"
				+ ".filter(zai -> $zai"
				+ ".referers(`TestScriptPathElement:Assembly#area`)"
				+ ".containsElement($base))");
		try (Transaction tx = beginTX()) {
			Plant plant1 = createPlant("plant1");
			ZArea z1 = createZArea("z1");
			ZAreaInstance zai1 = createZAreaInstance(z1);
			Assembly assembly1 = createAssembly("assembly1", zai1, plant1);

			// Assembly#area is the enumerated-instance side (inside the filter via ReferersStep).
			// partValue = null simulates removing the reference: ReferersStep.pivot returns empty
			// -> no ZAreaInstance is affected -> optimisation fires -> empty result.
			BaseObjects<? extends Collection<?>> baseForNull =
				path.getPathBase(assembly1, TestScriptPathElementFactory.getAreaAssemblyAttr(), () -> null);
			assertFalse("Expected a concrete set, not BaseObjects.all(), when partValue is null",
				baseForNull.isAll());
			assertEquals(set(), toSet(baseForNull.get()));

			// partValue = zai1 (non-null): ReferersStep.pivot returns {zai1} -> non-empty
			// -> some ZAreaInstance is affected -> conservative BaseObjects.all().
			assertPathBaseIsAll(path, assembly1, TestScriptPathElementFactory.getAreaAssemblyAttr());

			tx.commit();
		}
	}

	/**
	 * Tests that a script-step expression navigating through a derived (computed) attribute is
	 * rejected as a configuration error at construction time.
	 */
	public void testDerivedAttributeIsConfigurationError() {
		try {
			newPathByExpression(
				"assembly -> $assembly.get(`TestScriptPathElement:Assembly#derivedAssignments`)");
			fail("Expected ConfigurationError for derived attribute in script-step expression.");
		} catch (ConfigurationError ex) {
			/* The ConfigurationError from context.error() is wrapped by the reflective instantiation
			 * framework (DefaultConfigConstructorScheme), so the outer getMessage() does not carry
			 * the original text. Walk the cause chain to verify our error was reported. */
			Throwable cause = ex;
			boolean foundDerived = false;
			while (cause != null) {
				String msg = cause.getMessage();
				if (msg != null && msg.contains("derived")) {
					foundDerived = true;
					break;
				}
				cause = cause.getCause();
			}
			assertTrue("Error message chain should mention 'derived'", foundDerived);
		}
	}

	private void assertSources(Set<? extends TLObject> expected, PathByExpression path, TLObject destination) {
		BaseObjects<? extends Collection<? extends TLObject>> sources = path.getSources(destination);
		assertFalse(sources.isAll());
		assertEquals(expected, toSet(sources.get()));
	}

	private void assertPathBase(Set<? extends TLObject> expected, PathByExpression path, TLObject element,
			TLStructuredTypePart part) {
		Collection<? extends TLObject> base = getPathBase(path, element, part);
		assertEquals(expected, base);
	}

	private void assertPathBaseIsAll(PathByExpression path, TLObject element, TLStructuredTypePart part) {
		BaseObjects<? extends Collection<? extends TLObject>> base =
			path.getPathBase(element, part, () -> element.tValue(part));
		assertTrue("Expected BaseObjects.all() for part " + part.getName() + " but got a concrete set",
			base.isAll());
	}

	private Collection<? extends TLObject> getPathBase(PathByExpression path, TLObject element,
			TLStructuredTypePart part) {
		BaseObjects<? extends Collection<? extends TLObject>> base =
			path.getPathBase(element, part, () -> element.tValue(part));
		assertFalse(base.isAll());
		return toSet(base.get());
	}

	private boolean hasRole(Person person, BoundRole role, TLObject object) {
		return filterAllowed(person, role, list(object)).contains(object);
	}

	private <T extends TLObject> Set<T> filterAllowed(Person person, BoundRole role, Collection<T> objects) {
		return filterAllowed(person, list(role), objects);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends TLObject> Set<T> filterAllowed(Person person, Collection<? extends BoundRole> roles,
			Collection<T> objects) {
		return toSet(AccessManager.getInstance().getAllowedBusinessObjects(person, roles, (Collection) objects));
	}

	private Plant createPlant(String name) {
		Plant plant = factory().createPlant();
		plant.setName(name);
		return plant;
	}

	private Plant addPlant(Plants plants, String name) {
		Plant newPlant = createPlant(name);
		plants.addPlant(newPlant);
		return newPlant;
	}

	private ZArea createZArea(String name) {
		ZArea area = factory().createZArea();
		area.setName(name);
		return area;
	}

	private Assembly createAssembly(String name, ZAreaInstance area, Plant plant) {
		Assembly assembly = factory().createAssembly();
		assembly.setName(name);
		assembly.setArea(area);
		assembly.setPlant(plant);
		return assembly;
	}

	private ZAreaInstance createZAreaInstance(ZArea template) {
		return createZAreaInstance(template.getName(), template);
	}

	private ZAreaInstance createZAreaInstance(String name, ZArea template) {
		ZAreaInstance zAreaInstance = factory().createZAreaInstance();
		zAreaInstance.setName(name);
		zAreaInstance.setTemplate(template);
		return zAreaInstance;
	}

	private Assignment createAssignment(String name, Plant plant, ZArea area) {
		Assignment assignment = factory().createAssignment();
		assignment.setName(name);
		assignment.setArea(area);
		assignment.setPlant(plant);
		return assignment;
	}

	private PathByExpression newPathByExpression(String exprString) {
		try {
			Expr expr = ExprFormat.INSTANCE.getValue(PathByExpression.Config.EXPRESSION, exprString);
			PathByExpression.Config conf =
				(Config) TypedConfiguration.createConfigItemForImplementationClass(PathByExpression.class);
			conf.setExpression(expr);
			return TypedConfigUtil.createInstance(conf);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Creates a cumulated test.
	 */
	public static Test suite() {
		return KBSetup.getKBTest(TestScriptPathElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				AccessManager.Module.INSTANCE,
				TLSecurityDeviceManager.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}

}


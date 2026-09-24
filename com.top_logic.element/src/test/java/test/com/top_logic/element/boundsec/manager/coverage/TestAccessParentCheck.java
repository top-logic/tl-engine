/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.model.ModelService;

/**
 * Test for the access check delegating to the access parent of an object, against the model module
 * {@code TestSecurityCoverage} of the element test application.
 * <p>
 * The role rule of the module gives the owner of a {@code Covered} object the reader role on it,
 * which is granted read and write. The objects of the other types have no roles: a composition part
 * follows its container by default, a type with a configured access parent follows the reference
 * named, and two objects whose access parents point at each other are denied instead of recursing.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAccessParentCheck extends BasicTestCase {

	private static final String MODULE = "TestSecurityCoverage";

	private static final String COVERED = MODULE + ":Covered";

	private static final String PART_OF_COVERED = MODULE + ":PartOfCovered";

	private static final String SUB_PART = MODULE + ":SubPart";

	private static final String EXPLICIT_PART = MODULE + ":ExplicitPart";

	private static final String LINKED = MODULE + ":Linked";

	private static final String CYCLIC = MODULE + ":Cyclic";

	private static final String PARTS = "parts";

	private static final String SUBS = "subs";

	private static final String EXPLICIT_PARTS = "explicitParts";

	private static final String OWNER = "owner";

	private static final String TARGET = "target";

	private static final String NEXT = "next";

	private static final String NAME = "name";

	private Person _owner;

	private Person _other;

	private TLObject _covered;

	private TLObject _part;

	private TLObject _subPart;

	private TLObject _freePart;

	private TLObject _explicitPart;

	private TLObject _linked;

	private TLObject _cyclicA;

	private TLObject _cyclicB;

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		inTransaction(() -> {
			_owner = createPerson("accessParentOwner");
			_other = createPerson("accessParentOther");

			_covered = create(COVERED);
			_covered.tUpdateByName(OWNER, _owner);

			_part = create(PART_OF_COVERED);
			_part.tUpdateByName(NAME, "part");
			_covered.tUpdateByName(PARTS, List.of(_part));

			_subPart = create(SUB_PART);
			_part.tUpdateByName(SUBS, List.of(_subPart));

			_freePart = create(PART_OF_COVERED);

			_explicitPart = create(EXPLICIT_PART);
			_explicitPart.tUpdateByName(OWNER, _other);
			_covered.tUpdateByName(EXPLICIT_PARTS, List.of(_explicitPart));

			_linked = create(LINKED);
			_linked.tUpdateByName(TARGET, _covered);

			_cyclicA = create(CYCLIC);
			_cyclicB = create(CYCLIC);
			_cyclicA.tUpdateByName(NEXT, _cyclicB);
			_cyclicB.tUpdateByName(NEXT, _cyclicA);
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			_owner.tDelete();
			_other.tDelete();
		});
		_created.clear();

		super.tearDown();
	}

	public void testObjectWithOwnDefinition() {
		assertTrue(allowed(_owner, _covered, SimpleBoundCommandGroup.READ));
		assertTrue(allowed(_owner, _covered, SimpleBoundCommandGroup.WRITE));
		assertFalse(allowed(_other, _covered, SimpleBoundCommandGroup.READ));
	}

	public void testCompositionPartFollowsContainer() {
		assertTrue("The part has no roles, its container decides.", allowed(_owner, _part, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _part, SimpleBoundCommandGroup.READ));
	}

	public void testChainOfContainers() {
		assertTrue("The decision is delegated along the chain of containers.",
			allowed(_owner, _subPart, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _subPart, SimpleBoundCommandGroup.READ));
	}

	public void testCreateAndDeleteArePartWrite() {
		assertTrue("Deleting a part is writing its container, which the owner may.",
			allowed(_owner, _subPart, SimpleBoundCommandGroup.DELETE));
		assertFalse(allowed(_other, _subPart, SimpleBoundCommandGroup.DELETE));

		TLClass partType = type(PART_OF_COVERED);
		assertTrue("Creating a part in a container is writing the container.",
			rights().isAllowedCreate(_owner, partType, _covered));
		assertFalse(rights().isAllowedCreate(_other, partType, _covered));
		assertFalse("A delegating type cannot be created without a context deciding for it.",
			rights().isAllowedCreate(_owner, partType, (TLObject) null));
	}

	public void testPartWithoutContainerIsDenied() {
		assertFalse("An object whose access parent relation leads nowhere is not accessible.",
			allowed(_owner, _freePart, SimpleBoundCommandGroup.READ));
	}

	public void testConfiguredAccessParentShadowsRoleRule() {
		assertTrue("The container decides, the shadowed role rule granting the other person is ignored.",
			allowed(_owner, _explicitPart, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _explicitPart, SimpleBoundCommandGroup.READ));
	}

	public void testAccessParentThroughReference() {
		assertTrue("The object the reference points to decides.", allowed(_owner, _linked, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_other, _linked, SimpleBoundCommandGroup.READ));
	}

	public void testAttributeOfDelegatingObject() {
		TLStructuredTypePart name = type(PART_OF_COVERED).getPart(NAME);
		assertTrue(rights().isAllowed(_owner, _part, name, SimpleBoundCommandGroup.READ));
		assertFalse(rights().isAllowed(_other, _part, name, SimpleBoundCommandGroup.READ));
	}

	public void testCycleIsDenied() {
		assertFalse("Access parents pointing at each other decide nothing: access is denied.",
			allowed(_owner, _cyclicA, SimpleBoundCommandGroup.READ));
		assertFalse(allowed(_owner, _cyclicB, SimpleBoundCommandGroup.READ));
	}

	public void testAccessibleTypes() {
		// The type level query checks the roles a person holds on the security root, which the
		// owner does not hold: a delegating type follows the type it delegates to either way.
		Set<TLClass> accessible = rights().getAccessibleTypes(_owner, SimpleBoundCommandGroup.READ);
		assertEquals("A delegating type is accessible exactly where the type it delegates to is.",
			accessible.contains(type(COVERED)), accessible.contains(type(SUB_PART)));
		assertEquals(accessible.contains(type(COVERED)), accessible.contains(type(LINKED)));
		assertFalse("A type whose access parents form a cycle is never accessible.",
			accessible.contains(type(CYCLIC)));

		Set<TLClass> all = rights().getAccessibleTypes(PersonManager.getManager().getRoot(), SimpleBoundCommandGroup.READ);
		assertTrue("The technical administrator may access every type.", all.contains(type(SUB_PART)));
	}

	private static boolean allowed(Person person, TLObject object, BoundCommandGroup operation) {
		return rights().isAllowed(person, object, operation);
	}

	private static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	private TLObject create(String qualifiedTypeName) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(qualifiedTypeName));
		_created.add(result);
		return result;
	}

	private static Person createPerson(String name) {
		return Person.create(kb(), name, TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.pushSuperUser();
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			modification.run();
			tx.commit();
		} finally {
			ThreadContext.popSuperUser();
		}
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestAccessParentCheck.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}

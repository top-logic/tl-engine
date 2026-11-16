/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import static test.com.top_logic.model.search.expr.TestOperationDeclarations.assertError;
import static test.com.top_logic.model.search.expr.TestOperationDeclarations.assertNoErrors;
import static test.com.top_logic.model.search.expr.TestOperationDeclarations.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;
import test.com.top_logic.model.search.expr.TestOperationDeclarations.ConfigBuilder;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.services.InitialRolesManager;
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
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Test for attribute rights configured per type and attribute name.
 *
 * <p>
 * The test model (see <code>TestTLScriptSecurity.model.xml</code>) declares the interface
 * {@code Named} with the attribute {@code name}, its specialization {@code Foo} overriding
 * {@code name}, and {@code Bar} with its specializations {@code Sub} (and {@code SubSub}) and
 * {@code BarSibling} inheriting {@code name} unchanged. The role {@code NameReader} may read every
 * {@code Named} object by its class rights. The attribute rights of {@code Foo#name} and
 * {@code Sub#name} (see <code>TestTLScriptSecurity-test.config.xml</code>) grant reading the name
 * to the role {@code NameKeeper} instead. They apply to the named type and its specializations
 * only, not to the other types sharing the attribute.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPartRightsPerType extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	/** Role reading every {@code Named} object by the class rights. */
	private static final String NAME_READER = MODULE + ".NameReader";

	/** Role granted reading the name of a {@code Foo} and a {@code Sub} by attribute rights. */
	private static final String NAME_KEEPER = MODULE + ".NameKeeper";

	private static final String NAME = "name";

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	/** Holds {@link #NAME_READER} on all test objects. */
	private Person _reader;

	/** Holds {@link #NAME_KEEPER} on all test objects. */
	private Person _keeper;

	private TLObject _foo;

	private TLObject _bar;

	private TLObject _sub;

	private TLObject _subSub;

	private TLObject _barSibling;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_reader = TestPerson.createPerson("partRightsReader");
			_keeper = TestPerson.createPerson("partRightsKeeper");
			_foo = newNamed("Foo");
			_bar = newNamed("Bar");
			_sub = newNamed("Sub");
			_subSub = newNamed("SubSub");
			_barSibling = newNamed("BarSibling");
			tx.commit();
		}
	}

	private TLObject newNamed(String className) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(className));
		result.tUpdateByName(NAME, className);
		result.tUpdateByName("accountA", _reader);
		result.tUpdateByName("accountB", _keeper);
		return result;
	}

	@Override
	protected void tearDown() throws Exception {
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = beginTx()) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	@Override
	protected boolean checksSecurity() {
		return true;
	}

	/**
	 * The attribute rights of an overriding attribute apply to the overriding type, not to the
	 * other types sharing the attribute definition.
	 */
	public void testOverridingAttribute() {
		TLStructuredTypePart named = part("Named");
		TLStructuredTypePart fooName = part("Foo");
		assertNotSame("Foo overrides the attribute.", named, fooName);

		becomeUser(_reader);
		assertTrue("Bar has no attribute rights, the class rights decide.", readable(_reader, _bar, named));
		assertTrue(readable(_reader, _bar, part("Bar")));
		assertFalse("The attribute rights of Foo replace its class rights.", readable(_reader, _foo, fooName));
		assertFalse("The type of the object decides, not the given attribute.", readable(_reader, _foo, named));

		becomeUser(_keeper);
		assertTrue(readable(_keeper, _foo, fooName));
		assertTrue(readable(_keeper, _foo, named));
		assertFalse("The attribute rights of Foo do not apply to Bar.", readable(_keeper, _bar, named));
	}

	/**
	 * The attribute rights configured for a type inheriting the attribute apply to that type and its
	 * specializations, but not to its generalization and its sibling.
	 */
	public void testInheritedAttribute() {
		TLStructuredTypePart named = part("Named");
		assertSame("Sub inherits the attribute unchanged.", named, part("Sub"));

		becomeUser(_reader);
		assertFalse(readable(_reader, _sub, named));
		assertFalse(readable(_reader, _subSub, named));
		assertTrue(readable(_reader, _bar, named));
		assertTrue(readable(_reader, _barSibling, named));

		becomeUser(_keeper);
		assertTrue(readable(_keeper, _sub, named));
		assertTrue(readable(_keeper, _subSub, named));
		assertFalse("Not for the generalization.", readable(_keeper, _bar, named));
		assertFalse("Not for a sibling.", readable(_keeper, _barSibling, named));
	}

	/**
	 * The roles of the attribute rights of a type imply {@link SimpleBoundCommandGroup#DISCOVER} on
	 * that type and its specializations only.
	 */
	public void testDiscoverRoles() {
		BoundedRole keeper = role(NAME_KEEPER);
		assertTrue(discoverRoles("Foo").contains(keeper));
		assertTrue(discoverRoles("Sub").contains(keeper));
		assertTrue(discoverRoles("SubSub").contains(keeper));
		assertFalse(discoverRoles("Bar").contains(keeper));
		assertFalse(discoverRoles("BarSibling").contains(keeper));
		assertTrue(discoverRoles("Bar").contains(role(NAME_READER)));
	}

	/**
	 * The queries without object refer to the owner of the given attribute, only
	 * {@link ModelAccessRights#hasGrant(TLStructuredTypePart, com.top_logic.tool.boundsec.BoundCommandGroup)}
	 * also considers its specializations.
	 */
	public void testQueriesWithoutObject() {
		ModelAccessRights rights = ModelAccessRights.getInstance();
		TLStructuredTypePart named = part("Named");
		TLStructuredTypePart fooName = part("Foo");
		assertEquals(Collections.singleton(role(NAME_KEEPER)),
			rights.getAllowedRoles(fooName, SimpleBoundCommandGroup.READ));
		assertTrue(rights.getAllowedRoles(named, SimpleBoundCommandGroup.READ).isEmpty());
		assertTrue("A specialization has attribute rights.", rights.hasGrant(named, SimpleBoundCommandGroup.READ));
		assertTrue(rights.hasGrant(fooName, SimpleBoundCommandGroup.READ));
		assertFalse(rights.hasGrant(fooName, SimpleBoundCommandGroup.WRITE));
	}

	/**
	 * Different attribute rights of two independent generalizations are a configuration error,
	 * unless the common specialization has attribute rights of its own.
	 */
	public void testContradictingGeneralizations() throws Exception {
		String left = "<part name='TestTLScriptSecurity:Left#name'>"
			+ "<grant operation='Read' roles='" + NAME_READER + "'/></part>";
		String right = "<part name='TestTLScriptSecurity:Right#name'>"
			+ "<grant operation='Read' roles='" + NAME_KEEPER + "'/></part>";
		String sameAsLeft = "<part name='TestTLScriptSecurity:Right#name'>"
			+ "<grant operation='Read' roles='" + NAME_READER + "'/></part>";
		String own = "<part name='TestTLScriptSecurity:LeftRight#name'>"
			+ "<grant operation='Read' roles='" + NAME_KEEPER + "'/></part>";

		assertError("TestTLScriptSecurity:LeftRight#name are ambiguous",
			config().add("part", left).add("part", right));
		assertNoErrors(config().add("part", left).add("part", right).add("part", own));
		assertNoErrors(config().add("part", left).add("part", sameAsLeft));
		assertNoErrors(config().add("part", left));
	}

	/**
	 * A type reaching the attribute rights of a type and of its specialization over two paths gets
	 * those of the specialization, without a configuration error.
	 *
	 * <p>
	 * {@code LeftRight} reaches {@code Left#name} through {@code Left}, and {@code Named#name}
	 * through {@code Right}. {@code Left} specializes {@code Named}, so its rights win.
	 * </p>
	 */
	public void testMoreSpecificGeneralizationWins() throws Exception {
		String general = "<part name='TestTLScriptSecurity:Named#name'>"
			+ "<grant operation='Read' roles='" + NAME_READER + "'/></part>";
		String special = "<part name='TestTLScriptSecurity:Left#name'>"
			+ "<grant operation='Read' roles='" + NAME_KEEPER + "'/></part>";
		ConfigBuilder config = config().add("part", general).add("part", special);
		assertNoErrors(config);

		TLObject leftRight;
		try (Transaction tx = beginTx()) {
			leftRight = newNamed("LeftRight");
			tx.commit();
		}
		SecurityConfigurationService service = config.service();
		TLStructuredTypePart named = part("Named");
		assertTrue("The rights of Left apply.", service.isReadAllowed(_keeper, leftRight, named));
		assertFalse("The rights of Named do not apply.", service.isReadAllowed(_reader, leftRight, named));
	}

	private static boolean readable(Person person, TLObject object, TLStructuredTypePart attribute) {
		return ModelAccessRights.getInstance().isReadAllowed(person, object, attribute);
	}

	private static Set<BoundedRole> discoverRoles(String className) {
		return ModelAccessRights.getInstance().getAllowedRoles(type(className), SimpleBoundCommandGroup.DISCOVER);
	}

	private static TLStructuredTypePart part(String className) {
		return type(className).getPart(NAME);
	}

	private static TLClass type(String className) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + className);
	}

	private static BoundedRole role(String name) {
		return BoundedRole.getRoleByName(PersistencyLayer.getKnowledgeBase(), name);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	public static Test suite() {
		return suite(TestPartRightsPerType.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.person.TestPerson;

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
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Tests the {@link SimpleBoundCommandGroup#DISCOVER Discover} right (ticket #29782) in the security
 * core and in TL-Script, on the path structure {@code PathRoot -> PathA -> PathB -> PathC} of the
 * model {@code TestTLScriptSecurity}.
 *
 * <p>
 * Fixture: {@code root.as = [a1, a2]}, {@code a1.bs = [b1, b2]}, {@code b1.cs = [c1, c2]}. The
 * path user reads {@code c1} only and thereby holds the path role on {@code b1}, {@code a1} and
 * {@code root}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDiscoverRight extends AbstractSearchExpressionTest {

	private KnowledgeBase _kb;

	private final Map<ObjectKey, KnowledgeItem> _toDelete = new HashMap<>();

	private final UpdateListener _creationListener = new UpdateListener() {
		@Override
		public void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
			_toDelete.putAll(event.getCreatedObjects());
			_toDelete.keySet().removeAll(event.getDeletedObjectKeys());
		}
	};

	Person _pathUser;

	Person _stranger;

	TLObject _root, _a1, _a2, _b1, _b2, _c1, _c2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_pathUser = TestPerson.createPerson("discoverPathUser");
			_stranger = TestPerson.createPerson("discoverStranger");

			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _pathUser);

			_c1 = node("PathC", "c1");
			_c1.tUpdateByName("readers", Collections.singletonList(reader));
			_c2 = node("PathC", "c2");
			_b1 = node("PathB", "b1");
			_b1.tUpdateByName("cs", Arrays.asList(_c1, _c2));
			_b2 = node("PathB", "b2");
			_a1 = node("PathA", "a1");
			_a1.tUpdateByName("bs", Arrays.asList(_b1, _b2));
			_a2 = node("PathA", "a2");
			_root = node("PathRoot", "root");
			_root.tUpdateByName("as", Arrays.asList(_a1, _a2));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		becomeUser(PersonManager.getManager().getRoot());
		_kb.removeUpdateListener(_creationListener);
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			KBUtils.deleteAllKI(_toDelete.values().iterator());
			tx.commit();
		}
		_toDelete.clear();
		_kb = null;
		super.tearDown();
	}

	TLObject newObject(String className) {
		TLClass type = (TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className);
		return DynamicModelService.getFactoryFor("TestTLScriptSecurity").createObject(type);
	}

	TLObject node(String className, String name) {
		TLObject result = newObject(className);
		result.tUpdateByName("name", name);
		return result;
	}

	static ModelAccessRights rights() {
		return ModelAccessRights.getInstance();
	}

	public void testDiscoverIsRegistered() {
		assertSame(SimpleBoundCommandGroup.DISCOVER, CommandGroupRegistry.resolve("Discover"));
		assertTrue(SimpleBoundCommandGroup.DISCOVER.isReadGroup());
	}

	public void testReadImpliesDiscover() {
		assertTrue(rights().isDiscoverAllowed(_pathUser, _c1));
		assertFalse(rights().isDiscoverAllowed(_pathUser, _c2));
	}

	public void testAttributeRightImpliesDiscover() {
		// The path role on b1, a1, root grants only attribute rights.
		assertTrue(rights().isDiscoverAllowed(_pathUser, _b1));
		assertTrue(rights().isDiscoverAllowed(_pathUser, _a1));
		assertTrue(rights().isDiscoverAllowed(_pathUser, _root));
		assertFalse(rights().isReadAllowed(_pathUser, _a1));
	}

	public void testSiblingsNotDiscoverable() {
		assertFalse(rights().isDiscoverAllowed(_pathUser, _a2));
		assertFalse(rights().isDiscoverAllowed(_pathUser, _b2));
		assertFalse(rights().isDiscoverAllowed(_stranger, _root));
	}

	public void testCreateImpliesDiscoverOnContainer() throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject writer = newObject("Employee");
			writer.tUpdateByName("account", _stranger);
			_b2.tUpdateByName("writers", Collections.singletonList(writer));
			tx.commit();
		}
		assertTrue(rights().isDiscoverAllowed(_stranger, _b2));
		assertFalse(rights().isReadAllowed(_stranger, _b2));
	}

	public void testAdminDiscoversEverything() {
		becomeUser(PersonManager.getManager().getRoot());
		assertTrue(rights().isDiscoverAllowed(_a2));
	}

	public void testAttributeGrantWithoutClassRead() {
		// a1 is not readable as a whole, but its structure attribute is.
		assertFalse(rights().isReadAllowed(_pathUser, _a1));
		assertTrue(rights().isReadAllowed(_pathUser, _a1, part("PathA", "bs")));
		assertFalse(rights().isReadAllowed(_pathUser, _a1, part("PathNode", "name")));
	}

	public void testAttributeGrantOnlyForHolderOfRole() {
		assertFalse(rights().isReadAllowed(_pathUser, _a2, part("PathA", "bs")));
	}

	public void testNavigationDeliversOnlyDiscoverable() throws Exception {
		becomeUser(_pathUser);
		assertEquals(Arrays.asList(_a1), eval("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`)", _root));
		assertEquals(Arrays.asList(_b1), eval("a -> $a.get(`TestTLScriptSecurity:PathA#bs`)", _a1));
		assertEquals(Arrays.asList(_c1), eval("b -> $b.get(`TestTLScriptSecurity:PathB#cs`)", _b1));
	}

	public void testLabelOfDiscoverableVisibleAttributeNot() throws Exception {
		becomeUser(_pathUser);
		assertEquals(Arrays.asList("a1"),
			eval("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`).map(a -> toString($a))", _root));
		assertNull(eval("a -> $a.get(`TestTLScriptSecurity:PathNode#name`)", _a1));
	}

	public void testReferersFiltered() throws Exception {
		becomeUser(_pathUser);
		assertEquals(set(_b1), asSet(eval("c -> $c.referers(`TestTLScriptSecurity:PathB#cs`)", _c1)));
	}

	public void testUncommittedObjectNotFiltered() throws Exception {
		becomeUser(_pathUser);
		Object result = execute(search("x -> new(`TestTLScriptSecurity:PathC`, transient: true)"), _root);
		assertNotNull(result);
	}

	public void testUnsecuredSeesEverything() throws Exception {
		becomeUser(_pathUser);
		QueryExecutor executor = QueryExecutor.compile(search("r -> $r.get(`TestTLScriptSecurity:PathRoot#as`)"));
		executor.disableSecurity();
		assertEquals(Arrays.asList(_a1, _a2), executor.execute(_root));
	}

	/**
	 * A discover-only object must not pass a filter on an attribute it must not read, whether the
	 * filter is compiled into the database query or interpreted.
	 */
	public void testCompiledFilterDoesNotLeakAttributeOfDiscoverOnlyObject() throws Exception {
		becomeUser(_pathUser);
		assertEquals(Collections.emptyList(), 
			asList(eval("all(`TestTLScriptSecurity:PathA`).filter(a -> $a.get(`TestTLScriptSecurity:PathNode#name`) == \"a1\")")));
	}

	/**
	 * {@code all()} delivers only the discoverable instances.
	 */
	public void testAllDeliversOnlyDiscoverable() throws Exception {
		becomeUser(_pathUser);
		assertEquals(set(_a1), asSet(eval("all(`TestTLScriptSecurity:PathA`)")));
		assertEquals(Double.valueOf(1), eval("all(`TestTLScriptSecurity:PathA`).size()"));
	}

	/**
	 * The dynamic form {@code all($t)} filters as well.
	 */
	public void testDynamicAll() throws Exception {
		becomeUser(_pathUser);
		assertEquals(set(_a1), asSet(eval("t -> all($t)", TLModelUtil.findType("TestTLScriptSecurity:PathA"))));
	}

	/**
	 * A filter compiled into the database query (KBQuery) still delivers only discoverable objects.
	 */
	public void testCompiledFilterOnAll() throws Exception {
		becomeUser(_pathUser);
		assertEquals(set(_b1), asSet(eval("all(`TestTLScriptSecurity:PathB`).filter(b -> $b.get(`TestTLScriptSecurity:PathB#cs`).size() > 0)")));
	}

	/**
	 * A compiled filter that also matches undiscoverable objects does not deliver them.
	 */
	public void testCompiledFilterMatchingUndiscoverable() throws Exception {
		becomeUser(_pathUser);
		assertEquals(set(_b1), asSet(eval("all(`TestTLScriptSecurity:PathB`).filter(b -> $b.get(`TestTLScriptSecurity:PathB#cs`).size() >= 0)")));
	}

	/**
	 * Without security, all instances are delivered.
	 */
	public void testUnsecuredAllSeesEverything() throws Exception {
		becomeUser(_pathUser);
		QueryExecutor executor = QueryExecutor.compile(search("all(`TestTLScriptSecurity:PathA`)"));
		executor.disableSecurity();
		assertEquals(set(_a1, _a2), asSet(executor.execute()));
	}

	private static java.util.List<?> asList(Object value) {
		return new java.util.ArrayList<>((java.util.Collection<?>) value);
	}

	TLStructuredTypePart part(String className, String partName) {
		return ((TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className)).getPart(partName);
	}

	public static Test suite() {
		return suite(TestDiscoverRight.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			PersonManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}

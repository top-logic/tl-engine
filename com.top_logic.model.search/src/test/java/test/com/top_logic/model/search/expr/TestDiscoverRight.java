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

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
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.objects.KnowledgeItem;
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
import com.top_logic.model.search.constraint.DeleteConstraintByExpression;
import com.top_logic.model.search.expr.query.Args;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.ConstraintCheckByExpression;
import com.top_logic.model.search.providers.I18NConstants;
import com.top_logic.model.search.providers.LabelByExpression;
import com.top_logic.model.search.providers.LabelProviderByExpression;
import com.top_logic.model.search.providers.TitleByExpression;
import com.top_logic.model.search.providers.TooltipProviderByExpression;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.error.TopLogicException;

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
			// The singletons outlive the test, their values must not keep test objects.
			for (String name : Arrays.asList("HOME", "OFFICE")) {
				TLObject singleton = singleton(name);
				for (TLStructuredTypePart part : singleton.tType().getAllParts()) {
					if (!part.isDerived()) {
						singleton.tUpdate(part, TLModelUtil.getEmptyValue(part));
					}
				}
			}
			// A cascading delete may already have removed a tracked item (e.g. an association).
			for (KnowledgeItem item : _toDelete.values()) {
				if (item.isAlive()) {
					_kb.delete(item);
				}
			}
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

	static TLObject singleton(String name) {
		return ((TLClass) TLModelUtil.findType("TestTLScriptSecurity:PathHome")).getModule().getSingleton(name);
	}

	/**
	 * Lets the given person hold the role that the given reference of the given object delivers.
	 */
	void assign(TLObject object, String reference, Person person) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject employee = newObject("Employee");
			employee.tUpdateByName("account", person);
			object.tUpdateByName(reference, Collections.singletonList(employee));
			tx.commit();
		}
	}

	void setTitle(TLObject object, String title) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			object.tUpdateByName("title", title);
			tx.commit();
		}
	}

	/**
	 * R2 for a singleton with rights of its own: an attribute right on one of its attributes implies
	 * Discover on it.
	 */
	public void testAttributeRightImpliesDiscoverOnSingleton() throws Exception {
		TLObject office = singleton("OFFICE");
		assertFalse(rights().isDiscoverAllowed(_stranger, office));
		assign(office, "viewers", _stranger);
		assertTrue(rights().isReadAllowed(_stranger, office, part("PathOffice", "title")));
		assertFalse(rights().isReadAllowed(_stranger, office));
		assertTrue(rights().isDiscoverAllowed(_stranger, office));
	}

	/**
	 * R2 for a singleton with rights of its own: the right to create in one of its compositions
	 * implies Discover on it.
	 */
	public void testCreateImpliesDiscoverOnSingleton() throws Exception {
		TLObject office = singleton("OFFICE");
		assign(office, "writers", _stranger);
		assertFalse(rights().isReadAllowed(_stranger, office));
		assertTrue(rights().isDiscoverAllowed(_stranger, office));
	}

	public void testHasSingletonRights() {
		assertTrue(rights().hasSingletonRights((TLClass) TLModelUtil.findType("TestTLScriptSecurity:PathHome")));
		assertTrue(rights().hasSingletonRights((TLClass) TLModelUtil.findType("TestTLScriptSecurity:PathNode")));
		assertFalse(rights().hasSingletonRights((TLClass) TLModelUtil.findType("TestTLScriptSecurity:PathA")));
	}

	/**
	 * By its class rights, a home is readable whenever it is discoverable, but the rights of the
	 * singleton let a writer discover it without reading it. A filter compiled into the database
	 * query must therefore not deliver the singleton by the value of an attribute the user must not
	 * read.
	 */
	public void testCompiledFilterRespectsSingletonRights() throws Exception {
		TLObject home = singleton("HOME");
		setTitle(home, "home");
		assign(home, "editors", _stranger);
		assertTrue(rights().isDiscoverAllowed(_stranger, home));
		assertFalse(rights().isReadAllowed(_stranger, home, part("PathHome", "title")));
		becomeUser(_stranger);
		assertEquals(Collections.emptyList(), asList(eval(
			"all(`TestTLScriptSecurity:PathHome`).filter(h -> $h.get(`TestTLScriptSecurity:PathHome#title`) == \"home\")")));
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

	/**
	 * R4: An undiscoverable object passed as input is not visible to the script.
	 */
	public void testInputFiltered() throws Exception {
		becomeUser(_pathUser);
		assertNull(eval("x -> $x", _a2));
	}

	/**
	 * R4: Objects nested in an input collection are filtered, too.
	 */
	public void testNestedInputFiltered() throws Exception {
		becomeUser(_pathUser);
		assertEquals(Arrays.asList(_a1), eval("x -> $x", Arrays.asList(_a1, _a2)));
		assertEquals(Double.valueOf(1), eval("x -> $x.size()", Arrays.asList(_a1, _a2)));
	}

	/**
	 * R5: A new reference target must be discoverable.
	 */
	public void testSetToUndiscoverableTargetDenied() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject hidden = createHiddenC("hidden");
		becomeUser(_pathUser);
		assertPermissionDenied(
			() -> executeUnfiltered("b -> c -> $b.set(`TestTLScriptSecurity:PathB#cs`, list($c))", _b1, hidden));
	}

	/**
	 * Objects already contained in the stored value are not checked.
	 */
	public void testSetKeepingStoredHiddenTargetAllowed() throws Exception {
		grantWrite(_pathUser, _b1);
		becomeUser(_pathUser);
		executeUnfiltered("b -> c -> $b.set(`TestTLScriptSecurity:PathB#cs`, list($c))", _b1, _c2);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(Arrays.asList(_c2), _b1.tValueByName("cs"));
	}

	/**
	 * The dynamic set checks the targets, too.
	 */
	public void testDynamicSetToUndiscoverableTargetDenied() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject hidden = createHiddenC("hidden");
		becomeUser(_pathUser);
		assertPermissionDenied(() -> executeUnfiltered(
			"b -> c -> $b.dynamicSet(`TestTLScriptSecurity:PathB#cs`, list($c))", _b1, hidden));
	}

	/**
	 * R5: Adding an undiscoverable target is denied.
	 */
	public void testAddUndiscoverableTargetDenied() throws Exception {
		grantWrite(_pathUser, _b2);
		becomeUser(_pathUser);
		assertPermissionDenied(
			() -> executeUnfiltered("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, $c)", _b2, _c2));
	}

	/**
	 * R10: set replaces the whole value, including elements the user cannot discover.
	 */
	public void testSetReplacesIncludingHidden() throws Exception {
		grantWrite(_pathUser, _b1);
		becomeUser(_pathUser);
		eval("b -> $b.set(`TestTLScriptSecurity:PathB#cs`, list())", _b1);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(Collections.emptyList(), _b1.tValueByName("cs"));
	}

	/**
	 * R10: add keeps the elements the user cannot discover.
	 */
	public void testAddKeepsHidden() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject c3 = createReadableC("c3");
		becomeUser(_pathUser);
		eval("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, $c)", _b1, c3);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(Arrays.asList(_c1, _c2, c3), _b1.tValueByName("cs"));
	}

	/**
	 * R10: The index of add refers to the list the user sees.
	 */
	public void testAddAtVisibleIndex() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject c3 = createReadableC("c3");
		TLObject c4 = createReadableC("c4");
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			_b1.tUpdateByName("cs", Arrays.asList(_c2, _c1, c3));
			tx.commit();
		}
		becomeUser(_pathUser);
		// The user sees [c1, c3]; index 1 means "between c1 and c3" in the visible list.
		eval("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, 1, $c)", _b1, c4);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(Arrays.asList(_c2, _c1, c4, c3), _b1.tValueByName("cs"));
	}

	/**
	 * Index 0 of the visible list inserts before the first visible element.
	 */
	public void testAddAtVisibleIndexZero() throws Exception {
		grantWrite(_pathUser, _b1);
		TLObject c3 = createReadableC("c3");
		becomeUser(_pathUser);
		eval("b -> c -> $b.add(`TestTLScriptSecurity:PathB#cs`, 0, $c)", _b1, c3);
		becomeUser(PersonManager.getManager().getRoot());
		assertEquals(Arrays.asList(c3, _c1, _c2), _b1.tValueByName("cs"));
	}

	/**
	 * Evaluates the script with the inputs handed in unfiltered (the security of the script's own
	 * operations stays on), so that an undiscoverable object can reach a write operation.
	 */
	private Object executeUnfiltered(String script, Object... args) throws Exception {
		QueryExecutor executor = QueryExecutor.compile(kb(), model(), search(script));
		return executor.executeIntermediate(executor.context(), Args.some(args));
	}

	void grantWrite(Person person, TLObject b) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject writer = newObject("Employee");
			writer.tUpdateByName("account", person);
			b.tUpdateByName("editors", Collections.singletonList(writer));
			tx.commit();
		}
	}

	TLObject createHiddenC(String name) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject c = node("PathC", name);
			tx.commit();
			return c;
		}
	}

	TLObject createReadableC(String name) throws Exception {
		try (Transaction tx = _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject reader = newObject("Employee");
			reader.tUpdateByName("account", _pathUser);
			TLObject c = node("PathC", name);
			c.tUpdateByName("readers", Collections.singletonList(reader));
			tx.commit();
			return c;
		}
	}

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

	private static java.util.List<?> asList(Object value) {
		return new java.util.ArrayList<>((java.util.Collection<?>) value);
	}

	TLStructuredTypePart part(String className, String partName) {
		return ((TLClass) TLModelUtil.findType("TestTLScriptSecurity:" + className)).getPart(partName);
	}

	private static final String NAME_SCRIPT = "x -> $x.get(`TestTLScriptSecurity:PathNode#name`)";

	public void testLabelScriptUnsecured() throws Exception {
		LabelProviderByExpression.Config<?> config =
			TypedConfiguration.newConfigItem(LabelProviderByExpression.Config.class);
		config.update(config.descriptor().getProperty("label"),
			parse("x -> $x.get(`TestTLScriptSecurity:PathNode#name`)"));
		LabelProviderByExpression<?> provider =
			new LabelProviderByExpression<>(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		becomeUser(_pathUser);
		assertEquals("a1", provider.getLabel(_a1));
	}

	/**
	 * R12: Only labels belong to Discover; a tooltip script is evaluated secured and does not
	 * disclose attributes of a discover-only object.
	 */
	public void testTooltipScriptSecured() throws Exception {
		TooltipProviderByExpression.Config<?> config =
			TypedConfiguration.newConfigItem(TooltipProviderByExpression.Config.class);
		config.update(config.descriptor().getProperty("tooltip"), parse(NAME_SCRIPT));
		TooltipProviderByExpression<?> provider =
			new TooltipProviderByExpression<>(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		becomeUser(_pathUser);
		assertNull(provider.getTooltip(_a1));
	}

	/**
	 * R12: In a {@link LabelByExpression}, the label script is evaluated unsecured, the tooltip
	 * script secured.
	 */
	public void testLabelByExpressionTooltipSecured() throws Exception {
		LabelByExpression.Config<?> config = TypedConfiguration.newConfigItem(LabelByExpression.Config.class);
		config.update(config.descriptor().getProperty("label"), parse(NAME_SCRIPT));
		config.update(config.descriptor().getProperty("tooltip"), parse(NAME_SCRIPT));
		LabelByExpression provider =
			new LabelByExpression(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		QueryExecutor tooltipExpr = (QueryExecutor) privateField(LabelByExpression.class, "_tooltipExpr", provider);
		becomeUser(_pathUser);
		assertEquals("a1", provider.getLabel(_a1));
		assertNull(tooltipExpr.execute(_a1));
	}

	/**
	 * R12: A title script is evaluated secured.
	 */
	public void testTitleScriptSecured() throws Exception {
		TitleByExpression.Config<?> config = TypedConfiguration.newConfigItem(TitleByExpression.Config.class);
		config.update(config.descriptor().getProperty("expr"), parse(NAME_SCRIPT));
		TitleByExpression<?> provider =
			new TitleByExpression<>(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		QueryExecutor titleExpr = (QueryExecutor) privateField(TitleByExpression.class, "_expr", provider);
		becomeUser(_pathUser);
		assertNull(titleExpr.execute(_a1));
	}

	private static Object privateField(Class<?> type, String name, Object instance) throws Exception {
		java.lang.reflect.Field field = type.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(instance);
	}

	/**
	 * R12: The veto message of a delete constraint is evaluated unsecured but names only objects
	 * the user may discover.
	 */
	public void testDeleteVetoMasksUndiscoverable() throws Exception {
		DeleteConstraintByExpression.Config<?> config =
			TypedConfiguration.newConfigItem(DeleteConstraintByExpression.Config.class);
		config.update(config.descriptor().getProperty("expr"),
			parse("r -> 'In use by {0}'@en.fill($r.get(`TestTLScriptSecurity:PathRoot#as`))"));
		DeleteConstraintByExpression<?> constraint =
			new DeleteConstraintByExpression<>(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
		becomeUser(_pathUser);
		ResKey veto = constraint.getDeleteVeto(_root).get();
		assertEquals(Arrays.asList(_a1, I18NConstants.MASKED_OBJECT), asList(veto.arguments()[0]));
	}

	public void testConstraintMessageMasksUndiscoverable() {
		ResKey message = ResKey.forTest("test.masking.two").asResKey2().fill(_a2, _a1);
		becomeUser(_pathUser);
		ResKey masked = ConstraintCheckByExpression.maskUndiscoverable(message);
		assertSame(I18NConstants.MASKED_OBJECT, masked.arguments()[0]);
		assertSame(_a1, masked.arguments()[1]);
		assertEquals(message.plain(), masked.plain());
	}

	public void testConstraintMessageMasksNested() {
		ResKey inner = ResKey.forTest("test.masking.inner").asResKey1().fill(_a2);
		ResKey message = ResKey.forTest("test.masking.outer").asResKey1().fill(inner);
		becomeUser(_pathUser);
		ResKey masked = ConstraintCheckByExpression.maskUndiscoverable(message);
		ResKey maskedInner = (ResKey) masked.arguments()[0];
		assertSame(I18NConstants.MASKED_OBJECT, maskedInner.arguments()[0]);
	}

	public void testConstraintMessageUnchangedForDiscoverable() {
		ResKey message = ResKey.forTest("test.masking.one").asResKey1().fill(_a1);
		becomeUser(_pathUser);
		assertSame(message, ConstraintCheckByExpression.maskUndiscoverable(message));
	}

	public void testConstraintMessageMasksCollectionElements() {
		ResKey message = ResKey.forTest("test.masking.list").asResKey1().fill(Arrays.asList(_a1, _a2));
		becomeUser(_pathUser);
		ResKey masked = ConstraintCheckByExpression.maskUndiscoverable(message);
		assertEquals(Arrays.asList(_a1, I18NConstants.MASKED_OBJECT), masked.arguments()[0]);
	}

	public void testConstraintMessageMasksArrayElements() {
		ResKey message = ResKey.forTest("test.masking.array").asResKey1().fill((Object) new Object[] { _a2, _a1 });
		becomeUser(_pathUser);
		ResKey masked = ConstraintCheckByExpression.maskUndiscoverable(message);
		assertTrue(Arrays.equals(new Object[] { I18NConstants.MASKED_OBJECT, _a1 }, (Object[]) masked.arguments()[0]));
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

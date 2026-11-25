/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import static test.com.top_logic.model.search.expr.TestOperationDeclarations.assertError;
import static test.com.top_logic.model.search.expr.TestOperationDeclarations.assertNoErrors;
import static test.com.top_logic.model.search.expr.TestOperationDeclarations.config;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.top_logic.model.security.SecurityConfigurationService;
import com.top_logic.model.security.SecurityConfigurationService.ClassAttributes;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Test for the {@link SecurityConfigurationService.TLClassAccessRights#getDiscoverAttributes()
 * discover attributes} of a class: attributes a person holding
 * {@link SimpleBoundCommandGroup#DISCOVER} on an object may read.
 *
 * <p>
 * The test model (see <code>TestTLScriptSecurity.model.xml</code>) declares the interface
 * {@code Named} with the attributes {@code name}, {@code accountA} and {@code accountB}. The role
 * {@code NameReader} is held by the person in {@code accountA}, the role {@code NameKeeper} by the
 * person in {@code accountB}. Each test configures the service itself: {@code NameReader} may read
 * every {@code Named} object, {@code NameKeeper} may only discover it.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestDiscoverAttributes extends AbstractSearchExpressionTest {

	private static final String MODULE = "TestTLScriptSecurity";

	private static final String NAME_READER = MODULE + ".NameReader";

	private static final String NAME_KEEPER = MODULE + ".NameKeeper";

	private static final String NAME = "name";

	private static final String ACCOUNT_A = "accountA";

	/** The grants of all tests: {@link #NAME_READER} reads, {@link #NAME_KEEPER} discovers. */
	private static final String GRANTS = "<grant inherit='true' operation='Read' roles='" + NAME_READER + "'/>"
		+ "<grant inherit='true' operation='Discover' roles='" + NAME_KEEPER + "'/>";

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

	private TLObject _leftRight;

	/**
	 * A {@code DiscoverNamed}, whose name and related objects {@link #NAME_KEEPER} may read by the
	 * discover attributes of the application configuration. It is related to {@link #_foo}, which
	 * {@link #_keeper} may discover, and to {@link #_bar}, which {@link #_keeper} may not.
	 */
	private TLObject _discoverNamed;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_kb = PersistencyLayer.getKnowledgeBase();
		_kb.addUpdateListener(_creationListener);

		try (Transaction tx = beginTx()) {
			_reader = TestPerson.createPerson("discoverAttributesReader");
			_keeper = TestPerson.createPerson("discoverAttributesKeeper");
			_foo = newNamed("Foo");
			_bar = newNamed("Bar");
			_sub = newNamed("Sub");
			_subSub = newNamed("SubSub");
			_barSibling = newNamed("BarSibling");
			_leftRight = newNamed("LeftRight");
			_discoverNamed = newNamed("DiscoverNamed");
			_discoverNamed.tUpdateByName("related", List.of(_foo, _bar));
			tx.commit();
		}
	}

	private TLObject newNamed(String className) {
		TLObject result = DynamicModelService.getFactoryFor(MODULE).createObject(type(className));
		result.tUpdateByName(NAME, className);
		result.tUpdateByName(ACCOUNT_A, _reader);
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
	 * A person holding {@link SimpleBoundCommandGroup#DISCOVER} reads the discover attributes, but
	 * no other attribute.
	 */
	public void testDiscoverReadsDiscoverAttribute() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>"));

		assertTrue(service.isReadAllowed(_keeper, _bar, part("Bar", NAME)));
		assertFalse(service.isReadAllowed(_keeper, _bar, part("Bar", ACCOUNT_A)));
		assertTrue(service.isReadAllowed(_reader, _bar, part("Bar", NAME)));
		assertTrue(service.isReadAllowed(_reader, _bar, part("Bar", ACCOUNT_A)));
	}

	/**
	 * Without discover attributes, {@link SimpleBoundCommandGroup#DISCOVER} makes no attribute
	 * readable.
	 */
	public void testNoDiscoverAttributes() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named'>" + GRANTS + "</class>"));

		assertFalse(service.isReadAllowed(_keeper, _bar, part("Bar", NAME)));
		assertTrue(service.isReadAllowed(_reader, _bar, part("Bar", NAME)));
	}

	/**
	 * A discover attribute is readable, not writable.
	 */
	public void testWriteNotAffected() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>"));

		assertFalse(service.isAllowed(_keeper, _bar, part("Bar", NAME), SimpleBoundCommandGroup.WRITE));
	}

	/**
	 * A read grant of the attribute itself takes precedence over the discover attributes.
	 */
	public void testOwnPartGrantWins() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>")
			.add("part", "<part name='" + MODULE + ":Foo#name'>"
				+ "<grant operation='Read' roles='" + NAME_READER + "'/></part>"));

		assertFalse(service.isReadAllowed(_keeper, _foo, part("Foo", NAME)));
		assertTrue(service.isReadAllowed(_reader, _foo, part("Foo", NAME)));
		assertTrue(service.isReadAllowed(_keeper, _bar, part("Bar", NAME)));
	}

	/**
	 * The discover attributes of a class apply to its specializations.
	 */
	public void testSpecializationInherits() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>"));

		assertTrue(service.isReadAllowed(_keeper, _subSub, part("SubSub", NAME)));
		assertTrue(service.isReadAllowed(_keeper, _barSibling, part("BarSibling", NAME)));
		assertEquals(Set.of(NAME), service.getDiscoverAttributes(type("SubSub")));
	}

	/**
	 * A specialization replaces the discover attributes of its generalizations by its own, even by
	 * an empty setting.
	 */
	public void testSpecializationOverrides() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>")
			.add("class", "<class name='" + MODULE + ":Sub' discover-attributes=''/>"));

		assertFalse(service.isReadAllowed(_keeper, _sub, part("Sub", NAME)));
		assertFalse(service.isReadAllowed(_keeper, _subSub, part("SubSub", NAME)));
		assertTrue(service.isReadAllowed(_keeper, _bar, part("Bar", NAME)));
		assertTrue(service.getDiscoverAttributes(type("SubSub")).isEmpty());
	}

	/**
	 * A class entry without the setting keeps the discover attributes of its generalizations.
	 */
	public void testEntryWithoutSettingInherits() throws Exception {
		SecurityConfigurationService service = service(config()
			.add("class", "<class name='" + MODULE + ":Named' discover-attributes='name'>" + GRANTS + "</class>")
			.add("class", "<class name='" + MODULE + ":Sub'/>"));

		assertTrue(service.isReadAllowed(_keeper, _subSub, part("SubSub", NAME)));
	}

	/**
	 * Different discover attributes of two independent generalizations are a configuration error,
	 * unless the common specialization has discover attributes of its own.
	 */
	public void testContradictingGeneralizations() throws Exception {
		String named = "<class name='" + MODULE + ":Named'>" + GRANTS + "</class>";
		String left = "<class name='" + MODULE + ":Left' discover-attributes='name'/>";
		String right = "<class name='" + MODULE + ":Right' discover-attributes=''/>";
		String sameAsLeft = "<class name='" + MODULE + ":Right' discover-attributes='name'/>";
		String own = "<class name='" + MODULE + ":LeftRight' discover-attributes=''/>";

		assertError("discover attributes of " + MODULE + ":LeftRight are ambiguous",
			config().add("class", named).add("class", left).add("class", right));
		assertNoErrors(config().add("class", named).add("class", left).add("class", right).add("class", own));

		ConfigBuilder same = config().add("class", named).add("class", left).add("class", sameAsLeft);
		assertNoErrors(same);
		assertTrue(same.service().isReadAllowed(_keeper, _leftRight, part("LeftRight", NAME)));
	}

	/**
	 * A discover attribute the class does not have is a configuration error.
	 */
	public void testUnknownAttribute() throws Exception {
		assertError("has no attribute doesNotExist",
			config().add("class", "<class name='" + MODULE + ":Named' discover-attributes='doesNotExist'/>"));
	}

	/**
	 * TL-Script reads the discover attributes of an object the user may only discover, but no
	 * other attribute.
	 */
	public void testScriptReadsDiscoverAttribute() throws Exception {
		becomeUser(_keeper);
		assertEquals("DiscoverNamed", eval("x -> $x.get(`TestTLScriptSecurity:Named#name`)", _discoverNamed));
		assertNull(eval("x -> $x.get(`TestTLScriptSecurity:Named#accountA`)", _discoverNamed));

		becomeUser(_reader);
		assertEquals(_reader, eval("x -> $x.get(`TestTLScriptSecurity:Named#accountA`)", _discoverNamed));
	}

	/**
	 * A discover attribute that is a reference delivers only the targets the user may discover.
	 */
	public void testReferenceTargetNeedsDiscover() throws Exception {
		String related = "x -> $x.get(`TestTLScriptSecurity:DiscoverNamed#related`)";

		becomeUser(_keeper);
		assertEquals(set(_foo), asSet(eval(related, _discoverNamed)));

		becomeUser(_reader);
		assertEquals(set(_foo, _bar), asSet(eval(related, _discoverNamed)));
	}

	/**
	 * A filter on a discover attribute in a database query sees the attribute for a user who may
	 * only discover the objects.
	 */
	public void testFilterOnDiscoverAttribute() throws Exception {
		String byName = "all(`TestTLScriptSecurity:DiscoverNamed`)"
			+ ".filter(x -> $x.get(`TestTLScriptSecurity:Named#name`) == 'DiscoverNamed')";
		String byAccount = "all(`TestTLScriptSecurity:DiscoverNamed`)"
			+ ".filter(x -> $x.get(`TestTLScriptSecurity:Named#accountA`) != null)";

		becomeUser(_keeper);
		assertEquals(set(_discoverNamed), asSet(eval(byName)));
		assertEquals(set(), asSet(eval(byAccount)));
	}

	/**
	 * The editor offers the attributes of the configured class, including the inherited ones.
	 */
	public void testAttributeOptions() {
		Set<String> names = names(new ClassAttributes().apply(MODULE + ":Sub"));
		assertTrue(names.toString(), names.containsAll(Set.of(NAME, ACCOUNT_A, "accountB")));

		assertTrue(new ClassAttributes().apply(null).isEmpty());
		assertTrue(new ClassAttributes().apply("").isEmpty());
		assertTrue(new ClassAttributes().apply(MODULE + ":DoesNotExist").isEmpty());
		assertTrue(new ClassAttributes().apply(MODULE + ":").isEmpty());
	}

	private static Set<String> names(Collection<? extends TLStructuredTypePart> parts) {
		return parts.stream().map(TLStructuredTypePart::getName).collect(Collectors.toSet());
	}

	private static SecurityConfigurationService service(ConfigBuilder config) {
		assertNoErrors(config);
		return config.service();
	}

	private static TLStructuredTypePart part(String className, String name) {
		return type(className).getPart(name);
	}

	private static TLClass type(String className) {
		return (TLClass) TLModelUtil.findType(MODULE + ":" + className);
	}

	private Transaction beginTx() {
		return _kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE);
	}

	public static Test suite() {
		return suite(TestDiscoverAttributes.class,
			AccessManager.Module.INSTANCE,
			TLSecurityDeviceManager.Module.INSTANCE,
			InitialRolesManager.Module.INSTANCE,
			SecurityConfigurationService.Module.INSTANCE);
	}

}

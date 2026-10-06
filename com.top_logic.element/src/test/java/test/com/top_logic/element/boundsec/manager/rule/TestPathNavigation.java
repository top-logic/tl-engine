/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.boundsec.manager.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.boundsec.manager.rule.PathNavigation;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link PathNavigation} navigating an abstract reference.
 *
 * <p>
 * The test uses the model module {@code TestPathNavigation} of the element test application. An
 * abstract reference is derived, but can be navigated by a role rule, as long as all its concrete
 * overrides are stored and therefore fire change notifications.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestPathNavigation extends BasicTestCase {

	private static final String MODULE = "TestPathNavigation";

	private static final String ITEM = MODULE + ":Item";

	private static final String STORED_CONTAINER = MODULE + ":StoredContainer";

	private static final String INDIRECT_CONTAINER = MODULE + ":IndirectContainer";

	private static final String CONTAINER_ITEMS = MODULE + ":Container#items";

	private static final String INTERMEDIATE_CONTAINER_ITEMS = MODULE + ":IntermediateContainer#items";

	private static final String STORED_CONTAINER_ITEMS = MODULE + ":StoredContainer#items";

	private static final String INDIRECT_CONTAINER_ITEMS = MODULE + ":IndirectContainer#items";

	private static final String SOURCE_TARGETS = MODULE + ":Source#targets";

	private static final String STORED_SOURCE_TARGETS = MODULE + ":StoredSource#targets";

	private static final String COMPUTED_SOURCE_TARGETS = MODULE + ":ComputedSource#targets";

	private static final String UNIMPLEMENTED_ITEMS = MODULE + ":Unimplemented#items";

	private final List<TLObject> _created = new ArrayList<>();

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				object.tDelete();
			}
		});
		_created.clear();

		super.tearDown();
	}

	public void testAbstractReferenceWithStoredOverrides() {
		BufferingProtocol log = new BufferingProtocol();
		PathNavigation navigation = createNavigation(log, CONTAINER_ITEMS, false);

		assertEquals(List.of(), log.getErrors());
		assertTrue(PathNavigation.isNavigable(part(CONTAINER_ITEMS)));
		assertEquals("The stored overrides must be tracked, the abstract redeclaration holds no values.",
			Set.of(part(CONTAINER_ITEMS), part(STORED_CONTAINER_ITEMS), part(INDIRECT_CONTAINER_ITEMS)),
			new HashSet<>(navigation.getRelevantParts()));
		assertFalse(navigation.getRelevantParts().contains(part(INTERMEDIATE_CONTAINER_ITEMS)));
	}

	public void testAbstractReferenceWithComputedOverride() {
		BufferingProtocol log = new BufferingProtocol();
		createNavigation(log, SOURCE_TARGETS, false);

		assertEquals(1, log.getErrors().size());
		assertTrue("The error must name the computed override: " + log.getErrors(),
			log.getErrors().get(0).contains(COMPUTED_SOURCE_TARGETS));
		assertFalse(log.getErrors().get(0).contains(STORED_SOURCE_TARGETS));
		assertFalse(PathNavigation.isNavigable(part(SOURCE_TARGETS)));
		assertEquals(List.of(part(COMPUTED_SOURCE_TARGETS)), PathNavigation.untrackableParts(part(SOURCE_TARGETS)));
	}

	public void testComputedReference() {
		BufferingProtocol log = new BufferingProtocol();
		createNavigation(log, COMPUTED_SOURCE_TARGETS, false);

		assertEquals(1, log.getErrors().size());
		assertFalse(PathNavigation.isNavigable(part(COMPUTED_SOURCE_TARGETS)));
	}

	public void testAbstractReferenceWithoutOverride() {
		BufferingProtocol log = new BufferingProtocol();
		createNavigation(log, UNIMPLEMENTED_ITEMS, false);

		assertEquals(List.of(), log.getErrors());
		assertTrue(PathNavigation.isNavigable(part(UNIMPLEMENTED_ITEMS)));
	}

	public void testNavigateAbstractReference() {
		TLObject[] objects = new TLObject[4];
		inTransaction(() -> {
			TLObject item1 = create(ITEM);
			TLObject item2 = create(ITEM);
			TLObject stored = create(STORED_CONTAINER);
			TLObject indirect = create(INDIRECT_CONTAINER);
			stored.tUpdateByName("items", List.of(item1, item2));
			indirect.tUpdateByName("items", List.of(item2));
			objects[0] = item1;
			objects[1] = item2;
			objects[2] = stored;
			objects[3] = indirect;
		});
		TLObject item1 = objects[0];
		TLObject item2 = objects[1];
		TLObject stored = objects[2];
		TLObject indirect = objects[3];

		PathNavigation forward = createNavigation(new BufferingProtocol(), CONTAINER_ITEMS, false);
		assertEquals(Set.of(item1, item2), new HashSet<>(forward.getValues(stored)));
		assertEquals(Set.of(item2), new HashSet<>(forward.getValues(indirect)));

		PathNavigation backward = createNavigation(new BufferingProtocol(), CONTAINER_ITEMS, true);
		assertEquals(Set.of(stored), new HashSet<>(backward.getValues(item1)));
		assertEquals(Set.of(stored, indirect), new HashSet<>(backward.getValues(item2)));
	}

	private static PathNavigation createNavigation(BufferingProtocol log, String reference, boolean inverse) {
		PathElementConfig config = TypedConfiguration.newConfigItem(PathElementConfig.class);
		config.setAttribute(TLModelPartRef.ref(reference));
		config.setInverse(inverse);
		return new DefaultInstantiationContext(log).getInstance(config);
	}

	private static TLStructuredTypePart part(String qualifiedName) {
		return (TLStructuredTypePart) TLModelUtil.findPart(qualifiedName);
	}

	private TLObject create(String qualifiedTypeName) {
		TLClass type = (TLClass) TLModelUtil.findType(qualifiedTypeName);
		TLObject result = ModelService.getInstance().getFactory().createObject(type);
		_created.add(result);
		return result;
	}

	private static void inTransaction(Runnable modification) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			modification.run();
			tx.commit();
		}
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		return ElementWebTestSetup.createElementWebTestSetup(new TestSuite(TestPathNavigation.class));
	}

}

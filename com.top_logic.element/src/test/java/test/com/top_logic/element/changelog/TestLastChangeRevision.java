/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.changelog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import junit.framework.Test;

import test.com.top_logic.element.meta.TestWithModelExtension;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.Part;
import test.com.top_logic.element.structured.model.TestTypesFactory;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.changelog.ChangeLogBuilder;
import com.top_logic.element.changelog.LastChangeRevision;
import com.top_logic.element.changelog.model.Change;
import com.top_logic.element.changelog.model.ChangeSet;
import com.top_logic.element.changelog.model.Modification;
import com.top_logic.element.changelog.model.Update;
import com.top_logic.element.meta.AssociationStorageDescriptor;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.element.model.cache.ModelTables;
import com.top_logic.element.model.i18n.I18NAttributeStorage;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.LifecycleStorageModified;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests for {@link LastChangeRevision}.
 */
@SuppressWarnings("javadoc")
public class TestLastChangeRevision extends TestWithModelExtension {

	private static final String MODULE = "test.com.top_logic.element.changelog.TestLastChangeRevision";

	private static final String NAME = "name";

	private static final String TITLE = "title";

	private static final String SINGLE = "single";

	private static final String MANY = "many";

	private static final String CHILDREN = "children";

	private TLModule _testModule;

	private TLClass _itemType;

	private ModelFactory _factory;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		extendApplicationModel(TestLastChangeRevision.class, "ext.model.xml");
		_testModule = TLModelUtil.findModule(MODULE);
		_itemType = (TLClass) _testModule.getType("Item");
		_factory = DynamicModelService.getInstance().getFactory(_testModule);
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = beginTX()) {
			List<TLObject> instances = new ArrayList<>();
			for (TLClass clazz : _testModule.getClasses()) {
				instances.addAll(MetaElementUtil.getAllDirectInstancesOf(clazz, TLObject.class));
			}
			KBUtils.deleteAll(instances);
			_testModule.tDelete();
			tx.commit();
		}
		super.tearDown();
	}

	public void testCreation() {
		Revision created;
		TLObject item;
		try (Transaction tx = beginTX()) {
			item = _factory.createObject(_itemType);
			tx.commit();
			created = tx.getCommitRevision();
		}
		assertEquals(created, LastChangeRevision.of(item));
	}

	public void testTransientAndNull() {
		assertNull(LastChangeRevision.of(null));
		TLObject item = TransientObjectFactory.INSTANCE.createObject(_itemType);
		assertEquals(Revision.CURRENT, LastChangeRevision.of(item));
	}

	public void testPrimitive() {
		TLObject item = create("a");
		assertMoves(item, x -> x.tUpdateByName(NAME, "b"));
	}

	public void testSingleReference() {
		TLObject item = create("a");
		TLObject target = create("target");
		assertMoves(item, x -> x.tUpdateByName(SINGLE, target));
		assertMoves(item, x -> x.tUpdateByName(SINGLE, null));
	}

	public void testMultipleReference() {
		TLObject item = create("a");
		TLObject t1 = create("t1");
		TLObject t2 = create("t2");
		assertMoves(item, x -> add(x, MANY, t1));
		assertMoves(item, x -> add(x, MANY, t2));
		assertMoves(item, x -> remove(x, MANY, t1));
	}

	public void testDeletionOfLastReferenceValue() {
		TLObject item = create("a");
		TLObject t1 = create("t1");
		update(item, x -> add(x, MANY, t1));
		assertMoves(item, x -> remove(x, MANY, t1));
	}

	public void testI18N() {
		assertTrue("No storage descriptor for the translation table.",
			hasDescriptorForTable(I18NAttributeStorage.I18N_STORAGE_KO_TYPE));

		TLObject item = create("a");
		assertMovesWithoutOwnRow(item, x -> x.tUpdateByName(TITLE, i18n("Title")));
		assertMovesWithoutOwnRow(item, x -> x.tUpdateByName(TITLE, i18n("Other title")));
		assertMovesWithoutOwnRow(item, x -> x.tUpdateByName(TITLE, null));
	}

	/**
	 * Asserts that the given change moves the last change of the given object, while its own row
	 * is not changed.
	 */
	private void assertMovesWithoutOwnRow(TLObject item, Consumer<TLObject> change) {
		Revision ownRow = LifecycleStorageModified.lastUpdateRevision(item.tHandle());
		assertMoves(item, change);
		assertEquals("The owner's row must not be changed.", ownRow,
			LifecycleStorageModified.lastUpdateRevision(item.tHandle()));
	}

	public void testI18NChangeLog() {
		TLObject item = create("a");
		if (!hasHistory(item)) {
			// The change log requires historic versions.
			return;
		}
		update(item, x -> x.tUpdateByName(TITLE, i18n("Title")));
		ResKey newTitle = i18n("Other title");
		Revision changeRevision = update(item, x -> x.tUpdateByName(TITLE, newTitle));

		Collection<ChangeSet> log = new ChangeLogBuilder(_kb, ModelService.getApplicationModel())
			.setStartRev(changeRevision)
			.setStopRev(changeRevision)
			.build();
		Modification titleChange = null;
		for (ChangeSet cs : log) {
			for (Change change : cs.getChanges()) {
				if (change instanceof Update update && WrapperHistoryUtils.equalsUnversioned(update.getObject(), item)) {
					for (Modification modification : update.getModifications()) {
						if (modification.getPart().getName().equals(TITLE)) {
							titleChange = modification;
						}
					}
				}
			}
		}
		assertNotNull("No modification of the title reported.", titleChange);
		assertEquals(list(newTitle), list(titleChange.getNewValue().toArray()));
	}

	public void testUnrelatedChange() {
		TLObject item = create("a");
		TLObject other = create("other");
		assertStays(item, other, x -> x.tUpdateByName(NAME, "other-2"));
	}

	public void testChangeOfReferrer() {
		TLObject item = create("a");
		TLObject referrer = create("referrer");
		assertStays(item, referrer, x -> x.tUpdateByName(SINGLE, item));
		assertStays(item, referrer, x -> add(x, MANY, item));
		assertStays(item, referrer, x -> remove(x, MANY, item));
	}

	public void testComposition() {
		TLObject container = create("container");
		TLObject child = create("child");
		assertMoves(container, x -> add(x, CHILDREN, child));
		assertStays(container, child, x -> x.tUpdateByName(NAME, "child-2"));
	}

	/**
	 * A composition stored in the table of its parts: Adding, reordering, moving, and removing
	 * parts moves the container, and so does a change of a part itself.
	 */
	public void testInlineComposition() {
		ANode container = inTX(() -> TestTypesFactory.getInstance().createANode());
		Part p1 = inTX(() -> newPart("p1"));
		Part p2 = inTX(() -> newPart("p2"));

		assertMoves(container, x -> container.addCompositeList1(p1));
		assertMoves(container, x -> container.addCompositeList1(p2));
		assertMoves(container, x -> p1.setName("p1-2"));
		assertMoves(container, x -> container.setCompositeList1(list(p2, p1)));
		assertEquals(list(p2, p1), container.getCompositeList1());
		assertMoves(container, x -> {
			container.removeCompositeList1(p1);
			container.addCompositeList2(p1);
		});
		assertEquals(list(p2), container.getCompositeList1());
		assertEquals(list(p1), container.getCompositeList2());
		assertMoves(container, x -> container.removeCompositeList1(p2));
	}

	public void testHistoricObject() {
		TLObject item = create("a");
		if (!hasHistory(item)) {
			// No historic versions exist.
			return;
		}
		Revision before = update(item, x -> x.tUpdateByName(NAME, "b"));
		TLObject target = create("target");
		update(item, x -> x.tUpdateByName(SINGLE, target));

		TLObject historic = WrapperHistoryUtils.getWrapper(before, item);
		assertEquals(before, LastChangeRevision.of(historic));
	}

	private static boolean hasHistory(TLObject object) {
		return ((MOClass) object.tTable()).isVersioned();
	}

	private boolean hasDescriptorForTable(String tableName) {
		ModelTables modelTables = new ModelTables(ModelService.getApplicationModel());
		Map<MOStructure, List<AssociationStorageDescriptor>> storage = modelTables.lookupSeparateStorage(_itemType);
		return storage.keySet().stream().anyMatch(table -> table.getName().equals(tableName));
	}

	private void assertMoves(TLObject item, Consumer<TLObject> change) {
		Revision before = LastChangeRevision.of(item);
		Revision changeRevision = update(item, change);
		assertTrue(changeRevision.compareTo(before) > 0);
		assertEquals(changeRevision, LastChangeRevision.of(item));
	}

	private void assertStays(TLObject item, TLObject changed, Consumer<TLObject> change) {
		Revision before = LastChangeRevision.of(item);
		update(changed, change);
		assertEquals(before, LastChangeRevision.of(item));
	}

	private Revision update(TLObject item, Consumer<TLObject> change) {
		try (Transaction tx = beginTX()) {
			change.accept(item);
			tx.commit();
			return tx.getCommitRevision();
		}
	}

	private <T> T inTX(Supplier<T> fun) {
		try (Transaction tx = beginTX()) {
			T result = fun.get();
			tx.commit();
			return result;
		}
	}

	private static Part newPart(String name) {
		Part part = TestTypesFactory.getInstance().createPart();
		part.setName(name);
		return part;
	}

	private TLObject create(String name) {
		try (Transaction tx = beginTX()) {
			TLObject result = _factory.createObject(_itemType);
			result.tUpdateByName(NAME, name);
			tx.commit();
			return result;
		}
	}

	private static void add(TLObject item, String attribute, TLObject value) {
		List<Object> values = new ArrayList<>((Collection<?>) item.tValueByName(attribute));
		values.add(value);
		item.tUpdateByName(attribute, values);
	}

	private static void remove(TLObject item, String attribute, TLObject value) {
		List<Object> values = new ArrayList<>((Collection<?>) item.tValueByName(attribute));
		values.remove(value);
		item.tUpdateByName(attribute, values);
	}

	private static ResKey i18n(String text) {
		ResKey.Builder builder = ResKey.builder();
		for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
			builder.add(locale, text + " (" + locale + ")");
		}
		return builder.build();
	}

	public static Test suite() {
		return suite(TestLastChangeRevision.class);
	}

}

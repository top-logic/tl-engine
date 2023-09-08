/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.rewriter;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.element.model.util.TLModelTestUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ChangeSetReader;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.ReaderConfig;
import com.top_logic.knowledge.service.ReaderConfigBuilder;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.migration.rewriters.SetModelPartAnnotation;
import com.top_logic.knowledge.service.db2.migration.rewriters.SetModelPartAnnotation.Config;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;

/**
 * Tests {@link SetModelPartAnnotation}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestSetModelPartAnnotation extends TLModelTest {

	private static final String ANNOTATIONS_ATTRIBUTE = PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE;

	public void testUpdate() {
		String moduleName = "testUpdate";
		Transaction tx = begin();
		TLModule mod1 = addModule(moduleName);
		TLClass clazz = addClass(mod1, "clazz1");
		TLClassProperty part = addStringProperty(clazz, "part1");
		commit(tx);
		Transaction tx2 = begin();
		mod1.setAnnotation(i18N(ResKey.forTest("module-old")));
		clazz.setAnnotation(i18N(ResKey.forTest("clazz-old")));
		part.setAnnotation(i18N(ResKey.forTest("part-old")));
		commit(tx2);

		AnnotationConfigs newModuleAnnotations = newAnnotations("module-new");
		AnnotationConfigs newTypeAnnotations = newAnnotations("clazz-new");
		AnnotationConfigs newPartAnnotations = newAnnotations("part-new");

		CachingEventWriter out = rewrite(tx, moduleName, "clazz1", "part1", newPartAnnotations);
		ChangeSet cs = out.getAllEvents().get(0);
		assertNull(annotation(getCreation(cs, getId(mod1))));
		assertNull(annotation(getCreation(cs, getId(clazz))));
		assertConfigEquals(newPartAnnotations, annotation(getCreation(cs, getId(part))));
		cs = out.getAllEvents().get(1);
		assertConfigEquals(newAnnotations("module-old"), annotation(getUpdate(cs, getId(mod1))));
		assertConfigEquals(newAnnotations("clazz-old"), annotation(getUpdate(cs, getId(clazz))));
		assertNull(getUpdate(cs, getId(part)));

		out = rewrite(tx, moduleName, "clazz1", null, newTypeAnnotations);
		cs = out.getAllEvents().get(0);
		assertNull(annotation(getCreation(cs, getId(mod1))));
		assertConfigEquals(newTypeAnnotations, annotation(getCreation(cs, getId(clazz))));
		assertNull(annotation(getCreation(cs, getId(part))));
		cs = out.getAllEvents().get(1);
		assertConfigEquals(newAnnotations("module-old"), annotation(getUpdate(cs, getId(mod1))));
		assertNull(getUpdate(cs, getId(clazz)));
		assertConfigEquals(newAnnotations("part-old"), annotation(getUpdate(cs, getId(part))));

		out = rewrite(tx, moduleName, null, null, newModuleAnnotations);
		cs = out.getAllEvents().get(0);
		assertConfigEquals(newModuleAnnotations, annotation(getCreation(cs, getId(mod1))));
		assertNull(annotation(getCreation(cs, getId(clazz))));
		assertNull(annotation(getCreation(cs, getId(part))));
		cs = out.getAllEvents().get(1);
		assertNull(getUpdate(cs, getId(mod1)));
		assertConfigEquals(newAnnotations("clazz-old"), annotation(getUpdate(cs, getId(clazz))));
		assertConfigEquals(newAnnotations("part-old"), annotation(getUpdate(cs, getId(part))));
	}

	private ConfigurationItem annotation(ItemChange change) {
		return (ConfigurationItem) change.getValues().get(ANNOTATIONS_ATTRIBUTE);
	}

	public void testRewriteInSameCS() {
		String moduleName = "testRewriteInSameCS";
		Transaction tx = begin();
		TLModule mod1 = addModule(moduleName);
		mod1.setAnnotation(i18N(ResKey.forTest("module-old")));
		TLClass clazz = addClass(mod1, "clazz1");
		clazz.setAnnotation(i18N(ResKey.forTest("clazz-old")));
		TLClassProperty part = addStringProperty(clazz, "part1");
		part.setAnnotation(i18N(ResKey.forTest("part-old")));
		commit(tx);

		AnnotationConfigs partAnnotations = newAnnotations("part-new");
		CachingEventWriter out = rewrite(tx, moduleName, "clazz1", "part1", partAnnotations);
		ChangeSet cs = out.getAllEvents().get(0);
		ObjectCreation creation = getCreation(cs, getId(part));
		assertConfigEquals(partAnnotations, annotation(creation));

		AnnotationConfigs typeAnnotations = newAnnotations("clazz-new");
		out = rewrite(tx, moduleName, "clazz1", null, typeAnnotations);
		cs = out.getAllEvents().get(0);
		creation = getCreation(cs, getId(clazz));
		assertConfigEquals(typeAnnotations, annotation(creation));

		AnnotationConfigs moduleAnnotations = newAnnotations("module-new");
		out = rewrite(tx, moduleName, null, null, moduleAnnotations);
		cs = out.getAllEvents().get(0);
		creation = getCreation(cs, getId(mod1));
		assertConfigEquals(moduleAnnotations, annotation(creation));
	}

	private AnnotationConfigs newAnnotations(String i18N) {
		AnnotationConfigs moduleAnnotations = TypedConfiguration.newConfigItem(AnnotationConfigs.class);
		setAnnotation(moduleAnnotations, i18N(ResKey.forTest(i18N)));
		return moduleAnnotations;
	}

	public void testRewriteInDifferentCS() {
		String moduleName = "testRewriteInDifferentCS";
		Transaction tx = begin();
		TLModule mod1 = addModule(moduleName);
		commit(tx);
		Transaction tx1 = begin();
		TLClass clazz = addClass(mod1, "clazz1");
		clazz.setAnnotation(i18N(ResKey.forTest("clazz-old")));
		commit(tx1);
		Transaction tx2 = begin();
		TLClassProperty part = addStringProperty(clazz, "part1");
		part.setAnnotation(i18N(ResKey.forTest("part-old")));
		commit(tx2);

		AnnotationConfigs partAnnotations = newAnnotations("part-new");
		CachingEventWriter out = rewrite(tx, moduleName, "clazz1", "part1", partAnnotations);
		ChangeSet cs = out.getAllEvents().get(2);
		ObjectCreation creation = getCreation(cs, getId(part));
		assertConfigEquals(partAnnotations, annotation(creation));

		AnnotationConfigs typeAnnotations = newAnnotations("clazz-new");
		out = rewrite(tx, moduleName, "clazz1", null, typeAnnotations);
		cs = out.getAllEvents().get(1);
		creation = getCreation(cs, getId(clazz));
		assertConfigEquals(typeAnnotations, annotation(creation));
	}

	private CachingEventWriter rewrite(Transaction startTX, String moduleName, String className, String partName,
			AnnotationConfigs annotations) {
		SetModelPartAnnotation r = newRewriter(moduleName, className, partName, annotations);
		CachingEventWriter out = new CachingEventWriter();
		try (ChangeSetReader reader = getReader(startTX)) {
			ChangeSet cs;
			while ((cs = reader.read()) != null) {
				r.rewrite(cs, out);
			}
		}
		return out;
	}

	private ItemUpdate getUpdate(ChangeSet cs, ObjectBranchId id) {
		return getEvent(cs.getUpdates(), id);
	}

	private ObjectCreation getCreation(ChangeSet cs, ObjectBranchId id) {
		return getEvent(cs.getCreations(), id);
	}

	private <T extends ItemEvent> T getEvent(List<T> events, ObjectBranchId id) {
		return events.stream().filter(evt -> id.equals(evt.getObjectId())).findAny().orElse(null);
	}

	private ObjectBranchId getId(IdentifiedObject obj) {
		return ObjectBranchId.toObjectBranchId(obj.tId());
	}

	private void setAnnotation(AnnotationConfigs annotations, TLAnnotation annotation) {
		annotations.getAnnotations().add(annotation);
	}

	private TLI18NKey i18N(ResKey key) {
		TLI18NKey annotation = TypedConfiguration.newConfigItem(TLI18NKey.class);
		annotation.setValue(key);
		return annotation;
	}

	private SetModelPartAnnotation newRewriter(String module, String type, String part, AnnotationConfigs annotations) {
		Config config = TypedConfiguration.newConfigItem(SetModelPartAnnotation.Config.class);
		config.setModule(module);
		config.setType(type);
		config.setAttribute(part);
		config.setAnnotations(annotations);
		return TypedConfigUtil.createInstance(config);
	}

	private ChangeSetReader getReader(Transaction tx) {
		ReaderConfig config = ReaderConfigBuilder.createConfig(tx.getCommitRevision(), Revision.CURRENT);
		return getKnowledgeBase().getChangeSetReader(config);
	}

	private static void assertConfigEquals(ConfigurationItem expected, ConfigurationItem actual ) {
		assertConfigEquals(list(expected), list(actual));
	}

	@Override
	protected TLModel setUpModel() {
		TLModel model = TLModelTestUtil.createTLModelInTransaction(getKnowledgeBase());
		addCoreModule(model, setUpFactory());
		return model;
	}

	@Override
	protected TLFactory setUpFactory() {
		return new DefaultModelFactory();
	}

	@Override
	protected void tearDownModel() {
		/* nothing to do. Reverting the changes in the KnowledgeBase does not work, as reverting
		 * type-creations is not possible. Additionally, reverting slows down the test from 2
		 * seconds to 30 seconds and causes NPEs during the teardown, caused by MetaElements without
		 * scope. */
	}

	private KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private Transaction begin() {
		return getKnowledgeBase().beginTransaction();
	}

	private void commit(Transaction tx) {
		tx.commit();
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestSetModelPartAnnotation}.
	 */
	public static Test suite() {
		return TestUtils.doNotMerge(KBSetup.getSingleKBTest(TestSetModelPartAnnotation.class));
	}


}

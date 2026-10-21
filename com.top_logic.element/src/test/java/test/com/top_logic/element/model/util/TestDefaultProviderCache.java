/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.TLDefaultValue;
import com.top_logic.model.cache.TLModelCacheEntry;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations;
import com.top_logic.model.cache.TLModelOperations.PartDefault;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link TLModelOperations#getDefaultProviders(com.top_logic.model.TLStructuredType)} and
 * the default values it applies to created objects.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultProviderCache extends TLModelTest {

	private static final String MODULE = "testDefaultProviderCache";

	private static final String DEFAULT_VALUE = "hello";

	private Revision _preSetUpRevision;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_preSetUpRevision = HistoryUtils.getSessionRevision();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			extendModel(ClassRelativeBinaryContent.withSuffix(TestDefaultProviderCache.class, "model.xml"));
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		KBUtils.revert(kb(), _preSetUpRevision);
		super.tearDown();
	}

	public void testCacheActive() {
		assertTrue("The test requires the model cache.", cache() instanceof TLModelCacheEntry);
		List<PartDefault> first = cache().getDefaultProviders(concrete());
		assertSame("Default providers are not cached.", first, cache().getDefaultProviders(concrete()));
	}

	public void testOnlyAnnotatedParts() {
		assertEquals(List.of(attr(base(), "withDefault")), parts(cache().getDefaultProviders(concrete())));
	}

	public void testDerivedPartExcluded() {
		TLStructuredTypePart derived = attr(base(), "derived");
		assertTrue(derived.isDerived());
		assertNotNull("The derived attribute must be annotated to make the test meaningful.",
			DisplayAnnotations.getDefaultProvider(derived));

		assertEquals(List.of(attr(base(), "withDefault")), parts(cache().getDefaultProviders(base())));
	}

	public void testTransientObjectDefaults() {
		TLObject obj = TransientObjectFactory.INSTANCE.createObject(concrete());
		assertDefaults(obj);
	}

	public void testPersistentObjectDefaults() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject obj = getFactory().createObject(concrete());
			assertDefaults(obj);
			tx.rollback();
		}
	}

	private void assertDefaults(TLObject obj) {
		assertEquals(DEFAULT_VALUE, obj.tValueByName("withDefault"));
		assertNull(obj.tValueByName("plain"));
		assertNull(obj.tValueByName("other"));
		assertNull(obj.tValueByName("derived"));
	}

	public void testInvalidationOnAnnotationChange() {
		TLStructuredTypePart plain = attr(base(), "plain");
		TLStructuredTypePart withDefault = attr(base(), "withDefault");
		assertEquals(List.of(withDefault), parts(cache().getDefaultProviders(concrete())));

		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			plain.setAnnotation(TypedConfiguration.copy(withDefault.getAnnotation(TLDefaultValue.class)));
			tx.commit();
		}
		assertEquals(List.of(plain, withDefault), parts(cache().getDefaultProviders(concrete())));
		assertEquals(DEFAULT_VALUE, TransientObjectFactory.INSTANCE.createObject(concrete()).tValueByName("plain"));

		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			withDefault.removeAnnotation(TLDefaultValue.class);
			tx.commit();
		}
		assertEquals(List.of(plain), parts(cache().getDefaultProviders(concrete())));
		TLObject obj = TransientObjectFactory.INSTANCE.createObject(concrete());
		assertEquals(DEFAULT_VALUE, obj.tValueByName("plain"));
		assertNull(obj.tValueByName("withDefault"));
	}

	private static List<TLStructuredTypePart> parts(List<PartDefault> defaults) {
		return defaults.stream().map(PartDefault::part).toList();
	}

	private TLStructuredTypePart attr(TLClass type, String name) {
		return type.getPart(name);
	}

	private TLClass base() {
		return type(MODULE, "Base");
	}

	private TLClass concrete() {
		return type(MODULE, "Concrete");
	}

	private TLModelOperations cache() {
		return TLModelCacheService.getOperations();
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	@Override
	protected TLModel setUpModel() {
		return TLModelTestUtil.createTLModelInTransaction(kb());
	}

	@Override
	protected TLFactory setUpFactory() {
		return new DefaultModelFactory();
	}

	@Override
	protected void tearDownModel() {
		// Model changes are reverted in tearDown().
	}

	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestDefaultProviderCache.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}

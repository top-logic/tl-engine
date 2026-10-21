/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

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
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.cache.TLModelCacheEntry;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Test for {@link TLModelOperations#resolvePart(TLStructuredType, TLStructuredTypePart)} and its
 * caching in {@link TLModelCacheEntry}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestResolvePart extends TLModelTest {

	private static final String MODULE = "testResolvePart";

	private static final Pattern RESOLVED_PARTS_COUNT = Pattern.compile("resolvedParts = (\\d+)");

	private Revision _preSetUpRevision;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_preSetUpRevision = HistoryUtils.getSessionRevision();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			extendModel(ClassRelativeBinaryContent.withSuffix(TestResolvePart.class, "model.xml"));
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
	}

	public void testOverrideInActualType() {
		assertSame(attr(sub(), "a"), cache().resolvePart(sub(), attr(base(), "a")));
		assertNotSame(attr(base(), "a"), attr(sub(), "a"));
	}

	public void testInheritedPart() {
		assertSame(attr(base(), "b"), cache().resolvePart(sub(), attr(base(), "b")));
	}

	public void testOverrideAccessingSuperType() {
		assertSame(attr(base(), "a"), cache().resolvePart(base(), attr(sub(), "a")));
	}

	public void testOwnPart() {
		assertSame(attr(sub(), "a"), cache().resolvePart(sub(), attr(sub(), "a")));
	}

	public void testUnrelatedType() {
		assertNull(cache().resolvePart(other(), attr(base(), "a")));
	}

	public void testCached() {
		TLStructuredTypePart first = cache().resolvePart(sub(), attr(base(), "a"));
		int count = resolvedPartsCount();
		assertSame(first, cache().resolvePart(sub(), attr(base(), "a")));
		assertEquals(count, resolvedPartsCount());
	}

	public void testUnrelatedTypeCached() {
		int before = resolvedPartsCount();
		assertNull(cache().resolvePart(other(), attr(base(), "a")));
		int afterFirst = resolvedPartsCount();
		assertEquals("The result for an unrelated type is not cached.", before + 1, afterFirst);
		assertNull(cache().resolvePart(other(), attr(base(), "a")));
		assertEquals("The result for an unrelated type is computed again.", afterFirst, resolvedPartsCount());
	}

	public void testInvalidationOnModelChange() {
		TLStructuredTypePart baseB = attr(base(), "b");
		assertSame(baseB, cache().resolvePart(sub(), baseB));

		TLStructuredTypePart subB;
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			subB = TLModelUtil.addProperty(sub(), "b", baseB.getType());
			tx.commit();
		}
		assertSame(sub(), subB.getOwner());
		assertSame(baseB, subB.getDefinition());
		assertSame(subB, cache().resolvePart(sub(), baseB));
	}

	public void testReadThroughOverride() {
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject obj = getFactory().createObject(sub());
			obj.tUpdate(attr(sub(), "a"), "value");
			assertEquals("value", obj.tValue(attr(base(), "a")));
			assertEquals("value", obj.tValue(attr(sub(), "a")));
			tx.rollback();
		}
	}

	private int resolvedPartsCount() {
		Matcher matcher = RESOLVED_PARTS_COUNT.matcher(cache().toString());
		assertTrue("No resolved parts count in: " + cache(), matcher.find());
		return Integer.parseInt(matcher.group(1));
	}

	private TLStructuredTypePart attr(TLClass type, String name) {
		TLStructuredTypePart result = type.getPart(name);
		assertNotNull(result);
		return result;
	}

	private TLClass base() {
		return type(MODULE, "Base");
	}

	private TLClass sub() {
		return type(MODULE, "Sub");
	}

	private TLClass other() {
		return type(MODULE, "Other");
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
		Test modelTest = ServiceTestSetup.createSetup(TestResolvePart.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}

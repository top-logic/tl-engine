/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.i18n;

import static com.top_logic.knowledge.service.KBUtils.*;
import static java.util.Objects.*;

import java.util.Locale;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.LiteralKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.i18n.I18NAttributeStorage;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * {@link TestCase} for {@link I18NAttributeStorage}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestI18NAttributeStorage extends BasicTestCase {

	private static final Class<TestI18NAttributeStorage> THIS_CLASS = TestI18NAttributeStorage.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String TYPE_NAME = "I18NContainer";

	private static final String TITLE_ATTRIBUTE = "title";

	private static final String CONTENT_ATTRIBUTE = "content";

	private TLObject _tlObject;

	private Locale _fallbackLocale;

	/**
	 * One of the supported {@link Locale}s that is not the fallback {@link Locale}.
	 * <p>
	 * There is no need to repeat the tests for every non-fallback {@link Locale}. If they work for
	 * one, they should work for all.
	 * </p>
	 */
	private Locale _nonFallbackLocale;

	/**
	 * Tests that a {@link ResKey} that has only the fallback {@link Locale} set, is stored and
	 * retrieved with just the fallback {@link Locale} set.
	 */
	public void testFallbackLocale() {
		String originalText = "example";
		LiteralKey originalResKey = createResKey(_fallbackLocale, originalText);
		assertEquals(originalText, originalResKey.getTranslationWithoutFallbacks(_fallbackLocale));
		assertNull(originalResKey.getTranslationWithoutFallbacks(_nonFallbackLocale));
		inTransaction(() -> setTitle(originalResKey));
		LiteralKey storedResKey = getTitle();
		assertEquals(originalText, storedResKey.getTranslationWithoutFallbacks(_fallbackLocale));
		assertNull(storedResKey.getTranslationWithoutFallbacks(_nonFallbackLocale));
	}

	/**
	 * Tests that a {@link ResKey} that has only a non-fallback {@link Locale} set, is stored and
	 * retrieved with just that non-fallback {@link Locale} set.
	 */
	public void testNonFallbackLocale() {
		String originalText = "example";
		LiteralKey originalResKey = createResKey(_nonFallbackLocale, originalText);
		assertEquals(originalText, originalResKey.getTranslationWithoutFallbacks(_nonFallbackLocale));
		assertNull(originalResKey.getTranslationWithoutFallbacks(_fallbackLocale));
		inTransaction(() -> setTitle(originalResKey));
		LiteralKey storedResKey = getTitle();
		assertEquals(originalText, storedResKey.getTranslationWithoutFallbacks(_nonFallbackLocale));
		assertNull(storedResKey.getTranslationWithoutFallbacks(_fallbackLocale));
	}

	/** Test for Ticket #24969: Getting the value ignored changes in the current transaction. */
	public void testReadTransactionValue() {
		try (Transaction transaction = getKnowledgeBase().beginTransaction()) {
			String originalText = "example";
			ResKey newValue = createResKey(_fallbackLocale, originalText);
			setTitle(newValue);
			assertEquals(originalText, getTitle().getTranslationWithoutFallbacks(_fallbackLocale));
			transaction.rollback(); // No need to commit.
		}
	}

	public void testMultipleAttributes() {
		String originalTitleText = "The Title";
		ResKey originalTitleKey = createResKey(_fallbackLocale, originalTitleText);
		String originalContentText = "This is the content.";
		ResKey originalContentKey = createResKey(_fallbackLocale, originalContentText);
		inTransaction(() -> {
			setTitle(originalTitleKey);
			setContent(originalContentKey);
		});
		assertEquals(originalTitleText, getTitle().getTranslationWithoutFallbacks(_fallbackLocale));
		assertEquals(originalContentText, getContent().getTranslationWithoutFallbacks(_fallbackLocale));
	}

	private LiteralKey getTitle() {
		return (LiteralKey) _tlObject.tValueByName(TITLE_ATTRIBUTE);
	}

	private void setTitle(ResKey originalResKey) {
		_tlObject.tUpdateByName(TITLE_ATTRIBUTE, originalResKey);
	}

	private LiteralKey getContent() {
		return (LiteralKey) _tlObject.tValueByName(CONTENT_ATTRIBUTE);
	}

	private void setContent(ResKey originalResKey) {
		_tlObject.tUpdateByName(CONTENT_ATTRIBUTE, originalResKey);
	}

	private LiteralKey createResKey(Locale locale, String text) {
		return (LiteralKey) ResKey.builder().add(locale, text).build();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_fallbackLocale = requireNonNull(ResourcesModule.getInstance().getFallbackLocale());
		_nonFallbackLocale = requireNonNull(getNonFallbackLocale());
		assertNotEquals(_fallbackLocale, _nonFallbackLocale);
		inTransaction(this::createTestObject);
	}

	private Locale getNonFallbackLocale() {
		Set<String> supportedLanguages = set(ResourcesModule.getInstance().getSupportedLocaleNames());
		supportedLanguages.remove(_fallbackLocale.getLanguage());
		String nonDefaultLanguage = supportedLanguages.iterator().next();
		return new Locale(nonDefaultLanguage);
	}

	private void createTestObject() {
		_tlObject = instantiate(getType());
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(this::deleteTestObject);
		_fallbackLocale = null;
		_nonFallbackLocale = null;
		super.tearDown();
	}

	private void deleteTestObject() {
		_tlObject.tDelete();
		_tlObject = null;
	}

	private TLObject instantiate(TLClass type) {
		return DynamicModelService.getInstance().createObject(type, null, null);
	}

	private TLClass getType() {
		return (TLClass) getModule().getType(TYPE_NAME);
	}

	private TLModule getModule() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	private KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		Test customConfigSetup = createCustomConfigSetup(THIS_CLASS, kbSetup);
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

	private static Test createCustomConfigSetup(Class<?> testClass, Test innerSetup) {
		String configFileName = testClass.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(testClass, configFileName);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(innerSetup, createFilePath, true));
	}

}

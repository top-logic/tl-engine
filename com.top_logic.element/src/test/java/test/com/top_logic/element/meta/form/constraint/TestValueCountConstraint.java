/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.constraint;

import static com.top_logic.knowledge.service.KBUtils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLValueCount;
import com.top_logic.model.annotate.util.I18NConstants;
import com.top_logic.model.annotate.util.ValueCountConstraintCheck;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test for the {@link TLValueCount} annotation checked when an object is saved, and for the
 * {@link ValueCountConstraintCheck} checking a value in a form.
 */
@SuppressWarnings("javadoc")
public class TestValueCountConstraint extends BasicTestCase {

	private static final Class<TestValueCountConstraint> THIS_CLASS = TestValueCountConstraint.class;

	private static final String MODULE_NAME = THIS_CLASS.getSimpleName();

	private static final String PARENT_TYPE_NAME = "Parent";

	private static final String CHILD_TYPE_NAME = "Child";

	private static final String LIMITED_ATTRIBUTE_NAME = "limited";

	private static final String UNLIMITED_ATTRIBUTE_NAME = "unlimited";

	private static final int MAX = 3;

	private final List<TLObject> _created = new ArrayList<>();

	private TLObject _parent;

	private List<TLObject> _children;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		inTransaction(() -> {
			_parent = instantiate(PARENT_TYPE_NAME);
			_children = new ArrayList<>();
			for (int n = 0; n <= MAX; n++) {
				_children.add(instantiate(CHILD_TYPE_NAME));
			}
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> deleteAll(_created));
		_created.clear();
		super.tearDown();
	}

	public void testAnnotationRead() {
		assertEquals(Integer.valueOf(MAX), limited().getAnnotation(TLValueCount.class).getMax());
		assertNotNull(ValueCountConstraintCheck.forAttribute(limited()));
		assertNull(ValueCountConstraintCheck.forAttribute(unlimited()));
	}

	public void testMaximumStored() {
		inTransaction(() -> _parent.tUpdate(limited(), _children.subList(0, MAX)));
		assertEquals(MAX, size(_parent.tValue(limited())));
	}

	public void testBeyondMaximumRejected() {
		inTransaction(() -> _parent.tUpdate(limited(), _children.subList(0, MAX)));

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(ResKey.text("too many values"));
		try {
			_parent.tUpdate(limited(), _children);
			try {
				tx.commit();
				fail("More than " + MAX + " values must be rejected.");
			} catch (KnowledgeBaseException | TopLogicException ex) {
				ResKey problem = findProblem(ex);
				assertNotNull("No problem reported for too many values: " + ex, problem);
				assertEquals(List.of(MAX, MAX + 1), List.of(problem.arguments()));
			}
		} finally {
			tx.rollback();
		}
		assertEquals(MAX, size(_parent.tValue(limited())));
	}

	public void testWithoutAnnotationUnlimited() {
		inTransaction(() -> _parent.tUpdate(unlimited(), _children));
		assertEquals(MAX + 1, size(_parent.tValue(unlimited())));
	}

	public void testCheckValue() {
		ValueCountConstraintCheck check = new ValueCountConstraintCheck(MAX);
		assertNull(check.checkValue(null));
		assertNull(check.checkValue(List.of()));
		assertNull(check.checkValue(_children.subList(0, MAX)));
		assertNotNull(check.checkValue(_children));
	}

	/**
	 * The {@link I18NConstants#ERROR_TOO_MANY_VALUES__MAX_ACTUAL} problem reported in the causes of
	 * the given exception, also as argument of their messages.
	 */
	private static ResKey findProblem(Throwable ex) {
		String key = I18NConstants.ERROR_TOO_MANY_VALUES__MAX_ACTUAL.getKey();
		for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
			if (cause instanceof I18NFailure failure) {
				ResKey found = findKey(failure.getErrorKey(), key);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static ResKey findKey(ResKey resKey, String key) {
		if (key.equals(resKey.getKey())) {
			return resKey;
		}
		Object[] arguments = resKey.arguments();
		if (arguments != null) {
			for (Object argument : arguments) {
				if (argument instanceof ResKey nested) {
					ResKey found = findKey(nested, key);
					if (found != null) {
						return found;
					}
				}
			}
		}
		return null;
	}

	private static int size(Object value) {
		return ((Collection<?>) value).size();
	}

	private TLStructuredTypePart limited() {
		return part(LIMITED_ATTRIBUTE_NAME);
	}

	private TLStructuredTypePart unlimited() {
		return part(UNLIMITED_ATTRIBUTE_NAME);
	}

	private static TLStructuredTypePart part(String name) {
		return type(PARENT_TYPE_NAME).getPart(name);
	}

	private TLObject instantiate(String typeName) {
		TLObject result = DynamicModelService.getInstance().createObject(type(typeName));
		_created.add(result);
		return result;
	}

	private static TLClass type(String typeName) {
		return (TLClass) module().getType(typeName);
	}

	private static TLModule module() {
		return ModelService.getInstance().getModel().getModule(MODULE_NAME);
	}

	public static Test suite() {
		Test kbSetup = KBSetup.getSingleKBTest(THIS_CLASS);
		String configFileName = THIS_CLASS.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(THIS_CLASS, configFileName);
		Test customConfigSetup = TestUtils.doNotMerge(new CustomPropertiesSetup(kbSetup, createFilePath, true));
		return TLTestSetup.createTLTestSetup(customConfigSetup);
	}

}

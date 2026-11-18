/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.overlay;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.model.util.TLModelTest;
import test.com.top_logic.element.model.util.TLModelTestUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.FormObjectOverlay;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.model.ModelService;

/**
 * Test for a {@link FormObjectOverlay} of a transient object whose type overrides an attribute: the
 * attribute and its override share one {@link AttributeUpdate}, whichever of them it is looked up
 * with.
 *
 * <p>
 * The interface <code>Base</code> defines the attribute <code>value</code>, which the class
 * <code>Derived</code> overrides. The class <code>Foreign</code> has an unrelated attribute of the
 * same name.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFormObjectOverlayOverride extends TLModelTest {

	private static final String MODULE = "testFormObjectOverlayOverride";

	private static final String VALUE = "value";

	private static final String OTHER = "other";

	private TLStructuredTypePart _baseValue;

	private TLStructuredTypePart _derivedValue;

	private TLStructuredTypePart _other;

	private TLStructuredTypePart _foreignValue;

	private AttributeFormContext _form;

	private TLFormObject _overlay;

	private Revision _preSetUpRevision;

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

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_preSetUpRevision = HistoryUtils.getSessionRevision();
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			extendModel(ClassRelativeBinaryContent.withSuffix(TestFormObjectOverlayOverride.class, "model.xml"));
			tx.commit();
		}

		TLClass derived = type(MODULE + ":Derived");
		_baseValue = type(MODULE + ":Base").getPart(VALUE);
		_derivedValue = derived.getPart(VALUE);
		_other = derived.getPart(OTHER);
		_foreignValue = type(MODULE + ":Foreign").getPart(VALUE);
		assertSame("The attribute of the class overrides the attribute of the interface.", _baseValue,
			_derivedValue.getDefinition());

		TLObject object = TransientObjectFactory.INSTANCE.createObject(derived, null, null);
		_form = new AttributeFormContext(ResPrefix.forTest(MODULE));
		_overlay = _form.editObject(object);
	}

	@Override
	protected void tearDown() throws Exception {
		_baseValue = null;
		_derivedValue = null;
		_other = null;
		_foreignValue = null;
		_overlay = null;
		_form = null;
		KBUtils.revert(kb(), _preSetUpRevision);
		super.tearDown();
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * An update created for the override is found with the overridden attribute and stays bound to
	 * the override.
	 */
	public void testUpdateFoundThroughDefinition() {
		AttributeUpdate update = _overlay.newEditUpdateDefault(_derivedValue, false);

		assertSame(update, _overlay.getUpdate(_baseValue));
		assertSame(update, _overlay.getUpdate(_derivedValue));
		assertSame("The update applies the annotations of the override.", _derivedValue, update.getAttribute());
	}

	/**
	 * An update requested for the overridden attribute is bound to the override in the type of the
	 * object, and requesting it again through the override delivers the same update.
	 */
	public void testUpdateCreatedThroughDefinition() {
		AttributeUpdate update = _overlay.newEditUpdateDefault(_baseValue, false);

		assertSame(_derivedValue, update.getAttribute());
		assertSame(update, _overlay.newEditUpdateDefault(_derivedValue, false));
	}

	/**
	 * The value of the update of the override is the value of the overridden attribute, and a
	 * value written through the overridden attribute is the value of the override.
	 */
	public void testValueSharedWithDefinition() {
		_overlay.newEditUpdateDefault(_derivedValue, false);

		_overlay.tUpdate(_derivedValue, "v1");
		assertEquals("v1", _overlay.tValue(_baseValue));

		_overlay.tUpdate(_baseValue, "v2");
		assertEquals("v2", _overlay.tValue(_derivedValue));
	}

	/**
	 * The value entered in the field of the override is the value of the overridden attribute.
	 */
	public void testFieldValueReadThroughDefinition() {
		AttributeUpdate update = _overlay.newEditUpdateDefault(_derivedValue, false);
		FormField field =
			(FormField) AttributeFormFactory.getInstance().toFormMember(update, _form.getAttributeUpdateContainer());

		field.setValue("entered");

		assertSame(field, _overlay.getField(_baseValue));
		assertEquals("entered", _overlay.tValue(_baseValue));
	}

	/**
	 * A callback waiting with the overridden attribute receives the update created later for the
	 * override.
	 */
	public void testWithUpdateThroughDefinition() {
		List<AttributeUpdate> received = new ArrayList<>();
		_overlay.withUpdate(_baseValue, received::add);
		assertEquals(List.of(), received);

		AttributeUpdate update = _overlay.newEditUpdateDefault(_derivedValue, false);

		assertEquals(List.of(update), received);
	}

	/**
	 * Removing the update through the overridden attribute removes the update of the override.
	 */
	public void testRemoveUpdateThroughDefinition() {
		AttributeUpdate update = _overlay.newEditUpdateDefault(_derivedValue, false);

		assertSame(update, _overlay.removeUpdate(_baseValue));
		assertNull(_overlay.getUpdate(_derivedValue));
	}

	/**
	 * Attributes without a common definition have updates of their own.
	 */
	public void testOtherAttributesUnaffected() {
		_overlay.newEditUpdateDefault(_derivedValue, false);

		assertNull(_overlay.getUpdate(_other));
		assertNull("An attribute of the same name of another type is no attribute of the object.",
			_overlay.getUpdate(_foreignValue));
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		Test modelTest = ServiceTestSetup.createSetup(TestFormObjectOverlayOverride.class, ModelService.Module.INSTANCE);
		Test kbTest = KBSetup.getSingleKBTest(modelTest);
		return TestUtils.doNotMerge(kbTest);
	}

}

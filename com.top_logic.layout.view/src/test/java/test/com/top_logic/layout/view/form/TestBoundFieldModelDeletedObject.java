/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;
import test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioConstants;
import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.BObj;

import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.view.form.AttributeFieldModel;
import com.top_logic.layout.view.form.BoundFieldModel;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests a {@link BoundFieldModel} whose bound object is deleted in the knowledge base.
 *
 * <p>
 * Deleting an object delivers its model event while the form displaying it still binds its fields
 * to it. A control redrawing itself then asks its field for the value: the field must answer without
 * accessing the deleted object, which fails.
 * </p>
 */
public class TestBoundFieldModelDeletedObject extends AbstractDBKnowledgeBaseTest {

	/** The attribute the field is bound to, named like the persistent attribute of a {@link BObj}. */
	private TLStructuredTypePart _part;

	/** The value changes the field fired. */
	private List<Object> _changes;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.form");
		TLClass type = TLModelUtil.addClass(module, "B");
		_part = TLModelUtil.addProperty(type, KnowledgeBaseTestScenarioConstants.A1_NAME, type);

		_changes = new ArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		_changes = null;
		_part = null;

		super.tearDown();
	}

	/**
	 * Tests that a field bound to a deleted object keeps the value it last showed.
	 */
	public void testDeletedObjectKeepsLastValue() throws Exception {
		BObj b1 = create("b1");
		AttributeFieldModel field = field(b1);

		delete(b1);

		assertEquals("A field over a deleted object shows the value it last showed.", "b1", field.getValue());
	}

	/**
	 * Tests that refreshing a field bound to a deleted object changes nothing.
	 */
	public void testDeletedObjectIsNotRefreshed() throws Exception {
		BObj b1 = create("b1");
		AttributeFieldModel field = field(b1);

		delete(b1);
		field.refreshFromObject();

		assertEquals("A deleted object holds no value to be shown.", List.of(), _changes);
		assertEquals("The field keeps its value.", "b1", field.getValue());
	}

	/**
	 * Tests that an edit of a field bound to a deleted object is dropped.
	 */
	public void testEditOfDeletedObjectIsDropped() throws Exception {
		BObj b1 = create("b1");
		AttributeFieldModel field = field(b1);

		delete(b1);
		field.setValue("edited");

		assertEquals("An edit of a deleted object changes nothing.", List.of(), _changes);
		assertEquals("The field keeps its value.", "b1", field.getValue());
	}

	/**
	 * Tests that a field in edit mode, bound to a {@link TLObjectOverlay} of an object that is
	 * deleted, keeps the value it last showed: the overlay reads unchanged attributes from the
	 * deleted object.
	 */
	public void testOverlayOfDeletedObjectKeepsLastValue() throws Exception {
		BObj b1 = create("b1");
		TLObjectOverlay overlay = new TLObjectOverlay(b1);
		AttributeFieldModel field = field(overlay);

		delete(b1);
		field.refreshFromObject();

		assertEquals("A field over an overlay of a deleted object shows the value it last showed.", "b1",
			field.getValue());
		assertEquals("A deleted object holds no value to be shown.", List.of(), _changes);
	}

	/**
	 * Tests that a field bound to a live object shows a change of that object: the deleted-object
	 * behavior does not keep a field from following its object.
	 */
	public void testLiveObjectIsRefreshed() throws Exception {
		BObj b1 = create("b1");
		AttributeFieldModel field = field(b1);

		change(b1, "b1 updated");

		assertEquals("A field over a live object reads the value it holds.", "b1 updated", field.getValue());

		field.refreshFromObject();

		assertEquals("Refreshing shows the changed value.", List.of("b1 updated"), _changes);
	}

	/**
	 * A field bound to the attribute {@link #_part} of the given object, recording its value
	 * changes in {@link #_changes}.
	 */
	private AttributeFieldModel field(TLObject object) {
		AttributeFieldModel result = new AttributeFieldModel(object, _part);
		result.addListener(new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				_changes.add(newValue);
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// Not recorded.
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				// Not recorded.
			}
		});
		return result;
	}

	private BObj create(String name) throws Exception {
		BObj result;
		Transaction tx = begin();
		try {
			result = BObj.newBObj(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
		return result;
	}

	private void change(BObj object, String name) throws Exception {
		Transaction tx = begin();
		try {
			object.setA1(name);
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	private void delete(BObj object) throws Exception {
		Transaction tx = begin();
		try {
			object.tDelete();
			tx.commit();
		} finally {
			tx.rollback();
		}
		assertFalse("The object is deleted.", object.tValid());
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestBoundFieldModelDeletedObject.class);
	}

}

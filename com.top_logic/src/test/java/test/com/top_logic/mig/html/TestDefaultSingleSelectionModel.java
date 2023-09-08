/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mig.html;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.component.model.SelectionListener;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * The class {@link TestDefaultSingleSelectionModel} tests {@link DefaultSingleSelectionModel}
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDefaultSingleSelectionModel extends TestCase {

	static class Listener implements SelectionListener {

		Set<?> _formerlySelectedObjects;

		Set<?> _selectedObjects;

		@Override
		public void notifySelectionChanged(SelectionModel model, Set<?> formerlySelectedObjects, Set<?> selectedObjects) {
			assertNotNull(formerlySelectedObjects);
			assertNotNull(selectedObjects);
			_formerlySelectedObjects = formerlySelectedObjects;
			_selectedObjects = selectedObjects;
		}

		void reset() {
			_formerlySelectedObjects = null;
			_selectedObjects = null;
		}
	}

	private DefaultSingleSelectionModel _model;

	private Listener _l;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_model = new DefaultSingleSelectionModel(SelectionModelOwner.NO_OWNER);
		_l = new Listener();
		_model.addSelectionListener(_l);
	}

	public void testSetSelection() {
		Integer selected = 5;
		_model.setSelected(selected, true);
		assertTrue(_model.isSelected(selected));
		assertEquals(set(), _l._formerlySelectedObjects);
		assertEquals(set(5), _l._selectedObjects);
		_l.reset();
		
		Integer newSelected = 7;
		_model.setSelection(set(newSelected));
		assertFalse(_model.isSelected(selected));
		assertTrue(_model.isSelected(newSelected));
		assertEquals(set(5), _l._formerlySelectedObjects);
		assertEquals(set(7), _l._selectedObjects);
		_l.reset();

		_model.setSelection(set(newSelected));
		assertNull(_l._formerlySelectedObjects);
		assertNull(_l._selectedObjects);

		_model.setSelection(set());
		assertFalse(_model.isSelected(newSelected));
		assertEquals(set(7), _l._formerlySelectedObjects);
		assertEquals(set(), _l._selectedObjects);
		_l.reset();
	}

	public void testCannotChangeFixedSelection() {
		final Object fixedSelection = new Object();
		_model.setSelected(fixedSelection, true);
		_model.setDeselectionFilter(new Filter<Object>() {

			@Override
			public boolean accept(Object anObject) {
				return anObject != fixedSelection;
			}
		});

		Object newSelection = new Object();

		_model.setSelected(newSelection, true);
		assertEquals(fixedSelection, _model.getSingleSelection());

		_model.setSelected(fixedSelection, false);
		assertEquals(fixedSelection, _model.getSingleSelection());
	}

	public void testAcceptNoNewSelection() {
		final Object fixedSelection = new Object();
		_model.setSelected(fixedSelection, true);
		_model.setDeselectionFilter(new Filter<Object>() {

			@Override
			public boolean accept(Object anObject) {
				return anObject != fixedSelection;
			}
		});

		Object newSelection = new Object();
		assertFalse(_model.isSelectable(newSelection));
	}

	public void testCannotClearFixedSelection() {
		final Object fixedSelection = new Object();
		_model.setSelected(fixedSelection, true);
		_model.setDeselectionFilter(new Filter<Object>() {

			@Override
			public boolean accept(Object anObject) {
				return anObject != fixedSelection;
			}
		});

		_model.clear();
		assertEquals(fixedSelection, _model.getSingleSelection());
	}

}


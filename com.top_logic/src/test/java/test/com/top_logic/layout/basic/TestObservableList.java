/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.layout.basic.ObservableList;
import com.top_logic.layout.component.model.ModelChangeListener;

/**
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class TestObservableList extends BasicTestCase {

	protected abstract ObservableList createObservableList();

	protected List beforeChange, afterChange;

	protected final ModelChangeListener modelChangeListener = new ModelChangeListener() {

		@Override
		public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
			beforeChange = (List) oldModel;
			afterChange = (List) newModel;
			return Bubble.BUBBLE;
		}

	};

	protected final ObservableList createObservableList(Collection listeners) {
		ObservableList observableList = createObservableList();
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			observableList.addModelChangedListener((ModelChangeListener) it.next());
		}
		return observableList;
	}

	public void testModelChangeListener() {
		ObservableList observableList = createObservableList();
		ModelChangeListener listener = new ModelChangeListener() {

			@Override
			public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
				throw new RuntimeException();
			}

		};

		observableList.addModelChangedListener(listener);
		try {
			observableList.add(new Object());
			fail("the given Listener was not informed about a adding a new object.");
		} catch (RuntimeException ex) {
			// expected
		}

		observableList.removeModelChangedListener(listener);
		try {
			observableList.add(new Object());
		} catch (RuntimeException ex) {
			throw new AssertionFailedError("the given listener was informed about adding a new object, but it had been removed as listener.");
		}
	}

	public void testAdd() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object = new Object();
		observableList.add(object);
		assertTrue(beforeChange.isEmpty());
		assertTrue(afterChange.contains(object));
	}

	public void testAddAtIndex() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object = new Object();
		Object object2 = new Object();
		observableList.add(object);
		observableList.add(0, object2);
		assertTrue(afterChange.get(0) == object2);
		assertTrue(afterChange.get(1) == object);
	}

	public void testAddAll() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Set all = new HashSet();
		all.add(new Object());
		all.add(new Object());
		all.add(new Object());

		Object object = new Object();
		observableList.add(object);
		observableList.addAll(all);
		assertEquals(beforeChange, Collections.singletonList(object));
		assertTrue(afterChange.containsAll(all));
	}

	public void testAddAllAt() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Set all = new HashSet();
		all.add(new Object());
		all.add(new Object());
		all.add(new Object());

		Object object = new Object();
		observableList.add(object);
		observableList.add(object);
		observableList.addAll(1, all);
		assert (new HashSet(afterChange.subList(1, 1 + all.size())).equals(all));
	}

	public void testRemoveObject() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		observableList.add(object1);
		List oldList = this.afterChange;
		Object object2 = new Object();
		observableList.remove(object2);
		assertEquals(oldList, this.afterChange);
		observableList.remove(object1);
		assertEquals(Collections.EMPTY_LIST, this.afterChange);

		observableList.add(object1);
		observableList.add(object1);
		observableList.remove(object1);
		assertTrue(this.afterChange.contains(object1));
	}

	public void testRemoveAt() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		List shadow = new ArrayList(2);
		observableList.add(object1);
		shadow.add(object1);
		observableList.add(object2);
		observableList.add(object3);
		shadow.add(object3);
		observableList.remove(1);
		assertEquals(shadow, afterChange);
	}

	public void testRemoveAll() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		Set shadow = new HashSet();
		shadow.add(object1);
		shadow.add(object3);
		observableList.add(object1);
		observableList.add(object2);
		observableList.add(object3);

		observableList.removeAll(shadow);
		assertTrue(beforeChange.containsAll(shadow));
		for (Iterator i = shadow.iterator(); i.hasNext();) {
			assertFalse(afterChange.contains(i.next()));
		}
	}

	public void testClear() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		observableList.add(object1);
		observableList.add(object2);
		observableList.add(object3);

		observableList.clear();
		assertTrue(afterChange.isEmpty());
		assertTrue(beforeChange.contains(object1));
		assertTrue(beforeChange.contains(object2));
		assertTrue(beforeChange.contains(object3));
	}

	public void testRetainAll() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		observableList.add(object1);
		observableList.add(object2);
		observableList.add(object3);

		observableList.retainAll(Collections.singleton(object2));

		assertEquals(afterChange, Collections.singletonList(object2));
	}

	public void testSet() {
		ObservableList observableList = createObservableList(Collections.singleton(modelChangeListener));
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		Object object4 = new Object();
		observableList.add(object1);
		observableList.add(object2);
		observableList.add(object3);

		observableList.set(1, object4);
		assertTrue(beforeChange.get(1) == object2);
		assertTrue(afterChange.get(1) == object4);
	}

	public void testEquals() {
		ObservableList observableList1 = createObservableList(Collections.singleton(modelChangeListener));

		Set listeners = new HashSet();
		listeners.add(new ModelChangeListener() {

			@Override
			public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
				return Bubble.BUBBLE;
			}

		});
		listeners.add(new ModelChangeListener() {

			@Override
			public Bubble modelChanged(Object sender, Object oldModel, Object newModel) {
				return Bubble.BUBBLE;
			}

		});
		ObservableList observableList2 = createObservableList(listeners);
		Object object1 = new Object();
		Object object2 = new Object();
		Object object3 = new Object();
		observableList1.add(object1);
		observableList1.add(object2);
		observableList1.add(object3);
		observableList2.add(object1);
		observableList2.add(object2);
		assertFalse(observableList1.equals(observableList2));

		observableList2.add(object3);
		assertTrue(observableList1.equals(observableList2));
		assertEquals(observableList1.hashCode(), observableList2.hashCode());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		beforeChange = null;
		afterChange = null;
	}

}

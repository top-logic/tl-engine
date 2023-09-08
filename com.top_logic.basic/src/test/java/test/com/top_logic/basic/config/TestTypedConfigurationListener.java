/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.ExpectedFailure;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationChange.Kind;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.io.character.CharacterContents;

/**
 * Test case for {@link ConfigurationListener} on {@link ConfigurationItem}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTypedConfigurationListener extends AbstractTypedConfigurationTestCase {

	public interface A extends ConfigurationItem {
	
		String X = "x";

		String Y = "y";

		String L = "l";

		String M = "m";

		String INDEXED_LIST = "indexed-list";
	
		@Name(X)
		int getX();
	
		void setX(int value);
	
		@Name(Y)
		int getY();
	
		void setY(int value);
	
		@Name(L)
		List<A> getL();

		void setL(List<A> value);

		@Name(INDEXED_LIST)
		@Key(X)
		List<A> getIndexedList();

		void setIndexedList(List<A> value);

		@Name(M)
		@Key(X)
		Map<Integer, A> getM();

		void setM(Map<Integer, A> value);

	}

	abstract class Observer implements ConfigurationListener {
		Map<PropertyDescriptor, List<Object>> _updates = new HashMap<>();

		public List<Object> getValues(PropertyDescriptor property) {
			List<Object> result = _updates.get(property);
			if (result == null) {
				return Collections.emptyList();
			}
			return result;
		}

		protected boolean hasValues(ConfigurationChange change) {
			PropertyDescriptor property = change.getProperty();
			List<Object> values = _updates.get(property);
			return values != null;
		}

		protected List<Object> getValues(ConfigurationChange change) {
			PropertyDescriptor property = change.getProperty();
			List<Object> values = _updates.get(property);
			if (values == null) {
				values = new ArrayList<>();
				initValues(change, values);

				_updates.put(property, values);
			}
			return values;
		}

		/**
		 * Called, whenever the first event for a property is received.
		 * 
		 * @param change
		 *        The change.
		 * @param values
		 *        The fresh values list.
		 */
		protected void initValues(ConfigurationChange change, List<Object> values) {
			// Hook for subclasses.
		}
	}

	final class ValueObserver extends Observer {
		@Override
		public void onChange(ConfigurationChange change) {
			if (change.getKind() != ConfigurationChange.Kind.SET) {
				return;
			}

			getValues(change).add(change.getNewValue());
		}

		@Override
		protected void initValues(ConfigurationChange change, List<Object> values) {
			values.add(change.getOldValue());
		}
	}

	final class ListObserver extends Observer {
		@Override
		public void onChange(ConfigurationChange change) {
			Kind kind = change.getKind();
			if (kind == ConfigurationChange.Kind.ADD) {
				if (hasValues(change)) {
					getValues(change).add(change.getIndex(), change.getNewValue());
				} else {
					// Only initialize values.
					getValues(change);
				}
			}
			else if (kind == ConfigurationChange.Kind.REMOVE) {
				if (hasValues(change)) {
					Object oldValue = change.getOldValue();
					assertTrue("Old value not found: " + oldValue, getValues(change).remove(oldValue));
				} else {
					// Only initialize values.
					getValues(change);
				}
			}
		}

		@Override
		protected void initValues(ConfigurationChange change, List<Object> values) {
			values.addAll((Collection<?>) change.getModel().value(change.getProperty()));
		}
	}

	final class MapObserver extends Observer {
		@Override
		public void onChange(ConfigurationChange change) {
			Kind kind = change.getKind();
			if (kind == ConfigurationChange.Kind.ADD) {
				if (hasValues(change)) {
					getValues(change).add(change.getNewValue());
				} else {
					getValues(change);
				}
			}
			else if (kind == ConfigurationChange.Kind.REMOVE) {
				if (hasValues(change)) {
					Object oldValue = change.getOldValue();
					assertTrue("Old value not found: " + oldValue, getValues(change).remove(oldValue));
				} else {
					getValues(change);
				}
			}
		}

		@Override
		protected void initValues(ConfigurationChange change, List<Object> values) {
			values.addAll(((Map<?, ?>) change.getModel().value(change.getProperty())).values());
		}
	}

	public static class AssertNoEvents implements ConfigurationListener {
		@Override
		public void onChange(ConfigurationChange change) {
			fail("Unexpected change event: " + change);
		}
	}

	public void testListening() {
		A a1 = newA();
		
		Observer listener = new ValueObserver();
		PropertyDescriptor x = a1.descriptor().getProperty(A.X);
		PropertyDescriptor y = a1.descriptor().getProperty(A.Y);
		a1.addConfigurationListener(x, listener);
		
		a1.setX(1);
		a1.setX(2);
		a1.setY(3);

		assertEquals(list(0, 1, 2), listener.getValues(x));
		assertEquals(list(), listener.getValues(y));
	}

	public void testInitialValue() {
		A a1 = newRawA();

		Observer listener = new ValueObserver();
		PropertyDescriptor x = a1.descriptor().getProperty(A.X);
		a1.addConfigurationListener(x, listener);

		a1.setX(1);

		assertEquals(1, listener.getValues(x).get(1));
	}

	public void testListeningAll() {
		A a1 = newA();

		Observer listener = new ValueObserver();
		PropertyDescriptor x = a1.descriptor().getProperty(A.X);
		PropertyDescriptor y = a1.descriptor().getProperty(A.Y);
		a1.addConfigurationListener(null, listener);

		a1.setX(0);
		a1.setX(1);
		a1.setX(1);
		a1.setX(2);
		a1.setX(2);
		a1.setY(3);

		assertEquals(list(0, 1, 2), listener.getValues(x));
		assertEquals(list(0, 3), listener.getValues(y));
	}

	public void testListeningList() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);

		a1.getL().add(l1);
		a1.getL().add(l2);
		a1.getL().add(l3);
		assertEquals(list(l1, l2, l3), listener.getValues(l));

		a1.getL().remove(1);
		assertEquals(list(l1, l3), listener.getValues(l));

		a1.removeConfigurationListener(l, listener);

		a1.getL().add(l2);
		a1.getL().remove(l1);
		assertEquals(list(l1, l3), listener.getValues(l));
	}

	public void testListNoOp() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);

		a1.setL(list(l1, l2, l3));

		assertEquals(list(l1, l2, l3), listener.getValues(l));

		a1.setL(list(l1, l3));

		assertEquals(list(l1, l3), listener.getValues(l));

		a1.removeConfigurationListener(l, listener);

		a1.addConfigurationListener(l, new AssertNoEvents());
		a1.setL(list(l1, l3));
	}

	public void testListeningMap() {
		A a1 = newA();
	
		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);
	
		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);
	
		putM(a1, l1);
		putM(a1, l2);
		putM(a1, l3);
		assertEquals(list(l1, l2, l3), listener.getValues(m));
	
		a1.getM().remove(2);
		assertEquals(list(l1, l3), listener.getValues(m));
	
		a1.removeConfigurationListener(m, listener);
	
		putM(a1, l2);
		a1.getM().remove(1);
		assertEquals(list(l1, l3), listener.getValues(m));
	}

	public void testMapNoOp() {
		A a1 = newA();

		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);

		putAllM(a1, list(l1, l2, l3));
		assertEquals(list(l1, l2, l3), listener.getValues(m));

		putAllM(a1, list(l1, l3));
		assertEquals(list(l1, l3), listener.getValues(m));

		a1.removeConfigurationListener(m, listener);

		a1.addConfigurationListener(m, new AssertNoEvents());
		putAllM(a1, list(l1, l3));
	}

	public void testListeningUpdatedList() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);

		a1.setL(Arrays.asList(l1, l2, l3));

		assertEquals(list(l1, l2, l3), a1.getL());
		assertEquals(list(l1, l2, l3), listener.getValues(l));

		a1.setL(Arrays.asList(l2));

		assertEquals(list(l2), a1.getL());
		assertEquals(list(l2), listener.getValues(l));
	}

	public void testListeningUpdatedMap() {
		A a1 = newA();
	
		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);
	
		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);
	
		a1.setM(map(l1, l2, l3));
	
		assertValues(list(l1, l2, l3), a1.getM());
		assertEqualsContent(list(l1, l2, l3), listener.getValues(m));
	
		a1.setM(map(l2));
	
		assertValues(list(l2), a1.getM());
		assertEqualsContent(list(l2), listener.getValues(m));
	}

	public void testListeningListAddAll() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);
		A l4 = newA(4);
		A l5 = newA(5);

		a1.getL().addAll(Arrays.asList(l1, l2, l5));

		assertEquals(list(l1, l2, l5), a1.getL());
		assertEquals(list(l1, l2, l5), listener.getValues(l));

		a1.getL().addAll(2, Arrays.asList(l3, l4));

		assertEquals(list(l1, l2, l3, l4, l5), a1.getL());
		assertEquals(list(l1, l2, l3, l4, l5), listener.getValues(l));

		a1.getL().addAll(2, Collections.<A> emptyList());

		assertEquals(list(l1, l2, l3, l4, l5), a1.getL());
		assertEquals(list(l1, l2, l3, l4, l5), listener.getValues(l));

		a1.getL().removeAll(list(l2, l3, l4));

		assertEquals(list(l1, l5), a1.getL());
		assertEquals(list(l1, l5), listener.getValues(l));
	}

	public void testListeningMapPutAll() {
		A a1 = newA();
	
		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);
	
		A l1 = newA(1);
		A l2 = newA(2);
		A l3 = newA(3);
		A l4 = newA(4);
		A l5 = newA(5);
	
		a1.getM().putAll(map(l1, l2, l5));
	
		assertValues(list(l1, l2, l5), a1.getM());
		assertEqualsContent(list(l1, l2, l5), listener.getValues(m));
	
		a1.getM().putAll(map(l3, l4));
	
		assertValues(list(l1, l2, l3, l4, l5), a1.getM());
		assertEqualsContent(list(l1, l2, l3, l4, l5), listener.getValues(m));
	
		a1.getM().putAll(map());
	
		assertValues(list(l1, l2, l3, l4, l5), a1.getM());
		assertEqualsContent(list(l1, l2, l3, l4, l5), listener.getValues(m));
	
		a1.getM().values().removeAll(list(l2, l3, l4));
	
		assertValues(list(l1, l5), a1.getM());
		assertEqualsContent(list(l1, l5), listener.getValues(m));
	}

	public void testListeningLoadedList() {
		A a1 = newA(null, "<a y='0'><l><entry x='1' y='1'/><entry x='2' y='1'/><entry x='3' y='2'/></l></a>");

		doObserveL(a1);
	}

	public void testListeningInheritedList() {
		A baseConfig = newA();
		baseConfig.getL().add(newA(1));
		baseConfig.getL().add(newA(2));
		baseConfig.getL().add(newA(3));

		A a1 = newA(baseConfig, "<a y='0'></a>");

		doObserveL(a1);
	}

	private void doObserveL(A a1) {
		assertEquals(3, a1.getL().size());
		A l1 = a1.getL().get(0);
		A l2 = a1.getL().get(1);
		A l3 = a1.getL().get(2);
		assertEquals(1, l1.getX());
		assertEquals(2, l2.getX());
		assertEquals(3, l3.getX());

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l4 = newA(4);
		A l5 = newA(5);

		a1.getL().add(l5);
		a1.getL().add(3, l4);
		assertEquals(list(l1, l2, l3, l4, l5), a1.getL());
		assertEquals(list(l1, l2, l3, l4, l5), listener.getValues(l));
	}

	public void testListeningLoadedMap() {
		A a1 = newA(null, "<a y='0'><m><entry x='1' y='1'/><entry x='2' y='1'/><entry x='3' y='2'/></m></a>");

		doObserveM(a1);
	}

	public void testListeningInheritedMap() {
		A baseConfig = newA();
		putM(baseConfig, newA(1));
		putM(baseConfig, newA(2));
		putM(baseConfig, newA(3));

		A a1 = newA(baseConfig, "<a y='0'></a>");

		doObserveM(a1);
	}

	private void doObserveM(A a1) {
		assertEquals(3, a1.getM().size());
		A l1 = a1.getM().get(1);
		A l2 = a1.getM().get(2);
		A l3 = a1.getM().get(3);
		assertEquals(1, l1.getX());
		assertEquals(2, l2.getX());
		assertEquals(3, l3.getX());

		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);

		A l4 = newA(4);
		A l5 = newA(5);

		putM(a1, l5);
		putM(a1, l4);
		assertValues(list(l1, l2, l3, l4, l5), a1.getM());
		assertEqualsContent(list(l1, l2, l3, l4, l5), listener.getValues(m));
	}

	public void testListeningMapPutExisting() {
		A a1 = newA();

		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		a1.getM().putAll(map(l1, l2));

		A l2a = newA(2);
		l2.setY(42);

		putM(a1, l2a);

		assertValues(list(l1, l2a), a1.getM());
		assertEqualsContent(list(l1, l2a), listener.getValues(m));
	}

	public void testListeningMapRemoveNonExisting() {
		A a1 = newA();

		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);

		A l1 = newA(1);
		A l2 = newA(2);
		a1.getM().putAll(map(l1, l2));

		A l3 = newA(3);

		a1.getM().remove(l3.getX());
		a1.getM().keySet().remove(l3.getX());
		a1.getM().values().remove(l3);

		assertValues(list(l1, l2), a1.getM());
		assertEqualsContent(list(l1, l2), listener.getValues(m));
	}

	public void testListeningListRemoveAll() {
		A a1 = loadA();

		A l1 = a1.getL().get(0);
		A l2 = a1.getL().get(1);
		A l3 = a1.getL().get(2);

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		a1.getL().removeAll(Arrays.asList(l1, l3));

		assertEquals(list(l2), a1.getL());
		assertEquals(list(l2), listener.getValues(l));
	}

	public void testRegistration() {
		A a1 = newA();

		Observer l1 = new ValueObserver();
		Observer l2 = new ValueObserver();
		Observer l3 = new ValueObserver();
		Observer l4 = new ValueObserver();
		
		PropertyDescriptor x = a1.descriptor().getProperty(A.X);
		
		assertFalse(a1.removeConfigurationListener(x, l4));
		a1.setX(1);
		assertTrue(a1.addConfigurationListener(x, l1));
		assertFalse(a1.addConfigurationListener(x, l1));
		assertFalse(a1.removeConfigurationListener(x, l4));
		a1.setX(2);
		assertTrue(a1.addConfigurationListener(x, l2));
		assertFalse(a1.addConfigurationListener(x, l2));
		assertFalse(a1.removeConfigurationListener(x, l4));
		a1.setX(3);
		assertTrue(a1.removeConfigurationListener(x, l2));
		a1.setX(4);
		assertTrue(a1.addConfigurationListener(x, l2));
		assertTrue(a1.removeConfigurationListener(x, l1));
		a1.setX(5);
		assertTrue(a1.addConfigurationListener(x, l1));
		assertTrue(a1.addConfigurationListener(x, l3));
		assertFalse(a1.removeConfigurationListener(x, l4));
		a1.setX(6);
		assertTrue(a1.removeConfigurationListener(x, l3));
		a1.setX(7);
		assertTrue(a1.removeConfigurationListener(x, l2));
		a1.setX(8);
		assertTrue(a1.removeConfigurationListener(x, l1));
		a1.setX(9);

		assertEquals(list(1, 2, 3, 4, 6, 7, 8), l1.getValues(x));
		assertEquals(list(2, 3, 5, 6, 7), l2.getValues(x));
		assertEquals(list(5, 6), l3.getValues(x));
		assertEquals(list(), l4.getValues(x));
	}

	public void testWildcardListener() {
		A a1 = newA();

		Observer l1 = new ValueObserver();
		Observer l2 = new ValueObserver();

		PropertyDescriptor x = a1.descriptor().getProperty(A.X);
		PropertyDescriptor all = null;

		a1.setX(1);

		a1.addConfigurationListener(x, l1);
		a1.addConfigurationListener(all, l2);

		a1.setX(2);

		a1.removeConfigurationListener(x, l1);
		a1.removeConfigurationListener(all, l2);

		a1.setX(3);

		assertEquals(list(1, 2), l1.getValues(x));
		assertEquals(list(1, 2), l2.getValues(x));
		assertEquals(list(), l2.getValues(all));
	}

	public void testRecursiveRegistration() {
		A a1 = newA();

		Observer l1 = new ValueObserver();
		Observer l2 = new ValueObserver();
		final Observer l3 = new ValueObserver();

		final PropertyDescriptor x = a1.descriptor().getProperty(A.X);

		a1.addConfigurationListener(x, new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				change.getModel().addConfigurationListener(x, l3);
			}
		});
		a1.addConfigurationListener(x, l1);
		a1.addConfigurationListener(x, l2);

		a1.setX(1);
		a1.setX(2);
		a1.setX(3);

		assertEquals(list(1, 2, 3), l3.getValues(x));
		assertEquals(list(0, 1, 2, 3), l1.getValues(x));
		assertEquals(list(0, 1, 2, 3), l2.getValues(x));
	}

	public void testRecursiveDeregistration() {
		A a1 = newA();

		final Observer l1 = new ValueObserver();
		Observer l2 = new ValueObserver();
		final Observer l3 = new ValueObserver();

		final PropertyDescriptor x = a1.descriptor().getProperty(A.X);

		a1.addConfigurationListener(x, new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				assertFalse(change.getModel().removeConfigurationListener(x, l3));
				change.getModel().removeConfigurationListener(x, l1);
			}
		});
		a1.addConfigurationListener(x, l1);
		a1.addConfigurationListener(x, l2);

		a1.setX(1);
		a1.setX(2);
		a1.setX(3);

		assertEquals(list(0, 1), l1.getValues(x));
		assertEquals(list(0, 1, 2, 3), l2.getValues(x));
	}

	public void testFailedNotification() {
		A a1 = newA();

		Observer l1 = new ValueObserver();
		Observer l2 = new ValueObserver();

		final PropertyDescriptor x = a1.descriptor().getProperty(A.X);

		a1.addConfigurationListener(x, new ConfigurationListener() {
			int cnt = 0;
			@Override
			public void onChange(ConfigurationChange change) {
				cnt++;
				if (cnt == 2) {
					change.getModel().removeConfigurationListener(change.getProperty(), this);
				}
				throw new ExpectedFailure();
			}
		});
		a1.addConfigurationListener(x, l1);
		a1.addConfigurationListener(x, l2);

		// Note: If an exception in one listener should really stop the notification process seems
		// to be debatable, but for the moment the most trivial implementation.
		try {
			a1.setX(1);
			fail("Notification must fail.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
		try {
			a1.setX(2);
			fail("Notification must fail.");
		} catch (ExpectedFailure ex) {
			// Expected.
		}
		a1.setX(3);

		assertEquals(list(2, 3), l1.getValues(x));
	}

	public void testListeningListUpdate() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.L);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l2 = newA(2);

		a1.getL().addAll(Arrays.asList(l1));

		assertEquals(list(l1), a1.getL());
		assertEquals(list(l1), listener.getValues(l));

		A previous = a1.getL().set(0, l2);
		assertEquals(l1, previous);
		assertEquals(list(l2), a1.getL());
		assertEquals(list(l2), listener.getValues(l));
	}

	public void testNotListeningIllegalListUpdate() {
		A a1 = newA();

		Observer listener = new ListObserver();
		PropertyDescriptor l = a1.descriptor().getProperty(A.INDEXED_LIST);
		a1.addConfigurationListener(l, listener);

		A l1 = newA(1);
		A l1_2 = newA(1);

		try {
			a1.setIndexedList(Arrays.asList(l1, l1_2));
			fail("Indexed list with more than one entry for same key.");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", list(), listener.getValues(l));
		}
		a1.getIndexedList().add(l1);
		assertEquals("Add not notified.", list(l1), listener.getValues(l));
		try {
			a1.getIndexedList().add(l1_2);
			fail("Indexed list with more than one entry for same key.");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", list(l1), listener.getValues(l));
		}
		try {
			a1.getIndexedList().add(null);
			fail("Null values must not be added.");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", list(l1), listener.getValues(l));
		}
	}

	public void testNotListeningIllegalMapUpdate() {
		A a1 = newA();

		Observer listener = new MapObserver();
		PropertyDescriptor m = a1.descriptor().getProperty(A.M);
		a1.addConfigurationListener(m, listener);

		A m1 = newA(1);
		assertEquals("Tests expects thax X is key property.", a1.descriptor().getProperty(A.X), m.getKeyProperty());

		try {
			a1.setM(Collections.singletonMap(m1.getX() + 1, m1));
			fail("Key is not the value of the key property. Complete setting not possible");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", Collections.emptyList(), listener.getValues(m));
		}
		try {
			a1.getM().put(m1.getX() + 1, m1);
			fail("Key is not the value of the key property. Incremental setting not possible");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", Collections.emptyList(), listener.getValues(m));
		}
		a1.getM().put(m1.getX(), m1);
		assertEquals(list(m1), listener.getValues(m));
		try {
			a1.getM().put(m1.getX(), null);
			fail("Null values must not be added.");
		} catch (Exception ex) {
			// expected
			assertEquals("Setting failed, therfore no event.", list(m1), listener.getValues(m));
		}

	}

	private A newA() {
		A result = newA(0);
		result.setY(0);
		return result;
	}

	protected A newA(int x) {
		A result = newRawA();
		// Initialize values to become independent of the initial value.
		result.setX(x);
		return result;
	}

	private A newRawA() {
		return TypedConfiguration.newConfigItem(A.class);
	}

	private A loadA() {
		A baseConfig = newA();
		baseConfig.getL().add(newA(1));

		return newA(baseConfig, "<a y='0'><l><entry x='2' y='1'/><entry x='3' y='2'/></l></a>");
	}

	private A newA(A baseConfig, String xml) {
		return load(baseConfig, xml);
	}

	private A load(A baseConfig, String source) {
		String resourceName = source;
		Map<String, ConfigurationDescriptor> globalDescriptors =
			Collections.singletonMap("a", TypedConfiguration.getConfigurationDescriptor(A.class));
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, globalDescriptors);
		reader.setSource(CharacterContents.newContent(source, resourceName));
		reader.setBaseConfig(baseConfig);
		try {
			return (A) reader.read();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Loading fixture failed.", ex);
		}
	}

	private static Map<Integer, A> map(A... as) {
		HashMap<Integer, A> result = new HashMap<>();
		for (A a : as) {
			result.put(a.getX(), a);
		}
		return result;
	}

	private void putM(A a1, A l1) {
		a1.getM().put(l1.getX(), l1);
	}

	private void putAllM(A a1, List<A> elements) {
		Map<Integer, A> values = new HashMap<>();
		for (A element : elements) {
			values.put(element.getX(), element);
		}
		a1.setM(values);
	}

	private static void assertValues(Collection<?> c1, Map<?, ?> m) {
		assertEqualsContent(c1, m.values());
	}

	private static void assertEqualsContent(Collection<?> c1, Collection<?> c2) {
		assertEquals(toSet(c1), toSet(c2));
	}

	@Override
	protected Map<String, ConfigurationDescriptor> getDescriptors() {
		return Collections.emptyMap();
	}

	public static Test suite() {
		return AbstractTypedConfigurationTestCase.suite(TestTypedConfigurationListener.class);
	}

}

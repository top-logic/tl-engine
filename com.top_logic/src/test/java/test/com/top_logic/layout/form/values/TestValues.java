/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values;

import static com.top_logic.layout.form.values.Values.*;
import junit.framework.TestCase;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.Function2;
import com.top_logic.layout.form.values.Function3;
import com.top_logic.layout.form.values.Listener;
import com.top_logic.layout.form.values.ModifiableValue;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.Values;

/**
 * Test case for dependent {@link Values}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestValues extends TestCase {

	public void testMap() {
		ModifiableValue<Integer> input = modifiable();
		Value<Integer> output = map(input, new Mapping<Integer, Integer>() {
			@Override
			public Integer map(Integer arg) {
				return arg + 1;
			}
		});

		input.set(2);
		assertEquals(Integer.valueOf(3), output.get());

		input.set(5);
		assertEquals(Integer.valueOf(6), output.get());
	}

	public void testMap2() {
		ModifiableValue<Integer> input1 = modifiable();
		ModifiableValue<Integer> input2 = modifiable();
		Value<Integer> output = map(input1, input2, new Function2<Integer, Integer, Integer>() {
			@Override
			public Integer eval(Integer arg1, Integer arg2) {
				return arg1 + arg2;
			}
		});

		input1.set(2);
		input2.set(3);
		assertEquals(Integer.valueOf(5), output.get());

		input1.set(10);
		assertEquals(Integer.valueOf(13), output.get());

		input2.set(0);
		assertEquals(Integer.valueOf(10), output.get());
	}

	public void testMap3() {
		ModifiableValue<Integer> input1 = modifiable();
		ModifiableValue<Integer> input2 = modifiable();
		ModifiableValue<Integer> input3 = modifiable();
		Value<Integer> output = map(input1, input2, input3, new Function3<Integer, Integer, Integer, Integer>() {
			@Override
			public Integer eval(Integer arg1, Integer arg2, Integer arg3) {
				return arg1 * (arg2 + arg3);
			}
		});

		input1.set(1);
		input2.set(2);
		input3.set(3);
		assertEquals(Integer.valueOf(5), output.get());

		input1.set(2);
		assertEquals(Integer.valueOf(10), output.get());

		input2.set(7);
		assertEquals(Integer.valueOf(20), output.get());
	}

	static final class CountingListener implements Listener {
		private int _calls;

		@Override
		public void handleChange(Value<?> sender) {
			_calls++;
		}

		public int getCalls() {
			return _calls;
		}
	}

	public interface A extends ConfigurationItem {

		String B_NAME = "b";

		String FOO_NAME = "foo";

		@Name(B_NAME)
		B getB();

		@Name(FOO_NAME)
		String getFoo();

		interface B extends ConfigurationItem {
			String BAR_NAME = "bar";

			@Name(BAR_NAME)
			String getBar();

			void setBar(String value);
		}

		void setFoo(String value);

		void setB(A.B value);
	}

	public void testConfigurationValue() {
		A a = TypedConfiguration.newConfigItem(A.class);
		Value<String> foo = Values.configurationValue(a, A.FOO_NAME);
		CountingListener counter = new CountingListener();
		foo.addListener(counter);

		assertEquals("", foo.get());

		a.setFoo("hello");
		assertEquals(1, counter.getCalls());
		assertEquals("hello", foo.get());
	}

	public void testConfigurationPathValue() {
		A a = TypedConfiguration.newConfigItem(A.class);
		Value<String> bar = Values.configurationValue(a, A.B_NAME, A.B.BAR_NAME);
		CountingListener counter = new CountingListener();
		bar.addListener(counter);

		assertEquals(null, bar.get());
		assertEquals(0, counter.getCalls());

		A.B b1 = TypedConfiguration.newConfigItem(A.B.class);
		a.setB(b1);

		assertEquals("", bar.get());

		int before = counter.getCalls();
		b1.setBar("hello");

		assertEquals(before + 1, counter.getCalls());
		assertEquals("hello", bar.get());

		A.B b2 = TypedConfiguration.newConfigItem(A.B.class);
		b2.setBar("world");
		a.setB(b2);

		assertEquals(before + 2, counter.getCalls());
		assertEquals("world", bar.get());
	}

}

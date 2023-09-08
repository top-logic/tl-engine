/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.channel;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.layout.channel.CombiningChannel;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.DefaultChannel;
import com.top_logic.layout.channel.TransformingChannel;

/**
 * Test case for {@link ComponentChannel} implementations.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestComponentChannel extends TestCase {

	public void testLinkUnlink() {
		performLinkUnlinkTest(this::doLinkUnlink);
	}

	private void doLinkUnlink(UnlinkOperation unlink) {
		ComponentChannel a = new DefaultChannel(null, "a", "foo");
		ComponentChannel b = new DefaultChannel(null, "b", null);

		assertEquals("foo", a.get());
		assertEquals(null, b.get());

		b.link(a);

		assertEquals("foo", a.get());
		assertEquals("foo", b.get());

		a.set("bar");

		assertEquals("bar", a.get());
		assertEquals("bar", b.get());

		b.set("bazz");

		assertEquals("bazz", a.get());
		assertEquals("bazz", b.get());

		unlink.apply(a, b);

		assertEquals("bazz", a.get());
		assertEquals("bazz", b.get());

		a.set("xxx");

		assertEquals("xxx", a.get());
		assertEquals("bazz", b.get());

		b.set("yyy");

		assertEquals("xxx", a.get());
		assertEquals("yyy", b.get());
	}

	public void testSimpleTransform() {
		ComponentChannel a = new DefaultChannel(null, "a", "foo");
		ComponentChannel b = new TransformingChannel(null, a, (x, y) -> x + "-add");

		Object[] last = { null };
		ChannelListener receiver = new ChannelListener() {
			@Override
			public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
				last[0] = newValue;
				System.out.println("Received: " + oldValue + " -> " + newValue);
			}
		};

		b.addListener(receiver);

		assertEquals("foo", a.get());
		assertEquals("foo-add", b.get());
		assertEquals("foo-add", last[0]);

		a.set("bar");

		assertEquals("bar", a.get());
		assertEquals("bar-add", b.get());
		assertEquals("bar-add", last[0]);

		b.set("invalid");

		assertEquals("bar", a.get());
		assertEquals("invalid", b.get());
		assertEquals("invalid", last[0]);

		b.removeListener(receiver);

		a.set("xxx");

		// Value still available upon request.
		assertEquals("xxx-add", b.get());

		// Listener no longer informed.
		assertEquals("invalid", last[0]);
	}

	public void testTransformScenario() {
		performLinkUnlinkTest(this::doTransformScenario);
	}

	private void doTransformScenario(UnlinkOperation unlink) {
		ComponentChannel a = new DefaultChannel(null, "a", "foo");

		ComponentChannel b1 = new TransformingChannel(null, a, (x, y) -> x + "-add1");
		ComponentChannel b2 = new TransformingChannel(null, a, (x, y) -> x + "-add2");
		ComponentChannel c = new CombiningChannel(null, Arrays.asList(a, b1, b2));

		ComponentChannel d = new DefaultChannel(null, "d", null);

		assertEquals("foo", a.get());
		assertEquals("foo-add1", b1.get());
		assertEquals("foo-add2", b2.get());
		assertEquals(list("foo", "foo-add1", "foo-add2"), c.get());
		assertEquals(null, d.get());

		a.set("bar");

		assertEquals("bar", a.get());
		assertEquals("bar-add1", b1.get());
		assertEquals("bar-add2", b2.get());
		assertEquals(list("bar", "bar-add1", "bar-add2"), c.get());
		assertEquals(null, d.get());

		d.link(c);

		assertEquals("bar", a.get());
		assertEquals("bar-add1", b1.get());
		assertEquals("bar-add2", b2.get());
		assertEquals(list("bar", "bar-add1", "bar-add2"), c.get());
		assertEquals(list("bar", "bar-add1", "bar-add2"), d.get());

		a.set("xxx");

		assertEquals("xxx", a.get());
		assertEquals("xxx-add1", b1.get());
		assertEquals("xxx-add2", b2.get());
		assertEquals(list("xxx", "xxx-add1", "xxx-add2"), c.get());
		assertEquals(list("xxx", "xxx-add1", "xxx-add2"), d.get());

		unlink.apply(d, c);

		assertEquals("xxx", a.get());
		assertEquals("xxx-add1", b1.get());
		assertEquals("xxx-add2", b2.get());
		assertEquals(list("xxx", "xxx-add1", "xxx-add2"), c.get());
		assertEquals(list("xxx", "xxx-add1", "xxx-add2"), d.get());

		a.set("yyy");

		assertEquals("yyy", a.get());
		assertEquals("yyy-add1", b1.get());
		assertEquals("yyy-add2", b2.get());
		assertEquals(list("yyy", "yyy-add1", "yyy-add2"), c.get());
		assertEquals(list("xxx", "xxx-add1", "xxx-add2"), d.get());
	}

	private void performLinkUnlinkTest(TestOperation test) {
		test.apply((a, b) -> a.unlink(b));
		test.apply((a, b) -> b.unlink(a));
		test.apply((a, b) -> a.unlinkAll());
		test.apply((a, b) -> b.unlinkAll());
	}

	interface TestOperation {
		void apply(UnlinkOperation unlink);
	}

	interface UnlinkOperation {
		void apply(ComponentChannel target, ComponentChannel source);
	}

}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ObservingChannel;
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests that {@link ObservingChannels} lets the observing channels observe exactly while their
 * control is displayed.
 */
public class TestObservingChannels extends TestCase {

	/** The scope the window of the test context supplies. */
	private ModelScope _scope;

	private ReactContext _context;

	private ReactControl _root;

	/** What the observing channels were told, in order. */
	private List<String> _protocol;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_scope = (ModelScope) Proxy.newProxyInstance(ModelScope.class.getClassLoader(),
			new Class<?>[] { ModelScope.class }, (proxy, method, args) -> {
				throw new UnsupportedOperationException("No object is observed in this test.");
			});
		ModelScope scope = _scope;
		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")) {
			@Override
			public ModelScope getModelScope() {
				return scope;
			}
		};
		_root = new ReactControl(_context, null, "TLStack");
		_protocol = new ArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		_root = null;
		_context = null;
		_scope = null;
		_protocol = null;
		super.tearDown();
	}

	/**
	 * Tests that the channels are attached to the scope of the context when the control is
	 * displayed, and detached when it is not.
	 */
	public void testAttachedWhileDisplayed() {
		ObservingChannels.observeWhileDisplayed(_root, List.of(observing("a"), observing("b")), _context);
		assertEquals(List.of(), _protocol);

		_root.attach();
		assertEquals(List.of("attach a", "attach b"), _protocol);

		_protocol.clear();
		_root.detach();
		assertEquals(List.of("detach a", "detach b"), _protocol);
	}

	/**
	 * Tests that the channels are attached again when the control is displayed again.
	 */
	public void testAttachAfterDetach() {
		ObservingChannels.observeWhileDisplayed(_root, List.of(observing("a")), _context);
		_root.attach();
		_root.detach();
		_protocol.clear();

		_root.attach();
		assertEquals(List.of("attach a"), _protocol);
	}

	/**
	 * Tests that channels handed to an already displayed control start observing at once.
	 */
	public void testAttachedControl() {
		_root.attach();
		ObservingChannels.observeWhileDisplayed(_root, List.of(observing("a")), _context);
		assertEquals(List.of("attach a"), _protocol);
	}

	/**
	 * Tests that among the given channels only the observing ones are told about the display, and
	 * that channels of which none observes are accepted without effect.
	 */
	public void testOnlyObservingChannels() {
		ViewChannel plain = plainChannel();
		ObservingChannels.observeWhileDisplayed(_root, List.of(plain, observing("a")), _context);
		_root.attach();
		_root.detach();
		assertEquals(List.of("attach a", "detach a"), _protocol);

		ReactControl other = new ReactControl(_context, null, "TLStack");
		ObservingChannels.observeWhileDisplayed(other, List.of(plain), _context);
		other.attach();
		other.detach();
		assertEquals(List.of("attach a", "detach a"), _protocol);
	}

	/**
	 * A channel recording in {@link #_protocol} when it is attached and detached, and checking it
	 * is attached to the scope of the test context.
	 */
	private ObservingChannel observing(String name) {
		DefaultViewChannel delegate = new DefaultViewChannel(name);
		return (ObservingChannel) Proxy.newProxyInstance(ObservingChannel.class.getClassLoader(),
			new Class<?>[] { ObservingChannel.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "attach":
						assertSame("Attached to the scope of the context.", _scope, args[0]);
						_protocol.add("attach " + name);
						return null;
					case "detach":
						_protocol.add("detach " + name);
						return null;
					default:
						return method.invoke(delegate, args);
				}
			});
	}

	/**
	 * A channel that observes nothing.
	 */
	private static ViewChannel plainChannel() {
		DefaultViewChannel delegate = new DefaultViewChannel("plain");
		return (ViewChannel) Proxy.newProxyInstance(ViewChannel.class.getClassLoader(),
			new Class<?>[] { ViewChannel.class }, (proxy, method, args) -> method.invoke(delegate, args));
	}

}

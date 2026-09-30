/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Tests that a {@link ReactControl} is registered with the {@link SSEUpdateQueue} of its window
 * exactly while it is {@link ReactControl#isAttached() attached}.
 *
 * <p>
 * The queue is what the client addresses a control through, and the client only knows the controls
 * it was sent. Following the display state keeps every such control reachable while releasing any
 * control that was built but never displayed, or that left the display, from the queue.
 * </p>
 */
public class TestControlRegistration extends TestCase {

	private SSEUpdateQueue _queue;

	private ReactControl _first;

	private ReactControl _second;

	private ReactStackControl _parent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_queue = new SSEUpdateQueue();
		ReactContext context = new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
		_first = new ReactControl(context, null, "TLPanel");
		_second = new ReactControl(context, null, "TLPanel");
		_parent = new ReactStackControl(context, List.of(_first, _second));
	}

	@Override
	protected void tearDown() throws Exception {
		_queue.shutdown();
		super.tearDown();
	}

	private boolean registered(ReactControl control) {
		return _queue.getControl(control.getID()) == control;
	}

	private void render() throws Exception {
		_parent.write(new TagWriter());
	}

	/** A control that was built but never displayed is not held by the queue. */
	public void testAConstructedControlIsNotRegistered() {
		assertFalse("A control nobody displays is not addressable.", registered(_parent));
		assertFalse("A child nobody displays is not addressable.", registered(_first));
		assertFalse("The queue holds no control before a page is rendered.", _queue.hasControls());
	}

	/** Rendering registers the control and the children it renders. */
	public void testRenderingRegistersTheTree() throws Exception {
		render();

		assertTrue("A rendered control is addressable.", registered(_parent));
		assertTrue("A rendered child is addressable.", registered(_first));
		assertTrue("A rendered child is addressable.", registered(_second));
		assertTrue("A rendered page leaves controls in the queue.", _queue.hasControls());
	}

	/** An explicit attach registers the control and its children. */
	public void testAttachingRegistersTheTree() {
		_parent.attach();

		assertTrue("An attached control is addressable.", registered(_parent));
		assertTrue("An attached child is addressable.", registered(_first));
	}

	/** Detaching a parent unregisters its whole subtree. */
	public void testDetachingUnregistersTheSubtree() throws Exception {
		render();
		_parent.detach();

		assertFalse("A detached control is not addressable.", registered(_parent));
		assertFalse("The child of a detached control is not addressable.", registered(_first));
		assertFalse("The child of a detached control is not addressable.", registered(_second));
		assertFalse("The queue keeps nothing that left the display.", _queue.hasControls());
	}

	/** A control displayed again is addressable again. */
	public void testReattachingRegistersAgain() throws Exception {
		render();
		_parent.detach();
		_parent.attach();

		assertTrue("A control displayed again is addressable.", registered(_parent));
		assertTrue("The child of a control displayed again is addressable.", registered(_first));
	}

	/** Disposing unregisters the tree, and a trailing render of a disposed control does not undo that. */
	public void testADisposedControlIsNeverRegisteredAgain() throws Exception {
		render();
		_parent.cleanupTree();

		assertFalse("A disposed control is not addressable.", registered(_parent));
		assertFalse("The child of a disposed control is not addressable.", registered(_first));

		render();
		_first.attach();

		assertFalse("Rendering a disposed control does not resurrect it.", registered(_parent));
		assertFalse("Attaching a disposed control does not resurrect it.", registered(_first));
	}

}

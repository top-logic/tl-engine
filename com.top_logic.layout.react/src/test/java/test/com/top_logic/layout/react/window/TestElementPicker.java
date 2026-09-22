/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ElementPicker;
import com.top_logic.layout.react.window.PickKind;
import com.top_logic.layout.react.window.PickResult;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * Test for {@link ElementPicker}: starting a pick in one window and delivering what the user hit in
 * that window to the window that asked.
 *
 * <p>
 * The fixture is a bare one - a session registry with two windows, an inline control in the picked
 * one - so the pick mechanism is exercised without any application service.
 * </p>
 */
public class TestElementPicker extends TestCase {

	/** A control with nothing but an identity, standing in for whatever the user clicks. */
	private static final class DemoControl extends ReactControl {

		DemoControl(ReactContext context) {
			super(context, null, "Demo");
		}
	}

	/** {@link ReactContext} of one window, backed by that window's queue in the registry. */
	private static final class WindowContext implements ReactContext {

		private final ReactWindowRegistry _registry;

		private final String _windowName;

		WindowContext(ReactWindowRegistry registry, String windowName) {
			_registry = registry;
			_windowName = windowName;
		}

		@Override
		public String allocateId() {
			return getSSEQueue().allocateId();
		}

		@Override
		public String getWindowName() {
			return _windowName;
		}

		@Override
		public String getContextPath() {
			return "";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return _registry.getOrCreateQueue(_windowName);
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return _registry;
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

	private ReactWindowRegistry _registry;

	private WindowContext _tool;

	private WindowContext _app;

	private DemoControl _control;

	private AtomicReference<PickResult> _picked;

	@Override
	protected void setUp() {
		_registry = new ReactWindowRegistry("testSession");
		_tool = new WindowContext(_registry, "toolWindow");
		_app = new WindowContext(_registry, "appWindow");
		_control = new DemoControl(_app);
		_picked = new AtomicReference<>();
	}

	/**
	 * A control pick registers a pending pick and delivers the clicked control to the requester.
	 */
	public void testControlPickDeliversTheControl() {
		String token = startPick(PickKind.CONTROL);
		assertNotNull("Pick mode was not started.", token);

		deliver(report(token, PickKind.CONTROL, ElementPicker.CONTROL_ID_FIELD, _control.getID()));

		PickResult result = _picked.get();
		assertTrue("Expected a control pick, got: " + result, result instanceof PickResult.ControlPicked);
		PickResult.ControlPicked control = (PickResult.ControlPicked) result;
		assertSame(_control, control.control());
		assertEquals("appWindow", control.windowName());
		assertSame(_registry.getQueue("appWindow"), control.queue());
		assertEquals(PickKind.CONTROL, result.kind());
	}

	/**
	 * A view pick delivers the source path the client read from the clicked element.
	 */
	public void testViewPickDeliversThePath() {
		String token = startPick(PickKind.VIEW);

		deliver(report(token, PickKind.VIEW, ElementPicker.PATH_FIELD, "/WEB-INF/views/app.view.xml"));

		PickResult result = _picked.get();
		assertTrue("Expected a view pick, got: " + result, result instanceof PickResult.ViewPicked);
		assertEquals("/WEB-INF/views/app.view.xml", ((PickResult.ViewPicked) result).sourcePath());
		assertEquals(PickKind.VIEW, result.kind());
	}

	/**
	 * A report quoting a token that was never handed out delivers nothing.
	 */
	public void testWrongTokenDeliversNothing() {
		startPick(PickKind.CONTROL);

		assertNull(ElementPicker.resolve(_registry,
			report("someOtherToken", PickKind.CONTROL, ElementPicker.CONTROL_ID_FIELD, _control.getID())));
		assertNull("A foreign token must not deliver anything.", _picked.get());
	}

	/**
	 * A report naming a control the picked window does not hold delivers nothing.
	 */
	public void testUnknownControlDeliversNothing() {
		String token = startPick(PickKind.CONTROL);

		assertNull(ElementPicker.resolve(_registry,
			report(token, PickKind.CONTROL, ElementPicker.CONTROL_ID_FIELD, "vNoSuchControl")));
		assertNull("An unknown control ID must not deliver anything.", _picked.get());
	}

	/**
	 * A report whose kind is not the one the pick was started with delivers nothing.
	 */
	public void testMismatchingKindDeliversNothing() {
		String token = startPick(PickKind.CONTROL);

		assertNull(ElementPicker.resolve(_registry,
			report(token, PickKind.VIEW, ElementPicker.PATH_FIELD, "/WEB-INF/views/app.view.xml")));
		assertNull("A report of the wrong kind must not deliver anything.", _picked.get());
	}

	/**
	 * A pick is answered only once: the second report finds no pending pick.
	 */
	public void testPickIsConsumedByTheFirstReport() {
		String token = startPick(PickKind.CONTROL);
		Map<String, Object> report =
			report(token, PickKind.CONTROL, ElementPicker.CONTROL_ID_FIELD, _control.getID());

		deliver(report);
		_picked.set(null);

		assertNull(ElementPicker.resolve(_registry, report));
		assertNull("A consumed pick must not deliver twice.", _picked.get());
	}

	private String startPick(PickKind kind) {
		return ElementPicker.start(_tool, _app, kind, _picked::set);
	}

	private void deliver(Map<String, Object> report) {
		ElementPicker.Delivery delivery = ElementPicker.resolve(_registry, report);
		assertNotNull("Nothing to deliver for: " + report, delivery);
		assertEquals("toolWindow", delivery.pick().requesterWindowId());
		delivery.deliver();
	}

	private static Map<String, Object> report(String token, PickKind kind, String valueField, String value) {
		Map<String, Object> report = new HashMap<>();
		report.put(ElementPicker.TOKEN_FIELD, token);
		report.put(ElementPicker.KIND_FIELD, kind.getExternalName());
		report.put(valueField, value);
		return report;
	}
}

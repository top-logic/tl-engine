/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.util.Resources;

/**
 * Tests {@link SSEUpdateQueue#offerRefusedUrl(String)}: an address the browser refused to open a
 * window for is offered in a notice of the window, as a link the user's click opens.
 */
public class TestOfferRefusedUrl extends TestCase {

	/** An {@link ErrorSink} recording the warnings it is asked to show. */
	private static final class RecordingSink implements ErrorSink {

		final List<HTMLFragment> _warnings = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			fail("A refused window is no error.");
		}

		@Override
		public void showWarning(HTMLFragment content) {
			_warnings.add(content);
		}

		@Override
		public void showInfo(HTMLFragment content) {
			fail("A refused window is no mere information.");
		}
	}

	private final RecordingSink _sink = new RecordingSink();

	private final SSEUpdateQueue _queue = new SSEUpdateQueue();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ReactContext base = new DefaultReactContext("", "w", _queue, new ReactWindowRegistry("w"));
		ReactContext window = new ForwardingReactContext(base) {
			@Override
			public ErrorSink getErrorSink() {
				return _sink;
			}
		};
		_queue.setRootControl(new ReactStackControl(window, List.of()));
	}

	/**
	 * Tests that the refused address is offered as a link opening it in a window of its own, cut
	 * off from the application's window, next to the message saying what happened.
	 */
	public void testOffersLink() throws IOException {
		_queue.offerRefusedUrl("https://example.com/a?b=c&d=e");

		assertEquals(1, _sink._warnings.size());
		String html = render(_sink._warnings.get(0));
		assertTrue(html, html.contains(Resources.getInstance().getString(I18NConstants.NOTICE_OPEN_URL_REFUSED)));
		assertTrue(html, html.contains("<a href=\"https://example.com/a?b=c&amp;d=e\""));
		assertTrue(html, html.contains("target=\"_blank\""));
		assertTrue(html, html.contains("rel=\"noopener noreferrer\""));
		assertTrue("The link shows the address: " + html, html.contains(">https://example.com/a?b=c&amp;d=e</a>"));
	}

	/**
	 * Tests that only a web address is offered: the client opens nothing else in a window.
	 */
	public void testOffersOnlyWebAddresses() {
		_queue.offerRefusedUrl("javascript:alert(1)");
		_queue.offerRefusedUrl("mailto:info@example.com");
		_queue.offerRefusedUrl("docs/help.html");
		_queue.offerRefusedUrl("not an address");

		assertTrue(_sink._warnings.isEmpty());
	}

	/**
	 * Tests that a window without a root control - nothing displayed yet - offers nothing and does
	 * not fail.
	 */
	public void testWithoutRootControl() {
		new SSEUpdateQueue().offerRefusedUrl("https://example.com/");

		assertTrue(_sink._warnings.isEmpty());
	}

	private static String render(HTMLFragment fragment) throws IOException {
		TagWriter out = new TagWriter();
		fragment.write(DummyDisplayContext.forScope(new DummyControlScope()), out);
		return out.toString();
	}

	/** Suite providing the resources the notice is resolved with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestOfferRefusedUrl.class, ResourcesModule.Module.INSTANCE));
	}
}

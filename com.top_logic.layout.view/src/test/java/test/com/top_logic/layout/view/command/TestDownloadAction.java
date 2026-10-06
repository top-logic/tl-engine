/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.command;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.command.DownloadAction;
import com.top_logic.layout.view.command.I18NConstants;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests {@link DownloadAction}, the action handing the chain's file to the browser, and the
 * delivery of a file through {@link SSEUpdateQueue#deliverDownload(BinaryData)}.
 */
public class TestDownloadAction extends TestCase {

	/**
	 * A window queue remembering the files delivered to it, without a browser connected.
	 */
	private static final class Window extends SSEUpdateQueue {

		final List<BinaryData> _delivered = new ArrayList<>();

		@Override
		public String deliverDownload(BinaryData data) {
			_delivered.add(data);
			return super.deliverDownload(data);
		}
	}

	private final Window _window = new Window();

	private ReactContext context() {
		return new DefaultReactContext("", "test", _window, new ReactWindowRegistry("test"));
	}

	private static BinaryData file(String name) {
		return BinaryDataFactory.createBinaryData("content".getBytes(StandardCharsets.UTF_8), "text/plain", name);
	}

	/**
	 * Tests that the file of the chain is delivered under its own name, and that the chain goes on
	 * with the file.
	 */
	public void testDeliversFile() {
		BinaryData file = file("report.txt");

		Object result = new DownloadAction(null).execute(context(), file);

		assertSame("The chain continues with the file.", file, result);
		assertEquals(1, _window._delivered.size());
		assertSame(file, _window._delivered.get(0));
		assertEquals("The client is told to fetch the file.", 1, _window.pendingEventCount());
	}

	/**
	 * Tests that a configured name replaces the name of the file, the content staying the same.
	 */
	public void testRenames() throws IOException {
		DownloadAction action = new DownloadAction((context, file) -> ResKey.text("tickets.txt"));

		action.execute(context(), file("data.txt"));

		BinaryData delivered = _window._delivered.get(0);
		assertEquals("tickets.txt", delivered.getName());
		assertEquals("text/plain", delivered.getContentType());
		assertEquals("content", new String(delivered.getStream().readAllBytes(), StandardCharsets.UTF_8));
	}

	/**
	 * Tests that a chain without a file tells the user instead of silently doing nothing.
	 */
	public void testNothingToDownload() {
		assertFails(I18NConstants.ERROR_NOTHING_TO_DOWNLOAD, null);
		assertFails(I18NConstants.ERROR_NOT_A_FILE__VALUE.fill("some text"), "some text");
		assertTrue(_window._delivered.isEmpty());
	}

	private void assertFails(ResKey expected, Object input) {
		try {
			new DownloadAction(null).execute(context(), input);
			fail("A download of '" + StringServices.toString(input) + "' must fail.");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey());
		}
	}

	/**
	 * Tests that a chain without a window - running headless - passes the file on.
	 */
	public void testHeadless() {
		BinaryData file = file("report.txt");
		ReactContext headless = new ReactContext() {
			@Override
			public String allocateId() {
				return "id";
			}

			@Override
			public String getWindowName() {
				return null;
			}

			@Override
			public String getContextPath() {
				return "";
			}

			@Override
			public SSEUpdateQueue getSSEQueue() {
				return null;
			}

			@Override
			public ReactWindowRegistry getWindowRegistry() {
				return null;
			}

			@Override
			public ModelScope getModelScope() {
				return null;
			}
		};

		assertSame(file, new DownloadAction(null).execute(headless, file));
	}

	/**
	 * Tests that a delivered file is fetched once: a second request finds nothing, like one for a
	 * key never delivered.
	 */
	public void testDeliveredOnce() {
		BinaryData file = file("report.txt");
		SSEUpdateQueue queue = new SSEUpdateQueue();

		String key = queue.deliverDownload(file);

		assertNull(queue.takeDownload("unknown"));
		assertSame(file, queue.takeDownload(key));
		assertNull("A file is fetched once.", queue.takeDownload(key));
	}

	/**
	 * Suite with the {@link LabelProviderService} the message of a refused value is labelled by.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestDownloadAction.class, LabelProviderService.Module.INSTANCE));
	}

}

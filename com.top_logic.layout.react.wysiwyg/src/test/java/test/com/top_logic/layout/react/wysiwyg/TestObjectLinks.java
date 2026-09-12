/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ForwardingReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.wysiwyg.ObjectLinks;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;

/**
 * Tests that following an object link in displayed structured text leads to the place the linked
 * object is displayed at, and that a link leading nowhere says so.
 *
 * @see ObjectLinks
 */
public class TestObjectLinks extends TestCase {

	/** The command the client sends when the user follows an object link. */
	private static final String CMD_SHOW_OBJECT_LINK = "showObjectLink";

	/** The {@link #CMD_SHOW_OBJECT_LINK} argument naming the object to display. */
	private static final String ARG_HREF = "href";

	/** The object the application displays somewhere. */
	private static final String SHOWN = "shown";

	/** The object it displays nowhere. */
	private static final String UNSHOWN = "unshown";

	/**
	 * A link destination that names no object at all: it carries none of the arguments an object
	 * is described by, so nothing can be resolved from it.
	 */
	private static final String NO_OBJECT = "#anchor";

	/**
	 * The navigator under test: it displays {@link #SHOWN} and nothing else, and records what it
	 * was asked to display.
	 */
	private static final class Targets implements ObjectNavigator {

		private final List<Object> _shown = new ArrayList<>();

		@Override
		public boolean canShow(Object value) {
			return SHOWN.equals(value);
		}

		@Override
		public void show(ReactContext context, Object value) {
			_shown.add(value);
		}

		List<Object> shown() {
			return _shown;
		}
	}

	/**
	 * The place messages to the user are collected at, so that a test can tell a silent failure
	 * from a reported one.
	 */
	private static final class Messages implements ErrorSink {

		private final List<HTMLFragment> _errors = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			_errors.add(content);
		}

		@Override
		public void showWarning(HTMLFragment content) {
			// Not part of what is tested here.
		}

		@Override
		public void showInfo(HTMLFragment content) {
			// Not part of what is tested here.
		}

		List<HTMLFragment> errors() {
			return _errors;
		}
	}

	private Targets _navigator;

	private Messages _messages;

	private ReactContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_navigator = new Targets();
		_messages = new Messages();
		_context = context(_navigator);
	}

	/** An object with a place of its own is displayed there. */
	public void testAnObjectIsDisplayedWhereItBelongs() {
		ObjectLinks.show(_context, SHOWN);

		assertEquals(List.of(SHOWN), _navigator.shown());
		assertEquals(List.of(), _messages.errors());
	}

	/** An object with no place of its own is not silently dropped. */
	public void testAnObjectWithNoPlaceIsReported() {
		ObjectLinks.show(_context, UNSHOWN);

		assertEquals(List.of(), _navigator.shown());
		assertEquals(1, _messages.errors().size());
	}

	/** Displayed content outside the view layer says that it cannot lead anywhere. */
	public void testWithoutANavigatorTheLinkIsReportedAsLeadingNowhere() {
		ObjectLinks.show(context(null), SHOWN);

		assertEquals(1, _messages.errors().size());
	}

	/** A link that names no object reports that its object cannot be found. */
	public void testALinkNamingNoObjectIsReported() {
		ObjectLinks.follow(_context, NO_OBJECT);

		assertEquals(List.of(), _navigator.shown());
		assertEquals(1, _messages.errors().size());
	}

	/** The client command hands its argument to the resolution of the link. */
	public void testTheCommandFollowsTheLinkItIsGiven() {
		control().executeCommand(CMD_SHOW_OBJECT_LINK, Map.of(ARG_HREF, NO_OBJECT));

		assertEquals("The link names no object, which the user is told about.",
			1, _messages.errors().size());
	}

	/** A command without a link destination fails the same way as one naming no object. */
	public void testTheCommandSurvivesAMissingLinkDestination() {
		control().executeCommand(CMD_SHOW_OBJECT_LINK, Map.of());

		assertEquals(1, _messages.errors().size());
	}

	private ReactWysiwygControl control() {
		return new ReactWysiwygControl(_context, new AbstractFieldModel(null));
	}

	/**
	 * A context reporting to {@link #_messages} and displaying objects through the given navigator.
	 */
	private ReactContext context(ObjectNavigator navigator) {
		return new ForwardingReactContext(new DefaultReactContext("", "test", new SSEUpdateQueue())) {
			@Override
			public ObjectNavigator getObjectNavigator() {
				return navigator;
			}

			@Override
			public ErrorSink getErrorSink() {
				return _messages;
			}
		};
	}

	/**
	 * Test suite requiring the session resources a message to the user is composed from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObjectLinks.class, ThreadContextManager.Module.INSTANCE));
	}

}

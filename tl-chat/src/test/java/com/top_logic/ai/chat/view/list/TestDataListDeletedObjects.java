/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ReactMenuControl.MenuEntry;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * A {@code <data-list>} holds no deleted object on its channels (#676).
 *
 * <p>
 * The chat list's "Delete" command is hidden while its chat is being answered, which its rule finds
 * out by navigating from the chat to its executions - and navigating from a deleted object throws.
 * The row channel kept the chat its menu was last opened on, so after deleting that chat, leaving
 * the page and coming back, the command's rule ran over the deleted chat and the page could not be
 * displayed again.
 * </p>
 */
public class TestDataListDeletedObjects extends BasicTestCase {

	/** Navigates from the row, as the chat list's own rule does. */
	private static final String RULE = "s -> isEmpty($s)"
		+ " || $s.referers(`tl.ai.execution:AgentExecution#session`).isEmpty()";

	private static Person owner;

	private ViewChannel _owner;

	private ViewChannel _tick;

	private ViewChannel _selection;

	private ViewChannel _row;

	private DataListControl _list;

	private Person _previousUser;
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("data-list-deleted");
		}
		// The list reads its rows and its rules decide as the user the page is shown to; TL-Script
		// sees only what that user may read.
		TLContext context = TLContext.getContext();
		_previousUser = context.getPerson();
		context.setCurrentPerson(PersonManager.getManager().getRoot());
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		String window = "dataListWindow";
		DefaultReactContext react =
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry);
		ContextMenuOpener opener = new ContextMenuOpener(new ContextMenuOpener.MenuRenderer() {
			@Override
			public void show(int x, int y, List<MenuEntry> items, Function<String, HandlerResult> selectHandler,
					Runnable closeHandler) {
				// No browser to show a menu in.
			}

			@Override
			public void hide() {
				// Nothing shown.
			}
		});
		opener.bindReactContext(() -> react);
		ViewContext view = new DefaultViewContext(react).withContextMenuOpener(opener);
		_owner = new DefaultViewChannel("owner");
		_tick = new DefaultViewChannel("tick");
		_selection = new DefaultViewChannel("chatSession");
		view.registerChannel("owner", _owner);
		view.registerChannel("tick", _tick);
		view.registerChannel("chatSession", _selection);

		DataListElement.Config config = (DataListElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + DataListElement.Config.class.getName() + "'"
				+ " row-channel='session' selection='chatSession'>"
				+ "<inputs><input channel='owner'/><input channel='tick'/></inputs>"
				+ "<rows><![CDATA[o -> t -> all(`tl.ai.execution:AgentSession`)"
				+ ".filter(s -> $s.get(`tl.ai.execution:AgentSession#owner`) == $o)]]></rows>"
				+ "<row-commands>"
				+ "<generic-command input='session'>"
				+ "<label><en>Delete</en></label>"
				+ "<executability><visible-if><expr><![CDATA[" + RULE + "]]></expr></visible-if></executability>"
				+ "</generic-command>"
				+ "</row-commands>"
				+ "</data-list>"));
		_owner.set(owner);
		_tick.set(Integer.valueOf(0));
		DataListElement element = new DataListElement(new DefaultInstantiationContext(getClass()), config);
		_list = (DataListControl) element.createControl(view);
		_row = view.resolveChannel(new ChannelRef("session"));
	}

	@Override
	protected void tearDown() throws Exception {
		_list.cleanupTree();
		TLContext.getContext().setCurrentPerson(_previousUser);
		super.tearDown();
	}

	/**
	 * A chat deleted from its own menu while the page is left and shown again: the row channel no
	 * longer holds it, and the page is displayed.
	 */
	public void testARowDeletedWhileAwayIsReleased() {
		TLObject chat = session("deleted while away");
		_list.attach();
		rereadRows();
		openMenu(chat);
		assertEquals(chat, _row.get());

		_list.detach();
		delete(chat);
		_list.attach();

		assertNull("The deleted chat is no row of the list any more.", _row.get());
	}

	/**
	 * A row that leaves the list while it is shown is released as soon as the list reads its rows
	 * again, so leaving and coming back finds nothing left over.
	 */
	public void testARowThatLeavesTheListIsReleased() {
		TLObject chat = session("deleted while shown");
		_list.attach();
		rereadRows();
		openMenu(chat);

		delete(chat);
		rereadRows();

		assertNull("The deleted chat is no row of the list any more.", _row.get());
		_list.detach();
		_list.attach();
	}

	/**
	 * The chat that is open when it is deleted elsewhere - in another window, in the administration
	 * - is no selection any more when the list is shown again.
	 */
	public void testADeletedSelectionIsReleased() {
		TLObject chat = session("open elsewhere");
		_list.attach();
		_selection.set(chat);

		_list.detach();
		delete(chat);
		_list.attach();

		assertNull("A deleted chat is selected no more.", _selection.get());
	}

	/** A row still in the list keeps its place on the row channel. */
	public void testALiveRowStays() {
		TLObject chat = session("kept");
		_list.attach();
		rereadRows();
		openMenu(chat);

		rereadRows();
		_list.detach();
		_list.attach();

		assertEquals(chat, _row.get());
	}

	private void openMenu(TLObject row) {
		_list.rowMenu(Map.of("id", row.tHandle().getObjectName().toExternalForm(), "x", 0, "y", 0));
	}

	/**
	 * Writes an input of the rows anew, which makes the list run its rows expression again - what a
	 * model event does in a browser window.
	 */
	private void rereadRows() {
		_tick.set(Integer.valueOf(((Integer) _tick.get()).intValue() + 1));
	}

	private static TLObject session(String title) {
		TLObject session = ChatHistoryService.createSession(title, owner);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	private static void delete(TLObject session) {
		ChatHistoryService.deleteSession(session);
		HistoryUtils.updateSessionRevision();
		assertFalse("The fixture deleted the chat.", session.tValid());
	}

	/** The knowledge base with the chat model. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestDataListDeletedObjects.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}

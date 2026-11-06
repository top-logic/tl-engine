/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.data.ReflectiveDataObject;

/**
 * Drives the commands of a {@code <chat-conversation>} as the browser sends them: a conversation
 * rendered in a window of the test thread's sub-session, for one account, and each command run
 * within an interaction of that window.
 *
 * <p>
 * For the tests that pin what a chat command answers - ownership, refusals, what a run is started
 * with - without a browser. The services a chat control reads (the view's column and field
 * services, the session service) have to be in the suite.
 * </p>
 */
public final class ChatCommandDriver {

	/** A conversation whose state a test reads. */
	public static final class Conversation extends ChatConversationControl {

		Conversation(DefaultViewContext context, ChatConversationElement.Config config) {
			super(context, config);
		}

		/** What the control wrote into its state under the given key. */
		public Object state(String key) {
			return getState(key);
		}

		/** Writes the state again as a render does - what a browser reload of the window causes. */
		public void render() {
			onBeforeWrite();
		}
	}

	private final ReactWindowRegistry _registry;

	/** The sub-session of the driver's window, the one its conversations hang on. */
	private final TLSubSessionContext _subSession;

	/** The name of the driver's window. */
	private final String _window;

	private final DefaultViewContext _view;

	private final String _chatId;

	private final Person _previousPerson;

	/**
	 * Creates a {@link ChatCommandDriver}.
	 *
	 * @param window
	 *        The name of the window the conversations are rendered in.
	 * @param chatId
	 *        The chat id the conversations join, new for every test: the bindings hang on the test
	 *        thread's sub-session.
	 */
	public ChatCommandDriver(String window, String chatId) {
		this(window, chatId, TLContextManager.getSubSession());
	}

	/**
	 * A driver of a browser window of its own: a sub-session of the test thread's session beside
	 * the test thread's, in which its conversations are rendered and its commands run.
	 *
	 * <p>
	 * For a test with windows of several accounts. Two accounts on one sub-session are one window
	 * whose person changed - a login in the window - and the window's chat then stops what the
	 * account before ran in it.
	 * </p>
	 *
	 * @see #ChatCommandDriver(String, String)
	 */
	public static ChatCommandDriver ownWindow(String window, String chatId) {
		TLContext created = new TLContext();
		created.setSessionContext(TLContextManager.getSession());
		return new ChatCommandDriver(window, chatId, created);
	}

	private ChatCommandDriver(String window, String chatId, TLSubSessionContext subSession) {
		_subSession = subSession;
		_window = window;
		subSession.getSessionContext().setIfAbsent(window, subSession);
		_previousPerson = subSession.getPerson();
		_registry = new ReactWindowRegistry(window + "-session");
		SSEUpdateQueue queue = _registry.getOrCreateQueue(window);
		_view = new DefaultViewContext(new DefaultReactContext("/app", window, queue, _registry));
		_chatId = chatId;
	}

	/** Gives the sub-session back the account it had before; a window of its own is closed. */
	public void close() {
		_subSession.setPerson(_previousPerson);
		if (_subSession != TLContextManager.getSubSession()) {
			_subSession.getSessionContext().removeSubSession(_window);
		}
	}

	/**
	 * Runs the given computation in the driver's window. A window of its own sees what is committed
	 * now, as the next request of a browser window does.
	 */
	private <T> T inWindow(ComputationEx2<T, Exception, RuntimeException> job) throws Exception {
		if (_subSession == TLContextManager.getSubSession()) {
			return job.run();
		}
		return ThreadContextManager.inContext(_subSession, () -> {
			HistoryUtils.updateSessionRevision();
			return job.run();
		});
	}

	/** {@link #inWindow(ComputationEx2)} for a computation that throws nothing checked. */
	private <T> T inWindowUnchecked(ComputationEx2<T, Exception, RuntimeException> job) {
		try {
			return inWindow(job);
		} catch (RuntimeException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * A conversation rendered and displayed for the given account; its chat id is the driver's,
	 * named after the account, so two accounts are two conversations.
	 */
	public Conversation conversation(Person person) throws Exception {
		return conversation(person, "");
	}

	/**
	 * A conversation of the given configuration, rendered and displayed for the given account.
	 *
	 * @param content
	 *        The content of the {@code <chat-conversation>} element, e.g. a {@code <context>}.
	 */
	public Conversation conversation(Person person, String content) throws Exception {
		return inWindow(() -> render(person, content));
	}

	private Conversation render(Person person, String content) throws Exception {
		_subSession.setPerson(person);
		Conversation control = new Conversation(_view,
			(ChatConversationElement.Config) TypedConfiguration.parse(
				CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
					+ " chat-id='" + _chatId + "-" + person.getName() + "'>" + content + "</chat-conversation>")));
		control.attach();
		return control;
	}

	/**
	 * The conversation of the driver's chat id, rendered and displayed for whoever is signed in to
	 * the window - what the window's tree shows again after a login in it.
	 *
	 * @param person
	 *        The account signed in to the window; <code>null</code> for a visitor.
	 */
	public Conversation window(Person person) throws Exception {
		return inWindow(() -> renderWindow(person));
	}

	private Conversation renderWindow(Person person) throws Exception {
		_subSession.setPerson(person);
		Conversation control = new Conversation(_view,
			(ChatConversationElement.Config) TypedConfiguration.parse(
				CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
					+ " chat-id='" + _chatId + "'/>")));
		control.attach();
		return control;
	}

	/**
	 * Makes the given account the one the next commands act as, in every conversation of the
	 * driver's window; <code>null</code> is a window nobody is signed in to.
	 */
	public void actAs(Person person) {
		_subSession.setPerson(person);
	}

	/** The conversation's tracker, which the runs of its commands are registered on. */
	public static TaskProgressTracker tracker(ChatControl control) {
		return control.binding().tracker();
	}

	/** The files attached to the message being written in the conversation. */
	public static ChatAttachments attachments(ChatControl control) {
		return control.binding().attachments();
	}

	/**
	 * Runs a command as the browser sends it, and answers with the properties of its reply that
	 * are set, by their names in the answer.
	 */
	public Map<String, Object> command(ChatControl control, String name, Map<String, Object> arguments) {
		return fields(inWindowUnchecked(() -> {
			try (Interaction interaction = _registry.beginInteraction()) {
				return control.executeClientCommand(name, arguments);
			}
		}));
	}

	/** Uploads one file to the conversation, as the bridge's upload does, and answers with the reply. */
	public Map<String, Object> upload(ChatControl control, String fileName, String contentType, byte[] content) {
		Part part = filePart(fileName, contentType, content);
		return fields(inWindowUnchecked(() -> {
			try (Interaction interaction = _registry.beginInteraction()) {
				return control.handleUpload(null, List.of(part));
			}
		}));
	}

	/**
	 * The properties of the given reply that are set, by their names in the answer to the command.
	 */
	private static Map<String, Object> fields(DataObject reply) {
		if (reply == null) {
			return Map.of();
		}
		ReflectiveDataObject message = (ReflectiveDataObject) reply;
		Map<String, Object> result = new LinkedHashMap<>();
		for (String property : message.properties()) {
			Object value = message.get(property);
			if (value != null) {
				result.put(property, value);
			}
		}
		return result;
	}

	/** A multipart part of a file, as the servlet container hands it to an upload. */
	private static Part filePart(String fileName, String contentType, byte[] content) {
		return (Part) Proxy.newProxyInstance(Part.class.getClassLoader(), new Class<?>[] { Part.class },
			(proxy, method, args) -> switch (method.getName()) {
				case "getName" -> UploadSupport.FILE_PART;
				case "getSubmittedFileName" -> fileName;
				case "getContentType" -> contentType;
				case "getSize" -> Long.valueOf(content.length);
				case "getInputStream" -> new ByteArrayInputStream(content);
				default -> null;
			});
	}
}

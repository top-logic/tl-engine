/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactCommandReply;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContextManager;

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
	}

	private final ReactWindowRegistry _registry;

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
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		subSession.getSessionContext().setIfAbsent(window, subSession);
		_previousPerson = subSession.getPerson();
		_registry = new ReactWindowRegistry(window + "-session");
		SSEUpdateQueue queue = _registry.getOrCreateQueue(window);
		_view = new DefaultViewContext(new DefaultReactContext("/app", window, queue, _registry));
		_chatId = chatId;
	}

	/** Gives the sub-session back the account it had before. */
	public void close() {
		TLContextManager.getSubSession().setPerson(_previousPerson);
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
		TLContextManager.getSubSession().setPerson(person);
		Conversation control = new Conversation(_view,
			(ChatConversationElement.Config) TypedConfiguration.parse(
				CharacterContents.newContent("<chat-conversation xmlns:config='http://www.top-logic.com/ns/config/6.0'"
					+ " config:interface='" + ChatConversationElement.Config.class.getName() + "'"
					+ " chat-id='" + _chatId + "-" + person.getName() + "'>" + content + "</chat-conversation>")));
		control.attach();
		return control;
	}

	/** The conversation's tracker, which the runs of its commands are registered on. */
	public static TaskProgressTracker tracker(ChatControl control) {
		return control.binding().tracker();
	}

	/** The files attached to the message being written in the conversation. */
	public static ChatAttachments attachments(ChatControl control) {
		return control.binding().attachments();
	}

	/** Runs a command as the browser sends it, and answers with the fields of its reply. */
	public Map<String, Object> command(ChatControl control, String name, Map<String, Object> arguments) {
		HandlerResult result;
		try (Interaction interaction = _registry.beginInteraction()) {
			result = control.executeClientCommand(name, arguments);
		}
		ReactCommandReply reply = ReactCommandReply.of(result);
		return reply == null ? Map.of() : reply.getFields();
	}

	/** Uploads one file to the conversation, as the bridge's upload does, and answers with the reply. */
	public Map<String, Object> upload(ChatControl control, String fileName, String contentType, byte[] content) {
		Part part = filePart(fileName, contentType, content);
		HandlerResult result;
		try (Interaction interaction = _registry.beginInteraction()) {
			result = control.handleUpload(null, List.of(part));
		}
		ReactCommandReply reply = ReactCommandReply.of(result);
		return reply == null ? Map.of() : reply.getFields();
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

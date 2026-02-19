/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.top_logic.basic.LongID;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * Service for CRUD operations on persistent ChatSession objects.
 *
 * <p>
 * Each ChatSession belongs to a user (via WithOwner) and stores chat messages
 * as a JSON blob in a CLOB attribute.
 * </p>
 */
public class ChatHistoryService {

	private static final String MODULE_NAME = "tl.ai.chat";

	private static final String TYPE_NAME = "ChatSession";

	private static final String TABLE_NAME = "ChatSession";

	private static final String ATTR_TITLE = "title";

	private static final String ATTR_MESSAGES = "messages";

	private static final String ATTR_CREATED_AT = "createdAt";

	private static final String ATTR_LAST_UPDATED = "lastUpdated";

	private static final String ATTR_OWNER = "owner";

	private ChatHistoryService() {
		// Static utility class
	}

	/**
	 * Returns all chat sessions owned by the current user, sorted by lastUpdated descending.
	 */
	public static List<TLObject> getSessionsForCurrentUser() {
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
		if (currentPerson == null) {
			return Collections.emptyList();
		}

		TLClass sessionType = getChatSessionType();
		if (sessionType == null) {
			return Collections.emptyList();
		}

		List<TLObject> allInstances = MetaElementUtil.getAllInstancesOf(sessionType, TLObject.class);
		List<TLObject> userSessions = new ArrayList<>();
		for (TLObject obj : allInstances) {
			Object owner = obj.tValueByName(ATTR_OWNER);
			if (currentPerson.equals(owner)) {
				userSessions.add(obj);
			}
		}

		userSessions.sort((a, b) -> {
			Date dateA = (Date) a.tValueByName(ATTR_LAST_UPDATED);
			Date dateB = (Date) b.tValueByName(ATTR_LAST_UPDATED);
			if (dateA == null && dateB == null) return 0;
			if (dateA == null) return 1;
			if (dateB == null) return -1;
			return dateB.compareTo(dateA);
		});

		return userSessions;
	}

	/**
	 * Creates a new chat session for the current user.
	 *
	 * @param title
	 *        the session title.
	 * @return the created session object, or null on failure.
	 */
	public static TLObject createSession(String title) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Create chat session"))) {
			TLClass sessionType = getChatSessionType();
			if (sessionType == null) {
				return null;
			}

			TLFactory factory = ModelService.getInstance().getFactory();
			TLObject session = factory.createObject(sessionType);
			Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();

			session.tUpdateByName(ATTR_TITLE, title);
			session.tUpdateByName(ATTR_CREATED_AT, new Date());
			session.tUpdateByName(ATTR_LAST_UPDATED, new Date());
			session.tUpdateByName(ATTR_OWNER, currentPerson);

			tx.commit();
			return session;
		} catch (Exception ex) {
			Logger.error("Failed to create chat session: " + ex.getMessage(), ex, ChatHistoryService.class);
			return null;
		}
	}

	/**
	 * Updates the title of a chat session.
	 *
	 * @param session
	 *        the session object.
	 * @param newTitle
	 *        the new title.
	 */
	public static void updateTitle(TLObject session, String newTitle) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Update chat session title"))) {
			session.tUpdateByName(ATTR_TITLE, newTitle);
			session.tUpdateByName(ATTR_LAST_UPDATED, new Date());
			tx.commit();
		} catch (Exception ex) {
			Logger.error("Failed to update chat session title: " + ex.getMessage(), ex, ChatHistoryService.class);
		}
	}

	/**
	 * Saves the messages JSON blob to a chat session.
	 *
	 * @param session
	 *        the session object.
	 * @param messagesJson
	 *        the JSON string of messages.
	 */
	public static void saveMessages(TLObject session, String messagesJson) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Save chat messages"))) {
			session.tUpdateByName(ATTR_MESSAGES, messagesJson);
			session.tUpdateByName(ATTR_LAST_UPDATED, new Date());
			tx.commit();
		} catch (Exception ex) {
			Logger.error("Failed to save chat messages: " + ex.getMessage(), ex, ChatHistoryService.class);
		}
	}

	/**
	 * Deletes a chat session.
	 *
	 * @param session
	 *        the session object.
	 */
	public static void deleteSession(TLObject session) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(ResKey.text("Delete chat session"))) {
			session.tDelete();
			tx.commit();
		} catch (Exception ex) {
			Logger.error("Failed to delete chat session: " + ex.getMessage(), ex, ChatHistoryService.class);
		}
	}

	/**
	 * Loads the messages JSON from a session.
	 *
	 * @param session
	 *        the session object.
	 * @return the messages JSON string, or null.
	 */
	public static String getMessagesJson(TLObject session) {
		return (String) session.tValueByName(ATTR_MESSAGES);
	}

	/**
	 * Returns the title of a session.
	 */
	public static String getTitle(TLObject session) {
		return (String) session.tValueByName(ATTR_TITLE);
	}

	/**
	 * Finds a ChatSession by its KnowledgeObject ID.
	 *
	 * @param objectId
	 *        the string representation of the object ID.
	 * @return the session object, or null if not found.
	 */
	public static TLObject findSessionById(String objectId) {
		if (objectId == null || objectId.isEmpty()) {
			return null;
		}
		long id;
		try {
			id = Long.parseLong(objectId);
		} catch (NumberFormatException ex) {
			return null; // Invalid ID format from client — not a server error
		}
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try {
			KnowledgeObject ko = kb.getKnowledgeObject(TABLE_NAME, LongID.valueOf(id));
			if (ko != null) {
				return ko.getWrapper();
			}
		} catch (Exception ex) {
			Logger.error("Failed to find chat session " + objectId + ": " + ex.getMessage(), ex, ChatHistoryService.class);
		}
		return null;
	}

	/**
	 * Checks if the given session is owned by the current user.
	 */
	public static boolean isOwnedByCurrentUser(TLObject session) {
		Person currentPerson = TLContext.getContext().getCurrentPersonWrapper();
		Object owner = session.tValueByName(ATTR_OWNER);
		return currentPerson != null && currentPerson.equals(owner);
	}

	private static TLClass getChatSessionType() {
		TLModel model = ModelService.getApplicationModel();
		TLModule module = model.getModule(MODULE_NAME);
		if (module == null) {
			Logger.error("Module not found: " + MODULE_NAME, ChatHistoryService.class);
			return null;
		}
		return (TLClass) module.getType(TYPE_NAME);
	}

}

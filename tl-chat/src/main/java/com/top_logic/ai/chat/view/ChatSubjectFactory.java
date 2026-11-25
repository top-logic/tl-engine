/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * Creates the subject of a conversation with its first message, see
 * {@link ChatConversationElement.Config#getSubjectFactory()}.
 *
 * <p>
 * Offered to the chat by a displayed {@code <chat-conversation>}
 * ({@link ChatViewBinding#offerSubjectFactory(ChatSubjectFactory)}) and called by the send that
 * creates a conversation while the subject channel holds no object.
 * </p>
 */
public final class ChatSubjectFactory {

	private final QueryExecutor _function;

	private final TLStructuredType _type;

	private final ViewChannel _subject;

	/**
	 * Creates a {@link ChatSubjectFactory}.
	 *
	 * @param function
	 *        The function of the text of the first message and the list of its files.
	 * @param type
	 *        The type of the objects the function creates.
	 * @param subject
	 *        The channel that shows the created subject; <code>null</code> for none.
	 */
	public ChatSubjectFactory(QueryExecutor function, TLStructuredType type, ViewChannel subject) {
		_function = function;
		_type = type;
		_subject = subject;
	}

	/** The type of the objects {@link #create(String, List)} creates. */
	public TLStructuredType type() {
		return _type;
	}

	/**
	 * Creates the subject of a conversation, in a transaction of its own.
	 *
	 * @param text
	 *        The text of the first message.
	 * @param attachments
	 *        The files attached to the first message.
	 * @return The created subject, committed; never <code>null</code>.
	 * @throws TopLogicException
	 *         When the function refuses the message, or creates no object of {@link #type()}; nothing
	 *         is created then.
	 */
	public TLObject create(String text, List<AttachmentDto> attachments) throws TopLogicException {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.CREATED_CHAT_SUBJECT);
		try {
			Object result = _function.execute(text, binaryData(attachments));
			if (!(result instanceof TLObject subject) || !TLModelUtil.isCompatibleInstance(_type, subject)) {
				throw new TopLogicException(
					I18NConstants.ERROR_SUBJECT_NOT_OF_TYPE__TYPE.fill(TLModelUtil.qualifiedName(_type)));
			}
			tx.commit();
			return subject;
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Deletes a subject {@link #create(String, List)} created for a conversation that could not be
	 * created, so that no subject is left without its conversation.
	 */
	public void discard(TLObject subject) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction(I18NConstants.DISCARDED_CHAT_SUBJECT);
		try {
			subject.tDelete();
			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	/** Shows the given subject of a created conversation in the subject channel, if there is one. */
	public void show(TLObject subject) {
		if (_subject != null) {
			_subject.set(subject);
		}
	}

	private static List<BinaryData> binaryData(List<AttachmentDto> attachments) {
		List<BinaryData> result = new ArrayList<>(attachments.size());
		for (AttachmentDto attachment : attachments) {
			result.add(BinaryDataFactory.createBinaryData(Base64.getDecoder().decode(attachment.getBase64Data()),
				attachment.getMimeType(), attachment.getFileName()));
		}
		return result;
	}

}

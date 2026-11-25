/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.backend.AttachmentPolicy;
import com.top_logic.ai.chat.api.messages.DraftAttachmentDto;
import com.top_logic.basic.util.ResKey;
import com.top_logic.ai.core.message.ImageContent;

/**
 * The files attached to the message a conversation's user is writing, held on the server until the
 * message is sent.
 *
 * <p>
 * A file reaches the server as an upload to a chat control ({@link ChatControl#handleUpload}) and
 * waits here; the browser shows what {@link #list()} says and names a file by its id only to take
 * it off the message again ({@link ChatControl#CMD_REMOVE_ATTACHMENT}). The send takes the files
 * that are here when it starts ({@link #entries()}) and removes exactly those once the message has
 * gone ({@link #removeAll(Collection)}), so a file attached while the send was on its way stays for
 * the next message.
 * </p>
 *
 * <p>
 * One per conversation of a window, like the text the composer holds: switching to another chat of
 * the same conversation keeps the files, as it keeps the text.
 * </p>
 *
 * <p>
 * Which files a message may carry is the {@link AttachmentPolicy} of the conversation's answerer
 * ({@link com.top_logic.ai.chat.backend.ConversationBackendService#attachmentPolicy(com.top_logic.ai.core.execution.model.AgentSession)}),
 * checked when a file is attached and again when the message is sent.
 * </p>
 */
public final class ChatAttachments {

	/**
	 * One attached file: what the browser knows of it, and its content.
	 *
	 * @param id
	 *        The identifier the browser names the file with, e.g. to take it off the message.
	 * @param attachment
	 *        The file's name, media type, size and content, as the message carries it.
	 */
	public record Entry(String id, AttachmentDto attachment) {

		/** What the composer shows of this file. */
		DraftAttachmentDto toDraft() {
			return DraftAttachmentDto.create()
				.setId(id)
				.setFileName(attachment.getFileName())
				.setMimeType(attachment.getMimeType())
				.setSize(attachment.getSize());
		}
	}

	private final List<Entry> _entries = new ArrayList<>();

	private final List<Runnable> _listeners = new CopyOnWriteArrayList<>();

	/**
	 * Attaches a file, unless the message cannot carry it.
	 *
	 * @param fileName
	 *        The name the file was uploaded under.
	 * @param mimeType
	 *        The media type the browser stated.
	 * @param content
	 *        The file's bytes.
	 * @param policy
	 *        Which files the answerer of the conversation takes.
	 * @return Why the file was not attached, <code>null</code> when it was.
	 */
	public ResKey add(String fileName, String mimeType, byte[] content, AttachmentPolicy policy) {
		AttachmentDto attachment = AttachmentDto.create()
			.setFileName(fileName)
			.setMimeType(ImageContent.normalizeMediaType(mimeType))
			.setSize(content.length)
			.setBase64Data(Base64.getEncoder().encodeToString(content));
		synchronized (_entries) {
			ResKey refusal = policy.check(fileName, content.length, _entries.size(), sizeOf(_entries));
			if (refusal != null) {
				return refusal;
			}
			_entries.add(new Entry(UUID.randomUUID().toString(), attachment));
		}
		changed();
		return null;
	}

	/**
	 * Why a message may not carry the given files, or <code>null</code> if it may.
	 *
	 * <p>
	 * Asked when the message is sent: the files were attached under the policy of the chat shown
	 * then, and the chat the message goes to may take less.
	 * </p>
	 *
	 * @param entries
	 *        The files of the message, in the order they were attached.
	 * @param policy
	 *        Which files the answerer of the message's conversation takes.
	 * @return The reason of the refusal of the first file the message may not carry.
	 */
	public static ResKey refusal(List<Entry> entries, AttachmentPolicy policy) {
		int count = 0;
		long size = 0;
		for (Entry entry : entries) {
			AttachmentDto attachment = entry.attachment();
			ResKey refusal = policy.check(attachment.getFileName(), attachment.getSize(), count, size);
			if (refusal != null) {
				return refusal;
			}
			count++;
			size += attachment.getSize();
		}
		return null;
	}

	private static long sizeOf(List<Entry> entries) {
		long result = 0;
		for (Entry entry : entries) {
			result += entry.attachment().getSize();
		}
		return result;
	}

	/**
	 * Takes the file with the given id off the message.
	 *
	 * @return Whether there was such a file.
	 */
	public boolean remove(String id) {
		boolean removed;
		synchronized (_entries) {
			removed = _entries.removeIf(entry -> entry.id().equals(id));
		}
		if (removed) {
			changed();
		}
		return removed;
	}

	/**
	 * Takes the given files off the message, those a send has just carried; a file attached since is
	 * left.
	 */
	public void removeAll(Collection<Entry> sent) {
		if (sent.isEmpty()) {
			return;
		}
		synchronized (_entries) {
			_entries.removeAll(sent);
		}
		changed();
	}

	/** The files attached now, in the order they were attached. */
	public List<Entry> entries() {
		synchronized (_entries) {
			return List.copyOf(_entries);
		}
	}

	/** What the composer shows of the files attached now. */
	public List<DraftAttachmentDto> list() {
		List<Entry> entries = entries();
		List<DraftAttachmentDto> result = new ArrayList<>(entries.size());
		for (Entry entry : entries) {
			result.add(entry.toDraft());
		}
		return result;
	}

	/** The content of the given files, as a message carries them. */
	public static List<AttachmentDto> contentOf(List<Entry> entries) {
		List<AttachmentDto> result = new ArrayList<>(entries.size());
		for (Entry entry : entries) {
			result.add(entry.attachment());
		}
		return result;
	}

	/** Calls the given listener whenever a file is attached or taken off. */
	public void addListener(Runnable listener) {
		_listeners.add(listener);
	}

	/** Undoes {@link #addListener(Runnable)}. */
	public void removeListener(Runnable listener) {
		_listeners.remove(listener);
	}

	private void changed() {
		for (Runnable listener : _listeners) {
			listener.run();
		}
	}
}

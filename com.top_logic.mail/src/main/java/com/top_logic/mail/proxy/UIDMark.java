/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import java.util.Map.Entry;
import java.util.SortedMap;

import jakarta.mail.UIDFolder;

import com.top_logic.basic.util.ComputationEx;

/**
 * High-water mark of the messages of a {@link UIDFolder} that have been handled.
 *
 * <p>
 * A mark consists of the {@link UIDFolder#getUIDValidity() UID validity} of the folder and the
 * {@link #getLastUID() highest UID} up to which all messages have been handled. A mark is only
 * meaningful for the UID validity it was created for: when the server assigns a different
 * validity, the UIDs of the folder have been renumbered and the mark must be re-initialized, see
 * {@link #start(UIDMark, long, InitialMails, ComputationEx)}.
 * </p>
 *
 * <p>
 * A mark is stored as a single string (see {@link #format()} and {@link #parse(String)}), so that
 * validity and UID are always written together.
 * </p>
 */
public final class UIDMark {

	/**
	 * Separator between the UID validity and the last UID in the {@link #format() external form}.
	 */
	public static final char SEPARATOR = ':';

	private final long _uidValidity;

	private final long _lastUID;

	/**
	 * Creates a {@link UIDMark}.
	 *
	 * @param uidValidity
	 *        See {@link #getUIDValidity()}.
	 * @param lastUID
	 *        See {@link #getLastUID()}.
	 */
	public UIDMark(long uidValidity, long lastUID) {
		if (uidValidity < 0) {
			throw new IllegalArgumentException("Negative UID validity: " + uidValidity);
		}
		if (lastUID < 0) {
			throw new IllegalArgumentException("Negative UID: " + lastUID);
		}
		_uidValidity = uidValidity;
		_lastUID = lastUID;
	}

	/**
	 * The {@link UIDFolder#getUIDValidity() UID validity} of the folder this mark belongs to.
	 */
	public long getUIDValidity() {
		return _uidValidity;
	}

	/**
	 * The highest UID up to which all messages have been handled.
	 *
	 * <p>
	 * Messages with an UID greater than this value are not yet handled. The value
	 * <code>0</code> means that no message has been handled so far.
	 * </p>
	 */
	public long getLastUID() {
		return _lastUID;
	}

	/**
	 * Whether this mark is valid for a folder with the given UID validity.
	 */
	public boolean appliesTo(long uidValidity) {
		return _uidValidity == uidValidity;
	}

	/**
	 * Computes the mark after the messages with UIDs greater than {@link #getLastUID()} have been
	 * handled.
	 *
	 * <p>
	 * The mark advances over all successfully handled messages in ascending UID order and stops
	 * just before the first message whose handling failed. Such a message, and all following
	 * messages, are handled again starting from the resulting mark.
	 * </p>
	 *
	 * @param results
	 *        Whether handling of the message with the given UID succeeded. Entries with an UID not
	 *        greater than {@link #getLastUID()} are ignored.
	 * @return The advanced mark, or this mark, if it does not change.
	 */
	public UIDMark advance(SortedMap<Long, Boolean> results) {
		long last = _lastUID;
		for (Entry<Long, Boolean> entry : results.tailMap(Long.valueOf(_lastUID + 1)).entrySet()) {
			long uid = entry.getKey().longValue();
			if (!entry.getValue().booleanValue()) {
				last = Math.max(last, uid - 1);
				break;
			}
			last = uid;
		}
		return withLastUID(last);
	}

	private UIDMark withLastUID(long lastUID) {
		if (lastUID == _lastUID) {
			return this;
		}
		return new UIDMark(_uidValidity, lastUID);
	}

	/**
	 * The external form of this mark.
	 *
	 * @see #parse(String)
	 */
	public String format() {
		return Long.toString(_uidValidity) + SEPARATOR + Long.toString(_lastUID);
	}

	/**
	 * Parses the {@link #format() external form} of a mark.
	 *
	 * @param value
	 *        The external form, may be <code>null</code>.
	 * @return The parsed mark, or <code>null</code> if the given value is <code>null</code> or empty.
	 * @throws IllegalArgumentException
	 *         If the given value is not a valid external form of a mark.
	 */
	public static UIDMark parse(String value) throws IllegalArgumentException {
		if (value == null || value.isEmpty()) {
			return null;
		}
		int separatorIndex = value.indexOf(SEPARATOR);
		if (separatorIndex < 0) {
			throw new IllegalArgumentException("Missing separator '" + SEPARATOR + "' in UID mark: " + value);
		}
		try {
			long uidValidity = Long.parseLong(value.substring(0, separatorIndex));
			long lastUID = Long.parseLong(value.substring(separatorIndex + 1));
			return new UIDMark(uidValidity, lastUID);
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid UID mark: " + value, ex);
		}
	}

	/**
	 * Determines the mark to start reading a folder from.
	 *
	 * @param stored
	 *        The mark stored for the folder, <code>null</code> if there is none.
	 * @param uidValidity
	 *        The current {@link UIDFolder#getUIDValidity() UID validity} of the folder.
	 * @param mode
	 *        How to handle the mails present in the folder, if the stored mark is missing or does
	 *        not {@link #appliesTo(long) apply} to the current UID validity.
	 * @param highestUID
	 *        Computation of the highest UID currently assigned in the folder, see
	 *        {@link #highestUID(long, long)}. Only called, if the mark must be initialized.
	 * @return The stored mark, if it applies to the given UID validity, an
	 *         {@link #initial(long, InitialMails, long) initial mark} otherwise.
	 */
	public static <E extends Throwable> UIDMark start(UIDMark stored, long uidValidity, InitialMails mode,
			ComputationEx<Long, E> highestUID) throws E {
		if (stored != null && stored.appliesTo(uidValidity)) {
			return stored;
		}
		long highest = mode == InitialMails.SKIP ? highestUID.run().longValue() : 0;
		return initial(uidValidity, mode, highest);
	}

	/**
	 * Creates the mark for a folder that is read for the first time.
	 *
	 * @param uidValidity
	 *        The {@link UIDFolder#getUIDValidity() UID validity} of the folder.
	 * @param mode
	 *        How to handle the mails already present in the folder.
	 * @param highestUID
	 *        The highest UID currently assigned in the folder, see
	 *        {@link #highestUID(long, long)}. Only relevant for {@link InitialMails#SKIP}.
	 */
	public static UIDMark initial(long uidValidity, InitialMails mode, long highestUID) {
		switch (mode) {
			case SKIP:
				return new UIDMark(uidValidity, Math.max(highestUID, 0));
			case PROCESS:
				return new UIDMark(uidValidity, 0);
		}
		throw new IllegalArgumentException("No such mode: " + mode);
	}

	/**
	 * The highest UID assigned in a folder.
	 *
	 * @param uidNext
	 *        The {@link UIDFolder#getUIDNext() next UID} of the folder, a value less than
	 *        <code>1</code> if the server does not report it.
	 * @param lastMessageUID
	 *        The UID of the last message in the folder, <code>0</code> if the folder is empty.
	 *        Only used, if the next UID is unknown.
	 */
	public static long highestUID(long uidNext, long lastMessageUID) {
		if (uidNext > 0) {
			return uidNext - 1;
		}
		return Math.max(lastMessageUID, 0);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof UIDMark)) {
			return false;
		}
		UIDMark other = (UIDMark) obj;
		return _uidValidity == other._uidValidity && _lastUID == other._lastUID;
	}

	@Override
	public int hashCode() {
		return Long.hashCode(_uidValidity) * 31 + Long.hashCode(_lastUID);
	}

	@Override
	public String toString() {
		return format();
	}

}

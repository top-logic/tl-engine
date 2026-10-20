/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * An image content block within a message.
 *
 * @param url       the image URL or data URI
 * @param mediaType the MIME type (e.g. "image/png")
 *
 * @see ContentPart
 * @see #ACCEPTED_MEDIA_TYPES
 */
public record ImageContent(String url, String mediaType) implements ContentPart {

	/**
	 * The image media types a provider request can carry, in the order they are named to a caller.
	 *
	 * <p>
	 * Every one of the four request builders renders an {@link ImageContent} by handing its
	 * {@link #mediaType()} - or the {@code data:} URL built from it - to the provider unchanged, so
	 * this set is what the providers themselves accept and nothing on this side can widen it. An
	 * image of any other type has nowhere to go, and the place to say so is where the payload is
	 * built: refused there it names the type and the file, while the same image sent anyway comes
	 * back as a provider error that no script author or user can act on.
	 * </p>
	 *
	 * <p>
	 * Declared here, next to the record all four builders render, because both the core and the chat
	 * UI have to decide the same question and must not drift apart while doing it.
	 * </p>
	 *
	 * @see #isAccepted(String)
	 */
	public static final Set<String> ACCEPTED_MEDIA_TYPES = Collections.unmodifiableSet(
		new LinkedHashSet<>(List.of("image/png", "image/jpeg", "image/gif", "image/webp")));

	/**
	 * Creates an {@link ImageContent}.
	 *
	 * <p>
	 * Deliberately does not enforce {@link #ACCEPTED_MEDIA_TYPES}: this record is also how a stored
	 * conversation is read back, and a log written before a type was narrowed must stay readable
	 * rather than fail its whole session at deserialization time. What may be <em>sent</em> is
	 * therefore decided where a payload is built, by {@link #isAccepted(String)}.
	 * </p>
	 */
	public ImageContent {
		Objects.requireNonNull(url, "Image URL must not be null.");
		Objects.requireNonNull(mediaType, "Media type must not be null.");
	}

	/**
	 * The canonical form of a declared image media type: trimmed, lower-cased, without its MIME
	 * parameters, and with {@code image/jpg} resolved to {@code image/jpeg}.
	 *
	 * <p>
	 * The {@code jpg} spelling is common in the wild and accepted by no provider - Anthropic rejects
	 * it outright - so it is resolved once, here, rather than in each builder that would otherwise
	 * have to know about it. Callers normalize before they build the {@code data:} URL, so the URL
	 * and the media type always state the same thing.
	 * </p>
	 *
	 * @param mediaType
	 *        The declared type; may be {@code null} or carry parameters.
	 * @return The canonical type, or the empty string when nothing was declared. Never {@code null}.
	 */
	public static String normalizeMediaType(String mediaType) {
		if (mediaType == null) {
			return "";
		}
		int semicolon = mediaType.indexOf(';');
		String bare = (semicolon < 0 ? mediaType : mediaType.substring(0, semicolon))
			.trim().toLowerCase(Locale.ROOT);
		return "image/jpg".equals(bare) ? "image/jpeg" : bare;
	}

	/**
	 * Whether an image of the given media type can be sent to a provider at all.
	 *
	 * @param mediaType
	 *        The declared type; may be {@code null} or carry parameters, and is
	 *        {@link #normalizeMediaType(String) normalized} first.
	 * @return Whether the type is one of {@link #ACCEPTED_MEDIA_TYPES}.
	 */
	public static boolean isAccepted(String mediaType) {
		return ACCEPTED_MEDIA_TYPES.contains(normalizeMediaType(mediaType));
	}

	/**
	 * The {@link #ACCEPTED_MEDIA_TYPES} as one comma-separated string, for the message that refuses
	 * everything else.
	 */
	public static String acceptedMediaTypes() {
		return String.join(", ", ACCEPTED_MEDIA_TYPES);
	}
}

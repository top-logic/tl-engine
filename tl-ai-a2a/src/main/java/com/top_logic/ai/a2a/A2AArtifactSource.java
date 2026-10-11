/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a;

import java.util.List;

import com.top_logic.ai.a2a.protocol.ArtifactRef;

/**
 * Application hook: publishes artifacts by reference, so agents hand each other
 * <code>tl-artifact://&lt;app&gt;/&lt;kind&gt;/&lt;id&gt;</code> instead of the content and a reader
 * fetches only what it needs ({@code fetchArtifact(ref, section)}).
 *
 * <p>
 * Asked for every read of a reference of this application whose kind is not {@code task} (the
 * artifacts of A2A tasks, which the module serves itself), in the {@code TLContext} of the reading
 * account, so its read rights apply. Sources are asked in order ({@code <artifact-sources>} on
 * {@link A2AService}); the first non-<code>null</code> answer wins.
 * </p>
 */
public interface A2AArtifactSource {

	/**
	 * The artifact a reference of this application names, or <code>null</code> if this source does
	 * not know it.
	 */
	Artifact resolve(ArtifactRef ref);

	/**
	 * A published artifact.
	 *
	 * @param mediaType
	 *        E.g. {@code text/markdown}, {@code application/json}.
	 * @param content
	 *        A {@link String}, or a JSON value (maps, lists, strings, numbers, booleans).
	 * @param version
	 *        The version served, or <code>null</code>.
	 * @param sections
	 *        The sections a reader may ask for; <code>null</code>: derived from the content (Markdown
	 *        headings, JSON members).
	 */
	record Artifact(String name, String mediaType, Object content, String version, List<String> sections) {

		/** An artifact whose sections are derived from its content. */
		public Artifact(String name, String mediaType, Object content) {
			this(name, mediaType, content, null, null);
		}
	}
}

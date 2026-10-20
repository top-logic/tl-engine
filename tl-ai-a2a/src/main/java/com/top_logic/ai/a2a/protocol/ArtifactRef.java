/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

/**
 * A reference to a published artifact of an application:
 * <code>tl-artifact://&lt;app&gt;/&lt;kind&gt;/&lt;id&gt;[@version]</code>.
 *
 * <p>
 * What agents hand each other instead of the artifact itself: a document, a design, the
 * full answer of a task. The receiver reads only what it needs with {@code fetchArtifact(ref,
 * section)}. The id may contain slashes ({@code task/<taskId>/<artifact name>}).
 * </p>
 *
 * @param app
 *        The application that publishes the artifact, as it names itself ({@code app} of
 *        {@code A2AService}).
 * @param kind
 *        What kind of artifact, e.g. {@code spec}, {@code frame}, {@code task}.
 * @param id
 *        The artifact within its kind.
 * @param version
 *        A version, or <code>null</code> for the current one.
 */
public record ArtifactRef(String app, String kind, String id, String version) {

	/** The URI scheme of artifact references. */
	public static final String SCHEME = "tl-artifact://";

	/** Media type of a message part that carries a reference instead of content. */
	public static final String MEDIA_TYPE = "application/vnd.toplogic.artifact-ref";

	/** Kind of the artifacts of A2A tasks, served by every application with this module. */
	public static final String TASK_KIND = "task";

	/**
	 * Reads a reference.
	 *
	 * @return The reference, or <code>null</code> if the text is none.
	 */
	public static ArtifactRef parse(String text) {
		if (text == null) {
			return null;
		}
		String value = text.trim();
		if (!value.startsWith(SCHEME)) {
			return null;
		}
		String rest = value.substring(SCHEME.length());
		int appEnd = rest.indexOf('/');
		if (appEnd <= 0) {
			return null;
		}
		int kindEnd = rest.indexOf('/', appEnd + 1);
		if (kindEnd <= appEnd + 1 || kindEnd == rest.length() - 1) {
			return null;
		}
		String id = rest.substring(kindEnd + 1);
		String version = null;
		int at = id.lastIndexOf('@');
		if (at > 0) {
			version = id.substring(at + 1);
			id = id.substring(0, at);
		}
		return new ArtifactRef(rest.substring(0, appEnd), rest.substring(appEnd + 1, kindEnd), id,
			version == null || version.isEmpty() ? null : version);
	}

	/**
	 * The reference to an artifact of an A2A task.
	 */
	public static ArtifactRef ofTask(String app, String taskId, String artifactName) {
		return new ArtifactRef(app, TASK_KIND, taskId + "/" + artifactName, null);
	}

	/**
	 * The task of an artifact {@link #ofTask(String, String, String) of an A2A task}, else
	 * <code>null</code>.
	 */
	public String taskId() {
		int slash = id.indexOf('/');
		return TASK_KIND.equals(kind) && slash > 0 ? id.substring(0, slash) : null;
	}

	/**
	 * Whether a text is a reference.
	 */
	public static boolean isRef(String text) {
		return parse(text) != null;
	}

	@Override
	public String toString() {
		return SCHEME + app + "/" + kind + "/" + id + (version == null ? "" : "@" + version);
	}
}

/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

/**
 * The Maven packagings of TopLogic modules.
 *
 * <p>
 * A project with one of these packagings is built like a project inheriting from the TopLogic
 * parent POM named by {@link #getParentArtifactId()}, but may have a parent of its own or none.
 * Its main artifact is a jar, which dependent projects reference like any other jar dependency.
 * </p>
 *
 * @see TLParentInjector
 */
public enum TLPackaging {

	/**
	 * A runnable TopLogic application, built with the configuration of {@link #TL_PARENT_APP}.
	 */
	APP(TLPackaging.APP_ID, TLPackaging.TL_PARENT_APP),

	/**
	 * A TopLogic fragment module used by other modules and applications, built with the
	 * configuration of {@link #TL_PARENT_CORE}.
	 */
	FRAGMENT(TLPackaging.FRAGMENT_ID, TLPackaging.TL_PARENT_CORE);

	/**
	 * The packaging ID of {@link #APP}.
	 */
	public static final String APP_ID = "tl-app";

	/**
	 * The packaging ID of {@link #FRAGMENT}.
	 */
	public static final String FRAGMENT_ID = "tl-fragment";

	/**
	 * The group ID of the TopLogic parent POMs.
	 */
	public static final String TL_GROUP_ID = "com.top-logic";

	/**
	 * The artifact ID of the parent POM of TopLogic applications.
	 */
	public static final String TL_PARENT_APP = "tl-parent-app";

	/**
	 * The artifact ID of the parent POM of TopLogic fragment modules.
	 */
	public static final String TL_PARENT_CORE = "tl-parent-core";

	/**
	 * The file extension of the main artifact of a project with a TopLogic packaging.
	 */
	public static final String ARTIFACT_EXTENSION = "jar";

	/**
	 * The language of the main artifact of a project with a TopLogic packaging.
	 */
	public static final String ARTIFACT_LANGUAGE = "java";

	private final String _id;

	private final String _parentArtifactId;

	private TLPackaging(String id, String parentArtifactId) {
		_id = id;
		_parentArtifactId = parentArtifactId;
	}

	/**
	 * The packaging ID used in the <code>packaging</code> element of a POM.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The artifact ID of the TopLogic parent POM supplying the build configuration.
	 */
	public String getParentArtifactId() {
		return _parentArtifactId;
	}

	/**
	 * The {@link TLPackaging} with the given packaging ID.
	 *
	 * @return The packaging, or <code>null</code> if the given ID is no TopLogic packaging.
	 */
	public static TLPackaging fromId(String id) {
		for (TLPackaging packaging : values()) {
			if (packaging.getId().equals(id)) {
				return packaging;
			}
		}
		return null;
	}

	/**
	 * Whether the given artifact ID names a TopLogic parent POM that supplies the build
	 * configuration of a {@link TLPackaging}.
	 */
	public static boolean isTLParent(String groupId, String artifactId) {
		if (!TL_GROUP_ID.equals(groupId)) {
			return false;
		}
		for (TLPackaging packaging : values()) {
			if (packaging.getParentArtifactId().equals(artifactId)) {
				return true;
			}
		}
		return false;
	}
}

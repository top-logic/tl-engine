/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.war;

import java.io.File;
import java.util.Objects;

import org.apache.maven.artifact.Artifact;

/**
 * Specification of an additional overlay to use when building the final application WAR.
 */
public class Overlay {

	private String groupId;

	private String artifactId;

	private String classifier = null;

	/** default overlay type is war */
	private String type = "war";

	private String deployAspects = null;

	private File path = null;

	private boolean skip = false;

	/**
	 * The <code>groupId</code> of the artifact to use as overlay.
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @see #getGroupId()
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * The <code>artifactId</code> of the artifact to use as overlay.
	 * 
	 * <p>
	 * Only one of the options, {@link #getArtifactId()}, {@link #getDeployAspects()}, or
	 * {@link #getPath()} must be given.
	 * </p>
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @see #getArtifactId()
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * The classifier of the artifact to use as overlay.
	 */
	public String getClassifier() {
		return classifier;
	}

	/**
	 * The type of the artifact to use as overlay.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @see #getType()
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @see #getClassifier()
	 */
	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	/**
	 * Comma separated list of deploy folders, or artifact references where overlay web application
	 * resources are searched.
	 * 
	 * <p>
	 * The specified path names are relative to the {@link TLAppWar#getDeployDirectory()}.
	 * </p>
	 * 
	 * <p>
	 * If a value contains a ':' character, the value references a deploy artifact in the form of
	 * <code>groupId:artifactId:classifier</code>, where <code>:classifier</code> is optional. The
	 * corresponding artifact must be included as <b>optional</b> dependency in the project
	 * dependencies.
	 * </p>
	 * 
	 * <p>
	 * If a value is a plain directory name, the web application resources are taken from the
	 * {@link TLAppWar#getDeployWebapp()} sub directory.
	 * </p>
	 * 
	 * <p>
	 * Only one of the options, {@link #getArtifactId()}, {@link #getDeployAspects()}, or
	 * {@link #getPath()} must be given.
	 * </p>
	 * 
	 * @see #getPath()
	 */
	public String getDeployAspects() {
		return deployAspects;
	}

	/**
	 * @see #getDeployAspects()
	 */
	public void setDeployAspects(String value) {
		this.deployAspects = value;
	}

	/**
	 * A single path to the root directory containing web application resources to overlay on the
	 * generated application WAR.
	 * 
	 * <p>
	 * In contrast to {@link #getDeployAspects()}, no additional path resolution happens.
	 * </p>
	 * 
	 * <p>
	 * Only one of the options, {@link #getArtifactId()}, {@link #getDeployAspects()}, or
	 * {@link #getPath()} must be given.
	 * </p>
	 * 
	 * @see #getDeployAspects()
	 */
	public File getPath() {
		return path;
	}

	/**
	 * @see #getPath()
	 */
	public void setPath(File path) {
		this.path = path;
	}

	/**
	 * Whether to skip this overlay.
	 */
	public boolean isSkip() {
		return skip;
	}

	/**
	 * @see #isSkip()
	 */
	public void setSkip(boolean skip) {
		this.skip = skip;
	}

	boolean matches(Artifact a) {
		return Objects.equals(a.getGroupId(), getGroupId()) &&
			Objects.equals(a.getArtifactId(), getArtifactId()) &&
			Objects.equals(a.getClassifier(), getClassifier()) &&
			Objects.equals(a.getType(), getType());
	}

	String id() {
		return getGroupId() + ":" + getArtifactId() + ":" + getClassifier() + ":"
			+ getType();
	}

}

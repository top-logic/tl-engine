/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.building.ModelProcessor;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;

/**
 * Splices a parent POM above the top-most POM of a project's parent chain while the chain is
 * read by the model builder.
 *
 * <p>
 * The model builder reads a project's POMs from the project up to its top-most ancestor. Each POM
 * passes {@link #process(byte[])}: The first POM without a parent gets the {@link #getParent()
 * injected parent}, so that the injected parent and its own ancestors are read next and are
 * inherited with lower precedence than every POM of the project's own chain. The POMs of the
 * injected chain contribute no <code>dependencyManagement</code>: the project declares its
 * dependency versions itself (by importing the BOM).
 * </p>
 *
 * <p>
 * The splicer only acts while it is {@link #isActive() active}, i.e. while the parent chain is
 * read, not while imported BOMs are read.
 * </p>
 */
public class ParentSplicer {

	private final Parent _parent;

	private final ModelProcessor _modelProcessor;

	private boolean _active = true;

	private boolean _spliced;

	/**
	 * Creates a {@link ParentSplicer}.
	 *
	 * @param parent
	 *        The parent to inject above the top-most POM.
	 * @param modelProcessor
	 *        The reader for inspecting the POMs.
	 */
	public ParentSplicer(Parent parent, ModelProcessor modelProcessor) {
		_parent = parent;
		_modelProcessor = modelProcessor;
	}

	/**
	 * The parent injected above the top-most POM of the project's own parent chain.
	 */
	public Parent getParent() {
		return _parent;
	}

	/**
	 * Whether the parent chain is currently being read.
	 */
	public boolean isActive() {
		return _active;
	}

	/**
	 * Stops processing POMs, after the parent chain has been read.
	 */
	public void finish() {
		_active = false;
	}

	/**
	 * Whether the {@link #getParent() injected parent} has been spliced into the chain.
	 */
	public boolean isSpliced() {
		return _spliced;
	}

	/**
	 * Processes the contents of a POM read by the model builder.
	 *
	 * @param pom
	 *        The contents of the POM file.
	 * @return The contents to pass to the model builder.
	 */
	public byte[] process(byte[] pom) throws IOException {
		if (!_active) {
			return pom;
		}
		Map<String, Object> options = Collections.singletonMap(ModelProcessor.IS_STRICT, Boolean.FALSE);
		Model model = _modelProcessor.read(new ByteArrayInputStream(pom), options);
		if (_spliced) {
			// A POM of the injected chain.
			if (model.getDependencyManagement() == null) {
				return pom;
			}
			model.setDependencyManagement(null);
			return write(model);
		}
		if (model.getParent() != null) {
			return pom;
		}
		model.setParent(_parent.clone());
		_spliced = true;
		return write(model);
	}

	private static byte[] write(Model model) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		new MavenXpp3Writer().write(buffer, model);
		return buffer.toByteArray();
	}
}

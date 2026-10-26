/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.packaging;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.maven.model.building.FileModelSource;
import org.apache.maven.model.building.ModelSource2;

/**
 * {@link FileModelSource} passing its contents and the contents of its related POMs through a
 * {@link ParentSplicer}.
 */
public class SplicingModelSource extends FileModelSource {

	private final ParentSplicer _splicer;

	private byte[] _contents;

	/**
	 * Creates a {@link SplicingModelSource}.
	 *
	 * @param pomFile
	 *        The POM file to read.
	 * @param splicer
	 *        The splicer to pass the contents through.
	 */
	public SplicingModelSource(File pomFile, ParentSplicer splicer) {
		super(pomFile);
		_splicer = splicer;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		if (_contents == null) {
			_contents = _splicer.process(Files.readAllBytes(getFile().toPath()));
		}
		return new ByteArrayInputStream(_contents);
	}

	@Override
	public ModelSource2 getRelatedSource(String relPath) {
		ModelSource2 related = super.getRelatedSource(relPath);
		if (related instanceof FileModelSource) {
			return new SplicingModelSource(((FileModelSource) related).getFile(), _splicer);
		}
		return related;
	}
}

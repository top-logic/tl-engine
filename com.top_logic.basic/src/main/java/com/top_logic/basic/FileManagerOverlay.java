/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * {@link FileManager} that stacks one {@link FileManager} over another.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class FileManagerOverlay extends FileManagerDelegate {

	private final FileManager _overlay;

	/**
	 * Creates a {@link FileManagerOverlay}.
	 * 
	 * @param overlay
	 *        The {@link FileManager} to ask first.
	 * @param fallback
	 *        The {@link FileManager} to use, if the first does not provide a resource.
	 */
	public FileManagerOverlay(FileManager overlay, FileManager fallback) {
		super(fallback);
		_overlay = overlay;
	}
	
	@Override
	public boolean exists(String name) {
		return super.exists(name) || _overlay.exists(name);
	}

	@Override
	public void createData(String aName, BinaryContent content) throws IOException {
		// Do not create data in an overlay.
		super.createData(aName, content);
	}

	@Override
	public BinaryData getDataOrNull(String aName) {
		BinaryData overlayResult = _overlay.getDataOrNull(aName);
		if (overlayResult != null) {
			return overlayResult;
		}
		return super.getDataOrNull(aName);
	}

	@Override
	public File getIDEFile(String aName) {
		File overlayResult = _overlay.getIDEFile(aName);
		if (overlayResult.exists()) {
			return overlayResult;
		}
		File fallbackResult = super.getIDEFile(aName);
		if (fallbackResult.exists()) {
			return fallbackResult;
		}
		return overlayResult;
	}

	@Override
	public InputStream getStreamOrNull(String name) throws IOException {
		InputStream overlayResult = _overlay.getStreamOrNull(name);
		if (overlayResult != null) {
			return overlayResult;
		}
		return super.getStreamOrNull(name);
	}

	@Override
	public URL getResourceUrl(String name) throws MalformedURLException {
		URL overlayResult = _overlay.getResourceUrl(name);
		if (overlayResult != null) {
			return overlayResult;
		}
		return super.getResourceUrl(name);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		Set<String> overlayResult = _overlay.getResourcePaths(path);
		Set<String> fallbackResult = super.getResourcePaths(path);
		return CollectionUtil.union2(overlayResult, fallbackResult);
	}

	@Override
	public List<BinaryData> getDataOverlays(String name) throws IOException {
		List<BinaryData> overlayResult = _overlay.getDataOverlays(name);
		List<BinaryData> delegateResult = super.getDataOverlays(name);
		return CollectionUtil.concat(overlayResult, delegateResult);
	}

	@Deprecated
	@Override
	public List<File> getFiles(String name) throws IOException {
		List<File> overlayResult = _overlay.getFiles(name);
		List<File> delegateResult = super.getFiles(name);
		return CollectionUtil.concat(overlayResult, delegateResult);
	}

	@Override
	public void delete(String resourceName) throws IOException {
		_overlay.delete(resourceName);
		super.delete(resourceName);
	}

}

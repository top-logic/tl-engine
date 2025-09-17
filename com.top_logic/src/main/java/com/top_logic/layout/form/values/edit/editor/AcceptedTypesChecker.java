/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;

/**
 * Filename strategy to check, if a filename has the allowed type.
 * 
 * @author <a href="mailto:maziar.behdju@top-logic.com">Maziar Behdju</a>
 */
public class AcceptedTypesChecker implements FileNameStrategy {

	private Set<String> _acceptedMimeTypes = new HashSet<>();

	private List<String> _acceptedFilePattern = new ArrayList<>();

	private MimeTypes _mimeTypes = MimeTypes.getInstance();

	/**
	 * This constructor creates a new {@link AcceptedTypesChecker}.
	 * 
	 * @param acceptedTypes
	 *        Set of allowed MIME types and file endings.
	 */
	public AcceptedTypesChecker(Set<String> acceptedTypes) {
		if (!(acceptedTypes.contains("*.*") || acceptedTypes.contains("*/*"))) {
			for (String acceptedType : acceptedTypes) {
				if (acceptedType.contains("/")) {
					_acceptedMimeTypes.add(acceptedType);
				} else {
					_acceptedFilePattern.add(acceptedType);
				}
			}
		}
	}

	@Override
	public ResKey checkFileName(String fileName) {
		if (_acceptedMimeTypes.isEmpty() && _acceptedFilePattern.isEmpty()) {
			return null;
		}
		// Mime check
		String fileMimeType = _mimeTypes.getMimeType(fileName);
		String fileMainMimeType = fileMimeType.split("/")[0] + "/*";
		if (_acceptedMimeTypes.contains(fileMimeType) || _acceptedMimeTypes.contains(fileMainMimeType)) {
			return null;
		}
		// Type check
		for (String acceptedType : _acceptedFilePattern) {
			int wildcardIndex = acceptedType.lastIndexOf('*');
			if (wildcardIndex >= 0) {
				String pattern = acceptedType.substring(wildcardIndex + 1);
				if (fileName.length() >= pattern.length()
					&& fileName.substring(fileName.length() - pattern.length()).equalsIgnoreCase(pattern)) {
					return null;
				}
			} else {
				if (acceptedType.equalsIgnoreCase(fileName)) {
					return null;
				}
			}
		}

		return I18NConstants.ILLEGAL_FILE_TYPE__FILENAME__ALLOWED_TYPES.fill(fileName, getAcceptedTypesAsString(false));
	}

	/**
	 * Return a comma separated string of the accepted types.
	 * 
	 * @param resolveMimeTypes
	 *        true for getting MIME types for accepted file names
	 */
	public String getAcceptedTypesAsString(boolean resolveMimeTypes) {
		StringBuilder toReturn = new StringBuilder();
		for (Iterator<String> it = _acceptedMimeTypes.iterator(); it.hasNext();) {
			if (toReturn.length() > 0) {
				toReturn.append(",");
			}
			toReturn.append(it.next());
		}
		
		for (Iterator<String> it = _acceptedFilePattern.iterator(); it.hasNext();) {
			if (toReturn.length() > 0) {
				toReturn.append(",");
			}

			String filePattern = it.next();
			if (resolveMimeTypes) {
				toReturn.append(filePattern.replace("*", ""));
			} else {
				toReturn.append(filePattern);
			}
		}
		return toReturn.toString();
	}
}
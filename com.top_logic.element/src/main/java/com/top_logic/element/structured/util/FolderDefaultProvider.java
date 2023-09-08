/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.util;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;

/**
 * {@link DefaultProvider} creating a new empty {@link WebFolder} instance for singleton composition
 * references.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TargetType(value = TLTypeKind.COMPOSITION, name = WebFolder.WEB_FOLDER_TYPE)
public class FolderDefaultProvider implements DefaultProvider {

	/**
	 * Singleton {@link FolderDefaultProvider} instance.
	 */
	public static final FolderDefaultProvider INSTANCE = new FolderDefaultProvider();

	private FolderDefaultProvider() {
		// Singleton constructor.
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		if (createForUI) {
			// Only during commit.
			return null;
		}

		if (attribute.getModelKind() != ModelKind.REFERENCE) {
			// Only for references.
			return null;
		}

		TLReference reference = (TLReference) attribute;
		if (reference.isMultiple()) {
			// Only for singleton references.
			return null;
		}

		if (!reference.getEnd().isComposite()) {
			// Only for compositions, because only for those instances are automatically deleted
			// upon object deletion.
			return null;
		}

		String folderType = AttributeOperations.getFolderType(attribute);
		WebFolder webFolder = WebFolderFactory.getInstance().createNewWebFolder(attribute.getName(), folderType);
		return webFolder;
	}

}

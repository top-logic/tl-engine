/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.dialogs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.gui.layout.upload.FileNameStrategy;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.themeedit.browser.resource.ResourceType;

/**
 * Form definition for an icon upload.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface UploadForm extends ConfigurationItem {

	/**
	 * The icon to upload.
	 */
	@InstanceFormat
	@ItemDisplay(ItemDisplayType.VALUE)
	@AcceptedTypes(value = { "image/png", "image/gif", ".svg", "image/jpeg", ".ttf", ".eot", ".woff", ".woff2", ".otf",
		".afm", ".pfm", ".inf", ".pfb" }, checker = ResourcesOnly.class)
	BinaryData getData();

	/**
	 * {@link FileNameStrategy} for {@link UploadForm#getData()}
	 */
	class ResourcesOnly implements FileNameStrategy {

		@Override
		public ResKey checkFileName(String fileName) {
			int separator = fileName.lastIndexOf('.');
			if (separator < 0) {
				return I18NConstants.ERROR_ONLY_RESOURCE_TYPES__EXPECTED.fill(getExtensions());
			}

			String extension = fileName.substring(separator + 1).toLowerCase();
			if (!getExtensions().contains(extension)) {
				return I18NConstants.ERROR_ONLY_RESOURCE_TYPES__EXPECTED.fill(getExtensions());
			}
			return null;
		}

		private Set<String> getExtensions() {
			List<String> resources = new ArrayList<>();
			for (ResourceType type : ResourceType.values()) {
				resources.addAll(type.getExtensions());
			}

			return new HashSet<>(resources);
		}
	}
}

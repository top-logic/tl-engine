/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.XMLFragmentString;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.instance.importer.resolver.AbstractValueResolver;
import com.top_logic.model.instance.importer.resolver.ValueResolver;
import com.top_logic.model.instance.importer.schema.CustomValueConf;
import com.top_logic.model.wysiwyg.storage.StructuredTextValueResolver.HtmlConf;
import com.top_logic.model.wysiwyg.storage.StructuredTextValueResolver.HtmlConf.ImageConf;

/**
 * {@link ValueResolver} for storing and reading {@link StructuredText} from/to XML configuration.
 */
public class StructuredTextValueResolver extends AbstractValueResolver<HtmlConf, StructuredText> {

	/**
	 * Configuration representation of a {@link StructuredText} value.
	 */
	@TagName("html")
	public static interface HtmlConf extends CustomValueConf {
		/**
		 * The HTML source code.
		 */
		@Binding(value = XMLFragmentString.class)
		String getSourceCode();

		/**
		 * @see #getSourceCode()
		 */
		void setSourceCode(String sourceCode);

		/**
		 * Images used in {@link #getSourceCode()}
		 */
		@Key(ImageConf.ID)
		List<ImageConf> getImages();

		/**
		 * Configuration for a HTML embedded image.
		 */
		interface ImageConf extends ConfigurationItem {
			/**
			 * @see #getId()
			 */
			String ID = "id";

			/**
			 * The ID of the image used to reference it from the source code.
			 */
			@Name(ID)
			String getId();

			/**
			 * @see #getId()
			 */
			void setId(String key);

			/**
			 * The data of the image.
			 */
			BinaryData getData();

			/**
			 * @see #getData()
			 */
			void setData(BinaryData value);
		}
	}

	@Override
	protected StructuredText internalResolve(TLStructuredTypePart attribute, HtmlConf config) {
		Map<String, BinaryData> images = new HashMap<>();
		for (ImageConf image : config.getImages()) {
			images.put(image.getId(), image.getData());
		}
		return new StructuredText(config.getSourceCode(), images);
	}

	@Override
	protected HtmlConf internalCreateValueConf(TLStructuredTypePart attribute, StructuredText value) {
		HtmlConf html = TypedConfiguration.newConfigItem(HtmlConf.class);
		html.setSourceCode(value.getSourceCode());
		for (Entry<String, BinaryData> image : value.getImages().entrySet()) {
			ImageConf imageConf = TypedConfiguration.newConfigItem(ImageConf.class);
			imageConf.setId(image.getKey());
			imageConf.setData(image.getValue());
			html.getImages().add(imageConf);
		}
		return html;
	}

}

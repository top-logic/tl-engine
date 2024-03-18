/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.FormatFactory;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.util.Resources;
import com.top_logic.util.TLCollator;

/**
 * Definition of display formats.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public interface FormatBase extends ConfigurationItem {

	/**
	 * @see #getDefinition()
	 */
	String DEFINITION = "definition";

	/**
	 * Definition of the format to use.
	 */
	@Name(DEFINITION)
	@DefaultContainer
	PolymorphicConfiguration<? extends FormatFactory> getDefinition();

	/**
	 * Option provider that allows to select from all globally defined {@link FormDefinition}s.
	 */
	class FormatOptions extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			FormatterService service = FormatterService.getInstance();
			List<String> formats = new ArrayList<>(service.getFormats());
			formats.sort(new Comparator<String>() {
				Resources _resources = Resources.getInstance();

				TLCollator _collator = new TLCollator();

				@Override
				public int compare(String f1, String f2) {
					return _collator.compare(label(f1), label(f2));
				}

				private String label(String id) {
					return _resources.getString(service.getLabel(id));
				}
			});
			return formats;
		}
	}

	/**
	 * {@link LabelProvider} for {@link FormDefinition} options.
	 */
	class FormatLabel implements LabelProvider {
		@Override
		public String getLabel(Object object) {
			if (object == null) {
				return null;
			}
			String id = (String) object;
			FormatterService service = FormatterService.getInstance();
			Resources resources = Resources.getInstance();
			return resources.getString(service.getLabel(id));
		}
	}
}
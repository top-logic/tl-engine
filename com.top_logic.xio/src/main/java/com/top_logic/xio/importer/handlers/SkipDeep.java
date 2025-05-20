/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} that skips all XML content up to a matching end tag.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SkipDeep<C extends SkipDeep.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link SkipDeep}.
	 */
	@TagName("skip-deep")
	public interface Config<I extends SkipDeep<?>> extends ConfiguredImportHandler.Config<I> {
		/**
		 * Report warnings about ignored tags.
		 */
		boolean getWarn();

		/**
		 * @see #getWarn()
		 */
		void setWarn(boolean value);
	}

	private final boolean _warn;

	/**
	 * Creates a {@link SkipDeep} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SkipDeep(InstantiationContext context, C config) {
		super(context, config);
		_warn = config.getWarn();
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		if (_warn) {
			context.info(in.getLocation(), I18NConstants.WARN_IGNORING_UNKNOWN__TAG.fill(in.getLocalName()));
		}
		XMLStreamUtil.skipUpToMatchingEndTag(in);
		return null;
	}

}

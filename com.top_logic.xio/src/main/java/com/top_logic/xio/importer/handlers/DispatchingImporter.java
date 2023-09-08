/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.handlers.DispatchingImporter.Config.TagHandler;
import com.top_logic.xio.importer.handlers.Handler.Config.HandlerRef;

/**
 * {@link Handler} that dispatches to another {@link Handler} based on the current tag-name.
 * 
 * @see Config#getTags()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DispatchingImporter<C extends DispatchingImporter.Config<?>> extends ConfiguredImportHandler<C> {

	private static SkipDeep.Config<?> SKIP_DEEP = TypedConfiguration.newConfigItem(SkipDeep.Config.class);

	/**
	 * Configuration options for {@link DispatchingImporter}.
	 */
	@TagName("dispatch")
	public interface Config<I extends DispatchingImporter<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * The {@link Handler} to choose, if none of the {@link #getTags()} match the current
		 * element.
		 */
		HandlerRef getDefault();

		/**
		 * The tag name to {@link Handler} mapping.
		 */
		@Key(TagHandler.NAME)
		@DefaultContainer
		Map<String, TagHandler> getTags();

		/**
		 * Combination of tag name and {@link Handler} reference.
		 */
		interface TagHandler extends ConfigurationItem {
			public static final String NAME = "name";

			/**
			 * The tag name that triggers the {@link #getImpl() handler}.
			 */
			@Name(NAME)
			String getName();

			/**
			 * The {@link Handler} to invoke, if the current element has {@link #getName() the given
			 * tag name}.
			 */
			@DefaultContainer
			List<PolymorphicConfiguration<? extends Handler>> getImpl();
		}
	}

	private Map<String, Handler> _handlers = new HashMap<>();

	private Handler _defaultHandler;

	/**
	 * Creates a {@link DispatchingImporter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DispatchingImporter(InstantiationContext context, C config) {
		super(context, config);

		HandlerRef defaultRef = config.getDefault();
		if (defaultRef != null) {
			_defaultHandler = context.getInstance(defaultRef.getHandler());
		} else {
			_defaultHandler = context.getInstance(SKIP_DEEP);
		}
		_handlers = new HashMap<>();
		for (TagHandler tag : config.getTags().values()) {
			_handlers.put(tag.getName(), NestedImportHandler.buildHandlers(context, tag.getImpl()));
		}
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		Object result = null;
		while (true) {
			switch (in.next()) {
				case XMLStreamConstants.END_ELEMENT:
				case XMLStreamConstants.END_DOCUMENT: {
					return result;
				}

				case XMLStreamConstants.START_ELEMENT: {
					String localName = in.getLocalName();
					Handler importer = importer(localName);
					Object imported = importer.importXml(context, in);
					if (imported != null) {
						result = imported;
					}
					if (in.getEventType() != XMLStreamConstants.END_ELEMENT) {
						context.error(in.getLocation(), I18NConstants.ERROR_END_ELEMENT_EXPECTED__TAG.fill(localName));
					} else if (!localName.equals(in.getLocalName())) {
						context.error(in.getLocation(),
							I18NConstants.ERROR_UNEXPECTED_END_TAG__EXPECTED_FOUND.fill(localName, in.getLocalName()));
					}
					break;
				}

				default: {
					// Ignore.
				}
			}
		}
	}

	private Handler importer(String localName) {
		Handler result = _handlers.get(localName);
		if (result == null) {
			return _defaultHandler;
		}
		return result;
	}

}

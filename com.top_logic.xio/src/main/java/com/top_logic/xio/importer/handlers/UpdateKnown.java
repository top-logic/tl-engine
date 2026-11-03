/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
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
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.xio.importer.binding.ImportContext;

/**
 * {@link Handler} that updates the known objects of the descendant elements of the current
 * element.
 *
 * <p>
 * The whole subtree of the current element is walked. For each descendant element at any depth
 * whose tag name has a {@link Config#getTags() tag entry}, the ID is read from the
 * {@link TagConfig#getIdAttribute() ID attribute} of this entry. If this ID is known, i.e. it
 * resolves to an object that was imported before or that is found by an enclosing
 * {@link ResolveExisting}, the {@link TagConfig#getContent() handlers} of the entry are applied to
 * the element with the known object bound to {@link ImportContext#THIS_VAR} and to the
 * {@link TagConfig#getVar() variable}.
 * </p>
 *
 * <p>
 * If the handlers do not consume the element, its children are walked as well. An element whose
 * tag name has no entry, that has no ID, or whose ID is not known, is not updated, but its
 * children are walked. Nothing is created and nothing is linked, unless the handlers do so
 * explicitly.
 * </p>
 *
 * <p>
 * The current element itself is not updated, but only its descendants: The current element is
 * handled by the handlers next to this one.
 * </p>
 *
 * @see UnlessKnown.Config#getUpdate()
 */
@Label("Update of known descendants")
public class UpdateKnown<C extends UpdateKnown.Config<?>> extends ConfiguredImportHandler<C> {

	/**
	 * Configuration options for {@link UpdateKnown}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends UpdateKnown<?>> extends ConfiguredImportHandler.Config<I> {

		/**
		 * Tag name of {@link UpdateKnown}.
		 */
		String TAG_NAME = "update-known";

		/**
		 * @see #getTags()
		 */
		String TAGS = "tags";

		/**
		 * The descendant elements to update, by their tag name.
		 *
		 * <p>
		 * Descendant elements with a tag name not listed here are not updated, but their children
		 * are walked.
		 * </p>
		 */
		@Name(TAGS)
		@Key(TagConfig.NAME)
		@DefaultContainer
		Map<String, TagConfig> getTags();

	}

	/**
	 * Update of the known objects of descendant elements with a certain tag name, see
	 * {@link Config#getTags()}.
	 */
	@Label("Known descendant element")
	public interface TagConfig extends ConfigurationItem {

		/**
		 * @see #getName()
		 */
		String NAME = "name";

		/**
		 * @see #getIdAttribute()
		 */
		String ID_ATTRIBUTE = "id-attribute";

		/**
		 * @see #getVar()
		 */
		String VAR = "var";

		/**
		 * @see #getContent()
		 */
		String CONTENT = "content";

		/**
		 * The tag name of the descendant elements to update.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * The XML attribute of an element with the given {@link #getName() tag name} containing
		 * the ID of its known object.
		 *
		 * <p>
		 * An element without this attribute, or with an empty value, is not updated.
		 * </p>
		 */
		@Name(ID_ATTRIBUTE)
		@Mandatory
		String getIdAttribute();

		/**
		 * Explicit variable name in the context for the known object.
		 *
		 * <p>
		 * Inner handlers may reference the known object with this name.
		 * </p>
		 */
		@Name(VAR)
		@Label("Variable")
		@Nullable
		String getVar();

		/**
		 * The handlers to apply to an element with a known ID in the context of its known object.
		 *
		 * <p>
		 * If the handlers consume the element, its children are not walked.
		 * </p>
		 */
		@Name(CONTENT)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends Handler>> getContent();

	}

	private final Map<String, TagUpdate> _updates = new HashMap<>();

	/**
	 * Creates a {@link UpdateKnown} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UpdateKnown(InstantiationContext context, C config) {
		super(context, config);

		for (TagConfig tag : config.getTags().values()) {
			List<PolymorphicConfiguration<? extends Handler>> content = tag.getContent();
			Handler handler = content.isEmpty() ? null : NestedImportHandler.buildHandlers(context, content);
			_updates.put(tag.getName(), new TagUpdate(tag, handler));
		}
	}

	@Override
	public Object importXml(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		// Number of open descendant elements whose children are being walked.
		int depth = 0;
		while (true) {
			switch (in.next()) {
				case XMLStreamConstants.END_ELEMENT: {
					if (depth == 0) {
						return null;
					}
					depth--;
					break;
				}

				case XMLStreamConstants.END_DOCUMENT: {
					return null;
				}

				case XMLStreamConstants.START_ELEMENT: {
					if (!update(context, in)) {
						depth++;
					}
					break;
				}

				default: {
					// Ignore.
				}
			}
		}
	}

	/**
	 * Updates the known object of the current descendant element.
	 *
	 * @return Whether the element was consumed, i.e. the input is positioned at its end element.
	 */
	private boolean update(ImportContext context, XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		TagUpdate update = _updates.get(localName);
		if (update == null) {
			return false;
		}

		String id = ObjectImport.readIdAttribute(in, update.getConfig().getIdAttribute());
		if (id == null) {
			return false;
		}

		Object known = context.resolveObjectBackwards(this, in.getLocation(), id);
		if (known == null) {
			return false;
		}

		Handler handler = update.getHandler();
		if (handler == null) {
			return false;
		}
		String var = update.getConfig().getVar();
		context.withVar(ImportContext.THIS_VAR, known, in, (context1, in1) -> {
			return context1.withVar(var, known, in1, handler);
		});

		switch (in.getEventType()) {
			case XMLStreamConstants.START_ELEMENT: {
				// The handlers did not consume the element, its children are walked.
				return false;
			}
			case XMLStreamConstants.END_ELEMENT: {
				if (!localName.equals(in.getLocalName())) {
					context.error(in.getLocation(),
						I18NConstants.ERROR_UNEXPECTED_END_TAG__EXPECTED_FOUND.fill(localName, in.getLocalName()));
				}
				return true;
			}
			default: {
				context.error(in.getLocation(), I18NConstants.ERROR_END_ELEMENT_EXPECTED__TAG.fill(localName));
				return true;
			}
		}
	}

	/**
	 * Instantiated {@link TagConfig}.
	 */
	private static final class TagUpdate {

		private final TagConfig _config;

		private final Handler _handler;

		/**
		 * Creates a {@link TagUpdate}.
		 */
		TagUpdate(TagConfig config, Handler handler) {
			_config = config;
			_handler = handler;
		}

		/**
		 * The configuration of this update.
		 */
		TagConfig getConfig() {
			return _config;
		}

		/**
		 * The handlers to apply to an element with a known ID, <code>null</code> if there are none.
		 */
		Handler getHandler() {
			return _handler;
		}

	}

}

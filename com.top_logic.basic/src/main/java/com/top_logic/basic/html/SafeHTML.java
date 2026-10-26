/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.exception.I18NException;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.UICategory;

/**
 * Utility to check HTML input to only contain safe contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("HTML safety check")
@ServiceCategory.Of(UICategory.class)
public class SafeHTML extends ManagedClass {

	/** Name of a script tag */
	public static final String SCRIPT_TAG = "script";

	/** Attribute containing the type of a {@link #SCRIPT_TAG} */
	public static final String TYPE_ATTR = "type";

	/**
	 * Type of a {@link #SCRIPT_TAG} that marks it to contain TL-Script (those tags are rendered to
	 * the UI only for editing them in the HTML editor, not for executing them).
	 */
	public static final String TEXT_TLSSCRIPT_TYPE = "text/tlscript";

	/**
	 * Configuration for {@link SafeHTML} specifying which attribute names, tag names and attribute
	 * values are allowed.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<SafeHTML> {
		/**
		 * Attribute name for tag and attribute tags.
		 */
		public static final String NAME = "name";

		/**
		 * Configuration tag name for each tag.
		 */
		public static final String TAG = "tag";

		/**
		 * Configuration tag name for each attribute.
		 */
		public static final String ATTRIBUTE = "attribute";

		/**
		 * Whether checking is disabled.
		 */
		boolean isDisabled();

		/**
		 * Configuration tag name for each attribute name prefix.
		 */
		public static final String PREFIX = "prefix";

		/** Property name of {@link #getAllowedAttributePrefixes()}. */
		public static final String ALLOWED_ATTRIBUTE_PREFIXES = "allowed-attribute-prefixes";

		/**
		 * {@link List} of allowed attributes.
		 *
		 * @see #getAllowedAttributePrefixes()
		 */
		@ListBinding(tag = ATTRIBUTE, attribute = NAME)
		List<String> getAllowedAttributes();

		/**
		 * Prefixes of allowed attribute names.
		 *
		 * <p>
		 * An attribute whose name starts with one of these prefixes is allowed in addition to the
		 * {@link #getAllowedAttributes() allowed attributes}, e.g. every custom data attribute with
		 * the prefix <code>data-</code>.
		 * </p>
		 */
		@Name(ALLOWED_ATTRIBUTE_PREFIXES)
		@ListBinding(tag = PREFIX, attribute = NAME)
		List<String> getAllowedAttributePrefixes();

		/**
		 * {@link List} of allowed tags.
		 */
		@ListBinding(tag = TAG, attribute = NAME)
		List<String> getAllowedTags();

		/**
		 * {@link Map} of {@link AttributeChecker}'s.
		 */
		@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
		Map<String, NamedPolymorphicConfiguration<AttributeChecker>> getAttributeCheckers();
	}

	static final HTMLChecker NO_CHECK = new HTMLChecker() {
		@Override
		public boolean isAllowedTagName(String tag) {
			return true;
		}

		@Override
		public boolean isAllowedAttributeName(String attribute) {
			return true;
		}

		@Override
		public AttributeChecker getAttributeChecker(String attribute) {
			return null;
		}

		@Override
		public void checkTag(String tag) throws I18NException {
			// Ignore.
		}

		@Override
		public void checkAttributeValue(String attribute, String value) throws I18NException {
			// Ignore.
		}

		@Override
		public void checkAttributeName(String tag, String attribute) throws I18NException {
			// Ignore.
		}

		@Override
		public void checkAttribute(String tag, String attribute, String value) throws I18NException {
			// Ignore.
		}

		@Override
		public void check(String html) throws I18NException {
			// Ignore.
		}
	};

	private final HTMLChecker _checker;

	/**
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        {@link Config} for {@link SafeHTML}.
	 */
	public SafeHTML(InstantiationContext context, Config config) {
		super(context, config);

		_checker = config.isDisabled() ? NO_CHECK : new WhiteListHTMLChecker(context, config);
	}

	/**
	 * Return the only instance of this class.
	 * 
	 * @return The requested {@link SafeHTML} instance.
	 */
	public static HTMLChecker getInstance() {
		if (Module.INSTANCE.isActive()) {
			return Module.INSTANCE.getImplementationInstance()._checker;
		} else {
			return SafeHTML.NO_CHECK;
		}
	}

	/**
	 * Singleton holder for the {@link SafeHTML}.
	 */
	public static final class Module extends TypedRuntimeModule<SafeHTML> {

		/**
		 * Singleton {@link SafeHTML.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<SafeHTML> getImplementation() {
			return SafeHTML.class;
		}
	}
}

/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.i18n;

import java.util.Objects;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.AbstractConfigurationValueBinding;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueBinding;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.util.Resources;

/**
 * {@link HtmlResKey} implementation which takes the HTML from a given {@link ResKey}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultHtmlResKey
		implements HtmlResKey, HtmlResKey1, HtmlResKey2, HtmlResKey3, HtmlResKey4, HtmlResKey5, HtmlResKeyN {

	private final ResKey _content;

	/**
	 * Creates a {@link DefaultHtmlResKey}.
	 */
	public DefaultHtmlResKey(ResKey content) {
		_content = Objects.requireNonNull(content);
	}

	/**
	 * The {@link ResKey} which delivers the actual HTML source.
	 */
	public ResKey content() {
		return _content;
	}

	@Override
	public HTMLFragment getHtml(Resources resources) {
		String content = resources.getString(content());
		return Fragments.htmlSource(content);
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object... args) {
		return new DefaultHtmlResKey(content().asResKeyN().fill(args));
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
		return new DefaultHtmlResKey(content().asResKey5().fill(arg1, arg2, arg3, arg4, arg5));
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object arg1, Object arg2, Object arg3, Object arg4) {
		return new DefaultHtmlResKey(content().asResKey4().fill(arg1, arg2, arg3, arg4));
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object arg1, Object arg2, Object arg3) {
		return new DefaultHtmlResKey(content().asResKey3().fill(arg1, arg2, arg3));
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object arg1, Object arg2) {
		return new DefaultHtmlResKey(content().asResKey2().fill(arg1, arg2));
	}

	@SuppressWarnings("deprecation")
	@Override
	public HtmlResKey fill(Object arg) {
		return new DefaultHtmlResKey(content().asResKey1().fill(arg));
	}

	@Override
	public int hashCode() {
		return _content.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DefaultHtmlResKey other = (DefaultHtmlResKey) obj;
		return _content.equals(other._content);
	}

	/**
	 * {@link ValueBinding} for {@link HtmlResKey}.
	 * 
	 * @implNote The binding expects only {@link DefaultHtmlResKey} implementations.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ValueBinding extends AbstractConfigurationValueBinding<HtmlResKey> {

		private final ConfigurationValueBinding<ResKey> _delegate = new ResKey.ValueBinding(true);

		/** Singleton {@link ValueBinding} instance. */
		public static final ValueBinding INSTANCE = new ValueBinding();

		/**
		 * Creates a new {@link ValueBinding}.
		 */
		protected ValueBinding() {
			// singleton instance
		}

		@Override
		public HtmlResKey loadConfigItem(XMLStreamReader in, HtmlResKey baseValue)
				throws XMLStreamException, ConfigurationException {
			ResKey loadedKey = _delegate.loadConfigItem(in, content(baseValue));
			if (loadedKey == null) {
				return null;
			}
			return new DefaultHtmlResKey(loadedKey);
		}

		@Override
		public void saveConfigItem(XMLStreamWriter out, HtmlResKey item) throws XMLStreamException {
			_delegate.saveConfigItem(out, content(item));
		}

		private static ResKey content(HtmlResKey htmlResKey) {
			if (htmlResKey == null) {
				return null;
			}
			return ((DefaultHtmlResKey) htmlResKey).content();
		}

	}


}

/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.control;

import static com.top_logic.basic.shared.collection.iterator.IteratorUtilShared.*;
import static java.util.Collections.*;
import static java.util.Objects.*;

import java.io.IOException;
import java.util.Locale;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.search.SearchResult;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.doc.component.WithDocumentationLanguage;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.AbstractFormFieldControl;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.TLContext;

/**
 * A {@link Control} that displays an i18n {@link CompositeField} by displaying just one of the
 * languages.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class TranslatedI18nFieldControl<C extends AbstractFormFieldControl> extends AbstractFormMemberControl {

	/** The {@link ControlProvider} for the {@link TranslatedI18nFieldControl}. */
	public static abstract class Provider extends AbstractConfiguredInstance<Provider.Config>
			implements ControlProvider {

		/**
		 * Typed configuration interface definition for {@link TranslatedI18nFieldControl.Provider}.
		 * 
		 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
		 */
		public interface Config
				extends PolymorphicConfiguration<TranslatedI18nFieldControl.Provider>, WithDocumentationLanguage {

			// Sum interface

		}

		private LayoutComponent _contextComponent;

		/**
		 * Create a {@link TranslatedI18nStringFieldControl.Provider}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the new object in
		 * @param config
		 *        the configuration object to be used for instantiation
		 */
		public Provider(InstantiationContext context, Config config) {
			super(context, config);
			ModelSpec language = config.getLanguage();
			if (language != null) {
				context.resolveReference(InstantiationContext.OUTER, LayoutComponent.class,
					component -> _contextComponent = component);
			}
		}

		@Override
		public Control createControl(Object model, String style) {
			CompositeField field = (CompositeField) model;
			ComponentChannel languageChannel;
			if (_contextComponent != null) {
				Protocol log = new LogProtocol(TranslatedI18nFieldControl.class);
				languageChannel = ChannelLinking.resolveChannel(log, _contextComponent, getConfig().getLanguage());
				log.checkErrors();
			} else {
				languageChannel = null;
			}
			return createControl(field, languageChannel, style);
		}

		/**
		 * Creates the actual result control.
		 * 
		 * @param i18nField
		 *        The {@link CompositeField} holding as children the actual field to display
		 * @param language
		 *        The {@link ComponentChannel} of type {@link Locale} which holds the language to
		 *        determine the field to display.
		 * @param style
		 *        Style of the result control (See
		 *        {@link ControlProvider#createControl(Object, String)})
		 */
		protected abstract Control createControl(CompositeField i18nField, ComponentChannel language, String style);

	}

	private static final String CSS_CLASS = "tlTranslatedI18nField";

	private C _innerControl;

	private final String _style;

	private final ComponentChannel _language;

	private final ChannelListener _listener = this::onLanguageChange;

	/**
	 * Creates a {@link TranslatedI18nFieldControl}.
	 * 
	 * @param i18nField
	 *        Is not allowed to be null.
	 * @param language
	 *        Channel holding the {@link Locale} of the field to display.
	 * @param style
	 *        See the second parameter of {@link ControlProvider#createControl(Object, String)}.
	 */
	public TranslatedI18nFieldControl(CompositeField i18nField, ComponentChannel language, String style) {
		super(i18nField, emptyMap());
		_language = language;
		_style = style;
		_innerControl = createInnerControl();
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(DIV);
		writeControlAttributes(context, out);
		out.endBeginTag();
		_innerControl.write(context, out);
		out.endTag(DIV);
	}

	@Override
	protected void writeControlClassesContent(Appendable out) throws IOException {
		super.writeControlClassesContent(out);
		HTMLUtil.appendCSSClass(out, getCssClass());
	}

	/** The CSS class specific to this type of {@link Control}. */
	protected String getCssClass() {
		return CSS_CLASS;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		if (_language != null) {
			_language.addListener(_listener);
		}
	}

	@Override
	protected void internalDetach() {
		if (_language != null) {
			_language.removeListener(_listener);
		}
		super.internalDetach();
	}

	/**
	 * {@link ChannelListener} reacting on changes in {@link #_language}.
	 * 
	 * @param sender
	 *        The sending channel, i.e {@link #_language}.
	 * @param oldValue
	 *        Old value of the channel.
	 * @param newValue
	 *        New value of the channel.
	 */
	private void onLanguageChange(ComponentChannel sender, Object oldValue, Object newValue) {
		_innerControl = createInnerControl((Locale) newValue);
		requestRepaint();
	}

	private C createInnerControl() {
		return createInnerControl(language());
	}

	private C createInnerControl(Locale language) {
		FormField innerField = getInnerField(language);
		C innerControl = createInnerControl(innerField, _style);
		return requireNonNull(innerControl, "The inner control must not be null.");
	}

	private Locale language() {
		if (_language != null) {
			return (Locale) _language.get();
		}
		return TLContext.getLocale();
	}

	private FormField getInnerField(Locale language) {
		SearchResult<FormField> searchResult = new SearchResult<>();
		for (FormField partField : toList(getFieldModel().getFields())) {
			Locale fieldLanguage = partField.get(I18NField.LANGUAGE);
			searchResult.addCandidate(partField);
			if (isSameLanguage(language, fieldLanguage)) {
				searchResult.add(partField);
			}
		}
		return searchResult
			.getSingleResult("Failed to find the inner field for the language '" + language.getLanguage() + "'.");
	}

	private boolean isSameLanguage(Locale locale, Locale fieldLanguage) {
		/* Don't compare "languageCode" directly with "locale.getLanguage()", as the documentation
		 * of "Locale.getLanguage()" explicitly warns not to do that. */
		return fieldLanguage.getLanguage().equals(locale.getLanguage());
	}

	/** Create the {@link Control} that displays the inner {@link FormField}. */
	protected abstract C createInnerControl(FormField innerField, String style);

	/**
	 * Getter for the inner {@link Control}.
	 * 
	 * @return Never null.
	 */
	protected C getInnerControl() {
		return _innerControl;
	}

	/** Redirect to {@link #getModel()} that declares the correct type. */
	protected CompositeField getFieldModel() {
		return (CompositeField) getModel();
	}

}

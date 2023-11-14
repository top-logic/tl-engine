/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import static com.top_logic.layout.form.model.FormFactory.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.doc.model.Page;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.check.CheckScope;
import com.top_logic.layout.basic.check.MasterSlaveCheckProvider;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.TypedChannelSPI;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.CheckChangesValueVetoListener;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.toolbar.ToolBar;
import com.top_logic.layout.tree.component.WithSelectionPath;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * Displays the tree of documentation {@link Page}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DocumentationTreeComponent extends DocumentationViewerTree implements FormHandler {

	/**
	 * Name of the {@link #getChannel(String) channel} delivering
	 * {@link #getDocumentationLanguage()}.
	 */
	public static final String LANGUAGE_CHANNEL_NAME = "language";

	private static final Map<String, ChannelSPI> CHANNELS =
		channels(MODEL_AND_SELECTION_CHANNEL, WithSelectionPath.SELECTION_PATH_SPI,
			new TypedChannelSPI<>(LANGUAGE_CHANNEL_NAME, Locale.class, null));

	private static final String LANGUAGE_FIELD_CSS_CLASS = "languageSelect";

	/** {@link ConfigurationItem} for the {@link DocumentationTreeComponent}. */
	public interface Config extends DocumentationViewerTree.Config {
		// nothing needed, yet.
	}

	private static final String LANGUAGE_FIELD_NAME = "language";

	private FormContext _formContext;

	private HTMLFragment _languageControl;

	/** {@link TypedConfiguration} constructor for {@link DocumentationTreeComponent}. */
	public DocumentationTreeComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		languageChannel().set(TLContext.getLocale());
		languageChannel().addListener(this::handleLanguageChannelChanged);
	}

	@Override
	protected void componentsResolved(InstantiationContext context) {
		super.componentsResolved(context);
		initializeLanguageField();
	}

	private void initializeLanguageField() {
		/* The FormContext itself is not written to the GUI. It would be possible to use a FormField
		 * without even creating a FormContext. But that would lead to follow up problems, like
		 * scripting not being possible. Therefore, the FormContext exists, even though it is not
		 * written to the GUI. */
		FormContext formContext = FormComponent.createFormContext(this);
		FormField languageField = createLanguageField();
		formContext.addMember(languageField);
		/* Do not store unfinished FormContext. */
		_formContext = formContext;
		_languageControl = createLanguageControl(languageField);
	}

	private FormField createLanguageField() {
		Collection<Locale> options = Resources.getInstance().getSupportedLocalesInDisplayOrder();
		SelectField languageField = newSelectField(LANGUAGE_FIELD_NAME, options);
		languageField.setCssClasses(LANGUAGE_FIELD_CSS_CLASS);
		languageField.setMandatory(MANDATORY);
		languageField.setOptionComparator(LabelComparator.newCachingInstance());
		Locale language = findCurrentLocale(options);
		languageField.initSingleSelection(language);
		storeDocumentationLanguage(language);
		languageField.addValueListener(this::onLanguageChange);
		languageField.addValueVetoListener(new CheckChangesValueVetoListener(getCheckScope()));
		return languageField;
	}

	private Locale findCurrentLocale(Collection<Locale> options) {
		Locale currentLocale = TLContext.getLocale();
		for (Locale option : options) {
			if (Objects.equals(option.getLanguage(), currentLocale.getLanguage())) {
				return option;
			}
		}
		return Resources.getDefaultLocale();
	}

	/**
	 * {@link ValueListener} implementation when language was changed on client.
	 * 
	 * @param field
	 *        The changed field.
	 * @param oldValue
	 *        Former value of the field.
	 * @param newValue
	 *        New value of the field.
	 * 
	 * @see ValueListener#valueChanged(FormField, Object, Object)
	 */
	private void onLanguageChange(FormField field, Object oldValue, Object newValue) {
		Locale newLanguage = (Locale) CollectionUtil.getSingleValueFromCollection((Collection<?>) newValue);
		storeDocumentationLanguage(newLanguage);
		invalidate();
	}

	private void storeDocumentationLanguage(Locale newLanguage) {
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		subSession.set(StructuredTextConfigService.LOCALE, newLanguage);
		languageChannel().set(newLanguage);
	}

	/**
	 * The language in which the documentation should be displayed.
	 * <p>
	 * This is not the same as the language in which the application is displayed: When the
	 * documentation is edited, all its translations can be edited without switching the whole
	 * application to another language.
	 * </p>
	 * 
	 * @implNote This value is the value of the {@link ChannelSPI} with name
	 *           {@link #LANGUAGE_CHANNEL_NAME}.
	 */
	public Locale getDocumentationLanguage() {
		/* The field displaying the language does not listen to this property, for simplicity.
		 * Changes from outside would therefore cause no update to the field. */
		return (Locale) languageChannel().get();
	}

	private ComponentChannel languageChannel() {
		return getChannel(LANGUAGE_CHANNEL_NAME);
	}

	private CheckScope getCheckScope() {
		return MasterSlaveCheckProvider.INSTANCE.getCheckScope(this);
	}

	private HTMLFragment createLanguageControl(FormField languageField) {
		return getControlProvider(languageField).createFragment(languageField);
	}

	private HTMLFragmentProvider getControlProvider(FormField languageField) {
		HTMLFragmentProvider controlProvider = languageField.getControlProvider();
		if (controlProvider != null) {
			return controlProvider;
		}
		return DefaultFormFieldControlProvider.INSTANCE;
	}

	@Override
	protected void onSetToolBar(ToolBar oldToolBar, ToolBar newToolBar) {
		super.onSetToolBar(oldToolBar, newToolBar);
		if (!hasFormContext()) {
			/* The FormContext has not been built, yet. */
			return;
		}
		if (newToolBar != null) {
			setTitle(newToolBar);
		}
	}

	private void setTitle(ToolBar newToolBar) {
		HTMLFragment titleControl = createToolBarTitleControl(newToolBar);
		newToolBar.setTitle(titleControl);
	}

	private HTMLFragment createToolBarTitleControl(ToolBar toolBar) {
		if (toolBar.getTitle() == null) {
			return getLanguageControl();
		}
		BlockControl combinedControl = new BlockControl();
		combinedControl.addChild(toolBar.getTitle());
		combinedControl.addChild(getLanguageControl());
		return combinedControl;
	}

	private HTMLFragment getLanguageControl() {
		return requireNonNull(_languageControl);
	}

	@Override
	public FormContext getFormContext() {
		return requireNonNull(_formContext);
	}

	@Override
	public boolean hasFormContext() {
		return _formContext != null;
	}

	@Override
	public Command getApplyClosure() {
		return null;
	}

	@Override
	public Command getDiscardClosure() {
		return null;
	}

	@Override
	protected Map<String, ChannelSPI> channels() {
		return CHANNELS;
	}

	/**
	 * Handles the change of the {@link #LANGUAGE_CHANNEL_NAME} channel.
	 * 
	 * @param sender
	 *        The language channel.
	 * @param oldValue
	 *        Previous value of the channel.
	 * @param newValue
	 *        New value of the channel.
	 */
	private void handleLanguageChannelChanged(ComponentChannel sender, Object oldValue, Object newValue) {
		if (!hasFormContext()) {
			return;
		}

		FormField languageField = languageField(getFormContext());
		languageField.setValue(Collections.singletonList(newValue));
	}

	private FormField languageField(FormContext formContext) {
		return formContext.getField(LANGUAGE_FIELD_NAME);
	}
}

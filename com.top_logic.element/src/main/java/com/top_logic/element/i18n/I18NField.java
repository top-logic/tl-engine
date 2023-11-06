/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.LabelChangedListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.util.Resources;

/**
 * A {@link CompositeField} that displays a {@link FormField} for each language supported by the
 * application.
 * 
 * @param <F>
 *        The type of the inner {@link FormField}s.
 * @param <V>
 *        The value type of the {@link I18NField} representing an internationalized text, e.g.
 *        {@link ResKey}.
 * @param <B>
 *        The builder type for the value of the {@link I18NField}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class I18NField<F extends FormField, V, B> extends CompositeField {

	/** Field name language separator. */
	public static final String FIELD_NAME_LANGUAGE_SEPARATOR = "_";

	/** Property for language of the language fields. */
	public static final Property<Locale> LANGUAGE = InternationalizationEditor.LOCALE;

	/** Property for (temporary) disabling value listener in field. */
	public static final Property<Boolean> LISTENER_DISABLED =
		TypedAnnotatable.property(Boolean.class, "listenerDisabled");

	private static final String PROFY_FIELD_SUFFIX = "proxy";

	private final boolean _isMandatory;

	private final boolean _isDisabled;

	private final FormField _proxyField;

	private final Constraint _constraint;

	private final Constraint _mandatoryConstraint;

	private List<F> _languageFields;

	/**
	 * Creates a {@link I18NField}.
	 * 
	 * @implNote It is important to {@link #initLanguageFields() initialise} the language fields
	 *           after creation, i.e. caller must either trigger {@link #initLanguageFields()} or
	 *           add this note.
	 */
	protected I18NField(String fieldName, boolean isMandatory, boolean isDisabled, Constraint constraint, Constraint mandatoryConstraint) {
		super(fieldName, com.top_logic.layout.form.values.edit.editor.I18NConstants.LANGUAGE);
		_isMandatory = isMandatory;
		_isDisabled = isDisabled;
		_constraint = constraint;
		_mandatoryConstraint = mandatoryConstraint;
		_proxyField = createProxyField(isMandatory, isDisabled);
		addMember(_proxyField);
	}

	/**
	 * Creates the fields for the languages and adds them as children to this
	 * {@link CompositeField}.
	 * 
	 * <p>
	 * This method must be called after creation of this field.
	 * </p>
	 */
	protected void initLanguageFields() {
		if (_languageFields != null) {
			return;
		}
		_languageFields = createLanguageFields(_isMandatory, _isDisabled, _constraint);
		addListeners(_proxyField, _languageFields);
	}

	/**
	 * Returns the constraint for the inner fields. May be <code>null</code>.
	 */
	public Constraint getConstraint() {
		return _constraint;
	}

	@Override
	public void checkDependency() {
		for (F langField : getLanguageFields()) {
			langField.checkDependency();
		}
	}

	@Override
	public boolean addDependant(FormField dependant) {
		boolean result = false;
		for (F langField : getLanguageFields()) {
			result |= langField.addDependant(dependant);
		}
		return result;
	}

	@Override
	public boolean removeDependant(FormField dependant) {
		boolean result = false;
		for (F langField : getLanguageFields()) {
			result |= langField.removeDependant(dependant);
		}
		return result;
	}

	private FormField createProxyField(boolean isMandatory, boolean isDisabled) {
		FormField field = new I18NProxyField(PROFY_FIELD_SUFFIX);
		field.setMandatory(isMandatory);
		field.setDisabled(isDisabled);
		addListener(FormMember.LABEL_PROPERTY, new LabelChangedListener() {

			@Override
			public Bubble handleLabelChanged(Object sender, String oldLabel, String newLabel) {
				if (sender != I18NField.this) {
					// Label was set to a child of group where this listener was added to.
					return Bubble.BUBBLE;
				}
				String proxyLabel;
				if (newLabel == null) {
					proxyLabel = newLabel;
				} else {
					ResKey label = I18NConstants.I18N_PROXY_FIELD_LABEL__FIELD.fill(newLabel);
					proxyLabel = Resources.getInstance().getString(label);

				}
				field.setLabel(proxyLabel);
				return Bubble.BUBBLE;
			}

		});
		return field;
	}

	private List<F> createLanguageFields(boolean isMandatory, boolean isDisabled, Constraint constraint) {
		List<Locale> supportedLanguages = Resources.getInstance().getSupportedLocalesInDisplayOrder();
		List<F> fields = new ArrayList<>(supportedLanguages.size());
		I18NValueChangedListener listener = new I18NValueChangedListener();
		for (Locale language : supportedLanguages) {
			String innerFieldName = language.getLanguage();
			F field = createLanguageSpecificField(innerFieldName, isMandatory, isDisabled, constraint, language);
			field.set(LANGUAGE, language);
			field.addValueListener(listener);
			fields.add(field);
			addMember(field);
		}
		return fields;
	}

	/**
	 * Creates the input field for the given language.
	 * 
	 * @param fieldName
	 *        Never null.
	 * @param constraint
	 *        Null, if there is no constraint.
	 * @param language
	 *        Language to input in the resulting field. See
	 *        {@link ResourcesModule#getSupportedLocales()}.
	 * @return Is not allowed to be null.
	 */
	protected abstract F createLanguageSpecificField(String fieldName, boolean isMandatory, boolean isDisabled,
			Constraint constraint, Locale language);

	private void addListeners(final FormField proxyField, final List<F> fields) {
		proxyField.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (!Utils.getbooleanValue(field.get(LISTENER_DISABLED))) {
					V i18nValue = toI18NValue(newValue);
					for (F langField : fields) {
						langField.set(LISTENER_DISABLED, Boolean.TRUE);
						Locale locale = langField.get(LANGUAGE);
						langField.setValue(localize(locale, i18nValue));
						langField.set(LISTENER_DISABLED, null);
					}
				}
			}

		});
		proxyField.addWarningConstraint(new AbstractConstraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				Resources res = Resources.getInstance();
				StringBuilder sb = new StringBuilder();
				for (F field : fields) {
					if (field.hasWarnings()) {
						Locale language = field.get(LANGUAGE);
						if (sb.length() > 0) {
							sb.append(StringServices.LINE_BREAK);
						}
						sb.append(InternationalizationEditor.translateLanguageName(res, language) + ": ");
						sb.append(StringServices.toString(field.getWarnings(), " "));
					}
				}
				if (sb.length() > 0) {
					throw new CheckException(sb.toString());
				}
				return true;
			}
		});
		addListener(DISABLED_PROPERTY, new DisabledPropertyListener() {
			@Override
			public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				for (F field : fields) {
					field.setDisabled(newValue);
				}
				return Bubble.BUBBLE;
			}
		});
		addListener(IMMUTABLE_PROPERTY, new ImmutablePropertyListener() {
			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				for (F field : fields) {
					field.setImmutable(newValue);
				}
				return Bubble.BUBBLE;
			}
		});
		addListener(MANDATORY_PROPERTY, new MandatoryChangedListener() {
			@Override
			public Bubble handleMandatoryChanged(FormField sender, Boolean oldValue, Boolean newValue) {
				for (F field : fields) {
					field.setMandatory(newValue);
				}
				return Bubble.BUBBLE;
			}
		});
		addListener(BLOCKED_PROPERTY, new BlockedStateChangedListener() {
			@Override
			public Bubble handleIsBlockedChanged(FormField sender, Boolean oldValue, Boolean newValue) {
				for (F field : fields) {
					field.setBlocked(newValue);
				}
				return Bubble.BUBBLE;
			}
		});
		new I18NValueErrorWarningsUpdater(fields).attach();
	}

	/**
	 * Filters, casts or converts the given value.
	 * <p>
	 * This method is used if a (default) value is set on the whole {@link I18NField} and is about
	 * to be distributed to the localized {@link FormField}s. As {@link #setValue(Object)} and
	 * {@link #setDefaultValue(Object)} are untyped, there are no guarantees that the value has the
	 * correct type.
	 * </p>
	 * 
	 * @param value
	 *        May be null.
	 * @return A value representing the whole internationalized field value.
	 */
	protected abstract V toI18NValue(Object value);

	/**
	 * Computes the localized value of the given internationalizable model value in the given
	 * locale.
	 * 
	 * @param locale
	 *        Never null.
	 * @param i18nValue
	 *        Internal representation of the internationalized text value of this field. Null, if
	 *        this {@link I18NField} has no value.
	 * @return Value for the
	 *         {@link #createLanguageSpecificField(String, boolean, boolean, Constraint, Locale)
	 *         language specific field} in the given locale. Null, if there is no value for the
	 *         given {@link Locale}.
	 */
	protected abstract Object localize(Locale locale, V i18nValue);

	@Override
	protected FormField getProxy() {
		return _proxyField;
	}

	/**
	 * Gets the language fields.
	 */
	public List<F> getLanguageFields() {
		if (_languageFields == null) {
			throw new IllegalStateException("Field not initialized.");
		}
		return _languageFields;
	}

	/**
	 * Updates the value of the proxy field with the values of the language fields.
	 */
	protected void transportValues() {
		FormField proxy = getProxy();
		B builder = createValueBuilder();

		Resources res = Resources.getInstance();
		StringBuilder sb = new StringBuilder();
		boolean hasError = false, hasValue = false;
		for (F field : getLanguageFields()) {
			if (field.hasError()) {
				if (hasError) {
					sb.append(StringServices.LINE_BREAK);
				}
				Locale language = field.get(LANGUAGE);
				sb.append(InternationalizationEditor.translateLanguageName(res, language) + ": " + field.getError());
				hasError = true;
			} else if (field.hasValue()) {
				Locale locale = field.get(LANGUAGE);
				addValueToBuilder(builder, proxy, locale, field);
				hasValue = true;
			}
		}

		proxy.set(LISTENER_DISABLED, Boolean.TRUE);
		proxy.setValue(hasValue ? buildValue(builder) : null);
		if (hasError) {
			proxy.setError(sb.toString());
		}
		proxy.set(LISTENER_DISABLED, null);
	}

	/**
	 * Creates an object that can {@link #addValueToBuilder(Object, FormField, Locale, FormField) accumulate}
	 * the values for each {@link Locale}, until the whole value is {@link #buildValue(Object)
	 * built}.
	 * 
	 * @return Is not allowed to be null.
	 */
	protected abstract B createValueBuilder();

	/**
	 * Store the value from the given {@link FormField} in the given build.
	 * 
	 * @param builder
	 *        Never null.
	 * @param proxy
	 *        See {@link #getProxy()}.
	 * @param locale
	 *        Never null.
	 * @param field
	 *        Never null.
	 */
	protected abstract void addValueToBuilder(B builder, FormField proxy, Locale locale, F field);

	/**
	 * Build a value for this {@link I18NField} from the values accumulated in the given builder.
	 * 
	 * @param builder
	 *        Never null.
	 * @return Null, if that is the representation of the accumulated values: Either, because there
	 *         are no accumulated values, they are all null or empty.
	 */
	protected abstract V buildValue(B builder);

	/**
	 * {@link ValueListener} on language fields updating the proxy field.
	 */
	public class I18NValueChangedListener implements ValueListener {

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			if (!Utils.getbooleanValue(field.get(LISTENER_DISABLED))) {
				transportValues();
			}
		}
	}

	/**
	 * Listener to transport {@link FormField#getError()} and {@link FormField#getWarnings()} from
	 * language fields to the proxy field.
	 */
	public class I18NValueErrorWarningsUpdater implements ErrorChangedListener {

		private final List<F> _fields;

		/**
		 * Creates a new {@link I18NValueErrorWarningsUpdater}.
		 */
		public I18NValueErrorWarningsUpdater(List<F> fields) {
			_fields = fields;
		}

		@Override
		public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
			transportValues();
			return Bubble.CANCEL_BUBBLE;
		}

		/**
		 * Finally establishes this updater by adding the implementing listeners to the fields.
		 * 
		 * @see #detach() for removing all listeners that implement this updater.
		 */
		public void attach() {
			for (F field : _fields) {
				field.addListener(FormField.ERROR_PROPERTY, this);
			}
		}

		/**
		 * Removes all listeners that implement this updater from the fields.
		 * 
		 * @see #attach() for adding all listeners that implement this updater.
		 */
		public void detach() {
			for (F field : _fields) {
				field.removeListener(FormField.ERROR_PROPERTY, this);
			}

		}

	}

	/**
	 * Proxy field for {@link I18NField}.
	 * 
	 * @see I18NField#getProxy()
	 */
	public class I18NProxyField extends HiddenField {

		/**
		 * Creates a new {@link I18NProxyField}.
		 */
		protected I18NProxyField(String name) {
			super(name);
		}

		/**
		 * Overridden to dispatch default value changes to language fields as there exists no
		 * default value changed listener.
		 */
		@Override
		public void setDefaultValue(Object defaultValue) {
			super.setDefaultValue(defaultValue);
			if (!Utils.getbooleanValue(_proxyField.get(LISTENER_DISABLED))) {
				V i18nValue = toI18NValue(defaultValue);
				for (F langField : getLanguageFields()) {
					langField.set(LISTENER_DISABLED, Boolean.TRUE);
					Locale locale = langField.get(LANGUAGE);
					langField.setDefaultValue(localize(locale, i18nValue));
					langField.set(LISTENER_DISABLED, null);
				}
			}
		}

		@Override
		protected Constraint mandatoryConstraint() {
			return _mandatoryConstraint;
		}

	}

}

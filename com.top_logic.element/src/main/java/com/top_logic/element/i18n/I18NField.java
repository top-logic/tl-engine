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
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.BlockedStateChangedListener;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.LabelChangedListener;
import com.top_logic.layout.form.MandatoryChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.ToBeValidated;

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

	/** Property for whether all languages should be displayed in view mode. */
	public static final Property<Boolean> DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE =
		TypedAnnotatable.property(Boolean.class, "all values", Boolean.FALSE);

	/** Field name language separator. */
	public static final String FIELD_NAME_LANGUAGE_SEPARATOR = "_";

	/** Property for language of the language fields. */
	public static final Property<Locale> LANGUAGE = InternationalizationEditor.LOCALE;

	/** Property for (temporary) disabling value listener in field. */
	public static final Property<Boolean> LISTENER_DISABLED =
		TypedAnnotatable.property(Boolean.class, "listenerDisabled");

	private static final String PROFY_FIELD_SUFFIX = "proxy";

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
	protected I18NField(String fieldName, boolean isMandatory, boolean immutable, Constraint constraint, Constraint mandatoryConstraint) {
		super(fieldName, com.top_logic.layout.form.values.edit.editor.I18NConstants.LANGUAGE);
		_constraint = constraint;
		_mandatoryConstraint = mandatoryConstraint;
		_proxyField = createProxyField(isMandatory, immutable);
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
		_languageFields = createLanguageFields(getProxy().isMandatory(), getProxy().isLocallyImmutable(), _constraint);
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

	@Override
	public void check() {
		for (F langField : getLanguageFields()) {
			langField.check();
		}
		super.check();
	}

	@Override
	public boolean checkConstraints() {
		for (F langField : getLanguageFields()) {
			if (!langField.checkConstraints()) {
				return false;
			}
		}
		return super.checkConstraints();
	}

	@Override
	public boolean checkConstraints(Object value) {
		for (F langField : getLanguageFields()) {
			if (!langField.checkConstraints(value)) {
				return false;
			}
		}
		return super.checkConstraints(value);
	}

	private FormField createProxyField(boolean isMandatory, boolean immutable) {
		FormField field = new I18NProxyField(PROFY_FIELD_SUFFIX, isMandatory, immutable);
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

	private List<F> createLanguageFields(boolean isMandatory, boolean immutable, Constraint constraint) {
		List<Locale> supportedLanguages = Resources.getInstance().getSupportedLocalesInDisplayOrder();
		List<F> fields = new ArrayList<>(supportedLanguages.size());
		I18NValueChangedListener listener = new I18NValueChangedListener();
		for (Locale language : supportedLanguages) {
			String innerFieldName = language.getLanguage();
			F field = createLanguageSpecificField(innerFieldName, isMandatory, immutable, constraint, language);
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
				if (newValue) {
					if (fields.stream()
						.filter(FormField::hasValue)
						.anyMatch(I18NField.this::hasNonEmptyValue)) {
						// One field has a non empty value. Therefore the fields are marked as non
						// mandatory.
						for (F field : fields) {
							field.setMandatory(false);
						}
					} else {
						for (F field : fields) {
							field.setMandatory(true);
						}
					}
				} else {
					for (F field : fields) {
						field.setMandatory(false);
					}
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
	 * Whether the given language field has a non empty value.
	 */
	protected abstract boolean hasNonEmptyValue(F languageField);

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
	 * Updates the error of the proxy field with the errors of the language fields.
	 */
	protected void transportErrors() {
		ValueWithError proxyValue = createProxyValue();
		updateProxyIgnoreVeto(getProxy(), proxyValue);
	}

	void updateProxyIgnoreVeto(FormField proxy, ValueWithError proxyValue) {
		proxy.set(LISTENER_DISABLED, Boolean.TRUE);
		try {
			FormFieldInternals.updateFieldNoClientUpdate(proxy, proxyValue);
		} catch (VetoException ex) {
			// Ignore
		} finally {
			proxy.reset(LISTENER_DISABLED);
		}
	}

	/**
	 * Updates the value of the proxy field with the values of the language fields.
	 */
	protected void transportValues(FormField sender, Object formerValue) {
		ValueWithError proxyValue = createProxyValue();
		FormField proxy = getProxy();
		proxy.set(LISTENER_DISABLED, Boolean.TRUE);
		try {
			FormFieldInternals.updateFieldNoClientUpdate(proxy, proxyValue);
		} catch (VetoException ex) {
			/* TODO Ticket #15445: Value can not be reset by the ValueListener that triggers the
			 * change. Therefore the whole exception handling is done in a validation step. */
			DefaultDisplayContext.getDisplayContext().getLayoutContext().notifyInvalid(new ToBeValidated() {

				@Override
				public void validate(DisplayContext validationContext) {
					handleVetoInProxy(proxy, proxyValue, sender, formerValue, ex);
				}
			});
		} finally {
			proxy.reset(LISTENER_DISABLED);
		}
	}

	/**
	 * Handles a {@link VetoException} when setting value to {@link #getProxy()} does not work.
	 * 
	 * <p>
	 * Reverts the value in the changed field and sets a continuation to the {@link Exception} that
	 * sets the proxy value again.
	 * </p>
	 */
	void handleVetoInProxy(FormField proxy, ValueWithError proxyValue, FormField changedField, Object formerValue,
			VetoException ex) {
		changedField.setValue(formerValue);
		ex.setContinuationCommand(new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext continuationContext) {
				updateProxyIgnoreVeto(proxy, proxyValue);
				return HandlerResult.DEFAULT_RESULT;
			}
		});
		ex.process(DefaultDisplayContext.getDisplayContext().getWindowScope());
	}

	/**
	 * Creates the "client side" value of the proxy field from the values and errors of the language
	 * fields.
	 */
	protected ValueWithError createProxyValue() {
		FormField proxy = getProxy();
		B builder = createValueBuilder();
		boolean updateMandatory = proxy.isMandatory();
		boolean allFieldsEmpty = true;
		for (F field : getLanguageFields()) {
			if (field.hasValue()) {
				Locale locale = field.get(LANGUAGE);
				addValueToBuilder(builder, proxy, locale, field);
			}
			if (updateMandatory && allFieldsEmpty && hasNonEmptyValue(field)) {
				allFieldsEmpty = false;
			}
		}
		if (updateMandatory) {
			for (F field : getLanguageFields()) {
				field.setMandatory(allFieldsEmpty);
				field.check();
			}
		}

		Resources res = Resources.getInstance();
		StringBuilder sb = new StringBuilder();
		boolean hasError = false;
		for (F field : getLanguageFields()) {
			if (field.hasError()) {
				if (hasError) {
					sb.append(StringServices.LINE_BREAK);
				}
				Locale language = field.get(LANGUAGE);
				sb.append(InternationalizationEditor.translateLanguageName(res, language) + ": " + field.getError());
				hasError = true;
			}
		}
		return new ValueWithError(buildValue(builder), sb);
	}

	/**
	 * Creates an object that can {@link #addValueToBuilder(Object, FormField, Locale, FormField)
	 * accumulate} the values for each {@link Locale}, until the whole value is
	 * {@link #buildValue(Object) built}.
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
				transportValues(field, oldValue);
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
			transportErrors();
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

	static class ValueWithError {

		private Object _value;

		private StringBuilder _error;

		/**
		 * Creates a {@link I18NField.ValueWithError}.
		 *
		 */
		public ValueWithError(Object value, StringBuilder error) {
			_value = value;
			_error = error;
		}

		Object value() throws CheckException {
			if (_error != null && _error.length() > 0) {
				throw new CheckException(_error.toString());
			}
			return _value;
		}

	}

	/**
	 * Proxy field for {@link I18NField}.
	 * 
	 * @see I18NField#getProxy()
	 */
	public class I18NProxyField extends AbstractFormField {

		/**
		 * Creates a new {@link I18NProxyField}.
		 */
		protected I18NProxyField(String name, boolean mandatory, boolean immutable) {
			super(name, mandatory, immutable, NORMALIZE, NO_CONSTRAINT);
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

		@Override
		public <R, A> R visit(FormMemberVisitor<R, A> v, A arg) {
			return v.visitFormField(this, arg);
		}

		@Override
		protected Object parseRawValue(Object rawValue) throws CheckException {
			if (rawValue == NO_RAW_VALUE) {
				return null;
			}
			return ((ValueWithError) rawValue).value();
		}

		@Override
		protected Object unparseValue(Object value) {
			if (value == null) {
				return null;
			}
			return new ValueWithError(value, null);
		}

		@Override
		protected Object narrowValue(Object value) throws IllegalArgumentException, ClassCastException {
			return value;
		}

	}

}

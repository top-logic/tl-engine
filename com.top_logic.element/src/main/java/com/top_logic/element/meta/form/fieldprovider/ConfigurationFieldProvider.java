/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Container;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.element.meta.form.AbstractFieldProvider;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.ChangeStateListener;
import com.top_logic.layout.form.ErrorChangedListener;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.HasErrorChanged;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.internal.ConfigurationStorageMapping;

/**
 * {@link FieldProvider} for attributes using the {@link ConfigurationStorageMapping}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfigurationFieldProvider extends AbstractFieldProvider {

	private static final Property<EditContext> EDIT_CONTEXT =
		TypedAnnotatable.property(EditContext.class, "editContext");

	/**
	 * Context for editing {@link ConfigurationItem} values in model attributes.
	 * 
	 * <p>
	 * When an editor is built for a {@link ConfigurationItem} stored in a persistent object
	 * property, it is placed in a surrounding {@link ConfigEditContext} configuration with
	 * {@link #getBaseModel()} being set to the edited instance. This allows to access this context
	 * from e.g. option providers declared for properties of the edited configuration. To use this
	 * feature, the {@link ConfigurationItem} type edited must extend {@link ConfigPart} and declare
	 * {@link ConfigEditContext} as its {@link Container} property type.
	 * </p>
	 *
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface ConfigEditContext extends ConfigurationItem {

		/**
		 * @see #getBaseModel()
		 */
		String BASE_MODEL = "base-model";

		/**
		 * @see #getValue()
		 */
		String VALUE = "value";

		/**
		 * The persistent object, in which the {@link ConfigurationItem} value is stored.
		 * 
		 * <p>
		 * Kept for backwards compatibility. Can also be accessed through {@link #getEditContext()}.
		 * </p>
		 */
		@Name(BASE_MODEL)
		@InstanceFormat
		TLObject getBaseModel();

		/**
		 * @see #getBaseModel()
		 */
		void setBaseModel(TLObject value);

		/**
		 * The actual {@link ConfigurationItem} being edited.
		 */
		@Name(VALUE)
		@Subtypes({})
		ConfigurationItem getValue();

		/**
		 * @see #getValue()
		 */
		void setValue(ConfigurationItem value);

		/**
		 * The context of the value being edited.
		 */
		@InstanceFormat
		EditContext getEditContext();

		/**
		 * @see #getEditContext()
		 */
		void setEditContext(EditContext value);

	}

	/**
	 * Retrieves the {@link EditContext}.
	 */
	public static EditContext editContext(TypedAnnotatable options) {
		return options.get(EDIT_CONTEXT);
	}

	/**
	 * {@link CompositeField} building an editor for a {@link ConfigurationItem} value set.
	 *
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private final class ConfigurationField extends CompositeField {
		private final FormField _valueField;

		final Class<? extends ConfigurationItem> _configType;

		final EditContext _editContext;

		FormGroup _configGroup;

		/**
		 * Creates a {@link ConfigurationField}.
		 * 
		 * @param editContext
		 *        The context of the value being edited.
		 * @param name
		 *        See {@link #getName()}.
		 * @param configType
		 *        The type of configuration to edit/create.
		 */
		public ConfigurationField(EditContext editContext, String name, Class<? extends ConfigurationItem> configType) {
			super(name, ResPrefix.NONE);
			_editContext = editContext;
			_configType = configType;

			_valueField = FormFactory.newHiddenField("model");
			addMember(getValueField());

			OnValueChange listener = new OnValueChange();
			getValueField().addValueListener(listener);

			// Initialize value.
			listener.valueChanged(null, null, null);

			Templates.template(this, Templates.div(Templates.member("config")));
		}

		class OnValueChange implements ValueListener {
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (_configGroup != null && newValue == EditorFactory.getModel(_configGroup)) {
					return;
				}
				if (_configGroup != null) {
					removeMember(_configGroup);
				}
				_configGroup = new FormGroup("config", ResPrefix.NONE);
				addMember(_configGroup);
				ConfigurationItem model = TypedConfiguration.newConfigItem(_configType);

				ConfigEditContext context = TypedConfiguration.newConfigItem(ConfigEditContext.class);
				context.setBaseModel(_editContext.getObject());
				context.setEditContext(_editContext);
				context.setValue(model);

				if (newValue != null) {
					ConfigCopier.copyContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
						(ConfigurationItem) newValue, model);
				}

				InitializerProvider initializers = new InitializerIndex();
				initializers.set(EDIT_CONTEXT, _editContext);
				new EditorFactory(initializers).initEditorGroup(_configGroup, model);

				propagateValueOnChange(model);

				_configGroup.addListener(FormField.ERROR_PROPERTY, new ErrorChangedListener() {
					@Override
					public Bubble handleErrorChanged(FormField sender, String oldError, String newError) {
						if (!StringServices.isEmpty(newError)) {
							if (!getValueField().hasError()) {
								getValueField().setError(newError);
							}
						}
						return Bubble.BUBBLE;
					}
				});
				_configGroup.addListener(FormField.HAS_ERROR_PROPERTY, new HasErrorChanged() {
					@Override
					public Bubble hasErrorChanged(FormField sender, Boolean oldError, Boolean newError) {
						if (!newError) {
							getValueField().clearError();
						}
						return null;
					}
				});
			}

			private void propagateValueOnChange(ConfigurationItem model) {
				MemberChangedListener listener = new MemberChangedListener() {
					@Override
					public Bubble memberRemoved(FormContainer parent, FormMember member) {
						getValueField().setValue(model);
						return Bubble.BUBBLE;
					}

					@Override
					public Bubble memberAdded(FormContainer parent, FormMember member) {
						getValueField().setValue(model);
						return Bubble.BUBBLE;
					}
				};
				ChangeStateListener changedListener = new ChangeStateListener() {
					@Override
					public Bubble handleChangeStateChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
						if (newValue) {
							getValueField().setValue(model);
						}
						return Bubble.BUBBLE;
					}
				};
				_configGroup.addListener(FormContainer.MEMBER_ADDED_PROPERTY, listener);
				_configGroup.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, listener);
				_configGroup.addListener(FormMember.IS_CHANGED_PROPERTY, changedListener);
			}
		}

		@Override
		public boolean removeDependant(FormField dependant) {
			return false;
		}

		@Override
		public void checkDependency() {
			// Ignore.
		}

		@Override
		public boolean addDependant(FormField dependant) {
			throw new UnsupportedOperationException();
		}

		@Override
		protected FormField getProxy() {
			return getValueField();
		}

		FormField getValueField() {
			return _valueField;
		}
	}

	@Override
	public FormMember createFormField(com.top_logic.element.meta.form.EditContext update, String fieldName) {
		TLPrimitive type = (TLPrimitive) update.getValueType();
		ConfigurationStorageMapping storage = (ConfigurationStorageMapping) type.getStorageMapping();
		Class<? extends ConfigurationItem> configType = storage.getApplicationType();

		return new ConfigurationField(update, fieldName, configType);
	}

}

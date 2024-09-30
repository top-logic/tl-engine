/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationChange;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.Listener;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.Utils;

/**
 * {@link Editor} creating the UI for a {@link ConfigurationItem}-valued property.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ItemEditor implements Editor {

	/**
	 * CSS class for the whole item display (in non-compact mode).
	 */
	public static final String ITEM_CSS_CLASS = "dfItem";

	/**
	 * CSS class for the item title content (in non-compact mode).
	 */
	public static final String ITEM_TITLE_CSS_CLASS = "dfItemTitle";

	/**
	 * CSS class for the item fieldset content (in non-compact mode).
	 */
	public static final String ITEM_CONTENT_CSS_CLASS = "dfItemContent";

	/**
	 * CSS class for displaying a toolbar within a fieldset title box (in non-compact mode).
	 */
	public static final String TOOLBAR_CSS_CLASS = "dfToolbar";

	private static final String OPTIONS_NAME = "options";

	private static final String OUTER_CONTAINER_NAME = "outerContainer";

	static final String CONTENT_CONTAINER_NAME = "contentContainer";

	private static final String CONTENT_GROUP = "content";

	private static final String NO_CONTENTS_FIELD = "noContents";

	/**
	 * Singleton {@link ItemEditor} instance.
	 */
	public static final ItemEditor INSTANCE = new ItemEditor();

	private ItemEditor() {
		// Singleton constructor.
	}

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel valueModel) {
		PropertyDescriptor property = valueModel.getProperty();
		FormGroup result = group(container, editorFactory, property);

		if (!editorFactory.processControlProviderAnnotation(property, result)) {
			template(result, embedd(member(OUTER_CONTAINER_NAME)));
		}

		initContainer(editorFactory, embedded(group(result, OUTER_CONTAINER_NAME)), valueModel, result);

		return result;
	}

	private void initContainer(EditorFactory editorFactory, FormGroup container, ValueModel valueModel,
			FormGroup result) {
		final ConfigurationItem model = valueModel.getModel();
		final PropertyDescriptor property = valueModel.getProperty();

		final FormGroup contentContainer = embedded(group(container, CONTENT_CONTAINER_NAME));

		// Updates UI, whenever item value changes.
		ConfigurationListener updateUI = new ConfigurationListener() {
			@Override
			public void onChange(ConfigurationChange change) {
				// Drop old member, if exists.
				if (contentContainer.hasMember(CONTENT_GROUP)) {
					contentContainer.removeMember(CONTENT_GROUP);
				}

				// Create new contents group.
				FormGroup contents = group(contentContainer, CONTENT_GROUP);

				Object newValue = value(model, property);
				if (newValue != null) {
					// Build contents form.
					editorFactory.initEditorGroup(contents, property.getConfigurationAccess().getConfig(newValue));
				} else {
					if (!editorFactory.isCompact()) {
						// Placeholder text that represents a complex content that is not yet
						// created.
						FormField field = field(contents, NO_CONTENTS_FIELD);
						field.setImmutable(true);
						field.initializeField(Resources.getInstance().getString(I18NConstants.NO_CONTENTS));
						field.setLabel(ResKey.EMPTY_TEXT);
					}
				}

				if (property.isInstanceValued()) {
					// Prevent editing the configuration of an already instantiated implementation.
					contents.setImmutable(true);
				}
			}
		};
		model.addConfigurationListener(property, updateUI);
		updateUI.onChange(null);

		DerivedProperty<? extends Iterable<?>> optionProvider =
			optionProvider(editorFactory.formOptions(property));
		Value<? extends Iterable<?>> optionsValue;
		if (optionProvider != null) {
			optionsValue = optionProvider.getValue(model);
		} else {
			optionsValue = literal(null);
		}

		Object singletonOption = ListEditor.getSingletonOption(valueModel, optionProvider);

		if (property.isMandatory() && !property.isInstanceValued()) {
			ConfigurationAccess configAccess = valueModel.getProperty().getConfigurationAccess();
			ConfigurationItem defaultValue = configAccess.getConfig(valueModel.getValue());
			if (defaultValue == null) {
				if (optionProvider == null || singletonOption != null) {
					Object value = createDefaultValue(editorFactory, property, optionProvider, singletonOption);
					valueModel.setValue(value);
				} else {
					LabelProvider optionLabels = editorFactory.getOptionLabels(property);
					Listener valueUpdateOnOptionsChange =
						new ValueUpdateOnOptionsChange(editorFactory, valueModel,
							Fields.optionMapping(optionProvider),
							getOptionComparator(property, optionLabels));
					optionsValue.addListener(valueUpdateOnOptionsChange);
					valueUpdateOnOptionsChange.handleChange(optionsValue);
				}
			}
		}

		boolean singleOption;
		if (optionProvider == null || singletonOption != null) {
			CommandField createRemove = button(container, OPTIONS_NAME, null, new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					if (valueModel.getValue() == null) {
						ConfigurationItem defaultConfig =
							createDefaultConfig(property, optionProvider, singletonOption);
						if (property.isInstanceValued()) {
							CreateHandler createHandler = new CreateHandler() {
								@Override
								public void addElement(Object entry, ConfigurationItem elementModel) {
									valueModel.setValue(entry);
								}
							};
							InitializerUtil.initProperties(editorFactory, defaultConfig);
							return new AddDialog(editorFactory, createHandler, property, defaultConfig)
								.open(context);
						} else {
							Object newValue = createDefaultValue(editorFactory, property, defaultConfig);
							valueModel.setValue(newValue);
						}
					} else {
						valueModel.setValue(null);
					}
					return HandlerResult.DEFAULT_RESULT;
				}
			});
			createRemove.setControlProvider(Buttons.REMOVE_BUTTON);
			Value<Boolean> isEmpty = isEmpty(configurationValue(model, property));
			bindLabel(createRemove, ifElse(isEmpty,
				I18NConstants.CREATE_OBJECT,
				I18NConstants.DELETE_OBJECT));
			bindImage(createRemove, ifElse(isEmpty, com.top_logic.layout.form.values.edit.Icons.ADD_ICON,
				com.top_logic.layout.form.values.edit.Icons.REMOVE_ICON));
			bindNotExecutableImage(createRemove,
				ifElse(isEmpty, com.top_logic.layout.form.values.edit.Icons.ADD_ICON_DISABLED,
					com.top_logic.layout.form.values.edit.Icons.REMOVE_ICON_DISABLED));
			if (!property.isNullable() || (property.isMandatory() && !property.isInstanceValued())) {
				createRemove.setVisible(false);
			} else {
				Value<FieldMode> fieldMode = editorFactory.fieldMode(property, model);
				bindVisible(createRemove, not(or(map(fieldMode, IsLocallyImmutable.INSTANCE), isImmutable(result))));
			}
			singleOption = true;
		} else {
			SelectField optionsSelector = selectField(container, OPTIONS_NAME);
			optionsSelector.setLabel(container.getParent().getLabel());
			LabelProvider optionLabels = editorFactory.getOptionLabels(property);
			optionsSelector.setOptionLabelProvider(optionLabels);
			optionsSelector.setOptionComparator(getOptionComparator(property, optionLabels));
			if (!property.isNullable() || (property.isMandatory() && !property.isInstanceValued())) {
				optionsSelector.setMandatory(true);
			}

			OptionMapping optionMapping = Fields.optionMapping(optionProvider);
			bindOptions(optionsSelector, optionsValue, true);
			bindValue(optionsSelector, configurationValue(valueModel),
				selection -> {
					Object option = optionMapping.asOption(optionsSelector.getOptions(), selection);
					return CollectionUtilShared.singletonOrEmptyList(option);
				});

			optionsSelector.addValueListener(new SetValue(editorFactory, valueModel, optionMapping));
			singleOption = false;
		}

		HTMLTemplateFragment template;
		if (editorFactory.isCompact()) {
			template = embedd(member(OPTIONS_NAME), member(CONTENT_CONTAINER_NAME));
		} else {
			boolean minimized = Fields.displayMinimized(editorFactory, valueModel.getProperty());
			BlockControl errorBlock = EditorUtils.errorBlock(container);
			HTMLTemplateFragment legend;
			/* The actual label of the outer container is the label of the result which is the
			 * parent of the outer container. Therefore the label of the parent (..) must be
			 * used. */
			String parent = "..";
			if (singleOption) {
				legend = fragment(
					span(css(ITEM_TITLE_CSS_CLASS), label(parent), htmlTemplate(errorBlock)),
					span(css(TOOLBAR_CSS_CLASS), member(OPTIONS_NAME)));
			} else {
				legend = fragment(span(css(ITEM_TITLE_CSS_CLASS), labelWithColon(parent), htmlTemplate(errorBlock),
					member(OPTIONS_NAME)));

			}
			template = div(css(ITEM_CSS_CLASS),
				fieldsetBox(
					legend,
					div(css(ITEM_CONTENT_CSS_CLASS), embedd(member(CONTENT_CONTAINER_NAME))),
					ConfigKey.field(result))
						.setInitiallyCollapsed(minimized)
						.setInitializer(EditorUtils.showIfCollapsed(errorBlock)));
		}
		template(container, template);

		optionsValue.addListener(sender -> {
			FormContainer parent = container.getParent();
			if (parent == null) {
				return;
			}
			parent.removeMember(container);
			initContainer(editorFactory, embedded(group(parent, OUTER_CONTAINER_NAME)), valueModel, result);
		});
	}

	private Object createDefaultValue(final EditorFactory editorFactory, final PropertyDescriptor property,
			DerivedProperty<? extends Iterable<?>> optionProvider, Object singletonOption) {
		ConfigurationItem config = createDefaultConfig(property, optionProvider, singletonOption);
		return createDefaultValue(editorFactory, property, config);
	}

	ConfigurationItem createDefaultConfig(final PropertyDescriptor property,
			DerivedProperty<? extends Iterable<?>> optionProvider, Object singletonOption) {
		return ListEditor.defaultItemConstructor(property, optionProvider, singletonOption).get();
	}

	Object createDefaultValue(EditorFactory editorFactory, final PropertyDescriptor property,
			ConfigurationItem defaultConfig) {
		return property.getConfigurationAccess().createValue(editorFactory.getInstantiationContext(), defaultConfig);
	}

	static <M extends FormMember> M embedded(M member) {
		return template(member, embedd(items(self())));
	}

	private static class SetValue implements ValueListener {

		private final ValueModel _valueModel;

		private final EditorFactory _editorFactory;

		private final OptionMapping _optionMapping;

		public SetValue(EditorFactory editorFactory, ValueModel valueModel, OptionMapping optionMapping) {
			_editorFactory = editorFactory;
			_optionMapping = optionMapping;
			_valueModel = valueModel;
		}

		@Override
		public void valueChanged(FormField field, Object oldValue, Object newValue) {
			final ValueModel valueModel = _valueModel;
			PropertyDescriptor property = valueModel.getProperty();
			Object option = CollectionUtil.getSingleValueFrom(newValue);

			OptionModel<?> options = ((SelectField) field).getOptionModel();
			if (Utils.equals(_optionMapping.asOption(options, valueModel.getValue()), option)) {
				return;
			}

			ConfigurationItem newConfig = (ConfigurationItem) _optionMapping.toSelection(option);
			if (newConfig != null && property.isInstanceValued()) {
				CreateHandler createHandler = new CreateHandler() {
					@Override
					public void addElement(Object entry, ConfigurationItem elementModel) {
						valueModel.setValue(entry);
					}
				};
				InitializerUtil.initProperties(_editorFactory, newConfig);
				AddDialog addDialog = new AddDialog(_editorFactory, createHandler, property, newConfig);
				addDialog.open(DefaultDisplayContext.getDisplayContext());
			} else {
				valueModel.setValue(newConfig);
			}
		}
	}

	static Object value(final ConfigurationItem model, final PropertyDescriptor property) {
		return NamePath.value(model, property);
	}
}

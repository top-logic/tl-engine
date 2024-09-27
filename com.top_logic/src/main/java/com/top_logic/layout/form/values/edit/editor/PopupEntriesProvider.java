/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.model.PopupMenuField;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * {@link Provider} for the entries of the {@link PopupMenuField}, which the {@link ItemEditor}
 * builds.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
final class PopupEntriesProvider implements Provider<List<List<CommandModel>>> {

	private final ValueModel _valueModel;

	private final DerivedProperty<? extends Iterable<?>> _optionProvider;

	private final EditorFactory _editorFactory;

	PopupEntriesProvider(ValueModel valueModel, DerivedProperty<? extends Iterable<?>> optionProvider,
			EditorFactory editorFactory) {
		_valueModel = valueModel;
		_optionProvider = optionProvider;
		_editorFactory = editorFactory;
	}

	@Override
	public List<List<CommandModel>> get() {
		List<CommandModel> menu = list();
		LabelProvider labelProvider = createLabelProvider();
		Iterable<?> optionModel = createOptions();
		for (Object option : optionModel) {
			menu.add(createEntry(labelProvider, option));
		}

		Resources resources = Resources.getInstance();

		Collections.sort(menu, LabelComparator.newInstance(new LabelProvider() {
			@Override
			public String getLabel(Object object) {
				return resources.getString(((CommandModel) object).getLabel());
			}
		}));

		if (getProperty().isNullable() && (!getProperty().isMandatory())) {
			menu.add(0, createEmptyEntry());
		}
		return Collections.singletonList(menu);
	}

	private Iterable<?> createOptions() {
		return _optionProvider.get(getValueModel().getModel());
	}

	private OptionMapping optionMapping() {
		return Fields.optionMapping(_optionProvider);
	}

	private LabelProvider createLabelProvider() {
		return _editorFactory.getOptionLabels(getProperty());
	}

	private CommandModel createEmptyEntry() {
		ResKey label = getEmptyEntryLabel();
		return createMenuEntry(null, label);
	}

	private CommandModel createEntry(LabelProvider labelProvider, Object option) {
		ResKey label = ResKey.text(labelProvider.getLabel(option));
		return createMenuEntry(option, label);
	}

	private CommandModel createMenuEntry(Object value, ResKey label) {
		CommandModel entryModel =
			CommandModelFactory.commandModel(new SetValue(_editorFactory, getValueModel(), optionMapping(), value));
		entryModel.setLabel(label);
		return entryModel;
	}

	private ResKey getEmptyEntryLabel() {
		ResKey propertySpecificLabel = Labels.propertyLabelKey(getProperty(), "@empty");
		ResKey genericLabel = I18NConstants.ITEM_SELECTION_POPUP_EMPTY_ENTRY;
		ResKey labelKey = ResKey.fallback(propertySpecificLabel, genericLabel);
		return labelKey;
	}

	private PropertyDescriptor getProperty() {
		return getValueModel().getProperty();
	}

	private ValueModel getValueModel() {
		return _valueModel;
	}

	static final class SetValue implements Command {
		private final ValueModel _valueModel;

		private final Object _option;

		private final EditorFactory _editorFactory;

		private final OptionMapping _optionMapping;

		public SetValue(EditorFactory editorFactory, ValueModel valueModel, OptionMapping optionMapping,
				Object option) {
			_editorFactory = editorFactory;
			_optionMapping = optionMapping;
			_option = option;
			_valueModel = valueModel;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext selectContext) {
			final ValueModel valueModel = _valueModel;
			PropertyDescriptor property = valueModel.getProperty();
			ConfigurationItem newConfig = (ConfigurationItem) _optionMapping.toSelection(_option);
			if (newConfig != null && property.isInstanceValued()) {
				CreateHandler createHandler = new CreateHandler() {
					@Override
					public void addElement(Object entry, ConfigurationItem elementModel) {
						valueModel.setValue(entry);
					}
				};
				InitializerUtil.initProperties(_editorFactory, newConfig);

				return new AddDialog(_editorFactory, createHandler, property, newConfig).open(selectContext);
			} else {
				valueModel.setValue(newConfig);
				return HandlerResult.DEFAULT_RESULT;
			}
		}
	}

}

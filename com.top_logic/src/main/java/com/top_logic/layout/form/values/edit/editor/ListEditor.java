/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationListener;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.OnExpandedControl;
import com.top_logic.layout.form.control.TooltipControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.selection.MultiSelectDialog;
import com.top_logic.layout.form.selection.SelectDialogConfig;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.form.template.model.internal.TemplateRenderer;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.Listener;
import com.top_logic.layout.form.values.ModifiableValue;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.Values;
import com.top_logic.layout.form.values.edit.ConfigLabelProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Icons;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.annotation.TitleProperty;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.resources.AbstractResourceView;
import com.top_logic.layout.scripting.action.RecordingFailedAction;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.scripting.runtime.action.RecordingFailedActionOp;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Creates the edit UI for list-valued properties using the {@link Options} annotation for choosing
 * templates for new entries.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListEditor implements Editor {

	private static final String LIST_SORT = "sort";

	/**
	 * Singleton {@link ListEditor} instance.
	 */
	public static final ListEditor INSTANCE = new ListEditor();

	private ListEditor() {
		// Singleton constructor.
	}

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, final ValueModel valueModel) {
		PropertyDescriptor property = valueModel.getProperty();
		FormGroup list = group(container, editorFactory, property);

		final FormGroup content = group(list, EditorUtils.LIST_CONTENT_GROUP);

		Value<FieldMode> fieldMode = editorFactory.fieldMode(property, valueModel.getModel());
		Value<Boolean> readOnly = Values.map(fieldMode, IsLocallyImmutable.INSTANCE);

		PropertyDescriptor titleProperty;
		boolean forceCreationOfTitleField;
		TitleProperty titleAnnotation = editorFactory.getAnnotation(property, TitleProperty.class);
		if (titleAnnotation != null) {
			String titlePropertyName = titleAnnotation.name();
			if (StringServices.isEmpty(titlePropertyName)) {
				// empty title property is treated as "no title property".
				titleProperty = null;
				forceCreationOfTitleField = false;
			} else {
				titleProperty = resolveTitleProperty(property.getValueDescriptor(), titlePropertyName);
				forceCreationOfTitleField = true;
			}
		} else {
			titleProperty = property.getKeyProperty();
			/* When the title is not explicitly configured, it is unexpected (and in some cases
			 * undesirable) if a field is suddenly displayed for each key attribute. */
			forceCreationOfTitleField = false;
		}

		ElementFactory factory =
			new ElementFactory(editorFactory, content, valueModel, readOnly, titleProperty, forceCreationOfTitleField);

		factory.initElements();

		ConfigurationListener addListener = new AddElementListener(factory);
		valueModel.getModel().addConfigurationListener(property, addListener);

		DerivedProperty<? extends Iterable<?>> optionProvider = optionProvider(editorFactory.formOptions(property));
		FormMember add;

		Object singletonOption = getSingletonOption(valueModel, optionProvider);

		boolean polymorphicOptions = optionProvider != null && singletonOption == null;
		if (polymorphicOptions) {
			add = createPolymorphicAdd(editorFactory, valueModel, list, optionProvider);
		} else {
			add = createMonomorphicAdd(editorFactory, valueModel, list, optionProvider, singletonOption);
		}

		bindTooltip(add, ifElse(readOnly, literal(ResKey.text(null)), literal(addTooltip(valueModel, content))));
		bindLabel(add, ifElse(readOnly, literal(ResKey.text("")), addLabel(valueModel, content)));
		bindVisible(add, not(or(readOnly, Fields.isImmutable(list))));

		if (property.isOrdered()) {
			Command openSortDialog = new OpenSortDialog(valueModel, list);
			CommandField sortButton = button(list, LIST_SORT, Icons.SORT_ELEMENTS, openSortDialog);
			sortButton.setControlProvider(Buttons.SORT_BUTTON);
			sortButton.setNotExecutableImage(Icons.SORT_ELEMENTS_DISABLED);
			sortButton.setLabel(Resources.getInstance().getString(
				I18NConstants.OPEN_SORT_DIALOG__PROPERTY.fill(
					Labels.propertyLabel(valueModel))));
			ScriptingRecorder.annotateAsDontRecord(sortButton);
			bindExecutability(sortButton, map(members(content), new Mapping<List<?>, ExecutableState>() {
				@Override
				public ExecutableState map(List<?> input) {
					if (input == null || input.size() < 2) {
						return ExecutableState.createDisabledState(I18NConstants.LESS_THAN_TWO_SELECTED);
					}
					return ExecutableState.EXECUTABLE;
				}
			}));

			bindVisible(sortButton, not(or(readOnly, Fields.isImmutable(list))));
		}

		BooleanField changeField = EditorUtils.addAdditionalChangedField(list, content, editorFactory, valueModel);
		if (!editorFactory.processControlProviderAnnotation(property, list)) {
			template(list, listEditor(list, changeField, editorFactory, valueModel, polymorphicOptions, titleProperty));
		}
		return list;
	}

	private PropertyDescriptor resolveTitleProperty(ConfigurationDescriptor owningDescriptor,
			String titlePropertyName) {
		PropertyDescriptor titleProperty = owningDescriptor.getProperty(titlePropertyName);
		if (titleProperty == null) {
			throw new ConfigurationError(I18NConstants.ERROR_NO_TITLE_PROPERTY_IN_DESCRIPTOR__DESCRIPTOR_PROPERTY
				.fill(owningDescriptor, titlePropertyName));
		}
		return titleProperty;
	}

	private FormMember createMonomorphicAdd(EditorFactory editorFactory, ValueModel valueModel, FormGroup list,
			DerivedProperty<? extends Iterable<?>> optionProvider, Object singletonOption) {
		Provider<ConfigurationItem> constructor =
			defaultItemConstructor(valueModel.getProperty(), optionProvider, singletonOption);
		CreateHandler createHandler = newCreateHandler(valueModel);
		CreateElementCommand command = new CreateElementCommand(editorFactory, createHandler, valueModel, constructor);
		CommandField addButton = button(list, EditorUtils.LIST_ADD, Icons.ADD_ICON, command);
		addButton.setControlProvider(Buttons.ADD_BUTTON);
		return addButton;
	}

	private FormMember createPolymorphicAdd(EditorFactory editorFactory, ValueModel valueModel, FormGroup list,
			DerivedProperty<? extends Iterable<?>> optionProvider) {
		PropertyDescriptor property = valueModel.getProperty();
		SelectField template = selectField(list, EditorUtils.LIST_ADD);
		Labels.emptyLabel(template, valueModel);

		LabelProvider optionLabels = editorFactory.getOptionLabels(property);
		template.setOptionLabelProvider(optionLabels);
		bindOptions(template, property, valueModel.getModel(), optionProvider, false);
		optionProvider.getValue(valueModel.getModel()).addListener(new ClearCollectionListener(valueModel));
		template.setOptionComparator(getOptionComparator(property, optionLabels));
		ModifiableValue<ConfigurationItem> templateValue = fieldValue(template);
		CreateHandler createHandler = newCreateHandler(valueModel);
		CreateElementFromTemplate listener = new CreateElementFromTemplate(editorFactory, createHandler, valueModel,
			Fields.optionMapping(optionProvider), templateValue);
		onChange(listener, templateValue);
		return template;
	}

	private CreateHandler newCreateHandler(final ValueModel valueModel) {
		CreateHandler handler = new CreateHandler() {
			@Override
			public void addElement(Object entry, ConfigurationItem entryConfig) {
				valueModel.addValue(entry);
			}
		};
		return handler;
	}

	static Provider<ConfigurationItem> defaultItemConstructor(PropertyDescriptor property,
			DerivedProperty<? extends Iterable<?>> optionProvider, Object singletonOption) {
		Provider<ConfigurationItem> constructor;
		if (singletonOption == null) {
			constructor = () -> {
				@SuppressWarnings("unchecked")
				Class<? extends ConfigurationItem> elementType =
					(Class<? extends ConfigurationItem>) property.getElementType();

				return TypedConfiguration.newConfigItem(elementType);
			};
		} else {
			OptionMapping optionMapping = Fields.optionMapping(optionProvider);
			constructor = () -> {
				Object value = optionMapping.toSelection(singletonOption);
				return TypedConfiguration.copy((ConfigurationItem) value);
			};
		}
		return constructor;
	}

	static Object getSingletonOption(final ValueModel valueModel,
			DerivedProperty<? extends Iterable<?>> optionProvider) {
		Object singletonOption = null;
		if (optionProvider != null) {
			Iterable<?> options = optionProvider.get(valueModel.getModel());
			Iterator<?> iterator = options.iterator();
			if (iterator.hasNext()) {
				Object first = iterator.next();
				if (!iterator.hasNext()) {
					singletonOption = first;
				}
			}
		}
		return singletonOption;
	}

	/**
	 * Creates the template for displaying the editor.
	 * 
	 * @param member
	 *        The {@link FormContainer} created for the list (or map) valued property.
	 * @param constraintViolationField
	 *        Field transporting a configuration constraint violation: When the list value of the
	 *        configuration violates some {@link Constraint constraints}, this field carries the
	 *        corresponding errors.
	 * @param editorFactory
	 *        The context {@link EditorFactory}.
	 * @param valueModel
	 *        The property to create the editor for.
	 * @param polymorphicEntries
	 *        Whether there may be entries of different runtime-type.
	 * @param titleProperty
	 *        The property of the entries to display in the title for the entry.
	 * @return The template used for display.
	 */
	protected HTMLTemplateFragment listEditor(FormContainer member, FormField constraintViolationField,
			EditorFactory editorFactory, ValueModel valueModel, boolean polymorphicEntries,
			PropertyDescriptor titleProperty) {
		boolean compact = editorFactory.isCompact();
		PropertyDescriptor property = valueModel.getProperty();
		TagTemplate content =
			div(css("dfList " + ReactiveFormCSS.RF_LINE),
				div(css("dfListContent"),
					member(EditorUtils.LIST_CONTENT_GROUP,
						div(
							items(
								createEntry(compact, polymorphicEntries,
									titleProperty)
							)
						)
					)
				),
				div(css("dfListAdd"),
					label(EditorUtils.LIST_ADD),
					member(EditorUtils.LIST_ADD),
					property.isOrdered() ? member(LIST_SORT) : empty(),
					error(constraintViolationField))
			);
		if (compact) {
			return content;
		} else {
			boolean minimized = Fields.displayMinimized(editorFactory, valueModel.getProperty());
			BlockControl errorBox = EditorUtils.errorBlock(member);
			Consumer<GroupCellControl> initializer = EditorUtils.showIfCollapsed(errorBox);
			if (!compact && titleProperty != null) {
				initializer = initializer.andThen(groupCellControl -> {
					HTMLFragment title = groupCellControl.getTitle();
					Collapsible collapsible = groupCellControl.getCollapsible();
					groupCellControl.setTitle(Fragments.concat(title, listCommands(member, collapsible)));
				});
			}
			return div(
				fieldsetBox(span(label(), htmlTemplate(errorBox)), content, ConfigKey.field(member))
					.setInitiallyCollapsed(minimized)
					.setInitializer(initializer));
		}
	}

	private HTMLFragment listCommands(FormContainer member, Collapsible collapsible) {
		FormContainer contentGroup = (FormContainer) member.getMember(EditorUtils.LIST_CONTENT_GROUP);
		return Fragments.span("dfListCommands",
			new OnExpandedControl(collapsible, collapseCommand(contentGroup, false)).setControlTag(HTMLConstants.SPAN),
			new OnExpandedControl(collapsible, collapseCommand(contentGroup, true)).setControlTag(HTMLConstants.SPAN));
	}


	private HTMLFragment collapseCommand(FormContainer member, boolean collapse) {
		CommandModel command = new AbstractCommandModel() {

			@Override
			protected HandlerResult internalExecuteCommand(DisplayContext context) {
				for (Iterator<? extends FormMember> it = member.getMembers(); it.hasNext();) {
					FormContainer innerMember = (FormContainer) it.next();
					Collapsible collapsible = (Collapsible) innerMember.getMember(EditorUtils.LIST_ITEM_GROUP);
					collapsible.setCollapsed(collapse);
				}
				return HandlerResult.DEFAULT_RESULT;
			}
		};
		Resources resources = Resources.getInstance();
		if (collapse) {
			command.setImage(com.top_logic.layout.basic.Icons.COLLAPSE_ALL);
			command.setLabel(resources.getString(I18NConstants.COLLAPSE_ALL_ENTRIES_IN_LIST));
		} else {
			command.setImage(com.top_logic.layout.basic.Icons.EXPAND_ALL);
			command.setLabel(resources.getString(I18NConstants.EXPAND_ALL_ENTRIES_IN_LIST));
		}
		return new ButtonControl(command);
	}

	private HTMLTemplateFragment createEntry(boolean isCompact, boolean polymorphicOptions,
			PropertyDescriptor titleProperty) {
		if (isCompact) {
			return createCompactModeEntry();
		} else {
			return createFullModeEntry(polymorphicOptions, titleProperty);
		}
	}

	private HTMLTemplateFragment createFullModeEntry(boolean polymorphicOptions, PropertyDescriptor titleProperty) {
		if (titleProperty == null) {
			return div(
				css("dfListEntry"),
				EditorUtils.titleWithIconAndRemove(
					htmlTemplate(Icons.ITEM_ICON),
					label(EditorUtils.LIST_ITEM_GROUP),
					"colored"),
				div(css(ReactiveFormCSS.RF_COLUMNS_LAYOUT), member(EditorUtils.LIST_ITEM_GROUP, embedd())));
		} else {
			return div(
				css("dfListEntry"),
				EditorUtils.titleWithIconAndRemove(
						member(EditorUtils.LIST_ITEM_GROUP, MemberStyle.NONE, toggleButtonCP()),
						member(EditorUtils.LIST_ITEM_GROUP, MemberStyle.NONE,
							titleFragment(polymorphicOptions, titleProperty)),
						"colored"
					),
				member(EditorUtils.LIST_ITEM_GROUP, MemberStyle.NONE, displayCollapsibleWhenExpandedTemplate()));
		}

	}

	private static HTMLTemplateFragment titleFragment(boolean polymorphicOptions, PropertyDescriptor titleProperty) {
		String fieldName = fieldNameForProperty(titleProperty);
		return new HTMLTemplateFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
				FormContainer entryField = (FormContainer) TemplateRenderer.model(properties);
				FormMember representingField = representingField(entryField);
				if (representingField != null) {
					if (polymorphicOptions) {
						self(MemberStyle.LABEL_WITH_COLON).write(context, out, properties);
					}
					member(fieldName, MemberStyle.NONE, memberWithTooltipFragment()).write(context, out, properties);
					member(fieldName, MemberStyle.ERROR).write(context, out, properties);
				} else {
					self(MemberStyle.LABEL).write(context, out, properties);
				}
				writeErrorWhenCollapsed(context, out, entryField);
			}

			private void writeErrorWhenCollapsed(DisplayContext context, TagWriter out, FormContainer entryField)
					throws IOException {
				BlockControl content = EditorUtils.errorBlock(entryField);
				new OnExpandedControl(entryField, content)
					.setControlTag(HTMLConstants.SPAN)
					.setInverted(true)
					.write(context, out);
			}

			private FormMember representingField(FormContainer entryField) {
				FormMember representingField;
				if (entryField.hasMember(fieldName)) {
					representingField = entryField.getMember(fieldName);
				} else {
					representingField = null;
				}
				return representingField;
			}
		};
	}

	private static String fieldNameForProperty(PropertyDescriptor property) {
		return property.getPropertyName();
	}

	private static HTMLTemplateFragment memberWithTooltipFragment() {
		return new HTMLTemplateFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
				HTMLFragment memberFragment = TemplateRenderer.toFragmentInline(properties, self());
				FormMember member = (FormMember) TemplateRenderer.model(properties);
				new TooltipControl(member, memberFragment).write(context, out);
			}
		};
	}

	private HTMLTemplateFragment displayCollapsibleWhenExpandedTemplate() {
		return new HTMLTemplateFragment() {

			@Override
			public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
				HTMLFragment content = TemplateRenderer.toFragment(properties,
					div(css(ReactiveFormCSS.RF_COLUMNS_LAYOUT), self(embedd())));
				Collapsible collapsible = (Collapsible) TemplateRenderer.model(properties);
				new OnExpandedControl(collapsible, content).write(context, out);
			}
		};
	}

	private static ControlProvider toggleButtonCP() {
		return new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return GroupCellControl.createToggleImageButton((Collapsible) model);
			}
		};
	}

	private HTMLTemplateFragment createCompactModeEntry() {
		return div(
			css("dfListEntry"),
			span(css("dfListRemove"), member(EditorUtils.LIST_REMOVE)),
			label(),
			member(EditorUtils.LIST_ITEM_GROUP, embedd()));
	}

	static Value<Boolean> isFirstMember(FormMember member) {
		return eq(first(members(member.getParent())), literal(member));
	}

	private class ElementFactory implements CreateHandler {
		final EditorFactory _editorFactory;

		final FormContainer _parent;

		int id = 1;

		final ValueModel _model;

		private Value<Boolean> _readOnly;

		private PropertyDescriptor _titleProperty;

		private boolean _createTitleField;

		public ElementFactory(EditorFactory editorFactory, FormContainer parent, ValueModel model,
				Value<Boolean> readOnly, PropertyDescriptor titleProperty, boolean createTitleField) {
			_editorFactory = editorFactory;
			_parent = parent;
			_model = model;
			_readOnly = readOnly;
			_titleProperty = titleProperty;
			_createTitleField = createTitleField;
		}

		void initElements() {
			Object propertyValue = _model.getValue();
			Collection<?> currentValue;
			PropertyDescriptor property = _model.getProperty();
			if (propertyValue == null) {
				currentValue = Collections.emptyList();
			} else {
				if (property.kind() == PropertyKind.MAP) {
					currentValue = ((Map<?, ?>) propertyValue).values();
				} else {
					currentValue = (Collection<?>) propertyValue;
				}
			}

			ConfigurationAccess configAccess = property.getConfigurationAccess();
			boolean collapseEntries = Fields.collapseEntries(_editorFactory, property);
			for (Object elementModel : currentValue) {
				internalAddElement(elementModel, configAccess.getConfig(elementModel), collapseEntries);
			}
		}

		@Override
		public void addElement(Object element, ConfigurationItem elementModel) {
			// Do not minimize group for new entries.
			internalAddElement(element, elementModel, false);
		}

		private void internalAddElement(Object element, ConfigurationItem elementModel, boolean collapseGroup) {
			PropertyDescriptor property = _model.getProperty();
			final FormGroup elementGroup = group(_parent, EditorUtils.LIST_ELEMENT_PREFIX + (id++));

			// Name the element according to its index.
			elementGroup.setStableIdSpecialCaseMarker(_parent.size() - 1);

			EditorUtils.addRemoveButton(elementGroup, new Command() {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					_model.removeValue(element);

					// Rename remaining elements according to their new index.
					int elementId = 0;
					for (Iterator<? extends FormMember> it = _parent.getMembers(); it.hasNext();) {
						it.next().setStableIdSpecialCaseMarker(elementId++);
					}

					return HandlerResult.DEFAULT_RESULT;
				}
			}, _readOnly);

			ConfigurationListener listener = new RemoveDerivedModelListener(element, elementGroup);
			_model.getModel().addConfigurationListener(property, listener);

			FormGroup contentGroup = group(elementGroup, EditorUtils.LIST_ITEM_GROUP);

			if (_editorFactory.isCompact()) {
				Value<ResKey> labelValue = elementLabel(_model, isFirstMember(elementGroup), elementModel);
				bindLabel(elementGroup, labelValue);
			} else {
				// Lazy access to group label resource.
				elementGroup.setResources(new AbstractResourceView() {
					@Override
					protected String getResource(String resourceKey, boolean optional) {
						if (!EditorUtils.LIST_ITEM_GROUP.equals(resourceKey)) {
							return null;
						}

						return _editorFactory.getGroupLabels().getLabel(element);
					}
				});
			}

			contentGroup.setTooltip(createTooltipText(elementModel));

			PropertyDescriptor keyProperty = property.getKeyProperty();
			Set<PropertyDescriptor> hiddenAttributes;
			if (_editorFactory.isCompact()) {
				hiddenAttributes = Collections.emptySet();
			} else {
				hiddenAttributes = CollectionUtil.singletonOrEmptySet(_titleProperty);
			}
			Set<PropertyDescriptor> enforcingFields;
			if (_editorFactory.isCompact() || !_createTitleField) {
				enforcingFields = Collections.emptySet();
			} else {
				enforcingFields = CollectionUtil.singletonOrEmptySet(_titleProperty);
			}
			_editorFactory.initEditorGroup(contentGroup, elementModel, true, keyProperty, hiddenAttributes,
				enforcingFields);
			if (collapseGroup) {
				contentGroup.setCollapsed(true);
			}
			EditorFactory.setModel(contentGroup, element);

			if (property.isInstanceValued()) {
				// Prevent editing the configuration of an already instantiated implementation.
				contentGroup.setImmutable(true);
			}
		}

		private String createTooltipText(ConfigurationItem configuration) {
			return Resources.getInstance().getString(ResKey.forConfig(configuration).tooltip(), null);
		}
	}

	/**
	 * The label for the list element group, see {@link EditorUtils#LIST_ELEMENT_PREFIX}.
	 * 
	 * @param valueModel
	 *        The list property.
	 * @param isFirst
	 *        Whether the element is the first in the list.
	 * @param elementModel
	 *        The model of the list entry.
	 */
	protected Value<ResKey> elementLabel(ValueModel valueModel, Value<Boolean> isFirst,
			ConfigurationItem elementModel) {
		return staticElementLabel(valueModel, isFirst);
	}

	/**
	 * The label for the add-new-element button.
	 * @param valueModel
	 *        The list property.
	 * @param contentGroup
	 *        The {@link FormGroup} containing the technical list entries.
	 */
	protected Value<ResKey> addLabel(final ValueModel valueModel, final FormGroup contentGroup) {
		return staticElementLabel(valueModel, isEmpty(members(contentGroup)));
	}

	/**
	 * The tooltip for the add-new-element button.
	 * @param valueModel
	 *        The list property.
	 * @param contentGroup
	 *        The {@link FormGroup} containing the technical list entries.
	 */
	protected ResKey addTooltip(final ValueModel valueModel, final FormGroup contentGroup) {
		return I18NConstants.ADD_ELEMENT__PROPERTY.fill(Labels.propertyLabel(valueModel));
	}

	/**
	 * List-property-based label for an entry in a list.
	 * 
	 * @param valueModel
	 *        The list property.
	 * @param isFirst
	 *        Whether this is the first entry in the list.
	 * @return The label describing a list entry.
	 */
	protected Value<ResKey> staticElementLabel(ValueModel valueModel, Value<Boolean> isFirst) {
		PropertyDescriptor property = valueModel.getProperty();
		return ifElse(isFirst,
			literal(ResKey.fallback(Labels.propertyLabelKey(property, "@first"), ResKey.text(""))),
			literal(ResKey.fallback(Labels.propertyLabelKey(property, "@next"), ResKey.text(""))));
	}

	private static final class OpenSortDialog implements Command {
	
		final ValueModel _valueModel;

		final FormGroup _listGroup;
	
		public OpenSortDialog(ValueModel valueModel, FormGroup list) {
			_valueModel = valueModel;
			_listGroup = list;
		}
	
		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			final FormContainer contentGroup = contentGroup(_listGroup);

			FormContext sortContext = new FormContext("sortContext", ResPrefix.GLOBAL);
			ScriptingRecorder.annotateAsDontRecord(sortContext);

			SelectField currentElements = multiSelectField(sortContext, LIST_SORT);
			currentElements.setLabel(Labels.propertyLabel(_valueModel));
			selections(currentElements, CollectionUtil.toList(contentGroup.getMembers()));
			currentElements.setCustomOrder(true);

			final LabelProvider configLabels = new ConfigLabelProvider();
			
			currentElements.setOptionLabelProvider(new LabelProvider() {
				@Override
				public String getLabel(Object object) {
					FormMember itemGroup = ((FormGroup) object).getMember(EditorUtils.LIST_ITEM_GROUP);
					Object itemModel = EditorFactory.getModel(itemGroup);
					return configLabels.getLabel(itemModel);
				}
			});
			currentElements.addValueListener(new ValueListener() {
				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					@SuppressWarnings("unchecked")
					Collection<FormGroup> newMembers = (Collection<FormGroup>) newValue;

					updateListContent(contentGroup, _valueModel, newMembers);
					
					if (ScriptingRecorder.isRecordingActive()) {
						recordAction(configLabels, newMembers);
					}
				}

				private void recordAction(final LabelProvider itemLabels, Collection<FormGroup> newMembers) {
					Set<String> labels = new LinkedHashSet<>();
					for (FormGroup newMember : newMembers) {
						String label = itemLabels.getLabel(EditorFactory.getModel(newMember.getMember(EditorUtils.LIST_ITEM_GROUP)));
						boolean unique = labels.add(label);
						
						if (!unique) {
							RecordingFailedAction failure = TypedConfiguration.newConfigItem(RecordingFailedAction.class);
							failure.setImplementationClass(RecordingFailedActionOp.class);
							failure.setFailureMessage("Multiple items with name '" + label + "', no stable result can be guaranteed.");
							ScriptingRecorder.recordAction(failure);
							return;
						}
					}
					
					SortListFormAction sortAction = TypedConfiguration.newConfigItem(SortListFormAction.class);
					sortAction.setList(ModelResolver.buildModelName(_listGroup));
					sortAction.setItems(new ArrayList<>(labels));
					ScriptingRecorder.recordAction(sortAction);
				}

			});
	
			SelectDialogConfig config = TypedConfiguration.newConfigItem(SelectDialogConfig.class);
			config.setShowOptions(false);
			MultiSelectDialog dialog = new MultiSelectDialog(currentElements, config);
			dialog.open(context.getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	static void updateListOrder(ConfigurationItem scriptContext, final FormContainer listGroup, ValueModel valueModel, List<String> labels) {
		FormContainer contentGroup = contentGroup(listGroup);
		
		Collection<FormGroup> list = new ArrayList<>();
		
		final LabelProvider configLabels = new ConfigLabelProvider();
		Map<String, FormGroup> index = new HashMap<>();
		for (Iterator<? extends FormMember> it = contentGroup.getMembers(); it.hasNext(); ) {
			FormGroup elementGroup = (FormGroup) it.next();
			FormGroup item = (FormGroup) elementGroup.getMember(EditorUtils.LIST_ITEM_GROUP);
			String label = configLabels.getLabel(EditorFactory.getModel(item));
			FormGroup clash = index.put(label, elementGroup);
			if (clash != null) {
				ApplicationAssertions.fail(scriptContext,
					"Multiple items with name '" + label + "', no stable result can be guaranteed.");
			}
		}
		
		for (String label : labels) {
			FormGroup elementGroup = index.get(label);
			if (elementGroup == null) {
				ApplicationAssertions.fail(scriptContext,
					"List item with label '" + label + "' not found. Existing item labels: " + index.keySet());
			}
			list.add(elementGroup);
		}
		
		updateListContent(contentGroup, valueModel, list);
	}

	static FormContainer contentGroup(FormContainer listGroup) {
		return listGroup.getContainer(EditorUtils.LIST_CONTENT_GROUP);
	}

	static void updateListContent(final FormContainer contentGroup, ValueModel valueModel, Collection<FormGroup> newMembers) {
		List<Object> newListValue = getListModel(newMembers);

		// Apply changes.
		for (FormMember oldMember : CollectionUtil.toList(contentGroup.getMembers())) {
			contentGroup.removeMember(oldMember);
		}

		// Beware moving this call to a location after adding the members in their new
		// order: By setting the value to the model, UI elements are implicitly removed
		// from the UI.
		boolean disabledBefore = AddElementListener.setDisabled(true);
		try {
			valueModel.setValue(newListValue);
		} finally {
			AddElementListener.setDisabled(disabledBefore);
		}

		int id = 0;
		for (FormGroup newMember : newMembers) {
			contentGroup.addMember(newMember);
			
			newMember.setStableIdSpecialCaseMarker(id++);
		}
	}

	private static List<Object> getListModel(Collection<FormGroup> newMembers) {
		List<Object> newListValue = new ArrayList<>();
		for (FormGroup newMember : newMembers) {
			Object element = EditorFactory.getModel(newMember.getContainer(EditorUtils.LIST_ITEM_GROUP));
			newListValue.add(element);
		}
		return newListValue;
	}

	/**
	 * Button that adds a new element to a monomorphic list.
	 * 
	 * <p>
	 * There are no options for the add operation, only a "plus" button is available.
	 * </p>
	 */
	private static final class CreateElementCommand implements Command {

		private final EditorFactory _editorFactory;

		private final CreateHandler _createHandler;

		private final ValueModel _valueModel;

		private final Provider<ConfigurationItem> _constructor;

		public CreateElementCommand(EditorFactory editorFactory, CreateHandler createHandler,
				ValueModel valueModel, Provider<ConfigurationItem> constructor) {
			_editorFactory = editorFactory;
			_createHandler = createHandler;
			_valueModel = valueModel;
			_constructor = constructor;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			final ConfigurationItem elementModel = createElement();
			PropertyDescriptor property = getProperty();
			PropertyDescriptor keyProperty = property.getKeyProperty();
			if (property.isInstanceValued() || keyProperty != null) {
				InitializerUtil.initProperties(_editorFactory, elementModel);
				return new AddDialog(_editorFactory, _createHandler, property, elementModel).open(context);
			}
			return directlyAddElement(elementModel);
		}

		private PropertyDescriptor getProperty() {
			return _valueModel.getProperty();
		}

		private HandlerResult directlyAddElement(final ConfigurationItem elementModel) {
			_createHandler.addElement(elementModel, elementModel);
			return HandlerResult.DEFAULT_RESULT;
		}

		private ConfigurationItem createElement() {
			return _constructor.get();
		}

	}

	private static final class CreateElementFromTemplate implements Listener {

		private final ModifiableValue<?> _templateValue;

		private final EditorFactory _editorFactory;

		private final CreateHandler _createHandler;

		private final ValueModel _valueModel;

		private final OptionMapping _optionMapping;

		public CreateElementFromTemplate(EditorFactory editorFactory, CreateHandler createHandler,
				ValueModel valueModel, OptionMapping optionMapping, ModifiableValue<?> templateValue) {
			_editorFactory = editorFactory;
			_createHandler = createHandler;
			_valueModel = valueModel;
			_optionMapping = optionMapping;
			_templateValue = templateValue;

		}

		@Override
		public void handleChange(Value<?> sender) {
			Object value = _templateValue.get();
			if (value == null) {
				return;
			}
			value = _optionMapping.toSelection(value);
			ConfigurationItem elementModel = TypedConfiguration.copy((ConfigurationItem) value);

			PropertyDescriptor property = _valueModel.getProperty();
			PropertyDescriptor keyProperty = property.getKeyProperty();
			NamedConstant CONFIG_TYPE_ID =
				TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class)
					.getProperty(ConfigurationItem.CONFIGURATION_INTERFACE_NAME).identifier();

			// Make sure to reset selector, even if adding fails.
			_templateValue.set(null);

			if (property.isInstanceValued() || (keyProperty != null && keyProperty.identifier() != CONFIG_TYPE_ID
				&& !elementModel.valueSet(keyProperty))) {
				DisplayContext context = DefaultDisplayContext.getDisplayContext();
				InitializerUtil.initProperties(_editorFactory, elementModel);
				new AddDialog(_editorFactory, _createHandler, property, elementModel).open(context);
			} else {
				_createHandler.addElement(elementModel, elementModel);
			}
		}

	}

}

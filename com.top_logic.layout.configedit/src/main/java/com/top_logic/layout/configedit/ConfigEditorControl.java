/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.ReadOnly;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.customization.NoCustomizations;
import com.top_logic.basic.config.order.DefaultOrderStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.form.values.DerivedProperty;
import com.top_logic.layout.form.values.ListenerBinding;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl.GroupBorder;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.util.Resources;

/**
 * A {@link ReactControl} that renders a form for all PLAIN, REF, ITEM, LIST, ARRAY, and MAP
 * properties of a {@link ConfigurationItem}, plus a COMPLEX property whose value
 * {@link ConfigControlService#hasTextForm(ConfigurationItem, PropertyDescriptor) has a text form}
 * (e.g. a {@link com.top_logic.basic.util.ResKey} property).
 *
 * <p>
 * Each PLAIN/REF property, and a COMPLEX property whose value has a text form, is wrapped in a
 * {@link ReactFormFieldChromeControl} with label, mandatory indicator, and help text. ITEM
 * properties are rendered as collapsible {@link ReactFormGroupControl} sections containing a
 * nested {@link ConfigEditorControl} - unless the configuration writes the item as text, a
 * TL-Script expression for instance, in which case that text is edited as a field. LIST, ARRAY,
 * and MAP properties are rendered as collapsible sections containing nested editors for each
 * element - the same editor for all three, MAP differing only in the value's shape and in being
 * unordered. DERIVED and a COMPLEX property without a text form are skipped.
 * </p>
 *
 * <p>
 * The properties are displayed in the order of a {@link DisplayOrder} annotation of the item's
 * interface, the same as in other configuration forms. Without one, the properties of a super
 * interface come before the ones the interface declares itself.
 * </p>
 *
 * <p>
 * A property with a {@link DynamicMode dynamic mode} is hidden or stops accepting input while its
 * mode, computed from other properties of the item, says so.
 * </p>
 */
public class ConfigEditorControl extends ReactFormLayoutControl {

	private final ConfigFieldIndex _index;

	/**
	 * Whether every field and collection action this editor builds accepts input.
	 *
	 * <p>
	 * {@code false} while a {@link ConfigFormControl}'s own edit mode is off:
	 * {@link ConfigFieldModel#setEditable(boolean)} is applied to every PLAIN/REF/COMPLEX field as
	 * it is built, and this value is passed straight into {@link ConfigListEditorControl} - which,
	 * for a LIST/ARRAY/MAP property, renders no add/remove/reorder button at all rather than a
	 * disabled one - and into {@link PolymorphicItemControl} (via
	 * {@link #createPolymorphicGroup(ReactContext, String, ConfigurationItem, PropertyDescriptor, boolean)})
	 * for a polymorphic ITEM property, whose own type selector is disabled the same way a plain
	 * field is rather than left out - the currently chosen type must stay legible even while it may
	 * not be changed. Propagated unchanged into every nested {@link ConfigEditorControl} (via
	 * {@link #createNestedEditor(ReactContext, ConfigurationItem)}) and into every nested editor a
	 * {@link ConfigListEditorControl} builds over its own entries, so a form built with
	 * {@code editable = false} stays non-editable at every nesting depth. Every constructor that
	 * predates this field defaults it to {@code true}, keeping the view designer's write-through
	 * behaviour unchanged.
	 * </p>
	 */
	private final boolean _editable;

	private final ConfigurationItem _formModel;

	/**
	 * How this editor displays properties of the edited item, beyond what the properties declare
	 * themselves - unlike a {@link ReadOnly} or {@link Hidden} property, which is displayed so
	 * wherever it is edited.
	 */
	private final Map<PropertyDescriptor, FieldDisplay> _displays;

	/**
	 * Creates a {@link ConfigEditorControl} for all visible properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config) {
		this(context, config, Collections.emptySet(), false);
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties) {
		this(context, config, hiddenProperties, false);
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties and optionally skipping
	 * tree properties.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 * @param skipTreeProperties
	 *        If {@code true}, properties annotated with {@link TreeProperty} are skipped. Use
	 *        {@code true} for top-level tree node configurations, {@code false} for nested/inline
	 *        sub-configurations.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties) {
		this(context, config, hiddenProperties, skipTreeProperties, null);
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties, optionally skipping tree
	 * properties, and reporting every field it builds to the given {@link ConfigFieldIndex}.
	 *
	 * <p>
	 * Editable - see the six-argument constructor for a form that is not.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 * @param skipTreeProperties
	 *        If {@code true}, properties annotated with {@link TreeProperty} are skipped. Use
	 *        {@code true} for top-level tree node configurations, {@code false} for nested/inline
	 *        sub-configurations.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index) {
		this(context, config, hiddenProperties, skipTreeProperties, index, true);
	}

	/**
	 * Creates a {@link ConfigEditorControl}, hiding the given properties, optionally skipping tree
	 * properties, reporting every field it builds to the given {@link ConfigFieldIndex}, and
	 * deciding whether any of it may be changed.
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 * @param skipTreeProperties
	 *        If {@code true}, properties annotated with {@link TreeProperty} are skipped. Use
	 *        {@code true} for top-level tree node configurations, {@code false} for nested/inline
	 *        sub-configurations.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 * @param editable
	 *        Whether the built fields and collection actions accept input - see {@link #_editable}.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index,
			boolean editable) {
		this(context, config, hiddenProperties, skipTreeProperties, index, editable, config);
	}

	/**
	 * Creates a {@link ConfigEditorControl} that knows what is being edited as a whole.
	 *
	 * @param formModel
	 *        The root of the configuration under edit, handed down to every field and nested editor.
	 *        An option function or mapping of a property may need it, and it cannot be found by
	 *        walking up from the property's own item - see
	 *        {@link ConfigPropertyOptions#optionProvider(ConfigurationItem, PropertyDescriptor)}. The
	 *        outermost editor is the one that knows it; the constructors above take the item they
	 *        edit, which is right for exactly that case.
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index,
			boolean editable, ConfigurationItem formModel) {
		this(context, config, hiddenProperties, Collections.emptyMap(), skipTreeProperties, index, editable,
			formModel);
	}

	/**
	 * Creates a {@link ConfigEditorControl} displaying properties as the user interface wants them.
	 *
	 * @param displays
	 *        How to display properties of the given item, see {@link #_displays}.
	 *
	 * @see #ConfigEditorControl(ReactContext, ConfigurationItem, Set, boolean, ConfigFieldIndex,
	 *      boolean, ConfigurationItem)
	 */
	public ConfigEditorControl(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, Map<PropertyDescriptor, FieldDisplay> displays,
			boolean skipTreeProperties, ConfigFieldIndex index, boolean editable, ConfigurationItem formModel) {
		super(context);
		_index = index;
		_editable = editable;
		_formModel = formModel;
		_displays = displays;

		for (PropertyDescriptor property : displayProperties(config)) {
			if (hiddenProperties.contains(property)) {
				continue;
			}
			FieldDisplay display = _displays.get(property);
			if (display != null && !display.isVisible()) {
				continue;
			}
			// A group in a mode that does not accept input offers nothing to add or remove.
			boolean groupEditable = _editable && (display == null || display.isAccepting());
			if (!isSupportedKind(config, property)) {
				continue;
			}
			if (isHidden(property)) {
				continue;
			}
			if (skipTreeProperties && isTreeProperty(property)) {
				continue;
			}

			// An item the configuration writes as text - a TL-Script expression, for instance - is
			// edited as that text below, instead of as a form over its syntax tree. An item whose
			// format cannot express the value it currently holds has no such text and gets the
			// structured editor, the same as an item without a format at all.
			if (property.kind() == PropertyKind.ITEM
				&& !ConfigControlService.hasTextForm(config, property)) {
				if (PolymorphicConfiguration.class.isAssignableFrom(property.getType())) {
					String label = resolveLabel(property);
					PolymorphicItemControl polyGroup =
						createPolymorphicGroup(context, label, config, property, groupEditable);
					polyGroup.setHeader(createGroupHeader(context, property));
					addChild(polyGroup);
					followMode(config, property, polyGroup, null);
					disableIfDisabled(config, property, display);
				} else {
					ConfigurationAccess configAccess = property.getConfigurationAccess();
					ConfigurationItem nested = configAccess.getConfig(config.value(property));
					// A read-only form has nothing to show for an item without value; an editable
					// one offers to create it, since the item could not be entered otherwise. The
					// item is edited as a list of at most one entry, so that creating and removing
					// it looks the same as for an entry of a list.
					if (nested != null || groupEditable) {
						ConfigListEditorControl itemEditor = new ConfigListEditorControl(context,
							new ConfigItemValue(config, property), PolymorphicOptions.Choices.NONE, _index,
							groupEditable, _formModel);
						// No heading of its own: the entry of the item is headed by the property
						// already, see ConfigItemValue#entryTitle(ConfigurationItem). The group only
						// gives the editor the whole row.
						ReactFormGroupControl group = new ReactFormGroupControl(
							context, null, false, false, GroupBorder.NONE, true,
							List.of(), List.of(itemEditor));
						addChild(group);
						followMode(config, property, group, null);
						disableIfDisabled(config, property, display);
					}
				}
				continue;
			}

			if (property.kind() == PropertyKind.LIST || property.kind() == PropertyKind.ARRAY
				|| property.kind() == PropertyKind.MAP) {
				ConfigListEditorControl listEditor =
					new ConfigListEditorControl(context, config, property, _index, groupEditable, _formModel);
				// Over the full row, like the nested-item group above: a collection holds whole
				// forms - one per entry, each with its own header and actions - and a third of the
				// row is not a place to put a form. It also keeps a collection recognizable as one
				// section rather than as a column of the surrounding grid.
				// The button adding an entry stands next to the name of the collection, so that it is
				// clear which collection it adds to - see ConfigListEditorControl#headerAddButton().
				ReactButtonControl addButton = listEditor.headerAddButton();
				ReactFormGroupControl listGroup = new ReactFormGroupControl(
					context, null, true, false, GroupBorder.SUBTLE, true,
					addButton == null ? List.of() : List.of(addButton), List.of(listEditor));
				listGroup.setHeader(createGroupHeader(context, property));
				addChild(listGroup);
				followMode(config, property, listGroup, null);
				disableIfDisabled(config, property, display);
				continue;
			}

			ConfigFieldModel model =
				ConfigControlService.getInstance().createModel(config, property, _formModel);
			// A read-only value is displayed, but cannot be changed.
			model.setEditable(_editable && !readOnly(property));
			if (display != null) {
				model.setDisabled(display.mode() == FieldMode.DISABLED);
				if (display.mandatory()) {
					model.setMandatory(true);
				}
			}
			index(config, property, model);
			addCleanupAction(model::detach);

			ReactControl input = ConfigControlService.getInstance().createControl(context, model);

			String label = resolveLabel(property);
			String tooltip = resolveTooltip(property);

			LabelPosition labelPosition = (property.getType() == boolean.class || property.getType() == Boolean.class)
				? LabelPosition.AFTER : null;

			ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(
				context, label, model.isMandatory(), false, null, null, labelPosition,
				rendersWholeLine(property), true, input);
			if (tooltip != null && !tooltip.isEmpty()) {
				chrome.setTooltip(tooltip, label, true);
			}
			if (!(input instanceof ReactFormFieldControl)) {
				// A form field displays the error of its model itself; any other control - an editor
				// of its own, such as the TL-Script editor - leaves that to the chrome around it.
				showErrorInChrome(model, chrome);
			}
			addChild(chrome);
			followMode(config, property, chrome, model);
		}
	}

	/**
	 * Displays the error of the given field in the given chrome, for a field whose control does not
	 * display it itself.
	 */
	private void showErrorInChrome(ConfigFieldModel model, ReactFormFieldChromeControl chrome) {
		Runnable update = () -> chrome.setError(
			model.hasError() ? Resources.getInstance().getString(model.getError()) : null);
		update.run();
		FieldModelListener listener = new FieldModelListener() {
			@Override
			public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
				// The error is reported separately.
			}

			@Override
			public void onEditabilityChanged(FieldModel source, boolean editable) {
				// An error is only shown while the field is editable, see the model.
				update.run();
			}

			@Override
			public void onValidationChanged(FieldModel source) {
				update.run();
			}
		};
		model.addListener(listener);
		addCleanupAction(() -> model.removeListener(listener));
	}

	/**
	 * Resolves the label for the given property.
	 *
	 * @param property
	 *        The property descriptor.
	 * @return The label text.
	 */
	protected String resolveLabel(PropertyDescriptor property) {
		return Labels.propertyLabel(property, false);
	}

	/**
	 * Resolves the property's tooltip HTML, derived from the getter's {@code JavaDoc}. Returned
	 * verbatim (HTML), or {@code null} if no tooltip is defined.
	 *
	 * @param property
	 *        The property descriptor.
	 */
	protected String resolveTooltip(PropertyDescriptor property) {
		return Resources.getInstance().getString(property.labelKey(null).tooltip(), null);
	}

	/**
	 * Creates a header {@link ReactTextControl} for a property group (ITEM/LIST/ARRAY), carrying
	 * the property's label and, if available, its {@code JavaDoc} tooltip.
	 */
	protected ReactTextControl createGroupHeader(ReactContext context, PropertyDescriptor property) {
		String label = resolveLabel(property);
		ReactTextControl header = new ReactTextControl(context, label);
		String tooltip = resolveTooltip(property);
		if (tooltip != null && !tooltip.isEmpty()) {
			header.setTooltip(tooltip, label, true);
		}
		return header;
	}

	/**
	 * Creates a nested {@link ConfigEditorControl} for an ITEM property value.
	 *
	 * <p>
	 * Subclasses may override this to customize the nested editor (e.g. for testing).
	 * </p>
	 *
	 * <p>
	 * Propagates {@link #_editable} unchanged, so a form built read-only stays read-only at every
	 * nesting depth.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param nested
	 *        The nested configuration item to edit.
	 * @return A new editor control for the nested item.
	 */
	protected ConfigEditorControl createNestedEditor(ReactContext context, ConfigurationItem nested) {
		return newEditor(context, nested, Collections.emptySet(), false, _index, _editable, _formModel);
	}

	/**
	 * The seam a test double replaces to build a differently configured nested editor (e.g. one
	 * that bypasses {@link Labels}/{@link Resources} for testing) - constructs an editor, deciding
	 * nothing.
	 *
	 * <p>
	 * Kept separate from {@link #createNestedEditor(ReactContext, ConfigurationItem)} so that
	 * method's decision - which properties to hide, whether to skip tree properties, which
	 * {@link ConfigFieldIndex} to hand down, and whether the nested editor accepts input - is real
	 * production logic a test exercises too, rather than something a test double silently replaces
	 * along with the construction itself.
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param config
	 *        The configuration item to edit.
	 * @param hiddenProperties
	 *        Properties to exclude from the form.
	 * @param skipTreeProperties
	 *        If {@code true}, properties annotated with {@link TreeProperty} are skipped.
	 * @param index
	 *        The {@link ConfigFieldIndex} to report every built field to, or {@code null} if
	 *        nobody is collecting.
	 * @param editable
	 *        Whether the built editor's fields and collection actions accept input.
	 * @return A new editor control.
	 */
	protected ConfigEditorControl newEditor(ReactContext context, ConfigurationItem config,
			Set<PropertyDescriptor> hiddenProperties, boolean skipTreeProperties, ConfigFieldIndex index,
			boolean editable, ConfigurationItem formModel) {
		return new ConfigEditorControl(context, config, hiddenProperties, skipTreeProperties, index, editable,
			formModel);
	}

	/**
	 * Creates the {@link PolymorphicItemControl} for a polymorphic ITEM property.
	 *
	 * <p>
	 * Subclasses may override this to customize the polymorphic editor (e.g. for testing).
	 * </p>
	 *
	 * @param context
	 *        The React context.
	 * @param label
	 *        The group label.
	 * @param parentConfig
	 *        The parent configuration item.
	 * @param property
	 *        The polymorphic ITEM property.
	 * @param editable
	 *        Whether the built control's type selector accepts a change.
	 * @return A new polymorphic item control.
	 */
	protected PolymorphicItemControl createPolymorphicGroup(ReactContext context, String label,
			ConfigurationItem parentConfig, PropertyDescriptor property, boolean editable) {
		return new PolymorphicItemControl(context, label, parentConfig, property, this::createNestedEditor, editable);
	}

	/**
	 * Whether the given property is rendered as a field in this form.
	 *
	 * <p>
	 * {@link PropertyKind#PLAIN}, {@link PropertyKind#REF}, {@link PropertyKind#ITEM},
	 * {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, and {@link PropertyKind#MAP} are
	 * always supported - LIST, ARRAY, and MAP are the same sequence-of-elements editor, differing
	 * only in the value's shape and, for MAP, in being unordered. An ITEM is a nested form, except
	 * when the configuration writes it as text: such an item's value
	 * {@link ConfigControlService#hasTextForm(ConfigurationItem, PropertyDescriptor) has a text
	 * form} and is edited as that text, by the same service the other fields go through. A
	 * {@link PropertyKind#COMPLEX} property - e.g. a {@link com.top_logic.basic.util.ResKey}
	 * property, whose type carries both a {@code @Format} and a {@code ConfigurationValueBinding} -
	 * is supported only when its value has such a text form: exactly the subset
	 * {@link ConfigControlService#createModel(ConfigurationItem, PropertyDescriptor)} and
	 * {@link ConfigControlService#createControl(ReactContext, ConfigFieldModel)} accept.
	 * Admitting more here would hand them a property they reject with an
	 * {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param config
	 *        The configuration item holding the property - a format answers for the value the
	 *        property currently holds, so the item is part of the question.
	 */
	private static boolean isSupportedKind(ConfigurationItem config, PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF || kind == PropertyKind.ITEM
			|| kind == PropertyKind.LIST || kind == PropertyKind.ARRAY || kind == PropertyKind.MAP
			|| (kind == PropertyKind.COMPLEX && ConfigControlService.hasTextForm(config, property));
	}

	/**
	 * Lets the given display of a property follow the property's {@link DynamicMode dynamic mode},
	 * if it has one.
	 *
	 * <p>
	 * The mode is computed from other properties of the configuration, so it changes while the user
	 * edits those: an {@link FieldMode#INVISIBLE invisible} or {@link FieldMode#BLOCKED blocked}
	 * property is hidden, and the field of a property in a mode that does not accept input -
	 * {@link FieldMode#DISABLED disabled} or immutable - stops accepting it. A group of an ITEM or a
	 * collection property is only hidden: its nested editors decide their editability when they are
	 * built.
	 * </p>
	 *
	 * <p>
	 * The subscription lasts as long as this editor, like the field model it may switch.
	 * </p>
	 *
	 * @param config
	 *        The configuration item holding the property.
	 * @param display
	 *        The control displaying the property - its field chrome or its group.
	 * @param model
	 *        The field model of the property, or {@code null} for a group.
	 */
	private void followMode(ConfigurationItem config, PropertyDescriptor property, ReactControl display,
			ConfigFieldModel model) {
		DerivedProperty<FieldMode> provider = ConfigPropertyOptions.modeProvider(_formModel, property);
		if (provider == null) {
			return;
		}
		Value<FieldMode> mode = provider.getValue(config);
		applyMode(mode.get(), property, display, model);
		ListenerBinding binding = mode.addListener(sender -> applyMode(mode.get(), property, display, model));
		addCleanupAction(binding::close);
	}

	private void applyMode(FieldMode mode, PropertyDescriptor property, ReactControl display, ConfigFieldModel model) {
		boolean hidden = mode == FieldMode.INVISIBLE || mode == FieldMode.BLOCKED;
		if (display instanceof ReactFormFieldChromeControl chrome) {
			// A field hides itself through its own visibility, which is what the client reads.
			chrome.setVisible(!hidden);
		} else {
			display.setHidden(hidden);
		}
		if (model != null) {
			boolean accepting = mode == null || mode == FieldMode.ACTIVE;
			model.setEditable(_editable && !readOnly(property) && accepting);
		}
	}

	/**
	 * Reports a field to the {@link ConfigFieldIndex} this editor was given, if it was given one.
	 *
	 * <p>
	 * Nobody collects fields unless something is validating - the view designer and every nested
	 * editor built without one pass {@code null}. The check lives here, once, rather than at every
	 * place a field is built.
	 * </p>
	 *
	 * <p>
	 * The registration lasts exactly as long as the field does: it is taken back when this editor
	 * is disposed, on the same {@link #addCleanupAction(Runnable) cleanup} the field model's own
	 * {@link ConfigFieldModel#detach() detach} rides on. An editor is discarded and rebuilt for
	 * reasons the {@link ConfigFieldIndex}'s owner never hears about - a
	 * {@link ConfigListEditorControl} rebuilds on every add, remove and move - so a registration
	 * that outlived its field would leave the index answering for something no longer on screen.
	 * </p>
	 */
	private void index(ConfigurationItem item, PropertyDescriptor property, ConfigFieldModel model) {
		if (_index != null) {
			_index.register(item, property, model);
			addCleanupAction(() -> _index.unregister(item, property));
		}
	}

	/**
	 * Whether the field of the given property takes the whole row of the form instead of a column.
	 *
	 * <p>
	 * As in a form the legacy editor builds: the property, or else the type of its value, is
	 * annotated with {@link RenderWholeLine} - a TL-Script expression, for instance, whose type
	 * declares that its text needs the width.
	 * </p>
	 */
	private static boolean rendersWholeLine(PropertyDescriptor property) {
		RenderWholeLine annotation = annotation(property, RenderWholeLine.class);
		if (annotation == null) {
			ConfigurationDescriptor valueDescriptor = property.getValueDescriptor();
			if (valueDescriptor != null) {
				annotation = valueDescriptor.getConfigurationInterface().getAnnotation(RenderWholeLine.class);
			}
		}
		return annotation != null && annotation.value();
	}

	private static boolean isHidden(PropertyDescriptor property) {
		Hidden annotation = annotation(property, Hidden.class);
		return annotation != null && annotation.value();
	}

	/**
	 * Whether the field of the given property displays its value without accepting a change: the
	 * property is {@link ReadOnly}, or {@link FieldMode#IMMUTABLE immutable} in this editor.
	 */
	private boolean readOnly(PropertyDescriptor property) {
		FieldDisplay display = _displays.get(property);
		return isReadOnly(property) || (display != null && display.mode() == FieldMode.IMMUTABLE);
	}

	/**
	 * Displays the fields inside the group of the given property as
	 * {@link FieldMode#DISABLED disabled}, if that is the property's display.
	 *
	 * <p>
	 * The group is built without accepting input already, so that it offers nothing to add or
	 * remove; its fields would show their values only. Disabling them shows them as inputs that
	 * cannot be used instead. They are found through the {@link ConfigFieldIndex} by the items
	 * the property holds: a group not accepting input is never rebuilt, so the fields found are
	 * the ones displayed. Without an index, the fields keep showing their values only.
	 * </p>
	 */
	private void disableIfDisabled(ConfigurationItem config, PropertyDescriptor property, FieldDisplay display) {
		if (display == null || display.mode() != FieldMode.DISABLED || _index == null) {
			return;
		}
		Set<ConfigurationItem> items = Collections.newSetFromMap(new IdentityHashMap<>());
		collectItems(config.value(property), items);
		for (ConfigurationItem item : items) {
			for (ConfigFieldModel field : _index.fieldsOf(item)) {
				field.setDisabled(true);
			}
		}
	}

	/**
	 * Puts the given field before the fields of the properties, so that it is laid out with them.
	 *
	 * <p>
	 * For a field this editor does not create itself: the type selector of an entry, which chooses
	 * the configuration interface of the item rather than a value of one of its properties.
	 * </p>
	 */
	public void addLeadingField(ReactControl field) {
		List<ReactControl> children = new ArrayList<>(getChildren());
		children.add(0, field);
		replaceChildren(children);
	}

	/**
	 * The properties of the given item in the order they are displayed.
	 *
	 * @see DisplayOrder
	 */
	private static List<PropertyDescriptor> displayProperties(ConfigurationItem config) {
		return new DefaultOrderStrategy.Collector(NoCustomizations.INSTANCE, config.descriptor()).collect();
	}

	/**
	 * Adds the items the given property value holds, and all items nested in them, to the given
	 * set.
	 */
	private static void collectItems(Object value, Set<ConfigurationItem> items) {
		if (value instanceof ConfigurationItem item) {
			if (!items.add(item)) {
				return;
			}
			for (PropertyDescriptor nested : item.descriptor().getProperties()) {
				switch (nested.kind()) {
					case ITEM:
					case LIST:
					case ARRAY:
					case MAP:
						collectItems(item.value(nested), items);
						break;
					default:
						break;
				}
			}
		} else if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				collectItems(entry, items);
			}
		} else if (value instanceof Map<?, ?> map) {
			collectItems(map.values(), items);
		} else if (value instanceof Object[] array) {
			collectItems(Arrays.asList(array), items);
		}
	}

	/**
	 * Whether the given property's value cannot be changed.
	 */
	private static boolean isReadOnly(PropertyDescriptor property) {
		return annotation(property, ReadOnly.class) != null;
	}

	/**
	 * The given annotation of the property, or of the property it overrides.
	 *
	 * <p>
	 * A configuration overriding a property of its base configuration - narrowing the implementation
	 * class of a {@link com.top_logic.basic.config.PolymorphicConfiguration PolymorphicConfiguration},
	 * for instance - repeats neither the documentation nor the annotations of the declaration it
	 * overrides, so an annotation is looked up along that chain rather than on the override alone.
	 * </p>
	 */
	private static <T extends java.lang.annotation.Annotation> T annotation(PropertyDescriptor property,
			Class<T> annotationType) {
		T found = property.getAnnotation(annotationType);
		if (found != null) {
			return found;
		}
		for (PropertyDescriptor superProperty : property.getSuperProperties()) {
			T inherited = annotation(superProperty, annotationType);
			if (inherited != null) {
				return inherited;
			}
		}
		return null;
	}

	private static boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

}

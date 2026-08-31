/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ConfigurationAccess;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.util.Resources;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.LabelPosition;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;

/**
 * A {@link ReactControl} that renders a form for all PLAIN, REF, ITEM, LIST, ARRAY, and MAP
 * properties of a {@link ConfigurationItem}, plus a COMPLEX property that also has a value
 * provider (e.g. a {@link com.top_logic.basic.util.ResKey} property).
 *
 * <p>
 * Each PLAIN/REF property, and a COMPLEX property with a value provider, is wrapped in a
 * {@link ReactFormFieldChromeControl} with label, mandatory indicator, and help text. ITEM
 * properties are rendered as collapsible {@link ReactFormGroupControl} sections containing a
 * nested {@link ConfigEditorControl}. LIST, ARRAY, and MAP properties are rendered as collapsible
 * sections containing nested editors for each element - the same editor for all three, MAP
 * differing only in the value's shape and in being unordered. DERIVED and a binding-only COMPLEX
 * property are skipped.
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
		super(context);
		_index = index;
		_editable = editable;

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			if (hiddenProperties.contains(property)) {
				continue;
			}
			if (!isSupportedKind(property)) {
				continue;
			}
			if (isHidden(property)) {
				continue;
			}
			if (skipTreeProperties && isTreeProperty(property)) {
				continue;
			}

			if (property.kind() == PropertyKind.ITEM) {
				if (PolymorphicConfiguration.class.isAssignableFrom(property.getType())) {
					String label = resolveLabel(property);
					PolymorphicItemControl polyGroup =
						createPolymorphicGroup(context, label, config, property, _editable);
					polyGroup.setHeader(createGroupHeader(context, property));
					addChild(polyGroup);
				} else {
					ConfigurationAccess configAccess = property.getConfigurationAccess();
					ConfigurationItem nested = configAccess.getConfig(config.value(property));
					if (nested != null) {
						ConfigEditorControl nestedEditor = createNestedEditor(context, nested);
						ReactFormGroupControl group = new ReactFormGroupControl(
							context, null, true, false, "subtle", true,
							List.of(), List.of(nestedEditor));
						group.setHeader(createGroupHeader(context, property));
						addChild(group);
					}
				}
				continue;
			}

			if (property.kind() == PropertyKind.LIST || property.kind() == PropertyKind.ARRAY
				|| property.kind() == PropertyKind.MAP) {
				ConfigListEditorControl listEditor =
					new ConfigListEditorControl(context, config, property, _index, _editable);
				// Over the full row, like the nested-item group above: a collection holds whole
				// forms - one per entry, each with its own header and actions - and a third of the
				// row is not a place to put a form. It also keeps a collection recognizable as one
				// section rather than as a column of the surrounding grid.
				ReactFormGroupControl listGroup = new ReactFormGroupControl(
					context, null, true, false, "default", true,
					List.of(), List.of(listEditor));
				listGroup.setHeader(createGroupHeader(context, property));
				addChild(listGroup);
				continue;
			}

			ConfigFieldModel model = ConfigControlService.getInstance().createModel(config, property);
			model.setEditable(_editable);
			index(config, property, model);
			addCleanupAction(model::detach);

			ReactControl input = ConfigControlService.getInstance().createControl(context, model);

			String label = resolveLabel(property);
			String tooltip = resolveTooltip(property);
			LabelPosition labelPosition = (property.getType() == boolean.class || property.getType() == Boolean.class)
				? LabelPosition.AFTER : null;

			ReactFormFieldChromeControl chrome = new ReactFormFieldChromeControl(
				context, label, model.isMandatory(), false, null, null, labelPosition,
				false, true, input);
			if (tooltip != null && !tooltip.isEmpty()) {
				chrome.setTooltip(tooltip, label, true);
			}
			addChild(chrome);
		}
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
		return newEditor(context, nested, Collections.emptySet(), false, _index, _editable);
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
			boolean editable) {
		return new ConfigEditorControl(context, config, hiddenProperties, skipTreeProperties, index, editable);
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
	 * only in the value's shape and, for MAP, in being unordered. A {@link PropertyKind#COMPLEX}
	 * property - e.g. a {@link com.top_logic.basic.util.ResKey} property, whose type carries both
	 * a {@code @Format} and a {@code ConfigurationValueBinding} - is supported only when it also
	 * has a {@link PropertyDescriptor#getValueProvider() value provider}: exactly the subset
	 * {@link ConfigControlService#createModel(ConfigurationItem, PropertyDescriptor)} and
	 * {@link ConfigControlService#createControl(ReactContext, ConfigFieldModel)} accept.
	 * Admitting more here would hand them a property they reject with an
	 * {@link IllegalArgumentException}.
	 * </p>
	 */
	private static boolean isSupportedKind(PropertyDescriptor property) {
		PropertyKind kind = property.kind();
		return kind == PropertyKind.PLAIN || kind == PropertyKind.REF || kind == PropertyKind.ITEM
			|| kind == PropertyKind.LIST || kind == PropertyKind.ARRAY || kind == PropertyKind.MAP
			|| (kind == PropertyKind.COMPLEX && property.getValueProvider() != null);
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

	private static boolean isHidden(PropertyDescriptor property) {
		Hidden annotation = property.getAnnotation(Hidden.class);
		return annotation != null && annotation.value();
	}

	private static boolean isTreeProperty(PropertyDescriptor property) {
		TreeProperty annotation = property.getAnnotation(TreeProperty.class);
		return annotation != null && annotation.value();
	}

}

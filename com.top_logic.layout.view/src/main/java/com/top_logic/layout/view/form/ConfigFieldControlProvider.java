/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.configedit.ConfigFieldIndex;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigListEditorControl;
import com.top_logic.layout.configedit.FieldCollectionValue;
import com.top_logic.layout.configedit.PolymorphicOptions;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.form.model.FieldModelListener;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link ReactFieldControlProvider} rendering a configuration-valued attribute with the
 * configuration editor: the item editor for a single-valued attribute, the list editor for a
 * multi-valued one.
 *
 * <p>
 * Registered in {@link FieldControlService} for a type whose values are {@link ConfigurationItem}s.
 * An ordinary {@code <form>} thereby carries such an attribute without knowing that a configuration
 * editor exists.
 * </p>
 *
 * <p>
 * <b>What is edited is a copy.</b> An attribute's value in edit mode is the object the base model
 * still holds - {@link AttributeFieldModel} buffers a change in the overlay, but reading gives the
 * base object's own value until something is written. Editing that in place would change the model
 * before the form is saved, and would survive Cancel. So a copy is taken, and it is what the editor
 * works on.
 * </p>
 *
 * <p>
 * <b>The copy reaches the field on the first change, not before.</b> A form saves only what is
 * dirty, and dirty is decided by comparing the field's value with the one it started with - so
 * handing the copy over eagerly could make an untouched form look edited. The change signal is the
 * editor's own fields, via {@link ConfigFieldIndex#observeFields}: every change the user can make
 * goes through one of them, and a structural change rebuilds the editor and reports its fields
 * afresh. That is the same answer the legacy declarative editor gives, where the form fields are
 * likewise what says something changed - not the configuration, which is only written from them.
 * </p>
 */
public class ConfigFieldControlProvider implements ReactFieldControlProvider {

	@Override
	public ReactControl createControl(ReactContext context, TLStructuredTypePart part, FieldModel model) {
		Class<? extends ConfigurationItem> type = configType(part);
		if (type == null) {
			throw new IllegalArgumentException("Attribute '" + part
				+ "' is not configuration-valued, so it cannot be edited by the configuration editor.");
		}
		return part.isMultiple()
			? createListControl(context, model, type, MetaLabelProvider.INSTANCE.getLabel(part))
			: createItemControl(context, model, type);
	}

	/**
	 * The configuration interface the attribute's values have, or {@code null} if its values are
	 * not configurations at all.
	 */
	private static Class<? extends ConfigurationItem> configType(TLStructuredTypePart part) {
		if (!(part.getType() instanceof TLPrimitive primitive)) {
			return null;
		}
		StorageMapping<?> mapping = primitive.getStorageMapping();
		if (mapping == null) {
			return null;
		}
		Class<?> applicationType = mapping.getApplicationType();
		if (!ConfigurationItem.class.isAssignableFrom(applicationType)) {
			return null;
		}
		return applicationType.asSubclass(ConfigurationItem.class);
	}

	/**
	 * The item editor over a copy of what the field holds.
	 *
	 * <p>
	 * Free of the model: everything this decides - copy, and push on the first change - is about the
	 * field and the configuration. Available on its own for a caller that has a {@link FieldModel}
	 * holding a configuration but no {@link TLStructuredTypePart} to read a type off.
	 * </p>
	 */
	public static ReactControl createItemControl(ReactContext context, FieldModel model,
			Class<? extends ConfigurationItem> type) {
		Object current = model.getValue();
		ConfigurationItem edited = current instanceof ConfigurationItem item
			? TypedConfiguration.copy(item)
			: TypedConfiguration.newConfigItem(type);

		ConfigFieldIndex index = new ConfigFieldIndex();
		Push push = new Push(() -> model.setValue(edited));
		index.observeFields(push::watch);

		ReactControl editor =
			new ConfigEditorControl(context, edited, Collections.emptySet(), false, index, model.isEditable());
		push.armed();
		return editor;
	}

	/**
	 * The list editor over copies of what the field holds.
	 *
	 * @see #createItemControl(ReactContext, FieldModel, Class)
	 */
	public static ReactControl createListControl(ReactContext context, FieldModel model,
			Class<? extends ConfigurationItem> type, String label) {
		List<ConfigurationItem> edited = copies(model.getValue());

		// The collection works over a holder of this provider's own, not over the attribute's
		// field: the field must not see the copies before something is actually changed, and the
		// collection writes its list back on every add, remove and reorder.
		AbstractFieldModel holder = new AbstractFieldModel(edited);
		holder.setEditable(model.isEditable());
		FieldCollectionValue value = new FieldCollectionValue(holder, type, label);

		ConfigFieldIndex index = new ConfigFieldIndex();
		Push push = new Push(() -> model.setValue(new ArrayList<>(currentOf(holder))));
		index.observeFields(push::watch);
		// Adding, removing and reordering never touch a field, so the holder itself is watched too.
		holder.addListener(push.listener());

		ReactControl editor =
			new ConfigListEditorControl(context, value, PolymorphicOptions.Choices.NONE, index, model.isEditable());
		push.armed();
		return editor;
	}

	private static List<ConfigurationItem> copies(Object value) {
		List<ConfigurationItem> result = new ArrayList<>();
		if (value instanceof Collection<?> collection) {
			for (Object each : collection) {
				result.add(TypedConfiguration.copy((ConfigurationItem) each));
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static List<ConfigurationItem> currentOf(FieldModel holder) {
		Object value = holder.getValue();
		return value instanceof List<?> list ? (List<ConfigurationItem>) list : new ArrayList<>();
	}

	/**
	 * Hands the edited copy to the attribute's field, once anything has actually been changed.
	 *
	 * <p>
	 * Deliberately silent while the editor is still being built: building it registers every field
	 * it creates, which would otherwise count as a change and leave an untouched form looking
	 * edited.
	 * </p>
	 */
	private static final class Push {

		private final Runnable _push;

		private boolean _armed;

		Push(Runnable push) {
			_push = push;
		}

		/** Starts reacting - called once the editor is built. */
		void armed() {
			_armed = true;
		}

		/**
		 * Watches one field the editor built, and takes its registration as a change too.
		 *
		 * <p>
		 * The registration matters on its own: a field appearing after the editor was built means
		 * the editor was rebuilt, which is what a structural change does.
		 * </p>
		 */
		void watch(ConfigFieldModel field) {
			field.addListener(listener());
			fire();
		}

		FieldModelListener listener() {
			return new FieldModelListener() {
				@Override
				public void onValueChanged(FieldModel source, Object oldValue, Object newValue) {
					fire();
				}

				@Override
				public void onEditabilityChanged(FieldModel source, boolean editable) {
					// Not a change of what is edited.
				}

				@Override
				public void onValidationChanged(FieldModel source) {
					// A verdict about a value, not a new one.
				}
			};
		}

		private void fire() {
			if (_armed) {
				_push.run();
			}
		}
	}

}

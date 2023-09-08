/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.Fields.*;
import static com.top_logic.layout.form.values.Values.*;

import java.lang.reflect.Array;
import java.text.Format;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.VoidValueProvider;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.format.BuiltInFormats;
import com.top_logic.basic.config.format.ClassFormat;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.AttachListener;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.CollapsedListener;
import com.top_logic.layout.form.Collapsible;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.boxes.reactive_tag.GroupCellControl;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.form.values.Value;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.Icons;
import com.top_logic.layout.form.values.edit.Labels;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * Editor utilities.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class EditorUtils {

	/**
	 * Constraint checking the the value of a property is set.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class ValueSetConstraint extends AbstractConstraint {

		private final ValueModel _valueModel;

		/**
		 * Creates a new {@link ValueSetConstraint}.
		 */
		private ValueSetConstraint(ValueModel valueModel) {
			_valueModel = valueModel;
		}

		@Override
		public boolean check(Object value) throws CheckException {
			if (!_valueModel.getPropertyValue().isSet()) {
				throw errorEmptyValue();
			} else {
				Object propValue = _valueModel.getPropertyValue().get();
				if (propValue == null) {
					throw errorEmptyValue();
				}
				if (propValue instanceof Collection && ((Collection<?>) propValue).isEmpty()) {
					throw errorEmptyValue();
				}
				if (propValue instanceof Map && ((Map<?, ?>) propValue).isEmpty()) {
					throw errorEmptyValue();
				}
				if (propValue.getClass().isArray() && Array.getLength(propValue) == 0) {
					throw errorEmptyValue();
				}
			}
			return true;
		}

		private CheckException errorEmptyValue() {
			ResKey property = Labels.propertyLabelKey(_valueModel.getProperty());
			return new CheckException(I18NConstants.ERROR_MANDATORY_PROPERTY_NOT_SET__PROPERTY.fill(property));
		}
	}

	/**
	 * {@link FormMember} name for the key part of a map entry.
	 */
	public static final String MAP_VALUE_MEMBER_NAME = "value";

	/**
	 * {@link FormMember} name for the value part of a map entry.
	 */
	public static final String MAP_KEY_MEMBER_NAME = "key";

	/**
	 * Name for the {@link FormGroup} containing the whole content.
	 */
	public static final String LIST_CONTENT_GROUP = "content";

	/**
	 * {@link FormMember} name for the button to remove an entry.
	 */
	public static final String LIST_REMOVE = "remove";

	/**
	 * Name for the {@link FormGroup} containing the whole part for an entry.
	 */
	public static final String LIST_ITEM_GROUP = "item";

	/**
	 * {@link FormMember} name for the button to add a new entry.
	 */
	public static final String LIST_ADD = "add";

	/**
	 * Prefix for {@link FormGroup}s containing the content for each entry.
	 */
	public static final String LIST_ELEMENT_PREFIX = "element";

	/**
	 * {@link FormMember} name for the value of a list entry.
	 */
	public static final String LIST_VALUE_MEMBER_NAME = "value";

	/**
	 * Creates a form template for a {@link List} typed property.
	 * 
	 * @param initiallyCollapsed
	 *        Informations how to display the given group.
	 */
	public static HTMLTemplateFragment listTemplate(FormGroup container, boolean initiallyCollapsed) {
		return listTemplate(container, initiallyCollapsed, listEntryTemplate());
	}

	/**
	 * Creates a form template for a {@link Map} typed property.
	 * 
	 * @param initiallyCollapsed
	 *        Informations how to display the given group.
	 */
	public static HTMLTemplateFragment mapTemplate(FormGroup container, boolean initiallyCollapsed) {
		return listTemplate(container, initiallyCollapsed, mapEntryTemplate());
	}

	private static HTMLTemplateFragment listTemplate(FormGroup container, boolean initiallyCollapsed,
			HTMLTemplateFragment entry) {
		return div(
			fieldsetBox(
				span(label()),
				div(css("dfList"),
					div(css("dfListContent"),
						member(LIST_CONTENT_GROUP, div(
							items(div(css("dfListEntry"), entry))))),
					div(css("dfListAdd"),
						member(LIST_ADD))),
				ConfigKey.field(container)).setInitiallyCollapsed(initiallyCollapsed));
	}

	private static HTMLTemplateFragment mapEntryTemplate() {
		return EditorUtils.titleWithIconAndRemove(
			htmlTemplate(Icons.ITEM_ICON),
			div(member(EditorUtils.LIST_ITEM_GROUP, div(css("rf_columnsLayout"),
				fieldBox(MAP_KEY_MEMBER_NAME),
				fieldBox(MAP_VALUE_MEMBER_NAME)))),
			null);
	}

	private static HTMLTemplateFragment listEntryTemplate() {
		return EditorUtils.titleWithIconAndRemove(
			htmlTemplate(Icons.ITEM_ICON),
			span(member(EditorUtils.LIST_ITEM_GROUP), member(EditorUtils.LIST_ITEM_GROUP, MemberStyle.ERROR)),
			null);
	}

	/**
	 * Creates the value provider by the given {@link Class}.
	 */
	public static Optional<ConfigurationValueProvider<?>> createValueProvider(
			Class<? extends ConfigurationValueProvider<?>> clazz) {
		if (clazz != VoidValueProvider.class) {
			try {
				return Optional.of(ConfigUtil.getInstance(clazz));
			} catch (ConfigurationException exception) {
				Logger.error("Instance class could not be resolved.", exception, EditorUtils.class);

				throw new ConfigurationError(exception);
			}
		}

		return Optional.empty();
	}

	/**
	 * Creates a {@link FormField} for the given primitive type. Using the given
	 * {@link ConfigurationValueProvider} if present.
	 */
	public static FormField createPrimitiveField(FormGroup parent, String name, Class<?> type,
			Optional<ConfigurationValueProvider<?>> explicitValueProvider) {
		if (explicitValueProvider.isPresent()) {
			return complex(parent, name, getFormat(explicitValueProvider.get()));
		} else if (type == String.class) {
			return line(parent, name);
		} else if (type == Class.class) {
			return complex(parent, name, getFormat(ClassFormat.INSTANCE));
		} else if (type == Boolean.class || type == boolean.class) {
			BooleanField checkbox = checkbox(parent, name);
			checkbox.setValue(false);

			return checkbox;
		} else if (type == Double.class || type == double.class || type == Float.class
			|| type == float.class) {
			return doubleField(parent, name);
		} else if (type == Date.class) {
			return calendar(parent, name);
		} else if (type == Byte.class || type == byte.class) {
			return byteField(parent, name);
		} else if (type == Short.class || type == short.class) {
			return shortField(parent, name);
		} else if (type == Integer.class || type == int.class) {
			return intField(parent, name);
		} else if (type == Long.class || type == long.class) {
			return longField(parent, name);
		}

		ConfigurationValueProvider<?> defaultValueProvider;
		com.top_logic.basic.config.annotation.Format formatAnnotation =
			type.getAnnotation(com.top_logic.basic.config.annotation.Format.class);
		if (formatAnnotation == null) {
			defaultValueProvider = BuiltInFormats.getPrimitiveValueProvider(type);
		} else {
			try {
				defaultValueProvider = ConfigUtil.getInstance(formatAnnotation.value());
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
		if (defaultValueProvider == null) {
			return null;
		}
		return complex(parent, name, getFormat(defaultValueProvider));
	}

	@SuppressWarnings("unchecked")
	private static Format getFormat(ConfigurationValueProvider<?> valueProvider) {
		return new ConfigurationFormatAdapter((ConfigurationValueProvider<Object>) valueProvider);
	}

	/**
	 * Adds an additional field observing whether form members are added or removed.
	 * 
	 * <p>
	 * This method can be used to {@link PropertyDescriptor} for which no {@link FormField}s are
	 * generated that have a "changed" state, e.g. a list property for which a {@link FormContainer}
	 * is created and for each list element a separate {@link FormMember}. In that case adding a new
	 * {@link FormMember} for a new list element is not detected by any change check algorithm.
	 * </p>
	 * 
	 * <p>
	 * Moreover, this field is used as field proxy to add configuration constraints. When the
	 * configuration constraint fails, the returned field gets an error (or warning) state.
	 * </p>
	 * 
	 * @param container
	 *        The {@link FormContainer} to create the new field in.
	 * @param content
	 *        {@link FormContainer} to inspect to detect adding or removing members.
	 * @param factory
	 *        Factory which creates the form elements.
	 * @return The observing field.
	 */
	public static BooleanField addAdditionalChangedField(FormContainer container, FormContainer content,
			EditorFactory factory, ValueModel valueModel) {
		BooleanField changed = addChangeField(container, valueModel);
	
		MemberChangedListener listener = new MemberChangedListener() {

			private Collection<FormMember> _added = new HashSet<>();

			private boolean _remainsChanged = false;

			@Override
			public Bubble memberAdded(FormContainer parent, FormMember member) {
				if (parent == content) {
					if (!_remainsChanged) {
						_added.add(member);
						changed.setAsBoolean(true);
					}
					changed.checkWithAllDependencies();
				}
				return Bubble.BUBBLE;
			}

			@Override
			public Bubble memberRemoved(FormContainer parent, FormMember member) {
				if (parent == content) {
					if (!_remainsChanged) {
						boolean removed = _added.remove(member);
						if (!removed) {
							/* An element was removed which was not added. It is almost impossible
							 * to detect whether this operation will be reverted in the future. For
							 * safety reasons, the changed state remains true. */
							changed.setAsBoolean(true);
							_remainsChanged = true;
						} else if (_added.isEmpty()) {
							/* All changes were reverted. */
							changed.reset();
						}
					}
					changed.checkWithAllDependencies();
				}
				return Bubble.BUBBLE;
			}

		};
		content.addListener(FormContainer.MEMBER_ADDED_PROPERTY, listener);
		content.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, listener);

		if (valueModel.getProperty().isMandatory()
				|| factory.getAnnotation(valueModel.getProperty(), Mandatory.class) != null) {
			changed.addConstraint(new ValueSetConstraint(valueModel));
		}
		container.set(Editor.CONSTRAINT_FIELD, changed);
		return changed;
	}

	/**
	 * Creates an {@link BooleanField} in the given container that can be uses to detect changes for
	 * the given {@link ValueModel}.
	 * 
	 * @see #addAdditionalChangedField(FormContainer, FormContainer, EditorFactory, ValueModel)
	 */
	public static BooleanField addChangeField(FormContainer container, ValueModel valueModel) {
		BooleanField changed = checkbox(container, StringServices.randomUUID());
		changed.setLabel(Resources.getInstance()
			.getString(I18NConstants.CHANGES_IN__ATTRIBUTE.fill(Labels.propertyLabel(valueModel))));
		return changed;
	}

	/**
	 * Creates an {@link ErrorControl} for the given field wrapped into a {@link BlockControl}.
	 */
	public static BlockControl errorBlock(FormMember member) {
		BlockControl block = new BlockControl();
		block.addChild(new ErrorControl(member, true));
		return block;
	}

	/**
	 * Initializer for a {@link GroupCellControl} to show a given block only if the group is
	 * collapsed.
	 */
	public static Consumer<GroupCellControl> showIfCollapsed(BlockControl block) {
		return groupCellControl -> {
			Collapsible collapsible = groupCellControl.getCollapsible();

			CollapsedListener collapsedListener = new CollapsedListener() {
				@Override
				public Bubble handleCollapsed(Collapsible sender, Boolean oldValue, Boolean newValue) {
					// Do not care about inner collapsibles
					if (collapsible.equals(sender)) {
						block.setVisible(newValue.booleanValue());
					}
					return Bubble.BUBBLE;
				}
			};
			AttachListener attachedListener =
				new AttachListener(collapsible, Collapsible.COLLAPSED_PROPERTY, collapsedListener) {
					@Override
					protected void updateObservedState(AbstractControlBase sender) {
						block.setVisible(collapsible.isCollapsed());
					}
				};
			groupCellControl.addListener(AbstractControlBase.ATTACHED_PROPERTY, attachedListener);
		};
	}

	/**
	 * Creates a {@link HTMLTemplateFragment} to render an list entry title with an icon on the left
	 * side and a {@link #LIST_REMOVE remove} command on the right side.
	 * 
	 * @param iconTemplate
	 *        Template to render the "icon" of the title.
	 * @param titleContent
	 *        The content of the title.
	 * @param additionalCSS
	 *        Optional CSS class to use for the title {@link HTMLConstants#DIV}.
	 */
	public static HTMLTemplateFragment titleWithIconAndRemove(HTMLTemplateFragment iconTemplate,
			HTMLTemplateFragment titleContent, CharSequence additionalCSS) {
		String cssClass = "dfEntryTitle";
		if (!StringServices.isEmpty(additionalCSS)) {
			cssClass += " " + additionalCSS;
		}
		return div(css(cssClass),
			span(css("dfListIcon"), iconTemplate),
			span(css(FormConstants.FLEXIBLE_CSS_CLASS), titleContent),
			span(css(FormConstants.FIXED_RIGHT_CSS_CLASS),
				member(LIST_REMOVE)));
	}

	/**
	 * Adds a button with name {@link #LIST_REMOVE} to the given container.
	 * 
	 * @param container
	 *        The {@link FormContainer} to add the remove command to.
	 * @param removeCommand
	 *        The command to execute when activating the button.
	 * @param invisible
	 *        Whether the button must not be visible.
	 */
	public static CommandField addRemoveButton(FormContainer container, Command removeCommand,
			Value<Boolean> invisible) {
		CommandField removeButton = button(container, LIST_REMOVE, Icons.REMOVE_ICON, removeCommand);
		removeButton.setControlProvider(Buttons.REMOVE_BUTTON);
		Value<Boolean> parentImmutable = or(invisible, isImmutable(container));
		bindLabel(removeButton, literal(I18NConstants.REMOVE_ELEMENT));
		bindVisible(removeButton, not(parentImmutable));
		return removeButton;
	}
}

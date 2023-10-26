/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values;

import static com.top_logic.basic.util.Utils.*;
import static com.top_logic.layout.form.values.Values.*;

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.Equality;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.AlgorithmSpec;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.DefaultConfigConstructorScheme;
import com.top_logic.basic.config.DerivedPropertyAlgorithm;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.func.IGenericFunction;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.DisabledPropertyListener;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.MemberChangedListener;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.LongPrimitiveRangeConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.form.model.TreeTableField;
import com.top_logic.layout.form.model.utility.DefaultListOptionModel;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.IdentityOptionMapping;
import com.top_logic.layout.form.values.edit.InvalidOptionMapping;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.form.values.edit.OptionMappingProvider;
import com.top_logic.layout.form.values.edit.annotation.CollapseEntries;
import com.top_logic.layout.form.values.edit.annotation.CustomOptionOrder;
import com.top_logic.layout.form.values.edit.annotation.DisplayMinimized;
import com.top_logic.layout.form.values.edit.annotation.DynamicMandatory;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.EnumResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.resources.NestedResourceView;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.VisibilityListener;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.css.CssUtil;

/**
 * Utilities for creating interactive forms in a declarative style.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Fields {

	/**
	 * {@link Property} annotated to {@link DerivedProperty} returned by
	 * {@link #optionProvider(DeclarativeFormOptions)} holding the {@link OptionMapping}.
	 * 
	 * @see #optionMapping(DerivedProperty)
	 */
	public static final Property<OptionMapping> OPTION_MAPPING =
		TypedAnnotatable.property(OptionMapping.class, "option-mapping", IdentityOptionMapping.INSTANCE);

	/**
	 * {@link SwitchBlock} for a concrete value.
	 * 
	 * @see #getTestValue()
	 * @see Otherwise
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static class Case extends SwitchBlock {

		private final Object _testValue;

		public Case(Object testValue, Runnable[] actions) {
			super(actions);
			_testValue = testValue;
		}

		public Object getTestValue() {
			return _testValue;
		}

	}

	/**
	 * Actions to be executed on a certain event.
	 * 
	 * @see Fields#onValue(com.top_logic.layout.form.FormField, SwitchBlock...)
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public abstract static class SwitchBlock {

		private final Runnable[] _actions;

		public SwitchBlock(Runnable[] actions) {
			_actions = actions;
		}

		public Runnable[] getActions() {
			return _actions;
		}

	}

	/**
	 * {@link SwitchBlock} for any other value.
	 * 
	 * @see Case
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private static class Otherwise extends SwitchBlock {

		public Otherwise(Runnable[] actions) {
			super(actions);
		}

	}

	private static abstract class AbstractImageBinding implements Listener {

		private final CommandField _member;

		private final Value<ThemeImage> _image;

		public AbstractImageBinding(CommandField member, Value<ThemeImage> image) {
			_member = member;
			_image = image;
		}

		protected CommandField getField() {
			return _member;
		}

		protected ThemeImage getImage() {
			return _image.get();
		}

	}

	private static final class ImageBinding extends AbstractImageBinding {

		public ImageBinding(CommandField member, Value<ThemeImage> image) {
			super(member, image);
		}

		@Override
		public void handleChange(Value<?> target) {
			getField().setImage(getImage());
		}

	}

	private static final class NotExecutableImageBinding extends AbstractImageBinding {

		public NotExecutableImageBinding(CommandField member, Value<ThemeImage> image) {
			super(member, image);
		}

		@Override
		public void handleChange(Value<?> target) {
			getField().setNotExecutableImage(getImage());
		}

	}

	private static final class FieldModeBinding implements Listener {

		private final FormMember _member;

		private final Value<FieldMode> _visibility;

		private final PropertyDescriptor _property;

		private final ConfigurationItem _item;

		public FieldModeBinding(FormMember member, Value<FieldMode> visibility, PropertyDescriptor property,
				ConfigurationItem item) {
			_member = member;
			_visibility = visibility;
			_property = property;
			_item = item;
		}

		@Override
		public void handleChange(Value<?> target) {
			FieldMode newMode = _visibility.get();
			_member.setMode(newMode);
		}

	}

	private static final class ImmutabilityBinding implements Listener {
		private final FormMember _member;

		private final Value<Boolean> _immutability;

		public ImmutabilityBinding(FormMember member, Value<Boolean> visibility) {
			_member = member;
			_immutability = visibility;
		}

		@Override
		public void handleChange(Value<?> target) {
			_member.setImmutable(_immutability.get());
		}
	}

	private static final class VisibilityBinding implements Listener {
		private final FormMember _member;

		private final Value<Boolean> _visibility;

		public VisibilityBinding(FormMember member, Value<Boolean> visibility) {
			_member = member;
			_visibility = visibility;
		}

		@Override
		public void handleChange(Value<?> target) {
			_member.setVisible(_visibility.get());
		}
	}

	private static final class DisabledBinding implements Listener {
		private final FormMember _member;

		private final Value<Boolean> _disabled;

		public DisabledBinding(FormMember member, Value<Boolean> disabled) {
			_member = member;
			_disabled = disabled;
		}

		@Override
		public void handleChange(Value<?> target) {
			_member.setDisabled(_disabled.get());
		}
	}

	private static final class MandatoryBinding implements Listener {
		private final FormMember _member;

		private final Value<Boolean> _value;

		public MandatoryBinding(FormMember member, Value<Boolean> value) {
			_member = member;
			_value = value;
		}

		@Override
		public void handleChange(Value<?> target) {
			_member.setMandatory(_value.get());
		}
	}

	private static final class ValueBinding implements Listener {
		private final FormField _dest;

		private final Value<?> _value;

		private final Mapping<Object, ?> _converter;

		public ValueBinding(FormField dest, Value<?> value, Mapping<Object, ?> converter) {
			_dest = dest;
			_value = value;
			_converter = converter;
		}

		@Override
		public void handleChange(Value<?> target) {
			_dest.setValue(modelValue());
		}

		public Object modelValue() {
			return _converter.map(_value.get());
		}
	}

	private static final class LabelKeyBinding implements Listener {
		private final FormMember _dest;

		private final Value<ResKey> _value;

		public LabelKeyBinding(FormMember dest, Value<ResKey> value) {
			_dest = dest;
			_value = value;
		}

		@Override
		public void handleChange(Value<?> target) {
			_dest.setLabel(Resources.getInstance().getString(_value.get()));
		}
	}

	private static final class TooltipKeyBinding implements Listener {
		private final FormMember _dest;

		private final Value<ResKey> _value;

		public TooltipKeyBinding(FormMember dest, Value<ResKey> value) {
			_dest = dest;
			_value = value;
		}

		@Override
		public void handleChange(Value<?> target) {
			_dest.setTooltip(Resources.getInstance().getString(_value.get()));
		}
	}

	private static final class TooltipCaptionKeyBinding implements Listener {
		private final FormMember _dest;

		private final Value<ResKey> _value;

		public TooltipCaptionKeyBinding(FormMember dest, Value<ResKey> value) {
			_dest = dest;
			_value = value;
		}

		@Override
		public void handleChange(Value<?> target) {
			_dest.setTooltipCaption(Resources.getInstance().getString(_value.get()));
		}
	}

	private static final class OptionBinding implements Listener {
		private final SelectField _select;

		private final Value<? extends Iterable<?>> _optionValue;

		private final boolean _adjustValue;

		public OptionBinding(SelectField select, Value<? extends Iterable<?>> optionValue, boolean adjustValue) {
			_select = select;
			_optionValue = optionValue;
			_adjustValue = adjustValue;
		}

		@Override
		public void handleChange(Value<?> target) {
			if (_adjustValue) {
				setOptionsKeepValue(_select, _optionValue.get());
			} else {
				setOptions(_select, _optionValue.get());
			}
		}
	}

	private static final class OnValue implements ValueListener {
		private final Otherwise _otherwise;
	
		private final Map<Object, Case> _indexedCases;
	
		public OnValue(Map<Object, Case> indexedCases, Otherwise otherwise) {
			_otherwise = otherwise;
			_indexedCases = indexedCases;
		}
	
		@Override
		public void valueChanged(FormField changedField, Object oldValue, Object newValue) {
			Case caseBlock = _indexedCases.get(normalizeValue(changedField, newValue));
			if (caseBlock != null) {
				execute(caseBlock);
			} else if (_otherwise != null) {
				execute(_otherwise);
			}
		}
	
		private void execute(SwitchBlock switchBlock) {
			for (Runnable action : switchBlock.getActions()) {
				action.run();
			}
		}
	}

	private static final class VisibilityUpdate implements Runnable {
		private final boolean _visible;
	
		private final FormMember[] _members;
	
		public VisibilityUpdate(FormMember[] members, boolean visible) {
			_visible = visible;
			_members = members;
		}
	
		@Override
		public void run() {
			for (FormMember member : _members) {
				member.setVisible(_visible);
			}
		}
	}

	private static class MemberImmutable extends AbstractModifiableValue<Boolean> {
		private final FormMember _member;
		
		public MemberImmutable(FormMember member) {
			_member = member;
		}
		
		@Override
		public Boolean get() {
			return Boolean.valueOf(_member.isImmutable());
		}
		
		@Override
		public ListenerBinding addListener(final Listener listener) {
			FieldModeObserver adapter = new FieldModeObserver(this, listener);
			_member.addListener(FormMember.IMMUTABLE_PROPERTY, adapter);
			return () -> _member.removeListener(FormMember.IMMUTABLE_PROPERTY, adapter);
		}
		
		@Override
		public void set(Boolean newValue) {
			_member.setImmutable(newValue.booleanValue());
		}
	}
	
	private static class MemberDisabled extends AbstractModifiableValue<Boolean> {
		private final FormMember _member;

		public MemberDisabled(FormMember member) {
			_member = member;
		}

		@Override
		public Boolean get() {
			return Boolean.valueOf(_member.isDisabled());
		}

		@Override
		public ListenerBinding addListener(final Listener listener) {
			FieldModeObserver adapter = new FieldModeObserver(this, listener);
			_member.addListener(FormMember.DISABLED_PROPERTY, adapter);
			return () -> _member.removeListener(FormMember.DISABLED_PROPERTY, adapter);
		}

		@Override
		public void set(Boolean newValue) {
			_member.setDisabled(newValue.booleanValue());
		}
	}

	private static class MemberVisible extends AbstractModifiableValue<Boolean> {
		private final FormMember _member;

		public MemberVisible(FormMember member) {
			_member = member;
		}

		@Override
		public Boolean get() {
			return Boolean.valueOf(_member.isVisible());
		}

		@Override
		public ListenerBinding addListener(final Listener listener) {
			FieldModeObserver adapter = new FieldModeObserver(this, listener);
			_member.addListener(FormMember.VISIBLE_PROPERTY, adapter);
			return () -> _member.removeListener(FormMember.VISIBLE_PROPERTY, adapter);
		}

		@Override
		public void set(Boolean newValue) {
			_member.setVisible(newValue.booleanValue());
		}
	}

	private static class FieldModeObserver implements ImmutablePropertyListener, DisabledPropertyListener,
			VisibilityListener {
		private final Value<?> _value;

		private final Listener _listener;

		public FieldModeObserver(Value<?> value, Listener listener) {
			_value = value;
			_listener = listener;
		}

		@Override
		public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			_listener.handleChange(_value);
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleDisabledChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
			_listener.handleChange(_value);
			return Bubble.BUBBLE;
		}

		@Override
		public Bubble handleVisibilityChange(Object sender, Boolean oldVisibility, Boolean newVisibility) {
			_listener.handleChange(_value);
			return Bubble.BUBBLE;
		}
	}

	private static class FieldValue<T> extends AbstractModifiableValue<T> {
		private final FormField _field;
	
		public FieldValue(FormField field) {
			_field = field;
		}
	
		@Override
		public T get() {
			if (_field.hasValue()) {
				Object value = _field.getValue();
				@SuppressWarnings("unchecked")
				T result = (T) normalizeValue(_field, value);
				return result;
			} else {
				return null;
			}
		}
	
		@Override
		public ListenerBinding addListener(final Listener listener) {
			final Value<?> self = this;
			ValueListener adapter = new ValueListener() {
				@Override
				public void valueChanged(FormField changedField, Object oldValue, Object newValue) {
					listener.handleChange(self);
				}
			};
			_field.addValueListener(adapter);
			return () -> _field.removeValueListener(adapter);
		}

		@Override
		public void set(T newValue) {
			_field.setValue(internalizeValue(_field, newValue));
		}
	}

	static class Members extends AbstractValue<List<FormMember>> {

		private final FormContainer _container;

		public Members(FormContainer container) {
			_container = container;
		}

		@Override
		public List<FormMember> get() {
			return CollectionUtil.toList(_container.getMembers());
		}

		@Override
		public ListenerBinding addListener(final Listener listener) {
			MemberChangedListener changeListener = new MemberChangedListener() {

				@Override
				public Bubble memberAdded(FormContainer parent, FormMember member) {
					listener.handleChange(Members.this);
					return Bubble.BUBBLE;
				}

				@Override
				public Bubble memberRemoved(FormContainer parent, FormMember member) {
					listener.handleChange(Members.this);
					return Bubble.BUBBLE;
				}
			};
			_container.addListener(FormContainer.MEMBER_ADDED_PROPERTY, changeListener);
			_container.addListener(FormContainer.MEMBER_REMOVED_PROPERTY, changeListener);
			
			return () -> {
				_container.removeListener(FormContainer.MEMBER_ADDED_PROPERTY, changeListener);
				_container.removeListener(FormContainer.MEMBER_REMOVED_PROPERTY, changeListener);
			};
		}
	}

	private static final class ExecutabilityBinding implements Listener {
		private final CommandField _button;
	
		private final Value<ExecutableState> _executability;
	
		ExecutabilityBinding(CommandField button, Value<ExecutableState> executability) {
			_button = button;
			_executability = executability;
		}
	
		@Override
		public void handleChange(Value<?> sender) {
			ExecutableState state = _executability.get();
			if (state.isExecutable()) {
				_button.setExecutable();
			} else {
				_button.setNotExecutable(state.getI18NReasonKey());
			}
		}
	}

	private static final class ReducedValue<R, A> extends AbstractValue<R> {
		private final Value<? extends A>[] _inputs;

		private final Reduce<R, A> _reduce;
	
		ReducedValue(Value<? extends A>[] inputs, Reduce<R, A> reduce) {
			_inputs = inputs;
			_reduce = reduce;
		}
	
		@Override
		public R get() {
			R result = _reduce.neutral();
			for (Value<? extends A> input : _inputs) {
				A value = input.get();
				result = _reduce.combine(result, value);
				if (_reduce.isZero(result)) {
					break;
				}
			}
			return result;
		}
	
		@Override
		public ListenerBinding addListener(Listener listener) {
			List<ListenerBinding> bindings = new ArrayList<>();
			for (Value<?> executability : _inputs) {
				bindings.add(executability.addListener(listener));
			}
			return () -> {
				for (ListenerBinding binding : bindings) {
					binding.close();
				}
			};
		}
	}

	private static final Value<? extends ConfigurationItem> NULL = literal(null);

	private static final Class<?>[] SIGNATURE = { DeclarativeFormOptions.class };

	public static <T> ModifiableValue<T> fieldValue(final FormField field) {
		return new FieldValue<>(field);
	}

	public static Value<Boolean> fieldValue(final BooleanField field) {
		return new FieldValue<>(field);
	}

	public static Value<List<FormMember>> members(final FormContainer container) {
		return new Members(container);
	}

	/**
	 * {@link Mapping} calling {@link Fields#internalizeValue(FormField, Object)} with the given
	 * input.
	 */
	public static class UIConversion implements Mapping<Object, Object> {
		private final FormField _field;

		/**
		 * Creates a {@link UIConversion}.
		 * 
		 * @param field
		 *        The {@link FormField} to pass as context to
		 *        {@link Fields#internalizeValue(FormField, Object)}.
		 */
		public UIConversion(FormField field) {
			_field = field;
		}

		/**
		 * The {@link FormField} this conversion is for.
		 */
		public FormField getField() {
			return _field;
		}

		@Override
		public Object map(Object input) {
			return Fields.internalizeValue(_field, input);
		}
	}

	/**
	 * Binds the value of the given {@link FormField} to the given dynamic {@link Value}.
	 * 
	 * <p>
	 * This method is a short-cut for {@link Fields#bindValue(FormField, Value, Mapping)} with the
	 * default mapping {@link UIConversion}.
	 * </p>
	 * 
	 * @param dest
	 *        The destination field to update, if the given dynamic value changes.
	 * @param value
	 *        The dynamic value to store as value in the given field.
	 * @return The established binding.
	 */
	public static ListenerBinding bindValue(FormField dest, final Value<?> value) {
		return bindValue(dest, value, new UIConversion(dest));
	}

	/**
	 * Binds the value of the given {@link FormField} to the given dynamic {@link Value}.
	 * 
	 * @param dest
	 *        The destination field to update, if the given dynamic value changes.
	 * @param value
	 *        The dynamic value to store as value in the given field.
	 * @param converter
	 *        A conversion function to adapt the value object taken from the given {@link Value}
	 *        before storing to the given field.
	 * @return The established binding.
	 */
	public static ListenerBinding bindValue(FormField dest, final Value<?> value, Mapping<Object, ?> converter) {
		ValueBinding binding = new ValueBinding(dest, value, converter);
		dest.initializeField(binding.modelValue());
		return bindListener(value, binding);
	}

	public static FormMember bindLabel(FormMember dest, final Value<ResKey> value) {
		bindListener(value, new LabelKeyBinding(dest, value));
		return dest;
	}

	public static FormMember bindTooltip(FormMember dest, final Value<ResKey> value) {
		bindListener(value, new TooltipKeyBinding(dest, value));
		return dest;
	}

	public static FormMember bindTooltipCaption(FormMember dest, final Value<ResKey> value) {
		bindListener(value, new TooltipCaptionKeyBinding(dest, value));
		return dest;
	}

	/**
	 * The {@link Comparator} for the options of the given property.
	 */
	public static Comparator<Object> getOptionComparator(PropertyDescriptor property) {
		return getOptionComparator(property, null);
	}

	/**
	 * The {@link Comparator} for the options of the given property.
	 */
	public static Comparator<Object> getOptionComparator(PropertyDescriptor property, LabelProvider labelProvider) {
		if (hasCustomOptionOrder(property)) {
			return Equality.INSTANCE;
		}
		if (labelProvider == null) {
			return LabelComparator.newCachingInstance();
		}
		return LabelComparator.newCachingInstance(labelProvider);
	}

	/**
	 * Whether the property is annotated with {@link CustomOptionOrder}.
	 */
	public static boolean hasCustomOptionOrder(PropertyDescriptor property) {
		return property.getAnnotation(CustomOptionOrder.class) != null;
	}

	/**
	 * Computes a derived options property for the given {@link PropertyDescriptor} either from
	 * {@link Options} annotation or generically from type index lookup.
	 * 
	 * @see #optionMapping(DerivedProperty)
	 * @see EditorFactory#formOptions(PropertyDescriptor)
	 */
	public static DerivedProperty<? extends Iterable<?>> optionProvider(DeclarativeFormOptions options) {
		PropertyDescriptor property = options.getProperty();
		ConfigurationDescriptor descriptor = property.getDescriptor();
		String location = "options for '" + property.getPropertyName() + "'";
		AnnotationCustomizations customizations = options.getCustomizations();
		Options optionsAnnotation = customizations.getAnnotation(property, Options.class);
		if (optionsAnnotation == null) {
			PropertyKind kind = property.kind();
			if ((kind == PropertyKind.ITEM || kind == PropertyKind.ARRAY || kind == PropertyKind.LIST
				|| kind == PropertyKind.MAP)
				&& (itemDisplay(customizations, property) == ItemDisplay.ItemDisplayType.POLYMORPHIC)) {
				final Class<?> instanceType = property.getInstanceType();

				OptionMapping optionMapping;
				IGenericFunction<OptionModel<Class<?>>> fun;
				if (instanceType == null) {
					// Pure configurations, options are config items.
					fun = new Function0<>() {
						@Override
						public OptionModel<Class<?>> apply() {
							// Sortieren!
							Collection<Class<?>> configTypes =
								TypeIndex.getInstance()
									.getSpecializations(property.getElementType(), true, true, false);
							
							return new DefaultListOptionModel<>(filterHidden(customizations, configTypes));
						}
					};
					optionMapping = ItemOptionMapping.INSTANCE;
				} else {
					// Instance configuration, options are implementation classes.
					fun = new Function0<>() {
						@Override
						public OptionModel<Class<?>> apply() {
							// Sortieren!
							Collection<Class<?>> implClasses =
								TypeIndex.getInstance().getSpecializations(instanceType, true, false, false);

							ArrayList<Class<?>> implTypes = new ArrayList<>(implClasses.size());
							for (Class<?> implType : implClasses) {
								if (isHidden(customizations, implType)) {
									continue;
								}

								try {
									DefaultConfigConstructorScheme.getFactory(implType);
								} catch (NoClassDefFoundError | ConfigurationException ex) {
									// Ignore problematic class.
									continue;
								}

								implTypes.add(implType);
							}

							return new DefaultListOptionModel<>(implTypes);
						}
					};
					optionMapping = ImplOptionMapping.INSTANCE;
				}

				OptionModel<Class<?>> optionModel = fun.invoke();
				Iterator<Class<?>> iterator = optionModel.iterator();
				if (!iterator.hasNext()) {
					// No options found at all.
					return null;
				}

				NamePath[] noPaths = {};
				AlgorithmSpec spec = AlgorithmSpec.create(fun, noPaths);
				DerivedProperty derivedProp = DerivedProperty.createDerivedProperty(Iterable.class,
					descriptor, spec, property, location);
				derivedProp.set(OPTION_MAPPING, optionMapping);
				return derivedProp;
			}
			return null;
		}
		Class<? extends IGenericFunction<? extends Iterable<?>>> functionClass = optionsAnnotation.fun();
		Ref[] argumentsSpec = optionsAnnotation.args();

		DerivedProperty derivedValue =
			getDerivedValue(options, descriptor, Iterable.class, functionClass, argumentsSpec, location);
		derivedValue.set(OPTION_MAPPING, getOptionMapping(optionsAnnotation, derivedValue, options));
		return derivedValue;
	}

	private static OptionMapping getOptionMapping(Options optionsAnnotation, DerivedProperty<?> derivedProp,
			DeclarativeFormOptions options) {
		Class<? extends OptionMapping> mappingFun = optionsAnnotation.mapping();
		OptionMapping optionMapping;
		if (mappingFun == InvalidOptionMapping.class) {
			DerivedPropertyAlgorithm algorithm = derivedProp.getAlgorithm();
			IGenericFunction<?> function = algorithm.getFunction();
			if (function instanceof OptionMappingProvider) {
				optionMapping = ((OptionMappingProvider) function).getOptionMapping();
			} else {
				optionMapping = IdentityOptionMapping.INSTANCE;
			}
		} else {
			optionMapping = getInstance(options, OptionMapping.class, mappingFun);
		}
		return optionMapping;
	}

	static ArrayList<Class<?>> filterHidden(AnnotationCustomizations customizations,
			Collection<Class<?>> configTypes) {
		ArrayList<Class<?>> result = new ArrayList<>();
		for (Class<?> type : configTypes) {
			if (isHidden(customizations, type)) {
				continue;
			}
			result.add(type);
		}
		return result;
	}

	static boolean isHidden(AnnotationCustomizations customizations, Class<?> type) {
		Hidden annotation = customizations.getAnnotation(type, Hidden.class);
		return annotation != null && annotation.value();
	}

	/**
	 * Returns the {@link OptionMapping} for the derived options property created by
	 * {@link #optionProvider(DeclarativeFormOptions)}.
	 * 
	 * @see #optionProvider(DeclarativeFormOptions)
	 */
	public static OptionMapping optionMapping(DerivedProperty<? extends Iterable<?>> optionProvider) {
		return optionProvider.get(OPTION_MAPPING);
	}

	/**
	 * Dynamic value of {@link FormMember#getMode()} of the UI created for the given property.
	 * 
	 * @see EditorFactory#formOptions(PropertyDescriptor)
	 */
	public static DerivedProperty<Boolean> mandatoryProperty(DeclarativeFormOptions options) {
		PropertyDescriptor property = options.getProperty();
		DynamicMandatory annotation = options.getCustomizations().getAnnotation(property, DynamicMandatory.class);
		if (annotation == null) {
			return null;
		}
		ConfigurationDescriptor descriptor = property.getDescriptor();
		Class<? extends IGenericFunction<Boolean>> functionClass = annotation.fun();
		Ref[] argumentsSpec = annotation.args();
		String location = "dynamic mandatory annotation of '" + property.getPropertyName() + "'";

		return getDerivedValue(options, descriptor, Boolean.class, functionClass, argumentsSpec, location);
	}

	/**
	 * Creates a {@link DerivedProperty} that takes it's value from the given
	 * {@link IGenericFunction}.
	 *
	 * @param <T>
	 *        The value type of the created property.
	 * @param options
	 *        See {@link EditorFactory#formOptions(PropertyDescriptor)}.
	 * @param descriptor
	 *        {@link ConfigurationDescriptor} describing the base item on which the created property
	 *        operates on.
	 * @param type
	 *        The dynamic type describing the type of the created property.
	 * @param functionClass
	 *        The algorithm computing the value.
	 * @param argumentsSpec
	 *        Description of the arguments passed to the compute function.
	 * @param location
	 *        Description of the context in which the property is created (for error messages).
	 * @return The newly created property.
	 */
	public static <T> DerivedProperty<T> getDerivedValue(DeclarativeFormOptions options,
			ConfigurationDescriptor descriptor,
			Class<T> type, Class<? extends IGenericFunction<? extends T>> functionClass,
			Ref[] argumentsSpec, String location) {
		IGenericFunction<? extends T> function = getInstance(options, IGenericFunction.class, functionClass);
		AlgorithmSpec spec = AlgorithmSpec.create(function, argumentsSpec);

		return DerivedProperty.createDerivedProperty(type, descriptor, spec, options.getProperty(), location);
	}

	private static <T> T getInstance(DeclarativeFormOptions options, Class<T> expectedType, Class<?> functionClass) {
		T function;
		try {
			function = contextInstance(expectedType, functionClass, options);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Cannot instantiate options function.", ex);
		}
		return function;
	}

	private static <T> T contextInstance(Class<T> expectedType, Class<?> functionClass, DeclarativeFormOptions options)
			throws ConfigurationException {
		return ConfigUtil.getInstance(expectedType, null, functionClass, SIGNATURE, options);
	}

	/**
	 * Creates a {@link LabelProvider} for the {@link PropertyDescriptor} described by the given
	 * {@link DeclarativeFormOptions}.
	 */
	public static LabelProvider optionLabelsOrNull(DeclarativeFormOptions options) {
		OptionLabels labels = options.getCustomizations().getAnnotation(options.getProperty(), OptionLabels.class);
		if (labels == null) {
			return null;
		}

		try {
			return contextInstance(LabelProvider.class, labels.value(), options);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError("Option provider cannot be resolved.", ex);
		}
	}

	public static void bindOptions(SelectField field, PropertyDescriptor property, ConfigurationItem model,
			DerivedProperty<? extends Iterable<?>> options, boolean adjustValue) {
		bindOptions(field, options.getValue(model), adjustValue);
	}

	public static SelectField bindOptions(SelectField select, Value<? extends Iterable<?>> optionValue,
			boolean adjustValue) {
		bindListener(optionValue, new OptionBinding(select, optionValue, adjustValue));
		return select;
	}

	public static FormMember bindImage(CommandField member, final Value<ThemeImage> image) {
		bindListener(image, new ImageBinding(member, image));
		return member;
	}

	public static FormMember bindNotExecutableImage(CommandField member, Value<ThemeImage> image) {
		bindListener(image, new NotExecutableImageBinding(member, image));
		return member;
	}

	public static FormMember bindMode(FormMember member, final Value<FieldMode> visibility,
			PropertyDescriptor property, ConfigurationItem item) {
		bindListener(visibility, new FieldModeBinding(member, visibility, property, item));
		return member;
	}

	public static FormMember bindImmutable(FormMember member, final Value<Boolean> immutability) {
		bindListener(immutability, new ImmutabilityBinding(member, immutability));
		return member;
	}

	public static FormMember bindVisible(FormMember member, final Value<Boolean> visibility) {
		bindListener(visibility, new VisibilityBinding(member, visibility));
		return member;
	}

	public static FormMember bindDisabled(FormMember member, final Value<Boolean> disabled) {
		bindListener(disabled, new DisabledBinding(member, disabled));
		return member;
	}

	public static FormMember bindMandatory(FormMember member, final Value<Boolean> value) {
		bindListener(value, new MandatoryBinding(member, value));
		return member;
	}

	private static ListenerBinding bindListener(final Value<?> value, Listener listener) {
		ListenerBinding binding = value.addListener(listener);

		// Update initial value.
		listener.handleChange(value);

		return binding;
	}

	/**
	 * Dynamic value representing {@link FormMember#isVisible()} of the given {@link FormMember}.
	 */
	public static Value<Boolean> isVisible(FormMember member) {
		return new MemberVisible(member);
	}

	/**
	 * Dynamic value representing {@link FormMember#isDisabled()} of the given {@link FormMember}.
	 */
	public static Value<Boolean> isDisabled(FormMember member) {
		return new MemberDisabled(member);
	}

	/**
	 * Dynamic value representing {@link FormMember#isImmutable()} of the given {@link FormMember}.
	 */
	public static Value<Boolean> isImmutable(FormMember member) {
		return new MemberImmutable(member);
	}

	public static FormField onValue(FormField field, SwitchBlock... caseBlocks) {
		final Map<Object, Case> indexedCases = new HashMap<>();
		Otherwise foundOtherwise = null;
		for (SwitchBlock switchBlock : caseBlocks) {
			if (switchBlock instanceof Case) {
				Case caseBlock = (Case) switchBlock;
				indexedCases.put(normalizeValue(field, caseBlock.getTestValue()), caseBlock);
			} else {
				if (foundOtherwise != null) {
					throw new IllegalArgumentException("More than one otherwise block.");
				}
				foundOtherwise = (Otherwise) switchBlock;
			}
		}
		final Otherwise otherwise = foundOtherwise;
		ValueListener listener = new OnValue(indexedCases, otherwise);
		field.addValueListener(listener);
	
		listener.valueChanged(field, null, field.getValue());

		return field;
	}

	public static SwitchBlock caseValue(Object testValue, Runnable... actions) {
		return new Case(testValue, actions);
	}

	public static SwitchBlock otherwise(Runnable... actions) {
		return new Otherwise(actions);
	}

	public static <F extends FormField> F mandatory(F field) {
		field.setMandatory(true);
		return field;
	}

	public static <F extends FormMember> F invisible(F field) {
		field.setVisible(false);
		return field;
	}

	public static <F extends FormMember> F immutable(F field) {
		field.setImmutable(true);
		return field;
	}

	public static <F extends FormField> F checked(F checkbox) {
		checkbox.initializeField(true);
		return checkbox;
	}

	public static Runnable show(final FormMember... members) {
		return Fields.setVisibility(true, members);
	}

	public static Runnable hide(final FormMember... members) {
		return Fields.setVisibility(false, members);
	}

	public static Runnable setVisibility(final boolean visible, final FormMember... members) {
		return new VisibilityUpdate(members, visible);
	}

	/**
	 * Creates a {@link FormGroup} for the given property.
	 */
	public static FormGroup group(FormContainer parent, AnnotationCustomizations annotationCustomizations,
			PropertyDescriptor property) {
		FormGroup group = group(parent, property.getPropertyName());
		boolean displayMinimized = displayMinimized(annotationCustomizations, property);
		group.setCollapsed(displayMinimized);
		return group;
	}
	/**
	 * Creates a {@link FormGroup} into the {@link FormContainer}.
	 */
	public static FormGroup group(FormContainer parent, String name) {
		FormGroup result = new FormGroup(name, new NestedResourceView(parent.getResources(), name));
		parent.addMember(result);
		return result;
	}

	public static BooleanField checkbox(FormContainer parent, String name) {
		BooleanField field = FormFactory.newBooleanField(name);
		parent.addMember(field);
		return field;
	}

	public static FormField doubleField(FormContainer parent, String name) {
		FormField field = FormFactory.newDoubleField(name, 0d, false);
		parent.addMember(field);
		return field;
	}

	public static FormField byteField(FormContainer parent, String name) {
		FormField field = longField(parent, name);
		field.addConstraint(new LongPrimitiveRangeConstraint(Byte.MIN_VALUE, Byte.MAX_VALUE, true));
		return field;
	}

	public static FormField shortField(FormContainer parent, String name) {
		FormField field = longField(parent, name);
		field.addConstraint(new LongPrimitiveRangeConstraint(Short.MIN_VALUE, Short.MAX_VALUE, true));
		return field;
	}

	public static FormField intField(FormContainer parent, String name) {
		FormField field = longField(parent, name);
		field.addConstraint(new LongPrimitiveRangeConstraint(Integer.MIN_VALUE, Integer.MAX_VALUE, true));
		return field;
	}

	public static FormField longField(FormContainer parent, String name) {
		FormField field = FormFactory.newLongField(name, 0L, false);
		parent.addMember(field);
		return field;
	}

	public static ComplexField calendar(FormContainer parent, String name) {
		ComplexField field = FormFactory.newDateField(name, null, false);
		parent.addMember(field);
		return field;
	}

	public static ComplexField complex(FormContainer parent, String name, Format format) {
		ComplexField field = FormFactory.newComplexField(name, format);
		parent.addMember(field);
		return field;
	}

	public static StringField line(FormContainer parent, String name) {
		StringField field = FormFactory.newStringField(name);
		parent.addMember(field);
		return field;
	}

	public static StringField text(FormContainer parent, String name) {
		StringField field = FormFactory.newStringField(name);
		field.setControlProvider(MultiLineText.INSTANCE);
		parent.addMember(field);
		return field;
	}

	public static SelectField selectField(FormContainer parent, String fieldName) {
		SelectField field = FormFactory.newSelectField(fieldName, Collections.emptyList());
		parent.addMember(field);
		return field;
	}

	public static SelectField selectField(FormContainer parent, String fieldName, boolean multiple, boolean immutable) {
		SelectField field = FormFactory.newSelectField(fieldName, Collections.emptyList(), multiple, immutable);
		parent.addMember(field);
		return field;
	}

	public static SelectField selection(SelectField select, Object selection) {
		return selections(select, Collections.singletonList(selection));
	}

	public static SelectField selections(SelectField select, List<?> selections) {
		select.setAsSelection(selections);
		return select;
	}

	public static SelectField options(SelectField select, Collection<?> options) {
		return options(select, options, MetaResourceProvider.INSTANCE);
	}

	public static SelectField options(SelectField select, Collection<?> options, LabelProvider labels) {
		return options(select, options, labels, LabelComparator.newCachingInstance(labels));
	}

	public static SelectField options(SelectField select, Collection<?> options, Comparator<?> order) {
		return options(select, options, MetaResourceProvider.INSTANCE, order);
	}

	public static SelectField options(SelectField select, Collection<?> options, LabelProvider labels,
			Comparator<?> order) {
		List<Object> optionList = new ArrayList<>(options);
		select.setOptions(optionList);
		select.setOptionLabelProvider(labels);
		select.setOptionComparator(order);
		return select;
	}

	public static SelectField multiSelectField(FormContainer parent, String fieldName, List options) {
		SelectField result = multiSelectField(parent, fieldName);
		result.setOptions(options);
		return result;
	}

	public static SelectField multiSelectField(FormContainer parent, String fieldName) {
		SelectField field = FormFactory.newSelectField(fieldName, Collections.emptyList(), true, false);
		field.setControlProvider(SelectionControlProvider.SELECTION_INSTANCE);
		parent.addMember(field);
		return field;
	}

	public static SelectField enumField(FormContainer parent, String fieldName, Class<? extends Enum<?>> enumType) {
		SelectField field = FormFactory.newSelectField(fieldName, Arrays.asList(enumType.getEnumConstants()), false,
			Collections.singletonList(enumType.getEnumConstants()[0]), false);
		field.setOptionLabelProvider(EnumResourceProvider.INSTANCE);
		parent.addMember(field);
		return field;
	}

	public static CommandField button(FormContainer parent, String name, ThemeImage image, final Command command) {
		CommandField result = FormFactory.newCommandField(name, command);
		result.setImage(image);
		parent.addMember(result);
		return result;
	}

	public static CommandField button(FormContainer parent, String name, CommandHandler command,
			LayoutComponent component, Map<String, Object> arguments) {
		CommandField result = FormFactory.newCommandField(name, command, component, arguments);
		parent.addMember(result);
		return result;
	}

	public static TableField table(FormContainer parent, String name) {
		TableField result = FormFactory.newTableField(name);
		parent.addMember(result);
		return result;
	}

	public static TreeTableField treeTable(FormContainer parent, String name, AbstractTreeTableModel<?> treeModel,
			ConfigKey configKey) {
		TreeTableField treeTableField = FormFactory.newTreeTableField(name, configKey, treeModel);
		parent.addMember(treeTableField);
		return treeTableField;
	}

	public static TreeTableField treeTable(FormContainer parent, String name, AbstractTreeTableModel<?> treeModel) {
		TreeTableField treeTableField = FormFactory.newTreeTableField(name, treeModel);
		parent.addMember(treeTableField);
		return treeTableField;
	}

	public static TableField table(FormContainer parent, String name, TableModel model) {
		TableField result = table(parent, name);
		result.setTableModel(model);
		return result;
	}

	public static TableField table(FormContainer parent, String name, ConfigKey configKey) {
		TableField result = FormFactory.newTableField(name, configKey);
		parent.addMember(result);
		return result;
	}

	public static TableField table(FormContainer parent, String name, TableModel model, ConfigKey configKey) {
		TableField result = table(parent, name, configKey);
		result.setTableModel(model);
		return result;
	}

	public static FormField field(FormContainer parent, String name) {
		FormField result = FormFactory.newDisplayField(name, null);
		parent.addMember(result);
		return result;
	}

	public static FormField value(FormField field, Object value) {
		field.initializeField(value);
		return field;
	}

	public static Object normalizeValue(FormField contextField, Object value) {
		if (contextField instanceof SelectField) {
			SelectField selectField = (SelectField) contextField;
			if (selectField.isMultiple()) {
				if (selectField.hasCustomOrder()) {
					if (value instanceof Collection<?>) {
						if (value instanceof List<?>) {
							return value;
						} else {
							throw new UnsupportedOperationException(
								"Collection of unspecified order in a selection without implicit order.");
						}
					}
					return CollectionUtilShared.singletonOrEmptyList(value);
				} else {
					if (value instanceof Collection<?>) {
						if (value instanceof Set<?>) {
							return value;
						} else {
							return new HashSet<Object>((Collection<?>) value);
						}
					}
					return CollectionUtilShared.singletonOrEmptySet(value);
				}
			} else {
				if (value instanceof Collection<?>) {
					Collection<?> collection = (Collection<?>) value;
					if (collection.isEmpty()) {
						return null;
					}
					if (collection.size() == 1) {
						return collection.iterator().next();
					}
					throw new IllegalArgumentException("A single selection may not contain multiple values.");
				} else {
					return value;
				}
			}
		} else {
			return value;
		}
	}

	public static Object internalizeValue(FormField contextField, Object value) {
		if (contextField instanceof SelectField) {
			SelectField selectField = (SelectField) contextField;
			List<?> newValue = toList(value);
			OptionMapping optionMapping = selectField.get(OPTION_MAPPING);
			if (optionMapping != null) {
				ArrayList<Object> mappedValue = new ArrayList<>(newValue.size());
				for (Object entry : newValue) {
					Object option = optionMapping.asOption(selectField.getOptionModel(), entry);
					if (option == null) {
						continue;
					}
					mappedValue.add(option);
				}
				return mappedValue;
			} else {
				return newValue;
			}
		} else {
			return value;
		}
	}

	private static List<?> toList(Object value) {
		if (value instanceof Collection<?>) {
			if (value instanceof List<?>) {
				return (List<?>) value;
			} else {
				return new ArrayList<Object>(((Collection<?>) value));
			}
		}
		return CollectionUtilShared.singletonOrEmptyList(value);
	}

	public static CommandField bindExecutability(CommandField button, final Value<ExecutableState> executability) {
		bindListener(executability, new ExecutabilityBinding(button, executability));
		return button;
	}

	public static Value<ExecutableState> joinExecutability(final Value<? extends ExecutableState>... executabilities) {
		return new ReducedValue<>(executabilities,
			new Reduce<ExecutableState, ExecutableState>() {

			@Override
				public ExecutableState neutral() {
				return ExecutableState.EXECUTABLE;
			}

			@Override
				public ExecutableState combine(ExecutableState current, ExecutableState arg) {
				if (isZero(current)) {
					return current;
				} else {
					return arg;
				}
			}

			@Override
				public boolean isZero(ExecutableState current) {
				return !current.isExecutable();
			}
		});
	}

	public static SelectField setOptionsKeepValue(SelectField select, Iterable<?> newOptions) {
		boolean hadValueBefore = select.hasValue();
		List<?> valueBefore = hadValueBefore ? new ArrayList((List) select.getValue()) : null;
		setOptions(select, newOptions);
		if (valueBefore != null) {
			Set<Object> retained = new HashSet<>();
			Set<?> valueSet = new HashSet<>(valueBefore);
			if (newOptions != null) {
				for (Object option : newOptions) {
					if (valueSet.contains(option)) {
						retained.add(option);
					}
				}
			}
			valueBefore.retainAll(retained);
			select.setValue(valueBefore);
		}

		return select;
	}

	public static void setOptions(SelectField select, Iterable<?> newOptions) {
		if (newOptions instanceof OptionModel) {
			select.setOptionModel((OptionModel<?>) newOptions);
		} else if (newOptions instanceof List<?>) {
			select.setOptions((List<?>) newOptions);
		} else {
			select.setOptions(newOptions == null ? Collections.emptyList() : CollectionUtil.toListIterable(newOptions));
		}
	}

	public static FormContext formContext(LayoutComponent component) {
		return new FormContext(component);
	}

	/**
	 * Appends the given CSS class to the given {@link FormMember}.
	 * 
	 * @param formMember
	 *        Is not allowed to be null.
	 * @param cssClass
	 *        Is not allowed to be null.
	 * @return The given {@link FormMember}, for convenience.
	 */
	public static <T extends FormMember> T appendCssClass(T formMember, String cssClass) {
		requireNonNull(cssClass);
		String oldClasses = formMember.getCssClasses();
		String joinedClasses = CssUtil.joinCssClasses(oldClasses, cssClass);
		formMember.setCssClasses(joinedClasses);
		return formMember;
	}

	/**
	 * The annotated {@link com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType} of
	 * the given {@link PropertyDescriptor}.
	 */
	public static ItemDisplay.ItemDisplayType itemDisplay(AnnotationCustomizations customizations, PropertyDescriptor property) {
		ItemDisplayType result = itemDisplayOrNull(customizations, property);
		if (result == null) {
			return ItemDisplay.ItemDisplayType.POLYMORPHIC;
		}
		return result;
	}

	/**
	 * The explicit value configured through {@link ItemDisplay}, or <code>null</code>, if no
	 * annotation is given.
	 */
	public static ItemDisplay.ItemDisplayType itemDisplayOrNull(AnnotationCustomizations customizations,
			PropertyDescriptor property) {
		ItemDisplay displayAnnotation = customizations.getAnnotation(property, ItemDisplay.class);
		if (displayAnnotation == null) {
			return null;
		}
		return displayAnnotation.value();
	}

	/**
	 * Whether the UI for the given {@link PropertyDescriptor} should be initially displayed in a
	 * minimized state.
	 * 
	 * @see #collapseEntries(AnnotationCustomizations, PropertyDescriptor)
	 */
	public static boolean displayMinimized(AnnotationCustomizations annotationCustomizations,
			PropertyDescriptor property) {
		DisplayMinimized annotation = annotationCustomizations.getAnnotation(property, DisplayMinimized.class);
		if (annotation == null) {
			return false;
		} else {
			return annotation.value();
		}
	}

	/**
	 * Whether the UI for the entries of the given {@link PropertyDescriptor} (of type
	 * {@link PropertyKind#LIST}, {@link PropertyKind#MAP}, or {@link PropertyKind#ARRAY}), should
	 * be initially displayed collapsed.
	 * 
	 * @see #displayMinimized(AnnotationCustomizations,PropertyDescriptor)
	 */
	public static boolean collapseEntries(AnnotationCustomizations annotationCustomizations,
			PropertyDescriptor property) {
		CollapseEntries annotation = annotationCustomizations.getAnnotation(property, CollapseEntries.class);
		if (annotation == null) {
			return false;
		} else {
			return annotation.value();
		}
	}

}

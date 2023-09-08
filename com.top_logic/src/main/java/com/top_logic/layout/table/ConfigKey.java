/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;

/**
 * Factory for personalization keys.
 * 
 * <p>
 * A model stores user-specific personalizations identified by a {@link ConfigKey} in the personal
 * configuration.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfigKey {

	private static final Property<Boolean> PREVENT_PERSONALIZATION_PROPERTY =
		TypedAnnotatable.property(Boolean.class, "preventPersonalization", Boolean.FALSE);

	/**
	 * Creates a {@link ConfigKey}.
	 * 
	 * <p>
	 * Note: Protected to allow instantiation during test.
	 * </p>
	 */
	protected ConfigKey() {
		// Can be created only through static factories.
	}

	/**
	 * A unique attribute name for {@link PersonalConfiguration#setValue(String, Object)} and
	 * {@link PersonalConfiguration#getValue(String)}.
	 */
	public abstract String get();

	/**
	 * No personalization possible.
	 * 
	 * Use {@link DefaultTableData#createAnonymousTableData(TableModel)}.
	 */
	public static ConfigKey none() {
		return None.INSTANCE;
	}

	/**
	 * Creates a {@link ConfigKey} that takes its name from the given {@link FormMember}.
	 * 
	 * @param field
	 *        The source of the qualified name.
	 * @param fieldNameMapping
	 *        The algorithm to compute the field name part of the configuration key.
	 */
	public static ConfigKey field(Mapping<FormMember, String> fieldNameMapping, FormMember field) {
		return new FieldKey(field, fieldNameMapping);
	}

	/**
	 * Creates a {@link ConfigKey} that takes its name from the given {@link FormMember}. Uses
	 * {@link FormMember#QUALIFIED_NAME_MAPPING} as field name mapping.
	 * 
	 * @param field
	 *        The source of the qualified name.
	 */
	public static ConfigKey field(FormMember field) {
		return field(FormMember.QUALIFIED_NAME_MAPPING, field);
	}

	/**
	 * Creates a {@link ConfigKey} that takes its name from the given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        The source of the qualified name.
	 */
	public static ConfigKey component(final LayoutComponent component) {
		if (preventPersonalization(component)) {
			return none();
		}

		if (LayoutConstants.hasSynthesizedName(component)) {
			return none();
		}

		return new ConfigKey() {
			@Override
			public String get() {
				return component.getName().qualifiedName();
			}
		};
	}

	/**
	 * Creates a {@link ConfigKey} that takes its name from the given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        The source of the qualified name.
	 * @param partName
	 *        A name that uniquely distinguishes multiple personalization of the same component.
	 */
	public static ConfigKey part(final LayoutComponent component, final String partName) {
		if (preventPersonalization(component)) {
			return none();
		}

		if (LayoutConstants.hasSynthesizedName(component)) {
			return none();
		}

		return new ConfigKey() {
			@Override
			public String get() {
				return component.getName().qualifiedName() + partName;
			}
		};
	}

	/**
	 * Returns a {@link ConfigKey} that provides the given <code>name</code>.
	 * 
	 * @param name
	 *        The value to return in {@link ConfigKey#get()}. May be <code>null</code>.
	 * @return A {@link ConfigKey} that provides the given <code>name</code>.
	 */
	public static ConfigKey named(String name) {
		if (name == null) {
			return none();
		}
		return new ConfigKey() {

			@Override
			public String get() {
				return name;
			}
		};
	}

	/**
	 * Creates a {@link ConfigKey} derived from a given one.
	 * 
	 * <p>
	 * The result {@link ConfigKey} returns the value of the original key followed by the given
	 * suffix.
	 * </p>
	 * 
	 * <p>
	 * If the original key returns <code>null</code>, also the returned key returns
	 * <code>null</code>.
	 * </p>
	 * 
	 * @param orig
	 *        The {@link ConfigKey} to derive key from.
	 * @param suffix
	 *        Suffix of returned {@link ConfigKey}.
	 */
	public static ConfigKey derived(final ConfigKey orig, final String suffix) {
		if (orig == none()) {
			return orig;
		}
		if (suffix.isEmpty()) {
			return orig;
		}
		return new ConfigKey() {

			@Override
			public String get() {
				String origGet = orig.get();
				if (origGet == null) {
					return null;
				}
				return origGet + suffix;
			}
		};
	}

	/**
	 * Disable personalization relative to the given model.
	 * 
	 * @param model
	 *        The model for which personalization should be disabled.
	 */
	public static void setPreventPersonalization(TypedAnnotatable model) {
		model.set(PREVENT_PERSONALIZATION_PROPERTY, true);
	}

	static boolean preventPersonalization(TypedAnnotatable model) {
		return model.isSet(PREVENT_PERSONALIZATION_PROPERTY);
	}

	private static class FieldKey extends ConfigKey {
		private final Mapping<FormMember, String> _fieldNameMapping;

		private FormMember _field;

		FieldKey(FormMember field, Mapping<FormMember, String> fieldKeyMapping) {
			_field = field;
			_fieldNameMapping = fieldKeyMapping;
		}

		@Override
		public String get() {
			if (preventPersonalization(_field)) {
				return null;
			}

			FormContext formContext = _field.getFormContext();
			if (formContext == null) {
				// The field is no longer attached to a form. This can happen, if a TableField is
				// constructed, added to a form and directly removed from the form. This leaves a
				// TableViewModelEnforcer attached to the context later on trying to load the
				// corresponding TableViewModel. But since the field no longer has a FormContext,
				// resolving the configuration key fails.
				return null;
			}
			FormHandler owner = formContext.getOwningModel();
			if (owner instanceof LayoutComponent) {
				ComponentName componentName = ((LayoutComponent) owner).getName();
				if (LayoutConstants.isSyntheticName(componentName)) {
					return null;
				} else {
					return componentName.qualifiedName() + "." + getFieldName();
				}
			} else {
				return getFieldName();
			}
		}

		private String getFieldName() {
			return _fieldNameMapping.map(_field);
		}
	}

	private static final class None extends ConfigKey {
		/**
		 * Singleton {@link ConfigKey.None} instance.
		 */
		public static final ConfigKey.None INSTANCE = new ConfigKey.None();

		private None() {
			// Singleton constructor.
		}

		@Override
		public String get() {
			return null;
		}
	}

	/**
	 * Creates a {@link ConfigKey} used for layout size persistence, that takes its name from the
	 * given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        The source of the qualified name.
	 */
	public static ConfigKey componentSize(LayoutComponent component) {
		return part(component, "layoutSize");
	}

	/**
	 * Creates a {@link ConfigKey}, e.g. used for dialog size persistence, that takes its name from
	 * the given {@link LayoutComponent}.
	 * 
	 * @param component
	 *        The source of the qualified name.
	 */
	public static ConfigKey windowSize(LayoutComponent component) {
		return part(component, "windowSize");
	}

}
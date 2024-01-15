/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Objects;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.layout.editor.AllComponents;
import com.top_logic.layout.editor.ComponentNameMapping;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.ComponentNameLabelProvider;

/**
 * Name of a {@link LayoutComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(ComponentNameFormat.class)
@ControlProvider(SelectionControlProvider.class)
@Options(fun = AllComponents.class, mapping = ComponentNameMapping.class)
@OptionLabels(ComponentNameLabelProvider.class)
public abstract class ComponentName {

	/** Separator used to separate between the scope and the local name if there is a scope. */
	public static final char SCOPE_SEPARATOR = '#';

	/**
	 * Creates a new {@link ComponentName}.
	 * 
	 * <p>
	 * <b>Note:</b> It is <i>not</i> checked that the parts does not contain
	 * {@link #SCOPE_SEPARATOR}.
	 * </p>
	 * 
	 * @param scope
	 *        The scope of the component name. May be <code>null</code>, which means the name is a
	 *        local name. Must not contain {@link #SCOPE_SEPARATOR}.
	 * @param name
	 *        The local name of the component. Must not be <code>null</code>. Must not contain
	 *        {@link #SCOPE_SEPARATOR}.
	 */
	public static ComponentName newName(String scope, String name) {
		Objects.requireNonNull(name, "Name of a ComponentName must not be null.");
		if (scope == null) {
			return new LocalComponentName(name);
		} else {
			return new QualifiedComponentName(scope, name);
		}
	}

	/**
	 * Creates a new local {@link ComponentName}.
	 * 
	 * <p>
	 * <b>Note:</b> It is <i>not</i> checked that the parts does not contain
	 * {@link #SCOPE_SEPARATOR}.
	 * </p>
	 * 
	 * @param name
	 *        The local name of the component. Must not be <code>null</code>. Must not contain
	 *        {@link #SCOPE_SEPARATOR}.
	 */
	public static ComponentName newName(String name) {
		return newName(null, name);
	}

	/**
	 * Creates a new {@link ComponentName} from a configuration value.
	 * 
	 * @param prop
	 *        The property containing the given component name.
	 * @param name
	 *        The configuration value to create a {@link ComponentName} from.
	 * 
	 * @throws ConfigurationException
	 *         When the given configuration value is not a (syntactical) valid component name.
	 */
	public static ComponentName newConfiguredName(String prop, String name) throws ConfigurationException {
		return ComponentNameFormat.INSTANCE.getValue(prop, name);
	}

	/**
	 * The name of this {@link ComponentName}. Not <code>null</code>.
	 */
	public abstract String qualifiedName();

	/**
	 * The local name of this {@link ComponentName}. Not <code>null</code>.
	 */
	public abstract String localName();

	/**
	 * The scope of this {@link ComponentName}.
	 * 
	 * @return Either the scope of this {@link ComponentName} if is not {@link #isLocalName() a
	 *         local name}, or <code>null</code> when it is a local name.
	 */
	public abstract String scope();

	/**
	 * Whether this {@link ComponentName} is a local, i.e. unqualified, component name.
	 */
	public abstract boolean isLocalName();

	/**
	 * Creates a derived {@link ComponentName} with the given suffix.
	 */
	public ComponentName append(String suffix) {
		if (suffix.isEmpty()) {
			return this;
		}
		return doAppend(suffix);
	}

	/**
	 * Implementation of {@link #append(String)} for a non-empty suffix.
	 */
	protected abstract ComponentName doAppend(String suffix);

	/**
	 * The qualified name of this {@link ComponentName}.
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public final String toString() {
		return qualifiedName();
	}

	private static class LocalComponentName extends ComponentName {
	
		private String _localName;
	
		public LocalComponentName(String localName) {
			_localName = localName;
		}
	
		@Override
		public boolean isLocalName() {
			return true;
		}

		@Override
		public String scope() {
			return null;
		}
	
		@Override
		public String localName() {
			return _localName;
		}
	
		@Override
		public String qualifiedName() {
			return _localName;
		}
	
		@Override
		public int hashCode() {
			return 55799 * _localName.hashCode();
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocalComponentName other = (LocalComponentName) obj;
			if (!_localName.equals(other._localName))
				return false;
			return true;
		}
	
		@Override
		protected ComponentName doAppend(String suffix) {
			return new LocalComponentName(_localName + suffix);
		}
	}

	private static class QualifiedComponentName extends ComponentName {
	
		private String _scope;
	
		private String _localName;
	
		public QualifiedComponentName(String scope, String localName) {
			_scope = scope;
			_localName = localName;
		}
	
		@Override
		public boolean isLocalName() {
			return false;
		}

		@Override
		public String scope() {
			return _scope;
		}
	
		@Override
		public String localName() {
			return _localName;
		}
	
		@Override
		public String qualifiedName() {
			return _scope + SCOPE_SEPARATOR + _localName;
		}
	
		@Override
		public int hashCode() {
			return 8059 * _scope.hashCode() + 8039 * _localName.hashCode();
		}
	
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			QualifiedComponentName other = (QualifiedComponentName) obj;
			if (!_localName.equals(other._localName))
				return false;
			if (!_scope.equals(other._scope))
				return false;
			return true;
		}
	
		@Override
		protected ComponentName doAppend(String suffix) {
			return new QualifiedComponentName(_scope, _localName + suffix);
		}
	}

}



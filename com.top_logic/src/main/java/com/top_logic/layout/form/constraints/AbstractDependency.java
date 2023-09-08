/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.constraints;

import java.util.AbstractList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.listener.AbstractValueListenerDependency;

/**
 * Abstract base class for implementing symmetric dependencies that attach
 * constraints to multiple {@link FormField}s.
 *
 * @see AbstractValueListenerDependency
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractDependency {
	/**
	 * The {@link FormField}s between whose the dependency is established.
	 */
	final FormField[] _relatedFields;
	
	/**
	 * {@link FormField}s that are in the {@link Constraint#reportDependencies() dependencies} of
	 * each concrete constraint but not part of the symmetric dependency relation.
	 */
	private final List<FormField> _externalDependencies;
	
	private final boolean _asWarning;
	
	/**
	 * The constraints that implement this dependency.
	 * 
	 * <p>
	 * The {@link Constraint} at index <code>n</code> is added to the {@link FormField} in
	 * {@link #_relatedFields} at the same index <code>n</code>.
	 * </p>
	 */
	private Constraint[] _constraints;
    
    /**
	 * Creates a new dependency between the given {@link FormField}s.
	 * 
	 * @see #AbstractDependency(FormField[], List, boolean)
	 */
	public AbstractDependency(FormField[] relatedFields) {
	    this(relatedFields, false);
	}
	
    /**
	 * Creates a new dependency between the given {@link FormField}s.
	 * 
	 * @param asWarning
	 *        See {@link #getAsWarning()}.
	 * 
	 * @see #AbstractDependency(FormField[], List, boolean)
	 */
    public AbstractDependency(FormField[] relatedFields, boolean asWarning) {
		this(relatedFields, null, asWarning);
    }
    
    /**
	 * Creates a new dependency between the given {@link FormField}s.
	 * 
	 * @see #AbstractDependency(FormField[], List, boolean)
	 */
	public AbstractDependency(FormField[] relatedFields, List<FormField> additionalFields) {
		this(relatedFields, additionalFields, false);
    }

	/**
	 * Creates an {@link AbstractDependency}.
	 * 
	 * <p>
	 * This dependency only gets active after {@link #attach()} is called.
	 * </p>
	 *
	 * @param relatedFields
	 *        The {@link FormField}s between whose the dependency is established.
	 * @param externalDependencies
	 *        Additional {@link FormField}s to report in {@link Constraint#reportDependencies()} for
	 *        each concrete {@link Constraint} (fields that must have values before any of the
	 *        created {@link Constraint}s is being checked).
	 * @param asWarning
	 *        See {@link #getAsWarning()}
	 */
	public AbstractDependency(FormField[] relatedFields, List<FormField> externalDependencies, boolean asWarning) {
		_relatedFields = relatedFields;
		_externalDependencies = CollectionUtil.nonNull(externalDependencies);
		_asWarning = asWarning;
	}

	/**
	 * Flag whether this constraint is treated as
	 * {@link FormField#addWarningConstraint(com.top_logic.layout.form.Constraint) warning} or
	 * {@link FormField#addConstraint(com.top_logic.layout.form.Constraint) error}.
	 */
	public boolean getAsWarning() {
		return _asWarning;
	}

	/**
	 * The number of {@link FormField}s, which are related by this dependency.
	 */
	public int size() {
		return _relatedFields.length;
	}
	
	/**
	 * The {@link FormField} at the given index.
	 * 
	 * @see #size()
	 */
	public FormField get(int index) {
		return _relatedFields[index];
	}
	
	/**
	 * Finally establishes this dependency by adding the implementing
	 * constraints to the fields.
	 * 
	 * @see #detach() for removing all constraints that implement this
	 *      dependency.
	 */
	public void attach() {
		if (_constraints != null) {
			throw new IllegalStateException("Already attached.");
		}
		
		_constraints = new Constraint[_relatedFields.length];
		for (int n = 0; n < _relatedFields.length; n++) {
			_constraints[n] = new DependencyImplementation(n);
			// if there are additional fields to stablish relations
			// this will be done via a {@link DependencyExtension}
			if (!_externalDependencies.isEmpty()) {
				_constraints[n] = new DependencyExtension(_constraints[n], _externalDependencies);
			}
			if (_asWarning) {
				_relatedFields[n].addWarningConstraint(_constraints[n]);
			} else {
				_relatedFields[n].addConstraint(_constraints[n]);
			}
		}
	}
	
	/**
	 * Removes this dependency from its related fields.
	 * 
	 * @see #attach()
	 */
	public void detach() {
		if (_constraints == null) {
			throw new IllegalStateException("Not yet attached.");
		}

		for (int n = 0; n < _relatedFields.length; n++) {
			if (_asWarning) {
				_relatedFields[n].removeWarningConstraint(_constraints[n]);
		    }
		    else {
				_relatedFields[n].removeConstraint(_constraints[n]);
		    }
		}

		_constraints = null;
	}

    /**
     * Returns whether this dependency is attached.
     * 
     * @return <code>true</code> if this dependency is attached.
     */
    public boolean isAttached() {
		return _constraints != null;
    }

	/**
	 * Implements the check that enforces this dependency.
	 * 
	 * <p>
	 * Checked is the given value that has been set to the {@link FormField} at
	 * the given index of this dependency (The corresponding field can be
	 * retrieved through {@link #get(int) get(checkedFieldIndex}).
	 * </p>
	 * 
	 * @param checkedFieldIndex
	 *     Index (greater or equal to <code>0</code> and lower than
	 *     {@link #size()}) of the {@link FormField} that is currently
	 *     checked.
	 * @param value
	 *     The value that has been set to the corresponding field (see 
	 *     {@link Constraint#check(Object)}).
	 * @return
	 *     See {@link Constraint#check(Object)}
	 * @throws CheckException
	 *     See {@link Constraint#check(Object)}
	 */
	protected abstract boolean check(int checkedFieldIndex, Object value) throws CheckException;
	
	
	/**
	 * A {@link Constraint} implementation that enforces the dependency defined
	 * by its outer class.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	private class DependencyImplementation implements Constraint {
		
		final int checkedFieldIndex;
		
		public DependencyImplementation(int checkedFieldIndex) {
			this.checkedFieldIndex = checkedFieldIndex;
		}

		@Override
		public final boolean check(Object value) throws CheckException {
			return AbstractDependency.this.check(checkedFieldIndex, value);
		}
		
		@Override
		public final Collection<FormField> reportDependencies() {
			return new AbstractList<>() {
				@Override
				public FormField get(int i) {
					int accessedIndex;
					if (i < checkedFieldIndex) {
						accessedIndex = i;
					} else {
						accessedIndex = i + 1;
					}
					
					return _relatedFields[accessedIndex];
				}

				@Override
				public int size() {
					return _relatedFields.length - 1;
				}
			};
		}
	}
}

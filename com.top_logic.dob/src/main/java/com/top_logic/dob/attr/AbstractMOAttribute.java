/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;


import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOPartImpl;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.dob.schema.config.AttributeConfig;

/**
 * Base class for {@link MOAttribute} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMOAttribute extends MOPartImpl implements MOAttribute {

	private int index = -1;
	private MOStructure owner;

	/**
	 * type (which may be a simple or complex MetaObject) of this attribute.
	 */
	private MetaObject type;

	/**
	 * Designates an Attribute that must be set.
	 */
	private boolean isMandatory;
	
	/**
	 * Designates an attribute that must be set during construction.
	 */
	private boolean isInitial;

	/**
	 * Designates an Attribute that can be set only once.
	 */
	private boolean isImmutable;
	private boolean system;

	/** @see #isHidden() */
	private boolean _hidden;

	private MODefaultProvider _defaultProvider;

	/**
	 * Creates a {@link AbstractMOAttribute}.
	 * 
	 * @param name
	 *        See {@link #getName()}
	 * @param type
	 *        See {@link #getMetaObject()}
	 */
	public AbstractMOAttribute(String name, MetaObject type) {
		super(name);
		this.type = type;
	}

	/**
	 * Creates a new {@link AbstractMOAttribute} from given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractMOAttribute}.
	 */
	public AbstractMOAttribute(InstantiationContext context, AttributeConfig config) {
		super(config.getAttributeName());
		setMandatory(config.isMandatory());
		setImmutable(config.isImmutable());
		setInitial(config.isInitialAttribute());
		setHidden(config.isHidden());
		setDefaultProvider(config.getDefaultProvider());
		setSystem(config.isSystem());
	}

	protected final void initFrom(AbstractMOAttribute orig) {
		setMandatory(orig.isMandatory());
		setImmutable(orig.isImmutable());
		setInitial(orig.isInitial());
		setHidden(orig.isHidden());
		setDefaultProvider(orig.getDefaultProvider());
		setSystem(orig.isSystem());
	}
	
	@Override
	public void resolve(TypeContext context) throws DataObjectException {
		if (isFrozen()) {
			// Already resolved.
			return;
		}
		setMetaObject(getMetaObject().resolve(context));
	}
	
	@Override
	public final int getCacheIndex() {
		if (this.owner == null) {
			throw new IllegalStateException("Attribute '" + getName() + "' not yet added to an owner.");
		}
		return this.index;
	}

	@Override
	public MOStructure getOwner() {
		if (owner == null) {
			throw new IllegalStateException("The owner of attribute '" + getName() + "' is not initialized.");
		}
		
		return this.owner;
	}

	@Override
	public void initOwner(MOStructure newOwner, int newIndex) {
		checkUpdate();
		if (this.owner != null) {
			throw new IllegalStateException("This attribute '" + getName() + "' already belongs to type '" + this.owner.getName() + "'.");
		}
		
		if (newOwner == null) {
			throw new IllegalArgumentException("The owner must not be null.");
		}
		
		this.owner = newOwner;
		this.index = newIndex;
	}
	
	@Override
    public final boolean equals (Object other) { // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
    	return super.equals(other);
    }
    
    @Override
    public final int hashCode() {
    	return super.hashCode();
    }


    @Override
	public String toString() {
        return getName() + ": " + this.getMetaObject().getName();
    }

	/**
	 * Returns the type of the return value from this attribute.
	 *
	 * @return a MetaObject describing the type.
	 */
	@Override
	public MetaObject getMetaObject() {
	    return type;
	}
	
	@Override
	public void setMetaObject(MetaObject type) {
		checkUpdate();
		this.type = type;
	}

	/**
	 * Accessor for the Mandatory flag.
	 */
	@Override
	public boolean isMandatory() {
	    return isMandatory;
	}

	@Override
	public void setMandatory(boolean mandatory) {
		checkUpdate();
	    this.isMandatory = mandatory;
	}

	@Override
	public boolean isInitial() {
		return isInitial;
	}

	@Override
	public void setInitial(boolean value) {
		checkUpdate();
		this.isInitial = value;
	}

	/**
	 * Accessor for the Immutable flag.
	 */
	@Override
	public boolean isImmutable() {
	    return isImmutable;
	}

	@Override
	public void setImmutable(boolean immutable) {
		checkUpdate();
	    this.isImmutable = immutable;
	}

	@Override
	public boolean isSystem() {
		return system;
	}

	@Override
	public void setSystem(boolean value) {
		checkUpdate();
		this.system = value;
	}

	@Override
	public boolean isHidden() {
		return _hidden;
	}

	@Override
	public void setHidden(boolean value) {
		checkUpdate();
		_hidden = value;
	}

	@Override
	public MODefaultProvider getDefaultProvider() {
		return _defaultProvider;
	}

	@Override
	public void setDefaultProvider(MODefaultProvider defaultValue) {
		_defaultProvider = defaultValue;
		
	}

}

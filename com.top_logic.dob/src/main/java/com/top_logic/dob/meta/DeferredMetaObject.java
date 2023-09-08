/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.List;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.sql.DBTableMetaObject;

/**
 * The class {@link DeferredMetaObject} is a {@link MetaObject} which serves as
 * holder for the name of some other {@link MetaObject}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeferredMetaObject extends AbstractMetaObject implements MOClass {

	/**
	 * Creates a {@link DeferredMetaObject}.
	 *
	 * @param name See {@link #getName()}.
	 */
	public DeferredMetaObject(String name) {
		super(name);
	}

	@Override
	public Kind getKind() {
		throw new IllegalStateException("Unresolved type reference.");
	}

	@Override
	public MetaObject copy() {
		// No clone required.
		return this;
	}
	
	@Override
	public MetaObject resolve(TypeContext context) throws DataObjectException {
		MetaObject referencedType = context.getType(getName());
		
		return referencedType.resolve(context);
	}

	@Override
	public MOAttribute getAttribute(String attrName) throws NoSuchAttributeException {
		throw errorUnresolved();
	}

	@Override
	public MOAttribute getDeclaredAttributeOrNull(String name) {
		throw errorUnresolved();
	}

	@Override
	public boolean hasDeclaredAttribute(String attrName) {
		throw errorUnresolved();
	}

	@Override
	public MOAttribute getAttributeOrNull(String name) {
		throw errorUnresolved();
	}

	@Override
	public boolean hasAttribute(String attrName) {
		throw errorUnresolved();
	}

	@Override
	public List<MOAttribute> getAttributes() {
		throw errorUnresolved();
	}
	
	@Override
	public List<MOReference> getReferenceAttributes() {
		throw errorUnresolved();
	}

	@Override
	public String[] getAttributeNames() {
		throw errorUnresolved();
	}

	@Override
	public void addAttribute(MOAttribute anAttr) throws DuplicateAttributeException {
		throw errorUnresolved();
	}

	@Override
	public void overrideAttribute(MOAttribute attribute) {
		throw errorUnresolved();
	}

	@Override
	public int getCacheSize() {
		throw errorUnresolved();
	}

	@Override
	public List<MOIndex> getIndexes() {
		throw errorUnresolved();
	}

	@Override
	public MOIndex getPrimaryKey() {
		throw errorUnresolved();
	}

	@Override
	public boolean isAbstract() {
		throw errorUnresolved();
	}

	@Override
	public boolean isFinal() {
		throw errorUnresolved();
	}

	@Override
	public void setAbstract(boolean abstractModifier) {
		throw errorUnresolved();
	}

	@Override
	public void setSuperclass(MOClass aMOClass) {
		throw errorUnresolved();
	}

	@Override
	public MOClass getSuperclass() {
		throw errorUnresolved();
	}

	@Override
	public List<MOAttribute> getDeclaredAttributes() {
		throw errorUnresolved();
	}

	@Override
	public boolean isInherited(MOClass other) {
		throw errorUnresolved();
	}

	@Override
	public boolean isVersioned() {
		throw errorUnresolved();
	}

	@Override
	public void setVersioned(boolean versioned) {
		throw errorUnresolved();
	}

	@Override
	public <T extends MOAnnotation> void addAnnotation(T annotation) {
		throw errorUnresolved();
	}

	@Override
	public <T extends MOAnnotation> T getAnnotation(Class<T> annotationClass) {
		throw errorUnresolved();
	}

	@Override
	public <T extends MOAnnotation> T removeAnnotation(Class<T> annotationClass) {
		throw errorUnresolved();
	}

	@Override
	public DBTableMetaObject getDBMapping() {
		throw errorUnresolved();
	}

	@Override
	public void setAssociation(boolean isAssociation) {
		throw errorUnresolved();
	}

	@Override
	public boolean isAssociation() {
		throw errorUnresolved();
	}

	private IllegalStateException errorUnresolved() {
		return new IllegalStateException("Type '" + getName() + "' not yet resolved.");
	}
	
}


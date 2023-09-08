/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * Declaration of an {@link DBIndex}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeferredIndex extends AbstractMOIndex {

	private List<Pair<String, ReferencePart>> attributeParts = Collections.emptyList();

	/**
	 * Creates a {@link DeferredIndex}.
	 * 
	 * @param name
	 *        See {@link #getName()}.
	 * @param dbName
	 *        See {@link #getDBName()}.
	 * @param unique
	 *        See {@link #isUnique()}.
	 * @param custom
	 *        See {@link #isCustom()}.
	 * @param inMemory
	 *        See {@link #isInMemory()}.
	 * @param compress
	 *        See {@link #getCompress()}.
	 */
	public DeferredIndex(String name, String dbName, boolean unique, boolean custom, boolean inMemory, int compress) {
		super(name, dbName, unique, custom, inMemory, compress);
	}
		
	@Override
	public List<DBAttribute> getKeyAttributes() {
		throw new IllegalStateException("Index not yet resolved.");
	}

	@Override
	public MOIndex copy() {
		return this;
	}
	
	@Override
	public MOIndex resolve(MOStructure owner) {
		List<DBAttribute> attributes = new ArrayList<>();

		for (Pair<String, ReferencePart> attributePart : attributeParts) {
			MOAttribute attribute = getAttribute(owner, attributePart.getFirst());
			ReferencePart part = attributePart.getSecond();
			if (part != null) {
				if (!(attribute instanceof MOReference)) {
					throw new ConfigurationError("Non reference attribute '" + attribute + "' with part '" + part
						+ "' in '" + owner.getName() + "'");
				}
				MOReference reference = (MOReference) attribute;
				DBAttribute column = reference.getColumn(part);

				if (column == null) {
					if (part == ReferencePart.branch && !owner.getDBMapping().multipleBranches()) {
						/* This is ok. It may be that the whole application does not support
						 * branches, whereas the concrete type defines an index with branch
						 * attribute. */
					} else {
						StringBuilder error = new StringBuilder();
						error.append("No column for part '");
						error.append(part);
						error.append("' in reference attribute '");
						error.append(attribute);
						error.append("' in '");
						error.append(owner.getName());
						error.append("'");
						throw new ConfigurationError(error.toString());
					}
				}
				addColumn(attributes, column);
			} else {
				// each database must be included into the index.
				DBAttribute[] dbMapping = attribute.getDbMapping();
				for (DBAttribute column : dbMapping) {
					addColumn(attributes, column);
				}
			}
		}
		try {
			return new MOIndexImpl(getName(), getDBName(), attributes.toArray(new DBAttribute[0]), isUnique(),
				isCustom(), isInMemory(), getCompress());
		} catch (IncompatibleTypeException ex) {
			throw new ConfigurationError("Cannot resolve in context of given owner '" + owner.getName() + "'.", ex);
		}
	}

	private void addColumn(List<? super DBAttribute> attributes, DBAttribute column) {
		if (attributes.contains(column)) {
			throw new ConfigurationError("This indexpart '" + column + "' is already added.");
		}
		attributes.add(column);
	}

	public MOAttribute getAttribute(MOStructure owner, String attributeName) {
		try {
			return owner.getAttribute(attributeName);
		} catch (NoSuchAttributeException ex) {
			throw new ConfigurationError("Cannot resolve '" + this + "'in context of given owner.", ex);
		}
	}

	/**
	 * Sets the name of the attributes and the special aspect of that attribute to include in the
	 * index.
	 * 
	 * <p>
	 * The first part of each entry is the name of the attribute. The second part is either
	 * <code>null</code>, which means that all {@link MOAttribute#getDbMapping() database
	 * attributes} must be included, or the special aspect of the reference to include.
	 * </p>
	 */
	public void setAttributeParts(List<Pair<String, ReferencePart>> attributeParts) {
		this.attributeParts = attributeParts;
	}

	/**
	 * Creates a primary key for the given attributes parts.
	 * 
	 * @see MOIndexImpl#newPrimaryKey(DBAttribute...)
	 */
	public static MOIndex newPrimaryKey(List<Pair<String, ReferencePart>> attributeParts) {
		DeferredIndex primaryKey = new DeferredIndex(MOIndexImpl.PRIMARY_KEY_INDEX_NAME, null, UNIQUE, !DBIndex.CUSTOM,
			!IN_MEMORY, NO_COMPRESS);
		primaryKey.setAttributeParts(attributeParts);
		return primaryKey;
	}

}

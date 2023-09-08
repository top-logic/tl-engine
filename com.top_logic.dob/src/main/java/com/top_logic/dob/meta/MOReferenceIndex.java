/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link MOIndex} for an {@link MOReference}.
 * 
 * <p>
 * The index contains the columns to identify the value of a reference valued attribute, i.e. the
 * branch, identifier, and revision of the target object.
 * </p>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MOReferenceIndex<T extends MOReference> extends AbstractMOIndex {

	/** The {@link MOReference} to create {@link MOIndex} for. */
	protected final T _reference;

	/**
	 * Creates a new {@link MOReferenceIndex}.
	 */
	public MOReferenceIndex(T reference, String name, String dbName, boolean custom,
			boolean inMemory, int compress) {
		super(name, dbName, !UNIQUE, custom, inMemory, compress);
		_reference = reference;
	}

	/**
	 * Creates a new {@link MOReferenceIndex} for the given {@link MOReference}.
	 */
	public MOReferenceIndex(T reference) {
		this(reference, reference.getName(), null, CUSTOM, !IN_MEMORY, NO_COMPRESS);
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
		List<DBAttribute> indexAttributes = new ArrayList<>();
		DBAttribute branchColumn = _reference.getColumn(ReferencePart.branch);
		if (branchColumn != null) {
			indexAttributes.add(branchColumn);
		} else {
			addBranchColumn(owner, indexAttributes);
		}
		indexAttributes.add(_reference.getColumn(ReferencePart.name));
		DBAttribute revColumn = _reference.getColumn(ReferencePart.revision);
		if (revColumn != null) {
			indexAttributes.add(revColumn);
		} else {
			addRevisionColumn(owner, indexAttributes);
		}
		try {
			DBAttribute[] indexColumns = indexAttributes.toArray(new DBAttribute[indexAttributes.size()]);
			return new MOIndexImpl(getName(), getDBName(), indexColumns, isUnique(), isCustom(), isInMemory(),
				getCompress());
		} catch (IncompatibleTypeException ex) {
			throw new UnreachableAssertion("Index is not unique.");
		}
	}

	/**
	 * Adds the columns to identify the revision of the source object.
	 * 
	 * <p>
	 * This method is called when the target object does not have a own revision column, e.g. in
	 * case of a current reference in which the target object is found in the same revision as the
	 * source object.
	 * </p>
	 * 
	 * @param owner
	 *        The owner type of the {@link MOReference}.
	 * @param indexAttributes
	 *        The attributes to add index attributes to.
	 */
	protected abstract void addRevisionColumn(MOStructure owner, List<DBAttribute> indexAttributes);

	/**
	 * Adds the columns to identify the branch of the source object.
	 * 
	 * <p>
	 * This method is called when the target object does not have a own branch column, e.g. in case
	 * of a branch local reference in which the target object is found in the same branch as the
	 * source object.
	 * </p>
	 * 
	 * @param owner
	 *        The owner type of the {@link MOReference}.
	 * @param indexAttributes
	 *        The attributes to add index attributes to.
	 */
	protected abstract void addBranchColumn(MOStructure owner, List<DBAttribute> indexAttributes);

}


/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.meta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.ex.IncompatibleTypeException;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;

/**
 * Straightforward implementation of an MOIndex.
 *
 * @author  Klaus Halfmann
 */
public class MOIndexImpl extends AbstractMOIndex {

	static final String PRIMARY_KEY_INDEX_NAME = "primary";

	/** The Attributes that make up the index key */
    private List<DBAttribute> columns;

    /**
     * Create a new MOIndexImpl with given name(s), Attributes and flags
     *
     * @param   aName           the name of the new index.
     * @param   aDBName         a name suiteable as Index-Name for a Database
     * @param   indexColumns            the attributes of the new index.
     * @param   uniqueFlag      true = index will contain only one element per key.
     * @param custom See {@link #isCustom()}.
     * @param   inMemoryFlag    true = index will always be created in Memory
     * @throws IncompatibleTypeException in case the index is unique but uses non mandatory Attributes.
     */
    public MOIndexImpl(String aName, String aDBName, DBAttribute indexColumns[], 
                       boolean uniqueFlag, boolean custom, boolean inMemoryFlag, 
                       int aCompress) throws IncompatibleTypeException  {
    	super(aName, aDBName, uniqueFlag, custom, inMemoryFlag, aCompress);
    	
        assert indexColumns.length > 0 : "An index must have a non-empty list of index columns.";
    	
        columns = Arrays.asList(indexColumns);

        if (uniqueFlag) {
            checkUniqueMandatory();
        }
    }

    /**
     * Create a new MOIndexImpl with given name(s), Attributes and flags
     *
     * @param   aName           the name of the new index.
     * @param   aDBName         a name suiteable as Index-Name for a Database
     * @param   indexColumns            the attributes of the new index.
     * @param   uniqueFlag      true = index will contain only one element per key.
     * @param   inMemoryFlag    true = index will always be created in Memory
     */
    public MOIndexImpl (String aName, String aDBName, DBAttribute indexColumns[], 
                        boolean uniqueFlag, boolean inMemoryFlag, int compress) throws IncompatibleTypeException  {
		this(aName, null, indexColumns, uniqueFlag, !DBIndex.CUSTOM, inMemoryFlag, compress);
    }

    /**
     * Create a new MOIndexImpl with given name and Attributes.
     *
     * @param   aName       the name of the new index.
     * @param   indexColumns        the attributes of the new index.
     * @param   uniqueFlag  true = index will contain only one element per key.
     */
    public MOIndexImpl (String aName, DBAttribute indexColumns[], 
                        boolean uniqueFlag, boolean inMemoryFlag, int compress) throws IncompatibleTypeException  {
       this(aName, null, indexColumns, uniqueFlag, inMemoryFlag, compress);
    }

    /**
     * Create a new MOIndexImpl with given name,Attributes and uniqeFlag.
     *
     * Such an index will not be held in Memory.
     *
     * @param   aName       the name of the new index.
     * @param   indexColumns        the attributes of the new index.
     * @param   uniqueFlag  true = index will contain only one element per key.
     */
    public MOIndexImpl (String aName, DBAttribute indexColumns[], boolean uniqueFlag, int compress) throws IncompatibleTypeException {
        this(aName, indexColumns, uniqueFlag, !IN_MEMORY, compress);
    }

    /**
     * Create a new unique MOIndexImpl with given name and Attributes.
     *
     * @param   aName       the name of the new index.
     * @param   indexColumns        the attributes of the new index.
     */
    public MOIndexImpl (String aName, DBAttribute indexColumns[]) throws IncompatibleTypeException  {
		this(aName, indexColumns, UNIQUE, NO_COMPRESS);
    }

	/**
	 * Checks that no non mandatory attributes are contained in unique indices.
	 * 
	 * @throws IncompatibleTypeException
	 *         If the constraint is violated.
	 */
    private void checkUniqueMandatory() throws IncompatibleTypeException {
        
        for (DBAttribute dbAttr : columns) {
			if (!dbAttr.isSQLNotNull()) {
				StringBuilder nullableColumn = new StringBuilder();
				nullableColumn.append("Unique Index '");
				nullableColumn.append(getName());
				nullableColumn.append("' uses nullable database column '");
				nullableColumn.append(dbAttr.getDBName());
				nullableColumn.append("' in attribute '");
				nullableColumn.append(dbAttr.getAttribute().getName());
				nullableColumn.append("'");
				throw new IncompatibleTypeException(nullableColumn.toString());
            }
        }
    }

    @Override
	public List<DBAttribute> getKeyAttributes() {
    	return columns;
    }

	@Override
	public MOIndex copy() {
		ArrayList<Pair<String, ReferencePart>> attributeParts = new ArrayList<>();
		for (int n = 0, cnt = columns.size(); n < cnt; n++) {
			DBAttribute dbAttribute = columns.get(n);
			MOAttribute attribute = dbAttribute.getAttribute();
			String attributeName = attribute.getName();
			ReferencePart part;
			if (attribute instanceof MOReference) {
				part = ((MOReference) attribute).getPart(dbAttribute);
			} else {
				part = null;
			}
			attributeParts.add(new Pair<>(attributeName, part));
		}
		DeferredIndex result = new DeferredIndex(getName(), getDBName(), isUnique(), isCustom(), isInMemory(), getCompress());
		result.setAttributeParts(attributeParts);
		return result;
	}
	
	@Override
	public MOIndex resolve(MOStructure owner) {
		return this;
	}
	
	@Override
	public String toString() {
	    List<DBAttribute> attributes = getKeyAttributes();
	    int          size   = attributes.size();
		StringBuffer result = new StringBuffer(); 
		
		result.append('[');
		if (size > 0) {
            result.append(attributes.get(0).getDBName());
            for (int i=1; i < size; i++) {
                result.append(", ");
                result.append(attributes.get(i).getDBName());
            }
		}
		result.append(']');
		
		return result.toString();
	}

	/**
	 * Creates a primary key for the given {@link DBAttribute attributes}.
	 * 
	 * @see DeferredIndex#newPrimaryKey(List)
	 */
	public static MOIndex newPrimaryKey(DBAttribute... attributes) {
		try {
			return new MOIndexImpl(MOIndexImpl.PRIMARY_KEY_INDEX_NAME, null, attributes, UNIQUE, !DBIndex.CUSTOM,
				!IN_MEMORY, NO_COMPRESS);
		} catch (IncompatibleTypeException ex) {
			throw new IllegalArgumentException("Unable to create primary key from attributes: "
				+ Arrays.toString(attributes), ex);
		}
	}

}

/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.data;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.TLID;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.MORepository;

/**
 * Class adding identifier storing and initialization aspect to
 * {@link DataObjectImpl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultDataObject extends DataObjectImpl {

	private ObjectKey identifier;

	public DefaultDataObject(MetaObject anObject, Object[] storage) {
		super(anObject, storage);
		
        init();
	}

	public DefaultDataObject(MetaObject anObject) {
		super(anObject);

        init();
	}

	private void init() {
		setIdentifier(DummyIDFactory.createId());
	}
	
    @Override
	public final TLID getIdentifier () {
		return this.identifier.getObjectName();
    }

    @Override
	public final void setIdentifier (TLID anIdentifier) {
		Long trunkID = Long.valueOf(1); // TLContext.TRUNK_ID
		long currentContext = Long.MAX_VALUE; // Revision.CURRENT_REV
		this.identifier = new DefaultObjectKey(trunkID, currentContext, tTable(), anIdentifier);
    }
    
	@Override
	public MORepository getTypeRepository() {
		throw new UnsupportedOperationException("This DataObject-Implementation does not have a type repository");
	}
	
	/**
	 * TODO #2829: Delete TL 6 deprecation.
	 * 
	 * @deprecated Use {@link #tId()} instead
	 */
	@Deprecated
	@Override
	public final ObjectKey getObjectKey() {
		return tId();
	}

	@Override
	public ObjectKey tId() {
		return identifier;
	}

	@Override
	public IdentifiedObject resolveObject(ObjectKey objectKey) {
		throw new UnsupportedOperationException("This DataObject-Implementation can not resolve object keys");
	}

}

/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.dummy;

import java.util.Iterator;

import com.top_logic.basic.DummyIDFactory;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.data.AbstractDataObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.AbstractKnowledgeItem;
import com.top_logic.model.TLObject;
import com.top_logic.util.TLContext;

/**
 * The DummyKnowledgeObject a dummy knowledge object for testing.
 * 
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class DummyKnowledgeObject extends AbstractKnowledgeItem implements KnowledgeObject {

	/**
	 * Dummy type. It refers to an (hopefully) non existing type, but is necessary to create
	 * {@link DefaultObjectKey}.
	 */
	private static final MOClass TYPE = new MOClassImpl("notExistingType");

	public static DummyKnowledgeObject item() {
		return item(DummyIDFactory.createId(), TYPE);
	}

	public static DummyKnowledgeObject item(String id) {
		return item(StringID.valueOf(id), TYPE);
	}

	public static DummyKnowledgeObject item(TLID id) {
		return item(id, TYPE);
	}

	public static DummyKnowledgeObject item(String id, MOClass type) {
		return item(StringID.valueOf(id), type);
	}

	public static DummyKnowledgeObject item(TLID id, MOClass type) {
		return new DummyKnowledgeObject(id, type);
	}

	private DefaultObjectKey objectKey;

	private final Object[] storage;

	private TLObject _wrapper;

	private boolean _alive = true;

	/**
	 * @param id
	 *        the ID of the {@link KnowledgeObject}. must not be <code>null</code>
	 * @param type
	 *        type of the {@link KnowledgeObject}. must not be <code>null</code>
	 */
	private DummyKnowledgeObject(TLID id, MOClass type) {
		super(type);
		this.objectKey = new DefaultObjectKey(TLContext.TRUNK_ID, Revision.CURRENT.getCommitNumber(), type, id);
		this.storage = AbstractDataObject.newStorage(type);
	}

	@Override
	public MOClass tTable() {
		return (MOClass) super.tTable();
	}

	@Override
	public long getCreateCommitNumber() {
		return 1;
	}

	@Override
	public long getLastUpdate() {
		return 1;
	}

	/**
	 * Returns the internal storage of this {@link DummyKnowledgeObject}.
	 */
	public Object[] getStorage() {
		return storage;
	}

	@Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations() {
        return null;
    }

    @Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations(String anAssociationType) {
        return null;
    }

    @Override
	public Iterator<KnowledgeAssociation> getOutgoingAssociations(String anAssociationType,
			KnowledgeObject aKnowledgeObject) {
        return null;
    }

    @Override
	public Iterator<KnowledgeAssociation> getIncomingAssociations() {
        return null;
    }

    @Override
	public Iterator<KnowledgeAssociation> getIncomingAssociations(String anAssociationType) {
        return null;
    }
    
    @Override
	public boolean isAlive() {
		return _alive;
    }

	@Override
	public State getState() {
		return State.PERSISTENT;
	}

    @Override
	public KnowledgeBase getKnowledgeBase() {
		return DummyKnowledgeBase.INSTANCE;
    }

    @Override
	public void delete() throws DataObjectException {
		_alive = false;
    }

    @Override
	public void touch() {
		throw new IllegalStateException("Dummy objects can not be locked.");
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
		return objectKey;
    }

	@Override
	public FlexDataManager getFlexDataManager() {
		return null;
	}
	
	@Override
	public TLObject getWrapper() {
		return _wrapper;
	}

	public void setWrapper(TLObject wrapper) {
		_wrapper = wrapper;
	}

	@Override
	public ObjectKey getKnownKey(ObjectKey key) {
		return key;
	}

	@Override
	public Object getGlobalAttributeValue(String attribute) throws NoSuchAttributeException {
		return getAttributeValue(attribute);
	}

	@Override
	public Object getValue(MOAttribute attribute) {
		return attribute.getStorage().getApplicationValue(attribute, this, this, storage);
	}

	@Override
	public Object setValue(MOAttribute attribute, Object newValue) throws DataObjectException {
		AttributeStorage attributeStorage = attribute.getStorage();
		attributeStorage.checkAttributeValue(attribute, this, newValue);
		return attributeStorage.setApplicationValue(attribute, this, this, storage, newValue);
	}

}
/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.dummy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.TLID;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Base class for dummy {@link Wrapper} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DummyWrapper implements Wrapper {

	public static DummyWrapper obj() {
		return new DummyWrapper(DummyKnowledgeObject.item());
	}

	public static DummyWrapper obj(String id) {
		return new DummyWrapper(DummyKnowledgeObject.item(id));
	}

	public static DummyWrapper obj(TLID id) {
		return new DummyWrapper(DummyKnowledgeObject.item(id));
	}

	public static DummyWrapper obj(TLID id, MOClass type) {
		return new DummyWrapper(DummyKnowledgeObject.item(id, type));
	}

	public static DummyWrapper obj(String id, MOClass type) {
		return new DummyWrapper(DummyKnowledgeObject.item(id, type));
	}

	private final DummyKnowledgeObject wrappedObject;

	private Map<String, Object> _values = new HashMap<>();

	protected DummyWrapper(DummyKnowledgeObject item) {
		this.wrappedObject = item;
		item.setWrapper(this);
	}

	@Override
	public KnowledgeObject tHandle() {
		return wrappedObject;
	}

	@Override
	public TLStructuredType tType() {
		return null;
	}

	@Override
	public Object tValue(TLStructuredTypePart part) {
		return wrappedObject.getAttributeValue(part.getName());
	}

	@Override
	public void tUpdate(TLStructuredTypePart part, Object value) {
		wrappedObject.setAttributeValue(part.getName(), value);
	}

	@Override
	public void tDelete() {
		// Ignore.
	}

	@Override
	public KnowledgeBase getKnowledgeBase() {
		return DummyKnowledgeBase.INSTANCE;
	}

	@Override
	public Long getLastModified() {
		return null;
	}

	@Override
	public MOStructure tTable() {
		return wrappedObject.tTable();
	}

	@Override
	public Date getModified() {
		return null;
	}

	@Override
	public String getName() throws WrapperRuntimeException {
		return (String) getValue(AbstractWrapper.NAME_ATTRIBUTE);
	}

	/**
	 * Setter for {@link #getName()}
	 */
	@Override
	public void setName(String name) {
		setValue(AbstractWrapper.NAME_ATTRIBUTE, name);
	}

	@Override
	public Object getValue(String attributeName) {
		return tValueByName(attributeName);
	}

	@Override
	public Object tValueByName(String partName) {
		return _values.get(partName);
	}
	
	/**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @deprecated Use {@link #tValid()} instead
	 */
	@Deprecated
	@Override
	public final boolean isValid() {
		return tValid();
	}

	@Override
	public boolean tValid() {
		return true;
	}

	@Override
	public void setValue(String attributeName, Object value) {
		tUpdateByName(attributeName, value);
	}

	@Override
	public void tUpdateByName(String partName, Object value) {
		_values.put(partName, value);
	}

	@Override
	public int compareTo(Wrapper w) {
        if (this == w) {
            return 0;
        }
        if (!this.tValid()) {
            if (!w.tValid()) {
                return 0;
            }
            return 1;
        }
        if (!w.tValid()) {
            return -1;
        }
        
        String  theOtherName    = w.getName();
        String  theName         = getName();
        
        int result = 0;
        if (theName != null) {
            if (theOtherName != null) {
                result = theName.compareTo(theOtherName);
            } else { // theOtherName == null 
                return  -1;
            }
        } else { // theName == null
            if (theOtherName != null) {
                return 1;
            } // else { // both are null
        }
        if (result == 0) { // Not the same wrapper, but the same name ?
            // Use ObjectKey as stable order 
            result = KBUtils.getObjectKeyString(wrappedObject)
                .compareTo(KBUtils.getObjectKeyString(w.tHandle()));
        }
        return result;
	}

	@Override
	public Date getCreated() {
		//  TODO BHU Automatically created
		if (true) throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Person getCreator() {
		//  TODO BHU Automatically created
		if (true) throw new UnsupportedOperationException();
		return null;
	}

	@Override
	public Person getModifier() {
		//  TODO BHU Automatically created
		if (true) throw new UnsupportedOperationException();
		return null;
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
	public final ObjectKey tId() {
		return tHandle().tId();
	}
}

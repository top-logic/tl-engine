/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.ChangeAware;
import com.top_logic.element.meta.ValidityCheck;
import com.top_logic.element.model.PersistentScope;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.searching.FullTextBuBuffer;
import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.AssociationSetQuery;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * Static plug-in implementation of {@link TLObject} functionality.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class PersistentObjectImpl {

	private static final String ATTRIBUTE_SUFFIX_LAST_CHANGED = "_last_changed";

	private static final AssociationSetQuery<KnowledgeAssociation> COMPOSITION_LINKS =
		AssociationQuery.createIncomingQuery("containerLinks", ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION);

	public static void addMetaElement(TLObject self, TLClass aMetaElement) {
		PersistentScope.addMetaElement(self, aMetaElement);
	}

	public static void setMetaElement(AbstractWrapper self, TLClass aMetaElement) {
		AbstractWrapper.setReference(self, PersistentObject.TYPE_REF, aMetaElement);
	}

	public static TLClass tType(TLObject self) {
		return self.tGetDataReference(TLClass.class, PersistentObject.TYPE_REF);
	}

	/**
	 * Implementation of {@link TLObject#tContainer()}.
	 */
	public static TLObject tContainer(TLObject self) {
		KnowledgeAssociation link = tContainerLink(self);
		if (link == null) {
			return null;
		}
		return link.getSourceObject().getWrapper();
	}

	/**
	 * Implementation of {@link TLObject#tContainerReference()}.
	 */
	public static TLReference tContainerReference(TLObject self) {
		KnowledgeAssociation link = tContainerLink(self);
		if (link == null) {
			return null;
		}
		return ((KnowledgeItem) link.getAttributeValue(WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR)).getWrapper();
	}

	private static KnowledgeAssociation tContainerLink(TLObject self) {
		Set<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(self, COMPOSITION_LINKS);
		if (links.isEmpty()) {
			return null;
		}
		Iterator<KnowledgeAssociation> iterator = links.iterator();
		KnowledgeAssociation link = iterator.next();
		if (iterator.hasNext()) {
			throw new IllegalStateException("Object '" + self + "' is part of multiple containers.");
		}
		return link;
	}

	public static void removeMetaElement(TLObject self, TLClass aMetaElement) {
		PersistentScope.removeMetaElement(self, aMetaElement);
	}

	public static TLClass getMetaElement(TLObject self, String aMetaElementType) {
		return PersistentScope.getMetaElement(self, aMetaElementType);
	}

	public static Set<TLClass> getMetaElements(TLObject self) {
		return PersistentScope.getMetaElements(self);
	}

	/**
	 * Add a value to a collection-valued attribute
	 * 
	 * @param aKey
	 *        the attribute name
	 * @param aValue
	 *        the value
	 */
	public static void addValue(TLObject object, String aKey, Object aValue) {
        try{
			TLStructuredTypePart attribute = object.tType().getPart(aKey);
            if (AttributeOperations.isCollectionValued(attribute)) {
				AttributeOperations.addAttributeValue(object, attribute, aValue);
            }
            else{
				throw new IllegalStateException("Attribute is not collection-valued: " + attribute);
            } 
        }
        catch (NoSuchAttributeException e) {
			throw new IllegalStateException(aKey + " is not an attribute of " + object);
        }
        catch (Exception ex) {
            String message = "Problem adding attribute "+aValue+" to "+aKey;
			Logger.error(message, ex, PersistentObjectImpl.class);
            throw new IllegalStateException(message, ex);
        }  
    }


    /**
	 * Integrates MetaAttributes. Falls back to FlexWrapper mechanism if there is no attribute of
	 * the given name.
	 */
	public static Object getValue(TLObject self, String anAttribute) {
		TLStructuredType type = self.tType();
		if (type == null) {
			return self.tGetData(anAttribute);
        }
        
		TLStructuredTypePart part = type.getPart(anAttribute);
		if (part == null) {
			return self.tGetData(anAttribute);
		}
        
		return getValue(self, part);
    }

	public static Object getValue(TLObject self, TLStructuredTypePart part) {
		return AttributeOperations.getAttributeValue(self, part);
	}

    /**
	 * Remove a value from a collection-valued attribute
	 * 
	 * @param aKey
	 *        the attribute name
	 * @param aValue
	 *        the value
	 */
	public static void removeValue(TLObject self, String aKey, Object aValue) {
        try{
			TLStructuredTypePart attribute = self.tType().getPart(aKey);
            if (AttributeOperations.isCollectionValued(attribute)) {
				AttributeOperations.removeAttributeValue(self, attribute, aValue);
            }
            else {
				throw new IllegalStateException("Attribute is not a collection-valued: " + attribute);
            }
            
        }
        catch (NoSuchAttributeException e) {
			throw new IllegalStateException(aKey + " is not a attribute of " + self);
        }
        catch (Exception ex) {
            String message = "Problem removing attribute "+aValue+" from "+aKey;
			Logger.error(message, ex, PersistentObjectImpl.class);
            throw new IllegalStateException(message, ex);
        }  
    }

    /**
	 * Integrates MetaAttributes. Falls back to FlexWrapper mechanism if there is no attribute of
	 * the given name.
	 */
	public static void setValue(TLObject self, String aKey, Object aValue) {
		notifyPreChange(self, aKey, aValue);
    	
		TLStructuredType type = self.tType();
		if (type == null) {
			self.tSetData(aKey, aValue);
            return;
        }
            
		TLStructuredTypePart part = type.getPart(aKey);
		if (part == null) {
			self.tSetData(aKey, aValue);
            return;
        }
        
		setValue(self, part, aValue);  
    }

	public static void setValue(TLObject self, TLStructuredTypePart attribute, Object value) {
		try {
			if (!AttributeOperations.isCollectionValued(attribute)) {
				AttributeOperations.setAttributeValue(self, attribute, value);
            }
            else {
            	Collection<?> collectionValue;
                if (value == null) {
                    collectionValue = Collections.EMPTY_LIST;
                } else if (!(value instanceof Collection)) {
					throw new IllegalArgumentException("Value must be a collection.");
                } else {
                	collectionValue = (Collection<?>) value;
                }

				AttributeOperations.setAttributeValue(self, attribute, collectionValue);
            }

			touch(self, attribute);
		} catch (RuntimeException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SETTING_VALUE__ATTRIBUTE_VALUE.fill(attribute, value), ex);
        }
	}
    
	public static void touch(TLObject self, TLStructuredTypePart part) {
		if (AttributeOperations.getValidityCheck(part).isActive()) {
			String lastChangeProperty = getTouchProperty(part);
			Date lastChangeDate = self.tGetDataDate(lastChangeProperty);
			if (!ValidityCheck.INVALID_DATE.equals(lastChangeDate)) {
				self.tSetData(lastChangeProperty, new Date());
			}
		}
	}

	public static String getTouchProperty(TLStructuredTypePart part) {
		return part.getName() + ATTRIBUTE_SUFFIX_LAST_CHANGED;
	}

    /**
	 * @see ChangeAware#notifyPreChange(String, java.lang.Object)
	 */
	public static void notifyPreChange(TLObject self, String anAttributeName, Object aNewValue) {
		TLObject thePhys = self;
		if (thePhys instanceof ChangeAware) {
			((ChangeAware) thePhys).notifyPreChange(anAttributeName, aNewValue);
    	}
    }

    /**
	 * Produces the full-text representation of the given object.
	 */
	public static void generateFullText(FullTextBuBuffer buffer, TLObject self) {
		TLStructuredType type = self.tType();
		if (type != null) {
			for (TLStructuredTypePart part : type.getAllParts()) {
				if (part.getModelKind() == ModelKind.REFERENCE) {
					TLReference reference = (TLReference) part;

					if (reference.getEnd().isAggregate()) {
						// Do not consider "parent" references.
						continue;
					}

					if (AttributeOperations.isFullTextRelevant(part)) {
						Object value = getValue(self, part);

						// Prevent descending into references. Note: Referenced objects implement
						// FullTextSearchable. When passing FullTextSearchable objects to a
						// FullTextBuffer, its complete contents is analyzed.
						buffer.genericAddLabel(value);
					}
				} else {
					if (AttributeOperations.isFullTextRelevant(part)) {
						Object value = getValue(self, part);

						if (part.getType().getModelKind() == ModelKind.DATATYPE) {
							buffer.genericAddValue(value);
						} else {
							// Do not descend into objects.
							buffer.genericAddLabel(value);
						}
					}
				}
			}
		}
    }
}

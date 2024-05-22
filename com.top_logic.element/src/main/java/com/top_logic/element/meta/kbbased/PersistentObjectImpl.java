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
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.persistency.CompositionStorage.InTargetTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.LinkTable;
import com.top_logic.model.cache.TLModelCacheService;
import com.top_logic.model.cache.TLModelOperations.CompositionStorages;
import com.top_logic.model.cache.TLModelOperations.InSource;
import com.top_logic.util.error.TopLogicException;

/**
 * Static plug-in implementation of {@link TLObject} functionality.
 * 
 * @author <a href="mailto:kbu@top-logic.com">Karsten Buch</a>
 */
public class PersistentObjectImpl {

	private static final String ATTRIBUTE_SUFFIX_LAST_CHANGED = "_last_changed";

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
		TLStructuredType selfType = self.tType();
		if (selfType.getModelKind() != ModelKind.CLASS) {
			return null;
		}
		CompositionStorages compositionStorage =
			TLModelCacheService.getOperations().getCompositionStorages((TLClass) selfType);

		TLObject inTargetContainer = inTargetContainer(self, compositionStorage);
		if (inTargetContainer != null) {
			return inTargetContainer;
		}
		TLObject linkContainer = linkContainer(self, compositionStorage);
		if (linkContainer != null) {
			return linkContainer;
		}
		TLObject inSourceContainer = inSourceContainer(self, compositionStorage);
		if (inSourceContainer != null) {
			return inSourceContainer;
		}
		return null;
	}

	private static TLObject inSourceContainer(TLObject self, CompositionStorages compositionStorage) {
		for (InSource inSource : compositionStorage.storedInSource()) {
			AssociationSetQuery<TLObject> query = inSourceQuery(inSource);
			Set<TLObject> containers = AbstractWrapper.resolveLinks(self, query);
			switch (containers.size()) {
				case 0:
					continue;
				case 1:
					return containers.iterator().next();
				default:
					throw failMultipleContainers(self, containers);
			}
		}
		return null;
	}

	private static AssociationSetQuery<TLObject> inSourceQuery(InSource inSource) {
		String table = inSource.getTable();
		String targetRef = inSource.getPartAttribute();
		return AssociationQuery.createQuery("source for " + table + "." + targetRef, TLObject.class, table, targetRef);
	}

	private static TLObject linkContainer(TLObject self, CompositionStorages compositionStorage) {
		KnowledgeAssociation link = findLink(self, compositionStorage);
		if (link != null) {
			return link.getSourceObject().getWrapper();
		}
		return null;
	}

	private static TLObject inTargetContainer(TLObject self, CompositionStorages compositionStorage) {
		for (InTargetTable inTarget : compositionStorage.storedInTarget()) {
			TLObject container = self.tGetDataReference(TLObject.class, inTarget.getContainer());
			if (container != null) {
				// Container found
				return container;
			}
		}
		return null;
	}

	/**
	 * Implementation of {@link TLObject#tContainerReference()}.
	 */
	public static TLReference tContainerReference(TLObject self) {
		TLStructuredType selfType = self.tType();
		if (selfType.getModelKind() != ModelKind.CLASS) {
			return null;
		}
		CompositionStorages compositionStorage =
			TLModelCacheService.getOperations().getCompositionStorages((TLClass) selfType);

		TLReference inTargetReference = inTargetReference(self, compositionStorage);
		if (inTargetReference != null) {
			return inTargetReference;
		}
		TLReference linkReference = linkReference(self, compositionStorage);
		if (linkReference != null) {
			return linkReference;
		}
		TLReference inSourceReference = inSourceReference(self, compositionStorage);
		if (inSourceReference != null) {
			return inSourceReference;
		}
		return null;
	}

	private static TLReference inSourceReference(TLObject self, CompositionStorages compositionStorage) {
		for (InSource inSource : compositionStorage.storedInSource()) {
			AssociationSetQuery<TLObject> query = inSourceQuery(inSource);
			Set<TLObject> containers = AbstractWrapper.resolveLinks(self, query);
			switch (containers.size()) {
				case 0:
					continue;
				case 1:
					TLObject container = containers.iterator().next();
					return (TLReference) container.tType().getPartOrFail(inSource.getReferenceName());
				default:
					throw failMultipleContainers(self, containers);
			}
		}
		return null;
	}

	private static TLReference linkReference(TLObject self, CompositionStorages compositionStorage) {
		KnowledgeAssociation link = findLink(self, compositionStorage);
		if (link != null) {
			TLObject container = link.getSourceObject().getWrapper();
			return findConcreteReference(container,
				link.tGetDataReference(TLReference.class, WrapperMetaAttributeUtil.META_ATTRIBUTE_ATTR));
		}
		return null;
	}

	private static TLReference inTargetReference(TLObject self, CompositionStorages compositionStorage) {
		for (InTargetTable inTarget : compositionStorage.storedInTarget()) {
			TLReference compositeRef = self.tGetDataReference(TLReference.class, inTarget.getContainerReference());
			if (compositeRef != null) {
				// Container found
				TLObject container = self.tGetDataReference(TLObject.class, inTarget.getContainer());
				return findConcreteReference(container, compositeRef);
			}
		}
		return null;
	}

	private static TLReference findConcreteReference(TLObject self, TLReference ref) {
		return (TLReference) self.tType().getPartOrFail(ref.getName());
	}

	private static KnowledgeAssociation findLink(TLObject self, CompositionStorages storages) {
		Set<KnowledgeAssociation> links = findLinks(self, storages);
		if (links.isEmpty()) {
			return null;
		}
		Iterator<KnowledgeAssociation> iterator = links.iterator();
		KnowledgeAssociation link = iterator.next();
		if (iterator.hasNext()) {
			List<TLObject> allContainers = links.stream()
				.map(KnowledgeAssociation::getSourceObject)
				.map(KnowledgeItem::<TLObject> getWrapper)
				.collect(Collectors.toList());
			throw failMultipleContainers(self, allContainers);
		}
		return link;
	}

	private static IllegalStateException failMultipleContainers(TLObject self,
			Collection<? extends TLObject> allContainers) {
		return new IllegalStateException("Object '" + self + "' is part of multiple containers: " + allContainers);
	}

	private static Set<KnowledgeAssociation> findLinks(TLObject self, CompositionStorages storages) {
		for (LinkTable linkTable : storages.storedInLink()) {
			String tableName = linkTable.getTable();
			AssociationSetQuery<KnowledgeAssociation> query =
				AssociationQuery.createIncomingQuery("containerLinks for " + tableName, tableName);
			Set<KnowledgeAssociation> links = AbstractWrapper.resolveLinks(self, query);
			if (!links.isEmpty()) {
				return links;
			}
		}

		return Collections.emptySet();
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

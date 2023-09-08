/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.LongID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.ConstantMapping;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Resolves an {@link ExtID} for an object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	SecureRandomService.Module.class
})
public class ExtIDFactory extends ManagedClass {

	/** Default external id attribute when nothing is configured for the concrete type */
	public static String DEFAULT_EXTERNAL_ID_ATTR = "_externalId";

	private static final String SYSTEM_ID_PROPERTY = "system_id";

	/**
	 * Typed configuration interface definition for {@link ExtIDFactory}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends ManagedClass.ServiceConfiguration<ExtIDFactory> {

		/**
		 * Mapping from the static type to the attribute name where the {@link ExtID external id}
		 * for the type is stored.
		 */
		Mapping<? super String, String> getExternalAttributeMapping();

		/**
		 * Setter for {@link #getExternalAttributeMapping()}.
		 */
		void setExternalAttributeMapping(Mapping<? super String, String> mapping);

	}

	private long _systemId;

	private Mapping<? super String, String> _externalIdAttribute;

	/**
	 * Create a {@link ExtIDFactory}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ExtIDFactory(InstantiationContext context, Config config) {
		super(context, config);
		Mapping<? super String, String> configuredAttributeMapping = config.getExternalAttributeMapping();
		if (configuredAttributeMapping == null) {
			configuredAttributeMapping = new ConstantMapping<>(null);
		}
		_externalIdAttribute = configuredAttributeMapping;
	}

	@Override
	protected void startUp() {
		super.startUp();
		_systemId = fetchOrCreateSystemId(PersistencyLayer.getKnowledgeBase(), SecureRandomService.getInstance());
	}

	private long fetchOrCreateSystemId(KnowledgeBase kb, SecureRandomService secureRandom) {
		return fetchOrCreateSystemId(KBUtils.getConnectionPool(kb), secureRandom);
	}

	private long fetchOrCreateSystemId(ConnectionPool connectionPool, SecureRandomService secureRandom) {
		DBProperties dbProperties = new DBProperties(connectionPool);
		String value = dbProperties.getProperty(DBProperties.GLOBAL_PROPERTY, SYSTEM_ID_PROPERTY);
		if (value == null) {
			long newSystemId = secureRandom.getRandom().nextLong();
			dbProperties.setProperty(DBProperties.GLOBAL_PROPERTY, SYSTEM_ID_PROPERTY, Long.toString(newSystemId));
			return newSystemId;
		} else {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ex) {
				throw new RuntimeException("Invalid system id: " + value, ex);
			}
		}
	}

	/**
	 * Returns the only {@link ExtIDFactory} for this system.
	 */
	public static ExtIDFactory getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Returns the {@link ExtID} with the given {@link ExtID#objectId()}.
	 */
	public ExtID extID(long objectId) {
		return new ExtID(getSystemId(), objectId);
	}

	/**
	 * Returns the {@link ExtID} with {@link ExtID#objectId()} for the given {@link TLID}.
	 * 
	 * @param id
	 *        A non-null {@link LongID}.
	 */
	public ExtID extID(TLID id) {
		return extID(toLong(id));
	}

	private static long toLong(TLID id) {
		if (!(id instanceof LongID)) {
			throw new IllegalArgumentException("Only long ids supported: " + id);
		}
		return ((LongID) id).longValue();
	}

	/**
	 * Returns {@link ExtReference} for the given {@link TLObject}.
	 * 
	 * <p>
	 * If the given object is imported, the {@link ExtReference} of the imported object is returned,
	 * otherwise the {@link ExtReference} identifying the given object in external systems.
	 * </p>
	 */
	public ExtReference extReference(TLObject object) {
		ExtReference reference = lookupExtReference(object);
		if (reference == null) {
			return newExtReference(object);
		}
		return reference;
	}

	/**
	 * Returns the {@link ExtReference} for the given {@link TLObject}.
	 * <p>
	 * If the given object is imported, the {@link ExtReference} of the imported object is returned,
	 * otherwise null is returned.
	 * </p>
	 */
	public ExtReference lookupExtReference(TLObject object) {
		String externalIDAttribute = getExternalIDAttribute(object.tTable());
		Object externalId;
		try {
			externalId = object.tHandle().getAttributeValue(externalIDAttribute);
		} catch (NoSuchAttributeException ex) {
			throw new IllegalArgumentException(
				"No attribute '" + externalIDAttribute + "' for " + object + " to get external ID.");
		}
		if (externalId == null) {
			return null;
		}
		return ExtReferenceFormat.INSTANCE.parseExtReference((String) externalId);
	}

	/**
	 * Creates an {@link ExtReference} to identify the given object is external systems.
	 */
	public ExtReference newExtReference(TLObject object) {
		ObjectKey objectKey = object.tId();
		String objectType = TLModelUtil.qualifiedName(object.tType());
		return new ExtReference(objectKey.getBranchContext(), objectType, extID(objectKey.getObjectName()));
	}

	/**
	 * Whether the given event is an event for an imported object.
	 * 
	 * <p>
	 * This method checks whether the given {@link KnowledgeBase} event is an event that touches an
	 * imported object, e.g. the creation of the local copy of an foreign element.
	 * </p>
	 */
	public boolean isImported(ItemChange evt) {
		ObjectBranchId eventId = evt.getObjectId();
		String externalIDAttribute = getExternalIDAttribute(eventId.getObjectType());
		String externalId = (String) evt.getValues().get(externalIDAttribute);
		if (externalId != null) {
			ExtReference extReference = ExtReferenceFormat.INSTANCE.parseExtReference(externalId);
			return isImportedObject(eventId.getObjectName(), extReference);
		}
		return false;
	}

	/**
	 * Whether the given {@link TLObject} is imported, e.g. a local copy of a foreign element.
	 */
	public final boolean isImportedObject(TLObject object) {
		ExtReference ref = lookupExtReference(object);
		if (ref == null) {
			return false;
		}
		return isImportedObject(object.tIdLocal(), ref);
	}

	/**
	 * Dispatches to {@link #isImportedObject(TLID, ExtReference)} with the
	 * {@link TLObject#tIdLocal() id} of the given object.
	 */
	public final boolean isImportedObject(TLObject object, ExtReference ref) {
		return isImportedObject(object.tIdLocal(), ref);
	}

	/**
	 * Whether the object with the given {@link ExtReference} does <b>not</b> represent the local
	 * object with the given {@link TLID}, i.e. the object with the given local {@link TLID} is a
	 * shadow copy of an external object.
	 */
	public boolean isImportedObject(TLID localID, ExtReference ref) {
		ExtID extId = ref.getObjectName();
		return getSystemId() != extId.systemId() || extId.objectId() != toLong(localID);
	}

	/**
	 * Returns the name of the attribute which is used to store the external reference for instances
	 * of this static type.
	 */
	public String getExternalIDAttribute(MetaObject staticType) {
		String externalIdAttr = _externalIdAttribute.map(staticType.getName());
		if (externalIdAttr == null) {
			externalIdAttr = DEFAULT_EXTERNAL_ID_ATTR;
		}
		return externalIdAttr;
	}

	/**
	 * The system ID of the this system.
	 */
	public long getSystemId() {
		return _systemId;
	}

	/**
	 * {@link ExtIDFactory} module.
	 */
	public static final class Module extends TypedRuntimeModule<ExtIDFactory> {

		/**
		 * Singleton {@link Module} reference.
		 */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<ExtIDFactory> getImplementation() {
			return ExtIDFactory.class;
		}
	}

}

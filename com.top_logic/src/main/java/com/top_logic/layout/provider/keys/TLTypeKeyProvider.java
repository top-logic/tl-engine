/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider.keys;

import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.DefaultTypeKeyProvider;
import com.top_logic.basic.TypeKeyProvider;
import com.top_logic.basic.TypeKeyRegistry;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;

/**
 * Same as {@link DefaultTypeKeyProvider} but unwrapps {@link TLObject} implementations to get
 * access to the {@link MetaObject} type of an object.
 * 
 * @see TypeKeyRegistry for getting access to the functionality of this class.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLTypeKeyProvider extends DefaultTypeKeyProvider {

	private static final String CACHE_KEY_SUFFIX_HISTORIC = ".hist";

	/** The prefix to indicate that the key is a {@link TLClass}. */
	public static final String META_ELEMENT_PREFIX = "me:";

	/**
	 * The prefix to indicate that the key is a {@link TLClass} and this entry is for historic
	 * objects.
	 */
	public static final String META_ELEMENT_PREFIX_HISTORIC = "me.historic:";

	private final ConcurrentHashMap<String, TLClassKey> elementKeyCache =
		new ConcurrentHashMap<>();

	@Override
	public TypeKeyProvider.Key lookupTypeKey(Object obj) {
		if (obj instanceof TLObject) {
			TLObject tlObj = (TLObject) obj;
			if (tlObj.tValid()) {
				boolean isHistoric = !WrapperHistoryUtils.getRevision(tlObj).isCurrent();
				TLStructuredType type = tlObj.tType();
				if (type == null) {
					return INVALID_OBJECT_KEY;
				}

				return getClassKey(type, isHistoric);
			} else {
				return INVALID_OBJECT_KEY;
			}
		} else {
			return super.lookupTypeKey(obj);
		}
	}

	private TypeKeyProvider.Key getDefaultKey(TLStructuredType me, boolean isHistoric) {
		if (isHistoric) {
			return getClassKey(me, false);
		}

		if (me.getModelKind() == ModelKind.CLASS) {
			TLClass superType = TLModelUtil.getPrimaryGeneralization((TLClass) me);
			if (superType != null) {
				return getClassKey(superType, isHistoric);
			}
		}
		return getClassKey(Wrapper.class);
	}

	private class TLClassKey extends DefaultTypeKeyProvider.KeyWithDefault {
		private final String className;

		private final boolean historic;

		public TLClassKey(String className, boolean historic, Key defaultKey) {
			super(defaultKey);
			this.className = className;
			this.historic = historic;
		}

		@Override
		public String getConfigurationName() {
			return (this.isHistoric() ? META_ELEMENT_PREFIX_HISTORIC : META_ELEMENT_PREFIX) + className;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj instanceof TLClassKey) {
				TLClassKey other = (TLClassKey) obj;

				return className.equals(other.className) && this.isHistoric() == other.isHistoric();
			}
			return false;
		}

		@Override
		public int hashCode() {
			return className.hashCode() + (this.isHistoric() ? 1 : 0);
		}

		@Override
		public boolean isHistoric() {
			return historic;
		}

	}

	@Override
	public boolean reload() {
		elementKeyCache.clear();

		return super.reload();
	}

	/**
	 * {@link com.top_logic.basic.TypeKeyProvider.Key} for an invalid object.
	 */
	protected static final Key INVALID_OBJECT_KEY = new Key() {
		@Override
		public String getConfigurationName() {
			return "invalid";
		}

		@Override
		public Key getDefaultKey() {
			return null;
		}

		@Override
		public boolean isHistoric() {
			return false;
		}
	};

	/**
	 * The {@link com.top_logic.basic.TypeKeyProvider.Key} for instances of the given
	 * {@link TLStructuredType}.
	 *
	 * @param type
	 *        The type for which the instance key is requested.
	 * @param isHistoric
	 *        Whether the key is for historic instances.
	 * @return The {@link com.top_logic.basic.TypeKeyProvider.Key} to use for instances of the given
	 *         type.
	 */
	public TypeKeyProvider.Key getClassKey(TLStructuredType type, boolean isHistoric) {
		String meName = TLModelUtil.qualifiedName(type);
		String theKey = createCacheKey(meName, isHistoric);
		TLClassKey result = elementKeyCache.get(theKey);
		if (result == null) {
			Key keyDefault = getDefaultKey(type, isHistoric);
			TLClassKey newKey = new TLClassKey(meName, isHistoric, keyDefault);
			result = MapUtil.putIfAbsent(elementKeyCache, theKey, newKey);
		}

		return result;
	}

	/**
	 * Creates a key that is used as cache key.
	 * 
	 * @param name
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	protected String createCacheKey(String name, boolean isHistoric) {
		if (isHistoric) {
			return name + CACHE_KEY_SUFFIX_HISTORIC;
		}
		return name;
	}
}

/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.sql.CommitContext;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerProxy;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * {@link FlexDataManagerProxy} that applies a {@link DataTransformation} to the values during
 * load/store.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class TransformingFlexDataManager extends FlexDataManagerProxy {

	/**
	 * Transformation of {@link FlexData}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface DataTransformation {
		/**
		 * Transformation during
		 * {@link FlexDataManager#store(ObjectKey, FlexData, CommitContext)}.
		 * 
		 * @param typeName
		 *        The name of the type to which the given values belong.
		 * @param values
		 *        The collection of attribute values.
		 */
		FlexData encode(String typeName, FlexData values);

		/**
		 * Transformation during {@link FlexDataManager#load(KnowledgeBase, ObjectKey, boolean)}.
		 */
		FlexData decode(FlexData values, boolean mutable);
	}

	private final DataTransformation _transformation;

	/**
	 * Creates a {@link TransformingFlexDataManager}.
	 * 
	 * @param impl
	 *        See {@link FlexDataManagerProxy#FlexDataManagerProxy(FlexDataManager)}.
	 * @param transformation
	 *        The {@link DataTransformation} to apply to all {@link FlexData}.
	 */
	public TransformingFlexDataManager(FlexDataManager impl, DataTransformation transformation) {
		super(impl);

		_transformation = transformation;
	}

	@Override
	public boolean store(ObjectKey key, FlexData flexData, CommitContext context) {
		return super.store(key, _transformation.encode(key.getObjectType().getName(), flexData), context);
	}

	@Override
	public FlexData load(KnowledgeBase kb, ObjectKey key, boolean mutable) {
		return decode(mutable, super.load(kb, key, mutable));
	}

	@Override
	public <T> void loadAll(long dataRevision, AttributeLoader<T> callback,
			Mapping<? super T, ? extends ObjectKey> keyMapping, List<T> baseObjects, KnowledgeBase kb) {
		super.loadAll(dataRevision, new AttributeLoader<T>() {
			@Override
			public void loadData(long revision, T baseObject, FlexData data) {
				callback.loadData(revision, baseObject, decode(false, data));
			}

			@Override
			public void loadEmpty(long revision, T baseObject) {
				callback.loadEmpty(revision, baseObject);
			}
		}, keyMapping, baseObjects, kb);
	}

	FlexData decode(boolean mutable, FlexData encoded) {
		if (encoded == NoFlexData.INSTANCE) {
			return encoded;
		}

		return _transformation.decode(encoded, mutable);
	}

}

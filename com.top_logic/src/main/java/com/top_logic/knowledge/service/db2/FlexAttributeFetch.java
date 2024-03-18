/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.service.db2.AbstractDBKnowledgeItem.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.AttributeLoader;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;
import com.top_logic.model.export.PreloadContext;
import com.top_logic.model.export.PreloadOperation;

/**
 * {@link PreloadOperation} that bulk-loads flex attribute data.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class FlexAttributeFetch implements PreloadOperation {
	
	/** Sole instance of {@link FlexAttributeFetch}. */
	public static final FlexAttributeFetch INSTANCE = new FlexAttributeFetch();

	private FlexAttributeFetch() {
		// singleton instance
	}

	/**
	 * See: {@link PreloadOperation#prepare(PreloadContext, Collection)}
	 * <p>
	 * The base objects have to be of type {@link TLObject}.
	 * </p>
	 * 
	 * @param context
	 *        Not used. Is therefore allowed to be null.
	 */
	@Override
	public void prepare(PreloadContext context, Collection<?> baseObjects) {
		List<KnowledgeItem> knowledgeItems = new ArrayList<>(baseObjects.size());
		for (Object obj : baseObjects) {
			if (obj instanceof TLObject) {
				TLObject object = (TLObject) obj;
				if (!object.tTransient()) {
					knowledgeItems.add(object.tHandle());
				}
			}
		}
		prepareKnowledgeItems(IN_SESSION_REVISION, knowledgeItems);
	}

	/** Loads the flex attributes for the given {@link KnowledgeItem}s. */
	public void prepareKnowledgeItems(Collection<? extends KnowledgeItem> objects) {
		prepareKnowledgeItems(IN_SESSION_REVISION, objects);
	}

	/** Loads the flex attributes for the given {@link KnowledgeItem}s in the specified revision. */
	public void prepareKnowledgeItems(long revision, Collection<? extends KnowledgeItem> objects) {
		List<DynamicAttributedObject> items = new ArrayList<>();
		KnowledgeBase kb = null;
		for (KnowledgeItem wrappedObject : objects) {
			kb = wrappedObject.getKnowledgeBase();
			if (wrappedObject instanceof DynamicAttributedObject) {
				DynamicAttributedObject item = (DynamicAttributedObject) wrappedObject;
				if (item.needsToBeLoaded(revision)) {
					items.add(item);
				}
			}
		}

		if (items.isEmpty()) {
			return;
		}

		AttributeLoader<DynamicAttributedObject> loader = new AttributeLoader<>() {
			@Override
			public void loadData(long dataRevision, DynamicAttributedObject baseObject, FlexData data) {
				installDynamicValues(dataRevision, baseObject, data);
			}

			@Override
			public void loadEmpty(long dataRevision, DynamicAttributedObject baseObject) {
				installDynamicValues(dataRevision, baseObject, NoFlexData.INSTANCE);
			}

			private void installDynamicValues(long dataRevision, DynamicAttributedObject item, FlexData values) {
				// don't care when values has been loaded in the meantime
				item.initDynamicValues(dataRevision, values);
			}

		};
		FlexDataManager flexDataManager = items.get(0).getFlexDataManager();
		flexDataManager.loadAll(revision, loader, IdentifiedObject.KEY_MAPPING, items, kb);
	}
}

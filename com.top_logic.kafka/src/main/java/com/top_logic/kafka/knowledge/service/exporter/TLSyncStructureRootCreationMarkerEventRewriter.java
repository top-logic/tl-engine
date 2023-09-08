/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Objects.*;

import java.util.Collection;
import java.util.Map;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.kafka.knowledge.service.importer.TLSyncStructureRootCreationToUpdateEventRewriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.EventWriter;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.util.model.ModelService;

/**
 * An {@link EventRewriter} that marks creations of {@link StructuredElement} roots as such.
 * <p>
 * The {@link TLSyncStructureRootCreationToUpdateEventRewriter} will use this mark to detect them
 * and handle them specially.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class TLSyncStructureRootCreationMarkerEventRewriter implements EventRewriter {

	/**
	 * Additional attribute in TL-Sync {@link ChangeSet} that marks an {@link ObjectCreation} as the
	 * creation of a {@link StructuredElement#getRoot()} element.
	 */
	public static final String STRUCTURE_ROOT_MARKER_ATTRIBUTE = "__structure_root_marker_attribute";

	@Override
	public void rewrite(ChangeSet changeSet, EventWriter out) {
		Map<ObjectKey, String> rootStructureNameMap = createRootStructureNameMap();
		for (ObjectCreation creation : changeSet.getCreations()) {
			String rootStructureName = getRootStructureName(rootStructureNameMap, creation);
			if (rootStructureName != null) {
				markAsStructureRootCreation(rootStructureName, creation);
			}
		}
		out.write(changeSet);
	}

	private String getRootStructureName(Map<ObjectKey, String> rootMap, ObjectCreation creation) {
		return rootMap.get(getCurrentObjectKey(creation));
	}

	private ObjectKey getCurrentObjectKey(ObjectCreation creation) {
		return creation.getObjectId().toCurrentObjectKey();
	}

	private Map<ObjectKey, String> createRootStructureNameMap() {
		Map<ObjectKey, String> rootMap = map();
		Collection<TLModule> modules = ModelService.getApplicationModel().getModules();
		for (TLModule module : modules) {
			for (TLModuleSingleton link : module.getSingletons()) {
				ObjectKey rootId = requireNonNull(link.getSingleton().tId());
				String moduleName = requireNonNull(module.getName());
				rootMap.put(rootId, moduleName);
			}
		}
		return rootMap;
	}

	private void markAsStructureRootCreation(String structureName, ObjectCreation creation) {
		creation.getValues().put(STRUCTURE_ROOT_MARKER_ATTRIBUTE, structureName);
	}

}

/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.vector.index;

import com.top_logic.basic.Logger;
import com.top_logic.basic.col.ConcatenatedClosableIterator;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.AttributedWrapper;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.model.TLObject;

/**
 * Vector Index implementation of the {@link com.top_logic.knowledge.indexing.IndexingService}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@ServiceDependencies(VectorIndex.Module.class)
public class VectorIndexingService extends DefaultIndexingService {

	/** The Vector index */
	private final VectorIndex index;

	/**
	 * @param context
	 * 		Context
	 * @param config
	 * 		Config
	 */
	public VectorIndexingService(InstantiationContext context, DefaultIndexingServiceConfig config) {
		super(context, config);
		this.index = VectorIndex.getInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();

		if (this.index.getShouldReindexingOnRestart()) {
			this.rebuildIndex();
		}
	}

	@Override
	public boolean prepareService() {
		return false;
	}

	@Override
	public boolean serviceAvailable() {
		return true;
	}

	@Override
	public boolean containsContent(ObjectKey key) throws IndexException {
		return this.index.containsContent(key);
	}

	@Override
	public void indexContent(ContentObject content) throws IndexException {
		this.index.addContent(content);
	}

	@Override
	public void removeContent(ObjectKey key) throws IndexException {
		this.index.deleteContent(key);
	}

	@Override
	public void resetIndex() {
		Logger.info("Start resetting Vector index", DefaultIndexingService.class);
		index.resetIndex();
		Logger.info("Finished resetting Vector index", DefaultIndexingService.class);
	}

	@Override
	public void startIndex() {
		index.init(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
	}

	@Override
	public void rebuildIndex() {
		resetIndex();
		Logger.info("Start rebuilding search index", DefaultIndexingService.class);

		for (String modelFqn : this.index.getVectorIndexModels()) {
			if (modelFqn.trim().isEmpty()) {
				continue;
			}
			String[] parts = modelFqn.split(":");
			if (parts.length != 2) {
				Logger.warn("Invalid FQN '" + modelFqn + "'. Expected 'module:type'.", VectorIndex.class);
				continue;
			}

			ConcatenatedClosableIterator<TLObject> iterator =
				AttributeOperations.allInstancesIterator(MetaElementUtil.getGlobalType(parts[0], parts[1]),
					TLObject.class);

			while (iterator.hasNext()) {
				AttributedWrapper instance = (AttributedWrapper) iterator.next();

				this.indexContent(new VectorIndexContentObject(instance.tHandle()));

//				this.indexContent(WrapperContentObject(instance));
			}

			iterator.close();

		}

		Logger.info("Finished rebuilding search index", DefaultIndexingService.class);
	}

	@Override
	public void stopIndex() {
		index.shutDown();
	}
}

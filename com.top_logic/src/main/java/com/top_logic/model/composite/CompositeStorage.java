/*
 * Copyright (c) 2024 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.composite;

import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLReference;

/**
 * {@link StorageDetail} for {@link TLReference#isComposite() composition} references.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CompositeStorage extends StorageDetail {

	/**
	 * Computes a description how the container for a part is stored.
	 * 
	 * @param reference
	 *        the {@link TLReference} owner of the storage.
	 */
	ContainerStorage getContainerStorage(TLReference reference);

}


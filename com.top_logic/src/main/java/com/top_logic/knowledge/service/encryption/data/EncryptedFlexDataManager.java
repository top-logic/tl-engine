/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.encryption.data;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.knowledge.service.FlexDataManager;
import com.top_logic.knowledge.service.FlexDataManagerProxy;
import com.top_logic.knowledge.service.db2.DataTransformationChain;
import com.top_logic.knowledge.service.db2.SerializingTransformer;
import com.top_logic.knowledge.service.db2.SerializingTransformer.Config;
import com.top_logic.knowledge.service.db2.TransformingFlexDataManager;

/**
 * {@link FlexDataManagerProxy} taht applies a {@link SerializingTransformer}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public class EncryptedFlexDataManager extends TransformingFlexDataManager {

	/**
	 * Creates a {@link EncryptedFlexDataManager}.
	 * 
	 * @param config
	 *        The configuration.
	 * @param impl
	 *        See {@link FlexDataManagerProxy#FlexDataManagerProxy(FlexDataManager)}.
	 */
	public EncryptedFlexDataManager(Config config, FlexDataManager impl) {
		super(impl, new DataTransformationChain(new SerializingTransformer(config), new EncryptingTransformer()));
	}

}

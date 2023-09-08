/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.folder;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Container of a {@link FolderData}.
 * 
 * <p>
 * Implementing this interface allows creating stable names for {@link FolderData} instance and
 * retrieve them during test replay.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface FolderDataOwner extends NamedModel {

	/**
	 * The contained {@link FolderData}
	 */
	FolderData getFolderData();

	/**
	 * {@link ModelNamingScheme} for {@link FolderData} instances that are controlled by a named
	 * {@link FolderDataOwner}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public class OwnedFolderDataNaming extends AbstractModelNamingScheme<FolderData, OwnedFolderDataNaming.FolderName> {

		/**
		 * {@link ModelName} of a {@link FolderData} within a {@link FolderDataOwner}.
		 * 
		 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
		 */
		public interface FolderName extends ModelName {

			/**
			 * The name of the {@link FolderDataOwner}.
			 */
			ModelName getOwnerName();

			/**
			 * @see #getOwnerName()
			 */
			void setOwnerName(ModelName modelName);

		}

		@Override
		public Class<FolderName> getNameClass() {
			return FolderName.class;
		}

		@Override
		public Class<FolderData> getModelClass() {
			return FolderData.class;
		}

		@Override
		public FolderData locateModel(ActionContext context, FolderName name) {
			return ((FolderDataOwner) ModelResolver.locateModel(context, name.getOwnerName())).getFolderData();
		}

		@Override
		protected boolean isCompatibleModel(FolderData model) {
			return super.isCompatibleModel(model) && model.getOwner() != null;
		}

		@Override
		protected void initName(FolderName name, FolderData model) {
			name.setOwnerName(model.getOwner().getModelName());
		}

	}

}

/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.document;

import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.mig.html.layout.tiles.component.ComponentParameters;

/**
 * {@link ComponentParameters} for creation of a document tile that displays a PDF document as image
 * gallery.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface DocumentTileConfig extends ComponentParameters {

	/** Configuration name of the value of {@link #getDocumentName()}. */
	String DOCUMENT_NAME = "documentName";

	/** Configuration name of the value of {@link #getModel()}. */
	String MODEL = "model";

	/** Configuration name of the value of {@link #getModelBuilder()}. */
	String MODEL_BUILDER = "modelBuilder";

	/**
	 * Name of the PDF file in the documents of the {@link #getModel()} that must be displayed in
	 * the tile.
	 */
	@Mandatory
	@Name(DOCUMENT_NAME)
	String getDocumentName();

	/**
	 * Setter for {@link #getDocumentName()}.
	 */
	void setDocumentName(String documentName);

	/**
	 * The model of the document tile. This is not selected by the user but filled from
	 * configuration.
	 */
	@Hidden
	@Mandatory
	@Name(MODEL)
	ModelSpec getModel();

	/**
	 * The model builder of the document tile. This is not selected by the user but filled from
	 * configuration.
	 */
	@Hidden
	@Mandatory
	@Name(MODEL_BUILDER)
	String getModelBuilder();

}


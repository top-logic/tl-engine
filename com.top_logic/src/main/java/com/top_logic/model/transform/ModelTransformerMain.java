/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.xml.XMLTransformerMain;
import com.top_logic.model.TLModel;
import com.top_logic.model.binding.xml.ModelReader;
import com.top_logic.model.binding.xml.ModelWriter;
import com.top_logic.model.impl.TLModelImpl;

/**
 * Super class for command line tools for {@link TLModel} transformations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ModelTransformerMain extends XMLTransformerMain<TLModel> {

	/**
	 * Creates a {@link ModelTransformerMain}.
	 */
	public ModelTransformerMain() {
		super();
	}

	/**
	 * Creates a {@link ModelTransformerMain}.
	 * 
	 * @param isInteractive
	 *        See {@link #isInteractive()}.
	 * @param protocol
	 *        See {@link #getProtocol()}.
	 */
	public ModelTransformerMain(boolean isInteractive, Protocol protocol) {
		super(isInteractive, protocol);
	}

	@Override
	protected void outputXML(TLModel model, XMLStreamWriter outWriter) throws XMLStreamException {
		ModelWriter.writeModel(outWriter, model);
	}

	@Override
	protected TLModel inputXML(XMLStreamReader inXML) throws XMLStreamException {
		TLModel model = new TLModelImpl();
		new ModelReader(inXML, model).readModel();
		transform(model);
		return model;
	}

	protected abstract void transform(TLModel model);

}

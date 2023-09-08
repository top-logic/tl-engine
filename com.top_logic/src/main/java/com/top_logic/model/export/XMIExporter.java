/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import com.top_logic.basic.Settings;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.model.TLModel;
import com.top_logic.model.binding.xml.ModelWriter;

/**
 * Transformer frontend that translates a {@link TLModel} into XMI interchange
 * format.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class XMIExporter {
	
	private Transformer transformer;

	/**
	 * Creates a {@link XMIExporter}.
	 */
	public XMIExporter() {
		this.transformer = newTransformer();
	}

	private Transformer newTransformer() {
		TransformerFactory factory = XsltUtil.safeTransformerFactory();
		
		StreamSource transformationSource = 
			new StreamSource(XMIExporter.class.getResourceAsStream("tl-model-to-uml-xmi.xslt"));
		
		try {
			return factory.newTransformer(transformationSource);
		} catch (TransformerConfigurationException ex) {
			throw (AssertionError) new AssertionError("Transformer cannot be instantiated.").initCause(ex);
		}
	}

	/**
	 * Transforms the given model to XMI format and stores the result in the
	 * given file.
	 * 
	 * @param model
	 *        The model to transform.
	 * @param xmiFile
	 *        The XMI file to create.
	 */
	public void toXMI(TLModel model, File xmiFile) throws IOException, XMLStreamException, TransformerException {
		try (FileOutputStream outputStream = new FileOutputStream(xmiFile)) {
			toXMI(model, new StreamResult(outputStream));
		}
	}
	
	/**
	 * Transforms the given model to XMI format and stores the result in the
	 * given result location.
	 * 
	 * @param model
	 *        The model to transform.
	 * @param xmiFile
	 *        The XMI result location.
	 */
	public void toXMI(TLModel model, Result xmiFile) throws IOException, XMLStreamException, TransformerException {
		File modelFile = File.createTempFile("model", ".xml", Settings.getInstance().getTempDir());
		ModelWriter.writeModel(modelFile, model, false);
		
		toXMI(new StreamSource(modelFile), xmiFile);
		modelFile.delete();
	}

	/**
	 * Transforms the given model file to XMI format and stores the result in the given XMI file.
	 * 
	 * @param modelFile
	 *        The model to transform.
	 * @param xmiFile
	 *        The XMI file to create.
	 */
	public void toXMI(File modelFile, File xmiFile) throws TransformerException, IOException {
		try (FileOutputStream outputStream = new FileOutputStream(xmiFile)) {
			StreamSource modelSource = new StreamSource(modelFile);
			StreamResult xmiResult = new StreamResult(outputStream);
		
			toXMI(modelSource, xmiResult);
		}
	}

	/**
	 * Transforms the given model location to XMI format and stores the result
	 * in the given file.
	 * 
	 * @param modelSource
	 *        The location to read the model from.
	 * @param xmiResult
	 *        The location of the XMI document to create.
	 */
	public void toXMI(Source modelSource, Result xmiResult) throws TransformerException {
		transformer.transform(modelSource, xmiResult);
	}
	
}

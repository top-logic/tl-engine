/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.top_logic.basic.Main;
import com.top_logic.basic.Protocol;

/**
 * Base class for XML transformation main classes.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class XMLTransformerMain<M> extends Main {

	/**
	 * Parameter for specifying the input file.
	 */
	public static final String IN_PARAM = "-in";
	
	/**
	 * Parameter for specifying the output file.
	 */
	public static final String OUT_PARAM = "-out";

	/**
	 * Parameter for requesting pretty printing.
	 */
	public static final String PRETTY_PARAM = "-pretty";
	
	/**
	 * Parameter for disabling pretty printing.
	 */
	public static final String NO_PRETTY_PARAM = "-no-pretty";
	
	private String inFileName;
	private String outFileName;
	private boolean prettyPrint;
	

	/**
	 * Creates a {@link XMLTransformerMain}.
	 * 
	 * See {@link Main#Main()}
	 */
	public XMLTransformerMain() {
		super();
	}

	/**
	 * Creates a {@link XMLTransformerMain}.
	 * 
	 * @param isInteractive
	 *        See {@link #isInteractive()}.
	 * @param protocol
	 *        See {@link #getProtocol()}
	 */
	public XMLTransformerMain(boolean isInteractive, Protocol protocol) {
		super(isInteractive, protocol);
	}

	@Override
	protected int shortOption(char c, String[] args, int i) {
		int index = i - 1;
		if (IN_PARAM.equals(args[index])) {
			inFileName = args[i++];
		}
		else if (OUT_PARAM.equals(args[index])) {
			outFileName = args[i++];
		}
		else if (PRETTY_PARAM.equals(args[index])) {
			prettyPrint = true;
		}
		else if (NO_PRETTY_PARAM.equals(args[index])) {
			prettyPrint = false;
		}
		else {
			return super.shortOption(c, args, i);
		}

		return i;
	}
	
	@Override
	protected void showHelpOptions() {
        info("\t-in <FILE>             the model file to read ('-' for standard input)");
        info("\t-out <FILE>            the model file to write ('-' for standard output)");
        info("\t-pretty | -no-pretty   enable/disable pretty printing output.");
		super.showHelpOptions();
	}
	
	@Override
	protected void doActualPerformance() throws Exception {
		XMLInputFactory inputFactory = XMLStreamUtil.getDefaultInputFactory();
		InputStream inStream;
		if (isConsole(inFileName)) {
			inStream = System.in;
		} else {
			inStream = new FileInputStream(new File(inFileName));
		}
		XMLStreamReader inXML = inputFactory.createXMLStreamReader(inStream);

		M model = inputXML(inXML);
		
		checkErrors();
		
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		OutputStream outStream;
		boolean consoleOut = isConsole(outFileName);
		if (consoleOut) {
			outStream = System.out;
		} else {
			outStream = new FileOutputStream(new File(outFileName));
		}
		XMLStreamWriter outWriter = outputFactory.createXMLStreamWriter(outStream, "utf-8");
		if (prettyPrint) {
			outWriter = new XMLStreamIndentationWriter(outWriter);
		}
		
		outputXML(model, outWriter);
		
		if (consoleOut) {
			outWriter.flush();
		} else {
			outWriter.close();
		}
	}

	protected abstract M inputXML(XMLStreamReader inXML) throws XMLStreamException;

	protected abstract void outputXML(M model, XMLStreamWriter outWriter) throws XMLStreamException;

	private boolean isConsole(String fileName) {
		return fileName == null || ("-".equals(fileName));
	}

}

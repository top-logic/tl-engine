/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.layoutGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.tools.layout.InfoLine;
import com.top_logic.basic.tools.layout.LayoutConstants;
import com.top_logic.layout.processor.LayoutModelConstants;

/**
 * Base class for transformers that translate layout CSV specifications into component XML
 * definitions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractLayoutGenerator {

	/** Property name for the name of the referenced layout file. */
	protected static final String PARAM_LAYOUT_REFERENCE_NAME = "layout";

	/** Property name for the flag whether the default checker is used. */
	protected static final String PARAM_USE_DEFAULT_CHECKER = "useDefaultChecker";

	/** Property name for the name of the compound master component. */
	protected static final String PARAM_COMPOUND_MASTER_NAME = "compoundMasterName";

	/** Property name for the domain. */
	protected static final String PARAM_DOMAIN = "domain";

	/** Property name for the image. */
	protected static final String PARAM_IMAGE = "image";

	/** Property name for the resource key prefix. */
	protected static final String PARAM_RESOURCE_KEY = "resPrefix";

	/** Property name for the component name. */
	protected static final String PARAM_NAME = "componentName";

	/** A list of {@link InfoLine}s the generated layout is based on */
	private List<InfoLine> _infoLines;

	private String _destinationDir = "data/posTemplates/generated/";

	private String _fileNamePrefix = "pos";

	private String _masterFrameFileName = null;

	private String _templateDir;

	private final Map<String, Property> _propertyImpls = new LinkedHashMap<>();

	/** Computation of the value of a template property from an {@link InfoLine}. */
	protected abstract static class Property {
		/**
		 * The value of this property for the given line.
		 *
		 * @param line
		 *        The {@link InfoLine} to compute the value for.
		 * @return The property value, may be {@code null}.
		 */
		public abstract String getValue(InfoLine line);
	}

	public AbstractLayoutGenerator(List<InfoLine> someInfoLines) {
		_infoLines = someInfoLines;

		initProperties();
	}

	private void initProperties() {
		addProperty(PARAM_COMPOUND_MASTER_NAME, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getCompoundMasterName(line);
			}
		});
		addProperty(PARAM_USE_DEFAULT_CHECKER, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getUseDefaultChecker(line);
			}
		});
		addProperty(PARAM_DOMAIN, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getDomain(line);
			}
		});
		addProperty(PARAM_LAYOUT_REFERENCE_NAME, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getLayoutReferenceName(line);
			}
		});
		addProperty(PARAM_NAME, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getName(line);
			}
		});
		addProperty(PARAM_RESOURCE_KEY, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getResourceKey(line);
			}
		});
		addProperty(PARAM_IMAGE, new Property() {
			@Override
			public String getValue(InfoLine line) {
				return getImage(line);
			}
		});
	}

	/**
	 * Registers a template property.
	 *
	 * @param name
	 *        The name of the property.
	 * @param property
	 *        The computation of the property value.
	 */
	protected final void addProperty(String name, Property property) {
		_propertyImpls.put(name, property);
	}

	/** The names of all registered template properties. */
	public Set<String> getAllPropertyNames() {
		return Collections.unmodifiableSet(_propertyImpls.keySet());
	}

	/** The {@link InfoLine}s the generated layout is based on. */
	public List<InfoLine> getInfoLines() {
		return _infoLines;
	}

	/** Setter for {@link #getFileNamePrefix()}. */
	public final void setFileNamePrefix(String aPrefix) {
		_fileNamePrefix = aPrefix;
	}

	/** The prefix for the names of the generated files. */
	public final String getFileNamePrefix() {
		return _fileNamePrefix;
	}

	/**
	 * Setter for {@link #getMasterFrameFileName()}.
	 *
	 * @param fileName
	 *        The file name, the suffix {@code .xml} is appended if missing.
	 */
	public final void setMasterFrameFileName(String fileName) {
		if (!fileName.endsWith(".xml")) {
			fileName = fileName + ".xml";
		}
		_masterFrameFileName = fileName;
	}

	/** The name of the master frame file, or {@code null} if none is set. */
	public final String getMasterFrameFileName() {
		return _masterFrameFileName;
	}

	/** The directory to write the generated files to. */
	public final String getDestinationDir() {
		return _destinationDir;
	}

	/** Setter for {@link #getDestinationDir()}. */
	public final void setDestinationDir(String aDestinationDir) {
		this._destinationDir = aDestinationDir;
	}

	/** Setter for {@link #getTemplateDir()}. */
	public final void setTemplateDir(String templateDir) {
		_templateDir = templateDir;
	}

	/** The directory containing the templates. */
	public final String getTemplateDir() {
		return _templateDir;
	}

	/**
	 * Generates the layout files from the {@link #getInfoLines()}.
	 *
	 * @param interactive
	 *        Whether the user may be asked questions on the console.
	 * @throws IOException
	 *         If writing the files fails.
	 */
	public abstract void generateLayout(boolean interactive) throws IOException;

	/**
	 * The value of the given template property for the given line.
	 *
	 * @param propertyName
	 *        The name of the property, see {@link #getAllPropertyNames()}.
	 * @param aInfoLine
	 *        The line to compute the value for.
	 * @return The property value, {@code null} if it is empty, or the empty string for an unknown
	 *         property.
	 */
	protected final String getReplacement(String propertyName, InfoLine aInfoLine) {
		Property property = _propertyImpls.get(propertyName);
		if (property != null) {
			String result = StringServices.nonEmpty(property.getValue(aInfoLine));
			if (result == null) {
				return null;
			}
			if (propertyName.equals(PARAM_LAYOUT_REFERENCE_NAME) && !result.endsWith(LayoutModelConstants.XML_SUFFIX)) {
				return result + LayoutModelConstants.XML_SUFFIX;
			}
			return result;
		} else {
			System.out.println("Invalid property: " + propertyName + ".");
			return "";
		}
	}
	
	/** The value of the property {@link #PARAM_USE_DEFAULT_CHECKER} for the given line. */
	protected String getUseDefaultChecker(InfoLine aInfoLine) {
		boolean value = "TRUE".equalsIgnoreCase(InfoLine.value(aInfoLine, InfoLine.USE_DEFAULT_CHECKER));
		if (!value) {
			return null;
		}
		return "true";
	}

	/** The value of the property {@link #PARAM_COMPOUND_MASTER_NAME} for the given line. */
	protected String getCompoundMasterName(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.MASTER));
	}

	/** The value of the property {@link #PARAM_DOMAIN} for the given line. */
	protected String getDomain(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.DOMAIN));
	}

	/** The value of the property {@link #PARAM_IMAGE} for the given line. */
	protected String getImage(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.IMAGE));
	}

	/** The value of the property {@link #PARAM_NAME} for the given line. */
	protected String getName(InfoLine aInfoLine) {
		return StringServices.nonNull(InfoLine.value(aInfoLine, InfoLine.NAME));
	}

	/** The value of the property {@link #PARAM_LAYOUT_REFERENCE_NAME} for the given line. */
	protected String getLayoutReferenceName(InfoLine aInfoLine) {
		return StringServices.nonNull(InfoLine.value(aInfoLine, InfoLine.FILE));
	}

	/**
	 * The value of the property {@link #PARAM_RESOURCE_KEY} for the given line.
	 *
	 * @return The resource key ending with a dot, or the empty string if none is given.
	 */
	protected String getResourceKey(InfoLine aLine) {
	    String theKey = InfoLine.value(aLine, InfoLine.KEY);
		if (theKey == null) {
			return "";
		}

		if (theKey.endsWith(".")) {
			return theKey;
		} else {
			return join(theKey, ".");
	    }
	}

	/** The template to use for the given line on the given nesting level. */
	protected String getTemplateForLevel(InfoLine aLine, int aLevel) {
		Set<String> types = InfoLine.getTypes(aLine);
		if (types.contains(LayoutConstants.TYPE_TEMPLATE)) {
			return (InfoLine.value(aLine, InfoLine.TEMPLATE));
		}

		return LayoutConstants.getDefaultTemplate(types, aLevel, hasChildren(aLine));
	}

	/** Whether the given line has children, {@code true} for {@code null} (the root). */
	protected boolean hasChildren(InfoLine aLine) {
		if (aLine == null) {
			return true;
		} else {
			return aLine.hasChildren();
		}
	}

	/** Whether the given line has the given type. */
	protected static boolean hasType(InfoLine aLine, String aType) {
		return InfoLine.getTypes(aLine).contains(aType);
	}

	/**
	 * Appends the given suffix to the given base name.
	 *
	 * @return The concatenation, or the empty string if the base name is empty.
	 */
	protected static String join(String baseName, String suffix) {
		if (StringServices.isEmpty(baseName)) {
			return "";
		}
		return baseName + suffix;
	}

}

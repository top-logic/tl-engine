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

	protected static final String PARAM_LAYOUT_REFERENCE_NAME = "layout";

	protected static final String PARAM_USE_DEFAULT_CHECKER = "useDefaultChecker";

	protected static final String PARAM_COMPOUND_MASTER_NAME = "compoundMasterName";

	protected static final String PARAM_DOMAIN = "domain";

	protected static final String PARAM_IMAGE = "image";

	protected static final String PARAM_RESOURCE_KEY = "resPrefix";

	protected static final String PARAM_NAME = "componentName";

	/** A list of {@link InfoLine}s the generated layout is based on */
	private List<InfoLine> _infoLines;

	private String _destinationDir = "data/posTemplates/generated/";

	private String _fileNamePrefix = "pos";

	private String _masterFrameFileName = null;

	private String _templateDir;

	private final Map<String, Property> _propertyImpls = new LinkedHashMap<>();

	protected abstract static class Property {
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

	protected final void addProperty(String name, Property property) {
		_propertyImpls.put(name, property);
	}

	public Set<String> getAllPropertyNames() {
		return Collections.unmodifiableSet(_propertyImpls.keySet());
	}

	public List<InfoLine> getInfoLines() {
		return _infoLines;
	}

	public final void setFileNamePrefix(String aPrefix) {
		_fileNamePrefix = aPrefix;
	}

	public final String getFileNamePrefix() {
		return _fileNamePrefix;
	}

	public final void setMasterFrameFileName(String fileName) {
		if (!fileName.endsWith(".xml")) {
			fileName = fileName + ".xml";
		}
		_masterFrameFileName = fileName;
	}

	public final String getMasterFrameFileName() {
		return _masterFrameFileName;
	}

	public final String getDestinationDir() {
		return _destinationDir;
	}

	public final void setDestinationDir(String aDestinationDir) {
		this._destinationDir = aDestinationDir;
	}

	public final void setTemplateDir(String templateDir) {
		_templateDir = templateDir;
	}

	public final String getTemplateDir() {
		return _templateDir;
	}

	public abstract void generateLayout(boolean interactive) throws IOException;

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
	
	protected String getUseDefaultChecker(InfoLine aInfoLine) {
		boolean value = "TRUE".equalsIgnoreCase(InfoLine.value(aInfoLine, InfoLine.USE_DEFAULT_CHECKER));
		if (!value) {
			return null;
		}
		return "true";
	}

	protected String getCompoundMasterName(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.MASTER));
	}

	protected String getDomain(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.DOMAIN));
	}

	protected String getImage(InfoLine aInfoLine) {
		return StringServices.nonEmpty(InfoLine.value(aInfoLine, InfoLine.IMAGE));
	}

	protected String getName(InfoLine aInfoLine) {
		return StringServices.nonNull(InfoLine.value(aInfoLine, InfoLine.NAME));
	}

	protected String getLayoutReferenceName(InfoLine aInfoLine) {
		return StringServices.nonNull(InfoLine.value(aInfoLine, InfoLine.FILE));
	}

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

	protected String getTemplateForLevel(InfoLine aLine, int aLevel) {
		Set<String> types = InfoLine.getTypes(aLine);
		if (types.contains(LayoutConstants.TYPE_TEMPLATE)) {
			return (InfoLine.value(aLine, InfoLine.TEMPLATE));
		}

		return LayoutConstants.getDefaultTemplate(types, aLevel, hasChildren(aLine));
	}

	protected boolean hasChildren(InfoLine aLine) {
		if (aLine == null) {
			return true;
		} else {
			return aLine.hasChildren();
		}
	}

	protected static boolean hasType(InfoLine aLine, String aType) {
		return InfoLine.getTypes(aLine).contains(aType);
	}

	protected static String join(String baseName, String suffix) {
		if (StringServices.isEmpty(baseName)) {
			return "";
		}
		return baseName + suffix;
	}

}

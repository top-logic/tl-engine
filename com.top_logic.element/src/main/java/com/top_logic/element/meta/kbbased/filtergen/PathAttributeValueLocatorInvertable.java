/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Locate a value following an attribute name path.
 * All values on the path must be Wrappers.
 * The path parts are separated by dots.
 * 
 * @author    <a href=mailto:kbu@top-logic.com>kbu</a>
 */
public class PathAttributeValueLocatorInvertable extends
		PathAttributeValueLocator {
	
	/** Prefix used to denote that the following path element should be inverted. */
	public static final String INVERSION_PREFIX = "-";
	
	public PathAttributeValueLocatorInvertable() {
		super();
	}

	public PathAttributeValueLocatorInvertable(String aConfig) {
		super(aConfig);
	}
	
	@Override
	protected String[] getPath(String aConfig) {
		List<String> thePathElements = new ArrayList<>();
		while (!StringServices.isEmpty(aConfig)) {
			int theNextIndex = -1;
			if (aConfig.startsWith(INVERSION_PREFIX)) {
				// Inversion
				int theDotIndex = aConfig.indexOf(PATH_SEPARATOR);
				if (theDotIndex >= 0) {
					// More elements -> look for type and attribute part
					int theMEIndex = aConfig.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR, 1);
					int theInvIndex = aConfig.indexOf(INVERSION_PREFIX, 1);
					if (theMEIndex > 0 && (theInvIndex < 0 || theMEIndex < theInvIndex)) {
						// We have an type and it belongs to this inversion element
						theNextIndex = aConfig.indexOf(PATH_SEPARATOR, theMEIndex);
					}
					else {
						// No type -> only attribute
						theNextIndex = aConfig.indexOf(PATH_SEPARATOR);
					}
				}
			}
			else {
				// No inversion -> goto next path element
				theNextIndex = aConfig.indexOf(PATH_SEPARATOR);
			}
			
			if (theNextIndex >= 0) {
				// More elements -> continue parsing
				thePathElements.add(aConfig.substring(0, theNextIndex));
				aConfig = aConfig.substring(theNextIndex + 1);
			}
			else {
				// No more elements -> take and finish
				thePathElements.add(aConfig);
				aConfig = null;
			}
			
		}
		
		return thePathElements.toArray(new String[thePathElements.size()]);
	}
	
	@Override
	protected Object getValue(Wrapper aWrap, String aConfig) {
		String referencedPath = aConfig;
		if (aConfig.startsWith(INVERSION_PREFIX)) {
			// Check attributed
			if (!(aWrap instanceof Wrapper)) {
				throw new IllegalArgumentException("Wrapper must be an attributed to use inverted value locating: " + aWrap);
			}
			Wrapper    theAttributed = (Wrapper) aWrap;
			TLStructuredTypePart theMA         = null;

			// Check attribute
			aConfig = aConfig.substring(INVERSION_PREFIX.length());
			int theSepIndex = aConfig.indexOf(TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
			try {
				if (theSepIndex >= 0) {
					String theMEName = aConfig.substring(0, theSepIndex);
					aConfig          = aConfig.substring(theSepIndex + 1);
					MetaElementFactory meFactory = MetaElementFactory.getInstance();

					theMA = getMAFromGlobalME(meFactory, theMEName, aConfig);
					if (theMA == null) {
						boolean hasME = false;
						Iterator<TLClass> allMetaElements = meFactory.getAllMetaElements().iterator();
						while (theMA == null && allMetaElements.hasNext()) {
							TLClass theME = allMetaElements.next();
							if (theME.getName().equals(theMEName)) {
								hasME = true;
								theMA = MetaElementUtil.getMetaAttributeOrNull(theME, aConfig);
							}
						}
						if (!hasME) {
							throw handleMetaElementNotFound(referencedPath, theMEName);
						}
					}
				}
				else {
					theMA = (TLStructuredTypePart) theAttributed.tType().getPart(aConfig);
				}
			} catch (Exception ex) {
				throw new IllegalArgumentException("Problem getting referenced attribute '" + aConfig + "' of " + aWrap + "'", ex);
			}

			if (theMA != null) {
				return AttributeOperations.getReferers(theAttributed, theMA);
			} else {
				throw handleMetaAttributeNotFound(referencedPath, aConfig);
    	    }
		}
		else {
			return super.getValue(aWrap, aConfig);
		}
	}

	private static IllegalArgumentException handleMetaAttributeNotFound(String referencedPath, String maName) {
		StringBuilder error = new StringBuilder();
		error.append("Wrong referenced path '");
		error.append(referencedPath);
		error.append("': meta attribute named '");
		error.append(maName);
		error.append("' is unknown!");
		return new IllegalArgumentException(error.toString());
	}

	private static IllegalArgumentException handleMetaElementNotFound(String referencedPath, String meName) {
		StringBuilder error = new StringBuilder();
		error.append("Wrong referenced path '");
		error.append(referencedPath);
		error.append("': meta element named '");
		error.append(meName);
		error.append("' is unknown!");
		return new IllegalArgumentException(error.toString());
	}

	/**
	 * Returns the {@link TLStructuredTypePart} with the given name from the global {@link TLClass}
	 * with the given name.
	 * 
	 * @return <code>null</code> iff there is no such {@link TLClass} or the {@link TLClass}
	 *         does not have a {@link TLStructuredTypePart} with the gven name.
	 */
	private TLStructuredTypePart getMAFromGlobalME(MetaElementFactory meFactory, String meName, String maName) {
		TLClass me = meFactory.lookupGlobalMetaElement(meName);
		if (me != null) {
			return MetaElementUtil.getMetaAttributeOrNull(me, maName);
		}
		return null;
	}

	public static String getNewPath(String path) {
		StringBuilder newPath = new StringBuilder();
		while (!StringServices.isEmpty(path)) {
			int theNextIndex = -1;
			if (path.startsWith(INVERSION_PREFIX)) {
				// Inversion
				int theDotIndex = path.indexOf(PATH_SEPARATOR);
				if (theDotIndex >= 0) {
					// More elements -> look for type and attribute part
					int theMEIndex = path.indexOf(':', 1);
					int theInvIndex = path.indexOf(INVERSION_PREFIX, 1);
					if (theMEIndex > 0 && (theInvIndex < 0 || theMEIndex < theInvIndex)) {
						// We have an ME and it belongs to this inversion element
						theNextIndex = path.indexOf(PATH_SEPARATOR, theMEIndex);
					}
					else {
						// No type -> only attribute
						theNextIndex = path.indexOf(PATH_SEPARATOR);
					}
				}
			}
			else {
				// No inversion -> goto next path element
				theNextIndex = path.indexOf(PATH_SEPARATOR);
			}
			String nextPath;
			if (theNextIndex >= 0) {
				// More elements -> continue parsing
				nextPath = path.substring(0, theNextIndex + 1);
				path = path.substring(theNextIndex + 1);
			}
			else {
				// No more elements -> take and finish
				nextPath = path;
				path = null;
			}

			nextPath = nextPath.replace(':', TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR);
			if (nextPath.contains("" + TLModelUtil.QUALIFIED_NAME_PART_SEPARATOR)) {
				if (nextPath.startsWith(INVERSION_PREFIX)) {
					nextPath = INVERSION_PREFIX + "me:" + nextPath.substring(INVERSION_PREFIX.length());
				} else {
					nextPath = "me:" + nextPath;
				}
			}

			newPath.append(nextPath);
		}
		return newPath.toString();
	}

}

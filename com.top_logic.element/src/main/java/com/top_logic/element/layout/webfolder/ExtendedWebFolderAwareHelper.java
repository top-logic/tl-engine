/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.webfolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * Provides helper methods for {@link ExtendedWebfolderAware} implementors
 * using {@link Wrapper} objects.
 * 
 * @author    <a href=mailto:kkbu@top-logic.com>Karsten Buch</a>
 */
public class ExtendedWebFolderAwareHelper {

	/**
	 * Get the relevant WebFolderAttributes from a given component's model.
	 * 
	 * @param aComponent		the component, cf. above.
	 * @param nonEmptyOnly		if true, include only existing filders with children
	 * @param aBCG				the BoundCommandGroup to check
	 * @param anAttNameExcludes	the names of attributes to exclude. May be <code>null</code>.
	 * @return the attributes
	 * @throws IllegalArgumentException if the component or its model don't meet the assumptions above
	 */
	public static Collection<TLStructuredTypePart> getRelevantWebFolderAttributes(ExtendedWebfolderAware aComponent,
			boolean nonEmptyOnly,
																					BoundCommandGroup aBCG, Collection<String> anAttNameExcludes) {
		Wrapper theModel = aComponent.getWebFolderHolder();
		if ((theModel == null) || !(aComponent instanceof BoundChecker)) {
			throw new IllegalArgumentException("Component must be a BoundChecker, Model must be a Wrapper");
		}


		Collection<TLStructuredTypePart> folderAttributes =
			AttributeOperations.getWebFolderAttributes(theModel.tType());
	
		if ((folderAttributes == null) || folderAttributes.isEmpty()) {
			return Collections.emptyList();
		}
	
		Wrapper                      theWrapper            = theModel;
		List<TLStructuredTypePart> theNonEmptyFolderAtts = new ArrayList<>();
	
		for (Iterator<TLStructuredTypePart> theIt = folderAttributes.iterator(); theIt.hasNext();) {
			TLStructuredTypePart theFolderAttr = (TLStructuredTypePart) theIt.next();
			String                 theAttrName   = theFolderAttr.getName();
			
			if (anAttNameExcludes == null || !anAttNameExcludes.contains(theAttrName)) {
		
				boolean hasAccess = TLContext.isSuperUser();
				if (!hasAccess) {
					if (AttributeOperations.isClassified(theFolderAttr)) {
						hasAccess = AttributeOperations.getAccess(theFolderAttr, AccessManager.getInstance().getRoles(TLContext.getContext().getCurrentPersonWrapper(), 
	            				(BoundObject) theWrapper)).contains(aBCG);
					}
					else {
						hasAccess = ((BoundCheckerComponent) aComponent).allow(aBCG);
					}
				}
	
				if (hasAccess) {
					if (checkEmpty(nonEmptyOnly, theWrapper, theAttrName)) {
						theNonEmptyFolderAtts.add(theFolderAttr);
					}
				}
			}
		}
	
		Collections.sort(theNonEmptyFolderAtts, DisplayAnnotations.PART_ORDER);
	
		return theNonEmptyFolderAtts;

	}

	/**
	 * Check if the attribute value of a wrapper is a web folder and if the web folder is empty and compare 
	 * the result with the given nonEmptyOnly flag.
	 * 
	 * @param nonEmptyOnly	if true only non-empty folders will result in a return value 'true'
	 * @param aWrapper		the wrapper that has the web folder attribute
	 * @param anAttrName	the attribute name of the web folder attribute
	 * @return true if the web folder matches
	 */
	public static boolean checkEmpty(boolean nonEmptyOnly, Wrapper aWrapper, String anAttrName) {
		Object theValue = aWrapper.getValue(anAttrName);
		if (!(theValue instanceof WebFolder)) {
			return false;
		}
		
		WebFolder theFolder = (WebFolder) theValue;

		if (theFolder != null) {
			if (!nonEmptyOnly) {
				return true;
			}
			else {
				Collection<?> theChildren = theFolder.getContent();
				if (theChildren != null && !theChildren.isEmpty()) {
					return true;
				}
			}
		}
		
		return false;
	}

}

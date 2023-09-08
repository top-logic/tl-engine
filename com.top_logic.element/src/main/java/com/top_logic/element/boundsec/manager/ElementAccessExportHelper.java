/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.tree.TreeModel;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.attribute.AttributeClassifierManager;
import com.top_logic.element.boundsec.manager.rule.IdentityPathElement;
import com.top_logic.element.boundsec.manager.rule.PathElement;
import com.top_logic.element.boundsec.manager.rule.RoleProvider.Type;
import com.top_logic.element.boundsec.manager.rule.RoleRule;
import com.top_logic.element.boundsec.manager.rule.config.PathElementConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRuleConfig;
import com.top_logic.element.boundsec.manager.rule.config.RoleRulesConfig;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.compound.gui.admin.CompoundSecurityLayoutTreeModel;
import com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile.RolesProfileHandler;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.tool.boundsec.wrap.GroupResourceHelper;

/**
 * Provides the constants used for the xml representation of the security
 * and methods to export the certin security parts.
 *
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class ElementAccessExportHelper {

    public static final String XML_ATTRIBUTE_IS_GROUP = "isGroup";
    public static final String XML_TAG_MEMBER = "member";
    public static final String XML_ATTRIBUTE_VALUE = "value";
    public static final String XML_ATTRIBUTE_KEY = "key";
    public static final String XML_TAG_ENTRY = "entry";
    public static final String XML_TAG_LANGUAGE = "language";
    public static final String XML_TAG_GROUP = "group";
    public static final String XML_TAG_GROUPS = "groups";
    public static final String XML_TAG_SECURITY = "security";
    public static final String XML_TAG_ROLE = "role";
    public static final String XML_TAG_ACCESS = "access";
    public static final String XML_TAG_CLASSIFICATION = "classification";
    public static final String XML_TAG_AUTHORIZATION = "authorization";
    public static final String XML_TAG_CLASSIFIER = "classifier";
    public static final String XML_TAG_META_ATTRIBUTE = "metaAttribute";
    public static final String XML_ATTRIBUTE_NAME = "name";
    public static final String XML_TAG_META_ELEMENT = "metaElement";
    public static final String XML_TAG_CLASSIFICATIONS = "classifications";
    public static final String XML_TAG_PATH = "path";
    public static final String XML_ATTRIBUTE_DESCRIPTION = "decription";
    /**
     * Constructor. This class is a utility class, therefore the constructor is private
     */
    private ElementAccessExportHelper() {
        super();
    }

    /**
     * Exports the mapping from attributes to classifiers
     */
    private static void exportElementClassification(TagWriter out) throws IOException {
        out.beginTag(XML_TAG_CLASSIFICATIONS);

        ElementAccessManager theAM = (ElementAccessManager) AccessManager.getInstance();

        Collection theMetaElements = theAM.getSupportedMetaElements();
        for (Iterator theMEIt = theMetaElements.iterator(); theMEIt.hasNext();) {
            TLClass theMetaElement = (TLClass) theMEIt.next();

            boolean isClassified = false;

			for (Iterator theMAIt = TLModelUtil.getLocalMetaAttributes(theMetaElement).iterator(); theMAIt.hasNext();) {
                TLStructuredTypePart theMA = (TLStructuredTypePart) theMAIt.next();
                if (AttributeOperations.isClassified(theMA)) {
                    isClassified = true;
                    break;
                }
            }

            if ( ! isClassified ) {
                continue;
            }

            out.beginBeginTag(XML_TAG_META_ELEMENT);
            out.writeAttribute(XML_ATTRIBUTE_NAME, theMetaElement.getName());
            out.endBeginTag();

			for (Iterator theMAIt = TLModelUtil.getLocalMetaAttributes(theMetaElement).iterator(); theMAIt.hasNext();) {
                TLStructuredTypePart theMA = (TLStructuredTypePart) theMAIt.next();

                if ( ! AttributeOperations.isClassified(theMA)) {
                    continue;
                }

                out.beginBeginTag(XML_TAG_META_ATTRIBUTE);
                out.writeAttribute(XML_ATTRIBUTE_NAME, theMA.getName());
                out.endBeginTag();

                for (Iterator theCIt = AttributeOperations.getClassifiers(theMA).iterator(); theCIt.hasNext(); ) {
                    FastListElement theClassifier = (FastListElement) theCIt.next();

                    out.beginBeginTag(XML_TAG_CLASSIFIER);
                    out.writeAttribute(XML_ATTRIBUTE_NAME, theClassifier.getName());
                    out.endEmptyTag();

                }
                out.endTag(XML_TAG_META_ATTRIBUTE);
            }
            out.endTag(XML_TAG_META_ELEMENT);
        }

        out.endTag(XML_TAG_CLASSIFICATIONS);
    }

    /**
     * Exports the mapping from roles to classifiers
     */
    private static void exportRoleClassifiers(TagWriter out) throws Exception {
        out.beginTag(XML_TAG_AUTHORIZATION);

        AttributeClassifierManager theACM = AttributeClassifierManager.getInstance();
        for (Iterator theIt = theACM.getAllClassifiers().iterator(); theIt.hasNext(); ) {
            FastList theClassification = (FastList) theIt.next();

            out.beginBeginTag(XML_TAG_CLASSIFICATION);
            out.writeAttribute(XML_ATTRIBUTE_NAME, theClassification.getName());
            out.endBeginTag();

            for (Iterator theCIt = theClassification.elements().iterator(); theCIt.hasNext(); ) {
                FastListElement theClassifier = (FastListElement) theCIt.next();

                out.beginBeginTag(XML_TAG_CLASSIFIER);
                out.writeAttribute(XML_ATTRIBUTE_NAME, theClassifier.getName());
                out.endBeginTag();

                Map theRolesMap = ElementAccessHelper.getRolesMap(theClassifier);
                for (Iterator theAIt = theRolesMap.entrySet().iterator(); theAIt.hasNext(); ) {
                    Map.Entry theEntry = (Map.Entry) theAIt.next();
                    String     theAccessRight = (String)     theEntry.getKey();
                    Collection theRoles       = (Collection) theEntry.getValue();

                    out.beginBeginTag(XML_TAG_ACCESS);
                    out.writeAttribute(XML_ATTRIBUTE_NAME, theAccessRight);
                    out.endBeginTag();

                    for (Iterator theRIt = theRoles.iterator(); theRIt.hasNext(); ) {

                        BoundRole theRole = (BoundRole) theRIt.next();

                        out.beginBeginTag(XML_TAG_ROLE);
                        out.writeAttribute(XML_ATTRIBUTE_NAME, theRole.getName());
                        out.endEmptyTag();

                    }

                    out.endTag(XML_TAG_ACCESS);
                }


                out.endTag(XML_TAG_CLASSIFIER);
            }

            out.endTag(XML_TAG_CLASSIFICATION);
        }

        out.endTag(XML_TAG_AUTHORIZATION);
    }

    /**
	 * Exports the rules determining the roles
	 *
	 * @param someRules
	 *        the rules to export Map < {@link TLClass}, Collection < {@link RoleRule} > >
	 */
    private static void exportRules(TagWriter out, Map someRules) throws Exception {

        out.beginTag(RoleRulesConfig.XML_TAG_ROLE_RULES);

		if (!someRules.isEmpty()) {
			out.beginTag(RoleRulesConfig.XML_TAG_RULES);
		}
        for (Iterator theIt = someRules.entrySet().iterator(); theIt.hasNext();) {
            Map.Entry   theEntry = (Map.Entry) theIt.next();
            Collection  theRules = (Collection) theEntry.getValue();

            if (theRules == null || theRules.isEmpty()) {
                continue;
            }

            for (Iterator theRuleIt = theRules.iterator(); theRuleIt.hasNext();) {
                RoleRule theRule = (RoleRule) theRuleIt.next();
                TLClass theME = theRule.getMetaElement();
                MetaObject theMO = theRule.getMetaObject();
                TLClass theSourceME = theRule.getSourceMetaElement();
                MetaObject theSourceMO = theRule.getSourceMetaObject();
                BoundRole theRole = theRule.getRole();
                BoundRole theSourceRole = theRule.getSourceRole();
                Type theType = theRule.getType();
                String theBase = theRule.getBaseString();

                out.beginBeginTag(RoleRulesConfig.XML_TAG_RULE);

                if (theME != null) {
					out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_META_ELEMENT, TLModelUtil.qualifiedName(theME));
                }
                if (theMO != null) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_META_OBJECT, theMO.getName());
                }
                if (theSourceME != null) {
					out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_ELEMENT, TLModelUtil.qualifiedName(theSourceME));
                }
                if (theSourceMO != null) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_META_OBJECT, theSourceMO.getName());
                }
                if (theRole != null) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_ROLE, theRole.getName());
                }
                if (theSourceRole != null) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_SOURCE_ROLE, theSourceRole.getName());
                }
                out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_INHERIT, theRule.isInherit());
                if (theType != null) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_TYPE, theType.toString());
                }
                if (!StringServices.isEmpty(theBase)) {
                    out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_BASE, theBase);
                }
                if (theRule.getResourceKey() != null) {
					out.writeAttribute(RoleRuleConfig.XML_ATTRIBUTE_RESOURCE_KEY, theRule.getResourceKey().getKey());
                }
                out.endBeginTag();

				List<PathElement> path = theRule.getPath();
				if (!path.isEmpty()) {
					out.beginTag(RoleRuleConfig.XML_TAG_PATH_ELEMENT);
				}
				for (PathElement thePE : path) {
                    if (thePE instanceof IdentityPathElement) continue;
                    TLStructuredTypePart theMA = thePE.getMetaAttribute();
                    String theKA = thePE.getAssociation();

					out.beginBeginTag(RoleRuleConfig.XML_TAG_STEP_ELEMENT);
                    if (theMA != null) {
						out.writeAttribute(PathElementConfig.XML_ATTRIBUTE_META_ELEMENT,
							TLModelUtil.qualifiedName(theMA.getType()));
                        out.writeAttribute(PathElementConfig.XML_ATTRIBUTE_ATTRIBUTE,    theMA.getName());
                    }
                    if (!StringServices.isEmpty(theKA)) {
                        out.writeAttribute(PathElementConfig.XML_ATTRIBUTE_ASSOCIATION,  theKA);
                    }
                    out.writeAttribute(PathElementConfig.XML_ATTRIBUTE_INVERSE,          thePE.isInverse());
                    out.endEmptyTag();
                }
				if (!path.isEmpty()) {
					out.endTag(RoleRuleConfig.XML_TAG_PATH_ELEMENT);
				}

                out.endTag(RoleRulesConfig.XML_TAG_RULE);
            }

        }
		if (!someRules.isEmpty()) {
			out.endTag(RoleRulesConfig.XML_TAG_RULES);
		}
        out.endTag(RoleRulesConfig.XML_TAG_ROLE_RULES);
    }

    /**
	 * Exports the current groups including the mapping to persons
	 */
    private static void exportGroups(TagWriter out) throws Exception {

        out.beginTag(XML_TAG_GROUPS);

        for (Iterator theIt = Group.getAll().iterator(); theIt.hasNext(); ) {

            Group theGroup = (Group) theIt.next();

            if (theGroup.isRepresentativeGroup()) {
                // representative groups are not handled here
                continue;
            }

            out.beginBeginTag(XML_TAG_GROUP);
            out.writeAttribute(XML_ATTRIBUTE_NAME, theGroup.getName());
            out.endBeginTag();

			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String theName = GroupResourceHelper.findI18NName(theGroup, locale);
				String theDesc = GroupResourceHelper.findI18NDescription(theGroup, locale);
                out.beginBeginTag(XML_TAG_LANGUAGE);
				out.writeAttribute(XML_ATTRIBUTE_NAME, locale.toString());
                out.endBeginTag();

                if (theName != null) {
                    out.beginBeginTag(XML_TAG_ENTRY);
                    out.writeAttribute(XML_ATTRIBUTE_KEY,   GroupResourceHelper.I18N_INFIX_NAME);
                    out.writeAttribute(XML_ATTRIBUTE_VALUE, theName);
                    out.endEmptyTag();
                }
                if (theDesc != null) {
                    out.beginBeginTag(XML_TAG_ENTRY);
                    out.writeAttribute(XML_ATTRIBUTE_KEY,   GroupResourceHelper.I18N_INFIX_DESCRIPTION);
                    out.writeAttribute(XML_ATTRIBUTE_VALUE, theDesc);
                    out.endEmptyTag();
                }
                out.endTag(XML_TAG_LANGUAGE);
            }

            for (Iterator theMemberIt = theGroup.getMembers().iterator(); theMemberIt.hasNext();) {
                Wrapper theMember = (Wrapper) theMemberIt.next();
                String  theName   = theMember.getName();
                boolean isGroup   = theMember instanceof Group;
                out.beginBeginTag(XML_TAG_MEMBER);
                out.writeAttribute(XML_ATTRIBUTE_NAME,    theName);
                if (isGroup) {
                    out.writeAttribute(XML_ATTRIBUTE_IS_GROUP, Boolean.toString(isGroup));
                }
                out.endEmptyTag();
            }
            out.endTag(XML_TAG_GROUP);
        }
        out.endTag(XML_TAG_GROUPS);
    }

    private static void exportProfiles(TagWriter out) throws Exception {

        out.beginTag(RolesProfileHandler.PROFILES_TAG);

        RolesProfileHandler theRPH = new RolesProfileHandler();
		TreeModel theTreeModel = new CompoundSecurityLayoutTreeModel(BoundHelper.getInstance().getRootChecker());
        Collection theStructureNames = ((ElementAccessManager) AccessManager.getInstance()).getStructureNames();
        for (Iterator theIt = theStructureNames.iterator(); theIt.hasNext();) {
            String      theStructureName = (String) theIt.next();
			TLModule theRoot = TLModelUtil.findModule(theStructureName);
            List        theRoles         = new ArrayList(BoundHelper.getInstance().getPossibleRoles(theRoot));
            Collections.sort(theRoles);

            theRPH.writeProfilesFor(out, theRoles, theTreeModel);
        }

        out.endTag(RolesProfileHandler.PROFILES_TAG);
    }

    /**
     * Export the security. Depending on the given flags cetain parts are suppressed
     * from the export.
     *
     * @param out                     the destination to write to
     * @param exportClassifications   <code>true</code> indicates export of that the mapping from attributes to classifiers
     * @param exportClassifiers       <code>true</code> indicates export of that the mapping from roles to classifiers
     * @param exportRules             <code>true</code> indicates export of that the role rules
     */
    public static void export(TagWriter out, boolean exportClassifications, boolean exportClassifiers, boolean exportRules, boolean exportProfiles, boolean exportGroups) throws Exception {

        out.beginTag(XML_TAG_SECURITY);
        if (exportClassifications) {
            exportElementClassification(out);
        }
        if (exportClassifiers) {
            exportRoleClassifiers(out);
        }
        if (exportRules) {
            exportRules(out, ((ElementAccessManager) ElementAccessManager.getInstance()).getRules());
        }
        if (exportProfiles) {
            exportProfiles(out);
        }
        if (exportGroups) {
            exportGroups(out);
        }
        out.endTag(XML_TAG_SECURITY);
    }

}

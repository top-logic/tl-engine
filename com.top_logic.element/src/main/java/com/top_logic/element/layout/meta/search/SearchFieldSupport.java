/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.base.security.SecurityContext;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.layout.meta.search.AttributedSearchComponent.DerivedAttributeDescription;
import com.top_logic.element.layout.provider.MetaElementLabelProvider;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.LegacyTypeCodes;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.element.meta.query.FulltextFilter;
import com.top_logic.element.meta.query.MetaAttributeFilter;
import com.top_logic.element.meta.query.PublishedFlexWrapperLabelProvider;
import com.top_logic.element.meta.query.StoredQuery;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.knowledge.wrap.mapBasedPersistancy.MapBasedPersistancySupport;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.component.ControlComponent;
import com.top_logic.layout.form.AddedListener;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.constraints.SelectionSizeConstraint;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.resources.TLTypeResourceProvider;
import com.top_logic.model.util.TLModelI18N;
import com.top_logic.model.util.TLModelNamingConvention;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.wrap.Group;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.Utils;
import com.top_logic.util.resource.ResourceMapMapping;

/**
 * Supporting class for accessing and creating fields in a search component.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SearchFieldSupport {

    public static final String STORED_QUERY = "storedQuery";

    public static final String META_ELEMENT = "metaElement";

    public static final String FULL_TEXT = "fullText";

    public static final String TABLE_COLUMNS = "tableColumns";

    public static final String FULLTEXT_PARAM_MODE = "fullTextMode";
    
    public static final String FULLTEXT_PARAM_EXACT_MATCH = "fullTextExactMatch";


	public static final Property<ControlComponent> CONTROL_COMPONENT_KEY =
		TypedAnnotatable.property(ControlComponent.class, "CONTROL_COMPONENT_KEY");

    /**
     * Add the search input fields for the given meta element.
     *
     * @param    aContext      The form context, must not be <code>null</code>.
     * @param    aSearchME     The meta element to create a search UI for, may be <code>null</code>.
     * @param    aQuery        The stored query that may contain further filters to be included, may be <code>null</code>.
     * @param    anExcludeSet  The set of attribute names to be excluded from search, must not be <code>null</code>.
     * @param    useRelevantNegate    Flag, if relevant and negate fields have to be created too.
     * @return   The list of attribute names added to the form context, may be <code>null</code>.
     */
    public List addAttributesFromMetaElement(ControlComponent aComp, AttributeFormContext aContext, TLClass aSearchME, StoredQuery aQuery, Set anExcludeSet, boolean useRelevantNegate, Collection someDerivedAttributes) {
        if (aSearchME == null) {
			Logger.warn("No type available for the search!", SearchFieldSupport.class);

            return null; // is empty....
        }

        // 1. Get all meta attributes known to the meta element and calculate the size of the
        // collection to be created.
		Collection<TLStructuredTypePart> theAttrs = TLModelUtil.getMetaAttributes(aSearchME);
        List theAddedAtts = new ArrayList(theAttrs.size());

        // 2. If there is a stored query, collect the filters defined in it.
        Collection theFilters = null;

        if (aQuery != null) {
			theFilters = aQuery.getFilters();
        }

		aContext.set(SearchFieldSupport.CONTROL_COMPONENT_KEY, aComp);

        // 3. Loop through the attributes and create the AttributeUpdates for each of them.
        // This will only be done for those, who are not listed in the exclude list or
        // that are excluded from the GUI context.
        for (Iterator<TLStructuredTypePart> theIt = theAttrs.iterator(); theIt.hasNext(); ) {
            TLStructuredTypePart theMA      = theIt.next();
            String        theAttName = theMA.getName();

            // only attributes that are neither in any exclude list nor of type webfolder are used
			if (!anExcludeSet.contains(theAttName) && !isExcludeFromSearch(theMA)) {
				this.createUpdate4MetaAttribute(aContext, theAddedAtts, theFilters, aSearchME, theMA, theAttName,
					useRelevantNegate, null);
            }
        }

        for (Iterator<DerivedAttributeDescription> theIt = someDerivedAttributes.iterator(); theIt.hasNext();) {
            DerivedAttributeDescription theDAD = theIt.next();
            TLStructuredTypePart theMA         = theDAD.getTarget();
            String        theAttName    = theMA.getName();
            String        theAccessPath = theDAD.getAccessPath();
			this.createUpdate4MetaAttribute(aContext, theAddedAtts, theFilters, aSearchME, theMA, theAttName,
				useRelevantNegate, theAccessPath);
        }

        return (theAddedAtts);
    }

    /**
     * Get a form field for the StoredQueries
     *
     * @param    aQuery    the currently selected query. May be <code>null</code>
     * @return   A FormField for the StoredQueries
     */
    public static SelectField getStoredQueryConstraint(TLClass aME, StoredQuery aQuery) {
		{
        	Person            thePerson = TLContext.getContext().getCurrentPersonWrapper();
            List              theSQs    = StoredQuery.getStoredQueries(aME, thePerson, true);
            List<StoredQuery> theSel    = (aQuery != null) ? Collections.singletonList(aQuery) : Collections.<StoredQuery>emptyList();
            SelectField       theField  = FormFactory.newSelectField(STORED_QUERY, theSQs, false, theSel, false);

            theField.setOptionLabelProvider(PublishedFlexWrapperLabelProvider.INSTANCE);
            theField.addValueListener(ChangeStoredQueryListener.INSTANCE);

            return theField;
        }
    }

    /**
     * Get a form field for selecting a meta element to look up.
     *
     * @param    aList           The list of possible meta elements, must not be <code>null</code> or empty.
     * @param    aMetaElement    The initial meta element to be selected, must not be <code>null</code>.
     * @return   The requested form field, never <code>null</code>.
     * @see      ChangeMetaElementListener
     */
    public FormField getMetaElementField(List aList, TLClass aMetaElement) {
        if (CollectionUtil.isEmptyOrNull(aList)) {
            throw new IllegalArgumentException("Given list of meta elements is null or empty.");
        }
        else if (aMetaElement == null) {
            throw new IllegalArgumentException("Given meta element is null.");
        }
        else {
            SelectField theField = FormFactory.newSelectField(META_ELEMENT, aList, false, Collections.singletonList(aMetaElement), false);

            theField.setMandatory(true);
            theField.setOptionLabelProvider(new TLTypeResourceProvider());
            theField.addValueListener(ChangeMetaElementListener.INSTANCE);
			SelectFieldUtils.setCustomOrderComparator(theField);

            return theField;
        }
    }

    /**
     * Return the field for a full text search.
     *
     * @return    The requested full text search field, never <code>null</code>.
     */
    public FormField getFullTextField(String aText) {
        StringField theField = FormFactory.newStringField(SearchFieldSupport.FULL_TEXT, aText, false);

		InputColorListener.addTo(theField);

        return theField;
    }

    /**
     * Return the list of column names extracted from the given select field.
     *
     * @param    aField    The field to get the column names from, must not be <code>null</code>.
     * @return   The list of column names, if the given field is a {@link SelectField}, <code>null</code> otherwise.
     */
    public List<String> getColumnList(FormField aField, TLClass aME) {
        if (aField instanceof SelectField) {
            return (List) ((SelectField) aField).getValue();
        }
        else {
            return null;
        }
    }

	/**
	 * Return the field defining the columns to be displayed in the search result.
	 * 
	 * @param someDefaultColumns
	 *        The columns configured in XML, may be be <code>null</code>.
	 * @param aME
	 *        The meta element to get the possible column names from, must not be <code>null</code>.
	 * @param excludedAttributes
	 *        Attribute names to exclude from being selectable.
	 * 
	 * @return The requested form field, may be <code>null</code>.
	 */
	public FormField getColumnSelectField(List<String> someDefaultColumns, TLClass aME,
			Collection<String> someAdditionalResultColumnOptions, Collection<String> excludedAttributes) {
		if ((someDefaultColumns != null) && !someDefaultColumns.isEmpty()) {
			Collection<TLStructuredTypePart> theMAs = TLModelUtil.getMetaAttributes(aME);
            List<String>       theOrigVals        = new ArrayList<>(someDefaultColumns);
			ResKey thePrefix = TLModelNamingConvention.getTypeLabelKey(aME);
            List<String>       theCurrent         = new ArrayList<>(theOrigVals);
            Collection<String>  allAttributeNames     = new HashSet<>(someAdditionalResultColumnOptions);
			Map<String, ResKey> theRealAttributeNames = new HashMap<>();
            
            for (Iterator<String> theIT = someAdditionalResultColumnOptions.iterator(); theIT.hasNext();) {
            	String theKey = theIT.next();
				theRealAttributeNames.put(theKey, thePrefix.suffix(theKey));
            }
            // Look up the fully qualified column names from meta schema
            // and replace the values in the list "theCurrent"
            // skip if the Attribute is of type Webfolder
            for (Iterator<TLStructuredTypePart> theIt = theMAs.iterator(); theIt.hasNext();) {
                TLStructuredTypePart theMA   = theIt.next();
                
                if (isExcludeFromSearch(theMA)) {
					continue;
                }
				String theName = theMA.getName();
				ResKey theRealName = TLModelI18N.getI18NKey(theMA);
				allAttributeNames.add(theName);
				theRealAttributeNames.put(theName, theRealName);
            }
            allAttributeNames.addAll(theCurrent);

            // Look up the fully qualified column names from meta schema
            // and replace the values in the list "theCurrent"
            for (Iterator<String> theIt = allAttributeNames.iterator(); theIt.hasNext();) {
                String theName = theIt.next();

                if (theOrigVals.contains(theName)) {
                    theOrigVals.remove(theName);
                    
                    if (excludedAttributes.contains(theName)) {
                    	theCurrent.remove(theName);
                    	theRealAttributeNames.remove(theName);
                    }
                    if (!theRealAttributeNames.containsKey(theName)) {
						theRealAttributeNames.put(theName, thePrefix.suffix(theName));
                    }
                }
                else {
                    if (excludedAttributes.contains(theName)) {
                    	theRealAttributeNames.remove(theName);
                    }
                }
            }

            List<String> forSort = new ArrayList<>(theRealAttributeNames.keySet());

			ResourceMapMapping<String> rmm = new ResourceMapMapping<>(theRealAttributeNames);
            Comparator comp = Collator.getInstance();
            
			MappingSorter.sortByMappingInline(forSort, rmm, comp);

            SelectField theColumnField = FormFactory.newSelectField(TABLE_COLUMNS, forSort, true, false);

            theColumnField.setValue(theCurrent);
            theColumnField.setOptionLabelProvider(new MetaElementLabelProvider(DefaultResourceProvider.INSTANCE, aME, thePrefix));
            theColumnField.setOptionComparator(new MappedComparator(rmm, comp));
            theColumnField.setCustomOrder(true);
            theColumnField.addConstraint(new SelectionSizeConstraint(1));

            return theColumnField;
        }
        else {
            return null;
        }
    }

	private boolean isExcludeFromSearch(TLStructuredTypePart theMA) {
		if (!AttributeSettings.isSearchRelevant(theMA)) {
			return true;
		}
		if (DisplayAnnotations.isHidden(theMA)) {
			return true;
		}
		return false;
	}

	public FormGroup getPublishingFormGroup(AttributedSearchComponent aComponent, ResPrefix resPrefix) {
		FormGroup theGroup = new FormGroup(QueryUtils.FORM_GROUP, resPrefix);

		// The publishing Group is only visible if the selected query is
		// already published and if the current user equals the
		// owner of the selected query.
		StoredQuery theQuery = aComponent.getStoredQuery();

		SelectField visibleGroupsField = FormFactory.newSelectField(QueryUtils.VISIBLE_GROUPS_FIELD, Collections.EMPTY_LIST, true, false);
		BooleanField publishQueryField = FormFactory.newBooleanField(QueryUtils.PUBLISH_QUERY_FIELD);
		
		theGroup.addMember(visibleGroupsField);
		theGroup.addMember(publishQueryField);
		
		if (theQuery != null && QueryUtils.allowWriteAndPublish(aComponent)) {
			{
				List   groupAssociations = MapBasedPersistancySupport.getGroupAssociation(theQuery);
				Person theCreator        = theQuery.getCreator();
				Person theCurrentUser    = TLContext.getContext().getCurrentPersonWrapper();
				
				List visibleGroups = Group.getAll();
				visibleGroupsField.setOptions(visibleGroups);// = new SelectField(QueryUtils.VISIBLE_GROUPS_FIELD, visibleGroups, true, false);

				// root or owner can modify published queries
				// for the owner the unversioned identity must be compared, otherwise it wont be equal
				// even for the "same" person
				if (WrapperHistoryUtils.getUnversionedIdentity(theCurrentUser).equals(WrapperHistoryUtils.getUnversionedIdentity(theCreator)) || SecurityContext.isAdmin()) {
					boolean isPublished;
					
					if (groupAssociations.isEmpty()) {
						isPublished = false;
					}
					else {
						isPublished = true;
						visibleGroupsField.setAsSelection(groupAssociations);
					}
					theGroup.setVisible(true);
					theGroup.setDisabled(false);
					
					publishQueryField.setAsBoolean(isPublished);
					visibleGroupsField.setDisabled(!isPublished);
					visibleGroupsField.setMandatory(isPublished);
					visibleGroupsField.setVisible(true);
				}
				else {
					theGroup.setDisabled(true);
					theGroup.setMandatory(false);
					theGroup.setVisible(false);
					visibleGroupsField.setVisible(false);
				}
				
				visibleGroupsField.addValueListener(InputListener.INSTANCE);
				publishQueryField.addValueListener(new DisableListener(visibleGroupsField));
			}
		}
		else {
			theGroup.setVisible(false);
			theGroup.setDisabled(true);
		}

		
		return theGroup;
	}

    /** 
     * Create a attribute update for the given meta attribute.
     * 
     * @param someFilters         a {@link List} of active filters for the current search
     * @param anMA                the current {@link TLStructuredTypePart}
     * @param anAttName           the name of the attribute for which the field(s) should be created
     * @param useRelevantNegate   flag to decide whether the checkboxes for relevant and negate should be created or not.
     */
	protected void createUpdate4MetaAttribute(AttributeFormContext formContext, List someAdded, Collection someFilters,
			TLStructuredType type, TLStructuredTypePart anMA, String anAttName, boolean useRelevantNegate,
			String anAccessPath) {
        AttributeUpdate theUpdate     = null;
        boolean         createUpdate  = true;
        boolean         isMAFRelevant = false;
        boolean         isMAFNegative = false;
		AttributedSearchComponent theComp = (AttributedSearchComponent) formContext.get(CONTROL_COMPONENT_KEY);

		if (anMA.getType().getModelKind() == ModelKind.DATATYPE && anMA.isMultiple()) {
			if (AttributeOperations.getMetaAttributeType(anMA) != LegacyTypeCodes.TYPE_STRING_SET) {
				// Not supported.
				return;
			}
		}

        // Try to get update from StoredQuery
        if (someFilters != null) {
            MetaAttributeFilter theMAF = StoredQuery.getFilterFor(someFilters, anMA , anAccessPath);

            if (theMAF != null) {
            	isMAFRelevant = theMAF.isRelevant();
            	isMAFNegative = theMAF.getNegate();
				theUpdate = theMAF.getSearchValuesAsUpdate(formContext.getAttributeUpdateContainer(), type, anAccessPath);
                createUpdate = false;
            }
        }

        // Try to create it if it is still null
        if (theUpdate == null && createUpdate) {
			theUpdate =
				AttributeUpdateFactory.createAttributeUpdateForSearch(formContext.getAttributeUpdateContainer(),
					type, anMA, null, null, anAccessPath);
        }

        if (theUpdate != null) {
			FormMember theInput = formContext.addFormConstraintForUpdate(theUpdate);

            someAdded.add(anAttName);

            if (useRelevantNegate) {
            	if (theComp.getRelevantAndNegate()) {
					BooleanField theCheckbox =
						this.addRelevantAndNegateMember(formContext, theUpdate, isMAFNegative, isMAFRelevant);

					ChangeColorListener.addTo(theCheckbox);
            	}
                if (theInput instanceof FormField) {
                    ((FormField) theInput).addValueListener(InputListener.INSTANCE);
					InputColorListener.addTo((FormField) theInput);
                }
                else if (theInput instanceof FormContainer) {
                    for (Iterator<? extends FormMember> theIt = ((FormContainer) theInput).getMembers(); theIt.hasNext();) {
                    	FormMember theMember = theIt.next();

                        if (theMember instanceof FormField) {
                            ((FormField) theMember).addValueListener(InputListener.INSTANCE);
							InputColorListener.addTo((FormField) theMember, true);
                        }
                    }
                }
            }
            else if (theInput instanceof FormField) {
            	((FormField) theInput).addValueListener(InputListener.INSTANCE);
				InputColorListener.addTo((FormField) theInput);
            }
            else if (theInput instanceof FormContainer) {

                for (Iterator<? extends FormMember> theIt = ((FormContainer) theInput).getMembers(); theIt.hasNext();) {
                	FormMember theMember = theIt.next();

                    if (theMember instanceof FormField) {
                        ((FormField) theMember).addValueListener(InputListener.INSTANCE);
						InputColorListener.addTo((FormField) theMember, true);
                    }
                }
            }
        }
    }

    protected BooleanField addRelevantAndNegateMember(AttributeFormContext aFormCtx, AttributeUpdate anUpdate, boolean doNegate, boolean isRelevant) {
        Boolean       theNegateTristate = isRelevant ? Boolean.valueOf(!doNegate) : null;
        TLStructuredTypePart theMA             = anUpdate.getAttribute();
        BooleanField  theField          = FormFactory.newBooleanField(SearchFilterSupport.getRelevantAndNegateMemberName(theMA, anUpdate.getDomain()), theNegateTristate, false);

		AttributeFormFactory.setAttributeUpdate(theField, anUpdate);
        aFormCtx.addMember(theField);

        return theField;
    }

    public static BooleanField addRelevantAndNegateMember(AttributeFormContext aFormCtx, String aBasicID, boolean doNegate, boolean isRelevant) {
        Boolean      theNegateTristate = isRelevant ? Boolean.valueOf(!doNegate) : null;
        BooleanField theField = FormFactory.newBooleanField(SearchFilterSupport.getRelevantAndNegateMemberName(aBasicID), theNegateTristate, false);

        aFormCtx.addMember(theField);

        return theField;
    }

    public static class ChangeStoredQueryListener implements ValueListener {

        public static final ChangeStoredQueryListener INSTANCE = new ChangeStoredQueryListener();

        // Implementation of interface ValueListener

        @Override
		public void valueChanged(FormField aField, Object oldValue, Object newValue) {
			Object theProperty = aField.getFormContext().get(CONTROL_COMPONENT_KEY);
            StoredQuery theQuery = null;

            if (theProperty instanceof AttributedSearchComponent) {
                AttributedSearchComponent theComponent = (AttributedSearchComponent) theProperty;
                Object                    theNewValue  = CollectionUtil.getSingleValueFromCollection((List) newValue);

                if (theNewValue != null) {
                    boolean theCompMode = theComponent.isExtendedSearch();
                    Boolean isExtended  = (Boolean) ((StoredQuery) theNewValue).getValue(QueryUtils.IS_EXTENDED);

                    if (theCompMode != Utils.isTrue(isExtended)) {
                        theComponent.getSwitchSearchScopeCommand().switchSearchScope(theComponent);
                    }

					theComponent.setModel(theNewValue);
					theQuery = (StoredQuery) theNewValue;
                }
                else {
                    theComponent.resetStoredQuery();
                    aField.getFormContext().getMember(QueryUtils.FORM_GROUP).setVisible(false);

                }
                if (theComponent.hasFormContext()) {
                	AttributeFormContext theContext = (AttributeFormContext) theComponent.getFormContext();
                	SelectField          theField   = (SelectField) theContext.getField(STORED_QUERY);

                    theComponent.getSearchFilterSupport().fillFormContext(theContext, theQuery);
                    
                    theField.setDisabled(false);
                }

                // Invalidate the buttons if a change happens from
               	theComponent.invalidateButtons();
            }
        }
    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class ChangeMetaElementListener implements ValueListener {

        public static final ChangeMetaElementListener INSTANCE = new ChangeMetaElementListener();

        // Implementation of interface ValueListener

        @Override
		public void valueChanged(FormField aField, Object oldValue, Object newValue) {
			Object theProperty = aField.getFormContext().get(CONTROL_COMPONENT_KEY);

            if (theProperty instanceof AttributedSearchComponent) {
                AttributedSearchComponent theComponent = (AttributedSearchComponent) theProperty;
                Object                    theNewValue  = CollectionUtil.getSingleValueFromCollection((List) newValue);

                if (theNewValue != null) {
                    theComponent.setSearchMetaElement((TLClass) theNewValue);
                    theComponent.resetStoredQuery();
                    theComponent.fireModelModifiedEvent(theNewValue, theComponent);
                }

                theComponent.invalidateButtons();
            }

            aField.setDisabled(false);
        }
    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class ChangeColorListener implements ValueListener {

		private static final ChangeColorListener INSTANCE = new ChangeColorListener();

		public static void addTo(FormField member) {
        	member.addValueListener(INSTANCE);
			initialize(member, INSTANCE);
        }

        @Override
		public void valueChanged(FormField aCheckboxField, Object aOldValue, Object aNewValue) {
			AttributeUpdate theUpdate = AttributeFormFactory.getAttributeUpdate(aCheckboxField);
            FormContext      theContext = aCheckboxField.getFormContext();
            String           theName;

            if (theUpdate != null) {
                theName = MetaAttributeGUIHelper.getAttributeIDCreate(theUpdate.getAttribute(), theUpdate.getDomain());
            }
            else {
                int theLength = theContext.getName().length() + 1 + SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME.length();

                theName = aCheckboxField.getQualifiedName().substring(theLength);
            }

            if (theContext.hasMember(theName)) {
                FormMember theMember = theContext.getMember(theName);

                if (theMember instanceof FormField) {
					ControlComponent theComp = theContext.get(SearchFieldSupport.CONTROL_COMPONENT_KEY);

                    this.changeColor(theComp, (FormField) theMember, aNewValue);
                }
                else if (theMember instanceof FormContainer) {
					ControlComponent theComp = theContext.get(SearchFieldSupport.CONTROL_COMPONENT_KEY);

                    for (Iterator<? extends FormMember> theIt = ((FormContainer) theMember).getMembers(); theIt.hasNext();) {
                    	FormMember thePart = theIt.next();

                        if (thePart instanceof FormField) {
                            this.changeColor(theComp, (FormField) thePart, aNewValue);
                        }
                    }
                }
            }

			if (!Utils.equals(aOldValue, aNewValue) && theContext.hasMember(STORED_QUERY)) {
                SelectField theSelect = (SelectField) theContext.getField(STORED_QUERY);
                Object      theQuery  = CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());

                if (theQuery != null) {
                    theSelect.setDisabled(true);
                }
            }
        }

		public void changeColor(ControlComponent aComp, FormField field, Object newValue) {
			Boolean isPositive = (Boolean) newValue;
			updateCssClass(field, isPositive == null, isPositive);
        }

    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class InputListener implements ValueListener {

        public static final InputListener INSTANCE = new InputListener();

        //enforce use of INSTANCE 
        private InputListener() {
        	
        }
        // Implementation of interface ValueListener

        @Override
		public void valueChanged(FormField anInputField, Object aOldValue, Object aNewValue) {
            FormContext               theContext = anInputField.getFormContext();
			AttributedSearchComponent theComp =
				(AttributedSearchComponent) theContext.get(SearchFieldSupport.CONTROL_COMPONENT_KEY);
            
            if (theComp.supressListeners()) {
                return;
            }

			AttributeUpdate theUpdate = AttributeFormFactory.getAttributeUpdate(anInputField);
            String          theName;
            FormContainer   theContainer;
            if (theUpdate != null) {
                theName      = SearchFilterSupport.getRelevantAndNegateMemberName(theUpdate.getAttribute(), theUpdate.getDomain());
                theContainer = theContext;
            } else {
                theName = "relevant";
                theContainer = anInputField.getParent();
            }

            if (theContainer.hasMember(theName)) {
                boolean isEmpty = Utils.isEmpty(aNewValue);

                if (Utils.isEmpty(aOldValue) || isEmpty) {
                    final BooleanField theRelevantCheckField = (BooleanField) theContainer.getMember(theName);
                    if (theRelevantCheckField.getValue() == null) { // Relevance was not set, yet
                        FormMember theRelevantMember = getRelevantGroup(theRelevantCheckField, theUpdate, theContext);
                        boolean othersEmpty = true;
                        if (theRelevantMember instanceof FormContainer) {
                            FormContainer theRelevantGroup = (FormContainer) theRelevantMember;
    
    						// check if any other element belonging to the same
    						// search attribute (i.e. contained in the same group as
    						// the relevant check box) has a non empty value. in
    						// this case nothing changes because the relevant field
    						// is already set and the group does not become
    						// irrelevant
                            for (Iterator<? extends FormMember> theIt = theRelevantGroup.getMembers(); theIt.hasNext();) {
                                FormMember theMember = theIt.next();
                                
                                if ( theMember instanceof FormField ) {
                                    FormField theFF = (FormField) theMember;
                                    
                                    if ( ! theFF.equals(theRelevantCheckField)
                                      && ! theFF.equals(anInputField)
                                      &&   theFF.hasValue()
                                      && ! Utils.isEmpty(theFF.getValue())) {
                                        othersEmpty = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (othersEmpty) {
                            theRelevantCheckField.setValue(isEmpty ? null : Boolean.TRUE);
                        }
                    }
                }
            }

			if (theContext.hasMember(STORED_QUERY)) {
                SelectField theSelect = (SelectField) theContext.getField(STORED_QUERY);
                Object      theQuery  = CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());

                if (theQuery != null) {
                    theSelect.setDisabled(true);
                }
            }
        }
    }

    /**
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class GroupInputListener implements ValueListener {

        /**
         * Map holding the FormFields participating in the decision.
         *
         * Map < FormField, Mapping >
         * Key: the field checked
         * Value: a mapping to allow certain values to be append to null
         */
        private Map partners;

        // Constructors

        /**
         * Creates a {@link SearchFieldSupport}.GroupInputListener.
         */
        public GroupInputListener(Map somePartners) {
            this.partners = somePartners;
        }

        // Implementation of interface ValueListener

        @Override
		public void valueChanged(FormField anInputField, Object aOldValue, Object aNewValue) {

            BooleanField theRelevantField = this.getRelevantField(anInputField);

            FormContext     theContext = anInputField.getFormContext();

            if (theRelevantField != null) {

                Mapping theMapping = (Mapping) this.partners.get(anInputField);

                boolean isEmpty  = theMapping.map(aNewValue) == null;
                boolean wasEmpty = theMapping.map(aOldValue) == null;

                if (wasEmpty || isEmpty) {
                    theRelevantField.setValue(isEmpty ? null : Boolean.TRUE);
                }
            }

            if (theContext.hasMember(STORED_QUERY)) {
                SelectField theSelect = (SelectField) theContext.getField(STORED_QUERY);
                Object      theQuery  = CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());

                if (theQuery != null) {
                    theSelect.setDisabled(true);
                }
            }
        }

        private BooleanField getRelevantField(FormField anInputField) {
            return (BooleanField) anInputField.getParent().getField("relevant");
        }


    }

    public static class SimpleNullMapping implements Mapping {
        public static final SimpleNullMapping INSTANCE = new SimpleNullMapping();
        @Override
		public Object map(Object anInput) {
            return Utils.isEmpty(anInput) ? null : anInput;
        }
    }


    /**
     * Change the color of the inputFileds when change and disable the #STORED_QUERY field.
     *
     * @author    <a href=mailto:mga@top-logic.com>Michael Gänsler</a>
     */
    public static class InputColorListener implements ValueListener {

    	/**
    	 * Instance for normal use: only the field this listener is registered to is affected.
    	 */
		private static final InputColorListener INSTANCE = new InputColorListener(false);

        /**
		 * Instance for Fields in FormGroups. Each FormField within the same
		 * group is affected as well.
		 */
		private static final InputColorListener SIBLING_INSTANCE = new InputColorListener(true);

		public static void addTo(FormField field) {
			addTo(field, false);
		}

		public static void addTo(FormField field, boolean siblings) {
			ValueListener listener = siblings ? SIBLING_INSTANCE : INSTANCE;
			field.addValueListener(listener);
			initialize(field, listener);
		}

        // Implementation of interface ValueListener
        private boolean hasSiblings;

        private InputColorListener(boolean siblings) {
        	this.hasSiblings = siblings;
        }

        /**
         * @see com.top_logic.layout.form.ValueListener#valueChanged(com.top_logic.layout.form.FormField, java.lang.Object, java.lang.Object)
         */
        @Override
		public void valueChanged(FormField anInputField, Object aOldValue, Object aNewValue) {
            FormContext               theContext = anInputField.getFormContext();
			AttributedSearchComponent theComp =
				(AttributedSearchComponent) theContext.get(SearchFieldSupport.CONTROL_COMPONENT_KEY);
            
            if (theComp.supressListeners()) {
                return;
            }

			boolean isEmpty  = Utils.isEmpty(aNewValue);
			Boolean isChecked = Boolean.valueOf(!isEmpty);
			FormField theChkbox = null;

			if(hasSiblings) {
				FormContainer theParent = anInputField.getParent();
				String theChkBoxName = getCheckboxFromFieldName(theParent.getName());
				
				if (theContext.hasMember(theChkBoxName)) {
					theChkbox = theContext.getField(theChkBoxName);
					Object val = theChkbox.getValue();
					isChecked = val != null ? (Boolean) val : Boolean.TRUE;
				}
				boolean allChildrenEmpty = isEmpty;
				Iterator<? extends FormMember> theIt = theParent.getMembers();
				
				while( theIt.hasNext()) {
					FormMember theMember = theIt.next();

					if (theMember instanceof FormField) {
						FormField field = (FormField)theMember;
						if (field.hasValue()) {
							Object val = field.getValue();
							if (val != null) {
								allChildrenEmpty = false;
							}
						}
					}
				}
				isChecked = theChkbox == null ? Boolean.TRUE : isChecked;
				theIt     = theParent.getMembers();
				
				while( theIt.hasNext()) {
					FormMember theMember = theIt.next();

                    if (theMember instanceof FormField) {
                    	setStyle(theMember, allChildrenEmpty, isChecked);
                    }
				}
				isEmpty = allChildrenEmpty;
			}
			else {
				String theChkBoxName = getCheckboxFromFieldName(anInputField.getName());
				
				if (theContext.hasMember(theChkBoxName)) {
					theChkbox = theContext.getField(theChkBoxName);
					Object val = theChkbox.getValue();
					isChecked = val != null ? (Boolean) val : Boolean.TRUE;
				}
				setStyle(anInputField, isEmpty, isChecked);
			}

			if(theChkbox != null) {
				theChkbox.setValue(isEmpty ? null : isChecked);
			}

			if (!Utils.equals(aOldValue, aNewValue) && theContext.hasMember(STORED_QUERY)) {
                SelectField theSelect = (SelectField) theContext.getField(STORED_QUERY);
                Object      theQuery  = CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());

                if (theQuery != null) {
                    theSelect.setDisabled(true);
                }
            }
        }

		private void setStyle(FormMember field, boolean isEmpty, Boolean isChecked) {
			updateCssClass(field, isEmpty, isChecked);
		}

        private String getCheckboxFromFieldName(String aFieldName) {
        	int pos = aFieldName.indexOf("_");
			if (pos < 0) {
				return SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME + aFieldName;
			}
			return SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME + aFieldName.substring(pos);
        }
    }

	static void updateCssClass(FormMember field, boolean isEmpty, Boolean isPositive) {
		String newClass =
			isEmpty ? "searchIrrelevant" : isPositive.booleanValue() ? "searchPositive" : "searchNegative";
		field.removeCssClass("searchIrrelevant");
		field.removeCssClass("searchPositive");
		field.removeCssClass("searchNegative");
		field.addCssClass(newClass);
	}

    /**
	 * Used to disable/enable the referenceField based on the current value of a
	 * {@link BooleanField}.
     *
     * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
     */
    private class DisableListener implements ValueListener {
    	private FormField	referenceField;

    	public DisableListener(FormField aField) {
    		this.referenceField = aField;
    	}

    	@Override
		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
    		FormContext     theContext = aField.getFormContext();

    		if (((BooleanField) aField).getAsBoolean()) {
    			this.referenceField.setDisabled(false);
				this.referenceField.setMandatory(true);
    		}
    		else {
    			this.referenceField.setDisabled(true);
				this.referenceField.setMandatory(false);
    		}
    		if (theContext.hasMember(STORED_QUERY)) {
    			SelectField theSelect = (SelectField) theContext.getField(STORED_QUERY);
    			Object      theQuery  = CollectionUtil.getSingleValueFromCollection(theSelect.getSelection());

    			if (theQuery != null) {
    				theSelect.setDisabled(true);
    			}
    		}
    	}
    }

    public void addFullTextField(AttributeFormContext aContext, StoredQuery aQuery, boolean useRelevantAndNegate, boolean isAnd, boolean exactMatch) {
        Collection theFilters = null;
        
        if (aQuery != null) {
			theFilters = aQuery.getFilters();
        }

        // Full text search
        String  theFulltext = "";
        boolean theNeg      = false;
		boolean theRel = false;

        if (theFilters != null && !theFilters.isEmpty() ) {
            List ftFilters  = MapBasedPersistancySupport.getObjectsOfType(theFilters, FulltextFilter.class);

            if (ftFilters != null) {
                FulltextFilter theFilter = (FulltextFilter) ftFilters.get(0);
                theFulltext = theFilter.getPattern();
                isAnd       = theFilter.getMode();
                exactMatch  = theFilter.getExactMatch();
                theNeg      = theFilter.getNegate();
                theRel      = theFilter.isRelevant();

                if (ftFilters.size() > 1) {
                    Logger.warn("Found more than one FulltextFilter in a Query", this);
                }
            }
        }
        aContext.addMember(FormFactory.newBooleanField(FULLTEXT_PARAM_MODE, Boolean.valueOf(isAnd), false));
        aContext.addMember(FormFactory.newBooleanField(FULLTEXT_PARAM_EXACT_MATCH, Boolean.valueOf(exactMatch), false));
        aContext.addMember(this.getFullTextField(theFulltext));

        BooleanField theCheckbox = SearchFieldSupport.addRelevantAndNegateMember(aContext, SearchFieldSupport.FULL_TEXT, theNeg, theRel);

		ChangeColorListener.addTo(theCheckbox);
    }

	/**
	 * Returns the full text field formerly added by
	 * {@link #addFullTextField(AttributeFormContext, StoredQuery, boolean, boolean, boolean)}.
	 * 
	 * @param formContext
	 *        The {@link FormContext} to which {@link #getFullTextField(String)} was added.
	 */
	public FormField findFullTextField(AttributeFormContext formContext) {
		return formContext.getField(FULL_TEXT);
	}

	/**
	 * Returns the {@link SelectField} in the given {@link FormContext} to select the search result
	 * columns.
	 * 
	 * @return The {@link SelectField} to select the search result columns or <code>null</code>,
	 *         when there is no such field.
	 */
    public static SelectField getTableColumnsFields(FormContext context) {
		if (context.hasMember(TABLE_COLUMNS)) {
			return (SelectField) context.getMember(TABLE_COLUMNS);
		}
		return null;
	}

	private static FormMember getRelevantGroup(FormField aCheckboxField,
            AttributeUpdate theUpdate, FormContext theContext) {
        FormMember theMember;
        String           theName;

        if (theUpdate != null) {
			theName = MetaAttributeGUIHelper.getAttributeIDCreate(theUpdate.getAttribute());
        }
        else {
            int theLength = theContext.getName().length() + 1 + SearchFilterSupport.RELEVANTANDNEGATE_CONSTRAINT_NAME.length();

            theName = aCheckboxField.getQualifiedName().substring(theLength);
        }

        if (theContext.hasMember(theName)) {
            theMember = theContext.getMember(theName);
        } else {
            // the relevant checkbox is enclosed by a form container
            // this container holds all attributes that are to be colorized
            theMember = aCheckboxField.getParent();
        }
        return theMember;
    }

    public static class I18NComparator implements Comparator {

		private ResPrefix resPrefix;
        private boolean descending;
        private Comparator baseComparator;

		public I18NComparator(ResPrefix aResPrefix) {
            this(aResPrefix, false);
        }

		public I18NComparator(ResPrefix aResPrefix, boolean sortDescending) {
            this.resPrefix  = aResPrefix;
            this.descending = sortDescending;
            this.baseComparator = Collator.getInstance(TLContext.getLocale());
        }

        @Override
		public int compare(Object aO1, Object aO2) {
            Resources theRes = Resources.getInstance();

            if (aO1 instanceof String && aO2 instanceof String) {
				String theR1 = resPrefix == null ? (String) aO1 : theRes.getString(resPrefix.key((String) aO1));
				String theR2 = resPrefix == null ? (String) aO2 : theRes.getString(resPrefix.key((String) aO2));
                if (descending) {
//                    return theR1.compareToIgnoreCase(theR2);
                	return this.baseComparator.compare(theR1, theR2);
                }
                else {
//                    return theR2.compareToIgnoreCase(theR1);
                	return this.baseComparator.compare(theR2, theR1);
                }
            }
            return 0;
        }
    }

	/**
	 * Calls the given {@link ValueListener} with the given field's value, when the field is first
	 * attached to it's form context.
	 */
	static void initialize(FormField field, ValueListener listener) {
		if (field.getFormContext() != null) {
			doInitialize(field, listener);
		} else {
			FormMember top = withoutParent(field);

			top.addListener(FormField.ADDED_TO_PARENT, new AddedListener() {
				@Override
				public Bubble handleAddedToParent(FormMember sender, FormContainer newParent) {
					if (sender == top) {
						initialize(field, listener);

						top.removeListener(FormField.ADDED_TO_PARENT, this);
					}
					return Bubble.BUBBLE;
				}
			});
		}
	}

	private static FormMember withoutParent(FormField field) {
		FormMember last = field;
		while (last.getParent() != null) {
			last = last.getParent();
		}
		return last;
	}

	static void doInitialize(FormField field, ValueListener listener) {
		Object value = field.getValue();
		listener.valueChanged(field, value, value);
	}

}


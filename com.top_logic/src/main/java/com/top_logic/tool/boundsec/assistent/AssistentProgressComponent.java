/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.assistent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.Log;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * Showing progress of an AssistentComponent.
 * Just put it in a layout and configure the master as the AssistentComponent.
 * 
 * This components shows the major steps of the AssistentComponent. To configure them use the 
 * right attribute while defining AssistentStepInfos in your XML.
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class AssistentProgressComponent extends LayoutComponent
		implements HTMLConstants, ComponentChannel.ChannelListener {

	private AssistentProgressModel _progressModel;

	public interface Config extends LayoutComponent.Config {
		@Name("horizontal")
		@BooleanDefault(false)
		boolean getHorizontal();
	}

    /** Switch between old, horizontal=false and new , horizontal=true design */
    private boolean horizontal;

    /**
     * Create AssistentProgressComponent from XML.
     */
    public AssistentProgressComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
        horizontal = atts.getHorizontal();
    }
    
    /** here the content is written */
    @Override
	public void writeBody(ServletContext context, HttpServletRequest req,
            HttpServletResponse resp, TagWriter out) throws IOException, ServletException {
        if (horizontal)
            writeHorizontal(req, out);
        else
            writeVertical(req, out);
    }

    /** 
     * Write the Progress of the {@link AssistentComponent} in a vertical Table.
     */
    protected void writeVertical(HttpServletRequest req, TagWriter out) throws IOException {
		String spacerBack = req.getContextPath() + this.getTheme().getValue(Icons.SPACER_RIGHT).getFileLink();
        
        HTMLUtil.beginTable(out, "Outer Asssistant Progress", 0);
        HTMLUtil.beginTr(out);
        HTMLUtil.beginTd(out);
            HTMLUtil.beginTable(out, "Inner Asssistant Progress", 0);
            this.writeRows(out);
            HTMLUtil.endTable(out);
        HTMLUtil.endTd(out);
		HTMLUtil.beginBeginTd(out);
		out.writeAttribute(WIDTH_ATTR, "100%"); // push vertical bar to right
		HTMLUtil.endBeginTd(out);
            HTMLUtil.endTd(out);       	
		HTMLUtil.beginBeginTd(out);
		out.writeAttribute(STYLE_ATTR, "background-image:url(" + spacerBack + "); width:2px;");
		HTMLUtil.endBeginTd(out);
            HTMLUtil.endTd(out);
        HTMLUtil.endTr(out);
        HTMLUtil.endTable(out);
    }
    
    /** all rows */
    private void writeRows(TagWriter out) throws IOException {
        
        int count = this.getProgressModel().orderedSteps.size();
        for(int i=0; i<count; i++) {
            String theKey = (String) this.getProgressModel().orderedSteps.get(i);
            HTMLUtil.beginTr(out);
            HTMLUtil.beginTd(out);
            this.writeEntry(out, theKey);
            HTMLUtil.endTd(out);
            HTMLUtil.endTr(out);
        }
    }

    /** a single entry */
    private void writeEntry(TagWriter out, String aKey) throws IOException {
        
        String theText = (String) this.getProgressModel().majorSteps.get(aKey);
        boolean isSelected = this.getProgressModel().isSelected(aKey);
        
        if(isSelected) {
            theText = "<strong>" + theText + "</strong>";
        }
        
        out.writeContent("<p style='font-size:14px;'>" + theText + "</p>");
    }

    /** 
     * Write the Progress of the {@link AssistentComponent} in some horizontzal strcuture.
     */
    protected void writeHorizontal(HttpServletRequest req, TagWriter aOut) throws IOException {
        // Div start
        aOut.beginBeginTag(DIV);
        aOut.writeAttribute(CLASS_ATTR, "assistantContent");
        aOut.writeAttribute(ALIGN_ATTR, "center");
        aOut.endBeginTag();
        
        // List start
        aOut.beginBeginTag(OL);
        aOut.writeAttribute(CLASS_ATTR, "assistantList");
        aOut.endBeginTag();
        
        int count = this.getProgressModel().orderedSteps.size();
        String  selectClass = " assistantPrev";
        boolean selected    = getProgressModel().nothingSelected();
        
        for(int i=0; i<count; i++) {
            String theKey = (String) this.getProgressModel().orderedSteps.get(i);
            
            // List element start
            String classStr = "assistantElement";
            aOut.beginBeginTag(LI);
            if (i == 0) {
                classStr += " assistantFirstElement";
            }
            
            if (getProgressModel().isSelected(theKey)) {
                classStr += " assistantCurrentStep";
                selectClass = " assistantNext";
                selected = true;
            } else {
                classStr += selectClass;
            }
    
            aOut.writeAttribute(CLASS_ATTR, classStr);
            aOut.endBeginTag();
            
            String theText = (String) this.getProgressModel().majorSteps.get(theKey);
            aOut.writeContent(theText);
            
            ThemeImage themeImage = null;
            
            if(!selected){
            	themeImage = Icons.STEP_FINISHED;
            }
            else if(selected && getProgressModel().isSelected(theKey)){
            	themeImage = Icons.STEP_ACTUAL;
            }
            else if(selected && !getProgressModel().isSelected(theKey)){
            	themeImage = Icons.STEP_OPEN;
            }
            
            if(themeImage != null){
            	// Image
            	aOut.writeText(" ");
				themeImage.writeWithCss(DefaultDisplayContext.getDisplayContext(req), aOut, "assistantImage");
            }
            
            // List element end
            aOut.endTag(LI);
        }
        
        // List end
        aOut.endTag(OL);
        aOut.endTag(DIV);
    }


	@Override
	public void linkChannels(Log log) {
		super.linkChannels(log);
		modelChannel().addListener(this);
	}

	/** receive all major steps */
	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		if (newValue instanceof Collection) {
			LayoutComponent changedBy = sender.getComponent();
			ResPrefix theResPrefix = getResPrefix();
			if (StringServices.isEmpty(theResPrefix)) {
				theResPrefix = ((LayoutComponent) changedBy).getResPrefix();
			}
			this.setProgressModel(new AssistentProgressModel(theResPrefix));
			Collection steps = (Collection) newValue;
			Iterator iter = steps.iterator();
			while (iter.hasNext()) {
				AssistentStepInfo theStep = (AssistentStepInfo) iter.next();
				/* is this step a major step? */
				if (AssistentComponent.isMajorStep(theStep)) {
					this.getProgressModel().addMajorStep(theStep.getStepKey());
                }
            }
        }
	}
    
	@Override
	protected boolean supportsInternalModel(Object object) {
		return object == null || object instanceof AssistentProgressModel;
	}

	private AssistentProgressModel getProgressModel() {
		return _progressModel;
	}

	private void setProgressModel(AssistentProgressModel pm) {
		_progressModel = pm;
	}

    /** inner model class */
    public static class AssistentProgressModel {

        /** map with the major steps. i18nKey -> name*/
        private Map majorSteps = new HashMap(5);

        /** list with i18nKeys in the correct order */
        private List orderedSteps = new ArrayList();
        
        /** the i18n prefix for resolving the keys */
		private ResPrefix resPrefix;
        
        /** the resolver for i18n */
        private Resources res;

        /** the key of major step, which is currently displayed in AssistantComponenet */
        private String selectedKey;
        
        /** CTor. */
		public AssistentProgressModel(ResPrefix aResPrefix) {
            this.resPrefix = aResPrefix;
            this.res = Resources.getInstance();
        }
        
        /** test if the given key of major step is in progress */
        public boolean isSelected(String aKey) {
            return aKey != null
                && aKey.equals(this.selectedKey);
        }
        
        /** test if any Key is selected at all. */
        public boolean nothingSelected() {
            return StringServices.isEmpty(this.selectedKey);
        }

        /** tell which major step is in progress now.
         * 
         * (Not part of Fetraure API)
         *
         * @param aKey name of major step, same as i18nkey.
         */
        public void setSelectedKey(String aKey) {
            
            String theName = (String) this.majorSteps.get(aKey);
            if(theName == null) {
                Logger.warn("unable to set major step to " + aKey, this);
            }
            else {
                this.selectedKey = aKey;
            }
            
        }
        
        /** add further step */
        public void addMajorStep(String aKey) {
            
			ResKey thei18nKey = this.resPrefix.key(aKey);
			String theName = this.res.getString(thei18nKey);
            if(theName == null) {
                Logger.warn("name of major step: " + thei18nKey + " cannot be resolved.", this);
				theName = thei18nKey.getKey();
            }
            this.majorSteps.put(aKey, theName);
            this.orderedSteps.add(aKey);
        }
		/**
		 * Returns the orderedSteps.
		 */
		public List getOrderedSteps() {
			return orderedSteps;
		}
		/**
		 * Returns the majorSteps.
		 */
		public Map getMajorSteps() {
			return majorSteps;
		}
    }
}

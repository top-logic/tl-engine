/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.progress;

import java.lang.reflect.Field;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.progress.AJAXProgressComponent;
import com.top_logic.layout.progress.ProgressInfo;


/**
 * Extend the AJAXProgressComponent for testing.
 *
 * @author     <a href="mailto:cca@top-logic.com>cca</a>
 */
public class TestedAJAXProgressComponent extends AJAXProgressComponent {
 

    private StringBuffer wholeMessage;
    
    TestedAJAXProgressComponent(InstantiationContext context, Config attr) throws ConfigurationException{
        super(context, attr);
        wholeMessage = new StringBuffer(); 
    }
    
    @Override
	public void resetState() {
        super.resetState();
    }
    
    @Override
	public void resetLocalVariables() {
        wholeMessage = new StringBuffer();
        super.resetLocalVariables();
    }

    @Override
	public int getState() {
        return super.getState();
    }

    @Override
	public void checkState() {
        super.checkState();
    }
    
    public String getMessageDelta(String aMsg){
        return super.getMessageDelta(aMsg, wholeMessage);
    }

    /**
     * Allow access to super.
     */
	public void accessibleResolveComponents(InstantiationContext context) {
		super.resolveComponent(context);
    }


    /**
     * Evil hack to access private Variable in superclass.
     */
    public void setProgressInfo(ProgressInfo someMyInfo) throws Exception {

        Field[] fields = this.getClass().getSuperclass().getDeclaredFields();
        Field infoField=null;
        for(int i=0;i<fields.length;i++){
            if(fields[i].getName().equals("progressInfo")){
                infoField = fields[i];
            }
        }

        infoField.setAccessible(true);
        infoField.set(this,someMyInfo);
    }

    /*
     * Test this class.
    public static void main(String[] args)throws Exception {
        Attributes attr=null;
        attr=NullAttributes.singleton;
        TestedAJAXProgressComponent comp = new TestedAJAXProgressComponent(attr);
        comp.setProgressInfo(null);

    }
    */
}

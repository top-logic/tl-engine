/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.knowledge.test.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.top_logic.base.merge.MergeMessage;
import com.top_logic.base.merge.MergeTreeModel;
import com.top_logic.base.merge.MergeTreeNode;
import com.top_logic.base.merge.RootMergeNode;
import com.top_logic.base.merge.Validator;
import com.top_logic.base.merge.layout.MergeTreeComponenet;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Example how to set up a Merge Tree.
 * 
 * Be aware that MergeTreeComponenet usually are inner steps of an assistant
 *  
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class MergeTreeDemo extends MergeTreeComponenet {

	public interface Config extends MergeTreeComponenet.Config {

		@Override
		@ImplementationClassDefault(DemoMergeTreeBuilder.class)
		public PolymorphicConfiguration<? extends ModelBuilder> getModelBuilder();
	}

    /** 
     * Create a new MergeTreeDemo from XML.
     */
    public MergeTreeDemo(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }
    
    /** 
     * Build or model on some demo classes found below.
     */
    public static class DemoMergeTreeBuilder extends MergeTreeComponenet.MergeTreeBuilder {

		/**
		 * Singleton {@link DemoMergeTreeBuilder} instance.
		 */
		public static final DemoMergeTreeBuilder INSTANCE = new DemoMergeTreeBuilder();

		private DemoMergeTreeBuilder() {
			// Singleton constructor.
		}

        /** 
         * Create a static Example of a Merge Tree.
         */
        @Override
		public RootMergeNode createRootNode(LayoutComponent aBaseComponenet) {
            RootMergeNode root = new RootMergeNode("Demo");
            new MergeTreeNode(root, Arrays.asList(new String[] { "1"  , "10", "100"     }), null);
            new MergeTreeNode(root, Arrays.asList(new String[] { "1.1", ""  , "NixZahl" }), null);
            
            return root;
        }
        
        /** 
         * Create an instance of the DemoValidator.
         */
        @Override
		public Validator createValidator(LayoutComponent aBaseComponenet) {
            return new DemoValidator();
        }

        @Override
		public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
			return aModel == null || aModel instanceof MergeTreeModel;
        }
    }
    
    /**
     * Example of a validator as a starting point.
     * 
     * @author    <a href=mailto:kha@top-logic.com>kha</a>
     */
    public static class DemoValidator implements Validator {

        /** 
         * Example of converting aList of Strings to a List of numbers.
         * 
         * This is the (almost) fool proof way to do it ...
         */
        @Override
		public boolean validate(MergeTreeNode aNode) {
            boolean result = doValidate(aNode);
            
            // Ususally the validators do not recurse ...
            List children = aNode.getChildren();
            if (children != null) {
                int size = children.size();
                for (int i = 0; i < size; i++) {
                	Object child = children.get(i);
                	if(child instanceof MergeTreeNode){
                		result = doValidate((MergeTreeNode) child) | result;
                	}
                }
            }
            return result;
            
        }

        /** 
         * Example of converting aList of Strings to a List of numbers.
         * 
         * This is the (almost) fool proof way to do it ...
          */
        private boolean doValidate(MergeTreeNode aNode) {
            Object source = aNode.getSource();
            if (null == source) {
				aNode.addMessage(new MergeMessage(MergeMessage.FATAL, ResKey.text("source is null"),
					!MergeMessage.APPROVEABLE));
                return false;
            }
            if (! (source instanceof List)) {
				aNode.addMessage(new MergeMessage(MergeMessage.DEBUG, ResKey.text("source is not a List"),
					!MergeMessage.APPROVEABLE));
                return false;
            }
            List theList = (List) source;
            if (theList.isEmpty()) {
				aNode.addMessage(new MergeMessage(MergeMessage.WARN, ResKey.text("Input was empty"),
					!MergeMessage.APPROVEABLE));
                return true;
            }
            int  size      = theList.size();
            aNode.setDest(new ArrayList(size));
            for (int i=0; i < size; i++) {
                Object theEntry = theList.get(i);
                if (theEntry == null) {
					aNode.addMessage(new MergeMessage(MergeMessage.DEBUG, ResKey.text("Null value at " + i
						+ " will be ignored"), !MergeMessage.APPROVEABLE));
                }
                if (theEntry instanceof String) {
                    aNode.addMessage(new ConvertStringMessage((String) theEntry));
                }
//                if (theEntry == null) {
//                    aNode.addMessage(new MergeMessage(MergeMessage.WARN, "Value at " + i + " ignored " + theEntry.getClass(),!MergeMessage.APPROVEABLE));
//                }
            }
            
            return true;
        }
        
        /** 
         * Inner class doing the actal conversion 
         */
        static class ConvertStringMessage extends MergeMessage {

            /** the value to convert */
            String convert;
            
            /** 
             * ConvertStringMessage
             */
            public ConvertStringMessage(String aString) {
				super(INFO, ResKey.text("Convert '" + aString + "' to int"));
                convert = aString;
            }
            
            /** 
             * Append the converted String to a List found in anOwner.
             * 
             * (But only when approved)
             */
            @Override
			public void perform(MergeTreeNode anOwner) throws Exception {
                if (approved) {
					((List) anOwner.getDest()).add(Integer.valueOf(convert));
                }
            }
        }

    }


    
}

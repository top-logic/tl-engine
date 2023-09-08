/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured;

import static com.top_logic.basic.col.factory.CollectionFactory.*;

import java.util.Random;
import java.util.Set;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.StructuredElementFactory;
import com.top_logic.model.TLClass;

/**
 * Common Generator to create Structures (mostly for testcases).
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class DemoStructureGenerator {
    
    private static final String[] NAMES = {
        "Alpha", "Agena", 
        "Bobcat", "Bravo", "Charlie", "Colossus", 
        "Delta", "Echo", "Enigma", 
        "Falcon", "Foxtrott", "Freee for All",
        "Golf", "Homepage", "Hotel", "India", "Jasper", "Journeys", 
        "Juliett", "Kilo", "Lima", "Leopard",
        "Manhatten", "Menlow",
        "Niagara", "November",
        "Orcas", "Oscar", 
        "Papa", "Penryn",
        "Quebec", "Random", "Roadrunner", "Robson", 
        "Santiger", "Sierra", "Silverlight", "Silverthorne", "Shorty", "SOA", 
        "Tango",  "<i>TopLogic</i>", "Tunny",
        "Universe",  
        "Victor", "Virtual Spaces", "Viridian",
        "Walküre", "Whiskey", "Web 2.0",  "X-Ray", "Yankee", "Zephir", "Zulu"
    };
    
    protected StructuredElement root;

    public StructuredElement getStructure(String aStructureName, Random rand, int aDepth) 
                                throws Exception  {
        if (root == null) {
            StructuredElementFactory theFactory = StructuredElementFactory.getInstanceForStructure(aStructureName);  
            root    = theFactory.getRoot();
            
            createStructure(root, rand, 1, aDepth );
        }

        return (root);
    }

    /**
     * Recursive Helper to create the Sumbelements.
     */
    protected void createStructure(StructuredElement anElement,  Random rand, int aDepth, int maxDepth)
                throws Exception {
        
        if (aDepth > maxDepth) {
            return;
        }
        
		Set<TLClass> types = anElement.getChildrenTypes();

		if (types.size() == 0) {
            return;
        }

        int width = getWidth(rand, aDepth, maxDepth);
        for (int i = 0; i < width; i++) {
			TLClass type = getType(types, rand, aDepth, i);
			String theName = createName(rand, aDepth, i, type);
            
			StructuredElement theChild = createChild(anElement, rand, aDepth, i, type, theName);

            assert theChild != null : "Child of " + anElement + " is null";

            createStructure(theChild, rand , aDepth + 1, maxDepth);
        }
    }


    /** 
     * Create a width for the given depth.
     */
    protected int getWidth(Random aRand, int aDepth, int maxDepth) {
        return 5 + aRand.nextInt(5);
    }

    /** 
     * Return some reasonable type from the array of possible types.
     */
	protected TLClass getType(Set<TLClass> types, Random rand, int aDepth, int aWidth) {
		return list(types).get(rand.nextInt(types.size()));
    }

    /** 
     * Create some "Nice" Name for a demo Structure.
     */
	protected String createName(Random rand, int aDepth, int aWidth, TLClass type) {
        String randName = NAMES[rand.nextInt(NAMES.length)];
		return type.getName() + ' ' + randName;
    }

    /**
	 * Allow subclasses to add extra Data for their elements.
	 */
	protected StructuredElement createChild(StructuredElement aParent, Random rand, int aDepth, int aWidth,
			TLClass type, String theName) throws Exception {
		return aParent.createChild(theName, type);
    }
}


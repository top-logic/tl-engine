/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.reporting.remote;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

/**
 * The JitterBug is a {@link Serializable} that will {@link #dawdle()} around some random time while being serialized.
 * 
 * Used to test some RMI code to simluate unpredictable jitter on the rmi way.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class JitterBug implements Serializable {
    
    static final Random rand = new Random(4712L);

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        dawdle();
    }
        

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        dawdle();
    }
    
    
    private void dawdle() {
        try {
            Thread.sleep(rand.nextInt(1000));
        }
        catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    

}


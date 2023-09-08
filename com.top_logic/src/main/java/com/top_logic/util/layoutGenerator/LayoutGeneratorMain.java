/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.layoutGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.XMain;
import com.top_logic.basic.io.CSVTokenizer;
import com.top_logic.basic.tools.layout.InfoLine;
import com.top_logic.basic.tools.layout.LayoutConstants;
import com.top_logic.knowledge.gui.layout.LayoutConfig;
import com.top_logic.util.error.TopLogicException;

/**
 * Wraps the {@link com.top_logic.util.layoutGenerator.LayoutGenerator2} so it can be used from the
 * command prompt or from ant.
 * 
 * @author <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * @author <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class LayoutGeneratorMain extends XMain {
    
    public static final String LONG_OPTION_FILE_NAME_PREFIX = "fileNamePrefix";
    public static final String LONG_OPTION_MASTER_FRAME_FILE_NAME = "masterFrameFileName";
    public static final String LONG_OPTION_DETINATION_DIR = "destinationDir";
    public static final String LONG_OPTION_TEMPLATE_DIR = "templateDir";
    public static final String LONG_OPTION_SOURCE_FILE = "sourceFile";
    
    public static final String SHORT_OPTION_FILE_NAME_PREFIX = "f";
    public static final String SHORT_OPTION_MASTER_FRAME_FILE_NAME = "m";
    public static final String SHORT_OPTION_DETINATION_DIR = "d";
    public static final String SHORT_OPTION_TEMPLATE_DIR = "t";
    public static final String SHORT_OPTION_SOURCE_FILE = "s";
    
    protected String fileNamePrefix;
    protected String masterFrameFileName;
    protected String destinationDir;
    protected String templateDir;
    
    protected String sourceFileName = "WEB-INF/conf/layout.xls";

    /** 
     * This function is called when -h or --help is found 
     */
    @Override
	protected void showHelp() {
        System.out.println("Generate layout acording to the given layout configuration.");
        System.out.println("Syntax: [<option>]* [<arguments>]");
        System.out.println("Options are:");
        System.out.println("\t-f | --fileNamePrefix  the prefix to use for the generated files");
        System.out.println("\t-r | --masterFrameFileName  the name of the generated master frame (root)");
        System.out.println("\t-d | --destinationDir  the directory to store the regerated files in");
        System.out.println("\t-t | --templateDir  the directory to get the templates from");
        System.out.println("\t-s | --sourceFile  the file conating the layout configuration (xls-file)");

        System.out.println("Arguments are:");
        System.out.println("\t - the name of the source file containing the");
    }

    /**
     * Care about the "feature" and "real" oprtions.
     */
    @Override
	protected int longOption(String option, String[] args, int i) {
        if (LONG_OPTION_FILE_NAME_PREFIX.equals(option)) {
            this.fileNamePrefix = args[i++];
        } else if (LONG_OPTION_MASTER_FRAME_FILE_NAME.equals(option)) {
            this.masterFrameFileName = args[i++];
        } else if (LONG_OPTION_DETINATION_DIR.equals(option)) {
            this.destinationDir = args[i++];
        } else if (LONG_OPTION_TEMPLATE_DIR.equals(option)) {
            this.templateDir = args[i++];
        } else if (LONG_OPTION_SOURCE_FILE.equals(option)) {
            this.sourceFileName = args[i++];
        } else {
            return super.longOption(option, args, i);
        }
        return i;
    }
    
    @Override
	protected int shortOption(char c, String[] args, int i) {
        if ('r' == c) {
            this.masterFrameFileName = args[i++];
            return i;
        } else {
            return super.shortOption(c, args, i);
        }
    }
    
    
    
	protected static List<InfoLine> readCSVLines(String aFileName) throws IOException {
		try (FileReader fileReader = new FileReader(aFileName);
				BufferedReader theReader = new BufferedReader(new FileReader(aFileName))) {
        
        CSVTokenizer theTokenizer  = new CSVTokenizer(';','"');
        
		List<InfoLine> result = new ArrayList<>(256);
        // first line contains headings
                      theReader.readLine();
        String line = theReader.readLine();
        int    linNum = 0;
        do {
            linNum ++;
            theTokenizer.reset(line);
            String level             = theTokenizer.nextToken(); 
            String name              = theTokenizer.nextToken();
            String file              = theTokenizer.nextToken();
            String key               = theTokenizer.nextToken();
				if (key != null) {
					key = key.trim();
					// make sure theKey is an I18N prefix
					if (!key.endsWith(".")) {
						key += '.';
					}
				}
            String picture           = theTokenizer.nextToken();
            String type              = theTokenizer.nextToken();
            String size              = theTokenizer.nextToken();
            String template          = theTokenizer.nextToken();
            String master            = theTokenizer.nextToken();
            String useDefaultChecker = theTokenizer.nextToken();
            String domain            = theTokenizer.nextToken();
				String targetProject = theTokenizer.nextToken();
				String targetPath = theTokenizer.nextToken();
				InfoLine infoLine = new InfoLine(level, name, file, key, picture, type, size, template, master,
					useDefaultChecker, domain, targetProject, targetPath);
				if (InfoLine.value(infoLine, InfoLine.LEVEL) == null) {
					throw new RuntimeException("Level must be set (line " + linNum + ")");
				}
				if (!InfoLine.getTypes(infoLine).contains(LayoutConstants.TYPE_NO_GENERATION)) {
					if (key == null) {
						throw new RuntimeException("Key null not allowed (line " + linNum + ")");
					}
					if (name == null) {
						throw new RuntimeException("Name null not allowed (line " + linNum + ")");
					}
				}
				result.add(infoLine);
            line = theReader.readLine();
        } while  (line != null);
        return result;        
		}
    }
    
    /**
     * Generate the layout from given fileName. 
     */
    public void generateLayout(String aFileName) throws Exception {
		List<InfoLine> theLines;
        
        FileManager theManager = FileManager.getInstance();
		File theLayoutFile = theManager.getIDEFileOrNull(aFileName);
        
		if (aFileName.endsWith(".csv"))
            theLines = readCSVLines(theLayoutFile.getCanonicalPath());
        else 
            throw new TopLogicException(LayoutGeneratorMain.class, "unknownExtension", 
                                            new Object[] { sourceFileName} );
        
		initStructure(theLines);

		AbstractLayoutGenerator theGenerator = new LayoutGenerator(getProtocol(), theLines);
        // TODOS TSA add paramaters for these
        // theGenerator.setTemplateDir("data/posTemplates/");
        
        if (this.destinationDir != null) {
            theGenerator.setDestinationDir(this.destinationDir);
        } else {
			theGenerator.setDestinationDir("WEB-INF/layouts/");
        }
        
        if (this.fileNamePrefix != null) {
            theGenerator.setFileNamePrefix(this.fileNamePrefix);
        } else {
            theGenerator.setFileNamePrefix("pos");
        }
        
        if (this.masterFrameFileName != null) {
            theGenerator.setMasterFrameFileName(this.masterFrameFileName);
		} else {
			theGenerator.setMasterFrameFileName(LayoutConfig.getDefaultLayout());
		}
        
        if (this.templateDir != null) {
            theGenerator.setTemplateDir(this.templateDir);
		} // else use default from generator
        
        theGenerator.generateLayout(interactive);
    }

	private void initStructure(List<InfoLine> lines) {
		ArrayList<InfoLine> parentForLevel = new ArrayList<>();

		int lastLevel = -1;
		for (int n = 0, cnt = lines.size(); n < cnt; n++) {
			InfoLine line = lines.get(n);
			int level = Integer.parseInt(InfoLine.value(line, InfoLine.LEVEL)) - 1;

			while (lastLevel > level) {
				assert lastLevel == parentForLevel.size() - 1;
				parentForLevel.remove(lastLevel--);
			}
			if (lastLevel == level) {
				InfoLine preceedingSibbling = parentForLevel.remove(lastLevel--);
				preceedingSibbling.setNextSibbling(line);
			}


			assert level == parentForLevel.size();
			parentForLevel.add(line);

			if (lastLevel >= 0) {
				InfoLine parent = parentForLevel.get(lastLevel);
				parent.addChild(line);
			}

			lastLevel = level;
		}
	}

	/**
	 * Any parameter will be used as Filename.
	 */
    @Override
	protected int parameter(String[] args, int i) {
        this.sourceFileName = args[i++];
        return i;
    }
    
    @Override
	protected void doActualPerformance() throws Exception {
        if (this.sourceFileName == null) {
			sourceFileName = "/WEB-INF/conf/layout.xls";
        }
        if (interactive)
            System.out.println("Now generating layout from " + sourceFileName);
        try {
            generateLayout(sourceFileName);
			getProtocol().checkErrors();
        } catch (Exception e) {
            if (interactive) {
                System.out.println("Generation failed");
                e.printStackTrace();
            }
            System.exit(2);
        }
        System.out.println("Generation successfull");
    }


    public static void main (String args[]) throws Exception {
        new LayoutGeneratorMain ().runMainCommandLine (args);
    }

}

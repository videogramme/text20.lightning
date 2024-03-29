/*
 * CoverageDetectorProperties.java
 * 
 * Copyright (c) 2011, Christoph Käding, DFKI. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package de.dfki.km.text20.lightning.evaluator.plugins.coveragedetector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.simpleframework.xml.Attribute;

import de.dfki.km.text20.lightning.plugins.saliency.textdetector.TextDetectorProperties;

/**
 * stores configurations, write them to a file on exit and tries to load them on startup 
 * 
 * @author Christoph Käding
 *
 */
public class CoverageDetectorProperties implements Serializable {

    /** generated serial id */
    private static final long serialVersionUID = -7009680382311094022L;

    /** minimal height to recognize letters */
    @Attribute
    private int letterHeight;

    /** minimal size to identify lines */
    @Attribute
    private int lineSize;

    /** indicates if debug images should be written */
    @Attribute
    private boolean debug;

    /** sensitivity of text recognition */
    @Attribute
    private double sensitivity;

    /** file where porperties are stored */
    private File propertiesFile;

    /** from properties file readed object */
    private Object object;

    /** singleton instance of this properties */
    private static CoverageDetectorProperties properties;

    /**
     * creates properties, tries to load property file
     */
    private CoverageDetectorProperties() {

        // creates properties file
        this.propertiesFile = new File("./plugins/CoverageDetector/properties.prop");

        // status is used to indicate if the properties object could be readed probably 
        boolean status = false;

        if (this.propertiesFile.exists()) {
            try {

                // read object from file
                ObjectInputStream inputStream = new ObjectInputStream((new FileInputStream(this.propertiesFile)));
                this.object = inputStream.readObject();

                if (this.object instanceof TextDetectorProperties) {

                    // store readed configurations
                    this.letterHeight = ((TextDetectorProperties) this.object).getLetterHeight();
                    this.lineSize = ((TextDetectorProperties) this.object).getLineSize();
                    this.debug = ((TextDetectorProperties) this.object).isDebug();
                    this.sensitivity = ((TextDetectorProperties) this.object).getSenitivity();

                    // reading successful
                    status = true;
                    System.out.println("\r\nTextDetector properties file was found.");
                    System.out.println("letter height: " + this.letterHeight + ", line size: " + this.lineSize + ", senitivity: " + this.sensitivity + ", debug: " + this.debug);
                }

                // cleanup
                inputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // if reading was not successful or properties file was not found
        if (!status) {
            this.restoreDefault();
            System.out.println("\r\nCoverageDetector properties file was not found.\r\n");
        }
    }

    /**
     * restores default values
     */
    public void restoreDefault() {
        // set default values
        this.letterHeight = 7;
        this.lineSize = 100;
        this.debug = false;
        this.sensitivity = 1.5;
    }

    /**
     * @return the letterHeight
     */
    public int getLetterHeight() {
        return this.letterHeight;
    }

    /**
     * @param letterHeight the letterHeight to set
     */
    public void setLetterHeight(int letterHeight) {
        this.letterHeight = letterHeight;
    }

    /**
     * @return the lineSize
     */
    public int getLineSize() {
        return this.lineSize;
    }

    /**
     * @param lineSize the lineSize to set
     */
    public void setLineSize(int lineSize) {
        this.lineSize = lineSize;
    }

    /**
     * @return the debug
     */
    public boolean isDebug() {
        return this.debug;
    }

    /**
     * @param debug the debug to set
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * @return the sensitivity
     */
    public double getSenitivity() {
        return this.sensitivity;
    }

    /**
     * @param sensitivity the sensitivity to set
     */
    public void setSensitivity(double sensitivity) {
        this.sensitivity = sensitivity;
    }

    /**
     * creates and returns the singleton instance
     * 
     * @return properties
     */
    public static CoverageDetectorProperties getInstance() {
        if (properties == null) {
            properties = new CoverageDetectorProperties();
        }
        return properties;
    }

    /**
     * write properties to propertiesFile
     */
    public void writeProperties() {
        try {

            // write object
            new File("plugins/TextDetector").mkdirs();
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(this.propertiesFile));
            outputStream.writeObject(this);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

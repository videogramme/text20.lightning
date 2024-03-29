/*
 * SaliencyDetector.java
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
 *
 */
package de.dfki.km.text20.lightning.plugins.saliency;

import java.awt.Point;
import java.awt.image.BufferedImage;

import net.xeoh.plugins.base.Plugin;
import de.dfki.km.text20.lightning.plugins.CommonPluginInterface;
import de.dfki.km.text20.lightning.plugins.PluginInformation;

/**
 * Interface for different algorithms for screen analyzing.
 * 
 * @author Christoph Käding
 */
public interface SaliencyDetector extends Plugin, CommonPluginInterface {

    /**
     * Analyzes the given processed screenshot and calculates an offset which is added to the fixation point.
     * 
     * @param screenShot in which center is the fixation point
     * @return offset of the next position which is realized as the real target
     */
    public Point analyse(BufferedImage screenShot);

    /**
     * Returns some information about the plugin.
     * Make sure you always return the same object, because a id will be set in it!!
     * 
     * @return saliency plugin informations
     */
    public PluginInformation getInformation();
}

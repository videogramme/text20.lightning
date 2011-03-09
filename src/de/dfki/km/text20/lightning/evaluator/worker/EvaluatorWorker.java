/*
 * EvaluatorWorker.java
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
package de.dfki.km.text20.lightning.evaluator.worker;

import static net.jcores.CoreKeeper.$;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import de.dfki.km.text20.lightning.plugins.saliency.SaliencyDetector;
import de.dfki.km.text20.lightning.worker.training.DataContainer;

/**
 * @author Christoph Käding
 *
 */
public class EvaluatorWorker {

    private Map<String, EvaluationContainer> results;

    private long currentTimeStamp;

    public EvaluatorWorker(long currentTimeStamp) {
        this.results = new Hashtable<String, EvaluationContainer>();
        this.currentTimeStamp = currentTimeStamp;
    }

    /**
     * 
     * @param detector
     * @param container
     * @param writeLog
     * @param drawImage
     * @param path
     * @throws EvaluatorException
     */
    public void evaluate(SaliencyDetector detector, DataContainer container,
                         boolean drawImage, String path) {
        BufferedImage screenShot = null;
        Point offset = new Point();

        // test if associated screenshot is available
        File screenFile = new File(path + "/data/" + container.getUser() + "/" + container.getUser() + "_" + container.getTimestamp() + ".png");

        if (!screenFile.exists()) return;

        // read screenshot
        try {
            screenShot = ImageIO.read(screenFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // calculate offset
        offset = detector.analyse(screenShot);

        if (drawImage)
            this.drawPicture(detector, offset, path + "/evaluation/" + container.getUser() + "_" + this.currentTimeStamp + "/" + container.getUser() + "_" + container.getTimestamp() + ".png", screenShot, container.getMouseOffset());

        if (this.results.containsKey(container.getUser() + container.getTimestamp())) {
            this.results.get(container.getUser() + container.getTimestamp()).add(detector.getInformation().getId(), offset.distance(container.getMouseOffset()));
        } else {
            this.results.put(container.getUser() + container.getTimestamp(), new EvaluationContainer(detector.getInformation().getId(), offset.distance(container.getMouseOffset()), path + "/evaluation/evaluation.log", container.getUser(), container.getTimestamp()));
        }

    }

    /**
     * A call of this method indicates that the evaluation is finished and the result file can be updated.
     * 
     * @param writeLog 
     * @param detectors 
     * 
     * @return name of best ranked detector 
     */
    @SuppressWarnings("boxing")
    public String getBestResult(boolean writeLog, ArrayList<SaliencyDetector> detectors) {
        double bestValue = Double.MAX_VALUE;
        int bestKey = -1;
        double veryBestValue = Double.MAX_VALUE;
        String veryBestName = "";

        for (String key : this.results.keySet()) {
            for (int i = 0; i < this.results.get(key).getKeys().size(); i++) {

                if (bestValue > this.results.get(key).getAverage(this.results.get(key).getKeys().get(i))) {
                    bestKey = this.results.get(key).getKeys().get(i);
                    bestValue = this.results.get(key).getAverage(this.results.get(key).getKeys().get(i));
                }
                if (veryBestValue > this.results.get(key).getAverage(this.results.get(key).getKeys().get(i))) {
                    veryBestValue = this.results.get(key).getAverage(this.results.get(key).getKeys().get(i));
                    veryBestName = detectors.get(this.results.get(key).getKeys().get(i)).getInformation().getDisplayName();
                }

                if (writeLog) {
                    if (i == 0)
                        $(this.results.get(key).getLogPath()).file().append("Session - User: " + this.results.get(key).getName() + ", Timestamp: " + this.results.get(key).getTimeStamp() + "\n");
                    $(this.results.get(key).getLogPath()).file().append(detectors.get(this.results.get(key).getKeys().get(i)).getInformation().getDisplayName() + ": " + this.results.get(key).getAverage(this.results.get(key).getKeys().get(i)) + " Pixel distance in average.\n");
                    if (i == this.results.get(key).getKeys().size() - 1)
                        $(this.results.get(key).getLogPath()).file().append("Best result for " + detectors.get(bestKey).getInformation().getDisplayName() + "\n\n");
                }
            }

            bestValue = Double.MAX_VALUE;
            bestKey = -1;
        }
        return veryBestName;
    }

    private void drawPicture(SaliencyDetector detector, Point point, String path,
                             BufferedImage screenShot, Point mouseOffset) {
        // initialize variables
        int color = (int) (Math.random() * 255);
        int dimension = screenShot.getHeight();
        File file = new File(path);
        boolean alreadyExists = file.exists();

        try {
            if (alreadyExists) screenShot = ImageIO.read(file);

            // create screenshot graphic
            Graphics2D graphic = screenShot.createGraphics();
            graphic.setFont(graphic.getFont().deriveFont(5));

            if (alreadyExists) {
                // visualize fixation point 
                graphic.setColor(new Color(255, 255, 0, 255));
                graphic.drawOval(dimension / 2 - 5, dimension / 2 - 5, 10, 10);
                graphic.drawChars(("fixation point").toCharArray(), 0, 14, 12 + dimension / 2, 12 + dimension / 2);
                graphic.setColor(new Color(255, 255, 0, 32));
                graphic.fillOval(dimension / 2 - 5, dimension / 2 - 5, 10, 10);

                // visualize mouse point 
                graphic.setColor(new Color(255, 0, 0, 255));
                graphic.drawOval(mouseOffset.x - 5, mouseOffset.y - 5, 10, 10);
                graphic.drawChars(("mouse target").toCharArray(), 0, 12, 12 + mouseOffset.x, 12 + mouseOffset.y);
                graphic.setColor(new Color(255, 0, 0, 32));
                graphic.fillOval(mouseOffset.x - 5, mouseOffset.y - 5, 10, 10);

                System.out.println((dimension / 2 - 5) + ":" + (dimension / 2 - 5) + " - " + (mouseOffset.x - 5) + ":" + (mouseOffset.y - 5));
            }

            // visualize calculations
            color = (50 + color) % 256;
            graphic.setColor(new Color(0, 255 - color, color, 255));
            graphic.drawOval(point.x + dimension / 2 - 5, point.y + dimension / 2 - 5, 10, 10);
            graphic.drawChars(detector.getInformation().getDisplayName().toCharArray(), 0, detector.getInformation().getDisplayName().toCharArray().length, point.x + 12 + dimension / 2, point.y + 12 + dimension / 2);
            graphic.setColor(new Color(0, 255 - color, color, 32));
            graphic.fillOval(point.x + dimension / 2 - 5, point.y + dimension / 2 - 5, 10, 10);

            // write the image
            file.mkdirs();
            ImageIO.write(screenShot, "png", file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
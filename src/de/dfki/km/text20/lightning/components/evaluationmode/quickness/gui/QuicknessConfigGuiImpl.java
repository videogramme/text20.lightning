/*
 * QuicknessConfigGuiImpl.java
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
package de.dfki.km.text20.lightning.components.evaluationmode.quickness.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.filechooser.FileFilter;

import de.dfki.km.text20.lightning.MainClass;
import de.dfki.km.text20.lightning.components.evaluationmode.quickness.QuicknessMode;
import de.dfki.km.text20.lightning.components.evaluationmode.quickness.WordXMLParser;
import de.dfki.km.text20.lightning.plugins.PluginInformation;

/**
 * @author Christoph Käding
 *
 */
@SuppressWarnings("serial")
public class QuicknessConfigGuiImpl extends QuicknessConfigGui implements ActionListener {

    /** */
    private QuicknessMode quicknessMode;

    /** */
    private ArrayList<File> files;

    /** */
    private String currentFile;

    /** */
    private ArrayList<String> currentWords;

    /** */
    private boolean autoSelect;

    /** frame for configuration guis of plugins */
    private JFrame child;

    /** timer for window focus */
    private Timer timer;

    /**
     * creates instance and initializes some stuff
     * 
     * @param quicknessMode
     */
    public QuicknessConfigGuiImpl(QuicknessMode quicknessMode) {
        // initialize variables
        this.quicknessMode = quicknessMode;
        this.files = new ArrayList<File>();
        this.currentWords = new ArrayList<String>();
        this.child = null;

        // initialze combobox
        this.manageComboBoxSaliencyDetector();

        // add action listener
        this.buttonCancel.addActionListener(this);
        this.buttonOK.addActionListener(this);
        this.buttonRemove.addActionListener(this);
        this.buttonSelect.addActionListener(this);
        this.comboBoxDetector.addActionListener(this);
        this.buttonDetector.addActionListener(this);

        // initialize timer
        this.timer = new Timer(500, new ActionListener() {

            @SuppressWarnings({ "synthetic-access", "unqualified-field-access" })
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (child.isShowing()) return;
                timer.stop();
                setEnabled(true);
                requestFocus();
            }
        });

        // show gui
        this.setVisible(true);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (this.autoSelect) {
            return;
        } else if (event.getSource() == this.buttonCancel) {
            this.buttonCancelActionPerformed();
        } else if (event.getSource() == this.buttonOK) {
            this.buttonOKActionPerformed();
        } else if (event.getSource() == this.buttonRemove) {
            this.buttonRemoveActionPerformed();
        } else if (event.getSource() == this.buttonSelect) {
            this.buttonSelectActionPerformed();
        } else if (event.getSource() == this.comboBoxDetector) {
            this.comboBoxDetectorActionPerformed();
        } else if (event.getSource() == this.buttonDetector) {
            this.buttonDetectorActionPerformed();
        }
    }

    /**
     * fired when the buttonOK is clicked
     * starts mode
     */
    @SuppressWarnings("boxing")
    private void buttonOKActionPerformed() {
        // initialize variables
        WordXMLParser parser = new WordXMLParser();

        // run through all files
        for (File file : this.files) {

            // validate file
            if (parser.isValid(file)) {

                // read file
                parser.readFile(this, file);

                // check if variables are updated by parser  
                if (!this.currentFile.equals("")) {

                    // add data 
                    this.quicknessMode.addToData(this.currentFile, this.currentWords);

                    // reset data
                    this.currentFile = "";
                    this.currentWords.clear();
                }
            }
        }

        // set other stuff
        this.quicknessMode.setStartWithDetector(this.checkBoxStartWithDetector.isSelected());
        this.quicknessMode.setSteps(Integer.parseInt(this.spinnerSteps.getValue().toString()));
        MainClass.getInstance().getInternalPluginManager().setCurrentSaliencyDetector(((PluginInformation) this.comboBoxDetector.getSelectedItem()).getId());

        // close config
        this.dispose();

        // show dialog and react on answer
        if (JOptionPane.showOptionDialog(null, "Ready to start?", "Quickness", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, new Object[] { "Yes", "No" }, 0) == 0)
            this.quicknessMode.start();
    }

    /**
     * manages the visibility of the configbutton
     */
    private void comboBoxDetectorActionPerformed() {
        //        this.buttonDetectorConfig.setText(((PluginInformation) this.comboBoxDetector.getSelectedItem()).getDisplayName() + " Configuration");
        this.buttonDetector.setEnabled(((PluginInformation) this.comboBoxDetector.getSelectedItem()).isGuiAvailable());
    }

    /**
     * Fired if the Detector Config button is clicked. Shows the configdialog.
     */
    private void buttonDetectorActionPerformed() {
        this.child = MainClass.getInstance().getInternalPluginManager().getSaliencyDetectors().get(((PluginInformation) this.comboBoxDetector.getSelectedItem()).getId()).getGui();
        this.child.setVisible(true);
        this.setEnabled(false);
        this.timer.start();
    }

    /**
     * fired when the buttonCancel is clicked
     * closes gui
     */
    private void buttonCancelActionPerformed() {
        this.dispose();
    }

    /**
     * fired when the buttonSelect is clicked
     * opens the file chooser
     */
    private void buttonSelectActionPerformed() {
        JFileChooser chooser = initChooser();
        chooser.showOpenDialog(null);
    }

    /**
     * fired when the buttonRemove is clicked
     * removes selected files from array list and listFiles
     */
    private void buttonRemoveActionPerformed() {
        // for every selected file ...
        for (Object selected : this.listFiles.getSelectedValues()) {

            // ... remove it from array list
            if (selected instanceof File) this.files.remove(selected);
        }

        // clear listFiles and add all in files included files
        this.listFiles.removeAll();
        this.listFiles.setListData(this.files.toArray());
    }

    /**
     * Takes the list of available plugins and shows them in the combobox.
     */
    private void manageComboBoxSaliencyDetector() {
        // remove content
        this.autoSelect = true;
        this.comboBoxDetector.removeAllItems();
        this.autoSelect = false;

        // add all saliency detectors to the combobox
        for (int i = 0; i < MainClass.getInstance().getInternalPluginManager().getSaliencyDetectors().size(); i++) {
            this.comboBoxDetector.addItem(MainClass.getInstance().getInternalPluginManager().getSaliencyDetectors().get(i).getInformation());
        }

        // preselect the current one
        if (MainClass.getInstance().getInternalPluginManager().getCurrentSaliencyDetector() != null)
            this.comboBoxDetector.setSelectedItem(MainClass.getInstance().getInternalPluginManager().getCurrentSaliencyDetector().getInformation());

        // set renderer
        this.comboBoxDetector.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // set new displayed attribute
                if (value instanceof PluginInformation) {
                    setText(((PluginInformation) value).getDisplayName());
                    setToolTipText(((PluginInformation) value).getToolTip());
                }

                return this;
            }
        });

        // initialize button
        //        this.buttonDetectorConfig.setText(this.internalPluginManager.getCurrentSaliencyDetector().getInformation().getDisplayName() + " Configuration");
        this.buttonDetector.setEnabled(MainClass.getInstance().getInternalPluginManager().getCurrentSaliencyDetector().getInformation().isGuiAvailable());
    }

    /**
     * initializes filechooser
     * 
     * @return
     */
    private JFileChooser initChooser() {
        JFileChooser chooser = new JFileChooser() {

            // react on selection
            @SuppressWarnings({ "unqualified-field-access", "synthetic-access" })
            public void approveSelection() {
                super.approveSelection();

                // add selected files to array list ...
                // TODO: add regex to filter other XML-files
                for (File allFiles : getSelectedFiles())
                    for (File file : getAllXMLFiles(allFiles))
                        // ignore duplicates
                        if (!files.contains(file)) files.add(file);

                // ... and to the listFiles
                listFiles.setListData(files.toArray());
            }
        };

        // set behavior of this chooser
        chooser.setMultiSelectionEnabled(true);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        chooser.setFileFilter(new FileFilter() {

            // filter string, only files with this extension and directories will be shown
            private String extension = ".xml";

            @Override
            public String getDescription() {
                return this.extension;
            }

            // set filter
            @Override
            public boolean accept(File file) {
                if (file == null) return false;

                if (file.isDirectory()) return true;

                return file.getName().toLowerCase().endsWith(this.extension);
            }
        });

        // return the created chooser
        return chooser;
    }

    /**
     * return all *.xml-files within a directory
     * 
     * @param file
     * @return arraylist of files
     */
    private ArrayList<File> getAllXMLFiles(File file) {
        // initialize list
        ArrayList<File> result = new ArrayList<File>();

        // if given file is a file ...
        if (file.isFile()) {

            // ... and ends with '.xml' ....
            if (file.getName().endsWith(".xml")) {

                // return it
                result.add(file);
                return result;
            }

            // or if given file is a directory ...
        } else if (file.isDirectory()) {

            // ... check all included files
            for (File includedFiles : file.listFiles()) {
                result.addAll(this.getAllXMLFiles(includedFiles));
            }

            // return results
            return result;
        }

        // if file is either a file nor a directory, return a empty list
        return result;
    }

    /**
     * @param currentFile the currentFile to set
     */
    public void setCurrentFile(String currentFile) {
        this.currentFile = currentFile;
    }

    /**
     * @param currentWords the currentWords to set
     */
    public void setCurrentWords(ArrayList<String> currentWords) {
        this.currentWords = currentWords;
    }
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.performance.graph;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import junit.framework.AssertionFailedError;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;

/**
 * @author SDimitrov
 */
public class Grapher {

    private static final int GRAPH_HEIGHT= 300;
    private static final int GRAPH_WIDTH= 800;

    String testResultDirectory;
    String outputDirectory;
    String referenceBuildId;
    String buildTypeFilter;
    Scenario[] scenarios;
    
    public Grapher() {
        super();
    }
    public Grapher(Scenario [] scenarios,String output,String reference) {
    	this.scenarios=scenarios;
    	outputDirectory=output;
    	referenceBuildId=reference;
    	run();
    }

    public void run() {

        Display display= Display.getDefault();
        Color black= display.getSystemColor(SWT.COLOR_BLACK);
        Color green= display.getSystemColor(SWT.COLOR_DARK_GREEN);

        new File(outputDirectory).mkdirs();

         for (int s= 0; s < scenarios.length; s++) {
            Scenario t= scenarios[s];
            String scenarioName= t.getScenarioName();
            Dim[] dimensions= t.getDimensions();
            for (int i= 0; i < dimensions.length; i++) {
                Dim dim= dimensions[i];
                String dimensionName= dim.getName();
                LineGraph graph= new LineGraph(scenarioName + ": " + dimensionName, dim);
                TimeSeries ts=null;
                try{
                ts= t.getTimeSeries(dim);
                int n= ts.getLength();
                
                if (n > 0) {
	                for (int j= 0; j < n; j++) {
	                    String buildID= ts.getLabel(j);
	                    double value= ts.getValue(j);
	                    Color c= buildID.indexOf(referenceBuildId)  >= 0 ? green : black;
	                    if (c == green || n-2<j)
	                    	graph.addItem(buildID, dim.getDisplayValue(value), value, c);
	                    else
	                    	graph.addItem(buildID, value, c);
	                }
	                
	                drawGraph(graph, outputDirectory + "/" + scenarioName.replace('#', '.').replace(':','_').replace('\\','_') + "_" + dimensionName);
                }
                } catch (AssertionFailedError e){
                	//System.err.println("Unable to get result for: "+t.getScenarioName()+" "+ts.toString());
                }
            }
        }
    }

    public void drawGraph(LineGraph p, String output) {

        Image image= new Image(Display.getDefault(), GRAPH_WIDTH, GRAPH_HEIGHT);

        p.paint(image);

        ImageLoader il= new ImageLoader();
        il.data= new ImageData[] { image.getImageData()};

        OutputStream out= null;
        try {
            out= new BufferedOutputStream(new FileOutputStream(output + ".jpeg"));
            //System.out.println("writing: " + output);
            il.save(out, SWT.IMAGE_JPEG);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            image.dispose();
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e1) {
                    // silently ignored
                }
            }
        }
    }
}
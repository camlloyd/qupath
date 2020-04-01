/*-
 * #%L
 * This file is part of QuPath.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package qupath.lib.gui.commands.scriptable;

import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.value.ObservableValue;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.images.ImageData;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathCellObject;
import qupath.lib.objects.PathDetectionObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.PathObjectTools;
import qupath.lib.objects.PathTileObject;
import qupath.lib.objects.TMACoreObject;
import qupath.lib.plugins.workflow.DefaultScriptableWorkflowStep;
import qupath.lib.plugins.workflow.WorkflowStep;
import qupath.lib.scripting.QP;


/**
 * Select objects according to a specified predicate.
 * 
 * @author Pete Bankhead
 * 
 * @param <T> generic parameter for {@link ImageData}
 *
 */
public class SelectObjectsByClassCommand<T> implements PathCommand {
	
	public final static Logger logger = LoggerFactory.getLogger(SelectObjectsByClassCommand.class);
	
	private ObservableValue<ImageData<T>> manager;
	private Class<? extends PathObject> cls;
	
	public SelectObjectsByClassCommand(final ObservableValue<ImageData<T>> manager, final Class<? extends PathObject> cls) {
		super();
		this.manager = manager;
		this.cls = cls;
	}

	@Override
	public void run() {
		ImageData<?> imageData = manager.getValue();
		if (imageData == null)
			return;
		selectObjectsByClass(imageData, cls);
	}
	
	/**
	 * Select objects that are instances of a specified class, logging an appropriate method in the workflow.
	 * 
	 * @param imageData
	 * @param cls
	 */
	public static void selectObjectsByClass(final ImageData<?> imageData, final Class<? extends PathObject> cls) {
		if (cls == TMACoreObject.class)
			QP.selectTMACores(imageData.getHierarchy());
		else
			QP.selectObjectsByClass(imageData.getHierarchy(), cls);
		
		Map<String, String> params = Collections.singletonMap("Type", PathObjectTools.getSuitableName(cls, false));
		String method;
		if (cls == PathAnnotationObject.class)
			method = "selectAnnotations();";
		else if (cls == PathDetectionObject.class)
			method = "selectDetections();";
		else if (cls == TMACoreObject.class)
			method = "selectTMACores();";
		else if (cls == PathCellObject.class)
			method = "selectCells();";
		else if (cls == PathTileObject.class)
			method = "selectTiles();";
		else
			// TODO: Get a suitable name to disguise Java classes
			method = "selectObjectsByClass(" + cls.getName() + ");";
		
		WorkflowStep newStep = new DefaultScriptableWorkflowStep("Select objects by class", params, method);
		WorkflowStep lastStep = imageData.getHistoryWorkflow().getLastStep();
		if (newStep.equals(lastStep))
			imageData.getHistoryWorkflow().replaceLastStep(newStep);
		else
			imageData.getHistoryWorkflow().addStep(newStep);
	}
	

}

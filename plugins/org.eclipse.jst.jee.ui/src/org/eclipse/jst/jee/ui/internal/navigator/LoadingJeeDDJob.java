/***********************************************************************
 * Copyright (c) 2011 by SAP AG, Walldorf.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     SAP AG - initial API and implementation
 ***********************************************************************/
package org.eclipse.jst.jee.ui.internal.navigator;


import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.viewers.Viewer;


public class LoadingJeeDDJob extends Job {
	
	//private final Viewer viewer;
	//private final JEE5ContentProvider rootProvider;
	//private final IProject project;
	//private final LoadingGroupProvider provider;

	public LoadingJeeDDJob(Viewer viewer2, LoadingGroupProvider provider, IProject project, JEE5ContentProvider rootProvider) {
		super(provider.getText());
		//this.viewer = viewer2;
		//this.provider = provider;
		//this.project = project;
		//this.rootProvider = rootProvider;
	}

	@Override
	protected IStatus run(IProgressMonitor arg0) {
		
		return Status.OK_STATUS;
		
		/*
		LoadingDDUIJob updateUIJob = new LoadingDDUIJob((StructuredViewer) viewer, provider.getPlaceHolder());
		updateUIJob.schedule();

		List children = new ArrayList();
		AbstractGroupProvider rootObject = null;

		try {
			rootObject = (rootProvider != null) ? rootProvider.getNewContentProviderInstance(project) : null;
			rootProvider.registerProvider(project, rootObject);
			if (rootObject != null) {
					children.add(rootObject);
			}
		} catch (IllegalStateException e) {
			if (!project.isAccessible()){
				//Project is most likely closed or deleted at this time.
				return Status.CANCEL_STATUS;
			}
			throw e;
		} finally {
			
			provider.dispose();
			new ClearJeePlaceHolderJob((AbstractTreeViewer) viewer, provider, project, children.toArray()).schedule();
		}


		return Status.OK_STATUS;*/
	}

}
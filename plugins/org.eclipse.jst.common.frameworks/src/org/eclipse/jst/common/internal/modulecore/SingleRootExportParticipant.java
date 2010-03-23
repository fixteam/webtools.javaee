/*******************************************************************************
 * Copyright (c) 2009 Red Hat and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat - Initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.common.internal.modulecore;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jst.common.internal.modulecore.SingleRootUtil.SingleRootCallback;
import org.eclipse.wst.common.componentcore.internal.DependencyType;
import org.eclipse.wst.common.componentcore.internal.flat.AbstractFlattenParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.ChildModuleReference;
import org.eclipse.wst.common.componentcore.internal.flat.FlatFolder;
import org.eclipse.wst.common.componentcore.internal.flat.FlatResource;
import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
import org.eclipse.wst.common.componentcore.internal.flat.IFlatFile;
import org.eclipse.wst.common.componentcore.internal.flat.IFlatFolder;
import org.eclipse.wst.common.componentcore.internal.flat.IFlatResource;
import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
import org.eclipse.wst.common.componentcore.internal.flat.VirtualComponentFlattenUtility;
import org.eclipse.wst.common.componentcore.internal.flat.FlatVirtualComponent.FlatComponentTaskModel;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualReference;

/**
 * Single root optimization. 
 * @author rob
 */
public class SingleRootExportParticipant extends AbstractFlattenParticipant {
	private SingleRootParticipantCallback callbackHandler;
	private IVirtualComponent rootComponent;
	private FlatComponentTaskModel dataModel;
	private IFlattenParticipant[] delegates;
	private List<IChildModuleReference> children;
	
	public interface SingleRootParticipantCallback extends SingleRootCallback {
		public IFlattenParticipant[] getDelegateParticipants();
	}
	
	public SingleRootExportParticipant() {
		super();
		callbackHandler = null;
	}
	public SingleRootExportParticipant(SingleRootParticipantCallback handler) {
		this();
		callbackHandler = handler;
	}
	
	@Override
	public void initialize(IVirtualComponent component,
			FlatComponentTaskModel dataModel, List<IFlatResource> resources) {
		this.rootComponent = component;
		this.dataModel = dataModel;
	}


	private void initializeDelegates() {
		if (callbackHandler != null) {
			delegates = callbackHandler.getDelegateParticipants();
		}
		else {
			delegates = new IFlattenParticipant[] {};
		}
	}
	
	@Override
	public boolean canOptimize(IVirtualComponent component,
			FlatComponentTaskModel dataModel) {
		return new SingleRootUtil(component, callbackHandler).isSingleRoot();
	}

	@Override
	public void optimize(IVirtualComponent component, FlatComponentTaskModel dataModel, 
			List<IFlatResource> resources, List<IChildModuleReference> childModules) {
		try {
			resources.clear(); // We want complete control
			childModules.clear();
			children = childModules;
			initializeDelegates();
			
			IContainer container = new SingleRootUtil(component, callbackHandler).getSingleRoot();
			IFlatResource[] mr = getMembers(resources, container, new Path("")); //$NON-NLS-1$
			int size = mr.length;
			for (int j = 0; j < size; j++) {
				resources.add(mr[j]);
			}
			
			addChildModules(component);
			
			// run finalizers
			for (int i = 0; i < delegates.length; i++) {
				delegates[i].finalize(component, dataModel, resources);
			}
		} catch( CoreException ce ) {
			// TODO 
		}
	}

	protected IFlatResource[] getMembers(List<IFlatResource> members, 
			IContainer cont, IPath path) throws CoreException {
		IResource[] res = cont.members();
		int size2 = res.length;
		List list = new ArrayList(size2);
		for (int j = 0; j < size2; j++) {
			if (res[j] instanceof IContainer) {
				IContainer cc = (IContainer) res[j];
				// Retrieve already existing module folder if applicable
				IFlatFolder mf = (FlatFolder) VirtualComponentFlattenUtility.getExistingModuleResource(members,path.append(new Path(cc.getName()).makeRelative()));
				if (mf == null) {
					mf = new FlatFolder(cc, cc.getName(), path);
					IFlatFolder parent = (FlatFolder) VirtualComponentFlattenUtility.getExistingModuleResource(members, path);
					if (path.isEmpty() || path.equals(new Path("/"))) //$NON-NLS-1$
						members.add(mf);
					else {
						if (parent == null)
							parent = VirtualComponentFlattenUtility.ensureParentExists(members, path, cc);
						VirtualComponentFlattenUtility.addMembersToModuleFolder(parent, new IFlatResource[] {mf});
					}
				}
				IFlatResource[] mr = getMembers(members, cc, path.append(cc.getName()));
				VirtualComponentFlattenUtility.addMembersToModuleFolder(mf, mr);
			} else {
				IFile f = (IFile) res[j];
				IFlatFile mf = VirtualComponentFlattenUtility.createModuleFile(f, path);
				if (shouldAddComponentFile(rootComponent, mf))
					list.add(mf);
			}
		}
		FlatResource[] mr = new FlatResource[list.size()];
		list.toArray(mr);
		return mr;
	}
	
	protected void addChildModules(IVirtualComponent vc) throws CoreException {
		IVirtualReference[] allReferences = vc.getReferences();
    	for (int i = 0; i < allReferences.length; i++) {
    		IVirtualReference reference = allReferences[i];
			if (reference.getDependencyType() == DependencyType.USES ) {
				if (shouldIgnoreReference(vc, reference))
					continue;
				
				if (isChildModule(vc, reference)) {
					ChildModuleReference cm = new ChildModuleReference(reference, new Path("")); //$NON-NLS-1$
					List<IChildModuleReference> duplicates = new ArrayList();
					for (IChildModuleReference tmp : children) {
						if (tmp.getRelativeURI().equals(cm.getRelativeURI()))
							duplicates.add(tmp);
					}
					children.removeAll(duplicates);
					children.add(cm);
				}
			}
    	}
	}
	
	protected boolean isChildModule(IVirtualComponent component, IVirtualReference referencedComponent) {
		for (int i = 0; i < delegates.length; i++) {
			if (delegates[i].isChildModule(component, referencedComponent, dataModel))
				return true;
		}
		return false;
	}

	protected boolean shouldIgnoreReference(IVirtualComponent component, IVirtualReference referencedComponent) {
		for (int i = 0; i < delegates.length; i++ ) {
			if (delegates[i].shouldIgnoreReference(component, referencedComponent, dataModel))
				return true;
		}
		return false;
	}
	

	public boolean shouldAddComponentFile(IVirtualComponent component, IFlatFile file) {
		for (int i = 0; i < delegates.length; i++) {
			if (delegates[i].isChildModule(component, dataModel, file)) {
				ChildModuleReference child = new ChildModuleReference(component.getProject(), file);
				children.add(child); 
				return false;
			} else if (!delegates[i].shouldAddExportableFile(component, component, dataModel, file))
				return false;
		}
		return true;
	}
	

}
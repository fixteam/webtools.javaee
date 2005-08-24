/*******************************************************************************
 * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jst.j2ee.internal.jca.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jem.util.logger.proxy.Logger;
import org.eclipse.jst.j2ee.application.internal.operations.J2EEComponentCreationOperation;
import org.eclipse.jst.j2ee.internal.J2EEConstants;
import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
import org.eclipse.jst.j2ee.jca.Connector;
import org.eclipse.jst.j2ee.jca.modulecore.util.ConnectorArtifactEdit;
import org.eclipse.wst.common.componentcore.ComponentCore;
import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

public class ConnectorComponentCreationOperation extends J2EEComponentCreationOperation implements IConnectorComponentCreationDataModelProperties{

    public ConnectorComponentCreationOperation(IDataModel model) {
        super(model);
    }

    protected void createAndLinkJ2EEComponentsForMultipleComponents() throws CoreException {
        IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
        component.create(0, null);
        //create and link connectorModule Source Folder
        IVirtualFolder connectorModuleFolder = component.getRootFolder().getFolder(new Path("/")); //$NON-NLS-1$        
        connectorModuleFolder.createLink(new Path(model.getStringProperty(JAVASOURCE_FOLDER)), 0, null); //$NON-NLS-1$ //$NON-NLS-2$
        
        //create and link META-INF folder
        IVirtualFolder metaInfFolder = connectorModuleFolder.getFolder(J2EEConstants.META_INF);
        metaInfFolder.create(IResource.FORCE, null);    
    }
    
    protected void createAndLinkJ2EEComponentsForSingleComponent() throws CoreException {
        IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
        component.create(0, null);
        //create and link connectorModule Source Folder
        IVirtualFolder connectorModuleFolder = component.getRootFolder().getFolder(new Path("/")); //$NON-NLS-1$        
        connectorModuleFolder.createLink(new Path(model.getStringProperty(JAVASOURCE_FOLDER)), 0, null); //$NON-NLS-1$ //$NON-NLS-2$
        
        //create and link META-INF folder
        IVirtualFolder metaInfFolder = connectorModuleFolder.getFolder(J2EEConstants.META_INF);
        metaInfFolder.create(IResource.FORCE, null);    
    }
    
    protected void createDeploymentDescriptor(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
        ConnectorArtifactEdit artifactEdit = null;
        Integer version = null;
        try {
			ComponentHandle handle = ComponentHandle.create(getProject(),model.getStringProperty(COMPONENT_DEPLOY_NAME));
            artifactEdit = ConnectorArtifactEdit.getConnectorArtifactEditForWrite(handle);
            version = (Integer)model.getProperty(COMPONENT_VERSION);
            Connector connector = (Connector)artifactEdit.createModelRoot(version.intValue());
//TODO: implement pre-fill of dd     
//            artifactEdit.save(monitor);
//            
//            String sourceFolder = "connectorModule"; //$NON-NLS-1$
//            IContainer container = getProject().getFolder(sourceFolder);
//            IFile file = container.getFile(new Path(J2EEConstants.RAR_DD_URI));
//            OutputStream out = new WorkbenchByteArrayOutputStream(file);
//            
//            String template = version.intValue() == J2EEVersionConstants.JCA_1_0_ID
//                    ? IConnectorNatureConstants.CONNECTOR_XML_TEMPLATE_10
//                    : IConnectorNatureConstants.CONNECTOR_XML_TEMPLATE_15;
//            InputStream in = getClass().getResourceAsStream(template);
//            if (in != null & out != null) {
//                try {
//                    ArchiveUtil.copy(in, out);
//                } catch (IOException ioe) {
//                    Logger.getLogger().logError(ioe);
//                } finally{
//                    try{
//                        if(null != out){
//                            out.close();
//                        } if(null != in){
//                            in.close();
//                        }
//                    }catch (IOException ioe) {
//                        Logger.getLogger().logError(ioe);
//                    } 
//                }
//
//                if( connector != null ){
//                    String projectName = model.getStringProperty(PROJECT_NAME);
//                    if( projectName != null )
//                        connector.setDisplayName(projectName);
//                }
//            }
            
            artifactEdit.save(monitor);
        } catch (Exception e) {
            Logger.getLogger().logError(e);
        } finally {
            if (artifactEdit != null)
                artifactEdit.dispose();
        }
        

    }

    protected String getVersion() {
        int version = model.getIntProperty(COMPONENT_VERSION);
        return J2EEVersionUtil.getJCATextVersion(version);
    }

    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        try {
            super.execute(IModuleConstants.JST_CONNECTOR_MODULE, monitor);
        } catch (CoreException e) {
            Logger.getLogger().log(e.getMessage());
        } catch (InvocationTargetException e) {
            Logger.getLogger().log(e.getMessage());
        } catch (InterruptedException e) {
            Logger.getLogger().log(e.getMessage());
        }
        return OK_STATUS;
    }

    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
        // TODO Auto-generated method stub
        return null;
    }

}

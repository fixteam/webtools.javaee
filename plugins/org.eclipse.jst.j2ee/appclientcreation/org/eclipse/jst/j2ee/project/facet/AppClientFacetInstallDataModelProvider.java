package org.eclipse.jst.j2ee.project.facet;

import java.util.Set;

import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;

public class AppClientFacetInstallDataModelProvider
	extends FacetInstallDataModelProvider
	implements IJ2EEFacetInstallDataModelProperties{

	public Set getPropertyNames() {
		Set names = super.getPropertyNames();
		names.add(EAR_PROJECT_NAME);
		names.add(CONFIG_FOLDER);
		return names;
	}
	
	public Object getDefaultProperty(String propertyName) {
		if(propertyName.equals(FACET_ID)){
			return J2EEProjectUtilities.APPLICATION_CLIENT;
		}
		return super.getDefaultProperty(propertyName);
	}
	
	public IDataModelOperation getDefaultOperation() {
		return new AppClientFacetInstallOperation(model);
	}
}

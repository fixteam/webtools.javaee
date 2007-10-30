package org.eclipse.jst.servlet.ui.internal.wizard;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties;
import org.eclipse.jst.j2ee.internal.web.operations.INewFilterClassDataModelProperties;
import org.eclipse.jst.j2ee.internal.wizard.StringArrayTableWizardSection;
import org.eclipse.jst.j2ee.model.IModelProvider;
import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.webapplication.Servlet;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.common.componentcore.internal.operation.IArtifactEditOperationDataModelProperties;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;

/**
 * Filter Wizard Setting Page
 */
public class AddFilterWizardPage extends DataModelWizardPage {
	final static String[] FILTEREXTENSIONS = {"java"}; //$NON-NLS-1$

	private Text displayNameText;

	private StringArrayTableWizardSection urlSection;
	private ServletNameArrayTableWizardSection servletSection;

	public AddFilterWizardPage(IDataModel model, String pageName) {
		super(model, pageName);
		setDescription(IWebWizardConstants.ADD_FILTER_WIZARD_PAGE_DESC);
		this.setTitle(IWebWizardConstants.ADD_FILTER_WIZARD_PAGE_TITLE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jem.util.ui.wizard.WTPWizardPage#getValidationPropertyNames()
	 */
	protected String[] getValidationPropertyNames() {
		return new String[]{INewFilterClassDataModelProperties.DISPLAY_NAME, 
		        INewFilterClassDataModelProperties.INIT_PARAM, 
		        INewFilterClassDataModelProperties.URL_MAPPINGS,
		        INewFilterClassDataModelProperties.SERVLET_MAPPINGS};
	}

	protected Composite createTopLevelComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = 300;
		composite.setLayoutData(data);

		createNameDescription(composite);
		
		StringArrayTableWizardSectionCallback callback = new StringArrayTableWizardSectionCallback();
		StringArrayTableWizardSection initSection = new StringArrayTableWizardSection(composite, IWebWizardConstants.INIT_PARAM_LABEL, IWebWizardConstants.ADD_BUTTON_LABEL, IWebWizardConstants.EDIT_BUTTON_LABEL, 
				IWebWizardConstants.REMOVE_BUTTON_LABEL, new String[]{IWebWizardConstants.NAME_LABEL, IWebWizardConstants.VALUE_LABEL, IWebWizardConstants.DESCRIPTION_LABEL}, null,// WebPlugin.getDefault().getImage("initializ_parameter"),
				model, INewFilterClassDataModelProperties.INIT_PARAM);
		initSection.setCallback(callback);
		
		urlSection = new StringArrayTableWizardSection(composite, IWebWizardConstants.URL_MAPPINGS_LABEL, IWebWizardConstants.ADD_BUTTON_LABEL, IWebWizardConstants.EDIT_BUTTON_LABEL, IWebWizardConstants.REMOVE_BUTTON_LABEL,
				new String[]{IWebWizardConstants.URL_PATTERN_LABEL}, null,// WebPlugin.getDefault().getImage("url_type"),
				model, INewFilterClassDataModelProperties.URL_MAPPINGS);
		urlSection.setCallback(callback);

		IProject p = (IProject) model.getProperty(INewJavaClassDataModelProperties.PROJECT);
		IModelProvider provider = ModelProviderManager.getModelProvider(p);
		Object mObj = provider.getModelObject();
		ArrayList<String> servletsList = new ArrayList<String>();
		if (mObj instanceof org.eclipse.jst.j2ee.webapplication.WebApp) {
			org.eclipse.jst.j2ee.webapplication.WebApp webApp = (org.eclipse.jst.j2ee.webapplication.WebApp) mObj;
			List<Servlet> servlets = webApp.getServlets();
			for (Servlet servlet : servlets) {
				servletsList.add(servlet.getServletName());
			}
		} else if (mObj instanceof org.eclipse.jst.javaee.web.WebApp) {
			org.eclipse.jst.javaee.web.WebApp webApp= (org.eclipse.jst.javaee.web.WebApp) mObj;
			List<org.eclipse.jst.javaee.web.Servlet> servlets = webApp.getServlets();
			for (org.eclipse.jst.javaee.web.Servlet servlet : servlets) {
				servletsList.add(servlet.getServletName());
			}
		}
		ServletNamesArrayTableWizardSectionCallback callback1 =  new ServletNamesArrayTableWizardSectionCallback(servletsList);
        servletSection = new ServletNameArrayTableWizardSection(composite, 
                IWebWizardConstants.SERVLET_MAPPINGS_LABEL, IWebWizardConstants.ADD_BUTTON_LABEL, 
                IWebWizardConstants.REMOVE_BUTTON_LABEL,
                new String[]{IWebWizardConstants.SERVLET_NAME_LABEL}, null,
                model, INewFilterClassDataModelProperties.SERVLET_MAPPINGS);
        servletSection.setCallback(callback1);
		
		String text = displayNameText.getText();
		// Set default URL Pattern
		List input = new ArrayList();
		input.add(new String[]{"/" + text}); //$NON-NLS-1$
		if (urlSection != null)
			urlSection.setInput(input);
		displayNameText.setFocus();

		IStatus projectStatus = validateProjectName();
		if (!projectStatus.isOK()) {
			setErrorMessage(projectStatus.getMessage());
			composite.setEnabled(false);
		}
	    Dialog.applyDialogFont(parent);
		return composite;
	}

	protected IStatus validateProjectName() {
		// check for empty
		if (model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME) == null || model.getStringProperty(IArtifactEditOperationDataModelProperties.PROJECT_NAME).trim().length() == 0) {
			return WTPCommonPlugin.createErrorStatus(IWebWizardConstants.NO_WEB_PROJECTS);
		}
		return WTPCommonPlugin.OK_STATUS;
	}

	protected void createNameDescription(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// display name
		Label displayNameLabel = new Label(composite, SWT.LEFT);
		displayNameLabel.setText(IWebWizardConstants.NAME_LABEL);
		displayNameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		displayNameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		displayNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		displayNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String text = displayNameText.getText();
				// Set default URL Pattern
				List input = new ArrayList();
				input.add(new String[]{"/" + text}); //$NON-NLS-1$
				if (urlSection != null)
					urlSection.setInput(input);
			}

		});
		synchHelper.synchText(displayNameText, INewFilterClassDataModelProperties.DISPLAY_NAME, null);

		// description
		Label descLabel = new Label(composite, SWT.LEFT);
		descLabel.setText(IWebWizardConstants.DESCRIPTION_LABEL);
		descLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
		Text descText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		descText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		synchHelper.synchText(descText, INewFilterClassDataModelProperties.DESCRIPTION, null);
	}

	public String getDisplayName() {
		return displayNameText.getText();
	}
	
	public boolean canFlipToNextPage() {
		if (model.getBooleanProperty(INewFilterClassDataModelProperties.USE_EXISTING_CLASS))
			return false;
		return super.canFlipToNextPage();
	}
}
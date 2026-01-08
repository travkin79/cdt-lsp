package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public abstract class AbstractLspTemplateContextType extends TemplateContextType {

	public AbstractLspTemplateContextType(String contextTypeId, String contextTypeName) {
		super(contextTypeId, contextTypeName);
		addTemplateVariableResolvers();
	}

	protected void addTemplateVariableResolvers() {
		addGlobalTemplateVariableResolvers();
	}

	protected final void addGlobalTemplateVariableResolvers() {
		addResolver(new GlobalTemplateVariables.User());

		addResolver(new GlobalTemplateVariables.Date());
		addResolver(new GlobalTemplateVariables.Time());
		addResolver(new GlobalTemplateVariables.Year());

		addResolver(new GlobalTemplateVariables.Cursor());
		addResolver(new GlobalTemplateVariables.LineSelection());
		addResolver(new GlobalTemplateVariables.WordSelection());

		addResolver(new GlobalTemplateVariables.Dollar());
	}

}

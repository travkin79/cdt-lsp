package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.cdt.lsp.plugin.LspPlugin;

public class CppLspTemplateContextType extends AbstractLspTemplateContextType {

	public static final String CONTEXT_ID = LspPlugin.PLUGIN_ID + ".templates.context.cpp"; //$NON-NLS-1$

	public CppLspTemplateContextType() {
		super(CONTEXT_ID, "C/C++ (LSP)");
	}

	@Override
	protected void addTemplateVariableResolvers() {
		super.addTemplateVariableResolvers();

		// TODO add additional C/C++-specific template variable resolvers
	}

}

package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.cdt.lsp.plugin.LspPlugin;

public class DefaultLspTemplateContextType extends AbstractLspTemplateContextType {

	public static final String CONTEXT_ID = LspPlugin.PLUGIN_ID + ".templates.context"; //$NON-NLS-1$

	public DefaultLspTemplateContextType() {
		super(CONTEXT_ID, "Default context (LSP)");
	}

}

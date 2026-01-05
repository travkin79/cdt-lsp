package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;

public class LspDefaultTemplateContextType extends TemplateContextType {

	public static final String CONTEXT_ID = LspPlugin.PLUGIN_ID + ".templates.context"; //$NON-NLS-1$

	public LspDefaultTemplateContextType() {
		this(CONTEXT_ID, "LSP default context");
	}

	protected LspDefaultTemplateContextType(String id, String name) {
		super(id, name);
		addGlobalTemplateVariableResolvers();
	}

	private void addGlobalTemplateVariableResolvers() {
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

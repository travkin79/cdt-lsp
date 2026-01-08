package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.cdt.lsp.plugin.LspPlugin;

public class CommentLspTemplateContextType extends AbstractLspTemplateContextType {

	public static final String CONTEXT_ID = LspPlugin.PLUGIN_ID + ".templates.context.comment"; //$NON-NLS-1$

	public CommentLspTemplateContextType() {
		super(CONTEXT_ID, "Comment (LSP)");
	}

}

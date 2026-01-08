package org.eclipse.cdt.lsp.editor.assist;

import org.eclipse.cdt.lsp.plugin.LspPlugin;

public class CommentDocumentationLspTemplateContextType extends AbstractLspTemplateContextType {

	public static final String CONTEXT_ID = LspPlugin.PLUGIN_ID + ".templates.context.comment.doc"; //$NON-NLS-1$

	public CommentDocumentationLspTemplateContextType() {
		super(CONTEXT_ID, "Documentation Comment (LSP)");
	}

}

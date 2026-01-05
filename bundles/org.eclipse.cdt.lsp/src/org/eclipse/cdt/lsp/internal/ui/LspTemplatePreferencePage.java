package org.eclipse.cdt.lsp.internal.ui;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.templates.TemplatePreferencePage;

public class LspTemplatePreferencePage extends TemplatePreferencePage {

	public LspTemplatePreferencePage() {
		setPreferenceStore(LspPlugin.getDefault().getPreferenceStore());
		setTemplateStore(LspPlugin.getDefault().getTemplateStore());
		setContextTypeRegistry(LspPlugin.from(LspPlugin.getDefault().getTemplateContextRegistry()));
	}

	private static class LspEditTemplateDialog extends TemplatePreferencePage.EditTemplateDialog {

		public LspEditTemplateDialog(Shell shell, Template template, boolean edit, boolean isNameModifiable,
				org.eclipse.jface.text.templates.ContextTypeRegistry contextTypeRegistry) {
			super(shell, template, edit, isNameModifiable, contextTypeRegistry);
		}

		@Override
		protected SourceViewer createViewer(Composite parent) {
			// TODO configure viewer
			return super.createViewer(parent);
		}
	}

	@Override
	protected String getFormatterPreferenceKey() {
		return super.getFormatterPreferenceKey();
		// TODO adapt pref key
		//return PreferenceConstants.TEMPLATES_USE_CODEFORMATTER;
	}

	@Override
	protected Template editTemplate(Template template, boolean edit, boolean isNameModifiable) {
		LspEditTemplateDialog dialog = new LspEditTemplateDialog(getShell(), template, edit, isNameModifiable,
				getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			return dialog.getTemplate();
		}
		return null;
	}

	@Override
	protected SourceViewer createViewer(Composite parent) {
		// TODO configure viewer
		return super.createViewer(parent);
	}

	// * create preference page for editing / adding / removing custom code templates (for all LSP-based editors)
	// * add context types, at least one, for LSP-based editors
	// * generalize implementation to LSP4E and arbitrary editors
	// * finish code template proposal calculation
	// * sort proposals
}

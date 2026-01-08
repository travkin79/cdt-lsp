/*******************************************************************************
 * Copyright (c) 2023, 2024, 2025 Bachmann electronic GmbH and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Gesa Hentschke (Bachmann electronic GmbH) - initial implementation
 * Alexander Fedorov (ArSysOp) - use Platform for logging
 *******************************************************************************/

package org.eclipse.cdt.lsp.plugin;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.cdt.lsp.editor.assist.CommentDocumentationLspTemplateContextType;
import org.eclipse.cdt.lsp.editor.assist.CommentLspTemplateContextType;
import org.eclipse.cdt.lsp.editor.assist.CppLspTemplateContextType;
import org.eclipse.cdt.lsp.editor.assist.DefaultLspTemplateContextType;
import org.eclipse.cdt.lsp.internal.server.CLanguageServerEnableCache;
import org.eclipse.cdt.lsp.internal.server.CLanguageServerRegistry;
import org.eclipse.cdt.lsp.server.ICLanguageServerProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.text.templates.ContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class LspPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.lsp"; //$NON-NLS-1$
	public static final String LSP_C_EDITOR_ID = "org.eclipse.cdt.lsp.CEditor"; //$NON-NLS-1$
	public static final String C_EDITOR_ID = "org.eclipse.cdt.ui.editor.CEditor"; //$NON-NLS-1$

	private static final String CUSTOM_TEMPLATES_KEY = PLUGIN_ID + ".text.templates.custom"; //$NON-NLS-1$
	private static final String TEMPLATES_REGISTRY_ID = PLUGIN_ID + ".templates"; //$NON-NLS-1$

	// The shared instance
	private static LspPlugin plugin;

	private ICLanguageServerProvider cLanguageServerProvider;

	private ContributionContextTypeRegistry contextTypeRegistry = null;
	private TemplateStore templateStore = null;

	// Disable warnings, see https://github.com/eclipse-cdt/cdt-lsp/issues/88 and https://github.com/eclipse-cdt/cdt-lsp/issues/101.
	// We keep this reference to avoid the logger being garbage collected.
	private static final Logger logger = Logger.getLogger("org.eclipse.tm4e.core.internal.oniguruma.OnigRegExp"); //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public LspPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		cLanguageServerProvider = new CLanguageServerRegistry().createCLanguageServerProvider();

		// Disable warnings, see https://github.com/eclipse-cdt/cdt-lsp/issues/88 and https://github.com/eclipse-cdt/cdt-lsp/issues/101
		logger.setLevel(Level.SEVERE);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		CLanguageServerEnableCache.stop();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static LspPlugin getDefault() {
		return plugin;
	}

	public ICLanguageServerProvider getCLanguageServerProvider() {
		return cLanguageServerProvider;
	}

	public ContextTypeRegistry getTemplateContextRegistry() {
		if (contextTypeRegistry == null) {
			contextTypeRegistry = new ContributionContextTypeRegistry(TEMPLATES_REGISTRY_ID);
			contextTypeRegistry.addContextType(DefaultLspTemplateContextType.CONTEXT_ID);
			contextTypeRegistry.addContextType(CommentLspTemplateContextType.CONTEXT_ID);
			contextTypeRegistry.addContextType(CommentDocumentationLspTemplateContextType.CONTEXT_ID);

			// TODO add language-specific context types, probably from extensions
			contextTypeRegistry.addContextType(CppLspTemplateContextType.CONTEXT_ID);
		}
		return contextTypeRegistry;
	}

	@SuppressWarnings("deprecation")
	private static class ContextTypeRegistryWrapper extends org.eclipse.jface.text.templates.ContextTypeRegistry {

		@Override
		public void addContextType(TemplateContextType contextType) {
			delegate.addContextType(contextType);
		}

		@Override
		public Iterator<TemplateContextType> contextTypes() {
			return delegate.contextTypes();
		}

		@Override
		public TemplateContextType getContextType(String id) {
			return delegate.getContextType(id);
		}

		private final ContextTypeRegistry delegate;

		public ContextTypeRegistryWrapper(ContextTypeRegistry registry) {
			this.delegate = registry;
		}

	}

	public static ContextTypeRegistryWrapper from(ContextTypeRegistry registry) {
		return new ContextTypeRegistryWrapper(registry);
	}

	public TemplateStore getTemplateStore() {
		if (templateStore == null) {
			templateStore = new ContributionTemplateStore(from(getTemplateContextRegistry()), getPreferenceStore(),
					CUSTOM_TEMPLATES_KEY);
			try {
				templateStore.load();
			} catch (IOException e) {
				Platform.getLog(this.getClass()).error(e.getMessage(), e);
			}

			//			Template[] templates = CUIPlugin.getDefault().getTemplateStore().getTemplates();
			//			for (Template template : templates) {
			//				System.out.println("CDT template: " + template.getName() + ", " + template.getDescription() + ", "
			//						+ template.getContextTypeId());
			//			}
			// TODO check if the templates were already added (and if they are custom / user-made)
			//			for (TemplatePersistenceData data : CUIPlugin.getDefault().getTemplateStore().getTemplateData(false)) {
			//				if ("org.eclipse.cdt.ui.text.templates.c".equals(data.getTemplate().getContextTypeId())) {
			//					// TODO change the data, adapt the context type to LSP
			//					Template originalTemplate = data.getTemplate();
			//					Template templateCopy = new Template(originalTemplate.getName(), originalTemplate.getDescription(),
			//							CppLspTemplateContextType.CONTEXT_ID, originalTemplate.getPattern(),
			//							originalTemplate.isAutoInsertable());
			//					TemplatePersistenceData copy = new TemplatePersistenceData(templateCopy, data.isEnabled());
			//
			//					templateStore.add(copy);
			//				}
			//			}

			templateStore.startListeningForPreferenceChanges();
		}
		return templateStore;
	}

}

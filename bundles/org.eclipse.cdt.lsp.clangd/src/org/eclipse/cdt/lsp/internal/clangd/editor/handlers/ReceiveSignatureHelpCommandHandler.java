package org.eclipse.cdt.lsp.internal.clangd.editor.handlers;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4j.SignatureHelp;
import org.eclipse.lsp4j.SignatureHelpParams;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IURIEditorInput;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ReceiveSignatureHelpCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return execute(HandlerUtil.getActiveEditor(event), HandlerUtil.getCurrentSelection(event));
	}

	@SuppressWarnings("restriction")
	private Object execute(IEditorPart activeEditor, ISelection currentSelection) {
		// Try to adapt to ITextEditor (e.g. to support editors embedded in
		// MultiPageEditorParts), otherwise use activeEditor.
		IEditorPart innerEditor = Optional.ofNullable((IEditorPart) Adapters.adapt(activeEditor, ITextEditor.class))
				.orElse(activeEditor);

		if (!(currentSelection instanceof ITextSelection)) {
			return null;
		}

		ITextSelection textSelection = (ITextSelection) currentSelection;

		getUri(innerEditor).ifPresent(fileUri -> {
			IDocument document = LSPEclipseUtils.getDocument(innerEditor.getEditorInput());

			SignatureHelpParams signatureHelpParams = new SignatureHelpParams();
			try {
				signatureHelpParams.setTextDocument(LSPEclipseUtils.toTextDocumentIdentifier(document));
				signatureHelpParams.setPosition(LSPEclipseUtils.toPosition(textSelection.getOffset(), document));
			} catch (BadLocationException e) {
				// TODO handle this
				e.printStackTrace();
			}

			try {
				Optional<SignatureHelp> result = LanguageServers.forDocument(document)
						.computeFirst(server -> server.getTextDocumentService().signatureHelp(signatureHelpParams))
						.get();
				if (result.isPresent()) {
					// TODO return AST
					System.out.println(result.get());
				}
			} catch (InterruptedException | java.util.concurrent.ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

		return null;
	}

	/**
	 * Returns the URI of the given editor depending on the type of its
	 * {@link IEditorPart#getEditorInput()}.
	 *
	 * @return the URI or an empty {@link Optional} if the URI couldn't be
	 *         determined.
	 */
	private static Optional<URI> getUri(IEditorPart editor) {
		IEditorInput editorInput = editor.getEditorInput();

		if (editorInput instanceof IFileEditorInput) {
			return Optional.of(((IFileEditorInput) editor.getEditorInput()).getFile().getLocationURI());
		} else if (editorInput instanceof IURIEditorInput) {
			return Optional.of(((IURIEditorInput) editorInput).getURI());
		} else {
			return Optional.empty();
		}
	}

}

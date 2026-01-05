package org.eclipse.cdt.lsp.editor.assist;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.cdt.lsp.plugin.LspPlugin;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4e.LanguageServerWrapper;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.internal.CancellationUtil;
import org.eclipse.lsp4e.operations.semanticTokens.AbstractSemanticTokensDataStreamProcessor;
import org.eclipse.lsp4e.operations.semanticTokens.SemanticTokensClient;
import org.eclipse.lsp4e.outline.SymbolsModel;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.SemanticTokenModifiers;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm4e.core.grammar.IGrammar;
import org.eclipse.tm4e.core.grammar.ITokenizeLineResult;
import org.eclipse.tm4e.languageconfiguration.internal.registry.ILanguageConfigurationDefinition;
import org.eclipse.tm4e.languageconfiguration.internal.registry.LanguageConfigurationRegistryManager;
import org.eclipse.tm4e.registry.ITMScope;
import org.eclipse.tm4e.registry.TMEclipseRegistryPlugin;
import org.eclipse.tm4e.ui.text.TMPartitions;

public class LspTemplateCompletionProcessor extends TemplateCompletionProcessor {

	private static final ICompletionProposal[] NO_PROPOSALS = {};
	private static final Template[] NO_TEMPLATES = {};

	private URI toUri(IDocument document) {
		IFile documentFile;
		URI documentURI;

		IPath path = LSPEclipseUtils.toPath(document);
		if (path == null) {
			documentFile = null;
			URI uri = null;
			URI docUri = LSPEclipseUtils.toUri(document);
			if (docUri != null) {
				TextDocumentIdentifier docIdentifier = LSPEclipseUtils.toTextDocumentIdentifier(docUri);
				// Set the documentURI if valid URI for LSs that use custom protocols e.g. jdt://
				try {
					uri = new URI(docIdentifier.getUri());
				} catch (URISyntaxException e) {
					// Ignore
				}
			}
			documentURI = uri;
		} else {
			documentFile = LSPEclipseUtils.getFile(path);
			documentURI = documentFile == null ? LSPEclipseUtils.toUri(path) : LSPEclipseUtils.toUri(documentFile);
		}
		return documentURI;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {

		//ICompletionProposal[] templateProposals = super.computeCompletionProposals(viewer, offset);

		if (viewer == null || viewer.getDocument() == null) {
			return NO_PROPOSALS;
		}

		IDocument document = viewer.getDocument();
		URI documentURI = toUri(document);
		SymbolsModel symbolsModel = new SymbolsModel();
		symbolsModel.setUri(documentURI);

		CompletableFuture<List<Either<SymbolInformation, DocumentSymbol>>> symbols;
		final var params = new DocumentSymbolParams(LSPEclipseUtils.toTextDocumentIdentifier(document));
		CompletableFuture<Optional<LanguageServerWrapper>> languageServer = LanguageServers.forDocument(document)
				.withCapability(ServerCapabilities::getDocumentSymbolProvider)
				.computeFirst((w, ls) -> CompletableFuture.completedFuture(w));

		try {
			symbols = languageServer.get(1000, TimeUnit.MILLISECONDS).filter(Objects::nonNull)
					.filter(LanguageServerWrapper::isActive)
					.map(s -> s.execute(ls -> ls.getTextDocumentService().documentSymbol(params)))
					.orElse(CompletableFuture.completedFuture(null));
		} catch (TimeoutException | ExecutionException | InterruptedException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			symbols = CompletableFuture.completedFuture(null);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}

		symbols.thenAcceptAsync(response -> symbolsModel.update(response)).join();

		try {
			symbolsModel.update(symbols.get());
		} catch (InterruptedException | ExecutionException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
		}

		Object[] elements = symbolsModel.getElements();

		// ################################################

		try {
			// TODO It seems, clangd does not return sematic tokens for comments. Try using TM4E tokens instead?
			List<ContextTypeRegion> contextTypeRegions = SemanticTokensClient.DEFAULT
					.requestFullSemanticTokens(document,
							(legend, semanticTokens) -> convertToContextTypeRegions(document, semanticTokens, legend))
					.thenApply(regionsOpt -> regionsOpt.orElse(Collections.emptyList())).get(300, TimeUnit.SECONDS);

			System.out.println(contextTypeRegions.size());
		} catch (TimeoutException e) {
			LanguageServerPlugin.logWarning(
					"Timed out after waiting for %dms for semantic tokens from Language Servers".formatted(300), e);
		} catch (InterruptedException e) {
			LanguageServerPlugin.logError(e);
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			if (!CancellationUtil.isRequestCancelledException(e)) { // do not report error if the server has cancelled the request
				//				LanguageServerPlugin.logError("Failed to fetch semantic tokens for '%s' from Language Servers"
				//						.formatted(resource.getLocation()), e);
			}
		}

		// ################################################

		IDocumentPartitioner partitioner = document.getDocumentPartitioner();
		if (partitioner != null) {
			ITypedRegion region = partitioner.getPartition(offset);
			if (region != null) {
				String regionType = region.getType();

				System.out.println(regionType);
			}
		}
		if (TMPartitions.hasPartitioning(document)) {
			var contentTypes = TMPartitions.getContentTypesForOffset(document, offset);
			System.out.println(contentTypes.length);
		}

		ILanguageConfigurationDefinition[] langs = LanguageConfigurationRegistryManager.getInstance().getDefinitions();
		for (ILanguageConfigurationDefinition lang : langs) {
			System.out.println(
					"Language config: " + lang.getContentType().getName() + ", " + lang.getContentType().getId());
		}

		ILanguageConfigurationDefinition cppLang = Arrays.stream(langs)
				.filter(lang -> "org.eclipse.tm4e.language_pack.cpp".equals(lang.getContentType().getId())).findFirst()
				.orElse(null);
		IContentType cppContentType = cppLang.getContentType();
		ITMScope cppScope = ITMScope.parse("source.cpp");
		IGrammar cppGrammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarForScope(cppScope);

		IGrammar grammar = TMEclipseRegistryPlugin.getGrammarRegistryManager().getGrammarFor(cppContentType);
		try {
			IRegion lineRegion = document.getLineInformationOfOffset(offset);
			int line = document.getLineOfOffset(offset);
			int lineStart = lineRegion.getOffset();
			String lineText = document.get(lineStart, lineRegion.getLength());
			int column = offset - lineStart;
			ITokenizeLineResult tokenResult = cppGrammar.tokenizeLine(lineText);

			System.out.println(tokenResult.toString());
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			String type = document.getContentType(offset);

			System.out.println(type);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//CommentSupport rule = cppLang.getLanguageConfiguration().getComments();

		//		CommentSupport commentSupport = LanguageConfigurationRegistryManager.getInstance()
		//				.getCommentSupport(cppContentType);

		// LanguageConfigurationRegistryManager.getInstance().getCommentSupport(null)

		//Image image = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TEMPLATE);

		String prefix = extractPrefix(viewer, offset);
		Region region = new Region(offset - prefix.length(), prefix.length());
		TemplateContext context = createContext(viewer, region);

		// TODO consider selection

		if (context == null) {
			return NO_PROPOSALS;
		}

		//Template[] templates = CUIPlugin.getDefault().getTemplateStore().getTemplates();
		Template[] templates = getTemplates(context.getContextType().getId());

		ICompletionProposal[] proposals = Arrays.stream(templates).filter(t -> context.canEvaluate(t))
				// TODO consider selection / offset prefix and filter templates according to that
				// TODO calculate relevance / order value
				.map(template -> createProposal(template, context, region, 1))
				.toArray(size -> new ICompletionProposal[size]);

		if (proposals.length > 0) {
			return proposals;
		}

		String replacementText = "test completion"; //$NON-NLS-1$
		return new ICompletionProposal[] {
				new CompletionProposal(replacementText, offset, replacementText.length(), offset) };
	}

	private static class ContextTypeRegion extends Region {
		private final TemplateContextType contextType;

		public ContextTypeRegion(TemplateContextType contextType, int offset, int length) {
			super(offset, length);
			this.contextType = contextType;
		}

		public TemplateContextType getContextType() {
			return this.contextType;
		}
	}

	private List<ContextTypeRegion> convertToContextTypeRegions(IDocument document, SemanticTokens semanticTokens,
			SemanticTokensLegend legend) {
		if (semanticTokens == null || legend == null) {
			return Collections.emptyList();
		}

		return new SemanticTokenToContextTypeProcessor(this::mapToContextTypeId, pos -> positionToOffset(document, pos))
				.getTokensData(semanticTokens.getData(), legend);
	}

	private static class SemanticTokenToContextTypeProcessor
			extends AbstractSemanticTokensDataStreamProcessor<String, ContextTypeRegion> {

		public SemanticTokenToContextTypeProcessor(final Function<String, String> tokenTypeMapper,
				final Function<Position, Integer> offsetMapper) {
			super(offsetMapper, tokenTypeMapper);
		}

		@Override
		protected ContextTypeRegion createTokenData(String contextTypeId, int offset, int length,
				List<String> tokenModifiers) {

			if (contextTypeId != null) {
				// TODO pick up the right context type and check token modifiers
				if (tokenModifiers.contains(SemanticTokenModifiers.Documentation)) {
					// TODO Do we have a (javadoc) comment here?
				}
				TemplateContextType contextType = LspPlugin.getDefault().getTemplateContextRegistry()
						.getContextType(LspDefaultTemplateContextType.CONTEXT_ID);
				return new ContextTypeRegion(contextType, offset, length);
			}
			return null;
		}
	}

	private String mapToContextTypeId(String tokenType) {
		System.out.println("Token type: " + tokenType);

		return switch (tokenType) {
		// TODO add more context types
		case "comment" -> LspDefaultTemplateContextType.CONTEXT_ID + ".comment"; //$NON-NLS-1$ //$NON-NLS-2$
		default -> LspDefaultTemplateContextType.CONTEXT_ID;
		};
	}

	private Integer positionToOffset(IDocument document, Position position) {
		try {
			return LSPEclipseUtils.toOffset(position, document);
		} catch (BadLocationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TemplateContextType getContextType(ITextViewer viewer, IRegion region) {
		// TODO Detect context type depending on given region
		return LspPlugin.getDefault().getTemplateContextRegistry()
				.getContextType(LspDefaultTemplateContextType.CONTEXT_ID);
	}

	@Override
	protected Image getImage(Template template) {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TEMPLATE);
	}

	@Override
	protected Template[] getTemplates(String contextTypeId) {
		if (!LspDefaultTemplateContextType.CONTEXT_ID.equals(contextTypeId)) {
			return NO_TEMPLATES;
		}

		TemplateStore templateStore = LspPlugin.getDefault().getTemplateStore();
		if (templateStore == null) {
			return NO_TEMPLATES;
		}

		Template[] customTemplates = templateStore.getTemplates(contextTypeId);
		if (customTemplates == null || customTemplates.length == 0) {
			return NO_TEMPLATES;
		}

		return customTemplates;
	}

}

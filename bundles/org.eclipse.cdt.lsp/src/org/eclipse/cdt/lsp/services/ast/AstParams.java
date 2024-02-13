package org.eclipse.cdt.lsp.services.ast;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.lsp4j.util.ToStringBuilder;

public class AstParams extends TextDocumentPositionParams {

	@NonNull
	private Range range;

	public AstParams() {
	}

	public AstParams(@NonNull final TextDocumentIdentifier textDocument, @NonNull final Range range) {
		super(textDocument, range.getStart());
		this.range = Preconditions.<Range>checkNotNull(range, "range"); //$NON-NLS-1$
	}

	@NonNull
	public Range getRange() {
		return range;
	}

	public void setRange(@NonNull Range range) {
		this.range = Preconditions.<Range>checkNotNull(range, "range"); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("textDocument", getTextDocument()); //$NON-NLS-1$
		b.add("range", getRange()); //$NON-NLS-1$
		return b.toString();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstParams other = (AstParams) obj;
		if (this.getTextDocument() == null) {
			if (other.getTextDocument() != null)
				return false;
		} else if (!this.getTextDocument().equals(other.getTextDocument()))
			return false;
		if (this.range == null) {
			if (other.range != null)
				return false;
		} else if (!this.range.equals(other.range))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.getTextDocument() == null) ? 0 : this.getTextDocument().hashCode());
		return prime * result + ((this.range == null) ? 0 : this.range.hashCode());
	}
}

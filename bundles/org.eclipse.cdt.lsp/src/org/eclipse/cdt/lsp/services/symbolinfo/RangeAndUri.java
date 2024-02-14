package org.eclipse.cdt.lsp.services.symbolinfo;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;
import org.eclipse.lsp4j.util.ToStringBuilder;

@SuppressWarnings("all")
public class RangeAndUri {
	@NonNull
	private Range range;

	@NonNull
	private String uri;

	@NonNull
	public Range getRange() {
		return this.range;
	}

	public void setRange(@NonNull final Range range) {
		this.range = Preconditions.checkNotNull(range, "range");
	}

	@NonNull
	public String getUri() {
		return this.uri;
	}

	public void setUri(@NonNull final String uri) {
		this.uri = Preconditions.checkNotNull(uri, "uri");
	}

	@Override
	public String toString() {
		ToStringBuilder b = new ToStringBuilder(this);
		b.add("range", this.range);
		b.add("uri", this.uri);
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
		RangeAndUri other = (RangeAndUri) obj;
		if (this.range == null) {
			if (other.range != null)
				return false;
		} else if (!this.range.equals(other.range))
			return false;
		if (this.uri == null) {
			if (other.uri != null)
				return false;
		} else if (!this.uri.equals(other.uri))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.range == null) ? 0 : this.range.hashCode());
		return prime * result + ((this.uri == null) ? 0 : this.uri.hashCode());
	}
}

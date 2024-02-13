package org.eclipse.cdt.lsp.services.ast;

import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.eclipse.lsp4j.util.Preconditions;

public class AstNode {

	@NonNull
	private String role;

	@NonNull
	private String kind;

	private String detail;

	private String arcana;

	@NonNull
	private Range range;

	private AstNode[] children;

	public AstNode() {

	}

	public String getRole() {
		return role;
	}

	public void setRole(@NonNull String role) {
		this.role = Preconditions.<String>checkNotNull(kind, "kind"); //$NON-NLS-1$
	}

	public String getKind() {
		return kind;
	}

	public void setKind(@NonNull String kind) {
		this.kind = Preconditions.<String>checkNotNull(kind, "kind"); //$NON-NLS-1$
	}

	public String getDetail() {
		return detail;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getArcana() {
		return arcana;
	}

	public void setArcana(String arcana) {
		this.arcana = arcana;
	}

	public Range getRange() {
		return range;
	}

	public void setRange(@NonNull Range range) {
		this.range = Preconditions.<Range>checkNotNull(range, "range"); //$NON-NLS-1$
	}

	public AstNode[] getChildren() {
		return children;
	}

	public void setChildren(AstNode[] children) {
		this.children = children;
	}

}

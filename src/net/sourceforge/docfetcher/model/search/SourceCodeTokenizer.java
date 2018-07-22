package net.sourceforge.docfetcher.model.search;

import org.apache.lucene.analysis.util.CharTokenizer;

public final class SourceCodeTokenizer extends CharTokenizer {

	/**
	 * Collects only characters which can be part of an identifier in typical
	 * programming languages.
	 */
	@Override
	protected boolean isTokenChar(int c) {
	    return Character.isLetterOrDigit(c) || (c=='_');
	}
	
}

/*******************************************************************************
 * Copyright (c) 2018 Zhengmian Hu.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Zhengmian Hu - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model.index;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiReader;

import java.io.IOException;

/**
 * Created by huzhengmian on 2018/5/7.
 */
public class DecoratedMultiReader extends MultiReader {
    public DecoratedMultiReader(IndexReader... subReaders) throws IOException {
        super(subReaders);
    }

    public DecoratedMultiReader(IndexReader[] subReaders, boolean closeSubReaders) throws IOException {
        super(subReaders,closeSubReaders);
    }

    public final int decoratedReaderIndex(int docID) {
        return super.readerIndex(docID);
    }
}

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

package net.sourceforge.docfetcher.model;

import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.TextField;

/**
 * Created by huzhengmian on 2018/5/7.
 */
public class FieldTypes {
    public static final FieldType TYPE_TEXT_WITH_POSITIONS_OFFSETS_STORED=new FieldType(TextField.TYPE_STORED);
    public static final FieldType TYPE_TEXT_WITH_POSITIONS_OFFSETS_NOT_STORED=new FieldType(TextField.TYPE_STORED);

    static {
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_NOT_STORED.setStoreTermVectors(true);
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_NOT_STORED.setStoreTermVectorOffsets(true);
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_NOT_STORED.setStoreTermVectorPositions(true);
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_STORED.setStoreTermVectors(true);
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_STORED.setStoreTermVectorOffsets(true);
        TYPE_TEXT_WITH_POSITIONS_OFFSETS_STORED.setStoreTermVectorPositions(true);
    }

}

/******************************************************************************************
 * Copyright (c) 2024 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong, xszhong@bit.edu.cn, zhongxiaoshi@gmail.com
 * 				  Chenyu Jin, cyjin@bit.edu.cn
 * ****************************************************************************************/

package bit.cs.struct;

import java.util.List;

public class Paragraph {
    private String text;
    private List<TaggedToken> taggedTokenList;

    public Paragraph(String text, List<TaggedToken> taggedTokenList) {
        this.text = text;
        this.taggedTokenList = taggedTokenList;
    }

    public List<TaggedToken> getTaggedTokenList() {
        return taggedTokenList;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}

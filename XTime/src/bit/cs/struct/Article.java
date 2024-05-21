/******************************************************************************************
 * Copyright (c) 2017-2024 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong, xszhong@bit.edu.cn, zhongxiaoshi@gmail.com
 * 				  Chenyu Jin, cyjin@bit.edu.cn
 * ****************************************************************************************/

package bit.cs.struct;

import java.util.List;

public class Article {
    private String text;
    private List<Paragraph> paragraphs;
    private List<TaggedToken> taggedTokenList;

    public Article(String text, List<Paragraph> paragraphs, List<TaggedToken> taggedTokenList) {
        this.text = text;
        this.paragraphs = paragraphs;
        this.taggedTokenList = taggedTokenList;
    }

    public Article(String text, List<TaggedToken> taggedTokenList) {
        this.text = text;
        this.taggedTokenList = taggedTokenList;
    }

    public String getText() {
        return text;
    }

    public List<Paragraph> getParagraphs() {
        return paragraphs;
    }

    public List<TaggedToken> getTaggedTokenList() {
        return taggedTokenList;
    }
}

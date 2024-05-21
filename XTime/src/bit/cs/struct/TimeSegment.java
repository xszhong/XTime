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

import bit.cs.tool.XTimeRegex.TimexType;

public class TimeSegment {
    private int beginTokenPosition;
    private int endTokenPosition;

    private int timeTokenPosition;
    private TaggedToken timeToken;
    private TimexType timexType;
    private List<TaggedToken> segmentTokenList;
    private boolean isDependent;
    private MetaInfo metaInfo;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (TaggedToken taggedToken : segmentTokenList) {
            sb.append(taggedToken.getToken());
            sb.append(" ");
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public TimeSegment(int timeTokenPosition) {
        this.timeTokenPosition = timeTokenPosition;
    }

    public TimeSegment(int timeTokenPosition, int beginTokenPosition, int endTokenPosition) {
        this(timeTokenPosition);
        this.beginTokenPosition = beginTokenPosition;
        this.endTokenPosition = endTokenPosition;
    }
    
    public TimeSegment(TimexType timexType, List<TaggedToken> segmentTokenList, boolean isDependent, TaggedToken timeToken, int timeTokenPosition, int beginTokenPosition, int endTokenPosition) {
        this.beginTokenPosition = beginTokenPosition;
        this.endTokenPosition = endTokenPosition;
        this.timeTokenPosition = timeTokenPosition;
        this.timeToken = timeToken;
        this.timexType = timexType;
        this.segmentTokenList = segmentTokenList;
        this.isDependent = isDependent;
    }
//
//    public TimeSegment(MetaInfo metaInfo, TimexType timexType, List<TaggedToken> segmentTokenList, boolean isDependent, TaggedToken timeToken, int timeTokenPosition, int beginTokenPosition, int endTokenPosition) {
//        this.beginTokenPosition = beginTokenPosition;
//        this.endTokenPosition = endTokenPosition;
//        this.timeTokenPosition = timeTokenPosition;
//        this.timeToken = timeToken;
//        this.timexType = timexType;
//        this.segmentTokenList = segmentTokenList;
//        this.isDependent = isDependent;
//        this.metaInfo = metaInfo;
//    }

    public List<TaggedToken> getSegmentTokenList() {
        return segmentTokenList;
    }

    public TimeSegment setSegmentTokenList(List<TaggedToken> segmentTokenList) {
        this.segmentTokenList = segmentTokenList;
        return this;
    }

    public TimexType getTimexType() {
        return timexType;
    }

    public TimeSegment setTimexType(TimexType timexType) {
        this.timexType = timexType;
        return this;
    }

    public boolean isDependent() {
        return isDependent;
    }

    public TimeSegment setDependent(boolean dependent) {
        isDependent = dependent;
        return this;
    }

    public TaggedToken getTimeToken() {
        return timeToken;
    }

    public TimeSegment setTimeToken(TaggedToken timeToken) {
        this.timeToken = timeToken;
        return this;
    }

    public int getTimeTokenPosition() {
        return timeTokenPosition;
    }

    public int getBeginTokenPosition() {
        return beginTokenPosition;
    }

    public void setBeginTokenPosition(int beginTokenPosition) {
        this.beginTokenPosition = beginTokenPosition;
    }

    public int getEndTokenPosition() {
        return endTokenPosition;
    }

    public void setEndTokenPosition(int endTokenPosition) {
        this.endTokenPosition = endTokenPosition;
    }
}

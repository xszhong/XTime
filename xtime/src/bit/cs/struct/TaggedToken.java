/******************************************************************************************
 * Copyright (c) 2017 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong (xszhong@bit.edu.cn, zhongxiaoshi@gmail.com)
 * ****************************************************************************************/

package bit.cs.struct;

import java.util.Set;

import bit.cs.tool.InduceTokenTriples;
import bit.cs.tool.XTimeRegex.TokenType;

public class TaggedToken extends BasicTaggedToken {
	private Set<TokenType> tokenTypeSet;
	private String value;
	
	public TaggedToken(String token, String tag){
		super(token, tag);
	}
	
	public TaggedToken(String token, String lemma, String tag, int tokenPosition, int beginCharPosition, int endCharPosition){
		super(token, lemma, tag, tokenPosition, beginCharPosition, endCharPosition);
	}

	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Set<TokenType> getTokenTypeSet(){
		return tokenTypeSet;
	}
	public void setTokenTypeSet(Set<TokenType> tokenTypeSet){
		this.tokenTypeSet = tokenTypeSet;
	}
	public void addTokenType(TokenType tokenType){
		tokenTypeSet.add(tokenType);
	}
	public void removeTokenType(TokenType tokenType){
		this.tokenTypeSet.remove(tokenType);
	}
	public void removeTokenType(){
		this.tokenTypeSet.clear();
	}
	
	public boolean isYear(){
		return InduceTokenTriples.isYear(tokenTypeSet);
	}
	
	public boolean isYearYear(){
		return InduceTokenTriples.isYearYear(tokenTypeSet);
	}
	
	public boolean isSeason(){
		return InduceTokenTriples.isSeason(tokenTypeSet);
	}
	
	public boolean isMonth(){
		return InduceTokenTriples.isMonth(tokenTypeSet);
	}
	
	public boolean isMonthMonth(){
		return InduceTokenTriples.isMonthMonth(tokenTypeSet);
	}
	
	public boolean isYearMonth(){
		return InduceTokenTriples.isYearMonth(tokenTypeSet);
	}
	
	public boolean isWeek(){
		return InduceTokenTriples.isWeek(tokenTypeSet);
	}
	
	public boolean isWeekWeek(){
		return InduceTokenTriples.isWeekWeek(tokenTypeSet);
	}
	
	public boolean isDate(){
		return InduceTokenTriples.isDate(tokenTypeSet);
	}
	
	public boolean isTime(){
		return InduceTokenTriples.isTime(tokenTypeSet);
	}
	
	public boolean isTimeTime(){
		return InduceTokenTriples.isTimeTime(tokenTypeSet);
	}
	
	public boolean isHalfDay(){
		return InduceTokenTriples.isHalfDay(tokenTypeSet);
	}
	
	public boolean isHalfDayHalfDay(){
		return InduceTokenTriples.isHalfDayHalfDay(tokenTypeSet);
	}
	
	public boolean isTimeZone(){
		return InduceTokenTriples.isTimeZone(tokenTypeSet);
	}
	
	public boolean isEra(){
		return InduceTokenTriples.isEra(tokenTypeSet);
	}
	
	public boolean isContinuousTimeUnit(){
		return InduceTokenTriples.isContinuousTimeUnit(tokenTypeSet);
	}

	public boolean isDiscreteTimeUnit(){
		return InduceTokenTriples.isDiscreteTimeUnit(tokenTypeSet);
	}

	public boolean isOrdinalTimeUnit(){
		return InduceTokenTriples.isOrdinalTimeUnit(tokenTypeSet);
	}

	public boolean isNumberTimeUnit(){
		return InduceTokenTriples.isNumberTimeUnit(tokenTypeSet);
	}
	
	public boolean isContinuousPeriod() {
		return InduceTokenTriples.isContinuousPeriod(tokenTypeSet);
	}
	
	public boolean isDiscretePeriod() {
		return InduceTokenTriples.isDiscretePeriod(tokenTypeSet);
	}
	
	public boolean isDuration(){
		return InduceTokenTriples.isDuration(tokenTypeSet);
	}
	
	public boolean isDurationDuration(){
		return InduceTokenTriples.isDurationDuration(tokenTypeSet);
	}
	
	public boolean isDayTime(){
		return InduceTokenTriples.isDayTime(tokenTypeSet);
	}
	
	public boolean isTimeline(){
		return InduceTokenTriples.isTimeline(tokenTypeSet);
	}
	
	public boolean isHoliday(){
		return InduceTokenTriples.isHoliday(tokenTypeSet);
	}

	public boolean isToday(){
		return InduceTokenTriples.isToday(tokenTypeSet);
	}

	public boolean isYesterday(){
		return InduceTokenTriples.isYesterday(tokenTypeSet);
	}

	public boolean isTomorrow(){
		return InduceTokenTriples.isTomorrow(tokenTypeSet);
	}

	public boolean isPeriodical(){
		return InduceTokenTriples.isPeriodical(tokenTypeSet);
	}
	
	public boolean isDecade(){
		return InduceTokenTriples.isDecade(tokenTypeSet);
	}
	
	public boolean isNumber() {
		return InduceTokenTriples.isNumber(tokenTypeSet);
	}
	
	public boolean isNumberNumber() {
		return InduceTokenTriples.isNumberNumber(tokenTypeSet);
	}
	
	public boolean isOrdinal() {
		return InduceTokenTriples.isOrdinal(tokenTypeSet);
	}
	
	public boolean isOrdinalOrdinal() {
		return InduceTokenTriples.isOrdinalOrdinal(tokenTypeSet);
	}
	
	public boolean isNumeral(){
		return InduceTokenTriples.isNumeral(tokenTypeSet);
	}
	
	public boolean isNumeralNumeral(){
		return InduceTokenTriples.isNumeralNumeral(tokenTypeSet);
	}
	
	public boolean isInArticle(){
		return InduceTokenTriples.isInArticle(tokenTypeSet);
	}
	
	public boolean isPrefixCommon(){
		return InduceTokenTriples.isPrefixCommon(tokenTypeSet);
	}

	public boolean isPrefixQuantitative(){
		return InduceTokenTriples.isPrefixQuantitative(tokenTypeSet);
	}
	
	public boolean isPrefixOperate(){
		return InduceTokenTriples.isPrefixOperate(tokenTypeSet);
	}
	
	public boolean isPrefixFrequent() {
		return InduceTokenTriples.isPrefixFrequent(tokenTypeSet);
	}

	public boolean isSuffixOperate(){
		return InduceTokenTriples.isSuffixOperate(tokenTypeSet);
	}

	public boolean isSuffixDura(){
		return InduceTokenTriples.isSuffixDura(tokenTypeSet);
	}

	public boolean isLinkage(){
		return InduceTokenTriples.isLinkage(tokenTypeSet);
	}
	
	public boolean isComma(){
		return InduceTokenTriples.isComma(tokenTypeSet);
	}
	
	public boolean isTimeToken(){
		return InduceTokenTriples.isTimeToken(tokenTypeSet);
	}
	
	public boolean isPrefixMod() {
		return InduceTokenTriples.isPrefixMod(tokenTypeSet);
	}
	
	public boolean isSuffixMod() {
		return InduceTokenTriples.isSuffixMod(tokenTypeSet);
	}
	
	public boolean sameTokenTypeWith(TaggedToken taggedToken){
		Set<TokenType> temTokenTypeSet = taggedToken.getTokenTypeSet();
		if(temTokenTypeSet.isEmpty() || tokenTypeSet.isEmpty() || temTokenTypeSet.size() != tokenTypeSet.size())
			return false;
		for(TokenType tokenType : tokenTypeSet) {
			for(TokenType temTokenType : temTokenTypeSet) {
				if(tokenType.equals(temTokenType)) {
					return true;
				}
			}
		}
		return false;
	}
}

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

import java.util.Set;

import bit.cs.tool.InduceTokenTypeValue;
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
		return InduceTokenTypeValue.isYear(tokenTypeSet);
	}
	
	public boolean isYearYear(){
		return InduceTokenTypeValue.isYearYear(tokenTypeSet);
	}
	
	public boolean isSeason(){
		return InduceTokenTypeValue.isSeason(tokenTypeSet);
	}
	
	public boolean isMonth(){
		return InduceTokenTypeValue.isMonth(tokenTypeSet);
	}
	
	public boolean isMonthMonth(){
		return InduceTokenTypeValue.isMonthMonth(tokenTypeSet);
	}
	
	public boolean isYearMonth(){
		return InduceTokenTypeValue.isYearMonth(tokenTypeSet);
	}
	
	public boolean isWeek(){
		return InduceTokenTypeValue.isWeek(tokenTypeSet);
	}
	
	public boolean isWeekWeek(){
		return InduceTokenTypeValue.isWeekWeek(tokenTypeSet);
	}
	
	public boolean isDate(){
		return InduceTokenTypeValue.isDate(tokenTypeSet);
	}
	
	public boolean isTime(){
		return InduceTokenTypeValue.isTime(tokenTypeSet);
	}
	
	public boolean isTimeTime(){
		return InduceTokenTypeValue.isTimeTime(tokenTypeSet);
	}
	
	public boolean isHalfDay(){
		return InduceTokenTypeValue.isHalfDay(tokenTypeSet);
	}
	
	public boolean isHalfDayHalfDay(){
		return InduceTokenTypeValue.isHalfDayHalfDay(tokenTypeSet);
	}
	
	public boolean isTimeZone(){
		return InduceTokenTypeValue.isTimeZone(tokenTypeSet);
	}
	
	public boolean isEra(){
		return InduceTokenTypeValue.isEra(tokenTypeSet);
	}
	
	public boolean isContinuousTimeUnit(){
		return InduceTokenTypeValue.isContinuousTimeUnit(tokenTypeSet);
	}

	public boolean isDiscreteTimeUnit(){
		return InduceTokenTypeValue.isDiscreteTimeUnit(tokenTypeSet);
	}

	public boolean isOrdinalTimeUnit(){
		return InduceTokenTypeValue.isOrdinalTimeUnit(tokenTypeSet);
	}

	public boolean isNumberTimeUnit(){
		return InduceTokenTypeValue.isNumberTimeUnit(tokenTypeSet);
	}
	
	public boolean isContinuousPeriod() {
		return InduceTokenTypeValue.isContinuousPeriod(tokenTypeSet);
	}
	
	public boolean isDiscretePeriod() {
		return InduceTokenTypeValue.isDiscretePeriod(tokenTypeSet);
	}
	
	public boolean isDuration(){
		return InduceTokenTypeValue.isDuration(tokenTypeSet);
	}
	
	public boolean isDurationDuration(){
		return InduceTokenTypeValue.isDurationDuration(tokenTypeSet);
	}
	
	public boolean isDayTime(){
		return InduceTokenTypeValue.isDayTime(tokenTypeSet);
	}
	
	public boolean isTimeline(){
		return InduceTokenTypeValue.isTimeline(tokenTypeSet);
	}
	
	public boolean isHoliday(){
		return InduceTokenTypeValue.isHoliday(tokenTypeSet);
	}

	public boolean isToday(){
		return InduceTokenTypeValue.isToday(tokenTypeSet);
	}

	public boolean isYesterday(){
		return InduceTokenTypeValue.isYesterday(tokenTypeSet);
	}

	@Override
	public String toString() {
		return this.getToken();
	}

	public boolean isTomorrow(){
		return InduceTokenTypeValue.isTomorrow(tokenTypeSet);
	}

	public boolean isPeriodical(){
		return InduceTokenTypeValue.isPeriodical(tokenTypeSet);
	}
	
	public boolean isDecade(){
		return InduceTokenTypeValue.isDecade(tokenTypeSet);
	}
	
	public boolean isNumber() {
		return InduceTokenTypeValue.isNumber(tokenTypeSet);
	}
	
	public boolean isNumberNumber() {
		return InduceTokenTypeValue.isNumberNumber(tokenTypeSet);
	}
	
	public boolean isOrdinal() {
		return InduceTokenTypeValue.isOrdinal(tokenTypeSet);
	}
	
	public boolean isOrdinalOrdinal() {
		return InduceTokenTypeValue.isOrdinalOrdinal(tokenTypeSet);
	}
	
	public boolean isNumeral(){
		return InduceTokenTypeValue.isNumeral(tokenTypeSet);
	}
	
	public boolean isNumeralNumeral(){
		return InduceTokenTypeValue.isNumeralNumeral(tokenTypeSet);
	}
	
	public boolean isInArticle(){
		return InduceTokenTypeValue.isInArticle(tokenTypeSet);
	}
	
	public boolean isPrefixCommon(){
		return InduceTokenTypeValue.isPrefixCommon(tokenTypeSet);
	}

	public boolean isPrefixQuantitative(){
		return InduceTokenTypeValue.isPrefixQuantitative(tokenTypeSet);
	}
	
	public boolean isPrefixOperate(){
		return InduceTokenTypeValue.isPrefixOperate(tokenTypeSet);
	}
	
	public boolean isPrefixFrequent() {
		return InduceTokenTypeValue.isPrefixFrequent(tokenTypeSet);
	}

	public boolean isSuffixOperate(){
		return InduceTokenTypeValue.isSuffixOperate(tokenTypeSet);
	}

	public boolean isSuffixDura(){
		return InduceTokenTypeValue.isSuffixDura(tokenTypeSet);
	}

	public boolean isLinkage(){
		return InduceTokenTypeValue.isLinkage(tokenTypeSet);
	}
	
	public boolean isComma(){
		return InduceTokenTypeValue.isComma(tokenTypeSet);
	}
	
	public boolean isTimeToken(){
		return InduceTokenTypeValue.isTimeToken(tokenTypeSet);
	}
	
	public boolean isPrefixMod() {
		return InduceTokenTypeValue.isPrefixMod(tokenTypeSet);
	}
	
	public boolean isSuffixMod() {
		return InduceTokenTypeValue.isSuffixMod(tokenTypeSet);
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

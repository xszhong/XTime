/******************************************************************************************
 * Copyright (c) 2017-2024 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong, xszhong@bit.edu.cn, zhongxiaoshi@gmail.com
 * 				  Chenyu Jin, cyjin@bit.edu.cn
 * ****************************************************************************************/

package bit.cs.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bit.cs.struct.BasicTaggedToken;
import bit.cs.struct.TaggedToken;
import bit.cs.tool.XTimeRegex;
import bit.cs.tool.XTimeRegex.TokenType;
import bit.cs.util.IOProcess;

public class InduceTokenTypeValue {
	
	public static Map<TokenType, Map<String, String>> tripleMap;
	public static String inputTripleFile = "resources/regex/Triple.txt";
	
	static {
		tripleMap = loadTripleMap(inputTripleFile);
		//System.out.println(tripleMap.size());
	}
	
	public static String getTokenValue(BasicTaggedToken taggedToken) {

		return null;
	}

	public static String getTokenValue(String token, Set<TokenType> tokenTypeSet) {
		for (TokenType tokenType:tokenTypeSet) {
			if (tripleMap.containsKey(tokenType)) {
				Map<String, String> regexMap = tripleMap.get(tokenType);
				for (String regex : regexMap.keySet()) {
					String value = regexMap.get(regex);
					Pattern regexPattern=Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
					Matcher regexMatcher=regexPattern.matcher(token);
					if (regexMatcher.matches())
						return value;
				}
			}
		}
		return null;
	}

	public static String getTokenValue(String token, TokenType tokenType) {
		if(! tripleMap.containsKey(tokenType))
			return null;
		Map<String, String> regexMap = tripleMap.get(tokenType);
		for(String regex : regexMap.keySet()) {
			String value = regexMap.get(regex);
			if(Pattern.matches(regex, token))
				return value;
		}
		return null;
		
	}

	public static Set<TokenType> getTokenType(BasicTaggedToken taggedToken){
		return getTokenType(taggedToken.getToken(), taggedToken.getTag());
	}
	
	public static Set<TokenType> getTokenType(TaggedToken taggedToken){
		return getTokenType(taggedToken.getToken(), taggedToken.getTag());
	}
	
	public static Set<TokenType> getTokenType(String token, String posTag){
		Set<TokenType> tokenTypeSet = new HashSet<TokenType>();
		
		Matcher mYear1 = XTimeRegex.YEAR_PATTERN_1.matcher(token);
		Matcher mYear2 = XTimeRegex.YEAR_PATTERN_2.matcher(token);
		Matcher mMidYear = XTimeRegex.YEAR_MID_PATTERN.matcher(token);
		Matcher mEraYear = XTimeRegex.ERA_YEAR_PATTERN.matcher(token);
		if(mYear1.matches() || mYear2.matches() || mMidYear.matches() || mEraYear.matches())
			tokenTypeSet.add(TokenType.YEAR);
			
		Matcher mYearYear = XTimeRegex.YEAR_YEAR_PATTERN.matcher(token);
		if(mYearYear.matches())
			tokenTypeSet.add(TokenType.YEAR_YEAR);
			
		Matcher mSeason = XTimeRegex.SEASON_PATTERN.matcher(token);
		Matcher mSeasonMid = XTimeRegex.SEASON_MID_PATTERN.matcher(token);
		if(mSeason.matches() && posTag.startsWith("NN") || mSeasonMid.matches())
			tokenTypeSet.add(TokenType.SEASON);
		
		Matcher mMonth = XTimeRegex.MONTH_PATTERN.matcher(token);
		Matcher mMidMonth = XTimeRegex.MONTH_MID_PATTERN.matcher(token);
		if((mMonth.matches() && posTag.startsWith("NN")) || mMidMonth.matches())
			tokenTypeSet.add(TokenType.MONTH);
		Matcher mMonthMonth = XTimeRegex.MONTH_MONTH_PATTERN.matcher(token);
		if(mMonthMonth.matches())
			tokenTypeSet.add(TokenType.MONTH_MONTH);
		
		Matcher mYearMonth1 = XTimeRegex.YEAR_MONTH_PATTERN_1.matcher(token);
		Matcher mYearMonth2 = XTimeRegex.YEAR_MONTH_PATTERN_2.matcher(token);
		if(mYearMonth1.matches() || mYearMonth2.matches())
			tokenTypeSet.add(TokenType.YEAR_MONTH);
		
		Matcher mWeek = XTimeRegex.WEEK_PATTERN.matcher(token);
		if(mWeek.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.WEEK);
		
		Matcher mWeekWeek = XTimeRegex.WEEK_WEEK_PATTERN.matcher(token);
		if(mWeekWeek.matches())
			tokenTypeSet.add(TokenType.WEEK_WEEK);
		
		Matcher mDate1 = XTimeRegex.DATE_PATTERN_1.matcher(token);
		Matcher mDate2 = XTimeRegex.DATE_PATTERN_2.matcher(token);
		Matcher mDate3 = XTimeRegex.DATE_PATTERN_3.matcher(token);
		if(mDate1.matches() || mDate2.matches() || mDate3.matches())
			tokenTypeSet.add(TokenType.DATE);
		
		Matcher mTime1 = XTimeRegex.TIME_PATTERN_1.matcher(token);
		Matcher mTime2 = XTimeRegex.TIME_PATTERN_2.matcher(token);
		if(mTime1.matches() || mTime2.matches())
			tokenTypeSet.add(TokenType.TIME);
		Matcher mTimeTime = XTimeRegex.TIME_TIME_PATTERN.matcher(token);
		if(mTimeTime.matches())
			tokenTypeSet.add(TokenType.TIME_TIME);
		
		Matcher mHalfDay1 = XTimeRegex.HALFDAY_PATTERN_1.matcher(token);
		Matcher mHalfDay2 = XTimeRegex.HALFDAY_PATTERN_2.matcher(token);
		if(mHalfDay1.matches() || mHalfDay2.matches())
			tokenTypeSet.add(TokenType.HALFDAY);
		Matcher mHalfDayHalfDay = XTimeRegex.HALFDAY_HALFDAY_PATTERN.matcher(token);
		if(mHalfDayHalfDay.matches())
			tokenTypeSet.add(TokenType.HALFDAY_HALFDAY);
		
		Matcher mTimeZone = XTimeRegex.TIME_ZONE_PATTERN.matcher(token);
		if(mTimeZone.matches())
			tokenTypeSet.add(TokenType.TIME_ZONE);
		
		Matcher mEra = XTimeRegex.ERA_PATTERN.matcher(token);
		if(mEra.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.ERA);
		
		Matcher mContinuousTimeUnit = XTimeRegex.CONTINUOUS_TIMEUNIT_PATTERN.matcher(token);
		if(mContinuousTimeUnit.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.CONTINUOUS_TIMEUNIT);

		Matcher mDisceteTimeUnit = XTimeRegex.DISCRETE_TIMEUNIT_PATTERN.matcher(token);
		if(mDisceteTimeUnit.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.DISCRETE_TIMEUNIT);

		Matcher mContinuousPeriod = XTimeRegex.CONTINUOUS_PERIOD_PATTERN.matcher(token);
		if(mContinuousPeriod.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.CONTINUOUS_PERIOD);
		
		Matcher mDiscretePeriod = XTimeRegex.DISCRETE_PERIOD_PATTERN.matcher(token);
		if(mDiscretePeriod.matches() && posTag.startsWith("NN"))
			tokenTypeSet.add(TokenType.DISCRETE_PERIOD);
		
		Matcher mDuration = XTimeRegex.DURATION_PATTERN.matcher(token);
		if(mDuration.matches() && (posTag.startsWith("NN") || posTag.equals("JJ") || posTag.equals("CD")))
			tokenTypeSet.add(TokenType.DURATION);
		
		Matcher mDurationDuration1 = XTimeRegex.DURATION_DURATION_PATTERN_1.matcher(token);
		Matcher mDurationDuration2 = XTimeRegex.DURATION_DURATION_PATTERN_2.matcher(token);
		if(! mDuration.matches() && ( mDurationDuration1.matches() || mDurationDuration2.matches()))
			tokenTypeSet.add(TokenType.DURATION_DURATION);
		
		Matcher mOrdinalTimeUnit = XTimeRegex.ORDINAL_TIMEUNIT_PATTERN.matcher(token);
		if(mOrdinalTimeUnit.matches()) {
			tokenTypeSet.add(TokenType.ORDINAL_TIMEUNIT);
		}

		Matcher mNumberTimeUnit = XTimeRegex.NUMBER_TIMEUNIT_PATTERN.matcher(token);
		if(mNumberTimeUnit.matches()) {
			tokenTypeSet.add(TokenType.NUMBER_TIMEUNIT);
		}
		
		Matcher mDayTime = XTimeRegex.DAY_TIME_PATTERN.matcher(token);
		Matcher mDayTimeMid = XTimeRegex.DAY_TIME_MID_PATTERN.matcher(token);
		if(mDayTime.matches() && (posTag.startsWith("NN") || posTag.equals("RB") || posTag.equals("JJ")) || mDayTimeMid.matches())
			tokenTypeSet.add(TokenType.DAY_TIME);
		
		Matcher mTimeline = XTimeRegex.TIMELINE_PATTERN.matcher(token);
		if(mTimeline.matches() && (posTag.startsWith("NN") || posTag.equals("RB")||posTag.equals("JJ"))) //todo
			tokenTypeSet.add(TokenType.TIMELINE);
		
		Matcher mHoliday = XTimeRegex.HOLIDAY_PATTERN.matcher(token);
		if(mHoliday.matches())
			tokenTypeSet.add(TokenType.HOLIDAY);

		Matcher mToday = XTimeRegex.TODAY_PATTERN.matcher(token);
		if(mToday.matches())
			tokenTypeSet.add(TokenType.TODAY);

		Matcher mYesterday = XTimeRegex.YESTERDAY_PATTERN.matcher(token);
		if(mYesterday.matches())
			tokenTypeSet.add(TokenType.YESTERDAY);

		Matcher mTomorrow = XTimeRegex.TOMORROW_PATTERN.matcher(token);
		if(mTomorrow.matches())
			tokenTypeSet.add(TokenType.TOMORROW);
		
		Matcher mPeriodical = XTimeRegex.PERIODICAL_PATTERN.matcher(token);
		if(mPeriodical.matches() && (posTag.startsWith("NN") || posTag.equals("RB") || posTag.equals("JJ")))
			tokenTypeSet.add(TokenType.PERIODICAL);
		
		Matcher mDecade = XTimeRegex.DECADE_PATTERN.matcher(token);
		Matcher mDecadeMid = XTimeRegex.DECADE_MID_PATTERN.matcher(token);
		if(mDecade.matches() && posTag.startsWith("NN") || mDecadeMid.matches())
			tokenTypeSet.add(TokenType.DECADE);
		
		Matcher mDigit1 = XTimeRegex.DIGIT_PATTERN_1.matcher(token);
		Matcher mDigit2 = XTimeRegex.DIGIT_PATTERN_2.matcher(token);
		Matcher mNumber1 = XTimeRegex.NUMBER_PATTERN_1.matcher(token);
		Matcher mNumber2 = XTimeRegex.NUMBER_PATTERN_2.matcher(token);
		if(mYear1.matches() || mYear2.matches())
			;
		else if(mDigit1.matches() || mDigit2.matches() || mNumber1.matches() || mNumber2.matches()) {
			tokenTypeSet.add(TokenType.NUMBER);
			tokenTypeSet.add(TokenType.NUMERAL);
		}
		
		Matcher mOrdinal1 = XTimeRegex.ORDINAL_PATTERN_1.matcher(token);
		Matcher mOrdinal2 = XTimeRegex.ORDINAL_PATTERN_2.matcher(token);
		if((mOrdinal1.matches() || mOrdinal2.matches()) && (posTag.equals("JJ") || posTag.equals("CD") || posTag.equals("RB"))){
			tokenTypeSet.add(TokenType.ORDINAL);
			tokenTypeSet.add(TokenType.NUMERAL);
		}
		
		Matcher mDigitDigit = XTimeRegex.DIGIT_DIGIT_PATTERN.matcher(token);
		Matcher mNumberNumber = XTimeRegex.NUMBER_NUMBER_PATTERN.matcher(token);
		
		if(! mYearYear.matches() && ! mNumber2.matches() && (mDigitDigit.matches() || mNumberNumber.matches())) {
			tokenTypeSet.add(TokenType.NUMBER_NUMBER);
			tokenTypeSet.add(TokenType.NUMERAL_NUMERAL);
		}
		
		Matcher mOrdinalOrdinal = XTimeRegex.ORDINAL_ORDINAL_PATTERN.matcher(token);
		if(mOrdinalOrdinal.matches()) {
			tokenTypeSet.add(TokenType.ORDINAL_ORDINAL);
			tokenTypeSet.add(TokenType.NUMBER_NUMBER);
		}
			
		
		Matcher mInArticle = XTimeRegex.INARTICLE_PATTERN.matcher(token);
		if(mInArticle.matches())
			tokenTypeSet.add(TokenType.INARTICLE);
		
		Matcher mPrefixCommon = XTimeRegex.PREFIX_COMMON_PATTERN.matcher(token);
		if(tokenTypeSet.isEmpty() && mPrefixCommon.matches())
			tokenTypeSet.add(TokenType.PREFIX_COMMON);
		
		Matcher mPrefixQuantitative = XTimeRegex.PREFIX_QUANTITATIVE_PATTERN.matcher(token);
		if(mPrefixQuantitative.matches())
			tokenTypeSet.add(TokenType.PREFIX_QUANTITATIVE);

		Matcher mPrefixOperate = XTimeRegex.PREFIX_OPERATE_PATTERN.matcher(token);
		if(mPrefixOperate.matches())
			tokenTypeSet.add(TokenType.PREFIX_OPERATE);
		
		Matcher mPrefixFrequent = XTimeRegex.PREFIX_FREQUENT_PATTERN.matcher(token);
		if(mPrefixFrequent.matches())
			tokenTypeSet.add(TokenType.PREFIX_FREQUENT);

		Matcher mSuffixOperate = XTimeRegex.SUFFIX_OPERATE_PATTERN.matcher(token);
		if(mSuffixOperate.matches())
			tokenTypeSet.add(TokenType.SUFFIX_OPERATE);

		Matcher mSuffixDura = XTimeRegex.SUFFIX_DURA_PATTERN.matcher(token);
		if(mSuffixDura.matches())
			tokenTypeSet.add(TokenType.SUFFIX_DURA);
		
		Matcher mLinkage = XTimeRegex.LINKAGE_PATTERN.matcher(token);
		if(mLinkage.matches())
			tokenTypeSet.add(TokenType.LINKAGE);
		
		Matcher mComma = XTimeRegex.COMMA_PATTERN.matcher(token);
		if(mComma.matches())
			tokenTypeSet.add(TokenType.COMMA);
		
		if(tokenTypeSet.size() > 1){
			//System.out.println(token + "\t" + posTag + "\ttokenTypeSet size: " + tokenTypeSet.size() + "\t" + tokenTypeSet.toString());
		}

		return tokenTypeSet;
	}
	
	/***************************************************************/
	public static boolean isYear(TokenType tokenType){
		if(tokenType.equals(TokenType.YEAR) || tokenType.equals(TokenType.YEAR_YEAR))
			return true;
		return false;
	}
	public static boolean isYear(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isYear(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isYear(TaggedToken taggedToken){
		return isYear(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isYearYear(TokenType tokenType){
		if(tokenType.equals(TokenType.YEAR_YEAR))
			return true;
		return false;
	}
	public static boolean isYearYear(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isYearYear(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isYearYear(TaggedToken taggedToken){
		return isYearYear(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isSeason(TokenType tokenType){
		if(tokenType.equals(TokenType.SEASON))
			return true;
		return false;
	}
	public static boolean isSeason(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isSeason(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isSeason(TaggedToken taggedToken){
		return isSeason(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isMonth(TokenType tokenType){
		if(tokenType.equals(TokenType.MONTH) || tokenType.equals(TokenType.MONTH_MONTH) || tokenType.equals(TokenType.YEAR_MONTH))
			return true;
		return false;
	}
	public static boolean isMonth(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isMonth(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isMonth(TaggedToken taggedToken){
		return isMonth(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isMonthMonth(TokenType tokenType){
		if(tokenType.equals(TokenType.MONTH_MONTH))
			return true;
		return false;
	}
	public static boolean isMonthMonth(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isMonthMonth(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isMonthMonth(TaggedToken taggedToken){
		return isMonthMonth(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isYearMonth(TokenType tokenType){
		if(tokenType.equals(TokenType.YEAR_MONTH))
			return true;
		return false;
	}
	public static boolean isYearMonth(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isYearMonth(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isYearMonth(TaggedToken taggedToken){
		return isYearMonth(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isWeek(TokenType tokenType){
		if(tokenType.equals(TokenType.WEEK) || tokenType.equals(TokenType.WEEK_WEEK))
			return true;
		return false;
	}
	public static boolean isWeek(Set<TokenType> tokenTypeSet){
		if(tokenTypeSet.isEmpty())
			return false;
		TokenType tokenType = tokenTypeSet.iterator().next();
		return isWeek(tokenType);
	}
	public static boolean isWeek(TaggedToken taggedToken){
		return isWeek(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isWeekWeek(TokenType tokenType){
		if(tokenType.equals(TokenType.WEEK_WEEK))
			return true;
		return false;
	}
	public static boolean isWeekWeek(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isWeekWeek(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isWeekWeek(TaggedToken taggedToken){
		return isWeekWeek(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDate(TokenType tokenType){
		if(tokenType.equals(TokenType.DATE))
			return true;
		return false;
	}
	public static boolean isDate(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDate(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDate(TaggedToken taggedToken){
		return isDate(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isTime(TokenType tokenType){
		if(tokenType.equals(TokenType.TIME) || tokenType.equals(TokenType.TIME_TIME))
			return true;
		return false;
	}
	public static boolean isTime(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTime(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTime(TaggedToken taggedToken){
		return isTime(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isTimeTime(TokenType tokenType){
		if(tokenType.equals(TokenType.TIME_TIME))
			return true;
		return false;
	}
	public static boolean isTimeTime(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTimeTime(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTimeTime(TaggedToken taggedToken){
		return isTimeTime(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isHalfDay(TokenType tokenType){
		if(tokenType.equals(TokenType.HALFDAY) || tokenType.equals(TokenType.HALFDAY_HALFDAY))
			return true;
		return false;
	}
	public static boolean isHalfDay(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isHalfDay(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isHalfDay(TaggedToken taggedToken){
		return isHalfDay(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isHalfDayHalfDay(TokenType tokenType){
		if(tokenType.equals(TokenType.HALFDAY_HALFDAY))
			return true;
		return false;
	}
	public static boolean isHalfDayHalfDay(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isHalfDayHalfDay(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isHalfDayHalfDay(TaggedToken taggedToken){
		return isHalfDayHalfDay(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isTimeZone(TokenType tokenType){
		if(tokenType.equals(TokenType.TIME_ZONE))
			return true;
		return false;
	}
	public static boolean isTimeZone(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTimeZone(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTimeZone(TaggedToken taggedToken){
		return isTimeZone(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isEra(TokenType tokenType){
		if(tokenType.equals(TokenType.ERA))
			return true;
		return false;
	}
	public static boolean isEra(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isEra(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isEra(TaggedToken taggedToken){
		return isEra(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isContinuousTimeUnit(TokenType tokenType){
		if(tokenType.equals(TokenType.CONTINUOUS_TIMEUNIT))
			return true;
		return false;
	}
	public static boolean isContinuousTimeUnit(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isContinuousTimeUnit(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isContinuousTimeUnit(TaggedToken taggedToken){
		return isContinuousTimeUnit(taggedToken.getTokenTypeSet());
	}

	public static boolean isDiscreteTimeUnit(TokenType tokenType){
		if(tokenType.equals(TokenType.DISCRETE_TIMEUNIT))
			return true;
		return false;
	}
	public static boolean isDiscreteTimeUnit(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDiscreteTimeUnit(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDiscreteTimeUnit(TaggedToken taggedToken){
		return isDiscreteTimeUnit(taggedToken.getTokenTypeSet());
	}

	public static boolean isContinuousPeriod(TokenType tokenType){
		if(tokenType.equals(TokenType.CONTINUOUS_PERIOD))
			return true;
		return false;
	}
	public static boolean isContinuousPeriod(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isContinuousPeriod(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isContinuousPeriod(TaggedToken taggedToken){
		return isContinuousPeriod(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDiscretePeriod(TokenType tokenType){
		if(tokenType.equals(TokenType.DISCRETE_PERIOD))
			return true;
		return false;
	}
	public static boolean isDiscretePeriod(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDiscretePeriod(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDiscretePeriod(TaggedToken taggedToken){
		return isDiscretePeriod(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDuration(TokenType tokenType){
		if(tokenType.equals(TokenType.DURATION) || tokenType.equals(TokenType.DURATION_DURATION))
			return true;
		return false;
	}
	public static boolean isDuration(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDuration(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDuration(TaggedToken taggedToken){
		return isDuration(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDurationDuration(TokenType tokenType){
		if(tokenType.equals(TokenType.DURATION_DURATION))
			return true;
		return false;
	}
	public static boolean isDurationDuration(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDurationDuration(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDurationDuration(TaggedToken taggedToken){
		return isDurationDuration(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isOrdinalTimeUnit(TokenType tokenType){
		if(tokenType.equals(TokenType.ORDINAL_TIMEUNIT))
			return true;
		return false;
	}
	public static boolean isOrdinalTimeUnit(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isOrdinalTimeUnit(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isOrdinalTimeUnit(TaggedToken taggedToken){
		return isOrdinalTimeUnit(taggedToken.getTokenTypeSet());
	}

	public static boolean isNumberTimeUnit(TokenType tokenType){
		if(tokenType.equals(TokenType.NUMBER_TIMEUNIT))
			return true;
		return false;
	}
	public static boolean isNumberTimeUnit(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isNumberTimeUnit(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isNumberTimeUnit(TaggedToken taggedToken){
		return isNumberTimeUnit(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDayTime(TokenType tokenType){
		if(tokenType.equals(TokenType.DAY_TIME))
			return true;
		return false;
	}
	public static boolean isDayTime(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDayTime(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDayTime(TaggedToken taggedToken){
		return isDayTime(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isTimeline(TokenType tokenType){
		if(tokenType.equals(TokenType.TIMELINE))
			return true;
		return false;
	}
	public static boolean isTimeline(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTimeline(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTimeline(TaggedToken taggedToken){
		return isTimeline(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isHoliday(TokenType tokenType){
		if(tokenType.equals(TokenType.HOLIDAY))
			return true;
		return false;
	}
	public static boolean isHoliday(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isHoliday(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isHoliday(TaggedToken taggedToken){
		return isHoliday(taggedToken.getTokenTypeSet());
	}

	public static boolean isToday(TokenType tokenType){
		if(tokenType.equals(TokenType.TODAY))
			return true;
		return false;
	}
	public static boolean isToday(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isToday(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isToday(TaggedToken taggedToken){
		return isToday(taggedToken.getTokenTypeSet());
	}

	public static boolean isYesterday(TokenType tokenType){
		if(tokenType.equals(TokenType.YESTERDAY))
			return true;
		return false;
	}
	public static boolean isYesterday(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isYesterday(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isYesterday(TaggedToken taggedToken){
		return isYesterday(taggedToken.getTokenTypeSet());
	}

	public static boolean isTomorrow(TokenType tokenType){
		if(tokenType.equals(TokenType.TOMORROW))
			return true;
		return false;
	}
	public static boolean isTomorrow(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTomorrow(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTomorrow(TaggedToken taggedToken){
		return isTomorrow(taggedToken.getTokenTypeSet());
	}

	public static boolean isPeriodical(TokenType tokenType){
		if(tokenType.equals(TokenType.PERIODICAL))
			return true;
		return false;
	}
	public static boolean isPeriodical(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isPeriodical(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isPeriodical(TaggedToken taggedToken){
		return isPeriodical(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isDecade(TokenType tokenType){
		if(tokenType.equals(TokenType.DECADE))
			return true;
		return false;
	}
	public static boolean isDecade(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isDecade(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isDecade(TaggedToken taggedToken){
		return isDecade(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isNumber(TokenType tokenType){
		if(tokenType.equals(TokenType.NUMBER) || tokenType.equals(TokenType.NUMBER_NUMBER))
			return true;
		return false;
	}
	public static boolean isNumber(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isNumber(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isNumber(TaggedToken taggedToken){
		return isNumber(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isNumberNumber(TokenType tokenType){
		if(tokenType.equals(TokenType.NUMBER_NUMBER))
			return true;
		return false;
	}
	public static boolean isNumberNumber(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isNumberNumber(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isNumberNumber(TaggedToken taggedToken){
		return isNumberNumber(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isOrdinal(TokenType tokenType){
		if(tokenType.equals(TokenType.ORDINAL) || tokenType.equals(TokenType.ORDINAL_ORDINAL))
			return true;
		return false;
	}
	public static boolean isOrdinal(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isOrdinal(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isOrdinal(TaggedToken taggedToken){
		return isOrdinal(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isOrdinalOrdinal(TokenType tokenType){
		if(tokenType.equals(TokenType.ORDINAL_ORDINAL))
			return true;
		return false;
	}
	public static boolean isOrdinalOrdinal(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isOrdinalOrdinal(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isOrdinalOrdinal(TaggedToken taggedToken){
		return isOrdinalOrdinal(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isNumeral(TokenType tokenType){
		if(tokenType.equals(TokenType.NUMERAL) || tokenType.equals(TokenType.NUMERAL_NUMERAL))
			return true;
		return false;
	}
	public static boolean isNumeral(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isNumeral(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isNumeral(TaggedToken taggedToken){
		return isNumeral(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isNumeralNumeral(TokenType tokenType){
		if(tokenType.equals(TokenType.NUMERAL_NUMERAL))
			return true;
		return false;
	}
	public static boolean isNumeralNumeral(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isNumeralNumeral(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isNumeralNumeral(TaggedToken taggedToken){
		return isNumeralNumeral(taggedToken.getTokenTypeSet());
	}

	public static boolean isInArticle(TokenType tokenType){
		if(tokenType.equals(TokenType.INARTICLE))
			return true;
		return false;
	}
	public static boolean isInArticle(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isInArticle(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isInArticle(TaggedToken taggedToken){
		return isInArticle(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isPrefixCommon(TokenType tokenType){
		if(tokenType.equals(TokenType.PREFIX_COMMON))
			return true;
		return false;
	}
	public static boolean isPrefixCommon(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isPrefixCommon(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isPrefixCommon(TaggedToken taggedToken){
		return isPrefixCommon(taggedToken.getTokenTypeSet());
	}

	public static boolean isPrefixQuantitative(TokenType tokenType){
		if(tokenType.equals(TokenType.PREFIX_QUANTITATIVE))
			return true;
		return false;
	}
	public static boolean isPrefixQuantitative(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isPrefixQuantitative(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isPrefixQuantitative(TaggedToken taggedToken){
		return isPrefixQuantitative(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isPrefixOperate(TokenType tokenType){
		if(tokenType.equals(TokenType.PREFIX_OPERATE))
			return true;
		return false;
	}
	public static boolean isPrefixOperate(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isPrefixOperate(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isPrefixOperate(TaggedToken taggedToken){
		return isPrefixOperate(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isPrefixFrequent(TokenType tokenType){
		if(tokenType.equals(TokenType.PREFIX_FREQUENT))
			return true;
		return false;
	}
	public static boolean isPrefixFrequent(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isPrefixFrequent(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isPrefixFrequent(TaggedToken taggedToken){
		return isPrefixFrequent(taggedToken.getTokenTypeSet());
	}

	public static boolean isSuffixOperate(TokenType tokenType){
		if(tokenType.equals(TokenType.SUFFIX_OPERATE))
			return true;
		return false;
	}
	public static boolean isSuffixOperate(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isSuffixOperate(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isSuffixOperate(TaggedToken taggedToken){
		return isSuffixOperate(taggedToken.getTokenTypeSet());
	}

	public static boolean isSuffixDura(TokenType tokenType){
		if(tokenType.equals(TokenType.SUFFIX_DURA))
			return true;
		return false;
	}
	public static boolean isSuffixDura(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isSuffixDura(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isSuffixDura(TaggedToken taggedToken){
		return isSuffixDura(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isLinkage(TokenType tokenType){
		if(tokenType.equals(TokenType.LINKAGE))
			return true;
		return false;
	}
	public static boolean isLinkage(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isLinkage(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isLinkage(TaggedToken taggedToken){
		return isLinkage(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isComma(TokenType tokenType){
		if(tokenType.equals(TokenType.COMMA))
			return true;
		return false;
	}
	public static boolean isComma(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isComma(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isComma(TaggedToken taggedToken){
		return isComma(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isTimeToken(TokenType tokenType){
		if(isYear(tokenType) || isSeason(tokenType) || isMonth(tokenType) || isWeek(tokenType) || isDate(tokenType) || isTime(tokenType) || isHalfDay(tokenType) || isTimeZone(tokenType) 
			|| isEra(tokenType) || isContinuousTimeUnit(tokenType) || isDiscreteTimeUnit(tokenType) || isContinuousPeriod(tokenType) || isDiscretePeriod(tokenType) || isDuration(tokenType) 
			|| isOrdinalTimeUnit(tokenType) || isDayTime(tokenType) || isTimeline(tokenType) || isHoliday(tokenType) || isPeriodical(tokenType) || isDecade(tokenType))
				return true;
		return false;
	}
	public static boolean isTimeToken(Set<TokenType> tokenTypeSet){
		for(TokenType tokenType : tokenTypeSet) {
			if(isTimeToken(tokenType))
				return true;
		}
		return false;
	}
	public static boolean isTimeToken(TaggedToken taggedToken){
		return isTimeToken(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isPrefixMod(TokenType tokenType) {
		if(isPrefixCommon(tokenType) || isPrefixQuantitative(tokenType) || isPrefixOperate(tokenType) || isPrefixFrequent(tokenType))
			return true;
		return false;
	}
	
	public static boolean isPrefixMod(Set<TokenType> tokenTypeSet) {
		for(TokenType tokenType : tokenTypeSet)
			if(isPrefixMod(tokenType))
				return true;
		return false;
	}
	
	public static boolean isPrefixMod(TaggedToken taggedToken) {
		return isPrefixMod(taggedToken.getTokenTypeSet());
	}
	
	public static boolean isSuffixMod(TokenType tokenType) {
		if(isSuffixOperate(tokenType) || isSuffixDura(tokenType))
			return true;
		return false;
	}
	
	public static boolean isSuffixMod(Set<TokenType> tokenTypeSet) {
		for(TokenType tokenType : tokenTypeSet)
			if(isSuffixMod(tokenType))
				return true;
		return false;
	}
	
	public static boolean isSuffixMod(TaggedToken taggedToken) {
		return isSuffixMod(taggedToken.getTokenTypeSet());
	}
	
	private static Map<TokenType, Map<String, String>> loadTripleMap(String inputTripleFile){
		Map<TokenType, Map<String, String>> tripleMap = new HashMap<TokenType, Map<String, String>>();
		
		BufferedReader br = IOProcess.newReader(inputTripleFile);
		String line;
		try {
			while((line = br.readLine()) != null) {
				String[] items = line.trim().split("\t");
				if(items.length < 2)
					continue;
				
				String regex = items[0];
				String strTokenType = items[1].substring(1, items[1].length() - 1);
				//System.out.println(strTokenType);
				TokenType tokenType = getTokenType(strTokenType);
				String value = null;
				if(items.length >= 3)
					value = items[2];
				
				if(! tripleMap.containsKey(tokenType))
					tripleMap.put(tokenType, new HashMap<String, String>());
				
				tripleMap.get(tokenType).put(regex, value);
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally { }
		
		return tripleMap;
	}
	
	private static TokenType getTokenType(String strTokenType) {
		if(strTokenType.equals("YEAR"))
			return TokenType.YEAR;
		if(strTokenType.equals("MONTH"))
			return TokenType.MONTH;
		else if(strTokenType.equals("DISCRETE_PERIOD"))
			return TokenType.DISCRETE_PERIOD;
		else if(strTokenType.equals("WEEK"))
			return TokenType.WEEK;
		else if(strTokenType.equals("NUMBER"))
			return TokenType.NUMBER;
		else if(strTokenType.equals("ORDINAL"))
			return TokenType.ORDINAL;
		else if(strTokenType.equals("INARTICLE"))
			return TokenType.INARTICLE;
		else if(strTokenType.equals("DECADE"))
			return TokenType.DECADE;
		else if(strTokenType.equals("CONTINUOUS_TIMEUNIT"))
			return TokenType.CONTINUOUS_TIMEUNIT;
		else if(strTokenType.equals("DISCRETE_TIMEUNIT"))
			return TokenType.DISCRETE_TIMEUNIT;
		else if(strTokenType.equals("CONTINUOUS_PERIOD"))
			return TokenType.CONTINUOUS_PERIOD;
		else if(strTokenType.equals("PERIODICAL"))
			return TokenType.PERIODICAL;
		else if(strTokenType.equals("DAY_TIME"))
			return TokenType.DAY_TIME;
		else if(strTokenType.equals("SEASON"))
			return TokenType.SEASON;
		else if(strTokenType.equals("TIMELINE"))
			return TokenType.TIMELINE;
		else if(strTokenType.equals("PREFIX_COMMON"))
			return TokenType.PREFIX_COMMON;
		else if(strTokenType.equals("PREFIX_QUANTITATIVE"))
			return TokenType.PREFIX_QUANTITATIVE;
		else if(strTokenType.equals("PREFIX_OPERATE"))
			return TokenType.PREFIX_OPERATE;
		else if(strTokenType.equals("PREFIX_FREQUENT"))
			return TokenType.PREFIX_FREQUENT;
		else if(strTokenType.equals("HOLIDAY"))
			return TokenType.HOLIDAY;
		else if(strTokenType.equals("SUFFIX_OPERATE"))
			return TokenType.SUFFIX_OPERATE;
		else if(strTokenType.equals("SUFFIX_DURA"))
			return TokenType.SUFFIX_DURA;
		else
			return null;
	}

}

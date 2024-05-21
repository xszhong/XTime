/******************************************************************************************
 * Copyright (c) 2017-2024 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors : Xiaoshi Zhong, xszhong@bit.edu.cn, zhongxiaoshi@gmail.com
 * 				Chenyu Jin, cyjin@bit.edu.cn
 * ****************************************************************************************/

package bit.cs.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bit.cs.struct.*;
import bit.cs.tool.InduceTokenTypeValue;
import bit.cs.tool.ParseTimeML;
import bit.cs.tool.StanfordPipeline;
import bit.cs.tool.XTimeRegex;
import bit.cs.tool.TimeML;
import bit.cs.tool.XTimeRegex.TimexType;
import bit.cs.tool.XTimeRegex.TokenType;
import bit.cs.util.IOProcess;

public class XTime {
    private StanfordPipeline pipeline;
    private ParseTimeML parseTimeML;
    private int globalTid = 1;

    public XTime() {
        pipeline = new StanfordPipeline();
        parseTimeML = new ParseTimeML();
    }

    /**
     * 从文件夹中提取时间表达式
     *
     * @param inputTmlDir
     * @param outputTmlDir
     */
    public void extractTimexFromTmlFolder(String inputTmlDir, String outputTmlDir) throws ParseException {
        //检查路径名
        inputTmlDir = IOProcess.checkPath(inputTmlDir);
        outputTmlDir = IOProcess.checkPath(outputTmlDir);

        //获取目录下所有文件
        File[] inputTmlFiles = IOProcess.getFiles(inputTmlDir);
        if (inputTmlFiles == null)
            IOProcess.findNoPath(inputTmlDir);

        //提取所有文件的timex
        for (File inputTmlFile : inputTmlFiles) {
            String filename = inputTmlFile.getName();
            System.out.println("Parsing " + filename + "...");
            extractTimexFromTmlFile(inputTmlFile, outputTmlDir + filename);
            this.globalTid=1;
        }
    }

    //overload
    public void extractTimexFromTmlFile(String inputTmlFile, String outputTmlFile) throws ParseException {
        extractTimexFromTmlFile(new File(inputTmlFile), outputTmlFile);
    }

    /**
     * 提取文件中的timex
     *
     * @param inputTmlFile
     * @param outputTmlFile
     */
    public void extractTimexFromTmlFile(File inputTmlFile, String outputTmlFile) throws ParseException {
        //获取输入文件的docId和dct(编号和发表时间？)
        String docId = TimeML.extractTimeMLDocId(inputTmlFile);
        String dct = TimeML.extractTimeMLDCT(inputTmlFile);

        //将tml文件解析为Article类对象
        Article article = parseTimeML.parseTimeML(inputTmlFile, true);

        //根据dct和Article对象获取timex并保存至结果tml
        String result = extractTimex(article, dct, true);
        TimeML.saveToTimeML(docId, dct, result, outputTmlFile);
    }

    public String extractTimexFromText(String inputText, String date) throws ParseException {
        List<TaggedToken> taggedTokenList = pipeline.tagging(inputText);
        Article article = new Article(inputText, taggedTokenList);

        return extractTimex(article, date);
    }

    private String extractTimex(Article article, String date) throws ParseException {
        String text = article.getText();
        List<TaggedToken> taggedTokenList = article.getTaggedTokenList();
        List<Integer> timeTokenList = identifyTimeToken(taggedTokenList);

        List<TimeSegment> timeSegmentList = identifyTimeSegment(taggedTokenList, timeTokenList, date);

        String tmlText = InduceTimeMLText(text, taggedTokenList, timeSegmentList, date, globalTid);

        taggedTokenList.clear();
        timeTokenList.clear();
        timeSegmentList.clear();

        return tmlText;
    }

    private String extractTimex(Article article, String date, boolean splitParagraph) throws ParseException {
        if (!splitParagraph)
            return extractTimex(article, date);
        if (article.getParagraphs() == null || article.getParagraphs().size() <= 0) {
            throw new RuntimeException("Invalid paragraphs.");
        }

        StringBuilder tmlSb = new StringBuilder();
        for (Paragraph paragraph : article.getParagraphs()) {
            String text = paragraph.getText();
            List<TaggedToken> taggedTokenList = paragraph.getTaggedTokenList();

            List<Integer> timeTokenList = identifyTimeToken(taggedTokenList);
            List<TimeSegment> timeSegmentList = identifyTimeSegment(taggedTokenList, timeTokenList, date);
            String tmlText = InduceTimeMLText(text, taggedTokenList, timeSegmentList, date, globalTid);

            tmlSb.append(tmlText);
            tmlSb.append("\n\n");

            taggedTokenList.clear();
            timeTokenList.clear();
            timeSegmentList.clear();
        }

        return tmlSb.toString();
    }

    private void printMetaInfo(TimeSegment timeSegment, OutputStreamWriter osw) {
        MetaInfo properties = timeSegment.getMetaInfo();
        List<TaggedToken> segmentTokenList = timeSegment.getSegmentTokenList();

        try {
            for (TaggedToken taggedToken : segmentTokenList) {
                osw.write(taggedToken.getToken() + " ");
            }
            osw.write("\t");
            for (MetaInfo.MetaInfoType t : MetaInfo.MetaInfoType.values()) {
                if (properties.getMetaInfoValue(t) != null)
                    osw.write(t.toString() + ":" + properties.getMetaInfoValue(t) + "\t");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String InduceTimeMLText(String text, List<TaggedToken> taggedTokenList, List<TimeSegment> timeSegmentList, String date, int startTid) throws ParseException {
        StringBuffer tmlSb = new StringBuffer();
        int tid = startTid;
        int lastCharPosition = 0;

        if (!timeSegmentList.isEmpty()) {
            boolean isTimex = true;
            int timexBeginTokenPosition = timeSegmentList.get(0).getBeginTokenPosition();
            int timexEndTokenPosition = timeSegmentList.get(0).getEndTokenPosition();

            TimexType timexType = timeSegmentList.get(0).getTimexType();
            String value = date;
            int timexTypePriority = 0;
            int segmentTypePriority = 0;

            SimpleDateFormat sdf = null;
            if (date.contains(","))
                sdf = new SimpleDateFormat("MMMMM dd, yyyy", Locale.US);
            else if (date.contains("-"))
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            else {
                date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            Calendar dct = Calendar.getInstance();
            dct.setTime(sdf.parse(date));

            int dctYear = dct.get(Calendar.YEAR);
            int dctMonth = dct.get(Calendar.MONTH) + 1;
            int dctDate = dct.get(Calendar.DATE);

            String refDate = dctYear + "-";
            if (dctMonth < 10)
                refDate += "0" + dctMonth + "-";
            else
                refDate += dctMonth + "-";
            if (dctDate < 10)
                refDate += "0" + dctDate;
            else
                refDate += dctDate;

            MetaInfo tempMetaInfo = new MetaInfo(null);
            timexTypePriority = XTimeRegex.getTimexTypePriority(timexType);

            for (int i = 1; i < timeSegmentList.size(); i++) {
                TimeSegment timeSegment = timeSegmentList.get(i);
                TimeSegment lastTimeSegment = timeSegmentList.get(i - 1);

                int segmentBeginTokenPosiiton = timeSegment.getBeginTokenPosition();
                int segmentEndTokenPosition = timeSegment.getEndTokenPosition();
                if (XTimeRegex.getTimexTypePriority(timeSegmentList.get(i - 1).getTimexType()) > XTimeRegex.getTimexTypePriority(timexType))
                    timexType = timeSegmentList.get(i - 1).getTimexType();
                timexTypePriority = XTimeRegex.getTimexTypePriority(timexType);
                segmentTypePriority = XTimeRegex.getTimexTypePriority(timeSegmentList.get(i).getTimexType());
                if (tempMetaInfo.getRefDate() == null)
                    tempMetaInfo = timeSegmentList.get(i - 1).getMetaInfo();

                //判断timeSegment重合
                if (timexEndTokenPosition + 1 == segmentBeginTokenPosiiton || timexEndTokenPosition > segmentBeginTokenPosiiton) {//有重合
                    isTimex = false;
                } else if (timexEndTokenPosition == segmentBeginTokenPosiiton) {//相连

                    if (taggedTokenList.get(segmentBeginTokenPosiiton).isComma()) {//连接处为逗号
                        if (segmentBeginTokenPosiiton == 0 || segmentBeginTokenPosiiton + 1 == taggedTokenList.size())
                            isTimex = true;
                        else {
                            TaggedToken commaPreToken = taggedTokenList.get(segmentBeginTokenPosiiton - 1);
                            TaggedToken commaSufToken = taggedTokenList.get(segmentBeginTokenPosiiton + 1);

                            if ((commaPreToken.isTimeToken() || commaPreToken.isNumeral()) && commaSufToken.isTimeToken() && !commaPreToken.sameTokenTypeWith(commaSufToken))
                                isTimex = false;
                            else
                                isTimex = true;
                        }
                    } else if (taggedTokenList.get(segmentBeginTokenPosiiton).isLinkage())
                        isTimex = true;
                    else
                        isTimex = false;
                } else
                    isTimex = true;

                if (!isTimex) {
                    if (timexTypePriority < segmentTypePriority)
                        timexType = XTimeRegex.getTimexType(segmentTypePriority);

                    tempMetaInfo.setTimexType(timexType);

                    timexEndTokenPosition = segmentEndTokenPosition;

                    //merge metaInfos
                    TimeSegment timeSegment1 = timeSegmentList.get(i - 1);
                    TimeSegment timeSegment2 = timeSegmentList.get(i);
                    MetaInfo metaInfo1 = timeSegment1.getMetaInfo();
                    MetaInfo metaInfo2 = timeSegment2.getMetaInfo();
                    tempMetaInfo.setRefDate(metaInfo1.getRefDate());

                    for (MetaInfo.MetaInfoType t : MetaInfo.MetaInfoType.values()) {
                        if (metaInfo1.getMetaInfoValue(t) == null && metaInfo2.getMetaInfoValue(t) != null)
                            tempMetaInfo.setMetaInfoValue(t, metaInfo2.getMetaInfoValue(t));
                        else if (metaInfo1.getMetaInfoValue(t) != null && metaInfo2.getMetaInfoValue(t) == null)
                            tempMetaInfo.setMetaInfoValue(t, metaInfo1.getMetaInfoValue(t));
                        else if (metaInfo1.getMetaInfoValue(t) != null && metaInfo2.getMetaInfoValue(t) != null) {
                            if (!timeSegment1.isDependent() && timeSegment2.isDependent())
                                tempMetaInfo.setMetaInfoValue(t, metaInfo1.getMetaInfoValue(t));
                            else
                                tempMetaInfo.setMetaInfoValue(t, metaInfo2.getMetaInfoValue(t));
                        }
                    }
                } else {
                    if (taggedTokenList.get(timexBeginTokenPosition).isComma() || taggedTokenList.get(timexBeginTokenPosition).isLinkage() || taggedTokenList.get(timexBeginTokenPosition).getTag().equals("IN"))
                        timexBeginTokenPosition++;
                    if (taggedTokenList.get(timexEndTokenPosition).isComma() || taggedTokenList.get(timexEndTokenPosition).isLinkage())
                        timexEndTokenPosition--;

                    TaggedToken timexBeginTaggedToken = taggedTokenList.get(timexBeginTokenPosition);
                    TaggedToken timexEndTaggedToken = taggedTokenList.get(timexEndTokenPosition);
                    String timexEndToken = timexEndTaggedToken.getToken();
                    int beginCharPosition = timexBeginTaggedToken.getBeginCharPosition();
                    int endCharPosition = timexEndTaggedToken.getEndCharPosition();

                    tmlSb.append(text, lastCharPosition, beginCharPosition);
                    lastCharPosition = beginCharPosition;

                    String nn = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER);
                    String yy = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.YEARYEAR);
                    String mm = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.MONTHMONTH);

                    for (int index = timexBeginTokenPosition; index <= timexEndTokenPosition; index++) {
                        TaggedToken temTaggedToken = taggedTokenList.get(index);
                        if (temTaggedToken.isYearYear() || temTaggedToken.isYearMonth() || temTaggedToken.isMonthMonth() || temTaggedToken.isWeekWeek() || temTaggedToken.isTimeTime()
                                || temTaggedToken.isHalfDayHalfDay() || temTaggedToken.isNumeralNumeral()) {

                            int temBeginCharPosition = temTaggedToken.getBeginCharPosition();
                            String[] items = temTaggedToken.getToken().split("-");
                            String timex = text.substring(lastCharPosition, temBeginCharPosition + items[0].length());
                            tempMetaInfo.setTimexType(timexType);

                            if (nn != null) {
                                if (lastTimeSegment.getTimeToken().isEra()) {
                                    tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, nn.split("-")[0]);
                                } else
                                    tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER, nn.split("-")[0]);
                            }
                            if (yy != null)
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[0]);
                            if (mm != null)
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, mm.split("-")[0]);
                            if (lastTimeSegment.getTimeToken().isEra())
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.ERA, lastTimeSegment.getTimeToken().getToken());

                            value = tempMetaInfo.getTimexValue();

                            tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex) + "-");
                            lastCharPosition = temBeginCharPosition + items[0].length() + "-".length();
                            tid++;
                        }
                    }

                    if (timexEndToken.endsWith("'s")) {
                        String timex = text.substring(lastCharPosition, endCharPosition - 2);
                        tempMetaInfo.setTimexType(timexType);
                        value = tempMetaInfo.getTimexValue();
                        tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));
                        lastCharPosition = endCharPosition - 2;
                    } else if (timexEndToken.endsWith("s") && (timexEndTokenPosition + 1 < taggedTokenList.size()) && taggedTokenList.get(timexEndTokenPosition + 1).getToken().equals("'")) {
                        String timex = text.substring(lastCharPosition, taggedTokenList.get(timexEndTokenPosition + 1).getEndCharPosition());
                        tempMetaInfo.setTimexType(timexType);
                        value = tempMetaInfo.getTimexValue();
                        tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));

                        lastCharPosition = taggedTokenList.get(timexEndTokenPosition + 1).getEndCharPosition();
                    } else {
                        String timex = text.substring(lastCharPosition, endCharPosition);
                        tempMetaInfo.setTimexType(timexType);

                        if (nn != null) {
                            if (lastTimeSegment.getTimeToken().isEra()) {
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, nn.split("-")[1]);
                            } else
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER, nn.split("-")[1]);
                        }
                        if (yy != null) {
                            if (yy.split("-")[1].length() == 2)
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[0].substring(0, 2) + yy.split("-")[1]);
                            else if (yy.split("-")[1].length() == 4)
                                tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[1]);
                        }
                        if (mm != null)
                            tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, mm.split("-")[1]);
                        if (lastTimeSegment.getTimeToken().isEra())
                            tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.ERA, lastTimeSegment.getTimeToken().getToken());
                        value = tempMetaInfo.getTimexValue();
                        tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));

                        lastCharPosition = endCharPosition;
                    }
                    tid++;
                    tempMetaInfo = new MetaInfo(null);
                    timexBeginTokenPosition = segmentBeginTokenPosiiton;
                    timexEndTokenPosition = segmentEndTokenPosition;
                    timexType = TimexType.DATE;

                }
            }
            // processing last timeSegment
            TimeSegment timeSegment = timeSegmentList.get(timeSegmentList.size() - 1);

            if (timexType == null) {
                timexType = timeSegment.getTimexType();
            } else {
                segmentTypePriority = timeSegment.getTimexType().ordinal();
                if (segmentTypePriority > timexType.ordinal())
                    timexType = timeSegment.getTimexType();
            }
            if (tempMetaInfo.getRefDate() != null) {
                //merge metaInfos
                MetaInfo metaInfo = timeSegment.getMetaInfo();

                for (MetaInfo.MetaInfoType t : MetaInfo.MetaInfoType.values()) {
                    if (metaInfo.getMetaInfoValue(t) != null)
                        tempMetaInfo.setMetaInfoValue(t, metaInfo.getMetaInfoValue(t));
                }
            } else {
                tempMetaInfo = timeSegment.getMetaInfo();
            }

            if (taggedTokenList.get(timexBeginTokenPosition).isComma() || taggedTokenList.get(timexBeginTokenPosition).isLinkage() || taggedTokenList.get(timexBeginTokenPosition).getTag().equals("IN"))
                timexBeginTokenPosition++;
            if (taggedTokenList.get(timexEndTokenPosition).isComma() || taggedTokenList.get(timexEndTokenPosition).isLinkage())
                timexEndTokenPosition--;

            TaggedToken timexBeginTaggedToken = taggedTokenList.get(timexBeginTokenPosition);
            TaggedToken timexEndTaggedToken = taggedTokenList.get(timexEndTokenPosition);
            String timexEndToken = timexEndTaggedToken.getToken();
            int beginCharPosition = timexBeginTaggedToken.getBeginCharPosition();
            int endCharPosition = timexEndTaggedToken.getEndCharPosition();


            tmlSb.append(text.substring(lastCharPosition, beginCharPosition));
            lastCharPosition = beginCharPosition;

            String nn = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER);
            String yy = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.YEARYEAR);
            String mm = tempMetaInfo.getMetaInfoValue(MetaInfo.MetaInfoType.MONTHMONTH);

            for (int index = timexBeginTokenPosition; index <= timexEndTokenPosition; index++) {
                TaggedToken temTaggedToken = taggedTokenList.get(index);
                if (temTaggedToken.isYearYear() || temTaggedToken.isYearMonth() || temTaggedToken.isMonthMonth() || temTaggedToken.isWeekWeek() || temTaggedToken.isTimeTime()
                        || temTaggedToken.isHalfDayHalfDay() || temTaggedToken.isNumeralNumeral()) {

                    int temBeginCharPosition = temTaggedToken.getBeginCharPosition();
                    String[] items = temTaggedToken.getToken().split("-");
                    String timex = text.substring(lastCharPosition, temBeginCharPosition + items[0].length());
                    tempMetaInfo.setTimexType(timexType);

                    if (nn != null) {
                        if (timeSegment.getTimeToken().isEra()) {
                            tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, nn.split("-")[0]);
                        } else
                            tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER, nn.split("-")[0]);
                    }

                    if (yy != null)
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[0]);
                    if (mm != null)
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, mm.split("-")[0]);
                    if (timeSegment.getTimeToken().isEra())
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.ERA, timeSegment.getTimeToken().getToken());

                    value = tempMetaInfo.getTimexValue();
                    tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex) + "-");
                    lastCharPosition = temBeginCharPosition + items[0].length() + "-".length();
                    tid++;
                }
            }

            if (timexEndToken.endsWith("'s")) {
                String timex = text.substring(lastCharPosition, endCharPosition - 2);
                tempMetaInfo.setTimexType(timexType);
                value = tempMetaInfo.getTimexValue();
                tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));

                lastCharPosition = endCharPosition - 2;
            } else if (timexEndToken.endsWith("s") && (timexEndTokenPosition + 1 < taggedTokenList.size()) && taggedTokenList.get(timexEndTokenPosition + 1).getToken().equals("'")) {
                String timex = text.substring(lastCharPosition, taggedTokenList.get(timexEndTokenPosition + 1).getEndCharPosition());
                tempMetaInfo.setTimexType(timexType);
                value = tempMetaInfo.getTimexValue();
                tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));

                lastCharPosition = taggedTokenList.get(timexEndTokenPosition + 1).getEndCharPosition();
            } else {
                String timex = text.substring(lastCharPosition, endCharPosition);
                tempMetaInfo.setTimexType(timexType);

                if (nn != null) {
                    if (timeSegment.getTimeToken().isEra()) {
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, nn.split("-")[1]);
                    } else
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER, nn.split("-")[1]);
                }

                if (yy != null) {
                    if (yy.split("-")[1].length() == 2)
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[0].substring(0, 2) + yy.split("-")[1]);
                    else if (yy.split("-")[1].length() == 4)
                        tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, yy.split("-")[1]);
                }
                if (mm != null)
                    tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, mm.split("-")[1]);
                if (timeSegment.getTimeToken().isEra())
                    tempMetaInfo.setMetaInfoValue(MetaInfo.MetaInfoType.ERA, timeSegment.getTimeToken().getToken());
                value = tempMetaInfo.getTimexValue();
                tmlSb.append(TimeML.getTIMEX3(tid, timexType.toString(), value, timex));

                lastCharPosition = endCharPosition;
            }

            tid++;
        }

        tmlSb.append(text.substring(lastCharPosition));
        this.globalTid = tid;
        return tmlSb.toString();
    }

    private List<TimeSegment> identifyTimeSegment(List<TaggedToken> taggedTokenList, List<Integer> timeTokenList, String date) throws ParseException {
        List<TimeSegment> timeSegmentList = new ArrayList<TimeSegment>();
        int firstToken = 0;
        int lastToken = taggedTokenList.size() - 1;

        SimpleDateFormat sdf = null;
        if (date.contains(","))
            sdf = new SimpleDateFormat("MMMMM dd, yyyy", Locale.US);
        else if (date.contains("-"))
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        else {
            date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6);
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        }
        Calendar dct = Calendar.getInstance();
        dct.setTime(sdf.parse(date));

        int dctYear = dct.get(Calendar.YEAR);
        int dctMonth = dct.get(Calendar.MONTH) + 1;
        int dctDate = dct.get(Calendar.DATE);
        int dctWeek = dct.get(Calendar.WEEK_OF_YEAR);

        String refDate = dctYear + "-";
        if (dctMonth < 10)
            refDate += "0" + dctMonth + "-";
        else
            refDate += dctMonth + "-";
        if (dctDate < 10)
            refDate += "0" + dctDate;
        else
            refDate += dctDate;

        for (int i = 0; i < timeTokenList.size(); i++) {
            //从每个timeToken处开始扩展
            int timeTokenPosition = timeTokenList.get(i);

            int beginToken = timeTokenPosition;
            int endToken = timeTokenPosition;
            TaggedToken taggedTimeToken = taggedTokenList.get(timeTokenPosition);

            TimexType timexType = null;

            // mapping time token to timex type
            if (taggedTimeToken.isTime() || taggedTimeToken.isHalfDay() || taggedTimeToken.isTimeZone() || taggedTimeToken.isDayTime()) {
                timexType = TimexType.TIME;
            } else if (taggedTimeToken.isYear() || taggedTimeToken.isMonth() || taggedTimeToken.isWeek() || taggedTimeToken.isDate() || taggedTimeToken.isEra()
                    || taggedTimeToken.isSeason() || taggedTimeToken.isDecade() || taggedTimeToken.isTimeline() || taggedTimeToken.isHoliday()
                    || taggedTimeToken.isContinuousTimeUnit() || taggedTimeToken.isDiscreteTimeUnit() || taggedTimeToken.isOrdinalTimeUnit()) {
                timexType = TimexType.DATE;
            } else if (taggedTimeToken.isPeriodical() || taggedTimeToken.isDiscretePeriod()) {
                timexType = TimexType.SET;
            } else if (taggedTimeToken.isDuration() || taggedTimeToken.isContinuousPeriod()) {
                timexType = TimexType.DURATION;
            }

            if (taggedTimeToken.isPeriodical()) {
                List<TaggedToken> segmentTokenList = new ArrayList<TaggedToken>();
                for (int j = beginToken; j <= endToken; j++)
                    segmentTokenList.add(taggedTokenList.get(j));
                timeSegmentList.add(new TimeSegment(TimexType.SET, segmentTokenList, false, taggedTimeToken, timeTokenPosition, beginToken, endToken));
                continue;
            } else if (taggedTimeToken.isDuration()) {
                List<TaggedToken> segmentTokenList = new ArrayList<TaggedToken>();
                for (int j = beginToken; j <= endToken; j++)
                    segmentTokenList.add(taggedTokenList.get(j));
                timeSegmentList.add(new TimeSegment(TimexType.DURATION, segmentTokenList, false, taggedTimeToken, timeTokenPosition, beginToken, endToken));
                continue;
            } else if (taggedTimeToken.isOrdinalTimeUnit()) {
                List<TaggedToken> segmentTokenList = new ArrayList<TaggedToken>();
                for (int j = beginToken; j <= endToken; j++)
                    segmentTokenList.add(taggedTokenList.get(j));
                timeSegmentList.add(new TimeSegment(TimexType.DATE, segmentTokenList, false, taggedTimeToken, timeTokenPosition, beginToken, endToken));
                continue;
            }

            int leftBound = firstToken;
            int rightBound = lastToken;
            if (i > 0)
                leftBound = timeTokenList.get(i - 1) + 1;
            if (i < timeTokenList.size() - 1)
                rightBound = timeTokenList.get(i + 1) - 1;

            /** Search its left side */
            boolean findLeftDependentSegment = false;
            int leftTimeTokenPosition = -1;
            int leftBeginToken = leftTimeTokenPosition;
            int leftEndToken = leftTimeTokenPosition;
            for (int j = timeTokenPosition - 1; j >= leftBound; j--) {
                //检索到的前一个token
                TaggedToken taggedPreMod = taggedTokenList.get(j);
                if (taggedPreMod.isPrefixMod() || taggedPreMod.isNumeral() || taggedPreMod.isInArticle()) {//直接扩张
                    beginToken = j;
                    if (taggedPreMod.isPrefixQuantitative() || taggedPreMod.isNumber()) {
                        if (taggedTimeToken.isContinuousTimeUnit() || taggedTimeToken.isContinuousPeriod())
                            timexType = TimexType.DURATION;
                        else if (taggedTimeToken.isDiscreteTimeUnit() || taggedTimeToken.isDiscretePeriod())
                            timexType = TimexType.SET;
                    } else if (taggedPreMod.isPrefixFrequent())
                        timexType = TimexType.SET;
                    else if (taggedPreMod.isInArticle() && (taggedTimeToken.isContinuousTimeUnit() || taggedTimeToken.isDiscreteTimeUnit())) {
                        timexType = TimexType.DURATION;
                    }
                } else if (taggedPreMod.isComma()) {//扩张然后跳出
                    beginToken = j;
                    break;
                } else if (taggedPreMod.isLinkage()) {//连接词
                    if (j - 1 >= leftBound && taggedTimeToken.isTimeToken() && taggedTokenList.get(j - 1).isNumeral()) {
                        findLeftDependentSegment = true;//找到依赖timeSegment
                        leftTimeTokenPosition = j - 1;
                        leftBeginToken = leftTimeTokenPosition;
                        leftEndToken = leftTimeTokenPosition;
                        for (int k = leftTimeTokenPosition - 1; k >= leftBound; k--) {
                            if (taggedPreMod.isPrefixMod() || taggedPreMod.isNumeral() || taggedPreMod.isInArticle()) {
                                leftBeginToken = k;
                                if (taggedPreMod.isPrefixQuantitative() || taggedPreMod.isNumber()) {
                                    if (taggedTimeToken.isContinuousTimeUnit() || taggedTimeToken.isContinuousPeriod())
                                        timexType = TimexType.DURATION;
                                    else if (taggedTimeToken.isDiscreteTimeUnit() || taggedTimeToken.isDiscretePeriod())
                                        timexType = TimexType.SET;
                                } else if (taggedPreMod.isPrefixFrequent())
                                    timexType = TimexType.SET;
                                else if (taggedPreMod.isInArticle() && (taggedTimeToken.isContinuousTimeUnit() || taggedTimeToken.isDiscreteTimeUnit()))
                                    timexType = TimexType.DURATION;
                            } else
                                break;
                        }
                        beginToken = j + 1;
                    } else
                        beginToken = j;
                    break;
                } else
                    break;
            }

            /** Search its right side */
            boolean findRightDependentSegment = false;
            int rightTimeTokenPosition = 0;
            int rightBeginToken = rightTimeTokenPosition;
            int rightEndToken = rightTimeTokenPosition;
            for (int j = timeTokenPosition + 1; j <= rightBound; j++) {
                TaggedToken taggedSufMod = taggedTokenList.get(j);
                if (taggedSufMod.isSuffixMod() || taggedSufMod.isNumeral()) {
                    endToken = j;
                    if (taggedSufMod.isSuffixOperate()) {
                        if (timexType.equals(TimexType.DURATION))
                            timexType = TimexType.DATE;
                    } else if (taggedSufMod.isSuffixDura())
                        timexType = TimexType.DURATION;
                } else if (taggedSufMod.isComma()) {
                    endToken = j;
                    break;
                } else if (taggedSufMod.isLinkage()) {
                    if (j + 1 <= rightBound && taggedTimeToken.isTimeToken() && (taggedTokenList.get(j + 1).isNumeral() || taggedTokenList.get(j + 1).isHalfDay())) {
                        findRightDependentSegment = true;
                        rightTimeTokenPosition = j + 1;
                        rightBeginToken = rightTimeTokenPosition;
                        rightEndToken = rightTimeTokenPosition;
                        for (int k = rightTimeTokenPosition + 1; k <= rightBound; k++) {
                            if (taggedSufMod.isSuffixMod() || taggedSufMod.isNumeral()) {
                                rightEndToken = k;
                                if (taggedSufMod.isSuffixOperate()) {
                                    if (timexType.equals(TimexType.DURATION)) {
                                        timexType = TimexType.DATE;
                                    }
                                } else if (taggedSufMod.isSuffixDura()) {
                                    timexType = TimexType.DURATION;
                                }
                            } else
                                break;
                        }
                        endToken = j - 1;
                    } else
                        endToken = j;
                    break;
                } else
                    break;
            }

            if (findLeftDependentSegment) {
                List<TaggedToken> segmentTokenListLeft = new ArrayList<TaggedToken>();
                for (int j = leftBeginToken; j <= leftEndToken; j++)
                    segmentTokenListLeft.add(taggedTokenList.get(j));
                timeSegmentList.add(new TimeSegment(timexType, segmentTokenListLeft, true, taggedTimeToken, leftTimeTokenPosition, leftBeginToken, leftEndToken));
            }

            List<TaggedToken> segmentTokenList = new ArrayList<TaggedToken>();
            for (int j = beginToken; j <= endToken; j++)
                segmentTokenList.add(taggedTokenList.get(j));
            timeSegmentList.add(new TimeSegment(timexType, segmentTokenList, false, taggedTimeToken, timeTokenPosition, beginToken, endToken));

            if (findRightDependentSegment) {
                List<TaggedToken> segmentTokenListRight = new ArrayList<TaggedToken>();
                for (int j = rightBeginToken; j <= rightEndToken; j++)
                    segmentTokenList.add(taggedTokenList.get(j));
                timeSegmentList.add(new TimeSegment(timexType, segmentTokenListRight, true, taggedTimeToken, rightTimeTokenPosition, rightBeginToken, rightEndToken));
            }
        }

        //TimeSegment value
        for (int k = 0; k < timeSegmentList.size(); k++) {
            TimeSegment segment = timeSegmentList.get(k);
            TimexType timexType = segment.getTimexType();

            List<TaggedToken> segmentTokenList = new ArrayList<TaggedToken>(segment.getSegmentTokenList());

            MetaInfo properties = new MetaInfo(refDate, timexType);
            String modifySign = "";
            String modifyValue = "";
            MetaInfo.MetaInfoType modifyType = null;

            //remove useless prefix and suffix
            if (segmentTokenList.size() > 0) {
                if (segmentTokenList.get(0).isComma())
                    segmentTokenList.remove(0);
                if (segmentTokenList.get(segmentTokenList.size() - 1).isComma())
                    segmentTokenList.remove(segmentTokenList.size() - 1);
                if (Pattern.matches("(?i)and|of|to|early|late|almost|nearly|about|-", segmentTokenList.get(0).getToken()))
                    segmentTokenList.remove(0);
                if (Pattern.matches("(?i)and|of|to|early|late|almost|nearly|about|-", segmentTokenList.get(segmentTokenList.size() - 1).getToken()))
                    segmentTokenList.remove(segmentTokenList.size() - 1);
                if (segmentTokenList.size() > 1 && Pattern.matches("(?i)the", segmentTokenList.get(0).getToken()) && (segmentTokenList.get(1).isPrefixMod() || segmentTokenList.get(1).isNumeral()))
                    segmentTokenList.remove(0);
            }

            for (int i = 0; i < segmentTokenList.size(); i++) {
                TaggedToken token = segmentTokenList.get(i);
                String tokenValue = token.getValue();

                if (tokenValue != null) {
                    if (token.isMonth()) {
//                        dctMonth = Integer.parseInt(tokenValue);
//                        refDate = refDate.split("-")[0] + "-" + tokenValue + "-" + refDate.split("-")[2];
                        properties.setRefDate(refDate);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, tokenValue);
                    } else if ((i > 0 && segmentTokenList.get(i - 1).isMonth() && token.isNumeral()) || (i < segmentTokenList.size() - 1 && segmentTokenList.get(i + 1).isMonth() && token.isNumeral())) {
                        if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAY) == null)
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.DAY, tokenValue);
                    }
                    if (token.isDate())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, tokenValue);
                    if (token.isDiscreteTimeUnit())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DISCRETETIMEUNIT, tokenValue);
                    if (token.isDiscretePeriod())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DISCRETEPERIOD, tokenValue);
                    if (token.isContinuousTimeUnit())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT, tokenValue);
                    if (token.isContinuousPeriod())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSPERIOD, tokenValue);

                    if (token.isWeek())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.WEEK, tokenValue);

                    if (token.isNumber())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, tokenValue);
                    if (token.isInArticle())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, tokenValue);

                    if (token.isOrdinal()) {
                        if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.ORDINAL) == null)
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.ORDINAL, tokenValue);
                    }

                    if (token.isDecade())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DECADE, tokenValue);

                    if (token.isPeriodical())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.PERIODICAL, tokenValue);

                    if (token.isDayTime())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DAYTIME, tokenValue);

                    if (token.isSeason())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.SEASON, tokenValue);

                    if (token.isTimeline()) {
                        if (token.isToday()) {
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, refDate);
//                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIMELINE, tokenValue);
                        } else if (token.isTomorrow()) {
                            dct.add(Calendar.DATE, 1);
                            int tmrYear = dct.get(Calendar.YEAR);
                            int tmrMonth = dct.get(Calendar.MONTH) + 1;
                            int tmrDate = dct.get(Calendar.DATE);

                            String tmrDateStr = tmrYear + "-";
                            if (tmrMonth < 10)
                                tmrDateStr += "0" + tmrMonth + "-";
                            else
                                tmrDateStr += tmrMonth + "-";
                            if (tmrDate < 10)
                                tmrDateStr += "0" + tmrDate;
                            else
                                tmrDateStr += tmrDate;
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, tmrDateStr);
                            dct.add(Calendar.DATE, -1);
                        } else if (token.isYesterday()) {
                            dct.add(Calendar.DATE, -1);
                            int ystYear = dct.get(Calendar.YEAR);
                            int ystMonth = dct.get(Calendar.MONTH) + 1;
                            int ystDate = dct.get(Calendar.DATE);

                            String ystDateStr = ystYear + "-";
                            if (ystMonth < 10)
                                ystDateStr += "0" + ystMonth + "-";
                            else
                                ystDateStr += ystMonth + "-";
                            if (ystDate < 10)
                                ystDateStr += "0" + ystDate;
                            else
                                ystDateStr += ystDate;
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, ystDateStr);
                            dct.add(Calendar.DATE, 1);
                        } else {
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIMELINE, tokenValue);
                        }
                    }

                    if (token.isPrefixQuantitative())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, tokenValue);

                    if (token.isHoliday()) {
                        String month = tokenValue.split("-")[0];
                        String day = tokenValue.split("-")[1];
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, month);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DAY, day);
                    }

                    if (token.isPrefixOperate() || token.isSuffixOperate())
                        modifySign = tokenValue;

                } else {
                    if (token.isYear() && !token.isYearYear()) {
                        String pureYear = "";
                        String strYear = token.getToken();
                        for (int index = 0; index < strYear.length(); index++) {
                            if (Character.isDigit(strYear.charAt(index)))
                                pureYear += strYear.charAt(index);
                        }
//                        dctYear=Integer.parseInt(pureYear);
//                        refDate = dctYear+"-"+refDate.split("-")[1]+"-"+refDate.split("-")[2];
                        properties.setRefDate(refDate);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, pureYear);
                    } else if (token.isYearYear()) {
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEARYEAR, token.getToken());
                    } else if (token.isMonthMonth()) {
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTHMONTH, InduceTokenTypeValue.getTokenValue(token.getToken().split("-")[0], TokenType.MONTH) + "-" + InduceTokenTypeValue.getTokenValue(token.getToken().split("-")[1], TokenType.MONTH));
                    } else if (token.isMonth()) {
                        String value = InduceTokenTypeValue.getTokenValue(token.getToken().split("-")[1], TokenType.MONTH);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, value);
                    } else if (token.isDate()) {
                        SimpleDateFormat simpleDateFormat = null;
                        if (token.getToken().contains(","))
                            sdf = new SimpleDateFormat("MMMMM dd, yyyy", Locale.US);
                        else if (token.getToken().contains("-")) {
                            String[] items = token.getToken().split("-");
                            if (items[0].length() >= 3)
                                sdf = new SimpleDateFormat("yyyy-MM-dd");
                            else if (Integer.parseInt(items[0]) <= 12)
                                sdf = new SimpleDateFormat("MM-dd-yyyy");
                            else
                                sdf = new SimpleDateFormat("dd-MM-yyyy");
                        } else if (token.getToken().contains("/")) {
                            String[] items = token.getToken().split("/");
                            if (items[0].length() >= 3)
                                sdf = new SimpleDateFormat("yyyy/MM/dd");
                            else if (Integer.parseInt(items[0]) <= 12)
                                sdf = new SimpleDateFormat("MM/dd/yyyy");
                            else
                                sdf = new SimpleDateFormat("dd/MM/yyyy");
                        } else if (token.getToken().contains(".")) {
                            String[] items = token.getToken().split("\\.");
                            if (items[0].length() >= 3)
                                sdf = new SimpleDateFormat("yyyy.MM.dd");
                            else if (Integer.parseInt(items[0]) <= 12)
                                sdf = new SimpleDateFormat("MM.dd.yyyy");
                            else
                                sdf = new SimpleDateFormat("dd.MM.yyyy");
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(sdf.parse(token.getToken()));

                        int tokenYear = calendar.get(Calendar.YEAR);
                        int tokenMonth = calendar.get(Calendar.MONTH) + 1;
                        int tokenDate = calendar.get(Calendar.DATE);

                        String dateValue = tokenYear + "-";
                        if (tokenMonth < 10)
                            dateValue += "0" + tokenMonth + "-";
                        else
                            dateValue += tokenMonth + "-";
                        if (tokenDate < 10)
                            dateValue += "0" + tokenDate;
                        else
                            dateValue += tokenDate;
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, dateValue);
                    } else if (token.isTime()) {
                        String hour = token.getToken().split(":")[0];
                        String time = "";
                        if (hour.length() == 1)
                            time = "0";
                        time += token.getToken();
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, time);
                    } else if (token.isEra())
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.ERA, token.getToken().toUpperCase());
                    else if (Pattern.matches("\\d+", token.getToken())) {
                        if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER) == null)
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, token.getToken());
                    } else if (token.isOrdinalTimeUnit()) {
                        String token1 = token.getToken().split("-")[0];
                        String token2 = token.getToken().split("-")[1];
                        String value1 = InduceTokenTypeValue.getTokenValue(token1, TokenType.ORDINAL);
                        String value2 = InduceTokenTypeValue.getTokenValue(token2, TokenType.CONTINUOUS_TIMEUNIT);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT, value2);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.ORDINAL, value1);

                    } else if (token.isNumberTimeUnit()) {
                        String token1 = token.getToken().split("-")[0];
                        String token2 = token.getToken().split("-")[1];
                        String value1 = InduceTokenTypeValue.getTokenValue(token1, TokenType.NUMBER);
                        String value2 = InduceTokenTypeValue.getTokenValue(token2, TokenType.CONTINUOUS_TIMEUNIT);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT, value2);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, value1);
                    } else if (token.isPrefixCommon()) {
                        modifySign = "+";
                        modifyValue = "0";
                    } else if (token.isDuration()) {
                        String tmp = token.getToken();
                        String number = "";
                        int index = 0;
                        for (; index < tmp.length(); index++) {
                            if (Character.isDigit(tmp.charAt(index)))
                                number += tmp.charAt(index);
                            else if (Character.isAlphabetic(tmp.charAt(index)))
                                break;
                        }
                        String cUnit = tmp.substring(index).toLowerCase();
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBER, number);

                        String value = InduceTokenTypeValue.getTokenValue(cUnit, TokenType.CONTINUOUS_TIMEUNIT);
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT, value);
                    } else if (token.isNumberNumber()) {
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.NUMBERNUMBER, token.getToken());
                    }
                }
                if (token.isHalfDay()) {
                    if (segmentTokenList.size() == 1) {
                        if (k - 1 >= 0 && !Character.isDigit(token.getToken().charAt(0))) {
                            properties = timeSegmentList.get(k - 1).getMetaInfo();
                            if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.TIME) != null) {
                                String[] times = properties.getMetaInfoValue(MetaInfo.MetaInfoType.TIME).split(":");
                                String hour = times[0];
                                if (Pattern.matches("(?i)a\\.?m\\.?", token.getToken())) {
                                    if (hour.equals("12")) {
                                        hour = "0";
                                    }
                                } else if (Pattern.matches("(?i)p\\.?m\\.?", token.getToken())) {
                                    if (!hour.equals("12"))
                                        hour = String.valueOf(Integer.parseInt(hour) + 12);
                                }
                                for (int j = 1; j < times.length; j++)
                                    hour += ":" + times[j];
                                properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, hour);
                            }
                        } else {
                            String tmp = token.getToken();
                            String hour = "";
                            String minute = "00";
                            int index = 0;
                            for (; index < tmp.length(); index++) {
                                if (Character.isDigit(tmp.charAt(index)))
                                    hour += tmp.charAt(index);
                                else
                                    break;
                            }

                            if (hour.length() > 2) {
                                minute = hour.substring(hour.length() - 2);
                                hour = hour.substring(0, hour.length() - 2);
                            }

                            String unit = tmp.substring(index);
                            if (Pattern.matches("(?i)a\\.?m\\.?", unit)) {
                                if (hour.equals("12")) {
                                    hour = "0";
                                }
                            } else if (Pattern.matches("(?i)p\\.?m\\.?", unit)) {
                                if (!hour.equals("12"))
                                    hour = String.valueOf(Integer.parseInt(hour) + 12);
                            }
                            if (!hour.equals("")) {
                                if (hour.length() < 2)
                                    hour = "0" + hour;
                                hour += ":" + minute;
                            }
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, hour);
                        }
                    } else {
                        if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER) != null) {
                            String hour = properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER);
                            if (Pattern.matches("(?i)a\\.?m\\.?", token.getToken())) {
                                if (hour.equals("12")) {
                                    hour = "0";
                                }
                            } else if (Pattern.matches("(?i)p\\.?m\\.?", token.getToken())) {
                                if (!hour.equals("12"))
                                    hour = String.valueOf(Integer.parseInt(hour) + 12);
                            }
                            String time = "";
                            if (hour.length() < 2)
                                hour = "0" + hour;
                            hour += ":00";
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, hour);
                        } else if (i >= 1) {
                            String temp = segmentTokenList.get(i - 1).getToken();
                            String[] times = new String[5];
                            if (temp.contains(":"))
                                times = temp.split(":");
                            else if (temp.contains("."))
                                times = temp.split("\\.");
                            String hour = times[0];
                            if (Pattern.matches("(?i)a\\.?m\\.?", token.getToken())) {
                                if (hour.equals("12")) {
                                    hour = "0";
                                }
                            } else if (Pattern.matches("(?i)p\\.?m\\.?", token.getToken())) {
                                if (!hour.equals("12"))
                                    hour = String.valueOf(Integer.parseInt(hour) + 12);
                            }
                            for (int j = 1; j < times.length; j++)
                                hour += ":" + times[j];
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, hour);
                        } else {
                            String tmp = token.getToken();
                            String hour = "";
                            int index = 0;
                            for (; index < tmp.length(); index++) {
                                if (Character.isDigit(tmp.charAt(index)))
                                    hour += tmp.charAt(index);
                                else
                                    break;
                            }
                            String unit = tmp.substring(index);
                            if (Pattern.matches("(?i)a\\.?m\\.?", unit)) {
                                if (hour.equals("12")) {
                                    hour = "0";
                                }
                            } else if (Pattern.matches("(?i)p\\.?m\\.?", unit)) {
                                if (!hour.equals("12"))
                                    hour = String.valueOf(Integer.parseInt(hour) + 12);
                            }
                            if (!hour.equals(""))
                                hour += ":00";
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIME, hour);
                        }
                    }
                }

            }
            if (segment.isDependent()) {
                if (segment.getTimeToken().isMonth())
                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, String.valueOf(segment.getTimeToken().getValue()));
                if (segment.getTimeToken().isContinuousPeriod())
                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSPERIOD, segment.getTimeToken().getValue());
            }

            //getDate
            if (segment.getTimexType().equals(TimexType.DATE) && properties.getMetaInfoValue(MetaInfo.MetaInfoType.DATE) == null) {

                if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) != null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER) != null) {
                    if (Integer.parseInt(properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER)) <= 31) {
                        properties.setMetaInfoValue(MetaInfo.MetaInfoType.DAY, properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER));
//                        dctDate = Integer.parseInt(properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER));
//                        refDate = refDate.split("-")[0] + "-" + refDate.split("-")[1] + "-" + dctDate;
                        properties.setRefDate(refDate);
                    }
                }
                if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.YEAR) == null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) == null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAY) != null) {
                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, String.valueOf(dctYear));
                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.MONTH, String.valueOf(dctMonth));
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.YEAR) == null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) != null) {
                    boolean alreadySetYear = true;
                    if (k > 1) {
                        TimeSegment preSegment = timeSegmentList.get(k - 1);
                        if (preSegment.getTimeToken().isYear() && 0 <= segment.getBeginTokenPosition() - preSegment.getEndTokenPosition() && segment.getBeginTokenPosition() - preSegment.getEndTokenPosition() < 3) {
                            String year = preSegment.getTimeToken().getToken();
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, year);
                        } else if (k < timeSegmentList.size() - 1) {
                            TimeSegment sufSegment = timeSegmentList.get(k + 1);
                            if (sufSegment.getTimeToken().isYear() && 0 <= sufSegment.getBeginTokenPosition() - segment.getEndTokenPosition() && sufSegment.getBeginTokenPosition() - segment.getEndTokenPosition() < 3) {
                                String year = sufSegment.getTimeToken().getToken();
                                properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, year);
                            } else
                                alreadySetYear = false;
                        } else
                            alreadySetYear = false;
                    } else if (k < timeSegmentList.size() - 1) {
                        TimeSegment sufSegment = timeSegmentList.get(k + 1);
                        if (sufSegment.getTimeToken().isYear() && 0 <= sufSegment.getBeginTokenPosition() - segment.getEndTokenPosition() && sufSegment.getBeginTokenPosition() - segment.getEndTokenPosition() < 3) {
                            String year = sufSegment.getTimeToken().getToken();
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, year);
                        } else
                            alreadySetYear = false;
                    } else
                        alreadySetYear = false;
                    if (!alreadySetYear) {
                        int curMonth = dct.get(Calendar.MONTH) + 1;
                        int targetMonth = Integer.parseInt(properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH));
                        if (targetMonth - curMonth > 6)
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, String.valueOf(dctYear - 1));
                        else if (curMonth - targetMonth > 6)
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, String.valueOf(dctYear + 1));
                        else
                            properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, String.valueOf(dctYear));
                    }
                }

                if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.YEAR) != null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) != null
                        && properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAY) != null) {
                    String tdate = properties.getMetaInfoValue(MetaInfo.MetaInfoType.YEAR) + "-";
                    if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH).length() == 1)
                        tdate += "0";
                    tdate += properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) + "-";

                    if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAY).length() == 1)
                        tdate += "0";
                    tdate += properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAY);

                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.DATE, tdate);
                }
                if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.ERA) != null && properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER) != null)
                    properties.setMetaInfoValue(MetaInfo.MetaInfoType.YEAR, properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER));
            }


            //modify
            if (!modifySign.equals("")) {
                if (!modifyValue.equals("0")) {
                    if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER) != null)
                        modifyValue = properties.getMetaInfoValue(MetaInfo.MetaInfoType.NUMBER);
                    else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.ORDINAL) != null)
                        modifyValue = properties.getMetaInfoValue(MetaInfo.MetaInfoType.ORDINAL);
                    else
                        modifyValue = "1";
                }

                if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.DISCRETETIMEUNIT) != null)
                    modifyType = MetaInfo.MetaInfoType.WEEK;
                else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT) != null) {
                    switch (properties.getMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSTIMEUNIT)) {
                        case "DE":
                            modifyType = MetaInfo.MetaInfoType.DECADE;
                            break;
                        case "Y":
                            modifyType = MetaInfo.MetaInfoType.YEAR;
                            break;
                        case "Q":
                            modifyType = MetaInfo.MetaInfoType.QUARTER;
                            break;
                        case "M":
                            modifyType = MetaInfo.MetaInfoType.MONTH;
                            break;
                        case "D":
                            modifyType = MetaInfo.MetaInfoType.DAY;
                            break;
                        case "W":
                            modifyType = MetaInfo.MetaInfoType.WEEK;
                            break;
                        case "C":
                            modifyType = MetaInfo.MetaInfoType.CENTURY;
                            break;
                    }
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSPERIOD) != null) {
                    switch (properties.getMetaInfoValue(MetaInfo.MetaInfoType.CONTINUOUSPERIOD)) {
                        case "DE":
                            modifyType = MetaInfo.MetaInfoType.DECADE;
                            break;
                        case "Y":
                            modifyType = MetaInfo.MetaInfoType.YEAR;
                            break;
                        case "Q":
                            modifyType = MetaInfo.MetaInfoType.QUARTER;
                            break;
                        case "M":
                            modifyType = MetaInfo.MetaInfoType.MONTH;
                            break;
                        case "D":
                            modifyType = MetaInfo.MetaInfoType.DAY;
                            break;
                        case "W":
                            modifyType = MetaInfo.MetaInfoType.WEEK;
                            break;
                        case "C":
                            modifyType = MetaInfo.MetaInfoType.CENTURY;
                            break;
                    }
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.SEASON) != null) {
                    modifyType = MetaInfo.MetaInfoType.YEAR;
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.MONTH) != null) {
                    modifyType = MetaInfo.MetaInfoType.YEAR;
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.WEEK) != null) {
                    modifyType = MetaInfo.MetaInfoType.WEEK;
                } else if (properties.getMetaInfoValue(MetaInfo.MetaInfoType.DAYTIME) != null) {
                    modifyType = MetaInfo.MetaInfoType.DAY;
                }
            }

            if (modifyType != null && !modifyValue.equals("X"))
                properties.updateMetaInfo(modifyType, Integer.parseInt(modifySign + modifyValue));
            else if (modifyValue.equals("X") && modifyType != null && modifySign.equals("+"))
                properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIMELINE, "FUTURE_REF");
            else if (modifyValue.equals("X") && modifyType != null && modifySign.equals("-"))
                properties.setMetaInfoValue(MetaInfo.MetaInfoType.TIMELINE, "PAST_REF");

            segment.setMetaInfo(properties);
        }

        return timeSegmentList;
    }

    private List<Integer> identifyTimeToken(List<TaggedToken> taggedTokenList) {
        List<Integer> timeTokenList = new ArrayList<Integer>();

        for (int i = 0; i < taggedTokenList.size(); i++) {
            TaggedToken taggedToken = taggedTokenList.get(i);
            String token = taggedToken.getToken();
            String tag = taggedToken.getTag();

            Set<TokenType> tokenTypeSet = InduceTokenTypeValue.getTokenType(token, tag);
            taggedToken.setTokenTypeSet(tokenTypeSet);

            String value = InduceTokenTypeValue.getTokenValue(taggedToken.getToken(), tokenTypeSet);
            taggedToken.setValue(value);

            if (taggedToken.isHalfDay()) {
                if (token.equals("am") || token.equals("pm")) {
                    boolean isHalfDay = true;
                    if (i == 0 || (!InduceTokenTypeValue.isNumeral(taggedTokenList.get(i - 1)) && !InduceTokenTypeValue.isTime(taggedTokenList.get(i - 1))))
                        isHalfDay = false;

                    if (!isHalfDay)
                        taggedToken.removeTokenType();
                }
            } else if (taggedToken.isTimeZone()) {
                boolean isTimeZone = true;
                if (i == 0 || !taggedTokenList.get(i - 1).isTime() && !taggedTokenList.get(i - 1).isHalfDay() && !taggedTokenList.get(i - 1).isNumeral())
                    isTimeZone = false;

                if (!isTimeZone)
                    taggedToken.removeTokenType();
            } else if (taggedToken.isEra()) {
                boolean isEra = true;
                if (i == 0 || !taggedTokenList.get(i - 1).isNumeral())
                    isEra = false;

                if (!isEra)
                    taggedToken.removeTokenType();
            } else if (taggedToken.isYear()) {
                if (i > 0 && (taggedTokenList.get(i - 1).isInArticle() || taggedTokenList.get(i - 1).getToken().toLowerCase().equals("the")))
                    taggedTokenList.get(i - 1).removeTokenType();
            }

            if (taggedToken.isTimeToken())
                timeTokenList.add(Integer.valueOf(i));
        }

        return timeTokenList;
    }
}

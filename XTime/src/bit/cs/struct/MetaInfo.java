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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import bit.cs.tool.XTimeRegex.TimexType;

public class MetaInfo {
	
	public static enum MetaInfoType{
		CENTURY, DECADE, YEAR, SEASON, QUARTER, MONTH, WEEK, DAY, DATE, TIMELINE, WEEKOFYEAR,
		TIME, DAYTIME, YEARYEAR, ERA, MONTHMONTH,
		CONTINUOUSTIMEUNIT, DISCRETETIMEUNIT, CONTINUOUSPERIOD, NUMBER, NUMBERNUMBER, ORDINAL,
		PERIODICAL, DISCRETEPERIOD
	}
    private String refDate;
    
    private TimexType timexType;
    
    private Map<MetaInfoType, String> metaInfoMap;

	public MetaInfo(String refDate) {
    	this.refDate = refDate;
    	metaInfoMap = new HashMap<MetaInfoType, String>();
    }
	
	public MetaInfo(String refDate, TimexType timexType) {
    	this.refDate = refDate;
    	this.timexType = timexType;
    	metaInfoMap = new HashMap<MetaInfoType, String>();
    }

    public String getRefDate() {
    		return refDate;
    }
    
    public void setRefDate(String refDate) {
        this.refDate = refDate;
    }

    public TimexType getRefType() {
        return timexType;
    }

    public void setTimexType(TimexType timexType) {
        this.timexType = timexType;
    }

    public String getMetaInfoValue(MetaInfoType metaInfoType) {
		if(metaInfoMap.containsKey(metaInfoType))
			return metaInfoMap.get(metaInfoType);
		return null;
    }
    
    public void setMetaInfoValue(MetaInfoType metaInfoType, String metaInfoValue) {
    	metaInfoMap.put(metaInfoType, metaInfoValue);
    }

    public String getTimexValue() throws ParseException {
    	String refDate = getRefDate();
        String refYear=refDate.split("-")[0];
        
        String currentDate = null;
        String currentYear = null;
        
        if(metaInfoMap.containsKey(MetaInfoType.DATE) && metaInfoMap.get(MetaInfoType.DATE) != null && ! metaInfoMap.get(MetaInfoType.DATE).isEmpty()) {
        	currentDate = metaInfoMap.get(MetaInfoType.DATE);
        	currentYear = currentDate.split("-|/")[0];
        }else {
        	if(metaInfoMap.containsKey(MetaInfoType.YEAR))
        		currentYear = metaInfoMap.get(MetaInfoType.YEAR);
        	else
        		currentYear = refYear;
			currentDate=refDate;
        }

        String timexValue = "";

        if(timexType.equals(TimexType.DATE)){
        	if(metaInfoMap.containsKey(MetaInfoType.TIMELINE)) {
        		String temValue = metaInfoMap.get(MetaInfoType.TIMELINE);
        		if(temValue != null && ! temValue.isEmpty())
        			timexValue = temValue;
        		else {
        			timexValue = metaInfoMap.get(MetaInfoType.DATE);
        		}
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.DATE)) {
        		timexValue = metaInfoMap.get(MetaInfoType.DATE);
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.MONTH)) {
				if(metaInfoMap.containsKey(MetaInfoType.YEAR))
					currentYear=metaInfoMap.get(MetaInfoType.YEAR);
				timexValue = currentYear + "-" + metaInfoMap.get(MetaInfoType.MONTH);
				if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSTIMEUNIT)&&metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT).equals("D"))
					timexValue+="-XX";

				if(metaInfoMap.containsKey(MetaInfoType.NUMBERNUMBER)){
					if(metaInfoMap.get(MetaInfoType.NUMBERNUMBER).length()<2)
						timexValue+="-0"+metaInfoMap.get(MetaInfoType.NUMBERNUMBER);
					else
						timexValue+="-"+metaInfoMap.get(MetaInfoType.NUMBERNUMBER);
				}

				if(metaInfoMap.containsKey(MetaInfoType.ERA)){
					String eraValue = metaInfoMap.get(MetaInfoType.ERA).replace(".","").toUpperCase();
					for(int i = 0;i<4-currentYear.length();i++){
						eraValue += "0";
					}
					timexValue=eraValue+timexValue;
				}
			}
			else if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSTIMEUNIT)) {
        		String temValue = metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT);
        		
        		if(temValue.equals("C")) {
        			if(metaInfoMap.containsKey(MetaInfoType.CENTURY))
        				timexValue=String.valueOf(Integer.parseInt(metaInfoMap.get(MetaInfoType.CENTURY))-1);
        			else if(metaInfoMap.containsKey(MetaInfoType.ORDINAL))
        				timexValue=String.valueOf(Integer.parseInt(metaInfoMap.get(MetaInfoType.ORDINAL))-1);
        			else
						timexValue = currentYear.substring(0, 2);
				}
        		else if(temValue.equals("DE")) {
        			if(metaInfoMap.containsKey(MetaInfoType.DECADE))
        				timexValue=metaInfoMap.get(MetaInfoType.DECADE);
					else
						timexValue = currentYear.substring(0, 3);
				}
        		else if(temValue.equals("Y"))
        			timexValue = currentYear;
				else if(temValue.equals("W")){
					if(metaInfoMap.containsKey(MetaInfoType.YEAR)&&metaInfoMap.containsKey(MetaInfoType.WEEKOFYEAR)){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						Calendar calendar = Calendar.getInstance();
						calendar.setTime(sdf.parse(refDate));
						int currentWeek = 1;
						int dctWOY = calendar.get(Calendar.WEEK_OF_YEAR);
						int dctYear = calendar.get(Calendar.YEAR);
						int curWOY = Integer.parseInt(metaInfoMap.get(MetaInfoType.WEEKOFYEAR));
						int curYear = Integer.parseInt(metaInfoMap.get(MetaInfoType.YEAR));
						if((dctWOY>curWOY&&dctYear==curYear)||dctYear>curYear){//prev
							while(calendar.get(Calendar.DAY_OF_WEEK)!=currentWeek||calendar.get(Calendar.WEEK_OF_YEAR)!=curWOY){
								calendar.add(Calendar.DATE,-1);
							}
						}
						else if((dctWOY<curWOY&&dctYear==curYear)||dctYear<curYear){//next
							while(calendar.get(Calendar.DAY_OF_WEEK)!=currentWeek||calendar.get(Calendar.WEEK_OF_YEAR)!=curWOY){
								calendar.add(Calendar.DATE,1);
							}
						}
						int year = calendar.get(Calendar.YEAR);
						int month = calendar.get(Calendar.MONTH) + 1;

//						String dateStr = year + "-";
//						if (month < 10)
//							dateStr += "0" + month;
//						else
//							dateStr += month;
						String dateStr = String.valueOf(year);
						timexValue=dateStr+"-W"+metaInfoMap.get(MetaInfoType.WEEKOFYEAR);

					}
				}
        		else if(temValue.equals("M")  || temValue.equals("WE") || temValue.equals("WXX") || temValue.equals("2W")) {
					timexValue = currentYear + "-" + temValue;
				}
        		else if(temValue.equals("D")) {
//					timexValue = "XXXX-XX-XX";
					timexValue = "P1D";
				}
        		else if(temValue.equals("Q")){
        			if(metaInfoMap.containsKey(MetaInfoType.ORDINAL))
        				timexValue = currentYear+"-"+metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT)+metaInfoMap.get(MetaInfoType.ORDINAL);
        			else if(metaInfoMap.containsKey(MetaInfoType.NUMBER))
						timexValue = currentYear+"-"+metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT)+metaInfoMap.get(MetaInfoType.NUMBER);
				}
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.DISCRETETIMEUNIT)) {
				if(metaInfoMap.containsKey(MetaInfoType.WEEKOFYEAR))
					timexValue = currentYear + "-W" + metaInfoMap.get(MetaInfoType.WEEKOFYEAR)+"-WE";
				else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(sdf.parse(refDate));

					if(!metaInfoMap.containsKey(MetaInfoType.WEEKOFYEAR)) {
						int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
						if (weekOfYear < 10)
							setMetaInfoValue(MetaInfoType.WEEKOFYEAR, "0" + String.valueOf(weekOfYear));
						else
							setMetaInfoValue(MetaInfoType.WEEKOFYEAR, String.valueOf(weekOfYear));
					}
					timexValue = currentYear + "-W"+ metaInfoMap.get(MetaInfoType.WEEKOFYEAR) + "-" + metaInfoMap.get(MetaInfoType.DISCRETETIMEUNIT);
				}
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.WEEK)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(refDate));
				int currentWeek = Integer.parseInt(metaInfoMap.get(MetaInfoType.WEEK))+1;
				if(currentWeek>7)
					currentWeek = 1;

				if(metaInfoMap.containsKey(MetaInfoType.WEEKOFYEAR)) {
					int dctWOY = calendar.get(Calendar.WEEK_OF_YEAR);
					int dctYear = calendar.get(Calendar.YEAR);
					int curWOY = Integer.parseInt(metaInfoMap.get(MetaInfoType.WEEKOFYEAR));
					int curYear = Integer.parseInt(metaInfoMap.get(MetaInfoType.YEAR));
					if((dctWOY>curWOY&&dctYear==curYear)||dctYear>curYear){//prev
						while(calendar.get(Calendar.DAY_OF_WEEK)!=currentWeek||calendar.get(Calendar.WEEK_OF_YEAR)!=curWOY){
							calendar.add(Calendar.DATE,-1);
						}
					}
					else if((dctWOY<curWOY&&dctYear==curYear)||dctYear<curYear){//next
						while(calendar.get(Calendar.DAY_OF_WEEK)!=currentWeek||calendar.get(Calendar.WEEK_OF_YEAR)!=curWOY){
							calendar.add(Calendar.DATE,1);
						}
					}
					else{
						if(currentWeek==1||calendar.get(Calendar.DAY_OF_WEEK)<currentWeek) {
							while (calendar.get(Calendar.DAY_OF_WEEK) != currentWeek)
								calendar.add(Calendar.DATE, 1);
						}
						else{
							while (calendar.get(Calendar.DAY_OF_WEEK) != currentWeek)
								calendar.add(Calendar.DATE, -1);
						}
					}
					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH) + 1;
					int date = calendar.get(Calendar.DATE);

					String dateStr = year + "-";
					if (month < 10)
						dateStr += "0" + month + "-";
					else
						dateStr += month + "-";
					if (date < 10)
						dateStr += "0" + date;
					else
						dateStr += date;
					timexValue = dateStr;// + "-WXX-" + metaInfoMap.get(MetaInfoType.WEEK);
				}
				else {
					int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
					if (weekOfYear < 10)
						setMetaInfoValue(MetaInfoType.WEEKOFYEAR, "0" + String.valueOf(weekOfYear));
					else
						setMetaInfoValue(MetaInfoType.WEEKOFYEAR, String.valueOf(weekOfYear));

					while (calendar.get(Calendar.DAY_OF_WEEK) != currentWeek) {
						if(calendar.get(Calendar.DAY_OF_WEEK) < currentWeek)
							calendar.add(Calendar.DATE, 1);
						else
							calendar.add(Calendar.DATE,-1);
					}

					int year = calendar.get(Calendar.YEAR);
					int month = calendar.get(Calendar.MONTH) + 1;
					int date = calendar.get(Calendar.DATE);

					String dateStr = year + "-";
					if (month < 10)
						dateStr += "0" + month + "-";
					else
						dateStr += month + "-";
					if (date < 10)
						dateStr += "0" + date;
					else
						dateStr += date;

					timexValue = dateStr;// + "-WXX-" + metaInfoMap.get(MetaInfoType.WEEK);
				}
			}
        	else if(metaInfoMap.containsKey(MetaInfoType.QUARTER)) {
        		timexValue = currentYear + "-" + metaInfoMap.get(MetaInfoType.QUARTER);
        		if(metaInfoMap.containsKey(MetaInfoType.ORDINAL))
        			timexValue += metaInfoMap.get(MetaInfoType.ORDINAL);
        		else
        			timexValue += "X";
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.SEASON))
        		timexValue = currentYear + "-" + metaInfoMap.get(MetaInfoType.SEASON);
			else if(metaInfoMap.containsKey(MetaInfoType.ERA)&&metaInfoMap.containsKey(MetaInfoType.YEAR)) {
				String tmpValue = metaInfoMap.get(MetaInfoType.ERA).replace(".","").toUpperCase();
				for(int i = 0;i<4-currentYear.length();i++){
					tmpValue += "0";
				}
				timexValue = tmpValue+currentYear;
			}
			else if(metaInfoMap.containsKey(MetaInfoType.ERA)&&metaInfoMap.containsKey(MetaInfoType.NUMBER)) {
				String tmpValue = metaInfoMap.get(MetaInfoType.ERA).replace(".","").toUpperCase();
				for(int i = 0;i<4-metaInfoMap.get(MetaInfoType.NUMBER).length();i++){
					tmpValue += "0";
				}
				timexValue = tmpValue+metaInfoMap.get(MetaInfoType.NUMBER);
			}
        	else if(metaInfoMap.containsKey(MetaInfoType.YEAR))
        		timexValue = currentYear;
        	else if(metaInfoMap.containsKey(MetaInfoType.DECADE))
        		timexValue = metaInfoMap.get(MetaInfoType.DECADE);
        	else if(metaInfoMap.containsKey(MetaInfoType.CENTURY))
        		timexValue = currentYear.substring(0, 3);

        	else{
				timexValue = currentDate;
			}

        }
		else if(timexType.equals(TimexType.TIME)){
        	if(metaInfoMap.containsKey(MetaInfoType.TIME)) {
        		timexValue = "T" + metaInfoMap.get(MetaInfoType.TIME);
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.DAYTIME)) {
        		String temValue = metaInfoMap.get(MetaInfoType.DAYTIME);
        		if(temValue != null && ! temValue.isEmpty())
        			timexValue = "T" + temValue;
        		else
        			timexValue = "TXX";
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSTIMEUNIT)) {
        		String temValue = metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT);
        		if(temValue == null || temValue.isEmpty())
        			timexValue = "TXX:XX:XX";
        		else if(temValue.startsWith("H"))
        			timexValue = "TXX";
        		else if(temValue.startsWith("M"))
        			timexValue = "TXX:XX";
        	}
        	
        	if(metaInfoMap.containsKey(MetaInfoType.DATE))
        		timexValue = metaInfoMap.get(MetaInfoType.DATE) + timexValue;
        	else if(metaInfoMap.containsKey(MetaInfoType.WEEK)){

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(refDate));
				int currentWeek = Integer.parseInt(metaInfoMap.get(MetaInfoType.WEEK))+1;
				if(currentWeek>7)
					currentWeek = 1;

				if(!metaInfoMap.containsKey(MetaInfoType.WEEKOFYEAR)){
					int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
					if (weekOfYear < 10)
						setMetaInfoValue(MetaInfoType.WEEKOFYEAR, "0" + String.valueOf(weekOfYear));
					else
						setMetaInfoValue(MetaInfoType.WEEKOFYEAR, String.valueOf(weekOfYear));
				}

				while(calendar.get(Calendar.DAY_OF_WEEK)!=currentWeek){
					calendar.add(Calendar.DATE,1);
				}

				int year = calendar.get(Calendar.YEAR);
				int month = calendar.get(Calendar.MONTH) + 1;
				int date = calendar.get(Calendar.DATE);

				String dateStr = year + "-";
				if (month < 10)
					dateStr += "0" + month + "-";
				else
					dateStr += month + "-";
				if (date < 10)
					dateStr += "0" + date;
				else
					dateStr += date;

				timexValue=dateStr+timexValue;//+"-WXX-"+metaInfoMap.get(MetaInfoType.WEEK)+timexValue;

			}
        	else
        		timexValue = getRefDate() + timexValue;
        }
        else if(timexType.equals(TimexType.SET)){
            if(metaInfoMap.containsKey(MetaInfoType.PERIODICAL))
				timexValue = metaInfoMap.get(MetaInfoType.PERIODICAL);
            else if(metaInfoMap.containsKey(MetaInfoType.DISCRETEPERIOD)) {
				timexValue = "XXXX-" + metaInfoMap.get(MetaInfoType.DISCRETEPERIOD);
				if(!metaInfoMap.containsKey(MetaInfoType.SEASON)) {
					if (metaInfoMap.containsKey(MetaInfoType.NUMBER))
						timexValue += metaInfoMap.get(MetaInfoType.NUMBER);
					else if (metaInfoMap.containsKey(MetaInfoType.ORDINAL))
						timexValue += metaInfoMap.get(MetaInfoType.ORDINAL);
				}
			}
			else if(metaInfoMap.containsKey(MetaInfoType.DISCRETETIMEUNIT))
				timexValue = "XXXX-" + metaInfoMap.get(MetaInfoType.DISCRETEPERIOD);
			else if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSTIMEUNIT)) {
				timexValue = "P1" + metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT);
			}
			else if(metaInfoMap.containsKey(MetaInfoType.MONTH))
				timexValue="XXXX-"+metaInfoMap.get(MetaInfoType.MONTH);
			else if(metaInfoMap.containsKey(MetaInfoType.WEEK)&&!metaInfoMap.containsKey(MetaInfoType.DAYTIME))
				timexValue="XXXX-WXX-"+metaInfoMap.get(MetaInfoType.WEEK);
			else if(metaInfoMap.containsKey(MetaInfoType.DAYTIME)&&!metaInfoMap.containsKey(MetaInfoType.WEEK))
				timexValue = "XXXX-XX-XXT" + metaInfoMap.get(MetaInfoType.DAYTIME);
			else if(metaInfoMap.containsKey(MetaInfoType.DAYTIME)&&metaInfoMap.containsKey(MetaInfoType.WEEK))
				timexValue = "XXXX-WXX-"+metaInfoMap.get(MetaInfoType.WEEK)+"T" + metaInfoMap.get(MetaInfoType.DAYTIME);
		}
        else if(timexType.equals(TimexType.DURATION)) {
        	if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSPERIOD)) {
        		timexValue += "P";
        		String temValue = metaInfoMap.get(MetaInfoType.CONTINUOUSPERIOD);
        		if(temValue.startsWith("T"))
        			timexValue += "T";
        	
        		if(metaInfoMap.containsKey(MetaInfoType.NUMBER))
        			timexValue += metaInfoMap.get(MetaInfoType.NUMBER);
        		else
        			timexValue += "X";
        			
        		if(temValue.equals("DE"))
        			timexValue += temValue;
        		else
        			timexValue += temValue.substring(temValue.length() - 1);
        	}
			else if(metaInfoMap.containsKey(MetaInfoType.CONTINUOUSTIMEUNIT)) {
        		timexValue += "P";
        		String temValue = metaInfoMap.get(MetaInfoType.CONTINUOUSTIMEUNIT);
        		if(temValue.startsWith("T"))
        			timexValue += "T";
        		if(metaInfoMap.containsKey(MetaInfoType.NUMBER))
        			timexValue += metaInfoMap.get(MetaInfoType.NUMBER);
        		else
        			timexValue += "X";
				if(temValue.equals("DE"))
					timexValue += temValue;
        		else
        			timexValue += temValue.substring(temValue.length() - 1);
        	}
        		
        }
        
        return timexValue;
    }

    public void updateMetaInfo(MetaInfoType field,int quantity) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(sdf.parse(refDate));

		if(field.equals(MetaInfoType.CENTURY)){
			String curCentury="";
			if (metaInfoMap.containsKey(MetaInfoType.CENTURY))
				curCentury=metaInfoMap.get(MetaInfoType.CENTURY);
			else
				curCentury=refDate.split("-")[0].substring(0,2);
			int newValue=Integer.parseInt(curCentury)+quantity;
			setMetaInfoValue(field,String.valueOf(newValue));
		}
		else if(field.equals(MetaInfoType.DECADE)){
			String curDecade="";
			if (metaInfoMap.containsKey(MetaInfoType.DECADE))
				curDecade=metaInfoMap.get(MetaInfoType.DECADE);
			else
				curDecade=refDate.split("-")[0].substring(0,3);
			int newValue=Integer.parseInt(curDecade)+quantity;
			setMetaInfoValue(field,String.valueOf(newValue));
		}
		else if(field.equals(MetaInfoType.YEAR)){
			String curYear="";
			if (metaInfoMap.containsKey(MetaInfoType.YEAR))
				curYear=metaInfoMap.get(MetaInfoType.YEAR);
			else
				curYear=refDate.split("-")[0];
			int newValue=Integer.parseInt(curYear)+quantity;
			setMetaInfoValue(field,String.valueOf(newValue));
		}
		else if(field.equals(MetaInfoType.QUARTER)){
			String curQuarter="";
			if (metaInfoMap.containsKey(MetaInfoType.ORDINAL))
				curQuarter=metaInfoMap.get(MetaInfoType.ORDINAL);
			else
				curQuarter=String.valueOf((Integer.parseInt(refDate.split("-")[1])-1)/3+1);

			String curYear="";
			if (metaInfoMap.containsKey(MetaInfoType.YEAR))
				curYear=metaInfoMap.get(MetaInfoType.YEAR);
			else
				curYear=refDate.split("-")[0];

			int quarterNum=Integer.parseInt(curQuarter);
			int yearNum=Integer.parseInt(curYear);

			if(quantity<0){
				while(quantity<0) {
					quarterNum--;
					if(quarterNum==0) {
						quarterNum = 4;
						yearNum--;
					}
					quantity++;
				}
			}
			else if(quantity>0){
				while(quantity>0) {
					quarterNum++;
					if(quarterNum==5) {
						quarterNum = 1;
						yearNum++;
					}
					quantity--;
				}
			}
			setMetaInfoValue(MetaInfoType.YEAR,String.valueOf(yearNum));
			setMetaInfoValue(MetaInfoType.ORDINAL,String.valueOf(quarterNum));
		}
		else if(field.equals(MetaInfoType.MONTH)){

			calendar.add(Calendar.MONTH,quantity);

			String newMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
			if(newMonth.length()==1)
				newMonth="0"+newMonth;
			String newYear=String.valueOf(calendar.get(Calendar.YEAR));

			setMetaInfoValue(MetaInfoType.MONTH,newMonth);
			setMetaInfoValue(MetaInfoType.YEAR,newYear);

		}
		else if(field.equals(MetaInfoType.DAY)){

			calendar.add(Calendar.DATE,quantity);

			String newDay=String.valueOf(calendar.get(Calendar.DATE));
			if(newDay.length()==1)
				newDay="0"+newDay;
			String newMonth=String.valueOf(calendar.get(Calendar.MONTH)+1);
			if(newMonth.length()==1)
				newMonth="0"+newMonth;
			String newYear=String.valueOf(calendar.get(Calendar.YEAR));

			setMetaInfoValue(MetaInfoType.DAY,newDay);
			setMetaInfoValue(MetaInfoType.MONTH,newMonth);
			setMetaInfoValue(MetaInfoType.YEAR,newYear);
			setMetaInfoValue(MetaInfoType.DATE,newYear+"-"+newMonth+"-"+newDay);

		}
		else if(field.equals(MetaInfoType.WEEK)){

			calendar.add(Calendar.WEEK_OF_YEAR,quantity);

			String newWOY=String.valueOf(calendar.get(Calendar.WEEK_OF_YEAR));
			if(newWOY.length()==1)
				newWOY="0"+newWOY;

			String newYear=String.valueOf(calendar.get(Calendar.YEAR));

			setMetaInfoValue(MetaInfoType.WEEKOFYEAR,newWOY);
			setMetaInfoValue(MetaInfoType.YEAR,newYear);

		}

	}
}

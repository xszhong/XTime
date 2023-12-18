/******************************************************************************************
 * Copyright (c) 2017-2023 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong (xszhong@bit.edu.cn, zhongxiaoshi@gmail.com),
 * 				  Chenyu Jin (cyjin@bit.edu.cn)
 * ****************************************************************************************/

package bit.cs.examples;

import java.text.ParseException;

import bit.cs.main.XTime;

public class XTimeExample {

	public static final String DATASET = "te3-platinum";
	public static final String INPUT_TML_DIR = "resources/"+DATASET+"/tml/";
	public static final String OUTPUT_TML_DIR = "resources/"+DATASET+"/output-timeml/";

	public static final String FILE="test_136";
	public static final String INPUT_TML_FILE = "resources/"+DATASET+"/tml/"+FILE+".tml";
	public static final String OUTPUT_TML_FILE = "resources/"+DATASET+"/output-timeml/"+FILE+".tml";

	public static final String INPUT_TEXT = "the fourth month of 2018";
	public static final String DATE = "2013-03-21";
	
	public static void main(String[] args) throws ParseException {

		XTime synTime = new XTime();
		
		/**
		 * Input TimeML folder, output TimeML folder
		 * */
//		synTime.extractTimexFromTmlFolder(INPUT_TML_DIR, OUTPUT_TML_DIR);
		
		/**
		 * Input TimeML file, output TimeML file
		 * */
//		synTime.extractTimexFromTmlFile(INPUT_TML_FILE, OUTPUT_TML_FILE);

		/**
		 * Input plain text, output tagged TimeML text
		 * */
		String tmlText = synTime.extractTimexFromText(INPUT_TEXT, DATE);
		System.out.println(tmlText);
	}
}

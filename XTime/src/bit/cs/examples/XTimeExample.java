/******************************************************************************************
 * Copyright (c) 2017-2024 Xiaoshi Zhong
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the GNU lesser Public License v3 which accompanies this distribution,
 * and is available at http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors : Xiaoshi Zhong, xszhong@bit.edu.cn, zhongxiaoshi@gmail.com
 * 				  Chenyu Jin, cyjin@bit.edu.cn
 * ****************************************************************************************/

package bit.cs.examples;

import java.text.ParseException;

import bit.cs.main.XTime;

public class XTimeExample {

	public static final String INPUT_TML_DIR = "resources/te3-platinum/tml/";
	public static final String OUTPUT_TML_DIR = "resources/te3-platinum/output-timeml/";

	public static final String INPUT_TML_FILE = "resources/te3-platinum/tml/AP_20130322.tml";
	public static final String OUTPUT_TML_FILE = "resources/te3-platinum/output-timeml/AP_20130322.tml";

	public static final String INPUT_TEXT = "Zhizhen was born at September 18, 2021 10:27:00";
	public static final String DATE = "2023-12-13";
	
	public static void main(String[] args) throws ParseException {

		XTime xTime = new XTime();
		
		/**
		 * Input TimeML folder, output TimeML folder
		 * */
		xTime.extractTimexFromTmlFolder(INPUT_TML_DIR, OUTPUT_TML_DIR);
		
		/**
		 * Input TimeML file, output TimeML file
		 * */
		xTime.extractTimexFromTmlFile(INPUT_TML_FILE, OUTPUT_TML_FILE);

		/**
		 * Input plain text, output tagged TimeML text
		 * */
		String tmlText = xTime.extractTimexFromText(INPUT_TEXT, DATE);
		System.out.println(tmlText);
	}
}

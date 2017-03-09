package cn.yyx.research.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegularExpression {

	public static void main(String[] args) {
		String regEx = ".+(?<!-copy)\\.java$";
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher("hhacopy.java");
		boolean rs = matcher.matches();
		System.out.println(rs);
	}

}

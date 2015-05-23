package com.usrinfo.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.usrinfo.parsers.ParseCV;

public class Dictionary {

	private static List<String> usernames;
	public static String root = ParseCV.root;
	public final static String NAMES_FILE = "resources/parser/names.txt";

	public static synchronized List<String> getUserDictionary() {
		if (usernames == null) {
			usernames = new ArrayList<>();
			BufferedReader in = null;
			try {
				InputStream resource = new FileInputStream(new File(root,
						NAMES_FILE));
				in = new BufferedReader(
						new InputStreamReader(resource, "utf-8"));
				String str = in.readLine();
				while (str != null) {
					usernames.add(str.trim());
					str = in.readLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (null != in) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return usernames;
	}
}

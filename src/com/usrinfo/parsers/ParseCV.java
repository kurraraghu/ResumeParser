package com.usrinfo.parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.myml.gexp.chunker.Chunker;
import com.myml.gexp.chunker.Chunkers;
import com.myml.gexp.chunker.common.GraphExpChunker;
import com.myml.gexp.graph.matcher.GraphRegExp;
import static com.myml.gexp.chunker.common.GraphExpChunker.mark;
import static com.myml.gexp.chunker.common.GraphExpChunker.match;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.opt;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.or;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.seq;
import com.myml.gexp.chunker.common.typedef.GraphUtils;
import com.usrinfo.data.Dictionary;

/*
 This class represents the Parser class, it will take in an input text, parse it, analyze it and then output
 the generated meaningful sentences as well as hot keywords.
 */
public class ParseCV {
	private String inputFile; // path to input file
	private String[] tokens;
	private ArrayList<String> delimiters; // list of delimiters that are used
											// when extracting text
	private HashMap<Integer, ArrayList<String>> keywords; // list of all
															// potential hot
															// keywords
	private ArrayList<String> keywordsFound; // list of the hotkeywords found in
												// text
	private ArrayList<String> namesFound;
	private ArrayList<String> meaningfulSentences; // generated list of
													// meaningful sentences
	private ArrayList<String> names;
	private List<String> lines; // the input text when broken up into
	// several lines
	private ArrayList<String> verbs; // list of action verbs used to process
										// resumes
	private String firstName;
	private String middleName;
	private String lastName;
	private String emailid;
	private String mobile;
	private String personname;
	public static String root = "/";
	// directories of various system files
	public final static String DELIMITERS_FILE = "resources/parser/delimiters.txt";
	public final static String KEYWORDS_FILE = "resources/parser/keywords.txt";
	public final static String VERBS_FILE = "resources/parser/verbs.txt";
	public final static String NAMES_FILE = "resources/parser/names.txt";

	// list of informative delimiters that are not included in the delimiters
	// file
	public final static String[] infoDelims = { "experience", "proficiency",
			"minimum", "recent", "expertise", "proficient", "strong",
			"excellent", };

	public ParseCV(String inputFile) throws Exception {
		this.inputFile = inputFile;
		this.keywordsFound = new ArrayList<String>();
		this.namesFound = new ArrayList<String>();
		this.meaningfulSentences = new ArrayList<String>();
		initDelimiters();
		initKeywords();
		initVerbs();
		// initNames();
		String paragraphText = getInput();
		lines = split(paragraphText);
		this.tokens = tokenize(paragraphText);
	}

	// initializer for arrayList of verbs, extracts data from the verbs file
	private void initVerbs() {
		verbs = new ArrayList<String>();
		BufferedReader br;
		try {
			InputStream in = new FileInputStream(new File(root, VERBS_FILE));
			br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				verbs.add(line);
			}
		}
		// if the file is not found, the program displays an error message and
		// exits
		catch (IOException e) {
			System.out.println("The file " + VERBS_FILE + " was not found.");
			System.exit(0);
		}
	}

	// initializer for arrayList of delimiters, extracts data from the
	// delimiters file
	private void initDelimiters() {
		delimiters = new ArrayList<String>();
		BufferedReader in;
		try {
			InputStream resource = new FileInputStream(new File(root,
					DELIMITERS_FILE));
			in = new BufferedReader(new InputStreamReader(resource));
			String str = in.readLine();
			while (str != null) {
				delimiters.add(" " + str + " ");
				str = in.readLine();
			}
			delimiters.add(". ");
			delimiters.add("; ");
		}
		// if the file is not found, the program displays an error message and
		// exits
		catch (IOException e) {
			System.out.println("The file " + DELIMITERS_FILE
					+ " was not found.");
			System.exit(0);
		}
	}

	// initializer for arrayList of delimiters, extracts data from the
	// delimiters file
	private void initNames() {
		names = new ArrayList<String>();
		BufferedReader in;
		try {
			InputStream resource = new FileInputStream(new File(root,
					NAMES_FILE));
			in = new BufferedReader(new InputStreamReader(resource));
			String str = in.readLine();
			while (str != null) {
				names.add(" " + str.trim() + " ");
				str = in.readLine();
			}
		}
		// if the file is not found, the program displays an error message and
		// exits
		catch (IOException e) {
			System.out.println("The file " + NAMES_FILE + " was not found.");
			System.exit(0);
		}
	}

	private void initKeywords() {
		keywords = new HashMap<Integer, ArrayList<String>>();
		BufferedReader br;
		try {
			InputStream in = new FileInputStream(new File(root, KEYWORDS_FILE));
			br = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = br.readLine()) != null) {
				int length = line.split(" ").length;
				if (!keywords.containsKey(new Integer(length))) {
					keywords.put(new Integer(length), new ArrayList<String>());
				}
				(keywords.get(new Integer(length))).add(line);
			}
		}
		// if the file is not found, the program displays an error message and
		// exits
		catch (IOException e) {
			System.out.println("The file " + KEYWORDS_FILE + " was not found.");
			System.exit(0);
		}
	}

	// grabs input from the inputFile specified and converts the text into a
	// list of sentences for processing
	public String getInput() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(new File(inputFile)));
			String line = in.readLine();
			String inputText = new String();
			while (line != null) {
				// adds a ; character so that when processed it will be broken
				// up
				inputText += line + "; ";
				line = in.readLine();
			}
			return inputText;
		}
		// if the file is not found, the program displays an error message and
		// exits
		catch (IOException e) {
			System.out.println("The file " + inputFile + " was not found.");
			return null;
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

	// recursively removes trailing punctation or anything that is not a letter
	// or number
	private String cleanUp(String str) {
		if (str.length() == 0) {
			return "";
		}
		String ending = str.substring(str.length() - 1);
		if (ending.matches("^[a-zA-Z0-9_]*$")) {
			return str;
		}
		return cleanUp(str.substring(0, str.length() - 1));
	}

	// used to process the string text originally converted into one massive
	// string
	// this method breaks up the input text into various lines based on the
	// delimiters specified and returns an ArrayList of the resulting lines
	private ArrayList<String> split(String str) {
		ArrayList<String> lines = new ArrayList<String>();
		ArrayList<Integer> splice = new ArrayList<Integer>();
		HashMap<Integer, String> spliceLocs = new HashMap<Integer, String>();
		if (str == null || str.length() == 0) {
			return lines;
		}
		do {
			for (String delim : delimiters) {
				int i = str.indexOf(delim);
				if (i >= 0) {
					splice.add(new Integer(i));
					spliceLocs.put(new Integer(i), delim);
				}
			}
			Collections.sort(splice);
			try {
				lines.add(new String(cleanUp(str.substring(0, splice.get(0)))
						.getBytes("utf-8")));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			str = str.substring(splice.get(0)
					+ spliceLocs.get(splice.get(0)).length());
			splice.clear();
			spliceLocs.clear();
		} while (str.length() > 0);
		return lines;
	}

	// binary search, used to determine if a word is a hot keyword or not, or if
	// a verb is an action verb
	// both the verb file and the keywords file are kept in alphabetical order
	// to maintain binary search capability
	private int search(String s, ArrayList<String> list) {
		return binarySearch_helper(s, list, 0, list.size() - 1);
	}

	// helper function to implement binary search
	private int binarySearch_helper(String s, ArrayList<String> list,
			int start, int end) {
		if (end < start) {
			return -1;
		}
		s = s.toLowerCase();
		int middle = (start + end) / 2;
		if (s.equals(list.get(middle).toLowerCase())) {
			return middle;
		} else if (list.get(middle).toLowerCase().compareTo(s) < 0) {
			return binarySearch_helper(s, list, middle + 1, end);
		} else {
			return binarySearch_helper(s, list, start, middle - 1);
		}
	}

	// analyzes the list of lines and finds hot keywords and well as meaningful
	// sentences

	public void analyzeCandidateDetails() {
		// Pattern emailPattern = Pattern.compile(
		// "\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
		// Pattern.CASE_INSENSITIVE);
		Pattern emailPattern = Pattern.compile(
				"[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}",
				Pattern.CASE_INSENSITIVE);

		boolean dectEmail = false;
		boolean dectMobile = false;
		for (String word : tokens) {
			word = word.trim();
			// let find email id here
			if (null == emailid) {
				Matcher matcher = emailPattern.matcher(word.toUpperCase());
				if (matcher.matches()) {
					emailid = word;
					dectEmail = true;
				}
			}
			for (String phonepattern : phonepatterns) {
				Pattern pattern = Pattern.compile(phonepattern);
				Matcher matcher = pattern.matcher(word);
				while (matcher.find()) {
					mobile = matcher.group();
					dectMobile = true;
					break;
				}
			}

			if (dectMobile && dectEmail) {
				break;
			}
		}

		List<String> userDictionary = Dictionary.getUserDictionary();
		int i = 0;
		StringBuilder blr = new StringBuilder();
		for (String word : tokens) {
			if (userDictionary.contains(word)) {
				blr.append(word);
			}
			if (i == 50) {
				personname = blr.toString();
				break;
			}
			i++;
		}

		analyzeCv();
	}

	public ArrayList<String> getNamesFound() {
		return namesFound;
	}

	public String getPersonname() {
		return personname;
	}

	public final static String[] tokenize(String sentences) {
		// InputStream is = null;
		// TokenizerModel model;
		// try {
		// is = new FileInputStream(new File(root,"resources/en-token.bin"));
		// model = new TokenizerModel(is);
		// Tokenizer tokenizer = new TokenizerME(model);
		// String tokens[] = tokenizer.tokenize(sentences);
		// return tokens;
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// } catch (InvalidFormatException e) {
		// e.printStackTrace();
		// } catch (IOException e) {
		// e.printStackTrace();
		// } finally {
		// try {
		// if (null != is) {
		// is.close();
		// }
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// return null;
		List<String> tokenlist = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(sentences);
		while (st.hasMoreElements()) {
			tokenlist.add(st.nextElement().toString());
		}
		return tokenlist.toArray(new String[tokenlist.size()]);
	}

	public void analyzeCv() {
		for (String str : lines) {
			for (Integer i : new ArrayList<Integer>(keywords.keySet())) {
				if (i == 1) {
					for (String word : str.split(" ")) {
						word = cleanUp(word);

						// this is for sentences
						if (search(word.toLowerCase(), verbs) >= 0) {
							if (!meaningfulSentences.contains(str)) {
								meaningfulSentences.add(str);
							}
						}

						// keywords
						if (search(word.toLowerCase(),
								keywords.get(new Integer(1))) >= 0) {
							if (!keywordsFound.contains(word.toLowerCase())) {
								keywordsFound.add(word.toLowerCase());
							}
							if (!meaningfulSentences.contains(str)
									&& str.split(" ").length > 2) {
								meaningfulSentences.add(str);
							}
						}
					}
				} else {
					for (String word : keywords.get(i)) {
						if (str.toLowerCase().contains(word.toLowerCase())
								&& !keywordsFound.contains(word)) {
							if (!keywordsFound.contains(word.toLowerCase())) {
								keywordsFound.add(word.toLowerCase());
							}
							if (!meaningfulSentences.contains(str)
									&& str.split(" ").length > 2) {
								meaningfulSentences.add(str);
							}
						}
					}
				}
			}
			// for (String delim : infoDelims) {
			// if (str.toLowerCase().contains(delim.toLowerCase())) {
			// if (!meaningfulSentences.contains(str)
			// && str.split(" ").length > 2) {
			// meaningfulSentences.add(str);
			// }
			// }
			// }
			//
			// for(String name : names){
			// if (str.toLowerCase().contains(name.toLowerCase()) &&
			// !namesFound.contains(name)) {
			// namesFound.add(name);
			// }
			// }
		}

		Collections.sort(keywordsFound);
	}

	public String getOutput() {
		StringBuilder blr = new StringBuilder();
		blr.append("Meaningful Sentences: ");
		blr.append("=====================");
		for (String str : meaningfulSentences) {
			blr.append("\n");
			blr.append(reduceSpace(str));
		}
		blr.append("\n\n");
		blr.append("Hot Keywords:");
		blr.append("=============");
		for (String str : keywordsFound) {
			blr.append("\n");
			blr.append(str);
		}

		blr.append("\n\n");
		blr.append("Full Name: ").append(getFirstName()).append(" ")
				.append(getMiddleName()).append(" ").append(getLastName());

		blr.append("\n\n");
		blr.append("Emailid:");
		blr.append(emailid);

		blr.append("\n\n");
		blr.append("mobile:");
		blr.append(mobile);
		return blr.toString();
	}

	// reduces enormous amounts of middle space found in between strings and
	// recursively reduces it into on space
	private String reduceSpace(String s) {
		if (!s.contains("  ")) {
			return s;
		}
		return reduceSpace(s.replace("  ", " "));
	}

	public static void main(String[] args) {
		String inputFile = "C:\\Users\\mthukkaram\\Downloads\\subinjohn-SubinJohn_Resume.doc";
		// String outputFile="E:\\sample.txt";
		// final String input =
		// "/home/customersuccess/Java Developer 2+Exp.docx";
		// final String output = "/home/customersuccess/parsed.txt";
		ParseCV cv;
		try {
			cv = new ParseCV(inputFile);
			cv.analyzeCandidateDetails();
			// String parseCV = cv.getOutput();
			System.out.println(cv.getPersonname());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Set<String> findEmailId(String content) {
		Pattern p = Pattern.compile(
				"\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b",
				Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(content);
		Set<String> emails = new HashSet<String>();
		while (matcher.find()) {
			emails.add(matcher.group());
		}
		return emails;
	}

	static String phonepatterns[] = { "\\d{10}",
			"\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}",
			"\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}",
			"\\(\\d{3}\\)-\\d{3}-\\d{4}", "\\d{3}-\\d{7}" };

	public static String getPhoneNumber(String content) {
		String phone = null;
		for (String phonepattern : phonepatterns) {
			Pattern pattern = Pattern.compile(phonepattern);
			Matcher matcher = pattern.matcher(content);
			while (matcher.find()) {
				phone = matcher.group();
				break;
			}
		}
		return phone;

		/**
		 * 
		 * //validate phone numbers of format "1234567890" if
		 * (phoneNo.matches("\\d{10}")) return true; //validating phone number
		 * with -, . or spaces else
		 * if(phoneNo.matches("\\d{3}[-\\.\\s]\\d{3}[-\\.\\s]\\d{4}")) return
		 * true; //validating phone number with extension length from 3 to 5
		 * else if(phoneNo.matches("\\d{3}-\\d{3}-\\d{4}\\s(x|(ext))\\d{3,5}"))
		 * return true; //validating phone number where area code is in braces
		 * () else if(phoneNo.matches("\\(\\d{3}\\)-\\d{3}-\\d{4}")) return
		 * true; //return false if nothing matches the input else return false;
		 */
	}

	private static Chunker createPersonChunker() {
		GraphRegExp.Matcher Token = match("Token");
		GraphRegExp.Matcher Honorific = GraphUtils.regexp(
				"^(Dr|Prof|Ms|Mrs|Mr)$", Token);
		GraphRegExp.Matcher CapitalizedWord = GraphUtils.regexp("^[A-Z]\\w+$",
				Token);
		GraphRegExp.Matcher CapitalLetter = GraphUtils.regexp("^[A-Z]$", Token);
		GraphRegExp.Matcher FirstName = GraphUtils.regexp("^(Bill|John)$",
				Token);
		GraphRegExp.Matcher Dot = match("Dot");
		GraphRegExp.Matcher Comma = match("Comma");

		GraphRegExp.Matcher commonPart = seq(CapitalizedWord,
				or(CapitalizedWord, seq(CapitalLetter, Dot)));
		Chunker chunker = Chunkers
				.pipeline(
						Chunkers.regexp("Token", "\\w+"),
						Chunkers.regexp("Dot", "\\."),
						Chunkers.regexp("Comma", ","),
						new GraphExpChunker(
								null,
								or(mark("Person",
										or(seq(Honorific, opt(Dot), commonPart),
												seq(FirstName, CapitalizedWord),
												seq(commonPart, Comma, opt(Dot)),
												seq(CapitalizedWord,
														CapitalLetter, Dot,
														CapitalizedWord))),
										seq(mark(
												"Person",
												seq(CapitalizedWord,
														CapitalLetter))))));
		return chunker;
	}

	/**
	 * @return the keywordsFound
	 */
	public final ArrayList<String> getKeywordsFound() {
		return keywordsFound;
	}

	/**
	 * @param keywordsFound
	 *            the keywordsFound to set
	 */
	public final void setKeywordsFound(ArrayList<String> keywordsFound) {
		this.keywordsFound = keywordsFound;
	}

	/**
	 * @return the meaningfulSentences
	 */
	public final ArrayList<String> getMeaningfulSentences() {
		return meaningfulSentences;
	}

	/**
	 * @param meaningfulSentences
	 *            the meaningfulSentences to set
	 */
	public final void setMeaningfulSentences(
			ArrayList<String> meaningfulSentences) {
		this.meaningfulSentences = meaningfulSentences;
	}

	/**
	 * @return the emailid
	 */
	public final String getEmailid() {
		return emailid;
	}

	/**
	 * @param emailid
	 *            the emailid to set
	 */
	public final void setEmailid(String emailid) {
		this.emailid = emailid;
	}

	/**
	 * @return the mobile
	 */
	public final String getMobile() {
		return mobile;
	}

	/**
	 * @param mobile
	 *            the mobile to set
	 */
	public final void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		if (firstName != null) {
			sb.append(firstName);
		}
		if (middleName != null) {
			sb.append(middleName);
		}
		if (lastName != null) {
			sb.append(lastName);
		}
		return sb.toString();
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String[] getTokens() {
		return tokens;
	}

	public void setTokens(String[] tokens) {
		this.tokens = tokens;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

}
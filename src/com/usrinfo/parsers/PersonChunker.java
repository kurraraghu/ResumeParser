package com.usrinfo.parsers;

import static com.myml.gexp.chunker.common.GraphExpChunker.mark;
import static com.myml.gexp.chunker.common.GraphExpChunker.match;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.opt;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.or;
import static com.myml.gexp.graph.matcher.GraphRegExpMatchers.seq;

import java.util.SortedSet;
import java.util.TreeSet;
import com.myml.gexp.chunker.Chunk;
import com.myml.gexp.chunker.Chunker;
import com.myml.gexp.chunker.Chunkers;
import com.myml.gexp.chunker.TextWithChunks;
import com.myml.gexp.chunker.common.GraphExpChunker;
import com.myml.gexp.chunker.common.typedef.GraphUtils;
import com.myml.gexp.graph.matcher.GraphRegExp;

public class PersonChunker {
    public static Chunker createPersonChunker() {
        GraphRegExp.Matcher Token = match("Token");
        GraphRegExp.Matcher Honorific = GraphUtils.regexp("^(Dr|Prof|Ms|Mrs|Mr)$", Token);
        GraphRegExp.Matcher CapitalizedWord = GraphUtils.regexp("^[A-Z]\\w+$", Token);
        GraphRegExp.Matcher CapitalLetter = GraphUtils.regexp("^[A-Z]$", Token);
        GraphRegExp.Matcher FirstName = GraphUtils.regexp("^(Bill|John)$", Token);
        GraphRegExp.Matcher Dot = match("Dot");
        GraphRegExp.Matcher Comma = match("Comma");

        GraphRegExp.Matcher commonPart = seq(CapitalizedWord, or(CapitalizedWord, seq(CapitalLetter, Dot)));
        Chunker chunker = Chunkers.pipeline(
                Chunkers.regexp("Token", "\\w+"),
                Chunkers.regexp("Dot", "\\."),
                Chunkers.regexp("Comma", ","),
                new GraphExpChunker(null,
                        or(
                                mark("Person", or(
                                        seq(Honorific, opt(Dot), commonPart),
                                        seq(FirstName, CapitalizedWord),
                                        seq(commonPart, Comma, opt(Dot)),
                                        seq(CapitalizedWord, CapitalLetter, Dot, CapitalizedWord))
                                ),
                                seq(mark("Person", seq(CapitalizedWord, CapitalLetter)))
                        )
                )
        );
        return chunker;
}
 

/*public static void main(String[] args) {
	 String text = " Mr. Ahuna Edwards Bangalore. " +
             "Devid Kent is second person. " +
             "Devid M. Donald is just another person. " +
             "Bill Smith is person with first name. Jonny K said: I'm happy.";
     Chunker personChunker = createPersonChunker();
     SortedSet<String> result = new TreeSet<String>();
     for (Chunk ch : personChunker.chunk(new TextWithChunks(text))) {
             if (ch.type.equals("Person")) {
                     System.out.println("Person[" + ch.getContent() + "]");
                     result.add(ch.getContent());
             }
     }
   System.out.println(result);
}*/

}

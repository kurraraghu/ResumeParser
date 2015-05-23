package com.usrinfo.parsers;

import com.myml.gexp.chunker.Chunk;
import com.myml.gexp.chunker.Chunker;
import com.myml.gexp.chunker.Chunkers;
import com.myml.gexp.chunker.TextWithChunks;
import com.myml.gexp.chunker.common.GraphExpChunker;
import com.myml.gexp.chunker.common.GraphMatchWrapper;
import com.myml.gexp.chunker.common.typedef.GraphUtils;
import com.myml.gexp.graph.matcher.GraphRegExp;
import com.myml.gexp.graph.matcher.GraphRegExpExt;
import org.junit.Assert;
import org.junit.Test;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static com.myml.gexp.chunker.common.typedef.GraphUtils.*;

/**
 * Author: java developer 1
 * Date: 04.02.2011
 * Time: 17:14:51
 */
public class ExamplesTest{

        /**
         * 1. @Honorific CapitalizedWord CapitalizedWord
         * a. @Honorific is a list of honorific titles such as {Dr., Prof., Mr., Ms., Mrs.
         * etc.)
         * b. Example: Mr. John Edwards
         * <p/>
         * 2. @FirstNames CapitalizedWord
         * a. @FirstNames is a list of common first names collected from sites like the
         * U.S. census and other relevant sites
         * b. Example: Bill Hellman
         * <p/>
         * 3. CapitalizedWord CapitalizedWord [,] @PersonSuffix
         * a. @PersonSuffix is a list of common suffixes such as {Jr., Sr., II, III, etc.}
         * b. Example: Mark Green, Jr.
         * <p/>
         * 4. CapitalizedWord CapitalLetter [.] CapitalizedWord
         * a. CapitalLetter followed by an optional period is a middle initial of a person
         * and a strong indicator that this is a person name
         * b. Example: Nancy M. Goldberg
         * <p/>
         * <p/>
         * 5. CapitalizedWord CapitalLetter @PersonVerbs
         * a. @PersonVerbs is a list of common verbs that are strongly associated with
         * people such as {said, met, walked, etc.}
         */



         
}


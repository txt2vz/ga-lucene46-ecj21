package wordTools;

import org.apache.lucene.index.Term;

/**
 * 
 *Store an ordered list of terms for use by GP functions
 */

public class TermList {

	private Term[] termArray;
	private final static int TERM_ARRAY_MAX_PRINT_SIZE = 256;

	public TermList(Term[] terms) {
		termArray = terms;
	}

	/**
	 * protected method to allow GPs to retrieve terms from the array
	 */
	public Term getTerm(int x) {

		if (termArray == null || termArray.length < 1) {
			System.err.println("error in wordList: empty or null list");
			return null;
		}
		
		if (x >= termArray.length || x < 0) {
			return termArray[0];
		}

		return termArray[x];
	}
	
	public Term[] getTermArray(){
		return termArray;
	}
	
	public int size() {
		return termArray.length;
	}

	public String toString() {		

		if (termArray == null)
			return " term List is null ";

		if (termArray.length < 1)
			return " term array is empty in TermList toString";
		
		StringBuffer report = new StringBuffer(); 
		
		for (int x = 0; x < termArray.length && x < TERM_ARRAY_MAX_PRINT_SIZE; x++) {
			report.append(termArray[x].text() + " ");
		}
		return report.toString() ;
	}
}

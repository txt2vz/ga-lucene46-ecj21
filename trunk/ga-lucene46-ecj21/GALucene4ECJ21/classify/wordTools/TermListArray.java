package wordTools;

import org.apache.lucene.index.Term;


/**
 * Hold an array of wordLists with protected access for GP functions.
 * 
 * @author Laurie
 *
 */


public class TermListArray {
	
	public final static int NUMBER_WORDS_LISTS_TO_PRINT = 20;
   
	private TermList[] arrayOfWordLists;

	public TermListArray(TermList[] wl) {
		arrayOfWordLists = wl;
	}

	/**
	 * protected method to retrieve words from the map
	 */
	public Term getWord(int x, int y) {

		if (arrayOfWordLists == null || arrayOfWordLists.length < 1) {
			System.err.println("error in wordListArray: empty or null");
			return null;
		}
		if (x >= arrayOfWordLists.length || x<0)  {			
			return arrayOfWordLists[0].getTerm(y);
		}		 
		return arrayOfWordLists[x].getTerm(y);
	}
	
	public int getWordListArrayLength(){
		return arrayOfWordLists.length;
	}
	
	public TermList[]  getWordListArray(){
		return arrayOfWordLists;
	}

	public String toString() {
		
		if (arrayOfWordLists == null)
			return " word ListArray is null ";

		if (arrayOfWordLists.length<1)
			return " word ListArray is empty";
		
		String report = new String("wordListArray length: " + arrayOfWordLists.length);
		
		for (int x = 0; x < arrayOfWordLists.length  && x < NUMBER_WORDS_LISTS_TO_PRINT; x++) {
			System.out.println(x + " " + arrayOfWordLists[x].toString());
		}
		
		return report ;
	}
}

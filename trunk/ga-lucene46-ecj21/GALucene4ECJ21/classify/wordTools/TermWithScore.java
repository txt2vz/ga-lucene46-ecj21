package wordTools;

import org.apache.lucene.index.Term;

/**
 * Terms and a score indicating how useful
 * the term is likely to be in a classification task 
 */
public class TermWithScore implements Comparable {
    private final Term term;
    private final double score;

    public int compareTo(Object o) {
        final TermWithScore tws = (TermWithScore) o;

        if (tws.score > score) {
            return 1;
        } else
        if (tws.score < score) {
            return -1;
        } else {
            return 0;
        }
    }

    public TermWithScore(Term t, double d) {
        term = t;
        score = d;
    }
    public double getScore(){
    	return score;
    }
    
    public Term getTerm() {
    	return term;
    }
    
    public String toString(){
    	return term.text() + " " + score;
    }
}

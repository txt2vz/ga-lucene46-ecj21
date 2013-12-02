package wordTools;

/**
 * Ngram strings (normally words or stemmed words)
 * and a score indicating how useful
 * the string is likely to be in a classification task 
 */
public class StringWithScore implements Comparable {
    private final String ngram;
    private final double score;

    public int compareTo(Object o) {
        final StringWithScore ng = (StringWithScore) o;

        if (ng.score > score) {
            return 1;
        } else
        if (ng.score < score) {
            return -1;
        } else {
            return 0;
        }
    }

    public StringWithScore(String ng, double d) {
        ngram = ng;
        score = d;
    }
    public double getScore(){
    	return score;
    }
    
    public String getWord() {
    	return ngram;
    }
    
    public String toString(){
    	return ngram + " " + score;

    }
}

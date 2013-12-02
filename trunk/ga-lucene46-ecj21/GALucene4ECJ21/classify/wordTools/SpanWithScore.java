package wordTools;

import org.apache.lucene.search.spans.SpanNearQuery;

public class SpanWithScore implements Comparable {
	
    private final SpanNearQuery snq;
    private final double score;

    public int compareTo(Object o) {
        final SpanWithScore sws = (SpanWithScore) o;

        if (sws.score > score) {
            return 1;
        } else
        if (sws.score < score) {
            return -1;
        } else {
            return 0;
        }
    }

    public SpanWithScore(SpanNearQuery sng, double d) {
        this.snq = sng;
        score = d;
    }
    public double getScore(){
    	return score;
    }
//    
    public SpanNearQuery getSpan() {
    	return snq;
    }
    
    public String toString(){
    	return snq + " " + score;

    }
}

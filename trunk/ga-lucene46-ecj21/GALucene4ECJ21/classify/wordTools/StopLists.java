package wordTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

/**
 * set path to the stop word set here
 * 
 * @author Laurie
 * 
 */

public class StopLists {

	public static Set<String> textFileToStopList() throws FileNotFoundException {

		final Set<String> stopSet = new TreeSet<String>();

		final Scanner sc = new Scanner(
				new File(
					//	C:\Users\laurie\Java\classifyGA\classifyGA\classify\cfg
					//	"C:\\Users\\laurie\\Java\\classifyGA\\classifyGA\\classify\\cfg\\stop_words_moderate.txt"));
		"C:\\Users\\Laurie\\Java\\classifyGAN\\classify\\cfg\\stop_words_moderate.txt"));

		while (sc.hasNext()) {
			stopSet.add(sc.next());
		}
		return stopSet;
	}
}

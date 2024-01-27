package notryken.chatnotify.util;

import java.util.ArrayList;
import java.util.Iterator;

public class MiscUtil {

    /**
     * Removes all duplicate {@code String}s from the specified
     * {@code ArrayList}.
     * @param originalList the {@code ArrayList} to process.
     */
    public static void removeDuplicates(ArrayList<String> originalList) {
        ArrayList<String> newList = new ArrayList<>();
        Iterator<String> iterPrefixes = originalList.iterator();
        while (iterPrefixes.hasNext()) {
            String prefix = iterPrefixes.next();
            if (newList.contains(prefix)) {
                iterPrefixes.remove();
            }
            else {
                newList.add(prefix);
            }
        }
        originalList.clear();
        originalList.addAll(newList);
    }
}

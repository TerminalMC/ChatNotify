package notryken.chatnotify.util;

import java.util.ArrayList;

public class ListUtil {

    /**
     * Removes all duplicate {@code String}s from the specified
     * {@code ArrayList}.
     * @param list the {@code ArrayList} to process.
     */
    public static void removeDuplicates(ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>();
        for (String str : list) {
            if (!newList.contains(str)) newList.add(str);
        }
        list.clear();
        list.addAll(newList);
    }

    /**
     * Removes all duplicate {@code String}s from the specified
     * {@code ArrayList}, case-insensitive.
     * @param list the {@code ArrayList} to process.
     */
    public static void removeDuplicatesCaseInsensitive(
            ArrayList<String> list) {
        ArrayList<String> newList = new ArrayList<>();
        for (String str0 : list) {
            boolean match = false;
            for (String str1 : newList) {
                if (str1.equalsIgnoreCase(str0)) {
                    match = true;
                    break;
                }
            }
            if (!match) newList.add(str0);
        }
        list.clear();
        list.addAll(newList);
    }
}

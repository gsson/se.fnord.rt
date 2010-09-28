package se.fnord.rt.core.internal.attributes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class LinkParser implements RTAttributeParser<List<Integer>> {

    private static int parseLink(final String link) {
        return Integer.parseInt(link.substring(link.lastIndexOf('/') + 1));
    }

    @Override
    public List<Integer> parse(String value) {
        final String[] split = value.split(",");
        final ArrayList<Integer> links = new ArrayList<Integer>(split.length);
        for (String link : split)
            links.add(parseLink(link.trim()));
        return links;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String dump(Object value) {
        final List<Integer> links = (List<Integer>) value;
        return StringUtils.join(links, ", ");
    }

}

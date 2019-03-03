
package com.hutsko;

import org.jsoup.nodes.Element;

public class PageAnalyzer {
    private static final String FILE_PATH = "https://tech.onliner.by/2019/02/20/muzykant-v-it";
    public static final String GET_ALL_COMMENTS_REQUEST_PATH = "https://comments.api.onliner.by/news/tech.post/414510/comments?last_comment_id=0324e1d0-35f1-11e9-a247-b93a98d4ed90&limit=99999&v=0.06049730447098667";


    public static void main(String[] args) {
    }

    static String uniqueName(Element element) {
        Element raw = element.getElementsByAttribute("href").get(1);
        String link = raw.attr("href");
        int lastSeparatorPosition = link.lastIndexOf("/");
        return link.substring(lastSeparatorPosition + 1);
    }
}

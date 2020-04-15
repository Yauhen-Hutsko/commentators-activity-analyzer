package com.hutsko;

import com.hutsko.util.UrlValidator;

public class ArticleLoaderFactory {
    static UrlValidator validator;

    static {
        validator = new UrlValidator();
    }

    public static ArticleLoader getInstance(String url) {
        ArticleLoader instance = null;
        switch (validator.validate(url)) {
            case ONLINER:
                instance = new OnlinerArticleLoader(url);
                break;
            case TUT:
                throw new UnsupportedOperationException();
            default:
                throw new UnsupportedOperationException();
        }
        return instance;
    }
}

package com.hutsko.util;

import com.hutsko.Sites;

import java.util.UnknownFormatConversionException;

public class UrlValidator {

    public Sites validate(String url) {
        if (url.contains("onliner.by")) {
            return Sites.ONLINER;
        } else if (url.contains("tut.by")) {
            return Sites.TUT;
        } else if (url.contains("dev.by")) {
            return Sites.DEV;
        } else throw new UnknownFormatConversionException(url);
    }
}

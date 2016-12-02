package com.di.nomothesia.enums;

/**
 * Created by psour on 22/11/2016.
 */
public enum YearsEnum {
    Y2006("2006"),
    Y2007("2007"),
    Y2008("2008"),
    Y2009("2009"),
    Y2010("2010"),
    Y2011("2011"),
    Y2012("2012"),
    Y2013("2013"),
    Y2014("2014"),
    Y2015("2015");

    private String year;

    YearsEnum(String year) {
        this.year = year;
    }

    public String getYear() {
        return this.year;
    }
}

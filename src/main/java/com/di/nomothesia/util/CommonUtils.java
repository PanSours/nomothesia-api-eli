package com.di.nomothesia.util;

import com.di.nomothesia.NomothesiaException;
import com.di.nomothesia.config.AppConfig;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * CommonUtils code for various operations.
 * <p>
 * Created by psour on 16/11/2016.
 */
public final class CommonUtils {

    private CommonUtils(){
        //Empty Constructor
    }

    public static String trimDoubleQuotes(String text) {
        int textLength = text.length();
        if (textLength >= 2 && text.charAt(0) == '"' && text.charAt(textLength - 1) == '"') {
            return text.substring(1, textLength - 1);
        }

        return text;
    }

}

//package com.di.nomothesia.util;
//
//import org.springframework.util.StringUtils;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.regex.Pattern;
//
///**
// * Utility class for Url manipulation, in a Servlet environment.
// * Created by psour on 16/11/2016.
// */
//public final class UrlUtil {
//    private static final String[] EMPTY_ARRAY = new String[0];
//    private UrlUtil() {}
//
//    public static String currentUrl(HttpServletRequest request) {
//        return baseUrl(request, EMPTY_ARRAY);
//    }
//
//    private static String baseUrl(HttpServletRequest request, String[] paramsToRemove) {
//        StringBuilder sb = new StringBuilder(request.getServletPath());
//        if(!StringUtils.isEmpty(request.getPathInfo())) {
//            sb.append(request.getPathInfo());
//        }
//        String qs = request.getQueryString();
//        if(!StringUtils.isEmpty(qs)) {
//            sb.append("?").append(stripParams(qs, paramsToRemove));
//        }
//        return sb.toString();
//    }
//
//    private static String stripParams(String queryString, String[] paramsToRemove) {
//        String qs = queryString;
//        for (String param : paramsToRemove) {
//            qs = qs.replaceAll(reqexForParamsRemoval(param), "");
//        }
//        if(qs.startsWith("&")) {
//            qs = qs.substring(1);
//        }
//        return qs;
//    }
//
//    private static String reqexForParamsRemoval(String paramToRemove) {
//        //   "&"+paramToRemove+"(\\=[^&]*)?(?=&|$)|^"+paramToRemove+"(\\=[^&]*)?(?=&|$)"
//        StringBuilder sb = new StringBuilder("&");
//        sb.append(escapeBrackets(paramToRemove)).append("(\\=[^&]*)?(?=&|$)|^")
//                .append(escapeBrackets(paramToRemove)).append("(\\=[^&]*)?(?=&|$)");
//        return sb.toString();
//    }
//
//    private static String escapeBrackets(String paramToRemove) {
//        return paramToRemove.replace("[", Pattern.quote("[")).replace("]", Pattern.quote("]"));
//    }
//}

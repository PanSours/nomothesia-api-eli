<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}"/>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link rel="shortcut icon" href="/resources/images/logo.png" >
        <title><spring:message code="title.aboutus"/></title>

        <!-- Bootstrap -->
        <link rel="stylesheet" href="/resources/css/bootstrap.min.css" type='text/css'>
        <link rel="stylesheet" href="/resources/css/bootstrap-theme.min.css" type='text/css'>
        <link rel='stylesheet' href='/resources/css/jura.css' type='text/css'>
        <link rel='stylesheet' href='/resources/css/comfortaa.css' type='text/css'>

        <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
        <script src="/resources/js/html5shiv.min.js"></script>
        <script src="/resources/js/respond.min.js"></script>
        <![endif]-->

        <!-- Load CSS -->
        <link href="/resources/css/navbar.css" rel="stylesheet"/>
        <link href="/resources/css/aboutus.css" rel="stylesheet"/>
        <!-- <link href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8/themes/base/jquery-ui.css" rel="stylesheet" type="text/css"/> -->
    </head>

    <body>
        <!-- Include Navbar -->
        <%@ include file="/resources/base/navbar.html"%>

        <!-- Page Content -->
        <div class="container">
            <div class="row">
                <div class="col-md-2"></div>
                    <div class="col-md-8">
                        <div class="tabs" id="tabs">
                            <ul style="margin: 0; padding: 0; list-style-type: none; text-align: center;">
                                <li style="display: inline; padding-right: 20px;"><a href="#tab1"><img
                                    src="/resources/images/ilias.jpg" width = "100" length="100" class="img-circle"></a>
                                </li>
                                <li style="display: inline; padding-right: 20px;"><a href="#tab2"><img
                                    src="/resources/images/soursos.jpg" width = "100" length="100" class="img-circle"></a>
                                </li>
                                <li style="display: inline; padding-right: 20px;"><a href="#tab3"><img
                                    src="/resources/images/charnik.jpg" width = "100" length="100" class="img-circle"></a>
                                </li>
                                <li style="display: inline; padding-right: 20px;"><a href="#tab4"><img
                                    src="/resources/images/koubarak.jpg" width = "100" length="100" class="img-circle"></a>
                                </li>
                            </ul>
                            <div id="tab1">
                                <p>
                                    <br/>
                                        <h4><spring:message code="halk.name"/></h4>
                                        <spring:message code="halk.status"/>
                                    <br/>
                                        ihalk[at]di.uoa.gr <br/><a href="http://cgi.di.uoa.gr/~ihalk" target="_blank">http://cgi.di.uoa.gr/~ihalk</a>
                                    <br/>
                                </p>
                                <p style="text-align: justify"><spring:message code="halk.info"/><a href="http://madgik.di.uoa.gr/" target="_blank"> Management of Data and Information Knowledge Group (MaDgiK)</a></p>
                                <p style="text-align: justify"><spring:message code="halk.info2"/></p>
                                <p style="text-align: justify"><spring:message code="halk.info3"/></p>
                            </div>
                            <div id="tab2">
                                <p>
                                    <br/>
                                        <h4><spring:message code="sours.name"/></h4>
                                        <spring:message code="sours.status"/>
                                    <br/>
                                        sourspan[at]gmail.com <br/>&#160;
                                    <br/>
                                </p>
                                <p style="text-align: justify"><spring:message code="sours.info"/></p>
                                <p style="text-align: justify"><spring:message code="sours.info2"/></p><br/>
                            </div>
                            <div id="tab3">
                                <p>
                                    <br/>
                                        <h4><spring:message code="char.name"/></h4>
                                        <spring:message code="char.status"/>
                                    <br/>
                                        charnik[at]di.uoa.gr <br/><a href="http://cgi.di.uoa.gr/~charnik" target="_blank">http://cgi.di.uoa.gr/~charnik</a>
                                    <br/>
                                </p>
                                <p style="text-align: justify"><spring:message code="char.info"/><a href="http://madgik.di.uoa.gr/" target="_blank"> Management of Data and Information Knowledge Group (MaDgiK)</a></p>
                                <p style="text-align: justify"><spring:message code="char.info2"/></p><br/><br/>
                            </div>
                            <div id="tab4">
                                <p>
                                    <br/>
                                        <h4><spring:message code="kouba.name"/></h4>
                                        <spring:message code="kouba.status"/>
                                    <br/>
                                        koubarak[at]di.uoa.gr <br/><a href="http://cgi.di.uoa.gr/~koubarak" target="_blank">http://cgi.di.uoa.gr/~koubarak</a>
                                    <br/>
                                </p>
                                <p style="text-align: justify"><spring:message code="kouba.info"/></p>
                                <p><spring:message code="kouba.info2"/></p>
                                <p style="text-align: justify"><spring:message code="kouba.info3"/></p><br/>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Include Footer -->
        <%@ include file="/resources/base/footer.html"%>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="/resources/js/jquery-3.1.1.min"></script>
        <!-- Include all compiled plugins (below), or include individual files as needed -->
        <script src="/resources/js/bootstrap.min.js"></script>
        <script src="/resources/js/jquery-3.1.1.js"></script>
        <script src="/resources/js/jquery-ui.js"></script>

        <script src="resources/js/aboutus.js"></script>
    </body>
</html>

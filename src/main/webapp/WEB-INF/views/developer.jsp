<%@page import="com.di.nomothesia.model.LegalDocument"%>
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
        <title><spring:message code="title.info"/></title>

        <!-- Bootstrap -->
        <link rel="stylesheet" href="/resources/css/bootstrap.min.css" type='text/css'>
        <link rel="stylesheet" href="/resources/css/bootstrap-theme.min.css" type='text/css'>
        <link rel='stylesheet' href='/resources/css/jura.css' type='text/css'>
        <link rel='stylesheet' href='/resources/css/comfortaa.css' type='text/css'>
        <link rel="stylesheet" href="/resources/css/dataTables.bootstrap.css" type="text/css">

        <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
        <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
        <!--[if lt IE 9]>
        <script src="/resources/js/html5shiv.min.js"></script>
        <script src="/resources/js/respond.min.js"></script>
        <![endif]-->

        <!-- Load CSS -->
        <link href="/resources/css/navbar.css" rel="stylesheet"/>

        <style>
            #footer {
                position:relative;
                bottom: 0;
                width:100%;
                height:60px;   /* Height of the footer */
            }
        </style>
    </head>

    <body>
        <!-- Include Navbar -->
        <%@ include file="/resources/base/navbar.html"%>

        <!-- Tabs -->
        <div class="container">
            <div class="row">
                <div class="col-md-12">
                    <div class="row" style="padding:10px;">
                        <div role="tabpanel">
                            <!-- Nav tabs -->
                            <ul class="nav nav-tabs" role="tablist">
                                <li role="presentation" class="active"><a href="#introduction" aria-controls="home" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="developer.insert"/></a></li>
                                <li role="presentation"><a href="#background" aria-controls="home" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="developer.back"/></a></li>
                                <li role="presentation"><a href="#legislation" aria-controls="profile" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="developer.legr"/></a></li>
                                <li role="presentation"><a href="#uris" aria-controls="profile" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="developer.uri"/></a></li>
                                <li role="presentation"><a href="#restservices" aria-controls="profile" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="developer.rest"/></a></li>
                            </ul>

                            <!-- Tab panes -->
                            <div class="tab-content" style="text-align:justify;">
                                <div role="tabpanel" style="text-align: center;" class="tab-pane fade in active" id="introduction">
                                    <br/>
                                    <iframe src="https://www.slideshare.net/slideshow/embed_code/key/EDBgLLvuH1Hxdb" width="900" height="500" frameborder="0" marginwidth="0" marginheight="0" scrolling="no"></iframe>
                                    <br/>
                                    <br/>
                                </div>
                                <div role="tabpanel" class="tab-pane fade" id="background">
                                    <br/>
                                    <spring:message code="developer.text1"/>
                                </div>
                                <div role="tabpanel" class="tab-pane fade" id="legislation">
                                    <br/>
                                    <spring:message code="developer.text2"/>
                                </div>
                                <div role="tabpanel" class="tab-pane fade" id="uris">
                                    <br/>
                                    <spring:message code="developer.text3"/>
                                </div>
                                <div role="tabpanel" class="tab-pane fade" id="restservices">
                                    <br/>
                                    <spring:message code="developer.text4"/>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Include Footer -->
        <%@ include file="/resources/base/footer.html"%>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="/resources/js/jquery.min.js"></script>
        <!-- Include all compiled plugins (below), or include individual files as needed -->
        <script src="/resources/js/bootstrap.min.js"></script>
    </body>
</html>
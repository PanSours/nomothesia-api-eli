<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
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
    <link rel="shortcut icon" href="./resources/images/logo.png">
    <title><spring:message code="title"/></title>

    <!-- Bootstrap -->
    <link rel="stylesheet" href="./resources/css/bootstrap.min.css" type='text/css'>
    <link rel="stylesheet" href="./resources/css/bootstrap-theme.min.css" type='text/css'>
    <link rel='stylesheet' href='./resources/css/jura.css' type='text/css'>
    <link rel='stylesheet' href='./resources/css/comfortaa.css' type='text/css'>
    <link rel="stylesheet" href="./resources/css/dataTables.bootstrap.css" type="text/css">

    <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
    <script src="./resources/js/html5shiv.min.js"></script>
    <script src="./resources/js/respond.min.js"></script>
    <![endif]-->

    <!-- Load CSS -->
    <link rel="stylesheet" href="./resources/css/navbar.css" type='text/css'>

    <!-- jQueryUI Calendar-->
    <link rel="stylesheet" href="./resources/css/jquery-ui.css" type='text/css'>

    <style>
        #footer {
            position: absolute;
            width: 100%;
            height: 60px; /* Height of the footer */
        }
    </style>
</head>

<body>

    <c:import url="./WEB-INF/views/navbar.html"/>

    <!-- Search Form -->
    <div class="container">
        <div class="row">
            <div class="col-md-6">
                <div class="row" style="padding:10px;">
                    <div class="jumbotron" style="padding: 20px 20px 20px 20px;">
                        <form role="form" method="GET" action="/search">
                            <div class="table-responsive">
                                <table width="100%">
                                    <tr>
                                        <td width="45%">
                                            <div class="form-group">
                                                <label><spring:message code="home.keywords"/></label>
                                                <input type="text" id="keywords" name="keywords" class="form-control" placeholder="<spring:message code="home.placeholder"/>">
                                            </div>
                                        </td>
                                        <td width="5%"></td>
                                        <td width="45%">
                                            <div class="form-group">
                                                <label><spring:message code="home.type"/></label>
                                                <select class="form-control" name="type">
                                                    <option value="">-</option>
                                                    <option value="con"><spring:message code="home.con"/></option>
                                                    <option value="law"><spring:message code="home.law"/></option>
                                                    <option value="pd"><spring:message code="home.pd"/></option>
                                                    <option value="amc"><spring:message code="home.amc"/></option>
                                                    <option value="md"><spring:message code="home.md"/></option>
                                                </select>
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td width="45%">
                                            <div class="form-group">
                                                <label><spring:message code="home.serial"/></label>
                                                <div class="row">
                                                    <div class="col-md-5">
                                                        <input class="form-control" value="" type="number" name="year" min="1976" max="2015" step="1">
                                                    </div>
                                                    <div class="col-md-1" style="font-size: 25px;">/</div>
                                                    <div class="col-md-5">
                                                        <input class="form-control" type="text" name="id" placeholder="N.">
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td width="5%"></td>
                                        <td width="45%">
                                            <div class="form-group">
                                                <label><spring:message code="home.date"/></label>
                                                <div class='input-group date'>
                                                    <input type='text' name="date" id='datepicker' class="form-control" placeholder="<spring:message code="home.dateplaceholder"/>"/>
                                                    <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
                                                </div>
                                            </div>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td style="text-align: right;">
                                            <button type="submit" class="btn btn-primary btn-lg"><spring:message code="navbar.search"/></button>
                                        </td>
                                    </tr>

                                    <tr>
                                        <td></td>
                                        <td></td>
                                        <td style="text-align: right; margin-top: 5px;">
                                            <a href="/search"><spring:message code="home.advancedsearch"/></a>
                                        </td>
                                    </tr>
                                </table>
                            </div>

                            <input type="hidden" name="datefrom" value="" class="form-control"/>
                            <input type="hidden" name="dateto" value="" class="form-control"/>
                            <input type="hidden" name="fek_isuue" value="" class="form-control"/>
                            <input type="hidden" name="fek_year" value="" class="form-control"/>
                            <input type="hidden" name="fek_id" value="" class="form-control"/>
                        </form>
                    </div>
                </div>

                <div class="row" style="padding:10px;">
                    <div role="tabpanel">
                        <!-- Nav tabs -->
                        <ul class="nav nav-tabs" role="tablist">
                            <li role="presentation" class="active"><a href="#home" aria-controls="home" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message
                                    code="home.new"/></a></li>
                            <li role="presentation"><a href="#profile" aria-controls="profile" role="tab" data-toggle="tab" style="font-family: 'Comfortaa', cursive;"><spring:message code="home.most"/></a></li>
                        </ul>

                        <!-- Tab panes -->
                        <div class="tab-content">
                            <div role="tabpanel" class="tab-pane fade in active" id="home">
                                <br/>
                                <div class="table-responsive">
                                    <table id="example" class="table table-striped table-bordered">
                                        <thead>
                                        <tr>
                                            <th><spring:message code="home.title"/></th>
                                            <th><spring:message code="home.code"/></th>
                                            <th><spring:message code="home.datesimple"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="ldr" items="${ldrecent}" varStatus="loop">
                                            <tr>
                                                <td><a href="<c:url value="${ldr.getURI()}"/>"><c:if
                                                        test="${not empty ldr.getTitle()}">${ldr.getTitle()}</c:if><c:if
                                                        test="${empty ldr.getTitle()}"><spring:message
                                                        code="home.${ldr.getDecisionType()}"/> ${ldr.getYear()}/${ldr.getId()} (ΑΓΝΩΣΤΟΣ ΤΙΤΛΟΣ)</c:if></a>
                                                </td>
                                                <td><spring:message code="home.${ldr.getDecisionType()}"/> ${ldr.getYear()}/${ldr.getId()}</td>
                                                <td>${ldr.getPublicationDate()}</td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <div role="tabpanel" class="tab-pane fade" id="profile">
                                <br/>
                                <div class="table-responsive">
                                    <table id="example2" class="table table-striped table-bordered">
                                        <thead>
                                        <tr>
                                            <th><spring:message code="home.title"/></th>
                                            <th><spring:message code="home.code"/></th>
                                            <th><spring:message code="home.datesimple"/></th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach var="ldv" items="${ldviewed}" varStatus="loop">
                                            <tr>
                                                <td><a href="${ldv.getURI()}"/><c:if
                                                        test="${not empty ldv.getTitle()}">${ldv.getTitle()}</c:if><c:if
                                                        test="${empty ldv.getTitle()}"><spring:message
                                                        code="home.${ldv.getDecisionType()}"/> ${ldv.getYear()}/${ldv.getId()} (ΑΓΝΩΣΤΟΣ ΤΙΤΛΟΣ)</c:if>
                                                </td>
                                                <td><spring:message
                                                        code="home.${ldv.getDecisionType()}"/> ${ldv.getYear()}/${ldv.getId()}</td>
                                                <td>${ldv.getPublicationDate()}</td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div style="text-align: justify;padding:10px;">
                    <p><img style="width:55%; margin-left: 2px; margin-bottom: 2px;" align="right"
                            src="./resources/images/greekcloud.png">
                        <spring:message code="home.text"/>
                    <p><spring:message code="home.text2"/></p>
                    <p><spring:message code="home.text3"/></p>
                    </p>
                    <p>
                        <spring:message code="home.text4"/>
                    <ul>
                        <li><a href="http://doc.metalex.eu/" target="_blank">Metalex Document Server</a> <spring:message
                                code="home.text5"/></li>
                        <li><a href="http://www.legislation.gov.uk/" target="_blank">Legislation.gov.uk</a> <spring:message
                                code="home.text6"/></li>
                    </ul>
                    </p>
                    <div align="center" style="padding:10px;">
                        <a class="btn btn-primary btn-lg" href="/legislation.owl" style="width:100%"><span
                                class="glyphicon glyphicon-save" aria-hidden="true"></span> <spring:message
                                code="download.button1"/></a>
                    </div>
                    <div align="center" style="padding:10px;">
                        <a class="btn btn-primary btn-lg" href="/legislation.n3" style="width:100%"><span
                                class="glyphicon glyphicon-save" aria-hidden="true"></span> <spring:message
                                code="download.button2"/></a>
                    </div>
                </div>
            </div>
        </div>
    </div>

<div id="footer" style="text-align: center; font-family:'Jura';">
    <h5><spring:message code="footer"/> - Open Data&#160;&#160; <img src="./resources/images/rdf.png" width="15"/></h5>
</div>

<!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js"></script>
<!-- Include all compiled plugins (below), or include individual files as needed -->
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.2/jquery-ui.js"></script>
<script type="text/javascript" language="javascript"
        src="//cdn.datatables.net/1.10.4/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" language="javascript"
        src="//cdn.datatables.net/plug-ins/3cfcc339e89/integration/bootstrap/3/dataTables.bootstrap.js"></script>


<script type="text/javascript">
    var is_profile_inited = false;
    $(document).on('shown.bs.tab', '[data-toggle="tab"]', function (e) {
        if ($(this).attr("href") == '#profile' && is_profile_inited == false) {
            is_profile_inited = true;
            $("#example2").dataTable({
                "autoWidth": false,
                "scrollY": "500px",
                "scrollCollapse": true,
                "paging": true,
                "iDisplayLength": 6,
                "aaSorting": [],
                "bSortable": true,
                "aoColumnDefs": [
                    {"aTargets": [0], "bSortable": true},
                    {"aTargets": [1], "bSortable": true},
                    {"aTargets": [2], "bSortable": true}],

                <c:set var="localeCode2" value="${pageContext.response.locale}" />
                <c:choose>
                <c:when test="${localeCode2 == 'en' }">
                "bLengthChange": false
                </c:when>
                <c:when test="${localeCode2 == 'el_GR' }">
                "bLengthChange": false,
                "oLanguage": {
                    "sLengthMenu": "Εμφάνισε _MENU_ εγγραφές ",
                    "sZeroRecords": "Δεν βρέθηκε τίποτα",
                    "sInfo": "Εμφανίζει από _START_ μέχρι _END_ των _TOTAL_ εγγραφών",
                    "sInfoEmpty": "Εμφανίζει 0 εγγραφές",
                    "sInfoFiltered": "(φιλτραρισμένες _MAX_ συνολικά εγγραφές)",
                    "sSearch": "Αναζήτηση",
                    "oPaginate": {
                        "sNext": "Επόμενη",
                        "sPrevious": "Προηγούμενη"
                    }

                }
                </c:when>
                </c:choose>
            });
        }
    });
</script>

<script>
    $(function () {
        $("#datepicker").datepicker({
            <c:set var="localeCode2" value="${pageContext.response.locale}" />
            <c:choose>
            <c:when test="${localeCode2 == 'en' }">
            dateFormat: 'yy-mm-dd',
            firstDay: 1,
            maxDate: '0',
            isRTL: false
            </c:when>
            <c:when test="${localeCode2 == 'el_GR' }">
            closeText: 'Κλείσιμο',
            prevText: 'Προηγούμενος',
            nextText: 'Επόμενος',
            currentText: 'Τρέχων Μήνας',
            monthNames: ['Ιανουάριος', 'Φεβρουάριος', 'Μάρτιος', 'Απρίλιος', 'Μάιος', 'Ιούνιος',
                'Ιούλιος', 'Αύγουστος', 'Σεπτέμβριος', 'Οκτώβριος', 'Νοέμβριος', 'Δεκέμβριος'],
            monthNamesShort: ['Ιαν', 'Φεβ', 'Μαρ', 'Απρ', 'Μαι', 'Ιουν',
                'Ιουλ', 'Αυγ', 'Σεπ', 'Οκτ', 'Νοε', 'Δεκ'],
            dayNames: ['Κυριακή', 'Δευτέρα', 'Τρίτη', 'Τετάρτη', 'Πέμπτη', 'Παρασκευή', 'Σάββατο'],
            dayNamesShort: ['Κυρ', 'Δευ', 'Τρι', 'Τετ', 'Πεμ', 'Παρ', 'Σαβ'],
            dayNamesMin: ['Κυ', 'Δε', 'Τρ', 'Τε', 'Πε', 'Πα', 'Σα'],
            dateFormat: 'yy-mm-dd',
            firstDay: 1,
            maxDate: '0',
            isRTL: false
            </c:when>
            </c:choose>
        });
    });
</script>

<script>
    $(document).ready(function () {

        $("#example").dataTable({
            "autoWidth": false,
            "scrollY": "500px",
            "scrollCollapse": true,
            "paging": true,
            "iDisplayLength": 6,
            "aaSorting": [],
            "bSortable": true,
            "aoColumnDefs": [
                {"aTargets": [0], "bSortable": true},
                {"aTargets": [1], "bSortable": true},
                {"aTargets": [2], "bSortable": true}],
            <c:set var="localeCode2" value="${pageContext.response.locale}" />
            <c:choose>
            <c:when test="${localeCode2 == 'en' }">
            "bLengthChange": false
            </c:when>
            <c:when test="${localeCode2 == 'el_GR' }">
            "bLengthChange": false,
            "oLanguage": {
                "sLengthMenu": "Εμφάνισε _MENU_ εγγραφές ",
                "sZeroRecords": "Δεν βρέθηκε τίποτα",
                "sInfo": "Εμφανίζει από _START_ μέχρι _END_ των _TOTAL_ εγγραφών",
                "sInfoEmpty": "Εμφανίζει 0 εγγραφές",
                "sInfoFiltered": "(φιλτραρισμένες _MAX_ συνολικά εγγραφές)",
                "sSearch": "Αναζήτηση",
                "oPaginate": {
                    "sNext": "Επόμενη",
                    "sPrevious": "Προηγούμενη"
                }

            }
            </c:when>
            </c:choose>
        });

    });
</script>

<script>
    $(function () {
        var availableTags = [
            <c:forEach var="tag" items="${tags}" >
            "${tag}",
            </c:forEach>];

        $("#keywords").autocomplete({
            source: availableTags
        });
    });
</script>
</body>
</html>
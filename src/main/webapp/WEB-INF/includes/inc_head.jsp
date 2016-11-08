<!DOCTYPE html>
<html>
<head>
<title>TALYTICA</title>
<meta charset="UTF-8">
<meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, width=device-width"/>
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">

<link rel="shortcut icon" type="image/gif" href="/images/favico.gif">
<link rel="stylesheet" href="https://fonts.googleapis.com/css?family=Roboto">
<link rel="stylesheet" type='text/css' href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap.min.css">
<link rel="stylesheet" type='text/css' href="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/css/bootstrap-theme.min.css">
<link rel='stylesheet' type='text/css' href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.6.1/css/font-awesome.min.css">
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.11/css/dataTables.bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/responsive/2.0.2/css/responsive.bootstrap.min.css"/>
<link rel="stylesheet" type='text/css' href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-daterangepicker/2.1.19/daterangepicker.min.css">
<link rel='stylesheet' type='text/css' href='/css/custom.css' media='all' />
<link rel='stylesheet' type='text/css' href='/css/admin_style.css' media='all' />

<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.3/jquery.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.6/js/bootstrap.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/PapaParse/4.1.2/papaparse.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/easy-pie-chart/2.1.6/jquery.easypiechart.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.12.0/moment.min.js"></script>
<script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?&key=AIzaSyA2bSTr1nfJEneqGPFjpJTASqy8P7cVyrc&libraries=places"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/geocomplete/1.7.0/jquery.geocomplete.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.3.0/Chart.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.11/js/jquery.dataTables.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/1.10.11/js/dataTables.bootstrap.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/responsive/2.0.2/js/dataTables.responsive.min.js"></script>
<script type="text/javascript" src="https://cdn.datatables.net/responsive/2.0.2/js/responsive.bootstrap.min.js"></script>
<script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-daterangepicker/2.1.19/daterangepicker.min.js"></script>
<script type="text/javascript" src='/js/stub_scripts.js'></script>
<script type="text/javascript" src='/js/custom.js'></script>
<script type="text/javascript" src='/js/admin_scripts.js'></script>
</head>

<body class="nav-md">
  <div class="container body">
    <div class="main_container">
      <div class="col-md-3 left_col">
	    <div class="left_col scroll-view">
          <div class="navbar nav_title" style="border: 0;">
            <a href="/index.jsp" class="site_title"><span><img height="55px" width="auto" src="/images/tal-logo-sm.png" alt="TALYTICA"></span></a>
          </div>
          <div class="clearfix"></div>
          <br>
          <!-- sidebar menu -->
          <div id="sidebar-menu" class="main_menu_side hidden-print main_menu">
            <div class="menu_section">
              <ul class="nav side-menu">
                <li class="active"><a href="/index.jsp"><i class="fa fa-tachometer"></i> Dashboard</a>
                </li>
                <li><a><i class="fa fa-users"></i> Candidates <span class="fa fa-chevron-down"></span></a>
                  <ul class="nav child_menu" style="display: block;">
                    <li><a href="/invite_applicant.jsp">Send Assessment</a></li>
                    <li><a href="/completed_applications.jsp">Candidate Search</a></li>
                    <li><a href="/respondant_score.jsp">Candidate Results</a></li>                    
                  </ul>
                <li><a href="/positions.jsp"><i class="fa fa-briefcase"></i> Positions</a></li>
                  <li><a href="/assessments.jsp"><i class="fa fa-edit"></i> Assessments</a>
                  </li>
                  <li><a><i class="fa fa-gears"></i> Administration <span class="fa fa-chevron-down"></span></a>
                    <ul class="nav child_menu" style="display: none;">
						<li><a href="/data_admin.jsp">User Administration</a></li>
						<li><a href="/data_admin.jsp">Payroll Administration</a></li>
						<li><a href="/survey_admin.jsp">Survey Administration</a></li>
						<li><a href="/data_admin.jsp">My Account</a></li>
                    </ul>
                  </li>
                </ul>
              </div>
            </div>
            <!-- /sidebar menu -->
          </div>
      </div>
      <div class="top_nav">
        <div class="nav_menu">
          <nav class="" role="navigation">
            <div class="nav toggle"><a id="menu_toggle"><i class="fa fa-bars"></i></a></div>
            <ul class="nav navbar-nav navbar-right">
              <li class="">
                <a href="javascript:;" class="user-profile dropdown-toggle" data-toggle="dropdown" aria-expanded="false"><span id="user_fname">John Doe</span> <span class=" fa fa-angle-down"></span></a>
                <ul class="dropdown-menu dropdown-usermenu animated fadeInDown pull-right">
                  <li><a href="/data_admin.jsp">Settings</a></li>
                  <li><a href="#">Help</a></li>
                  <li><a href="#" onclick='logout();'><i class="fa fa-sign-out pull-right"></i> Log Out</a></li>
                </ul>
              </li>
            </ul>
          </nav>
        </div>
      </div>
      <!-- /top navigation -->
       <div class="right_col" role="main">
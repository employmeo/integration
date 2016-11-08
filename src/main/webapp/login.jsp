<html>
<head>
<title>Employmeo | Login</title>
<meta charset="UTF-8">
<meta name="viewport" content="initial-scale=1.0, maximum-scale=1.0, width=device-width"/>
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black-translucent">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1">
<link rel="shortcut icon" type="image/gif" href="/images/favico.gif">
<link rel="stylesheet" type='text/css' href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css">
<link rel="stylesheet" type='text/css' href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap-theme.min.css">
<link rel='stylesheet' type='text/css' href="https://maxcdn.bootstrapcdn.com/font-awesome/4.5.0/css/font-awesome.min.css">
<link rel='stylesheet' type='text/css' href='/css/admin_style.css' media='all' />
<link rel='stylesheet' type='text/css' href='/css/custom.css' media='all' />
<script type="text/javascript" src="https://code.jquery.com/jquery-1.12.1.min.js"></script>
<script type="text/javascript" src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min.js"></script>
<script type="text/javascript" src='/js/stub_scripts.js'></script>
<script type="text/javascript" src='/js/admin_scripts.js'></script>
<script type="text/javascript" src='/js/custom.js'></script>
</head>
<body class="coverpage" style="background-image:url('/images/background-<%=new java.util.Random().nextInt(8)+1%>.jpg');">
<div class="container-fluid">
    <div id="wrapper">
<div class="container" style="background: black;color: white;opacity: .85;">
	<div id='wait' class="hidden text-center" style='position:absolute;width:100%;position: absolute;background: black;opacity: .75;padding: 35%;height: 100%;z-index: 99;'>
		<i class="fa fa-spinner fa-5x fa-spin"></i>
	</div>
	<div class="col-xs-12 text-center"><img src="/images/emp-logo-sm.png" style="width:65%;"></div>
	<div class="col-xs-12 text-center"><hr></div>
    <div id='logindiv' class="col-xs-12 text-center">
		  <form name="login" method="post" action="javascript:login();" id="loginform">
            <h1>Sign In</h1>
			<div class="clearfix"><span class='text-danger h4' id='loginresponse'></span></div>
			<div class="clearfix" style="height: 15px;"></div>
			<div><input class="form-control" type="email" name="email" required placeholder="Email Address"></div>
			<div class="clearfix" style="height: 15px;"></div>
			<div><input class="form-control" type="password" name="password" placeholder="Password" required></div>
			<div class="clearfix" style="height: 15px;"></div>
            <div>
              <div class="col-xs-12 col-sm-6 col-md-6"><input type="checkbox" id="rememberme" name="rememberme" value="true" checked><label for="rememberme">Stay Logged In</label></div>
              <div class="col-xs-12 col-sm-6 col-md-6"><button class="btn btn-info btn-block submit" type="submit">Sign In</button></div>
            </div>
			<div class="clearfix" style="height: 15px;"></div>
			<div class="col-xs-12 text-center"><hr></div>
            <div class="col-xs-12 text-center"><p class="change_link">Forgot Password?
                <a href="#" onClick="$('#logindiv').toggleClass('hidden');$('#forgotdiv').toggleClass('hidden');"> Request Reset </a></p>
            </div>
			<input id="toPage" type="hidden" value="/index.jsp">
          </form>
        </div>
        <div id='forgotdiv' class="col-xs-12 text-center hidden">
		  <form name="forgotpass" method="post" action="javascript:forgotPass();" id="forgotpassform">
            <h1>Forgot Password</h1>
            <div class="col-xs-12 text-center"><p class="change_link">Enter your email address and click the "Request Password Reset" button below.</p></div>
			<div class="clearfix" style="height: 15px;"></div>
			<div><input class="form-control" type="email" name="email" required placeholder="Email Address"></div>
			<div class="clearfix" style="height: 15px;"></div>
            <div>
              <div class="col-xs-12"><button class="btn btn-info submit" type="submit">Request Password Reset</button></div>
            </div>
			<div class="clearfix" style="height: 15px;"></div>
          </form>
			<div class="col-xs-12 text-center"><hr></div>
            <div class="col-xs-12 text-center"><p id='emailtoyou' class="change_link">An email will be sent to you, with instructions to reset your password.</p>           
                <a href="#" onClick="$('#logindiv').toggleClass('hidden');$('#forgotdiv').toggleClass('hidden');"> Return to Login Screen </a>
            </div>
        </div>

      </div>
  </div>
</div>
</body>
<script>
    $(document).ready(function() {
    	if (document.URL.indexOf('login.jsp') == -1) {
        	$('#toPage').val(document.URL);    		
    	} else {
        	$('#toPage').val('index.jsp');    		    		
    	}
    });
</script>
</html>
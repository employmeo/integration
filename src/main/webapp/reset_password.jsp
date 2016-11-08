<html>
<head>
<title>Employmeo | Reset Password</title>
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
	<div class="col-xs-12 text-center"><img src="/images/emp-logo-sm.png" style="width:65%;"></div>
	<div class="col-xs-12 text-center"><hr></div>
    <div class="col-xs-12 text-center">
		  <form name="resetpassform" method="post" action="javascript:resetPassword();" id='resetpassform'>
            <h1>Password Reset</h1>
			<div class="clearfix" style="height: 15px;"></div>
            <div class="col-xs-12 text-center"><p class="change_link">Please choose a new password, and enter it in both fields below.</p></div>
			<div class="clearfix" style="height: 15px;"></div>
			<div><input class="form-control" type="password" id='newpass' name="newpass" placeholder="New Password" required></div>
			<div class="clearfix" style="height: 15px;"></div>
			<div><input class="form-control" type="password" id='repeatpass' name="repeatpass" placeholder="Repeat Password" required></div>
			<div class="clearfix" style="height: 15px;"></div>
            <div>
              <div class="col-xs-12"><button class="btn btn-default submit" type="submit">Change Password</button></div>
            </div>
			<div class="clearfix" style="height: 15px;"></div>
            <div class="col-xs-12 text-center"><p id='errormsg' class="text-danger"></p></div>
          </form>
        </div>

      </div>
     </div>
    </div>
</body>

<script>
	var urlParams;
	(window.onpopstate = function() {
		var match, pl = /\+/g, // Regex for replacing addition symbol with a space
		search = /([^&=]+)=?([^&]*)/g, decode = function(s) {
			return decodeURIComponent(s.replace(pl, " "));
		}, query = window.location.search.substring(1);

		urlParams = {};
		while (match = search.exec(query))
			urlParams[decode(match[1])] = decode(match[2]);
	})();
	var email = urlParams.user;
	var hash = urlParams.hash;
	
	$(document).ready(function() {
	    $('input[name=repeatpass]').keyup(function () {
	        'use strict';

	        if ($('input[name=newpass]').val() === $(this).val()) {
	            $(this)[0].setCustomValidity('');
	        } else {
	            $(this)[0].setCustomValidity('Passwords must match');
	        }
	    });
	});
	
</script>
</html>
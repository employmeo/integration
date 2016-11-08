//Useful Global Variables
var tenureChart;
var profileChart;
var respondantProfile;
var positionProfile;
var historyChart;
var dashApplicants;
var dashHires;

var respondant;
var surveyList;
var positionList;
var locationList;
var qTable;
var detailedScores;

Chart.defaults.global.defaultFontColor = '#000';
Chart.defaults.global.defaultFontFamily = '"Helvetica Neue", Roboto, Arial, "Droid Sans", sans-serif';

//basic user / account functions (login/logout/etc)
function login() {
	$.ajax({
		type: "POST",
		async: true,
		data : $('#loginform').serialize(),
		url: "/admin/login",
		xhrFields: {
			withCredentials: true
		},
		beforeSend : function() {
			$("#wait").removeClass('hidden');			
			$('#loginresponse').text('');
		},
		success: function(data) {
			var startPage = $('#toPage').val();
			if (startPage != null) window.location.assign(startPage);
		},
		statusCode: {
		      401: function(){
					$('#loginresponse').text('Login failed');
					$("#wait").addClass('hidden');
		      }
		},
		error: function(data) {
			$('#loginresponse').text('Login failed');
			$("#wait").addClass('hidden');
		}		
	});	
}

function logout() {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/logout",
		xhrFields: {
			withCredentials: true
		},
		success: function(data) {
			window.location.assign('/login.jsp');
		}
	});	
}

function forgotPass() {
	$.ajax({
		type: "POST",
		async: true,
		data : $('#forgotpassform').serialize(),
		url: "/admin/forgotpassword",
		success: function(data) {
			// disable forms
			$('#forgotpassform :submit').text('Request Sent');
			$('#forgotpassform :input').prop('disabled', true);
			$('#emailtoyou').text('An password reset request has been submitted. Please check your email for instructions to reset your password.');	
		},
		error: function(data) {
			console.log(data);			
		}
	});	
}

function resetPassword() {
	$.ajax({
		type: "POST",
		url: "/admin/changepass",
		async: true,
	    headers: { 
	        'Accept': 'application/json',
	        'Content-Type': 'application/json' 
	    },
	    dataType: 'json',
		data : JSON.stringify({
			'email' : email,
			'hash' : hash,
			'newpass' : $('input[name=newpass]').val()
		}),
		success: function(data) {
			if (data.user_fname != null) {
				// drop a cookie
				document.cookie = "user_fname=" + data.user_fname;
				window.location.assign('/index.jsp');
			} else {
				$('#errormsg').text('Unable to change your password. Please request another password reset.');
			}
		},
		error: function(data) {
			console.log(data);			
		}
	});	
}

function getUserFname() {
	var name = "user_fname=";
	var ca = document.cookie.split(';');
	for(var i=0; i<ca.length; i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1);
		if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
	}
	return "";
}

//section for updating selectors
function updatePositionsSelect(detail) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getpositions",
		success: function(data)
		{
			positionList = data;
			$.each(data, function (index, value) {
				$('#position_id').append($('<option/>', { 
					value: this.position_id,
					text : this.position_name 
				}));
			});
			if (detail) changePositionTo($('#position_id').val());
		}
	});
}

function updateLocationsSelect(detail) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getlocations",
		success: function(data)
		{
			locationList = data;
			$.each(data, function (index, value) {
				$('#location_id').append($('<option/>', { 
					value: this.location_id,
					text : this.location_name 
				}));
			});
//			if (detail) changeLocationTo($('#location_id').val());
		}
	});
}

function updateSurveysSelect(detail) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getassessments",
		success: function(data)
		{
			surveyList = data;
			$.each(data, function (index, value) {
				$('#asid').append($('<option />', { 
					value: this.survey_asid,
					text : this.survey_name
				}));
			});
			if (detail) changeSurveyTo($('#asid').val());
		}
	});
}

function listSurveysSelect(detail) {
	$.ajax({
		type: "GET",
		async: true,
		url: "/survey/list",
		success: function(data)
		{
			surveyList = data;
			$.each(data, function (index, value) {
				$('#survey_id').append($('<option />', { 
					value: this.survey_id,
					text : this.survey_name
				}));
			});
			if (detail) changeSurveyTo($('#survey_id').val());
		}
	});
}

function initializeDatePicker(callback) {

	var cb = function(start, end, label) {
		$('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
		$('#fromdate').val(start.format('YYYY-MM-DD'));
		$('#todate').val(end.format('YYYY-MM-DD'));
		callback();
	}

	var optionSet1 = {
			startDate: moment().subtract(29, 'days'),
			endDate: moment(),
			minDate: '01/01/2012',
			maxDate: moment().format('MM/DD/YYYY'),
			dateLimit: {
				days: 365
			},
			showDropdowns: true,
			showWeekNumbers: true,
			timePicker: false,
			timePickerIncrement: 1,
			timePicker12Hour: true,
			ranges: {
				'This Month': [moment().startOf('month'), moment().endOf('month')],
				'Last Month': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')],
				'Last 30 Days': [moment().subtract(29, 'days'), moment()],
				'Last 90 Days': [moment().subtract(89, 'days'), moment()],
				'Last 180 Days': [moment().subtract(179, 'days'), moment()]
			},
			opens: 'left',
			buttonClasses: ['btn btn-default'],
			applyClass: 'btn-small btn-primary',
			cancelClass: 'btn-small',
			format: 'MM/DD/YYYY',
			separator: ' to ',
			locale: {
				applyLabel: 'Submit',
				cancelLabel: 'Clear',
				fromLabel: 'From',
				toLabel: 'To',
				customRangeLabel: 'Custom',
				daysOfWeek: ['Su', 'Mo', 'Tu', 'We', 'Th', 'Fr', 'Sa'],
				monthNames: ['January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December'],
				firstDay: 1
			}
	};
	$('#reportrange span').html(moment().subtract(89, 'days').format('MMMM D, YYYY') + ' - ' + moment().format('MMMM D, YYYY'));
	$('#fromdate').val(moment().subtract(89, 'days').format('YYYY-MM-DD'));
	$('#todate').val(moment().format('YYYY-MM-DD'));

	$('#reportrange').daterangepicker(optionSet1, cb);

	return;
}


//Section for inviting new applicants
function inviteApplicant() {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/inviteapplicant",
		data: $('#inviteapplicant').serialize(),
		beforeSend: function(data) {
			$("#inviteapplicant :input").prop('readonly', true);
			$("#spinner").removeClass('hidden');
		},
		success: function(data)
		{
			$('#inviteapplicant').trigger('reset');
			$('#invitationform').addClass('hidden');
			$('#invitationsent').removeClass('hidden');
		},
		complete: function(data) {
			$("#inviteapplicant :input").prop('readonly', false);
			$("#spinner").addClass('hidden');
		}
	});
	return false; // so as not to trigger actual action.
}

function resetInvitation() {
	$('#invitationsent').addClass('hidden');
	$('#invitationform').removeClass('hidden');	
}

function exportSurvey() {
	$.ajax({
		type: "GET",
		async: true,
		url: "/survey/definition",
		data: $('#exportsurvey').serialize(),
		beforeSend: function(data) {
			$("#exportsurvey :input").prop('readonly', true);
			$("#spinner").removeClass('hidden');
		},
		success: function(data)
		{
			$('#exportsurvey').trigger('reset');
			$('#exportsurveyform').addClass('hidden');
			$('#surveyexported').removeClass('hidden');
			
			$('#surveydefinition').text(JSON.stringify(data));		
		},
		complete: function(data) {
			$("#exportsurvey :input").prop('readonly', false);
			$("#spinner").addClass('hidden');
		}
	});
	return false; // so as not to trigger actual action.
}

function resetExport() {
	$('#surveyexported').addClass('hidden');
	$('#exportsurveyform').removeClass('hidden');
	$('#surveydefinition').text('');
}

function resetPersistence() {
	$('#surveypersisted').addClass('hidden');
	$('#persistsurveyform').removeClass('hidden');
	$('#persistenceresults').text('');
}

function persistSurvey() {
	$.ajax({
		type: "POST",
		async: true,
		headers: { 
	        'Accept': 'application/json',
	        'Content-Type': 'application/json' 
	    },
	    dataType: 'json',		
		url: "/survey/definition",
		data: $('#inputsurveydefinition').val(),
		beforeSend: function(data) {
			$("#persistsurvey :input").prop('readonly', true);
			$("#spinner").removeClass('hidden');
		},
		success: function(data)
		{
			$('#persistsurvey').trigger('reset');
			$('#persistsurveyform').addClass('hidden');
			$('#surveypersisted').removeClass('hidden');
			console.log(data);
			if(data.message != null) {
				$('#persistenceresults').text(data.message);
			}
		},	
		complete: function(data) {
			$("#persistsurvey :input").prop('readonly', false);
			$("#spinner").addClass('hidden');
		}
	});
	return false; // so as not to trigger actual action.
}

// Corefactor migrations

function exportCorefactors() {
	$.ajax({
		type: "GET",
		async: true,
		url: "/survey/corefactor",
		//data: $('#exportsurvey').serialize(),
		beforeSend: function(data) {
			$("#exportcf :input").prop('readonly', true);
			$("#spinner").removeClass('hidden');
		},
		success: function(data)
		{
			//$('#exportcf').trigger('reset');
			$('#exportcfform').addClass('hidden');
			$('#cfexported').removeClass('hidden');
			
			$('#cfdefinition').text(JSON.stringify(data));		
		},
		complete: function(data) {
			$("#exportcf :input").prop('readonly', false);
			$("#spinner").addClass('hidden');
		}
	});
	return false; // so as not to trigger actual action.
}

function resetCfExport() {
	$('#cfexported').addClass('hidden');
	$('#exportcfform').removeClass('hidden');
	$('#cfdefinition').text('');
}

function resetCfPersistence() {
	$('#cfpersisted').addClass('hidden');
	$('#persistcfform').removeClass('hidden');
	$('#cfpersistenceresults').text('');
}

function persistCorefactors() {
	$.ajax({
		type: "POST",
		async: true,
		headers: { 
	        'Accept': 'application/json',
	        'Content-Type': 'application/json' 
	    },
	    dataType: 'json',		
		url: "/survey/corefactor",
		data: $('#inputcfdefinition').val(),
		beforeSend: function(data) {
			$("#persistcf :input").prop('readonly', true);
			$("#spinner").removeClass('hidden');
		},
		success: function(data)
		{
			$('#persistcf').trigger('reset');
			$('#persistcfform').addClass('hidden');
			$('#cfpersisted').removeClass('hidden');
			console.log(data);
			if(data.message != null) {
				$('#cfpersistenceresults').text(data.message);
			}
		},	
		complete: function(data) {
			$("#persistcf :input").prop('readonly', false);
			$("#spinner").addClass('hidden');
		}
	});
	return false; // so as not to trigger actual action.
}

//Section for search respondants / build respondants table
function initRespondantsTable() {
	var rTable = $('#respondants').DataTable( {
		responsive: true,
		order: [[ 0, 'desc' ]],
		columns: [
		          { className: 'text-center', responsivePriority: 1, title: 'Score', 
		        	  data: 'respondant_profile_icon', 
		        	  render : function ( data, type, row ) {
		        		  return '<div class="profilemini ' + row.respondant_profile_class +
		        		  '"><i class="fa '+ data + '"></i></div>';
		        	  }
		          },
		          { responsivePriority: 2, className: 'text-left', title: 'First Name', data: 'respondant_person_fname'},
		          { responsivePriority: 3, className: 'text-left', title: 'Last Name', data: 'respondant_person_lname'},
		          { responsivePriority: 6, className: 'text-left', title: 'Email', data: 'respondant_person_email'},
		          { responsivePriority: 7, className: 'text-left', title: 'Position', data: 'respondant_position_name'},
		          { responsivePriority: 8, className: 'text-left', title: 'Location', data: 'respondant_location_name'}
		          ]
	});
	$.fn.dataTable.ext.errMode = 'none';
	updateRespondantsTable();
}

function updateRespondantsTable() {

	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getrespondants",
		data: $('#refinequery').serialize(),
		beforeSend: function() {
			$("#waitingmodal").removeClass("hidden");
			rTable = $('#respondants').DataTable();
			rTable.clear();
		},
		success: function(data)
		{
			rTable = $('#respondants').DataTable();
			if (data.length > 0) {
				$('#respondants').dataTable().fnAddData(data);
				rTable.$('tr').click(function (){
					rTable.$('tr.selected').removeClass('selected');
					$(this).addClass('selected');
					var respondant = $('#respondants').dataTable().fnGetData(this);
					showApplicantScoring(respondant);
				});
				rTable.on('click', 'i', function (){
					var respondant = rTable.row($(this).parents('tr')).data();
					window.location.assign('/respondant_score.jsp?&respondant_id='+respondant.respondant_id);
				});
			}
		},
		complete: function() {
			$("#waitingmodal").addClass("hidden");
		}
	});
}

//Section for looking at / manipulating surveys
function changeSurveyTo(asid) {
	$(surveyList).each(function(li) {
		if (asid == this.survey_asid) {
			updateSurveyFields(this);
			updateSurveyQuestions(this);
		}		
	});
}

function updateSurveyFields(survey) {
	console.log(survey);
	$('#assessmentname').text(survey.survey_name);
	$('#assessmenttime').text(msToTime(survey.survey_completion_time));
	$('#assessmentdesc').html(survey.survey_description);
	$('#completionguage').data('easyPieChart').update(100*survey.survey_completion_pct);  
	$('#questiontotal').text(survey.questions.length);
	function msToTime(s) {
		  var ms = s % 1000;
		  s = (s - ms) / 1000;
		  var secs = s % 60;
		  s = (s - secs) / 60;
		  var mins = s % 60;
		  return + mins + ':' + (secs<10 ? '0':'') + secs;
	}	
}

function initSurveyQuestionsTable() {
	qTable = $('#questions').DataTable( {
		responsive: true,
		order: [[0, 'asc'],[ 1, 'asc' ]],
		columns: [{ title: 'Sec', data: 'question_page'},
		          { title: '#', data: 'question_sequence'},
		          { title: 'Question', data: 'question_text'}],
		          columnDefs: [{ responsivePriority: 2, targets: 2},
		                       { responsivePriority: 4, targets: 1},
		                       { responsivePriority: 6, targets: 0}]
	});
}	

function updateSurveyQuestions(survey) {
	if (qTable == null) initSurveyQuestionsTable();
	qTable.clear();
	$('#questions').dataTable().fnAddData(survey.questions);
	qTable.$('tr').click(function (){
		qTable.$('tr.selected').removeClass('selected');
		$(this).addClass('selected');
	});
	return
}

function updateDash() {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/updatedash",
		data: $('#refinequery').serialize(),
		success: function(data)
		{
			$('#invitecount').html(data.totalinvited);
			$('#completedcount').html(data.totalcompleted);
			$('#scoredcount').html(data.totalscored);	
			$('#hiredcount').html(data.totalhired);

			refreshDashApplicants(data.applicantData);
			refreshDashHires(data.hireData);
			refreshProgressBars(data.applicantData, data.hireData);
			updateHistory(getHistoryData());
		}
	});

}


function refreshDashApplicants(dataApplicants) {
	if (dashApplicants != null) dashApplicants.destroy();
	// Build Applicants Widget
	dashApplicants = new Chart($("#dashApplicants").get(0).getContext("2d"), {
		type: 'doughnut',
		data: dataApplicants,
		options: {
			cutoutPercentage : 35,
			responsive : true,
			legend: { display: false }
		}});
}

function refreshDashHires(dataHires) {
	if (dashHires != null) {
		dashHires.destroy();
		dashHires = null;
	}

	// Build Hires Widget
	dashHires = new Chart($("#dashHires").get(0).getContext("2d"), {
		type: 'doughnut', 
		data: dataHires, 
		options: {
			cutoutPercentage : 35,
			responsive : true,
			animation : { onProgress : function (chart){
				var ctx = chart.chartInstance.chart.ctx;
				var total = 0;
				ctx.fillText(total, ctx.width/2 - 20, ctx.width/2, 200);

			}},	
			legend: { display: false }
	}});
}

function refreshProgressBars(dataApplicants, dataHires) {
	var rate;

	rate = Math.round(100*dataHires.datasets[0].data[1] / dataApplicants.datasets[0].data[1]);
	$('#risingstarbar').attr('aria-valuenow',rate);
	$('#risingstarbar').attr('style','width:'+rate+'%;');
	$('#risingstarrate').html(rate + '%');

	rate = Math.round(100*dataHires.datasets[0].data[2] / dataApplicants.datasets[0].data[2]);
	$('#longtimerbar').attr('aria-valuenow',rate);
	$('#longtimerbar').attr('style','width:'+rate+'%;');
	$('#longtimerrate').html(rate + '%');

	rate = Math.round(100*dataHires.datasets[0].data[3] / dataApplicants.datasets[0].data[3]);
	$('#churnerbar').attr('aria-valuenow',rate);
	$('#churnerbar').attr('style','width:'+rate+'%;');
	$('#churnerrate').html(rate + '%');

	rate = Math.round(100*dataHires.datasets[0].data[4] / dataApplicants.datasets[0].data[4]);
	$('#redflagbar').attr('aria-valuenow',rate);
	$('#redflagbar').attr('style','width:'+rate+'%;');
	$('#redflagrate').html(rate + '%');

}

function updateHistory(historyData) {
	var dashHistory = $("#dashHistory").get(0).getContext("2d");
	historyChart = new Chart(dashHistory, {
		type: 'bar', data: historyData,
		options: { 
			bar: {stacked: true},
			scales: { 
				xAxes: [{
					gridLines: {color : "rgba(0, 0, 0, 0)"},
					stacked: true,
					categoryPercentage: 0.5
				}],
				yAxes: [{gridLines: {display: true}, scaleLabel: {fontSize: '18px'}, stacked: true}]
			},
			responsive: true,
			legend: { display: false }
		}
	});
	return;
}

function showApplicantScoring(applicantData) {
	renderAssessmentScore(applicantData.scores);
	refreshPositionTenure(getPositionTenureData()); // use stub code
}

function presentPredictions(dataScores) {
	$('#candidateicon').html('<i class="fa ' + respondant.respondant_profile_icon +'"></i>');
	$('#candidateicon').addClass(respondant.respondant_profile_class);
	$('#compositescore').text(Math.round(respondant.respondant_composite_score));
	$('#candidatename').text(respondant.respondant_person_fname + ' ' + respondant.respondant_person_lname);
	$('#candidateemail').text(respondant.respondant_person_email);
	$('#candidateaddress').text(respondant.respondant_person_address);
	$('#candidateposition').text(respondant.respondant_position_name);
	$('#candidatelocation').text(respondant.respondant_location_name);
	$('#assessmentname').text(respondant.respondant_survey_name);
	$('#assessmentdate').text(respondant.respondant_created_date);
	
	var fulltext = respondant.respondant_person_fname +
	               "'s application is in the top " +
	               Math.round(respondant.respondant_composite_score) +
	               " percentile of applicants to " + 
	               respondant.respondant_location_name + ".";
	$('#fulltextdesc').text(fulltext);
	
	renderAssessmentScore(dataScores.scores);
	var header = $('<h4 />',{'text': 'Probability that ' + respondant.respondant_person_fname + ' ...'});
	$('#predictions').empty();
	$('#predictions').append($('<div />',{'class':'row text-center'}).append(header));

	// now - lets assume 3 max.
	var counter = 0;
	for (var i in respondant.predictions) {
		if (i==3) break;
        counter++;
		addPrediction(respondant.predictions[i]);
		produceHistogram(respondant.predictions[i]);
	}

	for (var i=3; i>counter; i--) {
		var card = $('<div />', { 'class' : 'col-md-4 col-sm-4 col-xs-12 text-center'});
		var preddiv = $('<div />', { 'class' : 'card-dashed text-center'});
	    preddiv.append($('<canvas />', {
	    	'class' : 'chart',
	    	'style' : 'height:100px;width:100%;'
	    }));
		preddiv.append($('<hr />'));
	    preddiv.append($('<h5 />',{'text' : 'Configure an additional prediction'} ));
		preddiv.append($('<hr />'));
	    preddiv.append($('<canvas />', {
	    	'class' : 'chart',
	    	'style' : 'height:auto;width:100%;'
	    }));
        card.append(preddiv);
	    $('#predictions').append(card);
	}
}

function addPrediction(prediction) {
	var card = $('<div />', { 'class' : 'col-md-4 col-sm-4 col-xs-12 text-center'});
	var preddiv = $('<div />', { 'class' : 'card-solid text-center'});
    preddiv.append($('<h5 />',{'text' : prediction.label} ));
    
    var spanid = 'prediction_' + prediction.prediction_id;
    var spanChart = $('<span />', {
    	'class' : 'chart',
    	'id' : spanid,
    	'data-percent' : 0
    }).append($('<span />', {
    	'class' : 'percent',
    	'style' : 'line-height:100px;font-size:30px;'
    }));

    var canvasid = 'histogram_' + prediction.prediction_id;
    var histCanvas = $('<canvas />', {
    	'class' : 'chart',
    	'id' : canvasid,
    	'style' : 'height:auto;width:100%;'
    });
	preddiv.append(spanChart);
	preddiv.append($('<hr />'));
    preddiv.append($('<h5 />',{'text' : 'Compared to other applicants...'} ));
	preddiv.append(histCanvas);
	var comparison = respondant.respondant_person_fname + "'s predictions is better than " +
    	(prediction.prediction_percentile * 100).toFixed(0) +
	    "% of other applicants."
    preddiv.append($('<h5 />',{'text' : comparison} ));
    card.append(preddiv);
    $('#predictions').append(card);
	
	var color;
	switch (Math.floor(4*prediction.prediction_percentile)) {
		case 0:
			color = '#d9534f';
			break;
		case 1:
			color = '#F39C12';
			break;
		case 2:
			color = '#3498DB';
			break;
		case 3:
			color = '#26B99A';
			break;
		default:
			color = 'gray';
			break;		
	}
	
	$('#'+spanid).easyPieChart({
    	easing: 'easeOutBounce',
    	lineWidth: '10',
    	barColor: color,
    	scaleColor: false,
    	size: $('#'+spanid).width(),
    	onStep: function(from, to, percent) { $(this.el).find('.percent').text(Math.round(percent));}
  	});
	$('#'+spanid).data('easyPieChart').update(100*prediction.prediction_score);	
}

function produceHistogram(prediction) {
//	var histdiv = $('<div />', { 'class' : 'col-md-4 col-sm-4 col-xs-12 text-center'});
 //   histdiv.append($('<h5 />',{'text' : prediction.label} ));
    
    var canvasid = 'histogram_' + prediction.prediction_id;
//    var histCanvas = $('<canvas />', {
//    	'class' : 'chart',
//   	'id' : canvasid,
//    	'style' : 'height:auto;width:100%;'
//    });

//	histdiv.append(histCanvas);
//	$('#histograms').append(histdiv);
	var ctx = document.getElementById(canvasid);

	var color;
	switch (Math.floor(4*prediction.prediction_percentile)) {
		case 0:
			color = '#d9534f';
			break;
		case 1:
			color = '#F39C12';
			break;
		case 2:
			color = '#3498DB';
			break;
		case 3:
			color = '#26B99A';
			break;
		default:
			color = 'gray';
			break;		
	}
	
	var mean= getPredictionMean(prediction);
	var stdev = getPredictionStDev(prediction);	
	var labels = new Array();
	var bgColors = new Array();
	var borderColors = new Array();
	var datapoints = new Array();
	
	// Generate labels and data, and highlight person
	for (var i = 0; i<20; i++) {
		var low = mean + ((i-10)*stdev)/3;
		var high = mean + ((i-9)*stdev)/3;
		var label = Math.round(100*low) + "-" + Math.round(100*high) + '%';
		if (i == 0) {
			label = "<" + Math.round(100*high) + '%';
			low = 0;
		}
		if (i == 19) {
			label = Math.round(100*low) + "%+";
			high = 1;		
		}
		labels[i] = label;
		var datapoint = cdf(high,mean,stdev) - cdf(low,mean,stdev);
		datapoints[i] = datapoint;
		if ((prediction.prediction_score >= low) && (prediction.prediction_score < high)) {
			bgColors[i] = color;
		} else {
			bgColors[i] = '#ccc';
		}
	}
	
	var data = {
		    labels: labels,
		    datasets: [
		        {
		            label: "frequency",
		            borderWidth: 1,
		            backgroundColor: bgColors,
		            borderColor: borderColors,
		            data: datapoints
		        }
		    ]
		};
    var options = {
    		    legend: { display : false},
  	  	        scales: {
  	  	            xAxes: [{
  	  	                stacked: false,
  	  	                gridLines: {display:false},
  	  	            	display: true
  	  	            }],
  	  	            yAxes: [{
  	  	                stacked: false,
  	  	                gridLines: {display:false},
  	  	            	display: false
  	  	            }],
  	  	            showScale: false
  	  	        }
    		};
    
	var myBarChart = new Chart(ctx, {
	    type: 'bar',
	    data: data,
	    options: options
	});

}

function changePositionTo(pos_id) {
	$(positionList).each(function() {
		if (pos_id == this.position_id) {
			this.data = getStubDataForRoleBenchmark(); /// replace with REST call or pull from other var

			updatePositionDetails(this);
			updatePositionModelDetails(this.data.role_benchmark);
			updateGradesTable(this.data.role_benchmark.role_grade);
			updateCriticalFactorsChart(this);

		}		
	});
}

function updatePositionDetails(position) {
	$('#positionname').text(position.position_name);
	$('#positiondesc').text(position.position_description);
}

//Payroll tools section
function uploadPayroll(e) {
	$('#csvFile').parse({
		config : {
			header: true,
			dynamicTyping: true,
			complete: function(results, file) {
				$('#payroll').DataTable( {
					responsive: true,
					data: results.data,
					columns : [
					           { title: 'Employee ID', data: 'employee'},
					           { title: 'Raise Rate', data: 'RaiseRate'},
					           { title: 'Total Hours', data: 'Total Hours' },
					           { title: 'Tenure', data: 'Tenure' },
					           { title: 'Monthly Hours', data: 'Monthly Hours' }
					           ]

				});

				console.log("Parsing complete:", results, file);
			}
		},
		before : function(file, inputElem){},
		error: function(err, file, inputElem, reason){},
		complete : {}

	})
}


//Respondant scoring section
function getScore(respondantId) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getscore",
		data: {
			"respondant_id" : respondantId   	
		},
		success: function(data)
		{
			respondant = data.respondant;
			presentRespondantScores(data);
		}
	});    
}

//Respondant scoring section
function getScoreUuid(respondantUuid) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getscore",
		data: {
			"respondant_uuid" : respondantUuid   	
		},
		success: function(data)
		{
			respondant = data.respondant;
			presentRespondantScores(data);
		}
	});    
}

//Respondant scoring section
function getPredictions(respondantId) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getscore",
		data: {
			"respondant_id" : respondantId   	
		},
		success: function(data)
		{
			respondant = data.respondant;
			presentPredictions(data);
		}
	});    
}

//Respondant scoring section
function getPredictionsUuid(respondantUuid) {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getscore",
		data: {
			"respondant_uuid" : respondantUuid   	
		},
		success: function(data)
		{
			respondant = data.respondant;
			presentPredictions(data);
		}
	});    
}

function copyToClipboard(element) {
    var $temp = $("<input>");
    $("body").append($temp);
    $temp.val($(element).text()).select();
    document.execCommand("copy");
    $temp.remove();
}

function presentRespondantScores(dataScores) {
	$('#candidatename').text(respondant.respondant_person_fname + ' ' + respondant.respondant_person_lname);
	$('#candidateemail').text(respondant.respondant_person_email);
	$('#candidateaddress').text(respondant.respondant_person_address);
	$('#candidateposition').text(respondant.respondant_position_name);
	$('#candidatelocation').text(respondant.respondant_location_name);
	$('#assessmentname').text(respondant.respondant_survey_name);
	$('#assessmentdate').text(respondant.respondant_created_date);

	detailedScores = dataScores.detailed_scores;
	renderDetailedAssessmentScore();
}

function showAllDetails() {
	for (i in detailedScores) {
		var score = detailedScores[i];
		showDetail(score.corefactor_id);
	}
	$('#hideall').removeClass('hidden');
	$('#showall').addClass('hidden');
}

function hideAllDetails() {
	for (i in detailedScores) {
		var score = detailedScores[i];
		hideDetail(score.corefactor_id);
	}	
	$('#showall').removeClass('hidden');
	$('#hideall').addClass('hidden');
}

function showDetail(cfid) {
	$('#cfmessage_' + cfid).removeClass('hidden');	
	$('#expander_' + cfid).attr('onclick', 'hideDetail('+cfid+')');
	$('#expander_' + cfid).removeClass('fa-plus-square-o');
	$('#expander_' + cfid).addClass('fa-minus-square-o');
}


function hideDetail(cfid) {
	$('#cfmessage_' + cfid).addClass('hidden');
	$('#expander_' + cfid).attr('onclick', 'showDetail('+cfid+')');
	$('#expander_' + cfid).removeClass('fa-minus-square-o');
	$('#expander_' + cfid).addClass('fa-plus-square-o');
}

function renderDetailedAssessmentScore() {
	$('#assessmentresults').empty();
	detailedScores.sort(function(a, b) {
	    return a.corefactor_display_group.localeCompare(b.corefactor_display_group);
	});
	var displaygroup = "";
	
	for (var i in detailedScores) {
		var score = detailedScores[i];
		if (displaygroup != score.corefactor_display_group) {
			displaygroup = score.corefactor_display_group;
			var grouprow = $('<tr />');
			grouprow.append($('<th />', {'style':'text-align:center;'}).append($('<h4 />',{text:displaygroup})));
			$('#assessmentresults').append(grouprow);
		}
		var row = $('<tr />', {
			'title' : score.corefactor_description});
		var namediv = $('<div />', {
			'class' : 'col-xs-10 col-sm-8 col-md-6 col-lg-6 text-left',
			title: score.corefactor_description});
		var expander = $('<h5 />');
		expander.append($('<strong />', { text : score.corefactor_name + ' '}));
		expander.append($('<i />', {
			'onclick' : "showDetail(" + score.corefactor_id + ")",
			'class' : 'fa fa-plus-square-o',
			'id' : 'expander_' + score.corefactor_id
		}));
		namediv.append(expander);
		var scorediv = $('<div />', {
			'class' : 'col-xs-2 col-sm-4 col-md-6 col-lg-6 text-right', 
			html : '<h5><strong>' + score.cf_score.toFixed(1) + " of " + score.corefactor_high + '</strong></h5>'});
		var lowdesc = $('<div />', {
			'class' : 'hidden-xs col-sm-3 col-md-2 col-lg-2 text-left', 
			html : '<h6><em>' +score.corefactor_low_desc + '</em></h6>'});
		var highdesc = $('<div />', {
			'class' : 'hidden-xs col-sm-3 col-md-2 col-lg-2 text-right', 
			html : '<h6><em>' +score.corefactor_high_desc + '</em></h6>'});
		var progress = $('<div />', {'class' : 'progress col-xs-12 col-sm-6 col-md-8 col-lg-8'
				}).append($('<div />', {
			'class': 'progress-bar progress-bar-success progress-bar-striped',
			'role': 'progressbar',
			'aria-valuenow' : score.cf_score,
			'aria-valuemin' : "1",
			'aria-valuemax' : "11",
			'style' : 'width: ' + (100*score.cf_score/score.corefactor_high) + '%;'
		}));

		var tablecell = $('<td />');
		tablecell.append(namediv);
		tablecell.append(scorediv);
		tablecell.append(lowdesc);
		tablecell.append(progress);
		tablecell.append(highdesc);
		row.append(tablecell);
		$('#assessmentresults').append(row);
		var messageRow = $('<tr />',{
			'id' : 'cfmessage_' + score.corefactor_id,
			'class' : 'hidden'
		}).append($('<td />',{
			'bgcolor' : '#F7F7F7',
			'border-top' : 'none',
			'text' : prepPersonalMessage(score.cf_description)
			}));
		$('#assessmentresults').append(messageRow);
		

	}
	
}

function prepPersonalMessage (message) {
	var pm = message;
	if (pm != null) {
		pm = pm.replace(new RegExp("\\[FNAME\\]","g"),respondant.respondant_person_fname);
		pm = pm.replace(new RegExp("\\[LNAME\\]","g"),respondant.respondant_person_lname);
	
		pm = pm.replace(new RegExp("\\[CHESHE\\]","g"),"This candidate");
		pm = pm.replace(new RegExp("\\[LHESHE\\]","g"),"this candidate");
		pm = pm.replace(new RegExp("\\[CHIMHER\\]","g"),"Him or her");
		pm = pm.replace(new RegExp("\\[LHIMHER\\]","g"),"him or her");
		pm = pm.replace(new RegExp("\\[HIMHER\\]","g"),"him or her");
		pm = pm.replace(new RegExp("\\[CHISHER\\]","g"),"His or her");
		pm = pm.replace(new RegExp("\\[LHISHER\\]","g"),"his or her");
		pm = pm.replace(new RegExp("\\[HIMSELFHERSELF\\]","g"),"him or herself");
	}
	return pm;
}



function renderAssessmentScore(scores) {
	$('#detailslink').prop("href", '/assessment_results.jsp?&respondant_id=' + respondant.respondant_id);
	$('#assessmentresults').empty();
	for (var key in scores) {
		var row = $('<tr />');
		var cell = $('<td />');
		var quartile = Math.floor(4*scores[key]/11);
		
		cell.append($('<div />', {'text': key }));
		cell.append( $('<div />', {'class' : 'progress'}).append($('<div />', {
			'class': 'progress-bar '+getBarClass(quartile)+' progress-bar-striped',
			'role': 'progressbar',
			'aria-valuenow' : scores[key],
			'aria-valuemin' : "1",
			'aria-valuemax' : "11",
			'style' : 'width: ' + scores[key]/0.11 + '%;',
			'text' : scores[key]
		}))); 
		row.append(cell);
		$('#assessmentresults').append(row);
	}

	var footer = $('<tr />');
	var legend = $('<td />', {'style':'background-color:#eee;'});
	legend.append($('<div />', {'class':'text-center', 'text': 'Bar Color Indicates Quartile'}));

	for (var i = 0; i<4; i++) {
		var div  = $('<div />', {'class' : 'col-xs-3 col-sm-3 col-md-3 col-lg 3'});	
		div.append( $('<div />', {'class' : 'progress'}).append($('<div />', {
			'class': 'progress-bar '+getBarClass(i)+' progress-bar-striped',
			'role': 'progressbar',
			'aria-valuenow' : 1,
			'aria-valuemin' : "0",
			'aria-valuemax' : "1",
			'style' : 'width: 100%;',
			'text' : i
		})));
		legend.append(div);
	}
	$('#assessmentresults').append(footer.append(legend));

    function getBarClass(quartile) {
    	var barclass;
		switch (quartile) {
		case 0:
			barclass = 'progress-bar-danger';
			break;
		case 1:
			barclass = 'progress-bar-warning';
			break;
		case 2:
			barclass = 'progress-bar-info';
			break;
		case 3:
			barclass = 'progress-bar-success';
			break;
		default:
			barclass = 'progress-bar-default';
			break;
		}
		return barclass;
	}

}


function updatePositionModelDetails(role_benchmark) {
	document.querySelector('#div_applicant_count').innerHTML = role_benchmark.applicant_count;
	document.querySelector('#div_hire_count').innerHTML = role_benchmark.hire_count;
	document.querySelector('#div_hire_rate').innerHTML = Math.round((role_benchmark.hire_count/role_benchmark.applicant_count)*100)+'%';		
}

function updateGradesTable(arr1) {
	$('#gradetable').empty();
	$('#gradefooter').empty();
	
	var frag = document.createDocumentFragment();
	// measure variables
	var avg0 = 0;
	var avg1 = 0;
			
	for (var i = 0, len = Object.keys(arr1).length; i < len; i++) {
		//summary variables
		avg0 += parseFloat(arr1[i].v0);
		avg1 += parseFloat(arr1[i].v1);
		
		var tr0 = document.createElement("tr");
		var td0 = document.createElement("td");
		var divClass;
		var iconClass;
		
		switch (arr1[i].grade){
			case "A":
				divClass='btn-success';
				iconClass='fa-rocket';
				break;
			case "B":
				divClass='btn-info';
				iconClass='fa-user-plus';
				break;
			case "C":
				divClass='btn-warning';
				iconClass='fa-warning';
				break;
			case "D":
				divClass='btn-danger';
				iconClass='fa-hand-stop-o';
				break;
		}	
		$(td0).append(getProfileBadge(divClass, iconClass));
		tr0.appendChild(td0);		
		
		var td1 = document.createElement("td");
		td1.className="text-center";
		td1.innerHTML = arr1[i].v0;
		
		var td2 = document.createElement("td");
		td2.className="text-center";
		td2.innerHTML = (arr1[i].v1*100).toPrecision(2)+'%';
		
		tr0.appendChild(td1);
		tr0.appendChild(td2);
		frag.appendChild(tr0);	
		
		var el = document.querySelector('#gradetable');
		el.appendChild(frag);
	}
	
	var tr0 = document.createElement("tr");
	var td0 = document.createElement("th");
	td0.innerHTML = "Average";
	var td1 = document.createElement("th");
	td1.className="text-center";
	td1.innerHTML = (avg0/Object.keys(arr1).length).toFixed(1);
	var td2 = document.createElement("th");
	td2.className="text-center";
	td2.innerHTML = (avg1*100/Object.keys(arr1).length).toFixed(1)+'%';
	
	tr0.appendChild(td0);
	tr0.appendChild(td1);
	tr0.appendChild(td2);	
	
	var el = document.querySelector('#gradefooter');
	el.appendChild(tr0);
}

function getProfileBadge(divClass,iconClass) {
	var div = $('<div />', {'class':'profilesquare'}).addClass(divClass);
	var icon = $('<i />', {'class':'fa'}).addClass(iconClass);
	$(div).append(icon);
	return div;
}	

function initCriticalFactorsChart() {
    var ctx = document.querySelector("#criticalfactorschart").getContext("2d");
	var barChartConfig = {
		    type: "bar",
	  	    data: {
	  	  	  labels: ["loading..."],
  	  	  	  
  	  	  	  datasets: [{
  	  	  		label: "Applicants",
  	  	        backgroundColor: 'rgba(200, 200, 200, 0.8)',
  	  	        borderColor: 'rgba(150, 150, 150, 0.8)',
  	  	  		borderWidth: 2,
  	  	  	    data: []
  	  	  	  },
  	  	  	{
  	  	  		label: "Employees",
  	  	  		backgroundColor: 'rgba(0, 200, 0, 0.8)',
  	  	  		borderColor: 'rgba(0, 150, 0, 0.8)',
  	  	  		borderWidth: 2,
  	  	    	data: []
  	  	    	  }
  	  	  	  ]
  	  	  	},
  	  	    options: {
  	  	    	responsive: true,
  	  	        maintainAspectRatio: false,
  	  	        title: {
  	  	        	display: true,
  	  	        	fontSize: 18,
  	  	        	text: 'Critical Factors'
  	  	        },
  	  	        legend: {
  	  	        	position: 'left',
  	  	        	labels: {
  	  	        		boxWidth: 12
  	  	        	}
  	  	        },
  	  	        scales: {
  	  	            xAxes: [{
  	  	                stacked: false
  	  	                ,gridLines: {display:false}
  	  	            	,display: true
  	  	            }],
  	  	            yAxes: [{
  	                    ticks: {
  	                    	min: 0,
  	                    	max: 12,
  	                    	beginAtZero : true
  	                    },
  	  	                stacked: false
  	  	                ,gridLines: {display:false}
  	  	            	,display: false
  	  	            }]
  	  	        ,showScale: false
  	  	        },
  	  	    animation: {
  	    	  	duration: 500,
  	    	  	onComplete: function () {
  	    	  	    // render the value of the chart above the bar
  	    	  	    var ctx = this.chart.ctx;
  	    	  	    ctx.font = Chart.helpers.fontString(Chart.defaults.global.defaultFontSize, 'normal', Chart.defaults.global.defaultFontFamily);
  	    	  	    ctx.fillStyle = this.chart.config.options.defaultFontColor;
  	    	  	    ctx.textAlign = 'center';
  	    	  	    ctx.textBaseline = 'bottom';
  	    	  	    var fontVar = 'normal 16px "Helvetica Neue", Roboto, Arial'
    	    	  	if (this.chart.width < 600) fontVar = 'bold 14px "Helvetica Neue", Roboto, Arial';

  	    	  	    this.data.datasets.forEach(function (dataset) {
  	    	  	    	
  	    	  	        for (var i = 0; i < dataset.data.length; i++) {
  	    	  	        	if (! dataset._meta[0].hidden) {
  	    	  	                var model = dataset._meta[0].data[i]._model;
  	    	  	                ctx.font = fontVar;
  	    	  	                ctx.fillText(dataset.data[i].toFixed(1), model.x, model.y - 0);
  	    	  	        	}
  	    	  	        }
  	    	  	    });
  	    	  	}}    
  	  	    }
  	  	};
	return new Chart(ctx, barChartConfig);

}
	
function updateCriticalFactorsChart(position) {

	position.position_corefactors.sort(function(a,b) {
		return a.corefactor_display_group.localeCompare(b.corefactor_display_group);
	});
	
	$('#corefactorlist').empty();
	$(position.position_corefactors).each(function () {
		var row = $('<tr/>');
		row.append($('<td />',{ text : this.corefactor_name }));
		row.append($('<td />',{ text : this.corefactor_description }));
		row.append($('<td />',{ text : this.corefactor_display_group }));		
		$('#corefactorlist').append(row);
	});
	
	var chartLabels = [];
	var chartData0 = []; 
	var chartData1 = [];
	
	for (var i = 0; i < position.position_corefactors.length;i++) {
		chartLabels.push(position.position_corefactors[i].corefactor_name);
		chartData0.push(position.position_corefactors[i].pm_score_a-2*Math.random());
		chartData1.push(position.position_corefactors[i].pm_score_a+1*Math.random());
	}
	
	cfBarChart.config.data.labels = chartLabels;	
	cfBarChart.config.data.datasets[0].data = chartData0;
	cfBarChart.config.data.datasets[1].data = chartData1;
	
	cfBarChart.update();
}



function lookupLastTenCandidates() {
	$.ajax({
		type: "POST",
		async: true,
		url: "/admin/getlastten",
		data: $('#refinequery').serialize(),
		success: function(respondants)
		{
			$('#recentcandidates').empty();
			for (var i = 0; i < respondants.length; i++ ) {
				var li = $('<li />', { 'class' : 'media event' });

				var div = $('<div />', {
					'class' : respondants[i].respondant_profile_class + ' profilebadge' 
				}).append($('<i />', {'class' : "fa " + respondants[i].respondant_profile_icon }));

				var ico = $('<a />', {
					'class' : "pull-left",
					'href' : '/respondant_score.jsp?&respondant_id=' + respondants[i].respondant_id
				}).append(div);

				var badge = $('<div />', { 'class' : 'media-body' });
				$('<a />', {
					'class' : 'title',
					'href' : '/respondant_score.jsp?&respondant_id=' + respondants[i].respondant_id,
					'text' : respondants[i].respondant_person_fname + ' ' + respondants[i].respondant_person_lname
				}).appendTo(badge);
				$('<p />', {
					'text' : respondants[i].respondant_position_name
				}).appendTo(badge);
				$('<p />', {
					'html' : '\<small\>' + respondants[i].respondant_location_name + '\<\/small\>'
				}).appendTo(badge);

				li.append(ico);
				li.append(badge);
				$('#recentcandidates').append(li);
			}}});
}

function cdf(x, mean, variance) {
	  return 0.5 * (1 + erf((x - mean) / (Math.sqrt(2 * variance * variance))));
}

function erf(x) {
	  // save the sign of x
	  var sign = (x >= 0) ? 1 : -1;
	  x = Math.abs(x);

	  // constants
	  var a1 =  0.254829592;
	  var a2 = -0.284496736;
	  var a3 =  1.421413741;
	  var a4 = -1.453152027;
	  var a5 =  1.061405429;
	  var p  =  0.3275911;

	  // A&S formula 7.1.26
	  var t = 1.0/(1.0 + p*x);
	  var y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);
	  return sign * y; // erf(-x) = -erf(x);
}
function updatePositionTenure(position) {
	refreshPositionTenure(getPositionTenureData()); // use stub code
}

function refreshPositionTenure(dataPositionTenure) {
	var positionTenure = $("#positionTenure").get(0).getContext("2d");
	if (tenureChart != null) tenureChart.destroy();
	tenureChart = new Chart(positionTenure, {type: 'bar', data: dataPositionTenure, options: { 
		showScale: true,
		scaleShowGridLines: false,
		responsive : true,
		legend: { display: false }
	}});	
}

function updatePositionProfile(position) {
	var dataPositionProfile = getApplicantProfileData(); // use stub code
	positionProfile = $("#positionProfile").get(0).getContext("2d");
	if (profileChart != null) profileChart.destroy();
	var dataPosProfile = {
			datasets : [{
				backgroundColor : position.position_profiles[0].profile_overlay,
				borderColor : position.position_profiles[0].profile_color,
				label : position.position_profiles[0].profile_name,
				pointBackgroundColor : position.position_profiles[0].profile_color,
				pointBorderColor : position.position_profiles[0].profile_color,
				pointHoverBackgroundColor : position.position_profiles[0].profile_highlight,
				pointHoverBorderColor : position.position_profiles[0].profile_highlight,
				data : []
			}],
			labels : []};
		
	$(position.position_corefactors).each(function (i) {
		dataPosProfile.labels[i] = this.corefactor_name;
		dataPosProfile.datasets[0].data[i] = this.pm_score_a;
	});
	

	profileChart = new Chart(positionProfile, {type: 'radar', data: dataPosProfile, options: { 
		scale: {
                ticks: {
                    beginAtZero: true
                },
                pointLabels : {
                	fontSize : 16
                }
        },
		legend: { display: false }
	}});	
}

function refreshPositionProfile(dataPositionProfile) {
	positionProfile = $("#positionProfile").get(0).getContext("2d");
	if (profileChart != null) profileChart.destroy();
	profileChart = new Chart(positionProfile, {type: 'radar', data: dataPositionProfile, options: { 
		showScale: true,
		responsive : true,
		defaultFontSize: 16,
		legend: { display: false }
	}});	
}


function renderPredictionElements(scores) {
	$('#corefactorsused').empty();
	var title = $('<div />', {
		'class' : 'form-group'
	});
	title.append($('<h4 />', {
		'class' : 'text-center',
		'text' : 'Corefactors'
	}));
	$('#corefactorsused').append(title);
	for (var key in scores) {
		var group = $('<div />', {
			'class' : 'form-group'
		});

		group.append($('<label />', {
			'class' : 'control-label col-md-6 col-sm-6 col-xs-6',
			'text' : key
		}));

		group.append($('<div />',{
			'class':'col-md-6 col-sm-6 col-xs-6'
		}).append($('<input />',{
			'class':'form-control text-right',
			'disabled' : true,
			'value' : scores[key],
			'type' : 'text'
		})));

		$('#corefactorsused').append(group);
	}
}

function renderPredictionChart(scores) {
	$('#candidateicon').html('<i class="fa ' + respondant.respondant_profile_icon +'"></i>');
	$('#candidateicon').addClass(respondant.respondant_profile_class);

	refreshPositionTenure(getPositionTenureData()); // use stub code
}

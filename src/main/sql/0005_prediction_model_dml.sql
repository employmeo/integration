-- definition data inserts 

-- prediction_targets
INSERT INTO employmeo.prediction_targets ("prediction_target_id", "name", "label", "description" )
values (1, 'hirability', 'Hirability', 'Likelihood of getting hired');
INSERT INTO employmeo.prediction_targets ("prediction_target_id","name", "label", "description" )
values (2, 'tenure_6_months', 'Stay 6 months', 'Likelihood of tenure lasting 6 months in said position');
INSERT INTO employmeo.prediction_targets ("prediction_target_id","name", "label", "description" )
values (3, 'get_raise_in_6_months', 'Get Raise', 'Likelihood of getting a raise within 6 months in this position');

-- prediction_models
INSERT into employmeo.prediction_models (prediction_model_id, model_name, model_type, prediction_target_id, version, description, active) 
VALUES (1,'simple_hirability_model',  'linear_regression', (SELECT prediction_target_id from employmeo.prediction_targets WHERE name = 'hirability'), 1, 'Simple linear regression model for hirability predictions', TRUE); 
INSERT into employmeo.prediction_models (prediction_model_id, model_name, model_type, prediction_target_id, version, description, active) 
VALUES (2,'simple_tenure_model', 'linear_regression', (SELECT prediction_target_id from employmeo.prediction_targets WHERE name = 'tenure_6_months'), 1, 'Simple linear regression model for tenure predictions', TRUE); 

-- position_prediction_config
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (1, 
	(select position_id from employmeo.positions where position_name = 'Manager' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'Employmeo')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'hirability'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_hirability_model')
);
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (2, 
	(select position_id from employmeo.positions where position_name = 'Manager' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'Employmeo')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'tenure_6_months'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_tenure_model')
);
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (3, 
	(select position_id from employmeo.positions where position_name = 'Cook' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'Employmeo')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'hirability'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_hirability_model')
);
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (4, 
	(select position_id from employmeo.positions where position_name = 'Crew' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'Employmeo')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'hirability'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_hirability_model')
);
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (5, 
	(select position_id from employmeo.positions where position_name = 'Clerk' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'Employmeo')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'hirability'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_hirability_model')
);
INSERT INTO employmeo.position_prediction_config ("position_prediction_config_id", "position_id" , "prediction_target_id", "model_id")
values (6, 
	(select position_id from employmeo.positions where position_name = 'Operations' 
		and position_account = (select account_id from employmeo.accounts where account_name = 'TCC')),
	(select prediction_target_id from employmeo.prediction_targets where name = 'hirability'),
	(select prediction_model_id from employmeo.prediction_models where model_name = 'simple_hirability_model')
);


--//@UNDO

DELETE from employmeo.position_prediction_config;
DELETE from employmeo.prediction_models;
DELETE from employmeo.prediction_targets;



-- linear regression model configurations

CREATE TABLE employmeo.linear_regression_config (
  config_id           bigserial NOT NULL PRIMARY KEY,
  model_id            bigint not null,
  corefactor_id    integer DEFAULT NULL,
  coefficient      double precision DEFAULT NULL,
  significance     double precision DEFAULT NULL,
  exponent	       double precision DEFAULT NULL,
  config_type	integer NOT NULL,
  required      boolean NOT NULL DEFAULT TRUE,
  active          boolean NOT NULL DEFAULT TRUE,
  created_date    timestamp WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,    
  CONSTRAINT fk_linear_regression_config_model_id
    FOREIGN KEY (model_id)
    REFERENCES employmeo.prediction_models(prediction_model_id),   
  CONSTRAINT fk_linear_regression_config_corefactor_id
    FOREIGN KEY (corefactor_id)
    REFERENCES employmeo.corefactors(corefactor_id) 
) WITH (
    OIDS = FALSE
);


--//@UNDO

DROP table employmeo.linear_regression_config;

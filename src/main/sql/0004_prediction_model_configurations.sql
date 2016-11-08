CREATE TABLE employmeo.prediction_targets (
  prediction_target_id  bigserial NOT NULL,
  "name"                varchar(50) NOT NULL,
  label                 varchar(100) NOT NULL,
  description           varchar(500),
  active                boolean NOT NULL DEFAULT true,
  created_date          timestamp WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  /* Keys */
  CONSTRAINT prediction_targets_pkey
    PRIMARY KEY (prediction_target_id), 
  CONSTRAINT uc_prediction_targets_name
    UNIQUE ("name")    
) WITH (
    OIDS = FALSE
);
  
  
CREATE TABLE IF NOT EXISTS  employmeo.prediction_models (
  prediction_model_id  bigserial NOT NULL PRIMARY KEY,
  model_name    varchar(50) NOT NULL,
  version          integer NOT NULL DEFAULT 1,
  model_type 	varchar(50) NOT NULL,
  prediction_target_id	bigint NOT NULL,
  description    varchar(500) NOT NULL,
  active          boolean NOT NULL DEFAULT TRUE,
  created_date    timestamp WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_prediction_models_prediction_target_id
    FOREIGN KEY (prediction_target_id)
    REFERENCES employmeo.prediction_targets(prediction_target_id)
) WITH (
    OIDS = FALSE
);
   

CREATE TABLE IF NOT EXISTS  employmeo.position_prediction_config (
  position_prediction_config_id  bigserial NOT NULL PRIMARY KEY,
  position_id bigint not null,
  prediction_target_id bigint not null,
  model_id bigint not null,
  target_threshold numeric(10) DEFAULT NULL::numeric,
  active          boolean NOT NULL DEFAULT TRUE,
  created_date    timestamp WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  CONSTRAINT fk_position_prediction_config_target_id
    FOREIGN KEY (prediction_target_id)
    REFERENCES employmeo.prediction_targets(prediction_target_id),
  CONSTRAINT fk_position_prediction_config_position_id
    FOREIGN KEY (position_id)
    REFERENCES employmeo.positions(position_id),
  CONSTRAINT fk_position_prediction_config_model_id
    FOREIGN KEY (model_id)
    REFERENCES employmeo.prediction_models(prediction_model_id)    
) WITH (
    OIDS = FALSE
);  

CREATE TABLE IF NOT EXISTS  employmeo.predictions (
  prediction_id  bigserial NOT NULL PRIMARY KEY,
  respondant_id bigint NOT NULL,
  position_prediction_config_id bigint NOT NULL,
  prediction_score double precision NOT NULL,
  score_percentile double precision,
  active          boolean NOT NULL DEFAULT TRUE,
  created_date    timestamp WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  CONSTRAINT fk_predictions_respondant_id
    FOREIGN KEY (respondant_id)
    REFERENCES employmeo.respondants(respondant_id),
  CONSTRAINT fk_predictions_position_prediction_config_id
    FOREIGN KEY (position_prediction_config_id)
    REFERENCES employmeo.position_prediction_config(position_prediction_config_id)  
) WITH (
    OIDS = FALSE
);
 
--//@UNDO

DROP table employmeo.predictions;
DROP table  employmeo.position_prediction_config;
DROP table  employmeo.prediction_models;
DROP table  employmeo.prediction_targets;



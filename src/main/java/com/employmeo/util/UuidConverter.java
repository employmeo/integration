package com.employmeo.util;

import java.util.UUID;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class UuidConverter  implements AttributeConverter<UUID, UUID> {  
	  /**
	   * Convert entity field to db column value
	   *
	   * @param attribute field
	   * @return UUID of entity
	   */
	  @Override
	  public UUID convertToDatabaseColumn(UUID attribute) {
	    return attribute;
	  }

	  /**
	   * Convert db column value to entity field
	   *
	   * @param dbData column
	   * @return UUID of entity
	   */
	  @Override
	  public UUID convertToEntityAttribute(UUID dbData) {
	    return dbData;
	  }
	}
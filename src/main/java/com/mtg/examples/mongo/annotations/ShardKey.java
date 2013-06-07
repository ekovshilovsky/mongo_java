package com.mtg.examples.mongo.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.code.morphia.mapping.Mapper;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ShardKey {
	String value() default Mapper.IGNORED_FIELDNAME;
}

package com.api.sqlitehelper;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface FieldPersistable{
	boolean nullable() default false;
	Type value() default Type.NONE ;
	boolean autoIncrement() default false;
	
	public enum Type{
		PRIMARY_KEY,
		FOREIGN_KEY,
		NONE
	}
	
	
}





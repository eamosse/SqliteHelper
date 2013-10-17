package com.api.sqlitehelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.api.sqlitehelper.FieldPersistable.Type;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;


public abstract class SQliteHelper extends SQLiteOpenHelper {

	
	public SQliteHelper(Context context,CursorFactory factory) {
		super(context, Utils.dbName, factory, Utils.dbVersion);
	}


	public static final Map<String, String> typeMapping = new HashMap<String, String>();
	static {
		typeMapping.put("double", "real");
		typeMapping.put("Double", "real");
		typeMapping.put("String", "text");
		typeMapping.put("int", "integer");
		typeMapping.put("Integer", "integer");
		typeMapping.put("Float", "real");
		typeMapping.put("float", "real");
		typeMapping.put("Date", "text");
		typeMapping.put("Long", "integer");
		typeMapping.put("long", "integer");
	};


	@Override
	public  abstract void onCreate(SQLiteDatabase db);//{
//		System.out.println(">>>>>>>>> Debut Creation des tables <<<<<<<<<<");
//		db.execSQL(createTable(Category.class));
//		db.execSQL(createTable(Product.class));
//		db.execSQL(createTable(Aid.class));
//		db.execSQL(createTable(Family.class));
//		db.execSQL(createTable(Portfolio.class));
//		db.execSQL(createTable(PortfolioDetail.class));
//		db.execSQL(createTable(Beneficiary.class));
//		db.execSQL(createTable(Address.class));
//		db.execSQL(createTable(RationCard.class));
//		db.execSQL(createTable(Trace.class));
//		db.execSQL(createTable(TraceDetail.class));

		//System.out.println(">>>>>>> fin creation des tables <<<<<<<<<<<<");
	//}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	/**
	 * Generic method for creating table into SQLite Database from a model (entity class)
	 * @param model
	 * @return the script "create table ..."
	 */
	public String createTable(Class<?> model){
		if(model.isAnnotationPresent(ClassPersistable.class)){
			int count = 0;
			String script = "create table "+model.getSimpleName()+" ( ";
			for(Field f : model.getDeclaredFields()){
				if(f.isAnnotationPresent(FieldPersistable.class)){
					if(count == 0){
						script += "\n ";
					}
					else{
						script += ",\n ";
					}
					
					if(f.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
						if(f.getAnnotation(FieldPersistable.class).autoIncrement() == true){
							script += f.getName()+" "+getTypeMapping(f.getType().getSimpleName())+" primary key autoincrement";
						}
						else{
							script += f.getName()+" "+getTypeMapping(f.getType().getSimpleName())+" primary key";
						}
					}
					else{
						if(f.getAnnotation(FieldPersistable.class).value() == Type.FOREIGN_KEY){
							Class<?> cl = f.getType();
							for(Field field : cl.getDeclaredFields()){
								if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value()==Type.PRIMARY_KEY)
									script+=field.getName()+" "+getTypeMapping(field.getType().getSimpleName())+" not null";
							}
						}
						else{
							script+=f.getName()+" "+getTypeMapping(f.getType().getSimpleName());
							if(f.getAnnotation(FieldPersistable.class).nullable() == false)
								script +=" not null";
						}
					}

					count++;
				}
			}
			if(count == 0){
				System.out.println("Missing FieldPersistable on Class "+model.getSimpleName()+" Fields to be persist.");
				script = null;
			}
			else{
				script += ");";
			}

			return script;
		}
		else{
			System.out.println("Class "+model.getSimpleName()+" is not a persistable class.");
			return null;
		}
	}


	/**
	 * Function getTypeMapping
	 * @param type
	 * @return SQLITE data type corresponding
	 */
	public static String getTypeMapping(String type){
		if(typeMapping.containsKey(type)){
			return typeMapping.get(type);
		}
		else{
			return "Not Supported";
		}
	}

}

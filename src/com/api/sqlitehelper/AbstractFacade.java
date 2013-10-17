package com.api.sqlitehelper;


import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.api.sqlitehelper.FieldPersistable.Type;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public abstract class AbstractFacade<T> {

	Class<T> entityClass;
	protected SQLiteDatabase dataBase;
	SQliteHelper sqLiteOpenHelper;
	Context context;

	/**
	 * Constructor
	 * @param entityClass
	 * @throws Exception 
	 */
	public AbstractFacade(Class<T> entityClass, Context context){
		this.entityClass = entityClass;
		this.context = context;
		sqLiteOpenHelper = Utils.helper;//new SQliteHelper(context,null);
	}

	/**
	 * @return the entityClass
	 */
	public Class<T> getEntityClass() {
		return entityClass;
	}

	/**
	 * @param entityClass the entityClass to set
	 */
	public void setEntityClass(Class<T> entityClass) {
		this.entityClass = entityClass;
	}


	public void openDataBase(){
		try {
			this.dataBase = sqLiteOpenHelper.getWritableDatabase();
		} catch (SQLiteException ex) {
			Log.v("Open database exception caught", ex.getMessage());
			this.dataBase= sqLiteOpenHelper.getReadableDatabase();
		}
	}

	public void closeDataBase(){
		this.dataBase.close();
	}

	public void insert(T entity) throws Exception{
		Field primaryKey = null;
		for(Field f : entityClass.getDeclaredFields())
			if(f.isAnnotationPresent(FieldPersistable.class)&& 
					f.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
				primaryKey = f;	
			}
		primaryKey.setAccessible(true);
		if(findById(primaryKey.getInt(entity)) == null)
			insert(entity, entityClass);
	}


	@SuppressWarnings("unchecked")
	private void insert(T entity, Class<T> aClass) throws Exception{
		ContentValues cv = new ContentValues();
		boolean isIdAutoincrement = false;  
		String pkFieldName = null;


		//open database
		openDataBase();

		//getting all fields of the instance
		Field[] fields = aClass.getDeclaredFields();

		for(Field f : fields){
			//the field is persistable
			if(f.isAnnotationPresent(FieldPersistable.class)){
				f.setAccessible(true);

				//the primary key is autoincrement
				if(f.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY &&
						f.getAnnotation(FieldPersistable.class).autoIncrement() == true){
					isIdAutoincrement = true;
					pkFieldName = f.getName();	
				}
				else{
					if(f.getAnnotation(FieldPersistable.class).value() == Type.FOREIGN_KEY){
						Class<T> cl = (Class<T>) f.getType();
						T reference = (T) f.get(entity);
						for(Field field : cl.getDeclaredFields()){
							if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value()==Type.PRIMARY_KEY){
								field.setAccessible(true);
								Object o = findById((Integer) field.get(reference), cl);
								openDataBase();
								if(o == null){
									insert(reference,cl);
									openDataBase();
								}
								cv.put(field.getName(), (Integer) field.get(reference));
							}
						}
					}
					else{
						if(f.getType().getSimpleName().equals("String")){
							cv.put(f.getName(), (String) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("int") || f.getType().getSimpleName().equals("Integer")){
							cv.put(f.getName(), (Integer) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("long") || f.getType().getSimpleName().equals("Long") ){
							cv.put(f.getName(), (Long) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("float") || f.getType().getSimpleName().equals("Float") ){
							cv.put(f.getName(), (Float) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("double") || f.getType().getSimpleName().equals("Double") ){
							cv.put(f.getName(), (Double) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("Date")){
							cv.put(f.getName(), new SimpleDateFormat("dd/MM/yy hh:mm").format((Date) f.get(entity)));
						}
					}
				}
			}
		}

		long id = dataBase.insert(aClass.getSimpleName(), null, cv);
		if(isIdAutoincrement){
			Field field = aClass.getDeclaredField(pkFieldName);
			field.setAccessible(true);
			field.set(entity,(int) id);
		}

		//close database
		closeDataBase();
	}

	//TODO perform update
	@SuppressWarnings("unchecked")
	public void update(T entity) throws Exception{
		ContentValues cv = new ContentValues();
		String pkFieldName = null;

		//open database
		openDataBase();

		//getting all fields of the instance
		Field[] fields = entityClass.getDeclaredFields();

		for(Field f : fields){
			//the field is persistable
			if(f.isAnnotationPresent(FieldPersistable.class)){
				f.setAccessible(true);

				if(f.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
					pkFieldName = f.getName();	
				}
				else{
					if(f.getAnnotation(FieldPersistable.class).value() == Type.FOREIGN_KEY){
						Class<T> cl = (Class<T>) f.getType();
						T reference = (T) f.get(entity);
						for(Field field : cl.getDeclaredFields()){
							if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value()==Type.PRIMARY_KEY){
								field.setAccessible(true);
								Object o = findById((Integer) field.get(reference), cl);
								if(o == null)
									insert(reference,cl);
									openDataBase();
								cv.put(field.getName(), (Integer) field.get(reference));
							}
						}
					}
					else{
						if(f.getType().getSimpleName().equals("String")){
							cv.put(f.getName(), (String) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("int") || f.getType().getSimpleName().equals("Integer")){
							cv.put(f.getName(), (Integer) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("long") || f.getType().getSimpleName().equals("Long") ){
							cv.put(f.getName(), (Long) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("float") || f.getType().getSimpleName().equals("Float") ){
							cv.put(f.getName(), (Float) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("double") || f.getType().getSimpleName().equals("Double") ){
							cv.put(f.getName(), (Double) f.get(entity));
						}
						if(f.getType().getSimpleName().equals("Date")){
							cv.put(f.getName(), new SimpleDateFormat("dd/MM/yy hh:mm").format((Date) f.get(entity)));
						}
					}
				}
			}
		}
		Field field = entityClass.getDeclaredField(pkFieldName);
		field.setAccessible(true);
		dataBase.update(entityClass.getSimpleName(), cv, pkFieldName+" = ?", new String[] {String.valueOf(field.get(entity))});

		//close database
		closeDataBase();
	}

	public void delete(int entityId){
		String pkFieldName = null;

		for (Field fd : entityClass.getDeclaredFields()){
			if (fd.isAnnotationPresent(FieldPersistable.class) && fd.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY)
				pkFieldName = fd.getName();
		}

		//open Database
		openDataBase();
		dataBase.delete(entityClass.getSimpleName(), pkFieldName+" = "+entityId, null);
		//close database
	}

	public List<T> findAll() throws Exception{
		List<T> list = new ArrayList<T>();

		//Opening database
		openDataBase();

		Cursor c = dataBase.query(entityClass.getSimpleName(),null, null,null, null, null, null);
		while (c.moveToNext()){
			T entity = convertCursorToEntity(c,entityClass);
			list.add(entity);
		}

		//close the cursor
		c.close();

		//close database
		closeDataBase();

		return list;
	}

	private T findById(int entityId, Class<T> aClass) throws Exception{
		T entity =null;
		String pkFieldName = null;

		for (Field fd : aClass.getDeclaredFields()){
			if (fd.isAnnotationPresent(FieldPersistable.class) && fd.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY)
				pkFieldName = fd.getName();
		}

		//Opening database
		openDataBase();

		Cursor c = dataBase.query(aClass.getSimpleName(),null, 
				pkFieldName+" = "+entityId, null, null, null, null);
		if(c.moveToNext())
			entity = convertCursorToEntity(c, aClass);

		//close the cursor
		c.close();

		//close database
		closeDataBase();

		return entity;
	}


	public T findById(int entityId) throws Exception{
		return findById(entityId, entityClass);

	}


	public List<T> findEntitiesByForeignKey(int foreignKeyId, Class<?> aClass) throws Exception{
		List<T> list = new ArrayList<T>();

		//getting the foreign key in the class
		String foreignKey = null;
		for(Field f : entityClass.getDeclaredFields()){
			if(f.isAnnotationPresent(FieldPersistable.class) && f.getAnnotation(FieldPersistable.class).value() == Type.FOREIGN_KEY){
				Class<?> cl = f.getType();
				if(cl.equals(aClass)){
					for(Field field : aClass.getDeclaredFields()){
						if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
							foreignKey = field.getName();
						}
					}	
				}
			}
		}

		//Opening database
		openDataBase();

		Cursor c = dataBase.query(entityClass.getSimpleName(), null,
				foreignKey+" = "+foreignKeyId, null, null, null, null);
		while (c.moveToNext()){
			T entity = convertCursorToEntity(c,entityClass);
			list.add(entity);
		}

		//close the cursor
		c.close();

		//close database
		closeDataBase();

		return list;
	}

	public T findEntityByForeignKey(int foreignKeyId, Class<?> aClass) throws Exception{
		T entity = null;

		//getting the foreign key in the class
		String foreignKey = null;
		for(Field f : entityClass.getDeclaredFields()){
			if(f.isAnnotationPresent(FieldPersistable.class) && f.getAnnotation(FieldPersistable.class).value() == Type.FOREIGN_KEY){

				Class<?> cl = f.getType();
				if(cl.equals(aClass)){
					for(Field field : aClass.getDeclaredFields()){
						if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
							foreignKey = field.getName();
						}
					}	
				}
			}
		}

		//Opening database
		openDataBase();

		Cursor c = dataBase.query(entityClass.getSimpleName(), null,
				foreignKey+" = "+foreignKeyId, null, null, null, null);
		if (c.moveToNext()){
			entity = convertCursorToEntity(c,entityClass);
		}

		//close the cursor
		c.close();

		//close database
		closeDataBase();

		return entity;
	}





	@SuppressWarnings("unchecked")
	public T convertCursorToEntity(Cursor c, Class<T> aClass) throws Exception{

		T entity = aClass.newInstance();

		//getting all fields of the instance
		Field[] fields = aClass.getDeclaredFields();

		for(Field f : fields){
			if(f.isAnnotationPresent(FieldPersistable.class)){
				f.setAccessible(true);

				if(f.getAnnotation(FieldPersistable.class).value()==Type.FOREIGN_KEY){
					Class<T> cl = (Class<T>) f.getType();
					String foreignKey = null;
					for(Field field : cl.getDeclaredFields()){
						if(field.isAnnotationPresent(FieldPersistable.class) && field.getAnnotation(FieldPersistable.class).value() == Type.PRIMARY_KEY){
							foreignKey = field.getName();
						}
					}
					int i = c.getInt(c.getColumnIndex(foreignKey));
					Object reference = findById(i, cl);
					f.set(entity, reference);
				}
				else{
					if(f.getType().getSimpleName().equals("String")){
						f.set(entity, c.getString(c.getColumnIndex(f.getName())));
					}
					if(f.getType().getSimpleName().equals("int") || f.getType().getSimpleName().equals("Integer")){
						f.set(entity, c.getInt(c.getColumnIndex(f.getName())));
					}
					if(f.getType().getSimpleName().equals("long") || f.getType().getSimpleName().equals("Long") ){
						f.set(entity, c.getLong(c.getColumnIndex(f.getName())));
					}
					if(f.getType().getSimpleName().equals("float") || f.getType().getSimpleName().equals("Float") ){
						f.set(entity, c.getFloat(c.getColumnIndex(f.getName())));
					}
					if(f.getType().getSimpleName().equals("double") || f.getType().getSimpleName().equals("Double") ){
						f.set(entity, c.getDouble(c.getColumnIndex(f.getName())));
					}
					if(f.getType().getSimpleName().equals("Date")){
						f.set(entity, new SimpleDateFormat("dd/MM/yy hh:mm").parse(c.getString(c.getColumnIndex(f.getName()))));
					}
				}
			}
		}
		return entity;
	}
}

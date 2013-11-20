package com.ese2013.mensaunibe.util.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.ese2013.mensaunibe.model.ModelMensa;
import com.ese2013.mensaunibe.model.ModelMenu;
import com.ese2013.mensaunibe.model.ModelWeeklyPlan;
import com.ese2013.mensaunibe.util.BuilderMensa;
import com.ese2013.mensaunibe.util.BuilderMenu;
import com.ese2013.mensaunibe.util.database.table.FavoriteTable;
import com.ese2013.mensaunibe.util.database.table.MensaTable;
import com.ese2013.mensaunibe.util.database.table.MenuTable;

/**
 * Database Class as an interface between the SQLite Database 
 * and the model
 * 
 * @author ese2013-team5
 *
 */
public class MensaDatabase {

	private SQLiteDatabase database;
	private MensaDatabaseHelper helper;

	public MensaDatabase() {
		helper = new MensaDatabaseHelper();
	}

	/**
	 * Opens the database	
	 * @throws SQLiteException if the database was not opened correctly
	 */
	public void open() throws SQLiteException {
		database = helper.getWritableDatabase();
	}

	/**
	 * Closes the databse
	 */
	public void close() {
		helper.close();
	}

	/**
	 * Stores the mensas and their menu plans to the database
	 * @param mensas List of the mensas that should be stored
	 */
	public void storeMensas(List<ModelMensa> mensas) {
		for (ModelMensa m : mensas) {
			storeSingleMensa(m);
			storeMenusOfMensa(m);
		}
	}
	
	/**
	 * Stores the favorite mensas to the database
	 * @param mensas List of the mensas that should be stored
	 */
	public void storeFavorites(List<ModelMensa> mensas) {
		for (ModelMensa mensa : mensas)  {
			if (mensa.isFavorite()) {
				ContentValues values = new ContentValues();
				values.put(FavoriteTable.COLUMN_ID, mensa.getId());
				database.insertWithOnConflict(FavoriteTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			}
			else {
				database.delete(FavoriteTable.TABLE_NAME, FavoriteTable.COLUMN_ID + " = " + mensa.getId(), null);
			}
		}
	}

	/**
	 * Loads all mensas not including their menus
	 * @return ArrayList with all the mensas from the database
	 */
	public ArrayList<ModelMensa> loadAllMensas() {
		ArrayList<ModelMensa> result = new ArrayList<ModelMensa>();
		final String SELECT_MENSAS = "select * from " + MensaTable.TABLE_NAME + " order by _id asc";
		Cursor cursor = database.rawQuery(SELECT_MENSAS, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(getMensaFromCursor(cursor));
			cursor.moveToNext();
		}
		return result;
	}
	
	/**
	 * Loads the menu plan of a given mensa from the database
	 * @param mensa The mensa for which the menus should be loaded
	 * @return The WeeklyPan for the mensa
	 */
	public ModelWeeklyPlan loadPlanForMensa(ModelMensa mensa) {
		ModelWeeklyPlan result = new ModelWeeklyPlan();
		final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
		for (String day : days) {
			List<ModelMenu> menus = getMenusForDay(mensa, day);
			result.setMenusForDay(day, menus);
		}
		return result;
	}

	private List<ModelMenu> getMenusForDay(ModelMensa mensa, String day) {
		List<ModelMenu> result = new ArrayList<ModelMenu>();
		final String SELECT_STATEMENT = "Select * from " + MenuTable.TABLE_NAME 
				+ " where " + MenuTable.COLUMN_MENSA_ID + " = " + mensa.getId()
				+ " and " + MenuTable.COLUMN_DAY + " = " + "'" + day + "'";
		Cursor cursor = database.rawQuery(SELECT_STATEMENT, null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			result.add(getMenuFromCursor(cursor, mensa));
			cursor.moveToNext();
		}
		return result;
	}

	private void storeSingleMensa(ModelMensa mensa) {
		ContentValues values = new ContentValues();
		values.put(MensaTable.COLUMN_ID, mensa.getId());
		values.put(MensaTable.COLUMN_NAME, mensa.getName());
		values.put(MensaTable.COLUMN_ADDRESS, mensa.getAddress());
		values.put(MensaTable.COLUMN_CITY, mensa.getCity());
		values.put(MensaTable.COLUMN_LAT, mensa.getLat());
		values.put(MensaTable.COLUMN_LON, mensa.getLon());
		database.insertWithOnConflict(MensaTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}
	
	private void storeMenusOfMensa(ModelMensa mensa) {
		ArrayList<ModelMenu> menus = mensa.getWeeklyPlan().getAllMenus();
		for (ModelMenu m : menus) {
			storeSingleMenu(m);
		}
	}
	
	private void storeSingleMenu(ModelMenu menu) {
		ContentValues values = new ContentValues();
		values.put(MenuTable.COLUMN_ID, menu.getMenuID());
		values.put(MenuTable.COLUMN_TITLE, menu.getTitle());
		values.put(MenuTable.COLUMN_DAY, menu.getDay());
		values.put(MenuTable.COLUMN_DATE, menu.getDate());
		values.put(MenuTable.COLUMN_DESC, menu.getDesc());
		values.put(MenuTable.COLUMN_MENSA_ID, menu.getMensaID());
		values.put(MenuTable.COLUMN_WEEK, menu.getWeek());
		values.put(MenuTable.COLUMN_PRICE, menu.getPrice());
		values.put(MenuTable.COLUMN_RATING, menu.getRating());
		database.insertWithOnConflict(MenuTable.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	}

	/**
	 * Returns a menu from the data on which the cursor points.
	 * The cursor itself must not be affected by this!
	 */
	private ModelMenu getMenuFromCursor(Cursor cursor, ModelMensa mensa) {
		BuilderMenu builder = new BuilderMenu(mensa);
		builder.setTitle(cursor.getString(cursor.getColumnIndex(MenuTable.COLUMN_TITLE)));
		builder.setDesc(cursor.getString(cursor.getColumnIndex(MenuTable.COLUMN_DESC)));
		builder.setPrice(cursor.getString(cursor.getColumnIndex(MenuTable.COLUMN_PRICE)));
		builder.setWeek(cursor.getInt(cursor.getColumnIndex(MenuTable.COLUMN_WEEK)));
		builder.setDate(cursor.getString(cursor.getColumnIndex(MenuTable.COLUMN_DATE)));
		builder.setDay(cursor.getString(cursor.getColumnIndex(MenuTable.COLUMN_DAY)));
		builder.setRating(cursor.getDouble(cursor.getColumnIndex(MenuTable.COLUMN_RATING)));
		builder.setMenuID(cursor.getInt(cursor.getColumnIndex(MenuTable.COLUMN_ID)));
		builder.setMensaID(cursor.getInt(cursor.getColumnIndex(MenuTable.COLUMN_MENSA_ID)));
		return builder.build();
	}

	/**
	 * Returns a mensa from the data on which the cursor points.
	 * The cursor itself must not be affected by this!
	 */
	private ModelMensa getMensaFromCursor(Cursor cursor) {
		BuilderMensa builder = new BuilderMensa();
		builder.setId(cursor.getInt(cursor.getColumnIndex(MensaTable.COLUMN_ID)));
		builder.setName(cursor.getString(cursor.getColumnIndex(MensaTable.COLUMN_NAME)));
		builder.setAddress(cursor.getString(cursor.getColumnIndex(MensaTable.COLUMN_ADDRESS)));
		builder.setCity(cursor.getString(cursor.getColumnIndex(MensaTable.COLUMN_CITY)));
		builder.setLat(cursor.getFloat(cursor.getColumnIndex(MensaTable.COLUMN_LAT)));
		builder.setLon(cursor.getFloat(cursor.getColumnIndex(MensaTable.COLUMN_LON)));
		builder.setIsFavorite(isMensaFavorite(builder.getId()));
		return builder.build();
	}
	
	/**
	 * Checks whether a mensa with a given id is a favorite or not
	 * @param id The id of the mensa
	 * @return true if the mensa is favorite else false
	 */
	public boolean isMensaFavorite(int id) {
		String statement = "select * from " + FavoriteTable.TABLE_NAME 
				+ " where " + FavoriteTable.COLUMN_ID + " = " + id + ";";
		Cursor cursor = database.rawQuery(statement, null);
		if (cursor.getCount() == 1) {
			Log.i("Mensa fav", "mensa " + id + " was set as fav");
			return true;
		}
		else {
			Log.i("Mensa not fav", "mensa " + id + " was not set as fav!");
			return false;
		}
	}
}
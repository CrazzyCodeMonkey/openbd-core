/* 
 *  Copyright (C) 2000 - 2015 aw2.0 Ltd
 *
 *  This file is part of Open BlueDragon (OpenBD) CFML Server Engine.
 *  
 *  OpenBD is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  Free Software Foundation,version 3.
 *  
 *  OpenBD is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with OpenBD.  If not, see http://www.gnu.org/licenses/
 *  
 *  Additional permission under GNU GPL version 3 section 7
 *  
 *  If you modify this Program, or any covered work, by linking or combining 
 *  it with any of the JARS listed in the README.txt (or a modified version of 
 *  (that library), containing parts covered by the terms of that JAR, the 
 *  licensors of this Program grant you additional permission to convey the 
 *  resulting work. 
 *  README.txt @ http://www.openbluedragon.org/license/README.txt
 *  
 *  http://openbd.org/
 */
package com.bluedragon.mongo;

import org.bson.Document;

import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.naryx.tagfusion.cfm.engine.cfArgStructData;
import com.naryx.tagfusion.cfm.engine.cfBooleanData;
import com.naryx.tagfusion.cfm.engine.cfData;
import com.naryx.tagfusion.cfm.engine.cfSession;
import com.naryx.tagfusion.cfm.engine.cfmRunTimeException;


public class MongoCollectionUpdate extends MongoCollectionList {

	private static final long serialVersionUID = 1L;


	public MongoCollectionUpdate() {
		min = 4;
		max = 7;
		setNamedParams( new String[] { "datasource", "collection", "query", "data", "upsert", "multi" } );
	}


	public String[] getParamInfo() {
		return new String[] {
				"datasource name.  Name previously created using MongoRegister",
				"collection name",
				"the query to which to find the object to update",
				"data to save into the collection.  Can be a single structure, or a JSON string (which will be converted to a structure via Mongo)",
				"if the database should create the element if it does not exist",
				"if the update should be applied to all objects matching"
		};
	}


	public java.util.Map<String, String> getInfo() {
		return makeInfo(
				"mongo",
				"Updates the data in mongo",
				ReturnType.BOOLEAN );
	}


	public cfData execute( cfSession _session, cfArgStructData argStruct ) throws cfmRunTimeException {
		MongoDatabase db = getMongoDatabase( _session, argStruct );

		String collection = getNamedStringParam( argStruct, "collection", null );
		if ( collection == null )
			throwException( _session, "please specify a collection" );

		cfData data = getNamedParam( argStruct, "data", null );
		if ( data == null )
			throwException( _session, "please specify data to update" );

		cfData query = getNamedParam( argStruct, "query", null );
		if ( query == null )
			throwException( _session, "please specify query to update" );

		boolean upsert = getNamedBooleanParam( argStruct, "upsert", false );
		boolean multi = getNamedBooleanParam( argStruct, "multi", false );

		try {

			MongoCollection<Document> col = db.getCollection( collection );
			Document qry = getDocument( query );
			Document dat = getDocument( data );
			long start = System.currentTimeMillis();

			if ( multi )
				col.updateMany( qry, dat, new UpdateOptions().upsert( upsert ) );
			else
				col.updateOne( qry, dat, new UpdateOptions().upsert( upsert ) );

			_session.getDebugRecorder().execMongo( col, "update", qry, System.currentTimeMillis() - start );

			return cfBooleanData.TRUE;

		} catch ( MongoException me ) {
			throwException( _session, me.getMessage() );
			return null;
		}
	}
}
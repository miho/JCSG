package eu.mihosoft.vrl.v3d.parametrics;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CSGDatabase {
	
	private static HashMap<String,Parameter> database=null;
	private static File dbFile=new File("CSGdatabase.json");
    private static final Type TT_mapStringString = new TypeToken<HashMap<String,Parameter>>(){}.getType();
    private static final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private static final HashMap<String,ArrayList<IParameterChanged>> parameterListeners=new HashMap<>();
	public static void set(String key, Parameter value){
		getDatabase();
		synchronized(database){
			getDatabase().put(key, value);
		}
	}
	public static Parameter get(String key){
		Parameter ret =null;
		getDatabase();// load database before synchronization
		synchronized(database){
			ret=getDatabase().get(key);
		}
		return ret;
	}
	
	public static   void clear(){

		getDatabase();
		synchronized(database){
			database.clear();
		}
		parameterListeners.clear();
		saveDatabase();
	}
	public static  void addParameterListener(String key, IParameterChanged l){
		ArrayList<IParameterChanged> list = getParamListeners(key);
		if(!list.contains(l)){
			list.add(l);
		}
	}
	public static  void removeParameterListener(String key, IParameterChanged l){
		if(parameterListeners.get(key)==null){
			return;
		}
		ArrayList<IParameterChanged> list = parameterListeners.get(key);
		if(list.contains(l)){
			list.remove(l);
		}
	}
	
	public static  ArrayList<IParameterChanged> getParamListeners(String key){
		if(parameterListeners.get(key)==null){
			parameterListeners.put(key, new ArrayList<>());
		}
		return parameterListeners.get(key);
	}
	

	public static void delete(String key){
		synchronized(database){
			getDatabase().remove(key);
		}
	}
	private static HashMap<String,Parameter> getDatabase() {
		if(database==null){
			new Thread(){
				public void run(){
					String jsonString;
					try {
						
						if(!getDbFile().exists()){
							setDatabase(new HashMap<String,Parameter>());
						}
						else{
					        InputStream in = null;
					        try {
					            in = FileUtils.openInputStream(getDbFile());
					            jsonString= IOUtils.toString(in);
					        } finally {
					            IOUtils.closeQuietly(in);
					        }
					        HashMap<String,Parameter> tm=gson.fromJson(jsonString, TT_mapStringString);
					        
					        
					        if(tm!=null){
//					        	System.out.println("Hash Map loaded from "+jsonString);
//					        	for(String k:tm.keySet()){
//						        	System.out.println("Key: "+k+" vlaue= "+tm.get(k));
//						        }
					        	setDatabase(tm);
					        }
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.err.println(dbFile.getAbsolutePath());
						setDatabase(new HashMap<String,Parameter>());
					}
					Runtime.getRuntime().addShutdownHook(new Thread() {
						@Override
						public void run() {
							saveDatabase();
						}
					});
				}
			}.start();
			long start = System.currentTimeMillis();
			while(database==null){
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if((System.currentTimeMillis()-start)>500){
					setDatabase(new HashMap<String,Parameter>());
				}
			}
		}
		return database;
	}
	
	public static void loadDatabaseFromFile(File f){
		InputStream in = null;
		String jsonString;
        try {
            try {
				in = FileUtils.openInputStream(f);
				jsonString= IOUtils.toString(in);
		        HashMap<String,Parameter> tm=gson.fromJson(jsonString, TT_mapStringString);
		        if(tm !=null)
			        for(String k:tm.keySet()){
			        	set(k,tm.get(k));
			        }
		        saveDatabase();
			} catch (Exception e) {
				System.err.println(f.getAbsolutePath());
				e.printStackTrace();
			}
            
        } finally {
            IOUtils.closeQuietly(in);
        }
	}
	
	public static String getDataBaseString(){
		String writeOut=null;
		getDatabase();
		synchronized(database){
			 writeOut  =gson.toJson(database, TT_mapStringString); 
		}
		return writeOut;
	}
	
	public static void saveDatabase(){
		String writeOut=getDataBaseString();
		try {
			if(!getDbFile().exists()){
				getDbFile().createNewFile();
			}
	        OutputStream out = null;
	        try {
	            out = FileUtils.openOutputStream(getDbFile(), false);
	            IOUtils.write(writeOut, out);
	            out.flush();
	            out.close(); // don't swallow close Exception if copy completes normally
	        } finally {
	            IOUtils.closeQuietly(out);
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void setDatabase(HashMap<String,Parameter> database) {
		if(CSGDatabase.database!=null){
			return;
		}
		CSGDatabase.database = database;
	}
	public static File getDbFile() {
		return dbFile;
	}
	public static void setDbFile(File dbFile) {
		if(!dbFile.exists())
			try {
				dbFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		CSGDatabase.dbFile = dbFile;
		loadDatabaseFromFile(dbFile);
	}

}

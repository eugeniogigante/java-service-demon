package javaservicedemon.storage;



import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javaservicedemon.entity.Messaggio;


/**
 * 
 * @author Eugenio Gigante
 *
 */
public class DBMessaggio {
	
	private Connection conn;
	private String query;
	private PreparedStatement st;
	private String path="C://Users//eugenio.gigante//AppData//Roaming//Skype//eugenio-gigante//main.db";
	
	/**
	 * Costruttore vuoto della classe DBMessaggio
	 */
	public DBMessaggio(){ }
	
	
	//METODI DI VISUALIZZAZIONE
	/**
	 * Restituisce la lista delgli utenti presenti nel sistema
	 * @return Lista degli utenti
	 * @throws SQLException
	 */
	public List<Messaggio> visualizzaMessaggi() throws SQLException{
		
		try{
			conn =new  SQLiteJDBC().getConnection(this.path);//DBConnectionPool.getConnection();
			
			query = "SELECT * FROM "+DBNames.TABLE_MESSAGES;
			st = conn.prepareStatement(query);
			ResultSet rs = st.executeQuery();
			
			return fillResults(rs);
			
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			st.close();
			//DBConnectionPool.releaseConnection(conn);
		}
		return null;
		
	}
	
	
	// METODI DI SUPPORTO PER LA CLASSE

	/**
	 * Riempie una lista di Messaggi con i risultati di una query
	 * @param rs ResultSet con i risultati di una query
	 * @return Lista dei messaggi trovati
	 * @throws SQLException
	 */
	private List<Messaggio> fillResults(ResultSet rs) throws SQLException {
		List<Messaggio> results = new ArrayList<Messaggio>();
		
		while(rs.next()){
			String d = rs.getString(DBNames.ATTR_MESSAGES__TIMESTAMP);
			String t = rs.getString(DBNames.ATTR_MESSAGES_BODY_XML);
			String mi = rs.getString(DBNames.ATTR_MESSAGES__AUTHOR);
			String de = rs.getString(DBNames.ATTR_MESSAGES_CHATNAME);
			results.add(new Messaggio(t, d, mi, de)); 
		}
		
		return results;
	}
	
	/**
	 * Esegue un aggiornamento al database
	 * @return true se l'aggiornamento è stato eseguito con successo, false altrimenti
	 * @throws SQLException
	 */
	private boolean update() throws SQLException {
		try{
			conn = new  SQLiteJDBC().getConnection(this.path);//DBConnectionPool.getConnection();
			st = conn.prepareStatement(query);
			
			if(st.executeUpdate() > 0){
				conn.commit();
				return true;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			st.close();
			//DBConnectionPool.releaseConnection(conn);
		}
		return false;
	}
	

}

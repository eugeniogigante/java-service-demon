package javaservicedemon.entity;


public class Messaggio {
	private String testo;
	private String data;
	private String mittente;
	private String destinatario;
	
	public Messaggio(String testo, String data, String mittente, String destinatario){
		this.data=data;
		this.testo=testo;
		this.mittente=mittente;
		this.destinatario=destinatario;
	}
	
	public void setTesto(String testo){
		this.testo=testo;
	}
	public String getTesto(){
		return this.testo;
	}
	public void setData(String data){
		this.data=data;
	}
	public String getData(){
		return this.data;
	}
	public void setMittente(String mittente){
		this.mittente=mittente;
	}
	public String getMittente(){
		return this.mittente;
	}
	public void setDestinatario(String destinatario){
		this.destinatario=destinatario;
	}
	public String getDestinatario(){
		return this.destinatario;
	}
	
}

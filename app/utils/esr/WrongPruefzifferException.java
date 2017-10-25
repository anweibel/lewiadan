package utils.esr;

public class WrongPruefzifferException extends Exception {

	private EsrZahlung zahlung;
	
	public void setZahlung(EsrZahlung zahlung) {
		this.zahlung = zahlung;
	}
	public EsrZahlung getZahlung() {
		return zahlung;
	}
}

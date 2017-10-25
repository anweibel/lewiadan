package utils.esr;

public class UnknownTransactionCode extends Exception {
	
	private EsrZahlung zahlung;
	private String TxCode;
	private String HumanReadableCode;
	
	public void setZahlung(EsrZahlung zahlung) {
		this.zahlung = zahlung;
	}
	public EsrZahlung getZahlung() {
		return zahlung;
	}
	public void setTxCode(String txCode) {
		TxCode = txCode;
	}
	public String getTxCode() {
		return TxCode;
	}
	public void setHumanReadableCode(String humanReadableCode) {
		HumanReadableCode = humanReadableCode;
	}
	public String getHumanReadableCode() {
		return HumanReadableCode;
	}
	
}

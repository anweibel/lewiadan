package utils.esr;

import java.util.Collection;

public class ZahlungResult {
	private Collection<EsrZahlung> zahlungen;
	private String warnings = "";
	
	public Collection<EsrZahlung> getZahlungen() {
		return zahlungen;
	}
	public void setZahlungen(Collection<EsrZahlung> zahlungen) {
		this.zahlungen = zahlungen;
	}
	public String getWarnings() {
		return warnings;
	}
	public void setWarnings(String warnings) {
		this.warnings = warnings;
	}
}

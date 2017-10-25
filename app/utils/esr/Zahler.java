package utils.esr;

public class Zahler {

		private String name;
		private String vorname;
		private String strasse;
		private String plz;
		private String ort;
		private String mitgliedTyp;
		
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getVorname() {
			return vorname;
		}
		public void setVorname(String vorname) {
			this.vorname = vorname;
		}
		public String getStrasse() {
			return strasse;
		}
		public void setStrasse(String strasse) {
			this.strasse = strasse;
		}
		public String getPlz() {
			return plz;
		}
		public void setPlz(String plz) {
			this.plz = plz;
		}
		public String getOrt() {
			return ort;
		}
		public void setOrt(String ort) {
			this.ort = ort;
		}
		
		public void setMitgliedTyp(String mitgliedTyp) {
			this.mitgliedTyp = mitgliedTyp;
		}
		public String getMitgliedTyp() {
			return mitgliedTyp;
		}
		public String toString(){
			return vorname + " " + name +"\n" +
				   strasse + "\n" +
				   plz + " " + ort + "\n" +
				   "Mitgliedtyp = " + mitgliedTyp; 
		}
		public String toCSV(){
			return vorname + ";" + name +";" +
				   strasse + ";" + plz + ";" + ort + ";"
				    + mitgliedTyp + ";"; 
		}
}

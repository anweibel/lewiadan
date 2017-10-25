package controllers;

import static play.modules.excel.RenderExcel.renderAsync;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import models.Zahlung;

import org.apache.commons.lang.time.DateUtils;

import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Monatsabrechnung extends Controller {
	
	private static String SELECT_SUMS_SQL = ""
			+ "SELECT zc.code, k.kname, SUM(z.zbetrag) " 
			+ "FROM zahlungen z "
			+ "LEFT OUTER JOIN zahlungscode zc ON z.zcid = zc.id "
			+ "LEFT OUTER JOIN konten k ON z.koid = k.id "
			+ "WHERE z.zdatum BETWEEN ? AND ? "
			+ "AND z.nid IS NOT NULL "
			+ "GROUP BY zc.code, k.kname "
			+ "ORDER BY k.kname DESC, zc.code ASC ";
	
	private static String SELECT_SUMS_SPRACHE_SQL = ""
			+ "SELECT zc.code, k.kname, SUM(z.zbetrag) " 
			+ "FROM zahlungen z "
			+ "LEFT JOIN namen n ON z.nid = n.id "
			+ "LEFT OUTER JOIN zahlungscode zc ON z.zcid = zc.id "
			+ "LEFT OUTER JOIN konten k ON z.koid = k.id "
			+ "WHERE z.zdatum BETWEEN ? AND ? "
			+ "AND n.sprache = ? "
			+ "AND z.nid IS NOT NULL "
			+ "GROUP BY zc.code, k.kname "
			+ "ORDER BY k.kname DESC, zc.code ASC ";
	
	private static SimpleDateFormat formatBindestrich = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat formatPunkt = new SimpleDateFormat("dd. MM. yyyy");
	private static NumberFormat numberFormat = NumberFormat.getCurrencyInstance(new Locale("de", "CH"));
	
	public static void index() {
		String von = flash.get("von") != null ? flash.get("von") : formatBindestrich.format(new Date());
		String bis = flash.get("bis") != null ? flash.get("bis") : formatBindestrich.format(new Date());
		render(von, bis);
	}

	public static void abrechnung(Date start, Date ende, Boolean showDetails, String sprache, String submit) throws Exception {
		
		flash.put("von", formatBindestrich.format(start));
		flash.put("bis", formatBindestrich.format(ende));
		
		if(DateUtils.truncatedCompareTo(start, ende, Calendar.DATE) > 0){
			flash.error("'Von'-Datum darf nicht nach 'Bis'-Datum liegen.");
			Monatsabrechnung.index();
		}
		
		if(showDetails == null)
			showDetails = Boolean.FALSE;
		
		List<Object[]> summen = null;

		boolean countAllLanguages = true;
		String sprachCode = "" + sprache.charAt(0);
		
		if(sprache.startsWith("Deuts") || sprache.startsWith("Fran")){
			countAllLanguages = false;
			summen = Zahlung.em().createNativeQuery(SELECT_SUMS_SPRACHE_SQL)
					.setParameter(1, start)
					.setParameter(2, ende)
					.setParameter(3, sprachCode)
					.getResultList();
		} else {
			summen = Zahlung.em().createNativeQuery(SELECT_SUMS_SQL)
					.setParameter(1, start)
					.setParameter(2, ende)
					.getResultList();
		}
		
		BigDecimal gesamtsumme = new BigDecimal(0);
		
		for(Object[] summe : summen){
			if(summe[0] == null){
				summe[0] = "(kein Zahlungscode)";
			}
			if(summe[1] == null){
				summe[1] = "(kein Konto)";
			}
			if(summe[2] != null){
				gesamtsumme = gesamtsumme.add((BigDecimal)summe[2]);
			}
		}
		
		List<Zahlung> einzelbuchungen;
		
		if(countAllLanguages){
			einzelbuchungen = Zahlung.find("datum BETWEEN ? AND ? AND (mitglied IS NOT NULL)", start, ende).fetch();
		} else {
			einzelbuchungen = Zahlung.find("datum BETWEEN ? AND ? AND (mitglied IS NOT NULL) AND mitglied.sprache = ?", start, ende, sprachCode).fetch();
		}

		Collections.sort(einzelbuchungen);

		if (submit.contains("XLS")) {
			Map<String, Object> beans = new HashMap<String, Object>();
			beans.put("gesamtsumme", gesamtsumme);
			beans.put("summen", summen);
			beans.put("sprache", sprache);
			beans.put("einzelbuchungen", einzelbuchungen);
			beans.put("start", formatPunkt.format(start));
			beans.put("ende", formatPunkt.format(ende));
			beans.put("showDetails", showDetails);
			
			String filename = "abrechnung_" + formatBindestrich.format(start) + "_"
					+ formatBindestrich.format(ende) + ".xls";
			response.setHeader("Content-Disposition", "attachment; filename="+ filename);
			
			renderAsync("app/views/Monatsabrechnung/abrechnung.xls", beans, "").get().apply(request, response);
		} else {
			
			render(start, ende, summen, showDetails, einzelbuchungen, gesamtsumme, sprache );
		}
	}
	
	// verwendet im View
	public static String zahlenFormat(BigDecimal b){
		return numberFormat.format(b).replace("SFr. ", "");
	}
}

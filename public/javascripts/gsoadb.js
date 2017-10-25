	
	//////////////////////////////
	// Kategorien selektieren
    //////////////////////////////
	var toggle = function(kategorieEl) {
		var input = document.getElementById('h'+kategorieEl.id);
		if(kategorieEl.className.indexOf('selected') > -1) {
			kategorieEl.className = 'kategorie btn btn-mini';
			input.value = '';
		} else {
			kategorieEl.className = 'kategorie selected btn btn-mini btn-primary';
			input.value = kategorieEl.id.replace("ausschluss", "");
		}
	}

	/////////////////////////////////
	// Auto-Vervollstaendigung PLZ
    /////////////////////////////////
	$("#object_ort").live("focus", function(e){
		
		if(!$("#object_ort").val()){
			var plz = $("#object_plz").val();
			$.ajax({
				url: plzRoute.url({plz: plz})
			}).done(function ( data ) {
				$("#object_ort").val(data.name);
			});
		}
	});

	/////////////////////////////////
	// Datepicker
    /////////////////////////////////
	settings = {
        renderer: $.ui.datepicker.defaultRenderer,
        monthNames: ['Januar','Februar','März','April','Mai','Juni',
        'Juli','August','September','Oktober','November','Dezember'],
        monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dez'],
        dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
        dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
        dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
        dateFormat: "yy-mm-dd",
        firstDay: 1,
        prevText: '&#x3c; zurück', prevStatus: '',
        prevJumpText: '&#x3c;&#x3c;', prevJumpStatus: '',
        nextText: 'vor &#x3e;', nextStatus: '',
        nextJumpText: '&#x3e;&#x3e;', nextJumpStatus: '',
        currentText: 'heute', currentStatus: '',
        todayText: 'heute', todayStatus: '',
        clearText: '-', clearStatus: '',
        closeText: 'schließen', closeStatus: '',
        yearStatus: '', monthStatus: '',
        weekText: 'Wo', weekStatus: '',
        dayStatus: 'DD d MM',
        defaultStatus: '',
        isRTL: false
	};
	$(".crud_date input").datepicker(settings);
	
	/////////////////////////////////
	// Bestaetigungsdialog
    /////////////////////////////////
	$( ".crudDelete" ).click(function (e) {
		$( "#dialog-confirm" ).dialog( "open" );
		e.preventDefault();
	});
	
    $( "#dialog-confirm" ).dialog({
    	autoOpen: false,
        resizable: false,
        height:240,
        modal: true,
        buttons: {
            "Ja": function() {
                $( this ).dialog( "close" );
                $( "#deleteForm" ).submit();
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        }
    });
    
	$( ".delZahlung, .finalDelete" ).click(function (e) {
		
		if(confirm($(this).attr("data-frage"))){
			return true;
		} else {
			e.preventDefault();
		} 
	});

	/////////////////////////////////
	// Anrededropdown
    /////////////////////////////////
    $(".dropdown-menu > li > a").live("click", function(e){
    	$(this).parent().parent().parent().find('input').val($(this).text());
	});
    
	/////////////////////////////////
	// Briefanrede
    /////////////////////////////////
    $("#object_briefanrede").live("focus", function(e){
    	
    	if($("#object_briefanrede").val().length == 0){
    		
    		var anrede = $("#object_anrede").val();
    		var vorname = $("#object_vorname").val();
    		var nachname = $("#object_name").val();
    		var sprache = $("#object_sprache").val();
    		
    		var neuerText = '';
    		
    		if(sprache == 'Deutsch'){
	    		if(anrede == 'Herr'){
	    			neuerText = 'Lieber ' + vorname + ' ' + nachname;
	    		} else if (anrede == 'Frau'){
	    			neuerText = 'Liebe ' + vorname + ' ' + nachname;
	    		} else if (anrede == 'Firma'){
	    			neuerText = 'Sehr geehrte Damen und Herren';
	    		}
    		} else {
	    		if(anrede == 'Monsieur'){
	    			neuerText = 'Cher ' + vorname + ' ' + nachname;
	    		} else if (anrede == 'Madame'){
	    			neuerText = 'Chère ' + vorname + ' ' + nachname;
	    		} else if (anrede == 'Entreprise'){
	    			neuerText = 'Mesdames, Messieurs,';
	    		}
    		}
    		
    		$("#object_briefanrede").val(neuerText);
    		$("#object_briefanrede").select();
    		e.preventDefault();
    	}
	});
    
	/////////////////////////////////
	// Gelöschte Mitglieder markieren
    /////////////////////////////////
    $(".mitglieder tr td:last-child, .mitglieder tr th:last-child").each( function(e){
    	if($(this).text().indexOf("2") >= 0){
    		$(this).parent().addClass("deleted");
    	}
    	
    	$(this).remove();
    });
    
    
	/////////////////////////////////
	// Fokus / Cursor
    /////////////////////////////////
    $("#object_anrede").focus();
    $("#object_betrag").focus();
    $(".suchFeld").focus();
    
    
	/////////////////////////////////
	// Gross-/Kleinschreibung
    /////////////////////////////////
    jQuery.fn.capitalise = function() {
	    var txt = $(this).val();
	    txt = txt.toLowerCase();
	    var newTxt = txt.replace(/(\b)([a-zA-Z])/g,
		    function(firstLetter){
		        return firstLetter.toUpperCase();
		    });
	    
	    
	    newTxt = newTxt.replace(" De ", " de ");
	    newTxt = newTxt.replace(" La ", " la ");
	    newTxt = newTxt.replace(" Du ", " du ");
	    newTxt = newTxt.replace(" Des ", " des ");
	    newTxt = newTxt.replace(" L'", " l'");
	    newTxt = newTxt.replace("Francois", "François");
	    newTxt = newTxt.replace("Stephan", "Stéphan");
	    newTxt = newTxt.replace("Jerome", "Jerôme");
	    newTxt = newTxt.replace("Cedric", "Cédric");
	    newTxt = newTxt.replace("Noel", "Noëlle");
	    newTxt = newTxt.replace("Delemont", "Delémont");
	    newTxt = newTxt.replace("Neuchatel", "Neuchâtel");
	    newTxt = newTxt.replace("Thonex", "Thônex");
	    newTxt = newTxt.replace("-Glane", "-Glâne");
	    newTxt = newTxt.replace("Chatel", "Châtel");
	    newTxt = newTxt.replace("Zuerich", "Zürich");
	    newTxt = newTxt.replace("ieres", "ières");
	    newTxt = newTxt.replace("llee", "llée");
	    newTxt = newTxt.replace("hateau", "hâteau");
	    newTxt = newTxt.replace("Crets", "Crêts");
	    newTxt = newTxt.replace("Geneve ", "Genève ");
	    newTxt = newTxt.replace(/Geneve\b/, "Genève");
	    newTxt = newTxt.replace("Chene-", "Chêne-");
	    newTxt = newTxt.replace(/Chene\b/, "Chêne");
	    newTxt = newTxt.replace(/Chenes\b/, "Chênes");
	    newTxt = newTxt.replace(/Donze\b/, "Donzé");
	    newTxt = newTxt.replace(/Aire\b/, "Aïre");
	    newTxt = newTxt.replace(/Rene\b/, "René");
	    newTxt = newTxt.replace(/Andre\b/, "André");
	    
	    $(this).val(newTxt);
	    return this;
    };
    
    $("#grossKlein").live("click", function() {
    	$("#object_vorname").capitalise();
    	$("#object_vorname2").capitalise();
    	$("#object_name").capitalise();
    	$("#object_name2").capitalise();
    	$("#object_strasse").capitalise();
    	$("#object_ort").capitalise();
    	$("#object_postfach").capitalise();
    	$("#object_briefanrede").val("");
    	$("#object_briefanrede").focus();
    	return false;
    });
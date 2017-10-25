package utils;

import java.util.List;

import models.MitgliedType;
import be.objectify.led.ObjectFactory;
import be.objectify.led.PropertyContext;
import be.objectify.led.factory.object.AbstractObjectFactory;
import be.objectify.led.validation.ValidationFunction;

public class MitgliedTypeFactory extends AbstractObjectFactory<MitgliedType> {

	@Override
	public MitgliedType createObject(String propertyName,
            String propertyValue,
            PropertyContext propertyContext) {
		
		if(propertyName.equals("Mitgliedertyp")){
			return MitgliedType.valueOf(propertyValue);
		} else {
    		throw new RuntimeException(propertyName + " ist das falsche Objekt");
    	}
	}

	@Override
	public Class getBoundClass() {
		return MitgliedType.class;
	}
}

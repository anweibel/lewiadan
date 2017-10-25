package utils;

import be.objectify.led.PropertyContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MapPropertyContext implements PropertyContext
{
    private Map<String, String> data = new HashMap<String, String>();

    public void setValue(String name,
                         String value)
    {
        data.put(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public String getValue(String name)
    {
        return data.get(name);
    }
}

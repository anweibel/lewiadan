package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;
import be.objectify.led.DefaultFactoryResolver;
import be.objectify.led.FactoryResolver;
import be.objectify.led.ObjectFactoryRegistry;
import be.objectify.led.PropertyContext;
import be.objectify.led.PropertySetter;

/**
* @author Steve Chaloner (steve@objectify.be)
 */
public class ExcelBinder
{
    public static <T> List bind(File file,
                                Class<T> modelType) throws ExcelBindingException
    {
        try
        {
        	WorkbookSettings s = new WorkbookSettings();
        	s.setEncoding("ISO8859_1");
            Workbook workbook = Workbook.getWorkbook(file, s);
            Sheet sheet = workbook.getSheet(0);
            List<String> headerNames = getHeaderNames(sheet);

            List<T> objects = new ArrayList<T>();

            ObjectFactoryRegistry ofr = new ObjectFactoryRegistry();
            // allows the binder to resolve Customer instances
            ofr.register(new ListFactory());
            ofr.register(new MitgliedTypeFactory());
            FactoryResolver factoryResolver = new DefaultFactoryResolver(ofr);

            // iterate over each non-header row and make it into an object
            for (int i = 1; i < sheet.getRows(); i++)
            {
                PropertyContext propertyContext = getPropertyContext(sheet,
                                                                     i,
                                                                     headerNames);
                PropertySetter propertySetter = new PropertySetter(factoryResolver,
                                                                   propertyContext);
                T t = modelType.newInstance();
                propertySetter.process(t);
                objects.add(t);
            }
            return objects;
        }
        catch (IOException e)
        {
            throw new ExcelBindingException("IO problem when binding",
                                            e);
        }
        catch (BiffException e)
        {
            throw new ExcelBindingException("Excel problem when binding",
                                            e);
        }
        catch (InstantiationException e)
        {
            throw new ExcelBindingException("Could not create new instance of " + modelType.getName(),
                                            e);
        }
        catch (IllegalAccessException e)
        {
            throw new ExcelBindingException("Could not create new instance of " + modelType.getName(),
                                            e);
        }
    }

    private static List<String> getHeaderNames(Sheet sheet)
    {
        List<String> headerNames = new ArrayList<String>();
        Cell[] headerCells = sheet.getRow(0);
        for (Cell cell : headerCells)
        {
            headerNames.add(cell.getContents());
        }

        return headerNames;
    }

    private static PropertyContext getPropertyContext(Sheet sheet,
                                                      int rowIndex,
                                                      List<String> headerNames)
    {
        Cell[] cells = sheet.getRow(rowIndex);
        MapPropertyContext propertyContext = new MapPropertyContext();
        for (int i = 0; i < headerNames.size() && i < cells.length; i++){
        	String contents = cells[i].getContents();
        	propertyContext.setValue(headerNames.get(i), contents);
        }

        return propertyContext;
    }
}

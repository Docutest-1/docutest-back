package com.revature.services;

import java.util.Map;

import io.swagger.models.Model;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class JSONStringCreator {
    
    private int autoId = 1;

    /**
     * For OAS 2.0, recursively creates a JSON string representation of an object
     * found in the definitions section of a Swagger object. Default values are:
     * Numeric types: 0 String: "" Boolean: false Array: []. For id fields, auto
     * generates an incrementing integer starting at 1. Id fields must be named 
     * "id", case insensitive.
     * 
     * @param definitionKey String key of the definitions object corresponding to
     *                      the name of the object
     * @param definitions   Definitions object from Swagger object.
     * @return String representation of object found in definitions. Empty object
     *         "{}" if null.
     */
    public String createDefaultJSONString(String definitionKey, Map<String, Model> definitions) {
        if (definitionKey != null && definitions != null) {
            Model obj = definitions.get(definitionKey);

            if (obj != null) {
                Map<String, Property> fields = obj.getProperties();
                String returnString = "{";

                for (String field : fields.keySet()) {
                    Property p = fields.get(field);
                    String dataType = p.getType();

                    returnString += appendString(dataType, field, definitions, p);

                }

                return returnString + "}";
            }
        }

        return "{}";
    }

    // -------------------------- HELPER METHODS -----------------------------
    private String appendString(String dataType, String field, Map<String, Model> definitions, Property p) {
        String returnString = "";

        if (field.equalsIgnoreCase("id")) {
            returnString += "\"" + field + "\" : \"" + autoId + "\",";
            autoId++;
        } else {

            switch (dataType) {
            case "boolean":
                returnString += "\"" + field + "\" : \" " + false + "\",";
                break;

            case "string":
                returnString += "\"" + field + "\" : \"\",";
                break;

            case "number":
                returnString += "\"" + field + "\" : \"" + 0 + "\",";
                break;

            case "integer":
                returnString += "\"" + field + "\" : \"" + 0 + "\",";
                break;

            case "array":
                // check type of array
                String arrayType = ((ArrayProperty) p).getItems().getType();

                // array of objects
                // add single object
                if (arrayType.equals("ref")) {
                    RefProperty itemProperty = (RefProperty) ((ArrayProperty) p).getItems();
                    String name = itemProperty.getSimpleRef();
                    returnString += "\"" + field + "\" : [" + createDefaultJSONString(name, definitions) + "],";
                } else {
                    returnString += "\"" + field + "\" : [" + appendPrimitiveArray(arrayType) + "],";
                }
                break;

            case "ref":
                // object referenced
                String name = ((RefProperty) p).getSimpleRef();
                returnString += "\"" + field + "\" : " + createDefaultJSONString(name, definitions) + ",";
                break;

            default:
                // TODO log
            }
        }
        return returnString;
    }

    private String appendPrimitiveArray(String dataType) {
        String returnString = "";
        switch (dataType) {
        case "boolean":
            returnString += "false";
            break;

        case "string":
            returnString += "";
            break;

        case "number":
            returnString += "0";
            break;

        case "integer":
            returnString += "0";
            break;
        default:
            break;
        }
        return returnString;
    }

}

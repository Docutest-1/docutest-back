package com.revature.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revature.models.Endpoint;
import com.revature.models.Request;
import io.swagger.models.ArrayModel;
import io.swagger.models.HttpMethod;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OASService {

    private JSONStringCreator jsonCreator;

    @Autowired
    public OASService(JSONStringCreator jsonCreator) {
        this.jsonCreator = jsonCreator;
    }

    /**
     * For OAS2.0/Swagger v1. Generates an arraylist of Request objects based off
     * Swagger input specifications.
     * 
     * @param input
     * @return ArrayList of Requests. Returns an empty array if no endpoints are
     *         found, or if there was an issue parsing the endpoints. If no port is
     *         specified, defaults to port 80.
     */
    public List<Request> getRequests(Swagger input) {
        List<Request> requests = new ArrayList<>();

        if (input != null) {

            String host = input.getHost();

            if (host != null && host.length() > 0) {
                host = host.trim();
                host = host.replace("\"", "");

                String[] splitHost = host.split(":");
                String baseUrl = splitHost[0];
                int port;
                try {
                    port = Integer.parseInt(splitHost[1]);
                } catch (NumberFormatException | IndexOutOfBoundsException e) {
                    port = 80;
                }

                String basePath = input.getBasePath();
                Map<String, Path> endpoints = input.getPaths();

                for (Map.Entry<String, Path> entry : endpoints.entrySet()) {
                    Endpoint endpoint = new Endpoint();
                    endpoint.setBaseUrl(baseUrl);
                    endpoint.setPort(port);
                    endpoint.setBasePath(basePath);
                    endpoint.setPath(entry.getKey());

                    // create request object for each endpoint
                    Path pathOperations = entry.getValue();
                    Map<HttpMethod, Operation> verbs = pathOperations.getOperationMap();

                    for (Map.Entry<HttpMethod, Operation> operationEntry : verbs.entrySet()) {
                        Request req = new Request();

                        req.setEndpoint(endpoint);
                        req.setVerb(operationEntry.getKey());

                        List<Parameter> params = operationEntry.getValue().getParameters();

                        // fill out param info
                        setParams(req, params, input.getDefinitions());

                        requests.add(req);
                    }
                }
            }
        }
        return requests;
    }

    // ------------------------------- HELPER METHODS ------------------------------
    public void setParams(Request req, List<Parameter> params, Map<String, Model> definitions) {
        for (Parameter param : params) {
            // doesn't seem to be any requirements for indices, so we need to check if
            // parameter
            // is an instance of BodyParam/HeaderParam/etc
            if (param instanceof BodyParameter) {
                String body = createBody((BodyParameter) param, definitions);
                req.setBody(body);
            } else if (param instanceof PathParameter) {
                // TODO set path params
            }
            // can add other types of params as needed
        }
    }

    /**
     * Helper method for adding JSON objects for parameters.
     * {@link JSONStringCreator}'s createDefaultJSONString.
     * 
     * @param param       BodyParameter object for the given request.
     * @param definitions Definitions object from the Swagger input
     * @return JSON string representation of the object as defined by definitions.
     */
    private String createBody(BodyParameter param, Map<String, Model> definitions) {
        Model schema = param.getSchema();

        String definitionKey = "";
        String jsonBody = "";

        // schema is different for arrays of objects
        if (schema instanceof ArrayModel) {
            Property items = ((ArrayModel) schema).getItems();
            definitionKey = ((RefProperty) items).getSimpleRef();
            jsonBody = "[" + jsonCreator.createDefaultJSONString(definitionKey, definitions) + "]";
        } else {
            // getting actual object name from $ref: "#/definitions/myObjectName"
            String[] refArr = ((BodyParameter) param).getSchema().getReference().split("/");
            definitionKey = refArr[refArr.length - 1];
            jsonBody = jsonCreator.createDefaultJSONString(definitionKey, definitions);
        }

        return jsonBody;
    }

}

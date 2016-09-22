package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.codegen.examples.ExampleGenerator;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;
import io.swagger.models.Path;
import io.swagger.models.parameters.*;

import java.util.*;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CwapperCodegen extends DefaultCodegen implements CodegenConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CwapperCodegen.class);

    /**
     * Configures the type of generator.
     * 
     * @return the CodegenType for this generator
     * @see io.swagger.codegen.CodegenType
     */
    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    /**
     * Configures a friendly name for the generator. This will be used by the
     * generator to select the library with the -l flag.
     * 
     * @return the friendly name for the generator
     */
    public String getName() {
        return "cwapper";
    }

    /**
     * Returns human-friendly help for the generator. Provide the consumer with
     * help tips, parameters here
     * 
     * @return A string value for the help message
     */
    public String getHelp() {
        return "Generates a C++ API server with C++ cppcms lib";
    }

    public CwapperCodegen() {
        super();

        supportingFiles.add(new SupportingFile("base.mustache", "", "base.cpp"));
        supportingFiles.add(new SupportingFile("service.mustache", "", "service.cpp"));
        supportingFiles.add(new SupportingFile("restful.mustache", "", "restful.hpp"));
        supportingFiles.add(new SupportingFile("cwapper.mustache", "", "cwapper.hpp"));  
    }


    public void preprocessSwagger(Swagger swagger) {
		Map<String, Path> swaggerPaths = swagger.getPaths();

		for(String uri: swaggerPaths.keySet()) {
			Path path = swaggerPaths.get(uri);
			String RE = uri;

			if(path.getParameters() != null) {
				for(Parameter param: path.getParameters()) {
					LOGGER.warn(param.toString());
					//~ if(!(param instanceof PathParameter))
						//~ continue;
						
					String uriParam = String.format("{%s}", param.getName());
					String pattern = "([^/]+)"; // type == string or whatever
					if(((PathParameter)param).getType() == "integer") {
						pattern = "(\\\\d+)";
					}
					
					if(uri.contains(uriParam)) {
						LOGGER.warn(uri);
						LOGGER.warn(uriParam);	
						RE = RE.replace(uriParam, pattern);
					}
				}
				
				path.setVendorExtension("x-cppcms-argsQty", path.getParameters().size());
			} else { LOGGER.warn(path.toString()); }
			
			path.setVendorExtension("x-cppcms-RE", RE);
			path.setVendorExtension("x-pathName", uri.replace("/", "_").replace("{", "").replace("}", ""));
		}
		
		additionalProperties().put("paths", swaggerPaths.values());
	}


    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {

			}
        }
         
        return objs;
    }      
    
    public String escapeUnsafeCharacters(String input) {
        return input;
    }
}

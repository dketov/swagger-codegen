package io.swagger.codegen.languages;

import io.swagger.codegen.*;
import io.swagger.codegen.examples.ExampleGenerator;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;

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

    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
		LOGGER.warn(objs.toString());
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations != null) {
			LOGGER.warn(operations.toString());
            List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
            for (CodegenOperation operation : ops) {
				List<CodegenParameter> params = operation.pathParams;
				if (params != null && params.size() != 0) {
					operation.vendorExtensions.put("x-codegen-size", params.size());
				}
			}
        }
         
        return objs;
    }      
}

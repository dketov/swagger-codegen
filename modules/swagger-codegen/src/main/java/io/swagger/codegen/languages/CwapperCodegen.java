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

    public CodegenType getTag() {
        return CodegenType.SERVER;
    }

    public String getName() {
        return "cwapper";
    }

    public String getHelp() {
        return "Generates a C++ API server with C++ cppcms lib";
    }

    public CwapperCodegen() {
        super();
        
        super.languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList("int", "char", "bool", "long", "float", "double", "int32_t", "int64_t"));
                
        super.typeMapping = new HashMap<String, String>();
        typeMapping.put("string", "std::string");
        typeMapping.put("integer", "int32_t");
        typeMapping.put("long", "int64_t");
        typeMapping.put("boolean", "bool");
        typeMapping.put("array", "std::vector");
        typeMapping.put("map", "std::map");
        typeMapping.put("binary", "std::string");

        super.importMapping = new HashMap<String, String>();
        importMapping.put("std::vector", "#include <vector>");
        importMapping.put("std::map", "#include <map>");
        importMapping.put("std::string", "#include <string>");
     
        supportingFiles.add(new SupportingFile("base.mustache", "", "base.cpp"));
        supportingFiles.add(new SupportingFile("service.mustache", "", "service.cpp"));
        supportingFiles.add(new SupportingFile("restful.mustache", "", "restful.hpp"));
        supportingFiles.add(new SupportingFile("cwapper.mustache", "", "cwapper.hpp"));  
    }


    public void preprocessSwagger(Swagger swagger) {
		Map<String, Path> swaggerPaths = swagger.getPaths();
		
		if(swaggerPaths == null)
			return;

		for(String uri: swaggerPaths.keySet()) {
			Path path = swaggerPaths.get(uri);
			int paramQty = 0;

			path.setVendorExtension("x-cwappper-path", uri.replace("/", "_").replace("{", "").replace("}", ""));

			if(path.getOperations() == null)
				continue;

			for(Operation op: path.getOperations()) {
				
				if(op.getParameters() == null)
					continue;

				for(Parameter param: op.getParameters()) {
					if(!(param instanceof PathParameter))
						continue;
						
					String uriParam = String.format("{%s}", param.getName());
					String pattern = "([^/]+)"; // type == string or whatever
					if(((PathParameter)param).getType() == "integer")
						pattern = "(\\\\d+)";
					
					if(uri.contains(uriParam)) {
						uri = uri.replace(uriParam, pattern);
						paramQty++;
					}
				}
				
			}
				
			path.setVendorExtension("x-cwappper-paramQty", paramQty);
			path.setVendorExtension("x-cwappper-uri", uri);
		}
		
		additionalProperties().put("x-cwappper-paths", swaggerPaths.values());
	}

	private String cwapperRE(CodegenOperation op) {
		String RE = op.path;
		
		for(CodegenParameter param: op.pathParams) {
			String pathParam = String.format("{%s}", param.baseName);
			String pattern = "([^/]+)"; // param.isString == string or whatever

			//if(param.isInteger || param.isLong) o_O NullPointerException ???
				//~ pattern = "(\\\\d+)";
				
			//~ if(param.isFloat || param.isDouble)
				//~ pattern = "(\\\\d*\\\\.\\\\d*)"; // FIX me

			if(RE.contains(pathParam))
				RE = RE.replace(pathParam, pattern);
		}
		
		return RE;
	}
						
    public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
        Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
        if (operations == null)
			return objs;
		
		List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
		if (ops == null)
			return objs;
		
		class CwapperPath {
			public String path, re, fid;

			public List<CodegenOperation> operations = new ArrayList<CodegenOperation>();
			private Set<String> pathParams = new HashSet<String>();
				
			CwapperPath(String _path, String _re) {
				path = _path;
				re = _re;
				fid = path.replace("/", "_").replace("{", "").replace("}", "");
			}
			public String toString() {
				return String.format("%s->%s", path, operations.toString());
			}
			public boolean hasPathParams() { return !pathParams.isEmpty(); }

			public void add(CodegenOperation op) {	
				LOGGER.warn(op.toString());
				LOGGER.warn(op.pathParams.toString());
				for(CodegenParameter p: op.pathParams)
					pathParams.add(p.baseName);
			
				operations.add(op);
			}
		}
		
		Map<String, CwapperPath> cwapperMap = new HashMap<String, CwapperPath> ();
			
		for(CodegenOperation operation : ops) {
			String re = cwapperRE(operation);
			CwapperPath p = cwapperMap.get(operation.path);
			
			if(p == null) {
				p = new CwapperPath(operation.path, re);
				cwapperMap.put(operation.path, p);
			}
			
			p.add(operation);
		}

		List<CwapperPath> xcwapper = (List<CwapperPath>)additionalProperties().get("x-cwapper");
		if(xcwapper == null)
			additionalProperties().put("x-cwapper", new ArrayList<CwapperPath>(cwapperMap.values()));
		else
			xcwapper.addAll(cwapperMap.values());
		
        LOGGER.warn(cwapperMap.entrySet().toString());
        return objs;
    }      
    
    public String escapeUnsafeCharacters(String input) {
        return input;
    }

    public String escapeQuotationMark(String input) {
        return input.replace("\"", "\\\"");
    }
}

package io.swagger.codegen.languages;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.codegen.*;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.models.properties.*;
import io.swagger.models.Path;
import io.swagger.models.parameters.*;
import io.swagger.util.Yaml;

import java.util.*;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CwapperCodegen extends DefaultCodegen implements CodegenConfig {
    protected static final Logger LOGGER = LoggerFactory.getLogger(CwapperCodegen.class);
    private String port = "8888";

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
        
        templateDir = getName();

        languageSpecificPrimitives = new HashSet<String>(
                Arrays.asList("int", "char", "bool", "long", "float", "double", "int32_t", "int64_t"));

        typeMapping = new HashMap<String, String>();
        typeMapping.put("string", "std::string");
        typeMapping.put("integer", "int32_t");
        typeMapping.put("long", "int64_t");
        typeMapping.put("boolean", "bool");
        typeMapping.put("array", "std::vector");
        typeMapping.put("map", "std::map");
        typeMapping.put("binary", "std::string");

        supportingFiles.add(new SupportingFile("base.cpp.mustache", ".", "base.cpp"));
        supportingFiles.add(new SupportingFile("service.cpp.mustache", ".", "service.cpp"));
        supportingFiles.add(new SupportingFile("restful.hpp.mustache", ".", "restful.hpp"));
        supportingFiles.add(new SupportingFile("api.json.mustache", ".", "api.json"));
        supportingFiles.add(new SupportingFile("api.yaml.mustache", ".", "api.yaml"));
        supportingFiles.add(new SupportingFile("config.js.mustache", ".", "config.js"));
        supportingFiles.add(new SupportingFile("Makefile.mustache", ".", "Makefile"));
        supportingFiles.add(new SupportingFile("cwapper.hpp", ".", "cwapper.hpp"));
        supportingFiles.add(new SupportingFile("CMakeLists.txt", ".", "CMakeLists.txt"));
    }

    public void preprocessSwagger(Swagger swagger) {
        String host = swagger.getHost();

        if (host != null) {
            String[] parts = host.split(":");
            if (parts.length > 1) {
                port = parts[1];
            }
        }

        this.additionalProperties.put("serverPort", port);
    }

    private String cwapperRE(CodegenOperation op) {
        String RE = op.path;

        for(CodegenParameter param: op.pathParams) {
            String pathParam = String.format("{%s}", param.baseName);
            String pattern = "([^/]+)"; // param.isString == string or whatever

            //~ if(param.isInteger || param.isLong) // o_O NullPointerException ???
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

        class CwapperPath implements Comparable<CwapperPath> {
            public String path, re, fid;

            public List<CodegenOperation> operations = new ArrayList<CodegenOperation>();
            public List<CodegenParameter> pathParams = null;

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
                pathParams = op.pathParams;

                operations.add(op);
            }
            public int compareTo(CwapperPath other) {
                return re.compareTo(other.re);
            }
        }

        Map<String, CwapperPath> cwapperMap = new TreeMap<String, CwapperPath> ();

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

        return objs;
    }

    public Map<String, Object> postProcessSupportingFileData(Map<String, Object> objs) {
        Swagger swagger = (Swagger)objs.get("swagger");
        if(swagger != null) {
            try {
                objs.put("swagger-yaml", Yaml.mapper().writeValueAsString(swagger));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
            try {
                objs.put("swagger-json", new ObjectMapper().writeValueAsString(swagger));
            } catch (JsonProcessingException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return super.postProcessSupportingFileData(objs);
    }

    public String escapeUnsafeCharacters(String input) {
        return input;
    }

    public String escapeQuotationMark(String input) {
        return input.replace("\"", "\\\"");
    }
    
    public String removeNonNameElementToCamelCase(String name) {
        return removeNonNameElementToCamelCase(name, "[-:;#]");
    }
}

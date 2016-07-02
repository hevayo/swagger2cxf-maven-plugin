/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.maven.plugins;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.JavaCXFServerCodegen;
import io.swagger.models.Operation;
import org.apache.commons.lang.WordUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CxfCodeGen extends JavaCXFServerCodegen {

    Set reserveModelNames;

    public CxfCodeGen(){
        super();
        this.embeddedTemplateDir = this.templateDir = "ApacheCXFJaxRS";

        reserveModelNames = new HashSet<String>(
                Arrays.asList(
                        "String",
                        "boolean",
                        "Boolean",
                        "Double",
                        "Integer",
                        "Long",
                        "Float",
                        "Object",
                        "BigDecimal",
                        "List",
                        "Map",
                        "file")
        );
    }

    @Override
    public String getHelp() {
        return "Generates a Java JAXRS Server application based on Apache CXF framework.";
    }

    @Override
    public String getName() {
        return "jaxrs";
    }

    @Override
    public void processOpts() {
        super.processOpts();
        sourceFolder ="gen"+ File.separator +"java";

        modelTemplateFiles.clear();
        modelTemplateFiles.put("dto.mustache", ".java");

        apiTemplateFiles.clear();
        apiTemplateFiles.put("api.mustache", ".java");
        apiTemplateFiles.put("apiService.mustache", ".java");
        apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
        apiTemplateFiles.put("apiServiceFactory.mustache", ".java");

        supportingFiles.clear();
        supportingFiles.add(new SupportingFile("ApiException.mustache",
                                               (sourceFolder + File.separator + apiPackage).replace(".", java.io.File.separator), "ApiException.java"));
        supportingFiles.add(new SupportingFile("ApiResponseMessage.mustache",
                                               (sourceFolder + File.separator + apiPackage).replace(".", java.io.File.separator), "ApiResponseMessage.java"));
        supportingFiles.add(new SupportingFile("beans.mustache", ("main/webapp/WEB-INF"), "beans.xml"));

    }

    @Override
    public String apiFilename(String templateName, String tag) {

        String result = super.apiFilename(templateName, tag);

        //generate the Impl in main directory
        if (templateName.endsWith("Impl.mustache")) {
            String split[] = result.split(File.separator);
            result = getMainDirectory() + File.separator + "impl" + File.separator + split[split.length -1];
        }

        if (templateName.endsWith("Factory.mustache")) {
            String split[] = result.split(File.separator);
            result = getGenDirectory() + File.separator + "factories" + File.separator + split[split.length -1];
        }

        return result;
    }

    public String getMainDirectory() {
        return getOutputDir() + File.separator + "main" + File.separator + "java" + File.separator + apiPackage
                .replace(".", File.separator);
    }

    public String getGenDirectory() {
        return getOutputDir() + File.separator + "gen" + File.separator + "java" + File.separator + apiPackage
                .replace(".", File.separator);
    }

    @Override
    public String toApiName(String name) {
        name = name.replace('-' ,' ');
        name = WordUtils.capitalize(name);
        name = name.replaceAll("\\s","");
        name = super.toApiName(name);
        return name;
    }

    @Override
    public String toModelName(String name) {
        // model name cannot use reserved keyword, e.g. return
        if (reservedWords.contains(name)) {
            throw new RuntimeException(name + " (reserved word) cannot be used as a model name");
        }
        if(reserveModelNames.contains(name)){
            return camelize(name);
        }
        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name)+"DTO";
    }

    @Override
    public void addOperationToGroup(String tag, String resourcePath, Operation operation, CodegenOperation co,
            Map<String, List<CodegenOperation>> operations) {
        String basePath = resourcePath;
        if (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        int pos = basePath.indexOf("/");
        if (pos > 0) {
            basePath = basePath.substring(0, pos);
        }

        if (basePath.equals("")) {
            basePath = "default";
        } else {
            if (co.path.startsWith("/" + basePath)) {
                co.path = co.path.substring(("/" + basePath).length());
            }
            co.subresourceOperation = !co.path.isEmpty();
        }
        List<CodegenOperation> opList = operations.get(basePath);
        if (opList == null) {
            opList = new ArrayList<CodegenOperation>();
            operations.put(basePath, opList);
        }
        opList.add(co);
        co.baseName = basePath;
    }
}

package org.wso2.maven.plugins;

/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.Swagger;
import io.swagger.parser.SwaggerParser;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;


/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class APIGenMojo extends AbstractMojo {

    /**
     * Location of the output directory.
     */
    @Parameter(name = "output",
            property = "swagger.codegen.maven.plugin.output",
            defaultValue = "${project.basedir}/src/")
    private File output;

    /**
     * Location of the swagger spec, as URL or file.
     */
    @Parameter(required = true)
    private String inputSpec;

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;


    public void execute()
            throws MojoExecutionException {
        Swagger swagger = new SwaggerParser().read(inputSpec);

        CodegenConfig config = new CxfCodeGen();

        config.additionalProperties().put("invokerPackage", project.getArtifact().getArtifactId());
        config.additionalProperties().put("apiPackage", project.getArtifact().getArtifactId());
        config.additionalProperties().put("modelPackage", project.getArtifact().getArtifactId() + ".model");


        config.setOutputDir(output.getAbsolutePath());


        ClientOptInput input = new ClientOptInput().opts(new ClientOpts()).swagger(swagger);
        input.setConfig(config);
        new DefaultGenerator().opts(input).generate();

    }

}

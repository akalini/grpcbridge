package grpcbridge.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

class PublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply plugin: 'maven-publish'
        project.apply plugin: 'java-library'


        project.tasks.create("sourceJar", Jar.class) {
            from project.sourceSets.main.allJava
        }

        project.publishing {
            repositories {
                maven {
                    name = "GitHubPackages"
                    url = "https://maven.pkg.github.com/akalini/grpcbridge"
                    credentials {
                        username = project.findProperty("gpr.user")
                                ?: System.getenv("GITHUB_USERNAME")
                        password = project.findProperty("gpr.key")
                                ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
            publications {
                gpr(MavenPublication) {
                    from project.components.java
                    artifact project.sourceJar {
                        classifier "sources"
                    }
                }
                mavenJava(MavenPublication) {
                    pom {
                        description = 'Expose your gRPC based API as a set of HTTP RESTful endpoints. HTTP framework agnostic.'
                        url = 'https://github.com/akalini/grpcbridge'
                        licenses {
                            license {
                                name = 'The Apache License, Version 2.0'
                                url = 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                            }
                        }
                    }
                }
            }
        }
    }
}

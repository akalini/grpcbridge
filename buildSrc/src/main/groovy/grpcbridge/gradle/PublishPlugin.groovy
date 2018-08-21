package grpcbridge.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar

class PublishPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.apply plugin: 'com.jfrog.bintray'
        project.apply plugin: 'maven'
        project.apply plugin: 'maven-publish'

        project.bintray {
            user = 'akalini'
            key = System.getenv('BINTRAY_KEY')
            publications = ['mavenJava']
            publish = true
            pkg {
                repo = 'maven'
                name = project.group
                userOrg = 'akalini'
                licenses = ['Apache-2.0']
                vcsUrl = 'https://github.com/akalini/grpcbridge'

                version {
                    name = project.version
                    desc = 'Expose your gRPC based API as a set of HTTP RESTful endpoints. HTTP framework agnostic.'
                    released = new Date()
                }
            }
        }

        project.tasks.create("sourceJar", Jar.class) {
            from project.sourceSets.main.allJava
        }

        project.publishing {
            publications {
                mavenJava(MavenPublication) {
                    from project.components.java
                    groupId project.group
                    artifactId project.archivesBaseName
                    version project.version
                    artifact project.sourceJar {
                        classifier "sources"
                    }
                }
            }
        }
    }

}

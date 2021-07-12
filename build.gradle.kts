group = "com.liux.java.charles"
version = "1.1.0-SNAPSHOT"

allprojects {
    buildscript {
        repositories {
            maven {
                setUrl("https://maven.aliyun.com/repository/public/")
            }
            mavenCentral()
            mavenLocal()
        }
    }
    repositories {
        maven {
            setUrl("https://maven.aliyun.com/repository/public/")
        }
        mavenCentral()
        mavenLocal()
    }
}

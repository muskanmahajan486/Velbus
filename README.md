# OpenRemote VELBUS Component

Development
---

* Install JDK 1.8

* Run tests with `./gradlew clean check`

Publishing Artifact
---

Publish local snapshot only:

    ./gradlew clean build publishToMavenLocal

Publish releases to public repository:

    ./gradlew clean build publish -PartifactRepoPassword=<SECRET>

plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.api.utils.utilsSkills)
    implementation(projects.api.death)
    integrationImplementation(projects.api.player)
    integrationImplementation(projects.api.death)
    integrationImplementation(projects.api.utils.utilsSkills)
}

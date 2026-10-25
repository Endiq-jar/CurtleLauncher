package com.app.crutlelauncher.feature.mod.modloader

import com.app.mcgui.ProgressLayout
import com.app.crutlelauncher.R
import com.app.crutlelauncher.feature.download.enums.ModLoader
import com.app.crutlelauncher.feature.mod.ModrinthDirectApi
import com.app.crutlelauncher.feature.version.install.InstallTask
import com.app.crutlelauncher.utils.path.PathManager
import net.endiq.launcher.progresskeeper.ProgressKeeper
import java.io.File
import java.io.IOException

class CrutleClientDownloadTask(private val mcVersion: String) : InstallTask {
    companion object {
        private const val MODRINTH_SLUG = "crutleclient"
    }

    @Throws(Exception::class)
    override fun run(customName: String): File {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.mod_download_progress, "Crutle Client")

        val versionObj = ModrinthDirectApi.findCompatibleVersion(MODRINTH_SLUG, mcVersion, ModLoader.FABRIC)
            ?: throw IOException("No Crutle Client build is published for Minecraft $mcVersion yet.")
        val primaryFile = ModrinthDirectApi.primaryFileOf(versionObj)
            ?: throw IOException("Crutle Client's Modrinth listing for Minecraft $mcVersion has no downloadable file.")
        val url = primaryFile.get("url")?.takeIf { it.isJsonPrimitive }?.asString
            ?: throw IOException("Crutle Client's Modrinth file entry is missing a download URL.")
        val fileName = primaryFile.get("filename")?.takeIf { it.isJsonPrimitive }?.asString
            ?: "CrutleClient-$mcVersion.jar"

        val destinationFile = File(PathManager.DIR_CACHE, fileName)
        ModrinthDirectApi.downloadTo(url, destinationFile)

        ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
        return destinationFile
    }
}

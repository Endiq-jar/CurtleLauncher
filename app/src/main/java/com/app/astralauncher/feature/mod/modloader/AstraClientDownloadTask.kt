package com.app.astralauncher.feature.mod.modloader

import com.app.mcgui.ProgressLayout
import com.app.astralauncher.R
import com.app.astralauncher.feature.download.enums.ModLoader
import com.app.astralauncher.feature.mod.ModrinthDirectApi
import com.app.astralauncher.feature.version.install.InstallTask
import com.app.astralauncher.utils.path.PathManager
import net.endiq.launcher.progresskeeper.ProgressKeeper
import java.io.File
import java.io.IOException

class AstraClientDownloadTask(private val mcVersion: String) : InstallTask {
    companion object {
        private const val MODRINTH_SLUG = "astraclient"
    }

    @Throws(Exception::class)
    override fun run(customName: String): File {
        ProgressKeeper.submitProgress(ProgressLayout.INSTALL_RESOURCE, 0, R.string.mod_download_progress, "Astra Client")

        val versionObj = ModrinthDirectApi.findCompatibleVersion(MODRINTH_SLUG, mcVersion, ModLoader.FABRIC)
            ?: throw IOException("No Astra Client build is published for Minecraft $mcVersion yet.")
        val primaryFile = ModrinthDirectApi.primaryFileOf(versionObj)
            ?: throw IOException("Astra Client's Modrinth listing for Minecraft $mcVersion has no downloadable file.")
        val url = primaryFile.get("url")?.takeIf { it.isJsonPrimitive }?.asString
            ?: throw IOException("Astra Client's Modrinth file entry is missing a download URL.")
        val fileName = primaryFile.get("filename")?.takeIf { it.isJsonPrimitive }?.asString
            ?: "AstraClient-$mcVersion.jar"

        val destinationFile = File(PathManager.DIR_CACHE, fileName)
        ModrinthDirectApi.downloadTo(url, destinationFile)

        ProgressLayout.clearProgress(ProgressLayout.INSTALL_RESOURCE)
        return destinationFile
    }
}

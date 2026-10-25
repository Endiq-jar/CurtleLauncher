package com.app.crutlelauncher.event.single

/**
 * Broadcast when the version list is refreshed.
 * Version refreshes happen asynchronously, so event handling must move to the UI thread.
 * @see com.app.crutlelauncher.feature.version.VersionsManager
 */
class RefreshVersionsEvent(val mode: MODE) {
    enum class MODE {
        START, END
    }
}
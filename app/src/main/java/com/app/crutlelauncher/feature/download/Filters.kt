package com.app.crutlelauncher.feature.download

import com.app.crutlelauncher.feature.download.enums.Category
import com.app.crutlelauncher.feature.download.enums.ModLoader
import com.app.crutlelauncher.feature.download.enums.Sort


/**
 * Provides the filter info used for platform searches.
 */
class Filters {
    var name: String = ""
    var mcVersion: String? = null
    var modloader: ModLoader? = null
    var sort: Sort = Sort.RELEVANT
    var category: Category = Category.ALL

    override fun toString(): String {
        return "Filters(name='$name', mcVersion=$mcVersion, modloader=$modloader, sort=$sort, category=$category)"
    }
}
package com.topjohnwu.magisk.services

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActionTileService : TileService() {
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onClick() {
        val tile = qsTile ?: return
        
        if (Shell.isAppGrantedRoot() != true) {
            Toast.makeText(this, "Root access required", Toast.LENGTH_SHORT).show()
            return
        }

        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()

        scope.launch {
            withContext(Dispatchers.IO) {
                // Execute the custom script configured in Labs
                val script = com.topjohnwu.magisk.core.Config.customQsScript
                Shell.cmd(script).exec()
            }
            
            Toast.makeText(this@ActionTileService, "Action Script Executed", Toast.LENGTH_SHORT).show()
            
            tile.state = Tile.STATE_INACTIVE
            tile.updateTile()
        }
    }

    override fun onStartListening() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.updateTile()
    }
}

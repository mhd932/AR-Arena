package com.example.ararena

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ar.core.Pose
import io.github.sceneview.ar.ARScene

class MainActivity : ComponentActivity() {
    private val me = Player(android.os.Build.MODEL + "-" + (1000..9999).random(), Build.MODEL)
    private val others = mutableMapOf<String, Player>()
    private var origin by mutableStateOf<Pose?>(null)        // repère commun, fixé par "Calibrer"
    private var camPose: Pose? = null
    private lateinit var net: Net
    private var hud by mutableStateOf("En attente...")
    private var tick = 0
    private var revision by mutableIntStateOf(0)
    private var permissionsGranted by mutableStateOf(false)

    private val perms = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        permissionsGranted = requiredPermissions().all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (permissionsGranted) net.start() else hud = "Permissions refusées : relancer et autoriser caméra et proximité"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        net = Net(this, me.id) { _, msg -> runOnUiThread { onMessage(msg) } }
        perms.launch(requiredPermissions())

        setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize()) {
                    if (permissionsGranted) ARScene(
                        modifier = Modifier.fillMaxSize(),
                        onSessionUpdated = { _, frame ->
                            camPose = frame.camera.pose
                            origin?.let { o ->
                                val t = o.inverse().transformPoint(frame.camera.pose.translation)
                                me.pos = V3(t[0], t[1], t[2])
                                if (++tick % 3 == 0)   // ~20 Hz
                                    net.broadcast("POS|${me.id}|${t[0]}|${t[1]}|${t[2]}")
                            }
                        }
                    )
                    @Suppress("UNUSED_VARIABLE") val currentRevision = revision
                    Column(Modifier.align(Alignment.TopStart).padding(16.dp)) {
                        Text("PV ${me.hp}  |  Kills ${me.kills}  |  Morts ${me.deaths}")
                        Text("Joueurs connectés : ${net.peers.size}   $hud")
                    }
                    Column(Modifier.align(Alignment.BottomCenter).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Button(onClick = { calibrate() }) { Text("Calibrer (tous au même point, même direction)") }
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { shoot() }, enabled = me.alive && origin != null) { Text("TIR") }
                    }
                }
            }
        }
    }

    private fun requiredPermissions(): Array<String> = buildList {
        add(Manifest.permission.CAMERA)
        add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT <= 31) add(Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 31) {
            add(Manifest.permission.BLUETOOTH_ADVERTISE)
            add(Manifest.permission.BLUETOOTH_CONNECT)
            add(Manifest.permission.BLUETOOTH_SCAN)
        }
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.NEARBY_WIFI_DEVICES)
    }.toTypedArray()

    private fun calibrate() { camPose?.let { origin = it; hud = "Calibre" } }

    private fun shoot() {
        val cp = camPose ?: return; val o = origin ?: return
        val f = o.inverse().rotateVector(cp.rotateVector(floatArrayOf(0f, 0f, -1f)))
        val target = GameLogic.findTarget(me.pos, V3(f[0], f[1], f[2]), others.values)
        if (target != null) {
            val killed = GameLogic.applyHit(me, target)
            net.broadcast("HIT|${me.id}|${target.id}|$killed")
            hud = if (killed) "Éliminé : ${target.name}" else "Touché !"
        }
    }

    private fun onMessage(msg: String) {
        val p = msg.split("|")
        if (p.size < 4) return
        if (p[0] == "POS" && (p.size != 5 || p.drop(2).any { it.toFloatOrNull()?.isFinite() != true })) return
        when (p[0]) {
            "POS" -> others.getOrPut(p[1]) { Player(p[1], p[1]) }.pos = V3(p[2].toFloat(), p[3].toFloat(), p[4].toFloat())
            "HIT" -> if (p[2] == me.id) {
                me.hp -= GameLogic.DAMAGE
                hud = "Touché par ${p[1]}"
                if (me.hp <= 0) { me.deaths++; GameLogic.respawn(me); hud = "Mort — réapparition" }
            } else if (p[3] == "true") others[p[1]]?.let { it.kills++ }
        }
        revision++
    }

    override fun onDestroy() { net.stop(); super.onDestroy() }
}

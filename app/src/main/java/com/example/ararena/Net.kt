package com.example.ararena
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
/** Multijoueur local sans serveur ni internet : Nearby Connections (Wi-Fi/BLE), maillage P2P. */
class Net(ctx: Context, private val myName: String, private val onMsg: (String, String) -> Unit) {
    private val client = Nearby.getConnectionsClient(ctx)
    private val strategy = Strategy.P2P_CLUSTER
    private val service = "com.example.ararena"
    val peers = mutableStateListOf<String>()
    private val payloadCb = object : PayloadCallback() {
        override fun onPayloadReceived(id: String, p: Payload) {
            p.asBytes()?.let { onMsg(id, String(it, Charsets.UTF_8)) }
        }
        override fun onPayloadTransferUpdate(id: String, u: PayloadTransferUpdate) {}
    }
    private val connCb = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(id: String, i: ConnectionInfo) {
            client.acceptConnection(id, payloadCb)
        }
        override fun onConnectionResult(id: String, r: ConnectionResolution) {
            if (r.status.isSuccess && id !in peers) peers.add(id)
        }
        override fun onDisconnected(id: String) { peers.remove(id) }
    }
    fun start() {
        client.startAdvertising(myName, service, connCb,
            AdvertisingOptions.Builder().setStrategy(strategy).build())
        client.startDiscovery(service, object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(id: String, i: DiscoveredEndpointInfo) {
                client.requestConnection(myName, id, connCb)
            }
            override fun onEndpointLost(id: String) {}
        }, DiscoveryOptions.Builder().setStrategy(strategy).build())
    }
    fun broadcast(msg: String) {
        if (peers.isNotEmpty()) client.sendPayload(peers.toList(), Payload.fromBytes(msg.toByteArray(Charsets.UTF_8)))
    }
    fun stop() { client.stopAllEndpoints(); client.stopAdvertising(); client.stopDiscovery(); peers.clear() }
}

package com.nilsson.tipspromenad

import android.content.Context
import android.os.Handler
import android.os.Looper
import org.json.JSONArray
import org.json.JSONObject
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.UUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/** Minimal bounded HTTP transport; quiz validation/scoring uses the shared JS engine. */
object LocalQuizServer {
    private var socket: ServerSocket? = null
    private val executor = ThreadPoolExecutor(4, 8, 30, TimeUnit.SECONDS, ArrayBlockingQueue(32))
    private val waiting = ConcurrentHashMap<String, CompletableFuture<String>>()
    @Volatile var engine: ((String, String) -> Unit)? = null
    private val main = Handler(Looper.getMainLooper())
    @Synchronized fun start(context: Context) {
        if (socket != null) return
        val listener = ServerSocket(8086).also { socket = it }
        Thread({
            while (!listener.isClosed) {
                try { val client = listener.accept(); try { executor.execute { serve(context.applicationContext, client) } } catch (_: Exception) { client.close() } }
                catch (_: Exception) { break }
            }
        }, "Quiz-LAN-accept").start()
    }
    @Synchronized fun stop() { socket?.close(); socket = null; engine = null; waiting.values.forEach { it.complete(unavailable()) }; waiting.clear() }
    fun status(): String {
        val addresses = JSONArray()
        try { NetworkInterface.getNetworkInterfaces().toList().filter { it.isUp && !it.isLoopback }.forEach { network -> network.inetAddresses.toList().filterIsInstance<Inet4Address>().forEach { addresses.put("http://${it.hostAddress}:8086") } } } catch (_: Exception) { }
        return JSONObject().put("localHost", socket != null && engine != null).put("addresses", addresses).toString()
    }
    fun reply(id: String, response: String) { waiting.remove(id)?.complete(response) }
    fun dispatch(request: String, complete: (String) -> Unit) {
        try { executor.execute { complete(callEngine(request)) } } catch (_: Exception) { complete(unavailable()) }
    }
    private fun unavailable() = "{\"status\":503,\"body\":{\"error\":\"host_unavailable\"}}"
    private fun callEngine(request: String): String {
        val callback = engine ?: return unavailable()
        val id = UUID.randomUUID().toString(); val future = CompletableFuture<String>(); waiting[id] = future
        main.post { if (engine === callback) callback(id, request) else reply(id, unavailable()) }
        return try { future.get(8, TimeUnit.SECONDS) } catch (_: Exception) { unavailable() } finally { waiting.remove(id) }
    }
    private fun serve(context: Context, client: Socket) {
        client.use { connection ->
            connection.soTimeout = 5000
            try {
                val input = connection.getInputStream().buffered()
                fun line(): String {
                    val out = StringBuilder()
                    while (true) { val b = input.read(); if (b < 0 || b == 10) break; if (b != 13) out.append(b.toChar()); require(out.length < 4096) }
                    return out.toString()
                }
                val first = line().split(' '); require(first.size == 3)
                val method = first[0]; val path = URLDecoder.decode(first[1].substringBefore('?'), "UTF-8")
                val headers = mutableMapOf<String,String>(); var bytes = 0
                while (true) { val h = line(); if (h.isEmpty()) break; bytes += h.length; require(bytes < 12000); val parts = h.split(':', limit=2); require(parts.size==2); headers[parts[0].lowercase()] = parts[1].trim() }
                fun send(code: Int, type: String, body: ByteArray) {
                    val output = connection.getOutputStream(); output.write("HTTP/1.1 $code Response\r\nContent-Type: $type\r\nContent-Length: ${body.size}\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nConnection: close\r\n\r\n".toByteArray()); output.write(body); output.flush()
                }
                fun json(code: Int, body: String) = send(code,"application/json; charset=utf-8",body.toByteArray(Charsets.UTF_8))
                val origin = headers["origin"]
                if (origin != null && origin != "http://${headers["host"]}") { json(403,"{\"error\":\"origin\"}"); return }
                if (path=="/api/status" && method=="GET") { json(200,status()); return }
                if (path=="/api/hosts" && method=="POST") { json(403,"{\"error\":\"phone_host_only\"}"); return }
                if (path.startsWith("/api/")) {
                    require(method=="GET" || method=="POST"); require(headers["transfer-encoding"]==null)
                    val length=headers["content-length"]?.toInt()?:0; require(length in 0..12000)
                    val body=ByteArray(length); var offset=0; while(offset<length){val count=input.read(body,offset,length-offset);require(count>0);offset+=count}
                    val request=JSONObject().put("method",method).put("path",path).put("authorization",headers["authorization"]?:"").put("data",if(body.isEmpty()) JSONObject() else JSONObject(String(body,Charsets.UTF_8)))
                    val response=JSONObject(callEngine(request.toString())); json(response.getInt("status"),response.getJSONObject("body").toString()); return
                }
                require(method=="GET")
                val relative=if(path=="/") "index.html" else path.removePrefix("/")
                if(relative.split('/').any { it.startsWith('.') || it.contains('\\') } || !Regex("(index\\.html|script\\.js|styles\\.css|walk\\.css|service-worker\\.js|(lib|locales|Data)/[a-zA-Z0-9_./-]+)").matches(relative)) { json(404,"{}"); return }
                val type=when(relative.substringAfterLast('.')) { "html"->"text/html";"js"->"text/javascript";"json"->"application/json";"css"->"text/css";else->{json(404,"{}");return} }
                try { context.assets.open("quiz/$relative").use { send(200,"$type; charset=utf-8",it.readBytes()) } } catch (_: Exception) { json(404,"{}") }
            } catch (_: Exception) { /* Invalid/oversized requests close without touching host data. */ }
        }
    }
}

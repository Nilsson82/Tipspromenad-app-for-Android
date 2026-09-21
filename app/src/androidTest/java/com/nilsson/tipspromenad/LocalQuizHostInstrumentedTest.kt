package com.nilsson.tipspromenad

import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.net.Socket
import java.nio.ByteBuffer
import java.util.Base64
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.zip.CRC32

@RunWith(AndroidJUnit4::class)
class LocalQuizHostInstrumentedTest {
    private fun crc(bytes:ByteArray)=CRC32().apply{update(bytes)}.value
    private fun code(prefix:String,value:JSONObject):String {val bytes=value.toString().toByteArray();return prefix+Base64.getUrlEncoder().withoutPadding().encodeToString(bytes+ByteBuffer.allocate(4).putInt(crc(bytes).toInt()).array())}
    private fun nativeRequest(method:String,path:String,data:JSONObject=JSONObject(),authorization:String=""):JSONObject {
        val latch=CountDownLatch(1);var response=""
        LocalQuizServer.dispatch(JSONObject().put("method",method).put("path",path).put("data",data).put("authorization",authorization).toString()){response=it;latch.countDown()}
        assertTrue("Native engine timed out",latch.await(12,TimeUnit.SECONDS));return JSONObject(response)
    }
    private fun http(method:String,path:String,data:JSONObject?=null,authorization:String="",origin:String?=null):Pair<Int,JSONObject> {
        Socket("127.0.0.1",8086).use {socket->socket.soTimeout=12000;val body=data?.toString()?.toByteArray()?:ByteArray(0)
            val headers="$method $path HTTP/1.1\r\nHost: 127.0.0.1:8086\r\nContent-Type: application/json\r\nContent-Length: ${body.size}\r\nAuthorization: $authorization\r\n"+(if(origin!=null)"Origin: $origin\r\n" else "")+"Connection: close\r\n\r\n"
            socket.getOutputStream().write(headers.toByteArray()+body);val response=socket.getInputStream().bufferedReader().readText();val status=response.substringBefore("\r\n").split(' ')[1].toInt();return status to JSONObject(response.substringAfter("\r\n\r\n"))
        }
    }
    @Test fun phoneServesQuizAndCollectsParticipantResultsWithoutCloud() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(MainActivity::class.java).use {
            ContextCompat.startForegroundService(context,Intent(context,LocalQuizHostService::class.java))
            try {
                val end=System.currentTimeMillis()+15000
                while(!JSONObject(LocalQuizServer.status()).getBoolean("localHost")&&System.currentTimeMillis()<end)Thread.sleep(100)
                assertTrue(JSONObject(LocalQuizServer.status()).getBoolean("localHost"))
                val quiz=JSONObject().put("version",2).put("revision",2).put("quizId","1234567890abcdef").put("seed",1).put("created",1).put("name","Phone host test").put("language","en").put("answerCount",4).put("display","all").put("walk","none").put("walkValue",0).put("resultMode","collect").put("tieBreakerId",79).put("questionIds",JSONArray().put(31).put(32))
                val quizCode=code("TIPQ2.",quiz)
                val hosted=nativeRequest("POST","/api/hosts",JSONObject().put("code",quizCode));assertEquals(hosted.toString(),201,hosted.getInt("status"));val joinCode=hosted.getJSONObject("body").getString("joinCode");val admin=hosted.getJSONObject("body").getString("adminToken")
                assertEquals(403,http("POST","/api/hosts",JSONObject().put("code",quizCode)).first)
                val joined=http("POST","/api/join",JSONObject().put("joinCode",joinCode).put("name","Local participant"));assertEquals(200,joined.first)
                val result=JSONObject().put("version",2).put("quizId",quiz.getString("quizId")).put("resultId","abcdef1234567890").put("fingerprint",crc(quizCode.toByteArray())).put("name","Local participant").put("estimate",40000).put("answers",JSONArray().put(0).put(0))
                val submission=JSONObject().put("joinCode",joinCode).put("participantToken",joined.second.getString("participantToken")).put("code",code("TIPR2.",result))
                assertEquals(200,http("POST","/api/results",submission).first);assertEquals(200,http("POST","/api/results",submission).first)
                val board=http("GET","/api/hosts/$joinCode",authorization="Bearer $admin");assertEquals(1,board.second.getInt("count"));assertEquals(2,board.second.getJSONArray("rows").getJSONObject(0).getInt("correct"));assertEquals(75,board.second.getJSONArray("rows").getJSONObject(0).getInt("tieDifference"))
                assertEquals(403,http("GET","/api/hosts/$joinCode").first)
                assertEquals(403,http("POST","/api/join",JSONObject(),origin="https://unrelated.example").first)
            } finally {context.stopService(Intent(context,LocalQuizHostService::class.java))}
        }
    }
}

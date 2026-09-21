package com.nilsson.tipspromenad

import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.graphics.Bitmap
import java.io.File
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class OfflineWalkInstrumentedTest {
    private fun webView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) webView(view.getChildAt(i))?.let { return it }
        return null
    }
    @Test fun bundledCreatorParticipantAndScoringFlow() {
        val originalLocales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(androidx.core.os.LocaleListCompat.forLanguageTags("en"))
        }
        try {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            lateinit var web: WebView
            scenario.onActivity { web = requireNotNull(webView(it.window.decorView)) }
            fun js(script: String): String {
                scenario.onActivity { web = requireNotNull(webView(it.window.decorView)) }
                val done = CountDownLatch(1)
                var answer = ""
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    web.evaluateJavascript(script) { answer = it; done.countDown() }
                }
                assertTrue("JavaScript callback timed out", done.await(5, TimeUnit.SECONDS))
                return answer
            }
            fun waitFor(script: String) {
                val end = System.currentTimeMillis() + 15000
                while (System.currentTimeMillis() < end) {
                    if (js(script) == "true") return
                    Thread.sleep(100)
                }
                throw AssertionError("UI did not reach expected state: $script; ${js("document.body.innerText")}")
            }
            fun click(action: String) { js("document.querySelector('[data-action=\"$action\"]').click()") }
            fun screenshot(name: String) {
                val painted = CountDownLatch(1)
                InstrumentationRegistry.getInstrumentation().runOnMainSync {
                    web.postVisualStateCallback(0, object : WebView.VisualStateCallback() {
                        override fun onComplete(requestId: Long) { painted.countDown() }
                    })
                }
                assertTrue(painted.await(5, TimeUnit.SECONDS))
                InstrumentationRegistry.getInstrumentation().waitForIdleSync()
                Thread.sleep(250)
                val context = InstrumentationRegistry.getInstrumentation().targetContext
                val output = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
                val directory = if (output != null) File(output, "qa-screenshots").apply { mkdirs() }
                    else File(context.getExternalFilesDir(null), "qa-screenshots").apply { mkdirs() }
                val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
                File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                bitmap.recycle()
            }
            waitFor("!!document.querySelector('[data-action=create]')")
            assertTrue(js("location.href").contains("appassets.androidplatform.net"))
            // Test-owned records have a unique prefix. Preserve the user's previous draft/settings.
            js("window.testBackupReady=false;Promise.all([WalkStore.get('attempt'),WalkStore.get('settings')]).then(v=>{window.testBackups=v;window.testBackupReady=true;})")
            waitFor("window.testBackupReady===true")
            val backups = js("window.testBackups")
            try {
                screenshot("main-menu")
                js("window.dispatchEvent(new Event('tipspromenad:settings'))")
                waitFor("!!document.querySelector('dialog[open]')")
                screenshot("settings")
                js("(()=>{const d=document.querySelector('dialog');const s=d.querySelectorAll('select');s[0].value='en';s[1].value='en';s[2].value='3';s[3].value='all';s[4].value='none';const n=d.querySelectorAll('input[type=number]');n[0].value=5;n[1].value=0;d.querySelector('[data-action=save]').click()})()")
                waitFor("!document.querySelector('dialog[open]')")
                waitFor("!!document.querySelector('[data-action=random]')")
                click("random")
                assertEquals("0", js("document.querySelectorAll('.walk-content select,.walk-content input').length"))
                assertTrue(js("document.querySelector('.settings-summary').textContent").contains("5"))
                screenshot("random-quiz")
                click("start")
                waitFor("document.querySelectorAll('.question-card').length===5")
                assertEquals("0", js("document.querySelectorAll('.participant-name').length"))
                js("document.querySelectorAll('.question-card').forEach(card=>card.querySelector('input[type=radio]').click())")
                click("finish")
                waitFor("!!document.querySelector('.walk-score')")
                assertEquals("5", js("document.querySelectorAll('.walk-answer-correct').length"))
                assertEquals("0", js("document.querySelectorAll('[data-action=finish],.walk-content textarea,.question-card input:enabled').length"))
                assertTrue(js("Array.from(document.querySelectorAll('.question-card'))[0].querySelectorAll('.option-symbol')[1].textContent").contains("X"))
                screenshot("random-results")
                click("home")
                waitFor("!!document.querySelector('[data-action=resume]')")
                click("resume")
                waitFor("!!document.querySelector('.walk-score')")
                click("home")
                waitFor("!!document.querySelector('[data-action=create]')")
                js("window.singleReady=false;WalkStore.get('attempt').then(s=>{s.completed=false;s.quiz.display='one';s.quiz.walk='time';s.quiz.walkValue=180;s.index=0;s.unlocked=0;s.deadline=0;return WalkStore.put('attempt',s)}).then(()=>window.singleReady=true)")
                waitFor("window.singleReady===true")
                click("random")
                click("home")
                waitFor("!!document.querySelector('[data-action=resume]')")
                click("resume")
                waitFor("document.querySelectorAll('.question-card').length===1")
                screenshot("single-question")
                waitFor("!!document.querySelector('.unlock-status')")
                assertEquals("true", js("document.querySelector('[data-action=next]').disabled"))
                assertTrue(js("document.querySelector('.question-card legend').textContent").contains("1."))
                click("home")
                waitFor("!!document.querySelector('[data-action=resume]')")
                js("window.singleReady=false;WalkStore.get('attempt').then(s=>{s.gates[0].startedAt=Date.now()-181000;s.gates[0].elapsed=181;return WalkStore.put('attempt',s)}).then(()=>window.singleReady=true)")
                waitFor("window.singleReady===true")
                click("random")
                click("home")
                waitFor("!!document.querySelector('[data-action=resume]')")
                click("resume")
                waitFor("document.querySelector('[data-action=next]')?.disabled===false")
                assertTrue(js("document.querySelector('.question-card legend').textContent").contains("1."))
                click("next")
                waitFor("document.querySelector('.question-card legend')?.textContent.startsWith('2.')===true")
                click("previous")
                waitFor("document.querySelector('.question-card legend')?.textContent.startsWith('1.')===true")
                click("next")
                waitFor("document.querySelector('.question-card legend')?.textContent.startsWith('2.')===true")
                click("home")
                waitFor("!!document.querySelector('[data-action=create]')")
                click("create")
                assertEquals("5", js("document.querySelector('.wizard-body input[type=number]').value").trim('"'))
                js("(()=>{const n=document.querySelector('.walk-content input[type=text]');n.value='Instrumented walk';n.dispatchEvent(new Event('input'))})()")
                js("(()=>{const tie=document.querySelector('.wizard-body select');tie.value='79';tie.dispatchEvent(new Event('change'));})()")
                screenshot("create-quiz")
                click("next")
                click("previous")
                assertTrue(js("document.querySelector('.walk-content input').value").contains("Instrumented walk"))
                click("next")
                screenshot("create-categories")
                click("next")
                screenshot("create-selection")
                click("next")
                assertEquals("5", js("document.querySelectorAll('.review-questions li').length"))
                screenshot("create-review")
                click("create")
                waitFor("!!document.querySelector('.walk-content textarea')")
                js("window.testQuizCode=document.querySelector('.walk-content textarea').value;window.testQuiz=WalkCore.decodeQuiz(window.testQuizCode)")
                assertEquals("5", js("window.testQuiz.questionIds.length"))
                screenshot("share-quiz")
                click("home")
                waitFor("!!document.querySelector('[data-action=join]')")
                click("join")
                js("document.querySelector('.walk-content input').value=window.testQuizCode")
                click("join")
                waitFor("!!document.querySelector('[data-action=start]')")
                js("document.querySelector('.walk-content input').value='Emulator participant'")
                click("start")
                waitFor("document.querySelectorAll('.question-card').length===5")
                assertTrue(js("document.querySelector('.participant-name').textContent").contains("Emulator participant"))
                screenshot("quiz")
                js("document.querySelectorAll('.question-card').forEach(card=>card.querySelector('input[type=radio]').click())")
                waitFor("!!document.querySelector('.tie-question input')")
                js("(()=>{const estimate=document.querySelector('.tie-question input');estimate.value='40000';estimate.dispatchEvent(new Event('input'));})()")
                click("finish")
                waitFor("!!document.querySelector('.walk-content textarea')")
                js("window.testResultCode=document.querySelector('.walk-content textarea').value")
                assertTrue(js("window.testResultCode").contains("TIPR2."))
                click("home")
                waitFor("!!Array.from(document.querySelectorAll('[data-action=open]')).find(b=>b.textContent==='Instrumented walk')")
                js("Array.from(document.querySelectorAll('[data-action=open]')).find(b=>b.textContent==='Instrumented walk').click()")
                waitFor("!!document.querySelector('[data-action=import]')")
                click("import")
                waitFor("!!document.querySelector('.walk-content input')")
                js("document.querySelector('.walk-content input').value=window.testResultCode;Array.from(document.querySelectorAll('[data-action=import]')).at(-1).click()")
                waitFor("!!document.querySelector('.walk-table td')")
                assertTrue(js("document.querySelector('.walk-table').innerText").contains("Emulator participant"))
                assertEquals("1", js("document.querySelectorAll('.walk-table tr').length-1"))
                screenshot("results")
                click("home")
                waitFor("!!document.querySelector('[data-action=join]')")
                click("join")
                js("document.querySelector('.walk-content input').value=window.testQuizCode")
                click("join")
                waitFor("!!document.querySelector('[data-action=start]')")
                assertTrue(js("document.querySelector('.walk-content input').value").contains("Emulator participant"))
                // Verify indexed storage survives a new page, not just in-memory rendering.
                js("window.testSaved=false;WalkStore.get('quiz:'+window.testQuiz.quizId).then(v=>window.testSaved=v.results.length===1)")
                waitFor("window.testSaved===true")
            } finally {
                js("window.testBackups=$backups;window.testClean=false;Promise.all([WalkStore.put('attempt',window.testBackups[0]),WalkStore.put('settings',window.testBackups[1])]).then(()=>{const r=indexedDB.open('tipspromenad-walks',1);r.onsuccess=()=>{const db=r.result;const tx=db.transaction('records','readwrite');if(window.testQuiz)tx.objectStore('records').delete('quiz:'+window.testQuiz.quizId);tx.oncomplete=()=>{db.close();window.testClean=true;};};})")
                waitFor("window.testClean===true")
            }
        }
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(originalLocales)
            }
        }
    }
}

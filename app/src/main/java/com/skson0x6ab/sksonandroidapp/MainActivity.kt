package com.skson0x6ab.sksonandroidapp

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.json.JSONObject
import java.io.File
import okhttp3.*
import java.io.IOException
import java.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import android.util.Log
import android.widget.TextView
import org.json.JSONArray

class MainActivity : ComponentActivity() {

    private val client = OkHttpClient()
    private val TAG = "MainActivity"
    private lateinit var textview1 : TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button1 = findViewById<Button>(R.id.button_1)
        val button2 = findViewById<Button>(R.id.button_2)
        val button3 = findViewById<Button>(R.id.button_3)
        val button4 = findViewById<Button>(R.id.button_4)
        val button5 = findViewById<Button>(R.id.button_5)
        val button6 = findViewById<Button>(R.id.button_6)
        textview1 = findViewById(R.id.textview_1)

        button1.setOnClickListener {
            textview1.text = ""
            val url = "https://www.ff14.co.kr/news/notice?category=3"
            val jsonName = "FF14.json"
            val masterParsingRule = "table.ff14_board_list tbody tr"
            val subParsingRule = arrayListOf("td.type", "td .title a", "td.date")
            val subItem = arrayListOf("category", "text", "date")
            fetchWebPage(url, jsonName, masterParsingRule, subParsingRule, subItem)
        }
        button2.setOnClickListener {
            textview1.text = ""
            val url = "https://maplestory.nexon.com/News/Update"
            val jsonName = "Maplestory.json"
            val masterParsingRule = "div.update_board ul li"
            val subParsingRule = arrayListOf("empty","p a span", ".heart_date dl dd")
            val subItem = arrayListOf("category", "text", "date")
            fetchWebPage(url, jsonName, masterParsingRule, subParsingRule, subItem)
        }
        button3.setOnClickListener {
            textview1.text = ""
            val url = "https://genshin.hoyoverse.com/m/ko/news/"
            val jsonName = "Genshin.json"
            val masterParsingRule = "script"
            val subParsingRule = arrayListOf(",\"2024-",",")
            val subItem = arrayListOf("category", "text", "date")
            fetchWebPageNuxtGenshin(url, jsonName, masterParsingRule, subParsingRule, subItem)
        }
        button4.setOnClickListener {
            textview1.text = ""
            val url = "https://hsr.hoyoverse.com/ko-kr/news"
            val jsonName = "StarRail.json"
            val masterParsingRule = "script"
            val subParsingRule = arrayListOf("sTitle:","\"","dtStartTime:")
            val subItem = arrayListOf("category", "text", "date")
            fetchWebPageNuxtStarRail(url, jsonName, masterParsingRule, subParsingRule, subItem)
        }
    }

    private fun fetchWebPage(url: String, jsonName: String, masterParsingRule: String, subParsingRule: ArrayList<String>, subItem: ArrayList<String>) {
        // 비동기적으로 네트워크 작업 수행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OkHttp를 사용해 HTTP 요청 보내기
                val request = Request.Builder().url(url).build()
                Log.i(TAG, "HTTP Request")
                textview1.append("HTTP Request\n")

                val response = client.newCall(request).execute()
                Log.i(TAG, "HTTP Response")
                textview1.append("HTTP Response\n")

                // 응답이 성공적이면
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val document: Document = Jsoup.parse(responseBody)

                    // 데이터 파싱
                    val datas = document.select(masterParsingRule)

                    // JSON 객체 생성
                    //var jsonObject = JSONObject()
                    var jsonArray = JSONArray()

                    var num = 1
                    for(data in datas){
                        Log.i(TAG, data.toString())
                        val tmpJsonObject = JSONObject()
                        for(i in 0 until subItem.size){
                            val tmpData =
                                if (subParsingRule[i] == "empty"){
                                    num.toString()

                                }else{
                                    data.select(subParsingRule[i]).text()
                                }
                            tmpJsonObject.put(subItem[i], tmpData)
                        }
                        num += 1
                        jsonArray.put(tmpJsonObject)
                    }

                    // JSON 문자열로 변환
                    val jsonString = jsonArray.toString()
                    Log.i(TAG, jsonString)

                    // JSON 파일로 저장
                    val jsonFile = File(filesDir, jsonName)
                    jsonFile.writeText(jsonString)

                    // GitHub에 업로드
                    uploadToGitHub(jsonString, jsonName)
                } else {
                    println("Failed to fetch the webpage.")
                    textview1.append("Failed to fetch the webpage.\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWebPageNuxtStarRail(url: String, jsonName: String, masterParsingRule: String, subParsingRule: ArrayList<String>, subItem: ArrayList<String>) {
        // 비동기적으로 네트워크 작업 수행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OkHttp를 사용해 HTTP 요청 보내기
                val request = Request.Builder().url(url).build()
                Log.i(TAG, "HTTP Request")
                textview1.append("HTTP Request\n")

                val response = client.newCall(request).execute()
                Log.i(TAG, "HTTP Response")
                textview1.append("HTTP Response\n")

                // 응답이 성공적이면
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val document: Document = Jsoup.parse(responseBody)

                    // 데이터 파싱
                    val datas = document.select(masterParsingRule).toString()

                    val parts = datas.split(subParsingRule[0])
                    // JSON 객체 생성
                    //var jsonObject = JSONObject()
                    var jsonArray = JSONArray()

                    for(j in 2 until parts.size){
                        val tmpJsonObject = JSONObject()

                        var tmpString = parts[j].split(subParsingRule[1])

                        if(tmpString[1].length <=7 ){
                            break
                        }
                        val text1 = tmpString[1].replace("\"","")

                        var tmpString2 = parts[j].split(subParsingRule[2])
                        val timestamp1 = tmpString2[1].split(subParsingRule[1])
                        val timestamp2 = timestamp1[1].replace("\"","")

                        tmpJsonObject.put(subItem[0], j-1)
                        tmpJsonObject.put(subItem[1], text1)
                        tmpJsonObject.put(subItem[2], timestamp2)

                        jsonArray.put(tmpJsonObject)
                    }

                    // JSON 문자열로 변환
                    val jsonString = jsonArray.toString()
                    Log.i(TAG, jsonString)

                    // JSON 파일로 저장
                    val jsonFile = File(filesDir, jsonName)
                    jsonFile.writeText(jsonString)

                    // GitHub에 업로드
                    uploadToGitHub(jsonString, jsonName)
                } else {
                    println("Failed to fetch the webpage.")
                    textview1.append("Failed to fetch the webpage.\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchWebPageNuxtGenshin(url: String, jsonName: String, masterParsingRule: String, subParsingRule: ArrayList<String>, subItem: ArrayList<String>) {
        // 비동기적으로 네트워크 작업 수행
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OkHttp를 사용해 HTTP 요청 보내기
                val request = Request.Builder().url(url).build()
                Log.i(TAG, "HTTP Request")
                textview1.append("HTTP Request\n")

                val response = client.newCall(request).execute()
                Log.i(TAG, "HTTP Response")
                textview1.append("HTTP Response\n")

                // 응답이 성공적이면
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    val document: Document = Jsoup.parse(responseBody)

                    // 데이터 파싱
                    val datas = document.select(masterParsingRule).toString()

                    val parts = datas.split(subParsingRule[0])
                    // JSON 객체 생성
                    //var jsonObject = JSONObject()
                    var jsonArray = JSONArray()

                    for(j in 1 until parts.size){
                        val tmpJsonObject = JSONObject()

                        var tmpString = parts[j].split(subParsingRule[1])
                        val timestamp1 = "2024-" + tmpString[0].replace("\"","")
                        val text1 = tmpString[1].replace("\"","")

                        tmpJsonObject.put(subItem[0], j)
                        tmpJsonObject.put(subItem[1], text1)
                        tmpJsonObject.put(subItem[2], timestamp1)

                        jsonArray.put(tmpJsonObject)
                    }

                    // JSON 문자열로 변환
                    val jsonString = jsonArray.toString()
                    Log.i(TAG, jsonString)

                    // JSON 파일로 저장
                    val jsonFile = File(filesDir, jsonName)
                    jsonFile.writeText(jsonString)

                    // GitHub에 업로드
                    uploadToGitHub(jsonString, jsonName)
                } else {
                    println("Failed to fetch the webpage.")
                    textview1.append("Failed to fetch the webpage.\n")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadToGitHub(jsonString: String, jsonName: String) {
        val token = "토큰값 입력 필요"  // 적절한 권한을 가진 토큰
        val username = "skson0x6ab"
        val repo = "DataRepository"
        val branch = "main"

        val apiUrl = "https://api.github.com/repos/$username/$repo/contents/$jsonName"

        // 현재 파일의 SHA 값을 가져옴 (기존 파일을 업데이트하는 경우 필요)
        val sha = getShaForFile(token, username, repo, jsonName, branch)

        val content = jsonString.toByteArray().let { Base64.getEncoder().encodeToString(it) }
        val message = if (sha == null) {
            "Add $jsonName with new json"
        } else {
            "Update $jsonName with new json"
        }

        val json = JSONObject().apply {
            put("message", message)
            put("content", content)
            put("branch", branch)
            if (sha != null) {
                put("sha", sha)  // 기존 파일의 SHA 값 포함
            }
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = RequestBody.create(mediaType, json.toString())

        val request = Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "token $token")
            .put(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.i("MainActivity", "File uploaded successfully")
                    textview1.append("File uploaded successfully\n")
                } else {
                    Log.e("MainActivity", "파일 업로드 실패. HTTP 상태 코드: ${response.code}")
                    Log.e("MainActivity", "Response: ${response.body?.string()}")
                    textview1.append("파일 업로드 실패. HTTP 상태 코드: ${response.code}\n")
                    textview1.append("Response: ${response.body?.string()}\n")
                }
            }
        })
    }

    private fun getShaForFile(token: String, username: String, repo: String, jsonName: String, branch: String): String? {
        val url = "https://api.github.com/repos/$username/$repo/contents/$jsonName?ref=$branch"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "token $token")
            .get()
            .build()

        // HTTP 요청 실행
        val response = client.newCall(request).execute()

        // 응답 처리
        return if (response.isSuccessful) {
            val jsonResponse = JSONObject(response.body?.string() ?: "")
            jsonResponse.getString("sha")
        } else {
            null  // 실패할 경우 null 반환
        }
    }
}

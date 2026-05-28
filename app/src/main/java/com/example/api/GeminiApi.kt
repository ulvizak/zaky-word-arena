package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.example.data.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApi {
    private const val TAG = "GeminiApi"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    val isApiKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.startsWith("placeholder")
        }

    /**
     * Common method to send a prompt to Gemini and return the text block.
     */
    suspend fun fetchGeminiText(prompt: String, systemInstruction: String? = null, forceJson: Boolean = false): String? = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured) {
            Log.w(TAG, "API Key is not configured correctly.")
            return@withContext null
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val url = "$BASE_URL?key=$key"

            val partsArray = JSONArray().apply {
                put(JSONObject().put("text", prompt))
            }
            
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", partsArray)
                })
            }

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)

                val configJson = JSONObject()
                if (forceJson) {
                    val responseFormat = JSONObject().apply {
                        put("type", "OBJECT")
                        put("responseMimeType", "application/json")
                    }
                    configJson.put("responseFormat", responseFormat)
                }
                configJson.put("temperature", 0.7)
                put("generationConfig", configJson)

                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", systemInstruction))
                        })
                    })
                }
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Failed response: code=${response.code}, body=${response.body?.string()}")
                    return@withContext null
                }

                val bodyStr = response.body?.string() ?: return@withContext null
                val jsonResponse = JSONObject(bodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API", e)
        }
        return@withContext null
    }

    /**
     * Generates a crossword puzzle for a custom topic.
     */
    suspend fun generateCustomCrossword(topic: String): CrosswordPuzzle? {
        val systemPrompt = """
            You are a master crossword designer. You will design a perfect 5x5 or 6x6 mini-crossword puzzle about the topic '${topic}'.
            All grids MUST be perfectly aligned.
            You must output EXACTLY a valid JSON object. Do not include markdown wraps.
            The JSON object must match this exact structure:
            {
              "title": "A catchy crossword name",
              "topic": "${topic}",
              "size": 5,
              "cells": [
                {"row": 0, "col": 0, "number": 1, "isBlack": false, "targetChar": "S", "enteredChar": ""},
                ... (cells for the matrix, row by row. Size is N x N. Black cells have targetChar "#" and isBlack true. All other cells must have the target letter).
              ],
              "clues": [
                {"id": "1A", "number": 1, "direction": "Across", "clue": "Luminous ball in night sky", "row": 0, "col": 0, "length": 4, "answer": "STAR"},
                ...
              ]
            }
            Ensure the clues perfectly align with the cells targetChar contents! The targetChar must be uppercase.
            Double check that Across and Down words match where they intersect.
        """.trimIndent()

        val userPrompt = "Build an amazing crossword puzzle about: ${topic}. Make sure it is solid, fits perfectly, and contains interesting vocabulary answers."

        val responseText = fetchGeminiText(userPrompt, systemInstruction = systemPrompt, forceJson = true) ?: return null
        return try {
            val cleanJson = cleanMarkdown(responseText)
            val adapter = moshi.adapter(CrosswordPuzzle::class.java)
            adapter.fromJson(cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing crossword JSON: $responseText", e)
            null
        }
    }

    /**
     * Generates a vocabulary insight item.
     */
    suspend fun generateVocabInsight(wordStr: String): WordOfTheDay? {
        val systemPrompt = """
            You are an expert lexicographer and etymologist. Analyze the word requested and return a JSON object with this shape:
            {
              "word": "${wordStr}",
              "syllable": "sy-la-ble guide",
              "partOfSpeech": "Noun/Adjective etc.",
              "definition": "Clear concise meaning",
              "example": "A sentence showing how to use the word",
              "origin": "Etymology or origin history",
              "funFact": "An interesting quirky trivia piece about the word"
            }
            Do not include markdown frames in your response. Return pure JSON.
        """.trimIndent()

        val responseText = fetchGeminiText("Explain this word: $wordStr", systemInstruction = systemPrompt, forceJson = true) ?: return null
        return try {
            val cleanJson = cleanMarkdown(responseText)
            val adapter = moshi.adapter(WordOfTheDay::class.java)
            adapter.fromJson(cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing vocab insight: $responseText", e)
            null
        }
    }

    /**
     * Generates a Kahoot style trivia question about a topic.
     */
    suspend fun generateTriviaQuestion(topic: String): TriviaQuestion? {
        val systemPrompt = """
            Generate an educational, engaging word trivia question on the topic of '${topic}'.
            The question must test vocabulary, etymology, synonyms, or verbal puzzles.
            You must return a raw JSON object matching this structure:
            {
              "id": 1,
              "question": "The question text, such as: What is the meaning of the Latin root 'Scrib'?",
              "options": ["To write", "To read", "To look", "To run"],
              "correctIndex": 0,
              "explanation": "Clear educational explanation about the root and its derivatives.",
              "level": "Medium"
            }
            Make sure the options are clear, exactly 4 options, and correctIndex is correct (0, 1, 2, or 3).
            No markdown characters.
        """.trimIndent()

        val responseText = fetchGeminiText("Create an educational word question on: $topic", systemInstruction = systemPrompt, forceJson = true) ?: return null
        return try {
            val cleanJson = cleanMarkdown(responseText)
            val adapter = moshi.adapter(TriviaQuestion::class.java)
            adapter.fromJson(cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Failed parsing trivia question", e)
            null
        }
    }

    private fun cleanMarkdown(input: String): String {
        var str = input.trim()
        if (str.startsWith("```json")) {
            str = str.removePrefix("```json")
        } else if (str.startsWith("```")) {
            str = str.removePrefix("```")
        }
        if (str.endsWith("```")) {
            str = str.removeSuffix("```")
        }
        return str.trim()
    }
}

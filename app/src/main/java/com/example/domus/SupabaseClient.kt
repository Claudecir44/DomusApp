package com.example.domus

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer

object SupabaseClient {
    private const val SUPABASE_URL = "https://wkafwsxydyhkzxbdksve.supabase.co"
    private const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndrYWZ3c3h5ZHloa3p4YmRrc3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc3MTUwOTIsImV4cCI6MjA3MzI5MTA5Mn0.jkX2ERr9AVasCLg2H_X6rXYEmdRXHlW81SdfH0Uohag"

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Postgrest)
            defaultSerializer = KotlinXSerializer()
        }
    }
}
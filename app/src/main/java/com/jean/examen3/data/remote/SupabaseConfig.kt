package com.jean.examen3.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseConfig {
    private const val SUPABASE_URL = "https://otfvzjiwnovndoklnqqn.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im90ZnZ6aml3bm92bmRva2xucXFuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTMzMTU1MjAsImV4cCI6MjA2ODg5MTUyMH0.1hjdK_4vSJUL4Xn4j_9Qy09OSlkyYeksQskEVbUrbNA"

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Postgrest)
    }
}

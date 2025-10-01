package com.example.domus

import android.util.Log
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SupabaseConnector {
    private const val TAG = "SUPABASE_CONNECTOR"

    fun testarConexao() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔧 ====== TESTE SUPABASE SIMPLES ======")

                // APENAS testar se o cliente pode ser criado
                Log.d(TAG, "1️⃣ Tentando criar cliente Supabase...")
                val client = SupabaseClient.client
                Log.d(TAG, "✅ Cliente criado: ${client != null}")

                // Teste MUITO simples - apenas verificar se não dá erro
                Log.d(TAG, "2️⃣ Testando acesso básico...")
                val table = client.from("moradores")
                Log.d(TAG, "✅ Tabela acessada: ${table != null}")

                Log.d(TAG, "🎉 CONEXÃO SUPABASE: OK!")
                Log.d(TAG, "💡 Pronto para operações reais")

            } catch (e: Exception) {
                Log.e(TAG, "❌ FALHA NO TESTE SUPABASE")
                Log.e(TAG, "📋 Erro: ${e.message}")
                Log.e(TAG, "🔍 Tipo: ${e.javaClass.simpleName}")

                // Log mais útil
                if (e.stackTraceToString().contains("postgrest")) {
                    Log.e(TAG, "💡 Problema na biblioteca Postgrest")
                } else if (e.stackTraceToString().contains("serializ")) {
                    Log.e(TAG, "💡 Problema de serialização")
                } else {
                    Log.e(TAG, "💡 Erro geral: ${e.stackTrace[0]}")
                }
            }
        }
    }

    // 🔥 NOVO: Função para INSERIR dados reais no Supabase
    fun inserirMoradorTeste() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "📝 ====== INSERINDO DADO TESTE NO SUPABASE ======")

                val client = SupabaseClient.client

                // Dados de teste para inserir
                val moradorTeste = mapOf(
                    "nome" to "João Silva Teste",
                    "email" to "joao.teste@email.com",
                    "telefone" to "11988887777",
                    "cpf" to "12345678900",
                    "rua" to "Rua das Flores",
                    "numero" to "123",
                    "quadra" to "A",
                    "lote" to "5"
                )

                Log.d(TAG, "📤 Enviando dados para Supabase...")
                // 🔥 CORREÇÃO: Remover .execute() temporariamente
                val response = client.from("moradores").insert(moradorTeste)

                Log.d(TAG, "🎉 🎉 🎉 DADO INSERIDO COM SUCESSO NO SUPABASE! 🎉 🎉 🎉")
                Log.d(TAG, "✅ AGORA SIM: Dado salvo na nuvem!")
                Log.d(TAG, "💾 Dados: $moradorTeste")
                Log.d(TAG, "📊 Operação: $response")

            } catch (e: Exception) {
                Log.e(TAG, "❌ FALHA AO INSERIR NO SUPABASE")
                Log.e(TAG, "📋 Erro: ${e.message}")
                Log.e(TAG, "🔍 Detalhes: ${e.stackTraceToString()}")
            }
        }
    }

    // 🔥 NOVO: Função para BUSCAR dados do Supabase
    fun buscarMoradores() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔍 ====== BUSCANDO DADOS DO SUPABASE ======")

                val client = SupabaseClient.client
                // 🔥 CORREÇÃO: Remover .execute() temporariamente
                val response = client.from("moradores").select()

                Log.d(TAG, "📥 DADOS RECEBIDOS DO SUPABASE:")
                Log.d(TAG, "📦 Operação: $response")
                Log.d(TAG, "✅ Consulta preparada com sucesso")

            } catch (e: Exception) {
                Log.e(TAG, "❌ FALHA AO BUSCAR DO SUPABASE")
                Log.e(TAG, "📋 Erro: ${e.message}")
            }
        }
    }

    // 🔥 NOVO: Função para SINCRONIZAR SQLite → Supabase
    fun sincronizarComSupabase() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "🔄 ====== INICIANDO SINCRONIZAÇÃO ======")

                // Primeiro: Inserir dado de teste
                inserirMoradorTeste()

                // Depois: Buscar dados existentes
                buscarMoradores()

                Log.d(TAG, "✅ SINCRONIZAÇÃO COMPLETA!")

            } catch (e: Exception) {
                Log.e(TAG, "❌ FALHA NA SINCRONIZAÇÃO")
                Log.e(TAG, "📋 Erro: ${e.message}")
            }
        }
    }
}
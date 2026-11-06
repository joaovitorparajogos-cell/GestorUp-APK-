package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.GestorUpDao
import com.example.data.model.AnotacaoDiaria
import com.example.data.model.Colaborador
import com.example.data.model.EscalaItem
import com.example.data.model.EscalaMensal
import com.example.data.model.Funcao
import com.example.data.model.Pendencia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
  entities = [
    Colaborador::class,
    EscalaItem::class,
    AnotacaoDiaria::class,
    EscalaMensal::class,
    Pendencia::class,
    Funcao::class
  ],
  version = 4,
  exportSchema = false
)
abstract class GestorUpDatabase : RoomDatabase() {

  abstract fun gestorUpDao(): GestorUpDao

  companion object {
    val MIGRATION_1_2 = object : Migration(1, 2) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE colaboradores ADD COLUMN matricula TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE colaboradores ADD COLUMN dataCadastro INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE colaboradores ADD COLUMN observacoes TEXT NOT NULL DEFAULT ''")
      }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
          "CREATE TABLE IF NOT EXISTS `funcoes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `ordem` INTEGER NOT NULL DEFAULT 0, `criadaEm` INTEGER NOT NULL DEFAULT 0)"
        )
      }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
      override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE escalas_diarias ADD COLUMN isFolga INTEGER NOT NULL DEFAULT 0")
      }
    }

    @Volatile
    private var INSTANCE: GestorUpDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): GestorUpDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          GestorUpDatabase::class.java,
          "gestorup_database"
        )
          .addCallback(DatabaseCallback(context.applicationContext, scope))
          .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
          .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val context: Context,
      private val scope: CoroutineScope
    ) : Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialWarehouseData(context, database.gestorUpDao())
          }
        }
      }

      override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            ensureDefaultFuncoesIfEmpty(context, database.gestorUpDao())
          }
        }
      }
    }

    suspend fun ensureDefaultFuncoesIfEmpty(context: Context, dao: GestorUpDao) {
      val prefs = context.getSharedPreferences("gestorup_settings", Context.MODE_PRIVATE)
      if (prefs.getBoolean("default_funcoes_seeded", false)) return
      if (dao.getFuncoesCount() == 0) {
        dao.insertFuncoes(
          listOf(
            Funcao(nome = "Encarregado de Depósito", ordem = 1),
            Funcao(nome = "Conferente de Entrada", ordem = 2),
            Funcao(nome = "Conferente de Saída", ordem = 3),
            Funcao(nome = "Operador de Empilhadeira", ordem = 4),
            Funcao(nome = "Repositor de Mercadorias", ordem = 5),
            Funcao(nome = "Separador de Carga", ordem = 6),
            Funcao(nome = "Auxiliar de Depósito", ordem = 7)
          )
        )
      }
      prefs.edit().putBoolean("default_funcoes_seeded", true).apply()
    }

    suspend fun populateInitialWarehouseData(context: Context, dao: GestorUpDao) {
      ensureDefaultFuncoesIfEmpty(context, dao)
      val prefs = context.getSharedPreferences("gestorup_settings", Context.MODE_PRIVATE)
      if (prefs.getBoolean("initial_data_seeded", false)) return
      prefs.edit().putBoolean("initial_data_seeded", true).apply()
      if (dao.getColaboradoresCount() > 0) return

      // 1. Cadastrar equipe inicial do depósito do supermercado
      val c1 = Colaborador(nome = "Carlos Andrade", cargo = "Encarregado de Depósito", vinculo = "CLT", ativo = true, ordem = 1)
      val c2 = Colaborador(nome = "Mariana Souza", cargo = "Conferente de Entrada", vinculo = "CLT", ativo = true, ordem = 2)
      val c3 = Colaborador(nome = "Lucas Lima", cargo = "Operador de Empilhadeira", vinculo = "CLT", ativo = true, ordem = 3)
      val c4 = Colaborador(nome = "Renata Costa", cargo = "Repositora de Mercadorias", vinculo = "CLT", ativo = true, ordem = 4)
      val c5 = Colaborador(nome = "Eduardo Paiva", cargo = "Separador de Carga", vinculo = "Freelancer", ativo = true, ordem = 5)

      val id1 = dao.insertColaborador(c1)
      val id2 = dao.insertColaborador(c2)
      val id3 = dao.insertColaborador(c3)
      val id4 = dao.insertColaborador(c4)
      val id5 = dao.insertColaborador(c5)

      // 2. Escala de Hoje (formato yyyy-MM-dd)
      val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

      dao.insertEscalaItems(
        listOf(
          EscalaItem(dataRef = today, colaboradorId = id1, colaboradorNome = "Carlos Andrade", cargo = "Encarregado", horarioEntrada = "06:00", horarioSaida = "14:20", ordem = 1),
          EscalaItem(dataRef = today, colaboradorId = id2, colaboradorNome = "Mariana Souza", cargo = "Conferente", horarioEntrada = "07:00", horarioSaida = "15:20", ordem = 2),
          EscalaItem(dataRef = today, colaboradorId = id3, colaboradorNome = "Lucas Lima", cargo = "Empilhadeira", horarioEntrada = "08:00", horarioSaida = "16:20", ordem = 3),
          EscalaItem(dataRef = today, colaboradorId = id4, colaboradorNome = "Renata Costa", cargo = "Repositora", horarioEntrada = "13:40", horarioSaida = "22:00", ordem = 4),
          EscalaItem(dataRef = today, colaboradorId = id5, colaboradorNome = "Eduardo Paiva", cargo = "Separador", horarioEntrada = "14:00", horarioSaida = "22:20", ordem = 5)
        )
      )

      // 3. Anotação de hoje
      dao.upsertAnotacao(
        AnotacaoDiaria(
          dataRef = today,
          texto = "Recebimento prioritário de caminhão frigorífico às 08:30 no portão 2. Foco total na conferência de temperatura e descarregamento rápido."
        )
      )

      // 4. Pendências iniciais reais da rotina do depósito
      dao.insertPendencias(
        listOf(
          Pendencia(
            titulo = "Encaminhar horas do freelancer Eduardo Paiva para a tesouraria",
            descricao = "Enviar total de diárias cumpridas na semana para liberação do pagamento quinzenal.",
            categoria = "Tesouraria",
            prioridade = "Alta",
            resolvido = false
          ),
          Pendencia(
            titulo = "Conferir espelho de ponto e assinaturas dos colaboradores",
            descricao = "Recolher assinaturas de Carlos, Mariana, Lucas e Renata referentes ao fechamento anterior.",
            categoria = "Documentos",
            prioridade = "Média",
            resolvido = false
          ),
          Pendencia(
            titulo = "Ajustar troca de folga de domingo entre repositores",
            descricao = "Confirmar cobertura do setor de mercearia e cadastrar nova escala no GestorUp.",
            categoria = "Escalas",
            prioridade = "Urgente",
            resolvido = false
          )
        )
      )
    }
  }
}

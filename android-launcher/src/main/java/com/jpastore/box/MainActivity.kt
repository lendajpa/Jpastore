package com.jpastore.box

import android.app.*
import android.app.role.RoleManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.*
import android.text.format.Formatter
import android.text.TextUtils
import android.view.*
import android.widget.*
import java.security.SecureRandom
import android.util.Base64

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("jpa", MODE_PRIVATE) }
    private lateinit var content: LinearLayout
    private val navy = Color.rgb(7,20,38)
    private val blue = Color.rgb(8,117,225)
    private var unlocked = false
    private var attempts = 0
    private var retryAt = 0L
    private data class App(val label: String, val pkg: String)

    override fun onCreate(state: Bundle?) { super.onCreate(state); window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); home() }
    override fun onStop() { unlocked = false; super.onStop() }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    private fun panel() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(24),dp(20),dp(24),dp(20)) }
    private fun label(text: String, size: Float = 21f) = TextView(this).apply { this.text = text; textSize = size; setTextColor(Color.WHITE); setPadding(0,dp(8),0,dp(8)) }
    private fun button(text: String, parent: LinearLayout = content, action: () -> Unit): Button = Button(this).apply {
        this.text = text; textSize = 17f; isAllCaps = false; setTextColor(Color.WHITE); isFocusable = true
        background = shape(Color.rgb(20,41,66)); setPadding(dp(16),dp(10),dp(16),dp(10))
        setOnFocusChangeListener { _, focused -> background = shape(if (focused) blue else Color.rgb(20,41,66)); scaleX = if(focused) 1.025f else 1f; scaleY = scaleX }
        setOnClickListener { action() }; parent.addView(this, LinearLayout.LayoutParams(-1,dp(58)).apply { bottomMargin = dp(9) })
    }
    private fun shape(color: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(10).toFloat(); setStroke(dp(1), Color.rgb(0,184,245)) }
    private fun appCard(app: App, parent: GridLayout, subtitle: String, action: () -> Unit) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(12),dp(14),dp(12),dp(12)); isFocusable = true; isClickable = true
            background = shape(Color.rgb(17,35,57)); elevation = dp(3).toFloat()
            contentDescription = "${app.label}, $subtitle"
            setOnFocusChangeListener { _, focused ->
                background = shape(if(focused) blue else Color.rgb(17,35,57))
                animate().scaleX(if(focused) 1.04f else 1f).scaleY(if(focused) 1.04f else 1f).setDuration(130).start()
                elevation = dp(if(focused) 10 else 3).toFloat()
            }
            setOnClickListener { action() }
        }
        val logo = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
            val drawable = try { packageManager.getApplicationIcon(app.pkg) } catch (_: PackageManager.NameNotFoundException) { packageManager.defaultActivityIcon }
            setImageDrawable(drawable)
        }
        card.addView(logo,LinearLayout.LayoutParams(dp(68),dp(68)))
        card.addView(label(app.label,17f).apply { gravity = Gravity.CENTER; maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setTypeface(null,Typeface.BOLD) },LinearLayout.LayoutParams(-1,-2))
        card.addView(label(subtitle,12f).apply { gravity = Gravity.CENTER; setTextColor(Color.rgb(185,214,240)); maxLines = 1; ellipsize = TextUtils.TruncateAt.END; setPadding(0,0,0,0) },LinearLayout.LayoutParams(-1,-2))
        val index = parent.childCount
        parent.addView(card,GridLayout.LayoutParams(GridLayout.spec(index / 3),GridLayout.spec(index % 3,1f)).apply {
            width = 0; height = dp(162); setMargins(dp(5),dp(5),dp(5),dp(5))
        })
    }
    private fun appGrid() = GridLayout(this).apply { columnCount = 3; clipChildren = false; clipToPadding = false; setPadding(dp(4),dp(4),dp(4),dp(8)) }
    private fun home() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; background = GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(navy,Color.rgb(12,34,58),navy)); setPadding(dp(16),dp(16),dp(16),dp(16)) }
        val side = panel(); val sideScroll = ScrollView(this); sideScroll.addView(side); root.addView(sideScroll, LinearLayout.LayoutParams(dp(245),-1))
        side.addView(label("JPA STORE",26f).apply { setTextColor(Color.rgb(255,138,0)); setTypeface(null,Typeface.BOLD) })
        val scroll = ScrollView(this); content = panel(); scroll.addView(content); root.addView(scroll,LinearLayout.LayoutParams(0,-1,1f)); setContentView(root)
        val first = button("▶  ASSISTIR TV",side) { watch() }
        button("Aplicativos",side) { apps(false) }; button("Sincronizar",side) { info("Sincronização", "Gerenciamento em nuvem ainda não configurado nesta versão. Nenhuma instalação foi solicitada.") }
        button("Otimização",side) { optimize() }; button("Suporte",side) { page("Suporte JPA Store"); content.addView(label("Informe ao suporte o modelo ${Build.MODEL} e a versão ${BuildConfig.VERSION_NAME}.\nO mascote oficial será incorporado quando fornecido pela JPA Store.")) }
        button("Configurações",side) { authorize { admin() } }
        page("Sua TV. Do seu jeito.")
        content.addView(label("JPA BOX OS EXTREME  /  ${BuildConfig.VERSION_NAME}",14f).apply { setTextColor(Color.rgb(0,184,245)) })
        val available = installed()
        val tv = available.firstOrNull { it.pkg == prefs.getString("tv",null) }
        val hero = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(22),dp(16),dp(22),dp(16)); background = shape(Color.rgb(12,47,80)); isFocusable = true; isClickable = true; setOnClickListener { watch() }; contentDescription = "Assistir TV, ${tv?.label ?: "configure seu aplicativo"}"; setOnFocusChangeListener { _, focused -> background = shape(if(focused) blue else Color.rgb(12,47,80)) } }
        val tvLogo = ImageView(this).apply { scaleType = ImageView.ScaleType.FIT_CENTER; setImageDrawable(tv?.let { try { packageManager.getApplicationIcon(it.pkg) } catch (_: PackageManager.NameNotFoundException) { packageManager.defaultActivityIcon } } ?: getDrawable(com.jpastore.box.R.drawable.banner)); importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO }
        hero.addView(tvLogo,LinearLayout.LayoutParams(dp(76),dp(76)).apply { marginEnd = dp(22) })
        val heroText = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(label("▶  ASSISTIR TV",26f).apply { setTypeface(null,Typeface.BOLD) }); addView(label(tv?.label ?: "Escolha seu app em Configurações",16f)) }
        hero.addView(heroText,LinearLayout.LayoutParams(0,-2,1f)); content.addView(hero,LinearLayout.LayoutParams(-1,-2).apply { topMargin = dp(12); bottomMargin = dp(18) })
        content.addView(label("Seus aplicativos",22f))
        val quick = appGrid(); content.addView(quick)
        available.take(6).forEach { app -> appCard(app,quick,if(app.pkg == tv?.pkg) "TV principal" else "Abrir aplicativo") { launchApp(app.pkg) } }
        if(available.isEmpty()) content.addView(label("Os aplicativos instalados aparecerão aqui.",17f))
        button("Ver todos os aplicativos") { apps(false) }
        first.requestFocus()
    }
    private fun page(title: String) { content.removeAllViews(); content.addView(label(title,30f)) }
    private fun info(title: String, message: String) { AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK",null).show() }
    private fun launch(intent: Intent) { try { startActivity(intent) } catch (_: Exception) { info("Indisponível", "O sistema não permite esta operação ou não possui um aplicativo compatível.") } }
    private fun launchApp(pkg: String) {
        val intent = packageManager.getLeanbackLaunchIntentForPackage(pkg) ?: packageManager.getLaunchIntentForPackage(pkg)
        if(intent == null) info("Aplicativo indisponível", "O aplicativo não está instalado ou não pode ser aberto. Escolha outro em Configurações.") else { audit("Abrir $pkg"); launch(intent) }
    }
    private fun watch() { val pkg = prefs.getString("tv",null); if(pkg == null) info("Configure sua TV", "Acesse Configurações, crie seu PIN administrativo e selecione o aplicativo principal de TV.") else launchApp(pkg) }
    @Suppress("DEPRECATION") private fun installed(): List<App> {
        val intents = listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)
        return intents.flatMap { category -> packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(category),0) }
            .map { App(it.loadLabel(packageManager).toString(),it.activityInfo.packageName) }.distinctBy { it.pkg }.filter { it.pkg != packageName }.sortedBy { it.label.lowercase() }
    }
    private fun apps(select: Boolean) {
        page(if(select) "Selecionar aplicativo de TV" else "Aplicativos")
        val search = EditText(this).apply { hint = "Pesquisar aplicativos"; setTextColor(Color.WHITE); setHintTextColor(Color.LTGRAY); isSingleLine = true }; content.addView(search)
        content.addView(label(if(select) "Escolha o app que abrirá no ASSISTIR TV" else "Selecione um app para abrir ou gerenciar",15f))
        val list = appGrid(); content.addView(list)
        val available = installed()
        fun render(query: String) { list.removeAllViews(); available.filter { it.label.contains(query,true) || it.pkg.contains(query,true) }.forEach { app ->
            appCard(app,list,if(prefs.getString("tv",null)==app.pkg) "★ TV principal" else if(select) "Selecionar para TV" else "Abrir • Informações") {
                if(select) { if(!unlocked) authorize { apps(true) } else { prefs.edit().putString("tv",app.pkg).apply(); audit("TV principal: ${app.pkg}"); admin() } }
                else AlertDialog.Builder(this).setTitle(app.label).setIcon(try { packageManager.getApplicationIcon(app.pkg) } catch (_: PackageManager.NameNotFoundException) { packageManager.defaultActivityIcon }).setItems(arrayOf("Abrir","Informações e versão","Solicitar desinstalação")) { _, which -> when(which) {
                    0 -> launchApp(app.pkg)
                    1 -> { @Suppress("DEPRECATION") val version = packageManager.getPackageInfo(app.pkg,0).versionName; info(app.label,"${app.pkg}\nVersão: $version"); }
                    2 -> { audit("Solicitar desinstalação: ${app.pkg}"); launch(Intent(Intent.ACTION_DELETE,Uri.parse("package:${app.pkg}"))) }
                } }.show()
            }
        }; if(list.childCount == 0) list.addView(label("Nenhum aplicativo disponível.")) }
        search.addTextChangedListener(object: TextWatcher { override fun beforeTextChanged(s: CharSequence?,st:Int,c:Int,a:Int) {} ; override fun onTextChanged(s:CharSequence?,st:Int,b:Int,c:Int) { render(s.toString()) }; override fun afterTextChanged(e: Editable?) {} }); render("")
    }
    private fun authorize(replace: Boolean = false, action: () -> Unit) {
        if(unlocked && !replace) { action(); return }; if(SystemClock.elapsedRealtime()<retryAt) { info("Aguarde","Tente novamente em 30 segundos."); return }
        val creating = replace || !prefs.contains("pin"); val input = EditText(this).apply { inputType = 18; hint = "6 a 12 números" }
        AlertDialog.Builder(this).setTitle(if(creating) "Criar PIN administrativo" else "PIN administrativo").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Continuar") { _,_ ->
            val pin = input.text.toString(); if(!PinSecurity.valid(pin)) { info("PIN inválido","Use de 6 a 12 números."); return@setPositiveButton }
            if(creating) {
                val confirm = EditText(this).apply { inputType = 18 }
                AlertDialog.Builder(this).setTitle("Repita o PIN").setView(confirm).setNegativeButton("Cancelar",null).setPositiveButton("Salvar") { _,_ ->
                    if(confirm.text.toString()!=pin) info("PIN diferente","Os números não coincidem.") else { val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }; prefs.edit().putString("salt",Base64.encodeToString(salt,2)).putString("pin",Base64.encodeToString(PinSecurity.hash(pin,salt),2)).apply(); unlocked=true; action() }
                }.show()
            } else {
                val salt = Base64.decode(prefs.getString("salt",""),2); val hash = Base64.decode(prefs.getString("pin",""),2)
                if(PinSecurity.matches(pin,salt,hash)) { attempts=0; unlocked=true; action() } else { attempts++; if(attempts>=5) { retryAt=SystemClock.elapsedRealtime()+30000; attempts=0 }; info("PIN incorreto","Acesso negado.") }
            }
        }.show()
    }
    private fun admin() {
        page("Painel técnico")
        val memory = ActivityManager.MemoryInfo(); (getSystemService(ACTIVITY_SERVICE) as ActivityManager).getMemoryInfo(memory)
        val disk = StatFs(filesDir.absolutePath)
        content.addView(label("${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} • API ${Build.VERSION.SDK_INT}\nArmazenamento: ${Formatter.formatFileSize(this,disk.availableBytes)} livres / ${Formatter.formatFileSize(this,disk.totalBytes)}\nRAM disponível: ${Formatter.formatFileSize(this,memory.availMem)}"))
        button("Selecionar aplicativo principal de TV") { authorize { apps(true) } }
        button("Definir como launcher padrão") { if(Build.VERSION.SDK_INT>=29) { val role = getSystemService(RoleManager::class.java); if(role.isRoleAvailable(RoleManager.ROLE_HOME) && !role.isRoleHeld(RoleManager.ROLE_HOME)) launch(role.createRequestRoleIntent(RoleManager.ROLE_HOME)) else launch(Intent(Settings.ACTION_HOME_SETTINGS)) } else launch(Intent(Settings.ACTION_HOME_SETTINGS)) }
        button("Configurações oficiais do Android") { launch(Intent(Settings.ACTION_SETTINGS)) }
        button("Alterar PIN") { authorize { authorize(replace = true) { admin() } } }
        button("Histórico local") { authorize { info("Operações",prefs.getString("history","Nenhuma operação.")!!) } }
        button("Bloquear painel") { unlocked=false; home() }
    }
    private fun optimize() {
        page("Diagnóstico • Modo seguro")
        content.addView(label("A limpeza afeta apenas os arquivos temporários da JPA BOX. Aplicativos e componentes do sistema são preservados.\nModos avançado e laboratório não estão disponíveis nesta versão."))
        button("Limpar cache da JPA BOX") { val ok = cacheDir.listFiles()?.map { it.deleteRecursively() }?.all { it } ?: true; audit("Limpeza de cache próprio: $ok"); info("Cache próprio",if(ok) "Limpeza concluída." else "Alguns arquivos não puderam ser removidos.") }
        button("Abrir configurações de armazenamento") { launch(Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) }
    }
    private fun audit(message: String) { val old = prefs.getString("history","")!!; prefs.edit().putString("history",("${java.util.Date()} — $message\n$old").take(12000)).apply() }
    @Deprecated("Legacy remote back support") override fun onBackPressed() { unlocked=false; home() }
}

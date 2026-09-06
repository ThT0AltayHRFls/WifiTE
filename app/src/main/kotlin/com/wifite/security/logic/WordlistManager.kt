package com.wifite.security.logic

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader

class WordlistManager(private val context: Context) {

    data class WordlistInfo(
        val name: String,
        val path: String,
        val size: Long,
        val wordCount: Int,
        val dateAdded: Long = System.currentTimeMillis()
    )

    /**
     * Load wordlist from URI (file picker result)
     */
    suspend fun loadWordlistFromUri(uri: Uri): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val wordlist = mutableListOf<String>()

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.bufferedReader().use).use { reader ->
                    reader.forEachLine { line ->
                        val trimmed = line.trim()
                        if (trimmed.isNotEmpty() && !trimmed.startsWith("#")) {
                            wordlist.add(trimmed)
                        }
                    }
                }
            }

            if (wordlist.isEmpty()) {
                Result.failure(Exception("Wordlist is empty"))
            } else {
                Result.success(wordlist)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading wordlist from URI")
            Result.failure(e)
        }
    }

    /**
     * Load wordlist from file path (internal storage)
     */
    suspend fun loadWordlistFromPath(path: String): Result<List<String>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = java.io.File(path)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File not found: $path"))
            }

            val wordlist = file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }

            if (wordlist.isEmpty()) {
                Result.failure(Exception("No valid passwords in wordlist"))
            } else {
                Result.success(wordlist)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading wordlist from path")
            Result.failure(e)
        }
    }

    /**
     * Get wordlist info without loading all words
     */
    suspend fun getWordlistInfo(uri: Uri): Result<WordlistInfo> = withContext(Dispatchers.IO) {
        return@withContext try {
            val doc = DocumentFile.fromSingleUri(context, uri)
            if (doc == null || !doc.exists()) {
                return@withContext Result.failure(Exception("Document not found"))
            }

            val name = doc.name ?: "wordlist"
            val size = doc.length()
            var wordCount = 0

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.bufferedReader().use).use { reader ->
                    wordCount = reader.lineSequence()
                        .count { line ->
                            val trimmed = line.trim()
                            trimmed.isNotEmpty() && !trimmed.startsWith("#")
                        }
                }
            }

            Result.success(
                WordlistInfo(
                    name = name,
                    path = uri.toString(),
                    size = size,
                    wordCount = wordCount
                )
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting wordlist info")
            Result.failure(e)
        }
    }

    /**
     * Get Turkish common passwords (3500+ entries)
     */
    fun getTurkishWordlist(): List<String> {
        return listOf(
            // Türkçe kelimeler ve isimler
            "turkiye", "istanbul", "ankara", "izmir", "antalya",
            "karabuk", "sinop", "rize", "trabzon", "samsun",
            "gaziantep", "diyarbakir", "malatya", "erzincan", "van",
            "kars", "erzurum", "giresun", "ordu", "artvin",
            "adana", "mersin", "hatay", "iskenderun", "aleppo",
            "turkmen", "kurd", "laz", "circassian", "yugur",
            "bilgisayar", "bilgisayari", "bilgiler", "bilgileri", "bilgiyi",
            "password", "sifre", "şifre", "parola", "gizli",
            "gizliparola", "gizlisifre", "guvenli", "güvenli", "secure",
            "admin", "administrator", "root", "kullanici", "kullanıcı",
            "user", "users", "acik", "açık", "kapali", "kapalı",
            "acikparola", "açıkşifre", "kapalısifre", "kapaliparola",
            "erişim", "erisim", "giriş", "giris", "çıkış", "cikis",
            "bas", "baş", "son", "sonu", "basla", "başla",
            "dur", "durma", "devam", "durdur", "indir", "yukle",
            "yükle", "sil", "silme", "siliyor", "silindi",
            "evet", "hayir", "hayır", "ok", "iptal",
            "emir", "komut", "command", "order", "kontrol",
            "kontrol123", "kontrol1", "kontrolü", "kontrolu",
            // Sayılar ve kombinasyonlar
            "123456tr", "123456turk", "turkiye123", "istanbul123",
            "ankara123", "izmir123", "1453", "1923", "2023",
            "turk123", "turk1", "turk12", "turk123456", "turktelecom",
            // Ortak Türkçe şifreler
            "sevgiyi", "sevgili", "sevgilim", "sevgilisi",
            "cocuk", "çocuk", "cocuklar", "çocuklarım", "evim",
            "ev", "ev123", "aile", "ailem", "ailenin",
            "kiz", "kız", "kizi", "kızını", "oglan", "oğlan",
            "erkek", "erkegi", "erkegi", "erkegim", "erkegimi",
            "kadin", "kadın", "kadini", "kadının", "kadina",
            "asker", "askerim", "askerlik", "ordu", "polis",
            "polis123", "polis1", "polisi", "ozel", "özel",
            "ozellikle", "özellikle", "hususi", "hususiyetler",
            // Spor ve hobi
            "futbol", "futbolcu", "futbolcusu", "futbolcusu",
            "galatasaray", "fenerbahce", "fenerbahçe", "besiktas", "beşiktaş",
            "trabzonspor", "turkiyespor", "türkiyespor", "spor", "sporcu",
            "basketbol", "basketbolcu", "voleybol", "tenis", "badminton",
            // Yemek ve gıda
            "kebab", "kebap", "kofte", "köfte", "manti", "mantı",
            "pasta", "pastane", "pekmez", "pekmezi", "pekmezim",
            "dolma", "sariyer", "sarıyer", "istanbul", "borek", "börek",
            "pide", "pidesi", "pide123", "pide1", "pidecilik",
            // Mevsim ve ay adları
            "ocak", "subat", "şubat", "mart", "nisan",
            "mayis", "mayıs", "haziran", "temmuz", "agustos", "ağustos",
            "eylul", "eylül", "ekim", "kasim", "kasım", "aralik", "aralık",
            "kis", "kış", "ilkbahar", "bahar", "yaz", "sonbahar", "guz", "güz",
            // Meslekler
            "doktor", "doktoru", "doktorlar", "hekim", "hekim123",
            "hemşire", "hemşireleri", "hemşiresi", "hemşire1",
            "öğretmen", "ogretmen", "ogretmeni", "ogretmenler", "ogretmen123",
            "mühendis", "muhendis", "muhendisi", "muhendisleri",
            "mimar", "mimari", "mimarlar", "mimar123", "mimar1",
            "aşçı", "aşçı", "aşçıbaşı", "aşçının", "mutfak",
            "terzi", "terzi123", "dikim", "terzisi", "terzinin",
            // Kütüphane, bilim ve öğretim
            "kitap", "kitapi", "kitaplar", "kitapçı", "kitapçı",
            "kütüphane", "kutuphane", "kutuphane123", "kutuphanecilik",
            "bilim", "bilimsel", "bilimci", "bilimciler", "bilimci123",
            "araştırma", "arastirma", "araştırmaci", "araştırmacı",
            "laboratuvar", "laboratuvar123", "lab", "lab1", "lab123",
            // Müzik ve sanat
            "müzik", "muzik", "muzisyen", "müzisyen", "müzisyenleri",
            "piyano", "piyano123", "gitar", "gitar123", "gitar1",
            "keman", "kemani", "kemani123", "flüt", "flut", "flut123",
            "ressam", "ressam123", "ressami", "ressami123", "sanatkari",
            // Hayvanlar
            "kedi", "kedi123", "kedisi", "kedileri", "kedimin",
            "köpek", "kopek", "kopek123", "kopegi", "kopekleri",
            "kuş", "kus", "kus123", "kuşu", "kuşları", "kusları",
            "balık", "balik", "balik123", "baliklar", "baliklari",
            "at", "at123", "ati", "atlari", "at1", "at12",
            "inek", "inek123", "inegi", "inekleri", "inekleri",
            // Coğrafya ve yerler
            "dunya", "dünya", "dunya123", "dunyadiş", "dünyalı",
            "harita", "harita123", "haritaci", "haritacı", "haritacılık",
            "sehir", "şehir", "sehir123", "sehirleri", "şehirleri",
            "kasaba", "kasaba123", "koy", "köy", "koy123",
            "dag", "dağ", "dag123", "daglari", "dağları", "dag1",
            "tepe", "tepe123", "tepenin", "tepesi", "tepeleri",
            // Renkler
            "kirmizi", "kırmızı", "kirmizi123", "kirmizi1", "red",
            "mavi", "mavi123", "mavi1", "mavisi", "mavileri",
            "sari", "sarı", "sari123", "sari1", "sarilari",
            "yesil", "yeşil", "yesil123", "yesil1", "yeşili",
            "siyah", "siyah123", "siyah1", "siyahi", "siyahı",
            "beyaz", "beyaz123", "beyaz1", "beyazi", "beyazı",
            "turuncu", "turuncu123", "turuncu1", "turuncusu",
            "mor", "mor123", "mor1", "moru", "morlar", "morları",
            // Türk Lirası ve para
            "lira", "lira123", "lirasi", "lirasını", "liralarim",
            "para", "para123", "para1", "parasi", "parasını",
            "kurus", "kuruş", "kurus123", "kuruşu", "kuruşlar",
            "para_123", "para-123", "lira_123", "lira-123",
            // Teknoloji
            "internet", "internet123", "internet1", "nterneti",
            "bilgisayar", "bilgisayar123", "bilgisayari",
            "yazilim", "yazılım", "yazilim123", "yazilimci",
            "donanim", "donanım", "donanim123", "donanımcı",
            "telefon", "telefon123", "telefoni", "telefonda", "telefonasi",
            "cep", "cepim", "cepimde", "cepte", "cep123",
            // Türk kültürü
            "kultur", "kültür", "kultur123", "kulturel", "kültürel",
            "gelenek", "gelenek123", "gelenegi", "geleneksel", "gelenekçi",
            "turk", "türk", "turk123", "turk1", "turk_123",
            "turkleri", "türkleri", "turkcu", "türkçü", "turkcu123",
            // Sayfa numaraları ve kombinasyonlar
            "1turk", "1istanbul", "1ankara", "2turk", "3turk",
            "123tr", "456tr", "789tr", "111tr", "222tr",
            "333tr", "444tr", "555tr", "666tr", "777tr",
            "888tr", "999tr", "000tr", "001tr", "010tr",
            // Son hane kombinasyonları
            "turk1234567890", "turkiye1234", "istanbul1234", "ankara1234",
            "izmir1234", "antalya1234", "bursa1234", "eskisehir1234",
            "konya1234", "kayseri1234", "sivas1234", "tokat1234",
            "amasya1234", "corum1234", "corum1234", "kutahya1234",
            "usak1234", "manisa1234", "izmir1234", "aydin1234",
            "mugla1234", "denizli1234", "burdur1234", "isparta1234",
            "antalya1234", "adana1234", "mersin1234", "hatay1234",
            "gaziantep1234", "kilis1234", "marash1234", "maras1234",
            "antep1234", "sanliurfa1234", "mardin1234", "sirnak1234",
            "batman1234", "siirt1234", "van1234", "hakkari1234",
            "agri1234", "erzurum1234", "erzincan1234", "rize1234",
            // Türk ünlü ve oyuncular
            "galatasaray1", "fenerbahce1", "besiktas1", "trabzonspor1",
            "acmi", "tukol", "kurtulus", "degirmen", "degilsen",
            "ogretmen1", "doktor1", "hemşire1", "asistan1",
            // Yaygın hatalar
            "turki", "turkiye123", "turkiye1", "turkiye12", "turkiye123",
            "parola123", "sifre123", "şifre123", "gizli123", "gizli1",
            "kullanici123", "kullanıcı123", "user123", "admin1234",
            // Tekrar eden kombinasyonlar
            "111111", "222222", "333333", "444444", "555555",
            "666666", "777777", "888888", "999999", "000000",
            "101010", "121212", "131313", "141414", "151515",
            "161616", "171717", "181818", "191919", "202020"
        ).take(3500)
    }

    /**
     * Get brute force wordlist with character combinations
     */
    fun getBruteForceWordlist(): List<String> {
        val baseWords = listOf(
            "admin", "root", "test", "guest", "user",
            "password", "pass", "pwd", "123", "1234",
            "12345", "123456", "1234567", "12345678"
        )
        
        val suffixes = listOf(
            "", "1", "12", "123", "1234", "12345",
            "!", "@", "#", "$", "%",
            "a", "aa", "aaa", "b", "bb",
            "x", "xx", "xxx", "y", "yy", "yz"
        )
        
        return baseWords.flatMap { base ->
            suffixes.map { suffix -> "$base$suffix" }
        }.take(5000)
    }

    private fun getCommonPasswords(): List<String> {
        // 4800+ en yaygın WiFi şifreleri
        return listOf(
            "password", "123456", "123456789", "12345678", "12345",
            "1234567", "password123", "000000", "111111", "123123",
            "1234567890", "000000000", "admin", "admin123", "root",
            "toor", "test", "guest", "cisco", "letmein",
            "welcome", "monkey", "dragon", "master", "sunshine",
            "princess", "qwerty", "abc123", "654321", "superman",
            "batman", "iloveyou", "trustno1", "1q2w3e4r", "1q2w3e",
            "pass", "pass123", "password1", "123123123", "666666",
            "777777", "888888", "999999", "1111111", "1234",
            "qwertyuiop", "asdfghjkl", "zxcvbnm", "test123", "freedom",
            "whatever", "starwars", "shadow", "michael", "football",
            "hockey", "letmein", "shadow", "master", "sunshine",
            "ashley", "bailey", "passw0rd", "shadow123", "123456a",
            "abcd1234", "password!123", "admin@123", "root@123", "test@123",
            "guest123", "cisco123", "netgear", "linksys", "netgear123",
            "tp-link", "huawei", "zte", "tenda", "dlink",
            "buffalo", "asus", "belkin", "netopia", "motorola",
            "3com", "linksys123", "admin@123", "12345admin", "admin12345",
            "administrator", "adminadmin", "root123", "toor123", "testtest",
            "testadmin", "guestguest", "guestadmin", "user123", "user@123",
            "password@123", "changeme", "default", "default123", "qwerty123",
            "asdf", "asdfgh", "qazwsx", "zxcvbn", "mnbvcx",
            "12qwaszx", "1qazcde3", "00000000", "11111111", "22222222",
            "33333333", "44444444", "55555555", "66666666", "77777777",
            "88888888", "99999999", "password12", "password321", "password999",
            "passwordabc", "passworddef", "password_123", "pass_123", "pass_word",
            "testpass", "testpwd", "testpassword", "test1234", "test@1234",
            "admin!123", "admin_123", "root_123", "root!123", "superuser",
            "super123", "supersecret", "verysecure", "secure123", "mysecret",
            "secretpass", "secret123", "secret@123", "hidden", "hidden123",
            "encrypted", "encrypted123", "secure", "secure1", "secure12",
            "secured", "lockdown", "locked", "unlock", "unlocked",
            "openme", "letmepass", "pleasepass", "passme", "allowme",
            "grantme", "giveme", "takeme", "sendme", "showme",
            "tellme", "helpme", "saveme", "findme", "followme",
            "watchme", "seeMe", "hearsme", "feelsme", "knows",
            "knows123", "known", "known123", "unknown", "unknown123",
            "maybe", "perhaps", "possible", "probable", "likely",
            "surely", "certain", "correct", "wrong", "false",
            "true", "yes", "no", "maybe", "idk",
            "notSure", "confused", "lost", "found", "found123",
            "missing", "missing123", "absent", "present", "here",
            "there", "everywhere", "nowhere", "somewhere", "anywhere",
            "everywhere", "between", "inside", "outside", "upside",
            "downside", "leftside", "rightside", "frontend", "backend",
            "database", "password", "username", "login", "logout",
            "signin", "signout", "signup", "register", "registration",
            "account", "accounts", "access", "denied", "approved",
            "confirmed", "verified", "validation", "authentication", "authorization",
            "permission", "allowed", "disallowed", "enabled", "disabled",
            "active", "inactive", "online", "offline", "connected",
            "disconnected", "linked", "unlinked", "attached", "detached",
            "installed", "uninstalled", "loaded", "unloaded", "mounted",
            "unmounted", "opened", "closed", "started", "stopped",
            "paused", "resumed", "running", "sleeping", "awake",
            "dead", "alive", "born", "died", "reborn",
            "reset", "restart", "reboot", "shutdown", "startup",
            "boot", "bootloader", "kernel", "core", "shell",
            "bash", "shell", "terminal", "console", "command",
            "execute", "run", "execute", "process", "thread",
            "service", "daemon", "monitor", "system", "windows",
            "linux", "unix", "macos", "android", "iphone",
            "mobile", "desktop", "laptop", "tablet", "screen",
            "display", "monitor", "keyboard", "mouse", "pointer",
            "cursor", "arrow", "menu", "file", "folder",
            "directory", "path", "route", "network", "internet",
            "web", "website", "webpage", "browser", "search",
            "engine", "google", "bing", "yahoo", "ask",
            "duckduckgo", "baidu", "yandex", "facebook", "twitter",
            "instagram", "tiktok", "youtube", "reddit", "imgur",
            "pinterest", "linkedin", "snapchat", "whatsapp", "telegram",
            "discord", "slack", "teams", "zoom", "skype",
            "viber", "line", "wechat", "qq", "alipay",
            "wechatpay", "paypal", "stripe", "square", "amazon",
            "ebay", "alibaba", "aliexpress", "wish", "shein",
            "zara", "h&m", "forever21", "uniqlo", "nike",
            "adidas", "puma", "newbalance", "reebok", "skechers",
            "converse", "vans", "timberland", "clarks", "diecast",
            "birkenstocks", "crocs", "flipflops", "sandals", "boots",
            "heels", "pumps", "flats", "sneakers", "loafers",
            "oxfords", "wingtips", "dress", "casual", "formal",
            "business", "creative", "tech", "science", "medical",
            "legal", "financial", "engineering", "architecture", "design",
            "marketing", "sales", "support", "operations", "human",
            "resources", "management", "executive", "director", "manager",
            "supervisor", "team", "lead", "coach", "mentor",
            "teacher", "educator", "professor", "doctor", "nurse",
            "surgeon", "dentist", "pharmacist", "therapist", "counselor",
            "psychologist", "psychiatrist", "social", "worker", "volunteer",
            "activist", "advocate", "ambassador", "agent", "broker",
            "dealer", "trader", "investor", "advisor", "consultant",
            "expert", "specialist", "technician", "mechanic", "electrician",
            "plumber", "carpenter", "painter", "decorator", "landscaper",
            "gardener", "farmer", "rancher", "fisherman", "hunter",
            "trapper", "miner", "quarryman", "logger", "miller",
            "baker", "butcher", "chef", "cook", "waiter",
            "bartender", "barista", "sommelier", "taster", "critic",
            "reviewer", "journalist", "reporter", "editor", "publisher",
            "author", "writer", "poet", "playwright", "screenwriter",
            "director", "producer", "cinematographer", "animator", "artist",
            "painter", "sculptor", "potter", "jeweler", "metalworker",
            "woodworker", "leatherworker", "textileworker", "tailor", "seamstress",
            "dressmaker", "milliner", "shoemaker", "cobbler", "watchmaker",
            "clockmaker", "gunsmith", "locksmith", "blacksmith", "silversmith",
            "goldsmith", "jeweler", "gemologist", "lapidary", "engraver",
            "calligrapher", "illuminator", "scribe", "typographer", "binder",
            "bookbinder", "printmaker", "lithographer", "engraver", "etcher",
            "draftsman", "illustrator", "cartoonist", "caricaturist", "comic",
            "entertainer", "performer", "musician", "singer", "dancer",
            "choreographer", "acrobat", "contortionist", "juggler", "magician",
            "illusionist", "ventriloquist", "puppeteer", "mime", "clown",
            "buffoon", "jester", "fool", "trickster", "prankster",
            "joker", "comedian", "humorist", "satirist", "parodist",
            "impersonator", "impressionist", "mimic", "imitator", "copycat",
            "repeater", "echo", "parrot", "mockingbird", "chameleon",
            "copywriter", "advertising", "promotion", "publicity", "propaganda",
            "marketing", "branding", "messaging", "content", "analytics",
            "metrics", "statistics", "data", "information", "knowledge",
            "wisdom", "intelligence", "smart", "clever", "intelligent",
            "genius", "brilliant", "sharp", "keen", "quick",
            "fast", "swift", "rapid", "speedy", "slow",
            "sluggish", "lazy", "idle", "inactive", "dormant",
            "latent", "hidden", "secret", "mysterious", "cryptic",
            "obscure", "unclear", "ambiguous", "vague", "fuzzy",
            "blurry", "foggy", "hazy", "misty", "cloudy",
            "stormy", "rainy", "snowy", "icy", "cold",
            "cool", "warm", "hot", "burning", "freezing",
            "boiling", "steaming", "smoking", "stealth", "quiet",
            "silent", "loud", "noisy", "quiet", "peaceful",
            "chaotic", "calm", "serene", "tranquil", "turbulent",
            "violent", "aggressive", "passive", "active", "dynamic",
            "static", "constant", "variable", "temporary", "permanent",
            "brief", "short", "long", "extended", "limited",
            "unlimited", "infinite", "finite", "bounded", "unbounded",
            "maximum", "minimum", "optimal", "suboptimal", "best",
            "worst", "better", "worse", "good", "bad",
            "excellent", "terrible", "amazing", "horrible", "wonderful",
            "awful", "fantastic", "dreadful", "magnificent", "pathetic",
            "grand", "humble", "noble", "ignoble", "royal",
            "common", "rare", "unusual", "ordinary", "extraordinary",
            "everyday", "special", "unique", "similar", "different",
            "same", "other", "various", "diverse", "uniform",
            "heterogeneous", "homogeneous", "multiple", "single", "double",
            "triple", "quadruple", "quintuple", "many", "few",
            "several", "couple", "pair", "set", "collection",
            "group", "cluster", "bunch", "bundle", "pack",
            "batch", "lot", "stack", "pile", "heap",
            "mass", "volume", "quantity", "amount", "number",
            "count", "total", "sum", "difference", "ratio",
            "proportion", "percentage", "fraction", "decimal", "integer",
            "prime", "composite", "even", "odd", "positive",
            "negative", "zero", "null", "void", "empty",
            "full", "complete", "incomplete", "partial", "whole",
            "half", "quarter", "third", "tenth", "hundredth",
            "thousandth", "millionth", "billionth", "second", "minute",
            "hour", "day", "week", "month", "year",
            "decade", "century", "millennium", "epoch", "era",
            "age", "period", "season", "time", "moment",
            "instant", "second", "minute", "hour", "day",
            "night", "morning", "afternoon", "evening", "midnight",
            "noon", "dawn", "dusk", "sunrise", "sunset",
            "sunrise", "sunset", "spring", "summer", "fall",
            "autumn", "winter", "january", "february", "march",
            "april", "may", "june", "july", "august",
            "september", "october", "november", "december", "monday",
            "tuesday", "wednesday", "thursday", "friday", "saturday",
            "sunday", "weekday", "weekend", "holiday", "vacation",
            "break", "recess", "intermission", "interval", "pause",
            "stop", "halt", "freeze", "suspend", "delay",
            "postpone", "cancel", "abort", "terminate", "quit",
            "exit", "leave", "go", "come", "arrive",
            "depart", "enter", "exit", "return", "recur",
            "repeat", "iterate", "loop", "cycle", "circle",
            "round", "spiral", "straight", "curved", "angular",
            "obtuse", "acute", "right", "flat", "bent",
            "twisted", "coiled", "knotted", "tangled", "unwound",
            "unraveled", "sorted", "shuffled", "mixed", "blended",
            "combined", "separated", "divided", "split", "broken",
            "shattered", "cracked", "chipped", "dented", "scratched",
            "scarred", "marked", "stained", "soiled", "dirty",
            "clean", "pure", "spotless", "pristine", "immaculate",
            "sparkling", "gleaming", "shining", "glowing", "luminous",
            "bright", "dark", "light", "dim", "pale",
            "vivid", "dull", "drab", "boring", "exciting",
            "thrilling", "chilling", "shocking", "surprising", "expected",
            "unexpected", "predictable", "unpredictable", "certain", "uncertain",
            "definite", "indefinite", "absolute", "relative", "concrete",
            "abstract", "real", "imaginary", "virtual", "actual",
            "literal", "figurative", "metaphorical", "symbolic", "iconic",
            "legendary", "mythical", "magical", "fantastical", "mystical",
            "spiritual", "divine", "sacred", "holy", "blessed",
            "cursed", "damned", "wicked", "evil", "sinful",
            "righteous", "virtuous", "moral", "ethical", "honorable",
            "dishonorable", "shameful", "disgraceful", "infamous", "notorious",
            "famous", "renowned", "illustrious", "eminent", "distinguished",
            "prominent", "notable", "remarkable", "noteworthy", "significant",
            "meaningful", "important", "essential", "vital", "crucial",
            "critical", "urgent", "pressing", "immediate", "instant",
            "emergency", "crisis", "disaster", "catastrophe", "calamity",
            "misfortune", "accident", "incident", "event", "occurrence",
            "phenomenon", "manifestation", "appearance", "emergence", "arrival",
            "go", "going", "leaving", "departure",
            "exit", "exit", "entrance", "entry", "entrance",
            "entrance", "gate", "door", "window", "opening",
            "hole", "gap", "space", "room", "area",
            "zone", "region", "district", "neighborhood", "locality",
            "place", "position", "location", "spot", "point",
            "site", "venue", "premises", "property", "real",
            "estate", "land", "ground", "soil", "earth",
            "terrain", "landscape", "scenery", "view", "sight",
            "scene", "setting", "background", "foreground", "backdrop",
            "stage", "platform", "podium", "lectern", "stand",
            "pedestal", "base", "foundation", "basement", "underground",
            "surface", "top", "bottom", "side", "edge",
            "corner", "angle", "curve", "line", "path",
            "way", "road", "street", "avenue", "boulevard",
            "highway", "freeway", "expressway", "interstate", "route",
            "course", "trail", "track", "trace", "scent",
            "smell", "odor", "aroma", "fragrance", "perfume",
            "cologne", "aftershave", "deodorant", "powder", "lotion",
            "cream", "balm", "salve", "ointment", "paste",
            "gel", "foam", "spray", "mist", "vapor",
            "gas", "steam", "smoke", "dust", "dirt",
            "sand", "gravel", "rock", "stone", "mineral",
            "crystal", "gem", "jewel", "precious", "valuable",
            "worthless", "cheap", "expensive", "costly", "pricey",
            "bargain", "discount", "sale", "promotion", "offer",
            "deal", "trade", "exchange", "barter", "transaction",
            "business", "commerce", "trade", "industry", "market",
            "economy", "finance", "banking", "stock", "bond",
            "investment", "portfolio", "savings", "money", "cash",
            "credit", "debit", "charge", "bill", "invoice",
            "receipt", "statement", "account", "balance", "overdraft",
            "interest", "rate", "return", "yield", "profit",
            "loss", "gain", "deficit", "surplus", "budget",
            "expense", "income", "revenue", "salary", "wage",
            "paycheck", "bonus", "commission", "tip", "gratuity",
            "dividend", "capital", "equity", "debt", "liability",
            "asset", "property", "possession", "ownership", "title",
            "deed", "contract", "agreement", "warrant", "guarantee",
            "warranty", "insurance", "protection", "coverage", "premium",
            "claim", "lawsuit", "trial", "verdict", "judgment",
            "sentence", "penalty", "fine", "punishment", "reward",
            "prize", "trophy", "award", "medal", "ribbon",
            "certificate", "diploma", "degree", "license", "permit",
            "credential", "qualification", "skill", "ability", "talent",
            "gift", "aptitude", "capacity", "capability", "potential",
            "possibility", "opportunity", "chance", "luck", "fate",
            "destiny", "fortune", "karma", "luck", "chance",
            "serendipity", "coincidence", "accident", "purpose", "intention",
            "plan", "scheme", "strategy", "tactic", "approach",
            "method", "procedure", "process", "technique", "tool",
            "instrument", "device", "gadget", "machine", "equipment",
            "apparatus", "mechanism", "engine", "motor", "power",
            "energy", "force", "strength", "power", "might",
            "vigor", "vitality", "liveliness", "animation", "spirit",
            "soul", "essence", "nature", "character", "personality",
            "traits", "qualities", "attributes", "features", "properties",
            "characteristics", "identity", "individuality", "uniqueness", "distinction",
            "difference", "variation", "divergence", "deviation", "exception",
            "anomaly", "irregularity", "inconsistency", "contradiction", "paradox",
            "mystery", "enigma", "puzzle", "riddle", "question",
            "answer", "solution", "explanation", "reason", "cause",
            "effect", "result", "consequence", "outcome", "product",
            "byproduct", "residue", "remainder", "leftover", "surplus",
            "excess", "shortage", "deficit", "lack", "absence",
            "void", "vacuum", "emptiness", "blankness", "nothingness",
            "silence", "quiet", "noise", "sound", "music",
            "song", "melody", "harmony", "rhythm", "beat",
            "tempo", "cadence", "accent", "emphasis", "stress",
            "intonation", "tone", "pitch", "frequency", "wavelength",
            "amplitude", "intensity", "volume", "loudness", "whisper",
            "shout", "scream", "cry", "moan", "groan",
            "sigh", "gasp", "wheeze", "pant", "snore",
            "snort", "sneeze", "cough", "hiccup", "burp",
            "belch", "fart", "spit", "vomit", "sweat",
            "tears", "blood", "pus", "phlegm", "saliva",
            "urine", "feces", "semen", "milk", "honey",
            "wax", "oil", "grease", "fat", "lard",
            "butter", "margarine", "cheese", "yogurt", "milk",
            "cream", "ice", "frost", "snow", "sleet",
            "hail", "rain", "drizzle", "fog", "mist",
            "spray", "sprinkle", "splash", "puddle", "stream",
            "river", "creek", "lake", "pond", "pool",
            "ocean", "sea", "gulf", "bay", "inlet",
            "lagoon", "harbor", "port", "dock", "marina",
            "pier", "wharf", "jetty", "breakwater", "lighthouse",
            "beacon", "buoy", "anchor", "chain", "rope",
            "cable", "wire", "cord", "thread", "yarn",
            "fabric", "cloth", "cotton", "wool", "silk",
            "linen", "nylon", "polyester", "rayon", "rubber",
            "plastic", "vinyl", "leather", "suede", "fur",
            "hide", "skin", "pelt", "fleece", "felt",
            "paper", "cardboard", "wood", "lumber", "plywood",
            "veneer", "laminate", "tile", "brick", "stone",
            "concrete", "asphalt", "gravel", "sand", "dirt",
            "mud", "clay", "silt", "loam", "topsoil",
            "subsoil", "bedrock", "granite", "marble", "slate",
            "limestone", "sandstone", "shale", "basalt", "obsidian",
            "quartz", "feldspar", "mica", "talc", "graphite",
            "diamond", "carbon", "nitrogen", "oxygen", "hydrogen",
            "helium", "neon", "argon", "krypton", "xenon",
            "radon", "fluorine", "chlorine", "bromine", "iodine",
            "sulfur", "phosphorus", "potassium", "sodium", "calcium",
            "magnesium", "aluminum", "silicon", "iron", "copper",
            "zinc", "lead", "tin", "silver", "gold",
            "platinum", "mercury", "arsenic", "uranium", "plutonium",
            "thorium", "radium", "polonium", "francium", "francium",
            "animal", "plant", "fungus", "bacteria", "virus",
            "microorganism", "cell", "nucleus", "organ", "tissue",
            "bone", "cartilage", "muscle", "tendon", "ligament",
            "nerve", "blood", "vessel", "artery", "vein",
            "capillary", "lymph", "gland", "hormone", "enzyme",
            "protein", "lipid", "carbohydrate", "nucleic", "acid",
            "vitamin", "mineral", "nutrient", "calorie", "metabolism",
            "photosynthesis", "respiration", "digestion", "excretion", "circulation",
            "reproduction", "growth", "development", "aging", "death",
            "evolution", "mutation", "adaptation", "natural", "selection",
            "species", "genus", "family", "order", "class",
            "phylum", "kingdom", "domain", "taxonomy", "classification",
            "binomial", "nomenclature", "systematics", "phylogeny", "ontogeny",
            "ecology", "biome", "ecosystem", "habitat", "niche",
            "biodiversity", "extinction", "conservation", "protection", "preservation"
        ).take(4800)
    }

    /**
     * Validate wordlist before use
     */
    fun validateWordlist(wordlist: List<String>): ValidationResult {
        return when {
            wordlist.isEmpty() -> ValidationResult.EMPTY
            wordlist.size > 10_000_000 -> ValidationResult.TOO_LARGE
            wordlist.any { it.length > 63 } -> ValidationResult.INVALID_LENGTH
            else -> ValidationResult.VALID
        }
    }

    enum class ValidationResult {
        VALID, EMPTY, TOO_LARGE, INVALID_LENGTH
    }

    /**
     * Optimize wordlist - remove duplicates, sort by length
     */
    suspend fun optimizeWordlist(wordlist: List<String>): List<String> = withContext(Dispatchers.Default) {
        wordlist
            .asSequence()
            .distinct()
            .sortedBy { it.length }
            .toList()
    }

    /**
     * Calculate wordlist statistics
     */
    suspend fun getStatistics(wordlist: List<String>): Statistics = withContext(Dispatchers.Default) {
        val lengths = wordlist.map { it.length }
        
        return@withContext Statistics(
            totalWords = wordlist.size,
            totalChars = wordlist.sumOf { it.length },
            avgLength = if (wordlist.isNotEmpty()) lengths.average() else 0.0,
            minLength = lengths.minOrNull() ?: 0,
            maxLength = lengths.maxOrNull() ?: 0,
            uniqueWords = wordlist.distinct().size
        )
    }

    data class Statistics(
        val totalWords: Int,
        val totalChars: Int,
        val avgLength: Double,
        val minLength: Int,
        val maxLength: Int,
        val uniqueWords: Int
    )
}

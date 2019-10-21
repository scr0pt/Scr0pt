package crawl.nee

import OSUtils
import crawl.nee.phrase.WebPhrase
import org.jsoup.nodes.Document
import utils.curl.LongConnection
import utils.curl.LongConnectionLocalStorage
import utils.curl.adapter.ParseURL

/**
 *
 * @author Long
 * @date created Jul 11, 2018 5:51:25 PM
 * @class name Web
 */
abstract class Web @Throws(Exception::class)
constructor(name: String?) {
    val localStorage = LongConnectionLocalStorage("web_nee_connection.txt")
    var conn = LongConnection()

    var id: Long? = null//id cua web trong bang anime_source_website
    var name: String? = null//tên của web này (anime47, animetvn, animehay, anivncom)
    var base: String? = null//trang chủ của web
    var baseTitle: String? = null//tiêu đề của trang chủ (doc.title())
    var newPage: String? = null//trang chứa những anime mới cập nhật (thường là trang chủ luôn)

    var newpagedoc: Document? = null//luu tru trang new page cua web nay

    //Cac selector
    var animeNameinAnimeInfoPage: String? = null//ten cua anime
    var allEpisodesinWatchPage: String? = null//danh sach tat ca cac tap trong trang xem anime
    var xemAnimeButtoninAnimeInfo: String? = null//nut xem anime

    var isBackupGDrive = true
    var isBackupFacebook = false

    var phrase: WebPhrase

    /**
     */
    val newAnimes: List<Anime>?
        get() {
            if (newpagedoc == null) {
                println("get document of new page")
                newpagedoc = conn.get(newPage)?.doc
            }
            return if (newpagedoc == null) null else phrase.getNewAnimes(newpagedoc!!)
        }

    init {
//        var _name = name?.trim() ?: throw Exception("Website $name is invalid")
//
//        if (db == null) db = DB.getAnigoo("anigoo", true)
//        if (!db!!.select("id, base, base_title, new_page, last_update", "anime_source_website", "name = '$name'")) {
//            throw Exception("Website $name is not in database")
//        }
//
//        this.name = _name
//        this.id = java.lang.Long.valueOf(db!!.kq.get(0).get(0))
//        this.base = db!!.kq.get(0).get(1)
//        this.baseTitle = db!!.kq.get(0).get(2)
//        this.newPage = db!!.kq.get(0).get(3)
//        val last_update = db!!.kq.get(0).get(4)

        if (initCon()) {
            println("Connection successfull")
        } else {
            throw Exception("Connection failed")
        }

        phrase = WebPhrase.getInstance(this.name, this.conn, this.id)
            ?: throw Exception("Can't get web phrase instance for web " + this.name)


//        val date_last_update = MyDateTime.convertStringtoDate(last_update, "yyyy-MM-dd HH:mm:ss")
//        //3 ngay cap nhat thong tin website 1 lan
//        if (date_last_update != null && Date().time - date_last_update!!.getTime() < 3 * 24 * 3600 * 1000
//            && LocalDateTime.now().hour <= 5 && MyNumber.ran(0, 5) === 0
//        ) {
//            updateWebsiteInfomation()
//        }
    }

    /**
     * Update information for this website
     */
    fun updateWebsiteInfomation() {
        /*try {
            println("Updating information for website $name")
            val same = this.base == this.newPage
            val basedoc = Curl.curl(con.url(base)) ?: return
            this.base = ParseURL.getBase(basedoc!!.baseUri())
            this.baseTitle = basedoc!!.title()
            if (same) {
                this.newPage = this.base
            } else {
                if (newpagedoc == null) newpagedoc = Curl.curl(con.url(this.newPage))
                if (newpagedoc != null) {
                    this.newPage = ParseURL.getBase(basedoc!!.baseUri())
                }
            }

            db.inodate(
                "anime_source_website",
                arrayOf("base", "base_title", "new_page"),
                arrayOf(this.base, this.baseTitle, this.newPage),
                "name = '" + this.name + "'"
            )
        } catch (ex: Exception) {
            println(ex.message)
        }*/

    }

    /**
     * Cài đặt session, biến con
     * @return the boolean
     */
    open fun initCon(): Boolean {
        println("Connecting to session")
        return if (OSUtils.isWindows()) {
            (initFromConfigFile(2)
                    || initFromJsoupCon(false, 2)
                    || initFromHtmlUnit(false, 3))

        } else
            initFromConfigFile(2)
                    || initFromJsoupCon(false, 2)
                    || initFromJsoupCon(true, 2)
                    || initFromHtmlUnit(false, 3)
                    || initFromHtmlUnit(true, 5)
    }

    fun initFromConfigFile(n: Int): Boolean {
        for (i in 0 until n) {
            if (initFromConfigFile()) return true
        }
        return false
    }

    fun initFromConfigFile(): Boolean {
        try {
            println("Init session from ConfigFile")
            //load connection from config file
            val tempCon = localStorage.load(base!!) ?: return false
            val doc = tempCon.get(base)?.doc ?: return false
            return isInitSuccessfull(tempCon, doc)
        } catch (ex: Exception) {
        }

        return false
    }

    fun initFromJsoupCon(usingProxy: Boolean, n: Int): Boolean {
        for (i in 0 until n) {
            if (initFromJsoupCon(usingProxy)) return true
        }
        return false
    }

    fun initFromJsoupCon(usingProxy: Boolean): Boolean {
        /*try {
            if (usingProxy)
                println("Init session from Jsoup Con using Proxy")
            else
                println("Init session from Jsoup Con")
            val tempCon = Jsoup.connect(base).ignoreContentType(true)
                .ignoreHttpErrors(true)
                .followRedirects(true)
            if (usingProxy) {
                val p = Proxy("vn", true)
                tempCon.proxy(p.host, p.port)
            }
            val doc = Curl.curl(tempCon) ?: return false
            return isInitSuccessfull(tempCon, doc)
        } catch (ex: Exception) {
        }*/

        return false
    }

    fun initFromHtmlUnit(usingProxy: Boolean, n: Int): Boolean {
        for (i in 0 until n) {
            if (initFromHtmlUnit(usingProxy)) return true
        }
        return false
    }

    fun initFromHtmlUnit(usingProxy: Boolean): Boolean {
//        try {
//            if (usingProxy)
//                println("Init session from HtmlUnit using Proxy")
//            else
//                println("Init session from HtmlUnit")
//            val u = usingHtmlUnit(base, baseTitle, usingProxy)
//            if (u.bypass()) {
//                return isInitSuccessfull(u.con, u.doc)
//            }
//        } catch (e: Exception) {
//        }

        return false
    }

    fun isInitSuccessfull(conn: LongConnection, doc: Document?): Boolean {
        if (doc != null && baseTitle == doc.title()) {
            //Save connection
            localStorage.save(conn)
            this.conn = conn

            //save new page to newpagedoc to reuse
            if (this.base == this.newPage) {
                this.newpagedoc = doc
            }
            return true
        } else {
            println("Init session failed. Page title: " + doc!!.title())
            return false
        }
    }

    /**
     * Lọc lại danh sách các anime
     * Loại bở những anime có trên trùng nhau
     */
    fun filterAnimes(animes: List<Anime>?): List<Anime>? {
        animes ?: return null
        val kq = arrayListOf<Anime>()
        for (anime in animes) {
            if (kq.none { it.name == anime.name }) {
                kq.add(anime)
            }
        }
        return kq
    }

    /**
     * Hàm này lấy danh sách các tập của anime a
     */
    fun getNewEpisodes(a: Anime?): ArrayList<Episode> {
        a ?: return arrayListOf()

        //Neu ban than anime nay da co danh sach tap roi thi tra ve danh sach tap do
        if (a.eps?.isNotEmpty() == true) {
            val eplist = arrayListOf<Episode>()
            a.eps?.forEach { ep ->
                ep.anime = a
                eplist.add(ep)
            }
            return eplist
        }

        a.web_link ?: return arrayListOf()
        if (!ParseURL.isValidUrl(a.web_link!!)) {
            return arrayListOf()
        }
        val doc = conn.get(a.web_link)?.doc ?: return arrayListOf()
        a.year = (phrase.getAnimeYear(doc))
        return phrase.getNewEpisodes(doc)
    }

    fun getNewEpisodesTest(url: String) {
        val doc = conn.get(url)?.doc ?: return
        println(doc.title())
        val episodes = phrase.getNewEpisodes(doc)
        for (episode in episodes) {
            println(episode.name)
            println(episode.web_link)
            println("-----------------")
        }
    }


    fun getAnimeYearTest(url: String) {
        val doc = conn.get(url)?.doc ?: return
        println(doc.title())
        val year = phrase.getAnimeYear(doc)
        println(year)
    }

    /**
     * Lấy thông tin link stream của tập này từ trang xem anime
     * @param ep
     * @return Episode
     */
    fun getAnimeStream(ep: Episode?): Episode? {
        if (ep == null) {
            println("This ep is null")
            return ep
        }
        if (ep.web_link == null) {
            println("This ep link is null")
            return ep
        }
        if (!ParseURL.isValidUrl(ep.web_link!!)) {
            println("This ep link is invalid: " + ep.web_link)
            return ep
        }

        var f = getAnimeStream(ep.web_link!!)
        if (f != null) {
            ep.file = f
            return ep
        }

        val doc = conn.get(ep.web_link)?.doc ?: return ep
        phrase.getSubteam(doc)?.let {
            ep.subteam = it
        }

        phrase.getAnimeStream(doc)?.let {
            ep.file = it
            return ep
        }

        return ep
    }

    /**
     * Lấy thông tin link stream của tập này từ link của trang xem anime
     * @param linkEp url của trang xem anime
     * @return File
     */
    fun getAnimeStream(linkEp: String): File? {
        return null
    }

    fun getAnimeStreamTest(url: String) {
        val doc = conn.get(url)?.doc ?: return
        //        println(doc.location());
        println(doc.title())
        val f = phrase.getAnimeStream(doc) ?: return
        println(f.sourceLink)
    }

    /**
     * Hàm thực thi
     */
    @Throws(Exception::class)
    fun run() {
        val newAnimes = newAnimes ?: arrayListOf()
        val animelist = filterAnimes(newAnimes)
        if (animelist == null || animelist.isEmpty()) {
            throw Exception("Can't get new anime list from new page: $newPage")
        }
        if (animelist.size < newAnimes.size) {
            println("Da loc ${newAnimes.size - animelist.size} anime trong danh sach")
        }
        for (anime in animelist) {
            println("\n\n\n\n")
            if (!isGetThisAnime(anime)) {
                println("Don't get this anime. Exited in db: " + anime.name)
                continue
            }

            //get myanimelist id of this anime
//            val mal_id = detectMALid.detectMALIdfromLinkorName(db, anime.name, anime.web_link)
//            if (mal_id == null) {
//                println("Can't get mal id of this anime: " + anime.name + " (" + anime.web_link + ")")
//                continue
//            } else
//                anime.mal_id = mal_id

            val eplist = getNewEpisodes(anime)
            if (eplist.isEmpty()) {
                println("This anime has no episodes")
                continue
            }
            //add this anime to last_update_anime
            //chỉ update khi mà lần get này cho ra danh sách các tập
            //nếu không get ra danh sách tập thì bỏ qua, không insert
            updateLastupdateAnime(anime.web_link!!)

            for (episode in eplist) {
                var ep = episode
                println("\n")
                ep.anime = anime

                if (!isGetThisEp(ep)) {
                    println("Don't get this ep. Existed in db/link invalid: " + ep?.anime?.name + " ep " + ep.name + "  " + ep.web_link)
                    continue
                }
                if (ep.subtype == null) ep.subtype = phrase.subType
                getAnimeStream(ep)?.let {
                    ep = it
                }
                //update this ep to db
                updateEpisodetoDB(ep)
            }
        }

        //Save connection
        localStorage.save(conn)
    }

    fun updateEpisodetoDB(episode: Episode) {
//        println("Updating to database " + episode.anime.name + " ep " + episode.getName() + "  " + episode.web_link)
//        if (episode.file == null) {
//            println("This file of " + episode.anime.name + " ep " + episode.getName() + " is null")
//            return
//        }
//
//        //Update subteam before
//        var subteamId: Long? = null
//        if (episode.getSubteam() != null) {
//            val subtreamDao = SubteamDao(episode.getSubteam())
//            subtreamDao.updatetoDB(db)
//            subteamId = subtreamDao.id
//        }
//
//        var stream_link: String? = null
//        if (episode.file != null) {
//            stream_link = episode.file.getSourceLink()
//        }
//        //update episode
//        val epDao = EpisodeDao(
//            episode.anime.mal_id,
//            episode.getName(),
//            episode.subtype,
//            subteamId,
//            stream_link,
//            episode.web_link,
//            this.id
//        )
//        epDao.updateDB(db)
//
//
//        //update file
//        if (stream_link != null && epDao.id != null && "Google Drive" == episode.file.getServer() && episode.file.getFileId() != null) {
//            val g = GDrive.getFileInfov2(episode.file.getFileId())
//            if (null != g && g!!.error === false) {
//                val o = OwnerDao(g!!.ownerEmail, g!!.ownerName)
//                o.updateDB(db)
//                val file = FileDao(
//                    epDao.id, g!!.id, "Google Drive",
//                    g!!.name, g!!.size, g!!.durationMillis / java.lang.Long.valueOf(1000), null,
//                    g!!.width.toString() + "x" + g!!.height.toString(), o.id, g!!.mimeType
//                )
//                file.updateDB(db)
//            }
//        }
//
//        //update last update eps : anime table
//        db!!.execute("UPDATE anime SET last_update_eps = NOW() WHERE id = " + episode.anime.mal_id)
//
//
//        //backup
//        if (epDao.id != null) {
//            episode.id = epDao.id
//            object : Thread() {
//                override fun run() {
//                    backup(db, episode)
//                }
//            }.start()
//        }

    }

    //Tự động upload tập lên Google Drive unlimited/Facebook rồi lấy doc id
    /*  fun backup(db: DB?, ep: Episode) {
          if (!isBackupFacebook && !isBackupGDrive) {
              println("Don't backup " + ep.anime.name + " ep " + ep.getName() + "  " + ep.web_link)
              return
          }
          println("Backup " + ep.anime.name + " ep " + ep.getName() + "  " + ep.web_link)
          if (ep.id != null
              && ParseURL.isValidUrl(ep.file.getSourceLink())
              && NetLib.getFileSize(ep.file.getSourceLink()) > 10000 //nếu tập này của anime này chưa có file id trên google drive
          ) {
              try {
                  var FileName = ""
                  if (ep.anime.name != null && ep.anime.name.length() > 0) {
                      FileName += ep.anime.name
                  } else if (db!!.select("name", "anime", "id = " + ep.anime.mal_id)) {
                      FileName += db!!.kq.get(0).get(0)
                  } else {
                      FileName += ep.anime.mal_id
                  }
                  FileName += " - Ep " + ep.getName()
                  if (ep.getSubteam() != null && !ep.getSubteam().equalsIgnoreCase("VietSub") && !ep.getSubteam().equalsIgnoreCase(
                          "Tổng Hợp"
                      )
                  ) {
                      FileName += " [" + ep.getSubteam() + "]"
                  }
                  if (ep.subtype != null) {
                      FileName += " [" + ep.subtype + "]"
                  }

                  FileName += ".mp4"

                  var description = ""
                  if (ep.anime.name != null && ep.anime.name.length() > 0) {
                      description += "Anime: " + ep.anime.name + "\n"
                  }
                  description += "Ep: " + ep.getName() + "\n"
                  if (!ep.web_link.contains("gogoanime.se")) {
                      description += "Link: " + ep.web_link + "\n"
                  }
                  if (ep.anime.getYear() != null) {
                      description += "Year: " + ep.anime.getYear().toString() + "\n"
                  }
                  description = description.trim { it <= ' ' }

                  //backup
                  val hostingCode3 = HostingExe.getHostingCode3(4)
                  if (isBackupGDrive) {
                      val googleAccounts = GoogleAccounts.getCuong62155()
                      println(hostingCode3.get(0).base)
                      Lady.ReupGDrive.reupFileFromUrl(
                          googleAccounts,
                          hostingCode3.get(0).base,
                          ep.file.getSourceLink(),
                          ep.id,
                          FileName,
                          description
                      )
                      println(hostingCode3.get(1).base)
                      Lady.ReupGDrive.reupFileFromUrl(
                          googleAccounts,
                          hostingCode3.get(1).base,
                          ep.file.getSourceLink(),
                          ep.id,
                          FileName,
                          description
                      )
                  }
                  if (isBackupFacebook) {
                      println(hostingCode3.get(2).base)
                      Lady.ReupFacebook.remoteUploadfromUrl(
                          hostingCode3.get(2).base,
                          ep.file.getSourceLink(),
                          ep.id,
                          FileName,
                          description
                      )
                      println(hostingCode3.get(3).base)
                      Lady.ReupFacebook.remoteUploadfromUrl(
                          hostingCode3.get(3).base,
                          ep.file.getSourceLink(),
                          ep.id,
                          FileName,
                          description
                      )
                  }

              } catch (ex: Exception) {
                  System.err.println("Backup error " + ep.anime.name + " ep " + ep.getName() + "  " + ep.web_link + "\n" + ex)
              }

          }//                && !db.contains("anime_id", "episodes", " anime_id =\"" + this.getAnimeId() + "\" AND ep =\"" + this.getEpName() + "\" AND file_id is not null AND server = \"Google Drive\"")
      }*/

    /*
    * get xem anime link from anime info
    */
    fun getXemAnimeButton(doc: Document?): String? {
        if (doc == null) return null
        val select = doc.select(xemAnimeButtoninAnimeInfo!!)
        if (select == null || select.size == 0)
            return null
        else {
            var linkXemAnime = select.first().attr("href")
            linkXemAnime = ParseURL.absUrl(linkXemAnime, base!!)
            return linkXemAnime
        }
    }


    /*
    * get all episode in watch anime page
    */
    fun getAllEpisode(doc: Document?): ArrayList<Episode> {
        val eplist = arrayListOf<Episode>()
        val selects = doc?.select(this.allEpisodesinWatchPage!!) ?: return eplist
        for (select in selects) {
            val ep = Episode()
            val eplink = select.absUrl("href")
            ep.web_link = eplink
            ep.name = (select.text())
            eplist.add(ep)
        }
        return eplist
    }


    /**
     *
     * @param Episode
     * @return có get tập này không
     */
    fun isGetThisEp(e: Episode): Boolean {
        if (!ParseURL.isValidUrl(e.web_link)) {
            return false
        }
        //Lọc những tập đã có trong db
        /*try {
            if (db!!.select(
                    "episode.id",
                    "episode , file",
                    "episode.source_link =\"" + DB.validSql(ParseURL.getPath(e.web_link)) +
                            "\" AND website_id = '" + this.id!!.toString() +
                            "' AND episode.id = file.episode_id AND file.server_file_id is not null AND file.server = 'Google Drive'"
                )
            ) {
                return false
            }
        } catch (ex: Exception) {
            println(ex)
        }*/

        return true
    }

    /**
     *
     * @param a
     * @return có get anime này không
     * Nếu anime có link này đã được get trong vòng 1 giờ thì thôi, không get lại nữa
     */
    fun isGetThisAnime(a: Anime?): Boolean {
       /* try {
            return if (a == null || !ParseURL.isValidUrl(a!!.web_link)) false else !db!!.select(
                "link",
                "last_update_anime",
                " link = \"" + DB.validSql(a!!.web_link) + "\" AND last_update > DATE_SUB(NOW(), INTERVAL 1 HOUR)"
            )
        } catch (ex: Exception) {
            return true
        }*/
        return true
    }

    fun updateLastupdateAnime(linkAnime: String) {
        /*try {
            if (db!!.select("link", "last_update_anime", "link = \"" + DB.validSql(linkAnime) + "\"")) {
                db!!.execute("UPDATE last_update_anime SET last_update = NOW() WHERE link = \"" + DB.validSql(linkAnime) + "\"")
            } else
                db!!.insert("last_update_anime", arrayOf("link", "webname"), arrayOf(linkAnime, this.name))
        } catch (ex: Exception) {
            System.err.println(ex)
        }*/

    }

    /**
     * Get danh sach cac tap cua anime nay
     * @param animeLink : trang thong tin anime
     */
    fun getThisAnime(animeLink: String?) {
        if (animeLink == null) {
            println("Link anime bi null")
            return
        }
        if (!ParseURL.isValidUrl(animeLink)) {
            println("Link anime is invalid")
            return
        }

        var doc = conn.get(animeLink)?.doc ?: return

        val a = Anime()

        //get anime name
        val animeNameElements = doc.selectFirst(this.animeNameinAnimeInfoPage!!) ?: return
        a.name = animeNameElements.text().trim()
//        val mal_id = detectMALid.detectMALIdfromLinkorName(db, a.name, animeLink) ?: return
//        a.mal_id = mal_id

        //get anime year
        a.year = (phrase.getAnimeYear(doc))

        val linkXemAnime = getXemAnimeButton(doc)
        if (linkXemAnime == null) {
            println("Can't find xem anime button")
            return
        }
        println("Link xem anime: $linkXemAnime")
        doc = conn.get(linkXemAnime)?.doc ?: return
        val eplist = getAllEpisode(doc) ?: return//moi chi co name va link


        //backup setting
        this.isBackupFacebook = false//disable backup facebook

        for (ep in eplist) {
            var episode = ep
            println("\n")
            episode.anime = a
            getAnimeStream(episode)?.let {
                episode = it
            }
            if (episode.subtype == null) episode.subtype = phrase.subType
            //update this ep to db
            updateEpisodetoDB(episode)
        }
    }

    /*companion object {

        var db: DB? = null

        @Throws(Exception::class)
        fun getInstance(link: String): Web? {
            val webBase = ParseURL.getBase(link)
            if (db == null) db = DB.getAnigoo("anigoo", true)
            if (!db!!.select("name", "anime_source_website", "base = '$webBase'")) return null
            val webname = db!!.firstKq.get(0)
            when (webname) {
                "anime47" -> return Anime47()
                "animevsub" -> return AnimeVsub()
                "animehay" -> return AnimeHay()
                "animetvn" -> return AnimeTVN()
                "gogoanime" -> return GoGoAnime()
                else -> throw AssertionError()
            }
        }
    }*/
}

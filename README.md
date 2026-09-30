# Videra

**Videra** is a premium, ultra-lightweight Android media aggregator built entirely with Kotlin, Jetpack Compose, and AndroidX Media3. 

Unlike traditional open-source media apps that bundle fragile, hard-coded web scrapers, Videra acts as a beautifully designed **empty shell**. It utilizes a **Web-Addon Architecture** (similar to Stremio)—meaning it consumes media catalogs and streams via standard JSON APIs provided by the user. 

### Why Videra?
* 🛡️ **DMCA-Bulletproof:** Videra contains absolutely zero scraping logic, copyrighted material, or piracy links. It is a neutral video player and JSON consumer.
* 📱 **The "Shorts" Engine:** The first open-source aggregator to natively support both horizontal cinematic media (Movies/Anime) AND a highly memory-efficient, zero-buffering vertical swiping feed for micro-dramas (DramaBox/Reels style).
* 🪶 **Zero Bloat:** No XML. No heavy dependency injection frameworks. Just pure Jetpack Compose and ExoPlayer resulting in a tiny APK and buttery-smooth edge-to-edge UI.
* 🔄 **Dynamic Failover:** Built-in OkHttp interceptors automatically route traffic to backup APIs if a primary Add-on goes down, ensuring zero downtime for users.
